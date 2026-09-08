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
 * @param horizonDays  how long the player is prepared to spend. <b>Not a
 *                     formality: without it the cheapest plan is always "wait",
 *                     because free income accrues at no energy cost and an
 *                     unbounded horizon accrues an unbounded amount of it.</b>
 *                     The horizon is what makes {@link Objective#LEAST_ENERGY}
 *                     answerable and what {@link Objective#FEWEST_DAYS}
 *                     minimises within
 */
public record SolveRequest(
        ProfileId profile,
        GameDataVersion gameVersion,
        List<Goal> goals,
        Objective objective,
        int energyPerDay,
        int horizonDays
) {

    /**
     * The horizon a request gets when it does not name one.
     *
     * <p>Four weeks and change: long enough to contain a monthly reset and every
     * weekday twice over, short enough that "the cheapest plan" still means
     * something a player will act on rather than a receipt for waiting. It is a
     * default and not a constant of the domain — the moment a caller has a real
     * player in front of it, that player's deadline is the honest number.
     */
    public static final int DEFAULT_HORIZON_DAYS = 30;

    public SolveRequest {
        goals = List.copyOf(goals);
        if (energyPerDay <= 0) {
            throw new IllegalArgumentException("energyPerDay must be positive, was " + energyPerDay);
        }
        if (horizonDays <= 0) {
            throw new IllegalArgumentException("horizonDays must be positive, was " + horizonDays);
        }
    }

    /** The same request over {@link #DEFAULT_HORIZON_DAYS}. */
    public SolveRequest(
            ProfileId profile,
            GameDataVersion gameVersion,
            List<Goal> goals,
            Objective objective,
            int energyPerDay) {
        this(profile, gameVersion, goals, objective, energyPerDay, DEFAULT_HORIZON_DAYS);
    }
}
