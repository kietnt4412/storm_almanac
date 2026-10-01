import type { ReactElement } from 'react';

/**
 * The pull planner's three pictures (C2.8, agreed 2026-10-01): where the
 * reader's counter stands against the wall, the chance by date up to the
 * close, and how many copies the pulls end with. Each says its numbers in words
 * on the same screen, so the drawings are hidden from a screen reader.
 */

/**
 * The counter as a half dial from none to the wall, filled to the reader's
 * count, live as they type it. A drawn wall (ADR 0023) is certain somewhere in
 * a range, so the range is a band on the dial and not one notch.
 */
export function PityDial({
  pulls,
  hardAt,
  drawnFrom,
}: {
  pulls: number;
  hardAt: number;
  drawnFrom: number | null;
}): ReactElement {
  const cx = 110;
  const cy = 112;
  const r = 88;
  const point = (share: number) => {
    const angle = Math.PI * (1 - Math.max(0, Math.min(1, share)));
    return [cx + r * Math.cos(angle), cy - r * Math.sin(angle)] as const;
  };
  const arc = (from: number, to: number) => {
    const [x1, y1] = point(from);
    const [x2, y2] = point(to);
    return `M${x1.toFixed(1)} ${y1.toFixed(1)} A${r} ${r} 0 0 1 ${x2.toFixed(1)} ${y2.toFixed(1)}`;
  };
  const share = Math.min(pulls, hardAt) / hardAt;
  const left = Math.max(0, hardAt - pulls);
  return (
    <div className="flex flex-col items-center">
      <svg viewBox="0 0 220 132" className="w-full max-w-[240px]" aria-hidden="true">
        <path d={arc(0, 1)} fill="none" stroke="var(--line)" strokeWidth="16" strokeLinecap="round" />
        {drawnFrom !== null && (
          <path d={arc(drawnFrom / hardAt, 1)} fill="none" stroke="var(--violet)" strokeOpacity="0.35" strokeWidth="16" />
        )}
        {share > 0 && (
          <path d={arc(0, share)} fill="none" stroke="var(--brand)" strokeWidth="16" strokeLinecap="round" />
        )}
        <text x={cx} y={cy - 12} textAnchor="middle" fontSize="30" fontWeight="700" fill="var(--ink)">
          {pulls}
        </text>
        <text x={cx} y={cy + 8} textAnchor="middle" fontSize="11" fill="var(--muted)">
          of {drawnFrom !== null ? `${drawnFrom}–${hardAt}` : hardAt}
        </text>
        <text x={cx - r} y={cy + 18} textAnchor="middle" fontSize="10" fill="var(--muted)">
          0
        </text>
        <text x={cx + r} y={cy + 18} textAnchor="middle" fontSize="10" fill="var(--muted)">
          {hardAt}
        </text>
      </svg>
      <p className="text-center text-sm">
        {left === 0 ? (
          <>The next pull is certain.</>
        ) : drawnFrom !== null ? (
          <>
            Certain somewhere in the next <b>{Math.max(0, drawnFrom - pulls)}</b> to <b>{left}</b> pulls.
          </>
        ) : (
          <>
            <b>{left}</b> more pull{left === 1 ? '' : 's'} to the guarantee.
          </>
        )}
      </p>
    </div>
  );
}

const WIDTH = 600;
const HEIGHT = 210;
const PAD = { top: 16, right: 14, bottom: 28, left: 40 };

/**
 * The chance by date: each day from today to the banner's close, the pulls that
 * day's income affords read off the chain (`byDay`, one call). The horizon the
 * reader asked is marked, and the first day it is certain, when it is.
 */
export function ChanceByDate({
  byDay,
  asked,
  closesAt,
  now = new Date(),
}: {
  byDay: { day: number; pulls: number; chance: number }[];
  asked: number;
  closesAt: string | null;
  now?: Date;
}): ReactElement | null {
  if (byDay.length < 2) return null;
  const last = byDay.length - 1;
  const plotW = WIDTH - PAD.left - PAD.right;
  const plotH = HEIGHT - PAD.top - PAD.bottom;
  const x = (day: number) => PAD.left + (day / last) * plotW;
  const y = (chance: number) => PAD.top + (1 - chance) * plotH;
  const line = byDay.map((point, index) => `${index === 0 ? 'M' : 'L'}${x(point.day).toFixed(1)},${y(point.chance).toFixed(1)}`);
  const certain = byDay.find((point) => point.chance >= 1);
  const at = byDay[Math.min(asked, last)]!;
  const date = (day: number) =>
    new Intl.DateTimeFormat(undefined, { day: 'numeric', month: 'short', timeZone: 'UTC' }).format(
      new Date(now.getTime() + day * 86_400_000),
    );
  const step = Math.max(1, Math.ceil(last / 5 / 7) * 7);
  const ticks: number[] = [];
  for (let day = 0; day <= last; day += step) ticks.push(day);

  return (
    <figure className="space-y-1">
      <svg viewBox={`0 0 ${WIDTH} ${HEIGHT}`} className="h-auto w-full" aria-hidden="true">
        {[0, 0.5, 1].map((level) => (
          <g key={level}>
            <line
              x1={PAD.left}
              x2={WIDTH - PAD.right}
              y1={y(level)}
              y2={y(level)}
              stroke="var(--line)"
              strokeDasharray={level === 0 ? undefined : '3 4'}
            />
            <text x={PAD.left - 8} y={y(level) + 4} textAnchor="end" fontSize="11" fill="var(--muted)">
              {level * 100}%
            </text>
          </g>
        ))}
        {ticks.map((day) => (
          <text key={day} x={x(day)} y={HEIGHT - 8} textAnchor="middle" fontSize="11" fill="var(--muted)">
            {date(day)}
          </text>
        ))}
        <path
          d={`${line.join(' ')} L${x(last)},${y(0)} L${x(0)},${y(0)} Z`}
          fill="var(--brand)"
          fillOpacity="0.12"
        />
        <path d={line.join(' ')} fill="none" stroke="var(--brand)" strokeWidth="2.5" strokeLinejoin="round" />
        {certain && certain.day > 0 && (
          <g>
            <line x1={x(certain.day)} x2={x(certain.day)} y1={PAD.top} y2={y(0)} stroke="var(--violet)" strokeDasharray="3 3" />
            <text x={x(certain.day) + 4} y={PAD.top + 10} fontSize="11" fill="var(--violet)">
              certain · {date(certain.day)}
            </text>
          </g>
        )}
        {closesAt && (
          <text x={x(last) - 4} y={y(0) - 6} textAnchor="end" fontSize="11" fill="var(--muted)">
            closes
          </text>
        )}
        <circle cx={x(at.day)} cy={y(at.chance)} r="5" fill="var(--brand)" stroke="var(--raised)" strokeWidth="2" />
      </svg>
      <figcaption className="text-sm">
        By <b>{date(at.day)}</b> ({at.pulls.toLocaleString()} pulls): <b>{chanceWords(at.chance)}</b>.{' '}
        {certain ? (
          <>
            Certain by <b>{date(certain.day)}</b>
            {closesAt ? ', before the banner closes' : ''}.
          </>
        ) : (
          <>
            By the {closesAt ? 'close' : 'end'}, {date(last)}: <b>{chanceWords(byDay[last]!.chance)}</b>.
          </>
        )}
      </figcaption>
    </figure>
  );
}

/**
 * How many copies the pulls afforded end with, exactly: none, one, two, and the
 * last as "or more" (`byCopies`, from one walk of the chain). Copy exchanges
 * are not modelled (N38), and the caption says so.
 */
export function CopiesBar({ byCopies }: { byCopies: number[] }): ReactElement | null {
  if (byCopies.length < 2) return null;
  const last = byCopies.length - 1;
  const colours = ['var(--muted)', 'var(--brand)', 'var(--violet)', 'var(--signal)'];
  const name = (k: number) => (k === 0 ? 'None' : k === last ? `${k}+` : String(k));
  const words = byCopies
    .map((chance, k) => `${k === 0 ? 'none' : k === last ? `${k} or more` : `exactly ${k}`}: ${chanceWords(chance)}`)
    .join(' · ');
  return (
    <div className="space-y-2">
      <div className="flex items-baseline justify-between gap-2">
        <h3 className="font-medium">How many copies</h3>
        <span className="muted text-xs">with the pulls you can afford</span>
      </div>
      <div className="flex h-7 overflow-hidden rounded-lg" aria-hidden="true">
        {byCopies.map((chance, k) =>
          chance > 0 ? (
            <span
              key={k}
              data-testid="copies-segment"
              className="grid place-items-center overflow-hidden whitespace-nowrap text-[11px] font-semibold"
              style={{ width: `${chance * 100}%`, background: colours[Math.min(k, colours.length - 1)], color: 'var(--on-brand)' }}
            >
              {chance >= 0.12 ? `${name(k)} · ${Math.round(chance * 100)}%` : ''}
            </span>
          ) : null,
        )}
      </div>
      <p className="text-sm">
        {words}.{' '}
        <span className="muted">Copies bought in the shop are not counted, so more copies are likelier than this says.</span>
      </p>
    </div>
  );
}

function chanceWords(chance: number): string {
  if (chance >= 1) return '100%';
  if (chance <= 0) return '0%';
  if (chance < 0.001) return 'under 0.1%';
  if (chance > 0.999) return 'over 99.9%';
  return `${(chance * 100).toFixed(1)}%`;
}
