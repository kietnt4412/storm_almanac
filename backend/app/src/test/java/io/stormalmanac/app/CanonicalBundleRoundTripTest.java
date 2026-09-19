package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;

import io.stormalmanac.gamedata.ingest.CanonicalBundleParser;
import io.stormalmanac.gamedata.ingest.CanonicalBundleWriter;
import io.stormalmanac.gamedata.ingest.GameDataBundle;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The writer against the parser, over the fixture built to hold every shape.
 *
 * <p>{@link CanonicalBundleWriter} only exists so an adapter's output can become
 * a file a person approves, and a writer that quietly omits a field is worse
 * than no writer at all: the parser reads the gap as an absent optional, the
 * bundle is accepted, and a rotating stage becomes an always-open one. Nothing
 * fails. So the two are pinned to each other here rather than tested apart.
 *
 * <p>{@code proving-ground} is the right fixture for it precisely because it was
 * built to be exhaustive — all four source kinds, both sink kinds, the catalog
 * axis, a weekday rotation, an expiring event, a banner with soft pity and a
 * floor. No database, so this stays a fast test.
 */
class CanonicalBundleRoundTripTest {

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"proving-ground-1.0.json", "proving-ground-1.1.json"})
    @DisplayName("a bundle written out and read back is the bundle that went in")
    void roundTrips(String fixture) throws IOException {
        GameDataBundle original = read(fixture);

        GameDataBundle rewritten = new CanonicalBundleParser()
                .parse(new CanonicalBundleWriter().write(original));

        assertThat(rewritten)
                .as("every field the parser can read, the writer must be able to write")
                .isEqualTo(original);
    }

    @Test
    @DisplayName("writing is stable: the second pass produces the same bytes as the first")
    void isIdempotent() throws IOException {
        // A reviewer diffs one generated bundle against the last one. If the
        // writer's output depended on iteration order anywhere, every patch diff
        // would be full of noise that means nothing.
        CanonicalBundleWriter writer = new CanonicalBundleWriter();
        String once = writer.write(read("proving-ground-1.0.json"));
        String twice = writer.write(new CanonicalBundleParser().parse(once));

        assertThat(twice).isEqualTo(once);
    }

    @Test
    @DisplayName("the authored launch bundle survives the writer too, gates and progress included")
    void theAuthoredBundleRoundTrips() throws IOException {
        // proving-ground predates gates, progress and what a fodder rule feeds,
        // and the one bundle carrying all three is the real one.
        GameDataBundle original;
        try (InputStream in = java.nio.file.Files.newInputStream(java.nio.file.Path.of(
                "..", "..", "data", "bundles", "punishing-gray-raven-steering-by-light.json"))) {
            original = new CanonicalBundleParser().parse(in);
        }

        assertThat(new CanonicalBundleParser().parse(new CanonicalBundleWriter().write(original)))
                .isEqualTo(original);
    }

    private static GameDataBundle read(String fixture) throws IOException {
        try (InputStream in = CanonicalBundleRoundTripTest.class
                .getResourceAsStream("/gamedata/" + fixture)) {
            return new CanonicalBundleParser().parse(in);
        }
    }
}
