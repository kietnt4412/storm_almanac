package io.stormalmanac.gamedata.catalog;

import java.util.List;
import java.util.Map;

/**
 * A skill and its ranks. Combat data is a separate axis from progression data
 * and goes stale differently — a balance patch changes multipliers without
 * touching material costs — so it is versioned with the rest of the bundle and
 * diffed on every ingest.
 *
 * @param ranks index 0 is rank 1
 */
public record Skill(String id, String displayName, List<Rank> ranks) {

    /**
     * @param values     named multipliers, e.g. {@code {"damage": 1.32}}
     * @param upgradeCost referenced by id into the game's item table
     */
    public record Rank(int rank, Map<String, Double> values, String description, List<String> upgradeCost) {
        public Rank {
            values = Map.copyOf(values);
            upgradeCost = List.copyOf(upgradeCost);
        }
    }

    public Skill {
        ranks = List.copyOf(ranks);
    }
}
