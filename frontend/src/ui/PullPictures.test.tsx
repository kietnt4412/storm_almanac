import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { ChanceByDate, CopiesBar, PityDial } from './PullPictures';

/**
 * C2.8's pull pictures. jsdom draws nothing, so what is pinned is what each
 * says in words and the numbers the drawing is made of.
 */
describe('the pity dial', () => {
  it('says how many pulls to a fixed wall, and fills to the counter', () => {
    const { container } = render(<PityDial pulls={23} hardAt={60} drawnFrom={null} />);
    expect(screen.getByText(/to the guarantee/)).toHaveTextContent('37 more pulls to the guarantee.');
    // The rail, and the fill; no band for a wall that is fixed.
    expect(container.querySelectorAll('path')).toHaveLength(2);
  });

  it('draws a drawn wall as a band and says the range (ADR 0023)', () => {
    const { container } = render(<PityDial pulls={10} hardAt={100} drawnFrom={80} />);
    expect(screen.getByText(/Certain somewhere/)).toHaveTextContent('Certain somewhere in the next 70 to 90 pulls.');
    expect(container.querySelectorAll('path')).toHaveLength(3);
  });

  it('says the next pull is certain at the wall', () => {
    render(<PityDial pulls={60} hardAt={60} drawnFrom={null} />);
    expect(screen.getByText('The next pull is certain.')).toBeInTheDocument();
  });
});

describe('the chance by date', () => {
  const now = new Date('2026-10-01T09:00:00Z');
  const byDay = Array.from({ length: 35 }, (_, day) => ({
    day,
    pulls: 112 + Math.floor(day * 2.4),
    chance: day >= 17 ? 1 : 0.84 + day * 0.009,
  }));

  it('says the chance at the horizon asked, and the first day it is certain', () => {
    render(<ChanceByDate byDay={byDay} asked={0} closesAt="2026-11-04T23:00:00Z" now={now} />);
    expect(screen.getByText(/By/, { selector: 'figcaption' })).toHaveTextContent(
      'By Oct 1 (112 pulls): 84.0%. Certain by Oct 18, before the banner closes.',
    );
  });

  it('says the chance at the close when it never becomes certain', () => {
    const never = byDay.map((point) => ({ ...point, chance: 0.5 }));
    render(<ChanceByDate byDay={never} asked={3} closesAt="2026-11-04T23:00:00Z" now={now} />);
    expect(screen.getByText(/By the close/, { selector: 'figcaption' })).toHaveTextContent('By Oct 4 (119 pulls): 50.0%. By the close, Nov 4: 50.0%.');
  });
});

describe('the copies', () => {
  it('draws each count as wide as its chance, and says every one with the shop left out', () => {
    render(<CopiesBar byCopies={[0.04, 0.58, 0.3, 0.08]} />);
    expect(screen.getAllByTestId('copies-segment').map((segment) => segment.style.width)).toEqual([
      '4%',
      '57.99999999999999%',
      '30%',
      '8%',
    ]);
    expect(screen.getByText(/exactly 1/)).toHaveTextContent(
      'none: 4.0% · exactly 1: 58.0% · exactly 2: 30.0% · 3 or more: 8.0%. Copies bought in the shop are not counted',
    );
  });
});
