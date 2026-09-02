import { create } from 'zustand';
import { persist } from 'zustand/middleware';

/**
 * Planner-local state. Persisted to localStorage so that an inventory edited on
 * a train survives the tab being killed, and so that a solve can be requested
 * the moment the connection comes back.
 */
interface PlannerState {
  profileId: string | null;
  /** Item id to quantity. Flat by design: mergeable per key, patchable offline. */
  inventoryDraft: Record<string, number>;
  dirtyKeys: string[];

  selectProfile: (profileId: string) => void;
  setQuantity: (itemId: string, quantity: number) => void;
  clearDirty: () => void;
}

export const usePlannerStore = create<PlannerState>()(
  persist(
    (set) => ({
      profileId: null,
      inventoryDraft: {},
      dirtyKeys: [],

      selectProfile: (profileId) => set({ profileId, inventoryDraft: {}, dirtyKeys: [] }),

      setQuantity: (itemId, quantity) =>
        set((state) => ({
          inventoryDraft: { ...state.inventoryDraft, [itemId]: quantity },
          dirtyKeys: state.dirtyKeys.includes(itemId)
            ? state.dirtyKeys
            : [...state.dirtyKeys, itemId],
        })),

      clearDirty: () => set({ dirtyKeys: [] }),
    }),
    { name: 'storm-almanac-planner' },
  ),
);
