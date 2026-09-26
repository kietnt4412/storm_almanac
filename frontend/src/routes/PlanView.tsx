import { useMemo, useState } from 'react';
import { useMutation, useQueries, useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import {
  ApiError,
  getGames,
  getGoals,
  getMeasures,
  getUpgrades,
  solve,
  type Measure,
  type PayingFor,
  type Plan,
  type ShadowPrice,
} from '../api/client';
import { ProfileGate } from '../profile';
import { stateLabel, tracksOfGraph, type Track } from '../roster/tracks';
import { NextStep } from '../steps/Steps';
import { reachOf, usePlannerStore } from '../store/plannerStore';

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

  const run = useMutation<Plan, Error>({
    mutationFn: () => solve(profileId, { energyPerDay, horizonDays, objective, reach }),
  });

  // The tracks of whoever the plan pays for, so each step reads as the goal
  // screen named it. The same query key as there, so they are usually cached.
  const paidFor = [...new Set((run.data?.payingFor ?? []).flatMap((paying) => (paying.entity ? [paying.entity] : [])))];
  const graphs = useQueries({
    queries: paidFor.map((entity) => ({
      queryKey: ['upgrades', game, entity],
      queryFn: () => getUpgrades(game, entity),
      staleTime: Infinity,
    })),
  });
  const tracks = useMemo(() => {
    const byEntity = new Map<string, Track[]>();
    graphs.forEach((graph) => {
      if (graph.data) byEntity.set(graph.data.entity.id, tracksOfGraph(graph.data.steps));
    });
    return byEntity;
  }, [graphs]);

  const energyUnit = games.data?.games.find((published) => published.id === game)?.energyUnit ?? 'energy';
  const hasGoals = (goals.data?.goals.length ?? 0) > 0;

  return (
    <div className="space-y-4">
      <header>
        <h1 className="text-xl font-semibold">Plan</h1>
        <p className="muted text-sm">
          Computed from what you own and what you want — not from anything typed below. What is asked
          for here is what nothing else can tell us: how much you play, and how far you get.
        </p>
      </header>

      {!hasGoals && !goals.isPending && (
        <p className="card">
          No goals saved yet, so there is nothing to plan for. <Link to="/goals">Set some</Link> first —
          an empty plan and "you already have everything" are very different things to be told, and the
          server refuses to confuse them.
        </p>
      )}

      <form
        className="space-y-4"
        onSubmit={(event) => {
          event.preventDefault();
          run.mutate();
        }}
      >
        <div className="card flex flex-wrap items-end gap-4">
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
        {(measures.data?.measures.length ?? 0) > 0 && (
          <section className="card space-y-3">
            <div>
              <h2 className="font-medium">How far do you get?</h2>
              <p className="muted text-sm">
                Some of this game's income is paid by how well you did, and nothing anyone can read
                says how well that is. Leave one alone and the plan counts none of it — which makes
                the plan dearer than the truth rather than cheaper, and it will say so.
              </p>
            </div>
            {measures.data?.measures.map((ladder) => (
              <Ladder
                key={ladder.measure}
                ladder={ladder}
                score={reach[ladder.measure] ?? 0}
                onPick={(score) => setReach(profileId, ladder.measure, score)}
              />
            ))}
          </section>
        )}

        <button type="submit" className="btn" disabled={run.isPending || !hasGoals}>
          {run.isPending ? 'Solving…' : 'Work it out'}
        </button>
      </form>

      {run.isError && <Refusal error={run.error} />}
      {run.data && <Answer plan={run.data} energyUnit={energyUnit} tracks={tracks} />}

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
}: {
  plan: Plan;
  energyUnit: string;
  /** Each paid-for entity's tracks, by id; a step whose entity is missing keeps the server's name. */
  tracks?: Map<string, Track[]>;
}) {
  const paying = plan.payingFor ?? [];
  return (
    <div className="space-y-4">
      <section className="card">
        <div className="flex flex-wrap items-baseline gap-x-8 gap-y-2">
          <div>
            <div className="label">Total {energyUnit.toLowerCase()}</div>
            <div className="count text-2xl font-semibold">{plan.totalEnergy.toLocaleString()}</div>
          </div>
          <div>
            <div className="label">Days</div>
            <div className="count text-2xl font-semibold">{plan.etaDays.toFixed(1)}</div>
          </div>
          <div>
            <div className="label">Objective</div>
            <div>{plan.objective === 'FEWEST_DAYS' ? 'Fewest days' : `Least ${energyUnit.toLowerCase()}`}</div>
          </div>
          <div className="ml-auto text-right text-xs muted">
            <div>
              patch {plan.versionLabel} (v{plan.version})
            </div>
            <div>{new Date(plan.computedAt).toLocaleString()}</div>
          </div>
        </div>

        {(plan.notes.length > 0 || paying.length > 0) && (
          <ul className="mt-3 space-y-1 border-t pt-3 text-sm" style={{ borderColor: 'var(--line)' }}>
            {paying.length > 0 && (
              <li style={{ color: 'var(--signal)' }}>{payingForSentence(paying, tracks)}</li>
            )}
            {plan.notes.map((note) => (
              <li key={note} style={{ color: 'var(--signal)' }}>
                {note}
              </li>
            ))}
          </ul>
        )}
      </section>

      {plan.stages.length === 0 ? (
        <p className="card">
          Nothing to farm: what you already hold covers every goal on the list.
        </p>
      ) : (
        <section className="card">
          <h2 className="mb-2 font-medium">What to run</h2>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left">
                  <th className="label py-1">Stage</th>
                  <th className="label py-1 text-right">Runs</th>
                  <th className="label py-1 text-right">Each</th>
                  <th className="label py-1 text-right">Total</th>
                </tr>
              </thead>
              <tbody>
                {plan.stages.map((run) => (
                  <tr key={run.stage} className="border-t" style={{ borderColor: 'var(--line)' }}>
                    <td className="py-1" title={run.stage}>
                      {run.displayName ?? run.stage}
                      {plan.bindingStages.includes(run.stage) && (
                        <span className="ml-2 text-xs" style={{ color: 'var(--signal)' }}>
                          binding
                        </span>
                      )}
                    </td>
                    <td className="count py-1 text-right">{run.runs}</td>
                    <td className="count py-1 text-right">{run.energyCost}</td>
                    <td className="count py-1 text-right">{run.totalEnergy.toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {plan.bindingStages.length > 0 && (
            <p className="muted mt-2 text-xs">
              A binding stage is one the answer is pressed up against: running it more would change the
              plan, and running anything else more would not.
            </p>
          )}
        </section>
      )}

      {plan.conversions.length > 0 && (
        <section className="card">
          <h2 className="mb-2 font-medium">What to craft and buy</h2>
          <ul className="space-y-1 text-sm">
            {/*
              The totals first, since they are what the reader spends and gets;
              "× 429" beside one purchase read as one cheap buy (S9). A server
              from before the totals sends none, and gets the line it always did.
            */}
            {plan.conversions.map((conversion) => (
              <li key={conversion.step} title={conversion.step}>
                {conversion.total ? (
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
                )}
              </li>
            ))}
          </ul>
        </section>
      )}

      {plan.rewards.length > 0 && (
        <section className="card">
          <h2 className="mb-2 font-medium">What to claim</h2>
          <ul className="space-y-1 text-sm">
            {plan.rewards.map((claim) => (
              <li key={claim.reward} title={claim.reward}>
                {claim.displayName ?? claim.reward} <span className="count muted">× {claim.times}</span>
              </li>
            ))}
          </ul>
        </section>
      )}

      {plan.shadowPrice.length > 0 && <Prices prices={plan.shadowPrice} energyUnit={energyUnit} />}

      <p className="text-xs muted">{plan.attribution}</p>
    </div>
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
          <ul className="grid gap-1 text-sm sm:grid-cols-2">
            {priced.map((price) => (
              <li key={price.item} className="flex justify-between gap-4">
                <span>{price.displayName}</span>
                <span className="count muted">{price.price.toFixed(2)}</span>
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

/**
 * "Paying for 3 upgrade steps: Lucia: Inverse Crown — Level · 65, Red Orb · 18,
 * Promote · Ace ★1." One entity named once, and each state named by its track as
 * the goal screen names it. Until S8 this was the server's sentence, which could
 * only print a state the game gives no word for as its id.
 */
export function payingForSentence(paying: PayingFor[], tracks: Map<string, Track[]>): string {
  const byEntity = new Map<string, { name: string; states: string[] }>();
  for (const step of paying) {
    const known = step.entity && step.toState ? tracks.get(step.entity) : undefined;
    if (!step.entity || !step.toState || !known) {
      // No graph (yet) to name it from: the server's own name, whole.
      byEntity.set(step.step, { name: step.displayName, states: [] });
      continue;
    }
    const entry = byEntity.get(step.entity) ?? { name: step.entityName ?? step.entity, states: [] };
    entry.states.push(stateLabel(known, step.toState));
    byEntity.set(step.entity, entry);
  }
  const parts = [...byEntity.values()].map((entry) =>
    entry.states.length === 0 ? entry.name : `${entry.name} — ${entry.states.join(', ')}`,
  );
  return `Paying for ${paying.length} upgrade step${paying.length === 1 ? '' : 's'}: ${parts.join('; ')}.`;
}
