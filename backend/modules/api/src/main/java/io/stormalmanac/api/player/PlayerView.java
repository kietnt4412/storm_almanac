package io.stormalmanac.api.player;

import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.Goal;
import io.stormalmanac.identity.Account;
import io.stormalmanac.player.MergeOutcome;
import io.stormalmanac.planner.Objective;
import io.stormalmanac.planner.Plan;
import io.stormalmanac.planner.SolveRequest;
import io.stormalmanac.planner.StepNames;
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

    /**
     * <p><b>A list of states per entity, not a state.</b> An entity stands on
     * several tracks at once — a level, a rank, an evolution, a skill each —
     * and the game ties none of them to each other. A list rather than a set
     * because JSON has no set; it is sorted here so a client diffing two reads
     * sees a change only when one happened.
     */
    public record RosterResponse(String profile, Map<String, List<String>> entities) {

        public static RosterResponse of(Roster roster) {
            Map<String, List<String>> entities = new LinkedHashMap<>();
            roster.currentStates().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(id -> id.value())))
                    .forEach(e -> entities.put(
                            e.getKey().value(), e.getValue().stream().sorted().toList()));
            return new RosterResponse(roster.profile().value(), entities);
        }
    }

    public record RosterRequest(Map<String, List<String>> entities) {}

    /** A roster patch, on the same terms as {@link InventoryPatchRequest}. */
    public record RosterPatchRequest(Map<String, RosterEditView> entities) {}

    /**
     * @param states the states the entity has reached, in full, or {@code null}
     *               to say it is no longer on the roster. An omitted
     *               {@code states} member reads as null and therefore as a
     *               removal — JSON cannot tell absent from null, and inventing a
     *               third spelling for it would be a worse trade than saying so
     *               here. <b>The list is the whole set, not an addition to it:</b>
     *               the entity is the merge unit, so a client that means to add
     *               one state sends the states it now holds, and a state left
     *               out is a state given up
     */
    public record RosterEditView(List<String> states, Instant at) {}

    public record RosterPatchResponse(
            String profile, Map<String, List<String>> entities, List<String> applied, List<String> rejected) {

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
     * @param reach        what this account can reach in the modes that pay by
     *                     score, by measure: {@code {"phantom-pain-cage-score":
     *                     500000}}. Absent means none of them are counted, which
     *                     is the honest default rather than the generous one —
     *                     see ADR 0022 — and the plan's notes name every grant
     *                     it left out
     */
    public record PlanRequest(
            String objective,
            Integer energyPerDay,
            Integer horizonDays,
            Map<String, Integer> reach) {

        public Objective resolvedObjective() {
            return objective == null || objective.isBlank()
                    ? Objective.LEAST_ENERGY
                    : Objective.valueOf(objective.trim().toUpperCase());
        }

        public int resolvedHorizonDays() {
            return horizonDays == null ? SolveRequest.DEFAULT_HORIZON_DAYS : horizonDays;
        }

        public Map<String, Integer> resolvedReach() {
            return reach == null ? Map.of() : reach;
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
            List<ShadowPriceView> shadowPrice,
            List<String> bindingStages,
            List<String> notes,
            List<PayingForView> payingFor,
            Instant computedAt) {

        public static PlanResponse of(Plan plan, GameDefinition definition) {
            StepNames names = StepNames.of(definition);
            List<ShadowPriceView> prices = plan.explanation().shadowPrice().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.comparing(ItemId::value)))
                    .map(priced -> new ShadowPriceView(
                            priced.getKey().value(),
                            names.demand(priced.getKey()),
                            priced.getValue()))
                    .toList();

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
                                    run.stage().value(), names.stage(run.stage()),
                                    run.runs(), run.energyCost(), run.totalEnergy()))
                            .toList(),
                    // Read order, not id order: buy, open, feed, pay; a ladder's
                    // tiers bottom rung up. Still one order per plan, so two
                    // reads of it stay byte-identical.
                    plan.conversions().stream()
                            .sorted(Comparator.comparing(c -> c.sourceOrSinkId(), names.stepOrder()))
                            .map(c -> new ConversionView(c.sourceOrSinkId(), names.step(c.sourceOrSinkId()), c.times()))
                            .toList(),
                    plan.rewardClaims().stream()
                            .sorted(Comparator.comparing(c -> c.reward(), names.rewardOrder()))
                            .map(c -> new RewardClaimView(c.reward(), names.reward(c.reward()), c.times()))
                            .toList(),
                    plan.totalEnergy(),
                    plan.etaDays(),
                    prices,
                    plan.explanation().bindingStages().stream()
                            .map(id -> id.value())
                            .toList(),
                    plan.explanation().notes(),
                    plan.explanation().payingFor().stream()
                            .map(entry -> PayingForView.of(entry, names))
                            .toList(),
                    plan.computedAt());
        }
    }

    /**
     * One upgrade step the goals pay for, as the entity and the two states it
     * joins rather than as a sentence.
     *
     * <p>The page names a state the way the goal and roster screens already
     * do — the game's word where the bundle has one, a guess off the track's
     * ids where it has none, "Red Orb · 18" — and it cannot do that to a
     * sentence. The server does not guess ({@link StepNames}), so
     * {@code displayName} is its unguessed form, "Lucia: Inverse Crown to
     * abyssal-lament-18", for a client that names nothing itself.
     *
     * <p>The states are null only for an entry this version has no step for,
     * which a plan solved against it cannot produce; {@code step} then carries it.
     */
    public record PayingForView(
            String step,
            String entity,
            String entityName,
            String fromState,
            String toState,
            String displayName) {

        static PayingForView of(String entry, StepNames names) {
            return names.upgradeOf(entry)
                    .map(step -> new PayingForView(
                            entry,
                            step.entity().value(),
                            names.entity(step.entity()),
                            step.fromState(),
                            step.toState(),
                            names.upgrade(entry)))
                    .orElseGet(() -> new PayingForView(entry, null, null, null, null, entry));
        }
    }

    /**
     * What one more of an item would cost, and what to call it.
     *
     * <p>A list of three-field rows rather than the map of {@code id -> price}
     * this used to be, for the same reason {@code ShortfallLine} carries a name:
     * a demand line can stand for something that is not a catalog item — EXP,
     * or one step offered at several prices — and those have no entry in the
     * item table to look a name up in. The map put {@code progress:character-exp}
     * on the page beside a properly named {@code Cogs}. See N37 in TRACKER.md.
     *
     * <p>{@code item} is kept beside {@code displayName} rather than replaced by
     * it, because it is the key a reader would quote in a bug report and the one
     * a client can match against a shortfall line.
     */
    public record ShadowPriceView(String item, String displayName, double price) {}

    /**
     * A plan's lines carry a name beside their id, for the reason
     * {@link ShadowPriceView} does: the id is what a bug report quotes and what
     * {@code bindingStages} is matched against, and the name is what a reader
     * does. Until 2026-09-26 these three sent the id alone, and the page printed
     * {@code simulation-shop-weapon-enhancer-iv}. The words are
     * {@link StepNames}'s, built only from facts already published.
     */
    public record StageRunView(String stage, String displayName, int runs, int energyCost, int totalEnergy) {}

    public record ConversionView(String step, String displayName, int times) {}

    public record RewardClaimView(String reward, String displayName, int times) {}
}
