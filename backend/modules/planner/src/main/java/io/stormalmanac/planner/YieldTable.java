package io.stormalmanac.planner;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.StageId;
import io.stormalmanac.gamedata.Drop;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.Stage;
import io.stormalmanac.stats.DropEstimate;
import io.stormalmanac.stats.DropEstimateRepository;
import io.stormalmanac.stats.PoissonRateInterval;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * How much of an item one run of a stage is worth, and where that number came
 * from.
 *
 * <p>The optimizer's constraint is
 * {@code sum over stages of runs * yield >= demand}, so the coefficient it needs
 * is an <em>expected quantity per run</em>. Two different numbers claim to be
 * that and only one of them always is:
 *
 * <ul>
 *   <li>{@link Drop#expectedYield()} — declared by the bundle, unbounded above,
 *       already an expected quantity. Always usable.
 *   <li>{@link DropEstimate#pointEstimate()} — measured from player reports and
 *       carrying a Wilson interval, which makes it a <em>proportion</em>: the
 *       share of runs that yielded the item. That equals the expected quantity
 *       only for an item that drops at most once per run.
 * </ul>
 *
 * <p>So a measured estimate is preferred over a declaration only where the units
 * provably agree — {@link Drop#isExpressibleAsProbability()} — and the
 * declaration stands everywhere else. The alternative, quietly treating a
 * proportion as a quantity, would understate every multi-drop stage in the
 * catalogue and would do it invisibly.
 *
 * <p><b>A declared yield that says how many runs it was measured over is
 * discounted for how thin that evidence is.</b> The coefficient becomes the
 * lower end of a 95% {@link PoissonRateInterval} on the mean per run, so a stage
 * sampled over a hundred runs has to be visibly better than one sampled over ten
 * thousand before the solver will send a player there. This is the fix for the
 * failure the community benchmark exposed: {@code argmin} over point estimates
 * cannot tell a good stage from a lucky one, and a mean over 105 runs is a high
 * mean about as often as it is a low one. A yield with no sample size behind it
 * — a fixed-reward stage, an upstream that publishes rates without provenance —
 * is used exactly as declared, because there is no sampling error to discount.
 * ADR 0011.
 *
 * <p>What remains of the mismatch above: a <em>measured</em> {@code DropEstimate}
 * is still a proportion with a Wilson interval, still only usable where the units
 * agree, and still not discounted for its own sample size here. That is Phase 6's
 * to finish — nothing publishes an estimate yet — and when it does, the estimate
 * should arrive as a mean and a sample size and take the same path as a declared
 * one, at which point this branch collapses into one rule instead of two.
 *
 * <p>Nothing here consults the network or a database. It is built once per solve
 * from data already loaded, so the solver and the shadow-price re-solves read
 * the same numbers.
 */
public final class YieldTable {

    private final Map<StageId, Map<ItemId, Double>> yields;
    private final Map<StageId, List<ItemId>> measured;
    private final int discounted;

    private YieldTable(
            Map<StageId, Map<ItemId, Double>> yields,
            Map<StageId, List<ItemId>> measured,
            int discounted) {
        this.yields = yields;
        this.measured = measured;
        this.discounted = discounted;
    }

    /** Declared yields only. What a fresh title has before anyone has reported a run. */
    public static YieldTable declared(GameDefinition definition) {
        return of(definition, null);
    }

    /**
     * Declared yields, overridden by measured estimates wherever the units agree.
     *
     * @param estimates may be {@code null}, which means "nothing measured yet"
     */
    public static YieldTable of(GameDefinition definition, DropEstimateRepository estimates) {
        GameDataVersion version = definition.version();
        Map<StageId, Map<ItemId, Double>> yields = new LinkedHashMap<>();
        Map<StageId, List<ItemId>> measured = new HashMap<>();
        int discounted = 0;

        for (Stage stage : definition.stages()) {
            Map<ItemId, Double> row = new LinkedHashMap<>();
            for (Drop drop : stage.drops()) {
                double value = drop.expectedYield();
                if (drop.isSampled()) {
                    // What the sample supports rather than what it happened to
                    // show. Small samples lose more than large ones, which is
                    // the entire point: the alternative ranks a stage by how
                    // lucky its sample was.
                    value = PoissonRateInterval.ofMean(value, drop.sampledRuns()).lower();
                    discounted++;
                }
                if (estimates != null && drop.isExpressibleAsProbability()) {
                    Optional<DropEstimate> found = estimates.find(stage.stageId(), drop.item(), version);
                    if (found.isPresent()) {
                        value = found.get().pointEstimate();
                        measured.computeIfAbsent(stage.stageId(), s -> new ArrayList<>()).add(drop.item());
                    }
                }
                // A stage that declares an item and drops none of it is not a
                // source of it, and a zero coefficient in the model is noise.
                // The discount never causes this: the score bound is strictly
                // positive wherever anything at all was observed, and reaches
                // zero only when the observed total does. So a rare drop gets
                // small, sometimes drastically — one sighting in a thousand runs
                // supports about a fifth of the rate it shows — but a source
                // never silently stops being one.
                if (value > 0) row.put(drop.item(), value);
            }
            yields.put(stage.stageId(), Map.copyOf(row));
        }
        return new YieldTable(Map.copyOf(yields), Map.copyOf(measured), discounted);
    }

    /** Expected quantity of {@code item} from one run of {@code stage}; zero if it does not drop. */
    public double yield(StageId stage, ItemId item) {
        return yields.getOrDefault(stage, Map.of()).getOrDefault(item, 0.0);
    }

    public Map<ItemId, Double> yieldsOf(StageId stage) {
        return yields.getOrDefault(stage, Map.of());
    }

    /** True when this coefficient came from player reports rather than from the bundle. */
    public boolean isMeasured(StageId stage, ItemId item) {
        return measured.getOrDefault(stage, List.of()).contains(item);
    }

    /** How many coefficients in this table are measured. Rendered in the plan's notes. */
    public int measuredCount() {
        return measured.values().stream().mapToInt(List::size).sum();
    }

    /**
     * How many coefficients were discounted for the size of the sample behind
     * them. Rendered in the plan's notes, because a player told to farm a stage
     * deserves to know the number is the conservative end of a sample rather
     * than the middle of one.
     */
    public int discountedCount() {
        return discounted;
    }
}
