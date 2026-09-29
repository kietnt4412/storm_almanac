import { describe, expect, it } from 'vitest';
import type { ChangeForYou } from '../api/client';
import { detailName, groupChanges } from './changes';
import { tracksOfGraph, type LabelledStep } from './tracks';

/**
 * The shape the real 14 → 18 report had for the rehearsal account: two skills
 * whose one derived 4 → 18 row became fourteen read rows — thirteen steps added
 * and the last re-priced, on each — which the page must say as two lines, not 32.
 */
describe('grouping what a sequence changed', () => {
  const LUCIA = 'lucia-inverse-crown';

  const skill = (name: string, tag: string): LabelledStep[] =>
    Array.from({ length: 17 }, (_, index) => ({
      fromState: `${name}-${index + 1}`,
      toState: `${name}-${index + 2}`,
      section: 'Basic Skill',
      tag,
    }));
  const tracks = new Map([
    [LUCIA, tracksOfGraph([...skill('abyssal-lament', 'Red Orb'), ...skill('dusklit-severance', 'Yellow Orb')])],
  ]);

  const step = (kind: ChangeForYou['kind'], name: string, to: number, detail: string | null = null): ChangeForYou => ({
    kind,
    about: 'upgrade',
    slug: `${LUCIA}-${name}-${to}`,
    name: `Lucia: Inverse Crown to ${name}-${to}`,
    detail,
    before: detail ? '197000' : null,
    after: detail ? '26000' : null,
    entity: LUCIA,
    entityName: 'Lucia: Inverse Crown',
    fromState: `${name}-${to - 1}`,
    toState: `${name}-${to}`,
  });

  const reRead = (name: string): ChangeForYou[] => [
    ...Array.from({ length: 13 }, (_, index) => step('ADDED', name, index + 5)),
    step('CHANGED', name, 18, 'cost cogs'),
    step('CHANGED', name, 18, 'cost skill-point'),
  ];

  it('says a re-read skill curve once per track, by the name the goal screen gives it', () => {
    const lines = groupChanges([...reRead('abyssal-lament'), ...reRead('dusklit-severance')], tracks);

    expect(lines.map(({ name, what }) => ({ name, what }))).toEqual([
      { name: 'Lucia: Inverse Crown · Red Orb', what: '14 steps: 13 new, 1 changed' },
      { name: 'Lucia: Inverse Crown · Yellow Orb', what: '14 steps: 13 new, 1 changed' },
    ]);
  });

  it('groups by entity alone while the tracks are not known yet', () => {
    const lines = groupChanges([...reRead('abyssal-lament'), ...reRead('dusklit-severance')]);

    expect(lines).toHaveLength(1);
    expect(lines[0]).toMatchObject({ name: 'Lucia: Inverse Crown', what: '28 steps: 26 new, 2 changed' });
  });

  it('gives anything else a line of its own, saying what moved from what to what', () => {
    const lines = groupChanges(
      [
        {
          kind: 'CHANGED',
          about: 'stage',
          slug: 'pg-1-1',
          name: 'Ashfall Approach',
          detail: 'drop ore-rough',
          before: '1.4',
          after: '1.6',
        },
        { kind: 'REMOVED', about: 'stage', slug: 'pg-event-1', name: 'Ember Festival', detail: null, before: null, after: null },
        step('CHANGED', 'abyssal-lament', 18, 'cost cogs'),
      ],
      tracks,
      new Map([['ore-rough', 'Rough Ore']]),
    );

    expect(lines.map(({ name, what }) => ({ name, what }))).toEqual([
      { name: 'Ashfall Approach', what: 'Rough Ore drop 1.4 → 1.6' },
      { name: 'Ember Festival', what: 'gone' },
      { name: 'Lucia: Inverse Crown · Red Orb', what: '1 step changed' },
    ]);
  });

  it('names the item a detail is about, and leaves any other detail as it is', () => {
    const items = new Map([['cogs', 'Cogs']]);
    expect(detailName('cost cogs', items)).toBe('Cogs cost');
    expect(detailName('energy', items)).toBe('energy');
    expect(detailName('drop unknown-thing', items)).toBe('drop unknown-thing');
  });
});
