import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import type { Plan } from '../api/client';
import { Answer } from './PlanView';

/**
 * What a material is costing you, rendered under a name a person recognises.
 *
 * <p>This list used to be a map of `itemId -> price`, and a map has nowhere to
 * put a name. Most keys read acceptably because an item id is close to its
 * name; a demand line that stands for something the item table has no row for
 * does not. `progress:character-exp` appeared here and on the shortfall table
 * beside a properly named `Cogs` — N37.
 *
 * <p>jsdom computes no layout, so this is a test about text and never about
 * appearance. The ordering is worth pinning all the same: the list is read to
 * find out what is expensive, and the answer belongs at the top.
 */
describe('what each material is costing you', () => {
  const plan = (shadowPrice: Plan['shadowPrice']): Plan => ({
    id: 'plan-1',
    profile: 'p',
    game: 'punishing-gray-raven',
    version: 7,
    versionLabel: 'Steering By Light',
    attribution: 'read from the client',
    objective: 'LEAST_ENERGY',
    stages: [],
    conversions: [],
    rewards: [],
    totalEnergy: 0,
    etaDays: 0,
    shadowPrice,
    bindingStages: [],
    notes: [],
    computedAt: '2026-09-22T00:00:00Z',
  });

  it('names a progress line rather than printing its slug', () => {
    render(
      <Answer
        plan={plan([
          { item: 'progress:character-exp', displayName: 'Character EXP', price: 30 },
          { item: 'cogs', displayName: 'Cogs', price: 1 },
        ])}
        energyUnit="Serum"
      />,
    );

    expect(screen.getByText('Character EXP')).toBeInTheDocument();
    expect(screen.queryByText('progress:character-exp')).not.toBeInTheDocument();
  });

  it('puts the dearest first, whatever order the server sent', () => {
    // The server sorts by id so that two reads of one plan are byte-identical;
    // the page sorts by price because that is the question being asked.
    render(
      <Answer
        plan={plan([
          { item: 'cogs', displayName: 'Cogs', price: 1 },
          { item: 'progress:character-exp', displayName: 'Character EXP', price: 30 },
        ])}
        energyUnit="Serum"
      />,
    );

    const names = screen.getAllByRole('listitem').map((item) => item.textContent);
    expect(names[0]).toContain('Character EXP');
    expect(names[1]).toContain('Cogs');
  });

  it('says why a material costs nothing, rather than listing 0.00', () => {
    // S10: a panel of zeros read as broken. A zero is a real answer — one more
    // costs no extra Serum — and the sentence says so.
    render(
      <Answer
        plan={plan([
          { item: 'cogs', displayName: 'Cogs', price: 0 },
          { item: 'progress:character-exp', displayName: 'Character EXP', price: 30 },
          { item: 'skill-point', displayName: 'Skill Point', price: 0 },
        ])}
        energyUnit="Serum"
      />,
    );

    expect(screen.getAllByRole('listitem').map((item) => item.textContent)).toEqual(['Character EXP30.00']);
    expect(screen.queryByText('0.00')).not.toBeInTheDocument();
    expect(
      screen.getByText(/No extra serum for one more of Cogs, Skill Point — this plan already makes a spare/),
    ).toBeInTheDocument();
  });

  it('is one sentence and no list when every price is zero', () => {
    render(<Answer plan={plan([{ item: 'cogs', displayName: 'Cogs', price: 0 }])} energyUnit="Serum" />);

    expect(screen.queryByRole('listitem')).not.toBeInTheDocument();
    expect(screen.queryByText(/per extra unit/)).not.toBeInTheDocument();
    expect(screen.getByText(/one more of Cogs — .* gets it without spending serum/)).toBeInTheDocument();
  });

  it('shows no section at all when the plan priced nothing', () => {
    // An empty map is a real answer — every demanded item at its bound — and a
    // heading over nothing reads as something broken.
    render(<Answer plan={plan([])} energyUnit="Serum" />);

    expect(screen.queryByText(/costing you/)).not.toBeInTheDocument();
  });
});
