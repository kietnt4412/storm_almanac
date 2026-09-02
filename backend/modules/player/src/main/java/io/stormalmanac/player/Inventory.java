package io.stormalmanac.player;

import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.ProfileId;
import java.util.HashMap;
import java.util.Map;

/**
 * What the profile currently owns.
 *
 * <p>Bulk entry speed is where companion tools live or die, so this is
 * deliberately a flat item-to-quantity map with no nesting: it can be patched
 * one key at a time from an offline PWA and merged last-write-wins per key.
 */
public record Inventory(ProfileId profile, Map<ItemId, Integer> quantities) {

    public Inventory {
        quantities = Map.copyOf(quantities);
    }

    public static Inventory empty(ProfileId profile) {
        return new Inventory(profile, Map.of());
    }

    public int quantityOf(ItemId item) {
        return quantities.getOrDefault(item, 0);
    }

    public Inventory with(ItemId item, int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("quantity must not be negative");
        Map<ItemId, Integer> next = new HashMap<>(quantities);
        if (quantity == 0) {
            next.remove(item);
        } else {
            next.put(item, quantity);
        }
        return new Inventory(profile, next);
    }
}
