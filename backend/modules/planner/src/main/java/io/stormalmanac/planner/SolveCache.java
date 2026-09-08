package io.stormalmanac.planner;

import java.util.Optional;

/**
 * Plans, stored under the fingerprint of the question that produced them.
 *
 * <p>The third seam of the two-track structure, and the narrowest: a get and a
 * put over {@link SolveKey}. It is narrow on purpose. A cache that also knew how
 * to invalidate, or to scan, or to expire on a policy, would be a cache shaped
 * to flatter whichever implementation happened to offer those things, and
 * comparing it against the phase-8 replicated KV would prove nothing.
 *
 * <p><b>There is no invalidation method and that is the design.</b> A key
 * carries the game-data version, so a patch does not stale a plan — it makes a
 * different key, under which nothing is stored yet. The old entry is not
 * refreshed, it is unreachable. Eviction is therefore a memory-pressure concern
 * and never a correctness one.
 *
 * <p><b>With one exception, which is a debt and not a subtlety.</b> Drop
 * estimates are not in the key, because nothing publishes any yet. The moment
 * phase 6 does, they go into {@link SolveKey} in the same change, or this class
 * starts serving plans computed against yesterday's rates and calling them
 * today's. That is the one bug the key's design cannot catch on its own.
 *
 * @see SolveKey
 */
public interface SolveCache {

    /** Empty on a miss. Never blocks on a solve; the caller does that work. */
    Optional<Plan> get(String key);

    /**
     * Stores a plan against its key. Overwriting an existing entry under the
     * same key is a no-op in meaning — the key determines the answer — so
     * implementations need not check.
     */
    void put(String key, Plan plan);

    /** A cache that remembers nothing, for callers that do not want one. */
    static SolveCache none() {
        return new SolveCache() {
            @Override
            public Optional<Plan> get(String key) {
                return Optional.empty();
            }

            @Override
            public void put(String key, Plan plan) {}
        };
    }
}
