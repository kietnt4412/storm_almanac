import { useEffect, useRef } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getGames } from '../api/client';
import { GameMark } from './GameMark';
import { LOOKS, lookOf } from './gameChoice';
import { Wordmark } from './Wordmark';

/**
 * The first screen a browser sees (C2.9, the maintainer's idea of 2026-10-01):
 * which game do you play? Shown once, to a browser that has never chosen, and
 * the opening plays for the game picked — the entrance to that game.
 *
 * <p><b>It is what ends the flash of the wrong colours.</b> The page paints in
 * the stored game's palette before React renders (`main.tsx`), but nothing was
 * stored for a reader who never touched the switch, so every load painted Storm
 * and then turned into PGR once `/api/games` answered. A first visit that must
 * choose is a first visit that leaves a choice behind.
 *
 * <p><b>It does not wait for the server.</b> The games with a look are known
 * here, so they are offered at once; once the server answers, what it publishes
 * is offered and a look marked upcoming that it does not publish is greyed, as
 * in the switch. A server that cannot be reached leaves the looks offered: a
 * choice is how this browser likes the page, and is worth keeping either way.
 *
 * <p>Each option wears its own game's colours by `data-game`. Storm around them,
 * because no game is the page's yet.
 */
export function GamePicker({ onPick }: { onPick: (game: string) => void }) {
  const games = useQuery({ queryKey: ['games'], queryFn: getGames });
  const offered = games.data
    ? games.data.games.map((game) => ({ id: game.id, name: game.displayName }))
    : LOOKS.filter((look) => !look.upcoming).map((look) => ({ id: look.id, name: look.name }));
  const soon = LOOKS.filter((look) => look.upcoming && !offered.some((game) => game.id === look.id));

  const first = useRef<HTMLButtonElement>(null);
  useEffect(() => first.current?.focus(), []);

  return (
    <main className="picker" aria-labelledby="picker-title" data-testid="game-picker">
      <div className="picker-inner">
        <p className="label">Storm Almanac</p>
        <h1 id="picker-title" className="picker-title">
          Which game do you play?
        </h1>
        <p className="muted text-sm">The whole site is dressed for it. You can switch any time from the top bar.</p>

        <ul className="picker-options">
          {offered.map((game, index) => (
            <li key={game.id}>
              <button
                ref={index === 0 ? first : undefined}
                type="button"
                className="picker-option"
                data-game={lookOf(game.id) ? game.id : 'storm'}
                onClick={() => onPick(game.id)}
              >
                <GameMark game={game.id} size={56} />
                <span className="picker-option-name">
                  {lookOf(game.id) ? <Wordmark game={game.id} size="md" delay={200 + index * 150} /> : null}
                  <span className={lookOf(game.id) ? 'muted text-sm' : 'font-semibold'}>{game.name}</span>
                </span>
              </button>
            </li>
          ))}
          {soon.map((look) => (
            <li key={look.id}>
              <span className="picker-option picker-option-soon" data-game={look.id} aria-disabled="true">
                <GameMark game={look.id} size={56} />
                <span className="picker-option-name">
                  <span className="font-semibold">{look.name}</span>
                  <span className="text-[11px] font-medium uppercase tracking-wide">Coming soon</span>
                </span>
              </span>
            </li>
          ))}
        </ul>
      </div>
    </main>
  );
}
