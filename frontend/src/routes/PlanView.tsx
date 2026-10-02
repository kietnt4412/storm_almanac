import { useEffect, useMemo, useRef, useState, type CSSProperties, type ReactNode } from 'react';
import { useMutation, useQueries, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import {
  ApiError,
  getEntities,
  getGames,
  getGoals,
  getMeasures,
  getSavedPlan,
  getUpgrades,
  savePlanDone,
  solve,
  type Measure,
  type PayingFor,
  type Plan,
  type Remark,
  type SavedPlan,
  type ShadowPrice,
} from '../api/client';
import { ProfileGate } from '../profile';
import { sectionsOf, trackOf, tracksOfGraph, type Track } from '../roster/tracks';
import { NextStep } from '../steps/Steps';
import { SinceNotice } from './SinceNotice';
import { reachOf, usePlannerStore } from '../store/plannerStore';
import { SpendBars } from '../ui/SpendBar';
import { Explain } from '../ui/Explain';
import { Icon, type IconName } from '../ui/Icon';
import { Count, Reveal } from '../ui/motion';
import { PlanCircuit, type Faces } from '../ui/PlanCircuit';
import { ShareDialog } from '../ui/ShareDialog';
import { daysOf } from '../ui/time';
import { flowsOf, PlanFlow } from '../ui/PlanFlow';
import { WhyDays } from '../ui/WhyDays';
import { WhatIfPanel, type WhatIfLevers } from './WhatIf';

/**
 * The answer, and what it is worth.
 *
 * <p><b>The notes are not a footer.</b> {@code MipOptimizer} stops at its budget
 * rather than when it has finished, and says how much of the answer it could not
 * prove; a plan rendered without that is a confident number hiding a gap, which
 * is the one thing this project has been careful not to ship. So the notes sit
 * with the total, and the binding stages with the breakdown.
 *
 * <p><b>Energy a day is asked for and never guessed.</b> The route refuses to
 * default it, for a reason worth repeating in the interface: a plan computed
 * against the wrong daily energy is wrong in days without looking wrong.
 *
 * <p><b>So is how far the reader gets in anything the game scores.</b> Some
 * grants stand behind a bar — Punishing: Gray Raven's weekly Phantom Pain Cage
 * pays between 4 and 56 Scars depending on the week somebody had — and there is
 * no screen anywhere that says which of those a given reader is. It is a fact
 * about them, like the energy, so it is asked here and defaults to counting none
 * of it. That makes a plan dearer than the truth rather than cheaper, and every
 * grant left out is named in the notes. ADR 0022.
 *
 * <p><b>A returning reader starts from their last plan.</b> The server keeps the
 * last plan it answered for each profile, and the request that asked for it
 * (C3.1, ADR 0037); the screen opens on that plan, says when it was worked out,
 * and fills the form from its request.
 *
 * <p>A 422 is rendered as an answer, not a failure. "No plan exists for these
 * goals" is a real reply with the reason in it — an item nothing sources, a state
 * nothing reaches — and the sentence the server wrote is more use than anything
 * this screen could say instead.
 */
export function PlanView() {
  return <ProfileGate>{(profile) => <Solver profileId={profile.id} game={profile.game} />}</ProfileGate>;
}

function Solver({ profileId, game }: { profileId: string; game: string }) {
  const goals = useQuery({ queryKey: ['goals', profileId], queryFn: () => getGoals(profileId) });
  const games = useQuery({ queryKey: ['games'], queryFn: getGames });
  // Immutable per published version, like the rest of the catalog.
  const measures = useQuery({
    queryKey: ['measures', game],
    queryFn: () => getMeasures(game),
    staleTime: Infinity,
  });

  const [energyPerDay, setEnergyPerDay] = useState(240);
  const [horizonDays, setHorizonDays] = useState(14);
  const [objective, setObjective] = useState('LEAST_ENERGY');

  const reach = usePlannerStore((state) => reachOf(state, profileId));
  const setReach = usePlannerStore((state) => state.setReach);

  // The last plan this profile was shown, saved by the server with every plan
  // it answers (ADR 0037). Null rather than undefined for "never planned",
  // because a query may not resolve to undefined.
  const queryClient = useQueryClient();
  const saved = useQuery({
    queryKey: ['savedPlan', profileId],
    queryFn: async () => (await getSavedPlan(profileId)) ?? null,
  });

  // The form starts from what was asked last time, once. A reader coming back
  // — or on another device — should not have to retype their energy and how
  // far they get to see where they stood. Reach is seeded only when this
  // browser holds no answer of its own for the profile: a local answer is newer
  // than any plan it has not been sent with yet.
  const seeded = useRef(false);
  useEffect(() => {
    if (seeded.current || !saved.data) return;
    seeded.current = true;
    const asked = saved.data.request;
    setEnergyPerDay(asked.energyPerDay);
    setHorizonDays(asked.horizonDays);
    setObjective(asked.objective);
    if (usePlannerStore.getState().reach[profileId] === undefined) {
      Object.entries(asked.reach ?? {}).forEach(([measure, score]) => setReach(profileId, measure, score));
    }
  }, [saved.data, profileId, setReach]);

  // Whether the inputs are unfolded. Null is "as the screen would have it":
  // open while there is no answer to show, folded to one line once there is.
  const [editing, setEditing] = useState<boolean | null>(null);

  // The rate the last run was asked with, kept apart from the form so editing
  // the form after a plan does not redraw that plan's "Why N days?".
  const [askedRate, setAskedRate] = useState<number | null>(null);
  // A what-if kept (C2.20) asks with its own levers; everything else asks with the form.
  const run = useMutation<Plan, Error, WhatIfLevers | void>({
    mutationFn: (levers) => solve(profileId, levers ?? { energyPerDay, horizonDays, objective, reach }),
    onMutate: (levers) => setAskedRate(levers ? levers.energyPerDay : energyPerDay),
    // A new plan is saved on the latest sequence, so the report of what changed
    // since the last one is out of date too, here and on Home.
    onSuccess: () => {
      setEditing(null);
      queryClient.invalidateQueries({ queryKey: ['savedPlan', profileId] });
      queryClient.invalidateQueries({ queryKey: ['since', profileId] });
    },
  });

  // A what-if kept is the form's answer too: the form shows the levers it was
  // kept with, and the plan route saves it as any plan is saved.
  const [whatIfOpen, setWhatIfOpen] = useState(false);
  const keep = (levers: WhatIfLevers) => {
    setEnergyPerDay(levers.energyPerDay);
    setHorizonDays(levers.horizonDays);
    setObjective(levers.objective);
    Object.entries(levers.reach).forEach(([measure, score]) => setReach(profileId, measure, score));
    run.mutate(levers);
  };

  // What is on screen: the plan just worked out, or else the last one saved.
  const shown = run.data ?? saved.data?.plan;
  const fromBefore = !run.data && !run.isError && saved.data ? saved.data : null;

  // What is ticked off, kept with the saved plan (V20). Only the saved plan can
  // be ticked: a plan just answered is the saved one once it has been read back,
  // and until then there is nothing on the server to keep a tick against.
  const ticking = shown && saved.data && shown.id === saved.data.plan.id ? saved.data : null;
  const [done, setDone] = useState<string[]>([]);
  useEffect(() => setDone(ticking?.done ?? []), [ticking?.savedAt, ticking?.done]);
  const [tickFailed, setTickFailed] = useState(false);
  const tick = useMutation<void, Error, { savedAt: string; done: string[] }>({
    mutationFn: ({ savedAt, done: next }) => savePlanDone(profileId, savedAt, next),
    onMutate: () => setTickFailed(false),
    onSuccess: (_, { savedAt, done: next }) =>
      queryClient.setQueryData<SavedPlan | null>(['savedPlan', profileId], (current) =>
        current && current.savedAt === savedAt ? { ...current, done: next } : current,
      ),
    // A 409 is a plan worked out again elsewhere: read the new one. Anything
    // else puts the server's list back and says the tick did not keep.
    onError: (error) => {
      if (!(error instanceof ApiError && error.status === 409)) setTickFailed(true);
      queryClient.invalidateQueries({ queryKey: ['savedPlan', profileId] });
      setDone(ticking?.done ?? []);
    },
  });
  const toggle = ticking
    ? (key: string) => {
        const next = done.includes(key) ? done.filter((line) => line !== key) : [...done, key];
        setDone(next);
        tick.mutate({ savedAt: ticking.savedAt, done: next });
      }
    : undefined;

  // The tracks of whoever the plan pays for, so each step reads as the goal
  // screen named it — at the plan's own version. Asked for as "the latest",
  // they could come from an older sequence than the plan: a returning reader's
  // Signature Move levels 5–17 printed as ids beside a sequence 15 plan, because
  // the graph they were named from had no such states.
  const paidFor = [...new Set((shown?.payingFor ?? []).flatMap((paying) => (paying.entity ? [paying.entity] : [])))];
  const planVersion = shown?.version;
  const graphs = useQueries({
    queries: paidFor.map((entity) => ({
      queryKey: ['upgrades', game, entity, planVersion],
      queryFn: () => getUpgrades(game, entity, planVersion),
      staleTime: Infinity,
    })),
  });
  const tracks = useMemo(() => {
    const byEntity = new Map<string, Track[]>();
    graphs.forEach((graph) => {
      // In the game's section order, so what the plan pays for reads in the
      // order the roster and goal screens show it.
      if (graph.data) {
        byEntity.set(
          graph.data.entity.id,
          sectionsOf(tracksOfGraph(graph.data.steps), graph.data.sections).flatMap((section) => section.tracks),
        );
      }
    });
    return byEntity;
  }, [graphs]);

  // For the faces the circuit draws on what the plan pays for: the catalog's,
  // so an emblem here is the one the reader saw on every other screen.
  const entities = useQuery({ queryKey: ['entities', game], queryFn: () => getEntities(game) });

  const energyUnit = games.data?.games.find((published) => published.id === game)?.energyUnit ?? 'energy';
  const hasGoals = (goals.data?.goals.length ?? 0) > 0;
  // Only the ladders that pay for something a plan can spend. The daily
  // missions pay only Black Cards, and asking about them here would be a
  // question that moves no plan; the pull screen asks it instead.
  const planLadders = (measures.data?.measures ?? []).filter((ladder) => ladder.paysForPlans !== false);

  return (
    <div className="space-y-4">
      <header>
        <h1 className="text-xl font-semibold">Plan</h1>
        <Explain lead="Computed from what you own and what you want — not from anything typed below.">
          What is asked for here is what nothing else can tell us: how much you play, and how far you get.
        </Explain>
      </header>

      {!hasGoals && !goals.isPending && (
        <p className="card">
          No goals saved yet, so there is nothing to plan for. <Link to="/goals">Set some</Link> first —
          an empty plan and "you already have everything" are very different things to be told, and the
          server refuses to confuse them.
        </p>
      )}

      {/*
        The inputs, folded to one line once there is an answer to look at: a
        returning reader came for the plan, not for the form that asked for it.
        Folded, not removed — the fields stay in the page, filled from the saved
        request, and "Change" opens them.
      */}
      <details
        className="card"
        open={editing ?? !shown}
        // The browser fires a toggle for every change of `open`, React's own
        // included — so one that matches what the screen would show anyway is
        // React's, and only a change against it is the reader's. Taking every
        // toggle kept the inputs open after the saved plan arrived.
        onToggle={(event) => {
          if (event.currentTarget.open !== (editing ?? !shown)) setEditing(event.currentTarget.open);
        }}
      >
        <summary className="flex cursor-pointer list-none flex-wrap items-center gap-x-3 gap-y-1 text-sm">
          <span className="font-medium">{shown ? 'Planned with' : 'What to plan with'}</span>
          <span className="muted count">
            {[
              `${energyPerDay.toLocaleString()} ${energyUnit.toLowerCase()} a day`,
              `${horizonDays} days`,
              objective === 'FEWEST_DAYS' ? 'fewest days' : `least ${energyUnit.toLowerCase()}`,
              ...planLadders
                .filter((ladder) => (reach[ladder.measure] ?? 0) > 0)
                .map((ladder) => `${ladder.displayName ?? ladder.measure} ${reach[ladder.measure]!.toLocaleString()}+`),
            ].join(' · ')}
          </span>
          <span className="ml-auto font-medium" style={{ color: 'var(--brand)' }}>
            {(editing ?? !shown) ? 'Hide' : 'Change'}
          </span>
        </summary>

      <form
        className="mt-4 space-y-4"
        onSubmit={(event) => {
          event.preventDefault();
          run.mutate();
        }}
      >
        <div className="flex flex-wrap items-end gap-4">
          <div>
            <label className="label" htmlFor="energy">
              {energyUnit} a day
            </label>
            <input
              id="energy"
              className="input count w-24"
              type="number"
              min={1}
              value={energyPerDay}
              onChange={(event) => setEnergyPerDay(Number(event.target.value))}
            />
          </div>
          <div>
            <label className="label" htmlFor="horizon">
              Days
            </label>
            <input
              id="horizon"
              className="input count w-20"
              type="number"
              min={1}
              value={horizonDays}
              onChange={(event) => setHorizonDays(Number(event.target.value))}
            />
          </div>
          <div>
            <label className="label" htmlFor="objective">
              Optimise for
            </label>
            <select
              id="objective"
              className="input"
              value={objective}
              onChange={(event) => setObjective(event.target.value)}
            >
              <option value="LEAST_ENERGY">Least {energyUnit.toLowerCase()}</option>
              <option value="FEWEST_DAYS">Fewest days</option>
            </select>
          </div>
        </div>

        {/*
          Absent for a game that scores nothing, rather than an empty card with a
          heading. Most games have no ladder at all and a question with no answers
          under it reads as something broken.
        */}
        {planLadders.length > 0 && (
          <section className="space-y-3 border-t pt-4" style={{ borderColor: 'var(--line)' }}>
            <div>
              <h2 className="font-medium">How far do you get?</h2>
              <Explain lead="Some of this game's income is paid by how well you did.">
                Nothing anyone can read says how well that is. Leave one alone and the plan counts none
                of it — which makes the plan dearer than the truth rather than cheaper, and it will say
                so.
              </Explain>
            </div>
            {planLadders.map((ladder) => (
              <Ladder
                key={ladder.measure}
                ladder={ladder}
                score={reach[ladder.measure] ?? 0}
                onPick={(score) => setReach(profileId, ladder.measure, score)}
              />
            ))}
          </section>
        )}

        <button type="submit" className="btn" data-working={run.isPending || undefined} disabled={run.isPending || !hasGoals}>
          {run.isPending ? 'Solving…' : 'Work it out'}
        </button>
      </form>
      </details>

      {run.isError && <Refusal error={run.error} />}
      {!run.data && (
        <SinceNotice
          profileId={profileId}
          game={game}
          energyUnit={energyUnit}
          replanning={run.isPending}
          onReplan={() => run.mutate()}
        />
      )}
      {fromBefore && (
        <p className="muted text-sm">
          Your last plan, worked out {new Date(fromBefore.savedAt).toLocaleString()} on patch{' '}
          {fromBefore.plan.versionLabel} (v{fromBefore.plan.version}). It counts what you owned then —
          work it out again to plan from what you own now.
        </p>
      )}
      {tickFailed && (
        <p className="text-sm" style={{ color: 'var(--signal)' }}>
          That tick did not save — check your connection and tick it again.
        </p>
      )}
      {shown && !run.isError && (
        <Answer
          // A new answer lands (C2.17); ticking a line is the same answer and does not.
          key={shown.computedAt}
          plan={shown}
          energyUnit={energyUnit}
          tracks={tracks}
          faces={entities.data ? { game, entities: entities.data.entities } : undefined}
          done={done}
          onToggle={toggle}
          energyPerDay={run.data ? askedRate ?? undefined : saved.data?.request.energyPerDay}
          after={
            saved.data && hasGoals ? (
              <WhatIfPanel
                profileId={profileId}
                saved={saved.data}
                energyUnit={energyUnit}
                ladders={planLadders}
                onKeep={keep}
                keeping={run.isPending}
                open={whatIfOpen}
                setOpen={setWhatIfOpen}
              />
            ) : undefined
          }
        />
      )}

      <NextStep from="/plan" />
    </div>
  );
}

/**
 * One scored ladder, asked as "which rung do you clear?".
 *
 * <p><b>A dropdown of the bars rather than a number box, and that is not a
 * simplification.</b> The bars are the only scores that change the answer: a
 * reader who reaches 150 000 on a ladder whose rungs are 120 000 and 360 000 is
 * answering "120 000", and a free number field would invite them to type the
 * real figure and then wonder why the plan did not move. The rungs are the
 * question's whole domain.
 *
 * <p><b>Named by the bundle's word for the measure</b> (ADR 0033, since sequence
 * 13) — "Phantom Pain Cage" — and by the slug where it has none, which nobody
 * has ever seen written down. Either way what a player recognises is the Scars,
 * so what the chosen rung is worth sits beside it — which is also the number
 * that makes the choice worth making, since a ladder pays every rung at or
 * below where you stop rather than only the one you reached.
 */
export function Ladder({
  ladder,
  score,
  onPick,
}: {
  ladder: Measure;
  score: number;
  onPick: (score: number) => void;
}) {
  const cleared = ladder.bars.filter((bar) => bar.atLeast <= score);

  const paid = new Map<string, number>();
  for (const bar of cleared) {
    for (const grant of bar.grants) {
      paid.set(grant.displayName, (paid.get(grant.displayName) ?? 0) + grant.quantity);
    }
  }

  const cadence = ladder.bars[0]?.cadence.toLowerCase() ?? '';

  return (
    <div className="flex flex-wrap items-baseline gap-x-3 gap-y-1">
      <label className="flex items-center gap-2 text-sm">
        <span>{ladder.displayName ?? ladder.measure}</span>
        <select
          className="input"
          value={score}
          aria-label={`How far you get in ${ladder.displayName ?? ladder.measure}`}
          onChange={(event) => onPick(Number(event.target.value))}
        >
          {/*
            Zero is a real answer and stays on the list after it is chosen, rather
            than being a placeholder that disappears. It is also the default,
            because silence and "I don't get there" have the same consequence and
            the generous default is the one that lies.
          */}
          <option value={0}>I don’t get there</option>
          {ladder.bars.map((bar) => (
            <option key={bar.reward} value={bar.atLeast}>
              at least {bar.atLeast.toLocaleString()}
            </option>
          ))}
        </select>
      </label>

      <span className="muted text-sm">
        {cleared.length === 0
          ? `none of its ${ladder.bars.length} tier${ladder.bars.length === 1 ? '' : 's'} counted`
          : `${cleared.length} of ${ladder.bars.length} tiers — ${[...paid]
              .map(([name, quantity]) => `${quantity.toLocaleString()} ${name}`)
              .join(', ')}${cadenceWord(cadence)}`}
      </span>
    </div>
  );
}

/** The game's own cadence, in a word a sentence can end on. */
function cadenceWord(cadence: string): string {
  if (cadence === 'daily') return ' a day';
  if (cadence === 'weekly') return ' a week';
  if (cadence === 'monthly') return ' a month';
  return cadence ? ' once' : '';
}

function Refusal({ error }: { error: Error }) {
  const unanswerable = error instanceof ApiError && error.isUnanswerable;
  return (
    <div className="card">
      <p className="font-medium">{unanswerable ? 'There is no plan for this' : 'That did not work'}</p>
      {/* The server's own sentence: it names the item or the state, which is the
          only thing that makes the refusal actionable. */}
      <p className="muted mt-1 text-sm">{error.message}</p>
    </div>
  );
}

export function Answer({
  plan,
  energyUnit,
  tracks = new Map(),
  done = [],
  onToggle,
  energyPerDay,
  faces,
  after,
}: {
  plan: Plan;
  /** What sits under the answer's card and above its checklist: the what-if panel (C2.20). */
  after?: ReactNode;
  energyUnit: string;
  /** The catalog, for an emblem on each thing the plan pays for; without it, an icon. */
  faces?: Faces;
  /** The rate the plan was asked with, for "Why N days?"; without it, no bar. */
  energyPerDay?: number;
  /** Each paid-for entity's tracks, by id; a step whose entity is missing keeps the server's name. */
  tracks?: Map<string, Track[]>;
  /** The lines ticked off, by {@link lineKey}. */
  done?: string[];
  /** Ticks a line on or off; absent when this plan cannot be ticked, and then there are no boxes. */
  onToggle?: (key: string) => void;
}) {
  const paying = plan.payingFor ?? [];
  const groups = payingForGroups(paying, tracks);
  const spends = plan.conversions.flatMap((conversion) => (conversion.spends ? [conversion.spends] : []));
  const runs = plan.stages.reduce((sum, stage) => sum + stage.runs, 0);
  const [sharing, setSharing] = useState(false);
  return (
    <div className="space-y-4">
      <section className="card-raised land space-y-4">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div className="grid flex-1 grid-cols-3 gap-2 sm:max-w-md">
            <div className="stat-tile rise" style={tileDelay(0)}>
              <div className="stat-label">Total {energyUnit.toLowerCase()}</div>
              <div className="stat-value land-figure text-3xl" style={{ color: 'var(--brand)' }}>
                <Count value={plan.totalEnergy} />
              </div>
            </div>
            <div className="stat-tile rise" style={tileDelay(1)}>
              <div className="stat-label">Days</div>
              <div className="stat-value text-3xl">
                <Count value={plan.etaDays} format={daysOf} />
              </div>
            </div>
            <div className="stat-tile rise" style={tileDelay(2)}>
              <div className="stat-label">Runs</div>
              <div className="stat-value text-3xl">
                <Count value={runs} />
              </div>
            </div>
          </div>
          <div className="flex flex-col items-end gap-2 text-right text-xs muted">
            <div>
              <div>
                patch {plan.versionLabel} (v{plan.version})
              </div>
              <div>{new Date(plan.computedAt).toLocaleString()}</div>
            </div>
            {/* Only where the catalog is to hand, for the card's emblem (C2.13). */}
            {faces && (
              <button type="button" className="btn-quiet" onClick={() => setSharing(true)}>
                <Icon name="share" size={15} /> Share
              </button>
            )}
          </div>
        </div>
        {sharing && faces && (
          <ShareDialog plan={plan} energyUnit={energyUnit} entities={faces.entities} onClose={() => setSharing(false)} />
        )}

        <PlanCircuit plan={plan} energyUnit={energyUnit} faces={faces} />

        {energyPerDay !== undefined && <WhyDays plan={plan} energyPerDay={energyPerDay} energyUnit={energyUnit} />}

        {/* Where the energy goes, from what each stage pays (C2.8); a plan saved
            before stages carried that keeps the bars of where a currency goes. */}
        {flowsOf(plan, energyUnit).length > 0 ? (
          <PlanFlow plan={plan} energyUnit={energyUnit} />
        ) : (
          <SpendBars spends={spends} />
        )}

        {paying.length > 0 && <PaysFor steps={paying.length} groups={groups} />}
        <Remarks plan={plan} />
      </section>

      {after}

      {plan.stages.length === 0 && (
        <p className="card">
          Nothing to farm: what you already hold covers every goal on the list.
        </p>
      )}

      <Reveal>
        <Checklist plan={plan} energyUnit={energyUnit} done={done} onToggle={onToggle} />
      </Reveal>

      {plan.shadowPrice.length > 0 && (
        <Reveal>
          <Prices prices={plan.shadowPrice} energyUnit={energyUnit} />
        </Reveal>
      )}

      <p className="text-xs muted">{plan.attribution}</p>
    </div>
  );
}

/** Moved to `ui/time.ts` (C2.13), so the share card prints days the way this screen does. */
export { daysOf };

/**
 * What names one line of a plan in the list of what is done (V20): what the
 * line does and to what. A stage's id, a step's id and a reward's id are each
 * unique within their own kind, and the kind keeps them apart.
 */
/** The three tiles follow the card in, one after another. */
const tileDelay = (at: number) => ({ '--delay': `${120 + at * 90}ms` }) as CSSProperties;

export function lineKey(kind: 'run' | 'step' | 'claim', id: string): string {
  return `${kind}:${id}`;
}

/**
 * Everything the plan asks the reader to do, as one list to work down: the
 * runs, then what to buy, craft and feed, then what to claim (C2.5, agreed
 * 2026-10-01).
 *
 * <p><b>One list, because it is one job.</b> Until then these were three cards —
 * a table of stages and two lists — and a reader at the game with the plan open
 * beside it had to keep their place in three. Each line keeps the words it had:
 * a purchase still says its totals and how they are made up (S9).
 *
 * <p><b>A tick is the reader's, kept with the saved plan</b> (the maintainer's
 * answer, 2026-10-01), so it follows them to another device and is gone when a
 * new plan replaces this one. A plan that cannot be ticked — one not yet read
 * back as saved — shows the same list without boxes.
 */
function Checklist({
  plan,
  energyUnit,
  done,
  onToggle,
}: {
  plan: Plan;
  energyUnit: string;
  done: string[];
  onToggle?: (key: string) => void;
}) {
  const lines: { key: string; icon: IconName; title: string; label: string; body: ReactNode }[] = [
    ...plan.stages.map((run) => ({
      key: lineKey('run', run.stage),
      icon: 'run' as const,
      title: run.stage,
      label: run.displayName ?? run.stage,
      body: (
        <>
          <span>{run.displayName ?? run.stage}</span>
          {plan.bindingStages.includes(run.stage) && (
            <span className="ml-2 text-xs" style={{ color: 'var(--signal)' }}>
              binding
            </span>
          )}{' '}
          <span className="count muted whitespace-nowrap">
            · {run.runs.toLocaleString()} runs × {run.energyCost} = {run.totalEnergy.toLocaleString()}{' '}
            {energyUnit.toLowerCase()}
          </span>
        </>
      ),
    })),
    ...plan.conversions.map((conversion) => ({
      key: lineKey('step', conversion.step),
      icon: (conversion.spends ? 'shop' : 'feed') as IconName,
      title: conversion.step,
      label: conversion.total ?? conversion.displayName ?? conversion.step,
      // The totals first, since they are what the reader spends and gets;
      // "× 429" beside one purchase read as one cheap buy (S9). A server from
      // before the totals sends none, and gets the line it always did.
      body: conversion.total ? (
        <>
          {conversion.total}
          {/* One unit when it wraps: "429 ×" alone at a line's end reads as a stray number. */}
          {conversion.repeat && (
            <>
              {' '}
              <span className="count muted whitespace-nowrap">· {conversion.repeat}</span>
            </>
          )}
        </>
      ) : (
        <>
          {conversion.displayName ?? conversion.step}{' '}
          <span className="count muted">× {conversion.times}</span>
        </>
      ),
    })),
    ...plan.rewards.map((claim) => ({
      key: lineKey('claim', claim.reward),
      icon: 'gift' as const,
      title: claim.reward,
      label: claim.displayName ?? claim.reward,
      body: (
        <>
          {claim.displayName ?? claim.reward} <span className="count muted">× {claim.times}</span>
        </>
      ),
    })),
  ];
  if (lines.length === 0) return null;

  const ticked = lines.filter((line) => done.includes(line.key)).length;

  return (
    <section className="card">
      <div className="mb-2 flex items-baseline justify-between gap-3">
        <h2 className="font-medium">Do this</h2>
        {onToggle && (
          <span className="muted count text-sm">
            {ticked} of {lines.length} done
          </span>
        )}
      </div>
      <ul className="text-sm">
        {lines.map((line) => {
          const isDone = done.includes(line.key);
          return (
            <li
              key={line.key}
              title={line.title}
              className="flex items-start gap-3 border-t py-2 first:border-t-0"
              style={{ borderColor: 'var(--line)', opacity: isDone ? 0.55 : undefined }}
            >
              {onToggle && (
                <input
                  type="checkbox"
                  className="mt-0.5 h-4 w-4 shrink-0"
                  style={{ accentColor: 'var(--brand)' }}
                  checked={isDone}
                  aria-label={`Done: ${line.label}`}
                  onChange={() => onToggle(line.key)}
                />
              )}
              <span className="muted mt-px">
                <Icon name={line.icon} size={16} />
              </span>
              <span className={isDone ? 'line-through' : undefined}>{line.body}</span>
            </li>
          );
        })}
      </ul>
      {plan.bindingStages.length > 0 && (
        <p className="muted mt-2 text-xs">
          A binding stage is one the answer is pressed up against: running it more would change the
          plan, and running anything else more would not.
        </p>
      )}
    </section>
  );
}

/**
 * What one more of each material would cost, dearest first.
 *
 * <p><b>A zero is named, not listed.</b> Each price is a re-solve with one more
 * unit asked for, so 0.00 is a real answer: one more costs no extra energy,
 * because the plan's runs already make a spare or the material comes from
 * something that is not bought with energy. A column of 0.00 said none of that
 * and read as broken (S10), so the zeros are one sentence that says it and the
 * list keeps the prices that are not.
 */
function Prices({ prices, energyUnit }: { prices: ShadowPrice[]; energyUnit: string }) {
  // A price is the difference of two whole plans' energy, so it is 0 or at
  // least 1; the tolerance only keeps a float from printing as 0.00.
  const free = (priced: ShadowPrice) => Math.abs(priced.price) < 0.005;
  const priced = prices.filter((price) => !free(price)).sort((a, b) => b.price - a.price);
  const spare = prices.filter(free).map((price) => price.displayName);

  return (
    <section className="card">
      <h2 className="mb-1 font-medium">What each material is costing you</h2>
      {priced.length > 0 && (
        <>
          <p className="muted mb-2 text-xs">
            {energyUnit} per extra unit, at this answer. It is what the plan would pay to get one more
            — so it is also what a material is worth when a banner or an event hands you some.
          </p>
          {/* As bars against the dearest (C2.8), so what is precious shows at a glance; the number stays. */}
          <ul className="grid grid-cols-[minmax(6rem,12rem)_1fr_auto] items-center gap-x-3 gap-y-1.5 text-sm">
            {priced.map((price, index) => (
              <li key={price.item} className="contents">
                <span className="truncate">{price.displayName}</span>
                <span className="h-2.5 rounded-full" style={{ background: 'var(--line)' }} aria-hidden="true">
                  <span
                    className="wipe block h-full rounded-full"
                    data-testid="price-bar"
                    style={{
                      width: `${(price.price / priced[0]!.price) * 100}%`,
                      background: 'var(--violet)',
                      '--delay': `${Math.min(index, 12) * 60}ms`,
                    } as CSSProperties}
                  />
                </span>
                <span className="count muted text-right">{price.price.toFixed(2)}</span>
              </li>
            ))}
          </ul>
        </>
      )}
      {spare.length > 0 && (
        <p className={`text-sm ${priced.length > 0 ? 'muted mt-2' : ''}`}>
          No extra {energyUnit.toLowerCase()} for one more of {spare.join(', ')} — this plan already makes a
          spare, or gets {spare.length === 1 ? 'it' : 'them'} without spending {energyUnit.toLowerCase()}.
        </p>
      )}
    </section>
  );
}

function PaysFor({ steps, groups }: { steps: number; groups: PaidFor[] }) {
  return (
    <div className="mt-3 border-t pt-3" style={{ borderColor: 'var(--line)' }}>
      <h2 className="label">
        What this pays for · {steps} step{steps === 1 ? '' : 's'}
      </h2>
      <div className="mt-1 space-y-2 text-sm">
        {groups.map((group) => (
          <div key={group.name}>
            <div className="font-medium">{group.name}</div>
            {group.sections.length > 0 && (
              <ul className="mt-0.5 space-y-0.5">
                {group.sections.map((section, index) => (
                  <li key={section.name ?? `loose-${index}`}>
                    {section.name && <span className="muted">{section.name}: </span>}
                    {section.lines.join(' · ')}
                  </li>
                ))}
              </ul>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

/**
 * What the plan says about itself, by weight rather than in the order written
 * (T3). What changes the answer is shown first and in the signal colour; what
 * the plan takes on trust about the reader is shown plainly, because only the
 * reader can check it; how it was worked out is folded, because it is true of
 * every plan and was most of the wall the strangers could not read. <b>A warning
 * is never folded</b> — a stopped search is one, and a plan shown without its
 * gap is the one thing this page must not be.
 *
 * <p>A server older than the kinds sends only {@code notes}, and each is shown
 * as a warning: visible, exactly as it always was.
 */
export function Remarks({ plan }: { plan: Plan }) {
  const remarks = plan.remarks ?? plan.notes.map((text) => ({ kind: 'WARNING' as const, text }));
  const of = (kind: Remark['kind']) => remarks.filter((remark) => remark.kind === kind).map((remark) => remark.text);
  const warnings = of('WARNING');
  const done = of('DONE');
  const assumed = of('ASSUMPTION');
  const detail = of('DETAIL');
  if (remarks.length === 0) return null;

  return (
    <div className="mt-3 space-y-3 border-t pt-3 text-sm" style={{ borderColor: 'var(--line)' }}>
      {(warnings.length > 0 || done.length > 0) && (
        <ul className="space-y-1">
          {warnings.map((text) => (
            <li key={text} style={{ color: 'var(--signal)' }}>
              {text}
            </li>
          ))}
          {done.map((text) => (
            <li key={text}>{text}</li>
          ))}
        </ul>
      )}
      {assumed.length > 0 && (
        <div>
          <h2 className="label">What it assumes</h2>
          <ul className="mt-1 space-y-1">
            {assumed.map((text) => (
              <li key={text}>{text}</li>
            ))}
          </ul>
        </div>
      )}
      {detail.length > 0 && (
        <details>
          <summary className="muted cursor-pointer">How this was worked out</summary>
          <ul className="muted mt-1 space-y-1">
            {detail.map((text) => (
              <li key={text}>{text}</li>
            ))}
          </ul>
        </details>
      )}
    </div>
  );
}

/** One entity a plan pays for, its steps as ranges under the game's headings. */
export interface PaidFor {
  name: string;
  /** In the game's order; a section with no name holds steps no track could name. */
  sections: { name?: string; lines: string[] }[];
}

/**
 * What a plan pays for, as a reader would say it: "Selena: Pianissimo — Growth:
 * Level · 1 → 50, Promote · Private ★1 → Task Force ★2; Special Skill:
 * Signature Move · 1 → 18".
 *
 * <p>Until 2026-09-28 this was one sentence naming every step — thirty-one for
 * one construct, "Level · 2, Promote · 1, Level · 10, …, Fugal sonata · 18" —
 * and the strangers who closed Phase 4 read the plan as a wall (T3). A track
 * climbed step by step is one climb, so it is said once, from where the plan
 * starts it to where it ends, under the heading the roster and goal screens put
 * it under, with its states named exactly as those screens name them. Until S8
 * the sentence was the server's, which could only print a state the game gives
 * no word for as its id.
 *
 * @param tracks each entity's tracks in the game's section order; a step whose
 *               entity has none yet keeps the server's name, whole
 */
export function payingForGroups(paying: PayingFor[], tracks: Map<string, Track[]>): PaidFor[] {
  interface Building {
    name: string;
    known?: Track[];
    spans: Map<Track, { from: number; to: number }>;
    loose: string[];
  }
  const byEntity = new Map<string, Building>();
  for (const step of paying) {
    const known = step.entity ? tracks.get(step.entity) : undefined;
    if (!step.entity || !step.toState || !known) {
      // No graph (yet) to name it from: the server's own name, whole.
      byEntity.set(step.step, { name: step.displayName, spans: new Map(), loose: [] });
      continue;
    }
    const entry: Building = byEntity.get(step.entity) ?? {
      name: step.entityName ?? step.entity,
      known,
      spans: new Map(),
      loose: [],
    };
    byEntity.set(step.entity, entry);
    const track = trackOf(known, step.toState);
    const to = track ? track.states.findIndex((candidate) => candidate.state === step.toState) : -1;
    if (!track || to < 0) {
      entry.loose.push(step.displayName);
      continue;
    }
    const at = step.fromState ? track.states.findIndex((candidate) => candidate.state === step.fromState) : -1;
    const from = at >= 0 ? at : Math.max(0, to - 1);
    const span = entry.spans.get(track);
    entry.spans.set(track, span ? { from: Math.min(span.from, from), to: Math.max(span.to, to) } : { from, to });
  }

  return [...byEntity.values()].map((entry) => {
    const sections: PaidFor['sections'] = [];
    for (const track of entry.known ?? []) {
      const span = entry.spans.get(track);
      if (!span) continue;
      const line = `${track.tag ?? track.name} · ${track.states[span.from]!.label} → ${track.states[span.to]!.label}`;
      const last = sections[sections.length - 1];
      if (last && last.name === track.section) last.lines.push(line);
      else sections.push({ name: track.section, lines: [line] });
    }
    if (entry.loose.length > 0) sections.push({ lines: entry.loose });
    return { name: entry.name, sections };
  });
}
