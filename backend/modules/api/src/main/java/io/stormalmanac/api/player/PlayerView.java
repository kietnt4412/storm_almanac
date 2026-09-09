package io.stormalmanac.api.player;

import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gamedata.Goal;
import io.stormalmanac.identity.Account;
import io.stormalmanac.player.MergeOutcome;
import io.stormalmanac.planner.Objective;
import io.stormalmanac.planner.Plan;
import io.stormalmanac.planner.SolveRequest;
import io.stormalmanac.player.Goals;
import io.stormalmanac.player.Inventory;
import io.stormalmanac.player.PlayerProfile;
import io.stormalmanac.player.Roster;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The wire shapes for a player's own data.
 *
 * <p>Domain records do not go on the wire directly, for the same reason
 * {@code GameDataView} exists: a typed identifier serialises as
 * {@code {"value":"vertin"}} and a record renamed for clarity inside the model
 * would be a breaking API change outside it. Every identifier below is the plain
 * slug a caller already uses in a URL.
 *
 * <p>Maps rather than lists of pairs, for inventory and roster both. An
 * inventory is a few hundred entries that a client patches by key, and
 * {@code {"sharpened-tool": 42}} is what a client wants to hold; a list of
 * objects would make it build the map itself on every read.
 */
public final class PlayerView {

    private PlayerView() {}

    // ── Account and profiles ────────────────────────────────────────────────

    public record MeResponse(String accountId, String displayName, String email, List<ProfileResponse> profiles) {

        public static MeResponse of(Account account, List<PlayerProfile> profiles) {
            return new MeResponse(
                    account.id().value(),
                    account.displayName(),
                    account.email(),
                    profiles.stream().map(ProfileResponse::of).toList());
        }
    }

    public record ProfileResponse(String id, String game, String region, String displayName) {

        public static ProfileResponse of(PlayerProfile profile) {
            return new ProfileResponse(
                    profile.id().value(), profile.game().value(), profile.region(), profile.displayName());
        }
    }

    /**
     * @param region which server. Not optional and not defaulted: the two
     *               regions of one game run different patches, and a profile
     *               that does not say which is a profile whose plan is computed
     *               against the wrong one
     */
    public record CreateProfileRequest(String game, String region, String displayName) {}

    // ── What the profile owns ───────────────────────────────────────────────

    public record InventoryResponse(String profile, Map<String, Integer> items) {

        public static InventoryResponse of(Inventory inventory) {
            Map<String, Integer> items = new LinkedHashMap<>();
            inventory.quantities().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(id -> id.value())))
                    .forEach(e -> items.put(e.getKey().value(), e.getValue()));
            return new InventoryResponse(inventory.profile().value(), items);
        }
    }

    public record InventoryRequest(Map<String, Integer> items) {}

    /**
     * A patch: only the keys a device actually changed, each carrying when it
     * changed.
     *
     * <p><b>The timestamp is per key and not per request, and that is the whole
     * design.</b> A device that spent an hour offline did not make its edits at
     * one instant — it changed one item at 09:00 and another at 11:00 — and a
     * single batch stamp would have to lie about one of them. The lie is not
     * cosmetic: it decides which device wins for that key, so a batch stamp
     * would make some merges come out wrong, silently, in a way no test written
     * against a single clock would show. Three hundred copies of the same
     * timestamp on a bulk upload is the price, and it is cheap.
     */
    public record InventoryPatchRequest(Map<String, InventoryEditView> items) {}

    /**
     * @param quantity how many the player now has. Required; zero means "none
     *                 left" and removes the key, which is a normal edit rather
     *                 than an error
     * @param at       when the client made this edit, in its own clock. Required
     *                 — a client that cannot say when has no business on this
     *                 route and should PUT the whole aggregate instead. It is
     *                 clamped server-side, so it can be old but never future
     */
    public record InventoryEditView(Integer quantity, Instant at) {}

    /**
     * @param items    the merged inventory as the server now holds it, so a
     *                 client never has to guess what the merge decided
     * @param rejected the keys whose edits lost for being older than what was
     *                 stored. A client that ignores this list keeps showing
     *                 values the server does not have
     */
    public record InventoryPatchResponse(
            String profile, Map<String, Integer> items, List<String> applied, List<String> rejected) {

        public static InventoryPatchResponse of(Inventory merged, MergeOutcome<ItemId> outcome) {
            return new InventoryPatchResponse(
                    merged.profile().value(),
                    InventoryResponse.of(merged).items(),
                    outcome.applied().stream().map(ItemId::value).sorted().toList(),
                    outcome.rejected().stream().map(ItemId::value).sorted().toList());
        }
    }

    public record RosterResponse(String profile, Map<String, String> entities) {

        public static RosterResponse of(Roster roster) {
            Map<String, String> entities = new LinkedHashMap<>();
            roster.currentState().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(id -> id.value())))
                    .forEach(e -> entities.put(e.getKey().value(), e.getValue()));
            return new RosterResponse(roster.profile().value(), entities);
        }
    }

    public record RosterRequest(Map<String, String> entities) {}

    /** A roster patch, on the same terms as {@link InventoryPatchRequest}. */
    public record RosterPatchRequest(Map<String, RosterEditView> entities) {}

    /**
     * @param state the entity's current upgrade state, or {@code null} to say it
     *              is no longer on the roster. An omitted {@code state} member
     *              reads as null and therefore as a removal — JSON cannot tell
     *              absent from null, and inventing a third spelling for it would
     *              be a worse trade than saying so here
     */
    public record RosterEditView(String state, Instant at) {}

    public record RosterPatchResponse(
            String profile, Map<String, String> entities, List<String> applied, List<String> rejected) {

        public static RosterPatchResponse of(Roster merged, MergeOutcome<EntityId> outcome) {
            return new RosterPatchResponse(
                    merged.profile().value(),
                    RosterResponse.of(merged).entities(),
                    outcome.applied().stream().map(EntityId::value).sorted().toList(),
                    outcome.rejected().stream().map(EntityId::value).sorted().toList());
        }
    }

    public record GoalsResponse(String profile, List<GoalView> goals) {

        public static GoalsResponse of(Goals goals) {
            return new GoalsResponse(
                    goals.profile().value(), goals.goals().stream().map(GoalView::of).toList());
        }
    }

    public record GoalsRequest(List<GoalView> goals) {}

    /**
     * @param satisfiability {@code DETERMINISTIC} or {@code PROBABILISTIC}, and
     *                       absent means deterministic — the overwhelmingly
     *                       common case, and the one a caller who has not heard
     *                       of the distinction means
     */
    public record GoalView(String entity, String targetState, String satisfiability, Integer priority) {

        public static GoalView of(Goal goal) {
            return new GoalView(
                    goal.entity().value(), goal.targetState(), goal.satisfiability().name(), goal.priority());
        }
    }

    // ── The plan ────────────────────────────────────────────────────────────

    /**
     * @param objective    {@code LEAST_ENERGY} or {@code FEWEST_DAYS}; absent
     *                     means least energy
     * @param energyPerDay what the player actually regenerates plus any refills
     *                     they mean to spend. Required, because there is no
     *                     honest default: it is a fact about their account
     * @param horizonDays  how long they are prepared to spend. Absent means
     *                     {@link SolveRequest#DEFAULT_HORIZON_DAYS}, and the
     *                     default is a real choice — without a horizon the
     *                     cheapest plan is always "wait"
     */
    public record PlanRequest(String objective, Integer energyPerDay, Integer horizonDays) {

        public Objective resolvedObjective() {
            return objective == null || objective.isBlank()
                    ? Objective.LEAST_ENERGY
                    : Objective.valueOf(objective.trim().toUpperCase());
        }

        public int resolvedHorizonDays() {
            return horizonDays == null ? SolveRequest.DEFAULT_HORIZON_DAYS : horizonDays;
        }
    }

    /**
     * @param computedAgainst the game-data version this plan was solved against,
     *                        carried out to the caller rather than left in the
     *                        server's logs. A plan that cannot say which patch it
     *                        describes is one nobody can check after the next one
     * @param notes           what the solver wants the reader to know, including
     *                        whether the search finished or ran out of budget and
     *                        by how much
     */
    public record PlanResponse(
            String id,
            String profile,
            String game,
            long version,
            String versionLabel,
            String attribution,
            String objective,
            List<StageRunView> stages,
            List<ConversionView> conversions,
            List<RewardClaimView> rewards,
            int totalEnergy,
            double etaDays,
            Map<String, Double> shadowPrice,
            List<String> bindingStages,
            List<String> notes,
            Instant computedAt) {

        public static PlanResponse of(Plan plan) {
            Map<String, Double> prices = new LinkedHashMap<>();
            plan.explanation().shadowPrice().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(id -> id.value())))
                    .forEach(e -> prices.put(e.getKey().value(), e.getValue()));

            return new PlanResponse(
                    plan.id().value(),
                    plan.profile().value(),
                    plan.computedAgainst().game().value(),
                    plan.computedAgainst().sequence(),
                    plan.computedAgainst().label(),
                    plan.computedAgainst().attribution(),
                    plan.objective().name(),
                    plan.stageRuns().stream()
                            .map(run -> new StageRunView(
                                    run.stage().value(), run.runs(), run.energyCost(), run.totalEnergy()))
                            .toList(),
                    plan.conversions().stream()
                            .map(c -> new ConversionView(c.sourceOrSinkId(), c.times()))
                            .toList(),
                    plan.rewardClaims().stream()
                            .map(c -> new RewardClaimView(c.reward(), c.times()))
                            .toList(),
                    plan.totalEnergy(),
                    plan.etaDays(),
                    prices,
                    plan.explanation().bindingStages().stream()
                            .map(id -> id.value())
                            .toList(),
                    plan.explanation().notes(),
                    plan.computedAt());
        }
    }

    public record StageRunView(String stage, int runs, int energyCost, int totalEnergy) {}

    public record ConversionView(String step, int times) {}

    public record RewardClaimView(String reward, int times) {}
}
