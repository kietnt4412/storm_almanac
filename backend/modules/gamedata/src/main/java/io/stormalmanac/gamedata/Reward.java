package io.stormalmanac.gamedata;

import java.util.List;

/**
 * Free income on a cadence: dailies, weeklies, event handouts. Feeds both the
 * planner (as a supply the player does not have to farm) and the gacha income
 * model (as pull currency accruing over time).
 */
public record Reward(
        String id,
        Cadence cadence,
        List<ItemStack> grants,
        Availability availability
) implements Source {

    public enum Cadence { DAILY, WEEKLY, MONTHLY, EVENT, ONE_OFF }

    public Reward {
        grants = List.copyOf(grants);
    }

    @Override
    public List<ItemStack> potentialOutput() {
        return grants;
    }
}
