package io.stormalmanac.gamedata;

import io.stormalmanac.common.id.ItemId;

/**
 * One row of a stage's drop table, as declared by the game data.
 *
 * <p>This is the <em>declared</em> possibility, not the measured rate. What the
 * optimizer actually consumes is a {@code DropEstimate} from the stats module,
 * with a sample size and a confidence interval attached. The declaration is
 * still load-bearing: a report claiming an item that cannot drop here is
 * rejected on ingest rather than averaged in.
 */
public record Drop(ItemId item, double declaredProbability, int quantityPerHit) {
    public Drop {
        if (declaredProbability < 0 || declaredProbability > 1) {
            throw new IllegalArgumentException("declaredProbability must be in [0,1]");
        }
        if (quantityPerHit < 1) throw new IllegalArgumentException("quantityPerHit must be >= 1");
    }
}
