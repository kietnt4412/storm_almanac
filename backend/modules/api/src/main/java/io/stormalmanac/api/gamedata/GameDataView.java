package io.stormalmanac.api.gamedata;

import java.time.Instant;
import java.time.LocalDate;
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

    /**
     * One sourcing record: how a reader could go and check a number themselves.
     *
     * <p><b>{@code firstHand} is on the wire on purpose.</b> It is derivable from
     * {@code origin}, and a client deriving it would be a second copy of
     * {@code Provenance.Origin.isFirstHand()} — in TypeScript, where nothing can
     * be made to fail to compile when the Java one gains a member. The policy is
     * answered once, in the place that defines it, and travels as an answer.
     *
     * <p>{@code origin} travels beside it rather than being collapsed into the
     * boolean, because "somebody counted 300 runs" and "the publisher's rules
     * screen says so" are both ours and are not equally strong, and a reader
     * deciding whether to trust a number wants the difference.
     */
    public record ProvenanceView(
            String id, String origin, boolean firstHand, String detail, LocalDate observedOn) {}

    /**
     * Where this response's numbers were read — ADR 0016, served.
     *
     * <p>The records once and the references beside the facts, because the
     * realistic bundle is one sitting, one screen, one reader: a page's hundred
     * facts point at a handful of records, and repeating each record per fact
     * would be mostly duplication on a payload a phone fetches.
     *
     * @param sources every distinct record behind this response, in the order
     *                first met
     * @param facts   {@code kind:slug} to the {@code id} of the record it was
     *                read under. A fact missing from this map is one nobody
     *                sourced: it reads as {@code UNRECORDED}, which is the
     *                defined meaning of silence rather than an omission
     */
    public record SourcingView(List<ProvenanceView> sources, Map<String, String> facts) {}

    /** Rarity keeps both halves: the label the game writes, and the ordering. */
    public record RarityView(String label, int rank) {}

    /** One line of a cost, with the item's name resolved so a client need not join. */
    public record CostView(String item, String displayName, int quantity) {}

    /**
     * One item of the catalog, which is the vocabulary an inventory is written in.
     *
     * <p>Rarity and category travel with it because that is how an inventory is
     * read rather than how it is stored: a player entering a few hundred
     * quantities goes down a list ordered the way the game's own bag is, and a
     * client that had only slugs would have to invent an order of its own.
     */
    public record ItemView(String id, String displayName, RarityView rarity, String category) {}

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

    /**
     * One game on the index, with the version a reader would land on.
     *
     * <p>{@code energyUnit} travels because it is the word the product uses to
     * talk to a player — Activity, Serum, Vigour — and a screen that asks "how
     * much energy a day?" in a game's own noun is the difference between a tool
     * that knows the game and a form.
     */
    public record GameSummaryView(String id, String displayName, String energyUnit, VersionView latest) {}

    public record GamesResponse(List<GameSummaryView> games) {}

    public record VersionsResponse(String game, List<VersionView> versions) {}

    public record EntitiesResponse(
            String game, VersionView version, List<EntitySummaryView> entities, SourcingView sourcing) {}

    public record ItemsResponse(
            String game, VersionView version, List<ItemView> items, SourcingView sourcing) {}

    public record EntityResponse(
            String game, VersionView version, EntityView entity, SourcingView sourcing) {}

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
            List<CostView> totalCost,
            SourcingView sourcing) {}

    public record DiffResponse(
            String game, VersionView from, VersionView to, List<ChangeView> changes) {}
}
