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
 *
 * @param rewardClaims the free income this plan is counting on. Empty is the
 *                     common case and means the plan stands on farming alone
 * @param etaDays      how long the plan takes: the energy it spends divided by
 *                     the daily rate, or longer when it waits on a cadence or on
 *                     a stage that is only open some days. Never rounded up here
 *                     — "3.4 days" is truer than "4", and the rounding belongs to
 *                     whoever renders it
 */
public record Plan(
        PlanId id,
        ProfileId profile,
        GameDataVersion computedAgainst,
        Objective objective,
        List<StageRun> stageRuns,
        List<Conversion> conversions,
        List<RewardClaim> rewardClaims,
        int totalEnergy,
        double etaDays,
        Explanation explanation,
        Instant computedAt
) {
    public Plan {
        stageRuns = List.copyOf(stageRuns);
        conversions = List.copyOf(conversions);
        rewardClaims = List.copyOf(rewardClaims);
    }
}
