package io.stormalmanac.gacha;

import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gamedata.Craft;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Reward;
import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.gamedata.banner.PullPrice;
import io.stormalmanac.player.Inventory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * <p><b>Through conversions, since ADR 0034.</b> A game may pay its pull
 * currency only at one remove — Punishing: Gray Raven's missions pay Black
 * Cards, which exchange 1:1 into the ticket a pull spends. A {@link Craft} that
 * turns one item into one other item is such a conversion, and an item with a
 * chain of them ending at the currency is counted as currency: what the
 * account holds of it and what the grants pay of it, each converted in whole
 * lots. A craft with several inputs or outputs is a recipe, not an exchange,
 * and a craft that closes is not counted on, because the horizon may outlast
 * it.
 *
 * <p><b>Zero accrual is an honest answer and a visibly incomplete one.</b> A
 * game whose grants nobody has written down reports the balance and nothing
 * more. That is the bundle's gap rather than the model's, and it is the shape
 * of gap this repository prefers: dearer than the truth, never cheaper, and
 * legible as a missing reading rather than as a wrong number.
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
        Map<ItemId, List<Craft>> routes = routesInto(currency, game.crafts());

        // Accrued per item in its own units, the currency included, so each is
        // converted once at the end rather than grant by grant — a lot that two
        // grants fill together is a lot the account can exchange.
        Map<ItemId, Long> accrued = new LinkedHashMap<>();
        List<String> uncounted = new ArrayList<>();
        for (Reward reward : game.rewards()) {
            Map<ItemId, Long> perOccurrence = new LinkedHashMap<>();
            for (ItemStack grant : reward.grants()) {
                if (grant.item().equals(currency) || routes.containsKey(grant.item())) {
                    perOccurrence.merge(grant.item(), (long) grant.quantity(), Long::sum);
                }
            }
            if (perOccurrence.isEmpty()) continue;

            // Named before it is dropped. A reader whose budget is short by
            // exactly the weekly they did not answer for deserves to be told
            // which question would move it, which is the whole argument of
            // ADR 0022 applied to income instead of to a plan.
            if (!reward.isOfferedTo(reach)) {
                uncounted.add(reward.id() + " (needs " + reward.requires().measure()
                        + " >= " + reward.requires().atLeast() + ")");
                continue;
            }
            int occurrences = reward.cadence().occurrencesIn(days);
            perOccurrence.forEach((item, quantity) -> accrued.merge(item, quantity * occurrences, Long::sum));
        }

        long heldTotal = held.quantityOf(currency);
        long accruingTotal = accrued.getOrDefault(currency, 0L);
        List<Converted> converted = new ArrayList<>();
        for (Map.Entry<ItemId, List<Craft>> route : routes.entrySet()) {
            ItemId item = route.getKey();
            long heldOfItem = held.quantityOf(item);
            long accruedOfItem = accrued.getOrDefault(item, 0L);
            if (heldOfItem == 0 && accruedOfItem == 0) continue;

            // Converted separately, so a remainder in each rounds down on its
            // own: the balance a reader can exchange today and the income they
            // will exchange later are two exchanges, not one.
            heldTotal += convert(heldOfItem, route.getValue());
            accruingTotal += convert(accruedOfItem, route.getValue());
            converted.add(new Converted(
                    item.value(), route.getValue().stream().map(Craft::id).toList(), heldOfItem, accruedOfItem));
        }

        return new PullBudget(
                currency.value(), heldTotal, accruingTotal, price.perPull(), uncounted, converted);
    }

    /**
     * Every item with a chain of one-for-one-item conversions ending at the
     * currency, mapped to the chain that yields the most currency per unit.
     *
     * <p>Searched backwards from the currency, never revisiting an item on one
     * chain, so a pair of crafts that trade two items back and forth cannot
     * loop — and cannot manufacture currency, since only a chain that ends at it
     * is kept.
     */
    static Map<ItemId, List<Craft>> routesInto(ItemId currency, List<Craft> crafts) {
        List<Craft> exchanges = crafts.stream().filter(DeclaredIncomeModel::isExchange).toList();

        Map<ItemId, List<Craft>> best = new LinkedHashMap<>();
        Map<ItemId, Double> bestRate = new LinkedHashMap<>();
        walk(currency, List.of(), 1.0, exchanges, new HashSet<>(Set.of(currency)), best, bestRate);
        return best;
    }

    private static void walk(
            ItemId towards,
            List<Craft> chainAfter,
            double rateAfter,
            List<Craft> exchanges,
            Set<ItemId> onChain,
            Map<ItemId, List<Craft>> best,
            Map<ItemId, Double> bestRate) {

        for (Craft craft : exchanges) {
            ItemStack out = craft.produces().getFirst();
            if (!out.item().equals(towards)) continue;
            ItemStack in = craft.consumes().getFirst();
            if (onChain.contains(in.item())) continue;

            double rate = rateAfter * out.quantity() / in.quantity();
            List<Craft> chain = new ArrayList<>(chainAfter.size() + 1);
            chain.add(craft);
            chain.addAll(chainAfter);
            if (rate > bestRate.getOrDefault(in.item(), 0.0)) {
                best.put(in.item(), List.copyOf(chain));
                bestRate.put(in.item(), rate);
            }
            onChain.add(in.item());
            walk(in.item(), chain, rate, exchanges, onChain, best, bestRate);
            onChain.remove(in.item());
        }
    }

    private static boolean isExchange(Craft craft) {
        return craft.consumes().size() == 1
                && craft.produces().size() == 1
                && !craft.consumes().getFirst().item().equals(craft.produces().getFirst().item())
                && !craft.availability().isExpiring();
    }

    /** Whole lots only, step by step: a remainder at any step buys nothing further on. */
    private static long convert(long quantity, List<Craft> chain) {
        long amount = quantity;
        for (Craft craft : chain) {
            amount = amount / craft.consumes().getFirst().quantity() * craft.produces().getFirst().quantity();
        }
        return amount;
    }
}
