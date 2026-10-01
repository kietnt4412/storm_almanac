import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { Plan, Profile } from '../api/client';
import { usePlannerStore } from '../store/plannerStore';
import { coverageOf, Inventory, shows } from './Inventory';

/**
 * C2.8: the bag against what the plan needs. The rule that matters is the one
 * the old marks broke: a currency the plan spends is not a need, so Simulation
 * Score is "used", never "3,362 short".
 */
const base: Plan = {
  id: 'p',
  profile: 'p',
  game: 'punishing-gray-raven',
  version: 18,
  versionLabel: 'Anchored in Faith',
  attribution: '',
  objective: 'LEAST_ENERGY',
  stages: [],
  conversions: [
    {
      step: 'simulation-shop-cogs',
      times: 1,
      spends: { item: 'simulation-score', displayName: 'Simulation Score', quantity: 3362, buys: 'Cogs', boughtItem: 'cogs', boughtQuantity: 645600 },
    },
  ],
  rewards: [],
  totalEnergy: 1230,
  etaDays: 28,
  shadowPrice: [],
  bindingStages: [],
  notes: [],
  needs: [
    { item: 'cogs', displayName: 'Cogs', quantity: 689500 },
    { item: 'skill-point', displayName: 'Skill Point', quantity: 41 },
    { item: 'progress:character-exp', displayName: 'Character EXP', quantity: 367000 },
  ],
  computedAt: '2026-10-01T00:00:00Z',
};

describe('coverage of the plan\'s needs', () => {
  it('sets the bag against what the goals need, and calls a spent currency used, not short', () => {
    const known = new Set(['cogs', 'skill-point', 'simulation-score', 'lacrimosa-shard']);
    const coverage = coverageOf(base, { cogs: 700000, 'skill-point': 9, 'simulation-score': 120 }, known);
    expect(coverage.get('cogs')).toEqual({ state: 'covered', need: 689500 });
    expect(coverage.get('skill-point')).toEqual({ state: 'short', need: 41 });
    expect(coverage.get('simulation-score')).toEqual({ state: 'used' });
    expect(coverage.get('lacrimosa-shard')).toBeUndefined();
    // EXP is a need no bag holds, so it is no tile and no count.
    expect(coverage.has('progress:character-exp')).toBe(false);
  });

  it('marks only what is spent and bought for a plan saved before it carried its needs', () => {
    const { needs: _needs, ...old } = base;
    const coverage = coverageOf(old, {}, new Set(['cogs', 'simulation-score']));
    expect([...coverage.values()].every((entry) => entry.state === 'used')).toBe(true);
  });

  it('filters by what the chips say', () => {
    expect(shows('short', { state: 'short', need: 1 })).toBe(true);
    expect(shows('short', { state: 'covered', need: 1 })).toBe(false);
    expect(shows('used', { state: 'covered', need: 1 })).toBe(true);
    expect(shows('used', undefined)).toBe(false);
    expect(shows('unused', undefined)).toBe(true);
  });
});

describe('the bag screen against the plan', () => {
  const PGR = 'punishing-gray-raven';
  const PROFILE: Profile = { id: 'p', game: PGR, region: 'global', displayName: 'Main' };

  beforeEach(() => {
    usePlannerStore.setState({ profileId: PROFILE.id, outbox: {}, reach: {}, rejected: [], lastSyncedAt: null });
    const version = { game: PGR, sequence: 18, label: 'Anchored in Faith', attribution: 'read from the client' };
    const item = (id: string, displayName: string, rank: number) => ({
      id,
      displayName,
      rarity: { label: `${rank}★`, rank },
      category: 'material',
    });
    vi.stubGlobal(
      'fetch',
      vi.fn(async (url: string) => {
        if (url === '/api/me') {
          return ok({ accountId: 'a', displayName: 'Reader', email: 'r@example.invalid', profiles: [PROFILE] });
        }
        if (url === `/api/games/${PGR}/items`) {
          return ok({
            game: PGR,
            version,
            items: [item('cogs', 'Cogs', 4), item('skill-point', 'Skill Point', 5), item('simulation-score', 'Simulation Score', 3), item('pod', 'EXP Pod', 4)],
          });
        }
        if (url === `/api/me/profiles/${PROFILE.id}/inventory`) {
          return ok({ profile: PROFILE.id, items: { cogs: 700000, 'skill-point': 9, 'simulation-score': 120 } });
        }
        if (url === `/api/me/profiles/${PROFILE.id}/plan`) {
          return ok({ request: { objective: 'LEAST_ENERGY', energyPerDay: 160, horizonDays: 30, reach: {} }, plan: base, savedAt: '2026-10-01T00:00:00Z' });
        }
        return new Response(JSON.stringify({ status: 404, detail: `no fake for ${url}` }), { status: 404 });
      }),
    );
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('counts what is covered, filters to what is short, and dots a tile waiting to send', async () => {
    render(
      <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
        <MemoryRouter>
          <Inventory />
        </MemoryRouter>
      </QueryClientProvider>,
    );

    expect(await screen.findByText('Covers 1 of 2')).toBeInTheDocument();
    expect(screen.getByText('32 more — the plan gets them')).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: 'Short for the plan' }));
    expect(screen.getAllByRole('spinbutton').map((field) => field.getAttribute('aria-label'))).toEqual(['Skill Point']);

    // An edit not yet sent turns the tile's dot from saved to waiting.
    const skill = screen.getByRole('spinbutton', { name: 'Skill Point' });
    const tile = skill.closest('li')!;
    expect(within(tile).getByTestId('sync-dot').dataset.pending).toBe('false');
    await userEvent.clear(skill);
    await userEvent.type(skill, '41');
    expect(within(tile).getByTestId('sync-dot').dataset.pending).toBe('true');
    // Covered now, so it leaves the "short" filter.
    expect(screen.queryByRole('spinbutton', { name: 'Skill Point' })).not.toBeInTheDocument();
  });
});

function ok(body: unknown): Response {
  return new Response(JSON.stringify(body), { status: 200, headers: { 'Content-Type': 'application/json' } });
}
