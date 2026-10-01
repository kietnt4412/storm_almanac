import { useEffect, useState } from 'react';
import { GameMark } from './GameMark';
import { lookOf } from './gameChoice';
import { prefersMotion } from './motion';
import { Wordmark } from './Wordmark';

/**
 * The opening (C2.6, agreed 2026-10-01: once per browser session): the mark
 * turns in, the name slides out from behind it, then the page rises over both
 * from the bottom edge. For a game with a look, the name is the game's
 * wordmark, letter by letter, with the site's name under it — so the opening
 * holds a little longer, for the letters to land.
 *
 * <p><b>Once per session, not once per visit to Home.</b> This is a tool people
 * return to through the day; an opening on every navigation would stand between
 * a reader and the plan they came for. `sessionStorage` is the right lifetime —
 * a new tab tomorrow plays it, a reload in the middle of an edit does not — and
 * a browser that will not store it simply plays it again.
 *
 * <p><b>Any key or click skips it</b>, straight to the wipe, and it is never
 * shown to a reader who asked their system for less motion. It is
 * `aria-hidden`: the name it shows is the page's own, which a screen reader
 * hears from the page.
 *
 * <p>While it plays, the root carries `data-intro`, which holds every entrance
 * animation on the page paused — so the hero's words rise as the curtain lifts,
 * not behind it.
 */
const KEY = 'storm-almanac:intro-seen';
/** When the curtain starts to lift, and how long it takes. */
const HOLD_MS = 1500;
const HOLD_WORDMARK_MS = 2100;
const WIPE_MS = 650;

export function shouldPlayIntro(): boolean {
  if (!prefersMotion()) return false;
  try {
    return window.sessionStorage.getItem(KEY) === null;
  } catch {
    return true;
  }
}

function remember(): void {
  try {
    window.sessionStorage.setItem(KEY, '1');
  } catch {
    // Not remembered means it plays again next load, which is all it costs.
  }
}

export function Intro({ game }: { game: string | null }) {
  const [phase, setPhase] = useState<'off' | 'show' | 'wipe'>(() => (shouldPlayIntro() ? 'show' : 'off'));
  const hold = lookOf(game) ? HOLD_WORDMARK_MS : HOLD_MS;

  useEffect(() => {
    if (phase === 'off') return;
    const root = document.documentElement;
    if (phase === 'show') {
      root.setAttribute('data-intro', 'on');
      remember();
      const skip = () => setPhase('wipe');
      const timer = window.setTimeout(skip, hold);
      window.addEventListener('keydown', skip);
      window.addEventListener('pointerdown', skip);
      return () => {
        window.clearTimeout(timer);
        window.removeEventListener('keydown', skip);
        window.removeEventListener('pointerdown', skip);
      };
    }
    // The page's entrances start with the wipe, so they are moving as it is uncovered.
    root.removeAttribute('data-intro');
    const timer = window.setTimeout(() => setPhase('off'), WIPE_MS);
    return () => window.clearTimeout(timer);
  }, [phase, hold]);

  // Never leave the page's entrances paused behind an opening that is gone.
  useEffect(() => () => document.documentElement.removeAttribute('data-intro'), []);

  if (phase === 'off') return null;
  return (
    <div className={`intro ${phase === 'wipe' ? 'intro-wipe' : ''}`} aria-hidden="true" data-testid="intro">
      <div className="intro-lockup">
        <span className="intro-mark">
          <GameMark game={game} size={64} />
        </span>
        {lookOf(game) ? (
          <span className="intro-wordmark">
            <Wordmark game={game} size="lg" delay={350} />
            <span className="intro-site">Storm Almanac</span>
          </span>
        ) : (
          <span className="intro-name">
            <span>Storm Almanac</span>
          </span>
        )}
      </div>
      <span className="intro-skip">Press any key to skip</span>
    </div>
  );
}
