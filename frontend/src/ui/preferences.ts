import { useCallback, useState } from 'react';

/**
 * How this browser likes the page: whether the side panel is hidden. (Which
 * game it shows is `gameChoice.ts`; the light / dark switch that lived here
 * until 2026-10-01 was replaced by the game switch, and the scheme now follows
 * the device.)
 *
 * <p><b>Not in the planner store, on purpose.</b> That store is persisted with a
 * version and a migration, because what it holds is the reader's unsent work;
 * a layout preference is not work, and bumping that version for one would put a
 * migration in the path of an outbox for no reason. So this is a plain
 * `localStorage` key, and every read and write is wrapped: a private window, a
 * blocked site or a full quota must leave the page rendering with the defaults,
 * never failing.
 */
const PANEL_KEY = 'storm-almanac:panel-hidden';

function read(key: string): string | null {
  try {
    return window.localStorage.getItem(key);
  } catch {
    return null;
  }
}

function write(key: string, value: string): void {
  try {
    window.localStorage.setItem(key, value);
  } catch {
    // Remembering is a convenience. The choice still applies to this page.
  }
}

export function usePanelHidden(): [boolean, (hidden: boolean) => void] {
  const [hidden, setHidden] = useState(() => read(PANEL_KEY) === 'true');
  const choose = useCallback((next: boolean) => {
    write(PANEL_KEY, String(next));
    setHidden(next);
  }, []);
  return [hidden, choose];
}
