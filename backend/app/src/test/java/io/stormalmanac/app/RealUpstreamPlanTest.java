package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gamedata.Craft;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.gamedata.Goal;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Stage;
import io.stormalmanac.gamedata.Upgrade;
import io.stormalmanac.gamedata.ingest.CanonicalBundleParser;
import io.stormalmanac.gamedata.ingest.CanonicalBundleWriter;
import io.stormalmanac.gamedata.ingest.GameDataBundle;
import io.stormalmanac.gamedata.ingest.UpstreamAdapter;
import io.stormalmanac.planner.Conversion;
import io.stormalmanac.planner.MipOptimizer;
import io.stormalmanac.planner.Optimizer;
import io.stormalmanac.planner.Objective;
import io.stormalmanac.planner.Plan;
import io.stormalmanac.planner.SolveRequest;
import io.stormalmanac.planner.StageRun;
import io.stormalmanac.player.Goals;
import io.stormalmanac.player.Inventory;
import io.stormalmanac.player.PlayerProfile;
import io.stormalmanac.player.PlayerStateRepository;
import io.stormalmanac.player.Roster;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

/**
 * The optimizer against a real patch of somebody else's game.
 *
 * <p>Ninety-nine stages, real drop tables carrying three decimal places, and
 * upgrade costs nobody here chose. This is the only place the solver meets
 * numbers that were not arranged to make its sums come out, and it is therefore
 * the only place its p95 means anything: a model with three stages and one craft
 * says nothing about a branch-and-bound tree over a hundred integer variables.
 *
 * <h2>Why this test skips itself</h2>
 *
 * <p>Same reason as {@code RealUpstreamPatchTest}, and it is worth repeating
 * rather than cross-referencing: the snapshot is fetched and never committed
 * ({@code docs/adr/0009}), so CI has nothing to run this against and skips it.
 * <b>The strongest evidence for the optimizer is evidence the pipeline does not
 * produce.</b> Run it before believing a green build:
 *
 * <pre>
 * ./tools/fetch-upstream.sh
 * ./gradlew :app:test --tests '*RealUpstreamPlanTest'
 * </pre>
 *
 * <h2>What this does not yet claim</h2>
 *
 * <p>Phase 2's exit criterion is agreement with community-accepted answers on
 * five benchmark goal sets. That needs the community answers, which are a thing
 * to go and find rather than a thing to compute, so what is checked here is
 * weaker and still worth having: that a plan over real data is <em>sufficient</em>
 * — apply its runs at the declared yields and every demanded item is covered —
 * and that it is no worse than the obvious plan a player would make by hand.
 * A solver that is fast, self-consistent and confidently wrong is the failure
 * mode the plan warns about, and only the benchmark sets close it.
 */
@EnabledIf("snapshotIsPresent")
class RealUpstreamPlanTest {

    private static final GameId REVERSE_1999 = new GameId("reverse-1999");
    private static final String PATCH = "3.5";
    private static final Instant NOW = Instant.parse("2026-09-07T12:00:00Z");
    private static final ProfileId PROFILE = new ProfileId("real-plan");

    private final GameDefinition definition = load();

    @Test
    @DisplayName("a plan over a real patch covers every material the goal actually costs")
    void aRealPlanIsSufficient() {
        List<Goal> goals = fiveCharactersTo("insight-2");
        Plan plan = solve(goals, Inventory.empty(PROFILE));

        assertThat(plan.stageRuns()).isNotEmpty();
        assertThat(plan.totalEnergy()).isPositive();
        assertThat(plan.computedAgainst().label()).isEqualTo(PATCH);
        assertThat(plan.computedAgainst().attribution()).contains("Kornblume");

        // The assertion that matters: hand the plan back to the model and check
        // it pays for itself. A solver returning a cheaper-looking plan that does
        // not actually reach the goal is the one failure a timing test would miss.
        Map<ItemId, Double> supplied = supply(plan);
        Map<ItemId, Integer> owed = demandOf(goals);
        assertThat(owed).isNotEmpty();
        owed.forEach((item, quantity) -> assertThat(supplied.getOrDefault(item, 0.0))
                .as("the plan must actually produce the %d %s it owes", quantity, item.value())
                .isGreaterThanOrEqualTo(quantity - 1e-6));
    }

    @Test
    @DisplayName("a plan is never worse than farming each material at its own cheapest stage")
    void beatsThePlanAPlayerWouldMakeByHand() {
        // The baseline is what a wiki tells someone to do: for each material,
        // find the stage with the best energy-per-unit and run it enough times.
        // It is a feasible plan, so the solver's answer must be at most as dear —
        // and it should be strictly cheaper, because one run of one stage drops
        // five different materials and the baseline cannot see that.
        List<Goal> goals = fiveCharactersTo("insight-2");
        Plan plan = solve(goals, Inventory.empty(PROFILE));

        int baseline = perItemBaseline(demandOf(goals));
        assertThat(baseline).isPositive();
        assertThat(plan.totalEnergy()).isLessThanOrEqualTo(baseline);
    }

    @Test
    @DisplayName("what the player owns comes off the plan, on real costs")
    void inventoryReducesARealPlan() {
        List<Goal> goals = fiveCharactersTo("insight-2");
        Plan full = solve(goals, Inventory.empty(PROFILE));

        Inventory stocked = Inventory.empty(PROFILE);
        for (Map.Entry<ItemId, Integer> owed : demandOf(goals).entrySet()) {
            stocked = stocked.with(owed.getKey(), owed.getValue());
        }
        Plan nothingToDo = solve(goals, stocked);

        assertThat(full.totalEnergy()).isPositive();
        assertThat(nothingToDo.totalEnergy()).isZero();
        assertThat(nothingToDo.stageRuns()).isEmpty();
    }

    @Test
    @DisplayName("a plan that could not be proven cheapest says how much cheaper one could be")
    void doubtIsQuantified() {
        // "We ran out of time" is true and unhelpful. The linear relaxation gives
        // a bound no integer plan can beat, so the doubt gets a number — and that
        // number is what ADR 0004's reversal trigger should actually be read
        // against, rather than the wall clock alone.
        Plan plan = solve(fiveCharactersTo("insight-2"), Inventory.empty(PROFILE));

        String budgetNote = plan.explanation().notes().stream()
                .filter(note -> note.contains("time budget"))
                .findFirst()
                .orElse(null);
        if (budgetNote == null) {
            // Proven optimal on this machine. Nothing to qualify, and nothing to
            // fail: the assertion is about what a plan says when it cannot.
            return;
        }
        System.out.println("RealUpstreamPlanTest: " + budgetNote);
        assertThat(budgetNote).matches(".*no plan can be more than \\d+\\.\\d\\d% cheaper\\.");
    }

    @Test
    @DisplayName("p95 of fifty real solves is under the two-second synchronous budget")
    void staysInsideTheBudget() {
        // Phase 2's other exit criterion, and the only honest place to measure
        // it: 99 stages and 2 000-odd upgrades, not a fixture with three.
        List<Goal> goals = fiveCharactersTo("insight-2");
        Inventory empty = Inventory.empty(PROFILE);

        for (int warmup = 0; warmup < 5; warmup++) {
            solve(goals, empty);
        }
        List<Long> millis = new ArrayList<>();
        for (int run = 0; run < 50; run++) {
            long start = System.nanoTime();
            solve(goals, empty);
            millis.add((System.nanoTime() - start) / 1_000_000);
        }
        millis.sort(Long::compare);
        long p95 = millis.get((int) Math.ceil(0.95 * millis.size()) - 1);

        System.out.printf(
                "RealUpstreamPlanTest: %d solves over %s %s — median %d ms, p95 %d ms, max %d ms%n",
                millis.size(), REVERSE_1999.value(), PATCH,
                millis.get(millis.size() / 2), p95, millis.get(millis.size() - 1));

        // Each solve here also computes shadow prices, which are one extra solve
        // per demanded item. The budget covers the whole answer, explanation
        // included, because that is what a caller waits for.
        assertThat(p95).isLessThan(2000);
    }

    @Test
    @DisplayName("some real goals are impossible in this patch, and the refusal names the material")
    void someGoalsAreHonestlyImpossible() {
        // The finding that arrived with this test and is worth keeping as one.
        // The upstream carries a synthetic "Unreleased" stage so its own solver
        // can name materials that no real stage drops yet; the adapter refuses it
        // because a zero-cost source is an unbounded one. The consequence is that
        // Cicada Wings, Perpetual Cog and the two recipes needing them have no
        // source in this snapshot at all — so a plan for a character who needs
        // them cannot exist, and pretending otherwise would be the confidently
        // wrong answer the plan warns about.
        List<Goal> refused = new ArrayList<>();
        List<String> reasons = new ArrayList<>();
        for (EntityId entity : charactersReaching("insight-2")) {
            Goal goal = Goal.deterministic(entity, "insight-2");
            try {
                solve(List.of(goal), Inventory.empty(PROFILE));
            } catch (Optimizer.InfeasibleGoalException e) {
                refused.add(goal);
                reasons.add(e.getMessage());
            }
        }

        assertThat(refused).as("this snapshot has characters whose materials it cannot supply")
                .isNotEmpty();
        assertThat(reasons).allSatisfy(reason -> assertThat(reason)
                .as("a refusal has to name the item, not just say no")
                .startsWith("nothing available can produce "));
        System.out.printf("RealUpstreamPlanTest: %d of %d insight-2 goals are unsupplyable in %s; e.g. %s%n",
                refused.size(), charactersReaching("insight-2").size(), PATCH, reasons.get(0));
    }

    @Test
    @DisplayName("every line of a real plan names something that exists in the patch it was solved against")
    void theAnswerIsLegible() {
        Plan plan = solve(fiveCharactersTo("insight-2"), Inventory.empty(PROFILE));

        Map<ItemId, String> names = new HashMap<>();
        definition.items().forEach(item -> names.put(item.id(), item.displayName()));
        System.out.println(render(plan, names));

        // A plan is only useful if a player can find what it refers to. Every id
        // in it has to resolve in the same version it was computed against —
        // which is also the guard against a stage or craft id being fabricated
        // by an off-by-one in the solver's variable indexing.
        Map<String, String> stageNames = new HashMap<>();
        definition.stages().forEach(stage -> stageNames.put(stage.stageId().value(), stage.displayName()));
        assertThat(plan.stageRuns()).isNotEmpty().allSatisfy(run ->
                assertThat(stageNames).containsKey(run.stage().value()));
        assertThat(plan.conversions()).allSatisfy(made ->
                assertThat(definition.crafts()).extracting(Craft::id).contains(made.sourceOrSinkId()));

        assertThat(plan.explanation().notes())
                .anySatisfy(note -> assertThat(note).contains("declared yield"))
                .anySatisfy(note -> assertThat(note).contains("insight-2"));
    }

    // ── the goal sets ───────────────────────────────────────────────────────

    /**
     * Five characters whose requested state this snapshot can actually supply,
     * chosen by walking the bundle rather than by naming favourites.
     *
     * <p>A hard-coded roster would be a game-specific fixture in the one place
     * this project promises not to keep one, and it would rot the first time the
     * upstream renamed somebody. What is hard-coded is the shape of the question.
     *
     * <p>The filter is not fastidiousness. Some characters' Insight 2 costs
     * materials this snapshot has no source for at all — see
     * {@link #someGoalsAreHonestlyImpossible()} — and a goal set that mixes the
     * two would measure the refusal path instead of the solver.
     */
    private List<Goal> fiveCharactersTo(String state) {
        List<Goal> feasible = new ArrayList<>();
        for (EntityId entity : charactersReaching(state)) {
            Goal goal = Goal.deterministic(entity, state);
            try {
                solve(List.of(goal), Inventory.empty(PROFILE));
                feasible.add(goal);
            } catch (Optimizer.InfeasibleGoalException e) {
                // Not this one. The material does not exist in this patch.
            }
            if (feasible.size() == 5) break;
        }
        assertThat(feasible).as("the snapshot must carry five supplyable %s goals", state).hasSize(5);
        return List.copyOf(feasible);
    }

    private List<EntityId> charactersReaching(String state) {
        return definition.sinks().stream()
                .filter(Upgrade.class::isInstance).map(Upgrade.class::cast)
                .filter(upgrade -> upgrade.toState().equals(state))
                .map(Upgrade::entity)
                .distinct()
                .sorted(Comparator.comparing(EntityId::value))
                .toList();
    }

    /** The same walk the resolver does, repeated here so the assertions do not trust it. */
    private Map<ItemId, Integer> demandOf(List<Goal> goals) {
        Map<ItemId, Integer> owed = new HashMap<>();
        for (Goal goal : goals) {
            for (Upgrade upgrade : definition.sinks().stream()
                    .filter(Upgrade.class::isInstance).map(Upgrade.class::cast)
                    .filter(u -> u.entity().equals(goal.entity()))
                    .filter(u -> u.toState().equals("insight-1") || u.toState().equals("insight-2"))
                    .toList()) {
                for (ItemStack cost : upgrade.costs()) {
                    owed.merge(cost.item(), cost.quantity(), Integer::sum);
                }
            }
        }
        return owed;
    }

    /** What the plan's runs and crafts actually yield, at the declared rates. */
    private Map<ItemId, Double> supply(Plan plan) {
        Map<ItemId, Double> supplied = new HashMap<>();
        Map<String, Stage> stages = new HashMap<>();
        definition.stages().forEach(stage -> stages.put(stage.stageId().value(), stage));

        for (StageRun run : plan.stageRuns()) {
            Stage stage = stages.get(run.stage().value());
            stage.drops().forEach(drop ->
                    supplied.merge(drop.item(), drop.expectedYield() * run.runs(), Double::sum));
        }
        Map<String, Craft> crafts = new HashMap<>();
        definition.crafts().forEach(craft -> crafts.put(craft.id(), craft));
        for (Conversion conversion : plan.conversions()) {
            Craft craft = crafts.get(conversion.sourceOrSinkId());
            craft.produces().forEach(stack ->
                    supplied.merge(stack.item(), (double) stack.quantity() * conversion.times(), Double::sum));
            craft.consumes().forEach(stack ->
                    supplied.merge(stack.item(), -(double) stack.quantity() * conversion.times(), Double::sum));
        }
        return supplied;
    }

    /** Each material farmed at its own best stage, ignoring that runs drop several. */
    private int perItemBaseline(Map<ItemId, Integer> owed) {
        int total = 0;
        for (Map.Entry<ItemId, Integer> entry : owed.entrySet()) {
            double best = Double.MAX_VALUE;
            int runs = 0;
            for (Stage stage : definition.stages()) {
                for (var drop : stage.drops()) {
                    if (!drop.item().equals(entry.getKey()) || drop.expectedYield() <= 0) continue;
                    double perUnit = stage.energyCost() / drop.expectedYield();
                    if (perUnit < best) {
                        best = perUnit;
                        runs = (int) Math.ceil(entry.getValue() / drop.expectedYield()) * stage.energyCost();
                    }
                }
            }
            total += runs;
        }
        return total;
    }

    private static String render(Plan plan, Map<ItemId, String> names) {
        StringBuilder out = new StringBuilder("plan ").append(plan.id().value())
                .append(" · ").append(plan.totalEnergy()).append(" Activity · ")
                .append(String.format("%.1f", plan.etaDays())).append(" days\n");
        plan.stageRuns().forEach(run -> out.append("  run ").append(run.stage().value())
                .append(" x").append(run.runs())
                .append("  (").append(run.totalEnergy()).append(")\n"));
        plan.conversions().forEach(made -> out.append("  craft ").append(made.sourceOrSinkId())
                .append(" x").append(made.times()).append('\n'));
        plan.explanation().shadowPrice().forEach((item, price) -> out.append("  one more ")
                .append(names.getOrDefault(item, item.value()))
                .append(" costs ").append(price).append(" Activity\n"));
        plan.explanation().notes().forEach(note -> out.append("  · ").append(note).append('\n'));
        return out.toString();
    }

    // ── plumbing ────────────────────────────────────────────────────────────

    private Plan solve(List<Goal> goals, Inventory inventory) {
        MipOptimizer optimizer = new MipOptimizer(
                new OneVersion(definition),
                new FixedPlayer(REVERSE_1999, inventory),
                null,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofSeconds(2));

        return optimizer.solve(new SolveRequest(
                PROFILE, definition.version(), goals, Objective.LEAST_ENERGY, 240));
    }

    private static GameDefinition load() {
        UpstreamAdapter adapter = UpstreamAdapters.forGame(REVERSE_1999, note -> { }).orElseThrow();
        GameDataBundle converted = adapter.adapt(snapshot().resolve(PATCH), 0, PATCH);
        return new CanonicalBundleParser()
                .parse(new CanonicalBundleWriter().write(converted))
                .definitionApprovedAt(Instant.EPOCH);
    }

    static Path snapshot() {
        String configured = System.getProperty("storm-almanac.upstream");
        return configured != null ? Path.of(configured) : Path.of("..", "build", "upstream-snapshots");
    }

    @SuppressWarnings("unused") // named by @EnabledIf
    static boolean snapshotIsPresent() {
        return Files.isReadable(snapshot().resolve(PATCH).resolve("items.json"));
    }

    private record OneVersion(GameDefinition definition) implements GameDefinitionRepository {
        @Override
        public Optional<GameDefinition> findLatest(GameId game) {
            return find(game, definition.version().sequence());
        }

        @Override
        public Optional<GameDefinition> find(GameId game, long sequence) {
            return game.equals(definition.game().id()) && sequence == definition.version().sequence()
                    ? Optional.of(definition)
                    : Optional.empty();
        }

        @Override
        public List<GameDataVersion> versions(GameId game) {
            return List.of(definition.version());
        }
    }

    private record FixedPlayer(GameId game, Inventory inventory) implements PlayerStateRepository {
        @Override
        public List<PlayerProfile> profilesOf(AccountId account) {
            return List.of(profile());
        }

        @Override
        public Optional<PlayerProfile> findProfile(ProfileId id) {
            return Optional.of(profile());
        }

        private PlayerProfile profile() {
            return new PlayerProfile(PROFILE, new AccountId("real-plan"), game, "Tester", "global");
        }

        @Override
        public Inventory inventoryOf(ProfileId profile) {
            return inventory;
        }

        @Override
        public Roster rosterOf(ProfileId profile) {
            return new Roster(profile, Map.of());
        }

        @Override
        public Goals goalsOf(ProfileId profile) {
            return new Goals(profile, List.of());
        }

        @Override
        public void saveInventory(Inventory value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void saveRoster(Roster value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void saveGoals(Goals value) {
            throw new UnsupportedOperationException();
        }
    }
}
