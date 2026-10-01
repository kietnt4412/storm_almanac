import type { Rarity } from '../api/client';

/**
 * Every rank in a list, highest first. A rarity's colour is its place in this
 * order, so the colours come from the data rather than from a game's scale.
 */
export function ranksOf(things: { rarity: Rarity }[]): number[] {
  // A thing with no rarity (a fixture, a partial response) is left out, never thrown on.
  return [...new Set(things.flatMap((thing) => (thing.rarity ? [thing.rarity.rank] : [])))].sort((a, b) => b - a);
}

/**
 * The ranks of each kind, highest first. A weapon's scale is not a construct's:
 * a catalog that mixed them would colour the best construct by where it sits
 * among weapons.
 */
export function ranksByKind<T extends { rarity: Rarity }>(things: T[], kindOf: (thing: T) => string): Map<string, number[]> {
  const byKind = new Map<string, T[]>();
  for (const thing of things) byKind.set(kindOf(thing), [...(byKind.get(kindOf(thing)) ?? []), thing]);
  return new Map([...byKind].map(([kind, members]) => [kind, ranksOf(members)]));
}

/**
 * A rarity's colour, by its place among the ranks: the highest red, then
 * orange, purple and yellow (the maintainer, 2026-10-01), and anything lower
 * quiet. By position, not by label, so "6★" and "SSR" are coloured the same way
 * without this file knowing either. The four colours are their own tokens in
 * `index.css`, which no game palette redefines.
 */
export function tierColour(rank: number, ranks: number[]): string {
  const tier = ranks.indexOf(rank);
  return tier >= 0 && tier < 4 ? `var(--rarity-${tier + 1})` : 'var(--line)';
}
