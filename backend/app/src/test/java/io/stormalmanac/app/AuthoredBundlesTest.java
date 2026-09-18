package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;

import io.stormalmanac.gamedata.ingest.CanonicalBundleParser;
import io.stormalmanac.gamedata.ingest.GameDataBundle;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The bundles this project authored itself, checked against the two things that
 * make them worth having.
 *
 * <p>{@code data/bundles} is not test data. It is the product's game data, typed
 * by hand off a game screen because
 * {@code docs/adr/0015-game-data-is-sourced-first-hand-not-adapted.md} says a
 * fact enters a published bundle only that way. Nothing else in the build reads
 * it — the CLI is pointed at a file by a person — so without this test the
 * format could drift out from under the one artifact that proves ADR 0015 and
 * ADR 0016 work end to end, and nobody would find out until the next publish.
 *
 * <p>Two assertions, and the second is the one with teeth. That a bundle parses
 * is ordinary. That every fact in it is <em>ours to publish</em> is the claim the
 * provenance machinery exists to make falsifiable: a bundle that quietly grew a
 * {@code THIRD_PARTY} fact would still parse, still ingest, and be refused only
 * at the moment somebody tried to approve it.
 *
 * <p>The empty directory is failed rather than passed. A scan with nothing to
 * scan is the failure mode this repository has already been bitten by once —
 * see the denylist that passed vacuously in {@code EntityKindBoundaryTest}'s
 * history — and "no authored bundles" is a fact worth a red build, not a green
 * one.
 */
class AuthoredBundlesTest {

    /** Relative to {@code backend/app}, which is where the test task runs. */
    private static final Path BUNDLES = Path.of("..", "..", "data", "bundles");

    @Test
    @DisplayName("every authored bundle parses and is this project's to publish")
    void authoredBundlesAreFirstHand() throws IOException {
        List<Path> files = bundles();
        assertThat(files)
                .as("authored bundles in %s — ADR 0015 is unproven without at least one",
                        BUNDLES.toAbsolutePath().normalize())
                .isNotEmpty();

        List<String> refused = new ArrayList<>();
        for (Path file : files) {
            GameDataBundle bundle = parse(file);
            // Named rather than counted: an operator told "not first-hand" can do
            // nothing, and one told which fact can go and read the screen again.
            bundle.secondHandFacts().forEach(fact ->
                    refused.add(file.getFileName() + ": " + fact
                            + " came from " + bundle.provenanceOf(fact).id()
                            + " (" + bundle.provenanceOf(fact).origin() + ")"));
        }
        assertThat(refused)
                .as("facts these bundles are not entitled to publish")
                .isEmpty();
    }

    private static List<Path> bundles() throws IOException {
        if (!Files.isDirectory(BUNDLES)) {
            throw new IllegalStateException(
                    "authored bundle directory is missing: " + BUNDLES.toAbsolutePath().normalize()
                            + ". This test resolves paths relative to backend/app.");
        }
        try (Stream<Path> tree = Files.list(BUNDLES)) {
            return tree.filter(path -> path.getFileName().toString().endsWith(".json")).sorted().toList();
        }
    }

    private static GameDataBundle parse(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            return new CanonicalBundleParser().parse(in);
        }
    }
}
