import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import type { Plan } from '../api/client';
import { PlanCircuit } from './PlanCircuit';

/**
 * The plan as a circuit (C2.6). It draws what the checklist says, so what it
 * must get right is which nodes it keeps when there are too many to draw: the
 * dearest runs, one node per character, and a count for the rest rather than
 * silence.
 */
describe('the plan circuit', () => {
  it('keeps the dearest stages, counts the rest, and gives purchases and claims a node each', () => {
    const { container } = render(
      <PlanCircuit
        plan={plan({
          stages: [10, 50, 30, 20, 40].map((energy, index) => ({
            stage: `stage-${index}`,
            displayName: `Stage ${index}`,
            runs: energy / 10,
            energyCost: 10,
            totalEnergy: energy,
          })),
          conversions: [{ step: 'buy', times: 1 }],
          rewards: [{ reward: 'weekly', times: 2 }],
          payingFor: [
            step('level-80', 'lucia', 'Lucia: Inverse Crown'),
            step('promote-2', 'lucia', 'Lucia: Inverse Crown'),
            step('skill-1', 'selena', 'Selena: Pianissimo'),
          ],
        })}
        energyUnit="Serum"
      />,
    );

    const sources = [...container.querySelectorAll("[data-side='source']")].map((node) => node.textContent);
    expect(sources).toEqual(['Stage 15 runs', '4 more stages' + 'see the list below', '1 to buy or craft' + 'purchases, crafts, feeding', '1 to claim' + 'rewards on the way']);
    // One node a character, however many steps it takes, the most steps first.
    const targets = [...container.querySelectorAll("[data-side='target']")].map((node) => node.textContent);
    expect(targets).toEqual(['Lucia: Inverse Crown2 upgrades', 'Selena: Pianissimo1 upgrade']);
    expect(screen.getByText('150')).toBeInTheDocument();
  });

  it('draws nothing for a plan with nothing to do', () => {
    const { container } = render(
      <PlanCircuit plan={plan({ stages: [], conversions: [], rewards: [] })} energyUnit="Serum" />,
    );
    expect(container).toBeEmptyDOMElement();
  });
});

function step(id: string, entity: string, entityName: string) {
  return { step: id, entity, entityName, fromState: null, toState: null, displayName: `${entityName} to ${id}` };
}

function plan(lines: Pick<Plan, 'stages' | 'conversions' | 'rewards'> & Partial<Plan>): Plan {
  return {
    id: 'p',
    profile: 'profile',
    game: 'game',
    version: 1,
    versionLabel: '1.0',
    attribution: 'test',
    objective: 'MIN_ENERGY',
    totalEnergy: lines.stages.reduce((sum, stage) => sum + stage.totalEnergy, 0),
    etaDays: 1,
    shadowPrice: [],
    bindingStages: [],
    notes: [],
    computedAt: '2026-10-01T00:00:00Z',
    ...lines,
  };
}
