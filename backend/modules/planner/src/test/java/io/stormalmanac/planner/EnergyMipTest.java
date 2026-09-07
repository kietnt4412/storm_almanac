package io.stormalmanac.planner;

import static io.stormalmanac.planner.TestGame.GOLD;
import static io.stormalmanac.planner.TestGame.GOLD_STAGE;
import static io.stormalmanac.planner.TestGame.INGOT;
import static io.stormalmanac.planner.TestGame.ORE;
import static io.stormalmanac.planner.TestGame.ORE_STAGE;
import static io.stormalmanac.planner.TestGame.RELIC;
import static io.stormalmanac.planner.TestGame.RELIC_STAGE;
import static io.stormalmanac.planner.TestGame.stack;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gamedata.Availability;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Reward;
import io.stormalmanac.gamedata.Shop;
import java.time.Instant;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The mixed-integer program, on a game small enough to check on paper.
 *
 * <p>Every expected number below is worked out in its own comment. A solver test
 * that only asserts "some plan came back" would pass against a solver that had
 * quietly stopped solving.
 */
class EnergyMipTest {

    private static final Instant NOW = Instant.parse("2026-09-07T00:00:00Z");

    /**
     * s-ore   · 10 energy · 2.0 ore per run
     * s-gold  ·  5 energy · 100 gold per run
     * smelt   · 3 ore + 50 gold -> 1 ingot
     * forge   · 2 ingot + 100 gold -> 1 relic
     */
    private static GameDefinition workshop() {
        return TestGame.builder()
                .stage(ORE_STAGE, 10, ORE, 2.0)
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .craft("smelt", List.of(stack(ORE, 3), stack(GOLD, 50)), List.of(stack(INGOT, 1)))
                .craft("forge", List.of(stack(INGOT, 2), stack(GOLD, 100)), List.of(stack(RELIC, 1)))
                .build();
    }

    private static EnergyMip.Outcome solve(
            GameDefinition definition, Map<ItemId, Integer> inventory, Map<ItemId, Integer> demand) {
        return EnergyMip.solve(new EnergyMip.Inputs(
                definition, YieldTable.declared(definition), inventory, demand, NOW, 2000L));
    }

    @Test
    @DisplayName("the cheapest plan for a crafted item farms the inputs and does the craft")
    void solvesOneCraftDeep() {
        // 6 ingots need 18 ore and 300 gold.
        // 18 ore / 2.0 per run = 9 runs of s-ore  = 90 energy
        // 300 gold / 100 per run = 3 runs of s-gold = 15 energy
        EnergyMip.Outcome outcome = solve(workshop(), Map.of(), Map.of(INGOT, 6));

        assertThat(outcome.totalEnergy()).isEqualTo(105);
        assertThat(outcome.stageRuns()).extracting(run -> run.stage().value(), StageRun::runs)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("s-ore", 9),
                        org.assertj.core.groups.Tuple.tuple("s-gold", 3));
        assertThat(outcome.conversions()).containsExactly(new Conversion("smelt", 6));
    }

    @Test
    @DisplayName("a craft chain two deep needs no recursion, only the constraint rows")
    void solvesTwoCraftsDeep() {
        // 1 relic needs 2 ingot + 100 gold.
        // 2 ingot needs 6 ore + 100 gold.
        // 6 ore / 2.0 = 3 runs of s-ore = 30 energy; 200 gold / 100 = 2 runs of s-gold = 10.
        EnergyMip.Outcome outcome = solve(workshop(), Map.of(), Map.of(RELIC, 1));

        assertThat(outcome.totalEnergy()).isEqualTo(40);
        assertThat(outcome.conversions()).containsExactly(
                new Conversion("forge", 1), new Conversion("smelt", 2));
    }

    @Test
    @DisplayName("what the player already owns is subtracted before anything is farmed")
    void inventoryIsSubtracted() {
        // 6 ingots still need 18 ore and 300 gold; the player holds 10 ore and 250 gold.
        // 8 ore / 2.0 = 4 runs = 40 energy; 50 gold / 100 = 1 run = 5 energy.
        EnergyMip.Outcome outcome = solve(
                workshop(), Map.of(ORE, 10, GOLD, 250), Map.of(INGOT, 6));

        assertThat(outcome.totalEnergy()).isEqualTo(45);
    }

    @Test
    @DisplayName("a fractional run is rounded up into a whole one, which is the point of the MIP")
    void runsAreWholeNumbers() {
        // 0.3 relic per run: 1 / 0.3 = 3.33 runs, and a third of a run buys nothing.
        GameDefinition definition = TestGame.builder()
                .stage(RELIC_STAGE, 30, RELIC, 0.3)
                .build();

        EnergyMip.Outcome outcome = solve(definition, Map.of(), Map.of(RELIC, 1));

        assertThat(outcome.stageRuns()).singleElement()
                .satisfies(run -> assertThat(run.runs()).isEqualTo(4));
        assertThat(outcome.totalEnergy()).isEqualTo(120);
    }

    @Test
    @DisplayName("the cheaper of two stages wins on energy per unit, not on energy per run")
    void picksOnEnergyPerUnit() {
        // s-ore: 10 energy for 2.0 ore  = 5.0 energy per ore.
        // s-gold here drops ore too, at 30 energy for 10.0 ore = 3.0 energy per ore.
        // 20 ore is 2 runs of the dearer-looking stage, 60 energy, against 100.
        GameDefinition definition = TestGame.builder()
                .stage(ORE_STAGE, 10, ORE, 2.0)
                .stage(GOLD_STAGE, 30, ORE, 10.0)
                .build();

        EnergyMip.Outcome outcome = solve(definition, Map.of(), Map.of(ORE, 20));

        assertThat(outcome.totalEnergy()).isEqualTo(60);
        assertThat(outcome.stageRuns()).singleElement()
                .satisfies(run -> assertThat(run.stage()).isEqualTo(GOLD_STAGE));
    }

    @Test
    @DisplayName("an expired event stage makes its drops unreachable, and the message names it")
    void expiredStageIsNamedInTheRefusal() {
        GameDefinition definition = TestGame.builder()
                .stage(ORE_STAGE, 10, ORE, 2.0)
                .stage(RELIC_STAGE, 15,
                        new Availability(Set.of(), null, Instant.parse("2026-08-01T00:00:00Z")),
                        RELIC, 2.0)
                .build();

        assertThatThrownBy(() -> solve(definition, Map.of(), Map.of(RELIC, 1)))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("relic")
                .hasMessageContaining("s-relic")
                .hasMessageContaining("closed or unreleased");
    }

    @Test
    @DisplayName("an item sold only in a shop is refused by name, not costed at zero")
    void shopOnlyItemIsRefusedRatherThanFree() {
        GameDefinition definition = TestGame.builder()
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .source(new Shop("weekly-relic", GOLD, 500, new ItemStack(RELIC, 1),
                        3, Period.ofDays(7), Availability.ALWAYS))
                .build();

        assertThatThrownBy(() -> solve(definition, Map.of(), Map.of(RELIC, 1)))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("weekly-relic")
                .hasMessageContaining("time axis");
    }

    @Test
    @DisplayName("an item granted only by a reward is refused by name for the same reason")
    void rewardOnlyItemIsRefusedRatherThanFree() {
        GameDefinition definition = TestGame.builder()
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .source(new Reward("weekly-quest", Reward.Cadence.WEEKLY,
                        List.of(new ItemStack(RELIC, 2)), Availability.ALWAYS))
                .build();

        assertThatThrownBy(() -> solve(definition, Map.of(), Map.of(RELIC, 1)))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("weekly-quest")
                .hasMessageContaining("WEEKLY");
    }

    @Test
    @DisplayName("an item nothing in the game yields is refused, and says exactly that")
    void unknownItemIsRefused() {
        assertThatThrownBy(() -> solve(workshop(), Map.of(), Map.of(ItemId.of("moonstone"), 1)))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("moonstone")
                .hasMessageContaining("no source in this game version yields it");
    }

    @Test
    @DisplayName("a rotating stage still counts, because a plan spanning days reaches every weekday")
    void weekdayRotationDoesNotRemoveAStage() {
        GameDefinition definition = TestGame.builder()
                .stage(ORE_STAGE, 10,
                        new Availability(Set.of(java.time.DayOfWeek.TUESDAY), null, null), ORE, 2.0)
                .build();
        // NOW is a Monday; the stage opens tomorrow and the plan takes days anyway.
        assertThat(NOW.atZone(java.time.ZoneOffset.UTC).getDayOfWeek())
                .isEqualTo(java.time.DayOfWeek.MONDAY);

        EnergyMip.Outcome outcome = solve(definition, Map.of(), Map.of(ORE, 4));

        assertThat(outcome.totalEnergy()).isEqualTo(20);
    }

    @Test
    @DisplayName("a demand the inventory already covers costs no runs at all")
    void nothingToFarm() {
        EnergyMip.Outcome outcome = solve(workshop(), Map.of(INGOT, 10), Map.of(INGOT, 6));

        assertThat(outcome.totalEnergy()).isZero();
        assertThat(outcome.stageRuns()).isEmpty();
        assertThat(outcome.conversions()).isEmpty();
    }

    @Test
    @DisplayName("a free conversion loop is not run for its own sake")
    void pointlessConversionsAreNotMade() {
        // ingot -> relic -> ingot at no loss: the tie-break weight is what stops
        // the solver returning an arbitrary number of laps round the cycle.
        GameDefinition definition = TestGame.builder()
                .stage(ORE_STAGE, 10, ORE, 2.0)
                .craft("smelt", List.of(stack(ORE, 3)), List.of(stack(INGOT, 1)))
                .craft("bless", List.of(stack(INGOT, 1)), List.of(stack(RELIC, 1)))
                .craft("melt", List.of(stack(RELIC, 1)), List.of(stack(INGOT, 1)))
                .build();

        EnergyMip.Outcome outcome = solve(definition, Map.of(), Map.of(INGOT, 2));

        assertThat(outcome.totalEnergy()).isEqualTo(30); // 6 ore, 3 runs
        assertThat(outcome.conversions()).containsExactly(new Conversion("smelt", 2));
    }
}
