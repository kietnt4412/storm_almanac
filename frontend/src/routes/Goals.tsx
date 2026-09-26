import { useMemo, useState } from 'react';
import { useMutation, useQueries, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import {
  getEntities,
  getGoals,
  getRoster,
  getUpgrades,
  saveGoals,
  type Goal,
} from '../api/client';
import { ProfileGate } from '../profile';
import { TrackPicker } from '../roster/TrackPicker';
import { sectionsOf, stateLabel, statesOfGraph, trackOf, tracksOfGraph, type Track } from '../roster/tracks';
import { NextStep } from '../steps/Steps';
import { effectiveRoster, outboxOf, usePlannerStore } from '../store/plannerStore';

/**
 * What the player wants, in order.
 *
 * <p><b>The order is the content, which is why this screen replaces the whole
 * list rather than merging it.</b> The server has no PATCH for goals and that is
 * a decision rather than a gap: an inventory is a map, so a key is a merge unit
 * and two devices touching different keys have an obvious answer; a goal list is
 * an ordering, and two devices that reordered it have no per-key answer at all.
 * Inventing one here would mean a plan computed against priorities nobody chose.
 *
 * <p><b>The target states come from the game's own upgrade graph</b> — every
 * state reachable for that entity, read off the steps the catalog serves. Nothing
 * here knows what "insight-2" means, and a free-text box would let a player name
 * a state the solver can only refuse.
 *
 * <p>Saving needs a connection, unlike the inventory. That is the honest
 * consequence of the paragraph above rather than an omission: a queued
 * whole-list write would overwrite whatever a second device did in the meantime,
 * which is precisely what the inventory's per-key merge exists to avoid.
 */
export function Goals() {
  return <ProfileGate>{(profile) => <Picker profileId={profile.id} game={profile.game} />}</ProfileGate>;
}

function Picker({ profileId, game }: { profileId: string; game: string }) {
  const entities = useQuery({ queryKey: ['entities', game], queryFn: () => getEntities(game) });
  const saved = useQuery({ queryKey: ['goals', profileId], queryFn: () => getGoals(profileId) });
  const storedRoster = useQuery({ queryKey: ['roster', profileId], queryFn: () => getRoster(profileId) });

  const outbox = usePlannerStore((state) => outboxOf(state, profileId));
  const editRosterState = usePlannerStore((state) => state.editRosterState);

  // Every entity's upgrade graph, so a target can be offered per row without a
  // round trip on every change of the select. The catalog is immutable per
  // version, so these cache for as long as the patch does.
  const graphs = useQueries({
    queries: (entities.data?.entities ?? []).map((entity) => ({
      queryKey: ['upgrades', game, entity.id],
      queryFn: () => getUpgrades(game, entity.id),
      staleTime: Infinity,
    })),
  });

  // Targets and current states are two different lists per entity, and
  // conflating them was a real bug — see statesOfGraph, which is where the
  // distinction now lives, shared with the roster screen.
  const statesOf = useMemo(() => {
    const byEntity = new Map<string, { targets: string[]; tracks: Track[] }>();
    graphs.forEach((graph) => {
      const data = graph.data;
      if (!data) return;
      // In the game's section order, the same order the roster shows them in,
      // so a target is found where the reader already looked for it.
      byEntity.set(data.entity.id, {
        targets: statesOfGraph(data.steps).targets,
        tracks: sectionsOf(tracksOfGraph(data.steps), data.sections).flatMap((section) => section.tracks),
      });
    });
    return byEntity;
  }, [graphs]);

  const roster = effectiveRoster(storedRoster.data?.entities ?? {}, outbox.roster);

  // The editing copy. Seeded from the server once it answers; after that the
  // reader owns it until they save or discard, because a refetch landing on a
  // half-made list would silently throw away an unsaved reorder.
  const [draft, setDraft] = useState<Goal[] | null>(null);
  const goals = draft ?? saved.data?.goals ?? [];
  const dirty = draft !== null;

  const queries = useQueryClient();
  const save = useMutation({
    mutationFn: () => saveGoals(profileId, goals.map((goal, index) => ({ ...goal, priority: index }))),
    onSuccess: (response) => {
      setDraft(null);
      queries.setQueryData(['goals', profileId], response);
      // A plan is about these goals, so it is no longer about the right thing.
      queries.invalidateQueries({ queryKey: ['plan', profileId] });
    },
  });

  const [adding, setAdding] = useState('');

  if (entities.isPending || saved.isPending) return <p className="muted">Loading…</p>;

  const catalog = entities.data?.entities ?? [];
  const addable = catalog.filter((entity) => (statesOf.get(entity.id)?.targets.length ?? 0) > 0);

  return (
    <div className="space-y-4">
      <header className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-xl font-semibold">Goals</h1>
          <p className="muted text-sm">
            Highest first. Every goal has to be paid for, so the order is what gets dropped when a
            budget runs out — not what gets planned.
          </p>
        </div>
        <div className="flex items-center gap-2">
          {dirty && (
            <button type="button" className="btn-quiet" onClick={() => setDraft(null)}>
              Discard
            </button>
          )}
          <button type="button" className="btn" onClick={() => save.mutate()} disabled={!dirty || save.isPending}>
            {save.isPending ? 'Saving…' : dirty ? 'Save goals' : 'Saved'}
          </button>
        </div>
      </header>

      {save.isError && <p className="card text-sm">Could not save: {(save.error as Error).message}</p>}

      {goals.length === 0 ? (
        <p className="muted">
          Nothing yet. Add a character and the state you are taking her to — or start from her{' '}
          <Link to={`/catalog/${game}`}>catalog page</Link>, which also says what you are short of.
        </p>
      ) : (
        <ol className="space-y-2">
          {goals.map((goal, index) => {
            const entity = catalog.find((candidate) => candidate.id === goal.entity);
            const states = statesOf.get(goal.entity);
            const name = entity?.displayName ?? goal.entity;
            const onTrack = states && trackOf(states.tracks, goal.targetState);
            return (
              <li key={`${goal.entity}-${index}`} className="card flex flex-wrap items-center gap-3">
                <span className="count muted w-6 text-right">{index + 1}</span>

                <Link to={`/catalog/${game}/${goal.entity}`} className="font-medium">
                  {entity?.displayName ?? goal.entity}
                </Link>

                <label className="flex items-center gap-2 text-sm">
                  <span className="muted">to</span>
                  <select
                    className="input"
                    value={goal.targetState}
                    aria-label={`Target state for ${name}`}
                    onChange={(event) =>
                      setDraft(
                        goals.map((candidate, at) =>
                          at === index ? { ...candidate, targetState: event.target.value } : candidate,
                        ),
                      )
                    }
                  >
                    {/*
                      Grouped by track and named by it, because "promote-7" is
                      the bundle's id and "Promote · 7" is what the reader is
                      choosing. Until the graph arrives the saved target is the
                      only option, as it was.
                    */}
                    {states
                      ? states.tracks.map((track) => (
                          <optgroup
                            key={track.states[0]!.state}
                            label={track.tag ? `${track.tag} — ${track.name}` : track.name}
                          >
                            {track.states
                              .filter((candidate) => states.targets.includes(candidate.state))
                              .map((candidate) => (
                                <option key={candidate.state} value={candidate.state}>
                                  {stateLabel(states.tracks, candidate.state)}
                                </option>
                              ))}
                          </optgroup>
                        ))
                      : (
                          <option value={goal.targetState}>{goal.targetState}</option>
                        )}
                  </select>
                </label>

                <div className="flex items-center gap-2 text-sm">
                  <span className="muted">from</span>
                  {/*
                    The roster, edited where the goal is. A target without a
                    starting point is the difference between "6 Greater Sigils"
                    and "the whole track twice over", and asking for it only on
                    another screen is how it ends up never being set.

                    The same component the roster screen uses, and that is the
                    point of it being one: two editors of one aggregate that
                    drift apart is how a reader gets two answers to "where am I".

                    Only the goal's own track, since S2: every track here made a
                    goal row thirteen dropdowns long. The rest still count — a
                    gate on another track is charged too — and are one link away.
                  */}
                  {onTrack ? (
                    <TrackPicker
                      subject={name}
                      tracks={states.tracks}
                      only={[onTrack]}
                      states={roster[goal.entity] ?? []}
                      onChange={(next) => editRosterState(profileId, goal.entity, next)}
                    />
                  ) : (
                    <span className="muted">reading her tracks…</span>
                  )}
                  <Link to="/roster" className="text-xs">
                    other tracks
                  </Link>
                </div>

                <div className="ml-auto flex items-center gap-1">
                  <button
                    type="button"
                    className="btn-quiet"
                    disabled={index === 0}
                    aria-label="Move up"
                    onClick={() => setDraft(move(goals, index, index - 1))}
                  >
                    ↑
                  </button>
                  <button
                    type="button"
                    className="btn-quiet"
                    disabled={index === goals.length - 1}
                    aria-label="Move down"
                    onClick={() => setDraft(move(goals, index, index + 1))}
                  >
                    ↓
                  </button>
                  <button
                    type="button"
                    className="btn-quiet"
                    aria-label="Remove"
                    onClick={() => setDraft(goals.filter((_, at) => at !== index))}
                  >
                    ×
                  </button>
                </div>
              </li>
            );
          })}
        </ol>
      )}

      <div className="card flex flex-wrap items-end gap-3">
        <div>
          <label className="label" htmlFor="add-goal">
            Add a goal
          </label>
          <select
            id="add-goal"
            className="input"
            value={adding}
            onChange={(event) => setAdding(event.target.value)}
          >
            <option value="">Choose someone…</option>
            {addable.map((entity) => (
              <option key={entity.id} value={entity.id}>
                {entity.displayName} ({(entity.kindName ?? entity.kind).toLowerCase()})
              </option>
            ))}
          </select>
        </div>
        <button
          type="button"
          className="btn"
          disabled={!adding}
          onClick={() => {
            const first = statesOf.get(adding)?.tracks[0]?.states;
            const furthest = first?.[first.length - 1]?.state;
            if (!furthest) return;
            // The end of the first track is the default target: a player adding
            // a goal is almost never aiming at the first rung of it. The first
            // track and not the last listed state, because with a construct's
            // thirteen tracks the last state listed is the end of the least
            // likely one — the browser run caught a goal defaulting to a skill
            // unlock.
            setDraft([
              ...goals,
              { entity: adding, targetState: furthest, priority: goals.length },
            ]);
            setAdding('');
          }}
        >
          Add
        </button>
        {addable.length === 0 && (
          <p className="muted text-sm">
            This patch publishes no upgrade graph for anyone, so there is no state to aim at.
          </p>
        )}
      </div>

      {/*
        The list is a draft until it is saved, and leaving would lose it, so the
        way on saves first when there is anything to save.
      */}
      <NextStep
        from="/goals"
        before={dirty ? () => save.mutateAsync() : undefined}
        label={dirty ? 'Save and get the plan →' : undefined}
      />
    </div>
  );
}

function move<T>(list: T[], from: number, to: number): T[] {
  const copy = [...list];
  const [item] = copy.splice(from, 1);
  if (item === undefined) return list;
  copy.splice(to, 0, item);
  return copy;
}
