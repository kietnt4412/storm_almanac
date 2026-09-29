package io.stormalmanac.store;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

/**
 * Making a directory entry durable, which {@code fsync} on the file alone does not.
 *
 * <p>On Linux a newly created or renamed file can vanish in a crash even after
 * its own contents were synced, unless the directory holding it is synced too.
 * Windows cannot open a directory as a channel and journals its metadata
 * itself, so there the call is skipped rather than failed — and the crash
 * fuzzing that proves the rest is meant to run on the Linux runner.
 */
final class Fsync {

    private static final boolean WINDOWS =
            System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows");

    private Fsync() {}

    static void directory(Path directory) throws IOException {
        if (WINDOWS) return;
        try (FileChannel channel = FileChannel.open(directory, StandardOpenOption.READ)) {
            channel.force(true);
        }
    }
}
