package io.stormalmanac.gamedata;

import io.stormalmanac.common.id.ItemId;
import java.time.Period;
import java.util.List;

/**
 * A purchase, capped per period. The cap is the interesting part: an uncapped
 * shop entry would let the solver buy its way out of every constraint.
 *
 * <p><b>A shop that is free and unlimited is refused</b>, for the reason
 * {@link Craft} refuses a recipe that consumes nothing: the optimizer costs a
 * purchase at zero energy, so one such row mints any quantity of its offer. A
 * free offer with a limit is a real thing — a daily pack — and is kept.
 *
 * @param price       what one purchase costs in {@code currency}; covers the
 *                    whole {@code offer} stack, not one unit of it
 * <p><b>A limit that never resets</b> — thirty of a character's shards, ever —
 * is a {@code periodLimit} with a {@code null} period, written {@value #NEVER}
 * in a bundle and in storage, because ISO-8601 has no word for it. Its whole
 * allowance is offered to any horizon of at least a day, which assumes the
 * player has bought none of it yet: nothing a player records says how much of
 * it they have spent, so the planner says it assumed so whenever it buys one.
 *
 * @param periodLimit how many times per {@code period}; {@code 0} means unlimited,
 *                    and then {@code period} is inert
 * @param period      how often the limit resets, or {@code null} for never
 */
public record Shop(
        String id,
        ItemId currency,
        int price,
        ItemStack offer,
        int periodLimit,
        Period period,
        Availability availability
) implements Source {

    public Shop {
        if (price < 0) throw new IllegalArgumentException("shop '" + id + "' has a negative price");
        if (periodLimit < 0) {
            throw new IllegalArgumentException("shop '" + id + "' has a negative periodLimit");
        }
        if (offer.quantity() < 1) {
            throw new IllegalArgumentException("shop '" + id + "' offers nothing");
        }
        if (price == 0 && periodLimit == 0) {
            throw new IllegalArgumentException(
                    "shop '" + id + "' is free and unlimited, which is unbounded free supply"
                            + " rather than a purchase");
        }
        if (periodLimit > 0 && period != null && periodDays(period) < 1) {
            throw new IllegalArgumentException(
                    "shop '" + id + "' has a limit per period and no period of at least one day");
        }
    }

    @Override
    public List<ItemStack> potentialOutput() {
        return List.of(offer);
    }

    /** How a period that never ends is written, in a bundle and in storage. */
    public static final String NEVER = "never";

    public boolean isUnlimited() {
        return periodLimit == 0;
    }

    /** A limit that is spent once and never refills. */
    public boolean neverResets() {
        return periodLimit > 0 && period == null;
    }

    /** The period as ISO-8601, or {@value #NEVER}. */
    public String periodText() {
        return period == null ? NEVER : period.toString();
    }

    /**
     * The inverse of {@link #periodText()}.
     *
     * @throws java.time.format.DateTimeParseException when it is neither
     */
    public static Period parsePeriod(String text) {
        return NEVER.equals(text) ? null : Period.parse(text);
    }

    /**
     * How many purchases a plan running for {@code days} days can make, or
     * {@link Long#MAX_VALUE} when nothing limits them.
     *
     * <p>Rounds against the player, the same way {@link Reward.Cadence#occurrencesIn}
     * does and for the same reason. Only whole periods count, because nothing says
     * how much of the current period's allowance the player has already spent. A
     * month is 31 days and a year 366. A plan that assumes a purchase a shop may
     * not offer will come up short, and that is the costly mistake. Assuming too
     * few only makes the plan a little dearer than it has to be.
     *
     * <p>A limit that {@linkplain #neverResets never resets} is the exception,
     * and a deliberate one: rounded against the player it would be zero for
     * everybody, so it is the whole allowance, and the plan says so.
     */
    public long purchasesIn(int days) {
        if (days <= 0) return 0;
        if (isUnlimited()) return Long.MAX_VALUE;
        if (neverResets()) return periodLimit;
        return (long) periodLimit * (days / periodDays(period));
    }

    private static long periodDays(Period period) {
        return period.getYears() * 366L + period.getMonths() * 31L + period.getDays();
    }
}
