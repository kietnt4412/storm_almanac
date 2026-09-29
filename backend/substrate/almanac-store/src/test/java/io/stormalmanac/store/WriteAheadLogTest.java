package io.stormalmanac.store;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WriteAheadLogTest {

    @TempDir
    Path dir;

    /** A record as replay reported it; a delete has a null value. */
    record Seen(String key, String value) {}

    private static List<Seen> replay(Path path) throws IOException {
        List<Seen> seen = new ArrayList<>();
        WriteAheadLog.openAndReplay(path, Durability.SYNC_EACH_WRITE, (key, cell) ->
                seen.add(new Seen(new String(key, UTF_8),
                        cell instanceof Cell.Put put ? new String(put.value(), UTF_8) : null))).close();
        return seen;
    }

    private static void write(Path path, List<Seen> records) throws IOException {
        try (var log = WriteAheadLog.openAndReplay(path, Durability.SYNC_EACH_WRITE, (k, c) -> {})) {
            for (Seen record : records) {
                log.append(record.key().getBytes(UTF_8), record.value() == null
                        ? Cell.tombstone()
                        : new Cell.Put(record.value().getBytes(UTF_8)));
            }
        }
    }

    private static final List<Seen> RECORDS = List.of(
            new Seen("stage-1|item-a", "3 of 10"),
            new Seen("stage-1|item-b", ""),
            new Seen("stage-1|item-a", null),
            new Seen("stage-2|item-c", "a longer value than the others, to vary the record size"),
            new Seen("", "an empty key is a key"));

    @Test
    @DisplayName("A reopened log replays every record, deletes included, in the order written")
    void replaysInOrder() throws IOException {
        Path path = dir.resolve("wal.log");
        write(path, RECORDS);

        assertThat(replay(path)).containsExactlyElementsOf(RECORDS);
    }

    @Test
    @DisplayName("A log cut at any byte replays exactly the whole records before the cut, and appends after them")
    void everyTornTailIsAPrefix() throws IOException {
        Path whole = dir.resolve("whole.log");
        write(whole, RECORDS);
        byte[] bytes = Files.readAllBytes(whole);

        // Where each record ends, so the expected prefix for a cut is known exactly.
        List<Integer> ends = new ArrayList<>();
        int position = WriteAheadLog.HEADER_BYTES;
        for (Seen record : RECORDS) {
            position += WriteAheadLog.encode(record.key().getBytes(UTF_8), record.value() == null
                    ? Cell.tombstone() : new Cell.Put(record.value().getBytes(UTF_8))).remaining();
            ends.add(position);
        }
        assertThat(position).isEqualTo(bytes.length);

        for (int cut = 0; cut <= bytes.length; cut++) {
            Path torn = dir.resolve("torn-" + cut + ".log");
            Files.write(torn, Arrays.copyOf(bytes, cut));
            final int at = cut;
            int wholeRecords = (int) ends.stream().filter(end -> end <= at).count();

            assertThat(replay(torn)).as("cut at byte %d", cut)
                    .containsExactlyElementsOf(RECORDS.subList(0, wholeRecords));

            // The torn tail must be gone before anything is appended, or the new
            // record would sit behind garbage and vanish on the next replay.
            write(torn, List.of(new Seen("after", "the crash")));
            List<Seen> expected = new ArrayList<>(RECORDS.subList(0, wholeRecords));
            expected.add(new Seen("after", "the crash"));
            assertThat(replay(torn)).as("append after a cut at byte %d", cut).containsExactlyElementsOf(expected);
        }
    }

    @Test
    @DisplayName("A flipped bit in the last record fails its checksum and drops that record alone")
    void checksumCatchesACorruptTail() throws IOException {
        Path path = dir.resolve("wal.log");
        write(path, RECORDS);
        byte[] bytes = Files.readAllBytes(path);
        bytes[bytes.length - 3] ^= 0x10;
        Files.write(path, bytes);

        assertThat(replay(path)).containsExactlyElementsOf(RECORDS.subList(0, RECORDS.size() - 1));
    }

    @Test
    @DisplayName("A file that does not start with the log's magic is refused, not truncated")
    void refusesAForeignFile() throws IOException {
        Path path = dir.resolve("wal.log");
        byte[] foreign = "not a write-ahead log at all".getBytes(UTF_8);
        Files.write(path, foreign);

        assertThatThrownBy(() -> replay(path)).isInstanceOf(IOException.class).hasMessageContaining("not a write-ahead log");
        assertThat(Files.readAllBytes(path)).isEqualTo(foreign);
    }
}
