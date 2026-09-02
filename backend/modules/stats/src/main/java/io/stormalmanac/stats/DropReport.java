package io.stormalmanac.stats;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.common.id.StageId;
import java.time.Instant;
import java.util.Map;

/**
 * One player's observation of one stage run.
 *
 * <p>Append-only, immutable, time-ordered, and read almost exclusively as range
 * scans over {@code (stage, item, version)}. That access shape is an LSM tree's
 * native one, and it is the workload phase 7 exists to serve — sized against
 * real traffic rather than an imagined one.
 */
public record DropReport(
        long sequence,
        ProfileId profile,
        StageId stage,
        GameDataVersion gameVersion,
        int runs,
        Map<ItemId, Integer> observedDrops,
        Instant reportedAt
) {
    public DropReport {
        if (runs < 1) throw new IllegalArgumentException("runs must be >= 1");
        observedDrops = Map.copyOf(observedDrops);
    }
}
