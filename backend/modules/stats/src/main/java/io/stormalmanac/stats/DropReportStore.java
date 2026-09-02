package io.stormalmanac.stats;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.StageId;
import java.util.stream.Stream;

/**
 * The report log, as an append-and-scan interface.
 *
 * <p>This is the seam the whole two-track structure hangs on. The Postgres
 * implementation ships in phase 6 and stays in the codebase forever; the
 * {@code almanac-store} implementation joins it in phase 7, selectable by
 * config, and the benchmark between them gets published either way.
 *
 * <p>The interface is deliberately narrow — append, and range scan in key
 * order — because that is all the aggregation job needs and it is exactly what
 * an LSM tree is good at. Anything wider would be an interface designed to
 * flatter the hand-built implementation.
 *
 * @see io.stormalmanac.store
 */
public interface DropReportStore {

    /**
     * @return the assigned sequence number, monotonic and durable once returned
     */
    long append(DropReport report);

    /**
     * Reports for one key, oldest first. The caller closes the stream.
     */
    Stream<DropReport> scan(StageId stage, ItemId item, GameDataVersion version);

    /** Everything filed after {@code exclusiveFromSequence}; drives incremental aggregation. */
    Stream<DropReport> since(long exclusiveFromSequence, int limit);

    /** Highest durable sequence, or {@code -1} when empty. Recovery starts here. */
    long highestSequence();
}
