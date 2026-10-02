import { useEffect, useLayoutEffect, useMemo, useRef, useState, type CSSProperties } from 'react';
import { keepPreviousData, useQuery } from '@tanstack/react-query';
import {
  ApiError,
  whatIf,
  type Measure,
  type Plan,
  type SavedPlan,
  type ShadowPrice,
  type WhatIf,
  type WhatIfAsk,
} from '../api/client';
import { Explain } from '../ui/Explain';
import { Count, Odometer, prefersMotion, useFlip } from '../ui/motion';
import { daysOf } from '../ui/time';
import { Ladder } from './PlanView';

/**
 * What if… (C2.20, agreed 2026-10-02): the saved plan's question, asked again
 * while the reader drags it.
 *
 * <p><b>Nothing here is saved.</b> Every answer comes from
 * `POST /plan/what-if`, which the server keeps no record of; "Keep this" is
 * the one way out, and it asks the ordinary plan route with the same levers,
 * which saves as it always has. Pretended items cannot be kept — they are not
 * the reader's — so Keep waits until they are cleared.
 *
 * <p><b>A drag is a stream of questions, and only the last one matters.</b>
 * The levers settle for 250 ms before they are asked; the question is the query
 * key, so asking again what was already asked is answered from the page's own
 * cache, and a question the reader has moved past is aborted (React Query
 * cancels a query nobody observes once its signal has been read). The sliders
 * move in steps, so most of a drag is questions the server's cache has seen.
 *
 * <p><b>A shadow price tests itself.</b> The price is what one more unit is
 * worth to the plan with fractional runs — the LP relaxation — so "+10" is a
 * prediction: price × 10. Pressing it solves with ten more held and puts the
 * prediction beside what the whole-run plan actually saved. When they differ,
 * that is the lesson rather than a bug, and the row says why.
 */
export interface WhatIfLevers {
  energyPerDay: number;
  horizonDays: number;
  objective: string;
  reach: Record<string, number>;
}

/** A shadow price being tested: what it predicted, and the question whose answer settles it. */
interface Test {
  item: string;
  name: string;
  count: number;
  predicted: number;
  before: number;
  asked: string;
}

/** An answer, kept with the question it answers, so a test can tell whose answer it is. */
type Answered = WhatIf & { asked: string };

const SETTLE_MS = 250;
const LEAVE_MS = 350;

export function WhatIfPanel({
  profileId,
  saved,
  energyUnit,
  ladders,
  onKeep,
  keeping,
  open,
  setOpen,
}: {
  profileId: string;
  saved: SavedPlan;
  energyUnit: string;
  ladders: Measure[];
  onKeep: (levers: WhatIfLevers) => void;
  keeping: boolean;
  /** Held by the screen, so a kept plan landing — which remounts the answer — leaves the panel open. */
  open: boolean;
  setOpen: (open: boolean) => void;
}) {
  const start: WhatIfLevers = useMemo(
    () => ({
      energyPerDay: saved.request.energyPerDay,
      horizonDays: saved.request.horizonDays,
      objective: saved.request.objective,
      reach: saved.request.reach ?? {},
    }),
    [saved.request],
  );
  const [levers, setLevers] = useState<WhatIfLevers>(start);
  const [extra, setExtra] = useState<Record<string, number>>({});
  const [test, setTest] = useState<Test | null>(null);

  // A newly kept plan is the new starting point: the levers go back to it.
  useEffect(() => {
    setLevers(start);
    setExtra({});
    setTest(null);
  }, [start]);

  const ask: WhatIfAsk = { ...levers, extra };
  const asked = JSON.stringify(ask);
  const settled = useSettled(asked, SETTLE_MS);

  const answer = useQuery<Answered, Error>({
    queryKey: ['whatIf', profileId, settled],
    queryFn: async ({ signal }) => ({ ...(await whatIf(profileId, JSON.parse(settled), signal)), asked: settled }),
    enabled: open,
    placeholderData: keepPreviousData,
    // The answer depends on the inventory too, which another screen can change.
    staleTime: 60_000,
    retry: false,
  });

  const plan = answer.data?.plan;
  const waiting = open && (asked !== settled || answer.isFetching);

  // A test is settled by the answer to its own question, and only that one.
  const verdict = test && answer.data?.asked === test.asked ? test.before - answer.data.plan.totalEnergy : null;

  const move = (next: Partial<WhatIfLevers>) => {
    setTest(null);
    setLevers((current) => ({ ...current, ...next }));
  };
  const pretend = (price: ShadowPrice, count: number) => {
    if (!plan) return;
    const next = { ...extra, [price.item]: (extra[price.item] ?? 0) + count };
    setExtra(next);
    setTest({
      item: price.item,
      name: price.displayName,
      count,
      predicted: Math.round(price.price * count),
      before: plan.totalEnergy,
      asked: JSON.stringify({ ...levers, extra: next }),
    });
  };
  const reset = () => {
    setLevers(start);
    setExtra({});
    setTest(null);
  };

  const pretending = Object.keys(extra).length > 0;
  const unchanged = sameLevers(levers, start);

  if (!open) {
    return (
      <section className="card flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="font-medium">What if…</h2>
          <p className="muted text-sm">
            Drag your {energyUnit.toLowerCase()}, days or goal, and the plan is worked out again as you go. Nothing
            is saved until you keep it.
          </p>
        </div>
        <button type="button" className="btn" onClick={() => setOpen(true)}>
          Try a what-if
        </button>
      </section>
    );
  }

  return (
    <section className="card space-y-4" aria-label="What if">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <div>
          <h2 className="font-medium">What if…</h2>
          <Explain lead="Your saved plan, asked again with different numbers. Nothing here is saved.">
            Each change is solved by the same optimizer as the plan, on the server, as you move. A shadow price's
            "+" pretends you hold more of that item, to see whether the price was right.
          </Explain>
        </div>
        <button type="button" className="btn-quiet text-sm" onClick={() => setOpen(false)}>
          Close
        </button>
      </div>

      <div className="grid gap-6 md:grid-cols-[minmax(0,1fr)_minmax(0,1.2fr)]">
        <div className="space-y-3">
          <Slider
            id="what-if-energy"
            label={`${energyUnit} a day`}
            value={levers.energyPerDay}
            min={10}
            max={Math.max(600, Math.ceil((start.energyPerDay * 2) / 10) * 10)}
            step={10}
            onChange={(energyPerDay) => move({ energyPerDay })}
          />
          <Slider
            id="what-if-days"
            label="Within (days)"
            value={levers.horizonDays}
            min={1}
            max={Math.max(60, start.horizonDays * 2)}
            step={1}
            onChange={(horizonDays) => move({ horizonDays })}
          />
          <div className="flex flex-wrap items-center gap-2 text-sm">
            <span className="muted w-28">Optimise for</span>
            {(['LEAST_ENERGY', 'FEWEST_DAYS'] as const).map((objective) => (
              <button
                key={objective}
                type="button"
                className="btn-quiet"
                aria-pressed={levers.objective === objective}
                style={levers.objective === objective ? { borderColor: 'var(--brand)', color: 'var(--brand)' } : undefined}
                onClick={() => move({ objective })}
              >
                {objective === 'FEWEST_DAYS' ? 'Fewest days' : `Least ${energyUnit.toLowerCase()}`}
              </button>
            ))}
          </div>
          {ladders.map((ladder) => (
            <Ladder
              key={ladder.measure}
              ladder={ladder}
              score={levers.reach[ladder.measure] ?? 0}
              onPick={(score) => move({ reach: { ...levers.reach, [ladder.measure]: score } })}
            />
          ))}

          {plan && (
            <Prices
              plan={plan}
              energyUnit={energyUnit}
              extra={extra}
              test={test}
              verdict={verdict}
              waiting={waiting}
              onPretend={pretend}
              onClear={() => {
                setExtra({});
                setTest(null);
              }}
            />
          )}
        </div>

        <div className="space-y-3">
          {answer.isError && !plan ? (
            <Refused error={answer.error} />
          ) : plan ? (
            <Result
              plan={plan}
              saved={saved.plan}
              energyUnit={energyUnit}
              answered={answer.data!}
              waiting={waiting}
              refused={answer.isError ? answer.error : null}
            />
          ) : (
            <p className="muted text-sm">Working it out…</p>
          )}

          <div className="flex flex-wrap items-center gap-2">
            {pretending ? (
              <p className="muted text-sm">Pretended items can’t be kept — they aren’t yours. Clear them to keep the rest.</p>
            ) : unchanged ? (
              <p className="muted text-sm">These are your saved plan’s numbers. Move one to see what it changes.</p>
            ) : (
              <button
                type="button"
                className="btn"
                data-working={keeping || undefined}
                disabled={keeping}
                onClick={() => onKeep(levers)}
              >
                {keeping ? 'Saving…' : 'Keep this'}
              </button>
            )}
            {(!unchanged || pretending) && (
              <button type="button" className="btn-quiet" onClick={reset}>
                Reset
              </button>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}

function Slider({
  id,
  label,
  value,
  min,
  max,
  step,
  onChange,
}: {
  id: string;
  label: string;
  value: number;
  min: number;
  max: number;
  step: number;
  onChange: (value: number) => void;
}) {
  return (
    <div className="grid grid-cols-[7rem_minmax(0,1fr)_3.5rem] items-center gap-3 text-sm">
      <label className="muted" htmlFor={id}>
        {label}
      </label>
      <input
        id={id}
        type="range"
        min={min}
        max={max}
        step={step}
        value={value}
        style={{ accentColor: 'var(--brand)' }}
        onChange={(event) => onChange(Number(event.target.value))}
      />
      <output htmlFor={id} className="count text-right font-medium">
        {value.toLocaleString()}
      </output>
    </div>
  );
}

/** The answer: days and energy against the saved plan, how it was reached, and the runs. */
function Result({
  plan,
  saved,
  energyUnit,
  answered,
  waiting,
  refused,
}: {
  plan: Plan;
  saved: Plan;
  energyUnit: string;
  answered: WhatIf;
  waiting: boolean;
  refused: Error | null;
}) {
  const days = Math.round((plan.etaDays - saved.etaDays) * 10) / 10;
  const energy = plan.totalEnergy - saved.totalEnergy;
  return (
    <div className="space-y-3" style={waiting ? { opacity: 0.75, transition: 'opacity .2s' } : undefined}>
      <div className="flex flex-wrap items-end gap-6">
        <div>
          <div className="stat-label">Days</div>
          <div className="stat-value text-4xl" style={{ color: 'var(--brand)' }}>
            <Odometer text={daysOf(plan.etaDays)} />
          </div>
        </div>
        <div>
          <div className="stat-label">{energyUnit}</div>
          <div className="stat-value text-2xl">
            <Count value={plan.totalEnergy} from={saved.totalEnergy} duration={600} />
          </div>
        </div>
        <div className="flex flex-wrap gap-1.5 text-xs" aria-live="polite">
          {days === 0 && energy === 0 ? (
            <span className="muted">same as your saved plan</span>
          ) : (
            <>
              {days !== 0 && <Delta value={days} unit={Math.abs(days) === 1 ? 'day' : 'days'} />}
              {energy !== 0 && <Delta value={energy} unit={energyUnit.toLowerCase()} />}
              <span className="muted w-full">against your saved plan</span>
            </>
          )}
        </div>
      </div>
      <p className="count muted text-xs">
        {waiting
          ? 'Solving…'
          : answered.fromCache
            ? `Already worked out — served from the server’s cache in ${answered.solveMillis} ms`
            : `Solved on the server in ${answered.solveMillis} ms`}
      </p>
      {refused && <Refused error={refused} />}
      <Runs plan={plan} />
    </div>
  );
}

function Delta({ value, unit }: { value: number; unit: string }) {
  // Fewer days and less energy are both better, so a fall is the good colour.
  const better = value < 0;
  return (
    <span
      className="rounded-full px-2 py-0.5 font-medium"
      style={{
        background: `color-mix(in srgb, var(${better ? '--ok' : '--signal'}) 14%, transparent)`,
        color: `var(${better ? '--ok' : '--signal'})`,
      }}
    >
      {value > 0 ? '+' : '−'}
      {Math.abs(value).toLocaleString()} {unit}
    </span>
  );
}

function Refused({ error }: { error: Error }) {
  const unanswerable = error instanceof ApiError && error.isUnanswerable;
  return (
    <p className="text-sm" style={{ color: 'var(--signal)' }}>
      {unanswerable ? 'No plan for this: ' : 'That did not work: '}
      {error.message}
    </p>
  );
}

interface Row {
  key: string;
  name: string;
  runs: number;
  energy: number;
}

/** The runs, sliding to their new order as an answer arrives (F1). */
function Runs({ plan }: { plan: Plan }) {
  const rows: Row[] = plan.stages.map((stage) => ({
    key: stage.stage,
    name: stage.displayName ?? stage.stage,
    runs: stage.runs,
    energy: stage.totalEnergy,
  }));
  const order = rows.map((row) => row.key).join('|');
  const list = useRef<HTMLUListElement>(null);
  const { shown, entering } = useLeaving(rows, order);
  useFlip(list, order);
  const dearest = Math.max(1, ...rows.map((row) => row.energy));

  if (shown.length === 0) {
    return <p className="muted text-sm">Nothing to farm — what you hold covers it.</p>;
  }
  return (
    <ul ref={list} className="relative space-y-1.5" aria-label="Runs">
      {shown.map((row) => (
        <li
          key={row.key}
          data-flip={row.leaving ? undefined : row.key}
          className={`grid grid-cols-[minmax(0,1fr)_4rem_5rem] items-center gap-2 rounded-lg border px-3 py-1.5 text-sm ${
            row.leaving ? 'flip-leave' : entering.has(row.key) ? 'flip-enter' : ''
          }`}
          style={{ borderColor: 'var(--line)', background: 'var(--raised)' }}
          aria-hidden={row.leaving || undefined}
        >
          <span className="truncate">{row.name}</span>
          <span className="count text-right">×{row.runs.toLocaleString()}</span>
          <span className="h-1.5 rounded-full" style={{ background: 'var(--line)' }}>
            <span
              className="block h-full rounded-full"
              style={{
                width: `${(row.energy / dearest) * 100}%`,
                background: 'var(--brand)',
                transition: 'width .6s var(--ease-out)',
              }}
            />
          </span>
        </li>
      ))}
    </ul>
  );
}

/**
 * The rows to draw: this answer's, plus the last answer's rows that left it,
 * held for as long as they take to fold away. Which rows are new is said too,
 * so they can slide in. Nothing is held where motion is reduced.
 */
function useLeaving(rows: Row[], order: string) {
  const previous = useRef<Row[] | null>(null);
  const [gone, setGone] = useState<Row[]>([]);
  const [entering, setEntering] = useState<Set<string>>(new Set());
  // Before paint, so a row that joins is never drawn once without its entrance.
  useLayoutEffect(() => {
    const before = previous.current;
    previous.current = rows;
    if (!before || !prefersMotion()) return;
    const now = new Set(rows.map((row) => row.key));
    const had = new Set(before.map((row) => row.key));
    setGone(before.filter((row) => !now.has(row.key)));
    setEntering(new Set(rows.filter((row) => !had.has(row.key)).map((row) => row.key)));
    const done = setTimeout(() => {
      setGone([]);
      setEntering(new Set());
    }, LEAVE_MS + 200);
    return () => clearTimeout(done);
    // `order` names the rows; the array itself is new on every render.
  }, [order]);
  const here = new Set(rows.map((row) => row.key));
  const shown: (Row & { leaving?: boolean })[] = [
    ...rows,
    ...gone.filter((row) => !here.has(row.key)).map((row) => ({ ...row, leaving: true })),
  ];
  return { shown, entering };
}

/**
 * The shadow prices a reader can test: the catalog items the plan is paying
 * energy for, each with a step big enough to move the plan — ten Cogs move
 * nothing, so the step is the power of ten that the price says is worth about
 * twenty energy.
 */
function Prices({
  plan,
  energyUnit,
  extra,
  test,
  verdict,
  waiting,
  onPretend,
  onClear,
}: {
  plan: Plan;
  energyUnit: string;
  extra: Record<string, number>;
  test: Test | null;
  verdict: number | null;
  waiting: boolean;
  onPretend: (price: ShadowPrice, count: number) => void;
  onClear: () => void;
}) {
  const testable = plan.shadowPrice
    .filter((price) => price.holdable && price.price >= 0.005)
    .sort((a, b) => b.price - a.price)
    .slice(0, 5);
  const [pulse, setPulse] = useState(0);
  const names = new Map(plan.shadowPrice.map((price) => [price.item, price.displayName]));

  if (testable.length === 0 && Object.keys(extra).length === 0) return null;
  return (
    <div className="space-y-2 border-t pt-3" style={{ borderColor: 'var(--line)' }}>
      <h3 className="label">Test a price</h3>
      <ul className="space-y-2 text-sm">
        {testable.map((price) => {
          const count = stepFor(price.price);
          const mine = test?.item === price.item ? test : null;
          return (
            <li key={price.item} className="space-y-1">
              <div className="flex items-center justify-between gap-2">
                <span>
                  <span className="font-medium">{price.displayName}</span>{' '}
                  <span className="muted count">
                    · {price.price.toFixed(2)} {energyUnit.toLowerCase()} each
                  </span>
                </span>
                <button
                  type="button"
                  className="btn-quiet count"
                  onClick={() => {
                    setPulse((at) => at + 1);
                    onPretend(price, count);
                  }}
                >
                  +{count.toLocaleString()}
                </button>
              </div>
              {mine && (
                <p className="muted text-xs" aria-live="polite">
                  {verdict === null || waiting ? (
                    <>The price says −{mine.predicted.toLocaleString()}. Solving…</>
                  ) : (
                    <>
                      The price said <b>−{mine.predicted.toLocaleString()}</b>; the plan saved{' '}
                      <b>{verdict >= 0 ? '−' : '+'}{Math.abs(verdict).toLocaleString()}</b>. {why(mine.predicted, verdict)}
                    </>
                  )}
                </p>
              )}
            </li>
          );
        })}
      </ul>
      {test && <Ripple key={pulse} label={test.name} />}
      {Object.keys(extra).length > 0 && (
        <p className="flex flex-wrap items-center gap-2 text-xs">
          <span className="muted">Pretending:</span>
          {Object.entries(extra).map(([item, count]) => (
            <span key={item} className="chip">
              +{count.toLocaleString()} {names.get(item) ?? item}
            </span>
          ))}
          <button type="button" className="btn-quiet text-xs" onClick={onClear}>
            Clear
          </button>
        </p>
      )}
    </div>
  );
}

/** The power of ten whose worth at this price is about twenty energy, from 1 to 100 000. */
export function stepFor(price: number): number {
  if (!(price > 0)) return 1;
  const power = Math.ceil(Math.log10(20 / price));
  return Math.min(100_000, Math.max(1, 10 ** power));
}

/** Why a prediction and a re-solve differ, in one sentence; nothing when they agree. */
export function why(predicted: number, saved: number): string {
  if (Math.abs(predicted - saved) <= 1) return 'The price held.';
  if (saved < predicted) {
    return 'Less than the price said: runs come whole, and a price holds only until another limit binds.';
  }
  return 'More than the price said: with fewer to make, a whole run dropped out — runs come whole.';
}

/**
 * F4: a pulse from the item tested, through the plan, to the days — drawn once
 * per press, keyed by the press so each one runs again. A picture of the row's
 * sentence, so it is hidden from a screen reader; gone where motion is reduced.
 */
function Ripple({ label }: { label: string }) {
  const traces = ['M 34 40 C 100 40 100 14 160 14', 'M 34 40 C 100 40 100 66 160 66', 'M 176 14 C 250 14 250 40 314 40', 'M 176 66 C 250 66 250 40 314 40'];
  return (
    <svg viewBox="0 0 340 92" className="w-full max-w-sm" aria-hidden="true">
      {traces.map((d) => (
        <path key={d} className="ripple-trace" d={d} />
      ))}
      {traces.map((d, index) => (
        <path
          key={`pulse-${d}`}
          className="ripple-pulse"
          d={d}
          pathLength={100}
          style={{ '--delay': `${index < 2 ? 0 : 420}ms` } as CSSProperties}
        />
      ))}
      <circle className="ripple-node" cx={24} cy={40} r={10} />
      <circle className="ripple-node" cx={168} cy={14} r={7} />
      <circle className="ripple-node" cx={168} cy={66} r={7} />
      <circle className="ripple-node ripple-node-hot" cx={326} cy={40} r={11} />
      <text className="ripple-label" x={24} y={68} textAnchor="middle">
        {label.length > 14 ? `${label.slice(0, 13)}…` : label}
      </text>
      <text className="ripple-label" x={168} y={90} textAnchor="middle">
        the plan
      </text>
      <text className="ripple-label" x={326} y={68} textAnchor="middle">
        days
      </text>
    </svg>
  );
}

/** A value that follows `value` once it has stopped changing for `ms`. */
function useSettled<T>(value: T, ms: number): T {
  const [settled, setSettled] = useState(value);
  useEffect(() => {
    const timer = setTimeout(() => setSettled(value), ms);
    return () => clearTimeout(timer);
  }, [value, ms]);
  return settled;
}

function sameLevers(a: WhatIfLevers, b: WhatIfLevers): boolean {
  const reachOf = (levers: WhatIfLevers) =>
    JSON.stringify(Object.entries(levers.reach).filter(([, score]) => score > 0).sort());
  return (
    a.energyPerDay === b.energyPerDay &&
    a.horizonDays === b.horizonDays &&
    a.objective === b.objective &&
    reachOf(a) === reachOf(b)
  );
}
