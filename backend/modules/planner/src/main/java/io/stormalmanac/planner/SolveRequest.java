package io.stormalmanac.planner;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gamedata.Goal;
import java.util.List;
import java.util.Map;

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
 * @param reach        what the reader says they can reach, by measure: "I score
 *                     500 000 in the weekly Phantom Pain Cage". Some grants are
 *                     paid by how well a player does, and no screen tells this
 *                     project how well <em>this</em> player does — so, like
 *                     {@code energyPerDay}, it is a fact about the account that
 *                     the account's owner supplies (ADR 0022). Empty is the
 *                     honest default: it counts none of those grants, which
 *                     makes the plan dearer than the truth rather than cheaper,
 *                     and the plan says which ones it left out
 */
public record SolveRequest(
        ProfileId profile,
        GameDataVersion gameVersion,
        List<Goal> goals,
        Objective objective,
        int energyPerDay,
        int horizonDays,
        Map<String, Integer> reach
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
        reach = Map.copyOf(reach);
        if (energyPerDay <= 0) {
            throw new IllegalArgumentException("energyPerDay must be positive, was " + energyPerDay);
        }
        if (horizonDays <= 0) {
            throw new IllegalArgumentException("horizonDays must be positive, was " + horizonDays);
        }
        for (Map.Entry<String, Integer> reached : reach.entrySet()) {
            if (reached.getValue() == null || reached.getValue() < 0) {
                throw new IllegalArgumentException(
                        "reach of \"" + reached.getKey() + "\" must not be negative, was "
                                + reached.getValue());
            }
        }
    }

    /** The same request from a reader who has not said what they reach. */
    public SolveRequest(
            ProfileId profile,
            GameDataVersion gameVersion,
            List<Goal> goals,
            Objective objective,
            int energyPerDay,
            int horizonDays) {
        this(profile, gameVersion, goals, objective, energyPerDay, horizonDays, Map.of());
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
