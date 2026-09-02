package io.stormalmanac.gamedata;

import io.stormalmanac.common.id.ItemId;
import java.time.Period;
import java.util.List;

/**
 * A purchase, capped per period. The cap is the interesting part: an uncapped
 * shop entry would let the solver buy its way out of every constraint.
 *
 * @param periodLimit how many times per {@code period}; {@code 0} means unlimited
 */
public record Shop(
        String id,
        ItemId currency,
        int price,
        ItemStack offer,
        int periodLimit,
        Period period,
        Availability availability
) implements Source {

    @Override
    public List<ItemStack> potentialOutput() {
        return List.of(offer);
    }
}
