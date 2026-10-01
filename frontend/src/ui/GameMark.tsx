import type { ReactElement } from 'react';
import { lookOf } from './gameChoice';
import { Icon } from './Icon';

/**
 * A game's mark, for the small places a wordmark will not fit: the switch, the
 * pill the top bar becomes, the opening. Drawn here, in the game's own colours
 * by `data-game`. Not the game's icon, which is game art and may not be
 * shipped (CLAUDE.md). A game with no look gets the Storm bolt, which is the
 * site's own mark.
 *
 * <p>Two drawings, picked by the look's `emblem` rather than by which game it
 * is: a <b>plate</b> — cut corners, a slash through it, the lettering
 * stencilled on — and a <b>seal</b> — a double rule round the wordmark's first
 * letter in serif, the lettering spaced under it.
 */
export function GameMark({ game, size = 32 }: { game: string | null; size?: number }): ReactElement {
  const look = lookOf(game);
  if (!look) {
    return (
      <span
        className="inline-flex shrink-0 items-center justify-center rounded-lg"
        style={{
          width: size,
          height: size,
          background: 'color-mix(in srgb, var(--brand) 18%, transparent)',
          color: 'var(--brand)',
        }}
        aria-hidden="true"
      >
        <Icon name="bolt" size={Math.round(size * 0.56)} />
      </span>
    );
  }
  return (
    <svg
      className="game-mark shrink-0"
      data-game={look.id}
      width={size}
      height={size}
      viewBox="0 0 64 64"
      aria-hidden="true"
      focusable="false"
    >
      {look.emblem === 'plate' ? <Plate mark={look.mark} /> : <Seal letter={look.wordmark.word.charAt(0)} mark={look.mark} />}
    </svg>
  );
}

function Plate({ mark }: { mark: string }) {
  return (
    <>
      <polygon
        points="8,2 56,2 62,8 62,48 48,62 2,62 2,16"
        style={{ fill: 'var(--raised)', stroke: 'var(--brand)' }}
        strokeWidth="3"
      />
      <polygon points="40,2 56,2 22,62 6,62" style={{ fill: 'var(--brand)' }} opacity="0.9" />
      <polygon points="58,24 62,20 62,32 58,36" style={{ fill: 'var(--brand)' }} />
      <text
        x="32"
        y="40"
        textAnchor="middle"
        fontFamily="'Arial Black', 'Segoe UI Black', Impact, system-ui, sans-serif"
        fontWeight="900"
        fontStyle="italic"
        fontSize={mark.length > 3 ? 14 : 18}
        style={{ fill: 'var(--ink)' }}
      >
        {mark}
      </text>
      <rect x="10" y="47" width="22" height="2.5" style={{ fill: 'var(--ink)' }} opacity="0.55" />
    </>
  );
}

function Seal({ letter, mark }: { letter: string; mark: string }) {
  return (
    <>
      <rect x="2" y="2" width="60" height="60" rx="5" style={{ fill: 'var(--raised)', stroke: 'var(--brand)' }} strokeWidth="2.5" />
      <rect x="7" y="7" width="50" height="50" rx="2" fill="none" style={{ stroke: 'var(--brand)' }} strokeWidth="1" opacity="0.6" />
      <text
        x="32"
        y="39"
        textAnchor="middle"
        fontFamily="'Iowan Old Style', 'Palatino Linotype', Palatino, Georgia, serif"
        fontSize="32"
        style={{ fill: 'var(--ink)' }}
      >
        {letter}
      </text>
      <line x1="16" y1="44.5" x2="48" y2="44.5" style={{ stroke: 'var(--brand)' }} strokeWidth="1.2" />
      <text
        x="32.5"
        y="53.5"
        textAnchor="middle"
        fontFamily="'Iowan Old Style', 'Palatino Linotype', Palatino, Georgia, serif"
        fontSize="8.5"
        letterSpacing="2.2"
        style={{ fill: 'var(--brand)' }}
      >
        {mark}
      </text>
    </>
  );
}
