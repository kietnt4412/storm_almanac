package io.stormalmanac.gamedata;

import java.util.List;
import java.util.Map;

/**
 * Free income on a cadence: dailies, weeklies, event handouts. Feeds both the
 * planner (as a supply the player does not have to farm) and the gacha income
 * model (as pull currency accruing over time).
 *
 * @param requires what the reader has to be able to do to collect this, or
 *                 {@code null} for a grant that turns up for everybody. See
 *                 {@link Requirement}
 */
public record Reward(
        String id,
        Cadence cadence,
        List<ItemStack> grants,
        Availability availability,
        Requirement requires
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

    /**
     * A grant the game pays by how well the player did, written as the bar and
     * not as the payout.
     *
     * <p>Punishing: Gray Raven's weekly Phantom Pain Cage is nine tiers of
     * Scars, each behind a score, and the whole ladder is worth 56 a week to a
     * player who clears it and 4 to a player who barely starts. There is no
     * number this project can read off a screen that says which of those a
     * reader is: it is a fact about them, like the energy they regenerate, and
     * {@link io.stormalmanac.gamedata.GameDefinition game data} is the wrong
     * place to keep a fact about a reader. So the bundle writes one reward per
     * tier with the bar it stands behind, the reader says what they reach, and
     * the planner counts the tiers they clear and no others. ADR 0022.
     *
     * <p>Silence is not the top tier and not an error: a reader who says
     * nothing gets a plan that counts none of it, which is dearer than the
     * truth rather than cheaper, and the plan says which grants it left out.
     *
     * @param measure an opaque label the bundle supplies for the thing the game
     *                scores — {@code "phantom-pain-cage-score"} — matched
     *                against what the reader says they reach and never
     *                interpreted. Not an item: nobody holds a stack of it, and
     *                spending it is not a thing that happens
     * @param atLeast the score at which this grant becomes collectable
     */
    public record Requirement(String measure, int atLeast) {

        public Requirement {
            if (measure == null || measure.isBlank()) {
                throw new IllegalArgumentException("a requirement's measure must not be blank");
            }
            if (atLeast < 1) {
                throw new IllegalArgumentException(
                        "requirement on '" + measure + "' must ask for at least 1, was " + atLeast);
            }
        }

        /** True when the reader has said they reach this bar. */
        public boolean metBy(Map<String, Integer> reach) {
            return reach.getOrDefault(measure, 0) >= atLeast;
        }
    }

    public Reward {
        grants = List.copyOf(grants);
    }

    /** A grant with no bar in front of it, which is most of them. */
    public Reward(String id, Cadence cadence, List<ItemStack> grants, Availability availability) {
        this(id, cadence, grants, availability, null);
    }

    /**
     * Whether this reward is one the reader can collect at all.
     *
     * @param reach what the reader says they reach, by measure
     */
    public boolean isOfferedTo(Map<String, Integer> reach) {
        return requires == null || requires.metBy(reach);
    }

    @Override
    public List<ItemStack> potentialOutput() {
        return grants;
    }
}
