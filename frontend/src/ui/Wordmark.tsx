import type { CSSProperties, ReactElement } from 'react';
import { lookOf } from './gameChoice';

/**
 * A game's name as a wordmark (2026-10-01, the maintainer's ask): the big word,
 * a rule, and the small line under it — "PUNISHING / GRAY RAVEN",
 * "REVERSE / 1999". Every letter is its own span with its place in the
 * stagger, so `looks.css` can bring them in one at a time, each game its own
 * way, and keep the word alive after: a glitch for one, a sheen for the other.
 *
 * <p><b>Our own type, not the game's logo</b> (CLAUDE.md, no game art). The
 * shapes are system faces, skewed, cut and spaced here.
 *
 * <p>Nothing here branches on the game: the words are data in `gameChoice.ts`
 * and the look is the `data-game` attribute. A game with no look has no
 * wordmark, and the caller shows the site's name instead.
 *
 * <p>Keyed by game where it is used, so switching game plays it again. Said
 * once, whole, to a screen reader: the letters are for the eye.
 */
export function Wordmark({
  game,
  size = 'md',
  delay = 0,
}: {
  game: string | null;
  size?: 'sm' | 'md' | 'lg';
  /** When the first letter starts, in ms — for a wordmark that waits for something else to arrive first. */
  delay?: number;
}): ReactElement | null {
  const look = lookOf(game);
  if (!look) return null;
  const { word, line } = look.wordmark;
  return (
    <span
      className="wordmark"
      data-game={look.id}
      data-size={size}
      role="img"
      aria-label={look.name}
      style={{ '--wm-delay': `${delay}ms`, '--wm-count': word.length } as CSSProperties}
    >
      <span className="wm-word" aria-hidden="true">
        {Array.from(word).map((letter, index) => (
          <span key={index} className="wm-l" style={{ '--i': index } as CSSProperties}>
            {letter}
          </span>
        ))}
      </span>
      <span className="wm-rule" aria-hidden="true" />
      <span className="wm-line" aria-hidden="true">
        {Array.from(line).map((letter, index) => (
          <span key={index} className="wm-c" style={{ '--i': index } as CSSProperties}>
            {letter === ' ' ? ' ' : letter}
          </span>
        ))}
      </span>
    </span>
  );
}
