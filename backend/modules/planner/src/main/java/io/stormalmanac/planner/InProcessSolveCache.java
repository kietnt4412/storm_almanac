package io.stormalmanac.planner;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The boring implementation: a bounded LRU map inside the JVM that solved.
 *
 * <p><b>Why this and not Redis, which is what the plan says.</b> Redis buys one
 * thing — a cache shared between nodes — and there is exactly one node, and
 * under {@code D1} there is not even that: nothing is deployed. What it costs is
 * a serialisation contract for {@link Plan} that has to be versioned forever
 * after, because a plan written by one deploy is read by the next. An in-process
 * map has no such contract and is empty after a restart, which is the correct
 * behaviour for a cache whose contents are cheap to recompute. See
 * {@code docs/adr/0012-the-solve-cache-is-in-process-until-there-is-a-second-node.md}
 * for the reversal trigger, which is the second node and not a date.
 *
 * <p>Eviction is least-recently-used over an entry count rather than a byte
 * budget. A plan's size is dominated by its stage runs and its shadow prices,
 * both bounded by the size of a patch, so entries are within an order of
 * magnitude of each other and counting them is honest enough. Sizing by bytes
 * would need a measurement nobody has taken.
 *
 * <p>Thread-safe by a synchronized wrapper rather than by
 * {@code ConcurrentHashMap}: LRU ordering is a property of the map's iteration
 * order, which is precisely what the concurrent map declines to maintain. The
 * critical section is a map lookup, held while no solve is running — the
 * expensive work happens outside it, in the caller.
 */
public final class InProcessSolveCache implements SolveCache {

    /**
     * Four hundred plans. At a few kilobytes each this is single-digit
     * megabytes, and it is far more than one node serves distinct questions in
     * the window where a repeat is likely. The number is a guess and is labelled
     * as one: nothing has ever called this under load, so tuning it now would be
     * fitting a curve to no data.
     */
    public static final int DEFAULT_CAPACITY = 400;

    private final Map<String, Plan> entries;
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();

    public InProcessSolveCache() {
        this(DEFAULT_CAPACITY);
    }

    public InProcessSolveCache(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be positive, was " + capacity);
        }
        this.entries = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Plan> eldest) {
                return size() > capacity;
            }
        });
    }

    @Override
    public Optional<Plan> get(String key) {
        Plan plan = entries.get(key);
        (plan == null ? misses : hits).incrementAndGet();
        return Optional.ofNullable(plan);
    }

    @Override
    public void put(String key, Plan plan) {
        entries.put(key, plan);
    }

    /** How many plans are held. For tests and for whatever reports on it later. */
    public int size() {
        return entries.size();
    }

    public long hitCount() {
        return hits.get();
    }

    public long missCount() {
        return misses.get();
    }
}
