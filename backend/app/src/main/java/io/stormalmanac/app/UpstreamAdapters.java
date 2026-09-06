package io.stormalmanac.app;

import io.stormalmanac.adapters.r1999.KornblumeAdapter;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.gamedata.ingest.UpstreamAdapter;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Every parser adapter this build can run, and the only place in the product
 * that names one.
 *
 * <p>This class is the whole surface area of "which games do we support", and
 * that is deliberate: {@link ModuleBoundaryTest} lets {@code :app} reach into
 * the adapters layer and lets nothing else, so a game-specific type cannot leak
 * further in than this file without the build failing. Adding a title is one
 * entry here, one Gradle module, and nothing more — which is the claim the
 * project is built to make.
 *
 * <p>Not a Spring component. An adapter is a pure conversion from files to a
 * record; it needs no context, and keeping it out of the container means the
 * list is readable in one glance rather than assembled at runtime.
 */
final class UpstreamAdapters {

    private UpstreamAdapters() {
    }

    /** Every adapter, with their notes discarded — for listing, not for running. */
    static List<UpstreamAdapter> all() {
        return all(note -> { });
    }

    static List<UpstreamAdapter> all(Consumer<String> notes) {
        return List.of(new KornblumeAdapter(notes));
    }

    static Optional<UpstreamAdapter> forGame(GameId game, Consumer<String> notes) {
        return all(notes).stream().filter(adapter -> adapter.game().equals(game)).findFirst();
    }
}
