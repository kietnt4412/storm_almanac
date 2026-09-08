package io.stormalmanac.planner;

import static io.stormalmanac.planner.TestGame.GOLD;
import static io.stormalmanac.planner.TestGame.GOLD_STAGE;
import static io.stormalmanac.planner.TestGame.HERO;
import static io.stormalmanac.planner.TestGame.INGOT;
import static io.stormalmanac.planner.TestGame.ORE;
import static io.stormalmanac.planner.TestGame.ORE_STAGE;
import static io.stormalmanac.planner.TestGame.stack;
import static org.assertj.core.api.Assertions.assertThat;

import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.Goal;
import io.stormalmanac.player.Inventory;
import io.stormalmanac.player.Roster;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The cache, seen through the port that uses it.
 *
 * <p>The hard part of testing a cache is proving that a hit did <em>not</em> do
 * the work, and this fixture solves far too fast for a stopwatch to say so. What
 * says so instead is the clock: {@code computedAt} is stamped at solve time, so a
 * plan bearing an earlier instant than the clock now reads is a plan that was not
 * computed on this call. That is an assertion about the answer itself rather than
 * about how long it took to arrive.
 */
class SolveCachingTest {

    private static final Instant FIRST = Instant.parse("2026-09-08T12:00:00Z");
    private static final ProfileId ALICE = new ProfileId("p-alice");
    private static final ProfileId BOB = new ProfileId("p-bob");

    /** The same workshop {@link MipOptimizerTest} uses: 6 ingot costs 105 energy. */
    private static GameDefinition workshop() {
        return TestGame.builder()
                .stage(ORE_STAGE, 10, ORE, 2.0)
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .craft("smelt", List.of(stack(ORE, 3), stack(GOLD, 50)), List.of(stack(INGOT, 1)))
                .upgrade("i1", HERO, "insight-0", "insight-1", List.of(stack(INGOT, 6)))
                .build();
    }

    /** A clock the test moves by hand, so "was this recomputed" has an answer. */
    private static final class MovableClock extends Clock {
        private Instant now;

        MovableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration by) {
            now = now.plus(by);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }

    private static SolveRequest requestFor(ProfileId profile, GameDefinition definition) {
        return new SolveRequest(
                profile,
                definition.version(),
                List.of(Goal.deterministic(HERO, "insight-1")),
                Objective.LEAST_ENERGY,
                60);
    }

    private static MipOptimizer optimizer(
            GameDefinition definition,
            TestRepositories.Players players,
            SolveCache cache,
            Clock clock) {
        return new MipOptimizer(
                new TestRepositories.Definitions(definition),
                players,
                null,
                clock,
                Duration.ofSeconds(2),
                cache);
    }

    private static TestRepositories.Players justAlice(GameDefinition definition) {
        return TestRepositories.Players.of(
                definition.game().id(), ALICE, Inventory.empty(ALICE), new Roster(ALICE, Map.of()));
    }

    @Test
    @DisplayName("the same question asked twice is solved once, and the second answer says so")
    void aSecondAskIsServedFromTheCache() {
        GameDefinition definition = workshop();
        InProcessSolveCache cache = new InProcessSolveCache();
        MovableClock clock = new MovableClock(FIRST);
        MipOptimizer optimizer = optimizer(definition, justAlice(definition), cache, clock);

        Plan first = optimizer.solve(requestFor(ALICE, definition));
        clock.advance(Duration.ofMinutes(30));
        Plan second = optimizer.solve(requestFor(ALICE, definition));

        // The same answer, stamped with when it was computed rather than with
        // when it was handed over.
        assertThat(second.id()).isEqualTo(first.id());
        assertThat(second.totalEnergy()).isEqualTo(first.totalEnergy()).isEqualTo(105);
        assertThat(second.stageRuns()).isEqualTo(first.stageRuns());
        assertThat(second.computedAt()).isEqualTo(FIRST);

        assertThat(second.explanation().notes())
                .anySatisfy(note -> assertThat(note).contains("Served from cache", "30m ago"));
        assertThat(first.explanation().notes())
                .noneSatisfy(note -> assertThat(note).contains("Served from cache"));

        assertThat(cache.hitCount()).isEqualTo(1);
        assertThat(cache.missCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("a changed inventory is a different question, so it misses and is solved again")
    void changingTheStateMisses() {
        GameDefinition definition = workshop();
        InProcessSolveCache cache = new InProcessSolveCache();
        MovableClock clock = new MovableClock(FIRST);
        TestRepositories.Players players = justAlice(definition);
        MipOptimizer optimizer = optimizer(definition, players, cache, clock);

        Plan poor = optimizer.solve(requestFor(ALICE, definition));

        // Nine ore already in the bag is cheaper than mining all eighteen.
        players.with(ALICE, Inventory.empty(ALICE).with(ORE, 9), new Roster(ALICE, Map.of()));
        clock.advance(Duration.ofMinutes(5));
        Plan richer = optimizer.solve(requestFor(ALICE, definition));

        assertThat(richer.id()).isNotEqualTo(poor.id());
        assertThat(richer.totalEnergy()).isLessThan(poor.totalEnergy());
        assertThat(richer.computedAt()).isEqualTo(FIRST.plus(Duration.ofMinutes(5)));
        assertThat(richer.explanation().notes())
                .noneSatisfy(note -> assertThat(note).contains("Served from cache"));
        assertThat(cache.missCount()).isEqualTo(2);
        assertThat(cache.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("two profiles in the same position share the solve, and each plan names its own reader")
    void aSharedAnswerStillNamesWhoAskedForIt() {
        GameDefinition definition = workshop();
        InProcessSolveCache cache = new InProcessSolveCache();
        MovableClock clock = new MovableClock(FIRST);
        // Identical inventory and identical roster: the same question, asked by
        // two people. The key carries state and not identity, so this is one solve.
        TestRepositories.Players players = new TestRepositories.Players(definition.game().id())
                .with(ALICE, Inventory.empty(ALICE), new Roster(ALICE, Map.of()))
                .with(BOB, Inventory.empty(BOB), new Roster(BOB, Map.of()));
        MipOptimizer optimizer = optimizer(definition, players, cache, clock);

        Plan hers = optimizer.solve(requestFor(ALICE, definition));
        clock.advance(Duration.ofHours(2));
        Plan his = optimizer.solve(requestFor(BOB, definition));

        assertThat(cache.hitCount()).isEqualTo(1);
        assertThat(his.totalEnergy()).isEqualTo(hers.totalEnergy());
        // The envelope is re-stamped for whoever is reading it. The plan is not
        // recomputed and does not pretend to have been.
        assertThat(hers.profile()).isEqualTo(ALICE);
        assertThat(his.profile()).isEqualTo(BOB);
        assertThat(his.computedAt()).isEqualTo(FIRST);
        assertThat(his.explanation().notes())
                .anySatisfy(note -> assertThat(note).contains("2h ago"));
    }

    @Test
    @DisplayName("a cached plan does not accumulate a note every time it is read")
    void theStoredCopyStaysClean() {
        GameDefinition definition = workshop();
        InProcessSolveCache cache = new InProcessSolveCache();
        MovableClock clock = new MovableClock(FIRST);
        MipOptimizer optimizer = optimizer(definition, justAlice(definition), cache, clock);

        Plan first = optimizer.solve(requestFor(ALICE, definition));
        optimizer.solve(requestFor(ALICE, definition));
        optimizer.solve(requestFor(ALICE, definition));
        Plan fourth = optimizer.solve(requestFor(ALICE, definition));

        long servedNotes = fourth.explanation().notes().stream()
                .filter(note -> note.contains("Served from cache"))
                .count();
        assertThat(servedNotes).isEqualTo(1);
        assertThat(fourth.explanation().notes()).hasSize(first.explanation().notes().size() + 1);
    }

    @Test
    @DisplayName("with no cache the optimizer solves every time, and the plan is otherwise unchanged")
    void theCacheIsOptional() {
        GameDefinition definition = workshop();
        MovableClock clock = new MovableClock(FIRST);
        MipOptimizer optimizer =
                optimizer(definition, justAlice(definition), SolveCache.none(), clock);

        Plan first = optimizer.solve(requestFor(ALICE, definition));
        clock.advance(Duration.ofMinutes(1));
        Plan second = optimizer.solve(requestFor(ALICE, definition));

        assertThat(second.id()).isEqualTo(first.id());
        assertThat(second.computedAt()).isEqualTo(FIRST.plus(Duration.ofMinutes(1)));
        assertThat(second.explanation().notes()).isEqualTo(first.explanation().notes());
    }
}
