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
        // Catalog pages are cache-first; anything player-specific is not cached.
        runtimeCaching: [
          {
            urlPattern: /^\/api\/catalog\//,
            handler: 'StaleWhileRevalidate',
            options: { cacheName: 'catalog' },
          },
        ],
      },
    }),
  ],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
});
