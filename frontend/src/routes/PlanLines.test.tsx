import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import type { Plan } from '../api/client';
import { Answer } from './PlanView';

/**
 * What to run, craft, buy and claim, under names a reader recognises.
 *
 * <p>Until 2026-09-26 these rows printed their ids —
 * `simulation-shop-memory-enhancer-iv` — which D5's rehearsal named before a
 * stranger could. The server now sends a name beside each id. The id stays,
 * as a hover title and as what `bindingStages` is matched against, and it is
 * what renders when an older server sends no name: Vercel and Render deploy at
 * different instants, and a blank row is worse than an ugly one.
 */
describe('the lines of a plan', () => {
  const plan = (lines: Pick<Plan, 'stages' | 'conversions' | 'rewards' | 'bindingStages'>): Plan => ({
    id: 'plan-1',
    profile: 'p',
    game: 'punishing-gray-raven',
    version: 12,
    versionLabel: 'Anchored in Faith',
    attribution: 'read from the client',
    objective: 'LEAST_ENERGY',
    totalEnergy: 90,
    etaDays: 1,
    shadowPrice: [],
    notes: [],
    computedAt: '2026-09-26T00:00:00Z',
    ...lines,
  });

  it('names each row, and keeps the binding mark keyed on the stage id', () => {
    render(
      <Answer
        plan={plan({
          stages: [
            {
              stage: 'simulated-battlefield',
              displayName: 'Simulated Battlefield',
              runs: 3,
              energyCost: 30,
              totalEnergy: 90,
            },
          ],
          conversions: [
            {
              step: 'simulation-shop-memory-enhancer-iv',
              displayName: 'Buy 10 Memory Enhancer IV for 87 Simulation Score',
              times: 6,
            },
          ],
          rewards: [
            {
              reward: 'phantom-pain-cage-90000',
              displayName: 'Weekly, score 90,000+: 5 Phantom Pain Scar',
              times: 9,
            },
          ],
          bindingStages: ['simulated-battlefield'],
        })}
        energyUnit="Serum"
      />,
    );

    expect(screen.getByText('Simulated Battlefield')).toBeInTheDocument();
    expect(screen.getByText('binding')).toBeInTheDocument();
    expect(screen.getByText(/Buy 10 Memory Enhancer IV for 87 Simulation Score/)).toBeInTheDocument();
    expect(screen.getByText(/Weekly, score 90,000\+: 5 Phantom Pain Scar/)).toBeInTheDocument();
    expect(screen.queryByText(/simulation-shop-memory-enhancer-iv/)).not.toBeInTheDocument();
    expect(screen.queryByText(/phantom-pain-cage-90000/)).not.toBeInTheDocument();
  });

  it('says a purchase done many times by its totals, and how it is made up', () => {
    // S9: "Buy 1,200 Cogs for 1 Simulation Score × 429" read as one cheap buy.
    render(
      <Answer
        plan={plan({
          stages: [],
          conversions: [
            {
              step: 'simulation-shop-cogs',
              displayName: 'Buy 1,200 Cogs for 1 Simulation Score',
              times: 429,
              total: 'Buy 514,800 Cogs for 429 Simulation Score',
              repeat: '429 × 1,200 for 1',
            },
            {
              step: 'memory-exp-4-star',
              displayName: 'Feed Memory Enhancer IV into Memory EXP',
              times: 40,
              total: 'Feed 40 Memory Enhancer IV into Memory EXP',
              repeat: null,
            },
          ],
          rewards: [],
          bindingStages: [],
        })}
        energyUnit="Serum"
      />,
    );

    const [buy, feed] = screen.getAllByRole('listitem').map((line) => line.textContent);
    expect(buy).toBe('Buy 514,800 Cogs for 429 Simulation Score · 429 × 1,200 for 1');
    expect(feed).toBe('Feed 40 Memory Enhancer IV into Memory EXP');
  });

  it('keeps the old line when a server from before the totals sends none', () => {
    render(
      <Answer
        plan={plan({
          stages: [],
          conversions: [
            { step: 'simulation-shop-cogs', displayName: 'Buy 1,200 Cogs for 1 Simulation Score', times: 429 },
          ],
          rewards: [],
          bindingStages: [],
        })}
        energyUnit="Serum"
      />,
    );

    expect(screen.getByRole('listitem').textContent).toBe('Buy 1,200 Cogs for 1 Simulation Score × 429');
  });

  it('falls back to the id when the server sent no name', () => {
    render(
      <Answer
        plan={plan({
          stages: [{ stage: 'simulated-battlefield', runs: 3, energyCost: 30, totalEnergy: 90 }],
          conversions: [{ step: 'simulation-shop-cogs', times: 453 }],
          rewards: [],
          bindingStages: [],
        })}
        energyUnit="Serum"
      />,
    );

    expect(screen.getByText('simulated-battlefield')).toBeInTheDocument();
    expect(screen.getByText(/simulation-shop-cogs/)).toBeInTheDocument();
  });
});
