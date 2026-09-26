import { describe, expect, it } from 'vitest';
import type { PayingFor } from '../api/client';
import { tracksOfGraph } from '../roster/tracks';
import { payingForSentence } from './PlanView';

/**
 * The plan's "Paying for" line, S8 of the maintainer's second rehearsal: it
 * read "Lucia: Inverse Crown to Ace ★1, … to level-65, … to abyssal-lament-18",
 * because the server does not guess a name the game never gave. The page names
 * the state the way the goal screen does, from the step itself.
 */
describe('what a plan is paying for', () => {
  const LUCIA = 'lucia-inverse-crown';
  const tracks = new Map([
    [
      LUCIA,
      tracksOfGraph([
        { fromState: 'level-60', toState: 'level-65' },
        { fromState: 'level-65', toState: 'level-70' },
        { fromState: 'promote-8', toState: 'promote-9', toName: 'Ace ★1', section: 'Growth' },
        { fromState: 'abyssal-lament-4', toState: 'abyssal-lament-18', section: 'Basic Skill', tag: 'Red Orb' },
      ]),
    ],
  ]);
  const step = (id: string, fromState: string, toState: string): PayingFor => ({
    step: id,
    entity: LUCIA,
    entityName: 'Lucia: Inverse Crown',
    fromState,
    toState,
    displayName: `Lucia: Inverse Crown to ${toState}`,
  });

  it('names each state by its track, and the construct once', () => {
    expect(
      payingForSentence(
        [
          step('level-65', 'level-60', 'level-65'),
          step('abyssal-lament-18', 'abyssal-lament-4', 'abyssal-lament-18'),
          step('promote-9', 'promote-8', 'promote-9'),
        ],
        tracks,
      ),
    ).toBe('Paying for 3 upgrade steps: Lucia: Inverse Crown — Level · 65, Red Orb · 18, Promote · Ace ★1.');
  });

  it("keeps the server's name while the tracks are still loading, rather than an id", () => {
    expect(payingForSentence([step('level-65', 'level-60', 'level-65')], new Map())).toBe(
      'Paying for 1 upgrade step: Lucia: Inverse Crown to level-65.',
    );
  });
});
