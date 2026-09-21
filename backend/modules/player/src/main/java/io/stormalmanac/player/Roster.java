package io.stormalmanac.player;

import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.ProfileId;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Owned entities and the states each has reached.
 *
 * <p>States are the same opaque strings the upgrade graph uses, so "what is
 * this player short of" is a graph walk from where they stand to their goal
 * state with no game-specific interpretation anywhere.
 *
 * <p><b>A set per entity, not one state.</b> An entity is not on one track. A
 * PGR construct has a level, a rank, an evolution and six skills, and the game
 * does not tie them to each other — a player may stand at Lv 80 and rank 0,
 * which is a shape the game permits and which one state per entity cannot
 * write down. Holding one state meant a reader recorded on one track was
 * charged the whole of every other, and {@link
 * io.stormalmanac.planner.DemandResolver} could only recover the tracks that
 * happened to sit <em>behind</em> a recorded state through its gates. This
 * record is the other half of that: the ones beside it, which no inference can
 * reach, because only the player knows them.
 *
 * <p>The set is what the player has told us, not the closure of it. Working out
 * what else must be true — the states behind these, and the gates they imply —
 * belongs to the planner, which has the upgrade graph; this record has no graph
 * and does not get to guess. So two rosters holding different sets that mean
 * the same thing are different rosters here, and that is deliberate: a solve
 * key hashes what was said, and a player who says more has said something.
 */
public record Roster(ProfileId profile, Map<EntityId, Set<String>> currentStates) {

    public Roster {
        Map<EntityId, Set<String>> copy = new LinkedHashMap<>();
        currentStates.forEach((entity, states) -> {
            // An owned entity is always somewhere. An empty set is neither
            // "owned" nor "not owned" — leaving the entity out is how the
            // second is said — so it is a caller bug rather than a third case
            // for every reader of this map to handle.
            if (states.isEmpty()) {
                throw new IllegalArgumentException("entity '" + entity.value() + "' is on the roster at no state");
            }
            states.forEach(state -> {
                if (state == null || state.isBlank()) {
                    throw new IllegalArgumentException("entity '" + entity.value() + "' has a blank state");
                }
            });
            copy.put(entity, Set.copyOf(new LinkedHashSet<>(states)));
        });
        currentStates = Map.copyOf(copy);
    }

    public boolean owns(EntityId entity) {
        return currentStates.containsKey(entity);
    }

    /** Where this entity stands, or empty when it is not owned. */
    public Set<String> statesOf(EntityId entity) {
        return currentStates.getOrDefault(entity, Set.of());
    }
}
