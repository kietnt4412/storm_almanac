import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import type { Measure } from '../api/client';
import { Ladder } from './PlanView';

/**
 * The question ADR 0022 said would be the first thing to hurt.
 *
 * <p>A measure is an opaque slug the bundle supplies and is declared nowhere —
 * no display name, no description — so this control has to ask a reader about
 * `phantom-pain-cage-score` without ever having a name for it. What makes that
 * answerable is the payout, and the payout is a sum rather than a lookup: a
 * ladder pays every rung at or below where somebody stops. Getting that sum
 * wrong is how a reader picks the rung that does not describe their week.
 *
 * <p>The other rule under test is the default. Silence counts nothing, and being
 * wrong that way makes a plan dearer than the truth rather than cheaper.
 */
describe('asking how far the reader gets', () => {
  const cage: Measure = {
    measure: 'phantom-pain-cage-score',
    bars: [
      {
        reward: 'cage-30000',
        atLeast: 30_000,
        cadence: 'WEEKLY',
        grants: [{ item: 'phantom-pain-scar', displayName: 'Phantom Pain Scar', quantity: 4 }],
      },
      {
        reward: 'cage-90000',
        atLeast: 90_000,
        cadence: 'WEEKLY',
        grants: [
          { item: 'phantom-pain-scar', displayName: 'Phantom Pain Scar', quantity: 5 },
          { item: 'cogs', displayName: 'Cogs', quantity: 6000 },
        ],
      },
    ],
  };

  it('counts nothing until the reader says otherwise, and says so', () => {
    render(<Ladder ladder={cage} score={0} onPick={() => {}} />);

    expect(screen.getByText('none of its 2 tiers counted')).toBeInTheDocument();
  });

  it('adds up every rung at or below the answer, not only the one reached', () => {
    // 4 Scars at the first bar and 5 at the second is 9 a week to somebody who
    // clears both, which is the whole reason the question is worth asking. A
    // control that showed only the top rung's 5 would make the mode look half as
    // valuable as it is.
    render(<Ladder ladder={cage} score={90_000} onPick={() => {}} />);

    expect(screen.getByText(/2 of 2 tiers/)).toBeInTheDocument();
    expect(screen.getByText(/9 Phantom Pain Scar/)).toBeInTheDocument();
    expect(screen.getByText(/6,000 Cogs/)).toBeInTheDocument();
    // The game's own cadence, because "9 Scars" and "9 Scars a week" are
    // different facts and only one of them is true.
    expect(screen.getByText(/a week$/)).toBeInTheDocument();
  });

  it('counts the rungs below the answer and not the one above it', () => {
    render(<Ladder ladder={cage} score={30_000} onPick={() => {}} />);

    expect(screen.getByText(/1 of 2 tiers/)).toBeInTheDocument();
    expect(screen.getByText(/4 Phantom Pain Scar/)).toBeInTheDocument();
    expect(screen.queryByText(/Cogs/)).not.toBeInTheDocument();
  });

  it('offers the bars themselves, because nothing between them changes the answer', async () => {
    // A reader at 150 000 on a ladder whose rungs are 30 000 and 90 000 is
    // answering 90 000. A free number field would invite them to type their real
    // score and then wonder why the plan did not move.
    const picked = vi.fn();
    render(<Ladder ladder={cage} score={0} onPick={picked} />);

    const dropdown = screen.getByLabelText('How far you get in phantom-pain-cage-score');
    expect([...dropdown.querySelectorAll('option')].map((option) => option.value)).toEqual([
      '0',
      '30000',
      '90000',
    ]);

    await userEvent.selectOptions(dropdown, '90000');
    expect(picked).toHaveBeenCalledWith(90_000);
  });

  it('keeps "I don’t get there" on the list after it has been chosen', async () => {
    // It is a real answer rather than a placeholder. A reader who deliberately
    // said no should be able to see that they did, and to change their mind
    // back to it.
    const picked = vi.fn();
    render(<Ladder ladder={cage} score={90_000} onPick={picked} />);

    await userEvent.selectOptions(
      screen.getByLabelText('How far you get in phantom-pain-cage-score'),
      '0',
    );

    expect(picked).toHaveBeenCalledWith(0);
  });
});
