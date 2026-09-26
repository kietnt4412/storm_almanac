import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import type { Goal } from '../api/client';
import { flatten, moveRow, removeRow, rowsOf, setTarget, targetsBeyond } from './goalRows';
import { TargetPicker } from './TargetPicker';
import { statesOfGraph, tracksOfGraph } from './tracks';

/**
 * S7 of the maintainer's second rehearsal: "Lucia fully built" was about twelve
 * goal rows, each picked from one flat list of 61. A row is now one entity with
 * a target per track, and the list the server keeps is unchanged — so what is
 * pinned here is the list each edit produces.
 */
const STEPS = [
  { fromState: 'level-60', toState: 'level-65' },
  { fromState: 'level-65', toState: 'level-70' },
  { fromState: 'promote-8', toState: 'promote-9', toName: 'Ace ★1', section: 'Growth' },
  { fromState: 'promote-9', toState: 'promote-10', toName: 'Ace ★2', section: 'Growth' },
  { fromState: 'lament-4', toState: 'lament-18', section: 'Basic Skill', tag: 'Red Orb' },
];
const TRACKS = tracksOfGraph(STEPS);
const TARGETS = statesOfGraph(STEPS).targets;
const [LEVEL, PROMOTE, LAMENT] = [
  TRACKS.find((track) => track.name === 'Level')!,
  TRACKS.find((track) => track.name === 'Promote')!,
  TRACKS.find((track) => track.tag === 'Red Orb')!,
];

const goal = (entity: string, targetState: string): Goal => ({ entity, targetState });

describe('goal rows', () => {
  it('groups by entity in order of first appearance, and flattens back with priorities', () => {
    const goals = [goal('lucia', 'level-70'), goal('selena', 'level-65'), goal('lucia', 'lament-18')];
    const rows = rowsOf(goals);
    expect(rows.map((row) => [row.entity, row.goals.map((one) => one.targetState)])).toEqual([
      ['lucia', ['level-70', 'lament-18']],
      ['selena', ['level-65']],
    ]);
    expect(flatten(rows).map((one) => [one.entity, one.targetState, one.priority])).toEqual([
      ['lucia', 'level-70', 0],
      ['lucia', 'lament-18', 1],
      ['selena', 'level-65', 2],
    ]);
  });

  it('sets one target per track, replacing the old one and keeping the row in track order', () => {
    let goals = [goal('lucia', 'lament-18'), goal('selena', 'level-65')];
    goals = setTarget(goals, 'lucia', TRACKS, LEVEL, 'level-65');
    goals = setTarget(goals, 'lucia', TRACKS, LEVEL, 'level-70');
    expect(goals.map((one) => one.targetState)).toEqual(['level-70', 'lament-18', 'level-65']);
  });

  it('clears a track with null, and a row with nothing left disappears', () => {
    const goals = setTarget([goal('lucia', 'level-70'), goal('selena', 'level-65')], 'lucia', TRACKS, LEVEL, null);
    expect(goals).toEqual([{ entity: 'selena', targetState: 'level-65', priority: 0 }]);
  });

  it('starts a row at the end for an entity with none', () => {
    const goals = setTarget([goal('selena', 'level-65')], 'lucia', TRACKS, PROMOTE, 'promote-10');
    expect(goals.map((one) => one.entity)).toEqual(['selena', 'lucia']);
  });

  it('moves and removes a whole row', () => {
    const goals = [goal('lucia', 'level-70'), goal('lucia', 'lament-18'), goal('selena', 'level-65')];
    expect(moveRow(goals, 1, 0).map((one) => one.entity)).toEqual(['selena', 'lucia', 'lucia']);
    expect(removeRow(goals, 'lucia').map((one) => one.entity)).toEqual(['selena']);
  });

  it('offers only what is ahead of where the reader stands', () => {
    expect(targetsBeyond(LEVEL, ['level-65'], TARGETS).map((one) => one.state)).toEqual(['level-70']);
    // Nothing recorded: the base is where they stand, and it is no target.
    expect(targetsBeyond(LEVEL, [], TARGETS).map((one) => one.state)).toEqual(['level-65', 'level-70']);
    expect(targetsBeyond(LAMENT, ['lament-18'], TARGETS)).toEqual([]);
  });
});

describe('the target picker', () => {
  it('leaves every track as it is until one is set, and says where each stands', async () => {
    const onChange = vi.fn();
    render(
      <TargetPicker
        subject="Lucia"
        tracks={TRACKS}
        order={['Growth', 'Basic Skill']}
        targets={TARGETS}
        roster={['level-65', 'lament-18']}
        row={{ entity: 'lucia', goals: [] }}
        onChange={onChange}
      />,
    );

    const promote = screen.getByLabelText('Target for Promote of Lucia');
    expect(promote).toHaveValue('');
    expect(screen.getByLabelText('Target for Level of Lucia')).toHaveValue('');
    expect(screen.getByText('now 65 →')).toBeInTheDocument();
    // Red Orb is at its end: nothing to aim at, and no dropdown to ask with.
    expect(screen.queryByLabelText(/Target for Red Orb/)).not.toBeInTheDocument();

    await userEvent.selectOptions(promote, 'Ace ★2');
    expect(onChange).toHaveBeenCalledWith(PROMOTE, 'promote-10');
    await userEvent.selectOptions(promote, 'leave as is');
    expect(onChange).toHaveBeenLastCalledWith(PROMOTE, null);
  });

  it('keeps a target the reader has since overtaken, marked, rather than dropping it', () => {
    render(
      <TargetPicker
        subject="Lucia"
        tracks={TRACKS}
        targets={TARGETS}
        roster={['level-70']}
        row={{ entity: 'lucia', goals: [goal('lucia', 'level-65')] }}
        onChange={() => {}}
      />,
    );
    expect(screen.getByLabelText('Target for Level of Lucia')).toHaveValue('level-65');
    expect(screen.getByText('65 (reached)')).toBeInTheDocument();
  });
});
