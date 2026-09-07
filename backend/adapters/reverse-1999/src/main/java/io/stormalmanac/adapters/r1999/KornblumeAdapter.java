package io.stormalmanac.adapters.r1999;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.StageId;
import io.stormalmanac.gamedata.Availability;
import io.stormalmanac.gamedata.Craft;
import io.stormalmanac.gamedata.Drop;
import io.stormalmanac.gamedata.Game;
import io.stormalmanac.gamedata.Item;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Rarity;
import io.stormalmanac.gamedata.Sink;
import io.stormalmanac.gamedata.Source;
import io.stormalmanac.gamedata.Stage;
import io.stormalmanac.gamedata.Upgrade;
import io.stormalmanac.gamedata.catalog.Entity;
import io.stormalmanac.gamedata.catalog.StatCurve;
import io.stormalmanac.gamedata.ingest.BundleFormatException;
import io.stormalmanac.gamedata.ingest.GameDataBundle;
import io.stormalmanac.gamedata.ingest.UpstreamAdapter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Reverse: 1999, from a Kornblume data snapshot.
 *
 * <p>The first adapter written against somebody else's data, and therefore the
 * first real test of the claim that a new title costs a bundle plus one of
 * these. Everything peculiar to this upstream is in this file: that it keys on
 * English display names, that it ships one JSON file per concept, that it packs
 * a stat block into a seven-element array, that it encodes material lists as
 * two parallel arrays.
 *
 * <p><b>Provenance.</b> Kornblume is a presentation layer over three upstreams —
 * Huiji Wiki for characters, 必要的记录 for drop rates, ArkPlanner for the
 * farming algorithm — and it carries no licence file, which is all rights
 * reserved rather than permission. So its data is read to produce a bundle and
 * is never vendored into this repository. There is no snapshot in
 * {@code src/test/resources}; {@code tools/fetch-upstream.sh} downloads one on
 * demand. See open question Q3 in {@code TRACKER.md}.
 *
 * <p><b>Expected layout.</b> A directory holding the files as the upstream
 * publishes them under {@code public/data}:
 *
 * <pre>
 * &lt;dir&gt;/items.json      materials and currencies
 * &lt;dir&gt;/stages.json     stages, activity cost, drop tables
 * &lt;dir&gt;/formulas.json   crafting recipes
 * &lt;dir&gt;/arcanists.json  characters, stat blocks, upgrade costs
 * &lt;dir&gt;/psychubes.json  equipment
 * </pre>
 *
 * <h2>What is deliberately not converted, and why</h2>
 *
 * <p>Each of these is a place where the honest move was to drop data rather than
 * to invent a meaning for it. Every one is announced through the notes sink, so
 * a conversion that quietly halves the catalogue is visible before anybody
 * approves it.
 *
 * <ul>
 *   <li><b>{@code shops.json}</b> — its rows are {@code {Material, Quantity}}
 *       under an opaque shop key, with no currency, no unit price and no reset
 *       period. Our {@code Shop} needs all three. Guessing them would put
 *       invented prices into the optimizer's source set, which is worse than
 *       having no shop at all: a wrong number is used, a missing one is noticed.
 *   <li><b>Frequency (resonance patterns)</b> — several entries share one
 *       {@code Id} and differ by {@code Type}, because they are alternative
 *       patterns for the same step. An {@code Upgrade} is a required cost of one
 *       transition, so mapping all four would charge a player for every pattern
 *       at once. The model has no "or" and this adapter will not fake one.
 *   <li><b>A stage that costs nothing</b> — the upstream carries a synthetic
 *       {@code Unreleased} row so its own solver can name items that no real
 *       stage drops yet. A zero-cost source is free output: an optimizer would
 *       run it without bound and report that everything is obtainable today.
 *   <li><b>A formula that lists no materials</b> — the same file holds recipes
 *       and base materials, and a base material's row has an empty
 *       {@code Material} array. Converted faithfully it becomes a craft that
 *       consumes nothing, which is the zero-cost source above wearing a
 *       different hat. This one was not reasoned out in advance: the optimizer
 *       found it on its first real solve and cheerfully crafted 294 250
 *       Sharpodonty out of nothing. {@code Craft} now refuses the shape
 *       outright, so the rule is kept for every future upstream and not only
 *       this one.
 *   <li><b>Unreleased characters and equipment</b> — kept out of the catalogue,
 *       because a plan that farms for something not in the game is not a plan.
 *       They arrive on their own in a later snapshot, and the patch diff is
 *       where they should show up.
 *   <li><b>Skills, talents, banners, rewards, fodder</b> — this upstream simply
 *       does not publish them. An empty section is the truthful output.
 * </ul>
 */
public final class KornblumeAdapter implements UpstreamAdapter {

    /** The stat block is a bare array upstream; these are its columns, in order. */
    private static final List<String> STAT_COLUMNS = List.of(
            "attack", "health", "reality-def", "mental-def", "technique", "crit-rate", "crit-dmg");

    /**
     * Upgrade tracks that are a straight line: step N is entered from step N-1
     * and costs exactly what its row says. Each becomes a chain of
     * {@link Upgrade}s over opaque states like {@code insight-2}.
     */
    private static final List<String> LINEAR_TRACKS = List.of("Insight", "Resonance", "Euphoria", "Mastery");

    private static final GameId GAME = new GameId("reverse-1999");

    private final ObjectMapper json;
    private final Consumer<String> notes;

    public KornblumeAdapter() {
        this(new ObjectMapper(), note -> { });
    }

    public KornblumeAdapter(Consumer<String> notes) {
        this(new ObjectMapper(), notes);
    }

    KornblumeAdapter(ObjectMapper json, Consumer<String> notes) {
        this.json = json;
        this.notes = notes;
    }

    @Override
    public GameId game() {
        return GAME;
    }

    @Override
    public String expects() {
        return "a directory holding items.json, stages.json, formulas.json, arcanists.json"
                + " and psychubes.json, as published under kornblume's public/data";
    }

    @Override
    public GameDataBundle adapt(Path upstream, long sequence, String label) {
        if (!Files.isDirectory(upstream)) {
            throw new BundleFormatException(
                    "not a directory: " + upstream.toAbsolutePath() + " — " + expects());
        }

        Names itemNames = new Names("item");
        Names entityNames = new Names("character or equipment");

        List<Item> items = items(read(upstream, "items.json"), itemNames);
        List<Source> sources = new ArrayList<>();
        sources.addAll(stages(read(upstream, "stages.json"), itemNames));
        sources.addAll(crafts(read(upstream, "formulas.json"), itemNames, notes));

        List<Sink> sinks = new ArrayList<>();
        List<Entity> entities = new ArrayList<>();
        arcanists(read(upstream, "arcanists.json"), itemNames, entityNames, entities, sinks);
        entities.addAll(psychubes(read(upstream, "psychubes.json"), entityNames));

        // Both namespaces are checked before a bundle exists, so an unknown name
        // is reported as an unknown name rather than as a foreign key violation
        // several seconds later.
        itemNames.verify();
        entityNames.verify();

        notes.accept("converted %d items, %d stages, %d crafts, %d entities, %d upgrades"
                .formatted(items.size(),
                        sources.stream().filter(Stage.class::isInstance).count(),
                        sources.stream().filter(Craft.class::isInstance).count(),
                        entities.size(), sinks.size()));

        return new GameDataBundle(
                new Game(GAME, "Reverse: 1999", "Activity"),
                sequence,
                label,
                attribution(label),
                items,
                sources,
                sinks,
                List.of(),
                entities);
    }

    /**
     * Travels with every number this adapter produces, all the way out to the
     * API. "Numbers and text only, attributed" stops being an invariant the
     * moment a response cannot say where its numbers came from.
     */
    static String attribution(String label) {
        return "Reverse: 1999 data for patch " + label + ", converted from a Kornblume snapshot"
                + " (github.com/windbow27/kornblume), which consolidates Huiji Wiki for characters,"
                + " 必要的记录 for drop rates and ArkPlanner for the farming algorithm."
                + " Reverse: 1999 is © Bluepoch. Numbers and text only; no game assets.";
    }

    // ── items.json ──────────────────────────────────────────────────────────

    private List<Item> items(JsonNode root, Names itemNames) {
        List<Item> items = new ArrayList<>();
        for (JsonNode node : array(root, "items.json")) {
            String name = text(node, "Name", "items.json");
            items.add(new Item(
                    new ItemId(itemNames.declare(name)),
                    name,
                    rarity(node, "items.json item \"" + name + "\""),
                    // Slugged, unlike displayName and element: a category is a key
                    // — Fodder matches on it — and a key that is a display string
                    // is a rename waiting to break a join.
                    Names.slug(text(node, "Category", "items.json item \"" + name + "\""))));
        }
        return items;
    }

    // ── stages.json ─────────────────────────────────────────────────────────

    private List<Stage> stages(JsonNode root, Names itemNames) {
        List<Stage> stages = new ArrayList<>();
        int freeStages = 0;

        if (root == null || !root.isObject()) {
            // Alone among the five files this one is a map keyed by stage name,
            // not a list. An array here would iterate to nothing and produce a
            // bundle with no stages in it, which is the quiet failure.
            throw new BundleFormatException("stages.json must hold a JSON object keyed by stage name");
        }

        for (Map.Entry<String, JsonNode> entry : root.properties()) {
            String key = entry.getKey();
            JsonNode node = entry.getValue();
            int cost = (int) integer(node, "cost", "stages.json \"" + key + "\"");

            if (cost <= 0) {
                // Free output. Left out on purpose — see the class javadoc.
                freeStages++;
                continue;
            }

            List<Drop> drops = new ArrayList<>();
            JsonNode dropTable = node.get("drops");
            if (dropTable != null && dropTable.isObject()) {
                for (Map.Entry<String, JsonNode> drop : dropTable.properties()) {
                    if (!drop.getValue().isNumber()) {
                        throw new BundleFormatException("stages.json \"" + key + "\" drops \""
                                + drop.getKey() + "\" with a value that is not a number");
                    }
                    drops.add(new Drop(
                            new ItemId(itemNames.reference(drop.getKey(), "stage \"" + key + "\"")),
                            // Already expected quantity per run, above 1.0 for the
                            // generous stages. This is why Drop holds a yield and
                            // not a probability — see docs/prior-art.md §4.1.
                            drop.getValue().doubleValue()));
                }
            }
            stages.add(new Stage(
                    new StageId(Names.slug(key)),
                    node.hasNonNull("name") ? node.get("name").asText() : key,
                    cost,
                    drops,
                    Availability.ALWAYS));
        }
        if (freeStages > 0) {
            notes.accept("skipped " + freeStages + " stage(s) costing no Activity:"
                    + " a free source is an unbounded one");
        }
        return stages;
    }

    // ── formulas.json ───────────────────────────────────────────────────────

    private List<Craft> crafts(JsonNode root, Names itemNames, Consumer<String> notes) {
        List<Craft> crafts = new ArrayList<>();
        int catalogueRows = 0;

        for (JsonNode node : array(root, "formulas.json")) {
            String produced = text(node, "Name", "formulas.json");
            String at = "formula \"" + produced + "\"";

            // The upstream lists base materials in the same file as its recipes,
            // as rows with an empty Material array — Sharpodonty, Silver Ore and
            // ten others. They are catalogue entries, not conversions, and
            // converting them faithfully produced a craft that made currency out
            // of nothing at zero energy. The optimizer found it immediately and
            // ran it 294 250 times, which is the correct answer to the wrong
            // model. Same rule as the free stages above: a free source is an
            // unbounded one.
            List<ItemStack> materials = stacks(node, at, itemNames);
            if (materials.isEmpty()) {
                catalogueRows++;
                continue;
            }
            crafts.add(new Craft(
                    "craft-" + itemNames.reference(produced, at),
                    materials,
                    // Upstream states no output quantity, and every recipe it
                    // publishes makes one. Stated here rather than assumed
                    // silently: if a recipe ever makes two, this is the line.
                    List.of(new ItemStack(new ItemId(itemNames.reference(produced, at)), 1)),
                    Availability.ALWAYS));
        }
        if (catalogueRows > 0) {
            notes.accept("skipped " + catalogueRows + " formula(s) listing no materials:"
                    + " a base material's catalogue row, not a recipe, and a craft that"
                    + " consumes nothing is an unbounded source");
        }
        return crafts;
    }

    // ── arcanists.json ──────────────────────────────────────────────────────

    private void arcanists(
            JsonNode root, Names itemNames, Names entityNames, List<Entity> entities, List<Sink> sinks) {
        int unreleased = 0;
        int patterns = 0;

        for (JsonNode node : array(root, "arcanists.json")) {
            // The release flag is read before anything else, because an unreleased
            // row upstream is a placeholder and its other fields may be null. A
            // conversion must not be refused by a row it was going to drop.
            if (!node.path("IsReleased").asBoolean(false)) {
                unreleased++;
                continue;
            }
            String name = text(node, "Name", "arcanists.json");
            String slug = entityNames.declare(name);
            String at = "arcanist \"" + name + "\"";

            entities.add(new Entity(
                    new EntityId(slug),
                    name,
                    "character",
                    rarity(node, at),
                    // Verbatim, unlike category: nothing joins on an element, so
                    // slugging it would only make it uglier to read.
                    node.path("Afflatus").asText(""),
                    List.of(),
                    statCurves(node.get("Stats"), at),
                    List.of(),
                    List.of()));

            for (String track : LINEAR_TRACKS) {
                sinks.addAll(linearTrack(node.get(track), track, slug, at, itemNames));
            }
            patterns += node.path("Frequency").size();
        }
        if (unreleased > 0) {
            notes.accept("skipped " + unreleased + " unreleased character(s)");
        }
        if (patterns > 0) {
            notes.accept("skipped " + patterns + " resonance-pattern cost row(s):"
                    + " they are alternatives for one step and an Upgrade is not a choice");
        }
    }

    /**
     * A stat block of seven numbers per ascension node becomes seven curves of
     * one breakpoint each per node. An all-zero block means the upstream has not
     * filled the numbers in yet, and is written as no curves at all — a zero is
     * a claim about a character, an absence is not.
     */
    private List<StatCurve> statCurves(JsonNode stats, String at) {
        if (stats == null || !stats.isObject() || stats.isEmpty()) return List.of();

        Map<String, List<StatCurve.Breakpoint>> byStat = new LinkedHashMap<>();
        STAT_COLUMNS.forEach(stat -> byStat.put(stat, new ArrayList<>()));
        boolean anyNonZero = false;

        for (Map.Entry<String, JsonNode> node : stats.properties()) {
            // The key is "<ascension tier>-<level>", which is the same insight
            // this project spells as an opaque upgrade state.
            String[] parts = node.getKey().split("-", 2);
            if (parts.length != 2) {
                throw new BundleFormatException(
                        at + " has stat node \"" + node.getKey() + "\", expected \"<tier>-<level>\"");
            }
            int tier = number(parts[0], at + " stat node \"" + node.getKey() + "\"");
            int level = number(parts[1], at + " stat node \"" + node.getKey() + "\"");

            JsonNode values = node.getValue();
            if (!values.isArray() || values.size() != STAT_COLUMNS.size()) {
                throw new BundleFormatException(at + " stat node \"" + node.getKey() + "\" holds "
                        + values.size() + " values, expected " + STAT_COLUMNS.size() + " "
                        + STAT_COLUMNS);
            }
            for (int i = 0; i < STAT_COLUMNS.size(); i++) {
                double value = values.get(i).doubleValue();
                anyNonZero |= value != 0;
                byStat.get(STAT_COLUMNS.get(i)).add(new StatCurve.Breakpoint(tier, level, value));
            }
        }
        if (!anyNonZero) return List.of();

        return byStat.entrySet().stream()
                .map(entry -> new StatCurve(entry.getKey(), entry.getValue().stream()
                        // StatCurve interpolates between neighbours, so the
                        // breakpoints have to arrive in order whatever order the
                        // upstream's object keys came in.
                        .sorted(Comparator
                                .comparingInt(StatCurve.Breakpoint::ascensionTier)
                                .thenComparingInt(StatCurve.Breakpoint::level))
                        .toList()))
                .toList();
    }

    /**
     * One upgrade track — Insight, Resonance, Euphoria, Mastery — as a chain of
     * transitions between opaque states. Step {@code n} is entered from step
     * {@code n - 1}; the upstream numbers them from 1 for insight and from 2 for
     * resonance, and neither this method nor anything downstream needs to know
     * which, because the states are strings the game supplies.
     */
    private List<Upgrade> linearTrack(
            JsonNode track, String name, String entity, String at, Names itemNames) {
        if (track == null || !track.isArray()) return List.of();

        String state = Names.slug(name);
        List<Upgrade> upgrades = new ArrayList<>();
        for (JsonNode step : track) {
            long id = integer(step, "Id", at + " " + name);
            upgrades.add(new Upgrade(
                    entity + "-" + state + "-" + id,
                    new EntityId(entity),
                    state + "-" + (id - 1),
                    state + "-" + id,
                    stacks(step, at + " " + name + " " + id, itemNames)));
        }
        return upgrades;
    }

    // ── psychubes.json ──────────────────────────────────────────────────────

    private List<Entity> psychubes(JsonNode root, Names entityNames) {
        List<Entity> equipment = new ArrayList<>();
        int unreleased = 0;

        for (JsonNode node : array(root, "psychubes.json")) {
            // Release flag first: the real upstream ships unreleased psychubes as
            // rows whose every field but Id and Rarity is null.
            if (!node.path("IsReleased").asBoolean(false)) {
                unreleased++;
                continue;
            }
            String name = text(node, "Name", "psychubes.json");
            equipment.add(new Entity(
                    new EntityId(entityNames.declare(name)),
                    name,
                    // ADR 0007: equipment is an Entity with a kind, not a third
                    // top-level concept. This is the first time that decision has
                    // met real equipment data.
                    "equipment",
                    rarity(node, "psychube \"" + name + "\""),
                    "",
                    tags(node.path("Tag").asText("")),
                    List.of(),
                    List.of(),
                    List.of()));
        }
        if (unreleased > 0) {
            notes.accept("skipped " + unreleased + " unreleased psychube(s)");
        }
        return equipment;
    }

    /** {@code "ATK, Critical"} becomes two tags; {@code "None"} and null become none. */
    private static List<String> tags(String tag) {
        if (tag.isBlank() || "None".equals(tag) || "null".equals(tag)) return List.of();
        return Arrays.stream(tag.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                .map(Names::slug).toList();
    }

    // ── The two parallel arrays, which is how this upstream states a cost ────

    private static List<ItemStack> stacks(JsonNode node, String at, Names itemNames) {
        JsonNode materials = node.path("Material");
        JsonNode quantities = node.path("Quantity");
        if (!materials.isArray() || !quantities.isArray()) return List.of();

        if (materials.size() != quantities.size()) {
            throw new BundleFormatException(at + " lists " + materials.size() + " material(s) against "
                    + quantities.size() + " quantity(s); the upstream states a cost as two parallel"
                    + " arrays and they have gone out of step");
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < materials.size(); i++) {
            stacks.add(new ItemStack(
                    new ItemId(itemNames.reference(materials.get(i).asText(), at)),
                    (int) integer(quantities.get(i), at + " quantity " + i)));
        }
        return stacks;
    }

    // ── Plumbing ────────────────────────────────────────────────────────────

    private JsonNode read(Path upstream, String file) {
        Path path = upstream.resolve(file);
        if (!Files.isReadable(path)) {
            throw new BundleFormatException("cannot read " + path.toAbsolutePath()
                    + " — this adapter expects " + expects());
        }
        try (InputStream in = Files.newInputStream(path)) {
            return json.readTree(in);
        } catch (JacksonException e) {
            throw new BundleFormatException(file + " is not valid JSON: " + e.getOriginalMessage(), e);
        } catch (IOException e) {
            throw new BundleFormatException("cannot read " + path.toAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    private static Iterable<JsonNode> array(JsonNode root, String file) {
        if (root == null || !root.isArray()) {
            throw new BundleFormatException(file + " must hold a JSON array at the top level");
        }
        return root;
    }

    private static Rarity rarity(JsonNode node, String at) {
        int rank = (int) integer(node, "Rarity", at);
        // The upstream states a star count and nothing else, so the label is
        // derived. Rarity stays (label, rank) rather than an enum because the
        // second game's rarities are not this game's.
        return new Rarity(rank + "*", rank);
    }

    private static String text(JsonNode node, String field, String at) {
        JsonNode value = node.get(field);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw new BundleFormatException(at + " is missing a textual '" + field + "'");
        }
        return value.asText();
    }

    private static long integer(JsonNode node, String field, String at) {
        JsonNode value = node.get(field);
        if (value == null || !value.isIntegralNumber()) {
            throw new BundleFormatException(at + " is missing a whole-number '" + field + "'");
        }
        return value.longValue();
    }

    private static long integer(JsonNode value, String at) {
        if (!value.isIntegralNumber()) {
            throw new BundleFormatException(at + " must be a whole number, not " + value);
        }
        return value.longValue();
    }

    private static int number(String value, String at) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BundleFormatException(at + ": \"" + value + "\" is not a number", e);
        }
    }
}
