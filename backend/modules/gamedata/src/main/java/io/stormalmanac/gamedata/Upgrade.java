package io.stormalmanac.gamedata;

import io.stormalmanac.common.id.EntityId;
import java.util.List;

/**
 * A deterministic advance of one entity from one state to the next: Insight 1
 * to Insight 2, skill rank 3 to rank 4, level 40 to 50.
 *
 * <p>States are opaque strings supplied by the game bundle. The planner only
 * needs the graph they form, never their meaning.
 */
public record Upgrade(
        String id,
        EntityId entity,
        String fromState,
        String toState,
        List<ItemStack> costs
) implements Sink {

    public Upgrade {
        costs = List.copyOf(costs);
    }
}
