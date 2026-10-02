import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { UnitCard } from './UnitCard';
import { tracksOfGraph } from './tracks';

/**
 * C2.11's hangar card: the emblem in a ring for how far up they are, the next
 * rung and its price, and — turned over — what is left to the top of every
 * track, by the steps' own prices.
 */
const cogs = (quantity: number) => ({ item: 'cogs', displayName: 'Cogs', quantity });
const STEPS = [
  { fromState: 'promote-0', toState: 'promote-1', fromName: 'Private', toName: 'Sergeant', costs: [cogs(5000)], section: 'Growth' },
  { fromState: 'promote-1', toState: 'promote-2', toName: 'Elite', costs: [cogs(10000)], section: 'Growth' },
  { fromState: 'lament-1', toState: 'lament-2', costs: [cogs(1000)], section: 'Basic Skill', tag: 'Red Orb' },
];
const TRACKS = tracksOfGraph(STEPS);
const LUCIA = { id: 'lucia', displayName: 'Lucia: Inverse Crown', kind: 'character', rarity: { label: 'S', rank: 5 }, element: null, tags: [] };

function card(states: string[], onEdit = () => {}) {
  render(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter>
        <UnitCard entity={LUCIA} ranks={[5]} game="punishing-gray-raven" tracks={TRACKS} steps={STEPS} states={states} editing={false} onEdit={onEdit} />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('a unit card', () => {
  it('says how far up every track they are, and how many are at the top', () => {
    card(['promote-1', 'lament-2']);
    const front = screen.getByRole('region', { name: 'Lucia: Inverse Crown' });
    expect(within(front).getByText('67%')).toBeInTheDocument();
    expect(within(front).getByText(/1 of 2 at the top/)).toBeInTheDocument();
  });

  it('names the next rung on the first track not at the top, at its own price', () => {
    card(['promote-1']);
    const front = screen.getByRole('region', { name: 'Lucia: Inverse Crown' });
    expect(within(front).getByText('Sergeant → Elite')).toBeInTheDocument();
    expect(within(front).getByText('10,000')).toBeInTheDocument();
  });

  it('turns over to what is left to the top of every track, and the face turned away cannot be reached', async () => {
    card(['promote-0']);
    const back = screen.getByRole('region', { name: 'What is left for Lucia: Inverse Crown', hidden: true });
    expect(back).toHaveAttribute('inert');

    await userEvent.click(screen.getAllByRole('button', { name: /What's left/ })[0]!);

    expect(back).not.toHaveAttribute('inert');
    expect(screen.getByRole('region', { name: 'Lucia: Inverse Crown', hidden: true })).toHaveAttribute('inert');
    // 5,000 + 10,000 up the promote track, 1,000 up the orb.
    expect(within(back).getByText('16,000')).toBeInTheDocument();
  });

  it('asks to edit through the screen that owns the editor', async () => {
    const onEdit = vi.fn();
    card([], onEdit);
    await userEvent.click(screen.getAllByRole('button', { name: /Edit/ })[0]!);
    expect(onEdit).toHaveBeenCalledOnce();
  });
});
