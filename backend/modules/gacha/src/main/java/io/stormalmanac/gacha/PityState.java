package io.stormalmanac.gacha;

import io.stormalmanac.gamedata.banner.FeaturedRule;
import io.stormalmanac.gamedata.banner.PityScope;

/**
 * A player's carried pity, scoped as the banner declares.
 *
 * <p>{@code scopeKey} is what makes Punishing: Gray Raven expressible: pity
 * carries within a banner <em>type</em>, so a character-banner counter must not
 * leak into a weapon banner. The key is derived from the scope, never assumed
 * to be global.
 *
 * <p>{@code consecutiveLosses} is a count and not the flag this record used to
 * carry. A boolean says "the next hit is guaranteed featured", which is exactly
 * right for a rule that guarantees after one loss and cannot express a player
 * one loss into a rule that guarantees after two. {@link FeaturedRule} permits
 * that shape, so the state has to carry it or the engines would be guessing at
 * the middle of it — and a model that can only describe the game it was written
 * against is the thing this project is trying not to build.
 *
 * <p>The transitions live here rather than in either engine, so the exact chain
 * and the simulation advance state through the same three lines.
 */
public record PityState(PityScope scope, String scopeKey, int pullsSinceHit, int consecutiveLosses) {

    public PityState {
        if (pullsSinceHit < 0) {
            throw new IllegalArgumentException("pullsSinceHit must be >= 0, was " + pullsSinceHit);
        }
        if (consecutiveLosses < 0) {
            throw new IllegalArgumentException("consecutiveLosses must be >= 0, was " + consecutiveLosses);
        }
    }

    public static PityState fresh(PityScope scope, String scopeKey) {
        return new PityState(scope, scopeKey, 0, 0);
    }

    /** True when the next hit on this rarity must hand over the featured unit. */
    public boolean isGuaranteed(FeaturedRule rule) {
        return consecutiveLosses >= rule.guaranteeAfterLoss();
    }

    /** The pull missed the headline rarity: pity rises, the featured split is untouched. */
    public PityState afterMiss() {
        return new PityState(scope, scopeKey, pullsSinceHit + 1, consecutiveLosses);
    }

    /** The featured unit arrived: pity resets and so does the accumulated guarantee. */
    public PityState afterFeaturedHit() {
        return new PityState(scope, scopeKey, 0, 0);
    }

    /** The rarity hit and the split was lost: pity resets, the guarantee moves one closer. */
    public PityState afterOffFeaturedHit() {
        return new PityState(scope, scopeKey, 0, consecutiveLosses + 1);
    }
}
