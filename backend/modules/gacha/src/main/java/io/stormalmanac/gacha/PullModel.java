package io.stormalmanac.gacha;

import io.stormalmanac.gamedata.Rarity;
import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.gamedata.banner.FeaturedRule;
import io.stormalmanac.gamedata.banner.Floor;
import io.stormalmanac.gamedata.banner.PityRule;
import java.util.random.RandomGenerator;

/**
 * The part of a {@link BannerModel} that decides whether a pull hands over the
 * featured unit, validated once so that both engines refuse the same banners for
 * the same reasons and agree about the same rules.
 *
 * <p>Only the headline rarity is modelled, because it is the only rarity
 * {@link BannerEngine} is ever asked about and the only one the model pins under
 * pity. A lower rarity's pity rule cannot change the headline's rate: the
 * headline counter measures pulls since the last <em>headline</em> hit, and a
 * 5-star arriving does not reset it.
 *
 * <p><b>Floors are ignored, and that is correct rather than convenient.</b> A
 * floor guarantees <em>at least</em> its minimum rarity every N pulls — Reverse:
 * 1999's "one 4-star per 10-pull". It raises low outcomes up and never caps high
 * ones, so it leaves the probability of the headline rarity on any given pull
 * exactly where the rate table put it. That argument stops holding the moment a
 * floor's minimum reaches the headline rarity, because then the floor <em>is</em>
 * a second pity rule with a different period, and a chain that ignored it would
 * quietly understate the answer. Neither shipped game declares such a floor;
 * rather than assume no game ever will, {@link #of} refuses it by name.
 */
public record PullModel(Rarity headline, double baseRate, PityRule pity, FeaturedRule featured) {

    /**
     * Reads the headline question out of a banner, or refuses and says which
     * banner and why.
     */
    public static PullModel of(BannerModel banner) {
        if (banner.pityRules().isEmpty()) {
            throw new IllegalArgumentException(
                    "banner " + banner.id() + " declares no pity rule, so it has no rarity to pull for");
        }
        Rarity headline = banner.headlineRarity();
        Double base = banner.baseRates().get(headline);
        if (base == null) {
            throw new IllegalArgumentException("banner " + banner.id() + " declares a pity rule for rarity "
                    + headline.label() + " and no base rate for it");
        }
        for (Floor floor : banner.floors()) {
            if (floor.minimumRarity().rank() >= headline.rank()) {
                throw new IllegalArgumentException("banner " + banner.id() + " declares a floor of "
                        + floor.minimumRarity().label() + " every " + floor.everyN()
                        + " pulls, which guarantees the rarity being pulled for (" + headline.label()
                        + "). That is a second pity rule and neither engine models it");
            }
        }
        return new PullModel(headline, base, banner.pityRules().get(headline), banner.featuredRule());
    }

    /** The pull number at which the headline rarity is certain. */
    public int hardAt() {
        return pity.hardAt();
    }

    /**
     * Probability that the next pull hits the headline rarity.
     *
     * <p>A count at or past {@code hardAt} is read as "the next pull is the
     * guaranteed one" rather than refused: a state carried over from a banner
     * with a longer wall is a real thing for a player to have, and certainty is
     * the only answer that is not worse than the truth.
     */
    public double hitRateAt(int pullsSinceHit) {
        return pity.rateAt(Math.min(pullsSinceHit, hardAt() - 1), baseRate);
    }

    /** Probability a hit is the featured unit, given the losses carried into it. */
    public double featuredChanceAfter(int consecutiveLosses) {
        return consecutiveLosses >= featured.guaranteeAfterLoss() ? 1.0 : featured.chanceAtHit();
    }

    /**
     * One pull. The single place the rules are applied; the exact chain walks the
     * same branches with probabilities instead of a random number.
     */
    public PullResult draw(PityState from, RandomGenerator rng) {
        if (rng.nextDouble() >= hitRateAt(from.pullsSinceHit())) {
            return new PullResult(false, false, from.afterMiss());
        }
        if (rng.nextDouble() < featuredChanceAfter(from.consecutiveLosses())) {
            return new PullResult(true, true, from.afterFeaturedHit());
        }
        return new PullResult(true, false, from.afterOffFeaturedHit());
    }

    /**
     * The most pulls that can pass without the featured unit arriving: every wall
     * hit at the last moment, and every featured split lost until the guarantee
     * forces it. Punishing: Gray Raven published 120 is this number, and it is
     * 60 twice rather than 60 plus something.
     *
     * <p>A banner that hands the featured unit over outright needs one hit and not
     * two, so this is the wall itself there. Getting that wrong would only have
     * made a bound loose, which is exactly the kind of wrong that never fails a
     * test and quietly weakens one.
     */
    public long worstCasePulls() {
        long hitsNeeded = featured.chanceAtHit() >= 1.0 ? 1L : featured.guaranteeAfterLoss() + 1L;
        return (long) hardAt() * hitsNeeded;
    }
}
