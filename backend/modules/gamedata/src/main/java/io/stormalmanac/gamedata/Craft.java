package io.stormalmanac.gamedata;

import java.util.List;

/**
 * A deterministic conversion. Recursive by nature — a tier-3 material is
 * crafted from tier-2 materials that are themselves farmed or crafted — so the
 * planner expands these transitively rather than one level deep.
 */
public record Craft(
        String id,
        List<ItemStack> consumes,
        List<ItemStack> produces,
        Availability availability
) implements Source {

    public Craft {
        consumes = List.copyOf(consumes);
        produces = List.copyOf(produces);
    }

    @Override
    public List<ItemStack> potentialOutput() {
        return produces;
    }
}
