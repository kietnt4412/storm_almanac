package io.stormalmanac.stats;

/**
 * A score interval for a <em>mean per run</em>: how many of an item one run
 * yields, and how sure we are of it.
 *
 * <p><b>This is not the same question as {@link WilsonInterval}'s, and that is
 * the whole reason it exists.</b> Wilson answers <em>"what share of runs drop
 * this?"</em> — a binomial proportion, bounded by 1, estimated from a count of
 * successes in a count of trials. This answers <em>"how many of these does a run
 * give?"</em> — a rate, unbounded above, estimated from a total observed over a
 * number of runs. A stage that yields three copies a run has a proportion of 1.0
 * and a mean of 3.0, and the optimizer's constraint
 * {@code sum over stages of runs * yield >= demand} needs the second one. See
 * ADR 0011, which narrows ADR 0006 rather than replacing it: both intervals are
 * right, about different questions.
 *
 * <p>The estimator is the counting analogue of Wilson's — a score interval on a
 * Poisson rate, solving {@code |C - n*lambda| = z*sqrt(n*lambda)} for lambda:
 *
 * <pre>
 * lambda = ( C + z²/2  ±  z * sqrt(C + z²/4) ) / n
 * </pre>
 *
 * <p>Chosen for the same reasons ADR 0006 chose Wilson, which apply harder here
 * because upstream sample sizes run from a hundred runs to forty thousand. The
 * naive {@code C/n ± z*sqrt(C)/n} goes negative on a thin sample and collapses
 * to a point at zero when nothing has dropped yet; this one stays non-negative
 * everywhere, stays wide while the evidence is thin, and at {@code C = 0} gives
 * {@code [0, z²/n]} — "we have not seen it, and here is how big it could still
 * be" — which is the honest answer to a stage nobody has farmed much.
 *
 * <p><b>Poisson is an assumption and it is worth saying which way it is wrong.</b>
 * A run's yield is not really Poisson: a stage that drops exactly one of an item
 * on 40% of runs has variance below Poisson's, and a stage that drops five at
 * once has variance above it. So this interval is conservative for the first and
 * optimistic for the second, and neither error is large next to the thing it is
 * correcting — a hundred-fold difference in sample size that the model was
 * previously not carrying at all. Replacing it needs the per-run distribution,
 * which no upstream publishes and only our own drop reports could ever supply.
 *
 * @param lower inclusive lower bound on the mean per run, never negative
 * @param upper inclusive upper bound
 * @param confidence e.g. 0.95
 */
public record PoissonRateInterval(double lower, double upper, double confidence) {

    /** z for a two-sided 95% interval, matching {@link WilsonInterval}. */
    private static final double Z_95 = 1.959963984540054;

    public PoissonRateInterval {
        if (lower < 0 || lower > upper || Double.isNaN(lower) || Double.isNaN(upper)) {
            throw new IllegalArgumentException("degenerate interval [" + lower + ", " + upper + "]");
        }
    }

    /**
     * @param observed total quantity seen across every run, not a per-run mean.
     *                 A {@code double} because it survives a round trip through a
     *                 stored mean and a sample size, and because an upstream may
     *                 publish a rate it has already averaged.
     * @param runs how many runs those came from; must be positive
     */
    public static PoissonRateInterval of(double observed, long runs) {
        return of(observed, runs, Z_95, 0.95);
    }

    public static PoissonRateInterval of(double observed, long runs, double z, double confidence) {
        if (runs <= 0) {
            // Deliberately not "the full interval". No runs means the number in
            // hand did not come from counting, so there is nothing here to put a
            // confidence interval on and pretending otherwise would let a
            // declared yield be discounted as though it were a thin sample.
            throw new IllegalArgumentException(
                    "a rate interval needs at least one run; " + runs + " is a declared yield");
        }
        if (!(observed >= 0) || Double.isInfinite(observed)) {
            throw new IllegalArgumentException("observed must be finite and non-negative");
        }

        double z2 = z * z;
        double centre = observed + z2 / 2;
        double margin = z * Math.sqrt(observed + z2 / 4);

        return new PoissonRateInterval(
                Math.max(0.0, (centre - margin) / runs),
                (centre + margin) / runs,
                confidence);
    }

    /**
     * The interval for a yield already divided by its sample size.
     *
     * <p>The convenience that matches how game data arrives: upstream tables
     * publish a raw count and a run count, this project stores the quotient and
     * the run count, and multiplying back is exact enough — the product is the
     * count that was there before, to within the rounding of one division.
     */
    public static PoissonRateInterval ofMean(double meanPerRun, long runs) {
        return of(meanPerRun * runs, runs);
    }

    public double width() {
        return upper - lower;
    }

    /**
     * The centre of the interval — the observed mean shrunk upward by {@code
     * z²/2n}, not the observed mean itself. Rendered, never solved against: the
     * solver takes {@link #lower()}, for the reason ADR 0011 gives.
     */
    public double midpoint() {
        return (lower + upper) / 2;
    }
}
