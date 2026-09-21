package io.stormalmanac.player;

import io.stormalmanac.common.id.EntityId;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

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
 * <p><b>The merge unit is the entity, not the state.</b> An entity now carries
 * a set of states, and the whole set moves together: an edit states where the
 * entity stands, in full, as of {@code editedAt}. Making each state its own
 * merge key would be the finer grain and the wrong one — two devices that each
 * levelled the same construct on a different track would both win, and the
 * result would be a roster neither device has ever held, assembled by the
 * server out of two half-truths. The clock stays per entity for that reason,
 * and V6's table is untouched.
 *
 * @param states the opaque upgrade-graph states the entity has reached, or
 *               {@code null} to say the entity is no longer on the roster. An
 *               empty set is neither and is refused: an owned entity is always
 *               at some state, so an empty one is a client bug, and leaving the
 *               key out of the request is how a client says nothing about it
 */
public record RosterEdit(EntityId entity, Set<String> states, Instant editedAt) {

    public RosterEdit {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(editedAt, "editedAt");
        if (states != null) {
            if (states.isEmpty()) {
                throw new IllegalArgumentException("entity '" + entity.value() + "' has no states");
            }
            states.forEach(state -> {
                if (state == null || state.isBlank()) {
                    throw new IllegalArgumentException("entity '" + entity.value() + "' has a blank state");
                }
            });
            states = Set.copyOf(new LinkedHashSet<>(states));
        }
    }

    public boolean isRemoval() {
        return states == null;
    }
}
