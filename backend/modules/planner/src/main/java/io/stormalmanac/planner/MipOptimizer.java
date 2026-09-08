package io.stormalmanac.planner;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.PlanId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.common.id.StageId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.player.Inventory;
import io.stormalmanac.player.PlayerProfile;
import io.stormalmanac.player.PlayerStateRepository;
import io.stormalmanac.player.Roster;
import io.stormalmanac.stats.DropEstimateRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The {@link Optimizer} port, wired to the real repositories.
 *
 * <p>This class does the fetching and the narrating; {@link EnergyMip} does the
 * arithmetic and {@link DemandResolver} does the graph walk. Keeping them apart
 * is what lets the model be tested without a database and lets the shadow prices
 * below be computed by re-solving rather than by trusting a dual value.
 *
 * <p><b>On the two objectives.</b> Both are accepted and both are answered, and
 * under the current model they are answered by the same plan — a deliberately
 * stated fact rather than an oversight. With no time axis, the number of days a
 * plan takes is its energy divided by a constant, so the ordering of plans by
 * days is the ordering by energy. They separate exactly when the model gains
 * weekday rotation and expiring stages, because then a cheaper plan can be a
 * slower one. The plan's notes say so on every {@code FEWEST_DAYS} solve rather
 * than letting a caller infer that a distinct model ran.
 */
public final class MipOptimizer implements Optimizer {

    /**
     * The plan's synchronous budget. Past this the request belongs in the queue
     * {@link SolveCoordinator} describes; the solver is told so it returns the
     * best it has rather than running long.
     */
    public static final Duration DEFAULT_BUDGET = Duration.ofSeconds(2);

    /**
     * How much of the budget the search itself may have. The rest pays for the
     * explanation, which costs one re-solve per demanded item.
     *
     * <p>A count cap was the first attempt and was the wrong shape. Sixteen items
     * is nothing when a solve takes eight milliseconds and far too many when it
     * takes a second, and the number that matters to a caller is how long they
     * wait, not how many re-solves happened.
     */
    private static final double SEARCH_BUDGET_SHARE = 0.5;

    /**
     * The whole answer, explanation included, is finished by this fraction of the
     * budget. The margin exists because the budget is a promise to a caller
     * waiting on a request, and a solver told to stop at exactly two seconds
     * stops slightly after.
     */
    private static final double ANSWER_DEADLINE_SHARE = 0.9;

    /** Below this there is no point starting another re-solve. */
    private static final long MINIMUM_RESOLVE_MILLIS = 5;

    private final GameDefinitionRepository definitions;
    private final PlayerStateRepository players;
    private final DropEstimateRepository estimates;
    private final DemandResolver demands = new DemandResolver();
    private final Clock clock;
    private final Duration budget;
    private final SolveCache cache;

    /**
     * @param estimates measured drop rates, or {@code null} while nothing
     *                  publishes any. A null repository means "every coefficient
     *                  is the bundle's declared yield", which is the honest state
     *                  of the world until phase 6 and is said in the plan's notes
     * @param cache     where a finished plan is kept under its {@link SolveKey},
     *                  or {@code null} for no caching at all
     */
    public MipOptimizer(
            GameDefinitionRepository definitions,
            PlayerStateRepository players,
            DropEstimateRepository estimates,
            Clock clock,
            Duration budget,
            SolveCache cache) {
        this.definitions = definitions;
        this.players = players;
        this.estimates = estimates;
        this.clock = clock;
        this.budget = budget;
        this.cache = cache == null ? SolveCache.none() : cache;
    }

    public MipOptimizer(
            GameDefinitionRepository definitions,
            PlayerStateRepository players,
            DropEstimateRepository estimates,
            Clock clock,
            Duration budget) {
        this(definitions, players, estimates, clock, budget, SolveCache.none());
    }

    public MipOptimizer(GameDefinitionRepository definitions, PlayerStateRepository players) {
        this(definitions, players, null, Clock.systemUTC(), DEFAULT_BUDGET, SolveCache.none());
    }

    @Override
    public Plan solve(SolveRequest request) {
        long startedAtNanos = System.nanoTime();
        PlayerProfile profile = players.findProfile(request.profile())
                .orElseThrow(() -> new IllegalArgumentException(
                        "no profile " + request.profile().value()));

        GameDataVersion asked = request.gameVersion();
        if (!asked.game().equals(profile.game())) {
            throw new IllegalArgumentException(
                    "profile " + profile.id().value() + " plays " + profile.game().value()
                            + " and the request names " + asked.game().value());
        }
        GameDefinition definition = definitions.find(profile.game(), asked.sequence())
                .orElseThrow(() -> new IllegalArgumentException(
                        "no published version " + asked.sequence() + " of " + profile.game().value()));

        Inventory inventory = players.inventoryOf(profile.id());
        Roster roster = players.rosterOf(profile.id());

        // The lookup goes here and not earlier: the key is a fingerprint of the
        // player's state, so the state has to be read before the question can be
        // recognised. Those reads are three indexed lookups against the profile
        // and the solve they save is seconds, so the ordering costs nothing worth
        // measuring — but it does mean this cache never saves a database round
        // trip, only the arithmetic. Said plainly here because a cache that is
        // assumed to short-circuit more than it does is how a load test surprises
        // somebody.
        String key = SolveKey.of(definition.version(), request, inventory, roster);
        Optional<Plan> cached = cache.get(key);
        if (cached.isPresent()) {
            return served(cached.get(), profile.id());
        }

        Demand demand = demands.resolve(definition, roster, request.goals());

        Instant now = clock.instant();
        YieldTable yields = YieldTable.of(definition, estimates);
        // The budget covers the whole answer, so the search gets what is left
        // after the explanation's share. Handing the solver the full two seconds
        // and then explaining on top of that is how a "two-second budget" becomes
        // a four-second wait.
        long searchMillis =
                (long) (budget.toMillis() * SEARCH_BUDGET_SHARE);
        EnergyMip.Inputs inputs = new EnergyMip.Inputs(
                definition, yields, inventory.quantities(), demand.quantities(), now, searchMillis);

        EnergyMip.Outcome outcome = EnergyMip.solve(inputs);

        Plan plan = new Plan(
                PlanId.of("plan-" + key),
                profile.id(),
                definition.version(),
                request.objective(),
                outcome.stageRuns(),
                outcome.conversions(),
                outcome.totalEnergy(),
                etaDays(outcome.totalEnergy(), request.energyPerDay()),
                explain(request, demand, yields, inputs, outcome, startedAtNanos),
                now);

        // Stored as computed, without the "served from cache" note: that note
        // describes this delivery of the plan, not the plan, and a cached copy
        // that accumulated one per hit would be a plan whose explanation grew
        // every time somebody read it.
        cache.put(key, plan);
        return plan;
    }

    /**
     * A cached plan, handed to whoever asked this time.
     *
     * <p>Two things are re-stamped and one deliberately is not. The profile is,
     * because {@link SolveKey} fingerprints a player's <em>state</em> and not
     * their identity: two profiles holding the same items, the same roster and
     * the same goals ask the same question and deserve the same answer, but the
     * envelope has to name whoever is reading it. The note is added for the same
     * reason.
     *
     * <p>{@code computedAt} is <b>not</b> touched. A cache hit is a plan computed
     * earlier, and re-stamping the timestamp would turn that into a plan computed
     * now — a lie told by a field that exists to prevent exactly that, and one
     * nobody could catch from the outside.
     */
    private Plan served(Plan cached, ProfileId asker) {
        Duration age = Duration.between(cached.computedAt(), clock.instant());
        List<String> notes = new ArrayList<>(cached.explanation().notes());
        notes.add(("Served from cache: this plan was computed %s ago, against the same game"
                + " version, goals, inventory and roster. It was not re-solved. A patch"
                + " publishes a new version and so a different question, which is not in"
                + " this cache.")
                .formatted(readable(age)));

        return new Plan(
                cached.id(),
                asker,
                cached.computedAgainst(),
                cached.objective(),
                cached.stageRuns(),
                cached.conversions(),
                cached.totalEnergy(),
                cached.etaDays(),
                new Explanation(
                        cached.explanation().shadowPrice(),
                        cached.explanation().bindingStages(),
                        notes),
                cached.computedAt());
    }

    /** Coarse on purpose: nobody acts on the difference between 61 and 62 seconds. */
    private static String readable(Duration age) {
        long seconds = Math.max(0, age.getSeconds());
        if (seconds < 60) return seconds + "s";
        if (seconds < 3600) return (seconds / 60) + "m";
        if (seconds < 86_400) return (seconds / 3600) + "h";
        return (seconds / 86_400) + "d";
    }

    /**
     * Energy is spent at a flat rate here, so the estimate is a division. It is
     * the only place the request's {@code energyPerDay} is used, and it is
     * deliberately not rounded up: "3.4 days" is a truer thing to show a player
     * than "4 days", and the rounding belongs to whoever renders it.
     */
    private static double etaDays(int totalEnergy, int energyPerDay) {
        if (energyPerDay <= 0) {
            throw new IllegalArgumentException("energyPerDay must be positive, was " + energyPerDay);
        }
        return totalEnergy / (double) energyPerDay;
    }

    private Explanation explain(
            SolveRequest request,
            Demand demand,
            YieldTable yields,
            EnergyMip.Inputs inputs,
            EnergyMip.Outcome outcome,
            long startedAtNanos) {

        List<String> notes = new ArrayList<>();
        notes.add("Minimised energy over %d stage(s) and %d craft(s), against %d item constraint(s)."
                .formatted(outcome.stageVariables(), outcome.craftVariables(), outcome.constraints()));
        if (!outcome.provenOptimal()) {
            notes.add(Double.isNaN(outcome.optimalityGap())
                    ? "The search stopped on its time budget, so this is the cheapest plan found"
                            + " rather than the cheapest plan, and how much cheaper one could be is"
                            + " not known."
                    : "The search stopped on its time budget: this is the cheapest plan found, and"
                            + " no plan can be more than %.2f%% cheaper."
                                    .formatted(outcome.optimalityGap() * 100));
        }

        if (demand.steps().isEmpty()) {
            notes.add("Nothing to do: the roster already satisfies every goal.");
        } else {
            notes.add("Paying for %d upgrade step(s): %s."
                    .formatted(demand.steps().size(), String.join(", ", demand.steps())));
        }
        if (!demand.alreadyMet().isEmpty()) {
            notes.add("Already met, and not costed: " + demand.alreadyMet().stream()
                    .map(goal -> goal.entity().value() + " " + goal.targetState())
                    .reduce((a, b) -> a + ", " + b).orElse(""));
        }

        int measured = yields.measuredCount();
        notes.add(measured == 0
                ? "Every drop rate here is the bundle's declared yield. No player reports have"
                        + " been aggregated yet, so these numbers are the upstream's claim rather"
                        + " than a measurement."
                : measured + " drop coefficient(s) came from player reports; the rest are declared.");

        int discounted = yields.discountedCount();
        if (discounted > 0) {
            // Said out loud because it makes this plan cost more than the naive
            // arithmetic would, and a player comparing it against a community
            // guide deserves to know why rather than to find it out.
            notes.add(discounted + " drop rate(s) carry the number of runs they were sampled"
                    + " over, and are used at the conservative end of a 95% interval rather than"
                    + " at face value. A stage sampled a hundred times has to beat one sampled"
                    + " ten thousand times by more than luck before this plan will send you"
                    + " there.");
        }

        if (request.objective() == Objective.FEWEST_DAYS) {
            notes.add("Fewest days and least energy are the same plan under this model: with no"
                    + " time axis, days are energy divided by a constant. They separate once"
                    + " rotating and expiring stages are modelled.");
        }

        Map<ItemId, Double> shadowPrices = shadowPrices(demand, inputs, outcome, notes, startedAtNanos);
        List<StageId> binding = outcome.stageRuns().stream().map(StageRun::stage).toList();
        return new Explanation(shadowPrices, binding, List.copyOf(notes));
    }

    /**
     * What one more of each item would actually cost, in energy.
     *
     * <p>Computed by re-solving with the demand raised by one, not by reading a
     * dual value off the LP relaxation. ojAlgo will hand over multipliers, but
     * they are the <em>relaxation's</em> shadow prices, and this is an integer
     * program: the relaxation's marginal cost is not the marginal cost of one
     * more unit, and the sentence this number ends up in — "one more Greater
     * Sigil costs you 40 energy" — is a claim about the real plan. A re-solve is
     * more expensive and it is the number that was promised.
     *
     * <p>Zero is a real and useful answer: it means the item falls out of runs
     * the plan already makes for something else.
     */
    private Map<ItemId, Double> shadowPrices(
            Demand demand,
            EnergyMip.Inputs inputs,
            EnergyMip.Outcome base,
            List<String> notes,
            long startedAtNanos) {

        long deadline = startedAtNanos + (long) (budget.toNanos() * ANSWER_DEADLINE_SHARE);

        Map<ItemId, Double> prices = new LinkedHashMap<>();
        for (ItemId item : demand.quantities().keySet()) {
            // Each re-solve gets only what is left, so the last one cannot run
            // past the budget the caller was promised. On a small model this
            // never binds and every item is priced; on a hard one the plan comes
            // back with fewer prices and a line saying why, which is better than
            // a plan that arrives late.
            long remaining = (deadline - System.nanoTime()) / 1_000_000;
            if (remaining < MINIMUM_RESOLVE_MILLIS) {
                notes.add("Shadow prices stopped at %d of %d item(s): pricing one more of each costs"
                        .formatted(prices.size(), demand.quantities().size())
                        + " a re-solve, and the budget for this answer ran out.");
                break;
            }
            Map<ItemId, Integer> raised = new HashMap<>(demand.quantities());
            raised.merge(item, 1, Integer::sum);
            try {
                EnergyMip.Outcome marginal = EnergyMip.solve(
                        inputs.withDemand(raised).withBudget(Math.min(remaining, inputs.budgetMillis())));
                // Only comparable when both ends were solved to optimality. A
                // difference between two time-limited answers is noise with a
                // number on it, and a number a player would act on.
                if (base.provenOptimal() && marginal.provenOptimal()) {
                    prices.put(item, (double) (marginal.totalEnergy() - base.totalEnergy()));
                }
            } catch (Optimizer.InfeasibleGoalException e) {
                // One more unit put the goal set out of reach, which is worth
                // saying and is not worth failing the whole plan over.
                notes.add("One more " + item.value() + " is not obtainable: " + e.getMessage());
            }
        }
        return prices;
    }
}
