package io.stormalmanac.gamedata;

import java.util.List;

/**
 * Advancing one item by consuming other items of a class.
 *
 * <p>This is Punishing: Gray Raven's Memory enhancement, and it is in the model
 * from day one rather than bolted on at phase 11. It is the case that breaks a
 * naive resource graph: an item is simultaneously a resource and a sink, so
 * the optimizer cannot treat "items owned" as a pure supply.
 *
 * @param consumesCategory the item category eligible as fodder
 * @param progressPerUnit  how much of {@code target}'s requirement one unit closes
 * @param costs            anything spent alongside the fodder itself (gold, etc.)
 */
public record Fodder(
        String id,
        String consumesCategory,
        Rarity minimumRarity,
        int progressPerUnit,
        List<ItemStack> costs
) implements Sink {

    public Fodder {
        if (progressPerUnit < 1) throw new IllegalArgumentException("progressPerUnit must be >= 1");
        costs = List.copyOf(costs);
    }
}
