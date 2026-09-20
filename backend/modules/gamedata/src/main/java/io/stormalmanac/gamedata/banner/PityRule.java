package io.stormalmanac.gamedata.banner;

/**
 * One pity curve, general enough to express both shipped shapes.
 *
 * <pre>
 * Reverse: 1999      hardAt 70, softFrom 60, softJumpTo 0.04,  softStep 0.025
 * Punishing: GR      hardAt 60, no soft pity
 * PGR "floating"     drawnFrom 80, hardAt 100, no soft pity, base 0.015
 * </pre>
 *
 * <p><b>A guarantee may be drawn rather than fixed.</b> Punishing: Gray Raven's
 * Themed Construct pool generates a threshold uniformly over 80–100 at the start
 * of every pity cycle, redraws it on every S-Rank, and <em>does not tell the
 * player the drawn value</em> — the counter reads {@code 8/80~100}. So
 * {@code hardAt} alone cannot describe the banner: writing it as 80 promises a
 * wall the game does not honour and writing it as 100 forgets the other twenty.
 * {@code drawnFrom} is the bottom of that range and {@code hardAt} stays what it
 * has always been — the pull at which the rarity is certain.
 *
 * <p><b>The draw does not survive into {@link #rateAt}, and that is a theorem
 * rather than an approximation.</b> Condition on {@code c} misses. A miss
 * happens when the curve roll fails <em>and</em> the wall has not arrived, so
 * {@code c} misses are impossible for any {@code W <= c} and equally likely for
 * every {@code W > c} — the curve rolls are independent of {@code W}. The
 * posterior is therefore uniform on {@code {max(drawnFrom, c+1) .. hardAt}}, it
 * depends on nothing but {@code c}, and the chance this very pull is the forced
 * one is {@code 1 / (hardAt - c)}. A drawn guarantee is a rising hazard curve;
 * it is not a hidden variable the caller has to carry. See
 * {@code docs/adr/0023-a-drawn-guarantee-is-a-rate-curve.md}.
 *
 * @param hardAt     the pull number at which the rarity is guaranteed
 * @param softFrom   null when there is no soft pity
 * @param softJumpTo the rate the first pull past {@code softFrom} jumps to
 * @param softStep   the per-pull increment after that jump
 * @param drawnFrom  null when the guarantee is fixed at {@code hardAt}; otherwise
 *                   the bottom of the range the threshold is drawn from
 */
public record PityRule(int hardAt, Integer softFrom, Double softJumpTo, Double softStep, Integer drawnFrom) {

    public PityRule {
        if (hardAt < 1) throw new IllegalArgumentException("hardAt must be >= 1");
        if (softFrom != null && (softJumpTo == null || softStep == null)) {
            throw new IllegalArgumentException("soft pity needs both softJumpTo and softStep");
        }
        if (softFrom != null && softFrom >= hardAt) {
            throw new IllegalArgumentException("softFrom must precede hardAt");
        }
        if (drawnFrom != null && drawnFrom < 1) {
            throw new IllegalArgumentException("drawnFrom must be >= 1");
        }
        // Refused rather than quietly accepted: a range of one is a fixed wall,
        // and saying it the long way hides that from anyone reading the bundle.
        if (drawnFrom != null && drawnFrom >= hardAt) {
            throw new IllegalArgumentException(
                    "drawnFrom must precede hardAt; a guarantee drawn from a range of one is a fixed"
                            + " wall, so write it as PityRule.hard(" + hardAt + ")");
        }
    }

    /** The shape every banner had before a game drew its wall. */
    public PityRule(int hardAt, Integer softFrom, Double softJumpTo, Double softStep) {
        this(hardAt, softFrom, softJumpTo, softStep, null);
    }

    public static PityRule hard(int hardAt) {
        return new PityRule(hardAt, null, null, null, null);
    }

    /** A guarantee drawn uniformly over {@code [from, to]}, redrawn on every hit. */
    public static PityRule drawn(int from, int to) {
        return new PityRule(to, null, null, null, from);
    }

    public boolean hasSoftPity() {
        return softFrom != null;
    }

    /** True when the threshold is drawn per cycle rather than fixed at {@link #hardAt()}. */
    public boolean hasDrawnGuarantee() {
        return drawnFrom != null;
    }

    /**
     * What the curve alone offers at {@code pityCount} misses — base rate, or the
     * soft-pity ramp once it starts. No guarantee of any kind is applied.
     */
    public double curveRateAt(int pityCount, double baseRate) {
        int pull = pityCount + 1;
        if (!hasSoftPity() || pull <= softFrom) return baseRate;
        return Math.min(1.0, softJumpTo + softStep * (pull - softFrom - 1));
    }

    /**
     * Rate for the pull taken at {@code pityCount} misses in a row, with a drawn
     * guarantee integrated out.
     *
     * @param pityCount consecutive misses so far, so the next pull is {@code pityCount + 1}
     */
    public double rateAt(int pityCount, double baseRate) {
        int pull = pityCount + 1;
        if (pull >= hardAt) return 1.0;
        double curve = curveRateAt(pityCount, baseRate);
        if (!hasDrawnGuarantee() || pull < drawnFrom) return curve;
        // Uniform posterior on {pull .. hardAt}, so this pull is the forced one
        // with probability 1/(hardAt - pull + 1); otherwise the curve decides.
        double forced = 1.0 / (hardAt - pull + 1);
        return forced + (1.0 - forced) * curve;
    }

    /**
     * Rate for a caller that knows the threshold, because it drew one.
     *
     * <p>This is the sampling road and {@link #rateAt} is the analytic one. Both
     * exist so that the two engines can take different ones: a simulation that
     * reused the integrated curve would agree with the exact chain about the
     * marginalisation by construction, and prove nothing about it.
     */
    public double rateAtWall(int pityCount, double baseRate, int wall) {
        if (pityCount + 1 >= wall) return 1.0;
        return curveRateAt(pityCount, baseRate);
    }
}
