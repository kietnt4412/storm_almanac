import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import type { Odds } from '../api/client';
import { PullStrip } from './PullStrip';

/**
 * C2.14: a cell a pull from the counter to the guarantee, the counted ones
 * filled, the afforded ones lit, and a day slider whose pulls and chance are
 * `byDay`'s. The heat on a cell is the step in `curve` onto that pull.
 */
function odds(overrides: Partial<Odds> = {}): Odds {
  return {
    banner: 'b',
    bannerName: 'Banner',
    versionSequence: 18,
    versionLabel: 'Anchored in Faith',
    daysAsked: 10,
    days: 10,
    cappedAtClose: false,
    closesAt: null,
    copies: 1,
    pity: { banner: 'b', scopeKey: 'k', pullsSinceHit: 2, consecutiveLosses: 0, guaranteedNext: false, hardAt: 6 },
    budget: { currency: 'c', currencyName: 'Tickets', held: 0, accruing: 0, perPull: 1, pulls: 1, uncounted: [], converted: [] },
    chance: 0.2,
    expectedPulls: 3,
    worstCasePulls: 4,
    curve: [0, 0.1, 0.2, 0.3, 1],
    byDay: [
      { day: 0, pulls: 1, chance: 0.1 },
      { day: 5, pulls: 2, chance: 0.2 },
      { day: 10, pulls: 3, chance: 0.3 },
    ],
    method: 'chain',
    ...overrides,
  };
}

const states = () => [...document.querySelectorAll('.pull-cells .pull-cell')].map((cell) => cell.getAttribute('data-state'));

describe('the pull strip', () => {
  it('fills the counted pulls, then one cell a pull to the guarantee, lit as far as the asked day affords', () => {
    render(<PullStrip odds={odds()} />);
    expect(states()).toEqual(['counted', 'counted', 'afforded', 'afforded', 'afforded', 'ahead']);
    expect(screen.getByText('30%')).toBeInTheDocument();
  });

  it('heats the pull most likely to land the hit: here the guarantee', () => {
    render(<PullStrip odds={odds()} />);
    const heat = [...document.querySelectorAll<HTMLElement>('.pull-cells .pull-cell:not([data-state=counted])')].map((cell) =>
      Number(cell.style.getPropertyValue('--heat')),
    );
    expect(heat).toEqual([0.143, 0.143, 0.143, 1]);
  });

  it("moves along the days, saying each day's pulls and chance", () => {
    render(<PullStrip odds={odds()} />);
    fireEvent.change(screen.getByRole('slider', { name: 'Days from now' }), { target: { value: '0' } });
    expect(states()).toEqual(['counted', 'counted', 'afforded', 'ahead', 'ahead', 'ahead']);
    expect(screen.getByText('10%')).toBeInTheDocument();
    expect(screen.getByText('Today')).toBeInTheDocument();
  });

  it('draws nothing for a server that sends no curve', () => {
    const { container } = render(<PullStrip odds={odds({ curve: undefined })} />);
    expect(container).toBeEmptyDOMElement();
  });
});
