import { render } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { Emblem, initials, serial } from './Emblem';

const LUCIA = { id: 'lucia-inverse-crown', displayName: 'Lucia: Inverse Crown', kind: 'character', rarity: { label: 'S', rank: 5 } };
const SELENA = { id: 'selena-pianissimo', displayName: 'Selena: Pianissimo', kind: 'character', rarity: { label: 'S', rank: 5 } };

function drawn(subject: typeof LUCIA, game: string | null = 'punishing-gray-raven'): string {
  const { container } = render(<Emblem subject={subject} ranks={[5, 4]} game={game} />);
  // useId differs between renders; the drawing is everything but the clip's name.
  return container.innerHTML.replace(/_r_[^_]*_|«[^»]*»|:[a-z0-9]+:/g, 'ID');
}

describe('an emblem', () => {
  it('draws the same face for the same thing every time', () => {
    expect(drawn(LUCIA)).toBe(drawn(LUCIA));
  });

  it('draws two things of one kind differently', () => {
    expect(drawn(LUCIA)).not.toBe(drawn(SELENA));
  });

  it("is framed in the game's own style, and as the Storm hexagon for a game with no look", () => {
    const style = (game: string | null) =>
      render(<Emblem subject={LUCIA} game={game} />).container.querySelector('svg')?.getAttribute('data-emblem');
    expect(style('punishing-gray-raven')).toBe('plate');
    expect(style('reverse-1999')).toBe('seal');
    expect(style('proving-ground')).toBe('hex');
  });

  it('carries the initials of the first two words, never punctuation', () => {
    expect(initials('Helentine: Lacrimosa')).toBe('HL');
    expect(initials('EXP Pod (XL)')).toBe('EP');
    expect(initials('Samantha')).toBe('S');
    expect(initials('5★ Memory Shard')).toBe('5M');
  });

  it('stamps a code that belongs to the thing', () => {
    expect(serial('lucia-inverse-crown', 'plate')).toBe(serial('lucia-inverse-crown', 'plate'));
    expect(serial('lucia-inverse-crown', 'plate')).toMatch(/^[0-9A-Z]{2}-[0-9A-Z]{2}$/);
    expect(serial('lucia-inverse-crown', 'seal')).toMatch(/^Nº \d{3}$/);
    expect(serial('lucia-inverse-crown', 'plate')).not.toBe(serial('selena-pianissimo', 'plate'));
  });

  it('is hidden from a screen reader, which reads the name beside it', () => {
    const { container } = render(<Emblem subject={LUCIA} game={null} />);
    expect(container.querySelector('svg')).toHaveAttribute('aria-hidden', 'true');
  });
});
