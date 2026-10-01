package io.stormalmanac.gacha;

import io.stormalmanac.gamedata.banner.BannerModel;
import java.util.Arrays;

/**
 * Pity as a state machine, solved exactly.
 *
 * <p>The state a pull depends on is small: how many pulls since the headline
 * rarity last hit, how many featured splits have been lost since one was won,
 * and how many copies are already in hand. Everything else about a banner is a
 * constant. So the distribution after <em>n</em> pulls is <em>n</em> sparse
 * matrix-vector products over a few hundred states, and the answer is the mass
 * that reached the absorbing "enough copies" state. No sampling and no interval:
 * the only error is floating point.
 *
 * <p><b>A drawn guarantee costs this engine nothing, which was not obvious.</b>
 * Punishing: Gray Raven's Themed Construct pool draws its wall uniformly over
 * 80–100 and redraws on every hit, and the apparent options were to carry the
 * drawn threshold as a fourth dimension or to mix twenty-one chains. Neither is
 * needed: conditioned on {@code c} misses the posterior over the threshold is
 * uniform on {@code {max(from, c+1) .. hardAt}}, so it depends on {@code c}
 * alone and the marginal hazard is a function of the pity counter — a rising
 * curve, exactly the shape soft pity already had. The state space keeps its three
 * dimensions and gets a hundred deep instead of sixty.
 * {@link io.stormalmanac.gamedata.banner.PityRule} carries the derivation and
 * ADR 0023 the argument.
 *
 * <p>{@link #expectedPullsToFeatured} needs no linear solve at all, which is
 * worth writing down because reaching for one is the obvious move. Let
 * {@code H(c)} be the expected pulls to the next headline hit from pity count
 * {@code c}. Hard pity makes {@code H(hardAt-1) = 1} and every other value
 * follows backwards from it — {@code H(c) = 1 + (1-p(c))H(c+1)} — because a miss
 * can only ever move the counter forwards. Pity then resets on a hit, so the
 * expected wait from a fresh counter having carried {@code k} losses is
 * {@code L(k) = H(0) + (1-chanceAtHit)L(k+1)}, terminating at the guarantee.
 * Unrolled, that sum is geometric, so the whole answer is two scalars and a
 * {@link Math#pow}: the chain is cyclic but its cycle has one entry point.
 */
public final class MarkovBannerEngine implements BannerEngine {

    /**
     * The exact chain is only exact if it fits in memory. A banner whose wall,
     * guarantee depth and requested copies multiply out past this is refused by
     * name rather than met with an {@link OutOfMemoryError}; nothing either game
     * publishes comes within four orders of magnitude of it.
     */
    private static final long MAX_STATES = 20_000_000L;

    @Override
    public double probabilityOfFeatured(BannerModel banner, PityState from, int pulls, int copies) {
        double[] curve = curveOfFeatured(banner, from, pulls, copies);
        return curve[pulls];
    }

    /**
     * The same answer at every pull count from zero to {@code pulls}: element
     * {@code n} is the probability of at least {@code copies} featured copies
     * within {@code n} pulls.
     *
     * <p>Free, because the chain already walks one pull at a time and the answer
     * after each is the mass it has left behind. The pull planner draws it (C2), so
     * a reader sees the whole shape — how flat the start is, where pity bends it,
     * where the wall makes it certain — and not one number on it. {@link
     * #probabilityOfFeatured} is this curve's last point by construction, so the
     * two can never disagree.
     */
    public double[] curveOfFeatured(BannerModel banner, PityState from, int pulls, int copies) {
        if (pulls < 0) throw new IllegalArgumentException("pulls must be >= 0, was " + pulls);
        if (copies < 1) throw new IllegalArgumentException("copies must be >= 1, was " + copies);

        PullModel model = PullModel.of(banner);
        int walls = model.hardAt();
        int depth = model.featured().guaranteeAfterLoss() + 1;
        long states = (long) walls * depth * copies;
        if (states > MAX_STATES) {
            throw new IllegalArgumentException("banner " + banner.id() + " needs " + states
                    + " exact states for " + copies + " copies (wall " + walls + ", guarantee depth "
                    + depth + "), past the " + MAX_STATES + " this engine will allocate");
        }

        // [pulls since hit][losses carried][copies already in hand], the last
        // dimension stopping one short of the target because reaching it is
        // absorbing and the rest of the state stops mattering.
        double[][][] current = new double[walls][depth][copies];
        double[][][] next = new double[walls][depth][copies];
        current[Math.min(from.pullsSinceHit(), walls - 1)]
               [Math.min(from.consecutiveLosses(), depth - 1)]
               [0] = 1.0;
        double[] curve = new double[pulls + 1];
        curve[0] = chanceLeftBehind(current);

        for (int pull = 0; pull < pulls; pull++) {
            for (double[][] plane : next) {
                for (double[] row : plane) {
                    Arrays.fill(row, 0.0);
                }
            }
            for (int since = 0; since < walls; since++) {
                double hit = model.hitRateAt(since);
                double featuredShare = hit;
                for (int losses = 0; losses < depth; losses++) {
                    double toFeatured = featuredShare * model.featuredChanceAfter(losses);
                    double toOffFeatured = featuredShare - toFeatured;
                    for (int held = 0; held < copies; held++) {
                        double mass = current[since][losses][held];
                        if (mass == 0.0) continue;

                        double missed = mass * (1.0 - hit);
                        if (missed > 0.0) {
                            next[Math.min(since + 1, walls - 1)][losses][held] += missed;
                        }
                        double featured = mass * toFeatured;
                        // Mass that completes the last copy is simply not carried
                        // forward; the answer is read off what is left behind.
                        if (featured > 0.0 && held + 1 < copies) {
                            next[0][0][held + 1] += featured;
                        }
                        double lost = mass * toOffFeatured;
                        if (lost > 0.0) {
                            next[0][Math.min(losses + 1, depth - 1)][held] += lost;
                        }
                    }
                }
            }
            double[][][] swap = current;
            current = next;
            next = swap;
            curve[pull + 1] = chanceLeftBehind(current);
        }
        return curve;
    }

    /**
     * How many featured copies {@code pulls} pulls end with: element {@code k} is
     * the chance of exactly {@code k} for every {@code k} below {@code upTo}, and
     * the last element the chance of {@code upTo} or more (C2.8's copies chart).
     *
     * <p>One walk of the same chain as {@link #curveOfFeatured}, aimed at
     * {@code upTo} copies: the mass still in the chain sits at the copies it has,
     * which is "exactly k", and what left it reached {@code upTo}. So element
     * {@code upTo} is {@code curveOfFeatured(…, upTo)[pulls]}, and the sum of
     * elements from {@code k} on is the chance of at least {@code k}, by
     * construction. The elements sum to one.
     */
    public double[] copiesWithin(BannerModel banner, PityState from, int pulls, int upTo) {
        if (upTo < 1) throw new IllegalArgumentException("upTo must be >= 1, was " + upTo);
        PullModel model = PullModel.of(banner);
        int walls = model.hardAt();
        int depth = model.featured().guaranteeAfterLoss() + 1;
        // Walked again rather than shared with curveOfFeatured, which keeps no
        // state by copies: the two answers are tested to agree instead.
        double[][][] current = new double[walls][depth][upTo];
        double[][][] next = new double[walls][depth][upTo];
        current[Math.min(from.pullsSinceHit(), walls - 1)]
               [Math.min(from.consecutiveLosses(), depth - 1)]
               [0] = 1.0;
        for (int pull = 0; pull < pulls; pull++) {
            for (double[][] plane : next) {
                for (double[] row : plane) {
                    Arrays.fill(row, 0.0);
                }
            }
            for (int since = 0; since < walls; since++) {
                double hit = model.hitRateAt(since);
                for (int losses = 0; losses < depth; losses++) {
                    double toFeatured = hit * model.featuredChanceAfter(losses);
                    double toOffFeatured = hit - toFeatured;
                    for (int held = 0; held < upTo; held++) {
                        double mass = current[since][losses][held];
                        if (mass == 0.0) continue;
                        next[Math.min(since + 1, walls - 1)][losses][held] += mass * (1.0 - hit);
                        if (held + 1 < upTo) next[0][0][held + 1] += mass * toFeatured;
                        next[0][Math.min(losses + 1, depth - 1)][held] += mass * toOffFeatured;
                    }
                }
            }
            double[][][] swap = current;
            current = next;
            next = swap;
        }
        double[] exactly = new double[upTo + 1];
        double remaining = 0.0;
        for (double[][] plane : current) {
            for (double[] row : plane) {
                for (int held = 0; held < upTo; held++) {
                    exactly[held] += row[held];
                    remaining += row[held];
                }
            }
        }
        exactly[upTo] = Math.clamp(1.0 - remaining, 0.0, 1.0);
        return exactly;
    }

    /**
     * The answer is one minus the mass that never got there, rather than the
     * mass that did, and the difference is not a stylistic one. Accumulating
     * arrivals over seventy steps leaves a rounding residual of about 1e-13, so a
     * wall the game guarantees came back as 0.9999999999999895 and a probability
     * once came back above 1.0 — which is not a number to put in front of a
     * player, and not a wall a player can check against the client. Every path
     * out of the chain at certainty leaves the remaining mass exactly zero, so
     * read it there: certainty is exactly 1.0 and impossibility is exactly 0.0,
     * and the residual lands on the answers in between, where it is 1e-16 against
     * a value nobody reads past four decimals.
     */
    private static double chanceLeftBehind(double[][][] current) {
        double remaining = 0.0;
        for (double[][] plane : current) {
            for (double[] row : plane) {
                for (double mass : row) {
                    remaining += mass;
                }
            }
        }
        return Math.clamp(1.0 - remaining, 0.0, 1.0);
    }

    @Override
    public double expectedPullsToFeatured(BannerModel banner, PityState from) {
        PullModel model = PullModel.of(banner);
        int walls = model.hardAt();

        // H(c): expected pulls to the next headline hit, backwards from the wall.
        double[] toHit = new double[walls];
        toHit[walls - 1] = 1.0;
        for (int since = walls - 2; since >= 0; since--) {
            toHit[since] = 1.0 + (1.0 - model.hitRateAt(since)) * toHit[since + 1];
        }

        int guarantee = model.featured().guaranteeAfterLoss();
        int losses = Math.min(from.consecutiveLosses(), guarantee);
        double since = toHit[Math.min(from.pullsSinceHit(), walls - 1)];
        if (losses >= guarantee) {
            return since;
        }
        // Every further loss costs another full wait, and there are at most
        // guarantee - losses of them left: a geometric sum, not a linear solve.
        double lossChance = 1.0 - model.featured().chanceAtHit();
        return since + lossChance * toHit[0] * geometricSum(lossChance, guarantee - losses);
    }

    /** {@code 1 + q + ... + q^(terms-1)}, without looping over a guarantee depth. */
    private static double geometricSum(double q, int terms) {
        if (terms <= 0) return 0.0;
        if (q == 1.0) return terms;
        return (1.0 - Math.pow(q, terms)) / (1.0 - q);
    }

    @Override
    public String method() {
        return "markov-exact";
    }
}
