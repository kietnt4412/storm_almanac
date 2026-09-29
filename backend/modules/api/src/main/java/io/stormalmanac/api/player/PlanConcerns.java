package io.stormalmanac.api.player;

import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.StageId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Progress;
import io.stormalmanac.gamedata.Upgrade;
import io.stormalmanac.gamedata.diff.Axis;
import io.stormalmanac.gamedata.diff.Change;
import io.stormalmanac.gamedata.diff.VersionDiff;
import io.stormalmanac.planner.Conversion;
import io.stormalmanac.planner.Plan;
import io.stormalmanac.planner.RewardClaim;
import io.stormalmanac.planner.StageRun;
import io.stormalmanac.planner.StepNames;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Which of a patch's changes concern one reader's plan (C3.2, ADR 0037).
 *
 * <p><b>Read off the plans, not off the goals.</b> A goal names an entity and a
 * target; the steps between where the reader stands and that target are what the
 * solver's demand resolution works out, and a second copy of that walk here would
 * be a second answer to disagree with the first. So a change concerns the reader
 * when it touches something one of their two plans — on the sequence they last
 * planned on, and on the latest — actually uses:
 *
 * <ul>
 *   <li>a stage it runs, or a shop row, craft, fodder rule or price it converts
 *       through, or a grant it claims;
 *   <li>an upgrade step it pays for, the items that step costs, and the pool of
 *       progress it fills;
 *   <li>the game itself, whose day boundary moves every rotating stage.
 * </ul>
 *
 * <p>Both plans, because a patch can take a stage away (only the older plan ran
 * it) or add a cheaper one (only the newer plan runs it), and a reader wants to
 * hear about both.
 *
 * <p><b>Progression only.</b> {@link Axis} says why: a planner does not care that
 * a skill multiplier moved, and a banner's rates are the pull planner's business.
 */
final class PlanConcerns {

    private final Set<String> subjects = new HashSet<>();

    private PlanConcerns() {}

    /**
     * @param plans       the reader's plans, any of which may be absent where a
     *                    solve was refused
     * @param definitions the versions they were solved against, to price the
     *                    steps they pay for
     */
    static PlanConcerns of(List<Plan> plans, List<GameDefinition> definitions) {
        PlanConcerns concerns = new PlanConcerns();
        for (GameDefinition definition : definitions) {
            concerns.add("game", definition.game().id().value());
        }
        for (Plan plan : plans) {
            for (StageRun run : plan.stageRuns()) {
                concerns.add("stage", run.stage().value());
            }
            for (Conversion conversion : plan.conversions()) {
                // A fodder rule taking several items is one conversion per item,
                // "<rule>:<item>"; the subject is the rule.
                String step = conversion.sourceOrSinkId().split(":", 2)[0];
                for (String about : List.of("shop", "craft", "fodder", "upgrade")) {
                    concerns.add(about, step);
                }
            }
            for (RewardClaim claim : plan.rewardClaims()) {
                concerns.add("reward", claim.reward());
            }
            for (String entry : plan.explanation().payingFor()) {
                for (GameDefinition definition : definitions) {
                    concerns.addStep(entry, definition);
                }
            }
        }
        return concerns;
    }

    /** An entry of {@code payingFor}: one step id, or a step's several prices joined by " or ". */
    private void addStep(String entry, GameDefinition definition) {
        for (String step : entry.split(" or ")) {
            add("upgrade", step);
            definition.sinks().stream()
                    .filter(sink -> sink instanceof Upgrade upgrade && upgrade.id().equals(step))
                    .map(Upgrade.class::cast)
                    .forEach(upgrade -> {
                        for (ItemStack cost : upgrade.costs()) add("item", cost.item().value());
                        for (Progress progress : upgrade.progress()) add("progress", progress.kind());
                    });
        }
    }

    private void add(String about, String slug) {
        subjects.add(Change.label(about, slug));
    }

    boolean concerns(Change change) {
        return change.axis() == Axis.PROGRESSION && subjects.contains(change.subject());
    }

    List<Change> in(VersionDiff diff) {
        return diff.changes().stream().filter(this::concerns).toList();
    }

    /**
     * What a reader calls the subject of a change, in the words the version it
     * still exists in publishes — the newer one, or the older for something the
     * patch took away. An unknown kind keeps its slug.
     */
    static String name(Change change, GameDefinition definition) {
        StepNames names = StepNames.of(definition);
        String slug = change.slug();
        return switch (change.about()) {
            case "game" -> definition.game().displayName();
            case "stage" -> names.stage(StageId.of(slug));
            case "shop", "craft", "fodder" -> names.step(slug);
            case "reward" -> names.reward(slug);
            case "upgrade" -> names.upgrade(slug);
            case "item" -> names.demand(ItemId.of(slug));
            case "progress" -> definition.nameOfProgress(slug);
            default -> slug;
        };
    }
}
