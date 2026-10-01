import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { familiesOf, LadderSlider } from './LadderSlider';
import { rangeCost, sumOf } from './rangeCost';
import { TargetPicker } from './TargetPicker';
import { statesOfGraph, tracksOfGraph } from './tracks';

/**
 * C2.8's goal ladder: what a range costs by the steps' own prices, and what
 * that sum must leave to the plan. The rules are the ones that would charge a
 * reader twice if they were wrong: a choice summed, or a crossed state charged.
 */
const cogs = (quantity: number) => ({ item: 'cogs', displayName: 'Cogs', quantity });
const sp = (quantity: number) => ({ item: 'skill-point', displayName: 'Skill Point', quantity });
const exp = (quantity: number) => ({ kind: 'character-exp', displayName: 'Character EXP', quantity });

const STEPS = [
  { fromState: 'promote-3', toState: 'promote-4', toName: 'Elite ★2', costs: [cogs(20000)], section: 'Growth' },
  { fromState: 'promote-4', toState: 'promote-5', toName: 'Elite ★3', costs: [cogs(25000)], section: 'Growth' },
  { fromState: 'promote-5', toState: 'promote-6', toName: 'Task Force ★1', costs: [cogs(30000)], section: 'Growth' },
  { fromState: 'level-60', toState: 'level-65', costs: [], progress: [exp(400000)] },
  { fromState: 'level-65', toState: 'level-70', costs: [cogs(1000)], progress: [exp(600000)] },
  { fromState: 'lament-4', toState: 'lament-5', costs: [sp(1), cogs(5000)], section: 'Basic Skill', tag: 'Red Orb' },
  // One step at two prices (ADR 0021): the solver picks, so the sum must not.
  { fromState: 'resonance-0', toState: 'resonance-1', costs: [cogs(9000)] },
  { fromState: 'resonance-0', toState: 'resonance-1', costs: [sp(3)] },
];
const TRACKS = tracksOfGraph(STEPS);
const track = (name: string) => TRACKS.find((candidate) => candidate.name === name || candidate.tag === name)!;

describe('what a range costs, by the steps\' own prices', () => {
  it('adds the steps from where the reader stands to the target, and nothing before', () => {
    const promote = track('Promote');
    // Standing at promote-4 (index 1): promote-3 → 4 is behind them, and free.
    const cost = rangeCost(STEPS, promote, 1, 3);
    expect(cost.items).toEqual([cogs(55000)]);
    expect(cost.links).toBe(2);
  });

  it('puts what no inventory holds beside the items, summed by kind', () => {
    const cost = rangeCost(STEPS, track('Level'), 0, 2);
    expect(cost.progress).toEqual([exp(1000000)]);
    expect(cost.items).toEqual([cogs(1000)]);
  });

  it('names a step sold at several prices and adds none of them', () => {
    const cost = rangeCost(STEPS, track('Resonance'), 0, 1);
    expect(cost.items).toEqual([]);
    expect(cost.choices).toEqual([{ fromState: 'resonance-0', toState: 'resonance-1', options: [[cogs(9000)], [sp(3)]] }]);
  });

  it('costs nothing for a target that is not ahead', () => {
    expect(rangeCost(STEPS, track('Promote'), 2, 2).links).toBe(0);
    expect(rangeCost(STEPS, track('Promote'), 3, 1).links).toBe(0);
  });

  it('sums a row\'s tracks item by item', () => {
    const total = sumOf([rangeCost(STEPS, track('Promote'), 0, 1), rangeCost(STEPS, track('Red Orb'), 0, 1)]);
    expect(total.items).toEqual([cogs(25000), sp(1)]);
    expect(total.links).toBe(2);
  });
});

describe('the ladder', () => {
  const promote = track('Promote');

  it('is a slider a screen reader hears by the state\'s name', () => {
    render(<LadderSlider track={promote} at={0} chosen={2} allowed={[1, 2, 3]} label="Target" onChange={() => {}} />);
    const slider = screen.getByRole('slider', { name: 'Target' });
    expect(slider).toHaveValue('2');
    expect(slider).toHaveAttribute('aria-valuetext', 'Elite ★3');
  });

  it('snaps forward to the next state a step arrives at, and back to where they stand means none', () => {
    const onChange = vi.fn();
    render(<LadderSlider track={promote} at={0} chosen={3} allowed={[2, 3]} label="Target" onChange={onChange} />);
    const slider = screen.getByRole('slider');

    fireEvent.change(slider, { target: { value: '1' } });
    expect(onChange).toHaveBeenLastCalledWith('promote-5');
    fireEvent.change(slider, { target: { value: '0' } });
    expect(onChange).toHaveBeenLastCalledWith(null);
  });

  it('names runs of states that share all but their last word, and nothing when that adds nothing', () => {
    expect(familiesOf(promote)).toEqual([
      { name: '3', size: 1 },
      { name: 'Elite', size: 2 },
      { name: 'Task Force', size: 1 },
    ]);
    expect(familiesOf(track('Level'))).toBeNull();
  });
});

describe('a goal row\'s cost', () => {
  it('shows each moved track\'s cost and the row\'s sum, and says what the sum leaves out', () => {
    render(
      <TargetPicker
        subject="Lucia"
        tracks={TRACKS}
        targets={statesOfGraph(STEPS).targets}
        roster={['promote-4', 'level-60']}
        row={{
          entity: 'lucia',
          goals: [
            { entity: 'lucia', targetState: 'promote-6' },
            { entity: 'lucia', targetState: 'level-70' },
          ],
        }}
        steps={STEPS}
        onChange={() => {}}
      />,
    );

    expect(screen.getByText("The steps' own prices").parentElement).toHaveTextContent(
      '1,000,000 Character EXP · 56,000 Cogs',
    );
    expect(screen.getByText(/Not added in: a step sold at several prices, a gate/)).toBeInTheDocument();
  });
});
