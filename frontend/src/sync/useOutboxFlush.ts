import { useCallback, useEffect, useRef, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { ApiError, patchInventory, patchRoster } from '../api/client';
import { outboxOf, pendingCount, usePlannerStore, type ProfileOutbox } from '../store/plannerStore';

/**
 * Drains the outbox whenever there is something to send and a network to send it
 * on.
 *
 * <p>This is the client half of the merge the server has had since N23. Until
 * now every scenario the merge was tested against was one request following
 * another inside one JVM: the SQL was shaped for two devices and no second
 * device existed. What makes this the other half is not that it retries — it is
 * that an edit keeps the time it was made, so the server's tiebreak has real
 * clocks on both sides of it.
 *
 * <p><b>PATCH and never PUT.</b> A PUT would send this device's whole inventory,
 * which for a phone that has been offline means overwriting every key another
 * device touched in the meantime. That is the exact failure the per-key merge
 * exists to prevent, and it would be reintroduced here, in the client, where no
 * server test would ever see it.
 *
 * <p><b>Rejections are shown, not swallowed.</b> The response names the keys
 * whose edits lost. A client that ignores them keeps displaying a number the
 * server does not hold, which is worse than losing the edit.
 */
export interface FlushState {
  pending: number;
  online: boolean;
  flushing: boolean;
  rejected: string[];
  lastSyncedAt: string | null;
  error: string | null;
  flushNow: () => void;
  /** Stop showing which keys lost, once the reader has seen it. */
  acknowledge: () => void;
}

export function useOutboxFlush(profileId: string | null): FlushState {
  const outbox = usePlannerStore((state) => outboxOf(state, profileId));
  const settle = usePlannerStore((state) => state.settle);
  const rejected = usePlannerStore((state) => state.rejected);
  const acknowledge = usePlannerStore((state) => state.acknowledgeRejected);
  const lastSyncedAt = usePlannerStore((state) => state.lastSyncedAt);
  const queries = useQueryClient();

  const [online, setOnline] = useState(() => navigator.onLine);
  const [flushing, setFlushing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // A flush in flight must not be started twice — by the interval and the
  // online event arriving together, say. A ref rather than the state above
  // because the guard has to be read synchronously by the caller that sets it.
  const inFlight = useRef(false);

  const pending = pendingCount(outbox);

  const flush = useCallback(async () => {
    if (!profileId || inFlight.current) return;

    // Snapshot what is being sent. Anything typed while the request is in the
    // air belongs to the next flush, and `settle` compares timestamps so a key
    // re-edited mid-flight survives.
    const sending: ProfileOutbox = {
      inventory: { ...outbox.inventory },
      roster: { ...outbox.roster },
    };
    if (pendingCount(sending) === 0) return;

    inFlight.current = true;
    setFlushing(true);
    setError(null);
    try {
      const refused: string[] = [];

      if (Object.keys(sending.inventory).length > 0) {
        const response = await patchInventory(
          profileId,
          Object.fromEntries(
            Object.entries(sending.inventory).map(([item, edit]) => [
              item,
              { quantity: edit.value, at: edit.at },
            ]),
          ),
        );
        refused.push(...response.rejected);
      }

      if (Object.keys(sending.roster).length > 0) {
        const response = await patchRoster(
          profileId,
          Object.fromEntries(
            Object.entries(sending.roster).map(([entity, edit]) => [
              entity,
              { states: edit.value, at: edit.at },
            ]),
          ),
        );
        refused.push(...response.rejected);
      }

      settle(profileId, sending, refused);
      // The server's copy has moved, and so has anything computed from it.
      queries.invalidateQueries({ queryKey: ['inventory', profileId] });
      queries.invalidateQueries({ queryKey: ['roster', profileId] });
      queries.invalidateQueries({ queryKey: ['shortfall', profileId] });
    } catch (failure) {
      // The edits stay in the outbox. A failed flush is the normal state of an
      // offline device, not an error the player has to do anything about —
      // except when the session has gone, which they do have to know about.
      setError(
        failure instanceof ApiError && failure.isSignedOut
          ? 'Signed out — your edits are saved on this device and will sync when you sign in again.'
          : failure instanceof Error
            ? failure.message
            : 'could not reach the server',
      );
    } finally {
      inFlight.current = false;
      setFlushing(false);
    }
  }, [outbox, profileId, queries, settle]);

  useEffect(() => {
    const wentOnline = () => setOnline(true);
    const wentOffline = () => setOnline(false);
    window.addEventListener('online', wentOnline);
    window.addEventListener('offline', wentOffline);
    return () => {
      window.removeEventListener('online', wentOnline);
      window.removeEventListener('offline', wentOffline);
    };
  }, []);

  useEffect(() => {
    if (!online || pending === 0) return;

    // Debounced rather than per-keystroke: bulk entry is a burst of edits and
    // one PATCH carrying forty keys is the point of a per-key merge.
    const soon = window.setTimeout(() => void flush(), 1200);
    return () => window.clearTimeout(soon);
  }, [flush, online, pending]);

  return {
    pending,
    online,
    flushing,
    rejected,
    lastSyncedAt,
    error,
    flushNow: () => void flush(),
    acknowledge,
  };
}
