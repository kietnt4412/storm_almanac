import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { App } from '../App';
import type { Profile } from '../api/client';
import { profileForGame } from '../profile';
import { usePlannerStore } from '../store/plannerStore';
import { EntityPage } from './EntityPage';

/**
 * The character page asks about its own game.
 *
 * <p>Found driving it on 2026-09-24: an account with an R1999 profile selected
 * opened a PGR construct's page, the page asked for her shortfall with the R1999
 * profile, the server answered 400 with R1999's own parse error, and the page
 * retried that 400 with no limit under "Working it out…". Two faults, and each
 * has a test here: which profile is asked with, and what a refusal looks like.
 */

const PGR = 'punishing-gray-raven';
const R1999_PROFILE: Profile = { id: 'r1999-profile', game: 'reverse-1999', region: 'global', displayName: 'Main' };
const PGR_PROFILE: Profile = { id: 'pgr-profile', game: PGR, region: 'global', displayName: 'Main' };

describe('which profile a page about one game asks with', () => {
  it('prefers the selected profile when it plays that game', () => {
    const second: Profile = { ...PGR_PROFILE, id: 'pgr-second' };
    expect(profileForGame([PGR_PROFILE, second], 'pgr-second', PGR)).toBe(second);
  });

  it('otherwise takes a profile that plays it, never the selected one of another game', () => {
    expect(profileForGame([R1999_PROFILE, PGR_PROFILE], R1999_PROFILE.id, PGR)).toBe(PGR_PROFILE);
  });

  it('is none when nothing plays it', () => {
    expect(profileForGame([R1999_PROFILE], R1999_PROFILE.id, PGR)).toBeNull();
  });
});

describe('the character page', () => {
  let calls: { method: string; url: string; body?: string }[];

  beforeEach(() => {
    calls = [];
    usePlannerStore.setState({ profileId: R1999_PROFILE.id, outbox: {}, rejected: [], lastSyncedAt: null });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('asks for the shortfall with the profile for the game on the page', async () => {
    serve({ profiles: [R1999_PROFILE, PGR_PROFILE], shortfall: () => ok(shortfall()) });
    renderPage();

    expect(await screen.findByRole('cell', { name: 'Cogs' })).toBeInTheDocument();
    const asked = calls.filter((call) => call.url.includes('/shortfall'));
    expect(asked.length).toBeGreaterThan(0);
    for (const call of asked) {
      expect(call.url).toContain(`/api/me/profiles/${PGR_PROFILE.id}/shortfall?game=${PGR}&`);
    }
    expect(calls.some((call) => call.url.includes(R1999_PROFILE.id))).toBe(false);
  });

  it('with no profile for the game, says so and offers one rather than asking with another', async () => {
    serve({ profiles: [R1999_PROFILE], shortfall: () => ok(shortfall()) });
    renderPage();

    const make = await screen.findByRole('button', { name: 'Make a Punishing: Gray Raven profile' });
    expect(screen.getByText(/You have no Punishing: Gray Raven profile/)).toBeInTheDocument();
    expect(calls.some((call) => call.url.includes('/shortfall'))).toBe(false);

    await userEvent.click(make);

    await waitFor(() => expect(calls.some((call) => call.method === 'POST')).toBe(true));
    const created = calls.find((call) => call.method === 'POST')!;
    expect(created.url).toBe('/api/me/profiles');
    expect(JSON.parse(created.body!)).toMatchObject({ game: PGR });
    await waitFor(() => expect(usePlannerStore.getState().profileId).toBe(PGR_PROFILE.id));
    // And it stays: the account is re-read, and nothing switches it back.
    await waitFor(() =>
      expect(calls.filter((call) => call.url === '/api/me' && call.method === 'GET').length).toBeGreaterThan(1),
    );
    expect(usePlannerStore.getState().profileId).toBe(PGR_PROFILE.id);
    expect(await screen.findByRole('cell', { name: 'Cogs' })).toBeInTheDocument();
  });

  it('shows a refused shortfall once, with the server\'s sentence, instead of retrying it', async () => {
    serve({
      profiles: [PGR_PROFILE],
      shortfall: () => problem(400, 'profile pgr-profile plays reverse-1999, not punishing-gray-raven'),
    });
    renderPage();

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Could not work this out: profile pgr-profile plays reverse-1999, not punishing-gray-raven',
    );
    expect(screen.queryByText('Working it out…')).not.toBeInTheDocument();
    expect(calls.filter((call) => call.url.includes('/shortfall'))).toHaveLength(1);
  });

  /**
   * Inside the real shell, not on its own. The shell re-selects the account's
   * first profile when the selected one is not in the account, and on its own
   * the page made a profile, selected it, and passed — while in a browser the
   * shell switched the selection straight back.
   */
  function renderPage() {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter initialEntries={[`/catalog/${PGR}/samantha`]}>
          <Routes>
            <Route path="/" element={<App />}>
              <Route path="catalog/:game/:entity" element={<EntityPage />} />
            </Route>
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>,
    );
  }

  /** A fake of the routes this page reads, answering by path. */
  function serve({ profiles, shortfall }: { profiles: Profile[]; shortfall: () => Response }) {
    let known = [...profiles];
    vi.stubGlobal(
      'fetch',
      vi.fn(async (url: string, init: RequestInit = {}) => {
        const method = init.method ?? 'GET';
        calls.push({ method, url, body: init.body as string | undefined });
        if (url === '/api/me' && method === 'GET') {
          return ok({ accountId: 'a', displayName: 'Reader', email: 'r@example.invalid', profiles: known });
        }
        if (url === '/api/me/profiles' && method === 'POST') {
          known = [...known, PGR_PROFILE];
          return ok(PGR_PROFILE);
        }
        if (url === '/api/health') return ok({ status: 'UP' });
        if (url === '/api/games') {
          return ok({ games: [{ id: PGR, displayName: 'Punishing: Gray Raven', energyUnit: 'Serum', latest: version }] });
        }
        if (url === `/api/games/${PGR}/entities/samantha`) return ok(entity);
        if (url === `/api/games/${PGR}/entities/samantha/upgrades`) return ok(upgrades);
        if (url.endsWith('/goals')) return ok({ profile: 'p', goals: [] });
        if (url.endsWith('/roster')) return ok({ profile: 'p', entities: {} });
        if (url.includes('/shortfall')) return shortfall();
        return problem(404, `no fake for ${method} ${url}`);
      }),
    );
  }
});

const version = { game: PGR, sequence: 10, label: 'Anchored in Faith', attribution: 'read from the client' };

const entity = {
  game: PGR,
  version,
  entity: {
    id: 'samantha',
    displayName: 'Samantha',
    kind: 'memory',
    rarity: { label: '5*', rank: 5 },
    tags: [],
    statCurves: [],
    skills: [],
    talents: [],
  },
};

const upgrades = {
  game: PGR,
  version,
  entity: entity.entity,
  steps: [
    {
      id: 'samantha-overclock-1',
      fromState: 'overclock-0',
      toState: 'overclock-1',
      costs: [{ item: 'cogs', displayName: 'Cogs', quantity: 150 }],
    },
  ],
  totalCost: [],
};

function shortfall() {
  return {
    profile: PGR_PROFILE.id,
    game: PGR,
    version: 10,
    versionLabel: 'Anchored in Faith',
    attribution: 'read from the client',
    entity: 'samantha',
    currentStates: [],
    targetState: 'overclock-1',
    alreadyMet: false,
    steps: ['samantha-overclock-1'],
    items: [{ item: 'cogs', displayName: 'Cogs', required: 150, owned: 0, missing: 150 }],
    complete: false,
  };
}

function ok(body: unknown): Response {
  return new Response(JSON.stringify(body), { status: 200, headers: { 'Content-Type': 'application/json' } });
}

function problem(status: number, detail: string): Response {
  return new Response(JSON.stringify({ status, detail }), {
    status,
    headers: { 'Content-Type': 'application/problem+json' },
  });
}
