package io.stormalmanac.gamedata.banner;

/**
 * One pity curve, general enough to express both shipped shapes.
 *
 * <pre>
 * Reverse: 1999      hardAt 70, softFrom 60, softJumpTo 0.04,  softStep 0.025
 * Punishing: GR      hardAt 60, no soft pity
 * PGR "floating"     hardAt 80..100, no soft pity, base 0.015
 * </pre>
 *
 * @param hardAt     the pull number at which the rarity is guaranteed
 * @param softFrom   null when there is no soft pity
 * @param softJumpTo the rate the first pull past {@code softFrom} jumps to
 * @param softStep   the per-pull increment after that jump
 */
public record PityRule(int hardAt, Integer softFrom, Double softJumpTo, Double softStep) {

    public PityRule {
        if (hardAt < 1) throw new IllegalArgumentException("hardAt must be >= 1");
        if (softFrom != null && (softJumpTo == null || softStep == null)) {
            throw new IllegalArgumentException("soft pity needs both softJumpTo and softStep");
        }
        if (softFrom != null && softFrom >= hardAt) {
            throw new IllegalArgumentException("softFrom must precede hardAt");
        }
    }

    public static PityRule hard(int hardAt) {
        return new PityRule(hardAt, null, null, null);
    }

    public boolean hasSoftPity() {
        return softFrom != null;
    }

    /**
     * Rate for the pull taken at {@code pityCount} misses in a row.
     *
     * @param pityCount consecutive misses so far, so the next pull is {@code pityCount + 1}
     */
    public double rateAt(int pityCount, double baseRate) {
        int pull = pityCount + 1;
        if (pull >= hardAt) return 1.0;
        if (!hasSoftPity() || pull <= softFrom) return baseRate;
        double rate = softJumpTo + softStep * (pull - softFrom - 1);
        return Math.min(1.0, rate);
    }
}
