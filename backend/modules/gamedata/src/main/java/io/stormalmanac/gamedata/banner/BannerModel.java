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
        Availability window
) {

    public BannerModel {
        baseRates = Map.copyOf(baseRates);
        pityRules = Map.copyOf(pityRules);
        floors = List.copyOf(floors);
        double total = baseRates.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total > 1.0 + 1e-9) {
            throw new IllegalArgumentException("base rates sum to " + total + ", above 1.0");
        }
    }

    /** The rarity this banner is being pulled for; the rarest one with a pity rule. */
    public Rarity headlineRarity() {
        return pityRules.keySet().stream()
                .max(Rarity::compareTo)
                .orElseThrow(() -> new IllegalStateException("banner has no pity rule: " + id));
    }
}
