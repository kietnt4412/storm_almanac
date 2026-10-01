import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { App } from './App';
import { useGameChoice } from './ui/gameChoice';

/**
 * The side panel (C2, agreed 2026-09-30) and the game switch (C2.5, 2026-10-01).
 * Both are choices this browser remembers, and neither may be lost to a reload:
 * a reader who picked a game and got another back on the next visit would stop
 * picking.
 */
describe('the shell', () => {
  beforeEach(() => {
    window.localStorage.clear();
    document.documentElement.removeAttribute('data-game');
    useGameChoice.setState({ chosen: null });
    vi.stubGlobal(
      'fetch',
      vi.fn(async (url: string) => {
        if (url === '/api/health') return ok({ status: 'ok', version: 'test' });
        if (url === '/api/games') {
          return ok({
            games: [game('punishing-gray-raven', 'Punishing: Gray Raven'), game('proving-ground', 'The Proving Ground')],
          });
        }
        // Signed out is an answer, and the shell must render around it.
        return new Response(JSON.stringify({ status: 401 }), {
          status: 401,
          headers: { 'Content-Type': 'application/problem+json' },
        });
      }),
    );
  });

  afterEach(() => vi.unstubAllGlobals());

  it('offers every published game and an upcoming one as coming soon, and dresses the page for the one picked', async () => {
    renderShell();
    const user = userEvent.setup();

    const group = await screen.findByRole('group', { name: 'Game' });
    const pgr = within(group).getByRole('button', { name: /Punishing: Gray Raven/ });
    const proving = within(group).getByRole('button', { name: /The Proving Ground/ });
    // Published nowhere this server knows, so listed and not offered.
    expect(within(group).getByText('Reverse: 1999')).toBeInTheDocument();
    expect(within(group).queryByRole('button', { name: /Reverse: 1999/ })).not.toBeInTheDocument();

    // Nothing chosen: the first published game, which has a look.
    expect(pgr).toHaveAttribute('aria-pressed', 'true');
    await vi.waitFor(() => expect(document.documentElement).toHaveAttribute('data-game', 'punishing-gray-raven'));

    // A game with no look keeps the Storm palette: no attribute at all.
    await user.click(proving);
    expect(proving).toHaveAttribute('aria-pressed', 'true');
    expect(document.documentElement).not.toHaveAttribute('data-game');
    expect(window.localStorage.getItem('storm-almanac:game')).toBe('proving-ground');
  });

  it('comes back with the game a reader chose last time', async () => {
    window.localStorage.setItem('storm-almanac:game', 'proving-ground');
    useGameChoice.setState({ chosen: 'proving-ground' });
    renderShell();

    const group = await screen.findByRole('group', { name: 'Game' });
    expect(within(group).getByRole('button', { name: /The Proving Ground/ })).toHaveAttribute('aria-pressed', 'true');
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

function game(id: string, displayName: string) {
  return {
    id,
    displayName,
    energyUnit: 'Serum',
    latest: { sequence: 1, label: '1.0', publishedAt: '2026-09-01T00:00:00Z', attribution: 'test' },
    dayBoundary: null,
  };
}
