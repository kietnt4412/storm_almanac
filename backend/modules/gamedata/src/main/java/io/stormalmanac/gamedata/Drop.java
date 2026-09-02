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
 */
public record Drop(ItemId item, double expectedYield) {

    public Drop {
        if (!(expectedYield >= 0) || Double.isInfinite(expectedYield)) {
            // Also rejects NaN, which fails every ordinary comparison.
            throw new IllegalArgumentException("expectedYield must be finite and non-negative");
        }
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
