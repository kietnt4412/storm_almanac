package io.stormalmanac.planner;

import static io.stormalmanac.planner.TestGame.GOLD;
import static io.stormalmanac.planner.TestGame.GOLD_STAGE;
import static io.stormalmanac.planner.TestGame.HERO;
import static io.stormalmanac.planner.TestGame.INGOT;
import static io.stormalmanac.planner.TestGame.ORE;
import static io.stormalmanac.planner.TestGame.ORE_STAGE;
import static io.stormalmanac.planner.TestGame.RELIC;
import static io.stormalmanac.planner.TestGame.stack;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gamedata.Availability;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.gamedata.Goal;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Reward;
import io.stormalmanac.player.Goals;
import io.stormalmanac.player.Inventory;
import io.stormalmanac.player.PlayerProfile;
import io.stormalmanac.player.PlayerStateRepository;
import io.stormalmanac.player.Roster;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The port, end to end, against in-memory repositories.
 *
 * <p>The arithmetic is {@link EnergyMipTest}'s job. What is checked here is
 * everything the solver does not do: the version a plan is pinned to, the
 * fingerprint that makes it cacheable, the sentence that explains it, and the
 * marginal cost of one more of something.
 */
class MipOptimizerTest {

    private static final Instant NOW = Instant.parse("2026-09-07T12:00:00Z");
    private static final ProfileId PROFILE = new ProfileId("p1");

    /**
     * s-ore  · 10 energy · 2.0 ore per run
     * s-gold ·  5 energy · 100 gold per run
     * smelt  · 3 ore + 50 gold -> 1 ingot
     * hero's one upgrade costs 6 ingot, so the demand vector is exactly {ingot: 6}.
     */
    private static GameDefinition workshop() {
        return TestGame.builder()
                .stage(ORE_STAGE, 10, ORE, 2.0)
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .craft("smelt", List.of(stack(ORE, 3), stack(GOLD, 50)), List.of(stack(INGOT, 1)))
                .upgrade("i1", HERO, "insight-0", "insight-1", List.of(stack(INGOT, 6)))
                .build();
    }

    private static MipOptimizer optimizerOver(GameDefinition definition, Inventory inventory) {
        return new MipOptimizer(
                new TestRepositories.Definitions(definition),
                TestRepositories.Players.of(definition.game().id(), PROFILE, inventory, new Roster(PROFILE, Map.of())),
                null,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofSeconds(2));
    }

    private static SolveRequest request(GameDefinition definition, Objective objective) {
        return new SolveRequest(
                PROFILE,
                definition.version(),
                List.of(Goal.deterministic(HERO, "insight-1")),
                objective,
                60);
    }

    @Test
    @DisplayName("a plan carries the runs, the crafts, the energy and the version it was solved against")
    void solvesAndPinsTheVersion() {
        GameDefinition definition = workshop();
        // 6 ingot = 18 ore (9 runs, 90) + 300 gold (3 runs, 15) = 105 energy.
        Plan plan = optimizerOver(definition, Inventory.empty(PROFILE))
                .solve(request(definition, Objective.LEAST_ENERGY));

        assertThat(plan.totalEnergy()).isEqualTo(105);
        assertThat(plan.conversions()).containsExactly(new Conversion("smelt", 6));
        assertThat(plan.computedAgainst()).isEqualTo(definition.version());
        assertThat(plan.profile()).isEqualTo(PROFILE);
        assertThat(plan.computedAt()).isEqualTo(NOW);
        // 105 energy at 60 a day.
        assertThat(plan.etaDays()).isEqualTo(1.75);
    }

    @Test
    @DisplayName("the same question asked twice gets the same plan id, and a changed inventory does not")
    void planIdIsAFingerprintOfTheQuestion() {
        GameDefinition definition = workshop();
        SolveRequest request = request(definition, Objective.LEAST_ENERGY);

        Plan first = optimizerOver(definition, Inventory.empty(PROFILE)).solve(request);
        Plan again = optimizerOver(definition, Inventory.empty(PROFILE)).solve(request);
        Plan richer = optimizerOver(definition, Inventory.empty(PROFILE).with(ORE, 10)).solve(request);

        assertThat(first.id()).isEqualTo(again.id());
        assertThat(richer.id()).isNotEqualTo(first.id());
    }

    @Test
    @DisplayName("the shadow price is what one more actually costs, lumps and all")
    void shadowPriceIsTheIntegerMarginalCost() {
        GameDefinition definition = workshop();
        // 7 ingot needs 21 ore (11 runs, 110) and 350 gold (4 runs, 20) = 130,
        // against 105 for six. One more ingot costs 25, not the 17.5 an LP
        // relaxation would report.
        Plan plan = optimizerOver(definition, Inventory.empty(PROFILE))
                .solve(request(definition, Objective.LEAST_ENERGY));

        assertThat(plan.explanation().shadowPrice()).containsExactly(Map.entry(INGOT, 25.0));
    }

    @Test
    @DisplayName("the binding stages are the ones the plan actually runs, dearest first")
    void bindingStagesAreTheOnesRun() {
        GameDefinition definition = workshop();
        Plan plan = optimizerOver(definition, Inventory.empty(PROFILE))
                .solve(request(definition, Objective.LEAST_ENERGY));

        assertThat(plan.explanation().bindingStages()).containsExactly(ORE_STAGE, GOLD_STAGE);
    }

    @Test
    @DisplayName("a plan says its rates are declared rather than measured while nothing is measured")
    void saysWhereTheNumbersCameFrom() {
        GameDefinition definition = workshop();
        Plan plan = optimizerOver(definition, Inventory.empty(PROFILE))
                .solve(request(definition, Objective.LEAST_ENERGY));

        assertThat(plan.explanation().notes())
                .anySatisfy(note -> assertThat(note).contains("declared yield"))
                .anySatisfy(note -> assertThat(note).contains("i1"));
    }

    @Test
    @DisplayName("fewest days says why it came out the same, and blames the data rather than the model")
    void fewestDaysNamesTheReasonTheTwoObjectivesCoincide() {
        // This game declares no rewards and nothing that rotates, so a day buys
        // nothing but energy and the fastest plan is the cheapest one. That is
        // worth a sentence: "they came out the same" and "they are the same
        // question" are different claims, and only the first one is true here.
        GameDefinition definition = workshop();
        Plan energy = optimizerOver(definition, Inventory.empty(PROFILE))
                .solve(request(definition, Objective.LEAST_ENERGY));
        Plan days = optimizerOver(definition, Inventory.empty(PROFILE))
                .solve(request(definition, Objective.FEWEST_DAYS));

        assertThat(days.totalEnergy()).isEqualTo(energy.totalEnergy());
        assertThat(days.etaDays()).isEqualTo(energy.etaDays());
        assertThat(days.explanation().notes()).anySatisfy(note ->
                assertThat(note).contains("Nothing in this game's data accrues on a cadence"));
        assertThat(energy.explanation().notes()).noneSatisfy(note ->
                assertThat(note).contains("Nothing in this game's data accrues on a cadence"));
    }

    // -- The deadline a plan reports rather than schedules (ADR 0024) -------

    /** A day and a half after {@code NOW}: not even one weekly fits. */
    private static final Instant FESTIVAL_CLOSED = Instant.parse("2026-09-09T00:00:00Z");

    /** Three and a half days after {@code NOW}: three dailies fit, and no more. */
    private static final Instant FESTIVAL_CLOSES = Instant.parse("2026-09-11T00:00:00Z");

    /** The workshop, plus a festival handing out ingots until it shuts. */
    private static GameDefinition workshopWithFestival(Instant closesAt) {
        return TestGame.builder()
                .stage(ORE_STAGE, 10, ORE, 2.0)
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .craft("smelt", List.of(stack(ORE, 3), stack(GOLD, 50)), List.of(stack(INGOT, 1)))
                .source(new Reward("festival-daily", Reward.Cadence.DAILY,
                        List.of(new ItemStack(INGOT, 2)),
                        new Availability(Set.of(), null, closesAt)))
                .upgrade("i1", HERO, "insight-0", "insight-1", List.of(stack(INGOT, 6)))
                .build();
    }

    @Test
    @DisplayName("a plan leaning on a grant that is about to close says when it closes")
    void theDeadlineIsReported() {
        // The festival pays 2 ingots a day and shuts after three of them, which
        // is exactly the 6 the upgrade wants, for no energy at all. The reader
        // has three days to collect something the plan has already spent.
        GameDefinition definition = workshopWithFestival(FESTIVAL_CLOSES);
        Plan plan = optimizerOver(definition, Inventory.empty(PROFILE))
                .solve(request(definition, Objective.LEAST_ENERGY));

        assertThat(plan.totalEnergy()).isZero();
        assertThat(plan.rewardClaims()).containsExactly(new RewardClaim("festival-daily", 3));
        assertThat(plan.explanation().notes()).anySatisfy(note -> assertThat(note)
                .contains("On a deadline, and this plan is counting on them")
                .contains("festival-daily")
                .contains("2026-09-11T00:00:00Z")
                .contains("3 day(s) in"));
    }

    @Test
    @DisplayName("a grant that shut before the plan began is named, so the price is not a mystery")
    void theLapseIsReported() {
        // The same festival, closed a day and a half in: a weekly cadence gets
        // nothing out of it, the plan pays the full 105 energy, and before this
        // note the grant left no trace of having existed.
        GameDefinition definition = TestGame.builder()
                .stage(ORE_STAGE, 10, ORE, 2.0)
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .craft("smelt", List.of(stack(ORE, 3), stack(GOLD, 50)), List.of(stack(INGOT, 1)))
                .source(new Reward("festival-weekly", Reward.Cadence.WEEKLY,
                        List.of(new ItemStack(INGOT, 2)),
                        new Availability(Set.of(), null, FESTIVAL_CLOSED)))
                .upgrade("i1", HERO, "insight-0", "insight-1", List.of(stack(INGOT, 6)))
                .build();
        Plan plan = optimizerOver(definition, Inventory.empty(PROFILE))
                .solve(request(definition, Objective.LEAST_ENERGY));

        assertThat(plan.totalEnergy()).isEqualTo(105);
        assertThat(plan.rewardClaims()).isEmpty();
        assertThat(plan.explanation().notes()).anySatisfy(note -> assertThat(note)
                .contains("Closed too early to pay out once")
                .contains("festival-weekly")
                .contains("2026-09-09T00:00:00Z")
                .contains("would otherwise have allowed 4"));
    }

    @Test
    @DisplayName("fewest days says which way the horizon moves a deadline, and does not schedule it")
    void fewestDaysSaysWhatTheHorizonDoesToADeadline() {
        // Relics come only from an unhurried weekly, so three of them force a
        // 21-day horizon however fast everything else is -- and inside that
        // horizon the festival's own end date is what caps it at three claims.
        // The sentence has to be the true one: a longer plan collects no more of
        // it. ADR 0013 stands; nothing here is indexed by day.
        GameDefinition definition = TestGame.builder()
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .source(new Reward("festival-daily", Reward.Cadence.DAILY,
                        List.of(new ItemStack(INGOT, 2)),
                        new Availability(Set.of(), null, FESTIVAL_CLOSES)))
                .source(new Reward("slow-weekly", Reward.Cadence.WEEKLY,
                        List.of(new ItemStack(RELIC, 1)), Availability.ALWAYS))
                .upgrade("i1", HERO, "insight-0", "insight-1",
                        List.of(stack(INGOT, 6), stack(RELIC, 3)))
                .build();

        Plan days = optimizerOver(definition, Inventory.empty(PROFILE))
                .solve(request(definition, Objective.FEWEST_DAYS));
        Plan energy = optimizerOver(definition, Inventory.empty(PROFILE))
                .solve(request(definition, Objective.LEAST_ENERGY));

        assertThat(days.etaDays()).isEqualTo(21.0);
        assertThat(days.explanation().notes()).anySatisfy(note -> assertThat(note)
                .contains("Fewest days was asked for, and 1 of the grant(s) above close inside"
                        + " the 21-day horizon this search settled on")
                .contains("a longer plan collects no more of them"));
        // The deadline itself is reported either way; only the sentence about
        // what the horizon does to it belongs to this objective.
        assertThat(energy.explanation().notes())
                .anySatisfy(note -> assertThat(note).contains("On a deadline"))
                .noneSatisfy(note -> assertThat(note).contains("Fewest days was asked for"));
    }

    @Test
    @DisplayName("a plan bounded by a horizon it cannot fit into is refused, in days and energy")
    void theHorizonCanRefuseAPlan() {
        // 105 energy at 60 a day is two days' worth; one day is not enough.
        GameDefinition definition = workshop();
        SolveRequest tooShort = new SolveRequest(
                PROFILE, definition.version(), List.of(Goal.deterministic(HERO, "insight-1")),
                Objective.LEAST_ENERGY, 60, 1);

        assertThatThrownBy(() -> optimizerOver(definition, Inventory.empty(PROFILE)).solve(tooShort))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("within 1 day(s) at 60 energy a day");
    }

    @Test
    @DisplayName("two horizons are two questions, and the cache key knows it")
    void theHorizonIsPartOfTheFingerprint() {
        GameDefinition definition = workshop();
        MipOptimizer optimizer = optimizerOver(definition, Inventory.empty(PROFILE));
        List<Goal> goals = List.of(Goal.deterministic(HERO, "insight-1"));

        Plan thirty = optimizer.solve(new SolveRequest(
                PROFILE, definition.version(), goals, Objective.LEAST_ENERGY, 60, 30));
        Plan seven = optimizer.solve(new SolveRequest(
                PROFILE, definition.version(), goals, Objective.LEAST_ENERGY, 60, 7));

        assertThat(thirty.id()).isNotEqualTo(seven.id());
    }

    @Test
    @DisplayName("a goal already met produces an empty plan that explains itself")
    void nothingToDoIsAnAnswer() {
        GameDefinition definition = workshop();
        MipOptimizer optimizer = new MipOptimizer(
                new TestRepositories.Definitions(definition),
                TestRepositories.Players.of(definition.game().id(), PROFILE,
                        Inventory.empty(PROFILE), new Roster(PROFILE, Map.of(HERO, "insight-1"))),
                null,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofSeconds(2));

        Plan plan = optimizer.solve(request(definition, Objective.LEAST_ENERGY));

        assertThat(plan.totalEnergy()).isZero();
        assertThat(plan.stageRuns()).isEmpty();
        assertThat(plan.explanation().notes())
                .anySatisfy(note -> assertThat(note).contains("already satisfies every goal"));
    }

    @Test
    @DisplayName("a request naming another game's version is refused before anything is solved")
    void refusesAVersionFromAnotherGame() {
        GameDefinition definition = workshop();
        SolveRequest crossed = new SolveRequest(
                PROFILE,
                new GameDataVersion(GameId.of("some-other-game"), 0, "1.0", NOW, "test"),
                List.of(Goal.deterministic(HERO, "insight-1")),
                Objective.LEAST_ENERGY,
                60);

        assertThatThrownBy(() -> optimizerOver(definition, Inventory.empty(PROFILE)).solve(crossed))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("some-other-game");
    }
}
