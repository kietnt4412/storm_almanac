package io.stormalmanac.store;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The sorted in-memory layer every write lands in first.
 *
 * <p><b>The skip list is the JDK's</b>, deliberately. {@link ConcurrentSkipListMap}
 * is a lock-free skip list, which is exactly the structure the design calls for;
 * a hand-built one would be a data-structures exercise the crash fuzzing cannot
 * see, while the parts that decide whether an acknowledged write survives — the
 * log, the table format, the flush and compaction ordering — are all hand-built.
 *
 * <p>Keys order as unsigned bytes, lexicographically, which is the order every
 * layer below it uses too. Writers are serialised by the store; readers need no
 * lock.
 */
final class MemTable {

    /** Per-entry bookkeeping charged on top of key and value bytes. */
    static final int ENTRY_OVERHEAD = 32;

    private final ConcurrentSkipListMap<byte[], Cell> cells =
            new ConcurrentSkipListMap<>(Arrays::compareUnsigned);
    private final AtomicLong approximateBytes = new AtomicLong();

    void apply(byte[] key, Cell cell) {
        Cell previous = cells.put(key, cell);
        long delta = size(key, cell);
        if (previous != null) delta -= size(key, previous);
        approximateBytes.addAndGet(delta);
    }

    /** {@code null} when this layer knows nothing about the key. */
    Cell get(byte[] key) {
        return cells.get(key);
    }

    /** Cells in key order, tombstones included; {@code null} bounds are open. */
    Iterator<Map.Entry<byte[], Cell>> range(byte[] fromInclusive, byte[] toExclusive) {
        NavigableMap<byte[], Cell> view = cells;
        if (fromInclusive != null) view = view.tailMap(fromInclusive, true);
        if (toExclusive != null) view = view.headMap(toExclusive, false);
        return view.entrySet().iterator();
    }

    long approximateBytes() {
        return approximateBytes.get();
    }

    int size() {
        return cells.size();
    }

    boolean isEmpty() {
        return cells.isEmpty();
    }

    private static long size(byte[] key, Cell cell) {
        int value = cell instanceof Cell.Put put ? put.value().length : 0;
        return (long) key.length + value + ENTRY_OVERHEAD;
    }
}
