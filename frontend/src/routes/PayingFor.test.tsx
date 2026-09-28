import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import type { PayingFor, Plan } from '../api/client';
import { sectionsOf, tracksOfGraph } from '../roster/tracks';
import { payingForGroups, Remarks } from './PlanView';

/**
 * What a plan pays for. S8 of the maintainer's second rehearsal named each
 * state the way the goal screen does, from the step itself; T3, from the five
 * strangers who closed Phase 4, is that thirty-one of them in one sentence was
 * a wall. A track climbed step by step is one climb and is said once.
 */
describe('what a plan is paying for', () => {
  const LUCIA = 'lucia-inverse-crown';
  const graph = tracksOfGraph([
    { fromState: 'abyssal-lament-1', toState: 'abyssal-lament-2', section: 'Basic Skill', tag: 'Red Orb' },
    { fromState: 'abyssal-lament-2', toState: 'abyssal-lament-3', section: 'Basic Skill', tag: 'Red Orb' },
    { fromState: 'abyssal-lament-3', toState: 'abyssal-lament-4', section: 'Basic Skill', tag: 'Red Orb' },
    { fromState: 'level-1', toState: 'level-2', section: 'Growth' },
    { fromState: 'level-2', toState: 'level-10', section: 'Growth' },
    { fromState: 'level-10', toState: 'level-20', section: 'Growth' },
    { fromState: 'promote-0', toState: 'promote-1', toName: 'Private ★1', fromName: 'Recruit', section: 'Growth' },
    { fromState: 'promote-1', toState: 'promote-2', toName: 'Private ★2', section: 'Growth' },
  ]);
  // The game's order, which is not the order the steps arrive in.
  const tracks = new Map([[LUCIA, sectionsOf(graph, ['Growth', 'Basic Skill']).flatMap((section) => section.tracks)]]);
  const step = (fromState: string, toState: string): PayingFor => ({
    step: toState,
    entity: LUCIA,
    entityName: 'Lucia: Inverse Crown',
    fromState,
    toState,
    displayName: `Lucia: Inverse Crown to ${toState}`,
  });

  it('says each climb once, from where it starts to where it ends, under the game’s headings', () => {
    const groups = payingForGroups(
      [
        step('abyssal-lament-1', 'abyssal-lament-2'),
        step('level-1', 'level-2'),
        step('abyssal-lament-2', 'abyssal-lament-3'),
        step('level-2', 'level-10'),
        step('promote-0', 'promote-1'),
        step('abyssal-lament-3', 'abyssal-lament-4'),
        step('level-10', 'level-20'),
        step('promote-1', 'promote-2'),
      ],
      tracks,
    );

    expect(groups).toEqual([
      {
        name: 'Lucia: Inverse Crown',
        sections: [
          { name: 'Growth', lines: ['Level · 1 → 20', 'Promote · Recruit → Private ★2'] },
          { name: 'Basic Skill', lines: ['Red Orb · 1 → 4'] },
        ],
      },
    ]);
  });

  it('starts a climb where the plan starts it, not at the bottom of the track', () => {
    expect(payingForGroups([step('abyssal-lament-2', 'abyssal-lament-3')], tracks)[0]!.sections).toEqual([
      { name: 'Basic Skill', lines: ['Red Orb · 2 → 3'] },
    ]);
  });

  it("keeps the server's name while the tracks are still loading, rather than an id", () => {
    expect(payingForGroups([step('level-1', 'level-2')], new Map())).toEqual([
      { name: 'Lucia: Inverse Crown to level-2', sections: [] },
    ]);
  });

  it("says a step its tracks do not know by the server's name, rather than dropping it", () => {
    expect(payingForGroups([step('level-80', 'level-90')], tracks)[0]!.sections).toEqual([
      { lines: ['Lucia: Inverse Crown to level-90'] },
    ]);
  });
});

/**
 * The plan's notes, by weight (T3). They were one list in one colour, and the
 * strangers read "this takes at least 14 days" at the same volume as "worked
 * out from 1 stage, 3 shop offers and 3 recipes".
 */
describe('what a plan says about itself', () => {
  const plan = (over: Partial<Plan>): Plan => ({
    id: 'plan-1',
    profile: 'p',
    game: 'punishing-gray-raven',
    version: 15,
    versionLabel: 'Anchored in Faith',
    attribution: 'read from the client',
    objective: 'LEAST_ENERGY',
    stages: [],
    conversions: [],
    rewards: [],
    totalEnergy: 240,
    etaDays: 14,
    shadowPrice: [],
    bindingStages: [],
    notes: [],
    computedAt: '2026-09-28T00:00:00Z',
    ...over,
  });

  it('shows warnings and assumptions, and folds how it was worked out', () => {
    render(
      <Remarks
        plan={plan({
          remarks: [
            { kind: 'ASSUMPTION', text: 'Counting on free income over the horizon: the Cage.' },
            { kind: 'DETAIL', text: 'Worked out from 1 stage that could help.' },
            { kind: 'WARNING', text: 'This takes at least 14 day(s).' },
          ],
        })}
      />,
    );

    expect(screen.getByText('This takes at least 14 day(s).')).toBeVisible();
    expect(screen.getByText('What it assumes')).toBeVisible();
    expect(screen.getByText('Counting on free income over the horizon: the Cage.')).toBeVisible();
    expect(screen.getByText('How this was worked out')).toBeVisible();
    expect(screen.getByText('Worked out from 1 stage that could help.')).not.toBeVisible();
  });

  it('never folds a warning, and shows every note from a server older than the kinds', () => {
    render(<Remarks plan={plan({ notes: ['The search stopped on its time budget.', 'Worked out from 1 stage.'] })} />);

    expect(screen.getByText('The search stopped on its time budget.')).toBeVisible();
    expect(screen.getByText('Worked out from 1 stage.')).toBeVisible();
    expect(screen.queryByText('How this was worked out')).not.toBeInTheDocument();
  });
});
