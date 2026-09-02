/**
 * <b>almanac-store</b> — an embedded log-structured merge tree. Track B, phase 7.
 *
 * <p>Not started. Phase 7 is gated behind a publicly deployed product with real
 * traffic, because an engine written for a system with no users is an engine
 * written against imagined requirements. By the time this package has code in
 * it, the real write volume, the real read pattern and the real failure modes
 * will be known, and the design will be shaped by them.
 *
 * <p>Planned shape, in build order:
 * <pre>
 * write path   append to WAL (fsync policy configurable)
 *                -&gt; insert into memtable (skip list, sorted)
 *                  -&gt; memtable full -&gt; freeze, flush to SSTable
 * SSTable      sorted blocks + sparse index + bloom filter + footer checksums
 * read path    memtable -&gt; frozen memtables -&gt; SSTables newest-first,
 *              bloom filter short-circuits the misses
 * compaction   levelled, background threads, tombstone reclamation;
 *              read amplification vs write amplification as a tuning knob
 * recovery     replay WAL from the last durable flush, detect torn writes
 * </pre>
 *
 * <p>Bounded scope, deliberately: single-node, embedded, one key ordering, no
 * transactions across keys. A finished small engine beats an unfinished
 * ambitious one.
 *
 * <p>What proves it works: crash-consistency fuzzing (kill the process at
 * random points during writes and compactions; on restart every acknowledged
 * write is present and no unacknowledged one appears), property tests against
 * an in-memory reference model, and a published benchmark against the Postgres
 * implementation of {@code DropReportStore} on the real report workload.
 *
 * @see io.stormalmanac.stats.DropReportStore
 */
package io.stormalmanac.store;
