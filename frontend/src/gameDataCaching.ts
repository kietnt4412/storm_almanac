/**
 * How the service worker caches published game data. Here rather than inline in
 * vite.config.ts so a test can hold the rules against real URLs — the first
 * version of this rule matched nothing and the second matched too much, and
 * neither was visible from the config.
 *
 * <p><b>Only a version named in the URL is immutable.</b> `?version=7` is the
 * same bytes forever, so it is served from the cache and refreshed behind the
 * reader. A URL with no version means "the latest", which changes every time a
 * sequence is published — and until 2026-09-28 it was cached the same way, so a
 * browser that had visited before kept naming a construct's tracks from the
 * sequence it first saw. The plan beside them came from sequence 15 and the
 * tracks from before sequence 11: no section headings, no orb tags, and skill
 * levels 5–17 printed as ids because the old graph had no such states. Found by
 * the strangers who closed Phase 4.
 *
 * <p>So the latest goes to the network first and falls back to the cache only
 * when there is no network, which is what the offline promise needs and all it
 * needs. Nothing under `/api/me` is cached at all: a stale inventory served
 * from a worker is a plan computed against numbers the player has moved on from.
 *
 * <p>Order matters: workbox takes the first rule that matches, and it tests the
 * pattern against the full URL, query included — which is also why neither
 * pattern is anchored with `^`.
 */
export const gameDataCaching = [
  {
    urlPattern: /\/api\/games\/.*[?&]version=\d+/,
    handler: 'StaleWhileRevalidate' as const,
    options: { cacheName: 'game-data-versioned' },
  },
  {
    urlPattern: /\/api\/games\//,
    handler: 'NetworkFirst' as const,
    options: { cacheName: 'game-data-latest' },
  },
];
