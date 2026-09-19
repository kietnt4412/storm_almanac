package io.stormalmanac.gamedata.ingest;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.stormalmanac.gamedata.Availability;
import io.stormalmanac.gamedata.Craft;
import io.stormalmanac.gamedata.Fodder;
import io.stormalmanac.gamedata.Item;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Provenance;
import io.stormalmanac.gamedata.Rarity;
import io.stormalmanac.gamedata.Reward;
import io.stormalmanac.gamedata.Shop;
import io.stormalmanac.gamedata.Sink;
import io.stormalmanac.gamedata.Source;
import io.stormalmanac.gamedata.Stage;
import io.stormalmanac.gamedata.Upgrade;
import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.gamedata.banner.PityRule;
import io.stormalmanac.gamedata.catalog.Entity;
import io.stormalmanac.gamedata.catalog.Skill;
import io.stormalmanac.gamedata.catalog.StatCurve;
import io.stormalmanac.gamedata.catalog.Talent;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Writes the canonical bundle format: {@link CanonicalBundleParser} read
 * backwards.
 *
 * <p>It exists because an {@link UpstreamAdapter} produces a
 * {@link GameDataBundle} in memory, and a human has to approve it. Handing the
 * ingest path an object would skip the step where somebody can read the thing,
 * diff it against the last one, and keep the exact file that was approved. So
 * an adapter's output is written out as canonical JSON and then goes through
 * the same preview, ingest, publish loop a hand-written bundle does — one
 * ingest path, not two, and an adapter gets no privileges for being code.
 *
 * <p>The pairing is load-bearing and is tested as such: for every bundle,
 * parsing what this writes must give the bundle back. A field the writer
 * forgets is a field no adapter can ever supply, and it would go missing in
 * silence — the parser would simply see an absent optional.
 */
public final class CanonicalBundleWriter {

    private final ObjectMapper json;

    public CanonicalBundleWriter() {
        this(new ObjectMapper());
    }

    public CanonicalBundleWriter(ObjectMapper json) {
        this.json = json;
    }

    /** The bundle as canonical JSON, indented, because a person reads it. */
    public String write(GameDataBundle bundle) {
        try {
            return json.writerWithDefaultPrettyPrinter().writeValueAsString(tree(bundle))
                    + System.lineSeparator();
        } catch (JacksonException e) {
            throw new BundleFormatException("bundle could not be written: " + e.getOriginalMessage(), e);
        }
    }

    private ObjectNode tree(GameDataBundle bundle) {
        ObjectNode root = json.createObjectNode();

        ObjectNode game = root.putObject("game");
        game.put("id", bundle.game().id().value());
        game.put("displayName", bundle.game().displayName());
        game.put("energyUnit", bundle.game().energyUnit());

        root.put("sequence", bundle.sequence());
        root.put("label", bundle.label());
        root.put("attribution", bundle.attribution());

        array(root, "provenance", bundle.provenance(), this::provenance);
        root.put("sourcedBy", bundle.sourcedBy());
        // Sorted, not in map order. The overrides are the part of a bundle a
        // reviewer reads line by line, and this file is diffed against the last
        // one — an override list whose order tracked insertion would put noise
        // in every patch diff. The parser reads it back into an unordered map,
        // so nothing downstream can tell the difference.
        if (!bundle.factProvenance().isEmpty()) {
            ObjectNode overrides = root.putObject("factProvenance");
            bundle.factProvenance().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> overrides.put(entry.getKey(), entry.getValue()));
        }

        array(root, "items", bundle.items(), this::item);
        array(root, "entities", bundle.entities(), this::entity);
        array(root, "stages", sourcesOf(bundle.sources(), Stage.class), this::stage);
        array(root, "crafts", sourcesOf(bundle.sources(), Craft.class), this::craft);
        array(root, "shops", sourcesOf(bundle.sources(), Shop.class), this::shop);
        array(root, "rewards", sourcesOf(bundle.sources(), Reward.class), this::reward);
        array(root, "upgrades", sinksOf(bundle.sinks(), Upgrade.class), this::upgrade);
        array(root, "fodder", sinksOf(bundle.sinks(), Fodder.class), this::fodder);
        array(root, "banners", bundle.banners(), this::banner);
        return root;
    }

    // ── One method per shape, mirroring the parser ──────────────────────────

    private void provenance(ObjectNode node, Provenance provenance) {
        node.put("id", provenance.id());
        node.put("origin", provenance.origin().name());
        node.put("detail", provenance.detail());
        node.put("observedOn", provenance.observedOn().toString());
    }

    private void item(ObjectNode node, Item item) {
        node.put("id", item.id().value());
        node.put("displayName", item.displayName());
        rarity(node.putObject("rarity"), item.rarity());
        node.put("category", item.category());
    }

    private void entity(ObjectNode node, Entity entity) {
        node.put("id", entity.id().value());
        node.put("displayName", entity.displayName());
        node.put("kind", entity.kind());
        rarity(node.putObject("rarity"), entity.rarity());
        node.put("element", entity.element());
        strings(node, "tags", entity.tags());
        array(node, "statCurves", entity.statCurves(), this::statCurve);
        array(node, "skills", entity.skills(), this::skill);
        array(node, "talents", entity.talents(), this::talent);
    }

    private void statCurve(ObjectNode node, StatCurve curve) {
        node.put("stat", curve.stat());
        array(node, "breakpoints", curve.breakpoints(), (b, point) -> {
            b.put("ascensionTier", point.ascensionTier());
            b.put("level", point.level());
            b.put("value", point.value());
        });
    }

    private void skill(ObjectNode node, Skill skill) {
        node.put("id", skill.id());
        node.put("displayName", skill.displayName());
        array(node, "ranks", skill.ranks(), (r, rank) -> {
            r.put("rank", rank.rank());
            r.put("description", rank.description());
            ObjectNode values = r.putObject("values");
            rank.values().forEach(values::put);
            stacks(r, "upgradeCost", rank.upgradeCost());
        });
    }

    private void talent(ObjectNode node, Talent talent) {
        node.put("id", talent.id());
        node.put("displayName", talent.displayName());
        node.put("unlockCondition", talent.unlockCondition());
        node.put("effect", talent.effect());
    }

    private void stage(ObjectNode node, Stage stage) {
        node.put("id", stage.stageId().value());
        node.put("displayName", stage.displayName());
        node.put("energyCost", stage.energyCost());
        array(node, "drops", stage.drops(), (d, drop) -> {
            d.put("item", drop.item().value());
            d.put("expectedYield", drop.expectedYield());
            // Written only when there is one, because absence is the canonical
            // spelling of "declared, not measured" — see Drop. Writing a zero
            // would say something false and would also churn every bundle
            // converted before this field existed.
            if (drop.isSampled()) d.put("sampledRuns", drop.sampledRuns());
        });
        availability(node, stage.availability());
    }

    private void craft(ObjectNode node, Craft craft) {
        node.put("id", craft.id());
        stacks(node, "consumes", craft.consumes());
        stacks(node, "produces", craft.produces());
        availability(node, craft.availability());
    }

    private void shop(ObjectNode node, Shop shop) {
        node.put("id", shop.id());
        node.put("currency", shop.currency().value());
        node.put("price", shop.price());
        stack(node.putObject("offer"), shop.offer());
        node.put("periodLimit", shop.periodLimit());
        node.put("period", shop.period().toString());
        availability(node, shop.availability());
    }

    private void reward(ObjectNode node, Reward reward) {
        node.put("id", reward.id());
        node.put("cadence", reward.cadence().name());
        stacks(node, "grants", reward.grants());
        availability(node, reward.availability());
    }

    private void upgrade(ObjectNode node, Upgrade upgrade) {
        node.put("id", upgrade.id());
        node.put("entity", upgrade.entity().value());
        node.put("fromState", upgrade.fromState());
        node.put("toState", upgrade.toState());
        stacks(node, "costs", upgrade.costs());
        strings(node, "requires", upgrade.requires());
        array(node, "progress", upgrade.progress(), (p, progress) -> {
            p.put("kind", progress.kind());
            p.put("quantity", progress.quantity());
        });
    }

    private void fodder(ObjectNode node, Fodder fodder) {
        node.put("id", fodder.id());
        node.put("consumesCategory", fodder.consumesCategory());
        rarity(node.putObject("minimumRarity"), fodder.minimumRarity());
        if (fodder.progress() != null) node.put("progress", fodder.progress());
        node.put("progressPerUnit", fodder.progressPerUnit());
        stacks(node, "costs", fodder.costs());
    }

    private void banner(ObjectNode node, BannerModel banner) {
        node.put("id", banner.id().value());
        node.put("displayName", banner.displayName());
        node.put("bannerType", banner.bannerType());
        node.put("pityScope", banner.pityScope().name());

        ArrayNode rates = node.putArray("baseRates");
        for (Map.Entry<Rarity, Double> entry : banner.baseRates().entrySet()) {
            ObjectNode rate = rates.addObject();
            rarity(rate.putObject("rarity"), entry.getKey());
            rate.put("rate", entry.getValue());
        }

        ArrayNode rules = node.putArray("pityRules");
        for (Map.Entry<Rarity, PityRule> entry : banner.pityRules().entrySet()) {
            ObjectNode rule = rules.addObject();
            rarity(rule.putObject("rarity"), entry.getKey());
            PityRule pity = entry.getValue();
            rule.put("hardAt", pity.hardAt());
            // Written only when it is there: the parser reads a missing softFrom
            // as "this rarity has no soft pity", and a null triple says the same
            // thing more noisily.
            if (pity.softFrom() != null) {
                rule.put("softFrom", pity.softFrom());
                rule.put("softJumpTo", pity.softJumpTo());
                rule.put("softStep", pity.softStep());
            }
        }

        array(node, "floors", banner.floors(), (f, floor) -> {
            f.put("everyN", floor.everyN());
            rarity(f.putObject("minimumRarity"), floor.minimumRarity());
        });

        ObjectNode featured = node.putObject("featured");
        featured.put("chanceAtHit", banner.featuredRule().chanceAtHit());
        featured.put("guaranteeAfterLoss", banner.featuredRule().guaranteeAfterLoss());

        availability(node, banner.window());
    }

    // ── Plumbing ────────────────────────────────────────────────────────────

    private void rarity(ObjectNode node, Rarity rarity) {
        node.put("label", rarity.label());
        node.put("rank", rarity.rank());
    }

    private void stack(ObjectNode node, ItemStack stack) {
        node.put("item", stack.item().value());
        node.put("quantity", stack.quantity());
    }

    private void stacks(ObjectNode parent, String field, List<ItemStack> stacks) {
        array(parent, field, stacks, this::stack);
    }

    /**
     * {@link Availability#ALWAYS} is written as nothing at all, matching the
     * parser's reading of an absent object. Almost every source is always open,
     * and spelling that out on each one would bury the few that rotate or
     * expire.
     */
    private void availability(ObjectNode node, Availability availability) {
        if (availability == null || Availability.ALWAYS.equals(availability)) return;

        ObjectNode window = node.putObject("availability");
        if (!availability.days().isEmpty()) {
            ArrayNode days = window.putArray("days");
            // Calendar order rather than iteration order, so two bundles naming
            // the same days produce the same bytes.
            availability.days().stream().sorted(DayOfWeek::compareTo).forEach(day -> days.add(day.name()));
        }
        if (availability.opensAt() != null) window.put("opensAt", availability.opensAt().toString());
        if (availability.closesAt() != null) window.put("closesAt", availability.closesAt().toString());
    }

    private static <T extends Source> List<T> sourcesOf(List<Source> sources, Class<T> kind) {
        return sources.stream().filter(kind::isInstance).map(kind::cast).toList();
    }

    private static <T extends Sink> List<T> sinksOf(List<Sink> sinks, Class<T> kind) {
        return sinks.stream().filter(kind::isInstance).map(kind::cast).toList();
    }

    /** An empty section is omitted rather than written as an empty array. */
    private <T> void array(ObjectNode parent, String field, List<T> values, BiConsumer<ObjectNode, T> write) {
        if (values.isEmpty()) return;
        ArrayNode array = parent.putArray(field);
        values.forEach(value -> write.accept(array.addObject(), value));
    }

    private static void strings(ObjectNode parent, String field, List<String> values) {
        if (values.isEmpty()) return;
        ArrayNode array = parent.putArray(field);
        values.forEach(array::add);
    }
}
