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
 * @param demand        what the goals need of each item before the inventory is
 *                      taken off, as {@link Demand#quantities()} has it: gates
 *                      added, crossed states left out (C2.8). The inventory
 *                      screen sets the bag against it, which is the plan's own
 *                      idea of need rather than a second one worked out there
 * @param yields        what one run of each stage in the plan pays, as the solve
 *                      counted it: the {@link YieldTable} it used, so a sampled
 *                      rate arrives discounted (ADR 0011) and a page drawing
 *                      where the energy goes never re-reads the bundle (C2.8)
 */
public record Explanation(
        Map<ItemId, Double> shadowPrice,
        List<StageId> bindingStages,
        List<Note> remarks,
        List<String> payingFor,
        Map<ItemId, Integer> demand,
        Map<StageId, Map<ItemId, Double>> yields
) {
    public Explanation {
        shadowPrice = Map.copyOf(shadowPrice);
        bindingStages = List.copyOf(bindingStages);
        remarks = List.copyOf(remarks);
        payingFor = List.copyOf(payingFor);
        demand = Map.copyOf(demand);
        yields = Map.copyOf(yields);
    }

    /** An explanation that carries no demand and no yields, as one built before C2.8 did. */
    public Explanation(
            Map<ItemId, Double> shadowPrice, List<StageId> bindingStages, List<Note> remarks, List<String> payingFor) {
        this(shadowPrice, bindingStages, remarks, payingFor, Map.of(), Map.of());
    }

    /** The remarks' sentences alone, in order: what a page older than the kinds renders. */
    public List<String> notes() {
        return remarks.stream().map(Note::text).toList();
    }
}
