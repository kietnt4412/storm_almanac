import { describe, expect, it } from 'vitest';
import type { Item } from '../api/client';
import { groupByCategory } from './Inventory';

/**
 * The inventory's headings, S8 of the second rehearsal: one `CHARACTER-EXP-POD-*`
 * heading per Pod size, and `HARMONY-MATERIAL`, because a category was a key a
 * fodder rule matched on and nothing more. Since sequence 13 a category can have
 * a word, several categories can share one, and the page groups by the word.
 */
describe('grouping the inventory', () => {
  const item = (id: string, category: string, categoryName?: string): Item => ({
    id,
    displayName: id,
    rarity: { label: '4★', rank: 4 },
    category,
    ...(categoryName ? { categoryName } : {}),
  });

  it('puts categories that share a word under one heading, in arrival order', () => {
    const groups = groupByCategory([
      item('exp-pod-xl', 'character-exp-pod-xl', 'Character EXP'),
      item('cogs', 'currency', 'Currencies'),
      item('exp-pod-l', 'character-exp-pod-l', 'Character EXP'),
    ]);
    expect(groups.map(([heading, items]) => [heading, items.map((one) => one.id)])).toEqual([
      ['Character EXP', ['exp-pod-xl', 'exp-pod-l']],
      ['Currencies', ['cogs']],
    ]);
  });

  it('falls back to the key where there is no word, as every version before sequence 13 does', () => {
    expect(groupByCategory([item('accelerator', 'harmony-material')])[0]![0]).toBe('harmony-material');
  });
});
