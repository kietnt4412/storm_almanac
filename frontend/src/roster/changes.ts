import type { ChangeForYou } from '../api/client';
import { trackOf, type Track } from './tracks';

/**
 * What a new sequence changed for one reader, said a line per thing they would
 * recognise rather than a line per fact (C3.3).
 *
 * <p><b>Grouped, because the report is right and unreadable.</b> On real data,
 * sequences 14 → 18 made 32 changes to one reader's plan and 28 of them were
 * one reading of one skill curve: two skills' fourteen levels, each a step
 * added or re-priced. A reader wants "Lucia: Inverse Crown · Red Orb, 14 steps"
 * said once, named the way the goal and roster screens name that track.
 *
 * <p>Anything that is not an upgrade step keeps a line of its own, with what
 * moved and from what to what, because a stage's drop moving is the one line
 * of the report a reader most needs to read whole.
 */

export interface ChangeLine {
  key: string;
  /** What changed: "Lucia: Inverse Crown · Red Orb", "Ashfall Approach". */
  name: string;
  /** How: "14 steps: 13 new, 1 changed", "Rough Ore drop 1.4 → 1.6". */
  what: string;
}

/**
 * @param tracks    each entity's tracks, by id, at the latest sequence; an
 *                  entity with none (yet) is grouped whole under its name
 * @param itemNames item names by id, to say "Rough Ore drop" for {@code drop ore-rough}
 */
export function groupChanges(
  changes: ChangeForYou[],
  tracks: Map<string, Track[]> = new Map(),
  itemNames: Map<string, string> = new Map(),
): ChangeLine[] {
  interface Steps {
    name: string;
    added: Set<string>;
    changed: Set<string>;
    removed: Set<string>;
  }
  interface Subject {
    name: string;
    changes: ChangeForYou[];
  }
  // One map keeps the order each group was first met in, which is the server's.
  const groups = new Map<string, Steps | Subject>();

  for (const change of changes) {
    if (change.about === 'upgrade' && change.entity) {
      const known = tracks.get(change.entity);
      const track =
        known && ((change.toState && trackOf(known, change.toState)) || (change.fromState && trackOf(known, change.fromState)));
      const whose = change.entityName ?? change.entity;
      const key = track ? `upgrade|${change.entity}|${known!.indexOf(track)}` : `upgrade|${change.entity}`;
      const group = (groups.get(key) as Steps | undefined) ?? {
        name: track ? `${whose} · ${track.tag ?? track.name}` : whose,
        added: new Set<string>(),
        changed: new Set<string>(),
        removed: new Set<string>(),
      };
      groups.set(key, group);
      (change.kind === 'ADDED' ? group.added : change.kind === 'REMOVED' ? group.removed : group.changed).add(change.slug);
      continue;
    }
    const key = `${change.about}|${change.slug}`;
    const group = (groups.get(key) as Subject | undefined) ?? { name: change.name, changes: [] };
    groups.set(key, group);
    group.changes.push(change);
  }

  return [...groups.entries()].map(([key, group]) => ({
    key,
    name: group.name,
    what: 'changes' in group ? subjectWhat(group.changes, itemNames) : stepsWhat(group),
  }));
}

function stepsWhat(group: { added: Set<string>; changed: Set<string>; removed: Set<string> }): string {
  const parts = [
    { count: group.added.size, word: 'new' },
    { count: group.changed.size, word: 'changed' },
    { count: group.removed.size, word: 'gone' },
  ].filter((part) => part.count > 0);
  const total = parts.reduce((sum, part) => sum + part.count, 0);
  const steps = (n: number) => `${n} step${n === 1 ? '' : 's'}`;
  if (parts.length === 1) {
    const [only] = parts;
    return only!.word === 'new' ? `${total} new step${total === 1 ? '' : 's'}` : `${steps(total)} ${only!.word}`;
  }
  return `${steps(total)}: ${parts.map((part) => `${part.count} ${part.word}`).join(', ')}`;
}

function subjectWhat(changes: ChangeForYou[], itemNames: Map<string, string>): string {
  return changes
    .map((change) => {
      if (change.kind === 'ADDED') return 'new';
      if (change.kind === 'REMOVED') return 'gone';
      return `${detailName(change.detail, itemNames)} ${number(change.before)} → ${number(change.after)}`;
    })
    .join('; ');
}

/** "drop ore-rough" as "Rough Ore drop"; a detail naming no item is said as it is. */
export function detailName(detail: string | null, itemNames: Map<string, string>): string {
  if (!detail) return '';
  const [field, ...rest] = detail.split(' ');
  const item = itemNames.get(rest.join(' '));
  return item ? `${item} ${field}` : detail;
}

/** A whole number with its thousands marked, as every other number on the page is; anything else as sent. */
function number(value: string | null): string {
  if (value === null) return '(none)';
  return /^\d+$/.test(value) ? Number(value).toLocaleString() : value;
}
