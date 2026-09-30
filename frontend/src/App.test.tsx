import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { App } from './App';

/**
 * The side panel and the theme (C2, agreed 2026-09-30). Both are choices this
 * browser remembers, and neither may be lost to a reload: a reader who picked
 * light on a dark phone and got dark back on the next visit would stop picking.
 */
describe('the shell', () => {
  beforeEach(() => {
    window.localStorage.clear();
    document.documentElement.removeAttribute('data-theme');
    vi.stubGlobal(
      'fetch',
      vi.fn(async (url: string) => {
        if (url === '/api/health') return ok({ status: 'ok', version: 'test' });
        // Signed out is an answer, and the shell must render around it.
        return new Response(JSON.stringify({ status: 401 }), {
          status: 401,
          headers: { 'Content-Type': 'application/problem+json' },
        });
      }),
    );
  });

  afterEach(() => vi.unstubAllGlobals());

  it('puts the chosen theme on the document and remembers it, and "system" leaves it to the device', async () => {
    renderShell();
    const user = userEvent.setup();

    expect(screen.getByRole('button', { name: 'Theme follows the system' })).toHaveAttribute('aria-pressed', 'true');

    await user.click(screen.getByRole('button', { name: 'Light theme' }));
    expect(document.documentElement).toHaveAttribute('data-theme', 'light');
    expect(window.localStorage.getItem('storm-almanac:theme')).toBe('light');

    await user.click(screen.getByRole('button', { name: 'Theme follows the system' }));
    expect(document.documentElement).not.toHaveAttribute('data-theme');
    expect(window.localStorage.getItem('storm-almanac:theme')).toBe('system');
  });

  it('comes back with the theme a reader chose last time', () => {
    window.localStorage.setItem('storm-almanac:theme', 'dark');
    renderShell();

    expect(screen.getByRole('button', { name: 'Dark theme' })).toHaveAttribute('aria-pressed', 'true');
    expect(document.documentElement).toHaveAttribute('data-theme', 'dark');
  });

  it('hides the panel to a rail and remembers it, keeping every link named', async () => {
    renderShell();
    const user = userEvent.setup();

    await user.click(screen.getByRole('button', { name: 'Hide menu' }));
    expect(window.localStorage.getItem('storm-almanac:panel-hidden')).toBe('true');
    expect(screen.getByRole('button', { name: 'Show menu' })).toBeInTheDocument();
    // The rail shows icons; the words stay for a screen reader.
    expect(screen.getByRole('link', { name: 'Plan' })).toHaveAttribute('href', '/plan');
  });
});

function renderShell() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={['/catalog']}>
        <Routes>
          <Route path="/" element={<App />}>
            <Route path="catalog" element={<p>catalog</p>} />
          </Route>
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

function ok(body: unknown): Response {
  return new Response(JSON.stringify(body), { status: 200, headers: { 'Content-Type': 'application/json' } });
}
