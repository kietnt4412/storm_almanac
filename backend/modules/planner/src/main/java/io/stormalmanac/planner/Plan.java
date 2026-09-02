package io.stormalmanac.planner;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.PlanId;
import io.stormalmanac.common.id.ProfileId;
import java.time.Instant;
import java.util.List;

/**
 * The answer: the cheapest way to close the gap between what a player owns and
 * what they want.
 *
 * <p>{@code computedAgainst} is not decoration. A plan records the game-data
 * version it was solved against so that a patch invalidates it loudly instead
 * of leaving a stale plan looking authoritative.
 */
public record Plan(
        PlanId id,
        ProfileId profile,
        GameDataVersion computedAgainst,
        Objective objective,
        List<StageRun> stageRuns,
        List<Conversion> conversions,
        int totalEnergy,
        double etaDays,
        Explanation explanation,
        Instant computedAt
) {
    public Plan {
        stageRuns = List.copyOf(stageRuns);
        conversions = List.copyOf(conversions);
    }
}
