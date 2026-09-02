package io.stormalmanac.planner;

import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.StageId;
import java.util.List;
import java.util.Map;

/**
 * Why the plan is the plan.
 *
 * <p>Nobody trusts a black box that says grind for three weeks. Shadow prices
 * from the LP relaxation give the sentence a player will actually act on:
 * "4-6 is your binding constraint; 3-4 is 12% less efficient for this goal set."
 *
 * @param shadowPrice   marginal energy cost of one more unit of each item
 * @param bindingStages the constraints actually holding the solution back
 * @param notes         rendered verbatim under the plan
 */
public record Explanation(
        Map<ItemId, Double> shadowPrice,
        List<StageId> bindingStages,
        List<String> notes
) {
    public Explanation {
        shadowPrice = Map.copyOf(shadowPrice);
        bindingStages = List.copyOf(bindingStages);
        notes = List.copyOf(notes);
    }
}
