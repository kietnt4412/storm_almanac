import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { Dossier } from './Dossier';

/**
 * C2.12's dossier: the first track as stairs with the reader on them, and the
 * top of every track in one currency at a time, split by track.
 */
const cogs = (quantity: number) => ({ item: 'cogs', displayName: 'Cogs', quantity });
const sp = (quantity: number) => ({ item: 'sp', displayName: 'Skill Point', quantity });
const STEPS = [
  { id: 'a', fromState: 'promote-0', toState: 'promote-1', fromName: 'Private', toName: 'Sergeant', costs: [cogs(5000)], section: 'Growth' },
  { id: 'b', fromState: 'promote-1', toState: 'promote-2', toName: 'Elite', costs: [cogs(10000)], section: 'Growth' },
  { id: 'c', fromState: 'lament-1', toState: 'lament-2', costs: [cogs(1000), sp(3)], section: 'Basic Skill', tag: 'Red Orb' },
];
const LUCIA = { id: 'lucia', displayName: 'Lucia: Inverse Crown', kind: 'character', rarity: { label: 'S', rank: 5 }, element: null, tags: [] };

function dossier(states?: string[]) {
  render(
    <MemoryRouter>
      <Dossier entity={LUCIA} ranks={[5]} game="punishing-gray-raven" steps={STEPS} states={states} />
    </MemoryRouter>,
  );
}

describe('a dossier', () => {
  it('puts the reader on the first track and says how far there is to go', () => {
    dossier(['promote-1']);
    const stairs = screen.getByRole('region', { name: 'Promote, the climb' });
    expect(within(stairs).getByRole('img', { name: 'You are at Sergeant' })).toBeInTheDocument();
    expect(within(stairs).getByText(/1 step to Elite/)).toBeInTheDocument();
  });

  it('says nothing about where the reader is when nobody is signed in', () => {
    dossier(undefined);
    expect(screen.queryByText(/You are at/)).not.toBeInTheDocument();
    expect(screen.getByText('—')).toBeInTheDocument();
  });

  it('splits the top of every track by track, in the currency the most tracks spend, and switches currency', async () => {
    dossier();
    const split = screen.getByRole('region', { name: 'To the top of every track' });
    expect(within(split).getByRole('img', { name: 'Promote 15,000, Red Orb 1,000' })).toBeInTheDocument();

    await userEvent.click(within(split).getByRole('button', { name: 'Skill Point' }));
    expect(within(split).getByRole('img', { name: 'Red Orb 3' })).toBeInTheDocument();
  });

  it('plans this one through the goal screen', () => {
    dossier();
    expect(screen.getByRole('link', { name: /Plan this/ })).toHaveAttribute('href', '/goals?add=lucia');
  });
});
