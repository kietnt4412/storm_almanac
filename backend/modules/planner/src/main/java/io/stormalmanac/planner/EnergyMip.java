package io.stormalmanac.planner;

import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gamedata.Availability;
import io.stormalmanac.gamedata.Craft;
import io.stormalmanac.gamedata.Fodder;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.Item;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Reward;
import io.stormalmanac.gamedata.Shop;
import io.stormalmanac.gamedata.Sink;
import io.stormalmanac.gamedata.Source;
import io.stormalmanac.gamedata.Stage;
import io.stormalmanac.gamedata.Upgrade;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumSet;
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
 *             + sum_c y_c * (produce[c,i] - consume[c,i])
 *             + sum_r z_r * grant[r,i]                     &gt;=  demand_i - inventory_i
 *             for every set of stages sharing a weekday restriction:
 *               sum_s x_s * energy_s                       &lt;=  days it is open * energyPerDay
 *             y_c                                          &lt;=  purchases c's reset allows, for a shop
 *             z_r                                          &lt;=  occurrences of r's cadence
 *             x_s, y_c, z_r integer and &gt;= 0
 * </pre>
 *
 * <p>{@code c} ranges over crafts, shop offers <em>and</em> fodder. A purchase
 * is a conversion: it takes currency and gives the offer, and it costs no
 * energy. Only its cap is different. Feeding one unit of fodder is a conversion
 * too, into a progress item such as EXP that only fodder makes. See
 * {@link Exchange}.
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
 * <h2>The time axis, and why it is a parameter rather than an index</h2>
 *
 * <p>The obvious way to give a plan a calendar is to index every stage variable
 * by day, which multiplies the model by the horizon: a patch with a hundred
 * useful stages over thirty days is three thousand integer variables where there
 * were a hundred, and phase 2 closed with its p95 at 90% of its budget. So the
 * horizon is not an index here. It is a <em>scalar</em>, and everything time
 * makes true is a capacity computed from it:
 *
 * <ul>
 *   <li><b>Energy is finite</b> — a plan running {@code D} days may spend
 *       {@code D * energyPerDay}, which is the row that stops "wait long enough"
 *       from being a free lunch.
 *   <li><b>A cadence is a count</b> — a weekly reward fires {@code D / 7} times,
 *       and because the claim variable is an integer that division is exact
 *       rather than rounded.
 *   <li><b>Rotation is a shared capacity</b> — stages open on Tuesdays and
 *       Fridays compete for the energy of the Tuesdays and Fridays in the
 *       window, and nothing else does. See {@link #rotationCapacity}.
 * </ul>
 *
 * <p>Every one of those is linear in a <em>fixed</em> {@code D}, so
 * {@link Objective#LEAST_ENERGY} adds no integer variables at all beyond one per
 * reward, and a game whose data declares neither rewards nor rotation — which is
 * every game this project has ingested so far — gets a model the same size as
 * the one phase 2 measured. {@link Objective#FEWEST_DAYS} needs {@code D} itself
 * minimised, and gets it by {@linkplain #fewestDays binary search} over a
 * feasibility question that is monotone in {@code D}, rather than by making
 * {@code D} a variable the branch-and-bound has to branch on.
 *
 * <h2>What this model still does not contain</h2>
 * <ul>
 *   <li><b>Shop limits that are shared.</b> Every shop offer has its own cap:
 *       {@code periodLimit} times the number of whole periods in the horizon, or
 *       the whole allowance of a stock that {@linkplain Shop#neverResets never
 *       refills}. A price that rises partway through a limit is two offers, the
 *       cheap one capped at the discounted count, and needs nothing shared: a
 *       least-cost plan exhausts the cheap one first unaided. What cannot be
 *       written is one cap over several offers, or an order the game enforces
 *       that cost does not.
 *   <li><b>Expiry within the horizon, as a shared capacity.</b> A stage that
 *       closes in three days is bounded by what three days of energy could buy
 *       (see {@link #closingCap}), which is a real bound and not a joint one: two
 *       stages both closing on Friday are each capped, and not capped
 *       <em>together</em>. Correcting that needs the days to be an index after
 *       all, which is the trade this class exists to avoid.
 *   <li><b>Overshoot and fodder that is also an entity.</b> Fodder is a
 *       conversion into a progress item (see {@link Exchange#of(Fodder, ItemId,
 *       String)}), so EXP spilling past a cap is simply surplus, which is right
 *       for a demand that is itself a cap. Feeding a weapon to a weapon is not
 *       here at all: a weapon is an entity, not an item, and nothing owns a
 *       count of them.
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
     * <p><b>It is applied only where the tie actually exists, and that restraint
     * is load-bearing.</b> Energy costs are whole numbers and so are run counts,
     * so without this weight the objective is integral — and an integral
     * objective lets branch-and-bound discard any node whose bound is within one
     * of the incumbent, which is most of the tree. Adding a millionth to every
     * variable takes that away and asks the solver to resolve ten significant
     * digits instead. On the fixture nobody noticed; on a real patch, where a
     * currency demand runs to six figures, ojAlgo recursed until the stack gave
     * out. So conversions pay it only when the craft graph has a cycle, and
     * reward claims only when the game declares any rewards at all — which the
     * one real upstream does not, so the patch that found the stack overflow
     * still gets an integral objective.
     */
    private static final double TIE_BREAK = 1.0e-6;

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

    /**
     * How many distinct weekday restrictions get the exact treatment.
     *
     * <p>Rotation is a transportation problem — energy is supplied by days and
     * consumed by stages that may only draw on some of them — and such a problem
     * is feasible exactly when every <em>subset</em> of consumers fits inside the
     * supply of the days it can reach. That is one row per subset, so the cost is
     * {@code 2^k} in the number of distinct day-sets, not in the number of
     * stages. Real data has a handful: the acceptance fixture has one, and the
     * only real upstream read so far has none. Past this many the rows are cut
     * back to one per day-set plus the total, which is a relaxation — it can call
     * a schedule feasible that is not — and the plan says so.
     */
    private static final int EXACT_ROTATION_GROUPS = 6;

    /** Below this many milliseconds a probe is not worth starting. */
    private static final long MINIMUM_PROBE_MILLIS = 25;

    private EnergyMip() {}

    /**
     * @param rewardClaims    free income the plan leans on
     * @param daysNeeded      whole days the plan cannot be compressed below,
     *                        because of a cadence it waits on or a stage that is
     *                        only open some weekdays. Zero when nothing but
     *                        energy constrains the calendar
     * @param horizonUsed     the horizon this plan was solved against, which for
     *                        {@link Objective#FEWEST_DAYS} is the answer the
     *                        search settled on rather than the one asked for
     * @param stageVariables  how many stages survived pruning and became variables
     * @param craftVariables  how many crafts did
     * @param shopVariables   how many shop offers did
     * @param rewardVariables how many rewards did
     * @param constraints     how many item balance rows the model carried
     * @param provenOptimal   false when the search stopped on its time budget, so
     *                        this is the cheapest plan found rather than the
     *                        cheapest plan
     * @param optimalityGap   how much of this plan's energy could conceivably be
     *                        saved, as a fraction: the distance to the linear
     *                        relaxation's bound, which no integer plan can beat.
     *                        Zero when optimality was proven, {@code NaN} when the
     *                        bound could not be computed
     * @param rotationExact   false when there were too many distinct weekday
     *                        restrictions to enforce exactly, so the schedule is
     *                        checked group by group rather than jointly
     */
    record Outcome(
            List<StageRun> stageRuns,
            List<Conversion> conversions,
            List<RewardClaim> rewardClaims,
            int totalEnergy,
            int daysNeeded,
            int horizonUsed,
            int stageVariables,
            int craftVariables,
            int shopVariables,
            int rewardVariables,
            int constraints,
            boolean provenOptimal,
            double optimalityGap,
            boolean rotationExact
    ) {}

    /**
     * Everything the model reads. Deliberately a value: the same inputs give the
     * same plan.
     *
     * @param at           when the plan starts, which fixes the weekday the
     *                     horizon begins on. <b>Weekdays are read in UTC, and
     *                     that is a placeholder rather than a decision.</b> A
     *                     game rolls its day over on its own clock — Reverse:
     *                     1999 Global at 05:00 UTC−5, which is 10:00 UTC — so
     *                     the zone and the hour are properties of the game and
     *                     belong on its definition, not on this class. They are
     *                     not there yet because adding them is a bundle field, a
     *                     parser change and a migration, and no game this project
     *                     has ingested rotates, so the assumption is currently
     *                     inert. It stops being inert the moment one does; see
     *                     {@code docs/game-facts/reverse-1999-economy.md}
     * @param energyPerDay what the player earns and is willing to spend per day
     * @param horizonDays  how many days the plan may take
     */
    record Inputs(
            GameDefinition definition,
            YieldTable yields,
            Map<ItemId, Integer> inventory,
            Map<ItemId, Integer> demand,
            Instant at,
            long budgetMillis,
            Objective objective,
            int energyPerDay,
            int horizonDays
    ) {
        int inventoryOf(ItemId item) {
            return inventory.getOrDefault(item, 0);
        }

        int demandOf(ItemId item) {
            return demand.getOrDefault(item, 0);
        }

        Inputs withDemand(Map<ItemId, Integer> replacement) {
            return new Inputs(definition, yields, inventory, replacement, at, budgetMillis,
                    objective, energyPerDay, horizonDays);
        }

        Inputs withBudget(long millis) {
            return new Inputs(definition, yields, inventory, demand, at, millis,
                    objective, energyPerDay, horizonDays);
        }

        /**
         * The same question over a fixed number of days, asked for least energy.
         *
         * <p>Used twice: by the {@link #fewestDays} search, which asks it once per
         * probe, and by the caller pricing shadow prices against a plan whose
         * horizon has already been decided. Re-running the search for every
         * marginal item would price each of them against a different calendar.
         */
        Inputs pinnedTo(int days) {
            return new Inputs(definition, yields, inventory, demand, at, budgetMillis,
                    Objective.LEAST_ENERGY, energyPerDay, days);
        }
    }

    static Outcome solve(Inputs in) {
        if (in.demand().isEmpty()) {
            return empty(in.horizonDays());
        }
        return in.objective() == Objective.FEWEST_DAYS ? fewestDays(in) : solveAt(in, in.horizonDays());
    }

    /**
     * The shortest horizon the goal set fits into, and the cheapest plan inside it.
     *
     * <p>This is the objective's whole difference from least energy, and it is a
     * search rather than a variable because feasibility is <b>monotone in the
     * horizon</b>: another day adds energy, may add a reward occurrence, and may
     * add a day a rotating stage is open, and it takes nothing away. A monotone
     * predicate over a range of thirty is five probes by bisection, against a
     * branch-and-bound that would have to carry the day count as an integer
     * variable coupling every capacity row in the model.
     *
     * <p>The two objectives coincide exactly when neither a cadence nor a
     * rotation binds — then the only thing days buy is energy, the fastest plan
     * is the cheapest one, and both come back with the same runs. That is a
     * property of the <em>game data</em>, not of the model, and the plan says
     * which of the two it is looking at rather than leaving a reader to guess.
     */
    private static Outcome fewestDays(Inputs in) {
        int probes = Math.max(1, 32 - Integer.numberOfLeadingZeros(Math.max(1, in.horizonDays())) + 1);
        long probeBudget = Math.max(MINIMUM_PROBE_MILLIS, in.budgetMillis() / (probes + 1L));

        // The horizon the caller asked for has to work, or nothing does. Probing
        // it first turns an impossible goal set into the solver's own refusal —
        // re-run on the full budget so the message is the one a caller would have
        // got had they asked for least energy — rather than into a bisection that
        // narrows onto a failure and has to invent one.
        if (!feasibleAt(in.withBudget(probeBudget), in.horizonDays())) {
            return solveAt(in, in.horizonDays());
        }

        int low = 0;
        int high = in.horizonDays();
        while (low < high) {
            int mid = low + (high - low) / 2;
            if (feasibleAt(in.withBudget(probeBudget), mid)) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }

        // Whatever the probes did not spend belongs to the answer: the probes
        // only had to decide feasible-or-not, and this one has to be the cheapest
        // plan inside the horizon they settled on.
        long remaining = Math.max(MINIMUM_PROBE_MILLIS, in.budgetMillis() - probeBudget * probes);
        return solveAt(in.withBudget(remaining), low);
    }

    /**
     * Can the goal set be met at all in this many days?
     *
     * <p>A probe that runs out of budget is treated as a <b>no</b>. That is the
     * conservative direction: it can only make the answer longer than the true
     * shortest plan, never shorter, so the plan handed to a player is one they
     * can actually execute. The alternative — assuming a timed-out probe
     * succeeded — promises a deadline the model never proved.
     */
    private static boolean feasibleAt(Inputs in, int days) {
        try {
            solveAt(in, days);
            return true;
        } catch (Optimizer.InfeasibleGoalException e) {
            return false;
        }
    }

    private static Outcome empty(int horizon) {
        return new Outcome(List.of(), List.of(), List.of(), 0, 0, horizon, 0, 0, 0, 0, 0, true, 0.0, true);
    }

    private static Outcome solveAt(Inputs in, int horizonDays) {
        if (in.demand().isEmpty()) return empty(horizonDays);

        List<Stage> stages = open(in.definition().stages(), Stage::availability, in.at());
        List<Exchange> exchanges = exchanges(in, horizonDays);
        List<Reward> rewards = claimable(
                open(in.definition().rewards(), Reward::availability, in.at()), in.at(), horizonDays);
        Set<ItemId> relevant = relevantItems(in.demand().keySet(), exchanges);

        requireReachable(in, stages, exchanges, rewards, relevant, horizonDays);

        // Prune: a stage dropping nothing anybody needs is a variable the
        // branch-and-bound tree pays for and never uses.
        List<Stage> useful = stages.stream()
                .filter(s -> relevant.stream().anyMatch(i -> in.yields().yield(s.stageId(), i) > 0))
                .toList();
        List<Exchange> usefulExchanges = exchanges.stream()
                .filter(c -> c.produces().stream().anyMatch(p -> relevant.contains(p.item())))
                .toList();
        List<Reward> usefulRewards = rewards.stream()
                .filter(r -> r.grants().stream().anyMatch(g -> relevant.contains(g.item())))
                .toList();

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        model.options.time_abort = in.budgetMillis();
        model.options.integer(IntegerStrategy.DEFAULT.withGapTolerance(GAP));

        Map<ItemId, Long> ceiling = requirementCeiling(in, usefulExchanges, relevant);

        List<Variable> runs = new ArrayList<>(useful.size());
        for (Stage stage : useful) {
            Variable variable = model.newVariable("run:" + stage.stageId().value())
                    .lower(0).integer(true).weight(stage.energyCost());
            capRuns(variable, Math.min(
                    cap(stage, in.yields(), relevant, ceiling),
                    closingCap(stage, in, horizonDays)));
            runs.add(variable);
        }
        // A free offer is the other place a conversion can be made for nothing:
        // a limited daily pack at no price ties with not taking it, and would be
        // reported as something the plan wants done.
        double conversionWeight =
                hasCycle(usefulExchanges) || usefulExchanges.stream().anyMatch(e -> e.consumes().isEmpty())
                        ? TIE_BREAK
                        : 0.0;
        List<Variable> made = new ArrayList<>(usefulExchanges.size());
        for (Exchange exchange : usefulExchanges) {
            Variable variable = model.newVariable(exchange.variableName())
                    .lower(0).integer(true).weight(conversionWeight);
            capRuns(variable, cap(exchange, relevant, ceiling));
            made.add(variable);
        }
        // Free income is free, so nothing in the objective distinguishes a plan
        // that leans on four weekly quests from one that leans on all thirty
        // dailies it never needed — and the second would be reported to a player
        // as something the plan is counting on. The same millionth that keeps
        // pointless crafts out keeps unclaimed rewards out, and is paid only by
        // games that declare rewards at all.
        double rewardWeight = usefulRewards.isEmpty() ? 0.0 : TIE_BREAK;
        List<Variable> claims = new ArrayList<>(usefulRewards.size());
        for (Reward reward : usefulRewards) {
            Variable variable = model.newVariable("claim:" + reward.id())
                    .lower(0).integer(true).weight(rewardWeight)
                    .upper(occurrences(reward, in.at(), horizonDays));
            claims.add(variable);
        }

        for (ItemId item : relevant) {
            Expression balance = model.newExpression("item:" + item.value());
            for (int i = 0; i < useful.size(); i++) {
                double yield = in.yields().yield(useful.get(i).stageId(), item);
                if (yield > 0) balance.set(runs.get(i), yield);
            }
            for (int c = 0; c < usefulExchanges.size(); c++) {
                int net = net(usefulExchanges.get(c), item);
                if (net != 0) balance.set(made.get(c), net);
            }
            for (int r = 0; r < usefulRewards.size(); r++) {
                int granted = quantity(usefulRewards.get(r).grants(), item);
                if (granted != 0) balance.set(claims.get(r), granted);
            }
            balance.lower(in.demandOf(item) - in.inventoryOf(item));
        }

        boolean rotationExact = rotationCapacity(model, useful, runs, in, horizonDays);

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
                            + " stage and " + usefulExchanges.size() + " craft or shop variables over "
                            + relevant.size() + " constraints", e);
        }
        if (!result.getState().isFeasible()) {
            throw new Optimizer.InfeasibleGoalException(
                    "no combination of the " + useful.size() + " available stage(s), "
                            + count(usefulExchanges, false) + " craft(s), "
                            + count(usefulExchanges, true) + " shop offer(s) and " + usefulRewards.size()
                            + " reward(s) meets the goal set in " + in.definition().game().id()
                            + " " + in.definition().version().label() + " within " + horizonDays
                            + " day(s) at " + in.energyPerDay() + " energy a day"
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
                // the objective carries the tie-break weights and is therefore
                // not an energy total, and a plan whose stated cost does not add
                // up is worse than no plan.
                totalEnergy += count * stage.energyCost();
            }
        }
        List<Conversion> conversions = new ArrayList<>();
        for (Exchange exchange : usefulExchanges) {
            int count = wholeUnits(result.doubleValue(index++));
            if (count > 0) conversions.add(new Conversion(exchange.id(), count));
        }
        List<RewardClaim> claimed = new ArrayList<>();
        Map<Reward, Integer> claimCounts = new LinkedHashMap<>();
        for (Reward reward : usefulRewards) {
            int count = wholeUnits(result.doubleValue(index++));
            if (count > 0) {
                claimed.add(new RewardClaim(reward.id(), count));
                claimCounts.put(reward, count);
            }
        }

        plan.sort(Comparator.comparingInt(StageRun::totalEnergy).reversed()
                .thenComparing(run -> run.stage().value()));
        conversions.sort(Comparator.comparing(Conversion::sourceOrSinkId));
        claimed.sort(Comparator.comparing(RewardClaim::reward));
        boolean optimal = result.getState().isOptimal();
        return new Outcome(
                List.copyOf(plan),
                List.copyOf(conversions),
                List.copyOf(claimed),
                totalEnergy,
                daysNeeded(plan, claimCounts, in, horizonDays),
                horizonDays,
                useful.size(),
                count(usefulExchanges, false),
                count(usefulExchanges, true),
                usefulRewards.size(),
                relevant.size(),
                optimal,
                optimal ? 0.0 : gapAgainstRelaxation(model, result.getValue()),
                rotationExact);
    }

    // ── the time axis ───────────────────────────────────────────────────────

    /**
     * The rows that make a day a finite thing.
     *
     * <p>Stages are grouped by the weekday restriction they declare — every stage
     * with no restriction is one group, every stage open only on Tuesdays and
     * Fridays is another — and each group's energy is capped by the energy of the
     * days it can reach. That much is obvious. What is less obvious is that
     * capping each group on its own is <em>wrong</em>: a stage open on Tuesdays
     * only and a stage open on Tuesdays and Fridays both fit their own caps while
     * between them demanding more Tuesdays than the window contains.
     *
     * <p>So the rows are written over every subset of groups, against the days
     * that subset's union can reach. That is exactly the condition under which
     * the underlying transportation problem has a solution, and the number of
     * rows is {@code 2^k} in the number of <em>distinct restrictions</em> rather
     * than in the number of stages — one row for a game with no rotation at all,
     * three for the one in the acceptance fixture. Past
     * {@value #EXACT_ROTATION_GROUPS} distinct restrictions it falls back to one
     * row per group plus a total, returns false, and the plan says the schedule
     * was checked group by group rather than jointly.
     *
     * <p>One approximation remains and is deliberate: energy is allowed to split
     * across days as if it were continuous, so a plan is not asked to prove each
     * individual run fits inside one day's budget. On real numbers — a run costs
     * tens, a day supplies hundreds — that is slack nobody can act on.
     *
     * @return whether the constraint written is the exact one
     */
    private static boolean rotationCapacity(
            ExpressionsBasedModel model,
            List<Stage> useful,
            List<Variable> runs,
            Inputs in,
            int horizonDays) {

        if (useful.isEmpty()) return true;

        Map<Set<DayOfWeek>, List<Integer>> groups = new LinkedHashMap<>();
        for (int i = 0; i < useful.size(); i++) {
            groups.computeIfAbsent(useful.get(i).availability().days(), k -> new ArrayList<>()).add(i);
        }

        List<Set<DayOfWeek>> daySets = List.copyOf(groups.keySet());
        boolean exact = daySets.size() <= EXACT_ROTATION_GROUPS;
        List<List<Integer>> subsets = exact
                ? subsetsOf(daySets.size())
                : eachGroupAndTheWhole(daySets.size());

        int row = 0;
        for (List<Integer> subset : subsets) {
            Set<DayOfWeek> reachable = EnumSet.noneOf(DayOfWeek.class);
            boolean anyUnrestricted = false;
            for (int g : subset) {
                Set<DayOfWeek> days = daySets.get(g);
                if (days.isEmpty()) anyUnrestricted = true;
                reachable.addAll(days);
            }
            long days = anyUnrestricted
                    ? horizonDays
                    : matchingDays(reachable, in.at(), horizonDays);

            Expression capacity = model.newExpression("days:" + row++);
            for (int g : subset) {
                for (int i : groups.get(daySets.get(g))) {
                    capacity.set(runs.get(i), useful.get(i).energyCost());
                }
            }
            capacity.upper(days * (long) in.energyPerDay());
        }
        return exact;
    }

    /** Every non-empty subset of {@code n} groups, smallest first. */
    private static List<List<Integer>> subsetsOf(int n) {
        List<List<Integer>> subsets = new ArrayList<>();
        for (int mask = 1; mask < (1 << n); mask++) {
            List<Integer> members = new ArrayList<>();
            for (int g = 0; g < n; g++) {
                if ((mask & (1 << g)) != 0) members.add(g);
            }
            subsets.add(members);
        }
        return subsets;
    }

    /** The relaxation used when there are too many groups to enumerate. */
    private static List<List<Integer>> eachGroupAndTheWhole(int n) {
        List<List<Integer>> subsets = new ArrayList<>();
        for (int g = 0; g < n; g++) subsets.add(List.of(g));
        List<Integer> all = new ArrayList<>();
        for (int g = 0; g < n; g++) all.add(g);
        subsets.add(all);
        return subsets;
    }

    /**
     * How many of the next {@code horizonDays} days fall on one of these weekdays.
     *
     * <p>Counted rather than approximated as {@code horizon * |days| / 7}. Over a
     * horizon of ten days that approximation is out by a whole day either way,
     * and a day of energy is tens of stage runs.
     */
    private static long matchingDays(Set<DayOfWeek> days, Instant from, int horizonDays) {
        if (days.isEmpty()) return horizonDays;
        DayOfWeek start = from.atZone(ZoneOffset.UTC).getDayOfWeek();
        long matching = 0;
        for (int i = 0; i < horizonDays; i++) {
            if (days.contains(start.plus(i))) matching++;
        }
        return matching;
    }

    /** Rewards that fire at least once inside the horizon; the rest are not sources. */
    private static List<Reward> claimable(List<Reward> rewards, Instant at, int horizonDays) {
        return rewards.stream().filter(r -> occurrences(r, at, horizonDays) > 0).toList();
    }

    /**
     * How many times a reward can be collected before the horizon or the reward
     * itself runs out, whichever comes first.
     *
     * <p>An event reward that ends on Thursday grants three more dailies, not
     * thirty: a plan that assumed otherwise would be cheap on paper and short of
     * materials in practice.
     */
    private static int occurrences(Reward reward, Instant at, int horizonDays) {
        Instant closes = reward.availability().closesAt();
        int days = horizonDays;
        if (closes != null) {
            days = (int) Math.max(0, Math.min(horizonDays, Duration.between(at, closes).toDays()));
        }
        return reward.cadence().occurrencesIn(days);
    }

    /**
     * How many times a shop offer can be bought before the horizon or the shop
     * closes, whichever comes first. {@link Long#MAX_VALUE} when nothing limits it.
     */
    private static long purchases(Shop shop, Instant at, int horizonDays) {
        Instant closes = shop.availability().closesAt();
        int days = horizonDays;
        if (closes != null) {
            days = (int) Math.max(0, Math.min(horizonDays, Duration.between(at, closes).toDays()));
        }
        return shop.purchasesIn(days);
    }

    /**
     * A craft or a shop offer. The model cannot tell the two apart and should
     * not have to. Each consumes something, makes something and spends no
     * energy.
     *
     * <p>The one difference is {@code limit}. A recipe can be run as often as
     * its inputs allow. A shop offer can be bought as often as its reset allows
     * inside the horizon, which is the bound that stops the solver buying its
     * way out of every constraint. A purchase is reported as a
     * {@link Conversion} under the shop's id, because to a player it is the same
     * kind of instruction: go and do this N times.
     *
     * @param limit {@link Long#MAX_VALUE} for a craft and for an unlimited offer
     */
    private record Exchange(
            String id,
            boolean purchase,
            List<ItemStack> consumes,
            List<ItemStack> produces,
            long limit) {

        static Exchange of(Craft craft) {
            return new Exchange(craft.id(), false, craft.consumes(), craft.produces(), Long.MAX_VALUE);
        }

        /**
         * One unit of {@code item} fed into its progress kind. A recipe in all
         * but name: it consumes the unit and the rule's side costs, and makes
         * {@code progressPerUnit} of a progress item nothing else can make.
         * So the model needs no fodder row type of its own, and a Pod bought
         * in a shop to be fed to a level is a chain the solver walks the same
         * way it walks a box bought to be opened.
         *
         * @param id the rule's id, or the rule's id and the item's when the
         *           rule's category holds more than one item, since each is a
         *           separate instruction to the player
         */
        static Exchange of(Fodder rule, ItemId item, String id) {
            List<ItemStack> consumes = new ArrayList<>(rule.costs().size() + 1);
            consumes.add(new ItemStack(item, 1));
            consumes.addAll(rule.costs());
            return new Exchange(id, false, consumes,
                    List.of(new ItemStack(Demand.progressItem(rule.progress()), rule.progressPerUnit())),
                    Long.MAX_VALUE);
        }

        /**
         * Paying one of a step's several prices, which makes the one
         * {@link Demand#choiceItem} the step is owed as. The price is the
         * upgrade's items and its progress, so a price paid partly in EXP
         * pulls fodder in the same way a single-priced step does.
         */
        static Exchange of(Upgrade price) {
            List<ItemStack> consumes = new ArrayList<>(price.costs());
            price.progress().forEach(p -> consumes.add(new ItemStack(Demand.progressItem(p.kind()), p.quantity())));
            return new Exchange(price.id(), false, consumes,
                    List.of(new ItemStack(Demand.choiceItem(price), 1)), Long.MAX_VALUE);
        }

        static Exchange of(Shop shop, long purchases) {
            List<ItemStack> price = shop.price() > 0
                    ? List.of(new ItemStack(shop.currency(), shop.price()))
                    : List.of();
            return new Exchange(shop.id(), true, price, List.of(shop.offer()), purchases);
        }

        String variableName() {
            return (purchase ? "buy:" : "craft:") + id;
        }
    }

    /**
     * Every craft that is open, and every shop offer that is open and resets at
     * least once inside the horizon. The rest are not sources. {@link #whyNot}
     * says so by name.
     */
    private static List<Exchange> exchanges(Inputs in, int horizonDays) {
        List<Exchange> exchanges = new ArrayList<>();
        for (Craft craft : open(in.definition().crafts(), Craft::availability, in.at())) {
            exchanges.add(Exchange.of(craft));
        }
        for (Shop shop : open(in.definition().shops(), Shop::availability, in.at())) {
            long purchases = purchases(shop, in.at(), horizonDays);
            if (purchases > 0) exchanges.add(Exchange.of(shop, purchases));
        }
        for (List<Upgrade> prices : severalPrices(in.definition())) {
            prices.forEach(price -> exchanges.add(Exchange.of(price)));
        }
        for (Sink sink : in.definition().sinks()) {
            if (!(sink instanceof Fodder rule) || rule.progress() == null) continue;
            List<Item> eligible = in.definition().items().stream()
                    .filter(item -> item.category().equals(rule.consumesCategory()))
                    .filter(item -> item.rarity().rank() >= rule.minimumRarity().rank())
                    .toList();
            for (Item item : eligible) {
                String id = eligible.size() == 1 ? rule.id() : rule.id() + ":" + item.id().value();
                exchanges.add(Exchange.of(rule, item.id(), id));
            }
        }
        return exchanges;
    }

    /**
     * Every step the game offers at more than one price: upgrades sharing an
     * entity, a from-state and a to-state. A step with one price is paid in the
     * demand vector and never reaches the model.
     */
    private static Collection<List<Upgrade>> severalPrices(GameDefinition definition) {
        Map<ItemId, List<Upgrade>> byStep = new LinkedHashMap<>();
        for (Sink sink : definition.sinks()) {
            if (sink instanceof Upgrade upgrade) {
                byStep.computeIfAbsent(Demand.choiceItem(upgrade), k -> new ArrayList<>()).add(upgrade);
            }
        }
        return byStep.values().stream().filter(prices -> prices.size() > 1).toList();
    }

    private static int count(List<Exchange> exchanges, boolean purchases) {
        return (int) exchanges.stream().filter(e -> e.purchase() == purchases).count();
    }

    /**
     * The most runs a stage that is closing could absorb before it closes.
     *
     * <p>Not a joint constraint — see the class comment — but it is the
     * difference between a plan that farms an expiring event within the days it
     * has left and one that farms it five hundred times on its final afternoon.
     */
    private static long closingCap(Stage stage, Inputs in, int horizonDays) {
        Instant closes = stage.availability().closesAt();
        if (closes == null || stage.energyCost() <= 0) return Long.MAX_VALUE;

        long days = Math.max(0, Math.min(horizonDays, Duration.between(in.at(), closes).toDays()));
        return days * (long) in.energyPerDay() / stage.energyCost();
    }

    /**
     * The shortest number of whole days this particular plan can be executed in,
     * ignoring the energy total.
     *
     * <p>Energy alone gives a fractional answer — 105 energy at 60 a day is 1.75
     * days and always was — and the caller keeps that. What this adds is the part
     * that only comes in whole days: a plan claiming four weekly quests cannot be
     * done in less than four weeks however much energy is spare, and a plan
     * needing nine runs of a Tuesday-and-Friday stage needs enough Tuesdays and
     * Fridays to have happened.
     *
     * <p>Zero when neither binds, which is the common case and the reason a game
     * with no rewards and no rotation gets exactly the plan and the estimate it
     * got before this class had a calendar.
     */
    private static int daysNeeded(
            List<StageRun> plan, Map<Reward, Integer> claims, Inputs in, int horizonDays) {

        int needed = 0;
        for (Map.Entry<Reward, Integer> claim : claims.entrySet()) {
            for (int days = needed; days <= horizonDays; days++) {
                if (occurrences(claim.getKey(), in.at(), days) >= claim.getValue()) {
                    needed = days;
                    break;
                }
            }
        }

        Map<Set<DayOfWeek>, Long> energyByRestriction = new LinkedHashMap<>();
        for (StageRun run : plan) {
            Set<DayOfWeek> days = restrictionOf(in.definition(), run);
            if (days.isEmpty()) continue; // the unrestricted ones are the energy figure
            energyByRestriction.merge(days, (long) run.totalEnergy(), Long::sum);
        }
        for (Map.Entry<Set<DayOfWeek>, Long> entry : energyByRestriction.entrySet()) {
            for (int days = needed; days <= horizonDays; days++) {
                if (matchingDays(entry.getKey(), in.at(), days) * (long) in.energyPerDay()
                        >= entry.getValue()) {
                    needed = days;
                    break;
                }
            }
        }
        return needed;
    }

    private static Set<DayOfWeek> restrictionOf(GameDefinition definition, StageRun run) {
        return definition.stages().stream()
                .filter(s -> s.stageId().equals(run.stage()))
                .findFirst()
                .map(s -> s.availability().days())
                .orElse(Set.of());
    }

    // ── the rest, unchanged in intent ───────────────────────────────────────

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
     * whatever has to be consumed to craft or buy them, transitively. A shop's
     * currency is relevant exactly when something it sells is.
     *
     * <p>Items outside this set need no row. A stage that also drops something
     * nobody asked for is not thereby cheaper or dearer, and a row saying "you
     * may end up with more junk than you started with" constrains nothing.
     */
    private static Set<ItemId> relevantItems(Set<ItemId> demanded, List<Exchange> crafts) {
        Set<ItemId> relevant = new LinkedHashSet<>(demanded);
        boolean grew = true;
        while (grew) {
            grew = false;
            for (Exchange craft : crafts) {
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
            Inputs in,
            List<Stage> stages,
            List<Exchange> crafts,
            List<Reward> rewards,
            Set<ItemId> relevant,
            int horizonDays) {

        Set<ItemId> producible = new HashSet<>();
        for (Stage stage : stages) {
            producible.addAll(in.yields().yieldsOf(stage.stageId()).keySet());
        }
        for (Reward reward : rewards) {
            for (ItemStack granted : reward.grants()) producible.add(granted.item());
        }
        Deque<Exchange> pending = new ArrayDeque<>(crafts);
        boolean grew = true;
        while (grew) {
            grew = false;
            for (Exchange craft : List.copyOf(pending)) {
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
            unreachable.add(item.value() + " ("
                    + whyNot(in.definition(), item, in.at(), horizonDays) + ")");
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
    private static String whyNot(
            GameDefinition definition, ItemId item, Instant at, int horizonDays) {

        if (Demand.isChoiceItem(item)) {
            List<String> prices = severalPrices(definition).stream()
                    .filter(p -> Demand.choiceItem(p.get(0)).equals(item))
                    .flatMap(p -> p.stream().map(Upgrade::id))
                    .toList();
            return "it can be paid by any one of " + String.join(", ", prices)
                    + ", and nothing available supplies any of those prices";
        }
        if (Demand.isProgressItem(item)) {
            List<String> feeding = definition.sinks().stream()
                    .filter(s -> s instanceof Fodder f && f.progress() != null
                            && Demand.progressItem(f.progress()).equals(item))
                    .map(Sink::id)
                    .toList();
            return feeding.isEmpty()
                    ? "no fodder rule in this game version pays it"
                    : "it is paid only by feeding " + String.join(", ", feeding)
                            + ", and nothing available supplies what that consumes";
        }

        List<String> closed = new ArrayList<>();
        List<String> recipes = new ArrayList<>();
        List<String> shops = new ArrayList<>();
        Reward tooSlow = null;

        for (Source source : definition.sources()) {
            if (source.potentialOutput().stream().noneMatch(s -> s.item().equals(item))) continue;
            if (source instanceof Shop offer) {
                shops.add(whyNotBuyable(offer, at, horizonDays));
            } else if (source instanceof Reward grant) {
                // Reachable rewards are modelled now, so one reaching here either
                // is not open or does not come round inside the horizon.
                tooSlow = grant;
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
        if (!shops.isEmpty()) {
            return "it is sold only by " + String.join(", and by ", shops);
        }
        if (tooSlow != null) {
            return "its only source is the " + tooSlow.cadence() + " reward \"" + tooSlow.id()
                    + "\", which does not come round inside a " + horizonDays + "-day horizon";
        }
        if (!recipes.isEmpty()) {
            return "no stage drops it and the recipe(s) that make it — " + String.join(", ", recipes)
                    + " — cannot themselves be supplied";
        }
        return "no source in this game version yields it";
    }

    /** Of the three ways a shop can fail to supply, which one this is. */
    private static String whyNotBuyable(Shop shop, Instant at, int horizonDays) {
        String named = "the shop \"" + shop.id() + "\"";
        if (!isReachable(shop.availability(), at)) {
            return named + ", which is closed or unreleased";
        }
        if (purchases(shop, at, horizonDays) == 0) {
            return shop.neverResets()
                    ? named + ", whose allowance of " + shop.periodLimit() + " cannot be bought"
                            + " inside a " + horizonDays + "-day horizon"
                    : named + ", whose limit of " + shop.periodLimit() + " per " + shop.period()
                            + " does not reset inside a " + horizonDays + "-day horizon";
        }
        return named + ", whose currency \"" + shop.currency().value()
                + "\" nothing available can supply";
    }

    private static <T> List<T> open(List<T> sources, Function<T, Availability> availability, Instant at) {
        return sources.stream().filter(s -> isReachable(availability.apply(s), at)).toList();
    }

    /**
     * Open now, or open later this week, or open on some other weekday — all
     * count here. Closed for good, or not released yet, do not. Weekday rotation
     * is not a filter, it is a capacity: see {@link #rotationCapacity}.
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
            Inputs in, List<Exchange> crafts, Set<ItemId> relevant) {

        Map<ItemId, Long> outstanding = new LinkedHashMap<>();
        for (ItemId item : relevant) {
            outstanding.put(item, Math.max(0L, (long) in.demandOf(item) - in.inventoryOf(item)));
        }

        Map<ItemId, Long> ceiling = new LinkedHashMap<>(outstanding);
        for (int pass = 0; pass <= crafts.size() + 1; pass++) {
            Map<ItemId, Long> next = new LinkedHashMap<>(outstanding);
            boolean overflowed = false;

            for (Exchange craft : crafts) {
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

    /**
     * The same question for a craft or an offer: past this it is over-producing
     * everything it makes. A shop's own limit is a second bound, and the tighter
     * one wins. It is applied even when the ceiling cannot be computed, because
     * a shop's reset is data and not an estimate.
     */
    private static long cap(Exchange craft, Set<ItemId> relevant, Map<ItemId, Long> ceiling) {
        if (ceiling.isEmpty()) return craft.limit();

        long most = 0;
        for (ItemStack produced : craft.produces()) {
            if (produced.quantity() <= 0 || !relevant.contains(produced.item())) continue;
            long wanted = ceiling.getOrDefault(produced.item(), 0L);
            most = Math.max(most, -Math.floorDiv(-wanted, produced.quantity()));
        }
        return Math.min(most, craft.limit());
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
    private static boolean hasCycle(List<Exchange> crafts) {
        Map<String, List<String>> edges = new LinkedHashMap<>();
        for (Exchange from : crafts) {
            List<String> next = new ArrayList<>();
            for (Exchange to : crafts) {
                if (from == to) continue;
                boolean feeds = to.consumes().stream().anyMatch(consumed ->
                        from.produces().stream().anyMatch(p -> p.item().equals(consumed.item())));
                if (feeds) next.add(to.variableName());
            }
            edges.put(from.variableName(), next);
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

    private static int net(Exchange craft, ItemId item) {
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
