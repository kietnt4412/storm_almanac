package io.stormalmanac.gamedata.banner;

import io.stormalmanac.gamedata.Rarity;

/** "At least one 4-star per 10-pull." Independent of, and evaluated after, pity. */
public record Floor(int everyN, Rarity minimumRarity) {
    public Floor {
        if (everyN < 1) throw new IllegalArgumentException("everyN must be >= 1");
    }
}
