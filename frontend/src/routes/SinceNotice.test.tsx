import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { ChangeForYou, Plan, Profile, SavedPlan, Since } from '../api/client';
import { usePlannerStore } from '../store/plannerStore';
import { Home } from './Home';
import { PlanView } from './PlanView';

/**
 * C3.3: a reader returning after a new sequence sees what it changed for their
 * goals and plan, without asking — on Home, and above the saved plan.
 */
const PGR = 'punishing-gray-raven';
const PROFILE: Profile = { id: 'p', game: PGR, region: 'global', displayName: 'Main' };
const LUCIA = 'lucia-inverse-crown';

const plan = (version: number, totalEnergy: number): Plan => ({
  id: `plan-${version}`,
  profile: PROFILE.id,
  game: PGR,
  version,
  versionLabel: version === 18 ? 'Anchored in Faith' : 'Steering By Light',
  attribution: 'read from the client',
  objective: 'LEAST_ENERGY',
  stages: [],
  conversions: [],
  rewards: [],
  totalEnergy,
  etaDays: 28,
  shadowPrice: [],
  bindingStages: [],
  notes: [],
  computedAt: '2026-09-26T08:00:00Z',
});

const SAVED: SavedPlan = {
  request: { objective: 'LEAST_ENERGY', energyPerDay: 240, horizonDays: 60, reach: {} },
  plan: plan(14, 1_230),
  savedAt: '2026-09-26T08:00:00Z',
};

const added = (to: number): ChangeForYou => ({
  kind: 'ADDED',
  about: 'upgrade',
  slug: `${LUCIA}-abyssal-lament-${to}`,
  name: `Lucia: Inverse Crown to abyssal-lament-${to}`,
  detail: null,
  before: null,
  after: null,
  entity: LUCIA,
  entityName: 'Lucia: Inverse Crown',
  fromState: `abyssal-lament-${to - 1}`,
  toState: `abyssal-lament-${to}`,
});

const report = (changes: ChangeForYou[], latest: Partial<Since['onLatest']> = {}): Since => ({
  profile: PROFILE.id,
  game: PGR,
  savedVersion: 14,
  savedVersionLabel: 'Steering By Light',
  latestVersion: 18,
  latestVersionLabel: 'Anchored in Faith',
  savedAt: SAVED.savedAt,
  changes,
  allChanges: 405,
  onSaved: { version: 14, versionLabel: 'Steering By Light', totalEnergy: 1_230, etaDays: 28, refused: null },
  onLatest: {
    version: 18,
    versionLabel: 'Anchored in Faith',
    totalEnergy: 1_230,
    etaDays: 28,
    refused: null,
    ...latest,
  },
});

const UP_TO_DATE: Since = {
  ...report([]),
  savedVersion: 18,
  savedVersionLabel: 'Anchored in Faith',
  onSaved: null,
  onLatest: null,
};

let sent: { method: string; url: string }[];

beforeEach(() => {
  usePlannerStore.setState({ profileId: PROFILE.id, outbox: {}, reach: {}, rejected: [], lastSyncedAt: null });
  sent = [];
});

afterEach(() => {
  vi.unstubAllGlobals();
});

describe('the line on Home', () => {
  it('says a new sequence is out and how much of it touches the plan, and links to it', async () => {
    serve({ since: report([added(5), added(6), added(7)]) });
    renderAt(<Home />);

    expect(await screen.findByText(/3 changes in it touch your plan/)).toBeInTheDocument();
    expect(screen.getByText(/Anchored in Faith \(v18\)/)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'See what changed →' })).toHaveAttribute('href', '/plan');
  });

  it('says so when nothing in the sequence touches the plan, rather than staying silent', async () => {
    serve({ since: report([]) });
    renderAt(<Home />);

    expect(await screen.findByText(/nothing in it touches your plan/)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Re-plan →' })).toHaveAttribute('href', '/plan');
  });

  it('says nothing when the plan is up to date, or was never run', async () => {
    serve({ since: UP_TO_DATE });
    renderAt(<Home />);
    expect(await screen.findByText('Main')).toBeInTheDocument();
    await vi.waitFor(() => expect(sent.some((request) => request.url.endsWith('/since'))).toBe(true));
    expect(screen.queryByText(/New since your plan/)).not.toBeInTheDocument();
  });
});

describe('the panel on the plan screen', () => {
  it('shows what changed above the saved plan, grouped by track, with every line behind a toggle', async () => {
    serve({ since: report(Array.from({ length: 13 }, (_, index) => added(index + 5))) });
    renderAt(<PlanView />);

    const panel = await screen.findByRole('region', { name: 'Since this plan' });
    expect(within(panel).getByText(/patch Steering By Light \(v14\) → Anchored in Faith \(v18\)/)).toBeInTheDocument();
    expect(within(panel).getByText('13 of the 405 changes in it touch something your plan uses.')).toBeInTheDocument();
    expect(within(panel).getByText('Same cost: 1,230 Serum over 28.0 days')).toBeInTheDocument();
    expect(await within(panel).findByText('Lucia: Inverse Crown · Red Orb')).toBeInTheDocument();
    expect(within(panel).getByText('13 new steps')).toBeInTheDocument();
    expect(within(panel).getByText('Every line (13)')).toBeInTheDocument();
    // The saved plan stays below it, as it was shown.
    expect(screen.getByText(/Your last plan, worked out/)).toBeInTheDocument();
  });

  it('names a patch once when both sequences carry its label', async () => {
    serve({ since: { ...report([added(5)]), savedVersionLabel: 'Anchored in Faith' } });
    renderAt(<PlanView />);

    expect(
      await screen.findByText('Since this plan: v14 → v18, both on patch Anchored in Faith'),
    ).toBeInTheDocument();
  });

  it('says why a side cannot be planned, in the solver’s words', async () => {
    serve({
      since: report([added(5)], {
        totalEnergy: null,
        etaDays: null,
        refused: 'nothing sources Abyssal Core',
      }),
    });
    renderAt(<PlanView />);

    expect(
      await screen.findByText('On patch Anchored in Faith (v18) your goals can’t be planned: nothing sources Abyssal Core'),
    ).toBeInTheDocument();
    expect(screen.queryByText(/Same cost/)).not.toBeInTheDocument();
  });

  it('is one line when nothing touches the plan', async () => {
    serve({ since: report([]) });
    renderAt(<PlanView />);

    const panel = await screen.findByRole('region', { name: 'Since this plan' });
    expect(within(panel).getByText(/Patch Anchored in Faith \(v18\) is out since this plan, and nothing in it touches/)).toBeInTheDocument();
    expect(within(panel).queryByText(/What changed for you/)).not.toBeInTheDocument();
  });

  it('goes away after a re-plan, and nothing else offers to dismiss it', async () => {
    serve({ since: report([added(5)]) });
    renderAt(<PlanView />);

    const panel = await screen.findByRole('region', { name: 'Since this plan' });
    expect(within(panel).getAllByRole('button').map((button) => button.textContent)).toEqual([
      'Re-plan on the latest data (v18)',
    ]);

    await userEvent.click(within(panel).getByRole('button', { name: 'Re-plan on the latest data (v18)' }));

    await vi.waitFor(() => expect(screen.queryByRole('region', { name: 'Since this plan' })).not.toBeInTheDocument());
    expect(sent.filter((request) => request.method === 'POST' && request.url === `/api/me/profiles/${PROFILE.id}/plan`)).toHaveLength(1);
    // The report is asked again, and now says the plan is up to date.
    await vi.waitFor(() => expect(sent.filter((request) => request.url.endsWith('/since')).length).toBeGreaterThanOrEqual(2));
  });
});

function renderAt(page: React.ReactNode) {
  render(
    <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
      <MemoryRouter>{page}</MemoryRouter>
    </QueryClientProvider>,
  );
}

/** The server, with a saved plan on 14 until a re-plan saves one on 18. */
function serve({ since }: { since: Since }) {
  let current = since;
  let saved = SAVED;
  const replanned = () => {
    current = UP_TO_DATE;
    saved = { ...SAVED, plan: plan(18, 1_230) };
  };
  vi.stubGlobal(
    'fetch',
    vi.fn(async (url: string, init?: RequestInit) => {
      const method = init?.method ?? 'GET';
      sent.push({ method, url });

      if (url === '/api/me') {
        return ok({ accountId: 'a', displayName: 'Reader', email: 'r@example.invalid', profiles: [PROFILE] });
      }
      if (url === '/api/games') {
        return ok({
          games: [
            {
              id: PGR,
              displayName: 'Punishing: Gray Raven',
              energyUnit: 'Serum',
              latest: { game: PGR, sequence: 18, label: 'Anchored in Faith', attribution: 'read from the client' },
            },
          ],
        });
      }
      if (url === `/api/me/profiles/${PROFILE.id}/goals`) {
        return ok({ profile: PROFILE.id, goals: [{ entity: LUCIA, targetState: 'abyssal-lament-18' }] });
      }
      if (url === `/api/me/profiles/${PROFILE.id}/since`) return ok(current);
      if (url === `/api/me/profiles/${PROFILE.id}/plan`) {
        if (method === 'POST') {
          replanned();
          return ok(saved.plan);
        }
        return ok(saved);
      }
      if (url === `/api/games/${PGR}/entities/${LUCIA}/upgrades?version=18`) {
        return ok({
          game: PGR,
          version: { game: PGR, sequence: 18, label: 'Anchored in Faith', attribution: '' },
          entity: { id: LUCIA, displayName: 'Lucia: Inverse Crown' },
          steps: Array.from({ length: 17 }, (_, index) => ({
            id: `${LUCIA}-abyssal-lament-${index + 2}`,
            fromState: `abyssal-lament-${index + 1}`,
            toState: `abyssal-lament-${index + 2}`,
            costs: [],
            section: 'Basic Skill',
            tag: 'Red Orb',
          })),
          totalCost: [],
        });
      }
      return new Response(JSON.stringify({ status: 404, detail: `no fake for ${method} ${url}` }), { status: 404 });
    }),
  );
}

function ok(body: unknown) {
  return new Response(JSON.stringify(body), { status: 200, headers: { 'Content-Type': 'application/json' } });
}
