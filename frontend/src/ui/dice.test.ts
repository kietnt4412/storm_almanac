import { describe, expect, it } from 'vitest';
import type { PullTables } from '../api/client';
import { boardOf, cumulative, gapChanceAllows, largestGap, rollHistories, rollHistory, seeded } from './dice';

/**
 * The dice against curves worked out here by hand, in closed form, so the
 * fixture shares no code with the roller: if both were wrong the same way the
 * test would pass, which is exactly what two roads are for.
 */

const HISTORIES = 20_000;

/** Tables as the server writes them for a curve that is flat until its wall. */
function flat(rate: number, walls: number[], firstWalls: number[], featuredChance: number[]): PullTables {
  return {
    walls: walls.map((wall) => ({ wall, hitRates: Array.from({ length: wall }, (_, misses) => (misses === wall - 1 ? 1 : rate)) })),
    firstWalls,
    featuredChance,
  };
}

/** Chance of the first hit at pull n, n = 1.., on a flat rate with the wall drawn from `walls` after `carried` misses. */
function firstHit(rate: number, walls: number[], carried: number, upTo: number): number[] {
  const possible = walls.filter((wall) => wall > carried);
  const pmf = [0];
  for (let n = 1; n <= upTo; n++) {
    const pull = carried + n;
    const forced = possible.filter((wall) => wall === pull).length / possible.length;
    const above = possible.filter((wall) => wall > pull).length / possible.length;
    pmf.push((1 - rate) ** (n - 1) * (forced + above * rate));
  }
  return pmf;
}

function cdf(pmf: number[]): number[] {
  let running = 0;
  return pmf.map((chance) => (running += chance));
}

function roll(tables: PullTables, start: { pullsSinceHit: number; consecutiveLosses: number }, copies: number, ceiling: number) {
  const results = rollHistories(boardOf(tables), start, copies, ceiling, HISTORIES, seeded(20261002));
  const tally = new Array(ceiling + 1).fill(0);
  for (const pulls of results) tally[pulls]++;
  return cumulative(tally, HISTORIES);
}

function expectWithinThreeStandardErrors(dice: number[], exact: number[]) {
  expect(dice).toHaveLength(exact.length);
  exact.forEach((chance, pulls) => {
    const error = Math.sqrt((chance * (1 - chance)) / HISTORIES);
    expect(Math.abs((dice[pulls] ?? 0) - chance), `at ${pulls} pulls`).toBeLessThanOrEqual(3 * error + 1e-9);
  });
  expect(largestGap(dice, exact)).toBeLessThan(gapChanceAllows(HISTORIES));
}

const fresh = { pullsSinceHit: 0, consecutiveLosses: 0 };
const range = (from: number, to: number) => Array.from({ length: to - from + 1 }, (_, at) => from + at);

describe('the dice against the exact curve (C2.21)', () => {
  it('land within three standard errors on a fixed wall of 60', () => {
    const exact = cdf(firstHit(0.005, [60], 0, 60));
    expectWithinThreeStandardErrors(roll(flat(0.005, [60], [60], [1, 1]), fresh, 1, 60), exact);
  });

  it('land within three standard errors on a wall drawn from 80–100, drawing it per cycle', () => {
    const walls = range(80, 100);
    const exact = cdf(firstHit(0.015, walls, 0, 100));
    expectWithinThreeStandardErrors(roll(flat(0.015, walls, walls, [1, 1]), fresh, 1, 100), exact);
  });

  it('start from the counter carried: 85 misses on 80–100 can only have drawn 86 or more', () => {
    const walls = range(80, 100);
    const exact = cdf(firstHit(0.015, walls, 85, 15));
    expectWithinThreeStandardErrors(
      roll(flat(0.015, walls, range(86, 100), [1, 1]), { pullsSinceHit: 85, consecutiveLosses: 0 }, 1, 15),
      exact,
    );
  });

  it('lose a 70/30 split and go again, to 120 pulls at worst', () => {
    const one = firstHit(0.005, [60], 0, 60);
    const exact = [0];
    for (let n = 1; n <= 120; n++) {
      let twice = 0;
      for (let first = 1; first < n; first++) twice += (one[first] ?? 0) * (one[n - first] ?? 0);
      exact.push(0.7 * (one[n] ?? 0) + 0.3 * twice);
    }
    expectWithinThreeStandardErrors(roll(flat(0.005, [60], [60], [0.7, 1]), fresh, 1, 120), cdf(exact));
  });

  it('roll the same histories from the same seed, and different ones from another', () => {
    const board = boardOf(flat(0.015, range(80, 100), range(80, 100), [1, 1]));
    const once = rollHistories(board, fresh, 1, 100, 50, seeded(7));
    expect(rollHistories(board, fresh, 1, 100, 50, seeded(7))).toEqual(once);
    expect(rollHistories(board, fresh, 1, 100, 50, seeded(8))).not.toEqual(once);
  });

  it('refuse tables that let a history pass its worst case, rather than spin', () => {
    const never: PullTables = { walls: [{ wall: 60, hitRates: new Array(60).fill(0) }], firstWalls: [60], featuredChance: [1, 1] };
    expect(() => rollHistory(boardOf(never), fresh, 1, 60, seeded(1))).toThrow(/worst case of 60/);
    expect(() => boardOf({ ...never, firstWalls: [61] })).toThrow(/wall of 61/);
  });
});
