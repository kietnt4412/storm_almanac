package io.stormalmanac.gamedata;

import java.util.List;

/**
 * Consuming items of a class for progress: EXP Pods into a character's level,
 * enhancers into a weapon's.
 *
 * <p>This is Punishing: Gray Raven's Memory enhancement, and it is in the model
 * from day one rather than bolted on at phase 11. It is the case that breaks a
 * naive resource graph: an item is simultaneously a resource and a sink, so
 * the optimizer cannot treat "items owned" as a pure supply.
 *
 * @param consumesCategory the item category eligible as fodder
 * @param progress         the {@link Progress#kind()} this rule pays. {@code null}
 *                         for a rule read before any upgrade could name what it
 *                         feeds, which is inert: it parses, and nothing consumes it
 * @param progressPerUnit  how much of that progress one unit closes
 * @param costs            anything spent alongside the fodder itself (gold, etc.)
 */
public record Fodder(
        String id,
        String consumesCategory,
        Rarity minimumRarity,
        String progress,
        int progressPerUnit,
        List<ItemStack> costs
) implements Sink {

    public Fodder {
        if (progressPerUnit < 1) throw new IllegalArgumentException("progressPerUnit must be >= 1");
        if (progress != null && progress.isBlank()) {
            throw new IllegalArgumentException("progress must be absent or name a kind, not be blank");
        }
        costs = List.copyOf(costs);
    }
}
