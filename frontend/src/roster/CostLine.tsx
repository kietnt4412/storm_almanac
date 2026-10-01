import type { ReactElement } from 'react';
import type { RangeCost } from './rangeCost';

/**
 * A range's cost in one line, "1,048,000 Character EXP · 41 Skill Point ·
 * 197,000 Cogs", what no inventory holds first, then the items. A link sold at
 * several prices is named, never added (ADR 0021).
 */
export function CostLine({ cost, empty = 'Free' }: { cost: RangeCost; empty?: string }): ReactElement {
  const parts = [
    ...cost.progress.map((line) => ({ key: `progress:${line.kind}`, quantity: line.quantity, name: line.displayName })),
    ...cost.items.map((line) => ({ key: line.item, quantity: line.quantity, name: line.displayName })),
  ];
  const choices = cost.choices.length;
  if (parts.length === 0 && choices === 0) return <span className="muted">{empty}</span>;
  return (
    <span>
      {parts.map((part, index) => (
        <span key={part.key}>
          {index > 0 && <span className="muted"> · </span>}
          <b className="count">{part.quantity.toLocaleString()}</b> {part.name}
        </span>
      ))}
      {choices > 0 && (
        <span className="muted">
          {parts.length > 0 && ' · '}
          {choices === 1
            ? `1 step at one of ${cost.choices[0]!.options.length} prices`
            : `${choices} steps each at one of several prices`}
        </span>
      )}
    </span>
  );
}
