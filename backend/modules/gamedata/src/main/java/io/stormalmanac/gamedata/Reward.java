package io.stormalmanac.gamedata;

import java.util.List;

/**
 * Free income on a cadence: dailies, weeklies, event handouts. Feeds both the
 * planner (as a supply the player does not have to farm) and the gacha income
 * model (as pull currency accruing over time).
 */
public record Reward(
        String id,
        Cadence cadence,
        List<ItemStack> grants,
        Availability availability
) implements Source {

    public enum Cadence {
        DAILY(1), WEEKLY(7), MONTHLY(31), EVENT(0), ONE_OFF(0);

        private final int days;

        Cadence(int days) {
            this.days = days;
        }

        /**
         * How many times a plan running for {@code days} days can collect this,
         * and the only place the optimizer learns what a cadence is worth.
         *
         * <p>Every number here rounds <b>against</b> the player, deliberately.
         * {@code MONTHLY} is 31 days rather than 30 because a calendar month is
         * between 28 and 31 and a plan that assumes the short one promises income
         * that may not arrive. {@code EVENT} and {@code ONE_OFF} are capped at one
         * however long the horizon: an event handout repeats on a schedule this
         * model is not told, so counting it once is the most that can be claimed
         * without inventing data. Free income that fails to turn up is a plan that
         * quietly under-farms, which is the expensive direction to be wrong in —
         * the same reasoning as ADR 0011's sample-size discount, applied to time
         * instead of to drop rates.
         */
        public int occurrencesIn(int days) {
            if (days <= 0) return 0;
            return this.days == 0 ? 1 : days / this.days;
        }
    }

    public Reward {
        grants = List.copyOf(grants);
    }

    @Override
    public List<ItemStack> potentialOutput() {
        return grants;
    }
}
