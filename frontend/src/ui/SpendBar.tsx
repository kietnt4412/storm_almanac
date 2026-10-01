import type { CSSProperties, ReactElement } from 'react';

export interface Spend {
  item: string;
  displayName: string;
  quantity: number;
  buys: string;
}

/**
 * Where each currency the plan spends goes, as one stacked bar per currency
 * (C2.3): the plan below says "Buy 654,000 Cogs for 545 Simulation Score" three
 * lines apart from the other two purchases, and only a sum tells a reader that
 * EXP takes three quarters of what they farm.
 *
 * <p>Drawn from the purchases' numbers, never parsed from their sentences. A plan
 * saved before those numbers were sent draws nothing, and the lines below it
 * still say everything.
 */
const COLOURS = [
  'var(--brand)',
  'var(--violet)',
  'var(--signal)',
  'color-mix(in srgb, var(--brand) 45%, var(--violet))',
  'color-mix(in srgb, var(--signal) 50%, var(--violet))',
];

export function SpendBars({ spends }: { spends: Spend[] }): ReactElement | null {
  const byCurrency = new Map<string, { name: string; parts: Map<string, number> }>();
  for (const spend of spends) {
    const currency = byCurrency.get(spend.item) ?? { name: spend.displayName, parts: new Map<string, number>() };
    currency.parts.set(spend.buys, (currency.parts.get(spend.buys) ?? 0) + spend.quantity);
    byCurrency.set(spend.item, currency);
  }
  if (byCurrency.size === 0) return null;

  return (
    <div className="space-y-4">
      {[...byCurrency.entries()].map(([item, { name, parts }]) => {
        const sorted = [...parts.entries()].sort((a, b) => b[1] - a[1]);
        const total = sorted.reduce((sum, [, quantity]) => sum + quantity, 0);
        return (
          <figure key={item} className="space-y-2">
            <figcaption className="flex items-baseline justify-between gap-3 text-sm">
              <span className="font-medium">Where your {name} goes</span>
              <span className="count muted">{total.toLocaleString()} in all</span>
            </figcaption>
            <div
              className="flex h-3 w-full overflow-hidden rounded-full"
              style={{ background: 'var(--line)' }}
              aria-hidden="true"
            >
              {sorted.map(([buys, quantity], index) => (
                <span
                  key={buys}
                  style={{
                    width: `${(quantity / total) * 100}%`,
                    background: COLOURS[index % COLOURS.length],
                    borderColor: 'var(--raised)',
                    '--delay': `${200 + index * 120}ms`,
                  } as CSSProperties}
                  className="wipe h-full border-r-2 last:border-r-0"
                  title={`${buys}: ${quantity.toLocaleString()}`}
                />
              ))}
            </div>
            <ul className="flex flex-wrap gap-x-4 gap-y-1 text-xs">
              {sorted.map(([buys, quantity], index) => (
                <li key={buys} className="flex items-center gap-1.5">
                  <span
                    className="inline-block h-2.5 w-2.5 rounded-sm"
                    style={{ background: COLOURS[index % COLOURS.length] }}
                    aria-hidden="true"
                  />
                  <span>{buys}</span>
                  <span className="count muted">
                    {quantity.toLocaleString()} · {Math.round((quantity / total) * 100)}%
                  </span>
                </li>
              ))}
            </ul>
          </figure>
        );
      })}
    </div>
  );
}
