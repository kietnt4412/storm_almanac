package io.stormalmanac.api.gamedata;

import io.stormalmanac.api.ResourceNotFoundException;
import io.stormalmanac.api.gamedata.GameDataView.BreakpointView;
import io.stormalmanac.api.gamedata.GameDataView.ChangeView;
import io.stormalmanac.api.gamedata.GameDataView.CostView;
import io.stormalmanac.api.gamedata.GameDataView.DiffResponse;
import io.stormalmanac.api.gamedata.GameDataView.EntitiesResponse;
import io.stormalmanac.api.gamedata.GameDataView.EntityResponse;
import io.stormalmanac.api.gamedata.GameDataView.EntitySummaryView;
import io.stormalmanac.api.gamedata.GameDataView.EntityView;
import io.stormalmanac.api.gamedata.GameDataView.GameSummaryView;
import io.stormalmanac.api.gamedata.GameDataView.GamesResponse;
import io.stormalmanac.api.gamedata.GameDataView.BarView;
import io.stormalmanac.api.gamedata.GameDataView.ItemView;
import io.stormalmanac.api.gamedata.GameDataView.ItemsResponse;
import io.stormalmanac.api.gamedata.GameDataView.MeasureView;
import io.stormalmanac.api.gamedata.GameDataView.MeasuresResponse;
import io.stormalmanac.api.gamedata.GameDataView.ProvenanceView;
import io.stormalmanac.api.gamedata.GameDataView.RankView;
import io.stormalmanac.api.gamedata.GameDataView.RarityView;
import io.stormalmanac.api.gamedata.GameDataView.SkillView;
import io.stormalmanac.api.gamedata.GameDataView.SourcingView;
import io.stormalmanac.api.gamedata.GameDataView.StatCurveView;
import io.stormalmanac.api.gamedata.GameDataView.TalentView;
import io.stormalmanac.api.gamedata.GameDataView.UpgradeStepView;
import io.stormalmanac.api.gamedata.GameDataView.UpgradesResponse;
import io.stormalmanac.api.gamedata.GameDataView.VersionView;
import io.stormalmanac.api.gamedata.GameDataView.VersionsResponse;
import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gamedata.FactRef;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.gamedata.Item;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.ProvenanceRepository;
import io.stormalmanac.gamedata.ProvenanceRepository.Sourcing;
import io.stormalmanac.gamedata.Rarity;
import io.stormalmanac.gamedata.Reward;
import io.stormalmanac.gamedata.Upgrade;
import io.stormalmanac.gamedata.catalog.Entity;
import io.stormalmanac.gamedata.catalog.Skill;
import io.stormalmanac.gamedata.catalog.StatCurve;
import io.stormalmanac.gamedata.catalog.Talent;
import io.stormalmanac.gamedata.diff.VersionDiff;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Published game data, shaped for a reader.
 *
 * <p>This is the half of phase 1's exit criterion that the repository cannot
 * meet on its own. {@code GameDataIngestTest} asks the two questions — what
 * does Insight 2 cost, what does her S2 do at rank 3 — of the schema. The
 * criterion says the <em>API</em> answers them, and an answer nobody can
 * request over HTTP is not an answer.
 *
 * <p>Every read resolves a version first and then answers from that one
 * snapshot. Nothing here reads "the latest" halfway through: a catalog page
 * whose skills came from 1.1 and whose costs came from 1.0 would be wrong in a
 * way no single query could reveal, and a version is a full snapshot precisely
 * so that reading one is enough.
 *
 * <p><b>Known cost, deliberately not paid yet:</b> every request loads a whole
 * version — fifteen queries, a few thousand rows — to answer a question about
 * one entity. That is fine at this phase's traffic and it will not be fine at
 * phase 4's. The fix is a cache keyed on {@code (game, sequence)}, correct by
 * construction because a published version is immutable; it is not written now
 * because a cache added before there is a number to compare against is a guess,
 * and this shape is what makes the number easy to take.
 */
@Service
public class GameDataReadModel {

    private final GameDefinitionRepository definitions;
    private final ProvenanceRepository provenance;

    public GameDataReadModel(GameDefinitionRepository definitions, ProvenanceRepository provenance) {
        this.definitions = definitions;
        this.provenance = provenance;
    }

    /**
     * Every game this installation has published something for.
     *
     * <p>The one route here that is not about a game, and the only way into the
     * catalog for a reader who does not already know a slug. An empty list is a
     * 200: "this installation has published nothing yet" is a true answer about
     * something that exists, unlike a request for a game nobody has heard of.
     */
    public GamesResponse games() {
        return new GamesResponse(definitions.publishedGames().stream()
                .map(published -> new GameSummaryView(
                        published.game().id().value(),
                        published.game().displayName(),
                        published.game().energyUnit(),
                        version(published.version())))
                .toList());
    }

    /**
     * Every published version of a game, newest first.
     *
     * <p>A game with nothing published is a 404 rather than an empty list. The
     * repository cannot tell "no such game" from "no approved snapshot yet" —
     * both are "nothing published" — and answering 200 with an empty list would
     * assert that a game nobody has ever heard of exists.
     */
    public VersionsResponse versions(GameId game) {
        List<GameDataVersion> versions = definitions.versions(game);
        if (versions.isEmpty()) {
            throw new ResourceNotFoundException("nothing published for " + game.value());
        }
        return new VersionsResponse(game.value(), versions.stream().map(GameDataReadModel::version).toList());
    }

    /** The catalog index: every entity in one version, without its combat data. */
    public EntitiesResponse entities(GameId game, Long sequence) {
        GameDefinition data = load(game, sequence);
        return new EntitiesResponse(
                game.value(),
                version(data.version()),
                data.entities().stream().map(GameDataReadModel::summary).toList(),
                sourcing(data, data.entities().stream().map(FactRef::of).toList()));
    }

    /**
     * Every item in one version: the vocabulary an inventory is written in.
     *
     * <p>Added with phase 4's inventory editor and not before it, which is the
     * honest order — until something had to render a few hundred quantities,
     * items appeared on the wire only as the resolved names inside a cost, and a
     * route returning a list nobody listed would have been a guess about what a
     * client wanted. A bulk editor is the client that wants it: it needs the
     * whole list, sorted, before the player has typed anything.
     */
    public ItemsResponse items(GameId game, Long sequence) {
        GameDefinition data = load(game, sequence);
        List<Item> sorted = data.items().stream()
                // Rarest first, then by name. An inventory screen is read
                // top-down and the expensive materials are the ones a player is
                // actually counting.
                .sorted(Comparator.comparingInt((Item item) -> item.rarity().rank())
                        .reversed()
                        .thenComparing(Item::displayName))
                .toList();

        return new ItemsResponse(
                game.value(),
                version(data.version()),
                sorted.stream()
                        .map(item -> new ItemView(
                                item.id().value(),
                                item.displayName(),
                                rarity(item.rarity()),
                                item.category()))
                        .toList(),
                // In the order the list is rendered, not the order the bundle
                // declared them, so a reader scanning the two together is
                // scanning one order.
                sourcing(data, sorted.stream().map(FactRef::of).toList()));
    }

    /** One catalog page: stat curves, skills and their ranks, talents. */
    public EntityResponse entity(GameId game, Long sequence, EntityId entity) {
        GameDefinition data = load(game, sequence);
        Entity found = require(data, entity);
        Map<ItemId, Item> items = data.itemsById();

        return new EntityResponse(game.value(), version(data.version()), new EntityView(
                found.id().value(),
                found.displayName(),
                found.kind(),
                rarity(found.rarity()),
                found.element(),
                found.tags(),
                found.statCurves().stream().map(GameDataReadModel::curve).toList(),
                found.skills().stream().map(skill -> skill(skill, items)).toList(),
                found.talents().stream().map(GameDataReadModel::talent).toList()),
                // One fact. Everything on this page — the curve, every rank of
                // every skill, the talents — is the entity record, read in one
                // sitting off one set of screens. The items named inside a skill's
                // upgrade cost are not this page's claim: their quantities belong
                // to the entity and their display names are the item's own fact,
                // sourced on the route that serves items.
                sourcing(data, List.of(FactRef.of(found))));
    }

    /** What it costs to advance one entity: the upgrade graph with names resolved. */
    public UpgradesResponse upgrades(GameId game, Long sequence, EntityId entity) {
        GameDefinition data = load(game, sequence);
        Entity found = require(data, entity);
        Map<ItemId, Item> items = data.itemsById();

        List<Upgrade> upgrades = data.sinks().stream()
                .filter(Upgrade.class::isInstance).map(Upgrade.class::cast)
                .filter(upgrade -> upgrade.entity().equals(entity))
                .toList();

        List<UpgradeStepView> steps = upgrades.stream()
                .map(upgrade -> new UpgradeStepView(
                        upgrade.id(), upgrade.fromState(), upgrade.toState(), costs(upgrade.costs(), items)))
                .toList();

        // The entity, because the response carries its summary, and then every
        // step whose cost is on the page. These genuinely can differ: an
        // upgrade table read off a levelling screen is not the same reading as
        // the character's own page, and per-fact provenance exists so that the
        // two do not have to be claimed together.
        List<String> facts = new ArrayList<>();
        facts.add(FactRef.of(found));
        upgrades.forEach(upgrade -> facts.add(FactRef.of(upgrade)));

        return new UpgradesResponse(
                game.value(),
                version(data.version()),
                summary(found),
                steps,
                total(upgrades, items),
                sourcing(data, facts));
    }

    /**
     * Every measure this version scores a grant on, with the bars it pays at.
     *
     * <p>Collected off the rewards rather than read from a declaration, because
     * there is no declaration: ADR 0022 made a measure an opaque label a reward
     * carries and said in as many words that the first screen to ask the reader
     * about one would have to gather them this way. Gathering them here rather
     * than in the browser is the same argument the shortfall route makes — a
     * traversal implemented twice is implemented once too often, and the copy in
     * the client is the one no test runs.
     *
     * <p>Grants are carried and are not decoration. A reader being asked how far
     * they get in {@code phantom-pain-cage-score} has never seen that string;
     * what they recognise is the nine Scars at the top of it.
     *
     * <p>Nothing here is filtered by {@link io.stormalmanac.gamedata.Availability}.
     * A closed event's ladder is still a ladder, and a reward the plan would
     * drop for being out of its window is the planner's judgement to make
     * against a horizon this route does not have.
     */
    public MeasuresResponse measures(GameId game, Long sequence) {
        GameDefinition data = load(game, sequence);
        Map<ItemId, Item> items = data.itemsById();

        // Insertion-ordered on both axes: the measures in the order the bundle
        // first mentions them, and inside one, sorted by the bar. A ladder read
        // in rung order is the order a reader picks their own rung out of, and
        // a bundle is under no obligation to have written the rungs in it.
        Map<String, List<BarView>> ladders = new LinkedHashMap<>();
        for (Reward reward : data.rewards()) {
            Reward.Requirement bar = reward.requires();
            if (bar == null) continue;
            ladders.computeIfAbsent(bar.measure(), measure -> new ArrayList<>())
                    .add(new BarView(
                            reward.id(),
                            bar.atLeast(),
                            reward.cadence().name(),
                            costs(reward.grants(), items)));
        }
        ladders.values().forEach(bars -> bars.sort(Comparator.comparingInt(BarView::atLeast)));

        return new MeasuresResponse(
                game.value(),
                version(data.version()),
                ladders.entrySet().stream()
                        .map(ladder -> new MeasureView(ladder.getKey(), List.copyOf(ladder.getValue())))
                        .toList());
    }

    /**
     * What changed between two published versions.
     *
     * <p>Returns the diff itself rather than a response record so that the
     * controller can render it as JSON or as the same plain text the CLI
     * prints, from one computation.
     */
    public VersionDiff diff(GameId game, long from, long to) {
        return VersionDiff.between(load(game, from), load(game, to));
    }

    /** The diff as JSON. Static because it reads nothing but the diff it is given. */
    public static DiffResponse asResponse(VersionDiff diff) {
        return new DiffResponse(
                diff.from().game().value(),
                version(diff.from()),
                version(diff.to()),
                diff.changes().stream()
                        .map(change -> new ChangeView(
                                change.axis().name(),
                                change.kind().name(),
                                change.subject(),
                                change.detail(),
                                change.before(),
                                change.after()))
                        .toList());
    }

    // ── Resolution ──────────────────────────────────────────────────────────

    /** @param sequence null for the latest approved snapshot */
    private GameDefinition load(GameId game, Long sequence) {
        if (sequence == null) {
            return definitions.findLatest(game).orElseThrow(() ->
                    new ResourceNotFoundException("nothing published for " + game.value()));
        }
        return definitions.find(game, sequence).orElseThrow(() ->
                new ResourceNotFoundException("no published version " + sequence + " of " + game.value()));
    }

    private static Entity require(GameDefinition data, EntityId entity) {
        Entity found = data.entitiesById().get(entity);
        if (found == null) {
            throw new ResourceNotFoundException(
                    "no entity '" + entity.value() + "' in " + data.game().id().value()
                            + " " + data.version().label());
        }
        return found;
    }

    // ── Sourcing ────────────────────────────────────────────────────────────

    /**
     * Where the facts this response answered with were read.
     *
     * <p>Asked of the version that actually answered rather than of the sequence
     * the request named, which is the same rule every other read here follows: a
     * page whose numbers came from the latest snapshot and whose sourcing came
     * from one the caller happened to mention would be the one kind of wrong
     * answer this whole feature exists to prevent.
     *
     * <p>A fact with no row is absent from {@code facts} rather than present with
     * a fabricated {@code unrecorded} record. Absence is what the client renders
     * as "nobody said", and inventing an id for it would put a record in
     * {@code sources} that nobody authored.
     */
    private SourcingView sourcing(GameDefinition data, List<String> factRefs) {
        Sourcing found = provenance.of(data.game().id(), data.version().sequence(), factRefs);

        Map<String, String> facts = new LinkedHashMap<>();
        found.byFact().forEach((ref, record) -> facts.put(ref, record.id()));

        return new SourcingView(
                found.distinct().stream()
                        .map(record -> new ProvenanceView(
                                record.id(),
                                record.origin().name(),
                                record.isFirstHand(),
                                record.detail(),
                                record.observedOn()))
                        .toList(),
                facts);
    }

    // ── Mapping ─────────────────────────────────────────────────────────────

    private static VersionView version(GameDataVersion version) {
        return new VersionView(version.sequence(), version.label(), version.publishedAt(), version.attribution());
    }

    private static EntitySummaryView summary(Entity entity) {
        return new EntitySummaryView(
                entity.id().value(), entity.displayName(), entity.kind(),
                rarity(entity.rarity()), entity.element(), entity.tags());
    }

    private static RarityView rarity(Rarity rarity) {
        return new RarityView(rarity.label(), rarity.rank());
    }

    private static StatCurveView curve(StatCurve curve) {
        return new StatCurveView(curve.stat(), curve.breakpoints().stream()
                .map(point -> new BreakpointView(point.ascensionTier(), point.level(), point.value()))
                .toList());
    }

    private static SkillView skill(Skill skill, Map<ItemId, Item> items) {
        return new SkillView(skill.id(), skill.displayName(), skill.ranks().stream()
                .map(rank -> new RankView(
                        rank.rank(), rank.description(), rank.values(), costs(rank.upgradeCost(), items)))
                .toList());
    }

    private static TalentView talent(Talent talent) {
        return new TalentView(talent.id(), talent.displayName(), talent.unlockCondition(), talent.effect());
    }

    private static List<CostView> costs(List<ItemStack> stacks, Map<ItemId, Item> items) {
        return stacks.stream().map(stack -> cost(stack.item(), stack.quantity(), items)).toList();
    }

    /**
     * Sums every step's cost per item, in the order the items were first met.
     *
     * <p>Insertion-ordered rather than sorted: the bundle lists an upgrade's
     * costs in the order the game shows them, and re-sorting the total
     * alphabetically would make it read differently from its parts.
     */
    private static List<CostView> total(List<Upgrade> upgrades, Map<ItemId, Item> items) {
        Map<ItemId, Integer> summed = new LinkedHashMap<>();
        upgrades.forEach(upgrade -> upgrade.costs().forEach(stack ->
                summed.merge(stack.item(), stack.quantity(), Integer::sum)));

        return summed.entrySet().stream()
                .map(entry -> cost(entry.getKey(), entry.getValue(), items))
                .toList();
    }

    /**
     * A cost line with the item's name resolved.
     *
     * <p>The name falls back to the slug rather than throwing. Every reference
     * in a published version resolves — the parser refuses a dangling one and
     * the composite foreign keys refuse it again — so a miss here would mean
     * the load itself was broken, and failing a whole page over a display
     * string would be the wrong trade.
     */
    private static CostView cost(ItemId item, int quantity, Map<ItemId, Item> items) {
        Item known = items.get(item);
        return new CostView(item.value(), known == null ? item.value() : known.displayName(), quantity);
    }
}
