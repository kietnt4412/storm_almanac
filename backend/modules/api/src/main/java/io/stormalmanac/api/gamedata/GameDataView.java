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

    /**
     * One deterministic step of an entity's upgrade graph, and what it costs.
     *
     * <p>The four words are what the game calls the step's states and where its
     * track sits (ADR 0032). Each is null when the bundle gave none, which is
     * every step before sequence 11; a page falls back to the ids.
     */
    public record UpgradeStepView(
            String id,
            String fromState,
            String toState,
            List<CostView> costs,
            String fromName,
            String toName,
            String section,
            String tag) {}

    /** One change in a patch diff, flattened the way {@code Change} already is. */
    public record ChangeView(String axis, String kind, String subject, String detail, String before, String after) {}

    /**
     * One rung of a scored ladder: the bar, and what clearing it pays.
     *
     * <p>{@code grants} resolves the item names like every other cost on this
     * wire, and here that is load-bearing rather than tidy. The measure itself
     * is a slug nobody outside the bundle has ever seen, so the only thing on
     * this record that lets a reader recognise what they are being asked about
     * is what the rungs pay: "9 Phantom Pain Scars a week" is a question
     * somebody can answer, and "tier 9" is not.
     *
     * @param reward  the reward's own id, so a plan's note naming
     *                {@code phantom-pain-cage-1100000} can be found on this list
     *                rather than deciphered
     * @param cadence how often the rung is paid. Two ladders on one measure are
     *                a shape no published game has yet and the format permits,
     *                and without this they would be indistinguishable here
     */
    public record BarView(String reward, int atLeast, String cadence, List<CostView> grants) {}

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
            SourcingView sourcing,
            List<String> sections) {}

    public record DiffResponse(
            String game, VersionView from, VersionView to, List<ChangeView> changes) {}

    /**
     * One thing this version's grants are scored on, and every bar it pays at.
     *
     * <p><b>Why there is a route for this at all.</b> ADR 0022 made a scored
     * grant an answer the reader supplies — {@code reach} on the plan request —
     * and wrote the cost of that into its own consequences: a measure is
     * declared nowhere, so "a screen that wants to ask 'how far do you get in
     * the Phantom Pain Cage?' has to collect the measures off the rewards it can
     * see". This is that collection, done once on the server rather than once
     * per client, and no client can ask the question without it: the whole
     * difficulty is that there is no list of the questions.
     *
     * <p><b>The measure stays a slug, and that stays the trade.</b> It has no
     * declaration to hang a display name on, so {@code phantom-pain-cage-score}
     * is what a reader sees — the same trade an opaque state makes. The bars are
     * what makes it survivable rather than a fix for it.
     *
     * <p>Ordered by bar, lowest first, which is the order a ladder is climbed
     * and therefore the order a reader picks their own rung out of. An empty
     * list is a 200: a game whose grants all turn up for everybody has no
     * measures, which is an answer and not an absence.
     */
    public record MeasureView(String measure, List<BarView> bars) {}

    public record MeasuresResponse(String game, VersionView version, List<MeasureView> measures) {}
}
