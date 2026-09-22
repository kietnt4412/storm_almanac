package io.stormalmanac.gamedata.banner;

import io.stormalmanac.common.id.ItemId;

/**
 * What one pull on a banner costs, and in what.
 *
 * <p>Two fields that between them unblock {@code IncomeModel}, which could not
 * be written while a banner declared neither. The question a player actually
 * asks — "she arrives in 40 days, will I have enough?" — is arithmetic over a
 * balance and a price, and the model held the odds and not the price.
 *
 * <p><b>Per pull, not per ten.</b> Punishing: Gray Raven prices a ten-pull at
 * exactly ten times one, so there is no discount to express and inventing a
 * second field for one would be modelling a rule no read game has. A game that
 * discounts a multi-pull makes this a list, and that change has a reading behind
 * it rather than a guess.
 *
 * <p><b>The currency is an {@link ItemId} like any other.</b> Four Punishing:
 * Gray Raven pools each have their own ticket, all priced 250, so the currency
 * belongs to the banner rather than to the game — a field on {@code Game} would
 * have had to pick one of the four.
 *
 * @param currency the item a pull is paid in
 * @param perPull  how many of it one pull costs
 */
public record PullPrice(ItemId currency, int perPull) {

    public PullPrice {
        if (currency == null) {
            throw new IllegalArgumentException("a pull price must name the item it is paid in");
        }
        if (perPull < 1) {
            throw new IllegalArgumentException(
                    "a pull costs " + perPull + " " + currency.value()
                            + "; a free pull is a grant, not a price");
        }
    }
}
