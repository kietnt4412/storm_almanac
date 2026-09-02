package io.stormalmanac.gacha;

import io.stormalmanac.gamedata.Rarity;

/** One pull. {@code featured} is only meaningful when the pull hit the headline rarity. */
public record PullResult(Rarity rarity, boolean featured, PityState stateAfter) {}
