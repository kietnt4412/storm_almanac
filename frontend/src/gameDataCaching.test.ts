import { describe, expect, it } from 'vitest';
import { gameDataCaching } from './gameDataCaching';

/** The rule workbox would pick for a URL: the first whose pattern matches it, as the worker does. */
const ruleFor = (url: string) => gameDataCaching.find((rule) => rule.urlPattern.test(url))?.handler;

describe('what the service worker does with game data', () => {
  const upgrades = 'https://storm-almanac.vercel.app/api/games/punishing-gray-raven/entities/selena-pianissimo/upgrades';

  it('asks the network first for the latest, so a new sequence reaches a returning reader', () => {
    expect(ruleFor(upgrades)).toBe('NetworkFirst');
  });

  it('serves a named version from the cache, because a published version never changes', () => {
    expect(ruleFor(`${upgrades}?version=15`)).toBe('StaleWhileRevalidate');
    expect(ruleFor('https://storm-almanac.vercel.app/api/games/punishing-gray-raven/items?q=orb&version=15')).toBe(
      'StaleWhileRevalidate',
    );
  });

  it('caches nothing that belongs to a player', () => {
    expect(ruleFor('https://storm-almanac.vercel.app/api/me/profiles/p/roster')).toBeUndefined();
    expect(ruleFor('https://storm-almanac.vercel.app/api/me/profiles/p/plan?version=15')).toBeUndefined();
  });
});
