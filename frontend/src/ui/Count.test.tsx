import { act, render } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { Count } from './motion';

/**
 * A number that counts to its value (C2.17). The motion is a guest: the value
 * is on the page at once wherever motion cannot play, a screen reader is told
 * the value and never the frames, and the count always ends on the truth.
 */
describe('a counted number', () => {
  afterEach(() => {
    vi.useRealTimers();
    vi.unstubAllGlobals();
  });

  it('is its value at once where nothing animates', () => {
    const { container } = render(<Count value={4017} />);
    expect(container.textContent).toBe('4,017');
  });

  it('is its value at once for a reader who asked for less motion', () => {
    motion('reduce');
    const { container } = render(<Count value={4017} />);
    expect(container.textContent).toBe('4,017');
  });

  it('counts up in whole steps, tells a screen reader only the value, and ends on it', () => {
    motion('no-preference');
    vi.useFakeTimers({ toFake: ['requestAnimationFrame', 'cancelAnimationFrame', 'performance'] });
    const { container } = render(<Count value={4017} duration={900} />);

    act(() => vi.advanceTimersByTime(300));
    const frame = container.querySelector('[aria-hidden="true"]')!.textContent!;
    expect(Number(frame.replace(/,/g, ''))).toBeGreaterThan(0);
    expect(Number(frame.replace(/,/g, ''))).toBeLessThan(4017);
    expect(frame).not.toContain('.');
    expect(container.querySelector('.sr-only')).toHaveTextContent('4,017');

    act(() => vi.advanceTimersByTime(1000));
    expect(container.textContent).toBe('4,017');
  });

  it('ends on its value in a window that draws no frames', () => {
    motion('no-preference');
    vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout'] });
    vi.stubGlobal('requestAnimationFrame', () => 0);
    vi.stubGlobal('cancelAnimationFrame', () => {});
    const { container } = render(<Count value={4017} duration={900} />);
    expect(container.querySelector('[aria-hidden="true"]')).toHaveTextContent('0');

    act(() => vi.advanceTimersByTime(1200));
    expect(container.textContent).toBe('4,017');
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
