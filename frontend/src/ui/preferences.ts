import { useCallback, useEffect, useState } from 'react';

/**
 * How this browser likes the page: the theme, and whether the side panel is
 * hidden.
 *
 * <p><b>Not in the planner store, on purpose.</b> That store is persisted with a
 * version and a migration, because what it holds is the reader's unsent work;
 * a colour preference is not work, and bumping that version for one would put a
 * migration in the path of an outbox for no reason. So these are two plain
 * `localStorage` keys, and every read and write is wrapped: a private window, a
 * blocked site or a full quota must leave the page rendering with the defaults,
 * never failing.
 */
export type ThemeChoice = 'system' | 'light' | 'dark';

const THEME_KEY = 'storm-almanac:theme';
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

export function storedTheme(): ThemeChoice {
  const value = read(THEME_KEY);
  return value === 'light' || value === 'dark' ? value : 'system';
}

/**
 * Puts the choice on the document, where the palette in `index.css` reads it.
 * "System" is the absence of the attribute, so the media query decides.
 */
export function applyTheme(choice: ThemeChoice): void {
  const root = document.documentElement;
  if (choice === 'system') root.removeAttribute('data-theme');
  else root.setAttribute('data-theme', choice);
}

export function useTheme(): [ThemeChoice, (choice: ThemeChoice) => void] {
  const [theme, setTheme] = useState<ThemeChoice>(storedTheme);
  useEffect(() => applyTheme(theme), [theme]);
  const choose = useCallback((choice: ThemeChoice) => {
    write(THEME_KEY, choice);
    setTheme(choice);
  }, []);
  return [theme, choose];
}

export function usePanelHidden(): [boolean, (hidden: boolean) => void] {
  const [hidden, setHidden] = useState(() => read(PANEL_KEY) === 'true');
  const choose = useCallback((next: boolean) => {
    write(PANEL_KEY, String(next));
    setHidden(next);
  }, []);
  return [hidden, choose];
}
