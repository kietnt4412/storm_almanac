package io.stormalmanac.planner;

import static io.stormalmanac.planner.TestGame.GOLD;
import static io.stormalmanac.planner.TestGame.GOLD_STAGE;
import static io.stormalmanac.planner.TestGame.HERO;
import static io.stormalmanac.planner.TestGame.INGOT;
import static io.stormalmanac.planner.TestGame.ORE;
import static io.stormalmanac.planner.TestGame.ORE_STAGE;
import static io.stormalmanac.planner.TestGame.stack;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.gamedata.Goal;
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
                new FakeDefinitions(definition),
                new FakePlayers(definition.game().id(), inventory, new Roster(PROFILE, Map.of())),
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
    @DisplayName("fewest days says out loud that it is the same plan as least energy for now")
    void fewestDaysIsHonestAboutBeingTheSameModel() {
        GameDefinition definition = workshop();
        Plan energy = optimizerOver(definition, Inventory.empty(PROFILE))
                .solve(request(definition, Objective.LEAST_ENERGY));
        Plan days = optimizerOver(definition, Inventory.empty(PROFILE))
                .solve(request(definition, Objective.FEWEST_DAYS));

        assertThat(days.totalEnergy()).isEqualTo(energy.totalEnergy());
        assertThat(days.explanation().notes())
                .anySatisfy(note -> assertThat(note).contains("same plan under this model"));
        assertThat(energy.explanation().notes())
                .noneSatisfy(note -> assertThat(note).contains("same plan under this model"));
    }

    @Test
    @DisplayName("a goal already met produces an empty plan that explains itself")
    void nothingToDoIsAnAnswer() {
        GameDefinition definition = workshop();
        MipOptimizer optimizer = new MipOptimizer(
                new FakeDefinitions(definition),
                new FakePlayers(definition.game().id(), Inventory.empty(PROFILE),
                        new Roster(PROFILE, Map.of(HERO, "insight-1"))),
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

    // ── fakes ───────────────────────────────────────────────────────────────

    private record FakeDefinitions(GameDefinition definition) implements GameDefinitionRepository {
        @Override
        public Optional<GameDefinition> findLatest(GameId game) {
            return find(game, definition.version().sequence());
        }

        @Override
        public Optional<GameDefinition> find(GameId game, long sequence) {
            return game.equals(definition.game().id()) && sequence == definition.version().sequence()
                    ? Optional.of(definition)
                    : Optional.empty();
        }

        @Override
        public List<GameDataVersion> versions(GameId game) {
            return List.of(definition.version());
        }
    }

    private record FakePlayers(GameId game, Inventory inventory, Roster roster)
            implements PlayerStateRepository {

        @Override
        public List<PlayerProfile> profilesOf(AccountId account) {
            return List.of(profile());
        }

        @Override
        public Optional<PlayerProfile> findProfile(ProfileId id) {
            return id.equals(PROFILE) ? Optional.of(profile()) : Optional.empty();
        }

        private PlayerProfile profile() {
            return new PlayerProfile(PROFILE, new AccountId("a1"), game, "Tester", "global");
        }

        @Override
        public Inventory inventoryOf(ProfileId profile) {
            return inventory;
        }

        @Override
        public Roster rosterOf(ProfileId profile) {
            return roster;
        }

        @Override
        public Goals goalsOf(ProfileId profile) {
            return new Goals(profile, List.of());
        }

        @Override
        public void saveInventory(Inventory value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void saveRoster(Roster value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void saveGoals(Goals value) {
            throw new UnsupportedOperationException();
        }
    }
}
