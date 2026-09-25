package io.stormalmanac.gamedata.diff;

import io.stormalmanac.gamedata.Availability;
import io.stormalmanac.gamedata.Craft;
import io.stormalmanac.gamedata.DayBoundary;
import io.stormalmanac.gamedata.Drop;
import io.stormalmanac.gamedata.Fodder;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.Item;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.ProgressKind;
import io.stormalmanac.gamedata.Rarity;
import io.stormalmanac.gamedata.Reward;
import io.stormalmanac.gamedata.Shop;
import io.stormalmanac.gamedata.Sink;
import io.stormalmanac.gamedata.Source;
import io.stormalmanac.gamedata.Stage;
import io.stormalmanac.gamedata.Upgrade;
import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.gamedata.banner.PullPrice;
import io.stormalmanac.gamedata.catalog.Entity;
import io.stormalmanac.gamedata.catalog.Skill;
import io.stormalmanac.gamedata.catalog.StatCurve;
import io.stormalmanac.gamedata.catalog.Talent;
import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Flattens a {@link GameDefinition} into subjects and the facts about them.
 *
 * <p>This is what makes the diff a set comparison rather than a bespoke walk of
 * twenty record types, and it is why a version is stored as a full snapshot in
 * the first place. Adding a field to the model means adding one line here, and
 * forgetting to is the failure mode to watch for — a field nobody flattens is a
 * field the patch report will never mention.
 *
 * <p>Values are strings because the diff is read rather than computed against.
 * Collections that carry no meaningful order — a stage's drops, an upgrade's
 * costs — become one fact per element keyed by the element's own slug, so that
 * reordering a bundle produces no changes and moving a single number produces
 * exactly one.
 */
final class Facts {

    private Facts() {}

    /** A thing a patch note would name: {@code stage 'pg-1-1'}. */
    record Subject(Axis axis, String label) implements Comparable<Subject> {
        @Override
        public int compareTo(Subject other) {
            int byAxis = axis.compareTo(other.axis);
            return byAxis != 0 ? byAxis : label.compareTo(other.label);
        }
    }

    static Map<Subject, Map<String, String>> of(GameDefinition definition) {
        Map<Subject, Map<String, String>> facts = new LinkedHashMap<>();

        // The title itself, which for a long time nothing flattened. That was
        // harmless while the only field was the energy unit, and stopped being
        // harmless the moment a game could say when its day rolls over: a
        // correction to that moves every rotating stage's capacity, and a patch
        // report that could not mention it would show a sequence as "no changes"
        // while the plans under it moved. One subject, filed under progression
        // because the day boundary is a farming fact.
        Map<String, String> game = subject(
                facts, Axis.PROGRESSION, "game", definition.game().id().value());
        game.put("energy unit", definition.game().energyUnit());
        game.put("day rollover", dayBoundary(definition.game().dayBoundary()));

        // Named pools, which are not facts and declare no provenance, and are
        // flattened anyway. A sequence that renamed "Character EXP" to something
        // else would move what every shortfall table says, and a patch report
        // that could not mention it would be the same silence ADR 0025 closed
        // for the day boundary. One subject per kind, so a rename is one change
        // rather than a field on a subject nobody would look under.
        for (ProgressKind kind : definition.progressKinds()) {
            subject(facts, Axis.PROGRESSION, "progress", kind.kind()).put("name", kind.displayName());
        }

        // The order sections are shown in, which is not a fact either and moves
        // every page that groups by it (ADR 0032). One subject for the whole
        // order, so moving a heading is one change that says where it went.
        if (!definition.sections().isEmpty()) {
            subject(facts, Axis.PROGRESSION, "sections", "order")
                    .put("order", String.join(" · ", definition.sections()));
        }

        for (Item item : definition.items()) {
            Map<String, String> about = subject(facts, Axis.PROGRESSION, "item", item.id().value());
            about.put("name", item.displayName());
            about.put("rarity", rarity(item.rarity()));
            about.put("category", item.category());
        }

        for (Source source : definition.sources()) {
            switch (source) {
                case Stage stage -> {
                    Map<String, String> about = subject(facts, Axis.PROGRESSION, "stage", stage.id());
                    about.put("name", stage.displayName());
                    about.put("energy", String.valueOf(stage.energyCost()));
                    about.put("availability", availability(stage.availability()));
                    stage.drops().forEach(drop ->
                            about.put("drop " + drop.item().value(), sampledYield(drop)));
                }
                case Craft craft -> {
                    Map<String, String> about = subject(facts, Axis.PROGRESSION, "craft", craft.id());
                    about.put("availability", availability(craft.availability()));
                    stacks(about, "consumes", craft.consumes());
                    stacks(about, "produces", craft.produces());
                }
                case Shop shop -> {
                    Map<String, String> about = subject(facts, Axis.PROGRESSION, "shop", shop.id());
                    about.put("currency", shop.currency().value());
                    about.put("price", String.valueOf(shop.price()));
                    about.put("offer", shop.offer().item().value() + " x" + shop.offer().quantity());
                    about.put("limit", shop.neverResets()
                            ? shop.periodLimit() + " ever"
                            : shop.periodLimit() + " per " + shop.period());
                    about.put("availability", availability(shop.availability()));
                }
                case Reward reward -> {
                    Map<String, String> about = subject(facts, Axis.PROGRESSION, "reward", reward.id());
                    about.put("cadence", reward.cadence().name());
                    about.put("availability", availability(reward.availability()));
                    stacks(about, "grants", reward.grants());
                    if (reward.requires() != null) {
                        about.put("requires", reward.requires().measure()
                                + " >= " + reward.requires().atLeast());
                    }
                }
            }
        }

        for (Sink sink : definition.sinks()) {
            switch (sink) {
                case Upgrade upgrade -> {
                    Map<String, String> about = subject(facts, Axis.PROGRESSION, "upgrade", upgrade.id());
                    about.put("entity", upgrade.entity().value());
                    about.put("edge", upgrade.fromState() + " → " + upgrade.toState());
                    stacks(about, "cost", upgrade.costs());
                    upgrade.requires().forEach(state -> about.put("requires " + state, "yes"));
                    upgrade.progress().forEach(p -> about.put("progress " + p.kind(), String.valueOf(p.quantity())));
                    // What the game calls it: part of the step, so a rename is a
                    // change on the step (ADR 0032). Absent fields are left out,
                    // so every step published before sequence 11 flattens as it did.
                    Upgrade.Labels labels = upgrade.labels();
                    if (labels.fromName() != null) about.put("from name", labels.fromName());
                    if (labels.toName() != null) about.put("to name", labels.toName());
                    if (labels.section() != null) about.put("section", labels.section());
                    if (labels.tag() != null) about.put("tag", labels.tag());
                }
                case Fodder fodder -> {
                    Map<String, String> about = subject(facts, Axis.PROGRESSION, "fodder", fodder.id());
                    about.put("consumes", fodder.consumesCategory());
                    about.put("minimum rarity", rarity(fodder.minimumRarity()));
                    if (fodder.progress() != null) about.put("feeds", fodder.progress());
                    about.put("progress per unit", String.valueOf(fodder.progressPerUnit()));
                    stacks(about, "cost", fodder.costs());
                }
            }
        }

        for (Entity entity : definition.entities()) {
            Map<String, String> about = subject(facts, Axis.CATALOG, "entity", entity.id().value());
            about.put("name", entity.displayName());
            about.put("kind", entity.kind());
            about.put("rarity", rarity(entity.rarity()));
            about.put("element", entity.element());
            about.put("tags", String.join(", ", entity.tags()));

            for (StatCurve curve : entity.statCurves()) {
                curve.breakpoints().forEach(point -> about.put(
                        curve.stat() + " at tier " + point.ascensionTier() + " level " + point.level(),
                        String.valueOf(point.value())));
            }
            for (Skill skill : entity.skills()) {
                about.put("skill " + skill.id(), skill.displayName());
                for (Skill.Rank rank : skill.ranks()) {
                    String at = "skill " + skill.id() + " rank " + rank.rank();
                    about.put(at + " text", rank.description());
                    // One fact per named multiplier: the patch note has to be able
                    // to say "damage moved" without claiming "cost" moved too.
                    rank.values().forEach((key, value) -> about.put(at + " " + key, String.valueOf(value)));
                    stacks(about, at + " cost", rank.upgradeCost());
                }
            }
            for (Talent talent : entity.talents()) {
                about.put("talent " + talent.id(), talent.displayName());
                about.put("talent " + talent.id() + " unlock", talent.unlockCondition());
                about.put("talent " + talent.id() + " effect", talent.effect());
            }
        }

        for (BannerModel banner : definition.banners()) {
            Map<String, String> about = subject(facts, Axis.GACHA, "banner", banner.id().value());
            about.put("name", banner.displayName());
            about.put("type", banner.bannerType());
            about.put("pity scope", banner.pityScope().name());
            about.put("featured chance", String.valueOf(banner.featuredRule().chanceAtHit()));
            about.put("featured guarantee", String.valueOf(banner.featuredRule().guaranteeAfterLoss()));
            about.put("availability", availability(banner.window()));
            about.put("pull price", pullPrice(banner.pullPrice()));

            banner.baseRates().forEach((rarity, rate) ->
                    about.put("base rate " + rarity.label(), String.valueOf(rate)));
            banner.pityRules().forEach((rarity, rule) -> {
                about.put("pity " + rarity.label() + " hard at", String.valueOf(rule.hardAt()));
                about.put("pity " + rarity.label() + " soft from", String.valueOf(rule.softFrom()));
                about.put("pity " + rarity.label() + " soft jump", String.valueOf(rule.softJumpTo()));
                about.put("pity " + rarity.label() + " soft step", String.valueOf(rule.softStep()));
                // Without this a patch that turned a fixed wall into a drawn one
                // would report "hard at 80 -> 100" and call it a nerf, which is
                // the opposite of what happened.
                about.put("pity " + rarity.label() + " drawn from", String.valueOf(rule.drawnFrom()));
            });
            banner.floors().forEach(floor -> about.put(
                    "floor every " + floor.everyN(), rarity(floor.minimumRarity())));
        }

        return facts;
    }

    private static Map<String, String> subject(
            Map<Subject, Map<String, String>> facts, Axis axis, String kind, String slug) {
        // TreeMap: a rendered report lists a subject's changed fields in the same
        // order every time, whatever order the model happened to yield them in.
        return facts.computeIfAbsent(new Subject(axis, kind + " '" + slug + "'"), key -> new TreeMap<>());
    }

    private static void stacks(Map<String, String> about, String prefix, List<ItemStack> stacks) {
        stacks.forEach(stack -> about.put(prefix + " " + stack.item().value(), String.valueOf(stack.quantity())));
    }

    private static String rarity(Rarity rarity) {
        return rarity.label() + " (" + rarity.rank() + ")";
    }

    /**
     * A drop reads as its yield and, when there is one, the sample behind it.
     *
     * <p>One fact rather than two, because a resample changes both together and
     * a reader approving a publish wants to see <em>"0.21 over 105 runs →
     * 0.16 over 2 680 runs"</em> on one line. Splitting them would report the
     * same event twice and would let a diff show a rate holding steady while the
     * evidence for it collapsed.
     */
    private static String sampledYield(Drop drop) {
        return drop.isSampled()
                ? drop.expectedYield() + " over " + drop.sampledRuns() + " runs"
                : String.valueOf(drop.expectedYield());
    }

    /**
     * A rollover reads as a clock time, and an undeclared one says so.
     *
     * <p>"unstated" rather than "00:00 UTC": a reader approving the version that
     * first declares a boundary should see the reading arrive, not a value that
     * appears to have changed from one somebody chose.
     */
    private static String dayBoundary(DayBoundary boundary) {
        return boundary == null
                ? "unstated"
                : String.format("%02d:00 %s", boundary.hour(), boundary.zone().getId());
    }

    /**
     * A price reads as a count and a currency, and an undeclared one says so.
     *
     * <p>One fact rather than two, for the same reason a sampled yield is one:
     * a re-reading moves the number and the currency together — a pool that
     * changed its ticket changed its price — and a reader approving a publish
     * wants <em>"unstated → 250 × event-construct-rd-ticket"</em> on one line.
     */
    private static String pullPrice(PullPrice price) {
        return price == null
                ? "unstated"
                : price.perPull() + " × " + price.currency().value();
    }

    private static String availability(Availability availability) {
        String days = availability.days().isEmpty()
                ? "every day"
                : availability.days().stream()
                        .sorted(Comparator.naturalOrder())
                        .map(DayOfWeek::name)
                        .collect(Collectors.joining(", "));
        String opens = availability.opensAt() == null ? "always" : availability.opensAt().toString();
        String closes = availability.closesAt() == null ? "no end" : availability.closesAt().toString();
        return days + "; " + opens + " to " + closes;
    }
}
