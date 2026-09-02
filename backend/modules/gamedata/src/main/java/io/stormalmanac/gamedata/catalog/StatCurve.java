package io.stormalmanac.gamedata.catalog;

import java.util.List;

/**
 * A stat as a function of level and ascension tier.
 *
 * <p>Stored as breakpoints plus linear interpolation between them, because that
 * is how the community data repositories publish it and inventing a closed form
 * would mean inventing numbers.
 *
 * @param breakpoints ordered by {@link Breakpoint#level}
 */
public record StatCurve(String stat, List<Breakpoint> breakpoints) {

    public record Breakpoint(int ascensionTier, int level, double value) {}

    public StatCurve {
        breakpoints = List.copyOf(breakpoints);
    }

    public double valueAt(int ascensionTier, int level) {
        Breakpoint low = null;
        for (Breakpoint b : breakpoints) {
            if (b.ascensionTier() != ascensionTier) continue;
            if (b.level() == level) return b.value();
            if (b.level() < level) {
                low = b;
            } else if (low != null) {
                double span = b.level() - low.level();
                double t = (level - low.level()) / span;
                return low.value() + t * (b.value() - low.value());
            }
        }
        if (low == null) {
            throw new IllegalArgumentException(
                    "no breakpoint for " + stat + " at tier " + ascensionTier + " level " + level);
        }
        return low.value();
    }
}
