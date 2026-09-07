package io.stormalmanac.planner;

import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gamedata.Availability;
import io.stormalmanac.gamedata.Craft;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Reward;
import io.stormalmanac.gamedata.Shop;
import io.stormalmanac.gamedata.Source;
import io.stormalmanac.gamedata.Stage;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.integer.IntegerStrategy;
import org.ojalgo.type.context.NumberContext;

/**
 * The mixed-integer program itself, and nothing else.
 *
 * <pre>
 * minimise    sum_s x_s * energy_s
 * subject to  for every relevant item i:
 *               sum_s x_s * yield[s,i]
 *             + sum_c y_c * (produce[c,i] - consume[c,i])  &gt;=  demand_i - inventory_i
 *             x_s, y_c integer and &gt;= 0
 * </pre>
 *
 * <p>No repositories, no clock, no Spring: inputs in, an answer or a refusal
 * out. That is what makes the shadow-price re-solves affordable and what makes
 * the model testable without a database.
 *
 * <p><b>Crafting recursion is not a feature here, it is an absence.</b> A
 * tier-3 material crafted from tier-2 materials that are themselves crafted
 * needs no expansion pass: every craft is a variable, every intermediate
 * material is a constraint row, and the solver walks the chain because the
 * algebra makes it. A recursive expansion instead would have to pick a depth,
 * and would have to choose between routes the solver can simply price.
 *
 * <h2>What this model does not yet contain</h2>
 * <ul>
 *   <li><b>Shops and rewards.</b> Both are {@link Source}s and neither is a
 *       variable yet. A shop's cap is "n per period" and a reward's is "once per
 *       day", and a model with no time axis has no honest place to put either;
 *       uncapped, a shop lets the solver buy its way out of every constraint,
 *       which is the failure {@link Shop} was written to warn about. Items whose
 *       only source is one of these are reported as unreachable <em>by name</em>
 *       rather than quietly costed at zero.
 *   <li><b>Weekday rotation.</b> A stage open on Tuesdays and Fridays is treated
 *       as open, because a plan spanning days reaches every weekday. What is
 *       honoured is expiry and release: a stage that has closed, or has not yet
 *       opened, is not a source. Rotation starts to matter when the model gains
 *       a time axis, which is also where "fewest days" becomes a different plan
 *       rather than the same one divided by a constant.
 *   <li><b>Fodder.</b> Consuming a class of items for progress on another item
 *       of that class is a sink the demand vector does not yet express.
 * </ul>
 */
final class EnergyMip {

    /**
     * A conversion costs no energy, so a plan carrying a pointless craft scores
     * exactly as well as one without it. This weight breaks that tie towards
     * fewer conversions without pricing them: it stays below one unit of energy
     * for any plan with fewer than a million conversions, so it can never trade
     * a real energy saving away.
     *
     * <p><b>It is applied only when the craft graph has a cycle, and that
     * restraint is load-bearing.</b> Energy costs are whole numbers and so are
     * run counts, so without this weight the objective is integral — and an
     * integral objective lets branch-and-bound discard any node whose bound is
     * within one of the incumbent, which is most of the tree. Adding a
     * millionth to every conversion takes that away and asks the solver to
     * resolve ten significant digits instead. On the fixture nobody noticed; on
     * a real patch, where a currency demand runs to six figures, ojAlgo
     * recursed until the stack gave out. The tie-break only ever mattered for a
     * lossless conversion cycle, so it is now paid for only where one exists.
     */
    private static final double CONVERSION_TIE_BREAK = 1.0e-6;

    /**
     * How close to optimal is close enough to stop searching.
     *
     * <p>ojAlgo's default asks for seven significant digits, which on a plan
     * costing a few thousand Activity means proving that no arrangement saves a
     * hundredth of a point. That proof is most of the running time and none of
     * the value: four significant digits is a fifth of one stage run on a real
     * goal set, and no player can act on the difference. Small models are
     * unaffected — four digits is exact for a plan costing 105.
     *
     * <p>It is stated in the plan's notes when a solve stops on the budget rather
     * than on optimality, because "cheapest" and "cheapest I could prove in two
     * seconds" are different claims.
     */
    private static final NumberContext GAP = NumberContext.of(4, 4);

    private EnergyMip() {}

    /**
     * @param stageVariables how many stages survived pruning and became variables
     * @param craftVariables how many crafts did
     * @param constraints    how many item balance rows the model carried
     * @param provenOptimal  false when the search stopped on its time budget, so
     *                       this is the cheapest plan found rather than the
     *                       cheapest plan
     * @param optimalityGap  how much of this plan's energy could conceivably be
     *                       saved, as a fraction: the distance to the linear
     *                       relaxation's bound, which no integer plan can beat.
     *                       Zero when optimality was proven, {@code NaN} when the
     *                       bound could not be computed
     */
    record Outcome(
            List<StageRun> stageRuns,
            List<Conversion> conversions,
            int totalEnergy,
            int stageVariables,
            int craftVariables,
            int constraints,
            boolean provenOptimal,
            double optimalityGap
    ) {}

    /** Everything the model reads. Deliberately a value: the same inputs give the same plan. */
    record Inputs(
            GameDefinition definition,
            YieldTable yields,
            Map<ItemId, Integer> inventory,
            Map<ItemId, Integer> demand,
            Instant at,
            long budgetMillis
    ) {
        int inventoryOf(ItemId item) {
            return inventory.getOrDefault(item, 0);
        }

        int demandOf(ItemId item) {
            return demand.getOrDefault(item, 0);
        }

        Inputs withDemand(Map<ItemId, Integer> replacement) {
            return new Inputs(definition, yields, inventory, replacement, at, budgetMillis);
        }

        Inputs withBudget(long millis) {
            return new Inputs(definition, yields, inventory, demand, at, millis);
        }
    }

    static Outcome solve(Inputs in) {
        if (in.demand().isEmpty()) {
            return new Outcome(List.of(), List.of(), 0, 0, 0, 0, true, 0.0);
        }

        List<Stage> stages = open(in.definition().stages(), Stage::availability, in.at());
        List<Craft> crafts = open(in.definition().crafts(), Craft::availability, in.at());
        Set<ItemId> relevant = relevantItems(in.demand().keySet(), crafts);

        requireReachable(in, stages, crafts, relevant);

        // Prune: a stage dropping nothing anybody needs is a variable the
        // branch-and-bound tree pays for and never uses.
        List<Stage> useful = stages.stream()
                .filter(s -> relevant.stream().anyMatch(i -> in.yields().yield(s.stageId(), i) > 0))
                .toList();
        List<Craft> usefulCrafts = crafts.stream()
                .filter(c -> c.produces().stream().anyMatch(p -> relevant.contains(p.item())))
                .toList();

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.options.time_abort = in.budgetMillis();
        model.options.integer(IntegerStrategy.DEFAULT.withGapTolerance(GAP));

        Map<ItemId, Long> ceiling = requirementCeiling(in, usefulCrafts, relevant);

        List<Variable> runs = new ArrayList<>(useful.size());
        for (Stage stage : useful) {
            Variable variable = model.newVariable("run:" + stage.stageId().value())
                    .lower(0).integer(true).weight(stage.energyCost());
            capRuns(variable, cap(stage, in.yields(), relevant, ceiling));
            runs.add(variable);
        }
        double conversionWeight = hasCycle(usefulCrafts) ? CONVERSION_TIE_BREAK : 0.0;
        List<Variable> made = new ArrayList<>(usefulCrafts.size());
        for (Craft craft : usefulCrafts) {
            Variable variable = model.newVariable("craft:" + craft.id())
                    .lower(0).integer(true).weight(conversionWeight);
            capRuns(variable, cap(craft, relevant, ceiling));
            made.add(variable);
        }

        for (ItemId item : relevant) {
            Expression balance = model.newExpression("item:" + item.value());
            for (int i = 0; i < useful.size(); i++) {
                double yield = in.yields().yield(useful.get(i).stageId(), item);
                if (yield > 0) balance.set(runs.get(i), yield);
            }
            for (int c = 0; c < usefulCrafts.size(); c++) {
                int net = net(usefulCrafts.get(c), item);
                if (net != 0) balance.set(made.get(c), net);
            }
            balance.lower(in.demandOf(item) - in.inventoryOf(item));
        }

        Optimisation.Result result;
        try {
            result = model.minimise();
        } catch (RuntimeException | StackOverflowError e) {
            // ojAlgo's branch-and-bound recurses per node and hands failures back
            // wrapped from its own worker pool, so a tree too deep for the stack
            // arrives here rather than as a state. Not swallowed: this is the
            // documented trigger for the OR-Tools fallback ADR 0004 names, and a
            // caller needs to know the difference between "impossible" and "the
            // solver fell over".
            throw new IllegalStateException(
                    "the solver failed on " + in.definition().game().id() + " "
                            + in.definition().version().label() + " with " + useful.size()
                            + " stage and " + usefulCrafts.size() + " craft variables over "
                            + relevant.size() + " constraints", e);
        }
        if (!result.getState().isFeasible()) {
            throw new Optimizer.InfeasibleGoalException(
                    "no combination of the " + useful.size() + " available stage(s) and "
                            + usefulCrafts.size() + " craft(s) meets the goal set in "
                            + in.definition().game().id() + " "
                            + in.definition().version().label()
                            + " (solver state " + result.getState() + ")");
        }

        List<StageRun> plan = new ArrayList<>();
        int index = 0;
        int totalEnergy = 0;
        for (Stage stage : useful) {
            int count = wholeUnits(result.doubleValue(index++));
            if (count > 0) {
                plan.add(new StageRun(stage.stageId(), count, stage.energyCost()));
                // Summed from the integers rather than read off the objective:
                // the objective carries the conversion tie-break and is therefore
                // not an energy total, and a plan whose stated cost does not add
                // up is worse than no plan.
                totalEnergy += count * stage.energyCost();
            }
        }
        List<Conversion> conversions = new ArrayList<>();
        for (Craft craft : usefulCrafts) {
            int count = wholeUnits(result.doubleValue(index++));
            if (count > 0) conversions.add(new Conversion(craft.id(), count));
        }

        plan.sort(Comparator.comparingInt(StageRun::totalEnergy).reversed()
                .thenComparing(run -> run.stage().value()));
        conversions.sort(Comparator.comparing(Conversion::sourceOrSinkId));
        boolean optimal = result.getState().isOptimal();
        return new Outcome(
                List.copyOf(plan),
                List.copyOf(conversions),
                totalEnergy,
                useful.size(),
                usefulCrafts.size(),
                relevant.size(),
                optimal,
                optimal ? 0.0 : gapAgainstRelaxation(model, result.getValue()));
    }

    /**
     * How much of this plan could conceivably be saved, as a fraction.
     *
     * <p>When the search stops on its budget, "we could not prove this is
     * cheapest" is true and unhelpful — it says nothing about whether the plan is
     * a hair off or twice as dear. Dropping integrality gives a bound no integer
     * plan can beat, and one linear solve over a model already built puts a
     * number on the doubt: <em>within a fraction of a percent</em> is a plan to
     * act on, <em>within forty percent</em> is a reason to reach for the solver
     * ADR 0004 names as the fallback.
     *
     * <p>The model is relaxed in place, which is safe only because this is the
     * last thing done with it.
     */
    private static double gapAgainstRelaxation(ExpressionsBasedModel model, double achieved) {
        try {
            model.relax(false);
            Optimisation.Result bound = model.minimise();
            if (!bound.getState().isFeasible()) return Double.NaN;

            double best = bound.getValue();
            if (!Double.isFinite(best) || !Double.isFinite(achieved) || achieved <= 0) return Double.NaN;
            return Math.max(0.0, (achieved - best) / achieved);
        } catch (RuntimeException | StackOverflowError e) {
            // A bound is a nicety. Failing to find one must not lose the plan.
            return Double.NaN;
        }
    }

    /**
     * Every item the model needs a constraint row for: the demanded ones, plus
     * whatever has to be consumed to craft them, transitively.
     *
     * <p>Items outside this set need no row. A stage that also drops something
     * nobody asked for is not thereby cheaper or dearer, and a row saying "you
     * may end up with more junk than you started with" constrains nothing.
     */
    private static Set<ItemId> relevantItems(Set<ItemId> demanded, List<Craft> crafts) {
        Set<ItemId> relevant = new LinkedHashSet<>(demanded);
        boolean grew = true;
        while (grew) {
            grew = false;
            for (Craft craft : crafts) {
                if (craft.produces().stream().noneMatch(p -> relevant.contains(p.item()))) continue;
                for (ItemStack consumed : craft.consumes()) {
                    grew |= relevant.add(consumed.item());
                }
            }
        }
        return relevant;
    }

    /**
     * Refuse early, and say which item and why.
     *
     * <p>An {@code INFEASIBLE} from the solver is true and useless. "Nothing you
     * can farm drops Greater Sigil; its only source is the weekly shop, which
     * this model does not price yet" is the same fact with somewhere to go.
     */
    private static void requireReachable(
            Inputs in, List<Stage> stages, List<Craft> crafts, Set<ItemId> relevant) {

        Set<ItemId> producible = new HashSet<>();
        for (Stage stage : stages) {
            producible.addAll(in.yields().yieldsOf(stage.stageId()).keySet());
        }
        Deque<Craft> pending = new ArrayDeque<>(crafts);
        boolean grew = true;
        while (grew) {
            grew = false;
            for (Craft craft : List.copyOf(pending)) {
                boolean inputsHeld = craft.consumes().stream()
                        .allMatch(c -> producible.contains(c.item()) || in.inventoryOf(c.item()) > 0);
                if (!inputsHeld) continue;
                pending.remove(craft);
                for (ItemStack produced : craft.produces()) {
                    grew |= producible.add(produced.item());
                }
            }
        }

        List<String> unreachable = new ArrayList<>();
        for (ItemId item : relevant) {
            if (in.demandOf(item) <= in.inventoryOf(item) || producible.contains(item)) continue;
            unreachable.add(item.value() + " (" + whyNot(in.definition(), item, in.at()) + ")");
        }
        if (!unreachable.isEmpty()) {
            throw new Optimizer.InfeasibleGoalException(
                    "nothing available can produce " + String.join("; ", unreachable));
        }
    }

    /**
     * The half of an infeasibility message a player can act on.
     *
     * <p>Every branch here was earned by a refusal that was true and useless.
     * "No source yields it" said of an item with a perfectly good recipe whose
     * own ingredients do not exist sends a reader to look for a bug in the
     * recipe; naming the recipe and the reason sends them to the right place.
     */
    private static String whyNot(GameDefinition definition, ItemId item, Instant at) {
        List<String> closed = new ArrayList<>();
        List<String> recipes = new ArrayList<>();
        Shop shop = null;
        Reward reward = null;

        for (Source source : definition.sources()) {
            if (source.potentialOutput().stream().noneMatch(s -> s.item().equals(item))) continue;
            if (source instanceof Shop offer) {
                shop = offer;
            } else if (source instanceof Reward grant) {
                reward = grant;
            } else if (source instanceof Craft craft) {
                recipes.add(craft.id());
            } else if (!isReachable(source.availability(), at)) {
                closed.add(source.id());
            }
        }

        if (!closed.isEmpty()) {
            return "the only stage(s) yielding it are closed or unreleased: "
                    + String.join(", ", closed);
        }
        if (shop != null) {
            return "its only source is the shop \"" + shop.id() + "\", and shop purchases"
                    + " are not modelled until the plan has a time axis";
        }
        if (reward != null) {
            return "its only source is the " + reward.cadence() + " reward \"" + reward.id()
                    + "\", and free income is not modelled until the plan has a time axis";
        }
        if (!recipes.isEmpty()) {
            return "no stage drops it and the recipe(s) that make it — " + String.join(", ", recipes)
                    + " — cannot themselves be supplied";
        }
        return "no source in this game version yields it";
    }

    private static <T> List<T> open(List<T> sources, Function<T, Availability> availability, Instant at) {
        return sources.stream().filter(s -> isReachable(availability.apply(s), at)).toList();
    }

    /**
     * Open now, or open later today, or open on some other weekday — all count.
     * Closed for good, or not released yet, do not. Weekday rotation is
     * deliberately not a filter here; see the class comment.
     */
    private static boolean isReachable(Availability availability, Instant at) {
        if (availability.opensAt() != null && at.isBefore(availability.opensAt())) return false;
        return availability.closesAt() == null || at.isBefore(availability.closesAt());
    }

    /**
     * The most of each item any solution could ever want, and the reason a real
     * patch solves at all.
     *
     * <p>Branch-and-bound on an <em>unbounded</em> integer variable dives: with no
     * incumbent to compare against, ojAlgo follows one branch down the same
     * thread, and a variable that can legally be four hundred thousand is four
     * hundred thousand levels of somewhere to go. On the fixture the ranges were
     * small enough that nobody noticed. On Reverse: 1999, where a currency demand
     * is six figures and the stage that pays it yields nine thousand a run, the
     * dive ran out of stack — an actual {@code StackOverflowError} out of
     * ojAlgo's worker pool, not a slow answer.
     *
     * <p>The fix is to say what is obvious to a player and invisible to a solver:
     * nobody runs a stage more times than would supply the whole goal set on its
     * own. Formally, for item {@code i} this computes an upper bound on the
     * quantity any feasible solution needs — the outstanding demand, plus what
     * the crafts that could run would consume — and {@link #cap} turns that into
     * a bound per variable. Bounding a variable at a value no optimal solution
     * exceeds removes nothing from the search.
     *
     * <p>Returns an empty map when the expansion does not settle, which happens
     * only for a craft graph with a cycle. No bound is always safe; a wrong one
     * would silently cut off the answer.
     */
    private static Map<ItemId, Long> requirementCeiling(
            Inputs in, List<Craft> crafts, Set<ItemId> relevant) {

        Map<ItemId, Long> outstanding = new LinkedHashMap<>();
        for (ItemId item : relevant) {
            outstanding.put(item, Math.max(0L, (long) in.demandOf(item) - in.inventoryOf(item)));
        }

        Map<ItemId, Long> ceiling = new LinkedHashMap<>(outstanding);
        for (int pass = 0; pass <= crafts.size() + 1; pass++) {
            Map<ItemId, Long> next = new LinkedHashMap<>(outstanding);
            boolean overflowed = false;

            for (Craft craft : crafts) {
                long runs = cap(craft, relevant, ceiling);
                if (runs == Long.MAX_VALUE) {
                    overflowed = true;
                    break;
                }
                for (ItemStack consumed : craft.consumes()) {
                    if (!next.containsKey(consumed.item())) continue;
                    next.merge(consumed.item(), runs * consumed.quantity(), EnergyMip::addSaturating);
                }
            }
            if (overflowed) return Map.of();
            if (next.equals(ceiling)) return ceiling;
            ceiling = next;
        }
        // Did not settle: a conversion cycle, where "how much could anyone want"
        // has no finite answer worth computing. Solve unbounded and hope.
        return Map.of();
    }

    /** How many runs of a stage could ever help, given the ceiling on each item. */
    private static long cap(
            Stage stage, YieldTable yields, Set<ItemId> relevant, Map<ItemId, Long> ceiling) {
        if (ceiling.isEmpty()) return Long.MAX_VALUE;

        long most = 0;
        for (ItemId item : relevant) {
            double yield = yields.yield(stage.stageId(), item);
            if (yield <= 0) continue;
            most = Math.max(most, (long) Math.ceil(ceiling.getOrDefault(item, 0L) / yield));
        }
        return most;
    }

    /** The same question for a craft: past this it is over-producing everything it makes. */
    private static long cap(Craft craft, Set<ItemId> relevant, Map<ItemId, Long> ceiling) {
        if (ceiling.isEmpty()) return Long.MAX_VALUE;

        long most = 0;
        for (ItemStack produced : craft.produces()) {
            if (produced.quantity() <= 0 || !relevant.contains(produced.item())) continue;
            long wanted = ceiling.getOrDefault(produced.item(), 0L);
            most = Math.max(most, -Math.floorDiv(-wanted, produced.quantity()));
        }
        return most;
    }

    private static void capRuns(Variable variable, long cap) {
        if (cap >= 0 && cap < Integer.MAX_VALUE) variable.upper(cap);
    }

    private static long addSaturating(long a, long b) {
        long sum = a + b;
        return sum < 0 ? Long.MAX_VALUE : sum;
    }

    /**
     * True when some chain of crafts can come back to where it started.
     *
     * <p>Only a cycle can turn conversions into a free lap, and only a free lap
     * needs the tie-break weight that costs the objective its integrality.
     * Real recipe trees are tiers — a thing is made from cheaper things — so
     * this is almost always false, and finding that out costs a depth-first
     * walk over a graph with a few dozen nodes.
     */
    private static boolean hasCycle(List<Craft> crafts) {
        Map<String, List<String>> edges = new LinkedHashMap<>();
        for (Craft from : crafts) {
            List<String> next = new ArrayList<>();
            for (Craft to : crafts) {
                if (from == to) continue;
                boolean feeds = to.consumes().stream().anyMatch(consumed ->
                        from.produces().stream().anyMatch(p -> p.item().equals(consumed.item())));
                if (feeds) next.add(to.id());
            }
            edges.put(from.id(), next);
        }
        Set<String> done = new HashSet<>();
        Set<String> onPath = new LinkedHashSet<>();
        for (String start : edges.keySet()) {
            if (reachesItself(start, edges, done, onPath)) return true;
        }
        return false;
    }

    private static boolean reachesItself(
            String node, Map<String, List<String>> edges, Set<String> done, Set<String> onPath) {
        if (onPath.contains(node)) return true;
        if (!done.add(node)) return false;
        onPath.add(node);
        for (String next : edges.getOrDefault(node, List.of())) {
            if (reachesItself(next, edges, done, onPath)) return true;
        }
        onPath.remove(node);
        return false;
    }

    private static int net(Craft craft, ItemId item) {
        return quantity(craft.produces(), item) - quantity(craft.consumes(), item);
    }

    private static int quantity(List<ItemStack> stacks, ItemId item) {
        int total = 0;
        for (ItemStack stack : stacks) {
            if (stack.item().equals(item)) total += stack.quantity();
        }
        return total;
    }

    /**
     * A branch-and-bound solver returns 16.999999999 for sixteen. Rounding is
     * correct here; truncation is a bug that shows up as one missing run.
     */
    private static int wholeUnits(double value) {
        long rounded = Math.round(value);
        return rounded <= 0 ? 0 : Math.toIntExact(rounded);
    }
}
