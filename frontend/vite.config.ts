import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { VitePWA } from 'vite-plugin-pwa';

// These users are on a phone, next to the game. Offline inventory editing is
// not a nice-to-have: bulk entry is where companion tools live or die, and it
// happens in whatever signal the player has at the time.
export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      manifest: {
        name: 'Storm Almanac',
        short_name: 'Almanac',
        start_url: '/',
        display: 'standalone',
        background_color: '#0E1A19',
        theme_color: '#0D6F68',
      },
      workbox: {
        // Published game data is cached; anything player-specific is not.
        //
        // Two things were wrong with this rule until the screens that depend on
        // it existed. It named `/api/catalog/`, which is not a route this API
        // has ever served — the catalog lives under `/api/games/`. And it was
        // anchored with `^`, which cannot match: workbox tests the pattern
        // against the full request URL, so an anchored path matches nothing at
        // all. Both were invisible because nothing was reading from the cache.
        //
        // Safe to cache because a published version is immutable: the only way
        // a response under this prefix changes is a new patch, which is a
        // different `?version=`. Nothing under `/api/me` is here, and that is
        // the line that matters — a stale inventory served from a service
        // worker is a plan computed against numbers the player has moved on
        // from.
        runtimeCaching: [
          {
            urlPattern: /\/api\/games\//,
            handler: 'StaleWhileRevalidate',
            options: { cacheName: 'game-data' },
          },
        ],
      },
    }),
  ],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
      // The development sign-in, which exists only on a backend started from
      // source — it is excluded from the deployable jar. Proxied for the same
      // reason /api is: a session cookie set on localhost:8080 is not a cookie
      // localhost:5173 sends back, so a sign-in across two origins would appear
      // to succeed and leave the page anonymous. Same-origin here is also what
      // the deployed shape will be, via a rewrite (see D1 in TRACKER.md).
      '/dev': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
});
