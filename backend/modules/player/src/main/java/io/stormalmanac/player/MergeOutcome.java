package io.stormalmanac.player;

import java.util.List;

/**
 * Which keys of a patch were taken and which were refused for being older than
 * what was already stored.
 *
 * <p><b>A rejection is reported, not swallowed.</b> A merge that silently drops
 * the losing half leaves the client showing a value the server does not hold,
 * and the player edits from a screen that is quietly wrong until something else
 * makes them reload. Handing back the keys that lost lets a client do the only
 * correct thing, which is to take the server's value for those and keep its own
 * for the rest.
 *
 * @param <K> the aggregate's key type — {@code ItemId} for an inventory,
 *            {@code EntityId} for a roster. Generic rather than {@code String}
 *            because a bare slug is what these identifiers exist to stop being
 */
public record MergeOutcome<K>(List<K> applied, List<K> rejected) {

    public MergeOutcome {
        applied = List.copyOf(applied);
        rejected = List.copyOf(rejected);
    }

    public static <K> MergeOutcome<K> nothing() {
        return new MergeOutcome<>(List.of(), List.of());
    }
}
