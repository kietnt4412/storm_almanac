package io.stormalmanac.planner;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.PlanId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.common.id.StageId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.gamedata.Shop;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The {@link Optimizer} port, wired to the real repositories.
 *
 * <p>This class does the fetching and the narrating; {@link EnergyMip} does the
 * arithmetic and {@link DemandResolver} does the graph walk. Keeping them apart
 * is what lets the model be tested without a database and lets the shadow prices
 * below be computed by re-solving rather than by trusting a dual value.
 *
 * <p><b>On the two objectives.</b> Both are accepted and both are answered, and
 * since the model gained a time axis they are answered by genuinely different
 * searches: least energy takes the whole horizon and spends as little as it can
 * inside it, fewest days finds the shortest horizon the goal set fits into and
 * then spends as little as it can inside <em>that</em>. Whether the two plans
 * differ is a fact about the game's data rather than about the model — where
 * nothing rotates and nothing accrues on a cadence, days are energy divided by a
 * constant and the two coincide. The plan's notes say which case a reader is
 * looking at, because "these came out the same" and "these are the same
 * question" are different claims.
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
                definition, yields, inventory.quantities(), demand.quantities(), now, searchMillis,
                request.objective(), request.energyPerDay(), request.horizonDays(), request.reach());

        EnergyMip.Outcome outcome = EnergyMip.solve(inputs);

        Plan plan = new Plan(
                PlanId.of("plan-" + key),
                profile.id(),
                definition.version(),
                request.objective(),
                outcome.stageRuns(),
                outcome.conversions(),
                outcome.rewardClaims(),
                outcome.totalEnergy(),
                etaDays(outcome, request.energyPerDay()),
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
                cached.rewardClaims(),
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
     * How long the plan takes: the longer of what its energy costs and what its
     * calendar costs.
     *
     * <p>Energy is spent at a flat rate, so that half is a division and is
     * deliberately not rounded up — "3.4 days" is a truer thing to show a player
     * than "4 days", and the rounding belongs to whoever renders it. The other
     * half comes in whole days and cannot be divided at all: a plan leaning on
     * four weekly quests takes four weeks even with the energy to spare, and a
     * plan needing nine runs of a Tuesday-and-Friday stage waits for Tuesdays.
     *
     * <p>Where a game declares neither cadences nor rotation the second half is
     * zero and this is the division it always was.
     */
    private static double etaDays(EnergyMip.Outcome outcome, int energyPerDay) {
        if (energyPerDay <= 0) {
            throw new IllegalArgumentException("energyPerDay must be positive, was " + energyPerDay);
        }
        return Math.max(outcome.totalEnergy() / (double) energyPerDay, outcome.daysNeeded());
    }

    private Explanation explain(
            SolveRequest request,
            Demand demand,
            YieldTable yields,
            EnergyMip.Inputs inputs,
            EnergyMip.Outcome outcome,
            long startedAtNanos) {

        List<String> notes = new ArrayList<>();
        notes.add(("Minimised energy over %d stage(s), %d craft(s), %d shop offer(s) and %d"
                + " reward(s), against %d item")
                .formatted(outcome.stageVariables(), outcome.craftVariables(),
                        outcome.shopVariables(), outcome.rewardVariables(), outcome.constraints())
                + " constraint(s), inside a %d-day horizon at %d energy a day."
                        .formatted(outcome.horizonUsed(), request.energyPerDay()));
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

        if (!outcome.rewardClaims().isEmpty()) {
            // The single biggest way this plan can be wrong in practice, and it
            // is not the solver's fault: a plan is cheap partly because somebody
            // logs in. Saying which grants and how many turns that from an
            // assumption into something a reader can check against their own week.
            notes.add("Counting on free income over the horizon: " + outcome.rewardClaims().stream()
                    .map(claim -> claim.reward() + " ×" + claim.times())
                    .reduce((a, b) -> a + ", " + b).orElse("")
                    + ". Miss those and the plan costs more energy than it says.");
        }
        if (!outcome.withheldGrants().isEmpty()) {
            // The mirror of the note above, and the more actionable of the two:
            // that one says what the plan assumes a reader will collect, this one
            // says what it refused to assume. A reader who does clear the weekly
            // is holding a cheaper plan than the one in front of them, and the
            // only way they find that out is if the plan says so.
            notes.add("Not counted, because nothing says this account can collect them: "
                    + outcome.withheldGrants().stream()
                            .map(grant -> grant.reward() + " (needs " + grant.atLeast() + " of "
                                    + grant.measure() + "; this plan was asked for " + grant.said() + ")")
                            .collect(Collectors.joining(", "))
                    + ". Say what you reach and the plan gets cheaper, never dearer.");
        }
        if (!outcome.expiringClaims().isEmpty()) {
            // The supply is already right — occurrences truncates against the
            // close — so this note adds nothing to the arithmetic and everything
            // to whether it happens. A plan cannot collect a grant; a reader on
            // a Thursday can (ADR 0024).
            notes.add("On a deadline, and this plan is counting on them: "
                    + outcome.expiringClaims().stream()
                            .map(claim -> claim.reward() + " ×" + claim.times() + ", which closes "
                                    + claim.closesAt() + ", " + claim.daysLeft() + " day(s) in")
                            .collect(Collectors.joining("; "))
                    + ". The counts above already stop at those dates; what this plan cannot do is"
                    + " remind you on the day.");
        }
        if (!outcome.lapsedGrants().isEmpty()) {
            // The mirror of the deadline note, pointing at the past. Nothing is
            // actionable here and it is still worth a line: without it, a plan
            // made dearer by an event that ended is indistinguishable from a
            // plan that was always that dear.
            notes.add("Closed too early to pay out once, and this goal set needed what they grant: "
                    + outcome.lapsedGrants().stream()
                            .map(grant -> grant.reward() + " (window ends " + grant.closesAt()
                                    + "; this horizon would otherwise have allowed "
                                    + grant.missedClaims() + ")")
                            .collect(Collectors.joining(", "))
                    + ". Nothing can be done about a window that has shut — this is here so the"
                    + " price of the plan is not a mystery.");
        }
        Set<String> lifetime = inputs.definition().shops().stream()
                .filter(Shop::neverResets).map(Shop::id).collect(Collectors.toSet());
        List<String> spendsLifetime = outcome.conversions().stream()
                .filter(conversion -> lifetime.contains(conversion.sourceOrSinkId()))
                .map(conversion -> conversion.sourceOrSinkId() + " ×" + conversion.times())
                .toList();
        if (!spendsLifetime.isEmpty()) {
            // The same kind of assumption as the rewards above, pointing the
            // other way: nothing a player records says how much of an allowance
            // that never refills they have already bought, so the plan assumes
            // none, and a reader who has bought some is the one who can tell.
            notes.add("Buying from a limit that never resets: " + String.join(", ", spendsLifetime)
                    + ". This assumes none of that allowance has been bought yet; whatever has been"
                    + " must come from elsewhere.");
        }
        if (outcome.daysNeeded() > 0) {
            notes.add(("This plan cannot be finished in less than %d day(s) however much energy is"
                    + " spare, because it waits on a reward cadence or on a stage that is only open"
                    + " some weekdays.").formatted(outcome.daysNeeded()));
        }
        if (!outcome.rotationExact()) {
            notes.add("This game declares more distinct weekday restrictions than are checked"
                    + " jointly, so the schedule was checked one restriction at a time. Two sets of"
                    + " stages competing for the same weekday may between them want more of it than"
                    + " the horizon holds.");
        }

        if (request.objective() == Objective.FEWEST_DAYS) {
            notes.add(outcome.horizonUsed() < request.horizonDays()
                    ? ("The shortest horizon this goal set fits into is %d day(s), against the %d"
                            + " asked for; this is the cheapest plan inside it.")
                                    .formatted(outcome.horizonUsed(), request.horizonDays())
                    : ("This goal set needs the whole %d-day horizon, so fewest days and least"
                            + " energy are asking the same question of it.")
                                    .formatted(request.horizonDays()));
        }
        if (request.objective() == Objective.FEWEST_DAYS && !outcome.expiringClaims().isEmpty()) {
            // The one thing a reader might reasonably try after reading the
            // deadline note is to ask for a different horizon, so say which
            // direction that moves them. An expiring grant is capped by its own
            // end date, so a longer plan collects no more of it; a shorter one —
            // which is what this objective searches for — can collect fewer, if
            // the horizon it settles on ends before the window does. That is the
            // whole trade, and reporting it is what ADR 0024 does instead of
            // giving the claim a day index (ADR 0013).
            notes.add(("Fewest days was asked for, and %d of the grant(s) above close inside the"
                    + " %d-day horizon this search settled on. Their supply is fixed by their own"
                    + " end date rather than by how long the plan runs: a longer plan collects no"
                    + " more of them, and a shorter one may collect fewer.")
                    .formatted(outcome.expiringClaims().size(), outcome.horizonUsed()));
        }
        if (outcome.rewardVariables() == 0 && request.objective() == Objective.FEWEST_DAYS) {
            notes.add("Nothing in this game's data accrues on a cadence, so days here buy nothing"
                    + " but energy and the fastest plan is the cheapest one. That is a fact about"
                    + " the data, not about the model.");
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
     *
     * <p>Every re-solve is pinned to the horizon the plan itself settled on, and
     * asked for least energy whatever the plan's objective was. Otherwise a
     * fewest-days plan would price each marginal item against a calendar the
     * search had rediscovered for that item alone, and "one more sigil costs 40"
     * would silently mean "against a plan one day longer than yours".
     */
    private Map<ItemId, Double> shadowPrices(
            Demand demand,
            EnergyMip.Inputs inputs,
            EnergyMip.Outcome base,
            List<String> notes,
            long startedAtNanos) {

        long deadline = startedAtNanos + (long) (budget.toNanos() * ANSWER_DEADLINE_SHARE);
        EnergyMip.Inputs pinned = inputs.pinnedTo(base.horizonUsed());

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
                        pinned.withDemand(raised).withBudget(Math.min(remaining, inputs.budgetMillis())));
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
