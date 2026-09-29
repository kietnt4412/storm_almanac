import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { Plan, Profile, SavedPlan } from '../api/client';
import { usePlannerStore } from '../store/plannerStore';
import { PlanView } from './PlanView';

/**
 * C3.1: a returning reader starts from the last plan they were shown, and from
 * what they asked to get it (ADR 0037).
 */
describe('the saved plan', () => {
  const PGR = 'punishing-gray-raven';
  const PROFILE: Profile = { id: 'p', game: PGR, region: 'global', displayName: 'Main' };
  let sent: { method: string; url: string; body: unknown }[];

  const plan = (totalEnergy: number): Plan => ({
    id: `plan-${totalEnergy}`,
    profile: PROFILE.id,
    game: PGR,
    version: 18,
    versionLabel: 'Anchored in Faith',
    attribution: 'read from the client',
    objective: 'LEAST_ENERGY',
    stages: [],
    conversions: [],
    rewards: [],
    totalEnergy,
    etaDays: 2,
    shadowPrice: [],
    bindingStages: [],
    notes: [],
    computedAt: '2026-09-27T08:00:00Z',
  });

  const SAVED: SavedPlan = {
    request: { objective: 'FEWEST_DAYS', energyPerDay: 180, horizonDays: 30, reach: { 'phantom-pain-cage-score': 90_000 } },
    plan: plan(1_470),
    savedAt: '2026-09-27T08:00:00Z',
  };

  beforeEach(() => {
    usePlannerStore.setState({ profileId: PROFILE.id, outbox: {}, reach: {}, rejected: [], lastSyncedAt: null });
    sent = [];
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('opens on the last plan, says when it was worked out, and fills the form from its request', async () => {
    serve(SAVED);
    renderPlan();

    expect(await screen.findByText(/Your last plan, worked out/)).toBeInTheDocument();
    expect(screen.getByText('1,470')).toBeInTheDocument();
    expect(screen.getByText(/Anchored in Faith \(v18\)\. It counts what you owned then/)).toBeInTheDocument();

    expect(screen.getByLabelText(/a day/)).toHaveValue(180);
    expect(screen.getByLabelText('Days')).toHaveValue(30);
    expect(screen.getByLabelText('Optimise for')).toHaveValue('FEWEST_DAYS');
    // This browser had no answer of its own, so the saved one is taken.
    expect(usePlannerStore.getState().reach[PROFILE.id]).toEqual({ 'phantom-pain-cage-score': 90_000 });
  });

  it("does not overwrite how far this browser says the reader gets", async () => {
    usePlannerStore.setState({ reach: { [PROFILE.id]: { 'phantom-pain-cage-score': 1_100_000 } } });
    serve(SAVED);
    renderPlan();

    expect(await screen.findByText(/Your last plan, worked out/)).toBeInTheDocument();
    expect(usePlannerStore.getState().reach[PROFILE.id]).toEqual({ 'phantom-pain-cage-score': 1_100_000 });
  });

  it('shows nothing from before when there is no saved plan, and reads it again after working one out', async () => {
    serve(null);
    renderPlan();

    const submit = await screen.findByRole('button', { name: 'Work it out' });
    await vi.waitFor(() => expect(submit).toBeEnabled());
    expect(screen.queryByText(/Your last plan/)).not.toBeInTheDocument();

    await userEvent.click(submit);
    expect(await screen.findByText('90')).toBeInTheDocument();
    // The fresh answer is on screen, not framed as one from before.
    expect(screen.queryByText(/Your last plan/)).not.toBeInTheDocument();
    await vi.waitFor(() =>
      expect(sent.filter((r) => r.method === 'GET' && r.url === `/api/me/profiles/${PROFILE.id}/plan`)).toHaveLength(2),
    );
  });

  function renderPlan() {
    render(
      <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
        <MemoryRouter>
          <PlanView />
        </MemoryRouter>
      </QueryClientProvider>,
    );
  }

  function serve(saved: SavedPlan | null) {
    vi.stubGlobal(
      'fetch',
      vi.fn(async (url: string, init?: RequestInit) => {
        const method = init?.method ?? 'GET';
        sent.push({ method, url, body: init?.body ? JSON.parse(String(init.body)) : undefined });

        if (url === '/api/me') {
          return ok({ accountId: 'a', displayName: 'Reader', email: 'r@example.invalid', profiles: [PROFILE] });
        }
        if (url === `/api/me/profiles/${PROFILE.id}/goals`) {
          return ok({ profile: PROFILE.id, goals: [{ entity: 'lacrimosa', targetState: 'rank-2' }] });
        }
        if (url === `/api/me/profiles/${PROFILE.id}/plan`) {
          if (method === 'POST') return ok(plan(90));
          return saved ? ok(saved) : new Response(null, { status: 204 });
        }
        return new Response(JSON.stringify({ status: 404, detail: `no fake for ${method} ${url}` }), { status: 404 });
      }),
    );
  }
});

function ok(body: unknown) {
  return new Response(JSON.stringify(body), { status: 200, headers: { 'Content-Type': 'application/json' } });
}
