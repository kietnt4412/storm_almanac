// `vitest/config` rather than `vite` — it is a superset, and one config is one
// fewer thing to keep in sync than a vite.config.ts and a vitest.config.ts that
// have to agree about aliases and plugins forever.
import { defineConfig } from 'vitest/config';
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
        // A navigation to a path the server answers must reach the server.
        //
        // The generated worker answers every navigation with the cached
        // index.html, which is right for the app's own routes and wrong for
        // these: signing in *is* a navigation, to /oauth2/authorization/google
        // and back from the provider to /login/oauth2/code/google. Answered from
        // the cache, a returning reader's sign-in renders the app shell instead
        // of reaching Spring, and fails without an error anywhere — the first
        // visit works, because no worker is in control yet. Invisible until B5,
        // because sign-in had only ever run on the dev server, which registers
        // no worker at all. /api and /dev are here for the same reason: a
        // reader opening a route in the address bar gets the route.
        navigateFallbackDenylist: [/^\/api\//, /^\/oauth2\//, /^\/login\//, /^\/dev\//],
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
  // What the pipeline is allowed to assert about a screen, decided rather than
  // left to the next session — see TRACKER.md's N25.
  //
  // Component tests in jsdom, and deliberately not a headless browser. The
  // argument is the four defects a session found by driving a real browser:
  // a store selector that re-rendered forever, a focus order that was not the
  // order rows are drawn in, a goal screen that could not express the base of a
  // track, and `display: block` folding every table header into a column. The
  // first three are behaviour and a component test catches all three. The
  // fourth is layout, and **nothing short of a real browser will ever catch it**
  // — jsdom computes no layout, so a test asserting on it would assert on the
  // stylesheet's text rather than on what a reader sees.
  //
  // So the honest split is: behaviour is the pipeline's, appearance is a
  // person's. This does not replace driving the app before shipping a screen;
  // it stops the behavioural half of that work from having to be redone by hand
  // on every change.
  test: {
    environment: 'jsdom',
    // No `globals: true`. Every test imports `describe`/`it`/`expect` from
    // vitest by name, which keeps tsconfig's `types` list the small closed set
    // it is rather than needing an ambient entry to typecheck a test file —
    // and a half-configured global is how a suite ends up compiling in the
    // editor and not in the build.
    setupFiles: ['./src/test/setup.ts'],
    // Excluded because the same glob otherwise sweeps up the dependencies'
    // own suites once node_modules holds a testing library.
    include: ['src/**/*.test.{ts,tsx}'],
  },
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
