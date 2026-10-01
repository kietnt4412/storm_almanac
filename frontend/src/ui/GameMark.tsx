import type { ReactElement } from 'react';
import { lookOf } from './gameChoice';
import { Icon } from './Icon';

/**
 * A game's mark: its lettering on its own colour, drawn here. Not the game's
 * icon, which is game art and may not be shipped (CLAUDE.md); the maintainer
 * chose lettering over it on 2026-10-01. A game with no look gets the Storm
 * bolt, which is the site's own mark.
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
    <span
      className="inline-flex shrink-0 items-center justify-center rounded-lg font-extrabold leading-none"
      style={{
        width: size,
        height: size,
        background: 'var(--brand)',
        color: 'var(--on-brand)',
        // Four characters need a smaller face than three to sit inside the square.
        fontSize: Math.round(size * (look.mark.length > 3 ? 0.3 : 0.34)),
        letterSpacing: '-0.02em',
        boxShadow: 'inset 0 -2px 0 rgb(0 0 0 / 18%)',
      }}
      aria-hidden="true"
    >
      {look.mark}
    </span>
  );
}
