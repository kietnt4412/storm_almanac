package io.stormalmanac.planner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.PlanId;
import io.stormalmanac.common.id.ProfileId;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The bounded map, on its own, without a solver anywhere near it. */
class InProcessSolveCacheTest {

    private static Plan planNamed(String id) {
        return new Plan(
                PlanId.of(id),
                new ProfileId("p1"),
                new GameDataVersion(GameId.of("g"), 1, "1.0", Instant.EPOCH, "test"),
                Objective.LEAST_ENERGY,
                List.of(),
                List.of(),
                List.of(),
                0,
                0.0,
                new Explanation(Map.of(), List.of(), List.of()),
                Instant.EPOCH);
    }

    @Test
    @DisplayName("a plan put under a key comes back under that key and under no other")
    void storesAndRetrieves() {
        InProcessSolveCache cache = new InProcessSolveCache();
        cache.put("abc", planNamed("plan-abc"));

        assertThat(cache.get("abc")).map(Plan::id).contains(PlanId.of("plan-abc"));
        assertThat(cache.get("def")).isEmpty();
    }

    @Test
    @DisplayName("the least recently used entry is the one evicted, not the oldest written")
    void evictsLeastRecentlyUsed() {
        InProcessSolveCache cache = new InProcessSolveCache(2);
        cache.put("a", planNamed("plan-a"));
        cache.put("b", planNamed("plan-b"));

        // Reading "a" makes "b" the least recently used, so writing a third
        // entry must evict "b" — a write-ordered cache would evict "a".
        assertThat(cache.get("a")).isPresent();
        cache.put("c", planNamed("plan-c"));

        assertThat(cache.size()).isEqualTo(2);
        assertThat(cache.get("a")).isPresent();
        assertThat(cache.get("c")).isPresent();
        assertThat(cache.get("b")).isEmpty();
    }

    @Test
    @DisplayName("hits and misses are counted, because a cache nobody can measure is a guess")
    void countsHitsAndMisses() {
        InProcessSolveCache cache = new InProcessSolveCache();
        cache.put("a", planNamed("plan-a"));

        cache.get("a");
        cache.get("a");
        cache.get("b");

        assertThat(cache.hitCount()).isEqualTo(2);
        assertThat(cache.missCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("a capacity below one is refused rather than quietly meaning unbounded")
    void refusesAnImpossibleCapacity() {
        assertThatThrownBy(() -> new InProcessSolveCache(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("capacity");
    }

    @Test
    @DisplayName("the no-op cache forgets everything, so a caller without one needs no null check")
    void theNoneCacheRemembersNothing() {
        SolveCache none = SolveCache.none();
        none.put("a", planNamed("plan-a"));

        assertThat(none.get("a")).isEmpty();
    }
}
