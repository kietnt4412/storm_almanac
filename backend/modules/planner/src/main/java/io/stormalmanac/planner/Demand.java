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

    private static final String PROGRESS_PREFIX = "progress:";

    /**
     * The item a {@link io.stormalmanac.gamedata.Progress} kind is demanded as.
     *
     * <p>Progress is not an item — nothing holds EXP — but inside a plan it
     * behaves exactly like an intermediate one: fodder makes it and upgrades
     * need it. Spelling it as an item keeps the demand vector, the solve cache
     * key and the model's balance rows one shape rather than two. The colon
     * keeps it out of every real item's namespace, since a bundle slug is a
     * kebab-case word and has none. <b>Anything that reads a demand line as a
     * catalog item has to ask {@link #isProgressItem} first.</b>
     */
    public static ItemId progressItem(String kind) {
        return new ItemId(PROGRESS_PREFIX + kind);
    }

    public static boolean isProgressItem(ItemId item) {
        return item.value().startsWith(PROGRESS_PREFIX);
    }

    /** The kind a progress item stands for; only meaningful when {@link #isProgressItem} is true. */
    public static String progressKind(ItemId item) {
        return item.value().substring(PROGRESS_PREFIX.length());
    }
}
