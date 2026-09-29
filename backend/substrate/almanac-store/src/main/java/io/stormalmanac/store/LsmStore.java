package io.stormalmanac.store;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * An embedded, single-node, byte-keyed log-structured merge tree.
 *
 * <p>Keys order as unsigned bytes. A write is appended to the
 * {@linkplain WriteAheadLog log} and then applied to the {@linkplain MemTable
 * memtable}; it is acknowledged when {@code put} or {@code delete} returns, and
 * under {@link Durability#SYNC_EACH_WRITE} an acknowledged write survives a
 * crash at any instant. Opening a directory replays its log.
 *
 * <p><b>What this slice is.</b> The log, the memtable and recovery. The
 * memtable does not yet flush: SSTables, the merged read path and compaction are
 * the next slices of Phase 7, in the order {@code plan.html} gives.
 *
 * <p>One process owns a directory at a time, enforced by a lock file. Writers
 * are serialised; readers take no lock. <b>A write that fails leaves the store
 * refusing every later write</b>, because after a failed append the log's tail
 * is unknown and appending behind it could strand an acknowledged record behind
 * garbage. Reopening the directory is the recovery.
 */
public final class LsmStore implements AutoCloseable {

    static final String LOCK_FILE = "LOCK";
    static final String LOG_FILE = "wal-000001.log";

    private final Path directory;
    private final StoreOptions options;
    private final FileChannel lockChannel;
    private final FileLock lock;
    private final WriteAheadLog log;
    private final MemTable memTable = new MemTable();
    private final Object writeLock = new Object();

    private volatile IOException failure;
    private volatile boolean closed;

    private LsmStore(Path directory, StoreOptions options, FileChannel lockChannel, FileLock lock)
            throws IOException {
        this.directory = directory;
        this.options = options;
        this.lockChannel = lockChannel;
        this.lock = lock;
        this.log = WriteAheadLog.openAndReplay(directory.resolve(LOG_FILE), options.durability(), memTable::apply);
    }

    /** Opens, or creates, the store in {@code directory}, recovering whatever its log holds. */
    public static LsmStore open(Path directory, StoreOptions options) {
        try {
            Files.createDirectories(directory);
            FileChannel lockChannel = FileChannel.open(directory.resolve(LOCK_FILE),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            FileLock lock;
            try {
                lock = lockChannel.tryLock();
            } catch (IOException | RuntimeException e) {
                lockChannel.close();
                throw e;
            }
            if (lock == null) {
                lockChannel.close();
                throw new IllegalStateException(directory + " is already open in another process");
            }
            try {
                return new LsmStore(directory, options, lockChannel, lock);
            } catch (IOException | RuntimeException e) {
                lock.release();
                lockChannel.close();
                throw e;
            }
        } catch (java.nio.channels.OverlappingFileLockException e) {
            throw new IllegalStateException(directory + " is already open in this process", e);
        } catch (IOException e) {
            throw new UncheckedIOException("opening " + directory, e);
        }
    }

    public static LsmStore open(Path directory) {
        return open(directory, StoreOptions.defaults());
    }

    public void put(byte[] key, byte[] value) {
        requireKey(key);
        if (value == null) throw new IllegalArgumentException("value is required; delete a key to remove it");
        write(key.clone(), new Cell.Put(value.clone()));
    }

    public void delete(byte[] key) {
        requireKey(key);
        write(key.clone(), Cell.tombstone());
    }

    private void write(byte[] key, Cell cell) {
        synchronized (writeLock) {
            requireWritable();
            try {
                log.append(key, cell);
            } catch (IOException e) {
                failure = e;
                throw new UncheckedIOException("write failed; the store refuses further writes until reopened", e);
            }
            memTable.apply(key, cell);
        }
    }

    public Optional<byte[]> get(byte[] key) {
        requireKey(key);
        requireOpen();
        return memTable.get(key) instanceof Cell.Put put ? Optional.of(put.value().clone()) : Optional.empty();
    }

    /**
     * Live entries with {@code fromInclusive <= key < toExclusive}, in key order;
     * a {@code null} bound is open. <b>Weakly consistent</b>: a write that lands
     * while the scan runs may or may not be seen, but no entry is seen twice.
     */
    public Iterator<KeyValue> scan(byte[] fromInclusive, byte[] toExclusive) {
        requireOpen();
        Iterator<Map.Entry<byte[], Cell>> cells = memTable.range(
                fromInclusive == null ? null : fromInclusive.clone(),
                toExclusive == null ? null : toExclusive.clone());
        return new LiveEntries(cells);
    }

    /** Bytes the memtable is holding, approximately; the flush trigger in the next slice. */
    public long memTableBytes() {
        return memTable.approximateBytes();
    }

    Path directory() {
        return directory;
    }

    StoreOptions options() {
        return options;
    }

    WriteAheadLog.Replay recovered() {
        return log.replayed();
    }

    @Override
    public void close() {
        synchronized (writeLock) {
            if (closed) return;
            closed = true;
            IOException first = null;
            try {
                log.close();
            } catch (IOException e) {
                first = e;
            }
            try {
                lock.release();
                lockChannel.close();
            } catch (IOException e) {
                if (first == null) first = e;
                else first.addSuppressed(e);
            }
            if (first != null) throw new UncheckedIOException("closing " + directory, first);
        }
    }

    private void requireWritable() {
        requireOpen();
        if (failure != null) {
            throw new IllegalStateException("an earlier write failed; reopen the store to recover", failure);
        }
    }

    private void requireOpen() {
        if (closed) throw new IllegalStateException("the store is closed");
    }

    private static void requireKey(byte[] key) {
        if (key == null) throw new IllegalArgumentException("key is required");
    }

    /** Skips tombstones and hands out copies, so no caller can reach the store's arrays. */
    private static final class LiveEntries implements Iterator<KeyValue> {
        private final Iterator<Map.Entry<byte[], Cell>> cells;
        private KeyValue next;

        LiveEntries(Iterator<Map.Entry<byte[], Cell>> cells) {
            this.cells = cells;
        }

        @Override
        public boolean hasNext() {
            while (next == null && cells.hasNext()) {
                Map.Entry<byte[], Cell> entry = cells.next();
                if (entry.getValue() instanceof Cell.Put put) {
                    next = new KeyValue(entry.getKey().clone(), put.value().clone());
                }
            }
            return next != null;
        }

        @Override
        public KeyValue next() {
            if (!hasNext()) throw new NoSuchElementException();
            KeyValue result = next;
            next = null;
            return result;
        }
    }
}
