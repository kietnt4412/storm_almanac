import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import type { Plan } from '../api/client';
import { ShareCard, wrap } from './ShareCard';
import { ShareDialog } from './ShareDialog';

/**
 * C2.13: the plan as a picture to share. It must say what the plan says — its
 * cost, its days as the plan screen prints them, its runs, its patch — and be
 * drawn on the device, with no link made.
 */
const LUCIA = { id: 'lucia', displayName: 'Lucia: Inverse Crown', kind: 'character', rarity: { label: 'S', rank: 5 }, element: null, tags: [] };
const SELENA = { id: 'selena', displayName: 'Selena: Pianissimo', kind: 'character', rarity: { label: 'S', rank: 5 }, element: null, tags: [] };

const PLAN: Plan = {
  id: 'p',
  profile: 'profile',
  game: 'punishing-gray-raven',
  version: 18,
  versionLabel: 'Anchored in Faith',
  attribution: 'test',
  objective: 'MIN_ENERGY',
  totalEnergy: 1230,
  etaDays: 27.5,
  stages: [{ stage: 'battlefield', displayName: 'Simulated Battlefield', runs: 41, energyCost: 30, totalEnergy: 1230 }],
  conversions: [],
  rewards: [],
  payingFor: [
    { step: 'a', entity: 'lucia', entityName: 'Lucia: Inverse Crown', fromState: null, toState: null, displayName: 'a' },
    { step: 'b', entity: 'lucia', entityName: 'Lucia: Inverse Crown', fromState: null, toState: null, displayName: 'b' },
    { step: 'c', entity: 'selena', entityName: 'Selena: Pianissimo', fromState: null, toState: null, displayName: 'c' },
  ],
  shadowPrice: [],
  bindingStages: [],
  notes: [],
  computedAt: '2026-10-01T00:00:00Z',
};

describe('the share card', () => {
  it("draws the plan's own numbers: its cost, its days as the plan prints them, its runs and its patch", () => {
    render(<ShareCard plan={PLAN} energyUnit="Serum" gameName="Punishing: Gray Raven" face={{ entity: LUCIA, ranks: [5] }} others={1} />);
    expect(screen.getByRole('img', { name: 'Lucia: Inverse Crown: 1,230 Serum, 27.5 days' })).toBeInTheDocument();
    expect(screen.getByText('41')).toBeInTheDocument();
    expect(screen.getByText(/patch Anchored in Faith · v18/)).toBeInTheDocument();
    expect(screen.getByText(/· and 1 more/)).toBeInTheDocument();
  });

  it('sets a long name on two lines at most', () => {
    expect(wrap('LUCIA: INVERSE CROWN', 18)).toEqual(['LUCIA: INVERSE', 'CROWN']);
    expect(wrap('ONE TWO THREE FOUR FIVE SIX SEVEN EIGHT', 10)).toEqual(['ONE TWO', 'THREE FOUR…']);
  });
});

describe('sharing a plan', () => {
  it('puts the card up for whoever the plan is mostly for, says nothing is uploaded, and closes on Escape', async () => {
    vi.stubGlobal('fetch', vi.fn(async () => new Response(JSON.stringify({ games: [] }), { status: 200, headers: { 'Content-Type': 'application/json' } })));
    const onClose = vi.fn();
    render(
      <QueryClientProvider client={new QueryClient()}>
        <ShareDialog plan={PLAN} energyUnit="Serum" entities={[LUCIA, SELENA]} onClose={onClose} />
      </QueryClientProvider>,
    );
    const dialog = screen.getByRole('dialog', { name: 'Share your plan' });
    expect(dialog).toHaveTextContent('Nothing is uploaded and no link is made.');
    expect(screen.getByRole('img', { name: /^Lucia: Inverse Crown:/ })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Save as PNG' })).toBeInTheDocument();

    await userEvent.keyboard('{Escape}');
    expect(onClose).toHaveBeenCalled();
    vi.unstubAllGlobals();
  });
});
