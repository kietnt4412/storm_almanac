package io.stormalmanac.stats;

/**
 * A Wilson score interval for a binomial proportion.
 *
 * <p>Chosen over the normal approximation because drop-rate samples are small
 * and rates are near zero, which is exactly where the naive interval produces
 * nonsense — negative lower bounds, and a zero-width interval when no hit has
 * been observed yet. The Wilson interval stays inside [0,1] and stays wide
 * while the sample is thin, which is the honest thing to render next to a
 * number a player is about to spend three weeks acting on.
 *
 * @param lower inclusive lower bound, in [0,1]
 * @param upper inclusive upper bound, in [0,1]
 * @param confidence e.g. 0.95
 */
public record WilsonInterval(double lower, double upper, double confidence) {

    /** z for a two-sided 95% interval. The only level the UI currently shows. */
    private static final double Z_95 = 1.959963984540054;

    public WilsonInterval {
        if (lower < 0 || upper > 1 || lower > upper) {
            throw new IllegalArgumentException("degenerate interval [" + lower + ", " + upper + "]");
        }
    }

    public static WilsonInterval of(int successes, int trials) {
        return of(successes, trials, Z_95, 0.95);
    }

    public static WilsonInterval of(int successes, int trials, double z, double confidence) {
        if (trials < 0) throw new IllegalArgumentException("trials must not be negative");
        if (successes < 0 || successes > trials) {
            throw new IllegalArgumentException("successes must be in [0, trials]");
        }
        if (trials == 0) {
            // No evidence at all is the full interval, not a point at zero.
            return new WilsonInterval(0.0, 1.0, confidence);
        }

        double n = trials;
        double pHat = successes / n;
        double z2 = z * z;
        double denominator = 1 + z2 / n;
        double centre = (pHat + z2 / (2 * n)) / denominator;
        double margin = z * Math.sqrt(pHat * (1 - pHat) / n + z2 / (4 * n * n)) / denominator;

        return new WilsonInterval(
                clamp(centre - margin),
                clamp(centre + margin),
                confidence);
    }

    /** The centre of the interval, which is the shrunk estimate the solver should use. */
    public double midpoint() {
        return (lower + upper) / 2;
    }

    public double width() {
        return upper - lower;
    }

    private static double clamp(double v) {
        return Math.min(1.0, Math.max(0.0, v));
    }
}
