import { fireEvent, render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { App } from './App';
import { useGameChoice } from './ui/gameChoice';

/**
 * The top bar (C2.6, 2026-10-01, in place of C2's side panel) and the game
 * switch in it (C2.5). The game is a choice this browser remembers, and it may
 * not be lost to a reload: a reader who picked a game and got another back on
 * the next visit would stop picking.
 */
describe('the shell', () => {
  beforeEach(() => {
    freshBrowser();
    stubServer();
    // A browser that has chosen before; one that never has is asked first (below).
    remember('punishing-gray-raven');
  });

  afterEach(() => vi.unstubAllGlobals());

  it('offers every published game and an upcoming one as coming soon, and dresses the page for the one picked', async () => {
    renderShell();
    const user = userEvent.setup();

    const group = await openGameMenu(user);
    const pgr = within(group).getByRole('button', { name: /Punishing: Gray Raven/ });
    const proving = within(group).getByRole('button', { name: /The Proving Ground/ });
    // Published nowhere this server knows, so listed and not offered.
    expect(within(group).getByText('Reverse: 1999')).toBeInTheDocument();
    expect(within(group).queryByRole('button', { name: /Reverse: 1999/ })).not.toBeInTheDocument();

    expect(pgr).toHaveAttribute('aria-pressed', 'true');
    await vi.waitFor(() => expect(document.documentElement).toHaveAttribute('data-game', 'punishing-gray-raven'));

    // A game with no look keeps the Storm palette: no attribute at all.
    await user.click(proving);
    expect(proving).toHaveAttribute('aria-pressed', 'true');
    expect(document.documentElement).not.toHaveAttribute('data-game');
    expect(window.localStorage.getItem('storm-almanac:game')).toBe('proving-ground');
  });

  it('comes back with the game a reader chose last time', async () => {
    remember('proving-ground');
    renderShell();

    const group = await openGameMenu(userEvent.setup());
    expect(within(group).getByRole('button', { name: /The Proving Ground/ })).toHaveAttribute('aria-pressed', 'true');
  });

  it('puts the four planner steps under one menu, which Escape closes', async () => {
    renderShell();
    const user = userEvent.setup();

    const menu = screen.getByRole('navigation', { name: 'Menu' });
    expect(within(menu).getByRole('link', { name: 'Pulls' })).toHaveAttribute('href', '/pulls');
    expect(within(menu).queryByRole('link', { name: /Plan/ })).not.toBeInTheDocument();

    await user.click(within(menu).getByRole('button', { name: 'Planner' }));
    for (const [name, href] of [
      ['Inventory', '/inventory'],
      ['Roster', '/roster'],
      ['Goals', '/goals'],
      ['Plan', '/plan'],
    ]) {
      expect(within(menu).getByRole('link', { name: new RegExp(`^${name}`) })).toHaveAttribute('href', href);
    }

    await user.keyboard('{Escape}');
    expect(within(menu).queryByRole('link', { name: /^Inventory/ })).not.toBeInTheDocument();
  });

  it('gathers the bar into a pill once the page scrolls, and spreads it out again at the top', async () => {
    renderShell();
    const bar = screen.getByRole('banner');
    expect(bar).toHaveAttribute('data-scrolled', 'false');

    scrollTo(200);
    await vi.waitFor(() => expect(bar).toHaveAttribute('data-scrolled', 'true'));
    scrollTo(0);
    await vi.waitFor(() => expect(bar).toHaveAttribute('data-scrolled', 'false'));
  });

  it("shows the reader's Google picture, and their initial when it will not load", async () => {
    const signedOut = vi.mocked(fetch).getMockImplementation()!;
    vi.mocked(fetch).mockImplementation(async (url) =>
      url === '/api/me'
        ? ok({
            accountId: 'a1',
            displayName: 'Vertin',
            email: 'v@example.com',
            pictureUrl: 'https://example.invalid/vertin.png',
            profiles: [],
          })
        : signedOut(url),
    );
    renderShell();

    const account = await screen.findByRole('button', { name: 'Account' });
    const picture = account.querySelector('img');
    expect(picture).toHaveAttribute('src', 'https://example.invalid/vertin.png');
    // Google's photo host refuses some requests that say which site asked.
    expect(picture).toHaveAttribute('referrerpolicy', 'no-referrer');

    fireEvent.error(picture!);
    expect(account.querySelector('img')).toBeNull();
    expect(within(account).getByText('V')).toBeInTheDocument();
  });
});

/**
 * The first screen (C2.9, 2026-10-01): a browser that has never chosen a game
 * is asked before anything else, so that every later load paints in the right
 * game from its first frame instead of Storm and then the game.
 */
describe('the first screen', () => {
  beforeEach(() => {
    freshBrowser();
    stubServer();
  });

  afterEach(() => vi.unstubAllGlobals());

  it('asks a browser that never chose, remembers the answer, and opens on that game', async () => {
    renderShell();
    const user = userEvent.setup();

    expect(screen.getByRole('heading', { name: 'Which game do you play?' })).toBeInTheDocument();
    expect(screen.queryByRole('banner')).not.toBeInTheDocument();
    // Upcoming and not published: shown, and not something to press.
    expect(screen.getByText('Coming soon')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Reverse: 1999/ })).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /Punishing: Gray Raven/ }));

    expect(window.localStorage.getItem('storm-almanac:game')).toBe('punishing-gray-raven');
    expect(screen.getByRole('banner')).toBeInTheDocument();
    expect(screen.queryByTestId('game-picker')).not.toBeInTheDocument();
    await vi.waitFor(() => expect(document.documentElement).toHaveAttribute('data-game', 'punishing-gray-raven'));
  });

  it('offers the games it knows when the server cannot be reached, and keeps the one picked', async () => {
    vi.mocked(fetch).mockImplementation(() => Promise.reject(new TypeError('Failed to fetch')));
    renderShell();

    await userEvent.setup().click(screen.getByRole('button', { name: /Punishing: Gray Raven/ }));
    // With no list to check it against, the choice is trusted, not dropped for Storm.
    await screen.findByText(/Can't reach the server/);
    expect(document.documentElement).toHaveAttribute('data-game', 'punishing-gray-raven');
  });

  it('never stands in front of a link into one game', () => {
    renderShell('/catalog/punishing-gray-raven');

    expect(screen.queryByTestId('game-picker')).not.toBeInTheDocument();
    expect(screen.getByRole('banner')).toBeInTheDocument();
  });

  it('is not asked again once answered', () => {
    remember('punishing-gray-raven');
    renderShell();

    expect(screen.queryByTestId('game-picker')).not.toBeInTheDocument();
  });
});

function freshBrowser() {
  window.localStorage.clear();
  document.documentElement.removeAttribute('data-game');
  useGameChoice.setState({ chosen: null });
}

function remember(game: string) {
  window.localStorage.setItem('storm-almanac:game', game);
  useGameChoice.setState({ chosen: game });
}

function stubServer() {
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
}

async function openGameMenu(user: ReturnType<typeof userEvent.setup>) {
  await user.click(await screen.findByRole('button', { name: 'Game' }));
  return screen.findByRole('group', { name: 'Game' });
}

function scrollTo(y: number) {
  Object.defineProperty(window, 'scrollY', { value: y, configurable: true });
  window.dispatchEvent(new Event('scroll'));
}

function renderShell(path = '/catalog') {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/" element={<App />}>
            <Route path="catalog" element={<p>catalog</p>} />
            <Route path="catalog/:game" element={<p>catalog</p>} />
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
