import { useEffect, useState } from 'react';

/**
 * Countdowns for Home: to the game's daily reset and to a banner's close.
 *
 * <p>The reset is computed here from the zone and hour the game declares,
 * rather than sent as an instant, so a games list the worker cached last week
 * still counts down to the right hour. Zone arithmetic goes through `Intl`,
 * which knows every IANA zone, plus the fixed-offset spellings Java's `ZoneId`
 * writes and `Intl` may not accept ("Z", "UTC+07:00").
 */

const HOUR = 3_600_000;
const DAY = 24 * HOUR;

/** Minutes the zone's clock is ahead of UTC at this instant, or null for a zone neither reader knows. */
export function offsetMinutes(zone: string, at: Date): number | null {
  const fixed = /^(?:Z|UTC|GMT)$/.test(zone) ? [0] : /^(?:UTC|GMT)?([+-])(\d{2}):?(\d{2})?$/.exec(zone);
  if (fixed) {
    if (fixed.length === 1) return 0;
    const sign = fixed[1] === '-' ? -1 : 1;
    return sign * (Number(fixed[2]) * 60 + Number(fixed[3] ?? 0));
  }
  try {
    const parts = new Intl.DateTimeFormat('en-US', {
      timeZone: zone,
      hourCycle: 'h23',
      year: 'numeric',
      month: 'numeric',
      day: 'numeric',
      hour: 'numeric',
      minute: 'numeric',
      second: 'numeric',
    }).formatToParts(at);
    const part = (type: string) => Number(parts.find((candidate) => candidate.type === type)?.value);
    const wall = Date.UTC(part('year'), part('month') - 1, part('day'), part('hour'), part('minute'), part('second'));
    return Math.round((wall - Math.floor(at.getTime() / 1000) * 1000) / 60_000);
  } catch {
    return null;
  }
}

/**
 * The next instant the game's day starts: the coming `hour`:00 on the zone's
 * clock, strictly after `now`. Null when the zone cannot be read, so the page
 * says nothing rather than a wrong hour.
 */
export function nextReset(zone: string, hour: number, now: Date): Date | null {
  const offset = offsetMinutes(zone, now);
  if (offset === null) return null;
  const wallNow = new Date(now.getTime() + offset * 60_000);
  let wall = Date.UTC(wallNow.getUTCFullYear(), wallNow.getUTCMonth(), wallNow.getUTCDate(), hour);
  if (wall <= wallNow.getTime()) wall += DAY;
  // The offset at the reset itself, which a summer-time change in between moves.
  const offsetThen = offsetMinutes(zone, new Date(wall - offset * 60_000)) ?? offset;
  return new Date(wall - offsetThen * 60_000);
}

/** "34 days", "1 day 6 h", "3 h 12 min", "8 min" — coarse, because it is read at a glance. */
export function formatUntil(ms: number): string {
  if (ms <= 0) return 'now';
  const days = Math.floor(ms / DAY);
  const hours = Math.floor((ms % DAY) / HOUR);
  const minutes = Math.floor((ms % HOUR) / 60_000);
  if (days >= 2) return `${days} days`;
  if (days === 1) return hours > 0 ? `1 day ${hours} h` : '1 day';
  if (hours >= 1) return minutes > 0 ? `${hours} h ${minutes} min` : `${hours} h`;
  return `${Math.max(1, minutes)} min`;
}

/** The current time, re-read every `everyMs` so a countdown on screen moves. */
export function useNow(everyMs = 30_000): Date {
  const [now, setNow] = useState(() => new Date());
  useEffect(() => {
    const timer = setInterval(() => setNow(new Date()), everyMs);
    return () => clearInterval(timer);
  }, [everyMs]);
  return now;
}

/**
 * Whether something has been waited on for longer than `afterMs`. A wait that
 * ends sooner is never mentioned, so a warm server says nothing at all.
 */
export function useTakingLong(waiting: boolean, afterMs = 3_000): boolean {
  const [long, setLong] = useState(false);
  useEffect(() => {
    if (!waiting) {
      setLong(false);
      return;
    }
    const timer = setTimeout(() => setLong(true), afterMs);
    return () => clearTimeout(timer);
  }, [waiting, afterMs]);
  return waiting && long;
}

/** Days as a reader says them: "28", and "13.5" only when the half matters. */
export function daysOf(days: number): string {
  const tenths = Math.round(days * 10) / 10;
  return Number.isInteger(tenths) ? String(tenths) : tenths.toFixed(1);
}
