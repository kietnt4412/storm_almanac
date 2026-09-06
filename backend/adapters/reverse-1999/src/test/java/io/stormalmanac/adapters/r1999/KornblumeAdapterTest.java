package io.stormalmanac.adapters.r1999;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.StageId;
import io.stormalmanac.gamedata.Craft;
import io.stormalmanac.gamedata.Drop;
import io.stormalmanac.gamedata.Item;
import io.stormalmanac.gamedata.Source;
import io.stormalmanac.gamedata.Stage;
import io.stormalmanac.gamedata.Upgrade;
import io.stormalmanac.gamedata.catalog.Entity;
import io.stormalmanac.gamedata.catalog.StatCurve;
import io.stormalmanac.gamedata.ingest.BundleFormatException;
import io.stormalmanac.gamedata.ingest.CanonicalBundleParser;
import io.stormalmanac.gamedata.ingest.CanonicalBundleWriter;
import io.stormalmanac.gamedata.ingest.GameDataBundle;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The adapter, against a snapshot shaped exactly like the real upstream's.
 *
 * <p>The <em>shape</em> is the upstream's and the <em>numbers are ours</em>:
 * seven-element stat blocks, parallel Material/Quantity arrays, a stage map
 * keyed by name, a synthetic zero-cost row, an unreleased character. No
 * upstream data is committed here — see Q3 in {@code TRACKER.md} and the header
 * of {@code tools/fetch-upstream.sh}. What this file cannot prove is that the
 * real snapshot is shaped the way we believe; {@code RealUpstreamPatchTest}
 * does that, against a snapshot fetched on demand.
 */
class KornblumeAdapterTest {

    private static Path snapshot;
    private static GameDataBundle bundle;
    private static List<String> notes;

    @BeforeAll
    static void convert() throws URISyntaxException {
        snapshot = Path.of(KornblumeAdapterTest.class.getResource("/upstream").toURI());
        notes = new ArrayList<>();
        bundle = new KornblumeAdapter(notes::add).adapt(snapshot, 0, "1.0");
    }

    @Test
    @DisplayName("an upstream snapshot becomes a bundle the canonical parser accepts, unchanged")
    void roundTripsThroughTheCanonicalFormat() {
        // The path a conversion actually takes: adapt, write the file somebody
        // approves, ingest that file. If the writer drops a field the adapter
        // filled in, the loss happens here and nowhere louder.
        String canonical = new CanonicalBundleWriter().write(bundle);

        assertThat(new CanonicalBundleParser().parse(canonical))
                .as("what the adapter produced must survive being written out and read back")
                .isEqualTo(bundle);
    }

    @Test
    @DisplayName("every number carries its provenance out to the API")
    void isAttributed() {
        assertThat(bundle.attribution())
                .contains("Kornblume")
                .contains("Bluepoch")
                .contains("1.0");
        assertThat(bundle.game().energyUnit()).isEqualTo("Activity");
    }

    @Nested
    @DisplayName("display names become stable ids")
    class Naming {

        @Test
        @DisplayName("a display name is slugged, accents and all")
        void slugsNames() {
            assertThat(bundle.items()).extracting(Item::id)
                    .contains(new ItemId("sharpodonty"), new ItemId("moment-of-dissonance"))
                    // The upstream's own translation files key on exactly this
                    // convention, which is some evidence it is the right one.
                    .contains(new ItemId("cafe-creme"));
        }

        @Test
        @DisplayName("a name in another script keeps its script rather than folding away to nothing")
        void keepsNonLatinNames() {
            // Found by the real snapshot, not by imagination: folding to ASCII
            // first leaves this name empty and refuses the whole ingest. An id in
            // the script its name is written in is stable, unique and readable
            // to somebody; a hash would be none of those.
            assertThat(entity("зима").displayName()).isEqualTo("Зима");
        }

        @Test
        @DisplayName("a stage keyed by name keeps the name and gains an id")
        void slugsStages() {
            assertThat(stage("mountain-echoes-vi").displayName()).isEqualTo("Mountain Echoes VI");
            // "1-1" and "1-1H" are different stages and must stay different ids.
            assertThat(bundle.sources()).extracting(Source::id).contains("1-1", "1-1h");
        }

        @Test
        @DisplayName("two names that would collapse to one id are refused, naming both")
        void refusesCollidingNames(@TempDir Path dir) throws IOException {
            // "Silver Ore" and "Silver  Ore" are different upstream keys and the
            // same slug. Left alone, one row silently overwrites the other and a
            // drop table starts pointing at the wrong material.
            copy(dir);
            replace(dir.resolve("items.json"), "\"Holy Silver\"", "\"Silver  ore\"");

            assertThatThrownBy(() -> new KornblumeAdapter().adapt(dir, 0, "1.0"))
                    .isInstanceOf(BundleFormatException.class)
                    .hasMessageContaining("silver-ore")
                    .hasMessageContaining("Silver Ore")
                    .hasMessageContaining("Silver  ore");
        }

        @Test
        @DisplayName("a cited name the upstream never defines is refused, naming what cited it")
        void refusesUnknownNames(@TempDir Path dir) throws IOException {
            // The failure mode a rename upstream actually produces. Creating the
            // material on the fly would be worse than refusing: the optimizer
            // would then farm an item that no stage drops.
            copy(dir);
            replace(dir.resolve("arcanists.json"), "\"Page of Starlight\"", "\"Page of Starlite\"");

            assertThatThrownBy(() -> new KornblumeAdapter().adapt(dir, 0, "1.0"))
                    .isInstanceOf(BundleFormatException.class)
                    .hasMessageContaining("Page of Starlite")
                    .hasMessageContaining("Regulus");
        }
    }

    @Nested
    @DisplayName("what is dropped is dropped on purpose, and said out loud")
    class Exclusions {

        @Test
        @DisplayName("a stage that costs nothing is not a stage")
        void dropsFreeStages() {
            // Upstream carries it so its own solver can name items nothing drops
            // yet. To a MIP it is free output, and free output is unbounded.
            assertThat(bundle.sources()).extracting(Source::id).doesNotContain("unreleased");
            assertThat(notes).anySatisfy(note ->
                    assertThat(note).contains("1 stage(s) costing no Activity"));
        }

        @Test
        @DisplayName("an unreleased character is not in the catalog")
        void dropsUnreleasedCharacters() {
            assertThat(bundle.entities()).extracting(Entity::displayName).doesNotContain("Paper Heron");
            assertThat(notes).anySatisfy(note ->
                    assertThat(note).contains("1 unreleased character(s)"));
        }

        @Test
        @DisplayName("an unreleased psychube is not in the catalog")
        void dropsUnreleasedEquipment() {
            // The real upstream ships these as rows with a null Name, so the
            // release flag has to be read before anything else is: a conversion
            // must not be refused by a row it was about to drop.
            assertThat(bundle.entities()).extracting(Entity::displayName)
                    .containsExactly("Regulus", "Зима", "Gluttony", "Hopscotch", "Silent Aria");
            assertThat(notes).anySatisfy(note ->
                    assertThat(note).contains("1 unreleased psychube(s)"));
        }

        @Test
        @DisplayName("resonance patterns are alternatives, and an upgrade is not a choice")
        void dropsAlternativeCostRows() {
            // Two rows, one step, differing only by pattern. Converting both
            // would charge the player for both — an "or" the model has no way
            // to express and this adapter refuses to fake.
            assertThat(upgrades()).extracting(Upgrade::id).noneMatch(id -> id.contains("frequency"));
            assertThat(notes).anySatisfy(note ->
                    assertThat(note).contains("2 resonance-pattern cost row(s)"));
        }
    }

    @Nested
    @DisplayName("the model holds what the upstream says")
    class Fidelity {

        @Test
        @DisplayName("a drop of more than one per run survives, because a yield is not a probability")
        void keepsYieldsAboveOne() {
            // docs/prior-art.md §4.1: the record used to hold a probability in
            // [0,1] and would have rejected most of the real dataset on ingest.
            assertThat(stage("1-1").drops())
                    .contains(new Drop(new ItemId("sharpodonty"), 2.187));
        }

        @Test
        @DisplayName("a recipe of parallel arrays becomes consumes and produces")
        void convertsCrafts() {
            Craft craft = bundle.sources().stream()
                    .filter(Craft.class::isInstance).map(Craft.class::cast)
                    .filter(c -> c.id().equals("craft-holy-silver"))
                    .findFirst().orElseThrow();

            assertThat(craft.consumes()).extracting(s -> s.item().value(), s -> s.quantity())
                    .containsExactly(tuple("silver-ore", 3), tuple("sharpodonty", 500));
            assertThat(craft.produces()).singleElement()
                    .satisfies(s -> assertThat(s.item()).isEqualTo(new ItemId("holy-silver")));
        }

        @Test
        @DisplayName("an upgrade track becomes a chain over states the game names")
        void convertsUpgradeTracks() {
            // insight-1 → insight-2 and resonance-2 → resonance-3, with the
            // states left as opaque strings. Nothing downstream parses them, and
            // that is what lets the second game number its own steps differently.
            assertThat(upgrades())
                    .filteredOn(u -> u.entity().equals(new EntityId("regulus")))
                    .extracting(Upgrade::fromState, Upgrade::toState)
                    .contains(
                            tuple("insight-0", "insight-1"),
                            tuple("insight-1", "insight-2"),
                            tuple("resonance-1", "resonance-2"),
                            tuple("resonance-2", "resonance-3"),
                            tuple("euphoria-0", "euphoria-1"));
        }

        @Test
        @DisplayName("a stat block of seven numbers becomes seven curves, in level order")
        void convertsStatBlocks() {
            Entity regulus = entity("regulus");
            assertThat(regulus.statCurves()).extracting(StatCurve::stat)
                    .containsExactly("attack", "health", "reality-def", "mental-def",
                            "technique", "crit-rate", "crit-dmg");

            StatCurve attack = regulus.statCurves().getFirst();
            assertThat(attack.breakpoints())
                    .extracting(StatCurve.Breakpoint::ascensionTier, StatCurve.Breakpoint::level)
                    .containsExactly(tuple(0, 1), tuple(0, 30), tuple(1, 40));
            assertThat(attack.valueAt(1, 40)).isEqualTo(677.0);
        }

        @Test
        @DisplayName("an all-zero stat block is no stat block, not a character with zero attack")
        void refusesToInventStats() {
            assertThat(entity("зима").statCurves()).isEmpty();
        }

        @Test
        @DisplayName("equipment arrives as an entity with a kind, as ADR 0007 said it would")
        void convertsEquipment() {
            Entity gluttony = entity("gluttony");
            assertThat(gluttony.kind()).isEqualTo("equipment");
            assertThat(gluttony.tags()).containsExactly("atk", "critical");
            assertThat(entity("regulus").kind()).isEqualTo("character");
            // "None" and a null tag both mean no tags, not a tag called "None".
            assertThat(entity("hopscotch").tags()).isEmpty();
            assertThat(entity("silent-aria").tags()).isEmpty();
        }
    }

    @Test
    @DisplayName("a directory missing a file says which file and what the layout should be")
    void refusesAnIncompleteSnapshot(@TempDir Path dir) throws IOException {
        copy(dir);
        Files.delete(dir.resolve("psychubes.json"));

        assertThatThrownBy(() -> new KornblumeAdapter().adapt(dir, 0, "1.0"))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("psychubes.json")
                .hasMessageContaining("items.json, stages.json");
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private static Stage stage(String id) {
        return bundle.sources().stream()
                .filter(Stage.class::isInstance).map(Stage.class::cast)
                .filter(s -> s.stageId().equals(new StageId(id)))
                .findFirst().orElseThrow(() -> new AssertionError("no stage " + id));
    }

    private static Entity entity(String id) {
        return bundle.entities().stream()
                .filter(e -> e.id().equals(new EntityId(id)))
                .findFirst().orElseThrow(() -> new AssertionError("no entity " + id));
    }

    private static List<Upgrade> upgrades() {
        return bundle.sinks().stream()
                .filter(Upgrade.class::isInstance).map(Upgrade.class::cast).toList();
    }

    private static void copy(Path into) throws IOException {
        try (Stream<Path> files = Files.list(snapshot)) {
            for (Path file : files.toList()) {
                Files.copy(file, into.resolve(file.getFileName().toString()),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static void replace(Path file, String from, String to) throws IOException {
        Files.writeString(file, Files.readString(file).replace(from, to));
    }
}
