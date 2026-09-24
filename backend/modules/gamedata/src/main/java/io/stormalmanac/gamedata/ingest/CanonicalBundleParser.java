package io.stormalmanac.gamedata.ingest;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stormalmanac.common.id.BannerId;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.StageId;
import io.stormalmanac.gamedata.Availability;
import io.stormalmanac.gamedata.Craft;
import io.stormalmanac.gamedata.DayBoundary;
import io.stormalmanac.gamedata.Drop;
import io.stormalmanac.gamedata.FactRef;
import io.stormalmanac.gamedata.Fodder;
import io.stormalmanac.gamedata.Game;
import io.stormalmanac.gamedata.Item;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Progress;
import io.stormalmanac.gamedata.ProgressKind;
import io.stormalmanac.gamedata.Provenance;
import io.stormalmanac.gamedata.Rarity;
import io.stormalmanac.gamedata.Reward;
import io.stormalmanac.gamedata.Shop;
import io.stormalmanac.gamedata.Sink;
import io.stormalmanac.gamedata.Source;
import io.stormalmanac.gamedata.Stage;
import io.stormalmanac.gamedata.Upgrade;
import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.gamedata.banner.FeaturedRule;
import io.stormalmanac.gamedata.banner.Floor;
import io.stormalmanac.gamedata.banner.PityRule;
import io.stormalmanac.gamedata.banner.PityScope;
import io.stormalmanac.gamedata.banner.PullPrice;
import io.stormalmanac.gamedata.catalog.Entity;
import io.stormalmanac.gamedata.catalog.Skill;
import io.stormalmanac.gamedata.catalog.StatCurve;
import io.stormalmanac.gamedata.catalog.Talent;
import java.io.IOException;
import java.io.InputStream;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Reads the canonical bundle format: the JSON shape a parser adapter must
 * produce, and the only input {@link GameDataBundle} is built from.
 *
 * <p>An upstream data source is not expected to speak this format. Onboarding a
 * title is writing an adapter that converts whatever the upstream publishes into
 * this, and the conversion is where every game-specific quirk is allowed to
 * live — so that no other module ever has to know about one.
 *
 * <p>Parsing is done against Jackson's tree model rather than by data-binding
 * onto the domain records, for two reasons. The records stay free of mapping
 * annotations, which matters because {@code Source} and {@code Sink} are sealed
 * and a polymorphic binder would want to write its discriminator into them. And
 * every failure can name its own position: a bundle rejected at
 * {@code stages[4].drops[1].expectedYield} is a bundle somebody can fix.
 */
public final class CanonicalBundleParser {

    private final ObjectMapper json;

    public CanonicalBundleParser() {
        this(new ObjectMapper());
    }

    public CanonicalBundleParser(ObjectMapper json) {
        this.json = json;
    }

    public GameDataBundle parse(InputStream in) {
        JsonNode root;
        try {
            root = json.readTree(in);
        } catch (JacksonException e) {
            throw new BundleFormatException("bundle is not valid JSON: " + e.getOriginalMessage(), e);
        } catch (IOException e) {
            throw new BundleFormatException("bundle could not be read: " + e.getMessage(), e);
        }
        if (root == null || !root.isObject()) {
            throw new BundleFormatException("bundle must be a JSON object");
        }
        return parse(root);
    }

    public GameDataBundle parse(String text) {
        try {
            return parse(json.readTree(text));
        } catch (JacksonException e) {
            throw new BundleFormatException("bundle is not valid JSON: " + e.getOriginalMessage(), e);
        }
    }

    private GameDataBundle parse(JsonNode root) {
        JsonNode game = required(root, "game", "game");
        List<Source> sources = new ArrayList<>();
        sources.addAll(each(root, "stages", CanonicalBundleParser::stage));
        sources.addAll(each(root, "crafts", CanonicalBundleParser::craft));
        sources.addAll(each(root, "shops", CanonicalBundleParser::shop));
        sources.addAll(each(root, "rewards", CanonicalBundleParser::reward));

        List<Sink> sinks = new ArrayList<>();
        sinks.addAll(each(root, "upgrades", CanonicalBundleParser::upgrade));
        Map<String, String> factProvenance = new LinkedHashMap<>(factProvenance(root));
        for (UpgradeLadders.Row laddered : UpgradeLadders.expand(root)) {
            Upgrade upgrade = laddered(laddered);
            sinks.add(upgrade);
            if (laddered.sourcedBy() == null) continue;
            // Said in one place or not at all: a ladder that sources a row and a
            // factProvenance entry that sources it again can only agree by luck,
            // and the one that loses is the one somebody believes.
            String ref = FactRef.of(upgrade);
            if (factProvenance.putIfAbsent(ref, laddered.sourcedBy()) != null) {
                throw new BundleFormatException("factProvenance names '" + ref + "', which "
                        + laddered.at() + " already sources; say it on the ladder or here, not both");
            }
        }
        sinks.addAll(each(root, "fodder", CanonicalBundleParser::fodder));

        return new GameDataBundle(
                new Game(
                        new GameId(text(game, "id", "game.id")),
                        text(game, "displayName", "game.displayName"),
                        text(game, "energyUnit", "game.energyUnit"),
                        dayBoundary(game)),
                integer(root, "sequence", "sequence"),
                textOrEmpty(root, "label"),
                text(root, "attribution", "attribution"),
                each(root, "provenance", CanonicalBundleParser::provenance),
                textOrEmpty(root, "sourcedBy"),
                factProvenance,
                each(root, "items", CanonicalBundleParser::item),
                sources,
                sinks,
                each(root, "banners", CanonicalBundleParser::banner),
                each(root, "entities", CanonicalBundleParser::entity),
                each(root, "progressKinds", CanonicalBundleParser::progressKind));
    }

    // ── The domain shapes, one method each ──────────────────────────────────

    private static Provenance provenance(JsonNode node, String at) {
        String origin = text(node, "origin", at + ".origin");
        try {
            return new Provenance(
                    text(node, "id", at + ".id"),
                    Provenance.Origin.valueOf(origin),
                    text(node, "detail", at + ".detail"),
                    LocalDate.parse(text(node, "observedOn", at + ".observedOn")));
        } catch (IllegalArgumentException e) {
            // Covers both the unknown origin and the record's own validation, and
            // names the alternatives: the set is small, closed and not guessable
            // from the failure otherwise.
            throw new BundleFormatException(at + ": " + e.getMessage()
                    + " (origins: " + Arrays.toString(Provenance.Origin.values()) + ")", e);
        } catch (DateTimeParseException e) {
            throw new BundleFormatException(
                    at + ".observedOn must be an ISO date, e.g. 2026-09-09, not '" + e.getParsedString() + "'", e);
        }
    }

    /**
     * The per-fact overrides, as a flat object of {@code factRef -> provenanceId}.
     *
     * <p>Absent means "every fact came from {@code sourcedBy}", which is the
     * normal case and is why this is not required. What it must never mean is
     * "no fact has a provenance" — {@link GameDataBundle} resolves an absent
     * entry to the default rather than to nothing, so the two cannot be
     * confused.
     */
    private static Map<String, String> factProvenance(JsonNode root) {
        JsonNode node = root.get("factProvenance");
        if (node == null || node.isNull()) return Map.of();
        if (!node.isObject()) {
            throw new BundleFormatException(
                    "factProvenance must be an object of factRef -> provenance id");
        }
        Map<String, String> overrides = new LinkedHashMap<>();
        node.properties().forEach(entry -> {
            if (!entry.getValue().isTextual()) {
                throw new BundleFormatException(
                        "factProvenance." + entry.getKey() + " must be a provenance id");
            }
            overrides.put(entry.getKey(), entry.getValue().textValue());
        });
        return overrides;
    }

    /**
     * A name for a progress kind. Two textual fields and no rarity, no
     * provenance and no id — it is not a fact, it is the bundle spelling its own
     * opaque label for a reader. See {@link ProgressKind}.
     */
    private static ProgressKind progressKind(JsonNode node, String at) {
        return new ProgressKind(
                text(node, "kind", at + ".kind"),
                text(node, "displayName", at + ".displayName"));
    }

    private static Item item(JsonNode node, String at) {
        return new Item(
                new ItemId(text(node, "id", at + ".id")),
                text(node, "displayName", at + ".displayName"),
                rarity(required(node, "rarity", at + ".rarity"), at + ".rarity"),
                text(node, "category", at + ".category"));
    }

    private static Entity entity(JsonNode node, String at) {
        return new Entity(
                new EntityId(text(node, "id", at + ".id")),
                text(node, "displayName", at + ".displayName"),
                // Required and never defaulted: a bundle that omits it yields an
                // unroutable catalog. See ADR 0007.
                text(node, "kind", at + ".kind"),
                rarity(required(node, "rarity", at + ".rarity"), at + ".rarity"),
                textOrEmpty(node, "element"),
                strings(node, "tags"),
                each(node, "statCurves", CanonicalBundleParser::statCurve, at),
                each(node, "skills", CanonicalBundleParser::skill, at),
                each(node, "talents", CanonicalBundleParser::talent, at));
    }

    private static StatCurve statCurve(JsonNode node, String at) {
        return new StatCurve(
                text(node, "stat", at + ".stat"),
                each(node, "breakpoints", (b, bAt) -> new StatCurve.Breakpoint(
                        integerOr(b, "ascensionTier", 0),
                        (int) integer(b, "level", bAt + ".level"),
                        number(b, "value", bAt + ".value")), at));
    }

    private static Skill skill(JsonNode node, String at) {
        return new Skill(
                text(node, "id", at + ".id"),
                text(node, "displayName", at + ".displayName"),
                each(node, "ranks", (r, rAt) -> new Skill.Rank(
                        (int) integer(r, "rank", rAt + ".rank"),
                        values(r, rAt),
                        textOrEmpty(r, "description"),
                        stacks(r, "upgradeCost", rAt)), at));
    }

    private static Map<String, Double> values(JsonNode rank, String at) {
        JsonNode values = rank.get("values");
        if (values == null || values.isNull()) return Map.of();
        if (!values.isObject()) throw new BundleFormatException(at + ".values must be an object");
        Map<String, Double> parsed = new LinkedHashMap<>();
        values.properties().forEach(entry -> {
            if (!entry.getValue().isNumber()) {
                throw new BundleFormatException(at + ".values." + entry.getKey() + " must be a number");
            }
            parsed.put(entry.getKey(), entry.getValue().doubleValue());
        });
        return parsed;
    }

    private static Talent talent(JsonNode node, String at) {
        return new Talent(
                text(node, "id", at + ".id"),
                text(node, "displayName", at + ".displayName"),
                text(node, "unlockCondition", at + ".unlockCondition"),
                textOrEmpty(node, "effect"));
    }

    private static Stage stage(JsonNode node, String at) {
        return new Stage(
                new StageId(text(node, "id", at + ".id")),
                text(node, "displayName", at + ".displayName"),
                (int) integer(node, "energyCost", at + ".energyCost"),
                each(node, "drops", (d, dAt) -> new Drop(
                        new ItemId(text(d, "item", dAt + ".item")),
                        number(d, "expectedYield", dAt + ".expectedYield"),
                        // Optional, and its absence means "declared, not
                        // measured" rather than "measured over nothing". A
                        // bundle written before this field existed parses to
                        // exactly what it meant.
                        sampledRuns(d, dAt)), at),
                availability(node, at));
    }

    private static Craft craft(JsonNode node, String at) {
        List<ItemStack> consumes = stacks(node, "consumes", at);
        List<ItemStack> produces = stacks(node, "produces", at);
        // Checked here as well as in Craft so the refusal says which entry in
        // which file. A bundle is a document a human approves, and "craft
        // 'craft-sharpodonty' consumes nothing" is a sentence they can act on
        // where a stack trace off a record constructor is not.
        if (consumes.isEmpty()) {
            throw new BundleFormatException(at + ".consumes is empty: a craft that consumes"
                    + " nothing is unbounded free supply, and the optimizer would mint it");
        }
        if (produces.isEmpty()) {
            throw new BundleFormatException(at + ".produces is empty: nothing would ever run it");
        }
        return new Craft(text(node, "id", at + ".id"), consumes, produces, availability(node, at));
    }

    private static Shop shop(JsonNode node, String at) {
        return new Shop(
                text(node, "id", at + ".id"),
                new ItemId(text(node, "currency", at + ".currency")),
                (int) integer(node, "price", at + ".price"),
                stack(required(node, "offer", at + ".offer"), at + ".offer"),
                (int) integer(node, "periodLimit", at + ".periodLimit"),
                period(node, at),
                availability(node, at));
    }

    private static Period period(JsonNode node, String at) {
        String iso = text(node, "period", at + ".period");
        try {
            return Shop.parsePeriod(iso);
        } catch (DateTimeParseException e) {
            throw new BundleFormatException(
                    at + ".period must be an ISO-8601 period such as \"P1D\", or \"" + Shop.NEVER
                            + "\" for a limit that never resets, not \"" + iso + "\"", e);
        }
    }

    private static Reward reward(JsonNode node, String at) {
        String cadence = text(node, "cadence", at + ".cadence");
        try {
            return new Reward(
                    text(node, "id", at + ".id"),
                    Reward.Cadence.valueOf(cadence),
                    stacks(node, "grants", at),
                    availability(node, at),
                    requirement(node, at));
        } catch (IllegalArgumentException e) {
            throw new BundleFormatException(
                    at + ".cadence must be one of " + List.of(Reward.Cadence.values()) + ", not \"" + cadence + "\"",
                    e);
        }
    }

    /**
     * The bar a grant stands behind, or nothing.
     *
     * <p>Absent means the grant turns up for every reader, which is what every
     * reward written before ADR 0022 says. An object that is there is checked
     * whole: a measure with no bar, or a bar with no measure, is a row somebody
     * meant to finish.
     */
    private static Reward.Requirement requirement(JsonNode node, String at) {
        JsonNode requires = node.get("requires");
        if (requires == null || requires.isNull()) return null;
        if (!requires.isObject()) {
            throw new BundleFormatException(
                    at + ".requires must be an object with a measure and an atLeast");
        }
        try {
            return new Reward.Requirement(
                    text(requires, "measure", at + ".requires.measure"),
                    (int) integer(requires, "atLeast", at + ".requires.atLeast"));
        } catch (IllegalArgumentException e) {
            throw new BundleFormatException(at + ".requires is not a requirement: " + e.getMessage(), e);
        }
    }

    private static Upgrade upgrade(JsonNode node, String at) {
        return new Upgrade(
                text(node, "id", at + ".id"),
                new EntityId(text(node, "entity", at + ".entity")),
                text(node, "fromState", at + ".fromState"),
                text(node, "toState", at + ".toState"),
                stacks(node, "costs", at),
                strings(node, "requires"),
                each(node, "progress", (p, pAt) -> new Progress(
                        text(p, "kind", pAt + ".kind"),
                        (int) integer(p, "quantity", pAt + ".quantity")), at));
    }

    /** A row a ladder expanded, parsed exactly as a hand-written one and failing with its origin. */
    private static Upgrade laddered(UpgradeLadders.Row laddered) {
        try {
            return upgrade(laddered.row(), laddered.at());
        } catch (BundleFormatException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new BundleFormatException(laddered.at() + ": " + e.getMessage(), e);
        }
    }

    /**
     * {@code progress} is optional, and the one field in this format that is
     * optional because of data already published rather than because the
     * common case omits it: every fodder rule published before it existed has
     * none. Such a rule parses and feeds nothing, which is what it did then.
     */
    private static Fodder fodder(JsonNode node, String at) {
        JsonNode progress = node.get("progress");
        return new Fodder(
                text(node, "id", at + ".id"),
                text(node, "consumesCategory", at + ".consumesCategory"),
                rarity(required(node, "minimumRarity", at + ".minimumRarity"), at + ".minimumRarity"),
                progress == null || progress.isNull() ? null : text(node, "progress", at + ".progress"),
                (int) integer(node, "progressPerUnit", at + ".progressPerUnit"),
                stacks(node, "costs", at));
    }

    private static BannerModel banner(JsonNode node, String at) {
        Map<Rarity, Double> baseRates = new LinkedHashMap<>();
        forEach(node, "baseRates", (r, rAt) -> baseRates.put(
                rarity(required(r, "rarity", rAt + ".rarity"), rAt + ".rarity"),
                number(r, "rate", rAt + ".rate")), at);

        Map<Rarity, PityRule> pityRules = new LinkedHashMap<>();
        forEach(node, "pityRules", (r, rAt) -> pityRules.put(
                rarity(required(r, "rarity", rAt + ".rarity"), rAt + ".rarity"),
                pityRule(r, rAt)), at);

        JsonNode featured = node.get("featured");
        String scope = text(node, "pityScope", at + ".pityScope");
        try {
            return new BannerModel(
                    new BannerId(text(node, "id", at + ".id")),
                    text(node, "displayName", at + ".displayName"),
                    text(node, "bannerType", at + ".bannerType"),
                    baseRates,
                    pityRules,
                    each(node, "floors", (f, fAt) -> new Floor(
                            (int) integer(f, "everyN", fAt + ".everyN"),
                            rarity(required(f, "minimumRarity", fAt + ".minimumRarity"),
                                    fAt + ".minimumRarity")), at),
                    featured == null || featured.isNull()
                            ? FeaturedRule.ALWAYS
                            : new FeaturedRule(
                                    number(featured, "chanceAtHit", at + ".featured.chanceAtHit"),
                                    (int) integer(featured, "guaranteeAfterLoss",
                                            at + ".featured.guaranteeAfterLoss")),
                    PityScope.valueOf(scope),
                    availability(node, at),
                    pullPrice(node, at));
        } catch (IllegalArgumentException e) {
            throw new BundleFormatException(at + ": " + e.getMessage(), e);
        }
    }

    /**
     * What a pull costs, or null when the banner does not say.
     *
     * <p>Absent stays absent rather than becoming a zero price. A banner read
     * for its rates before anybody read its price is a real thing in this
     * repository's history, and it has to round-trip as one.
     */
    private static PullPrice pullPrice(JsonNode node, String at) {
        JsonNode price = node.get("pullPrice");
        if (price == null || price.isNull()) return null;
        return new PullPrice(
                new ItemId(text(price, "currency", at + ".pullPrice.currency")),
                (int) integer(price, "perPull", at + ".pullPrice.perPull"));
    }

    /**
     * When the game's day rolls over, if the bundle says.
     *
     * <p>Absent stays absent rather than becoming midnight UTC: a version is
     * immutable, and every one published before this field existed claimed
     * nothing about a reset. The zone is refused by name when it is not a zone
     * Java knows, because {@code "UTC+7"} and {@code "Asia/Bangkok"} are both
     * things a reader might type and only one of them is an identifier.
     */
    private static DayBoundary dayBoundary(JsonNode game) {
        JsonNode node = game.get("dayBoundary");
        if (node == null || node.isNull()) return null;
        if (!node.isObject()) {
            throw new BundleFormatException("game.dayBoundary must be an object of zone and hour");
        }
        String zone = text(node, "zone", "game.dayBoundary.zone");
        try {
            return new DayBoundary(
                    ZoneId.of(zone),
                    (int) integer(node, "hour", "game.dayBoundary.hour"));
        } catch (DateTimeException e) {
            throw new BundleFormatException(
                    "game.dayBoundary.zone is not a zone: '" + zone + "'", e);
        } catch (IllegalArgumentException e) {
            throw new BundleFormatException("game.dayBoundary: " + e.getMessage(), e);
        }
    }

    private static PityRule pityRule(JsonNode node, String at) {
        JsonNode softFrom = node.get("softFrom");
        boolean soft = softFrom != null && !softFrom.isNull();
        // Absent means the guarantee is fixed at hardAt, which is what every
        // bundle written before a game drew its wall was saying all along.
        JsonNode drawnFrom = node.get("drawnFrom");
        boolean drawn = drawnFrom != null && !drawnFrom.isNull();
        return new PityRule(
                (int) integer(node, "hardAt", at + ".hardAt"),
                soft ? (int) integer(node, "softFrom", at + ".softFrom") : null,
                soft ? number(node, "softJumpTo", at + ".softJumpTo") : null,
                soft ? number(node, "softStep", at + ".softStep") : null,
                drawn ? (int) integer(node, "drawnFrom", at + ".drawnFrom") : null);
    }

    private static Rarity rarity(JsonNode node, String at) {
        return new Rarity(
                text(node, "label", at + ".label"),
                (int) integer(node, "rank", at + ".rank"));
    }

    private static ItemStack stack(JsonNode node, String at) {
        return new ItemStack(
                new ItemId(text(node, "item", at + ".item")),
                (int) integer(node, "quantity", at + ".quantity"));
    }

    private static List<ItemStack> stacks(JsonNode parent, String field, String at) {
        return each(parent, field, CanonicalBundleParser::stack, at);
    }

    /**
     * An absent {@code availability} object means {@link Availability#ALWAYS} —
     * the common case by a wide margin, and spelling it out on every source
     * would bury the ones that actually rotate or expire.
     */
    private static Availability availability(JsonNode node, String at) {
        JsonNode window = node.get("availability");
        if (window == null || window.isNull()) return Availability.ALWAYS;

        Set<DayOfWeek> days = new LinkedHashSet<>();
        for (String day : strings(window, "days")) {
            try {
                days.add(DayOfWeek.valueOf(day));
            } catch (IllegalArgumentException e) {
                throw new BundleFormatException(
                        at + ".availability.days contains \"" + day + "\", which is not a weekday name", e);
            }
        }
        return new Availability(
                days,
                instant(window, "opensAt", at + ".availability.opensAt"),
                instant(window, "closesAt", at + ".availability.closesAt"));
    }

    // ── Scalars, each failure naming its own position ───────────────────────

    private static JsonNode required(JsonNode parent, String field, String at) {
        JsonNode node = parent.get(field);
        if (node == null || node.isNull()) throw new BundleFormatException(at + " is required");
        if (!node.isObject()) throw new BundleFormatException(at + " must be an object");
        return node;
    }

    private static String text(JsonNode parent, String field, String at) {
        JsonNode node = parent.get(field);
        if (node == null || node.isNull()) throw new BundleFormatException(at + " is required");
        if (!node.isTextual()) throw new BundleFormatException(at + " must be a string");
        String value = node.textValue();
        if (value.isBlank()) throw new BundleFormatException(at + " must not be blank");
        return value;
    }

    private static String textOrEmpty(JsonNode parent, String field) {
        JsonNode node = parent.get(field);
        return node == null || node.isNull() ? "" : node.asText();
    }

    private static long integer(JsonNode parent, String field, String at) {
        JsonNode node = parent.get(field);
        if (node == null || node.isNull()) throw new BundleFormatException(at + " is required");
        if (!node.isIntegralNumber()) throw new BundleFormatException(at + " must be a whole number");
        return node.longValue();
    }

    /**
     * A drop's sample size: optional, whole, and positive when it is there.
     *
     * <p>Refuses zero explicitly rather than folding it into the absent case.
     * Both end up as a declared yield, but a bundle that writes
     * {@code "sampledRuns": 0} is making a claim — that somebody measured this
     * over no runs — and the honest response to a claim that cannot be true is
     * to reject the file, not to quietly agree with it.
     */
    private static long sampledRuns(JsonNode parent, String at) {
        JsonNode node = parent.get("sampledRuns");
        if (node == null || node.isNull()) return 0;
        if (!node.isIntegralNumber()) {
            throw new BundleFormatException(at + ".sampledRuns must be a whole number of runs");
        }
        long runs = node.longValue();
        if (runs <= 0) {
            throw new BundleFormatException(at + ".sampledRuns is " + runs
                    + ": a measured yield needs at least one run behind it, and a yield the"
                    + " data declares rather than measures omits the field");
        }
        return runs;
    }

    private static int integerOr(JsonNode parent, String field, int fallback) {
        JsonNode node = parent.get(field);
        return node == null || node.isNull() ? fallback : node.intValue();
    }

    private static double number(JsonNode parent, String field, String at) {
        JsonNode node = parent.get(field);
        if (node == null || node.isNull()) throw new BundleFormatException(at + " is required");
        if (!node.isNumber()) throw new BundleFormatException(at + " must be a number");
        return node.doubleValue();
    }

    private static Instant instant(JsonNode parent, String field, String at) {
        JsonNode node = parent.get(field);
        if (node == null || node.isNull()) return null;
        try {
            return Instant.parse(node.asText());
        } catch (DateTimeParseException e) {
            throw new BundleFormatException(
                    at + " must be an ISO-8601 instant such as \"2026-01-01T00:00:00Z\"", e);
        }
    }

    private static List<String> strings(JsonNode parent, String field) {
        JsonNode array = parent.get(field);
        if (array == null || array.isNull()) return List.of();
        if (!array.isArray()) throw new BundleFormatException(field + " must be an array");
        List<String> values = new ArrayList<>();
        array.forEach(node -> values.add(node.asText()));
        return values;
    }

    /** Element mapper: gets the node and the path to it, for error messages. */
    private interface ElementParser<T> {
        T parse(JsonNode node, String at);
    }

    private static <T> List<T> each(JsonNode parent, String field, ElementParser<T> parser) {
        return each(parent, field, parser, "");
    }

    private static <T> List<T> each(JsonNode parent, String field, ElementParser<T> parser, String prefix) {
        JsonNode array = parent.get(field);
        String path = prefix.isEmpty() ? field : prefix + "." + field;
        if (array == null || array.isNull()) return List.of();
        if (!array.isArray()) throw new BundleFormatException(path + " must be an array");

        List<T> parsed = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            JsonNode element = array.get(i);
            String at = path + "[" + i + "]";
            if (!element.isObject()) throw new BundleFormatException(at + " must be an object");
            try {
                parsed.add(parser.parse(element, at));
            } catch (BundleFormatException e) {
                throw e;
            } catch (IllegalArgumentException e) {
                // The domain records validate in their compact constructors. Their
                // messages are good; what they cannot know is which element failed.
                throw new BundleFormatException(at + ": " + e.getMessage(), e);
            }
        }
        return parsed;
    }

    /** Same walk, for arrays whose elements are collected into a map instead. */
    private static void forEach(JsonNode parent, String field, BiConsumer<JsonNode, String> sink, String prefix) {
        each(parent, field, (node, at) -> {
            sink.accept(node, at);
            return Boolean.TRUE;
        }, prefix);
    }
}
