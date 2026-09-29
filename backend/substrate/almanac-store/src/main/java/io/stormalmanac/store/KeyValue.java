package io.stormalmanac.store;

/**
 * One live entry as a scan returns it. The arrays are the caller's copies.
 */
public record KeyValue(byte[] key, byte[] value) {}
