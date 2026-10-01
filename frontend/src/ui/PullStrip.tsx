import { useState, type CSSProperties, type ReactElement } from 'react';
import type { Odds } from '../api/client';
import { Count } from './motion';

/**
 * Pulls you can feel (C2.14, agreed 2026-10-01): every pull from the reader's
 * counter to the guarantee as a cell, the ones already counted filled, and a
 * day slider that fills the strip forward with what each day's income affords
 * and says the chance it gives.
 *
 * <p><b>The heat on a cell is the chance of landing on exactly that pull</b>:
 * the step between two neighbouring points of `curve`, which the chain
 * already answers at every pull count. Nothing here is simulated or guessed;
 * a day's pulls and chance are `byDay`'s, as the chart below says them.
 *
 * <p>A server a deploy behind sends neither, and then there is no strip.
 */
export function PullStrip({ odds }: { odds: Odds }): ReactElement | null {
  const curve = odds.curve;
  const days = odds.byDay && odds.byDay.length > 0 ? odds.byDay : undefined;
  const asked = days ? Math.max(0, days.findIndex((day) => day.day >= odds.days)) : 0;
  const [index, setIndex] = useState(asked);
  if (!curve || curve.length < 2) return null;

  const future = curve.length - 1;
  const landing = curve.slice(1).map((chance, at) => Math.max(0, chance - curve[at]!));
  const peak = Math.max(...landing, 1e-9);
  const day = days?.[Math.min(index, days.length - 1)];
  const afforded = Math.min(future, day ? day.pulls : odds.budget.pulls);
  const chance = day ? day.chance : odds.chance;
  const average = Math.round(odds.expectedPulls);
  const counted = odds.copies === 1 ? odds.pity.pullsSinceHit : 0;
  const date = day ? new Date(Date.now() + day.day * 86_400_000) : null;

  return (
    <section className="pull-strip space-y-3" aria-label="Your next pulls">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <span className="label">{day ? (day.day === 0 ? 'Today' : `In ${day.day} day${day.day === 1 ? '' : 's'}`) : 'With what you hold'}</span>
          <p className="text-sm">
            <b className="count">
              <Count value={afforded} duration={400} />
            </b> pull{afforded === 1 ? '' : 's'}
            {date && <span className="muted"> · {date.toLocaleDateString(undefined, { day: 'numeric', month: 'short' })}</span>}
          </p>
        </div>
        <div className="pull-strip-chance count" aria-live="polite">
          <Count value={chance} format={(value) => `${(value * 100).toFixed(chance > 0 && chance < 0.01 ? 2 : 0)}%`} duration={500} />
        </div>
      </div>

      {days && days.length > 1 && (
        <input
          type="range"
          className="pull-slider"
          min={0}
          max={days.length - 1}
          value={Math.min(index, days.length - 1)}
          onChange={(event) => setIndex(Number(event.target.value))}
          aria-label="Days from now"
          aria-valuetext={`${day?.day ?? 0} days: ${afforded} pulls, ${(chance * 100).toFixed(0)}%`}
          style={{ '--fill': `${(Math.min(index, days.length - 1) / (days.length - 1)) * 100}%` } as CSSProperties}
        />
      )}

      <div className="pull-cells" role="img" aria-label={`${counted} pulls already counted, ${afforded} of the next ${future} afforded`}>
        {/* The cells drop in one after another as the answer lands, capped so a long wall is not a wait. */}
        {Array.from({ length: counted }, (_, at) => (
          <span key={`c${at}`} className="pull-cell stagger-in" data-state="counted" style={cellDelay(at)} />
        ))}
        {landing.map((chanceHere, at) => {
          const pull = at + 1;
          return (
            <span
              key={pull}
              className="pull-cell stagger-in"
              data-state={pull <= afforded ? 'afforded' : 'ahead'}
              data-average={pull === average || undefined}
              title={`Pull ${counted + pull}: ${(chanceHere * 100).toFixed(2)}% to land here`}
              style={{ '--heat': (chanceHere / peak).toFixed(3), ...cellDelay(counted + at) } as CSSProperties}
            />
          );
        })}
      </div>

      <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs">
        {counted > 0 && (
          <span className="flex items-center gap-1.5">
            <span className="pull-cell" data-state="counted" /> already on your counter
          </span>
        )}
        <span className="flex items-center gap-1.5">
          <span className="pull-cell" data-state="afforded" style={{ '--heat': 0.6 } as CSSProperties} /> afforded by then
        </span>
        <span className="flex items-center gap-1.5">
          <span className="pull-cell" data-state="ahead" style={{ '--heat': 0.6 } as CSSProperties} /> brighter: likelier to land there
        </span>
        <span className="flex items-center gap-1.5">
          <span className="pull-cell" data-state="ahead" data-average="true" /> about average
        </span>
      </div>
    </section>
  );
}

const cellDelay = (at: number) => ({ '--delay': `${Math.min(at, 90) * 9}ms` }) as CSSProperties;
