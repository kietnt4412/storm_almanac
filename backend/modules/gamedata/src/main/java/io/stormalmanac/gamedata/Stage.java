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

    @Override
    public List<ItemStack> potentialOutput() {
        return drops.stream().map(d -> new ItemStack(d.item(), d.quantityPerHit())).toList();
    }
}
