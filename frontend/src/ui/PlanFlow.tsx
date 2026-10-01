import type { CSSProperties, ReactElement } from 'react';
import type { Plan } from '../api/client';

/**
 * Where the energy goes (C2.8, agreed 2026-10-01): for each currency the plan
 * spends, the stages that pay it on the left, the currency in the middle, and
 * what it buys on the right, each band as wide as its share.
 *
 * <p><b>Every width is a number the plan sent.</b> What a stage pays is its
 * runs times the yield the solve counted (`pays`, so a sampled rate arrives
 * already discounted); what a purchase spends is its `spends`. Nothing is split
 * by guess: with two stages paying one currency, each band is that stage's own
 * total. When the purchases spend more than the runs pay, the rest is drawn as
 * coming from the bag; when they spend less, the rest is left over.
 *
 * <p><b>It stops at what is bought, never at a goal</b>, for the reason
 * {@link PlanCircuit} runs through its chip: the plan does not say which
 * purchase pays for which goal. A plan saved before stages carried what they pay
 * draws none of this, and the screen keeps the bars it had.
 */
export interface FlowPart {
  key: string;
  label: string;
  detail?: string;
  quantity: number;
}

export interface Flow {
  item: string;
  name: string;
  sources: FlowPart[];
  sinks: FlowPart[];
  /** Both sides sum to this: what is paid in plus the bag equals what is bought plus what is left. */
  total: number;
}

export function flowsOf(plan: Plan, energyUnit: string): Flow[] {
  const spent = new Map<string, { name: string; parts: Map<string, number> }>();
  for (const conversion of plan.conversions) {
    const spend = conversion.spends;
    if (!spend) continue;
    const currency = spent.get(spend.item) ?? { name: spend.displayName, parts: new Map<string, number>() };
    currency.parts.set(spend.buys, (currency.parts.get(spend.buys) ?? 0) + spend.quantity);
    spent.set(spend.item, currency);
  }

  const flows: Flow[] = [];
  for (const [item, { name, parts }] of spent) {
    const sources: FlowPart[] = plan.stages.flatMap((stage) => {
      const paid = stage.pays?.find((pay) => pay.item === item);
      return paid && paid.total > 0
        ? [
            {
              key: stage.stage,
              label: stage.displayName ?? stage.stage,
              detail: `${stage.runs.toLocaleString()} runs · ${stage.totalEnergy.toLocaleString()} ${energyUnit.toLowerCase()}`,
              quantity: paid.total,
            },
          ]
        : [];
    });
    if (sources.length === 0) continue;
    const sinks: FlowPart[] = [...parts.entries()]
      .sort((a, b) => b[1] - a[1])
      .map(([buys, quantity]) => ({ key: buys, label: buys, quantity }));
    const earned = sources.reduce((sum, part) => sum + part.quantity, 0);
    const spending = sinks.reduce((sum, part) => sum + part.quantity, 0);
    if (spending > earned) sources.push({ key: 'bag', label: 'From your bag', quantity: spending - earned });
    if (earned > spending) sinks.push({ key: 'left', label: 'Left over', quantity: earned - spending });
    flows.push({ item, name, sources, sinks, total: Math.max(earned, spending) });
  }
  return flows;
}

const COLOURS = ['var(--violet)', 'var(--signal)', 'var(--brand)', 'color-mix(in srgb, var(--brand) 45%, var(--violet))'];
const W = 600;
const H = 150;
const GAP = 8;
const NODE = 12;
const [LEFT, MIDDLE, RIGHT] = [150, 300, 440];

export function PlanFlow({ plan, energyUnit }: { plan: Plan; energyUnit: string }): ReactElement | null {
  const flows = flowsOf(plan, energyUnit);
  if (flows.length === 0) return null;
  return (
    <div className="space-y-4">
      {flows.map((flow) => (
        <FlowChart key={flow.item} flow={flow} />
      ))}
    </div>
  );
}

function FlowChart({ flow }: { flow: Flow }): ReactElement {
  const scale = H / flow.total;
  const percent = (quantity: number) => `${Math.round((quantity / flow.total) * 100)}%`;
  const sentence = `${flow.sources.map((part) => `${part.label} ${fmt(part.quantity)}`).join(', ')} → ${fmt(flow.total)} ${flow.name} → ${flow.sinks
    .map((part) => `${part.label} ${percent(part.quantity)}`)
    .join(', ')}`;

  let into = 16;
  let fromLeft = 16;
  const left = flow.sources.map((part) => {
    const h = part.quantity * scale;
    const node = { part, y: fromLeft, h, at: into };
    fromLeft += h + GAP;
    into += h;
    return node;
  });
  let out = 16;
  let toRight = 16;
  const right = flow.sinks.map((part, index) => {
    const h = part.quantity * scale;
    const node = { part, y: toRight, h, at: out, colour: part.key === 'left' ? 'var(--muted)' : COLOURS[index % COLOURS.length]! };
    toRight += h + GAP;
    out += h;
    return node;
  });

  // A name and a number under it need 28 units, which a thin band does not
  // have: each side's labels are pushed down to stay that far apart.
  const leftLabels = stacked(left.map(({ y, h }) => y + Math.min(h / 2, 12) + 4));
  const rightLabels = stacked(right.map(({ y, h }) => y + Math.min(h / 2, 12) + 4));
  const height = Math.max(16 + H + GAP * Math.max(left.length, right.length), ...leftLabels, ...rightLabels) + 24;

  return (
    <figure className="space-y-1">
      {/* The caption says it in words, so the drawing is not read out twice. On a
          phone it keeps a readable size and scrolls sideways inside the card. */}
      <div className="overflow-x-auto">
      <svg viewBox={`0 0 ${W} ${height}`} className="w-full min-w-[520px] max-w-2xl" aria-hidden="true">
        {/* It flows as it arrives: what pays, then the pool, then what it buys (C2.17). */}
        {left.map(({ part, y, h, at }, index) => (
          <g key={part.key} className="wipe" style={flowDelay(index * 80)}>
            <path d={band(LEFT + NODE, y, MIDDLE, at, h)} fill="var(--brand)" opacity={0.25} />
            <rect x={LEFT} y={y} width={NODE} height={Math.max(h, 1)} rx={3} fill={part.key === 'bag' ? 'var(--muted)' : 'var(--brand)'} />
            <text x={LEFT - 6} y={leftLabels[index]} textAnchor="end" className="flow-text">
              {part.label}
            </text>
            <text x={LEFT - 6} y={leftLabels[index]! + 13} textAnchor="end" className="flow-text flow-muted">
              {part.detail ? `${part.detail} → ${fmt(part.quantity)}` : fmt(part.quantity)}
            </text>
          </g>
        ))}
        <g className="wipe" style={flowDelay(left.length * 80 + 150)}>
          <rect x={MIDDLE} y={16} width={NODE} height={H} rx={3} fill="var(--violet)" />
          <text x={MIDDLE + NODE / 2} y={10} textAnchor="middle" className="flow-text">
            {fmt(flow.total)} {flow.name}
          </text>
        </g>
        {right.map(({ part, y, h, at, colour }, index) => (
          <g key={part.key} className="wipe" style={flowDelay(left.length * 80 + 300 + index * 80)}>
            <path d={band(MIDDLE + NODE, at, RIGHT, y, h)} fill={colour} opacity={0.3} />
            <rect x={RIGHT} y={y} width={NODE} height={Math.max(h, 1)} rx={3} fill={colour} />
            <text x={RIGHT + NODE + 6} y={rightLabels[index]} className="flow-text">
              {part.label}
            </text>
            <text x={RIGHT + NODE + 6} y={rightLabels[index]! + 13} className="flow-text flow-muted">
              {fmt(part.quantity)} · {percent(part.quantity)}
            </text>
          </g>
        ))}
      </svg>
      </div>
      <figcaption className="muted text-xs">{sentence}</figcaption>
    </figure>
  );
}

/** Each label's baseline, no nearer the one above it than a name and its number. */
function stacked(wanted: number[]): number[] {
  const placed: number[] = [];
  for (const y of wanted) placed.push(placed.length === 0 ? y : Math.max(y, placed.at(-1)! + 28));
  return placed;
}

/** A band of height {@code h} from (x1, y1) to (x2, y2), curved like a Sankey link. */
function band(x1: number, y1: number, x2: number, y2: number, h: number): string {
  const mid = (x1 + x2) / 2;
  return `M${x1} ${y1} C${mid} ${y1}, ${mid} ${y2}, ${x2} ${y2} L${x2} ${y2 + h} C${mid} ${y2 + h}, ${mid} ${y1 + h}, ${x1} ${y1 + h} Z`;
}

function fmt(quantity: number): string {
  return Math.round(quantity).toLocaleString();
}

const flowDelay = (ms: number) => ({ '--delay': `${Math.min(ms, 1400)}ms` }) as CSSProperties;
