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
 * @param remarks       what the plan says about itself, each with its kind
 *                      ({@link Note}), in the order written
 * @param payingFor     the upgrade steps the goals are paying for, as
 *                      {@link Demand#steps()} lists them. Data rather than a
 *                      note: until 2026-09-26 it was the sentence "Paying for 3
 *                      upgrade step(s): Lucia to level-65, …", and a state the
 *                      game gives no word for could only be printed as its id.
 *                      The page names it the way it names every other state
 *                      (D5's S8), and that needs the step, not the sentence
 */
public record Explanation(
        Map<ItemId, Double> shadowPrice,
        List<StageId> bindingStages,
        List<Note> remarks,
        List<String> payingFor
) {
    public Explanation {
        shadowPrice = Map.copyOf(shadowPrice);
        bindingStages = List.copyOf(bindingStages);
        remarks = List.copyOf(remarks);
        payingFor = List.copyOf(payingFor);
    }

    /** The remarks' sentences alone, in order: what a page older than the kinds renders. */
    public List<String> notes() {
        return remarks.stream().map(Note::text).toList();
    }
}
