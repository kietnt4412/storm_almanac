import { create } from 'zustand';

/**
 * Which game the reader is looking at, and how each game looks (C2.5, the
 * maintainer's idea of 2026-10-01: a switch between the games in place of the
 * light / dark switch, re-dressing the whole site for the game chosen).
 *
 * <p><b>The look is presentation data, and this is the only place the client
 * names a game</b> ([ADR 0039](../../../docs/adr/0039-a-games-look-is-presentation-data-in-the-client.md),
 * [ADR 0040](../../../docs/adr/0040-a-games-look-is-a-drawing-a-wordmark-and-a-dress.md)).
 * Nothing here branches: a game's id picks a palette in `index.css` and a
 * dress in `looks.css` by the `data-game` attribute; its wordmark and its
 * emblem kind are data the components draw. A game with no entry gets the
 * Storm palette and the bolt, which is what the synthetic Proving Ground and
 * any future title get until somebody writes it one. No game art (CLAUDE.md):
 * the marks and wordmarks are our own drawing and type, not the games' icons
 * or logos.
 *
 * <p><b>"Coming soon" is the one thing the server cannot say.</b> A game is
 * offered once the server publishes it; a look marked `upcoming` is listed,
 * greyed, while it does not — the maintainer's choice for Reverse: 1999 on
 * production, whose data is not first-hand yet and so is not published.
 *
 * <p><b>The choice lives in `localStorage`, beside the panel's</b>, for the same
 * reason the theme did: it is how this browser likes the page, not the
 * reader's work, so it stays out of the persisted planner store and its
 * migrations. Every read and write is wrapped.
 */
export interface GameLook {
  id: string;
  /** The lettering in the mark, two to four characters. */
  mark: string;
  /** What the switch calls it before the server has said; the server's name wins once it has. */
  name: string;
  /** Listed as coming soon while the server does not publish it. */
  upcoming?: boolean;
  /**
   * The game's name set as a wordmark (2026-10-01, the maintainer's ask): a
   * large word and a small line under it, each letter animated in. Set in our
   * own type, in our own way, by `looks.css` under the game's `data-game` —
   * not the game's logo, which is game art (CLAUDE.md).
   */
  wordmark: { word: string; line: string };
  /** Which drawing `GameMark` makes in small places: a cut-corner plate, or a double-ruled seal. */
  emblem: 'plate' | 'seal';
}

export const LOOKS: GameLook[] = [
  {
    id: 'punishing-gray-raven',
    mark: 'PGR',
    name: 'Punishing: Gray Raven',
    wordmark: { word: 'PUNISHING', line: 'GRAY RAVEN' },
    emblem: 'plate',
  },
  {
    id: 'reverse-1999',
    mark: '1999',
    name: 'Reverse: 1999',
    upcoming: true,
    wordmark: { word: 'REVERSE', line: '1999' },
    emblem: 'seal',
  },
];

export function lookOf(game: string | null | undefined): GameLook | undefined {
  return LOOKS.find((look) => look.id === game);
}

const KEY = 'storm-almanac:game';

function read(): string | null {
  try {
    return window.localStorage.getItem(KEY);
  } catch {
    return null;
  }
}

function write(game: string): void {
  try {
    window.localStorage.setItem(KEY, game);
  } catch {
    // Remembering is a convenience. The choice still applies to this page.
  }
}

/**
 * Puts the game on the document, where `index.css` picks its palette. A game
 * with no look is no attribute, which is the Storm palette.
 */
export function applyGame(game: string | null): void {
  const root = document.documentElement;
  if (game && lookOf(game)) root.setAttribute('data-game', game);
  else root.removeAttribute('data-game');
}

/** The game this browser chose last, for the first paint. */
export function storedGame(): string | null {
  return read();
}

export const useGameChoice = create<{ chosen: string | null; choose: (game: string) => void }>((set) => ({
  chosen: read(),
  choose: (game) => {
    write(game);
    set({ chosen: game });
  },
}));
