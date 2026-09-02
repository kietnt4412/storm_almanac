package io.stormalmanac.stats;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.StageId;

/**
 * The aggregated rate the optimizer actually consumes.
 *
 * <p>Keyed by version because a patch can change a drop table silently. An
 * estimate never crosses a version boundary; it is recomputed from the reports
 * filed against that version.
 *
 * @param interval Wilson score interval, not a naive proportion — at n=7 a
 *                 naive 43% is noise, and the interval is what says so
 */
public record DropEstimate(
        StageId stage,
        ItemId item,
        GameDataVersion version,
        double pointEstimate,
        int sampleSize,
        WilsonInterval interval,
        Provenance provenance
) {

    /** How much the optimizer should trust this. Narrow interval, high weight. */
    public double confidenceWeight() {
        double width = interval.width();
        return width <= 0 ? 1.0 : 1.0 / (1.0 + width);
    }
}
