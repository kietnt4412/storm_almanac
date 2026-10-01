import { describe, expect, it } from 'vitest';
import type { Plan } from '../api/client';
import { tierColour, usesOf } from './Inventory';

/**
 * C2.5: the bag says what the saved plan does with each item (the maintainer's
 * answer, 2026-10-01), and its rarities are coloured by rank without knowing
 * any game's scale.
 */
describe('the inventory and the plan', () => {
  const plan = (conversions: Plan['conversions']): Plan => ({
    id: 'p',
    profile: 'p',
    game: 'g',
    version: 1,
    versionLabel: '1',
    attribution: '',
    objective: 'LEAST_ENERGY',
    stages: [],
    conversions,
    rewards: [],
    totalEnergy: 0,
    etaDays: 0,
    shadowPrice: [],
    bindingStages: [],
    notes: [],
    computedAt: '2026-10-01T00:00:00Z',
  });
  const buy = (step: string, bought: string | undefined, quantity: number, boughtQuantity: number) => ({
    step,
    times: 1,
    spends: { item: 'simulation-score', displayName: 'Simulation Score', quantity, buys: 'x', boughtItem: bought, boughtQuantity },
  });

  it('adds up what every purchase spends and buys, by item', () => {
    const uses = usesOf(
      plan([
        buy('cogs-a', 'cogs', 545, 654_000),
        buy('pods', 'exp-pod-l', 2_472, 120),
        buy('cogs-b', 'cogs', 5, 6_000),
        { step: 'feed', times: 120, spends: null },
      ]),
    );
    expect(uses.spends.get('simulation-score')).toBe(3_022);
    expect(uses.buys.get('cogs')).toBe(660_000);
    expect(uses.buys.get('exp-pod-l')).toBe(120);
  });

  it('marks nothing it cannot read off numbers — a plan saved before them, or none at all', () => {
    const old = usesOf(plan([buy('cogs', undefined, 545, 0)]));
    expect(old.buys.size).toBe(0);
    expect(old.spends.get('simulation-score')).toBe(545);
    expect(usesOf(undefined).spends.size).toBe(0);
  });

  it('colours a rarity by its place among the ranks present, highest first', () => {
    const ranks = [6, 5, 4, 3];
    expect(tierColour(6, ranks)).toBe('var(--signal)');
    expect(tierColour(5, ranks)).toBe('var(--violet)');
    expect(tierColour(4, ranks)).toBe('var(--brand)');
    expect(tierColour(3, ranks)).toBe('var(--line)');
  });
});
