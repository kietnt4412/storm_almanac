package io.stormalmanac.gacha;

import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.player.Inventory;
import java.util.List;
import java.util.Map;

/**
 * How many pulls an account can afford on a banner, now and after a horizon.
 *
 * <p>Pairing this with the simulator is the feature nobody ships well, and it
 * is the question players actually ask: "I have this much, she arrives in 40
 * days, what is my probability of guaranteeing her?" {@link BannerEngine}
 * answers the second half to three decimals and has never been able to answer
 * the first, because until a banner could say what a pull costs there was no
 * arithmetic to do. See {@code PullPrice} and N28 in TRACKER.md.
 *
 * <p><b>Days, not a date.</b> The planner's horizon is a scalar and not an
 * index (ADR 0013), and the same argument holds here: nothing in this
 * computation is indexed by which day it is, only by how many there are. A
 * caller holding two dates subtracts them, once, at the edge.
 *
 * <p><b>{@code reach} is the reader's, exactly as it is for a plan.</b> A grant
 * behind a score nobody has said they clear is dropped and reported, never
 * assumed (ADR 0022). Silence buys fewer pulls than the truth rather than more.
 */
public interface IncomeModel {

    /**
     * What this account can spend on this banner over {@code days}.
     *
     * @param days how far ahead to count accrual. Zero is "today", and is a
     *             real question rather than a degenerate one — it is the balance
     *             a reader sees in the client
     */
    PullBudget affordableWithin(
            GameDefinition game, BannerModel banner, Inventory held, int days, Map<String, Integer> reach);

    /**
     * What the account has, what it will collect, and what that buys.
     *
     * <p>Three numbers and not one, for the same reason a shortfall line carries
     * three: a reader who disagrees with "26 pulls" needs to see which half they
     * disagree with, and a client that divides for itself is a second copy of
     * the one division.
     *
     * @param currency  the item a pull is paid in, carried out so a client can
     *                  name it without looking the banner up again
     * @param held      what the inventory says the account has of it now
     * @param accruing  what the game's own declared grants pay over the horizon,
     *                  counting only the tiers this reader says they reach
     * @param perPull   the price, repeated here because a budget with no price
     *                  cannot be checked
     * @param uncounted the grants dropped for standing behind a bar the reader
     *                  has not answered for, named rather than silently omitted
     */
    record PullBudget(
            String currency, long held, long accruing, int perPull, List<String> uncounted) {

        public PullBudget {
            uncounted = List.copyOf(uncounted);
        }

        /**
         * Whole pulls. A remainder buys nothing: a banner does not sell
         * fractions of a pull, and rounding up would promise one that the
         * account cannot pay for.
         */
        public long pulls() {
            return (held + accruing) / perPull;
        }
    }
}
