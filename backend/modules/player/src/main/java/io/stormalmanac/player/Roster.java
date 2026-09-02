package io.stormalmanac.player;

import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.ProfileId;
import java.util.Map;

/**
 * Owned entities and the state each is currently at.
 *
 * <p>States are the same opaque strings the upgrade graph uses, so "what is
 * this player short of" is a graph walk from their state to their goal state
 * with no game-specific interpretation anywhere.
 */
public record Roster(ProfileId profile, Map<EntityId, String> currentState) {

    public Roster {
        currentState = Map.copyOf(currentState);
    }

    public boolean owns(EntityId entity) {
        return currentState.containsKey(entity);
    }
}
