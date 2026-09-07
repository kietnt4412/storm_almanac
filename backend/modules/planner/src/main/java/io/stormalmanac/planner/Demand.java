package io.stormalmanac.planner;

import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gamedata.Goal;
import java.util.List;
import java.util.Map;

/**
 * What a goal set actually costs, in items, before anything is farmed.
 *
 * <p>This is the right-hand side of every constraint in the optimizer, and it is
 * computed without the solver: the upgrade graph is a DAG of opaque states, and
 * the distance from where a player is to where they want to be is a walk over
 * it. Getting this wrong is the expensive kind of wrong — the solver will
 * cheerfully find the cheapest way to satisfy a demand vector nobody asked for.
 *
 * @param quantities how much of each item the goals need in total. Gross, not
 *                   net: the inventory is subtracted by the solver, because the
 *                   same demand vector is reused across profiles by the cache
 * @param steps      the upgrade ids being paid for, in the order a player would
 *                   perform them. The plan renders these so that a total has
 *                   something to be a total <em>of</em>
 * @param alreadyMet goals the roster already satisfies. Not an error and not
 *                   silence either — a player who asks for Insight 2 while
 *                   sitting at Insight 3 deserves to be told, rather than shown
 *                   an empty plan
 */
public record Demand(
        Map<ItemId, Integer> quantities,
        List<String> steps,
        List<Goal> alreadyMet
) {

    public Demand {
        quantities = Map.copyOf(quantities);
        steps = List.copyOf(steps);
        alreadyMet = List.copyOf(alreadyMet);
    }

    public boolean isEmpty() {
        return quantities.isEmpty();
    }

    public int quantityOf(ItemId item) {
        return quantities.getOrDefault(item, 0);
    }
}
