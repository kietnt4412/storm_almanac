import { fireEvent, render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { Odds } from '../api/client';
import { RollIt } from './RollIt';

/**
 * Roll it yourself (C2.21). jsdom has no worker, no canvas and no motion, so
 * this is the page's own-thread fallback and the finished pile — the same dice
 * a browser rolls, without the rain.
 */
describe('rolling the dice against the chain', () => {
  beforeEach(() => {
    // jsdom draws nothing and says so loudly; the panel paints only where it can.
    vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockReturnValue(null);
  });

  it('shows nothing for an answer from a server that sent no tables', () => {
    const { container } = render(<RollIt odds={{ ...odds(), model: undefined }} />);
    expect(container).toBeEmptyDOMElement();
  });

  it('rolls twenty thousand histories and lands within a point of the chain', async () => {
    render(<RollIt odds={odds()} />);

    // The reader's own pulls are where it reads first.
    expect(tile('Within 15 — the chain')).toHaveTextContent('7.2%');
    expect(tile('— your dice')).toHaveTextContent('—');

    await userEvent.click(screen.getByRole('button', { name: 'Roll 20,000 histories' }));

    expect(await screen.findByRole('button', { name: 'Roll again' })).toBeInTheDocument();
    expect(tile('Histories')).toHaveTextContent('20,000');
    const dice = Number(/([\d.]+)%/.exec(tile('— your dice').textContent ?? '')?.[1]);
    expect(Math.abs(dice - 7.2)).toBeLessThan(1);
    expect(screen.getByText(/largest gap .* where chance alone allows up to 0\.96 · the chain says 7\.2% within 15 pulls/)).toBeInTheDocument();
  });

  it('opens at the average pulls for a reader who can afford none, where "within 0" would say nothing', () => {
    const broke = odds();
    render(<RollIt odds={{ ...broke, budget: { ...broke.budget, held: 0, pulls: 0 } }} />);

    expect(screen.getByText('Within 52 — the chain')).toBeInTheDocument();
  });

  it('reads the chain at any pull count the reader slides to', () => {
    render(<RollIt odds={odds()} />);

    fireEvent.change(screen.getByLabelText('Pulls'), { target: { value: '60' } });

    expect(tile('Within 60 — the chain')).toHaveTextContent('100%');
  });

  function tile(label: string): HTMLElement {
    const found = screen.getByText(label).parentElement;
    if (!found) throw new Error(`no tile labelled ${label}`);
    return within(found).getByText(/./, { selector: '.stat-value' });
  }
});

/** A wall of 60 at 0.5% with every hit the featured unit, from a fresh counter, fifteen pulls afforded. */
function odds(): Odds {
  const curve = Array.from({ length: 61 }, (_, pulls) => (pulls === 60 ? 1 : Math.round((1 - 0.995 ** pulls) * 10_000) / 10_000));
  return {
    banner: 'b',
    bannerName: 'Banner',
    versionSequence: 18,
    versionLabel: '4.8.0',
    daysAsked: 0,
    days: 0,
    cappedAtClose: false,
    closesAt: null,
    copies: 1,
    pity: { banner: 'b', scopeKey: 'type:character', pullsSinceHit: 0, consecutiveLosses: 0, guaranteedNext: false, hardAt: 60 },
    budget: { currency: 't', currencyName: 'Ticket', held: 3_750, accruing: 0, perPull: 250, pulls: 15, uncounted: [], converted: [] },
    chance: curve[15] ?? 0,
    expectedPulls: 52,
    worstCasePulls: 60,
    curve,
    method: 'markov-chain',
    model: {
      walls: [{ wall: 60, hitRates: Array.from({ length: 60 }, (_, misses) => (misses === 59 ? 1 : 0.005)) }],
      firstWalls: [60],
      featuredChance: [1, 1],
    },
  };
}
