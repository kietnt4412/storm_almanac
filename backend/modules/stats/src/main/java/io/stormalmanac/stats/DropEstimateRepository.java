package io.stormalmanac.stats;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.StageId;
import java.util.List;
import java.util.Optional;

/** Published estimates. The planner reads this and nothing else in the stats module. */
public interface DropEstimateRepository {

    Optional<DropEstimate> find(StageId stage, ItemId item, GameDataVersion version);

    List<DropEstimate> forStage(StageId stage, GameDataVersion version);

    void publish(DropEstimate estimate);
}
