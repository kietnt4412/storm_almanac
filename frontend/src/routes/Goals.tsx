import { useEffect, useMemo, useState } from 'react';
import { useMutation, useQueries, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useSearchParams } from 'react-router-dom';
import {
  getEntities,
  getGoals,
  getRoster,
  getUpgrades,
  saveGoals,
  type Goal,
  type UpgradeStep,
} from '../api/client';
import { ProfileGate } from '../profile';
import { moveRow, removeRow, rowsOf, setTarget, type GoalRow } from '../roster/goalRows';
import { SkillTree } from '../roster/SkillTree';
import { TargetPicker } from '../roster/TargetPicker';
import { TrackPicker } from '../roster/TrackPicker';
import { sectionsOf, statesOfGraph, tracksOfGraph, type Track } from '../roster/tracks';
import { NextStep } from '../steps/Steps';
import { effectiveRoster, outboxOf, usePlannerStore } from '../store/plannerStore';
import { Emblem } from '../ui/Emblem';
import { ranksByKind } from '../ui/rarity';

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
 * <p><b>One row per entity, one target per track</b> (S7, 2026-09-26): "Lucia
 * fully built" was about twelve rows, each from one flat list of 61. A row is how
 * this screen groups the list; the list saved is the one the server always kept.
 * See {@code goalRows.ts}.
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
    const byEntity = new Map<string, { targets: string[]; tracks: Track[]; order: string[]; steps: UpgradeStep[] }>();
    graphs.forEach((graph) => {
      const data = graph.data;
      if (!data) return;
      // In the game's section order, the same order the roster shows them in,
      // so a target is found where the reader already looked for it.
      byEntity.set(data.entity.id, {
        targets: statesOfGraph(data.steps).targets,
        tracks: sectionsOf(tracksOfGraph(data.steps), data.sections).flatMap((section) => section.tracks),
        order: data.sections ?? [],
        steps: data.steps,
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
      setOpened([]);
      queries.setQueryData(['goals', profileId], response);
      // A plan is about these goals, so it is no longer about the right thing.
      queries.invalidateQueries({ queryKey: ['plan', profileId] });
    },
  });

  const [adding, setAdding] = useState('');
  // Rows the reader has opened and set nothing on yet. They cannot live in the
  // list, because a goal needs a target; the roster screen keeps its opened
  // entities the same way. Nothing is saved for them.
  // `?add=<entity>` opens one, from a character page's "Plan this" (C2.12): the
  // reader arrives with the row they came for already there.
  const [params] = useSearchParams();
  const [opened, setOpened] = useState<string[]>(() => {
    const asked = params.get('add');
    return asked ? [asked] : [];
  });
  // The entity whose "where they stand" is open, one at a time.
  const [standing, setStanding] = useState<string | null>(null);
  // The row being dragged by its grip (C2.8). A drop sends the whole list, as
  // ↑ and ↓ do, which stay for the keyboard and for touch, where HTML drag
  // and drop does not fire.
  const [dragging, setDragging] = useState<number | null>(null);
  // The tree (C2.15) on a wide screen, the ladders everywhere: the ladders are
  // the keyboard's and the phone's way in. The choice is this browser's.
  const wide = useWide();
  const [view, setView] = useState<'tree' | 'ladders'>(() => readView());
  const chooseView = (next: 'tree' | 'ladders') => {
    setView(next);
    try {
      window.localStorage.setItem(VIEW_KEY, next);
    } catch {
      // A preference; the choice still applies to this page.
    }
  };

  if (entities.isPending || saved.isPending) return <p className="muted">Loading…</p>;

  const catalog = entities.data?.entities ?? [];
  const ranks = ranksByKind(catalog, (entity) => entity.kind);
  const savedRows = rowsOf(goals);
  const rows: GoalRow[] = [
    ...savedRows,
    ...opened
      // A link may name someone this patch does not have; that opens nothing.
      .filter((entity) => !savedRows.some((row) => row.entity === entity) && catalog.some((one) => one.id === entity))
      .map((entity) => ({ entity, goals: [] })),
  ];
  const addable = catalog.filter(
    (entity) =>
      (statesOf.get(entity.id)?.targets.length ?? 0) > 0 && !rows.some((row) => row.entity === entity.id),
  );

  return (
    <div className="space-y-4">
      <header className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-xl font-semibold">Goals</h1>
          <p className="muted text-sm">
            One row for everyone you are building, highest first. Set a target on each track you want
            to move and leave the rest. Every goal has to be paid for, so the order is what gets dropped when a
            budget runs out — not what gets planned.
          </p>
        </div>
        <div className="flex items-center gap-2">
          {wide && (
            <div className="flex gap-1" role="group" aria-label="How to set targets">
              {(['tree', 'ladders'] as const).map((value) => (
                <button
                  key={value}
                  type="button"
                  className="chip"
                  aria-pressed={view === value}
                  style={view === value ? { borderColor: 'var(--brand)', background: 'color-mix(in srgb, var(--brand) 14%, var(--surface))' } : undefined}
                  onClick={() => chooseView(value)}
                >
                  {value === 'tree' ? 'Tree' : 'Ladders'}
                </button>
              ))}
            </div>
          )}
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

      {rows.length === 0 ? (
        <p className="muted">
          Nothing yet. Add someone below and set where you are taking them — or start from a{' '}
          <Link to={`/catalog/${game}`}>catalog page</Link>, which also says what you are short of.
        </p>
      ) : (
        <ol className="space-y-2">
          {rows.map((row, index) => {
            const entity = catalog.find((candidate) => candidate.id === row.entity);
            const graph = statesOf.get(row.entity);
            const name = entity?.displayName ?? row.entity;
            const isSaved = index < savedRows.length;
            return (
              <li
                key={row.entity}
                className="card space-y-3"
                style={dragging !== null && dragging !== index && isSaved ? { borderColor: 'var(--brand)' } : undefined}
                onDragOver={(event) => {
                  if (dragging !== null && isSaved) event.preventDefault();
                }}
                onDrop={(event) => {
                  event.preventDefault();
                  if (dragging !== null && isSaved && dragging !== index) setDraft(moveRow(goals, dragging, index));
                  setDragging(null);
                }}
              >
                <div className="flex flex-wrap items-center gap-3">
                  {isSaved && (
                    <span
                      draggable
                      className="muted cursor-grab select-none tracking-[-2px]"
                      title="Drag to reorder"
                      aria-hidden="true"
                      onDragStart={(event) => {
                        event.dataTransfer?.setData('text/plain', row.entity);
                        setDragging(index);
                      }}
                      onDragEnd={() => setDragging(null)}
                    >
                      ⋮⋮
                    </span>
                  )}
                  <span
                    className="count inline-grid h-6 w-6 shrink-0 place-items-center rounded-full text-xs font-bold"
                    style={{ background: 'var(--brand)', color: 'var(--on-brand)' }}
                    aria-label={`Priority ${index + 1}`}
                  >
                    {index + 1}
                  </span>
                  {entity && <Emblem subject={entity} ranks={ranks.get(entity.kind)} game={game} size={36} />}
                  <Link to={`/catalog/${game}/${row.entity}`} className="font-medium">
                    {name}
                  </Link>
                  <span className="muted text-sm">
                    {row.goals.length === 0
                      ? 'nothing set yet'
                      : `${row.goals.length} track${row.goals.length === 1 ? '' : 's'} to move`}
                  </span>
                  {/*
                    The roster, edited where the goal is: a target without a
                    starting point is "the whole track twice over". One entity's
                    at a time, because every entity's at once is the roster
                    screen, which is one link away as well.
                  */}
                  <button
                    type="button"
                    className="btn-quiet text-sm"
                    aria-expanded={standing === row.entity}
                    onClick={() => setStanding(standing === row.entity ? null : row.entity)}
                  >
                    {standing === row.entity ? 'Done' : 'Where they stand'}
                  </button>

                  <div className="ml-auto flex items-center gap-1">
                    <button
                      type="button"
                      className="btn-quiet"
                      disabled={!isSaved || index === 0}
                      aria-label={`Move ${name} up`}
                      onClick={() => setDraft(moveRow(goals, index, index - 1))}
                    >
                      ↑
                    </button>
                    <button
                      type="button"
                      className="btn-quiet"
                      disabled={!isSaved || index >= savedRows.length - 1}
                      aria-label={`Move ${name} down`}
                      onClick={() => setDraft(moveRow(goals, index, index + 1))}
                    >
                      ↓
                    </button>
                    <button
                      type="button"
                      className="btn-quiet"
                      aria-label={`Remove ${name}`}
                      onClick={() => {
                        setOpened(opened.filter((candidate) => candidate !== row.entity));
                        if (isSaved) setDraft(removeRow(goals, row.entity));
                      }}
                    >
                      ×
                    </button>
                  </div>
                </div>

                {!graph ? (
                  <p className="muted text-sm">reading her tracks…</p>
                ) : (
                  <>
                    {standing === row.entity && (
                      <div className="border-l-2 pl-3" style={{ borderColor: 'var(--line)' }}>
                        <p className="label mb-2">Where they stand now</p>
                        <TrackPicker
                          subject={name}
                          tracks={graph.tracks}
                          order={graph.order}
                          states={roster[row.entity] ?? []}
                          onChange={(next) => editRosterState(profileId, row.entity, next)}
                        />
                      </div>
                    )}
                    {wide && view === 'tree' && entity ? (
                    <SkillTree
                      entity={entity}
                      game={game}
                      ranks={ranks.get(entity.kind)}
                      tracks={graph.tracks}
                      order={graph.order}
                      targets={graph.targets}
                      roster={roster[row.entity] ?? []}
                      row={row}
                      steps={graph.steps}
                      onChange={(track, state) => setDraft(setTarget(goals, row.entity, graph.tracks, track, state))}
                    />
                    ) : (
                    <TargetPicker
                      subject={name}
                      tracks={graph.tracks}
                      order={graph.order}
                      targets={graph.targets}
                      roster={roster[row.entity] ?? []}
                      row={row}
                      steps={graph.steps}
                      onChange={(track, state) => setDraft(setTarget(goals, row.entity, graph.tracks, track, state))}
                      onChangeMany={(changes) =>
                        setDraft(
                          changes.reduce(
                            (next, { track, state }) => setTarget(next, row.entity, graph.tracks, track, state),
                            goals,
                          ),
                        )
                      }
                    />
                    )}
                  </>
                )}
              </li>
            );
          })}
        </ol>
      )}

      <div className="card flex flex-wrap items-end gap-3">
        <div>
          <label className="label" htmlFor="add-goal">
            Add someone
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
            // A row with every track left as it is, for the reader to set the
            // ones they want. The old screen guessed a target — the end of the
            // first track — which was one goal of twelve and usually not the one.
            setOpened([...opened, adding]);
            setAdding('');
          }}
        >
          Add
        </button>
        {addable.length === 0 && catalog.length > 0 && (
          <p className="muted text-sm">
            {rows.length > 0
              ? 'Everyone with something ahead of them already has a row.'
              : 'This patch publishes no upgrade graph for anyone, so there is no state to aim at.'}
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


const VIEW_KEY = 'storm-almanac:goal-view';

function readView(): 'tree' | 'ladders' {
  try {
    return window.localStorage.getItem(VIEW_KEY) === 'ladders' ? 'ladders' : 'tree';
  } catch {
    return 'tree';
  }
}

/** Whether the screen is wide enough for the tree; never, where there is no matchMedia. */
function useWide(): boolean {
  const query = '(min-width: 1024px)';
  const [wide, setWide] = useState(() => typeof window.matchMedia === 'function' && window.matchMedia(query).matches);
  useEffect(() => {
    if (typeof window.matchMedia !== 'function') return;
    const list = window.matchMedia(query);
    const change = () => setWide(list.matches);
    list.addEventListener('change', change);
    return () => list.removeEventListener('change', change);
  }, []);
  return wide;
}
