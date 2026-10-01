import { act, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { Intro } from './Intro';
import { Words } from './motion';

/**
 * The opening (C2.6, agreed 2026-10-01): once per browser session, skippable,
 * and never for a reader who asked for less motion. Each of those is a promise
 * a reader would notice broken — an opening on every visit to Home is the one
 * that would make them stop coming back.
 */
describe('the opening', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    window.sessionStorage.clear();
    document.documentElement.removeAttribute('data-intro');
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.unstubAllGlobals();
  });

  it('plays once, holds the page entrances while it does, then lifts and goes', () => {
    motion('no-preference');
    const { unmount } = render(<Intro game={null} />);

    expect(screen.getByTestId('intro')).toBeInTheDocument();
    expect(document.documentElement).toHaveAttribute('data-intro', 'on');

    act(() => vi.advanceTimersByTime(1500));
    expect(screen.getByTestId('intro')).toHaveClass('intro-wipe');
    expect(document.documentElement).not.toHaveAttribute('data-intro');

    act(() => vi.advanceTimersByTime(650));
    expect(screen.queryByTestId('intro')).not.toBeInTheDocument();
    unmount();

    // The same session: a reload, or Home opened again.
    render(<Intro game={null} />);
    expect(screen.queryByTestId('intro')).not.toBeInTheDocument();
  });

  it('skips straight to the lift on any key', () => {
    motion('no-preference');
    render(<Intro game={null} />);

    fireEvent.keyDown(window, { key: 'x' });
    expect(screen.getByTestId('intro')).toHaveClass('intro-wipe');
  });

  it('is never shown to a reader who asked for less motion', () => {
    motion('reduce');
    render(<Intro game={null} />);
    expect(screen.queryByTestId('intro')).not.toBeInTheDocument();
    expect(document.documentElement).not.toHaveAttribute('data-intro');
  });

  it('is not shown where the browser cannot say what the reader asked for', () => {
    // jsdom has no matchMedia: the still page is the safe answer.
    render(<Intro game={null} />);
    expect(screen.queryByTestId('intro')).not.toBeInTheDocument();
  });
});

describe('words that rise', () => {
  it('say the sentence whole to a screen reader, not word by word', () => {
    render(
      <h1>
        <Words text="Work out the cheapest way to get there" />
      </h1>,
    );
    expect(screen.getByRole('heading', { name: 'Work out the cheapest way to get there' })).toBeInTheDocument();
  });
});

function motion(preference: 'reduce' | 'no-preference') {
  vi.stubGlobal(
    'matchMedia',
    vi.fn((query: string) => ({
      matches: query.includes(preference === 'reduce' ? 'reduce' : '__never__'),
      media: query,
      addEventListener: () => {},
      removeEventListener: () => {},
    })),
  );
}
