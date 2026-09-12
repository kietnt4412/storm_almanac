package io.stormalmanac.gacha;

import io.stormalmanac.common.id.BannerId;
import io.stormalmanac.gamedata.Availability;
import io.stormalmanac.gamedata.Rarity;
import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.gamedata.banner.FeaturedRule;
import io.stormalmanac.gamedata.banner.Floor;
import io.stormalmanac.gamedata.banner.PityRule;
import io.stormalmanac.gamedata.banner.PityScope;
import java.util.List;
import java.util.Map;

/**
 * Both shipped games' banners, built from the published numbers and nothing else.
 *
 * <p>These are acceptance fixtures rather than examples: if the engines cannot
 * reproduce these rates from these records, the model is wrong and no amount of
 * engine work fixes it. So the rate tables here carry only figures the sources
 * actually state. Where a real banner also lists rates for rarities nobody is
 * pulling for, they are left out — not because they are uninteresting but because
 * inventing one would turn a fixture into a guess. That the engines ignore them
 * is a separate claim, proved separately in {@link MarkovBannerEngineTest}.
 *
 * <p>The pity scopes are the published ones and are not load-bearing for any
 * answer here; see the scope test for why that is deliberate.
 */
final class Banners {

    static final Rarity SIX_STAR = new Rarity("6*", 6);
    static final Rarity FIVE_STAR = new Rarity("5*", 5);
    static final Rarity FOUR_STAR = new Rarity("4*", 4);

    static final Rarity S_RANK = new Rarity("S", 3);

    private Banners() {}

    /** Reverse: 1999 debut banner — base 1.5%, soft pity from 60 to 4% then +2.5%/pull, hard 70. */
    static BannerModel reverseDebut() {
        return new BannerModel(
                BannerId.of("r1999-debut"),
                "Debut",
                "character",
                Map.of(SIX_STAR, 0.015),
                Map.of(SIX_STAR, new PityRule(70, 60, 0.04, 0.025)),
                List.of(new Floor(10, FOUR_STAR)),
                FeaturedRule.ALWAYS,
                PityScope.BANNER,
                Availability.ALWAYS);
    }

    /** Reverse: 1999 beginner banner — a 6-star within 30, and the same 4-star floor. */
    static BannerModel reverseBeginner() {
        return new BannerModel(
                BannerId.of("r1999-beginner"),
                "Beginner",
                "beginner",
                Map.of(SIX_STAR, 0.015),
                Map.of(SIX_STAR, PityRule.hard(30)),
                List.of(new Floor(10, FOUR_STAR)),
                FeaturedRule.ALWAYS,
                PityScope.BANNER,
                Availability.ALWAYS);
    }

    /**
     * Punishing: Gray Raven rotational banner — base S-rank 0.5%, hard pity 60,
     * featured 70% at the hit with the next one guaranteed after a loss. The
     * published 120-pull worst case is those two numbers and nothing else.
     */
    static BannerModel grayRavenRotational() {
        return rank("pgr-rotational", "Rotational", "character",
                0.005, PityRule.hard(60), new FeaturedRule(0.70, 1), PityScope.BANNER_TYPE);
    }

    /** Punishing: Gray Raven debut banner — the same wall, the featured unit outright. */
    static BannerModel grayRavenDebut() {
        return rank("pgr-debut", "Debut", "character",
                0.005, PityRule.hard(60), FeaturedRule.ALWAYS, PityScope.BANNER_TYPE);
    }

    /** Punishing: Gray Raven weapon banner — wall at 30, and its own pity bucket. */
    static BannerModel grayRavenWeapon() {
        return rank("pgr-weapon", "Weapon", "weapon",
                0.005, PityRule.hard(30), new FeaturedRule(0.70, 1), PityScope.BANNER_TYPE);
    }

    /** Punishing: Gray Raven beginner banner — wall at 40. */
    static BannerModel grayRavenBeginner() {
        return rank("pgr-beginner", "Beginner", "beginner",
                0.005, PityRule.hard(40), new FeaturedRule(0.70, 1), PityScope.BANNER_TYPE);
    }

    /** The "floating guarantee" variant — 1.5% base against a wall at 80. */
    static BannerModel grayRavenFloating() {
        return rank("pgr-floating", "Floating guarantee", "character",
                0.015, PityRule.hard(80), new FeaturedRule(0.70, 1), PityScope.BANNER_TYPE);
    }

    private static BannerModel rank(
            String slug, String name, String type,
            double base, PityRule pity, FeaturedRule featured, PityScope scope) {
        return new BannerModel(
                BannerId.of(slug), name, type,
                Map.of(S_RANK, base), Map.of(S_RANK, pity),
                List.of(), featured, scope, Availability.ALWAYS);
    }

    /** Every banner above, for the tests that make one claim about all of them. */
    static List<BannerModel> all() {
        return List.of(
                reverseDebut(), reverseBeginner(),
                grayRavenRotational(), grayRavenDebut(), grayRavenWeapon(),
                grayRavenBeginner(), grayRavenFloating());
    }

    static PityState freshFor(BannerModel banner) {
        return PityState.fresh(banner.pityScope(), banner.bannerType());
    }
}
