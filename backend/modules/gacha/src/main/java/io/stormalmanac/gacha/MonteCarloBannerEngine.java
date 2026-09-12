package io.stormalmanac.gacha;

import io.stormalmanac.gamedata.banner.BannerModel;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.ToLongFunction;

/**
 * The same questions answered by pulling the banner a great many times.
 *
 * <p>It exists to disagree with {@link MarkovBannerEngine}. Two methods sharing
 * nothing but {@link PullModel}'s three branches — one walking a probability
 * distribution, one walking a random number generator — landing on the same
 * number is a correctness argument about the pity model that neither can make
 * alone, and the first place they part company is a real bug rather than a
 * rounding difference.
 *
 * <p><b>Seeded, and that is the point.</b> An unseeded simulation turns every
 * agreement assertion into a coin flip that passes most of the time, which in a
 * pipeline is worse than no assertion: the failure arrives attached to an
 * innocent commit. A fixed seed makes the cross-check a regression test — the
 * same trials every run, so a change in the answer is a change in the code. The
 * statistical claim is a separate one and it is made in the test: the gap
 * between the engines is small <em>relative to this simulation's own standard
 * error</em>, which is what says they agree rather than that one seed was kind.
 *
 * <p>Parallel across virtual threads, and deterministic anyway. The generators
 * are split sequentially on the calling thread before any task starts, each
 * chunk runs a fixed number of trials, and the results are summed as integers in
 * chunk order — so the answer does not depend on which thread finished first.
 */
public final class MonteCarloBannerEngine implements BannerEngine {

    /**
     * Trials enough that the agreement criterion is a bound rather than a hope.
     *
     * <p><b>The plan says 100 000 and it asks for agreement within 0.3%, and those
     * two numbers do not fit together.</b> At a hundred thousand trials the
     * standard error of a mid-range probability is about 0.16 percentage points,
     * so 0.3 points is under two of them: a gap that size is ordinary sampling
     * noise, not a disagreement. Asked ninety questions, the cross-check found it
     * on the first run — the debut banner over thirty pulls, exact 0.139616
     * against simulated 0.136550, a gap of 0.31 points at 2.80 standard errors.
     * Nothing was wrong with either engine.
     *
     * <p>So the trial count follows from the tolerance rather than from a round
     * number. Half a million trials puts the standard error at 0.07 points at
     * worst, which makes 0.3 points a four-sigma bound — a gap that large is a bug
     * about 1 time in 16 000 rather than 1 in 200. The criterion is the plan's; the
     * sample size needed to assert it honestly is five times what the plan
     * guessed.
     */
    public static final int DEFAULT_TRIALS = 500_000;

    // Arbitrary and fixed. The value means nothing; every number the tests pin is
    // this seed's, so changing it rewrites them all and is not a tidy-up.
    private static final long DEFAULT_SEED = 20260912L;

    private static final int CHUNKS = 64;

    private final int trials;
    private final long seed;

    public MonteCarloBannerEngine() {
        this(DEFAULT_TRIALS, DEFAULT_SEED);
    }

    public MonteCarloBannerEngine(int trials, long seed) {
        if (trials < 1) throw new IllegalArgumentException("trials must be >= 1, was " + trials);
        this.trials = trials;
        this.seed = seed;
    }

    public int trials() {
        return trials;
    }

    /**
     * The standard error of a probability this engine reports, so a caller can
     * state how close agreement had to be rather than asserting a number that
     * happened to pass.
     */
    public double standardErrorAt(double probability) {
        return Math.sqrt(Math.max(0.0, probability * (1.0 - probability)) / trials);
    }

    @Override
    public double probabilityOfFeatured(BannerModel banner, PityState from, int pulls, int copies) {
        if (pulls < 0) throw new IllegalArgumentException("pulls must be >= 0, was " + pulls);
        if (copies < 1) throw new IllegalArgumentException("copies must be >= 1, was " + copies);
        PullModel model = PullModel.of(banner);

        long reached = run(chunk -> {
            long hits = 0;
            for (int trial = 0, of = chunk.trials(); trial < of; trial++) {
                PityState state = from;
                int held = 0;
                for (int pull = 0; pull < pulls && held < copies; pull++) {
                    PullResult result = model.draw(state, chunk.random());
                    state = result.stateAfter();
                    if (result.featured()) held++;
                }
                if (held >= copies) hits++;
            }
            return hits;
        });
        return (double) reached / trials;
    }

    @Override
    public double expectedPullsToFeatured(BannerModel banner, PityState from) {
        PullModel model = PullModel.of(banner);
        long ceiling = model.worstCasePulls();

        long total = run(chunk -> {
            long pulls = 0;
            for (int trial = 0, of = chunk.trials(); trial < of; trial++) {
                PityState state = from;
                long taken = 0;
                while (true) {
                    PullResult result = model.draw(state, chunk.random());
                    state = result.stateAfter();
                    taken++;
                    if (result.featured()) break;
                    if (taken > ceiling) {
                        // Hard pity and the featured guarantee between them make
                        // this unreachable. Arriving here means the transitions
                        // are wrong, not that the trial was unlucky.
                        throw new IllegalStateException("a trial passed the worst case of " + ceiling
                                + " pulls without the featured unit, which the rules forbid");
                    }
                }
                pulls += taken;
            }
            return pulls;
        });
        return (double) total / trials;
    }

    /** One chunk's work: its own generator and its own fixed share of the trials. */
    private record Chunk(SplittableRandom random, int trials) {}

    /**
     * Splits the generators sequentially, runs the chunks on virtual threads, and
     * adds the counts back in chunk order so the total is exact and repeatable.
     */
    private long run(ToLongFunction<Chunk> work) {
        int chunks = Math.min(trials, CHUNKS);
        int each = trials / chunks;
        int remainder = trials % chunks;

        SplittableRandom root = new SplittableRandom(seed);
        List<Callable<Long>> tasks = new ArrayList<>(chunks);
        for (int chunk = 0; chunk < chunks; chunk++) {
            Chunk slice = new Chunk(root.split(), each + (chunk < remainder ? 1 : 0));
            tasks.add(() -> work.applyAsLong(slice));
        }

        try (ExecutorService threads = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Long>> running = new ArrayList<>(chunks);
            for (Callable<Long> task : tasks) {
                running.add(threads.submit(task));
            }
            long total = 0;
            for (Future<Long> future : running) {
                total += future.get();
            }
            return total;
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("simulation interrupted", interrupted);
        } catch (ExecutionException failed) {
            Throwable cause = failed.getCause();
            if (cause instanceof RuntimeException runtime) throw runtime;
            throw new IllegalStateException("simulation failed", cause);
        }
    }

    @Override
    public String method() {
        return "monte-carlo";
    }
}
