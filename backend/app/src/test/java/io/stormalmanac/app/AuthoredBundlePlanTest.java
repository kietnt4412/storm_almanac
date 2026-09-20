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
import io.stormalmanac.planner.SolveRequest;
import io.stormalmanac.player.Inventory;
import io.stormalmanac.player.Roster;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Plans solved against the launch title's own published bundle, the first
 * plans this project has made from data it read first-hand.
 *
 * <p>{@code AuthoredBundlesTest} proves the bundle parses and is ours to
 * publish. This one proves it can be <em>planned from</em>. Until shops were
 * priced it could not, because every material in it is bought with a currency
 * that exactly one stage pays out. Every expected number is worked out from
 * the bundle's own rows in the comment beside it. So when the bundle changes,
 * the arithmetic has to be redone, not just the assertion updated. That is
 * the point: a correction to published data should show up in a plan.
 *
 * <p>What these plans leave out is as important as what they contain, and is
 * listed as N32 in {@code TRACKER.md}. Since sequence 1 a gate is paid and EXP
 * is fed, but only where the bundle has a price for them: level 80 is the one
 * level with one, so promote-13 carries the whole level and promote-12 carries
 * none of it, although in the game it needs level 75. A plan that looks
 * complete is not always complete, which is why the goals below were chosen
 * from the ones the bundle can express in full.
 */
class AuthoredBundlePlanTest {

    /** Relative to {@code backend/app}, which is where the test task runs. */
    private static final Path BUNDLE =
            Path.of("..", "..", "data", "bundles", "punishing-gray-raven-steering-by-light.json");

    private static final Instant NOW = Instant.parse("2026-09-19T06:00:00Z");
    private static final ProfileId PROFILE = new ProfileId("authored-bundle");
    private static final EntityId HELENTINE = new EntityId("helentine-lacrimosa");
    private static final EntityId SAMANTHA = new EntityId("samantha");
    private static final EntityId HEAR_THE_BELL = new EntityId("hear-the-bell");

    private final GameDefinition definition = load();

    @Test
    @DisplayName("a skill taken to its cap is two shop offers and one stage, and costs 150 Serum")
    void aSkillToItsCap() {
        // seeker-system 1 -> 18 is four rows: 1+2 000, 1+3 000, 1+4 000, then
        // 41+197 000, so 44 Skill Points and 206 000 Cogs.
        //   Skill Points: 15 for 69 Score, so 3 purchases (45) = 207 Score
        //   Cogs: 1 200 for 1 Score, 206 000 / 1 200 = 171.7, so 172 = 172 Score
        // 379 Score at 82 a run of Simulated Battlefield is 4.6, so 5 runs at 30
        // Serum = 150. No goal in the old model could say this, because both
        // materials are sold and nothing drops them.
        Plan plan = solve(Goal.deterministic(HELENTINE, "seeker-system-18"));

        assertThat(plan.totalEnergy()).isEqualTo(150);
        assertThat(plan.stageRuns()).singleElement().satisfies(run -> {
            assertThat(run.stage().value()).isEqualTo("simulated-battlefield");
            assertThat(run.runs()).isEqualTo(5);
        });
        assertThat(plan.conversions()).containsExactly(
                new Conversion("simulation-shop-cogs", 172),
                new Conversion("simulation-shop-skill-point", 3));
    }

    @Test
    @DisplayName("a Memory's Overclock goes stage, shop, box and craft, feeds its EXP, and costs 420 Serum")
    void aMemoryOverclock() {
        // samantha-overclock-1: 260 000 Cogs, 6 Major Overclock Alloy, 6 Memory
        // Overclock Circuit II, 7 Minor Overclock Alloy, 10 Memory Overclock
        // Circuit I, and since sequence 1 its 18 000 memory EXP.
        //   Cogs: 260 000 / 1 200 = 216.7, so 217 purchases = 217 Score
        //   beta box, ten open into 4 Major + 3 Core II + 3 Circuit II: 6 Major
        //     and 6 Circuit II both need 2 opens, so 20 boxes, 2 purchases at 150
        //     = 300 Score. The 6 Core II that come with them are surplus
        //   alpha box, ten open into 5 Minor + 5 Circuit I: 7 Minor and 10
        //     Circuit I both need 2 opens, so 2 purchases at 38 = 76 Score
        //   EXP: 18 000 / 300 per Memory Enhancer IV = 60 fed, bought 10 for 87,
        //     so 6 purchases = 522 Score
        // 1 115 Score at 82 a run is 13.6, so 14 runs = 420 Serum. It was 240
        // before the EXP had a row to live in, which is how much the old plan
        // was short: 43% of the goal.
        Plan plan = solve(Goal.deterministic(SAMANTHA, "overclock-1"));

        assertThat(plan.totalEnergy()).isEqualTo(420);
        assertThat(plan.conversions()).containsExactly(
                new Conversion("memory-exp-4-star", 60),
                new Conversion("open-overclock-material-box-alpha", 2),
                new Conversion("open-overclock-material-box-beta", 2),
                new Conversion("simulation-shop-cogs", 217),
                new Conversion("simulation-shop-memory-enhancer-iv", 6),
                new Conversion("simulation-shop-overclock-material-box-alpha", 2),
                new Conversion("simulation-shop-overclock-material-box-beta", 2));
    }

    @Test
    @DisplayName("her last rank is gated on level 80, so it costs the level's EXP too: 1 470 Serum")
    void aGatedRankPaysForItsGate() {
        // promote-1 .. promote-13: 542 500 Cogs, 453 purchases at 1 200 (452.1
        //   rounded up) = 453 Score
        // promote-13 requires level-80, and level-1 -> level-80 is 497 000
        //   character EXP. The only Pod the shop sells is L, 3 000 each, five
        //   for 103: 497 000 / 3 000 = 165.7, so 166 fed, and 166 / 5 = 33.2,
        //   so 34 purchases = 3 502 Score
        // 3 955 Score at 82 a run is 48.2, so 49 runs = 1 470 Serum. Without
        // the gate the same goal cost 6 runs, 180 Serum: the gate is 88% of it.
        Plan plan = solve(Goal.deterministic(HELENTINE, "promote-13"));

        assertThat(plan.totalEnergy()).isEqualTo(1470);
        assertThat(plan.conversions()).containsExactly(
                new Conversion("character-exp-pod-l", 166),
                new Conversion("simulation-shop-cogs", 453),
                new Conversion("simulation-shop-exp-pod-l", 34));
    }

    @Test
    @DisplayName("Pods the reader holds pay the gate, so only the Cogs are farmed: 180 Serum")
    void heldPodsPayTheGate() {
        // 25 EXP Pod (XL) at 20 000 is 500 000 EXP, past 497 000, so nothing is
        // bought for the level and the plan is the Cogs alone: 453 Score is 5.5
        // runs, so 6 runs = 180 Serum.
        Plan plan = solve(Goal.deterministic(HELENTINE, "promote-13"),
                Inventory.empty(PROFILE).with(new ItemId("exp-pod-xl"), 25));

        assertThat(plan.totalEnergy()).isEqualTo(180);
        assertThat(plan.conversions()).containsExactly(
                new Conversion("character-exp-pod-xl", 25),
                new Conversion("simulation-shop-cogs", 453));
    }

    @Test
    @DisplayName("Evolve to SS buys her shards cheapest first from a stock that never resets, and says it assumed none bought")
    void evolveBuysTheLifetimeStock() {
        // evolve-ss costs 30 Inver-Shards; she holds 2, so 28 are bought. The
        // first 10 are 10 Scars each and the next 20 are 20 each, so 10 + 18:
        // 100 + 360 = 460 Scars, exactly what the reader holds. Scars are not
        // farmed with Serum, so the plan costs none.
        Plan plan = solve(Goal.deterministic(HELENTINE, "evolve-ss"),
                Inventory.empty(PROFILE)
                        .with(new ItemId("inver-shard-lacrimosa"), 2)
                        .with(new ItemId("phantom-pain-scar"), 460));

        assertThat(plan.totalEnergy()).isZero();
        assertThat(plan.conversions()).containsExactly(
                new Conversion("phantom-pain-shop-inver-shard-lacrimosa", 18),
                new Conversion("phantom-pain-shop-inver-shard-lacrimosa-discounted", 10));
        assertThat(plan.explanation().notes()).anySatisfy(note -> assertThat(note)
                .startsWith("Buying from a limit that never resets")
                .contains("phantom-pain-shop-inver-shard-lacrimosa-discounted ×10")
                .contains("assumes none of that allowance has been bought yet"));
    }

    @Test
    @DisplayName("a reader who clears the Cage earns the Scars: 30 shards in nine weeks and no Serum")
    void evolveIsEarnedByClearingTheWeekly() {
        // 30 shards: the first 10 at 10 Scars and the next 20 at 20, so 500
        // Scars. A perfect Cage is 56 a week, and 500 / 56 = 8.9, so nine
        // weekly resets — 63 days — is the shortest horizon that can pay for it.
        // Nine weeks of all nine tiers is 504, four more than the shards cost,
        // so the plan skips the bottom tier once: 9 x 56 - 4 = 500, exactly.
        // That is the tie-break doing its job — a plan says it leans on the
        // grants it needs and not on every grant that exists. No stage pays
        // Scars, so no Serum is spent: the whole plan is waiting, which is
        // exactly what the mode is.
        Plan plan = solve(Goal.deterministic(HELENTINE, "evolve-ss"), Inventory.empty(PROFILE),
                63, Map.of("phantom-pain-cage-score", 1_100_000));

        assertThat(plan.totalEnergy()).isZero();
        assertThat(plan.conversions()).containsExactly(
                new Conversion("phantom-pain-shop-inver-shard-lacrimosa", 20),
                new Conversion("phantom-pain-shop-inver-shard-lacrimosa-discounted", 10));
        assertThat(plan.rewardClaims()).hasSize(9);
        assertThat(scarsClaimedBy(plan))
                .as("the Scars claimed are the Scars spent, to the one")
                .isEqualTo(500);
        assertThat(plan.explanation().notes()).anySatisfy(note -> assertThat(note)
                .startsWith("Counting on free income over the horizon")
                .contains("phantom-pain-cage-1100000 ×9"));
    }

    /** What the tiers the plan claims pay in Scars, read back off the bundle. */
    private int scarsClaimedBy(Plan plan) {
        Map<String, Integer> perClaim = definition.rewards().stream().collect(java.util.stream.Collectors
                .toMap(reward -> reward.id(), reward -> reward.grants().stream()
                        .filter(grant -> grant.item().equals(new ItemId("phantom-pain-scar")))
                        .mapToInt(grant -> grant.quantity()).sum()));
        return plan.rewardClaims().stream()
                .mapToInt(claim -> claim.times() * perClaim.getOrDefault(claim.reward(), 0))
                .sum();
    }

    @Test
    @DisplayName("a reader who clears only the first tier is eight weeks short, and the plan is refused")
    void clearingOneTierIsNotEnough() {
        // 30 000 is the bottom tier alone: 4 Scars a week, 36 over nine weeks
        // against the 500 the shards cost. The eight tiers above pay the same
        // item and are not counted, which is why the refusal says one reward and
        // not nine: the model is the game this reader plays, not the one the
        // bundle describes.
        assertThatThrownBy(() -> solve(Goal.deterministic(HELENTINE, "evolve-ss"),
                Inventory.empty(PROFILE), 63, Map.of("phantom-pain-cage-score", 30_000)))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("1 reward(s)");
    }

    @Test
    @DisplayName("saying nothing about the Cage, the refusal names the bar and what it was told")
    void silenceAboutTheCageIsRefusedByName() {
        // The reader holds no Scars and has not said what they clear, so the
        // only source of the currency is out of reach rather than absent — and
        // that is the difference between "this game has no route" and "tell me
        // what you clear". The refusal follows the currency one level down and
        // names the lowest bar of the nine, which is the least this reader
        // would have to say to be given a plan at all.
        assertThatThrownBy(() -> solve(Goal.deterministic(HELENTINE, "evolve-ss")))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("phantom-pain-scar")
                .hasMessageContaining("phantom-pain-cage-30000")
                .hasMessageContaining("phantom-pain-cage-score")
                .hasMessageContaining("reaching 0")
                .hasMessageContaining("8 more behind a higher score");
    }

    @Test
    @DisplayName("one Scar short and Evolve is refused, and the refusal cannot say which item ran out")
    void evolveOneScarShortIsRefused() {
        // 459 Scars buy the cheap ten and 17 of the rest; the 28th shard needs
        // 20 more, and this reader has not said they clear a Cage tier, so
        // nothing they can collect pays Scars. The refusal is the generic one:
        // the Scar has a source, the reader's own stock, so the item-by-item
        // diagnosis finds nothing missing, and a stock that runs out is a
        // quantity the MIP reports only as infeasible.
        assertThatThrownBy(() -> solve(Goal.deterministic(HELENTINE, "evolve-ss"),
                Inventory.empty(PROFILE)
                        .with(new ItemId("inver-shard-lacrimosa"), 2)
                        .with(new ItemId("phantom-pain-scar"), 459)))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("2 shop offer(s)")
                .hasMessageNotContaining("phantom-pain-scar");
    }

    @Test
    @DisplayName("Resonance holding nothing pays the one price that can be farmed: 246 Score, 90 Serum")
    void resonanceFarmsTheScorePrice() {
        // Three prices, and only Simulation Score has a source in the bundle.
        // 246 Score at 82 a run is exactly 3 runs = 90 Serum.
        Plan plan = solve(Goal.deterministic(SAMANTHA, "upper-resonance-1"));

        assertThat(plan.totalEnergy()).isEqualTo(90);
        assertThat(plan.conversions()).containsExactly(
                new Conversion("samantha-upper-resonance-by-simulation-score", 1));
    }

    @Test
    @DisplayName("Resonance holding 150 Memory Shards pays in shards, and farms nothing")
    void resonancePaysAHeldPrice() {
        Plan plan = solve(Goal.deterministic(SAMANTHA, "upper-resonance-1"),
                Inventory.empty(PROFILE).with(new ItemId("memory-shard-5-star"), 150));

        assertThat(plan.totalEnergy()).isZero();
        assertThat(plan.conversions()).containsExactly(
                new Conversion("samantha-upper-resonance-by-memory-shard", 1));
    }

    @Test
    @DisplayName("a weapon Overclock is refused, naming the one material nothing read supplies")
    void theWeaponsGapIsNamed() {
        // Hear the Bell wants 28 Weapon Overclock Core I, and the alpha box that
        // looked as though it should give some turned out to give Circuit I
        // instead. The bundle says so in a comment; the plan has to say so too.
        assertThatThrownBy(() -> solve(Goal.deterministic(HEAR_THE_BELL, "overclock-1")))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("weapon-overclock-core-i")
                .hasMessageContaining("no source in this game version yields it");
    }

    private Plan solve(Goal goal) {
        return solve(goal, Inventory.empty(PROFILE));
    }

    private Plan solve(Goal goal, Inventory inventory) {
        return solve(goal, inventory, 30, Map.of());
    }

    private Plan solve(Goal goal, Inventory inventory, int horizonDays, Map<String, Integer> reach) {
        MipOptimizer optimizer = new MipOptimizer(
                new InMemoryPlanning.OneVersion(definition),
                new InMemoryPlanning.FixedPlayer(
                        PROFILE, definition.game().id(), inventory,
                        new Roster(PROFILE, Map.of())),
                null,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofSeconds(2));

        // 240 Serum a day over thirty days is far more than any goal here
        // spends, so neither binds and each total is the goal's own price. The
        // horizon is a parameter only because a weekly grant is paid in weeks,
        // and nine of them do not fit in a month.
        return optimizer.solve(new SolveRequest(
                PROFILE, definition.version(), List.of(goal), Objective.LEAST_ENERGY, 240,
                horizonDays, reach));
    }

    private static GameDefinition load() {
        try (InputStream in = Files.newInputStream(BUNDLE)) {
            return new CanonicalBundleParser().parse(in).definitionApprovedAt(Instant.EPOCH);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "could not read " + BUNDLE.toAbsolutePath().normalize()
                            + ". This test resolves paths relative to backend/app.", e);
        }
    }
}
