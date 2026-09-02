package io.stormalmanac.planner;

import io.stormalmanac.common.id.StageId;

/**
 * "Run 3-4 seventeen times."
 *
 * <p>{@code runs} is an integer because the alternative — "run 3-4 exactly 17.3
 * times" — is useless to a player, and that single requirement is what makes
 * this a mixed-integer program rather than a linear one.
 */
public record StageRun(StageId stage, int runs, int energyCost) {
    public StageRun {
        if (runs < 0) throw new IllegalArgumentException("runs must not be negative");
    }

    public int totalEnergy() {
        return runs * energyCost;
    }
}
