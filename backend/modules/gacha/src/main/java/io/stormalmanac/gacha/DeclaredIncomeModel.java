package io.stormalmanac.gacha;

import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Reward;
import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.gamedata.banner.PullPrice;
import io.stormalmanac.player.Inventory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Income counted from what the bundle declares, and from nothing else.
 *
 * <p>The tempting implementation is a rate per day per game — "PGR gives about
 * 30 pulls a patch" — and it is exactly the kind of number this project does
 * not ship. It appears on no screen, it is a community estimate at best, and it
 * would be a game-specific constant in a module that is not allowed one. So
 * accrual here is the sum of the {@link Reward} rows that grant the pull
 * currency, at their own cadence, over the horizon: every term is a fact
 * somebody read, and a game whose grants nobody has written down accrues zero
 * and says so rather than guessing.
 *
 * <p><b>Zero accrual is an honest answer and a visibly incomplete one.</b>
 * Punishing: Gray Raven's first bundle declares no reward paying research
 * tickets, so this model will report the balance and nothing more. That is the
 * bundle's gap rather than the model's, and it is the shape of gap this
 * repository prefers: dearer than the truth, never cheaper, and legible as a
 * missing reading rather than as a wrong number.
 *
 * <p>Stateless, so one instance serves every caller.
 */
public final class DeclaredIncomeModel implements IncomeModel {

    @Override
    public PullBudget affordableWithin(
            GameDefinition game, BannerModel banner, Inventory held, int days, Map<String, Integer> reach) {

        if (days < 0) {
            throw new IllegalArgumentException("a horizon of " + days + " days counts backwards");
        }
        // Refuses rather than returning a budget of zero pulls: a banner nobody
        // has priced and a banner nobody can afford are different answers, and
        // a screen showing the second for the first would be wrong in the one
        // way a reader cannot detect.
        PullPrice price = banner.pricedPull();
        ItemId currency = price.currency();

        long accruing = 0;
        List<String> uncounted = new ArrayList<>();
        for (Reward reward : game.rewards()) {
            long perOccurrence = 0;
            for (ItemStack grant : reward.grants()) {
                if (grant.item().equals(currency)) perOccurrence += grant.quantity();
            }
            if (perOccurrence == 0) continue;

            // Named before it is dropped. A reader whose budget is short by
            // exactly the weekly they did not answer for deserves to be told
            // which question would move it, which is the whole argument of
            // ADR 0022 applied to income instead of to a plan.
            if (!reward.isOfferedTo(reach)) {
                uncounted.add(reward.id() + " (needs " + reward.requires().measure()
                        + " >= " + reward.requires().atLeast() + ")");
                continue;
            }
            accruing += perOccurrence * (long) reward.cadence().occurrencesIn(days);
        }

        return new PullBudget(
                currency.value(), held.quantityOf(currency), accruing, price.perPull(), uncounted);
    }
}
