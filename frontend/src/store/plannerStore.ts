import { create } from 'zustand';
import { persist } from 'zustand/middleware';

/**
 * One pending edit: the value, and the clock that will decide a tie.
 *
 * `at` is stamped when the player types, not when the network comes back. That
 * is the entire reason the outbox exists rather than a retry wrapper around a
 * request: an edit made on a train at 09:00 and flushed at 10:30 must lose to
 * one made in a browser at 10:00, and it can only do that if it still carries
 * 09:00. ADR 0014 has the server's half of the argument.
 */
export interface PendingEdit<T> {
  value: T;
  at: string;
}

interface ProfileOutbox {
  /** item slug → quantity. */
  inventory: Record<string, PendingEdit<number>>;
  /** entity slug → every state the entity has reached, or null for "no longer owned". */
  roster: Record<string, PendingEdit<string[] | null>>;
}

interface PlannerState {
  profileId: string | null;

  /**
   * Edits this device has made and the server has not confirmed, per profile.
   *
   * Keyed by profile because a person runs several and a phone that switches
   * between them must not flush one profile's counts into another's. Keyed by
   * item inside that, rather than appended to a list, because the merge is
   * per-key at the far end too: typing 4 and then 6 into the same field is one
   * pending edit of 6, and sending both would make the network carry a
   * keystroke log.
   */
  outbox: Record<string, ProfileOutbox>;

  /** Keys the server refused on the last flush, because it held something newer. */
  rejected: string[];
  lastSyncedAt: string | null;

  selectProfile: (profileId: string) => void;
  editQuantity: (profileId: string, item: string, quantity: number) => void;
  editRosterState: (profileId: string, entity: string, states: string[] | null) => void;
  /** Drop the edits a flush confirmed, keeping anything typed while it was in flight. */
  settle: (profileId: string, flushed: ProfileOutbox, rejected: string[]) => void;
  /** The reader has seen which keys lost; stop saying so. */
  acknowledgeRejected: () => void;
}

const emptyOutbox = (): ProfileOutbox => ({ inventory: {}, roster: {} });

/**
 * The empty outbox a selector hands back, as one frozen value.
 *
 * <p>It has to be the same object every time, and that is not a micro-optimisation:
 * zustand reads the store through `useSyncExternalStore`, which compares the
 * value a selector returned with the one it returned last. A selector that built
 * `{ inventory: {}, roster: {} }` fresh on every call never compares equal, so
 * every render scheduled another one — which is exactly what it did, and the
 * page rendered nothing at all until this was a constant.
 */
const NO_OUTBOX: ProfileOutbox = Object.freeze({
  inventory: Object.freeze({}) as Record<string, PendingEdit<number>>,
  roster: Object.freeze({}) as Record<string, PendingEdit<string[] | null>>,
});

export const usePlannerStore = create<PlannerState>()(
  persist(
    (set) => ({
      profileId: null,
      outbox: {},
      rejected: [],
      lastSyncedAt: null,

      selectProfile: (profileId) => set({ profileId, rejected: [] }),

      editQuantity: (profileId, item, quantity) =>
        set((state) => {
          const current = state.outbox[profileId] ?? emptyOutbox();
          return {
            outbox: {
              ...state.outbox,
              [profileId]: {
                ...current,
                inventory: {
                  ...current.inventory,
                  [item]: { value: quantity, at: new Date().toISOString() },
                },
              },
            },
          };
        }),

      editRosterState: (profileId, entity, rosterState) =>
        set((state) => {
          const current = state.outbox[profileId] ?? emptyOutbox();
          return {
            outbox: {
              ...state.outbox,
              [profileId]: {
                ...current,
                roster: {
                  ...current.roster,
                  [entity]: { value: rosterState, at: new Date().toISOString() },
                },
              },
            },
          };
        }),

      // Persisted, so it survives a reload — a lost edit the reader closed the
      // tab before seeing is exactly the one worth still telling them about.
      // Which is also why it takes an explicit dismissal rather than a timeout.
      acknowledgeRejected: () => set({ rejected: [] }),

      settle: (profileId, flushed, rejected) =>
        set((state) => {
          const current = state.outbox[profileId] ?? emptyOutbox();

          // Remove only what was actually sent, and only if it has not been
          // typed over since. A flush is not instantaneous and a player editing
          // through one would otherwise watch their last few entries vanish —
          // the bug that makes people stop trusting an offline tool.
          const keep = <T,>(
            held: Record<string, PendingEdit<T>>,
            sent: Record<string, PendingEdit<T>>,
          ): Record<string, PendingEdit<T>> =>
            Object.fromEntries(
              Object.entries(held).filter(([key, edit]) => !sent[key] || sent[key].at !== edit.at),
            );

          return {
            outbox: {
              ...state.outbox,
              [profileId]: {
                inventory: keep(current.inventory, flushed.inventory),
                roster: keep(current.roster, flushed.roster),
              },
            },
            rejected,
            lastSyncedAt: new Date().toISOString(),
          };
        }),
    }),
    {
      name: 'storm-almanac-planner',
      // Bumped when the shape changed from a flat draft to a per-profile
      // outbox. Without it a persisted v0 draft would rehydrate into fields
      // that no longer exist and the editor would render undefined counts.
      version: 1,
      migrate: (persisted, from) =>
        from < 1
          ? { profileId: null, outbox: {}, rejected: [], lastSyncedAt: null }
          : (persisted as PlannerState),
    },
  ),
);

/** The outbox for one profile, or an empty one — never undefined at a call site. */
export const outboxOf = (state: PlannerState, profileId: string | null): ProfileOutbox =>
  (profileId ? state.outbox[profileId] : undefined) ?? NO_OUTBOX;

/**
 * What to render: the server's inventory with this device's unsent edits on top.
 *
 * A player who types 40 must see 40 immediately, whether or not anything is
 * reachable. Showing the server's value until a flush confirms it would make an
 * offline editor feel broken in exactly the situation it exists for.
 */
export const effectiveInventory = (
  stored: Record<string, number>,
  pending: Record<string, PendingEdit<number>>,
): Record<string, number> => {
  const merged = { ...stored };
  for (const [item, edit] of Object.entries(pending)) {
    merged[item] = edit.value;
  }
  return merged;
};

export const effectiveRoster = (
  stored: Record<string, string[]>,
  pending: Record<string, PendingEdit<string[] | null>>,
): Record<string, string[]> => {
  const merged = { ...stored };
  for (const [entity, edit] of Object.entries(pending)) {
    if (edit.value === null) delete merged[entity];
    else merged[entity] = edit.value;
  }
  return merged;
};

export const pendingCount = (outbox: ProfileOutbox): number =>
  Object.keys(outbox.inventory).length + Object.keys(outbox.roster).length;

export type { ProfileOutbox };
