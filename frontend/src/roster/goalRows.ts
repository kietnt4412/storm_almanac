import type { Goal } from '../api/client';
import { standingOn, trackOf, type Track } from './tracks';

/**
 * The goal list, read as one row per entity with one target per track.
 *
 * <p><b>Why rows.</b> A goal is one entity and one state, and "Lucia fully
 * built" was about twelve of them, each picked from one flat list of 61 — S7 of
 * the maintainer's second rehearsal. The server keeps the list it always had,
 * an ordered list of (entity, state); a row is how the screen groups it, and
 * saving a row is saving its goals. So nothing on the wire changed.
 *
 * <p><b>The order is still the content.</b> Rows are in the order their entity
 * first appears, and a row's goals in the order of its tracks, so moving a row
 * moves every goal on it and the flattened list is what gets saved.
 *
 * <p><b>One target per track, the furthest.</b> A goal at rank 13 pays for ranks
 * 9 to 12 on the way, so two goals on one track say what the further one says
 * alone. A list saved by the old screen may hold both; the row shows the
 * further, and saving it keeps only that — the same plan.
 */
export interface GoalRow {
  entity: string;
  goals: Goal[];
}

export function rowsOf(goals: Goal[]): GoalRow[] {
  const rows: GoalRow[] = [];
  for (const goal of goals) {
    const row = rows.find((candidate) => candidate.entity === goal.entity);
    if (row) row.goals.push(goal);
    else rows.push({ entity: goal.entity, goals: [goal] });
  }
  return rows;
}

/** The list the server keeps: the rows in order, each row's goals in order, priority by position. */
export function flatten(rows: GoalRow[]): Goal[] {
  return rows.flatMap((row) => row.goals).map((goal, index) => ({ ...goal, priority: index }));
}

/** The target this row sets on a track: the furthest of its goals on it, if any. */
export function targetOn(track: Track, row: GoalRow): string | undefined {
  return standingOn(track, row.goals.map((goal) => goal.targetState));
}

/**
 * What a track can aim at from where the reader stands: every state after it
 * that some step arrives at. The base of a track is never a target — nothing
 * arrives there — and neither is where they already are.
 */
export function targetsBeyond(track: Track, roster: string[], targets: string[]): Track['states'] {
  const at = standingOn(track, roster);
  const from = at === undefined ? 0 : track.states.findIndex((candidate) => candidate.state === at) + 1;
  return track.states.slice(from).filter((candidate) => targets.includes(candidate.state));
}

/**
 * The list with one track's target on one entity's row replaced, or cleared
 * when {@code state} is null. Every other goal keeps its place; the row's goals
 * are put back in the order of {@code tracks}, with any on no track after them.
 */
export function setTarget(
  goals: Goal[],
  entity: string,
  tracks: Track[],
  track: Track,
  state: string | null,
): Goal[] {
  const rows = rowsOf(goals);
  const onTrack = new Set(track.states.map((candidate) => candidate.state));
  let row = rows.find((candidate) => candidate.entity === entity);
  if (!row) {
    row = { entity, goals: [] };
    rows.push(row);
  }
  const kept = row.goals.filter((goal) => !onTrack.has(goal.targetState));
  const next = state === null ? kept : [...kept, { entity, targetState: state }];
  const position = (goal: Goal) => {
    const index = tracks.findIndex((candidate) => candidate === trackOf(tracks, goal.targetState));
    return index < 0 ? tracks.length : index;
  };
  row.goals = next.sort((a, b) => position(a) - position(b));
  return flatten(rows.filter((candidate) => candidate.goals.length > 0));
}

export function moveRow(goals: Goal[], from: number, to: number): Goal[] {
  const rows = rowsOf(goals);
  const [row] = rows.splice(from, 1);
  if (!row) return goals;
  rows.splice(to, 0, row);
  return flatten(rows);
}

export function removeRow(goals: Goal[], entity: string): Goal[] {
  return flatten(rowsOf(goals).filter((row) => row.entity !== entity));
}
