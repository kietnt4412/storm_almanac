package io.stormalmanac.gamedata;

import java.util.List;

/**
 * A deterministic conversion. Recursive by nature — a tier-3 material is
 * crafted from tier-2 materials that are themselves farmed or crafted — so the
 * planner expands these transitively rather than one level deep.
 *
 * <p><b>A conversion has something on both sides.</b> A craft that consumes
 * nothing is not a cheap recipe, it is unbounded free supply: the optimizer
 * costs conversions at zero energy, so one such row lets it manufacture any
 * quantity of anything and every plan downstream of it is a lie. This is the
 * same rule {@code Stage} already keeps by refusing a source that costs no
 * energy, and it is enforced here rather than in the solver because a bundle
 * that carries one is wrong before anybody solves anything.
 *
 * <p>It is not a hypothetical. A real upstream lists its base materials in the
 * same file as its recipes, as rows with an empty material list, and converting
 * those faithfully produced a craft that made gold out of nothing. Free income
 * on a cadence is a {@link Reward}, which is capped; free income with no cap is
 * a defect.
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
        if (consumes.isEmpty()) {
            throw new IllegalArgumentException(
                    "craft '" + id + "' consumes nothing, which is unbounded free supply"
                            + " rather than a conversion");
        }
        if (produces.isEmpty()) {
            throw new IllegalArgumentException(
                    "craft '" + id + "' produces nothing, so nothing can ever want to run it");
        }
    }

    @Override
    public List<ItemStack> potentialOutput() {
        return produces;
    }
}
