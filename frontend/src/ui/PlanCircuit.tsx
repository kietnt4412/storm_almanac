import { useLayoutEffect, useRef, useState, type CSSProperties } from 'react';
import type { Plan } from '../api/client';
import { Icon, type IconName } from './Icon';

/**
 * The plan as a circuit (C2.6, agreed 2026-10-01): what the reader does on the
 * left, what it pays for on the right, the plan's cost on a chip in the middle,
 * and a trace from each side into the chip with a pulse of light running along
 * it — the shape of the site the maintainer recorded, put to something true.
 *
 * <p><b>Every trace runs through the chip, never from a stage to a goal.</b>
 * The plan does not say which stage pays for which goal — one run's drops feed
 * several, and the solver prices them together — so a line from one to the
 * other would draw a claim the answer does not make. Through the middle is
 * exactly what it does say: all of this, for all of that.
 *
 * <p>Up to four nodes a side, the dearest stages first and the characters with
 * the most steps first; the rest are counted, not dropped. A character is one
 * node however many steps it takes, which the plan says by its entity whether
 * or not the page has loaded the names of its tracks yet. The traces are measured off the laid-out nodes, so they follow
 * any wrap; on a phone, where the three columns stack, there is nothing beside
 * the chip to draw from, and the nodes stand as a list.
 */
interface Node {
  key: string;
  icon: IconName;
  label: string;
  detail: string;
}

const MAX = 4;

export function PlanCircuit({ plan, energyUnit }: { plan: Plan; energyUnit: string }) {
  const sources = sourcesOf(plan);
  const targets = targetsOf(plan);

  const box = useRef<HTMLDivElement>(null);
  const hub = useRef<HTMLDivElement>(null);
  const traces = useTraces(box, hub, sources.length, targets.length);

  if (sources.length === 0) return null;

  return (
    <div
      ref={box}
      className="relative grid items-center gap-3 sm:grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)] sm:gap-x-14"
      // A picture of what the checklist and "What this pays for" say in
      // words, so a screen reader hears those and not the same names twice.
      aria-hidden="true"
    >
      <svg className="pointer-events-none absolute inset-0 h-full w-full overflow-visible">
        {traces.map((trace, index) => (
          <g key={index}>
            <path className="circuit-trace" d={trace.d} pathLength={100} />
            <path
              className="circuit-pulse"
              d={trace.d}
              pathLength={100}
              style={{ '--delay': `${index * 420}ms` } as CSSProperties}
            />
            <circle cx={trace.start[0]} cy={trace.start[1]} r={2.5} fill="var(--brand)" />
          </g>
        ))}
      </svg>

      <div className="flex flex-col gap-2">
        {sources.map((node) => (
          <CircuitNode key={node.key} node={node} side="source" />
        ))}
      </div>

      <div ref={hub} className="circuit-hub mx-auto sm:min-h-[7rem]">
        <span className="label">Your plan</span>
        <span className="count text-2xl font-semibold leading-tight" style={{ color: 'var(--brand)' }}>
          {plan.totalEnergy.toLocaleString()}
        </span>
        <span className="muted text-xs">{energyUnit.toLowerCase()}</span>
      </div>

      {targets.length > 0 ? (
        <div className="flex flex-col gap-2">
          {targets.map((node) => (
            <CircuitNode key={node.key} node={node} side="target" />
          ))}
        </div>
      ) : (
        <span />
      )}
    </div>
  );
}

function CircuitNode({ node, side }: { node: Node; side: 'source' | 'target' }) {
  return (
    <div className="circuit-node" data-side={side} title={node.label}>
      <span className="shrink-0" style={{ color: side === 'source' ? 'var(--brand)' : 'var(--violet)' }}>
        <Icon name={node.icon} size={16} />
      </span>
      <span className="min-w-0">
        <span className="block truncate font-medium">{node.label}</span>
        <span className="muted block truncate text-xs">{node.detail}</span>
      </span>
    </div>
  );
}

/** The runs, dearest first, then what to buy or craft and what to claim, each as one node. */
function sourcesOf(plan: Plan): Node[] {
  const extras: Node[] = [];
  const steps = plan.conversions.length;
  if (steps > 0) {
    extras.push({
      key: 'steps',
      icon: 'shop',
      label: `${steps} to buy or craft`,
      detail: 'purchases, crafts, feeding',
    });
  }
  const claims = plan.rewards.length;
  if (claims > 0) {
    extras.push({ key: 'claims', icon: 'gift', label: `${claims} to claim`, detail: 'rewards on the way' });
  }
  const runs = [...plan.stages]
    .sort((a, b) => b.totalEnergy - a.totalEnergy)
    .map((stage) => ({
      key: `run:${stage.stage}`,
      icon: 'run' as const,
      label: stage.displayName ?? stage.stage,
      detail: `${stage.runs.toLocaleString()} run${stage.runs === 1 ? '' : 's'}`,
    }));
  const room = Math.max(1, MAX - extras.length);
  if (runs.length <= room) return [...runs, ...extras];
  const rest = runs.length - (room - 1);
  return [
    ...runs.slice(0, room - 1),
    { key: 'more-runs', icon: 'run', label: `${rest} more stages`, detail: 'see the list below' },
    ...extras,
  ];
}

/** What the plan pays for, one node per character, the most steps first. */
function targetsOf(plan: Plan): Node[] {
  const byEntity = new Map<string, { label: string; steps: number }>();
  for (const step of plan.payingFor ?? []) {
    const key = step.entity ?? step.step;
    const entry = byEntity.get(key) ?? { label: step.entityName ?? step.entity ?? step.displayName, steps: 0 };
    entry.steps += 1;
    byEntity.set(key, entry);
  }
  const nodes = [...byEntity.entries()]
    .sort(([, a], [, b]) => b.steps - a.steps)
    .map(([key, entry]) => ({
      key,
      icon: 'target' as const,
      label: entry.label,
      detail: `${entry.steps} upgrade${entry.steps === 1 ? '' : 's'}`,
    }));
  return cap(nodes, (rest) => ({ key: 'more-goals', icon: 'target', label: `${rest} more`, detail: 'also paid for' }));
}

function cap(nodes: Node[], more: (rest: number) => Node): Node[] {
  if (nodes.length <= MAX) return nodes;
  return [...nodes.slice(0, MAX - 1), more(nodes.length - (MAX - 1))];
}

interface Trace {
  d: string;
  start: [number, number];
}

/**
 * Each node's trace into the chip, measured off the page as laid out, and
 * measured again whenever the box changes size. A node not beside the chip —
 * the stacked phone layout — gets none.
 */
function useTraces(
  box: React.RefObject<HTMLDivElement | null>,
  hub: React.RefObject<HTMLDivElement | null>,
  sources: number,
  targets: number,
): Trace[] {
  const [traces, setTraces] = useState<Trace[]>([]);
  useLayoutEffect(() => {
    const container = box.current;
    const chip = hub.current;
    if (!container || !chip) return;
    const measure = () => {
      const origin = container.getBoundingClientRect();
      const h = chip.getBoundingClientRect();
      const next: Trace[] = [];
      const side = (kind: 'source' | 'target') => {
        const nodes = [...container.querySelectorAll<HTMLElement>(`[data-side='${kind}']`)];
        nodes.forEach((node, index) => {
          const r = node.getBoundingClientRect();
          const beside = kind === 'source' ? r.right <= h.left : r.left >= h.right;
          if (!beside) return;
          // Each trace meets the chip at its own height, so none run together.
          const y2 = h.top + (h.height * (index + 1)) / (nodes.length + 1) - origin.top;
          const y1 = r.top + r.height / 2 - origin.top;
          const x1 = (kind === 'source' ? r.right : r.left) - origin.left;
          const x2 = (kind === 'source' ? h.left : h.right) - origin.left;
          next.push({ d: elbow(x1, y1, x2, y2), start: [x1, y1] });
        });
      };
      side('source');
      side('target');
      setTraces(next);
    };
    measure();
    if (typeof ResizeObserver !== 'function') return;
    const observer = new ResizeObserver(measure);
    observer.observe(container);
    return () => observer.disconnect();
  }, [box, hub, sources, targets]);
  return traces;
}

/** Across, down or up, across again — a circuit-board trace, its corners rounded. */
function elbow(x1: number, y1: number, x2: number, y2: number): string {
  const mid = (x1 + x2) / 2;
  const dy = y2 - y1;
  if (Math.abs(dy) < 1) return `M${x1},${y1} H${x2}`;
  const dir = Math.sign(dy);
  const toward = Math.sign(x2 - x1);
  const r = Math.min(10, Math.abs(dy) / 2, Math.abs(mid - x1));
  return [
    `M${x1},${y1}`,
    `H${mid - toward * r}`,
    `Q${mid},${y1} ${mid},${y1 + dir * r}`,
    `V${y2 - dir * r}`,
    `Q${mid},${y2} ${mid + toward * r},${y2}`,
    `H${x2}`,
  ].join(' ');
}
