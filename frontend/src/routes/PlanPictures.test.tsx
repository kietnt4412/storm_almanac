import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import type { Plan } from '../api/client';
import { flowsOf } from '../ui/PlanFlow';
import { bindingOf, WhyDays } from '../ui/WhyDays';
import { Answer } from './PlanView';

/**
 * C2.8's three pictures on the plan screen. Each is drawn from numbers the plan
 * sent, so what is pinned is that the picture says what the plan says: which
 * half of `etaDays` binds, that the flow's two sides add up to the plan's own
 * totals, and that a zero price is said, not drawn.
 */
const base: Plan = {
  id: 'plan-1',
  profile: 'p',
  game: 'punishing-gray-raven',
  version: 18,
  versionLabel: 'Anchored in Faith',
  attribution: 'read from the client',
  objective: 'LEAST_ENERGY',
  stages: [],
  conversions: [],
  rewards: [],
  totalEnergy: 0,
  etaDays: 0,
  shadowPrice: [],
  bindingStages: [],
  notes: [],
  computedAt: '2026-10-01T00:00:00Z',
};

const cage = (tier: string, times: number) => ({
  reward: `phantom-pain-cage-${tier}`,
  displayName: `Phantom Pain Cage, weekly, score ${tier}+`,
  times,
  cadence: 'WEEKLY',
});

const battlefield = (runs: number) => ({
  stage: 'simulated-battlefield',
  displayName: 'Simulated Battlefield',
  runs,
  energyCost: 30,
  totalEnergy: runs * 30,
  pays: [{ item: 'simulation-score', displayName: 'Simulation Score', perRun: 82, total: runs * 82 }],
});

const buy = (step: string, quantity: number, buys: string) => ({
  step,
  times: 1,
  spends: { item: 'simulation-score', displayName: 'Simulation Score', quantity, buys },
});

describe('why the plan takes as long as it does', () => {
  it('says the weekly claims bind when the energy runs out first, and marks each at day 7k', () => {
    // 1,230 serum at 160 a day is 7.7 days; four weekly claims make it 28.
    const plan = { ...base, totalEnergy: 1230, etaDays: 28, rewards: [cage('30000', 4), cage('90000', 3)] };
    const binding = bindingOf(plan, 160);
    expect(binding.energyDays).toBeCloseTo(7.69, 2);
    expect(binding.marks).toEqual([7, 14, 21, 28]);
    expect(binding.lastClaim).toBe(28);

    render(<WhyDays plan={plan} energyPerDay={160} energyUnit="Serum" />);
    expect(screen.getByRole('heading', { name: 'Why 28 days?' })).toBeInTheDocument();
    expect(screen.getByText(/so the claims set the length, not the serum/)).toHaveTextContent(
      'The serum is spent by day 8. The plan counts 4 weekly claims, and the last comes on day 28',
    );
  });

  it('says the energy binds when there is nothing to wait for', () => {
    const plan = { ...base, totalEnergy: 1230, etaDays: 1230 / 160, rewards: [cage('30000', 1)] };
    expect(bindingOf(plan, 160).waitDays).toBe(0);

    render(<WhyDays plan={plan} energyPerDay={160} energyUnit="Serum" />);
    expect(screen.getByText(/sets the length/)).toHaveTextContent(
      'The serum sets the length: 1,230 at 160 a day is 7.7 days. The claims it counts all come sooner.',
    );
  });

  it('marks nothing for a plan saved before rewards carried their cadence, and still says what binds', () => {
    const plan = { ...base, totalEnergy: 300, etaDays: 14, rewards: [{ reward: 'r', times: 2 }] };
    expect(bindingOf(plan, 100).marks).toEqual([]);
    render(<WhyDays plan={plan} energyPerDay={100} energyUnit="Serum" />);
    expect(screen.getByText(/the plan waits to day/)).toBeInTheDocument();
  });
});

describe('where the energy goes', () => {
  it('draws each side as wide as the plan\'s own numbers, and both sides add up', () => {
    // The rehearsal account's plan: 41 runs pay 3,362 Score, all of it spent.
    const plan = {
      ...base,
      stages: [battlefield(41)],
      conversions: [buy('pods', 2488, 'EXP Pod (L)'), buy('cogs', 538, 'Cogs'), buy('sp', 336, 'Skill Point')],
    };
    const [flow] = flowsOf(plan, 'Serum');
    expect(flow!.sources.map((part) => [part.label, part.quantity])).toEqual([['Simulated Battlefield', 3362]]);
    expect(flow!.sinks.map((part) => part.quantity)).toEqual([2488, 538, 336]);
    expect(flow!.total).toBe(3362);
  });

  it('draws what the runs leave over, and what the bag pays when they fall short', () => {
    const over = flowsOf({ ...base, stages: [battlefield(5)], conversions: [buy('cogs', 379, 'Cogs')] }, 'Serum')[0]!;
    expect(over.sinks.at(-1)).toMatchObject({ label: 'Left over', quantity: 31 });
    expect(over.total).toBe(410);

    const short = flowsOf({ ...base, stages: [battlefield(1)], conversions: [buy('cogs', 100, 'Cogs')] }, 'Serum')[0]!;
    expect(short.sources.at(-1)).toMatchObject({ label: 'From your bag', quantity: 18 });
    expect(short.total).toBe(100);
  });

  it('draws nothing for a plan saved before stages said what they pay', () => {
    const { pays: _pays, ...old } = battlefield(41);
    expect(flowsOf({ ...base, stages: [old], conversions: [buy('cogs', 538, 'Cogs')] }, 'Serum')).toEqual([]);
  });
});

describe('what is precious', () => {
  it('draws a bar for each price against the dearest, and folds the zeros into a sentence', () => {
    render(
      <Answer
        plan={{
          ...base,
          shadowPrice: [
            { item: 'skill-point', displayName: 'Skill Point', price: 10 },
            { item: 'shard', displayName: 'Lacrimosa shard', price: 40 },
            { item: 'cogs', displayName: 'Cogs', price: 0 },
          ],
        }}
        energyUnit="Serum"
      />,
    );
    const bars = screen.getAllByTestId('price-bar');
    expect(bars.map((bar) => bar.style.width)).toEqual(['100%', '25%']);
    expect(screen.getByText(/No extra serum for one more of Cogs/)).toBeInTheDocument();
  });
});
