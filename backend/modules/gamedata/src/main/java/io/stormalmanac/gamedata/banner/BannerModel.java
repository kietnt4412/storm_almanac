package io.stormalmanac.gamedata.banner;

import io.stormalmanac.common.id.BannerId;
import io.stormalmanac.gamedata.Availability;
import io.stormalmanac.gamedata.Rarity;
import java.util.List;
import java.util.Map;

/**
 * One banner, described declaratively.
 *
 * <p>The argument in the README is that this record expresses Reverse: 1999's
 * rising soft-pity curve and Punishing: Gray Raven's split hard pity without a
 * single conditional on the game — general by evidence rather than assertion.
 * The published rates for both are the acceptance fixtures: if the engines in
 * the gacha module cannot reproduce them from this record alone, the model is
 * wrong and no amount of engine work fixes it.
 *
 * @param bannerType the pity bucket when {@link PityScope#BANNER_TYPE} applies
 * @param pullPrice  what one pull costs, or {@code null} when nobody has read
 *                   it. Null is <em>unstated</em> and not free, the same
 *                   distinction {@code Game.dayBoundary} draws: a banner read
 *                   for its rates before its price could be written down has to
 *                   round-trip as what it actually claimed, and there is no
 *                   default price to fall back on the way there is a default
 *                   midnight. A caller that needs one asks {@link #pricedPull}
 */
public record BannerModel(
        BannerId id,
        String displayName,
        String bannerType,
        Map<Rarity, Double> baseRates,
        Map<Rarity, PityRule> pityRules,
        List<Floor> floors,
        FeaturedRule featuredRule,
        PityScope pityScope,
        Availability window,
        PullPrice pullPrice
) {

    /** A banner read before a pull had a price. */
    public BannerModel(
            BannerId id,
            String displayName,
            String bannerType,
            Map<Rarity, Double> baseRates,
            Map<Rarity, PityRule> pityRules,
            List<Floor> floors,
            FeaturedRule featuredRule,
            PityScope pityScope,
            Availability window) {
        this(id, displayName, bannerType, baseRates, pityRules, floors, featuredRule, pityScope, window, null);
    }

    public BannerModel {
        baseRates = Map.copyOf(baseRates);
        pityRules = Map.copyOf(pityRules);
        floors = List.copyOf(floors);
        double total = baseRates.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total > 1.0 + 1e-9) {
            throw new IllegalArgumentException("base rates sum to " + total + ", above 1.0");
        }
    }

    /**
     * The price, or a refusal naming this banner.
     *
     * <p>Anything computing what a player can afford needs a price and cannot
     * invent one. Returning a zero cost would make every banner affordable and
     * would be indistinguishable, in an answer, from a banner somebody actually
     * read as free.
     */
    public PullPrice pricedPull() {
        if (pullPrice == null) {
            throw new IllegalStateException(
                    "banner " + id.value() + " does not say what a pull costs or what it is paid in,"
                            + " so what an account can afford on it is not a question this data answers");
        }
        return pullPrice;
    }

    /** The rarity this banner is being pulled for; the rarest one with a pity rule. */
    public Rarity headlineRarity() {
        return pityRules.keySet().stream()
                .max(Rarity::compareTo)
                .orElseThrow(() -> new IllegalStateException("banner has no pity rule: " + id));
    }
}
