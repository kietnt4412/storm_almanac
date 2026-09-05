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
import io.stormalmanac.gamedata.Drop;
import io.stormalmanac.gamedata.Fodder;
import io.stormalmanac.gamedata.Game;
import io.stormalmanac.gamedata.Item;
import io.stormalmanac.gamedata.ItemStack;
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
import io.stormalmanac.gamedata.catalog.Entity;
import io.stormalmanac.gamedata.catalog.Skill;
import io.stormalmanac.gamedata.catalog.StatCurve;
import io.stormalmanac.gamedata.catalog.Talent;
import java.io.IOException;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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
        sinks.addAll(each(root, "fodder", CanonicalBundleParser::fodder));

        return new GameDataBundle(
                new Game(
                        new GameId(text(game, "id", "game.id")),
                        text(game, "displayName", "game.displayName"),
                        text(game, "energyUnit", "game.energyUnit")),
                integer(root, "sequence", "sequence"),
                textOrEmpty(root, "label"),
                text(root, "attribution", "attribution"),
                each(root, "items", CanonicalBundleParser::item),
                sources,
                sinks,
                each(root, "banners", CanonicalBundleParser::banner),
                each(root, "entities", CanonicalBundleParser::entity));
    }

    // ── The domain shapes, one method each ──────────────────────────────────

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
                        number(d, "expectedYield", dAt + ".expectedYield")), at),
                availability(node, at));
    }

    private static Craft craft(JsonNode node, String at) {
        return new Craft(
                text(node, "id", at + ".id"),
                stacks(node, "consumes", at),
                stacks(node, "produces", at),
                availability(node, at));
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
            return Period.parse(iso);
        } catch (DateTimeParseException e) {
            throw new BundleFormatException(
                    at + ".period must be an ISO-8601 period such as \"P1D\", not \"" + iso + "\"", e);
        }
    }

    private static Reward reward(JsonNode node, String at) {
        String cadence = text(node, "cadence", at + ".cadence");
        try {
            return new Reward(
                    text(node, "id", at + ".id"),
                    Reward.Cadence.valueOf(cadence),
                    stacks(node, "grants", at),
                    availability(node, at));
        } catch (IllegalArgumentException e) {
            throw new BundleFormatException(
                    at + ".cadence must be one of " + List.of(Reward.Cadence.values()) + ", not \"" + cadence + "\"",
                    e);
        }
    }

    private static Upgrade upgrade(JsonNode node, String at) {
        return new Upgrade(
                text(node, "id", at + ".id"),
                new EntityId(text(node, "entity", at + ".entity")),
                text(node, "fromState", at + ".fromState"),
                text(node, "toState", at + ".toState"),
                stacks(node, "costs", at));
    }

    private static Fodder fodder(JsonNode node, String at) {
        return new Fodder(
                text(node, "id", at + ".id"),
                text(node, "consumesCategory", at + ".consumesCategory"),
                rarity(required(node, "minimumRarity", at + ".minimumRarity"), at + ".minimumRarity"),
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
                    availability(node, at));
        } catch (IllegalArgumentException e) {
            throw new BundleFormatException(at + ": " + e.getMessage(), e);
        }
    }

    private static PityRule pityRule(JsonNode node, String at) {
        JsonNode softFrom = node.get("softFrom");
        boolean soft = softFrom != null && !softFrom.isNull();
        return new PityRule(
                (int) integer(node, "hardAt", at + ".hardAt"),
                soft ? (int) integer(node, "softFrom", at + ".softFrom") : null,
                soft ? number(node, "softJumpTo", at + ".softJumpTo") : null,
                soft ? number(node, "softStep", at + ".softStep") : null);
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
