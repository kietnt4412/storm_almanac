package io.stormalmanac.common;

import io.stormalmanac.common.id.GameId;
import java.time.Instant;
import java.util.Comparator;

/**
 * A published snapshot of one game's data.
 *
 * <p>Every plan, every drop estimate and every catalog page records the version
 * it was computed against, so an old plan never silently becomes wrong when a
 * patch lands. Balance patches change combat multipliers without touching
 * material costs, so both axes are versioned together — see phase 12.
 *
 * @param game        which title this snapshot belongs to
 * @param sequence    monotonic within a game; the ordering key
 * @param label       the upstream patch label, e.g. {@code "1.9"}
 * @param publishedAt when a human approved this snapshot, not when it was fetched
 * @param attribution where these numbers came from. Required, and carried here
 *                    rather than left in the ingest half, because "numbers and
 *                    text only, attributed" is a project invariant and the
 *                    moment the numbers are served to a stranger is the moment
 *                    the attribution has to travel with them. A published
 *                    snapshot that cannot say where it came from is one nobody
 *                    can defend later
 */
public record GameDataVersion(GameId game, long sequence, String label, Instant publishedAt, String attribution) {

    public static final Comparator<GameDataVersion> NEWEST_FIRST =
            Comparator.comparingLong(GameDataVersion::sequence).reversed();

    public GameDataVersion {
        if (game == null) throw new IllegalArgumentException("game must not be null");
        if (sequence < 0) throw new IllegalArgumentException("sequence must not be negative");
        label = label == null || label.isBlank() ? String.valueOf(sequence) : label;
        if (publishedAt == null) throw new IllegalArgumentException("publishedAt must not be null");
        if (attribution == null || attribution.isBlank()) {
            throw new IllegalArgumentException("attribution must not be blank");
        }
    }

    public boolean isNewerThan(GameDataVersion other) {
        if (!game.equals(other.game)) {
            throw new IllegalArgumentException("cannot order versions across games");
        }
        return sequence > other.sequence;
    }
}
