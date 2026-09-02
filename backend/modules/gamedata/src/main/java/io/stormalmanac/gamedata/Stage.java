package io.stormalmanac.gamedata;

import io.stormalmanac.common.id.StageId;
import java.util.List;

/**
 * A farmable stage: the only source that consumes the energy budget, and
 * therefore the only one that appears in the optimizer's objective function.
 */
public record Stage(
        StageId stageId,
        String displayName,
        int energyCost,
        List<Drop> drops,
        Availability availability
) implements Source {

    public Stage {
        if (energyCost < 0) throw new IllegalArgumentException("energyCost must not be negative");
        drops = List.copyOf(drops);
    }

    @Override
    public String id() {
        return stageId.value();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Unlike the deterministic sources, a stage's output is a distribution.
     * The quantity here is a rounded-up expected yield, floored at 1 — enough
     * for reachability pruning, which only asks <em>which</em> items a stage can
     * produce. It is not a guarantee and must never be used as a supply figure;
     * the solver reads measured {@code DropEstimate}s for that.
     */
    @Override
    public List<ItemStack> potentialOutput() {
        return drops.stream()
                .map(d -> new ItemStack(d.item(), Math.max(1, (int) Math.ceil(d.expectedYield()))))
                .toList();
    }
}
