import { useMemo, useState } from 'react';
import { useQueries, useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { getEntities, getRoster, getUpgrades } from '../api/client';
import { ProfileGate } from '../profile';
import { TrackPicker } from '../roster/TrackPicker';
import { tracksOfGraph, type Track } from '../roster/tracks';
import { NextStep } from '../steps/Steps';
import { effectiveRoster, outboxOf, usePlannerStore } from '../store/plannerStore';

/**
 * Where the reader actually stands, for everyone they own.
 *
 * <p><b>Why this is a screen and not a field on a goal row.</b> Until now the
 * only place a roster state could be recorded was beside a goal, which meant a
 * reader could not say they have levelled a construct they are not currently
 * planning for. That is the common case and it is the expensive one: the
 * planner charges for every track it has not been told about, so an unrecorded
 * Lv 80 is billed as the whole level ladder, twice over if a second entity is in
 * the same position. ADR 0027 made the schema able to hold it and ADR 0026 made
 * the solver credit what stands behind it; neither is worth anything if there is
 * nowhere to type it.
 *
 * <p><b>It edits through the same outbox as the inventory.</b> A roster edit is
 * a per-key merge with a client clock, so this screen works offline and a
 * second device's newer answer wins on the keys it touched. The goal screen's
 * own copy of this control is the same component, deliberately: two editors of
 * one aggregate that drift apart is how a reader ends up with two answers to
 * "where am I".
 *
 * <p><b>The upgrade graphs are fetched only for the entities on screen.</b> The
 * goal screen fetches every graph in the catalog, which is fine for a launch
 * title with one construct and will not be for a second game with hundreds.
 * Here the list is what the reader owns plus whoever they are adding, so it
 * stays the size of a roster rather than the size of the game.
 */
export function Roster() {
  return <ProfileGate>{(profile) => <Editor profileId={profile.id} game={profile.game} />}</ProfileGate>;
}

function Editor({ profileId, game }: { profileId: string; game: string }) {
  const entities = useQuery({ queryKey: ['entities', game], queryFn: () => getEntities(game) });
  const stored = useQuery({ queryKey: ['roster', profileId], queryFn: () => getRoster(profileId) });

  const outbox = usePlannerStore((state) => outboxOf(state, profileId));
  const editRosterState = usePlannerStore((state) => state.editRosterState);

  const roster = effectiveRoster(stored.data?.entities ?? {}, outbox.roster);

  // Adding someone records them, at the base of every track (T6). Until
  // 2026-09-28 it only opened a row on screen: an entity with no states is one
  // not on the roster (V13), so nothing was stored until a dropdown changed —
  // and the dropdowns already showed the base, so a reader who owns her
  // untouched had nothing to change, and lost her on reload.
  const [adding, setAdding] = useState('');

  const shown = useMemo(() => Object.keys(roster), [roster]);

  // Every entity's graph, not only the shown ones': the add list offers only
  // someone with a track to stand on. Karenina was offered with nothing to
  // record, which the second rehearsal found. Cached for as long as the patch.
  const everyone = useMemo(
    () => [...new Set([...(entities.data?.entities ?? []).map((entity) => entity.id), ...shown])],
    [entities.data, shown],
  );
  const graphs = useQueries({
    queries: everyone.map((slug) => ({
      queryKey: ['upgrades', game, slug],
      queryFn: () => getUpgrades(game, slug),
      staleTime: Infinity,
    })),
  });

  const tracksOf = useMemo(() => {
    const byEntity = new Map<string, { tracks: Track[]; order: string[] }>();
    graphs.forEach((graph) => {
      if (!graph.data) return;
      byEntity.set(graph.data.entity.id, {
        tracks: tracksOfGraph(graph.data.steps),
        order: graph.data.sections ?? [],
      });
    });
    return byEntity;
  }, [graphs]);

  if (entities.isPending || stored.isPending) return <p className="muted">Loading…</p>;

  const catalog = entities.data?.entities ?? [];
  const nameOf = (slug: string) =>
    catalog.find((candidate) => candidate.id === slug)?.displayName ?? slug;
  const addable = catalog.filter(
    (entity) => !shown.includes(entity.id) && (tracksOf.get(entity.id)?.tracks.length ?? 0) > 0,
  );

  return (
    <div className="space-y-4">
      <header>
        <h1 className="text-xl font-semibold">Roster</h1>
        <p className="muted text-sm">
          Where you already stand, on every track. Anything left off is charged for from the
          beginning — so a construct you have levelled and not recorded is billed as the whole
          ladder. Saved as you type, and sent when there is a connection.
        </p>
      </header>

      {shown.length === 0 ? (
        <p className="muted">
          Nothing recorded yet. Add somebody below, or start from a{' '}
          <Link to={`/catalog/${game}`}>catalog page</Link>, which also says what you are short of
          from wherever you say you are.
        </p>
      ) : (
        <ul className="space-y-2">
          {shown.map((slug) => {
            const states = roster[slug] ?? [];
            const graph = tracksOf.get(slug);
            const tracks = graph?.tracks;
            return (
              <li key={slug} className="card space-y-3">
                <div className="flex items-center gap-3">
                  <Link to={`/catalog/${game}/${slug}`} className="font-medium">
                    {nameOf(slug)}
                  </Link>

                  {/* Null is the one answer that means "not owned" on the wire (V13). */}
                  <button
                    type="button"
                    className="btn-quiet ml-auto text-sm"
                    onClick={() => editRosterState(profileId, slug, null)}
                  >
                    Remove {nameOf(slug)} from the roster
                  </button>
                </div>

                {tracks === undefined ? (
                  <p className="muted text-sm">reading their tracks…</p>
                ) : tracks.length === 0 ? (
                  <p className="muted text-sm">
                    This patch publishes no upgrade graph for them, so there is no state to record.
                  </p>
                ) : (
                  <TrackPicker
                    subject={nameOf(slug)}
                    tracks={tracks}
                    order={graph?.order}
                    states={states}
                    onChange={(next) => editRosterState(profileId, slug, next)}
                  />
                )}
              </li>
            );
          })}
        </ul>
      )}

      <div className="card flex flex-wrap items-end gap-3">
        <div>
          <label className="label" htmlFor="add-roster">
            Add someone
          </label>
          <select
            id="add-roster"
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
            // Only someone with tracks is offered, so there is always a base to stand on.
            const tracks = tracksOf.get(adding)?.tracks ?? [];
            editRosterState(profileId, adding, basesOf(tracks));
            setAdding('');
          }}
        >
          Add
        </button>
        {addable.length === 0 && catalog.length > 0 && (
          <p className="muted text-sm">Everyone this patch gives something to record is already on the list.</p>
        )}
      </div>

      <NextStep from="/roster" />
    </div>
  );
}

/**
 * "Owned, untouched": the first state of every track, which is what the planner
 * charges from and what each dropdown shows before anything is chosen.
 */
export function basesOf(tracks: Track[]): string[] {
  return tracks.map((track) => track.states[0]!.state);
}
