package io.stormalmanac.gacha;

import io.stormalmanac.gamedata.banner.BannerModel;

/**
 * One pull, reported as the banner model can actually account for it.
 *
 * <p>There is no {@code Rarity} on this record, and the omission is the honest
 * reading of {@link BannerModel} rather than an oversight. The model pins the
 * rate of the headline rarity under pity and says nothing whatsoever about how
 * a pity-forced hit redistributes the rest of the rate table: when a rising
 * curve reaches 9%, the published numbers do not say which rarity gave up the
 * 7.5 points. Returning a rarity would mean inventing that answer, and it would
 * be invented differently for each game — which is the one thing this module is
 * not allowed to do.
 *
 * <p>So a pull is reported as the two facts the model does determine: whether it
 * hit the rarity being pulled for, and whether that hit was the featured unit.
 * {@code featured} is only meaningful when {@code headlineHit} is true.
 */
public record PullResult(boolean headlineHit, boolean featured, PityState stateAfter) {

    public PullResult {
        if (featured && !headlineHit) {
            throw new IllegalArgumentException("a pull cannot be featured without hitting the rarity");
        }
    }
}
