package io.stormalmanac.gamedata;

import io.stormalmanac.common.id.ItemId;

/**
 * One row of a stage's drop table: the expected quantity of an item yielded by a
 * single run.
 *
 * <p><b>This is a yield, not a probability.</b> A stage can drop several copies
 * of an item in one run, so the value is unbounded above — real upstream data
 * carries values well past 1.0. An earlier version of this record stored a
 * probability constrained to {@code [0,1]} alongside a separate quantity, which
 * would have rejected most of the upstream dataset on ingest. See
 * {@code docs/prior-art.md} section 4.1.
 *
 * <p>Expected yield is also the quantity the optimizer actually needs. The
 * constraint is
 *
 * <pre>
 * sum over stages s of  x_s * yield[s,i]  &gt;=  demand_i
 * </pre>
 *
 * so storing probability and quantity as separate factors would buy nothing and
 * invite the bug where one is used without the other.
 *
 * <p>This is the <em>declared</em> yield from the game data. What the optimizer
 * consumes is a {@code DropEstimate} from the stats module, with a sample size
 * and a confidence interval attached. The declaration stays load-bearing: a
 * report claiming an item that cannot drop here is rejected on ingest rather
 * than averaged in.
 *
 * <p><b>A yield can carry how many runs it was measured over, and it must.</b>
 * Upstream drop tables are samples, and their sample sizes differ by two orders
 * of magnitude — a mean over a hundred runs and a mean over forty thousand are
 * the same number to a model that does not carry {@code sampledRuns}, so the
 * noisy one wins an {@code argmin} about as often as it deserves to lose it.
 * That was measured, not feared: every disagreement over 25% between this
 * project's stage ranking and a published community guide came from this, and
 * nothing else. See {@code docs/benchmarks/reverse-1999-community-answers.md},
 * {@code docs/adr/0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md} and
 * {@code io.stormalmanac.stats.PoissonRateInterval}, which is where the sample
 * size turns back into a number the solver can use. This module only carries it:
 * what to do about a thin sample is a statistics decision, not a catalogue one.
 *
 * @param sampledRuns how many runs the yield was observed over, or <b>0 for a
 *                    yield the data <em>declares</em> rather than measures</b> —
 *                    a fixed-reward stage, or an upstream that publishes rates
 *                    without saying where they came from. Zero is "take this at
 *                    face value", not "measured badly": there is no such thing
 *                    as a mean over no runs, so the value could not have come
 *                    from sampling.
 */
public record Drop(ItemId item, double expectedYield, long sampledRuns) {

    public Drop {
        if (!(expectedYield >= 0) || Double.isInfinite(expectedYield)) {
            // Also rejects NaN, which fails every ordinary comparison.
            throw new IllegalArgumentException("expectedYield must be finite and non-negative");
        }
        if (sampledRuns < 0) {
            throw new IllegalArgumentException("sampledRuns must not be negative");
        }
    }

    /**
     * A declared yield: a number the data states rather than one it measured.
     *
     * <p>Kept as a constructor rather than pushed onto every call site because
     * most of them mean exactly this — a fixture, a synthetic title, a game whose
     * data publishes rates without provenance. An upstream that <em>does</em>
     * publish a sample size has to say so explicitly, which is the right way
     * round: forgetting the sample size should look like an absence of evidence,
     * not like an assertion of certainty.
     */
    public Drop(ItemId item, double expectedYield) {
        this(item, expectedYield, 0);
    }

    /** True when this yield came from a stated number of observed runs. */
    public boolean isSampled() {
        return sampledRuns > 0;
    }

    /**
     * True when this drop can be read as a per-run probability.
     *
     * <p>Only meaningful for items that drop at most once per run. Yields above
     * 1.0 are unambiguously multi-drop; yields at or below it are ambiguous, so
     * treat this as a display hint and never as a modelling decision.
     */
    public boolean isExpressibleAsProbability() {
        return expectedYield <= 1.0;
    }
}
