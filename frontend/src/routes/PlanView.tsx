import { useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { ApiError, getGames, getGoals, solve, type Plan } from '../api/client';
import { ProfileGate } from '../profile';

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

  const [energyPerDay, setEnergyPerDay] = useState(240);
  const [horizonDays, setHorizonDays] = useState(14);
  const [objective, setObjective] = useState('LEAST_ENERGY');

  const run = useMutation<Plan, Error>({
    mutationFn: () => solve(profileId, { energyPerDay, horizonDays, objective }),
  });

  const energyUnit = games.data?.games.find((published) => published.id === game)?.energyUnit ?? 'energy';
  const hasGoals = (goals.data?.goals.length ?? 0) > 0;

  return (
    <div className="space-y-4">
      <header>
        <h1 className="text-xl font-semibold">Plan</h1>
        <p className="muted text-sm">
          Computed from what you own and what you want — not from anything typed below. The two numbers
          here are facts about your sitting, not about your account.
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
        className="card flex flex-wrap items-end gap-4"
        onSubmit={(event) => {
          event.preventDefault();
          run.mutate();
        }}
      >
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
        <button type="submit" className="btn" disabled={run.isPending || !hasGoals}>
          {run.isPending ? 'Solving…' : 'Work it out'}
        </button>
      </form>

      {run.isError && <Refusal error={run.error} />}
      {run.data && <Answer plan={run.data} energyUnit={energyUnit} />}
    </div>
  );
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

function Answer({ plan, energyUnit }: { plan: Plan; energyUnit: string }) {
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

        {plan.notes.length > 0 && (
          <ul className="mt-3 space-y-1 border-t pt-3 text-sm" style={{ borderColor: 'var(--line)' }}>
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
                    <td className="py-1">
                      {run.stage}
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
          <h2 className="mb-2 font-medium">What to craft</h2>
          <ul className="space-y-1 text-sm">
            {plan.conversions.map((conversion) => (
              <li key={conversion.step}>
                {conversion.step} <span className="count muted">× {conversion.times}</span>
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
              <li key={claim.reward}>
                {claim.reward} <span className="count muted">× {claim.times}</span>
              </li>
            ))}
          </ul>
        </section>
      )}

      {Object.keys(plan.shadowPrice).length > 0 && (
        <section className="card">
          <h2 className="mb-1 font-medium">What each material is costing you</h2>
          <p className="muted mb-2 text-xs">
            {energyUnit} per extra unit, at this answer. It is what the plan would pay to get one more
            — so it is also what a material is worth when a banner or an event hands you some.
          </p>
          <ul className="grid gap-1 text-sm sm:grid-cols-2">
            {Object.entries(plan.shadowPrice)
              .sort(([, a], [, b]) => b - a)
              .map(([item, price]) => (
                <li key={item} className="flex justify-between gap-4">
                  <span>{item}</span>
                  <span className="count muted">{price.toFixed(2)}</span>
                </li>
              ))}
          </ul>
        </section>
      )}

      <p className="text-xs muted">{plan.attribution}</p>
    </div>
  );
}
