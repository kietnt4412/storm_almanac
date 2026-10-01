import { fireEvent, render } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { SkillTree } from './SkillTree';
import { tracksOfGraph } from './tracks';

/**
 * C2.15: the goal screen's tree. It is the ladders drawn another way, so what
 * it must get right is the same: where the reader stands is lit, only a state
 * some step arrives at can be aimed at, and a click sends the ladder's change.
 */
const cogs = (quantity: number) => ({ item: 'cogs', displayName: 'Cogs', quantity });
const STEPS = [
  { fromState: 'promote-0', toState: 'promote-1', fromName: 'Private', toName: 'Sergeant', costs: [cogs(5000)], section: 'Growth' },
  { fromState: 'promote-1', toState: 'promote-2', toName: 'Elite', costs: [cogs(10000)], section: 'Growth' },
  { fromState: 'promote-2', toState: 'promote-3', toName: 'Hero', costs: [cogs(20000)], section: 'Growth' },
  { fromState: 'lament-1', toState: 'lament-2', costs: [cogs(1000)], section: 'Basic Skill', tag: 'Red Orb' },
];
const TRACKS = tracksOfGraph(STEPS);
const LUCIA = { id: 'lucia', displayName: 'Lucia', kind: 'character', rarity: { label: 'S', rank: 5 }, element: null, tags: [] };

function tree(goal: string[], onChange = vi.fn(), targets = ['promote-1', 'promote-2', 'promote-3', 'lament-2']) {
  const { container } = render(
    <SkillTree
      entity={LUCIA}
      game="punishing-gray-raven"
      tracks={TRACKS}
      targets={targets}
      roster={['promote-1']}
      row={{ entity: 'lucia', goals: goal.map((targetState) => ({ entity: 'lucia', targetState, priority: 0 })) }}
      steps={STEPS}
      onChange={onChange}
    />,
  );
  const promote = [...container.querySelectorAll('.tree-spoke')].find((spoke) => spoke.textContent?.includes('Promote'))!;
  const nodes = [...promote.querySelectorAll('.tree-node')];
  return { onChange, nodes, states: nodes.map((node) => node.getAttribute('data-state')) };
}

describe('the skill tree', () => {
  it('lights where the reader stands and the path to the goal', () => {
    expect(tree(['promote-3']).states).toEqual(['have', 'have', 'want', 'want']);
    expect(tree([]).states).toEqual(['have', 'have', 'open', 'open']);
  });

  it('aims a track at a node ahead, and leaves it when the target or a reached node is clicked', () => {
    const ahead = tree([]);
    fireEvent.click(ahead.nodes[3]!);
    expect(ahead.onChange).toHaveBeenLastCalledWith(TRACKS.find((track) => track.name === 'Promote'), 'promote-3');
  });

  it('clears on the target itself, and on anything behind the reader', () => {
    const set = tree(['promote-3']);
    fireEvent.click(set.nodes[3]!);
    expect(set.onChange).toHaveBeenLastCalledWith(expect.anything(), null);
    fireEvent.click(set.nodes[0]!);
    expect(set.onChange).toHaveBeenLastCalledWith(expect.anything(), null);
  });

  it('offers nothing no step arrives at', () => {
    const closed = tree([], vi.fn(), ['promote-1', 'promote-2', 'lament-2']);
    expect(closed.states[3]).toBe('closed');
    fireEvent.click(closed.nodes[3]!);
    expect(closed.onChange).not.toHaveBeenCalled();
  });
});
