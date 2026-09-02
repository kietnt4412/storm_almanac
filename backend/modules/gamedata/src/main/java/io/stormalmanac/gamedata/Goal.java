package io.stormalmanac.gamedata;

import io.stormalmanac.common.id.EntityId;

/**
 * A target state for one entity, expressed against the sink graph.
 *
 * <p>{@code satisfiability} is the seam Punishing: Gray Raven forced open.
 * Reverse: 1999 goals are {@link Satisfiability#DETERMINISTIC}: pay the costs,
 * get the state. Resonance consumes duplicates for a random effect, so "reach
 * this Resonance" is {@link Satisfiability#PROBABILISTIC} — its cost is a
 * distribution, and the planner reports a percentile rather than a number.
 */
public record Goal(EntityId entity, String targetState, Satisfiability satisfiability, int priority) {

    public enum Satisfiability { DETERMINISTIC, PROBABILISTIC }

    public static Goal deterministic(EntityId entity, String targetState) {
        return new Goal(entity, targetState, Satisfiability.DETERMINISTIC, 0);
    }
}
