import type { ReactElement } from 'react';

/**
 * The chance of the featured unit against pulls, with where this reader's pulls
 * run out marked on it (C2.3).
 *
 * <p>The one number the pull planner used to show is a single point on this
 * curve, and the curve says what the point cannot: whether a few more pulls
 * would buy a lot or nothing, where soft pity bends it, where the wall makes it
 * certain. Drawn by hand in SVG rather than with a chart library, because it is
 * one line, two gridlines and a marker, and a dependency for that is weight a
 * phone downloads for nothing.
 *
 * <p>The chart is a picture of numbers the text beside it already says, so to a
 * screen reader it is one sentence, not a table of 60 points.
 */
const WIDTH = 600;
const HEIGHT = 220;
const PAD = { top: 12, right: 12, bottom: 28, left: 40 };

export function ChanceCurve({
  curve,
  afforded,
}: {
  /** Chance within n pulls, for n from 0 to the worst case. */
  curve: number[];
  /** Pulls the reader can afford; may exceed the curve's end, which is certainty. */
  afforded: number;
}): ReactElement | null {
  if (curve.length < 2) return null;

  const last = curve.length - 1;
  const plotW = WIDTH - PAD.left - PAD.right;
  const plotH = HEIGHT - PAD.top - PAD.bottom;
  const x = (pulls: number) => PAD.left + (pulls / last) * plotW;
  const y = (chance: number) => PAD.top + (1 - chance) * plotH;

  const line = curve.map((chance, pulls) => `${pulls === 0 ? 'M' : 'L'}${x(pulls).toFixed(1)},${y(chance).toFixed(1)}`);
  const area = `${line.join(' ')} L${x(last)},${y(0)} L${x(0)},${y(0)} Z`;

  const at = Math.min(afforded, last);
  const chanceAt = curve[at] ?? 1;
  const ticks = xTicks(last);
  const markerRight = x(at) > WIDTH - 150;

  return (
    <svg
      viewBox={`0 0 ${WIDTH} ${HEIGHT}`}
      className="h-auto w-full"
      role="img"
      aria-label={`Chance rises from 0% with no pulls to 100% at ${last} pulls; your ${afforded} pulls reach ${label(chanceAt)}.`}
    >
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

      {ticks.map((pulls) => (
        <text key={pulls} x={x(pulls)} y={HEIGHT - 8} textAnchor="middle" fontSize="11" fill="var(--muted)">
          {pulls}
        </text>
      ))}
      <text x={WIDTH - PAD.right} y={HEIGHT - 8} textAnchor="end" fontSize="11" fill="var(--muted)" dy="-14">
        pulls
      </text>

      <path d={area} fill="var(--brand)" fillOpacity="0.12" />
      <path d={line.join(' ')} fill="none" stroke="var(--brand)" strokeWidth="2.5" strokeLinejoin="round" />

      <line x1={x(at)} x2={x(at)} y1={PAD.top} y2={y(0)} stroke="var(--violet)" strokeWidth="1.5" strokeDasharray="4 4" />
      <circle cx={x(at)} cy={y(chanceAt)} r="5" fill="var(--violet)" stroke="var(--raised)" strokeWidth="2" />
      <text
        x={x(at) + (markerRight ? -10 : 10)}
        y={Math.max(PAD.top + 12, y(chanceAt) - 10)}
        textAnchor={markerRight ? 'end' : 'start'}
        fontSize="12"
        fontWeight="600"
        fill="var(--violet)"
      >
        your {afforded} pull{afforded === 1 ? '' : 's'} · {label(chanceAt)}
      </text>
    </svg>
  );
}

/** Round pull counts along the bottom: every 10, 20 or 60 depending on length. */
function xTicks(last: number): number[] {
  const step = last <= 60 ? 10 : last <= 150 ? 20 : last <= 300 ? 60 : 120;
  const ticks: number[] = [];
  for (let pulls = 0; pulls <= last; pulls += step) ticks.push(pulls);
  if (ticks[ticks.length - 1] !== last && last - (ticks[ticks.length - 1] ?? 0) >= step / 2) ticks.push(last);
  return ticks;
}

function label(chance: number): string {
  if (chance >= 1) return '100%';
  if (chance <= 0) return '0%';
  return `${(chance * 100).toFixed(chance < 0.1 ? 1 : 0)}%`;
}
