package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.gamedata.Goal;
import io.stormalmanac.gamedata.ingest.CanonicalBundleParser;
import io.stormalmanac.planner.Conversion;
import io.stormalmanac.planner.MipOptimizer;
import io.stormalmanac.planner.Objective;
import io.stormalmanac.planner.Optimizer;
import io.stormalmanac.planner.Plan;
import io.stormalmanac.planner.SolveRequest;
import io.stormalmanac.player.Goals;
import io.stormalmanac.player.Inventory;
import io.stormalmanac.player.PlayerProfile;
import io.stormalmanac.player.PlayerStateRepository;
import io.stormalmanac.player.Roster;
import java.io.IOException;
import java.io.InputStream;
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
 * The optimizer against a bundle that came off disk, rather than one built in
 * code.
 *
 * <p>{@code EnergyMipTest} proves the arithmetic on a model small enough to
 * check by hand. This proves the same solver reaches the right answer on the
 * acceptance fixture: parsed from the canonical format, carrying all four source
 * kinds, a rotating stage and an expiring event, with the numbers nobody chose
 * to make the sums come out round.
 *
 * <p>No database. The solver reads a {@link GameDefinition} and a
 * {@link PlayerStateRepository}; where those came from is not its business, and
 * keeping this test connectionless is what makes it the cheap second check on
 * every change to the model.
 */
class PlannerAcceptanceTest {

    /** Inside the fixture's event window, so nothing is filtered for being expired. */
    private static final Instant NOW = Instant.parse("2026-09-07T12:00:00Z");

    private static final ProfileId PROFILE = new ProfileId("acceptance");
    private static final EntityId WARDEN = new EntityId("warden");
    private static final EntityId AMULET = new EntityId("amulet-ember");

    private final GameDefinition provingGround = load();

    @Test
    @DisplayName("the cheapest way to Insight 1 is nine runs of the sigil stage and twenty-one of the gold one")
    void solvesTheFixturesFirstInsight() {
        // warden-insight-1 costs 4 sigil-lesser and 5 000 gold.
        //   sigil-lesser drops only at pg-2-3, 0.45 a run: 4 / 0.45 = 8.9 -> 9 runs at 20 = 180
        //   gold drops only at pg-1-1, 240 a run:      5 000 / 240 = 20.8 -> 21 runs at 10 = 210
        Plan plan = solve(goal(WARDEN, "insight-1"), Inventory.empty(PROFILE), Map.of());

        assertThat(plan.stageRuns()).extracting(run -> run.stage().value(), run -> run.runs())
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("pg-2-3", 9),
                        org.assertj.core.groups.Tuple.tuple("pg-1-1", 21));
        assertThat(plan.totalEnergy()).isEqualTo(390);
        assertThat(plan.computedAgainst().label()).isEqualTo("1.0");
    }

    @Test
    @DisplayName("crafting the refined ore beats farming it, and the solver finds that without being told")
    void prefersTheCraftWhenTheCraftIsCheaper() {
        // amulet-level-30 costs 4 ore-refined and 8 000 gold. Two routes:
        //   farm it at pg-2-3, 0.2 a run: 20 runs at 20 = 400, plus 34 runs of
        //     pg-1-1 for the gold = 340. Total 740.
        //   craft it: 4 refine-ore consume 12 ore-rough and 400 gold, so pg-1-1
        //     must cover 8 400 gold = 35 runs at 10 = 350, and those 35 runs also
        //     yield 49 ore-rough, comfortably past the 12 needed. Total 350.
        // Nothing in the model knows the second route exists until the solver
        // prices it: the craft is a variable and the ore is a constraint row.
        Plan plan = solve(goal(AMULET, "level-30"), Inventory.empty(PROFILE), Map.of());

        assertThat(plan.totalEnergy()).isEqualTo(350);
        assertThat(plan.conversions()).containsExactly(new Conversion("refine-ore", 4));
        assertThat(plan.stageRuns()).singleElement()
                .satisfies(run -> assertThat(run.stage().value()).isEqualTo("pg-1-1"));
    }

    @Test
    @DisplayName("what the player already owns comes off the top")
    void inventoryChangesThePlan() {
        Plan plan = solve(
                goal(WARDEN, "insight-1"),
                Inventory.empty(PROFILE).with(ItemId.of("sigil-lesser"), 4).with(ItemId.of("gold"), 5000),
                Map.of());

        assertThat(plan.totalEnergy()).isZero();
        assertThat(plan.stageRuns()).isEmpty();
    }

    @Test
    @DisplayName("a goal whose only source is the shop is refused by name, because shops are not priced yet")
    void theShopGapIsSaidOutLoud() {
        // warden-insight-2 costs 6 sigil-greater, and the only source of one in
        // this bundle is the weekly shop. The model has no time axis and so no
        // honest place for a per-period cap; an uncapped shop would let the
        // solver buy its way out of the constraint. Until then this is a refusal
        // that names the gap, not a plan that pretends the item is free.
        assertThatThrownBy(() -> solve(
                goal(WARDEN, "insight-2"),
                Inventory.empty(PROFILE),
                Map.of(WARDEN, "insight-1")))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("sigil-greater")
                .hasMessageContaining("weekly-sigil");
    }

    @Test
    @DisplayName("a plan explains itself: the steps it pays for, and what one more of each item costs")
    void theExplanationIsUsable() {
        Plan plan = solve(goal(AMULET, "level-30"), Inventory.empty(PROFILE), Map.of());

        assertThat(plan.explanation().notes())
                .anySatisfy(note -> assertThat(note).contains("amulet-level-30"));
        // One more refined ore is one more craft: 3 ore-rough (already spare) and
        // 100 gold, which the 35 runs do not have room for, so it is one more run.
        assertThat(plan.explanation().shadowPrice())
                .containsEntry(ItemId.of("ore-refined"), 10.0);
        assertThat(plan.explanation().bindingStages()).extracting(id -> id.value())
                .containsExactly("pg-1-1");
    }

    // ── plumbing ────────────────────────────────────────────────────────────

    private Plan solve(Goal goal, Inventory inventory, Map<EntityId, String> roster) {
        MipOptimizer optimizer = new MipOptimizer(
                new OneVersion(provingGround),
                new FixedPlayer(provingGround.game().id(), inventory, new Roster(PROFILE, roster)),
                null,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofSeconds(2));

        return optimizer.solve(new SolveRequest(
                PROFILE, provingGround.version(), List.of(goal), Objective.LEAST_ENERGY, 240));
    }

    private static Goal goal(EntityId entity, String state) {
        return Goal.deterministic(entity, state);
    }

    private static GameDefinition load() {
        String fixture = "/gamedata/proving-ground-1.0.json";
        try (InputStream in = PlannerAcceptanceTest.class.getResourceAsStream(fixture)) {
            if (in == null) throw new IllegalStateException("fixture not on the classpath: " + fixture);
            return new CanonicalBundleParser().parse(in).definitionApprovedAt(Instant.EPOCH);
        } catch (IOException e) {
            throw new IllegalStateException("could not read " + fixture, e);
        }
    }

    private record OneVersion(GameDefinition definition) implements GameDefinitionRepository {
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

    private record FixedPlayer(GameId game, Inventory inventory, Roster roster)
            implements PlayerStateRepository {

        @Override
        public List<PlayerProfile> profilesOf(AccountId account) {
            return List.of(profile());
        }

        @Override
        public Optional<PlayerProfile> findProfile(ProfileId id) {
            return Optional.of(profile());
        }

        private PlayerProfile profile() {
            return new PlayerProfile(PROFILE, new AccountId("acceptance"), game, "Tester", "global");
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
