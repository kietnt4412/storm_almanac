package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.Goal;
import io.stormalmanac.gamedata.ingest.CanonicalBundleParser;
import io.stormalmanac.planner.Conversion;
import io.stormalmanac.planner.MipOptimizer;
import io.stormalmanac.planner.Objective;
import io.stormalmanac.planner.Optimizer;
import io.stormalmanac.planner.Plan;
import io.stormalmanac.planner.RewardClaim;
import io.stormalmanac.planner.SolveRequest;
import io.stormalmanac.player.Inventory;
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
import java.util.Set;
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
    @DisplayName("over a month the cheapest way to Insight 1 is to farm nothing and collect the free income")
    void solvesTheFixturesFirstInsight() {
        // warden-insight-1 costs 4 sigil-lesser and 5 000 gold, and over a
        // thirty-day horizon the fixture hands both of them out for nothing:
        //   weekly-quest fires 30 / 7 = 4 times, granting 8 sigil-lesser and
        //     4 000 gold — the sigils alone cover the goal twice over
        //   daily-login fires 30 times at 300 gold, and four of them close the
        //     last 1 000 of the gold
        // Farming any of it would cost energy the plan does not have to spend, so
        // the answer is a calendar rather than a grind. This is the whole
        // difference the time axis makes, and it is why LEAST_ENERGY needs a
        // horizon: without one, "wait" is free and unbounded.
        Plan plan = solve(goal(WARDEN, "insight-1"), Inventory.empty(PROFILE), Map.of());

        assertThat(plan.totalEnergy()).isZero();
        assertThat(plan.stageRuns()).isEmpty();
        assertThat(plan.rewardClaims()).containsExactlyInAnyOrder(
                new RewardClaim("weekly-quest", 4), new RewardClaim("daily-login", 4));
        // Four weekly quests take four weeks however much energy is spare.
        assertThat(plan.etaDays()).isEqualTo(28.0);
        assertThat(plan.computedAgainst().label()).isEqualTo("1.0");
    }

    @Test
    @DisplayName("fewest days buys the calendar back with energy, and least energy sells it")
    void theTwoObjectivesAreNowDifferentPlans() {
        // The same goal, asked the other way. NOW is a Monday, so a two-day
        // horizon contains exactly one Tuesday and pg-2-3 is open on it:
        //   4 sigil-lesser at 0.45 a run is 9 runs at 20 = 180 energy, and one
        //     Tuesday supplies 240 — it fits, and one day would not, because a
        //     one-day horizon contains no Tuesday and no Friday at all
        //   5 000 gold less the two daily logins is 4 400, at 240 a run that is
        //     19 runs of pg-1-1 at 10 = 190
        // 370 energy for two days against nothing at all for twenty-eight. Those
        // are different plans in the way the phase-2 note promised and the old
        // model could not deliver: the trade is real and a player can see it.
        Plan fastest = solve(goal(WARDEN, "insight-1"), Inventory.empty(PROFILE), Map.of(),
                Objective.FEWEST_DAYS, 30);

        assertThat(fastest.totalEnergy()).isEqualTo(370);
        assertThat(fastest.etaDays()).isEqualTo(2.0);
        assertThat(fastest.explanation().notes()).anySatisfy(note ->
                assertThat(note).contains("shortest horizon this goal set fits into is 2 day(s)"));
    }

    @Test
    @DisplayName("a rotating stage cannot be farmed on a day it is shut, however much energy there is")
    void rotationBindsEvenWithEnergyToSpare() {
        // A one-day horizon starting on a Monday reaches no Tuesday and no
        // Friday, so pg-2-3 has no open day to be run on and the only source of
        // sigil-lesser inside a day is the weekly quest, which does not come
        // round either. 240 energy is plenty and buys nothing.
        assertThatThrownBy(() -> solve(
                goal(WARDEN, "insight-1"), Inventory.empty(PROFILE), Map.of(),
                Objective.LEAST_ENERGY, 1))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("within 1 day(s)");
    }

    @Test
    @DisplayName("crafting the refined ore beats farming it, and the solver finds that without being told")
    void prefersTheCraftWhenTheCraftIsCheaper() {
        // amulet-level-30 costs 4 ore-refined and 8 000 gold. Two routes:
        //   farm the ore at pg-2-3, 0.2 a run: 20 runs at 20 = 400.
        //   craft it: 4 refine-ore consume 12 ore-rough and 400 gold, and
        //     ore-rough comes only from pg-1-1 at 1.4 a run, so 12 / 1.4 = 8.6
        //     -> 9 runs at 10 = 90.
        // The gold is free over the horizon — 4 weekly quests and 8 daily logins
        // cover the 8 400 the goal and the craft want between them, less the
        // 2 160 those 9 runs bring in — so what is left to price is the ore, and
        // the craft wins on it. Nothing in the model knows the second route
        // exists until the solver prices it: the craft is a variable and the ore
        // is a constraint row.
        Plan plan = solve(goal(AMULET, "level-30"), Inventory.empty(PROFILE), Map.of());

        assertThat(plan.totalEnergy()).isEqualTo(90);
        assertThat(plan.conversions()).containsExactly(new Conversion("refine-ore", 4));
        assertThat(plan.stageRuns()).singleElement()
                .satisfies(run -> assertThat(run.stage().value()).isEqualTo("pg-1-1"));
        assertThat(plan.explanation().notes()).anySatisfy(note ->
                assertThat(note).contains("Counting on free income"));
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
    @DisplayName("an item only a shop sells is bought, and the currency to buy it is farmed")
    void theShopIsASource() {
        // warden-insight-2 costs 6 sigil-greater, 8 ore-refined and 20 000 gold,
        // and the only source of a greater sigil is the weekly shop: 500 gold
        // each, 3 a week, so 12 in thirty days and the limit does not bind.
        //   6 purchases       3 000 gold
        //   8 refine-ore        800 gold and 24 ore-rough
        //   the goal itself  20 000 gold
        // 23 800 gold, of which the horizon gives away 13 000 — thirty daily
        // logins at 300 and four weekly quests at 1 000. The other 10 800 is
        // exactly 45 runs of pg-1-1 at 240, and those 45 runs drop 63 ore-rough
        // against the 24 wanted, so nothing else is farmed: 450 energy.
        //
        // Until shops were priced this goal was refused, naming the shop. The
        // sigils cost nothing in energy themselves; what they cost is the gold
        // row, and the solver finds that through the currency.
        Plan plan = solve(
                goal(WARDEN, "insight-2"),
                Inventory.empty(PROFILE),
                Map.of(WARDEN, Set.of("insight-1")));

        assertThat(plan.totalEnergy()).isEqualTo(450);
        assertThat(plan.conversions()).containsExactly(
                new Conversion("refine-ore", 8), new Conversion("weekly-sigil", 6));
        assertThat(plan.stageRuns()).singleElement()
                .satisfies(run -> assertThat(run.stage().value()).isEqualTo("pg-1-1"));
        assertThat(plan.explanation().notes()).anySatisfy(note ->
                assertThat(note).contains("1 shop offer(s)"));
    }

    @Test
    @DisplayName("a shop's weekly limit is a real bound: a horizon with no whole week buys nothing")
    void theShopLimitBinds() {
        // Six days holds no whole week, and nothing says how much of this week's
        // allowance the player has already spent, so the shop is not a source
        // inside it — and the refusal says which shop and why.
        assertThatThrownBy(() -> solve(
                goal(WARDEN, "insight-2"),
                Inventory.empty(PROFILE),
                Map.of(WARDEN, Set.of("insight-1")),
                Objective.LEAST_ENERGY, 6))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("sigil-greater")
                .hasMessageContaining("weekly-sigil")
                .hasMessageContaining("does not reset inside a 6-day horizon");
    }

    @Test
    @DisplayName("a plan explains itself: the steps it pays for, and what one more of each item costs")
    void theExplanationIsUsable() {
        Plan plan = solve(goal(AMULET, "level-30"), Inventory.empty(PROFILE), Map.of());

        assertThat(plan.explanation().notes())
                // Named by the entity and the state it reaches, not by the
                // step's id, since 2026-09-26.
                .anySatisfy(note -> assertThat(note).contains("Paying for 1 upgrade step(s): Ember Amulet to level-30"));
        // One more refined ore is one more craft, so 3 more ore-rough. Nine runs
        // of pg-1-1 yield 12.6 and the five crafts want 15, so it is two more
        // runs — the lump, not the average.
        assertThat(plan.explanation().shadowPrice())
                .containsEntry(ItemId.of("ore-refined"), 20.0);
        // Gold is free at this horizon: the rewards have room for one more.
        assertThat(plan.explanation().shadowPrice())
                .containsEntry(ItemId.of("gold"), 0.0);
        assertThat(plan.explanation().bindingStages()).extracting(id -> id.value())
                .containsExactly("pg-1-1");
    }

    // ── plumbing ────────────────────────────────────────────────────────────

    private Plan solve(Goal goal, Inventory inventory, Map<EntityId, Set<String>> roster) {
        return solve(goal, inventory, roster, Objective.LEAST_ENERGY, 30);
    }

    private Plan solve(
            Goal goal,
            Inventory inventory,
            Map<EntityId, Set<String>> roster,
            Objective objective,
            int horizonDays) {

        MipOptimizer optimizer = new MipOptimizer(
                new InMemoryPlanning.OneVersion(provingGround),
                new InMemoryPlanning.FixedPlayer(
                        PROFILE, provingGround.game().id(), inventory, new Roster(PROFILE, roster)),
                null,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofSeconds(2));

        return optimizer.solve(new SolveRequest(
                PROFILE, provingGround.version(), List.of(goal), objective, 240, horizonDays));
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
}
