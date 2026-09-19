package io.stormalmanac.gamedata;

import io.stormalmanac.common.id.EntityId;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A deterministic advance of one entity from one state to the next: Insight 1
 * to Insight 2, skill rank 3 to rank 4, level 40 to 50.
 *
 * <p>States are opaque strings supplied by the game bundle. The planner only
 * needs the graph they form, never their meaning.
 *
 * @param costs    the items spent
 * @param requires states of the <em>same</em> entity, usually on another
 *                 track, that must be reached before this step can be taken: a
 *                 rank that needs a level, a passive that needs an evolution.
 *                 Reaching them is paid for as part of this step. A gate on
 *                 another entity is not expressible, deliberately; no reading
 *                 so far has needed one that is also a cost
 * @param progress what this step needs that no inventory holds, such as EXP.
 *                 Paid by {@link Fodder}
 */
public record Upgrade(
        String id,
        EntityId entity,
        String fromState,
        String toState,
        List<ItemStack> costs,
        List<String> requires,
        List<Progress> progress
) implements Sink {

    /**
     * Neither a gate nor a progress cost has an order, so both are normalised
     * here: gates sorted and distinct, progress summed per kind and sorted. The
     * same upgrade written two ways is then one value, which is what lets a
     * version read back from the database equal the bundle it came from.
     */
    public Upgrade {
        costs = List.copyOf(costs);
        requires = requires.stream().distinct().sorted().toList();
        Map<String, Integer> byKind = new TreeMap<>();
        progress.forEach(p -> byKind.merge(p.kind(), p.quantity(), Math::addExact));
        progress = byKind.entrySet().stream().map(e -> new Progress(e.getKey(), e.getValue())).toList();
        if (requires.contains(toState)) {
            throw new IllegalArgumentException("upgrade '" + id + "' requires the state it reaches");
        }
    }

    /** A step with a price and nothing else, which is every step the first two readings produced. */
    public Upgrade(String id, EntityId entity, String fromState, String toState, List<ItemStack> costs) {
        this(id, entity, fromState, toState, costs, List.of(), List.of());
    }
}
