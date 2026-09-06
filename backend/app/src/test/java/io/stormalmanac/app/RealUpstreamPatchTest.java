package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.gamedata.Stage;
import io.stormalmanac.gamedata.Upgrade;
import io.stormalmanac.gamedata.catalog.Entity;
import io.stormalmanac.gamedata.diff.Axis;
import io.stormalmanac.gamedata.diff.Change;
import io.stormalmanac.gamedata.diff.VersionDiff;
import io.stormalmanac.gamedata.ingest.CanonicalBundleParser;
import io.stormalmanac.gamedata.ingest.CanonicalBundleWriter;
import io.stormalmanac.gamedata.ingest.GameDataBundle;
import io.stormalmanac.gamedata.ingest.GameDataIngestRepository;
import io.stormalmanac.gamedata.ingest.UpstreamAdapter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Two real patches of somebody else's data, through the whole pipeline.
 *
 * <p>This is the half of Phase 1's exit criterion that the synthetic fixture
 * cannot meet. {@code proving-ground} exercises every shape the canonical format
 * has, and it was invented here — it cannot surprise us, and the interesting
 * failures in a data pipeline are all surprises. What runs here instead is a
 * Kornblume snapshot at patch 3.3 and the same snapshot at 3.5, converted by
 * {@code KornblumeAdapter}, ingested, published, and diffed against each other.
 *
 * <h2>Why this test skips itself</h2>
 *
 * <p>The snapshots are not in this repository and will not be. Kornblume carries
 * no licence file, which is all rights reserved rather than permission, so its
 * data is read and never vendored — Q3 in {@code TRACKER.md}. The consequence is
 * that CI has no snapshot and this class is skipped there, which is a real and
 * deliberate hole in the pipeline rather than an oversight: <b>the strongest
 * test in the repository is the one CI does not run.</b> Say so out loud rather
 * than letting a green tick imply otherwise.
 *
 * <p>To run it:
 *
 * <pre>
 * ./tools/fetch-upstream.sh
 * ./gradlew :app:test --tests '*RealUpstreamPatchTest'
 * </pre>
 *
 * <p>The default location is {@code backend/build/upstream-snapshots}, which {@code build/}
 * already keeps out of the index; {@code -Dstorm-almanac.upstream=<dir>} points
 * it elsewhere.
 */
@EnabledIf("snapshotsArePresent")
class RealUpstreamPatchTest extends GameDataDatabaseTest {

    private static final GameId REVERSE_1999 = new GameId("reverse-1999");

    /** The two patches {@code tools/fetch-upstream.sh} fetches, oldest first. */
    private static final List<String> PATCHES = List.of("3.3", "3.5");

    @Autowired
    private GameDataIngestRepository ingest;

    @Autowired
    private GameDefinitionRepository definitions;

    @Test
    @DisplayName("two real patches convert, ingest, publish and read back as what went in")
    void survivesTheWholePipeline() {
        // The claim the synthetic fixture could only make about data we wrote:
        // the schema holds somebody else's numbers and gives them back unchanged.
        for (int sequence = 0; sequence < PATCHES.size(); sequence++) {
            GameDataBundle bundle = convert(sequence, PATCHES.get(sequence));
            ingest.ingestDraft(bundle);
            GameDataVersion published = ingest.publish(REVERSE_1999, bundle.sequence());

            GameDefinition read = definitions.find(REVERSE_1999, published.sequence()).orElseThrow();

            assertThat(read.items()).containsExactlyInAnyOrderElementsOf(bundle.items());
            assertThat(read.sources()).containsExactlyInAnyOrderElementsOf(bundle.sources());
            assertThat(read.sinks()).containsExactlyInAnyOrderElementsOf(bundle.sinks());
            assertThat(read.entities()).containsExactlyInAnyOrderElementsOf(bundle.entities());
            assertThat(read.version().attribution()).contains("Kornblume");
        }
    }

    @Test
    @DisplayName("a real snapshot is bigger than any fixture, and every reference in it resolves")
    void isActuallyRealData() {
        // A conversion that quietly produced ten items would pass every other
        // assertion here. GameDataBundle refuses a dangling reference, so simply
        // constructing these proves referential integrity across the whole graph
        // — the numbers below are what stops an empty one looking healthy.
        GameDataBundle latest = convert(1, "3.5");

        assertThat(latest.items()).hasSizeGreaterThan(50);
        assertThat(latest.sources()).filteredOn(Stage.class::isInstance).hasSizeGreaterThan(80);
        assertThat(latest.entities()).filteredOn(e -> e.kind().equals("character")).hasSizeGreaterThan(100);
        assertThat(latest.entities()).filteredOn(e -> e.kind().equals("equipment")).isNotEmpty();
        assertThat(latest.sinks()).filteredOn(Upgrade.class::isInstance).hasSizeGreaterThan(500);

        // The defect docs/prior-art.md §4.1 found in our own Drop record: real
        // drop tables state expected quantity per run and go above 1.0, which the
        // original probability-in-[0,1] validation would have rejected wholesale.
        assertThat(latest.sources()).filteredOn(Stage.class::isInstance)
                .extracting(Stage.class::cast)
                .flatExtracting(Stage::drops)
                .anySatisfy(drop -> assertThat(drop.expectedYield()).isGreaterThan(1.0));
    }

    @Test
    @DisplayName("the patch report renders over real data, on both axes")
    void diffsARealPatch() {
        // "Tests over real patch data, including a patch that changes something."
        // 3.3 → 3.5 is a real interval of the upstream's own history: characters
        // were released, a material was added, recipes appeared.
        GameDataBundle from = convert(0, "3.3");
        GameDataBundle to = convert(1, "3.5");

        VersionDiff diff = VersionDiff.between(
                from.definitionApprovedAt(Instant.EPOCH),
                to.definitionApprovedAt(Instant.EPOCH));

        assertThat(diff.changes()).isNotEmpty();
        assertThat(diff.changes()).extracting(Change::axis)
                .as("a patch that adds characters moves the catalog, and their costs move progression")
                .contains(Axis.CATALOG, Axis.PROGRESSION);

        assertThat(to.entities()).extracting(Entity::displayName)
                .as("characters released between the two snapshots")
                .containsAll(List.of("Igor", "Lady by the Lake"))
                .doesNotContain("Paper Heron");
        assertThat(from.entities()).extracting(Entity::displayName)
                .doesNotContain("Igor", "Lady by the Lake");

        assertThat(diff.render()).isNotBlank();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Converts one snapshot the way the CLI does — through the file, not around
     * it. Going straight from adapter to repository would leave the writer
     * untested against real data, and the writer is what a reviewer approves.
     */
    private GameDataBundle convert(long sequence, String patch) {
        UpstreamAdapter adapter = UpstreamAdapters.forGame(REVERSE_1999, note -> { }).orElseThrow();
        GameDataBundle converted = adapter.adapt(snapshots().resolve(patch), sequence, patch);
        return new CanonicalBundleParser().parse(new CanonicalBundleWriter().write(converted));
    }

    static Path snapshots() {
        // Tests run with backend/app as the working directory, so the script's
        // default of backend/build/upstream-snapshots is one level up.
        String configured = System.getProperty("storm-almanac.upstream");
        return configured != null ? Path.of(configured) : Path.of("..", "build", "upstream-snapshots");
    }

    @SuppressWarnings("unused") // named by @EnabledIf
    static boolean snapshotsArePresent() {
        return PATCHES.stream().allMatch(patch ->
                Files.isReadable(snapshots().resolve(patch).resolve("items.json")));
    }
}
