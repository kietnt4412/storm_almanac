package io.stormalmanac.store;

/**
 * When a write counts as acknowledged.
 *
 * <p>The crash-consistency guarantee — every acknowledged write survives a
 * crash — is only a promise under {@link #SYNC_EACH_WRITE}. Under
 * {@link #OS_BUFFERED} a write survives the process dying but not the machine,
 * and a benchmark run in that mode must say so.
 */
public enum Durability {

    /** {@code fsync} the log before {@code put} or {@code delete} returns. */
    SYNC_EACH_WRITE,

    /** Hand the log to the OS and return; survives a killed process, not a lost machine. */
    OS_BUFFERED
}
