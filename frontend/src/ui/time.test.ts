import { describe, expect, it } from 'vitest';
import { formatUntil, nextReset, offsetMinutes } from './time';

describe('the next daily reset', () => {
  it('is today at the hour when the hour is still ahead, on a game whose clock is UTC', () => {
    expect(nextReset('UTC', 5, new Date('2026-10-01T00:18:00Z'))?.toISOString()).toBe('2026-10-01T05:00:00.000Z');
  });

  it('is tomorrow once the hour has passed, and never the instant itself', () => {
    expect(nextReset('UTC', 5, new Date('2026-10-01T07:00:00Z'))?.toISOString()).toBe('2026-10-02T05:00:00.000Z');
    expect(nextReset('UTC', 5, new Date('2026-10-01T05:00:00Z'))?.toISOString()).toBe('2026-10-02T05:00:00.000Z');
  });

  it("follows a civil zone's summer time", () => {
    // 05:00 in New York is 09:00 UTC in summer and 10:00 UTC in winter.
    expect(nextReset('America/New_York', 5, new Date('2026-07-01T12:00:00Z'))?.toISOString()).toBe(
      '2026-07-02T09:00:00.000Z',
    );
    expect(nextReset('America/New_York', 5, new Date('2026-12-01T12:00:00Z'))?.toISOString()).toBe(
      '2026-12-02T10:00:00.000Z',
    );
  });

  it('reads the fixed offsets Java writes, and says nothing for a zone it cannot read', () => {
    expect(offsetMinutes('Z', new Date())).toBe(0);
    expect(offsetMinutes('UTC+07:00', new Date())).toBe(420);
    expect(offsetMinutes('-05:00', new Date())).toBe(-300);
    expect(nextReset('Not/AZone', 5, new Date())).toBeNull();
  });
});

describe('a countdown', () => {
  it('is coarse: days far off, hours and minutes close', () => {
    const hour = 3_600_000;
    expect(formatUntil(34 * 24 * hour + 5 * hour)).toBe('34 days');
    expect(formatUntil(30 * hour)).toBe('1 day 6 h');
    expect(formatUntil(3 * hour + 12 * 60_000)).toBe('3 h 12 min');
    expect(formatUntil(8 * 60_000)).toBe('8 min');
    expect(formatUntil(-1)).toBe('now');
  });
});
