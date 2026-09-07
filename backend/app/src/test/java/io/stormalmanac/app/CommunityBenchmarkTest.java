package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.StageId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.Goal;
import io.stormalmanac.gamedata.Item;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Stage;
import io.stormalmanac.gamedata.Upgrade;
import io.stormalmanac.planner.Objective;
import io.stormalmanac.planner.Optimizer;
import io.stormalmanac.planner.Plan;
import io.stormalmanac.planner.SolveRequest;
import io.stormalmanac.player.Inventory;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

/**
 * Phase 2's exit criterion: does the optimizer agree with answers somebody else
 * worked out?
 *
 * <p>Everything else in this repository checks the solver against itself. A plan
 * covers its own demand, beats its own baseline, and comes in under its own
 * budget — all of which a confidently wrong model passes. The claims below were
 * not computed here. They come from a published community cheat sheet that names,
 * per material, the stage players are told to farm, quoting the drop rate it
 * expects. Provenance and the full table are in
 * {@code docs/benchmarks/reverse-1999-community-answers.md}.
 *
 * <h2>What agreement can and cannot mean</h2>
 *
 * <p>The guide answers "which stage is best for this material", which in our
 * model is {@code argmin} of Activity per unit at the declared yields, so that is
 * what is compared. It is a check on the <b>data and the model</b>, and only then
 * on the search: a stage ranking is arithmetic, and no branch-and-bound is
 * involved in getting it wrong.
 *
 * <p>The two sides are also not contemporaries. The guide is written against
 * patch 2.7 and this snapshot's drop tables were resampled at 3.3, so a
 * disagreement can be a stage that did not exist yet, a rate that was retuned, or
 * a genuine difference of opinion about what "best" means — the guide weighs the
 * other materials a run also drops, and ranks only Hard stages. Every one of
 * those is a fact about the comparison rather than a defect, which is why the
 * assertions here are about the <em>shape</em> of the agreement and the printed
 * table carries the detail.
 *
 * <h2>What this found</h2>
 *
 * <p>Writing it found the defect it was meant to find, before it ran once: the
 * community names stages this project's data did not contain, because the adapter
 * was reading the upstream's older stage table and had two thirds of the game
 * missing. See {@code KornblumeAdapter} and the session log for 2026-09-07.
 *
 * <p>What it found on running is subtler and is now the argument for
 * <b>Q8</b>: every disagreement of any size is a sample-size disagreement. The
 * upstream publishes how many runs each stage's drop counts were observed over —
 * from 105 to 41 212 — and our model throws that number away and treats a mean of
 * 105 runs as it treats a mean of 41 212.
 */
@EnabledIf("snapshotIsPresent")
class CommunityBenchmarkTest {

    /**
     * One published answer.
     *
     * @param material     the item, under the name <em>this</em> upstream gives it
     * @param alsoKnownAs  the guide's name for it where the two differ, so a
     *                     reader can find the row being cited. The rates are what
     *                     identify them: the guide's "Fox Tail" at 22.52% is this
     *                     upstream's Alopecurus Pratensis at 20.9%, which is the
     *                     same grass under a translation
     * @param stage        the stage the guide names as best for it
     * @param quotedRate   the drop rate the guide quotes there, as a proportion.
     *                     Above 1.0 is not a mistake — these are quantities per
     *                     run, which is the whole of {@code docs/prior-art.md} §4.1
     */
    record Answer(String material, String alsoKnownAs, String stage, double quotedRate) {

        static Answer of(String material, String stage, double quotedRate) {
            return new Answer(material, material, stage, quotedRate);
        }

        static Answer of(String material, String alsoKnownAs, String stage, double quotedRate) {
            return new Answer(material, alsoKnownAs, stage, quotedRate);
        }
    }

    /**
     * The guide's per-material picks, transcribed once.
     *
     * <p>Its "Magnesia Crystal at 2-9 Hard" is deliberately absent: 2-9H is not in
     * the upstream's sampled stage table at all, so this project has no opinion to
     * compare. That is recorded in the benchmark document rather than dropped
     * silently.
     */
    private static final List<Answer> ANSWERS = List.of(
            Answer.of("Bifurcated Skeleton", "10-13h", 0.4499),
            Answer.of("Biting Box", "7-26h", 0.3103),
            Answer.of("Clawed Pendulum", "11-5h", 0.4594),
            Answer.of("Holy Silver", "10-9h", 0.4797),
            Answer.of("Prophetic Bird", "2-6h", 0.4630),
            Answer.of("Salted Mandrake", "3-13h", 0.3413),
            Answer.of("Winged Key", "9-15h", 0.3255),
            Answer.of("Goose Neck", "5-4h", 0.2956),
            Answer.of("Red Lacquer Tablet", "Red Lacquer Slab", "9-1h", 0.3297),
            Answer.of("Golden Herb Incense", "Golden Grass Incense", "11-21h", 0.3882),
            Answer.of("Perpetual Cog", "7-16h", 0.2716),
            Answer.of("Pyroxene Ore", "Luminite Ore", "9-1h", 0.3066),
            Answer.of("Alopecurus Pratensis", "Fox Tail", "8-18h", 0.2252),
            Answer.of("Milled Magnesia", "5-8h", 0.2779),
            Answer.of("Rough Silver Ingot", "9-6h", 0.5895),
            Answer.of("Esoteric Bones", "5-7h", 0.2940),
            Answer.of("Liquefied Terror", "9-3h", 0.2139),
            Answer.of("Silver Ore", "7-19h", 2.1066),
            Answer.of("Spell of Banishing", "4-20h", 2.0855),
            Answer.of("Golden Beetle", "10-2h", 0.3130));

    /** A sample this small is a rumour; the guide's own picks sit in the thousands. */
    private static final int WELL_SAMPLED = 1_000;

    private final GameDefinition definition = RealUpstream.definition();
    private final Map<String, ItemId> byName = itemsByDisplayName();
    private final Map<String, Integer> sampleSizes = sampleSizes();

    @Test
    @DisplayName("every stage the community names as best is in this snapshot")
    void theCommunitysStagesExist() {
        // The cheapest of these assertions and the one that would have failed
        // loudest last session: with the older stage table, fourteen of these
        // twenty stages did not exist here at all. Nothing else noticed.
        List<String> missing = ANSWERS.stream()
                .map(Answer::stage)
                .filter(stage -> stageOf(stage) == null)
                .distinct()
                .toList();

        assertThat(missing)
                .as("a stage players are told to farm that this snapshot has never heard of"
                        + " means the stage table is short, not that the players are wrong")
                .isEmpty();
    }

    @Test
    @DisplayName("independently measured drop rates agree wherever both samples are large")
    void theNumbersAgree() {
        // Two measurements of the same game, taken by different people, a patch
        // apart. They should land on top of each other, and where they do not the
        // sample size says why — so the tolerance is only claimed where the
        // upstream's own sample is big enough to support one.
        List<String> disagreed = new ArrayList<>();
        int compared = 0;
        for (Answer answer : ANSWERS) {
            Stage stage = stageOf(answer.stage());
            if (sampleSizes.getOrDefault(stage.displayName(), 0) < WELL_SAMPLED) {
                continue;
            }
            compared++;
            double ours = yieldOf(stage, itemOf(answer));
            if (Math.abs(ours - answer.quotedRate()) > 0.03) {
                disagreed.add("%s at %s: they say %.1f%%, we measure %.1f%%".formatted(
                        answer.alsoKnownAs(), answer.stage(),
                        100 * answer.quotedRate(), 100 * ours));
            }
        }

        assertThat(compared).isGreaterThanOrEqualTo(15);
        assertThat(disagreed)
                .as("a rate this project cannot reproduce is a rate it should not plan against")
                .isEmpty();
    }

    @Test
    @DisplayName("on five benchmark materials the cheapest stage we compute is the one the community names")
    void agreesOnFiveGoalSets() {
        // Phase 2's exit criterion, in the form the community actually answers
        // it. Five is the number the plan asks for; the printed table is the
        // twenty, because reporting only the five that agree would be choosing
        // the benchmark after seeing the result.
        List<String> exact = new ArrayList<>();
        System.out.printf("%nCommunityBenchmarkTest — %s %s, %d stages%n",
                RealUpstream.REVERSE_1999.value(), RealUpstream.PATCH, definition.stages().size());
        System.out.printf("%-22s %-8s %8s %7s %5s  %-8s %8s %7s %7s%n",
                "material", "theirs", "per-unit", "runs", "rank", "ours", "per-unit", "runs", "gap");

        for (Answer answer : ANSWERS) {
            ItemId item = itemOf(answer);
            List<Stage> ranked = rankedFor(item);
            Stage claimed = stageOf(answer.stage());
            Stage best = ranked.getFirst();
            double gap = perUnit(claimed, item) / perUnit(best, item) - 1;
            if (best.equals(claimed)) {
                exact.add(answer.material());
            }
            System.out.printf("%-22s %-8s %8.1f %7d %2d/%-3d %-8s %8.1f %7d %6.1f%%%n",
                    answer.material(), answer.stage(), perUnit(claimed, item),
                    sampleSizes.getOrDefault(claimed.displayName(), 0),
                    ranked.indexOf(claimed) + 1, ranked.size(),
                    best.stageId().value(), perUnit(best, item),
                    sampleSizes.getOrDefault(best.displayName(), 0), 100 * gap);
        }
        System.out.println("agreed exactly: " + String.join(", ", exact));

        assertThat(exact)
                .as("Phase 2 closes on five goal sets where the answer is somebody else's")
                .hasSizeGreaterThanOrEqualTo(5);
    }

    @Test
    @DisplayName("where we prefer a stage the community does not, one of the two rests on a small sample")
    void everyDisagreementHasACause() {
        // The finding, and the reason Q8 is no longer a formality. A stage
        // measured over a hundred runs and one measured over forty thousand are
        // the same number to this model, and the hundred-run stage wins, because
        // a noisy mean is a high mean about as often as it is a low one.
        List<String> unexplained = new ArrayList<>();
        List<String> explained = new ArrayList<>();
        for (Answer answer : ANSWERS) {
            ItemId item = itemOf(answer);
            Stage claimed = stageOf(answer.stage());
            Stage best = rankedFor(item).getFirst();
            double gap = perUnit(claimed, item) / perUnit(best, item) - 1;
            if (gap <= 0.25) {
                continue;
            }
            int theirs = sampleSizes.getOrDefault(claimed.displayName(), 0);
            int ours = sampleSizes.getOrDefault(best.displayName(), 0);
            String line = "%s: %s (%d runs) over %s (%d runs), %.0f%%".formatted(
                    answer.material(), best.stageId().value(), ours, answer.stage(), theirs, 100 * gap);
            (Math.min(ours, theirs) < WELL_SAMPLED ? explained : unexplained).add(line);
        }

        System.out.println("disagreements over 25%: " + explained.size() + " on a small sample, "
                + unexplained.size() + " not");
        explained.forEach(line -> System.out.println("  " + line));
        assertThat(unexplained)
                .as("a large disagreement with well-sampled data on both sides is a defect"
                        + " in the model, and would need finding rather than noting")
                .isEmpty();
    }

    @Test
    @DisplayName("a plan is cheaper than following the community's advice material by material")
    void beatsTheAdviceItAgreesWith() {
        // The claim a player cares about, and the one the per-item baseline in
        // RealUpstreamPlanTest cannot make: that baseline is our own arithmetic
        // on both sides. Here the baseline is the published advice — farm each
        // material where the guide says, at the rate the guide quotes — and the
        // solver still has to come in under it. It should, and not because it is
        // clever: one run drops five materials and a per-material plan pays for
        // each of them separately.
        List<Goal> goals = fiveCharactersTo("insight-2");
        Plan plan = RealUpstream.optimizer(definition, Inventory.empty(RealUpstream.PROFILE),
                        Duration.ofSeconds(2))
                .solve(new SolveRequest(RealUpstream.PROFILE, definition.version(), goals,
                        Objective.LEAST_ENERGY, 240));

        int advised = 0;
        int covered = 0;
        Map<ItemId, Integer> owed = demandOf(goals);
        for (Map.Entry<ItemId, Integer> entry : owed.entrySet()) {
            Answer answer = answerFor(entry.getKey());
            if (answer == null) {
                continue;
            }
            covered++;
            Stage stage = stageOf(answer.stage());
            advised += (int) Math.ceil(entry.getValue() / answer.quotedRate()) * stage.energyCost();
        }

        assertThat(covered).as("the goal set has to overlap the benchmark for this to mean anything")
                .isGreaterThanOrEqualTo(3);
        System.out.printf("CommunityBenchmarkTest: the plan costs %d Activity;"
                        + " the guide's own advice costs %d for the %d benchmark materials in it%n",
                plan.totalEnergy(), advised, covered);
        assertThat(plan.totalEnergy())
                .as("a plan that covers every material must not cost more than farming"
                        + " a subset of them one at a time")
                .isLessThanOrEqualTo(advised);
    }

    // ── the goal set ────────────────────────────────────────────────────────

    /** The same shape of question as {@code RealUpstreamPlanTest}: five characters, Insight 2. */
    private List<Goal> fiveCharactersTo(String state) {
        List<EntityId> characters = definition.sinks().stream()
                .filter(Upgrade.class::isInstance).map(Upgrade.class::cast)
                .filter(upgrade -> upgrade.toState().equals(state))
                .map(Upgrade::entity)
                .distinct()
                .sorted(Comparator.comparing(EntityId::value))
                .toList();

        List<Goal> goals = new ArrayList<>();
        for (EntityId entity : characters) {
            Goal goal = Goal.deterministic(entity, state);
            try {
                RealUpstream.optimizer(definition, Inventory.empty(RealUpstream.PROFILE),
                        Duration.ofMillis(250)).solve(new SolveRequest(
                                RealUpstream.PROFILE, definition.version(), List.of(goal),
                                Objective.LEAST_ENERGY, 240));
                goals.add(goal);
            } catch (Optimizer.InfeasibleGoalException e) {
                // Not this one; it does not change what is being measured.
            }
            if (goals.size() == 5) break;
        }
        assertThat(goals).hasSize(5);
        return List.copyOf(goals);
    }

    private Map<ItemId, Integer> demandOf(List<Goal> goals) {
        Map<ItemId, Integer> owed = new HashMap<>();
        for (Goal goal : goals) {
            definition.sinks().stream()
                    .filter(Upgrade.class::isInstance).map(Upgrade.class::cast)
                    .filter(u -> u.entity().equals(goal.entity()))
                    .filter(u -> u.toState().equals("insight-1") || u.toState().equals("insight-2"))
                    .forEach(u -> {
                        for (ItemStack cost : u.costs()) {
                            owed.merge(cost.item(), cost.quantity(), Integer::sum);
                        }
                    });
        }
        return owed;
    }

    // ── the model's own answer to the question the guide answers ────────────

    private List<Stage> rankedFor(ItemId item) {
        List<Stage> ranked = new ArrayList<>(definition.stages().stream()
                .filter(stage -> yieldOf(stage, item) > 0)
                .toList());
        ranked.sort(Comparator.comparingDouble(stage -> perUnit(stage, item)));
        return ranked;
    }

    private static double yieldOf(Stage stage, ItemId item) {
        return stage.drops().stream()
                .filter(drop -> drop.item().equals(item))
                .mapToDouble(drop -> drop.expectedYield())
                .findFirst().orElse(0);
    }

    private static double perUnit(Stage stage, ItemId item) {
        double each = yieldOf(stage, item);
        return each <= 0 ? Double.POSITIVE_INFINITY : stage.energyCost() / each;
    }

    // ── lookups ─────────────────────────────────────────────────────────────

    private ItemId itemOf(Answer answer) {
        ItemId item = byName.get(answer.material());
        assertThat(item).as("the benchmark names %s, which this patch does not carry."
                + " If the upstream renamed it, rename it here and say so in the"
                + " benchmark document", answer.material()).isNotNull();
        return item;
    }

    private Answer answerFor(ItemId item) {
        return ANSWERS.stream()
                .filter(answer -> item.equals(byName.get(answer.material())))
                .findFirst().orElse(null);
    }

    private Stage stageOf(String id) {
        return definition.stages().stream()
                .filter(stage -> stage.stageId().equals(new StageId(id)))
                .findFirst().orElse(null);
    }

    private Map<String, ItemId> itemsByDisplayName() {
        Map<String, ItemId> names = new HashMap<>();
        for (Item item : definition.items()) {
            names.put(item.displayName(), item.id());
        }
        return names;
    }

    /**
     * How many runs each stage's drop counts were observed over, read from the
     * snapshot rather than from the model — because the model does not carry it,
     * which is the subject of Q8 and half of what this class is here to show.
     */
    private Map<String, Integer> sampleSizes() {
        Path directory = RealUpstream.snapshot().resolve(RealUpstream.PATCH);
        Map<String, Integer> counts = new HashMap<>();
        try (var files = Files.list(directory)) {
            Path sampled = files
                    .filter(file -> file.getFileName().toString().matches("stages\\d+_\\d+_greedy\\.json"))
                    .max(Comparator.comparing(file -> file.getFileName().toString()))
                    .orElse(null);
            if (sampled == null) {
                return Map.of();
            }
            JsonNode root = new ObjectMapper().readTree(sampled.toFile());
            for (Map.Entry<String, JsonNode> entry : root.properties()) {
                JsonNode count = entry.getValue().get("count");
                if (count != null) {
                    counts.put(entry.getKey(), count.asInt());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return counts;
    }

    @SuppressWarnings("unused") // named by @EnabledIf
    static boolean snapshotIsPresent() {
        return RealUpstream.snapshotIsPresent();
    }
}
