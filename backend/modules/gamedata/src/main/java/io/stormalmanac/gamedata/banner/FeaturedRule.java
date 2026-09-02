package io.stormalmanac.gamedata.banner;

/**
 * What happens once the rarity is hit and the game decides <em>which</em> unit.
 *
 * <p>Reverse: 1999 debut banners hand over the featured unit outright
 * ({@code chanceAtHit = 1.0}). Punishing: Gray Raven rotational banners split
 * 70/30 and guarantee the featured unit on the next hit after a loss, which is
 * where the 120-pull worst case comes from.
 *
 * @param chanceAtHit        probability the hit is the featured unit
 * @param guaranteeAfterLoss losses that force the next hit to be featured; 1 for
 *                           the usual "lose once, then guaranteed"
 */
public record FeaturedRule(double chanceAtHit, int guaranteeAfterLoss) {

    public static final FeaturedRule ALWAYS = new FeaturedRule(1.0, 1);

    public FeaturedRule {
        if (chanceAtHit < 0 || chanceAtHit > 1) {
            throw new IllegalArgumentException("chanceAtHit must be in [0,1]");
        }
        if (guaranteeAfterLoss < 1) throw new IllegalArgumentException("guaranteeAfterLoss must be >= 1");
    }
}
