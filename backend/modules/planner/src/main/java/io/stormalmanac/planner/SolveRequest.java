package io.stormalmanac.planner;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gamedata.Goal;
import java.util.List;

/**
 * Everything a solve depends on.
 *
 * <p>Solves are deterministic given this tuple plus the drop estimates, so the
 * request hashes to a cache key. That cache is Redis in phase 2 and is mirrored
 * by the hand-built replicated KV in phase 8 — both kept, both selectable.
 *
 * @param energyPerDay the profile's regeneration plus any refills it will spend
 */
public record SolveRequest(
        ProfileId profile,
        GameDataVersion gameVersion,
        List<Goal> goals,
        Objective objective,
        int energyPerDay
) {
    public SolveRequest {
        goals = List.copyOf(goals);
    }
}
