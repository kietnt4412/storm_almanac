import type { Cost, ProgressCost } from '../api/client';
import type { Track } from './tracks';

/**
 * What the steps between two places on one track cost, by their own prices
 * (C2.8): the live line under a goal's ladder, and the price a roster square
 * shows on hover.
 *
 * <p><b>This is not the plan.</b> It adds up what the steps say they cost and
 * stops there. The plan works out the cheapest way to pay, and three things
 * make the two differ, each said on the screen beside the sum:
 *
 * <ul>
 *   <li>A step sold at several prices (ADR 0021) is a choice the solver makes,
 *       so it is listed as "one of" and never added: adding every price would
 *       charge the reader for all of them.
 *   <li>A gate (ADR 0019) is demand the resolver adds. The upgrades route does
 *       not say which steps have one, so it is not here.
 *   <li>A state the roster has crossed is free (ADR 0026). The sum starts where
 *       the reader stands, so it never charges for one.
 * </ul>
 *
 * <p>A step is counted when both its ends are on the range, in track order. On
 * a track that is a line, which every published one is, that is the path.
 */
export interface CostedStep {
  fromState: string;
  toState: string;
  costs: Cost[];
  progress?: ProgressCost[];
}

export interface RangeCost {
  items: Cost[];
  progress: ProgressCost[];
  /** A link offered at several prices: each option is one step's costs. */
  choices: { fromState: string; toState: string; options: Cost[][] }[];
  /** How many links the range crosses, choices included. */
  links: number;
}

export const NO_COST: RangeCost = { items: [], progress: [], choices: [], links: 0 };

export function rangeCost(steps: CostedStep[], track: Track, from: number, to: number): RangeCost {
  if (to <= from) return NO_COST;
  const position = new Map(track.states.map((candidate, index) => [candidate.state, index]));
  const links = new Map<string, CostedStep[]>();
  for (const step of steps) {
    const a = position.get(step.fromState);
    const b = position.get(step.toState);
    if (a === undefined || b === undefined || a < from || b > to || b <= a) continue;
    const key = `${step.fromState}\u0000${step.toState}`;
    links.set(key, [...(links.get(key) ?? []), step]);
  }

  let total: RangeCost = { ...NO_COST, links: links.size };
  for (const sold of links.values()) {
    if (sold.length > 1) {
      total = {
        ...total,
        choices: [
          ...total.choices,
          { fromState: sold[0]!.fromState, toState: sold[0]!.toState, options: sold.map((step) => step.costs) },
        ],
      };
      continue;
    }
    total = {
      ...total,
      items: merged(total.items, sold[0]!.costs, (cost) => cost.item),
      progress: merged(total.progress, sold[0]!.progress ?? [], (cost) => cost.kind),
    };
  }
  return total;
}

/** Several ranges as one: a goal row's tracks, summed. */
export function sumOf(ranges: RangeCost[]): RangeCost {
  return ranges.reduce(
    (total, range) => ({
      items: merged(total.items, range.items, (cost) => cost.item),
      progress: merged(total.progress, range.progress, (cost) => cost.kind),
      choices: [...total.choices, ...range.choices],
      links: total.links + range.links,
    }),
    NO_COST,
  );
}

/** The list with each addition folded into the entry of the same key, in order of first appearance. */
function merged<T extends { quantity: number }>(into: T[], add: T[], key: (cost: T) => string): T[] {
  const next = into.map((cost) => ({ ...cost }));
  for (const cost of add) {
    const existing = next.find((candidate) => key(candidate) === key(cost));
    if (existing) existing.quantity += cost.quantity;
    else next.push({ ...cost });
  }
  return next;
}
