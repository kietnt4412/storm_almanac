package io.stormalmanac.gacha;

import io.stormalmanac.gamedata.banner.PityScope;

/**
 * A player's carried pity, scoped as the banner declares.
 *
 * <p>{@code scopeKey} is what makes Punishing: Gray Raven expressible: pity
 * carries within a banner <em>type</em>, so a character-banner counter must not
 * leak into a weapon banner. The key is derived from the scope, never assumed
 * to be global.
 */
public record PityState(PityScope scope, String scopeKey, int pullsSinceHit, boolean guaranteedFeatured) {

    public static PityState fresh(PityScope scope, String scopeKey) {
        return new PityState(scope, scopeKey, 0, false);
    }
}
