import { useEffect, useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import {
  ApiError,
  getEntity,
  getGoals,
  getRoster,
  getShortfall,
  getUpgrades,
  signInUrl,
  type Shortfall,
} from '../api/client';
import { Sourcing, merge } from '../catalog/Sourcing';
import { useSelectedProfile } from '../profile';
import { effectiveRoster, outboxOf, usePlannerStore } from '../store/plannerStore';

/**
 * One character page — and the argument for having a catalog at all.
 *
 * <p>The top half is true of the game: rarity, element, the stat curve, what each
 * skill does at each rank, what every step of the upgrade graph costs. A wiki
 * has all of that and probably has it better.
 *
 * <p>The bottom half is true of <em>this reader</em>: where their roster stands,
 * what the state they are aiming at still costs them, and which of those
 * materials they already hold. That is the half an account buys, and it is why
 * the overlay ships with the catalog rather than after it — a catalog page
 * without it is a page this project has no reason to serve.
 *
 * <p>The shortfall is asked of the server rather than computed here from the
 * steps on screen. The rules about what a chain of upgrades <em>means</em> — a
 * state nothing reaches, a state already behind the player — live in the
 * resolver, and a second copy of them in this file is a copy no test would ever
 * run.
 */
export function EntityPage() {
  const { game = '', entity = '' } = useParams();
  const detail = useQuery({ queryKey: ['entity', game, entity], queryFn: () => getEntity(game, entity) });
  const upgrades = useQuery({
    queryKey: ['upgrades', game, entity],
    queryFn: () => getUpgrades(game, entity),
    staleTime: Infinity,
  });

  if (detail.isPending) return <p className="muted">Loading…</p>;
  if (detail.isError) {
    return (
      <div className="card">
        <p>{(detail.error as Error).message}</p>
        <p className="mt-2 text-sm">
          <Link to={`/catalog/${game}`}>Back to the catalog</Link>
        </p>
      </div>
    );
  }

  const page = detail.data.entity;
  const steps = upgrades.data?.steps ?? [];

  return (
    <div className="space-y-5">
      <header>
        <Link to={`/catalog/${game}`} className="text-sm">
          ← Catalog
        </Link>
        <h1 className="mt-1 text-2xl font-semibold">{page.displayName}</h1>
        <p className="muted text-sm">
          {[page.rarity.label, page.kind, page.element, ...page.tags].filter(Boolean).join(' · ')}
        </p>
      </header>

      <Overlay game={game} entity={entity} states={statesOf(steps)} />

      {steps.length > 0 && (
        <section className="card">
          <h2 className="mb-2 font-medium">What each step costs</h2>
          <ul className="space-y-2 text-sm">
            {steps.map((step) => (
              <li key={step.id} className="flex flex-wrap items-baseline gap-x-3">
                <span className="font-medium">
                  {step.fromState} → {step.toState}
                </span>
                <span className="muted">
                  {step.costs.map((cost) => `${cost.quantity.toLocaleString()} ${cost.displayName}`).join(', ')}
                </span>
              </li>
            ))}
          </ul>
        </section>
      )}

      {page.skills.length > 0 && (
        <section className="card">
          <h2 className="mb-2 font-medium">Skills</h2>
          <div className="space-y-3">
            {page.skills.map((skill) => (
              <div key={skill.id}>
                <div className="font-medium">{skill.displayName}</div>
                <ul className="mt-1 space-y-1 text-sm">
                  {skill.ranks.map((rank) => (
                    <li key={rank.rank}>
                      <span className="muted">Rank {rank.rank}:</span> {rank.description}
                      {Object.keys(rank.values).length > 0 && (
                        <span className="muted">
                          {' '}
                          (
                          {Object.entries(rank.values)
                            .map(([name, value]) => `${name} ${value}`)
                            .join(', ')}
                          )
                        </span>
                      )}
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        </section>
      )}

      {page.talents.length > 0 && (
        <section className="card">
          <h2 className="mb-2 font-medium">Talents</h2>
          <ul className="space-y-1 text-sm">
            {page.talents.map((talent) => (
              <li key={talent.id}>
                <span className="font-medium">{talent.displayName}</span>{' '}
                <span className="muted">({talent.unlockCondition})</span> — {talent.effect}
              </li>
            ))}
          </ul>
        </section>
      )}

      {page.statCurves.length > 0 && (
        <section className="card">
          <h2 className="mb-2 font-medium">Stats</h2>
          <div className="space-y-2 text-sm">
            {page.statCurves.map((curve) => (
              <div key={curve.stat}>
                <span className="font-medium">{curve.stat}</span>{' '}
                <span className="muted count">
                  {curve.breakpoints
                    .map((point) => `A${point.ascensionTier} L${point.level}: ${point.value}`)
                    .join(' · ')}
                </span>
              </div>
            ))}
          </div>
        </section>
      )}

      {/*
        Under the numbers rather than over them, and a section rather than the
        one grey line this used to be. The line said who to credit; it could not
        say who read it, off what, on what day — which is the difference between
        ADR 0015 as a sentence in a README and as something the reader can check.
      */}
      <Sourcing
        sourcing={merge(detail.data.sourcing, upgrades.data?.sourcing)}
        attribution={detail.data.version.attribution}
      />
    </div>
  );
}

/** Every state the upgrade graph can reach, in the order the steps arrive. */
function statesOf(steps: { toState: string }[]): string[] {
  const states: string[] = [];
  for (const step of steps) {
    if (!states.includes(step.toState)) states.push(step.toState);
  }
  return states;
}

/**
 * The personalized half: what this reader still needs, from where they are.
 *
 * Signed out it says what signing in would add, rather than disappearing —
 * a blank space teaches nobody that the page has another half.
 */
function Overlay({ game, entity, states }: { game: string; entity: string; states: string[] }) {
  const { profile, signedOut } = useSelectedProfile();
  const goals = useQuery({
    queryKey: ['goals', profile?.id],
    queryFn: () => getGoals(profile!.id),
    enabled: Boolean(profile),
  });
  const storedRoster = useQuery({
    queryKey: ['roster', profile?.id],
    queryFn: () => getRoster(profile!.id),
    enabled: Boolean(profile),
  });
  const outbox = usePlannerStore((state) => outboxOf(state, profile?.id ?? null));

  const goalTarget = goals.data?.goals.find((goal) => goal.entity === entity)?.targetState;
  const [target, setTarget] = useState<string>('');

  // Default to what they already said they want, and only once it has arrived.
  // Aiming the selector somewhere else while their own goal exists would make
  // the page answer a question they did not ask.
  useEffect(() => {
    setTarget(goalTarget ?? states[states.length - 1] ?? '');
  }, [goalTarget, states]);

  const roster = effectiveRoster(storedRoster.data?.entities ?? {}, outbox.roster);
  const currentState = roster[entity];

  const shortfall = useQuery({
    queryKey: ['shortfall', profile?.id, entity, target, currentState],
    queryFn: () => getShortfall(profile!.id, entity, target),
    enabled: Boolean(profile && target),
    retry: (_failures, error) => !(error instanceof ApiError && error.isUnanswerable),
  });

  if (signedOut) {
    return (
      <div className="card">
        <p className="text-sm">
          <a href={signInUrl(`/catalog/${game}/${entity}`)}>Sign in</a> and this page also says what you
          are short of for her, from where your roster actually stands.
        </p>
      </div>
    );
  }

  if (!profile || states.length === 0) return null;

  return (
    <section className="card" style={{ borderColor: 'var(--brand)' }}>
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h2 className="font-medium">What you are short of</h2>
          <p className="muted text-sm">
            {currentState ? `You have her at ${currentState}.` : 'Not on your roster — this is the whole track.'}
          </p>
        </div>
        <label className="flex items-center gap-2 text-sm">
          <span className="muted">Aiming at</span>
          <select
            className="input"
            value={target}
            aria-label="Target state"
            onChange={(event) => setTarget(event.target.value)}
          >
            {states.map((state) => (
              <option key={state} value={state}>
                {state}
              </option>
            ))}
          </select>
        </label>
      </div>

      <div className="mt-3">
        {shortfall.isPending && <p className="muted text-sm">Working it out…</p>}
        {shortfall.isError && (
          <p className="muted text-sm">{(shortfall.error as Error).message}</p>
        )}
        {shortfall.data && <Lines shortfall={shortfall.data} />}
      </div>
    </section>
  );
}

function Lines({ shortfall }: { shortfall: Shortfall }) {
  const missing = useMemo(() => shortfall.items.filter((line) => line.missing > 0), [shortfall.items]);

  if (shortfall.alreadyMet) {
    return <p className="text-sm">You are already at or past {shortfall.targetState}. Nothing to do.</p>;
  }
  if (shortfall.complete) {
    return (
      <p className="text-sm">
        Everything {shortfall.targetState} costs is already in your inventory — you can do it now.
      </p>
    );
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="text-left">
            <th className="label py-1">Material</th>
            <th className="label py-1 text-right">Needed</th>
            <th className="label py-1 text-right">Held</th>
            <th className="label py-1 text-right">Short</th>
          </tr>
        </thead>
        <tbody>
          {shortfall.items.map((line) => (
            <tr key={line.item} className="border-t" style={{ borderColor: 'var(--line)' }}>
              <td className="py-1">{line.displayName}</td>
              <td className="count py-1 text-right">{line.required.toLocaleString()}</td>
              <td className="count py-1 text-right muted">{line.owned.toLocaleString()}</td>
              <td
                className="count py-1 text-right font-medium"
                style={{ color: line.missing > 0 ? 'var(--short)' : 'var(--muted)' }}
              >
                {line.missing.toLocaleString()}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <p className="muted mt-2 text-xs">
        {missing.length} of {shortfall.items.length} materials still short, over{' '}
        {shortfall.steps.length} step{shortfall.steps.length === 1 ? '' : 's'} · patch{' '}
        {shortfall.versionLabel}. <Link to="/plan">Plan the farming</Link>.
      </p>
    </div>
  );
}
