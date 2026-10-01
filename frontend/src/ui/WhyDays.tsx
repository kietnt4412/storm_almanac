import type { ReactElement } from 'react';
import type { Plan } from '../api/client';

/** Days as the plan screen says them, "28" and "7.7" (PlanView's daysOf, kept here to avoid a cycle). */
const daysOf = (days: number) => {
  const tenths = Math.round(days * 10) / 10;
  return Number.isInteger(tenths) ? String(tenths) : tenths.toFixed(1);
};

/** A cadence long enough to mark one claim at a time; a daily reward would be a comb. */
const PERIOD: Record<string, number> = { WEEKLY: 7, MONTHLY: 31 };

/**
 * "Why 28 days?" (C2.8, agreed 2026-10-01): the plan's length as one bar, the
 * energy it spends as a fill along it, and each claim of a weekly reward the
 * plan counts as a mark.
 *
 * <p><b>Read off what the plan says, never worked out again.</b> The server's
 * {@code etaDays} is the longer of two things: the energy divided by the daily
 * rate, and the whole days the plan has to wait, for a reward on a cadence or a
 * stage open only some weekdays. The page has the first half from the plan and
 * the rate it was asked with, so whichever is longer is the one that binds.
 *
 * <p><b>A claim is marked where the model counts it</b>: the k-th claim of a
 * weekly at day 7k (`Cadence.occurrencesIn`), not on a weekday. A daily reward
 * is not marked. A plan saved before rewards carried their cadence marks
 * nothing and still says which half binds.
 */
export interface Binding {
  energyDays: number;
  /** The days the plan has to wait however much energy is spare; 0 when energy binds. */
  waitDays: number;
  /** Each counted claim of a reward with a period of a week or more, by the day the model counts it. */
  marks: number[];
  /** The day of the last claim the plan counts, when claims are what it waits on. */
  lastClaim: number | null;
  claims: number;
}

export function bindingOf(plan: Plan, energyPerDay: number): Binding {
  const energyDays = energyPerDay > 0 ? plan.totalEnergy / energyPerDay : 0;
  const waitDays = plan.etaDays > energyDays + 0.05 ? plan.etaDays : 0;
  const marks = new Set<number>();
  let claims = 0;
  for (const claim of plan.rewards) {
    const period = claim.cadence ? PERIOD[claim.cadence] : undefined;
    if (!period) continue;
    claims = Math.max(claims, claim.times);
    for (let k = 1; k <= claim.times; k += 1) marks.add(k * period);
  }
  const sorted = [...marks].sort((a, b) => a - b);
  const last = sorted.at(-1);
  return {
    energyDays,
    waitDays,
    marks: sorted,
    lastClaim: waitDays > 0 && last !== undefined && Math.abs(last - waitDays) < 0.5 ? last : null,
    claims,
  };
}

export function WhyDays({
  plan,
  energyPerDay,
  energyUnit,
}: {
  plan: Plan;
  energyPerDay: number;
  energyUnit: string;
}): ReactElement | null {
  if (plan.etaDays <= 0 || energyPerDay <= 0) return null;
  const binding = bindingOf(plan, energyPerDay);
  const length = Math.max(plan.etaDays, binding.energyDays);
  const at = (day: number) => `${Math.min(100, (day / length) * 100)}%`;
  const unit = energyUnit.toLowerCase();
  const total = plan.totalEnergy.toLocaleString();
  const days = daysOf(plan.etaDays);
  const quarter = [0.25, 0.5, 0.75].map((share) => Math.round(length * share));

  return (
    <div className="space-y-2">
      <div className="flex flex-wrap items-baseline justify-between gap-2">
        <h2 className="font-medium">Why {days} days?</h2>
        <span className="muted text-xs">
          {energyPerDay.toLocaleString()} {unit} a day
        </span>
      </div>
      <div className="relative h-12" aria-hidden="true">
        <span className="absolute inset-x-0 top-4 h-4 overflow-hidden rounded-md" style={{ background: 'var(--line)' }}>
          <span
            className="absolute inset-y-0 left-0"
            style={{ width: at(binding.energyDays), background: 'color-mix(in srgb, var(--brand) 70%, transparent)' }}
          />
        </span>
        {binding.marks.map((day) => (
          <span
            key={day}
            className="absolute top-2 h-8 w-0.5"
            style={{ left: `calc(${at(day)} - 1px)`, background: 'var(--violet)' }}
          />
        ))}
        <span className="muted absolute top-9 text-[10px]" style={{ left: 0 }}>
          day 0
        </span>
        {quarter.map((day) => (
          <span key={day} className="muted absolute top-9 -translate-x-1/2 text-[10px]" style={{ left: at(day) }}>
            {day}
          </span>
        ))}
        <span className="muted absolute right-0 top-9 text-[10px]">{daysOf(length)}</span>
      </div>
      <p className="text-sm">
        {binding.waitDays > 0 ? (
          binding.lastClaim !== null ? (
            <>
              The {unit} is spent by <b>day {Math.ceil(binding.energyDays)}</b>. The plan counts{' '}
              <b>
                {binding.claims} weekly claim{binding.claims === 1 ? '' : 's'}
              </b>
              , and the last comes on <b>day {binding.lastClaim}</b>, so the claims set the length, not the {unit}.
            </>
          ) : (
            <>
              The {unit} is spent by <b>day {Math.ceil(binding.energyDays)}</b>, and the plan waits to day{' '}
              <b>{days}</b> for a reward on a cadence or a stage open only some weekdays.
            </>
          )
        ) : (
          <>
            The {unit} sets the length: <b>{total}</b> at {energyPerDay.toLocaleString()} a day is <b>{days} days</b>.
            {binding.marks.length > 0 && ' The claims it counts all come sooner.'}
          </>
        )}
      </p>
      <p className="muted flex flex-wrap gap-x-4 gap-y-1 text-xs" aria-hidden="true">
        <span>
          <span
            className="mr-1.5 inline-block h-2.5 w-2.5 rounded-sm align-[-1px]"
            style={{ background: 'color-mix(in srgb, var(--brand) 70%, transparent)' }}
          />
          {total} {unit} ÷ {energyPerDay.toLocaleString()} a day
        </span>
        {binding.marks.length > 0 && (
          <span>
            <span className="mr-1.5 inline-block h-2.5 w-2.5 rounded-sm align-[-1px]" style={{ background: 'var(--violet)' }} />
            a weekly claim the plan counts
          </span>
        )}
      </p>
    </div>
  );
}
