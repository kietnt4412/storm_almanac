package io.stormalmanac.player;

import io.stormalmanac.common.id.EntityId;
import java.time.Instant;
import java.util.Objects;

/**
 * One entity's worth of an edit made somewhere else, on the same terms as
 * {@link InventoryEdit}.
 *
 * <p>The roster is here and goals are not, and the difference is not effort. A
 * roster is a map keyed by entity, so two devices that touched different
 * entities have an obvious merge and two that touched the same one have an
 * obvious tiebreak. Goals are an ordered list whose <em>order</em> is the thing
 * the player is editing; there is no per-key answer to two devices reordering
 * it, so goals stay a whole-aggregate PUT rather than getting a merge that
 * quietly invents one.
 *
 * @param state the opaque upgrade-graph state the entity is now at, or
 *              {@code null} to say the entity is no longer on the roster.
 *              Blank is neither and is refused: an owned entity is always at
 *              some state, so a blank one is a client bug, and leaving the key
 *              out of the request is how a client says nothing about it
 */
public record RosterEdit(EntityId entity, String state, Instant editedAt) {

    public RosterEdit {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(editedAt, "editedAt");
        if (state != null && state.isBlank()) {
            throw new IllegalArgumentException("entity '" + entity.value() + "' has a blank state");
        }
    }

    public boolean isRemoval() {
        return state == null;
    }
}
