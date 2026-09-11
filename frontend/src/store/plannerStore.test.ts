import { renderHook } from '@testing-library/react';
import { beforeEach, describe, expect, it } from 'vitest';
import {
  effectiveInventory,
  effectiveRoster,
  outboxOf,
  pendingCount,
  usePlannerStore,
} from './plannerStore';

/**
 * The outbox's rules, including the one that cost a whole screen.
 *
 * These are pure functions over the store rather than rendered screens, which is
 * the point: every one of them is a rule a browser session had to rediscover by
 * looking at a blank page, and none of them needed a browser to state.
 */
describe('the offline outbox', () => {
  beforeEach(() => {
    usePlannerStore.setState({ profileId: null, outbox: {}, rejected: [], lastSyncedAt: null });
  });

  it('hands back the same empty outbox every time it is asked', () => {
    // Not a micro-optimisation, and not a style preference. Zustand reads the
    // store through useSyncExternalStore, which compares what a selector
    // returned with what it returned last; a selector building a fresh object
    // never compares equal, so every render schedules another one. This is the
    // defect that rendered the inventory screen as a blank page, and the shape
    // of it is invisible to a typechecker — both versions have the same type.
    const state = usePlannerStore.getState();

    expect(outboxOf(state, 'nobody')).toBe(outboxOf(state, 'nobody-else'));
    expect(outboxOf(state, null)).toBe(outboxOf(state, 'nobody'));
  });

  it('renders a profile with no pending edits without looping', () => {
    // The invariant above, asserted as the symptom a reader would see. React
    // throws rather than hanging when a snapshot is not cached, so a regression
    // fails here loudly instead of timing the suite out.
    const { result } = renderHook(() => usePlannerStore((state) => outboxOf(state, 'fresh-profile')));

    expect(pendingCount(result.current)).toBe(0);
  });

  it('keeps one pending edit per key, not a log of keystrokes', () => {
    // Typing 4 and then 6 into the same field is one edit of 6. The merge at the
    // far end is per key too (ADR 0014), so sending both would make the network
    // carry a keystroke log to reach the same answer.
    usePlannerStore.getState().editQuantity('p1', 'gold', 4);
    usePlannerStore.getState().editQuantity('p1', 'gold', 6);

    const outbox = outboxOf(usePlannerStore.getState(), 'p1');
    expect(Object.keys(outbox.inventory)).toEqual(['gold']);
    expect(outbox.inventory.gold?.value).toBe(6);
  });

  it('does not flush one profile’s counts into another’s', () => {
    // A person runs several profiles and a phone switches between them. The
    // outbox is keyed by profile for this reason and for no other.
    usePlannerStore.getState().editQuantity('p1', 'gold', 4);
    usePlannerStore.getState().editQuantity('p2', 'gold', 900);

    expect(outboxOf(usePlannerStore.getState(), 'p1').inventory.gold?.value).toBe(4);
    expect(outboxOf(usePlannerStore.getState(), 'p2').inventory.gold?.value).toBe(900);
  });

  it('stamps an edit when it was typed, not when it is sent', () => {
    // The whole reason this is an outbox and not a retry wrapper: an edit made
    // on a train at 09:00 and flushed at 10:30 has to lose to one made in a
    // browser at 10:00, and it can only do that if it still carries 09:00.
    const before = Date.now();
    usePlannerStore.getState().editQuantity('p1', 'gold', 4);

    const at = Date.parse(outboxOf(usePlannerStore.getState(), 'p1').inventory.gold?.at ?? '');
    expect(at).not.toBeNaN();
    expect(at).toBeGreaterThanOrEqual(before);
    expect(at).toBeLessThanOrEqual(Date.now());
  });

  it('shows what the player typed over what the server last said', () => {
    // A player who types 40 must see 40 immediately, reachable network or not.
    // Showing the stored value until a flush confirms it would make an offline
    // editor feel broken in the one situation it exists for.
    const merged = effectiveInventory({ gold: 1, ore: 7 }, { gold: { value: 40, at: now() } });

    expect(merged).toEqual({ gold: 40, ore: 7 });
  });

  it('treats a removal from the roster as a removal, not as an empty string', () => {
    // null means "no longer owned" and has to delete the key. Merging it as a
    // value would leave a character on the roster at a state that does not
    // exist, and the planner would then be asked what it costs to reach it.
    const merged = effectiveRoster(
      { sotheby: 'insight-2', regulus: 'insight-1' },
      { regulus: { value: null, at: now() } },
    );

    expect(merged).toEqual({ sotheby: 'insight-2' });
  });
});

const now = () => new Date().toISOString();
