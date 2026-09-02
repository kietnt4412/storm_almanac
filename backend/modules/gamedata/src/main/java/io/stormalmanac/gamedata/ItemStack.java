package io.stormalmanac.gamedata;

import io.stormalmanac.common.id.ItemId;

/** A quantity of one item. The unit of every cost, craft and reward in the model. */
public record ItemStack(ItemId item, int quantity) {
    public ItemStack {
        if (quantity < 0) throw new IllegalArgumentException("quantity must not be negative");
    }
}
