package io.stormalmanac.store;

/**
 * What the tree holds for one key: a value, or the record that it was deleted.
 *
 * <p>A delete cannot simply remove the key, because an older value for it may
 * sit in a layer the delete never touches — an SSTable written before it. The
 * tombstone shadows that value on every read until compaction drops both.
 */
sealed interface Cell {

    /** A live value. The array is owned by the store and never handed out. */
    record Put(byte[] value) implements Cell {}

    /** The key was deleted; shadows any older value below it. */
    record Tombstone() implements Cell {
        static final Tombstone INSTANCE = new Tombstone();
    }

    static Cell tombstone() {
        return Tombstone.INSTANCE;
    }
}
