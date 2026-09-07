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
import io.stormalmanac.player.Roster;
import java.util.List;
import java.util.Map;
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
        return new Roster(PROFILE, state == null ? Map.of() : Map.of(HERO, state));
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

    @Test
    @DisplayName("a state reachable two ways is refused, because choosing is the solver's job")
    void ambiguousRouteIsRefused() {
        GameDefinition forked = TestGame.builder()
                .upgrade("a", HERO, "start", "goal", List.of(stack(ORE, 1)))
                .upgrade("b", HERO, "start", "goal", List.of(stack(GOLD, 1)))
                .build();

        assertThatThrownBy(() -> resolver.resolve(
                forked, at("start"), List.of(Goal.deterministic(HERO, "goal"))))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("solver's job");
    }

    @Test
    @DisplayName("a current state the bundle has never heard of is still the player's state")
    void unknownCurrentStateIsNotOverridden() {
        Demand demand = resolver.resolve(
                twoTracks(), at("some-state-from-a-newer-patch"),
                List.of(Goal.deterministic(HERO, "level-20")));

        assertThat(demand.quantityOf(GOLD)).isEqualTo(50);
    }
}
