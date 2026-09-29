package io.stormalmanac.store;

/**
 * The knobs, each chosen against a synthetic workload until real reports exist
 * (ADR 0035) — so every one of them is a guess that says it is one.
 *
 * @param durability      when a write counts as acknowledged
 * @param memTableBytes   approximate size at which the memtable is frozen and flushed
 */
public record StoreOptions(Durability durability, long memTableBytes) {

    public static final long DEFAULT_MEMTABLE_BYTES = 4L * 1024 * 1024;

    public StoreOptions {
        if (durability == null) throw new IllegalArgumentException("durability is required");
        if (memTableBytes < 1) throw new IllegalArgumentException("memTableBytes must be >= 1");
    }

    public static StoreOptions defaults() {
        return new StoreOptions(Durability.SYNC_EACH_WRITE, DEFAULT_MEMTABLE_BYTES);
    }

    public StoreOptions withDurability(Durability durability) {
        return new StoreOptions(durability, memTableBytes);
    }

    public StoreOptions withMemTableBytes(long memTableBytes) {
        return new StoreOptions(durability, memTableBytes);
    }
}
