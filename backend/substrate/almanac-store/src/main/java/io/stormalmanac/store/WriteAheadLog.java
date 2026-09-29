package io.stormalmanac.store;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.CRC32C;

/**
 * The log every write reaches before the memtable does, and the only thing
 * recovery trusts.
 *
 * <pre>
 * file    := magic:u64  record*
 * record  := length:u32  crc32c(body):u32  body
 * body    := kind:u8  keyLength:u32  key  [ valueLength:u32  value ]   -- value only for a put
 * </pre>
 *
 * <p><b>A torn tail is expected, not an error.</b> A crash can stop a write at
 * any byte, so replay reads records until one is short or fails its checksum,
 * treats everything from there on as the write that never finished, and
 * truncates the file back to the last whole record before anything is appended
 * after it — otherwise a later record would sit behind garbage and be lost on
 * the next replay. <b>The cost of that rule:</b> a bit flipped by the disk in
 * the middle of the log is indistinguishable from a torn tail, and everything
 * after it is dropped too. Phase 7 promises crash consistency, not survival of
 * a failing disk, and says so here rather than implying otherwise.
 */
final class WriteAheadLog implements AutoCloseable {

    /** "ALMWAL01" — a file that does not start with it is not ours, and is refused. */
    static final long MAGIC = 0x414C4D57414C3031L;
    static final int HEADER_BYTES = Long.BYTES;
    static final int RECORD_HEADER_BYTES = 2 * Integer.BYTES;

    static final byte KIND_PUT = 1;
    static final byte KIND_DELETE = 2;

    /** What replay found. {@code discardedBytes} is the torn tail it cut off. */
    record Replay(int records, long validBytes, long discardedBytes) {}

    @FunctionalInterface
    interface Visitor {
        void accept(byte[] key, Cell cell);
    }

    private final Path path;
    private final FileChannel channel;
    private final Durability durability;
    private final Replay replayed;
    private long size;

    private WriteAheadLog(Path path, FileChannel channel, Durability durability, Replay replayed) {
        this.path = path;
        this.channel = channel;
        this.durability = durability;
        this.replayed = replayed;
        this.size = replayed.validBytes();
    }

    /**
     * Opens the log at {@code path}, replaying every whole record into
     * {@code visitor} and cutting off a torn tail, or creates it empty.
     */
    static WriteAheadLog openAndReplay(Path path, Durability durability, Visitor visitor)
            throws IOException {
        boolean existed = Files.exists(path);
        byte[] contents = existed ? Files.readAllBytes(path) : new byte[0];
        FileChannel channel = FileChannel.open(path,
                StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
        try {
            Replay replay;
            if (contents.length < HEADER_BYTES) {
                // Never created, or the crash came before its header was whole: nothing
                // was ever acknowledged from it, so start it again.
                channel.truncate(0);
                channel.write(ByteBuffer.allocate(HEADER_BYTES).putLong(0, MAGIC), 0);
                channel.force(true);
                Fsync.directory(path.toAbsolutePath().getParent());
                replay = new Replay(0, HEADER_BYTES, contents.length);
            } else {
                replay = replay(path, ByteBuffer.wrap(contents), visitor);
                if (replay.discardedBytes() > 0) {
                    channel.truncate(replay.validBytes());
                    channel.force(true);
                }
            }
            channel.position(replay.validBytes());
            return new WriteAheadLog(path, channel, durability, replay);
        } catch (IOException | RuntimeException e) {
            channel.close();
            throw e;
        }
    }

    private static Replay replay(Path path, ByteBuffer file, Visitor visitor) throws IOException {
        long magic = file.getLong(0);
        if (magic != MAGIC) {
            throw new IOException(path + " is not a write-ahead log (magic " + Long.toHexString(magic) + ")");
        }
        int position = HEADER_BYTES;
        int records = 0;
        CRC32C crc = new CRC32C();
        while (true) {
            int remaining = file.limit() - position;
            if (remaining < RECORD_HEADER_BYTES) break;
            long length = Integer.toUnsignedLong(file.getInt(position));
            int expectedCrc = file.getInt(position + Integer.BYTES);
            if (length > remaining - RECORD_HEADER_BYTES) break;
            int bodyStart = position + RECORD_HEADER_BYTES;
            crc.reset();
            crc.update(file.slice(bodyStart, (int) length));
            if ((int) crc.getValue() != expectedCrc) break;
            if (!decode(file.slice(bodyStart, (int) length), visitor)) break;
            position = bodyStart + (int) length;
            records++;
        }
        return new Replay(records, position, file.limit() - position);
    }

    /** False when a checksummed body still does not parse, which is treated as torn. */
    private static boolean decode(ByteBuffer body, Visitor visitor) {
        if (body.remaining() < 1 + Integer.BYTES) return false;
        byte kind = body.get();
        int keyLength = body.getInt();
        if (keyLength < 0 || keyLength > body.remaining()) return false;
        byte[] key = new byte[keyLength];
        body.get(key);
        switch (kind) {
            case KIND_PUT -> {
                if (body.remaining() < Integer.BYTES) return false;
                int valueLength = body.getInt();
                if (valueLength < 0 || valueLength != body.remaining()) return false;
                byte[] value = new byte[valueLength];
                body.get(value);
                visitor.accept(key, new Cell.Put(value));
            }
            case KIND_DELETE -> {
                if (body.hasRemaining()) return false;
                visitor.accept(key, Cell.tombstone());
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    /** Appends one record; durable on return under {@link Durability#SYNC_EACH_WRITE}. */
    void append(byte[] key, Cell cell) throws IOException {
        ByteBuffer record = encode(key, cell);
        while (record.hasRemaining()) {
            channel.write(record);
        }
        size = channel.position();
        if (durability == Durability.SYNC_EACH_WRITE) {
            channel.force(false);
        }
    }

    static ByteBuffer encode(byte[] key, Cell cell) {
        int bodyLength = 1 + Integer.BYTES + key.length;
        byte[] value = cell instanceof Cell.Put put ? put.value() : null;
        if (value != null) bodyLength += Integer.BYTES + value.length;
        ByteBuffer record = ByteBuffer.allocate(RECORD_HEADER_BYTES + bodyLength);
        record.position(RECORD_HEADER_BYTES);
        record.put(value != null ? KIND_PUT : KIND_DELETE);
        record.putInt(key.length).put(key);
        if (value != null) record.putInt(value.length).put(value);
        CRC32C crc = new CRC32C();
        crc.update(record.array(), RECORD_HEADER_BYTES, bodyLength);
        record.putInt(0, bodyLength);
        record.putInt(Integer.BYTES, (int) crc.getValue());
        return record.flip();
    }

    /** What opening this log found on disk. */
    Replay replayed() {
        return replayed;
    }

    long size() {
        return size;
    }

    Path path() {
        return path;
    }

    @Override
    public void close() throws IOException {
        if (channel.isOpen()) {
            channel.force(false);
            channel.close();
        }
    }
}
