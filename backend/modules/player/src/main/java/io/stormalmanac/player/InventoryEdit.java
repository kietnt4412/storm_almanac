package io.stormalmanac.player;

import io.stormalmanac.common.id.ItemId;
import java.time.Instant;
import java.util.Objects;

/**
 * One key's worth of an edit made somewhere else, possibly a while ago.
 *
 * <p>This is the unit offline sync moves, and it exists because
 * {@link Inventory} is the wrong unit for it: an inventory is a complete map, so
 * saving one means "this is now the whole inventory" and a phone that has been
 * in a tunnel for an hour would delete everything a browser added in the
 * meantime. An edit says less, and saying less is the entire feature.
 *
 * <p><b>{@code editedAt} is when the client made the change, not when the server
 * heard about it.</b> If the server stamped its own clock the phone's hour-old
 * edit would beat the browser's ten-minute-old one purely by arriving later,
 * which is the bug rather than the fix. The cost of trusting a client's clock is
 * that a wrong one can claim the future, and that is handled where the merge
 * happens rather than here — an edit is free to describe itself; it is not free
 * to win.
 *
 * @param quantity how many the player now has. Zero is a removal and not a
 *                 rejected value: "I have none of these" is the commonest edit a
 *                 player makes, and V5's fourth decision already says absent and
 *                 zero are one state
 */
public record InventoryEdit(ItemId item, int quantity, Instant editedAt) {

    public InventoryEdit {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(editedAt, "editedAt");
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must not be negative: " + quantity);
        }
    }

    public boolean isRemoval() {
        return quantity == 0;
    }
}
