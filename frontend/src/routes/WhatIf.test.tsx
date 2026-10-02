import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useState } from 'react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { Plan, SavedPlan, WhatIfAsk } from '../api/client';
import { stepFor, why, WhatIfPanel, type WhatIfLevers } from './WhatIf';

/**
 * C2.20: the saved plan asked again while the reader drags it, which must
 * never save, must only ever show the answer to the last question, and must
 * put a shadow price's prediction beside what the solver then found.
 */
describe('what if', () => {
  const plan = (totalEnergy: number, stages: Plan['stages'] = []): Plan => ({
    id: `plan-${totalEnergy}`,
    profile: 'p',
    game: 'punishing-gray-raven',
    version: 18,
    versionLabel: 'Anchored in Faith',
    attribution: 'read from the client',
    objective: 'LEAST_ENERGY',
    stages,
    conversions: [],
    rewards: [],
    totalEnergy,
    etaDays: totalEnergy / 240,
    shadowPrice: [
      { item: 'serum-pack', displayName: 'Serum Pack', price: 9, holdable: true },
      { item: 'progress:character-exp', displayName: 'Character EXP', price: 3, holdable: false },
    ],
    bindingStages: [],
    notes: [],
    computedAt: '2026-10-02T00:00:00Z',
  });

  const SAVED: SavedPlan = {
    request: { objective: 'LEAST_ENERGY', energyPerDay: 240, horizonDays: 30, reach: {} },
    plan: plan(2_400),
    savedAt: '2026-10-02T00:00:00Z',
  };

  let asked: WhatIfAsk[];

  beforeEach(() => {
    asked = [];
    // A pretend solver: energy falls with more a day, and each pretended Serum
    // Pack saves 6 — less than the 9 its price says, as whole runs would.
    vi.stubGlobal(
      'fetch',
      vi.fn(async (url: string, init: RequestInit) => {
        expect(url).toContain('/api/me/profiles/p/plan/what-if');
        const ask = JSON.parse(String(init.body)) as WhatIfAsk;
        asked.push(ask);
        const energy = 2_400 + (240 - ask.energyPerDay) * 5 - (ask.extra['serum-pack'] ?? 0) * 6;
        const stages = [{ stage: 'sim', displayName: 'Simulation run', runs: energy / 30, energyCost: 30, totalEnergy: energy }];
        return new Response(JSON.stringify({ plan: plan(energy, stages), solveMillis: 12, fromCache: false }), {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        });
      }),
    );
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  function Panel({ onKeep }: { onKeep: (levers: WhatIfLevers) => void }) {
    const [open, setOpen] = useState(false);
    return (
      <WhatIfPanel
        profileId="p"
        saved={SAVED}
        energyUnit="Serum"
        ladders={[]}
        onKeep={onKeep}
        keeping={false}
        open={open}
        setOpen={setOpen}
      />
    );
  }

  function renderPanel(onKeep = vi.fn()) {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(
      <QueryClientProvider client={client}>
        <Panel onKeep={onKeep} />
      </QueryClientProvider>,
    );
    return onKeep;
  }

  it('asks nothing until it is opened, then asks the saved plan’s question with nothing pretended', async () => {
    renderPanel();
    expect(asked).toHaveLength(0);

    await userEvent.click(screen.getByRole('button', { name: 'Try a what-if' }));

    expect(await screen.findByText('Solved on the server in 12 ms')).toBeInTheDocument();
    expect(asked[0]).toEqual({ energyPerDay: 240, horizonDays: 30, objective: 'LEAST_ENERGY', reach: {}, extra: {} });
    expect(screen.getByText('same as your saved plan')).toBeInTheDocument();
    // Its own numbers: nothing to keep yet.
    expect(screen.queryByRole('button', { name: 'Keep this' })).not.toBeInTheDocument();
  });

  it('asks again when a lever moves, says what changed against the saved plan, and keeps the levers it was asked with', async () => {
    const onKeep = renderPanel();
    await userEvent.click(screen.getByRole('button', { name: 'Try a what-if' }));
    await screen.findByText('Solved on the server in 12 ms');

    fireEvent.change(screen.getByLabelText('Serum a day'), { target: { value: '200' } });

    // 40 fewer a day costs 200 more Serum in this pretend solver.
    expect(await screen.findByText('+200 serum')).toBeInTheDocument();
    expect(asked.at(-1)!.energyPerDay).toBe(200);

    await userEvent.click(screen.getByRole('button', { name: 'Keep this' }));
    expect(onKeep).toHaveBeenCalledWith({ energyPerDay: 200, horizonDays: 30, objective: 'LEAST_ENERGY', reach: {} });
  });

  it('puts a shadow price’s prediction beside what the solve found, and says why they differ', async () => {
    renderPanel();
    await userEvent.click(screen.getByRole('button', { name: 'Try a what-if' }));
    await screen.findByText('Solved on the server in 12 ms');

    // Only what a reader can hold is offered: EXP is a demand line, not a thing in a bag.
    expect(screen.getByText('Serum Pack')).toBeInTheDocument();
    expect(screen.queryByText('Character EXP')).not.toBeInTheDocument();

    // At 9 a unit, a step of 10 is worth about twenty.
    await userEvent.click(screen.getByRole('button', { name: '+10' }));

    expect(await screen.findByText(/Less than the price said/)).toBeInTheDocument();
    expect(asked.at(-1)!.extra).toEqual({ 'serum-pack': 10 });
    const verdict = screen.getByText(/The price said/);
    expect(verdict).toHaveTextContent('The price said −90; the plan saved −60.');

    // Pretended items are not the reader's, so they cannot be kept.
    expect(screen.queryByRole('button', { name: 'Keep this' })).not.toBeInTheDocument();
    expect(screen.getByText(/Pretended items can’t be kept/)).toBeInTheDocument();
  });

  it('steps a price by the power of ten worth about twenty, and explains a gap in one sentence', () => {
    expect(stepFor(90)).toBe(1);
    expect(stepFor(9)).toBe(10);
    expect(stepFor(0.003)).toBe(10_000);
    expect(stepFor(0)).toBe(1);
    expect(why(90, 90)).toBe('The price held.');
    expect(why(90, 60)).toMatch(/^Less than the price said/);
    expect(why(90, 120)).toMatch(/^More than the price said/);
  });
});
