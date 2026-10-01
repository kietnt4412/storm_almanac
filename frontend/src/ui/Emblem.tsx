import { useId, type ReactElement, type ReactNode } from 'react';
import type { Rarity } from '../api/client';
import { lookOf } from './gameChoice';
import { tierColour } from './rarity';

/**
 * A face for something with none (C2.10, the maintainer's ask of 2026-10-01).
 * Game art may not be shipped (CLAUDE.md), so a construct was a name in a box
 * on every screen. This draws one from what the bundle already publishes: the
 * id seeds the drawing, the kind picks its family, the rarity colours the
 * frame, and the name gives the initials.
 *
 * <p><b>The same id always draws the same face</b>, on every screen and every
 * device: the seed is a hash of the id, never a random number, so a reader
 * learns to recognise it.
 *
 * <p><b>The frame is the game's own drawing style</b>, the one its mark uses
 * (`GameLook.emblem`): a cut-corner plate or a double-ruled seal, and the
 * Storm hexagon for a game with no look. Nothing here names a game (ADR 0039).
 */
export interface EmblemSubject {
  id: string;
  displayName: string;
  /** An entity's kind or an item's category: things of one kind share a family. */
  kind: string;
  rarity: Rarity;
}

export function Emblem({
  subject,
  ranks,
  game,
  size = 40,
  className = '',
}: {
  subject: EmblemSubject;
  /** Every rank among the things it is shown with, highest first, for the frame's colour. */
  ranks?: number[];
  game: string | null;
  size?: number;
  className?: string;
}): ReactElement {
  const clip = useId();
  const style = lookOf(game)?.emblem ?? 'hex';
  const rank = subject.rarity?.rank;
  const tone = ranks && rank !== undefined ? tierColour(rank, ranks) : 'var(--brand)';
  const tier = ranks && rank !== undefined ? ranks.indexOf(rank) : -1;
  const random = seeded(hash(subject.id));
  const kindFamily = hash(subject.kind) % FAMILIES.length;
  const family = FAMILIES[kindFamily]!;
  // A second, fainter motif picked by the id, never the kind's own: what
  // tells two constructs apart at a glance, where the first says they are both
  // constructs.
  const accent = FAMILIES[(kindFamily + 1 + (hash(subject.id) % (FAMILIES.length - 1))) % FAMILIES.length]!;
  const shape = FRAMES[style](random);

  return (
    <svg
      className={`emblem shrink-0 ${className}`.trim()}
      data-emblem={style}
      width={size}
      height={size}
      viewBox="0 0 64 64"
      aria-hidden="true"
      focusable="false"
      style={{ '--tone': tone } as React.CSSProperties}
    >
      <defs>
        <clipPath id={clip}>{shape.outline}</clipPath>
      </defs>
      <g className="emblem-ground">{shape.outline}</g>
      <g clipPath={`url(#${clip})`} className="emblem-motif">
        <g className="emblem-accent">{accent(random)}</g>
        {family(random)}
      </g>
      {shape.trim}
      <g className="emblem-frame">{shape.outline}</g>
      <text x="32" y={style === 'seal' ? 17.5 : 14.5} textAnchor="middle" className="emblem-serial" data-emblem={style}>
        {serial(subject.id, style)}
      </text>
      {tier >= 0 && tier < 4 && <Pips count={4 - tier} style={style} />}
      <text x="32" y="39.5" textAnchor="middle" className="emblem-initials" data-emblem={style}>
        {initials(subject.displayName)}
      </text>
    </svg>
  );
}

/**
 * The first letters of the first two words: "Helentine: Lacrimosa" is HL,
 * "EXP Pod (XL)" is EP. Punctuation separates words and is never a letter.
 */
export function initials(name: string): string {
  const words = name.split(/[^\p{L}\p{N}★]+/u).filter((word) => /^[\p{L}\p{N}]/u.test(word));
  return words
    .slice(0, 2)
    .map((word) => word.charAt(0))
    .join('')
    .toUpperCase();
}

/**
 * A short code off the id, stamped on the emblem: a serial on a plate or a
 * hexagon, a number on a seal. Decoration with a meaning — the same thing
 * always carries the same code.
 */
export function serial(id: string, style: 'plate' | 'seal' | 'hex'): string {
  const code = hash(`serial:${id}`).toString(36).toUpperCase().padStart(4, '0').slice(-4);
  return style === 'seal' ? `Nº ${(hash(`serial:${id}`) % 900) + 100}` : `${code.slice(0, 2)}-${code.slice(2)}`;
}

/** 32-bit FNV-1a: small, stable across engines, and spreads short ids well. */
export function hash(text: string): number {
  let value = 0x811c9dc5;
  for (let index = 0; index < text.length; index += 1) {
    value ^= text.charCodeAt(index);
    value = Math.imul(value, 0x01000193);
  }
  return value >>> 0;
}

/** Mulberry32: a few lines, and the same sequence from the same seed everywhere. */
function seeded(seed: number): () => number {
  let state = seed;
  return () => {
    state = (state + 0x6d2b79f5) >>> 0;
    let t = state;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

function between(random: () => number, low: number, high: number): number {
  return low + Math.floor(random() * (high - low + 1));
}

// ── Frames: the game's drawing style ─────────────────────────────────────────

type Frame = (random: () => number) => { outline: ReactNode; trim: ReactNode };

const FRAMES: Record<'plate' | 'seal' | 'hex', Frame> = {
  /** Cut corners, two always and the others by the seed, and a slash across. */
  plate: (random) => {
    const cut = (always: boolean) => (always || random() < 0.5 ? 9 : 0);
    const [tl, tr, br, bl] = [cut(true), cut(false), cut(true), cut(false)];
    const points = [
      [3 + tl, 3],
      [61 - tr, 3],
      [61, 3 + tr],
      [61, 61 - br],
      [61 - br, 61],
      [3 + bl, 61],
      [3, 61 - bl],
      [3, 3 + tl],
    ]
      .map(([x, y]) => `${x},${y}`)
      .join(' ');
    const slash = between(random, 30, 44);
    return {
      outline: <polygon points={points} />,
      trim: (
        <>
          <polygon points={`${slash},3 ${slash + 10},3 ${slash - 20},61 ${slash - 30},61`} className="emblem-slash" />
          <rect x="58" y={between(random, 18, 30)} width="3" height="12" className="emblem-tab" />
        </>
      ),
    };
  },
  /** A ring inside a ring, with ticks round the rim like a stamp. */
  seal: (random) => {
    const ticks = 12 + between(random, 0, 3) * 4;
    return {
      outline: <circle cx="32" cy="32" r="29" />,
      trim: (
        <>
          <circle cx="32" cy="32" r="24.5" className="emblem-inner" />
          {Array.from({ length: ticks }, (_, index) => {
            const angle = (index / ticks) * Math.PI * 2;
            return (
              <line
                key={index}
                x1={32 + Math.cos(angle) * 25.5}
                y1={32 + Math.sin(angle) * 25.5}
                x2={32 + Math.cos(angle) * 27.5}
                y2={32 + Math.sin(angle) * 27.5}
                className="emblem-tick"
              />
            );
          })}
        </>
      ),
    };
  },
  /** The Storm hexagon, with one edge marked. */
  hex: (random) => {
    const corners = Array.from({ length: 6 }, (_, index) => {
      const angle = (Math.PI / 3) * index - Math.PI / 2;
      return [32 + Math.cos(angle) * 29.5, 32 + Math.sin(angle) * 29.5];
    });
    const edge = between(random, 0, 5);
    const [a, b] = [corners[edge]!, corners[(edge + 1) % 6]!];
    return {
      outline: <polygon points={corners.map(([x, y]) => `${x},${y}`).join(' ')} />,
      trim: <line x1={a[0]} y1={a[1]} x2={b[0]} y2={b[1]} className="emblem-edge" />,
    };
  },
};

function Pips({ count, style }: { count: number; style: string }) {
  // Under the initials, centred: one pip for the quietest coloured rank, four for the highest.
  const width = count * 5 - 2;
  return (
    <g className="emblem-pips">
      {Array.from({ length: count }, (_, index) =>
        style === 'seal' ? (
          <circle key={index} cx={32 - width / 2 + index * 5 + 1.5} cy="48" r="1.5" />
        ) : (
          <rect key={index} x={32 - width / 2 + index * 5} y="46.5" width="3" height="3" transform={`rotate(45 ${32 - width / 2 + index * 5 + 1.5} 48)`} />
        ),
      )}
    </g>
  );
}

// ── Motifs: one family per kind, its variation from the id ───────────────────

type Family = (random: () => number) => ReactNode;

const FAMILIES: Family[] = [
  /** Blades from the centre, long and short in turn. */
  function rays(random) {
    const count = between(random, 3, 8);
    const turn = random() * Math.PI * 2;
    return Array.from({ length: count * 2 }, (_, index) => {
      const angle = turn + (index / (count * 2)) * Math.PI * 2;
      const reach = index % 2 === 0 ? 34 : 20;
      const spread = index % 2 === 0 ? 0.16 : 0.1;
      const point = (a: number, r: number) => `${32 + Math.cos(a) * r},${32 + Math.sin(a) * r}`;
      return (
        <polygon key={index} points={`32,32 ${point(angle - spread, reach)} ${point(angle + spread, reach)}`} />
      );
    });
  },
  /** Chevrons stacked up the middle. */
  function chevrons(random) {
    const count = between(random, 2, 4);
    const width = between(random, 18, 26);
    const top = 30 - count * 4.5;
    return Array.from({ length: count + 2 }, (_, index) => {
      const y = top + index * 9;
      return (
        <polyline
          key={index}
          points={`${32 - width},${y + 9} 32,${y} ${32 + width},${y + 9}`}
          className="emblem-line"
          strokeWidth={index === 0 ? 5 : 3}
        />
      );
    });
  },
  /** Rings, each broken at its own place. */
  function rings(random) {
    return [9, 16, 23, 30].map((radius) => {
      const start = random() * Math.PI * 2;
      const gap = 0.6 + random() * 1.2;
      const end = start + Math.PI * 2 - gap;
      const at = (a: number) => `${32 + Math.cos(a) * radius} ${32 + Math.sin(a) * radius}`;
      return (
        <path
          key={radius}
          d={`M ${at(start)} A ${radius} ${radius} 0 1 1 ${at(end)}`}
          className="emblem-line"
          strokeWidth={radius === 9 ? 4 : 2.5}
        />
      );
    });
  },
  /** A circuit: a walk across a grid of points, a dot at every stop. */
  function lattice(random) {
    const step = 11;
    const origin = 32 - step * 1.5;
    let [x, y] = [between(random, 0, 3), between(random, 0, 3)];
    const stops: [number, number][] = [[x, y]];
    for (let index = 0; index < 6; index += 1) {
      const horizontal = random() < 0.5;
      const delta = random() < 0.5 ? -1 : 1;
      if (horizontal) x = Math.min(3, Math.max(0, x + delta));
      else y = Math.min(3, Math.max(0, y + delta));
      stops.push([x, y]);
    }
    const at = ([cx, cy]: [number, number]) => [origin + cx * step, origin + cy * step];
    return (
      <>
        {Array.from({ length: 16 }, (_, index) => {
          const [cx, cy] = at([index % 4, Math.floor(index / 4)]);
          return <circle key={index} cx={cx} cy={cy} r="1.4" />;
        })}
        <polyline points={stops.map((stop) => at(stop).join(',')).join(' ')} className="emblem-line" strokeWidth="3" />
        {stops.map((stop, index) => {
          const [cx, cy] = at(stop);
          return <circle key={`s${index}`} cx={cx} cy={cy} r="3.2" />;
        })}
      </>
    );
  },
];
