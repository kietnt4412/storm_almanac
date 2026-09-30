import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { Banner, Profile } from '../api/client';
import { usePlannerStore } from '../store/plannerStore';
import { percent, Pulls } from './Pulls';

/**
 * C1's screen: the reader's counter, their income, a horizon — and one answer
 * that says what it counted.
 */
describe('the pull planner', () => {
  const PGR = 'punishing-gray-raven';
  const PROFILE: Profile = { id: 'p', game: PGR, region: 'global', displayName: 'Main' };
  let sent: { method: string; url: string; body: unknown }[];

  beforeEach(() => {
    usePlannerStore.setState({ profileId: PROFILE.id, outbox: {}, reach: {}, rejected: [], lastSyncedAt: null });
    sent = [];
    serve();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('offers only open, priced banners, and asks only the ladders that pay for pulls', async () => {
    renderPulls();

    expect(await screen.findByText('Adelyde: Anabasis — Crucible Event Construct')).toBeInTheDocument();
    // The closed pool and the unpriced one would only be refused.
    expect(screen.queryByText(/Closed Pool/)).not.toBeInTheDocument();
    expect(screen.queryByText(/Unpriced Pool/)).not.toBeInTheDocument();

    // The daily missions pay only Black Cards; a plan-only ladder is the plan page's question.
    expect(await screen.findByLabelText('How far you get in Daily missions')).toBeInTheDocument();
    expect(screen.queryByLabelText('How far you get in Chores')).not.toBeInTheDocument();

    // A pool that hands the featured unit over on every hit has no loss to report.
    expect(screen.queryByText(/was not the featured unit/)).not.toBeInTheDocument();
    expect(screen.getByText(/certain by pull 60/)).toBeInTheDocument();
  });

  it('saves the counter as part of asking, then says what the answer counted', async () => {
    renderPulls();

    const since = await screen.findByLabelText('Pulls since your last S');
    await userEvent.clear(since);
    await userEvent.type(since, '45');
    await userEvent.selectOptions(screen.getByLabelText('How far you get in Daily missions'), '100');
    await userEvent.click(screen.getByRole('button', { name: 'Work it out' }));

    // The headline, and the curve it is one point on, marked where the pulls run out.
    expect(await screen.findByText('100%', { selector: 'p' })).toBeInTheDocument();
    expect(screen.getByRole('img', { name: /your 15 pulls reach 100%/ })).toBeInTheDocument();

    const put = sent.find((request) => request.method === 'PUT');
    expect(put?.body).toEqual({ pullsSinceHit: 45, consecutiveLosses: 0 });
    const asked = sent.find((request) => request.method === 'POST');
    expect(asked?.body).toMatchObject({ banner: 'pgr-adelyde-anabasis', copies: 1, reach: { 'daily-missions': 100 } });

    expect(screen.getByText(/Includes 3,750 Black Card held/)).toBeInTheDocument();
    expect(screen.getByText(/you can afford at 250 Event Construct R&D Ticket each: 3,750 held/)).toBeInTheDocument();
    expect(screen.getByText(/make it certain/)).toBeInTheDocument();
  });

  it('writes a chance as a reader would say it, never as a false certainty', () => {
    expect(percent(1)).toBe('100%');
    expect(percent(0)).toBe('0%');
    expect(percent(0.0004)).toBe('under 0.1%');
    expect(percent(0.9996)).toBe('over 99.9%');
    expect(percent(0.4137)).toBe('41.4%');
  });

  function renderPulls() {
    render(
      <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
        <MemoryRouter>
          <Pulls />
        </MemoryRouter>
      </QueryClientProvider>,
    );
  }

  function serve() {
    const version = { sequence: 17, label: 'Anchored in Faith', attribution: 'read from the client' };
    const banner = (id: string, displayName: string, overrides: Partial<Banner> = {}): Banner => ({
      id,
      displayName,
      bannerType: 'crucible-event-construct',
      headline: { label: 'S', rank: 3 },
      baseRate: 0.005,
      hardAt: 60,
      drawnFrom: null,
      featuredChance: 1,
      guaranteeAfterLoss: 1,
      worstCasePulls: 60,
      pullPrice: { currency: 'event-construct-rd-ticket', currencyName: 'Event Construct R&D Ticket', perPull: 250 },
      opensAt: null,
      closesAt: '2099-01-01T00:00:00Z',
      open: true,
      ...overrides,
    });
    const pity = (pullsSinceHit: number) => ({
      banner: 'pgr-adelyde-anabasis',
      scopeKey: 'type:crucible-event-construct',
      pullsSinceHit,
      consecutiveLosses: 0,
      guaranteedNext: true,
      hardAt: 60,
    });

    vi.stubGlobal(
      'fetch',
      vi.fn(async (url: string, init?: RequestInit) => {
        const method = init?.method ?? 'GET';
        sent.push({ method, url, body: init?.body ? JSON.parse(String(init.body)) : undefined });

        if (url === '/api/me') {
          return ok({ accountId: 'a', displayName: 'Reader', email: 'r@example.invalid', profiles: [PROFILE] });
        }
        if (url === `/api/games/${PGR}/banners`) {
          return ok({
            game: PGR,
            version,
            banners: [
              banner('pgr-adelyde-anabasis', 'Adelyde: Anabasis — Crucible Event Construct'),
              banner('closed', 'Closed Pool', { open: false }),
              banner('unpriced', 'Unpriced Pool', { pullPrice: null }),
            ],
          });
        }
        if (url === `/api/games/${PGR}/measures`) {
          return ok({
            game: PGR,
            version,
            measures: [
              {
                measure: 'daily-missions',
                displayName: 'Daily missions',
                paysForPulls: true,
                paysForPlans: false,
                bars: [
                  {
                    reward: 'daily-missions-100',
                    atLeast: 100,
                    cadence: 'DAILY',
                    grants: [{ item: 'black-card', displayName: 'Black Card', quantity: 15 }],
                  },
                ],
              },
              {
                measure: 'chores',
                displayName: 'Chores',
                paysForPulls: false,
                paysForPlans: true,
                bars: [{ reward: 'chores-1', atLeast: 1, cadence: 'WEEKLY', grants: [] }],
              },
            ],
          });
        }
        if (url.startsWith(`/api/me/profiles/${PROFILE.id}/pity`)) {
          return ok(method === 'PUT' ? pity((JSON.parse(String(init?.body)) as { pullsSinceHit: number }).pullsSinceHit) : pity(0));
        }
        if (url === `/api/me/profiles/${PROFILE.id}/pulls`) {
          return ok({
            banner: 'pgr-adelyde-anabasis',
            bannerName: 'Adelyde: Anabasis — Crucible Event Construct',
            versionSequence: 17,
            versionLabel: 'Anchored in Faith',
            daysAsked: 0,
            days: 0,
            cappedAtClose: false,
            closesAt: '2099-01-01T00:00:00Z',
            copies: 1,
            pity: pity(45),
            budget: {
              currency: 'event-construct-rd-ticket',
              currencyName: 'Event Construct R&D Ticket',
              held: 3750,
              accruing: 0,
              perPull: 250,
              pulls: 15,
              uncounted: [],
              converted: [
                { item: 'black-card', displayName: 'Black Card', held: 3750, accruing: 0, via: ['direct-exchange-black-card'] },
              ],
            },
            chance: 1,
            expectedPulls: 15,
            worstCasePulls: 15,
            // From 45 on a wall of 60: rising to certainty at the fifteenth pull.
            curve: Array.from({ length: 16 }, (_, pulls) => (pulls === 15 ? 1 : Math.round((pulls / 15) ** 2 * 10_000) / 10_000)),
            method: 'exact Markov chain',
          });
        }
        return new Response(JSON.stringify({ status: 404, detail: `no fake for ${method} ${url}` }), { status: 404 });
      }),
    );
  }
});

function ok(body: unknown): Response {
  return new Response(JSON.stringify(body), { status: 200, headers: { 'Content-Type': 'application/json' } });
}
