/**
 * <b>almanac-store</b> — an embedded log-structured merge tree. Track B, phase 7.
 *
 * <p>Opened 2026-09-29 before the product had real traffic, on a synthetic
 * workload (ADR 0035): every figure measured against it names its workload as
 * generated until real drop reports exist.
 *
 * <p>Shape, in build order, and how far it has got:
 * <pre>
 * write path   append to WAL (fsync policy configurable)            built
 *                -&gt; insert into memtable (skip list, sorted)          built
 *                  -&gt; memtable full -&gt; freeze, flush to SSTable      next
 * SSTable      sorted blocks + sparse index + bloom filter + footer checksums
 * read path    memtable -&gt; frozen memtables -&gt; SSTables newest-first,
 *              bloom filter short-circuits the misses
 * compaction   levelled, background threads, tombstone reclamation;
 *              read amplification vs write amplification as a tuning knob
 * recovery     replay WAL from the last durable flush, detect torn writes   WAL half built
 * </pre>
 *
 * <p>Bounded scope, deliberately: single-node, embedded, one key ordering, no
 * transactions across keys. A finished small engine beats an unfinished
 * ambitious one. The engine knows bytes, not drop reports; the adapter that
 * encodes a {@code DropReport} into its keys lives with the port.
 *
 * <p>What proves it works: crash-consistency fuzzing (kill the process at
 * random points during writes and compactions; on restart every acknowledged
 * write is present and no unacknowledged one appears), property tests against
 * an in-memory reference model, and a published benchmark against the Postgres
 * implementation of {@code DropReportStore}.
 *
 * @see io.stormalmanac.stats.DropReportStore
 */
package io.stormalmanac.store;
