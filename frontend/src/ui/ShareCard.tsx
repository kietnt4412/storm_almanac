import { forwardRef, type ReactElement } from 'react';
import type { EntitySummary, Plan } from '../api/client';
import facesCss from '../faces.css?raw';
import { Emblem } from './Emblem';
import { lookOf } from './gameChoice';
import { tierColour } from './rarity';
import { daysOf } from './time';

/**
 * The plan as a picture to share (C2.13, agreed 2026-10-01): 1200 × 630, the
 * size every chat app previews, in the game's dress — the emblem of who the
 * plan is for, what it costs, how long, and where it was worked out.
 *
 * <p><b>It says what the plan says and nothing else.</b> The cost, the days
 * and the runs are the plan's own numbers; the patch is the one it was solved
 * on. A share that rounded or dressed them up would be a claim the site never
 * made.
 *
 * <p>Drawn as SVG so the page shows exactly what will be sent, then turned
 * into a PNG by {@link toPng}: an image in a chat has no page's stylesheet, so
 * the palette is written into it as values.
 */
export interface ShareFace {
  entity: EntitySummary;
  ranks?: number[];
}

const W = 1200;
const H = 630;

export const ShareCard = forwardRef<
  SVGSVGElement,
  { plan: Plan; energyUnit: string; gameName: string; face?: ShareFace; others: number }
>(function ShareCard({ plan, energyUnit, gameName, face, others }, ref): ReactElement {
  const style = lookOf(plan.game)?.emblem ?? 'hex';
  const tone = face?.ranks && face.entity.rarity ? tierColour(face.entity.rarity.rank, face.ranks) : 'var(--brand)';
  const runs = plan.stages.reduce((sum, stage) => sum + stage.runs, 0);
  const steps = plan.payingFor?.length ?? 0;
  const title = face?.entity.displayName ?? `${steps} upgrades`;
  const lines = wrap(title.toUpperCase(), 18);
  const topStage = [...plan.stages].sort((a, b) => b.runs - a.runs)[0];

  return (
    <svg
      ref={ref}
      className="share-card"
      data-style={style}
      xmlns="http://www.w3.org/2000/svg"
      viewBox={`0 0 ${W} ${H}`}
      width={W}
      height={H}
      role="img"
      aria-label={`${title}: ${plan.totalEnergy.toLocaleString()} ${energyUnit}, ${daysOf(plan.etaDays)} days`}
      style={{ '--tone': tone } as React.CSSProperties}
    >
      <defs>
        <radialGradient id="share-glow" cx="22%" cy="50%" r="60%">
          <stop offset="0" style={{ stopColor: tone, stopOpacity: 0.42 }} />
          <stop offset="1" style={{ stopColor: tone, stopOpacity: 0 }} />
        </radialGradient>
        <pattern id="share-stripes" width="22" height="22" patternUnits="userSpaceOnUse" patternTransform="rotate(45)">
          <rect width="2" height="22" className="share-stripe" />
        </pattern>
      </defs>

      <rect width={W} height={H} className="share-ground" />
      <rect width={W} height={H} fill="url(#share-stripes)" />
      <rect width={W} height={H} fill="url(#share-glow)" />
      <rect x="0" y="0" width={W} height="10" style={{ fill: tone }} />
      {style === 'plate' && <polygon points={`${W - 260},${H} ${W - 160},${H} ${W},${H - 160} ${W},${H - 260}`} style={{ fill: tone }} opacity="0.85" />}
      {style === 'seal' && <rect x="24" y="34" width={W - 48} height={H - 58} rx="6" fill="none" style={{ stroke: tone }} strokeWidth="2" opacity="0.6" />}

      {/* Who the plan is for, large, in its ring. */}
      <g transform="translate(70 135)">
        <circle cx="180" cy="180" r="172" className="share-ring" />
        <circle cx="180" cy="180" r="172" fill="none" style={{ stroke: tone }} strokeWidth="8" strokeDasharray="60 22" opacity="0.9" />
        {face ? (
          <g transform="translate(40 40)">
            <Emblem subject={face.entity} ranks={face.ranks} game={plan.game} size={280} />
          </g>
        ) : (
          <text x="180" y="210" textAnchor="middle" className="share-big" fontSize="96">
            {steps}
          </text>
        )}
      </g>

      <g transform="translate(500 0)">
        <text x="0" y="96" className="share-label">
          {gameName.toUpperCase()} · MY PLAN
        </text>
        {lines.map((line, index) => (
          <text key={index} x="0" y={168 + index * 66} className="share-title" fontSize={lines.length > 1 ? 58 : 64}>
            {line}
          </text>
        ))}
        <text x="0" y={168 + lines.length * 66 - 18} className="share-muted" fontSize="26">
          {steps} upgrade{steps === 1 ? '' : 's'}
          {others > 0 ? ` · and ${others} more` : ''}
        </text>

        {[
          { label: energyUnit.toUpperCase(), value: plan.totalEnergy.toLocaleString(), accent: true },
          { label: 'DAYS', value: daysOf(plan.etaDays) },
          { label: 'RUNS', value: runs.toLocaleString() },
        ].map((tile, index) => (
          <g key={tile.label} transform={`translate(${index * 216} 370)`}>
            <rect width="200" height="118" rx="10" className="share-tile" />
            <text x="22" y="40" className="share-label" fontSize="20">
              {tile.label}
            </text>
            <text x="22" y="96" className="share-number" style={tile.accent ? { fill: tone } : undefined}>
              {tile.value}
            </text>
          </g>
        ))}

        {topStage && (
          <text x="0" y="540" className="share-muted" fontSize="24">
            Most runs: {topStage.runs.toLocaleString()} × {topStage.displayName ?? topStage.stage}
          </text>
        )}
      </g>

      <text x="70" y="96" className="share-label">
        STORM ALMANAC
      </text>
      <text x="70" y={H - 34} className="share-foot">
        storm-almanac.vercel.app
      </text>
      <text x={style === 'plate' ? W - 230 : W - 70} y={H - 34} textAnchor="end" className="share-foot">
        patch {plan.versionLabel} · v{plan.version}
      </text>
    </svg>
  );
});

/** Words onto lines of about `width` characters, two lines at most, the rest cut with an ellipsis. */
export function wrap(text: string, width: number): string[] {
  const lines: string[] = [];
  let line = '';
  for (const word of text.split(' ')) {
    if (line && (line + ' ' + word).length > width) {
      lines.push(line);
      line = word;
    } else {
      line = line ? `${line} ${word}` : word;
    }
  }
  if (line) lines.push(line);
  return lines.length <= 2 ? lines : [lines[0]!, `${lines[1]!}…`];
}

/** The palette tokens the card and its emblem use, read off the page as it is dressed now. */
const TOKENS = ['--ground', '--surface', '--raised', '--ink', '--muted', '--line', '--brand', '--rarity-1', '--rarity-2', '--rarity-3', '--rarity-4'];

/**
 * The card as a PNG. An image carries no stylesheet, so the page's colours are
 * resolved and written in, with the emblem's rules and the card's own.
 */
export async function toPng(svg: SVGSVGElement, scale = 1): Promise<Blob> {
  const root = getComputedStyle(document.documentElement);
  const vars = TOKENS.map((token) => `${token}:${root.getPropertyValue(token).trim()}`).join(';');
  const copy = svg.cloneNode(true) as SVGSVGElement;
  const sheet = document.createElementNS('http://www.w3.org/2000/svg', 'style');
  sheet.textContent = `svg.share-card{${vars}}${facesCss}`;
  copy.insertBefore(sheet, copy.firstChild);
  copy.removeAttribute('class');
  copy.setAttribute('class', 'share-card');
  const markup = new XMLSerializer().serializeToString(copy);
  const url = URL.createObjectURL(new Blob([markup], { type: 'image/svg+xml;charset=utf-8' }));
  try {
    const image = new Image();
    image.decoding = 'async';
    await new Promise<void>((resolve, reject) => {
      image.onload = () => resolve();
      image.onerror = () => reject(new Error('The card could not be drawn.'));
      image.src = url;
    });
    const canvas = document.createElement('canvas');
    canvas.width = W * scale;
    canvas.height = H * scale;
    const context = canvas.getContext('2d');
    if (!context) throw new Error('This browser cannot draw the card.');
    context.drawImage(image, 0, 0, canvas.width, canvas.height);
    return await new Promise<Blob>((resolve, reject) =>
      canvas.toBlob((blob) => (blob ? resolve(blob) : reject(new Error('The card could not be saved.'))), 'image/png'),
    );
  } finally {
    URL.revokeObjectURL(url);
  }
}
