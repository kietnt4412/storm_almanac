package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;

import io.stormalmanac.gamedata.DayBoundary;
import io.stormalmanac.gamedata.Game;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.diff.Axis;
import io.stormalmanac.gamedata.diff.Change;
import io.stormalmanac.gamedata.diff.VersionDiff;
import io.stormalmanac.gamedata.ingest.CanonicalBundleParser;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The patch report, over the two fixture snapshots.
 *
 * <p>No database. The diff is a pure comparison of two {@link GameDefinition}s
 * and does not care where they came from, which is worth keeping true: a report
 * that needed a connection could not be produced from two files on disk, and
 * that is exactly what somebody reviewing an ingest wants to do.
 * {@code GameDataIngestTest} covers the other half — that the same diff comes
 * out when both sides have made the round trip through the schema.
 *
 * <p>The 1.1 fixture's own header lists every difference it was built to carry.
 * If a change below stops matching, either the fixture moved or the flattener
 * stopped looking at a field, and the second one is the failure that matters:
 * a field nobody flattens is a field the patch report will never mention.
 */
class GameDataDiffTest {

    private final VersionDiff diff = VersionDiff.between(load("1.0"), load("1.1"));

    @Test
    @DisplayName("a stage that disappeared is one line, not one line per drop it had")
    void aRemovedSubjectIsASingleChange() {
        assertThat(diff.on(Axis.PROGRESSION))
                .contains(new Change(Axis.PROGRESSION, Change.Kind.REMOVED, "stage 'pg-event-1'", null, null, null));

        // The event stage had a name, an energy cost, a window and a drop. Six
        // facts, one line — that is the whole reason subjects are diffed before
        // fields.
        assertThat(diff.changes()).filteredOn(change -> change.subject().equals("stage 'pg-event-1'"))
                .hasSize(1);
    }

    @Test
    @DisplayName("added content is reported once per thing, on the progression axis")
    void additionsAreReported() {
        assertThat(diff.on(Axis.PROGRESSION)).extracting(Change::subject)
                .contains("stage 'pg-3-1'", "item 'sigil-radiant'");
    }

    @Test
    @DisplayName("a drop rate that moved is reported with both numbers")
    void aChangedDropRate() {
        assertThat(diff.on(Axis.PROGRESSION))
                .contains(new Change(Axis.PROGRESSION, Change.Kind.CHANGED,
                        "stage 'pg-1-1'", "drop ore-rough", "1.4", "1.6"));
    }

    @Test
    @DisplayName("a material cost that moved is reported against the upgrade that pays it")
    void aChangedUpgradeCost() {
        assertThat(diff.on(Axis.PROGRESSION))
                .contains(new Change(Axis.PROGRESSION, Change.Kind.CHANGED,
                        "upgrade 'warden-insight-2'", "cost gold", "20000", "18000"));
    }

    @Test
    @DisplayName("a balance change to one multiplier does not report the multipliers beside it")
    void aChangedSkillMultiplier() {
        // The case the two axes exist for. Rank 2's damage moved and its cost
        // did not; a report that said "rank 2 changed" would make a reader open
        // the bundle to find out what.
        assertThat(diff.on(Axis.CATALOG))
                .contains(new Change(Axis.CATALOG, Change.Kind.CHANGED,
                        "entity 'warden'", "skill warden-strike rank 2 damage", "1.32", "1.28"));

        assertThat(diff.on(Axis.CATALOG)).extracting(Change::detail)
                .doesNotContain("skill warden-strike rank 2 cost", "skill warden-strike rank 1 damage");
    }

    @Test
    @DisplayName("a stat curve breakpoint that moved is reported at its tier and level")
    void aChangedStatCurve() {
        assertThat(diff.on(Axis.CATALOG))
                .contains(new Change(Axis.CATALOG, Change.Kind.CHANGED,
                        "entity 'warden'", "atk at tier 1 level 40", "415.0", "430.0"));
    }

    @Test
    @DisplayName("both axes are reported, and an untouched axis reports nothing")
    void bothAxesAreReported() {
        // Phase 1's exit criterion is that the report renders for both axes.
        assertThat(diff.on(Axis.PROGRESSION)).isNotEmpty();
        assertThat(diff.on(Axis.CATALOG)).isNotEmpty();
        // The banner did not move between these two snapshots, and the report
        // must not invent a change to say so.
        assertThat(diff.on(Axis.GACHA)).isEmpty();
    }

    @Test
    @DisplayName("a version diffed against itself reports nothing at all")
    void identityDiffIsEmpty() {
        // The guard against a flattener that formats a value non-deterministically
        // — a map iteration order, a double rendered differently on two passes —
        // which would otherwise surface as phantom changes in every report.
        assertThat(VersionDiff.between(load("1.1"), load("1.1")).isEmpty()).isTrue();
    }

    @Test
    @DisplayName("a day boundary arriving is a change the report names, not a silent one")
    void aDeclaredDayBoundaryIsReported() {
        // The title itself was flattened by nothing at all until the day
        // boundary landed, which meant the sequence that first declared one
        // previewed as "no changes" while every rotating stage's capacity moved
        // under it. Found by running the preview, not by a test.
        GameDefinition before = load("1.0");
        GameDefinition after = withRollover(before, new DayBoundary(ZoneId.of("UTC"), 5));

        assertThat(VersionDiff.between(before, after).on(Axis.PROGRESSION))
                .containsExactly(new Change(Axis.PROGRESSION, Change.Kind.CHANGED,
                        "game 'proving-ground'", "day rollover", "unstated", "05:00 UTC"));
    }

    @Test
    @DisplayName("the rendered report names the versions and groups by axis")
    void theReportRenders() {
        String report = diff.render();

        assertThat(report).startsWith("proving-ground: 1.0 → 1.1");
        assertThat(report).contains("progression · ").contains("catalog · ");
        assertThat(report).contains("~ stage 'pg-1-1' · drop ore-rough: 1.4 → 1.6");
        assertThat(report).contains("- stage 'pg-event-1'");
        assertThat(report).contains("+ stage 'pg-3-1'");
        assertThat(report).doesNotContain("gacha");
    }

    private static GameDefinition withRollover(GameDefinition definition, DayBoundary boundary) {
        Game game = definition.game();
        return new GameDefinition(
                new Game(game.id(), game.displayName(), game.energyUnit(), boundary),
                definition.version(), definition.items(), definition.sources(),
                definition.sinks(), definition.banners(), definition.entities(),
                // Carried, not defaulted. The seven-argument constructor exists
                // for versions published before a progress kind could be named,
                // and reaching for it here would drop the fixture's own names
                // and report a rename this test never made.
                definition.progressKinds());
    }

    private static GameDefinition load(String label) {
        String fixture = "/gamedata/proving-ground-" + label + ".json";
        try (InputStream in = GameDataDiffTest.class.getResourceAsStream(fixture)) {
            if (in == null) throw new IllegalStateException("fixture not on the classpath: " + fixture);
            // The approval time is not part of a diff; any instant will do.
            return new CanonicalBundleParser().parse(in).definitionApprovedAt(Instant.EPOCH);
        } catch (IOException e) {
            throw new IllegalStateException("could not read " + fixture, e);
        }
    }
}
