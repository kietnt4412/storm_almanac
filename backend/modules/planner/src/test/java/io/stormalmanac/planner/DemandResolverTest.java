package io.stormalmanac.planner;

import static io.stormalmanac.planner.TestGame.GOLD;
import static io.stormalmanac.planner.TestGame.HERO;
import static io.stormalmanac.planner.TestGame.INGOT;
import static io.stormalmanac.planner.TestGame.ORE;
import static io.stormalmanac.planner.TestGame.stack;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.Goal;
import io.stormalmanac.gamedata.Progress;
import io.stormalmanac.gamedata.Upgrade;
import io.stormalmanac.player.Roster;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The graph walk that decides what a goal set costs.
 *
 * <p>Everything the solver does afterwards is downstream of these numbers, so
 * these are the cases worth being pedantic about.
 */
class DemandResolverTest {

    private static final ProfileId PROFILE = new ProfileId("p1");

    private final DemandResolver resolver = new DemandResolver();

    /** Two tracks on one entity, each a chain, exactly as a real bundle ships them. */
    private static GameDefinition twoTracks() {
        return TestGame.builder()
                .upgrade("i1", HERO, "insight-0", "insight-1", List.of(stack(ORE, 4), stack(GOLD, 100)))
                .upgrade("i2", HERO, "insight-1", "insight-2", List.of(stack(INGOT, 2), stack(GOLD, 500)))
                .upgrade("i3", HERO, "insight-2", "insight-3", List.of(stack(INGOT, 6)))
                .upgrade("l1", HERO, "level-1", "level-20", List.of(stack(GOLD, 50)))
                .build();
    }

    private static Roster at(String state) {
        return new Roster(PROFILE, state == null ? Map.of() : Map.of(HERO, Set.of(state)));
    }

    @Test
    @DisplayName("a goal costs every step between where the player is and where they asked to be")
    void sumsTheChain() {
        Demand demand = resolver.resolve(
                twoTracks(), at("insight-1"), List.of(Goal.deterministic(HERO, "insight-3")));

        assertThat(demand.quantities()).containsOnly(
                Map.entry(INGOT, 8), Map.entry(GOLD, 500));
        assertThat(demand.steps()).containsExactly("i2", "i3");
        assertThat(demand.alreadyMet()).isEmpty();
    }

    @Test
    @DisplayName("an entity the player does not own is costed from the start of the track")
    void unownedStartsAtTheRoot() {
        Demand demand = resolver.resolve(
                twoTracks(), at(null), List.of(Goal.deterministic(HERO, "insight-2")));

        assertThat(demand.quantities()).containsOnly(
                Map.entry(ORE, 4), Map.entry(INGOT, 2), Map.entry(GOLD, 600));
        assertThat(demand.steps()).containsExactly("i1", "i2");
    }

    @Test
    @DisplayName("a goal the roster is already past costs nothing and says so")
    void alreadyMetIsReportedRatherThanRecharged() {
        Demand demand = resolver.resolve(
                twoTracks(), at("insight-3"), List.of(Goal.deterministic(HERO, "insight-1")));

        assertThat(demand.isEmpty()).isTrue();
        assertThat(demand.alreadyMet()).hasSize(1);
    }

    @Test
    @DisplayName("a goal on one track is not charged the other track's costs")
    void tracksAreIndependent() {
        Demand demand = resolver.resolve(
                twoTracks(), at("insight-2"), List.of(Goal.deterministic(HERO, "level-20")));

        assertThat(demand.quantities()).containsOnly(Map.entry(GOLD, 50));
        assertThat(demand.steps()).containsExactly("l1");
    }

    @Test
    @DisplayName("two goals sharing a step pay for it once")
    void sharedStepsAreNotDoubleCounted() {
        Demand demand = resolver.resolve(twoTracks(), at(null), List.of(
                Goal.deterministic(HERO, "insight-2"),
                Goal.deterministic(HERO, "insight-3")));

        assertThat(demand.steps()).containsExactly("i1", "i2", "i3");
        assertThat(demand.quantityOf(INGOT)).isEqualTo(8);
        assertThat(demand.quantityOf(GOLD)).isEqualTo(600);
    }

    @Test
    @DisplayName("a state no upgrade reaches is refused by name, not solved as free")
    void unreachableStateIsRefused() {
        assertThatThrownBy(() -> resolver.resolve(
                twoTracks(), at("insight-1"), List.of(Goal.deterministic(HERO, "insight-9"))))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("insight-9")
                .hasMessageContaining("hero");
    }

    @Test
    @DisplayName("an entity with no upgrades at all is refused by name")
    void unknownEntityIsRefused() {
        assertThatThrownBy(() -> resolver.resolve(
                twoTracks(), at(null), List.of(Goal.deterministic(new EntityId("nobody"), "insight-1"))))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("nobody");
    }

    @Test
    @DisplayName("a probabilistic goal is refused rather than costed as if it were certain")
    void probabilisticGoalsAreNotPretendedDeterministic() {
        Goal chancy = new Goal(HERO, "insight-2", Goal.Satisfiability.PROBABILISTIC, 0);

        assertThatThrownBy(() -> resolver.resolve(twoTracks(), at("insight-1"), List.of(chancy)))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("distribution");
    }

    /** Resonance, as the game offers it: one move, paid in either of two things. */
    private static GameDefinition twoPrices() {
        return TestGame.builder()
                .upgrade("i1", HERO, "insight-0", "insight-1", List.of(stack(ORE, 4)))
                .upgrade("by-ore", HERO, "insight-1", "resonance", List.of(stack(ORE, 9)))
                .upgrade("by-gold", HERO, "insight-1", "resonance", List.of(stack(GOLD, 900)))
                .build();
    }

    @Test
    @DisplayName("one step at two prices is owed once, and which price is paid is left to the solver")
    void severalPricesAreOneChoice() {
        Demand demand = resolver.resolve(twoPrices(), at(null), List.of(Goal.deterministic(HERO, "resonance")));

        Upgrade byOre = new Upgrade("by-ore", HERO, "insight-1", "resonance", List.of(stack(ORE, 9)));
        assertThat(demand.quantities()).containsOnly(
                Map.entry(ORE, 4), Map.entry(Demand.choiceItem(byOre), 1));
        assertThat(demand.steps()).containsExactly("i1", "by-ore or by-gold");
    }

    @Test
    @DisplayName("a step with several prices shared by two goals is still owed once")
    void severalPricesAreNotDoubleCounted() {
        GameDefinition beyond = TestGame.builder()
                .upgrade("by-ore", HERO, "start", "resonance", List.of(stack(ORE, 9)))
                .upgrade("by-gold", HERO, "start", "resonance", List.of(stack(GOLD, 900)))
                .upgrade("after", HERO, "resonance", "resonance-2", List.of(stack(INGOT, 1)))
                .build();

        Demand demand = resolver.resolve(beyond, at("start"), List.of(
                Goal.deterministic(HERO, "resonance"),
                Goal.deterministic(HERO, "resonance-2")));

        assertThat(demand.steps()).containsExactly("by-ore or by-gold", "after");
        assertThat(demand.quantities().values()).containsExactlyInAnyOrder(1, 1);
    }

    @Test
    @DisplayName("a state reachable from two different states is refused, because a route is not a price")
    void twoRoutesAreRefused() {
        GameDefinition forked = TestGame.builder()
                .upgrade("a", HERO, "start", "goal", List.of(stack(ORE, 1)))
                .upgrade("b", HERO, "elsewhere", "goal", List.of(stack(GOLD, 1)))
                .build();

        assertThatThrownBy(() -> resolver.resolve(
                forked, at("start"), List.of(Goal.deterministic(HERO, "goal"))))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("different states or behind different gates");
    }

    @Test
    @DisplayName("two prices behind different gates are refused, because the gate paid would depend on the choice")
    void differentlyGatedPricesAreRefused() {
        GameDefinition gatedApart = TestGame.builder()
                .upgrade("l1", HERO, "level-1", "level-20", List.of(stack(GOLD, 50)))
                .upgrade("a", HERO, "start", "goal", List.of(stack(ORE, 1)))
                .sink(new Upgrade("b", HERO, "start", "goal", List.of(stack(GOLD, 1)),
                        List.of("level-20"), List.of()))
                .build();

        assertThatThrownBy(() -> resolver.resolve(
                gatedApart, at("start"), List.of(Goal.deterministic(HERO, "goal"))))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("goal");
    }

    /** Insight 2 is gated on level 20, and the level is paid in EXP rather than items. */
    private static GameDefinition gated() {
        return TestGame.builder()
                .upgrade("i1", HERO, "insight-0", "insight-1", List.of(stack(ORE, 4)))
                .sink(new Upgrade("i2", HERO, "insight-1", "insight-2", List.of(stack(INGOT, 2)),
                        List.of("level-20"), List.of()))
                .sink(new Upgrade("l1", HERO, "level-1", "level-20", List.of(stack(GOLD, 50)),
                        List.of(), List.of(new Progress("hero-exp", 9000))))
                .build();
    }

    @Test
    @DisplayName("a gated step pays for the state it is gated on first, EXP included")
    void aGateIsPaidBeforeTheStepItGates() {
        Demand demand = resolver.resolve(gated(), at(null), List.of(Goal.deterministic(HERO, "insight-2")));

        assertThat(demand.steps()).containsExactly("i1", "l1", "i2");
        assertThat(demand.quantities()).containsOnly(
                Map.entry(ORE, 4), Map.entry(INGOT, 2), Map.entry(GOLD, 50),
                Map.entry(Demand.progressItem("hero-exp"), 9000));
    }

    @Test
    @DisplayName("a gate the player already stands on costs nothing")
    void aMetGateIsFree() {
        Demand demand = resolver.resolve(gated(), at("level-20"), List.of(Goal.deterministic(HERO, "insight-2")));

        assertThat(demand.steps()).containsExactly("i1", "i2");
        assertThat(demand.quantityOf(Demand.progressItem("hero-exp"))).isZero();
    }

    @Test
    @DisplayName("a gate shared with an explicit goal is paid for once")
    void aGateAndAGoalShareTheirSteps() {
        Demand demand = resolver.resolve(gated(), at(null), List.of(
                Goal.deterministic(HERO, "level-20"),
                Goal.deterministic(HERO, "insight-2")));

        assertThat(demand.steps()).containsExactly("l1", "i1", "i2");
        assertThat(demand.quantityOf(GOLD)).isEqualTo(50);
    }

    @Test
    @DisplayName("a player past a gated step has met its gate, and is not charged the gate's track again")
    void aCrossedGateIsBehindThePlayer() {
        Demand demand = resolver.resolve(gated(), at("insight-2"), List.of(Goal.deterministic(HERO, "level-20")));

        assertThat(demand.steps()).isEmpty();
        assertThat(demand.quantities()).isEmpty();
        assertThat(demand.alreadyMet()).hasSize(1);
    }

    @Test
    @DisplayName("the gate a crossed step demanded is met without the roster ever naming it")
    void aCrossedGateNeedsNoRosterEntry() {
        GameDefinition twoGates = TestGame.builder()
                .upgrade("l1", HERO, "level-1", "level-20", List.of(stack(GOLD, 50)))
                .upgrade("l2", HERO, "level-20", "level-40", List.of(stack(GOLD, 70)))
                .upgrade("i1", HERO, "insight-0", "insight-1", List.of(stack(ORE, 4)))
                .sink(new Upgrade("i2", HERO, "insight-1", "insight-2", List.of(stack(INGOT, 2)),
                        List.of("level-20"), List.of()))
                .sink(new Upgrade("i3", HERO, "insight-2", "insight-3", List.of(stack(INGOT, 3)),
                        List.of("level-40"), List.of()))
                .build();

        // Standing on insight-2 means level-20 was reached, because i2 could not
        // have been taken otherwise. Only the rest of the level track is owed.
        Demand demand = resolver.resolve(twoGates, at("insight-2"), List.of(Goal.deterministic(HERO, "insight-3")));

        assertThat(demand.steps()).containsExactly("l2", "i3");
        assertThat(demand.quantityOf(GOLD)).isEqualTo(70);
    }

    @Test
    @DisplayName("a step gated on a state only it leads to is refused rather than looped on")
    void aSelfGateIsRefused() {
        GameDefinition circular = TestGame.builder()
                .upgrade("i1", HERO, "insight-0", "insight-1", List.of(stack(ORE, 1)))
                .sink(new Upgrade("i2", HERO, "insight-1", "insight-2", List.of(stack(ORE, 1)),
                        List.of("insight-3"), List.of()))
                .upgrade("i3", HERO, "insight-2", "insight-3", List.of(stack(ORE, 1)))
                .build();

        assertThatThrownBy(() -> resolver.resolve(
                circular, at(null), List.of(Goal.deterministic(HERO, "insight-3"))))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("i2")
                .hasMessageContaining("itself");
    }

    @Test
    @DisplayName("a current state the bundle has never heard of is still the player's state")
    void unknownCurrentStateIsNotOverridden() {
        Demand demand = resolver.resolve(
                twoTracks(), at("some-state-from-a-newer-patch"),
                List.of(Goal.deterministic(HERO, "level-20")));

        assertThat(demand.quantityOf(GOLD)).isEqualTo(50);
    }

    // ── N34: the player stands on more than one track at once ───────────────

    @Test
    @DisplayName("a track the player records is not charged for, even when no gate implies it")
    void aStateBesideTheGoalIsCredited() {
        // level-40 sits beside the insight track rather than behind it: no
        // insight step is gated on it, so no inference can reach it. Only the
        // player saying so can, which is the whole of N34.
        Demand demand = resolver.resolve(
                twoTracks(),
                new Roster(PROFILE, Map.of(HERO, Set.of("insight-1", "level-20"))),
                List.of(Goal.deterministic(HERO, "insight-2"), Goal.deterministic(HERO, "level-20")));

        assertThat(demand.steps()).containsExactly("i2");
        assertThat(demand.quantityOf(GOLD)).isEqualTo(500);
        // The level goal is behind them, so it is met rather than bought again.
        assertThat(demand.alreadyMet()).hasSize(1);
    }

    @Test
    @DisplayName("the whole ladder is charged when the player records only the rank, and not when they record the level")
    void recordingTheLevelIsWhatStopsTheDoubleCharge() {
        GameDefinition gated = TestGame.builder()
                .upgrade("l1", HERO, "level-1", "level-20", List.of(stack(GOLD, 50)))
                .upgrade("l2", HERO, "level-20", "level-40", List.of(stack(GOLD, 70)))
                .upgrade("i1", HERO, "insight-0", "insight-1", List.of(stack(ORE, 4)))
                .sink(new Upgrade("i2", HERO, "insight-1", "insight-2", List.of(stack(INGOT, 2)),
                        List.of("level-40"), List.of()))
                .build();

        // Recorded at insight-1 only. Nothing implies the level track, so the
        // gate on i2 pulls in the whole ladder — 50 + 70. This is the charge
        // ADR 0026 could not remove and said so.
        Demand behindTheGate = resolver.resolve(
                gated, at("insight-1"), List.of(Goal.deterministic(HERO, "insight-2")));
        assertThat(behindTheGate.quantityOf(GOLD)).isEqualTo(120);

        // The same reader, having said where they actually are. PGR permits
        // exactly this — levels are not capped by rank — and it is now sayable.
        Demand havingSaidSo = resolver.resolve(
                gated,
                new Roster(PROFILE, Map.of(HERO, Set.of("insight-1", "level-40"))),
                List.of(Goal.deterministic(HERO, "insight-2")));
        assertThat(havingSaidSo.quantityOf(GOLD)).isZero();
        assertThat(havingSaidSo.steps()).containsExactly("i2");
    }

    @Test
    @DisplayName("an entity on the roster at no state at all is a caller bug, not an empty roster")
    void anEmptyStateSetIsRefused() {
        assertThatThrownBy(() -> new Roster(PROFILE, Map.of(HERO, Set.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at no state");
    }
}
