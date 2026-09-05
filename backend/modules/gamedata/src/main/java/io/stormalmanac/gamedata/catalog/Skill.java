package io.stormalmanac.gamedata.catalog;

import io.stormalmanac.gamedata.ItemStack;
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
     * @param values      named multipliers, e.g. {@code {"damage": 1.32}}
     * @param upgradeCost what it costs to reach this rank from the previous one.
     *                    An {@link ItemStack} list like every other cost in the
     *                    model: a bare list of item ids could not say
     *                    "four of these and one of those", and untyped ids are
     *                    against the rules the rest of the domain keeps.
     */
    public record Rank(int rank, Map<String, Double> values, String description, List<ItemStack> upgradeCost) {
        public Rank {
            if (rank < 1) throw new IllegalArgumentException("rank must be >= 1");
            values = Map.copyOf(values);
            upgradeCost = List.copyOf(upgradeCost);
        }
    }

    public Skill {
        ranks = List.copyOf(ranks);
    }
}
