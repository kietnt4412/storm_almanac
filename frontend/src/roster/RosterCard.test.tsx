import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { CompletionRing, completionOf, TrackLadders } from './RosterCard';
import { tracksOfGraph } from './tracks';

/**
 * C2.8's roster card: each track filled to where the reader stands, a ring for
 * all of them together, and a price on every rung not yet reached.
 */
const cogs = (quantity: number) => ({ item: 'cogs', displayName: 'Cogs', quantity });
const STEPS = [
  { fromState: 'promote-0', toState: 'promote-1', fromName: 'Private ★1', toName: 'Sergeant ★1', costs: [cogs(5000)], section: 'Growth' },
  { fromState: 'promote-1', toState: 'promote-2', toName: 'Sergeant ★2', costs: [cogs(10000)], section: 'Growth' },
  { fromState: 'promote-2', toState: 'promote-3', toName: 'Elite ★1', costs: [cogs(15000)], section: 'Growth' },
  { fromState: 'lament-1', toState: 'lament-2', costs: [cogs(1000)], section: 'Basic Skill', tag: 'Red Orb' },
];
const TRACKS = tracksOfGraph(STEPS);

describe('a roster card', () => {
  it('fills each track to the furthest state recorded on it, a crossed state counting as reached', () => {
    // promote-2 alone says promote-1 is behind them (ADR 0026).
    render(<TrackLadders tracks={TRACKS} order={['Growth', 'Basic Skill']} states={['promote-2']} steps={STEPS} />);
    const squares = screen.getAllByTestId('roster-square');
    expect(squares.map((square) => square.dataset.reached)).toEqual(['true', 'true', 'false', 'false']);
  });

  it('counts steps reached over steps on every track in its ring', () => {
    expect(completionOf(TRACKS, ['promote-2'])).toEqual({ climbed: 2, height: 4 });
    render(<CompletionRing climbed={2} height={4} />);
    expect(screen.getByRole('img', { name: '50% of the way up every track' })).toBeInTheDocument();
  });

  it('gives a rung not yet reached its step\'s price, and a reached one none', () => {
    render(<TrackLadders tracks={TRACKS} states={['promote-2']} steps={STEPS} />);
    expect(screen.getAllByRole('tooltip').map((tip) => tip.textContent)).toEqual([
      'Elite ★1 · 15,000 Cogs',
      '2 · 1,000 Cogs',
    ]);
    // Only what is ahead takes focus, so the keyboard reaches every price.
    expect(screen.getAllByTestId('roster-square').filter((square) => square.tabIndex === 0)).toHaveLength(2);
  });
});
