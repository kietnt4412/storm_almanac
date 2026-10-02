import type { PullTables } from '../api/client';

/**
 * The other road (C2.21): pull histories rolled on the reader's device against
 * the tables the server sent, to set beside the curve its exact chain drew.
 *
 * <p><b>No game's rule is written here.</b> The hit rate at each miss count
 * under each wall, which walls a cycle can draw, and the featured chance after
 * so many losses all come off the wire, read from the same `PullModel` both
 * server engines use. What lives here is the counter: a miss counts up, a hit
 * counts from zero, a lost split adds a loss and the featured unit clears them.
 *
 * <p><b>It draws the wall per cycle, where the chain integrates it out</b>
 * (ADR 0023). Two roads to one distribution, so the dice landing on the curve
 * says something about both — the same argument that caught the server's
 * simulation sampling the prior.
 *
 * <p><b>Seeded</b>, so a test repeats a run exactly, and so can a reader.
 */

/** Where the reader's counter stands as the first pull is taken. */
export interface Start {
  pullsSinceHit: number;
  consecutiveLosses: number;
}

/** One question for the dice: how many histories, from where, for how many copies. */
export interface Ask {
  tables: PullTables;
  start: Start;
  copies: number;
  /** The most pulls `copies` can take; a history past it means the tables are wrong. */
  ceiling: number;
  count: number;
  seed: number;
}

/** mulberry32: four lines, 32 bits of state, and plenty for a chart. */
export function seeded(seed: number): () => number {
  let state = seed >>> 0;
  return () => {
    state = (state + 0x6d2b79f5) >>> 0;
    let mixed = state;
    mixed = Math.imul(mixed ^ (mixed >>> 15), mixed | 1);
    mixed ^= mixed + Math.imul(mixed ^ (mixed >>> 7), mixed | 61);
    return ((mixed ^ (mixed >>> 14)) >>> 0) / 4294967296;
  };
}

/** The tables in the shape a roll reads them: rows to pick from, one per wall. */
export interface Board {
  walls: number[][];
  firstWalls: number[][];
  featuredChance: number[];
}

export function boardOf(tables: PullTables): Board {
  const byWall = new Map(tables.walls.map((wall) => [wall.wall, wall.hitRates]));
  const first = tables.firstWalls.map((wall) => {
    const rates = byWall.get(wall);
    if (!rates) throw new Error(`the first cycle can draw a wall of ${wall}, which the tables do not carry`);
    return rates;
  });
  return { walls: tables.walls.map((wall) => wall.hitRates), firstWalls: first, featuredChance: tables.featuredChance };
}

/** Pulls taken until the `copies`-th featured unit arrives, in one history. */
export function rollHistory(board: Board, start: Start, copies: number, ceiling: number, random: () => number): number {
  let misses = start.pullsSinceHit;
  let losses = start.consecutiveLosses;
  let rates = pick(board.firstWalls, random);
  let held = 0;
  for (let pulls = 1; ; pulls++) {
    if (pulls > ceiling) {
      throw new Error(`a history passed the worst case of ${ceiling} pulls, which the tables forbid`);
    }
    const rate = rates[Math.min(misses, rates.length - 1)] ?? 1;
    if (random() >= rate) {
      misses++;
      continue;
    }
    const featured = board.featuredChance[Math.min(losses, board.featuredChance.length - 1)] ?? 1;
    if (random() < featured) {
      held++;
      losses = 0;
      if (held >= copies) return pulls;
    } else {
      losses++;
    }
    // The game draws again on the rarity arriving, won or lost.
    misses = 0;
    rates = pick(board.walls, random);
  }
}

/** `count` histories off one generator, so consecutive batches continue one run. */
export function rollHistories(
  board: Board,
  start: Start,
  copies: number,
  ceiling: number,
  count: number,
  random: () => number,
): Int32Array {
  const out = new Int32Array(count);
  for (let history = 0; history < count; history++) out[history] = rollHistory(board, start, copies, ceiling, random);
  return out;
}

function pick<T>(choices: T[], random: () => number): T {
  const chosen = choices[Math.floor(random() * choices.length)] ?? choices[choices.length - 1];
  if (chosen === undefined) throw new Error('nothing to draw from');
  return chosen;
}

// ── Reading the pile ────────────────────────────────────────────────────────

/** The share of histories done within each pull count, from a tally indexed by pulls. */
export function cumulative(tally: ArrayLike<number>, landed: number): number[] {
  const out: number[] = [];
  let running = 0;
  for (let pulls = 0; pulls < tally.length; pulls++) {
    running += tally[pulls] ?? 0;
    out.push(landed === 0 ? 0 : running / landed);
  }
  return out;
}

/** The widest the dice's line sits from the chain's, over every pull count. */
export function largestGap(dice: number[], curve: number[]): number {
  let gap = 0;
  for (let pulls = 0; pulls < Math.min(dice.length, curve.length); pulls++) {
    gap = Math.max(gap, Math.abs((dice[pulls] ?? 0) - (curve[pulls] ?? 0)));
  }
  return gap;
}

/**
 * How wide the largest gap can be from chance alone, 95 times in 100: the
 * Kolmogorov–Smirnov bound, 1.36 / √n. For a curve that steps, as this one
 * does, it is a little generous, which only ever makes the dice look worse.
 */
export function gapChanceAllows(landed: number): number {
  return landed === 0 ? 1 : 1.36 / Math.sqrt(landed);
}

// ── Rolling off the main thread ─────────────────────────────────────────────

export const BATCH = 1_000;

/**
 * Rolls `ask` in batches, in a Web Worker where the browser has one, so 20 000
 * histories never hold a frame; in batches on the page's own thread where it
 * has not (jsdom, an old browser), which is slower and gives the same dice.
 * Returns a function that stops it.
 */
export function startRolling(ask: Ask, onBatch: (results: Int32Array) => void, onDone: () => void): () => void {
  if (typeof Worker === 'function') {
    try {
      const worker = new Worker(new URL('./dice.worker.ts', import.meta.url), { type: 'module' });
      worker.onmessage = (event: MessageEvent<{ results?: Int32Array; done?: boolean }>) => {
        if (event.data.results) onBatch(event.data.results);
        if (event.data.done) {
          worker.terminate();
          onDone();
        }
      };
      worker.postMessage(ask);
      return () => worker.terminate();
    } catch {
      // A browser that has workers and will not start this one: roll here.
    }
  }
  const board = boardOf(ask.tables);
  const random = seeded(ask.seed);
  let rolled = 0;
  let timer: ReturnType<typeof setTimeout> | undefined;
  const step = () => {
    const size = Math.min(BATCH, ask.count - rolled);
    onBatch(rollHistories(board, ask.start, ask.copies, ask.ceiling, size, random));
    rolled += size;
    if (rolled < ask.count) timer = setTimeout(step, 0);
    else onDone();
  };
  timer = setTimeout(step, 0);
  return () => clearTimeout(timer);
}
