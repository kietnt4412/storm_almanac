import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import type { Profile } from '../api/client';
import { Home, localPath } from './Home';

/**
 * Found in the maintainer's rehearsal on 2026-09-25: the form's defaults are the
 * first published game on "global", which is exactly the profile a reader with
 * one profile already has, and adding it answered "/api/me/profiles responded
 * 500". The server now refuses it by name; the form should not offer it at all.
 */

const PGR = 'punishing-gray-raven';
const THEL: Profile = { id: 'thel', game: PGR, region: 'global', displayName: 'Thel' };

describe('the add-a-profile form', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('says which profile already holds a game and server, and will not add a second', async () => {
    const posted = serve([THEL]);
    renderHome();

    expect(
      await screen.findByText('You already have a Punishing: Gray Raven profile on global: Thel.'),
    ).toBeInTheDocument();
    const add = screen.getByRole('button', { name: 'Add a profile' });
    expect(add).toBeDisabled();

    const server = screen.getByLabelText('Server');
    await userEvent.clear(server);
    await userEvent.type(server, 'cn');

    expect(screen.queryByText(/You already have/)).not.toBeInTheDocument();
    expect(add).toBeEnabled();
    expect(posted()).toBe(0);
  });

  it("names a profile's game as the game does, not by its id", async () => {
    serve([THEL]);
    renderHome();

    expect(await screen.findByText('Punishing: Gray Raven · global')).toBeInTheDocument();
    expect(screen.queryByText(/punishing-gray-raven · global/)).not.toBeInTheDocument();
  });

  it('goes back only to a path on this site', () => {
    expect(localPath('/inventory')).toBe('/inventory');
    expect(localPath('//evil.example')).toBeNull();
    expect(localPath('/\\evil.example')).toBeNull();
    expect(localPath('https://evil.example')).toBeNull();
    expect(localPath(null)).toBeNull();
  });

  it('offers the form as before when the place is free', async () => {
    serve([]);
    renderHome();

    expect(await screen.findByRole('button', { name: 'Add a profile' })).toBeEnabled();
    expect(screen.queryByText(/You already have/)).not.toBeInTheDocument();
  });
});

function serve(profiles: Profile[], saved?: unknown): () => number {
  let posts = 0;
  vi.stubGlobal(
    'fetch',
    vi.fn(async (url: string, init: RequestInit = {}) => {
      const method = init.method ?? 'GET';
      if (url === '/api/me' && method === 'GET') {
        return ok({ accountId: 'a', displayName: 'Reader', email: 'r@example.invalid', profiles });
      }
      if (url === '/api/me/profiles' && method === 'POST') {
        posts += 1;
        return ok(THEL);
      }
      if (saved && url === `/api/me/profiles/${THEL.id}/plan`) return ok(saved);
      if (url === '/api/games') {
        return ok({
          games: [
            {
              id: PGR,
              displayName: 'Punishing: Gray Raven',
              energyUnit: 'Serum',
              latest: { game: PGR, sequence: 10, label: 'Anchored in Faith', attribution: 'read from the client' },
            },
          ],
        });
      }
      return new Response(JSON.stringify({ status: 404, detail: `no fake for ${method} ${url}` }), {
        status: 404,
        headers: { 'Content-Type': 'application/problem+json' },
      });
    }),
  );
  return () => posts;
}

function renderHome() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <Home />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

function ok(body: unknown): Response {
  return new Response(JSON.stringify(body), { status: 200, headers: { 'Content-Type': 'application/json' } });
}

/**
 * The first note the five strangers who closed Phase 4 left: a profile could be
 * made and never managed. A profile on the wrong server blocked the right one.
 */
describe('managing a profile', () => {
  const ALT: Profile = { id: 'alt', game: PGR, region: 'asia', displayName: 'Alt' };

  afterEach(() => {
    vi.unstubAllGlobals();
    document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/';
  });

  it('asks before deleting, says what goes with it, and sends nothing on "Keep it"', async () => {
    const server = account([THEL, ALT]);
    renderHome();

    await userEvent.click(await screen.findByRole('button', { name: 'Delete Alt' }));

    expect(screen.getByText(/Its inventory, roster and goals go with it, on every device/)).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: 'Keep it' }));

    expect(server.writes).toEqual([]);
    expect(screen.getByText('Alt')).toBeInTheDocument();
  });

  it('deletes with the token a write needs, and the profile leaves the list', async () => {
    document.cookie = 'XSRF-TOKEN=token-from-the-server; path=/';
    const server = account([THEL, ALT]);
    renderHome();

    await userEvent.click(await screen.findByRole('button', { name: 'Delete Alt' }));
    await userEvent.click(screen.getByRole('button', { name: 'Delete it' }));

    await vi.waitFor(() => expect(screen.queryByText('Alt')).not.toBeInTheDocument());
    // A DELETE has no body, and the token used to go only with one.
    expect(server.writes).toEqual([{ method: 'DELETE', url: '/api/me/profiles/alt', token: 'token-from-the-server' }]);
    expect(screen.getByText('Thel')).toBeInTheDocument();
  });

  it('renames in place, and will not save a blank name', async () => {
    const server = account([THEL]);
    renderHome();

    await userEvent.click(await screen.findByRole('button', { name: 'Rename Thel' }));
    const name = screen.getByLabelText('New name for Punishing: Gray Raven · global');
    await userEvent.clear(name);
    expect(screen.getByRole('button', { name: 'Save' })).toBeDisabled();

    await userEvent.type(name, 'Main account');
    await userEvent.click(screen.getByRole('button', { name: 'Save' }));

    expect(await screen.findByText('Main account')).toBeInTheDocument();
    expect(server.writes).toEqual([
      { method: 'PATCH', url: '/api/me/profiles/thel', body: { displayName: 'Main account' }, token: '' },
    ]);
  });
});

interface Write {
  method: string;
  url: string;
  body?: unknown;
  token: string;
}

/** An account whose profiles a PATCH renames and a DELETE removes, as the server does. */
function account(initial: Profile[]): { writes: Write[] } {
  let profiles = [...initial];
  const writes: Write[] = [];
  vi.stubGlobal(
    'fetch',
    vi.fn(async (url: string, init: RequestInit = {}) => {
      const method = init.method ?? 'GET';
      const token = (init.headers as Record<string, string> | undefined)?.['X-XSRF-TOKEN'] ?? '';
      if (url === '/api/me' && method === 'GET') {
        return ok({ accountId: 'a', displayName: 'Reader', email: 'r@example.invalid', profiles });
      }
      if (url === '/api/games') {
        return ok({
          games: [
            {
              id: PGR,
              displayName: 'Punishing: Gray Raven',
              energyUnit: 'Serum',
              latest: { game: PGR, sequence: 15, label: 'Anchored in Faith', attribution: 'read from the client' },
            },
          ],
        });
      }
      const id = url.startsWith('/api/me/profiles/') ? url.slice('/api/me/profiles/'.length) : null;
      if (id && method === 'DELETE') {
        writes.push({ method, url, token });
        profiles = profiles.filter((profile) => profile.id !== id);
        return new Response(null, { status: 204 });
      }
      if (id && method === 'PATCH') {
        const body = JSON.parse(String(init.body)) as { displayName: string };
        writes.push({ method, url, body, token });
        profiles = profiles.map((profile) => (profile.id === id ? { ...profile, displayName: body.displayName } : profile));
        return ok(profiles.find((profile) => profile.id === id));
      }
      return new Response(JSON.stringify({ status: 404, detail: `no fake for ${method} ${url}` }), {
        status: 404,
        headers: { 'Content-Type': 'application/problem+json' },
      });
    }),
  );
  return { writes };
}

/**
 * C2: Home says where a profile's saved plan stands — what it costs and how
 * long — before the reader opens anything, and nothing for a profile never
 * planned.
 */
describe('the saved plan on Home', () => {
  afterEach(() => vi.unstubAllGlobals());

  it("shows the plan's cost in the game's energy, its days, and a way to it", async () => {
    serve([THEL], {
      request: { energyPerDay: 160, horizonDays: 30, objective: 'LEAST_ENERGY', reach: {} },
      savedAt: '2026-09-29T10:00:00Z',
      plan: {
        id: 'p1',
        profile: 'thel',
        game: PGR,
        version: 18,
        versionLabel: 'Anchored in Faith',
        attribution: '',
        objective: 'LEAST_ENERGY',
        stages: [],
        conversions: [],
        rewards: [],
        totalEnergy: 1230,
        etaDays: 28,
        shadowPrice: [],
        bindingStages: [],
        notes: [],
        computedAt: '2026-09-29T10:00:00Z',
      },
    });
    renderHome();

    expect(await screen.findByText('1,230')).toBeInTheDocument();
    expect(screen.getByText('Serum')).toBeInTheDocument();
    expect(screen.getByText('28.0')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Open plan →' })).toHaveAttribute('href', '/plan');
  });

  it('says nothing for a profile that has never been planned', async () => {
    serve([THEL]);
    renderHome();

    expect(await screen.findByText('Thel')).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Open plan →' })).not.toBeInTheDocument();
  });
});
