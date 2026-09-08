package io.stormalmanac.planner;

import static io.stormalmanac.planner.TestGame.ORE;
import static io.stormalmanac.planner.TestGame.RELIC;
import static org.assertj.core.api.Assertions.assertThat;

import io.stormalmanac.common.id.StageId;
import io.stormalmanac.gamedata.Availability;
import io.stormalmanac.gamedata.Drop;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.Stage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What the solver is actually given, once a sample size is in the data.
 *
 * <p>The numbers in these tests are the shape of the real disagreement the
 * community benchmark found: an upstream stage sampled over about a hundred
 * runs, ranked against one sampled over thousands, where the thin sample shows
 * the higher rate. Every stage here is invented; only the sample sizes are
 * borrowed, because they are what the arithmetic is about.
 */
class YieldTableTest {

    private static final StageId THIN = new StageId("s-thin");
    private static final StageId THICK = new StageId("s-thick");
    private static final StageId FIXED = new StageId("s-fixed");

    @Test
    @DisplayName("a declared yield reaches the model exactly as declared")
    void declaredIsUntouched() {
        YieldTable table = YieldTable.declared(game(
                stage(FIXED, 20, new Drop(ORE, 9000))));

        assertThat(table.yield(FIXED, ORE)).isEqualTo(9000.0);
        assertThat(table.discountedCount()).isZero();
    }

    @Test
    @DisplayName("the same rate over more runs survives the discount better")
    void thickSamplesLoseLess() {
        YieldTable table = YieldTable.declared(game(
                stage(THIN, 20, new Drop(ORE, 0.2, 105)),
                stage(THICK, 20, new Drop(ORE, 0.2, 8_000))));

        assertThat(table.yield(THIN, ORE)).isLessThan(table.yield(THICK, ORE));
        assertThat(table.yield(THIN, ORE)).isLessThan(0.2);
        assertThat(table.yield(THICK, ORE)).isLessThan(0.2);
        assertThat(table.discountedCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("a lucky hundred-run stage stops outranking a well-sampled one")
    void theBenchmarksDisagreement() {
        // Point estimates rank the thin stage first by a third. This is exactly
        // how the model came to prefer 4-5H over 5-8H for Milled Magnesia, and
        // it is the only thing every disagreement over 25% had in common.
        Drop lucky = new Drop(ORE, 0.20, 105);
        Drop measured = new Drop(ORE, 0.15, 8_000);
        assertThat(lucky.expectedYield()).isGreaterThan(measured.expectedYield());

        YieldTable table = YieldTable.declared(game(
                stage(THIN, 20, lucky),
                stage(THICK, 20, measured)));

        assertThat(table.yield(THIN, ORE)).isLessThan(table.yield(THICK, ORE));
    }

    @Test
    @DisplayName("the discount never removes the only source of an item")
    void aSourceStaysASource() {
        // A coefficient of zero is dropped from the model, so a discount that
        // could reach zero would make an item unobtainable rather than
        // expensive — a refusal the player could not act on.
        YieldTable table = YieldTable.declared(game(
                stage(THIN, 20, new Drop(RELIC, 0.001, 105))));

        assertThat(table.yield(THIN, RELIC)).isGreaterThan(0.0);
        assertThat(table.yieldsOf(THIN)).containsKey(RELIC);
    }

    @Test
    @DisplayName("a declared and a sampled yield can sit in the same table")
    void mixedProvenance() {
        YieldTable table = YieldTable.declared(game(
                stage(FIXED, 20, new Drop(ORE, 2.0)),
                stage(THICK, 20, new Drop(ORE, 2.0, 41_212))));

        assertThat(table.yield(FIXED, ORE)).isEqualTo(2.0);
        assertThat(table.yield(THICK, ORE)).isLessThan(2.0).isGreaterThan(1.9);
        assertThat(table.discountedCount()).isEqualTo(1);
    }

    private static GameDefinition game(Stage... stages) {
        TestGame builder = TestGame.builder();
        for (Stage stage : stages) builder.source(stage);
        return builder.build();
    }

    private static Stage stage(StageId id, int energy, Drop... drops) {
        return new Stage(id, id.value(), energy, List.of(drops), Availability.ALWAYS);
    }
}
