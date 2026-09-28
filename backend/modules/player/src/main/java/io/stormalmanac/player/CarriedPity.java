package io.stormalmanac.player;

import io.stormalmanac.common.id.ProfileId;

/**
 * What a reader says their pity counter shows, under the key a banner carries
 * it by.
 *
 * <p>Stored here and not as the gacha module's {@code PityState}, because the
 * player module is not allowed to know what a banner is: it holds the two
 * numbers a reader typed and the key they were typed against, and the gacha
 * module derives the key from a banner and reads the numbers back. Nothing in
 * this repository reads a game client, so these are always the reader's own
 * report of their screen.
 *
 * @param scopeKey           which counter — one per game, per banner type or
 *                           per banner, as the banner's scope says
 * @param pullsSinceHit      pulls since the last hit on the rarity pulled for
 * @param consecutiveLosses  hits in a row that were not the featured unit
 */
public record CarriedPity(ProfileId profile, String scopeKey, int pullsSinceHit, int consecutiveLosses) {

    public CarriedPity {
        if (scopeKey == null || scopeKey.isBlank()) {
            throw new IllegalArgumentException("a pity counter needs the key it is carried under");
        }
        if (pullsSinceHit < 0) {
            throw new IllegalArgumentException("pullsSinceHit must be >= 0, was " + pullsSinceHit);
        }
        if (consecutiveLosses < 0) {
            throw new IllegalArgumentException("consecutiveLosses must be >= 0, was " + consecutiveLosses);
        }
    }

    /** A counter nobody has reported: nothing pulled since the last hit, no loss carried. */
    public static CarriedPity fresh(ProfileId profile, String scopeKey) {
        return new CarriedPity(profile, scopeKey, 0, 0);
    }
}
