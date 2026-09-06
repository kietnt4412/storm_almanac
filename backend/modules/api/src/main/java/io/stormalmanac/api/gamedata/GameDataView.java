package io.stormalmanac.api.gamedata;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * The wire format for published game data — a read model, not the domain.
 *
 * <p>Serialising the domain records directly would almost work and would be a
 * mistake for three reasons, in increasing order of how much they cost later:
 *
 * <ol>
 *   <li>Typed identifiers are records, so {@code ItemId} would go out as
 *       {@code {"value":"gold"}}. The domain's rule that an id is never a bare
 *       string is a rule about Java, not about JSON.
 *   <li>A cost is an {@code ItemStack} — an id and a quantity. Somebody asking
 *       what Insight 2 costs wants "6 × Greater Sigil", and resolving the name
 *       is the read model's job precisely so that every client does not have to
 *       fetch the item table and join it by hand.
 *   <li>The domain is going to move. Sealed hierarchies get new members, records
 *       get fields, and phases 2 and 4 will reshape things. If the wire format
 *       is the domain then every one of those is a breaking API change nobody
 *       noticed making.
 * </ol>
 *
 * <p>Every response that carries numbers carries the {@link VersionView} they
 * came from, attribution included. "Numbers and text only, attributed" is a
 * project invariant, and the moment numbers are served to a stranger is the
 * moment the attribution has to travel with them.
 */
public final class GameDataView {

    private GameDataView() {}

    /**
     * Which snapshot answered this request.
     *
     * <p>On every payload rather than only on the version list, because a
     * catalog page that cannot say which patch it describes is the failure mode
     * the whole versioning model exists to prevent.
     */
    public record VersionView(long sequence, String label, Instant publishedAt, String attribution) {}

    /** Rarity keeps both halves: the label the game writes, and the ordering. */
    public record RarityView(String label, int rank) {}

    /** One line of a cost, with the item's name resolved so a client need not join. */
    public record CostView(String item, String displayName, int quantity) {}

    /** Enough of an entity to list it; the catalog index does not need its skills. */
    public record EntitySummaryView(
            String id, String displayName, String kind, RarityView rarity, String element, List<String> tags) {}

    public record BreakpointView(int ascensionTier, int level, double value) {}

    public record StatCurveView(String stat, List<BreakpointView> breakpoints) {}

    /**
     * One rank of a skill: the answer to "what does her S2 do at rank 3?".
     *
     * <p>{@code values} stays an open map because the multipliers a game names
     * are the game's business — an enum of stat names here would be the first
     * game-specific branch in the codebase.
     */
    public record RankView(int rank, String description, Map<String, Double> values, List<CostView> upgradeCost) {}

    public record SkillView(String id, String displayName, List<RankView> ranks) {}

    public record TalentView(String id, String displayName, String unlockCondition, String effect) {}

    /** One catalog page's worth of an entity. */
    public record EntityView(
            String id,
            String displayName,
            String kind,
            RarityView rarity,
            String element,
            List<String> tags,
            List<StatCurveView> statCurves,
            List<SkillView> skills,
            List<TalentView> talents) {}

    /** One deterministic step of an entity's upgrade graph, and what it costs. */
    public record UpgradeStepView(String id, String fromState, String toState, List<CostView> costs) {}

    /** One change in a patch diff, flattened the way {@code Change} already is. */
    public record ChangeView(String axis, String kind, String subject, String detail, String before, String after) {}

    // ── Responses ───────────────────────────────────────────────────────────

    public record VersionsResponse(String game, List<VersionView> versions) {}

    public record EntitiesResponse(String game, VersionView version, List<EntitySummaryView> entities) {}

    public record EntityResponse(String game, VersionView version, EntityView entity) {}

    /**
     * @param entity      the entity these steps belong to, so a client that
     *                    followed a link here still knows whose costs these are
     * @param totalCost   every step's cost summed per item. The question is
     *                    almost never "what does this one step cost" on its own
     *                    — it is "what do I still owe" — and summing it here
     *                    keeps one implementation of that instead of one per
     *                    client
     */
    public record UpgradesResponse(
            String game,
            VersionView version,
            EntitySummaryView entity,
            List<UpgradeStepView> steps,
            List<CostView> totalCost) {}

    public record DiffResponse(
            String game, VersionView from, VersionView to, List<ChangeView> changes) {}
}
