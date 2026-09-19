package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.common.id.EntityId;
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
 * listed as N32 in {@code TRACKER.md}. Nothing here checks a level gate. A goal
 * whose cost is partly EXP gets the materials half of that cost and not the EXP
 * half. A plan that looks complete is not always complete. That is why the
 * goals below were chosen from the ones the bundle can express in full.
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
    @DisplayName("a Memory's Overclock goes stage, shop, box and craft, and costs 240 Serum")
    void aMemoryOverclock() {
        // samantha-overclock-1: 260 000 Cogs, 6 Major Overclock Alloy, 6 Memory
        // Overclock Circuit II, 7 Minor Overclock Alloy, 10 Memory Overclock
        // Circuit I.
        //   Cogs: 260 000 / 1 200 = 216.7, so 217 purchases = 217 Score
        //   beta box, ten open into 4 Major + 3 Core II + 3 Circuit II: 6 Major
        //     and 6 Circuit II both need 2 opens, so 20 boxes, 2 purchases at 150
        //     = 300 Score. The 6 Core II that come with them are surplus
        //   alpha box, ten open into 5 Minor + 5 Circuit I: 7 Minor and 10
        //     Circuit I both need 2 opens, so 2 purchases at 38 = 76 Score
        // 593 Score at 82 a run is 7.2, so 8 runs = 240 Serum. Four layers deep,
        // and the solver walks all of them without being told the order.
        Plan plan = solve(Goal.deterministic(SAMANTHA, "overclock-1"));

        assertThat(plan.totalEnergy()).isEqualTo(240);
        assertThat(plan.conversions()).containsExactly(
                new Conversion("open-overclock-material-box-alpha", 2),
                new Conversion("open-overclock-material-box-beta", 2),
                new Conversion("simulation-shop-cogs", 217),
                new Conversion("simulation-shop-overclock-material-box-alpha", 2),
                new Conversion("simulation-shop-overclock-material-box-beta", 2));
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
        MipOptimizer optimizer = new MipOptimizer(
                new InMemoryPlanning.OneVersion(definition),
                new InMemoryPlanning.FixedPlayer(
                        PROFILE, definition.game().id(), Inventory.empty(PROFILE),
                        new Roster(PROFILE, Map.of())),
                null,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofSeconds(2));

        // 240 Serum a day over thirty days is far more than any goal here
        // spends, so neither binds and each total is the goal's own price.
        return optimizer.solve(new SolveRequest(
                PROFILE, definition.version(), List.of(goal), Objective.LEAST_ENERGY, 240, 30));
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
