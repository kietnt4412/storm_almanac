import { renderHook } from '@testing-library/react';
import { beforeEach, describe, expect, it } from 'vitest';
import {
  effectiveInventory,
  effectiveRoster,
  outboxOf,
  pendingCount,
  reachOf,
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
      { sotheby: ['insight-2'], regulus: ['insight-1'] },
      { regulus: { value: null, at: now() } },
    );

    expect(merged).toEqual({ sotheby: ['insight-2'] });
  });

  it('replaces an entity’s whole state set, because the entity is the merge unit', () => {
    // A pending edit states the entity in full. Merging state by state would
    // leave 'level-40' behind here, and the roster would say something this
    // device never said.
    const merged = effectiveRoster(
      { sotheby: ['insight-1', 'level-40'] },
      { sotheby: { value: ['insight-2'], at: now() } },
    );

    expect(merged).toEqual({ sotheby: ['insight-2'] });
  });
});

/**
 * What the reader says they reach, which is not player state and is not
 * nothing either.
 *
 * ADR 0022 keeps `reach` on the plan request, beside `energyPerDay`: the server
 * stores it nowhere and the solve key hashes it. What the store holds is only
 * the memory of the answer, so that this device stops asking on every solve.
 */
describe('what the reader says they reach', () => {
  beforeEach(() => {
    usePlannerStore.setState({ profileId: null, outbox: {}, reach: {}, rejected: [], lastSyncedAt: null });
  });

  it('hands back the same empty answer every time it is asked', () => {
    // The identical rule to the outbox's, for the identical reason: a selector
    // that builds a fresh object every call makes React schedule a render for
    // every render. It is worth asserting twice because it is invisible to a
    // typechecker, and this one sits on the screen the plan is asked from.
    const state = usePlannerStore.getState();

    expect(reachOf(state, 'nobody')).toBe(reachOf(state, 'nobody-else'));
    expect(reachOf(state, null)).toBe(reachOf(state, 'nobody'));
  });

  it('keeps one answer per measure, per profile', () => {
    // Two profiles are two accounts playing two games; one of them clearing the
    // weekly says nothing about the other, and a plan computed with the wrong
    // one counts income that reader never earns.
    usePlannerStore.getState().setReach('p1', 'phantom-pain-cage-score', 120_000);
    usePlannerStore.getState().setReach('p1', 'phantom-pain-cage-score', 500_000);
    usePlannerStore.getState().setReach('p2', 'phantom-pain-cage-score', 30_000);

    const state = usePlannerStore.getState();
    expect(reachOf(state, 'p1')).toEqual({ 'phantom-pain-cage-score': 500_000 });
    expect(reachOf(state, 'p2')).toEqual({ 'phantom-pain-cage-score': 30_000 });
  });

  it('remembers a deliberate zero rather than forgetting the question was asked', () => {
    // Zero and absent have the same consequence on the wire — neither counts a
    // scored grant — and they are different things to a reader. Somebody who
    // chose "I don't get there" should find their own answer when they come
    // back, not a form that looks untouched.
    usePlannerStore.getState().setReach('p1', 'gauntlet-depth', 800);
    usePlannerStore.getState().setReach('p1', 'gauntlet-depth', 0);

    expect(reachOf(usePlannerStore.getState(), 'p1')).toEqual({ 'gauntlet-depth': 0 });
  });
});

const now = () => new Date().toISOString();
