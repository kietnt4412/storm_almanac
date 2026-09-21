package io.stormalmanac.planner;

import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.Goal;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Progress;
import io.stormalmanac.gamedata.Sink;
import io.stormalmanac.gamedata.Upgrade;
import io.stormalmanac.player.Roster;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Turns "I want her at Insight 2" into "you owe 6 Greater Sigils and 20 000
 * Gold".
 *
 * <p>The whole method is a walk backwards over the upgrade graph, and it knows
 * nothing about what the states mean. States are opaque strings the bundle
 * supplies; the resolver only reads the edges they form. That is the entire
 * reason a second title costs a bundle and not a rewrite.
 *
 * <p><b>Backwards, not forwards, and that is the load-bearing choice.</b> A
 * forward walk needs a starting state, and a player who does not own the entity
 * yet has none. Walking back from the target until it meets either something the
 * player has already achieved or a track with nothing before it answers both
 * cases with one traversal, and answers the multi-track case — Insight, level,
 * skill ranks all hang off the same entity — without ever enumerating tracks.
 *
 * <p>Ordering note: {@link Goal#priority()} is deliberately ignored here. Every
 * goal in the set must be paid for, so priority cannot change the demand vector.
 * It becomes meaningful when a plan is capped by a budget and something has to
 * be dropped, which is not this phase.
 */
public final class DemandResolver {

    /**
     * @throws Optimizer.InfeasibleGoalException when a goal names a state the
     *         upgrade graph cannot reach, which is a data problem rather than a
     *         farming one and must not be reported as "grind harder"
     */
    public Demand resolve(GameDefinition definition, Roster roster, List<Goal> goals) {
        Map<EntityId, Map<String, List<Upgrade>>> incoming = incomingEdges(definition);

        Map<ItemId, Integer> totals = new LinkedHashMap<>();
        List<String> steps = new ArrayList<>();
        Set<String> counted = new HashSet<>();
        List<Goal> alreadyMet = new ArrayList<>();

        for (Goal goal : goals) {
            if (goal.satisfiability() == Goal.Satisfiability.PROBABILISTIC) {
                throw new UnsupportedOperationException(
                        "goal \"" + goal.targetState() + "\" on " + goal.entity()
                                + " is probabilistic: its cost is a distribution, and a plan"
                                + " reporting a percentile is not built yet");
            }
            Map<String, List<Upgrade>> edges = incoming.get(goal.entity());
            if (edges == null) {
                throw new Optimizer.InfeasibleGoalException(
                        "no upgrades for entity \"" + goal.entity() + "\" in "
                                + definition.game().id() + " " + definition.version().label());
            }

            String current = roster.currentState().get(goal.entity());
            Set<String> achieved = achieved(edges, current);
            if (achieved.contains(goal.targetState())) {
                alreadyMet.add(goal);
                continue;
            }

            new Payment(goal, edges, achieved, definition, totals, steps, counted)
                    .payUpTo(goal.targetState(), true);
        }
        return new Demand(totals, steps, alreadyMet);
    }

    /**
     * One goal's walk, carrying what every step it pays has to add to.
     *
     * <p><b>A gate is a goal inside a goal.</b> A step that requires a state is
     * paid for after the path to that state, which may itself contain gated
     * steps; so a rank that needs a level pulls the level track's whole price
     * into the plan, EXP included. That is the only reason gates are in the
     * model at all. A plan for a character's last rank that leaves out the
     * level the rank is gated on is cheaper than the truth by most of the goal.
     */
    private record Payment(
            Goal goal,
            Map<String, List<Upgrade>> edges,
            Set<String> achieved,
            GameDefinition definition,
            Map<ItemId, Integer> totals,
            List<String> steps,
            Set<String> counted,
            Set<String> gating) {

        Payment(Goal goal, Map<String, List<Upgrade>> edges, Set<String> achieved, GameDefinition definition,
                Map<ItemId, Integer> totals, List<String> steps, Set<String> counted) {
            this(goal, edges, achieved, definition, totals, steps, counted, new HashSet<>());
        }

        void payUpTo(String state, boolean mustBeReachable) {
            for (List<Upgrade> prices : pathTo(goal, state, mustBeReachable, edges, achieved, definition)) {
                Upgrade step = prices.get(0);
                if (counted.contains(step.id())) continue;
                if (!gating.add(step.id())) {
                    throw new Optimizer.InfeasibleGoalException(
                            "upgrade \"" + step.id() + "\" for " + goal.entity() + " is gated on a state"
                                    + " that can only be reached through that upgrade itself");
                }
                for (String required : step.requires()) {
                    if (!achieved.contains(required)) payUpTo(required, false);
                }
                gating.remove(step.id());

                if (prices.size() > 1) {
                    // One step, several prices: owe the step, and let the solver
                    // pick the price. Charging all of them would be a plan for
                    // doing the same thing three times.
                    prices.forEach(price -> counted.add(price.id()));
                    steps.add(String.join(" or ", prices.stream().map(Upgrade::id).toList()));
                    totals.merge(Demand.choiceItem(step), 1, Math::addExact);
                    continue;
                }
                counted.add(step.id());
                steps.add(step.id());
                for (ItemStack cost : step.costs()) {
                    totals.merge(cost.item(), cost.quantity(), Math::addExact);
                }
                for (Progress progress : step.progress()) {
                    totals.merge(Demand.progressItem(progress.kind()), progress.quantity(), Math::addExact);
                }
            }
        }
    }

    /** {@code entity -> toState -> the upgrades that arrive at it}. */
    private static Map<EntityId, Map<String, List<Upgrade>>> incomingEdges(GameDefinition definition) {
        Map<EntityId, Map<String, List<Upgrade>>> index = new HashMap<>();
        for (Sink sink : definition.sinks()) {
            if (sink instanceof Upgrade upgrade) {
                index.computeIfAbsent(upgrade.entity(), e -> new HashMap<>())
                        .computeIfAbsent(upgrade.toState(), s -> new ArrayList<>())
                        .add(upgrade);
            }
        }
        return index;
    }

    /**
     * Every state the player is at, has passed through, or must have reached to
     * be standing where they are.
     *
     * <p>Needed so that a goal already behind the player costs nothing rather
     * than costing the whole track again, and so that the walk below knows where
     * to stop. A current state the graph has never heard of is still achieved —
     * the bundle does not get to tell a player they are not where they are.
     *
     * <p><b>A crossed gate is a reached state, and the roster does not have to
     * say so.</b> The roster holds one state per entity, so a player recorded on
     * one track says nothing directly about the others. But an upgrade they have
     * demonstrably taken could only have been taken with its gates satisfied —
     * that is what a gate is, a condition the game itself enforced before
     * letting them through. So every gate on every upgrade behind them is
     * behind them too, and charging for it again bills a player for something
     * the game already made them do.
     *
     * <p>Only what <em>every</em> parent demands is claimed. Several upgrades
     * arriving at one state are either one step at several prices, which share
     * their gates and so intersect to themselves, or different routes — and
     * which route was taken is unknown here, so the intersection is the most
     * that is certain. {@link #pathTo} refuses the second shape outright, which
     * is why this is a safeguard rather than a live case.
     *
     * <p>This assumes a gated state cannot be lost once reached. Every state in
     * both published games is monotone — a level, a rank, an evolution — and a
     * game where one can be given up would need this walk to stop at the gate
     * rather than pass through it.
     */
    private static Set<String> achieved(Map<String, List<Upgrade>> edges, String current) {
        if (current == null) return Set.of();

        Set<String> seen = new LinkedHashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(current);
        while (!queue.isEmpty()) {
            String state = queue.removeFirst();
            if (!seen.add(state)) continue;
            List<Upgrade> parents = edges.getOrDefault(state, List.of());
            for (Upgrade edge : parents) {
                queue.add(edge.fromState());
            }
            if (!parents.isEmpty()) {
                Set<String> certain = new LinkedHashSet<>(parents.get(0).requires());
                parents.forEach(parent -> certain.retainAll(parent.requires()));
                queue.addAll(certain);
            }
        }
        return seen;
    }

    /**
     * The upgrades between {@code achieved} and {@code target}, in the order they
     * are performed. Each element is one step: a single upgrade, or several that
     * make the same move at different prices.
     *
     * @param mustBeReachable true for the goal itself, which has to be reached by
     *                        something; false for a gate, which may be the start
     *                        of its track and so already met by owning the entity
     */
    private static List<List<Upgrade>> pathTo(
            Goal goal,
            String target,
            boolean mustBeReachable,
            Map<String, List<Upgrade>> edges,
            Set<String> achieved,
            GameDefinition definition) {

        List<List<Upgrade>> reversed = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(target);

        while (!queue.isEmpty()) {
            String state = queue.removeFirst();
            if (achieved.contains(state) || !visited.add(state)) continue;

            List<Upgrade> parents = edges.getOrDefault(state, List.of());
            if (parents.isEmpty()) {
                if (mustBeReachable && state.equals(target)) {
                    throw new Optimizer.InfeasibleGoalException(
                            "no upgrade reaches state \"" + state + "\" for " + goal.entity()
                                    + " in " + definition.game().id() + " "
                                    + definition.version().label());
                }
                continue; // the start of a track: nothing precedes it, and nothing is owed for it
            }
            Upgrade first = parents.get(0);
            boolean onePath = parents.stream().allMatch(p ->
                    p.fromState().equals(first.fromState()) && p.requires().equals(first.requires()));
            if (!onePath) {
                // Several prices for one step are a choice the solver makes
                // (see Demand#choiceItem). Several *routes* are not: which
                // states are passed through, and so which gates are paid, would
                // change with the choice, and a demand vector cannot branch.
                throw new UnsupportedOperationException(
                        "state \"" + state + "\" for " + goal.entity() + " is reachable by "
                                + parents.size() + " upgrades from different states or behind"
                                + " different gates, and a choice of route is not modelled");
            }
            reversed.add(List.copyOf(parents));
            queue.add(first.fromState());
        }
        return reversed.reversed();
    }
}
