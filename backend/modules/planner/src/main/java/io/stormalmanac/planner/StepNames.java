package io.stormalmanac.planner;

import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.StageId;
import io.stormalmanac.gamedata.Fodder;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.Item;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Reward;
import io.stormalmanac.gamedata.Stage;
import io.stormalmanac.gamedata.Upgrade;
import io.stormalmanac.gamedata.Word;
import io.stormalmanac.gamedata.catalog.Entity;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * What to call a line of a plan: a stage to run, a thing to craft, buy, feed or
 * pay, a grant to claim, a step being paid for, a line of demand.
 *
 * <p>Each of these is reported by id, and until 2026-09-26 the plan page printed
 * the id — {@code simulation-shop-weapon-enhancer-iv} where a player would say
 * "buy ten Weapon Enhancer IV". D5's rehearsal named it before a stranger
 * could.
 *
 * <p><b>Every word here is a fact already, never a new one.</b> A stage, an
 * item and an entity have names read off the game, and since ADR 0032 so do
 * some states. A shop row, a craft, a fodder rule and a price have none, and
 * giving them one would be a new word read off a screen, with its own
 * provenance. So they are named by what they do, in the quantities and item
 * names the bundle has already published: "Buy 10 Weapon Enhancer IV for 262
 * Simulation Score" says more than any label would, and cannot disagree with the
 * numbers beside it. A state with no word of the game's keeps its id, rather
 * than one guessed from it; guessing is the page's business ({@code tracks.ts}),
 * and the page says it is guessing.
 *
 * <p>A grant behind a score is named by its bar, and by the thing scored when
 * the bundle has a word for the measure (ADR 0033, sequence 13): "Phantom Pain
 * Cage, weekly, score 90,000+". Without one it is the bar alone, since the
 * measure is an opaque label (ADR 0022) and printing it would print a slug.
 *
 * <p>An id this version does not know falls back to itself, so a line can be
 * ugly and never missing.
 */
public final class StepNames {

    private final GameDefinition definition;
    private final Map<ItemId, Item> items;
    private final Map<EntityId, Entity> entities;

    private StepNames(GameDefinition definition) {
        this.definition = definition;
        this.items = definition.itemsById();
        this.entities = definition.entitiesById();
    }

    public static StepNames of(GameDefinition definition) {
        return new StepNames(definition);
    }

    public String stage(StageId id) {
        return definition.stages().stream()
                .filter(stage -> stage.stageId().equals(id))
                .map(Stage::displayName)
                .findFirst()
                .orElse(id.value());
    }

    /**
     * A {@link Conversion}'s instruction. Its id is a shop row's, a craft's, a
     * price's (one of a step's several, ADR 0021), or a fodder rule's — with
     * {@code :<item>} after it when the rule takes more than one item, since
     * each is a separate thing to do.
     */
    public String step(String id) {
        return shop(id, 1).or(() -> craft(id, 1)).or(() -> price(id, 1)).or(() -> fodder(id, null)).orElse(id);
    }

    /**
     * The same instruction done {@code times} over, as the totals a reader acts
     * on: "Buy 514,800 Cogs for 429 Simulation Score" where {@link #step} says
     * "Buy 1,200 Cogs for 1 Simulation Score" and leaves the multiplying to
     * whoever reads it. D5's third run found a reader who read that line as one
     * cheap buy (S9). A feed says how many it feeds, which is the count itself.
     */
    public String step(String id, int times) {
        return shop(id, times).or(() -> craft(id, times)).or(() -> price(id, times))
                .or(() -> fodder(id, times)).orElse(id);
    }

    /**
     * How a purchase done many times is done, "429 × 1,200 for 1" — the number
     * of buys a reader makes in the shop, which {@link #step(String, int)}'s
     * totals no longer show. Empty for anything but a shop row bought more than
     * once: a box opened, an item fed and a price paid carry their count in the
     * total already, and a single buy has nothing to repeat.
     */
    public Optional<String> repeat(String id, int times) {
        if (times <= 1) return Optional.empty();
        return definition.shops().stream()
                .filter(shop -> shop.id().equals(id))
                .findFirst()
                .map(shop -> quantity(times) + " × " + quantity(shop.offer().quantity())
                        + (shop.price() == 0 ? "" : " for " + quantity(shop.price())));
    }

    /**
     * The order a reader works through {@link #step}s in: buy, then open what
     * was bought, then feed it, then pay. Id order was the order before names,
     * and read as no order at all once the names showed.
     */
    public Comparator<String> stepOrder() {
        return Comparator.comparingInt(this::kindOf)
                .thenComparing((String id) -> step(id))
                .thenComparing(Function.identity());
    }

    /** A grant and everything it pays: "Phantom Pain Cage, weekly, score 90,000+: 5 Phantom Pain Scar". */
    public String reward(String id) {
        return rewardById(id).map(reward -> when(reward) + ": " + stacks(reward.grants())).orElse(id);
    }

    /**
     * A grant said briefly, for a sentence that lists many: its bar when it has
     * one, which is what tells two tiers of one ladder apart, and what it pays
     * when it has none.
     */
    public String rewardBrief(String id) {
        return rewardById(id)
                .map(reward -> reward.requires() == null ? reward(id) : when(reward))
                .orElse(id);
    }

    /** By cadence, then by bar, so a ladder reads from its bottom rung up. */
    public Comparator<String> rewardOrder() {
        Comparator<Optional<Reward>> byReward = Comparator.comparing(
                (Optional<Reward> reward) -> reward.map(Reward::cadence).orElse(null),
                Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(reward -> reward
                        .map(r -> r.requires() == null ? 0 : r.requires().atLeast())
                        .orElse(Integer.MAX_VALUE));
        return Comparator.comparing(this::rewardById, byReward).thenComparing(Function.identity());
    }

    /**
     * A step a goal is paying for, as {@link Demand#steps()} lists it — one id,
     * or a step's several prices joined by {@code " or "}, which all reach the
     * same state and so are the same step to a reader: "Samantha to
     * upper-resonance-1". Which price is paid is the plan's "Pay …" line.
     */
    public String upgrade(String entry) {
        return upgradeOf(entry)
                .map(step -> entity(step.entity()) + " to " + state(step.entity(), step.toState()))
                .orElse(entry);
    }

    /**
     * The step an entry of {@link Demand#steps()} stands for — its first price
     * when it lists several, since they share both states — or empty when this
     * version has no such step.
     */
    public Optional<Upgrade> upgradeOf(String entry) {
        String first = entry.split(" or ", 2)[0];
        return definition.sinks().stream()
                .filter(sink -> sink instanceof Upgrade upgrade && upgrade.id().equals(first))
                .map(Upgrade.class::cast)
                .findFirst();
    }

    /** Where a goal points: "Helentine: Lacrimosa at Hero". */
    public String goal(EntityId entity, String state) {
        return entity(entity) + " at " + state(entity, state);
    }

    /**
     * A line of demand. One of three things, and only one of them has a name
     * in the item table: a catalog item; a progress kind such as EXP, named by
     * the bundle (ADR 0028); or <b>one step offered at several prices</b> (ADR
     * 0021), named by its prices — "one of: 150 5★ Memory Shard · 234 Special
     * Support Token" — which are what the reader is choosing between, and are
     * facts already where a name for each upgrade would be a new one.
     */
    public String demand(ItemId id) {
        if (Demand.isChoiceItem(id)) {
            List<String> prices = definition.sinks().stream()
                    .filter(sink -> sink instanceof Upgrade upgrade && Demand.choiceItem(upgrade).equals(id))
                    .map(sink -> price((Upgrade) sink))
                    .toList();
            return prices.isEmpty() ? id.value() : "one of: " + String.join(" · ", prices);
        }
        if (Demand.isProgressItem(id)) {
            return definition.nameOfProgress(Demand.progressKind(id));
        }
        return item(id);
    }

    /**
     * One price, every part of it: "10 Cogs + 18,000 Memory EXP". A gate is not
     * a price and is left out; the step's other prices share it.
     */
    public String price(Upgrade price) {
        return price(price, 1);
    }

    private String price(Upgrade price, int times) {
        return Stream.concat(
                        price.costs().stream().map(cost -> stack(cost, times)),
                        price.progress().stream()
                                .map(p -> quantity((long) p.quantity() * times) + " "
                                        + definition.nameOfProgress(p.kind())))
                .reduce((a, b) -> a + " + " + b)
                .orElse(price.id());
    }

    private int kindOf(String id) {
        if (shop(id, 1).isPresent()) return 0;
        if (craft(id, 1).isPresent()) return 1;
        if (fodder(id, null).isPresent()) return 2;
        if (price(id, 1).isPresent()) return 3;
        return 4;
    }

    private Optional<String> shop(String id, int times) {
        return definition.shops().stream()
                .filter(shop -> shop.id().equals(id))
                .findFirst()
                .map(shop -> "Buy " + stack(shop.offer(), times)
                        + (shop.price() == 0
                                ? ", free"
                                : " for " + stack(new ItemStack(shop.currency(), shop.price()), times)));
    }

    private Optional<String> craft(String id, int times) {
        return definition.crafts().stream()
                .filter(craft -> craft.id().equals(id))
                .findFirst()
                .map(craft -> "Open " + stacks(craft.consumes(), times) + " → " + stacks(craft.produces(), times));
    }

    private Optional<String> price(String id, int times) {
        return definition.sinks().stream()
                .filter(sink -> sink instanceof Upgrade upgrade && upgrade.id().equals(id))
                .findFirst()
                .map(sink -> "Pay " + price((Upgrade) sink, times));
    }

    /** @param count how many are fed, or null for the instruction without one */
    private Optional<String> fodder(String id, Integer count) {
        int split = id.indexOf(':');
        String ruleId = split < 0 ? id : id.substring(0, split);
        return definition.sinks().stream()
                .filter(sink -> sink instanceof Fodder rule && rule.id().equals(ruleId) && rule.progress() != null)
                .map(Fodder.class::cast)
                .findFirst()
                .flatMap(rule -> fed(rule, split < 0 ? null : new ItemId(id.substring(split + 1)))
                        .map(item -> "Feed " + (count == null ? "" : quantity(count) + " ") + item
                                + " into " + definition.nameOfProgress(rule.progress())
                                + (rule.costs().isEmpty() ? ""
                                        : ", with " + stacks(rule.costs(), count == null ? 1 : count))));
    }

    /** The item a fodder line feeds: named in its id, or the rule's only eligible one. */
    private Optional<String> fed(Fodder rule, ItemId named) {
        if (named != null) return Optional.of(item(named));
        List<Item> eligible = definition.items().stream()
                .filter(item -> item.category().equals(rule.consumesCategory()))
                .filter(item -> item.rarity().rank() >= rule.minimumRarity().rank())
                .toList();
        return eligible.size() == 1 ? Optional.of(eligible.get(0).displayName()) : Optional.empty();
    }

    private Optional<Reward> rewardById(String id) {
        return definition.rewards().stream().filter(reward -> reward.id().equals(id)).findFirst();
    }

    private String when(Reward reward) {
        if (reward.requires() == null) return cadence(reward.cadence());
        String measure = reward.requires().measure();
        String named = definition.nameOf(Word.Subject.MEASURE, measure);
        String bar = "score " + quantity(reward.requires().atLeast()) + "+";
        return named.equals(measure)
                ? cadence(reward.cadence()) + ", " + bar
                : named + ", " + cadence(reward.cadence()).toLowerCase(Locale.ROOT) + ", " + bar;
    }

    /** What a scored mode is called, or its measure when the bundle gives it no word. */
    public String measure(String measure) {
        return definition.nameOf(Word.Subject.MEASURE, measure);
    }

    private static String cadence(Reward.Cadence cadence) {
        return switch (cadence) {
            case DAILY -> "Daily";
            case WEEKLY -> "Weekly";
            case MONTHLY -> "Monthly";
            case EVENT -> "Event";
            case ONE_OFF -> "Once";
        };
    }

    /** What the catalog calls an entity, or its id when this version does not know it. */
    public String entity(EntityId id) {
        Entity known = entities.get(id);
        return known == null ? id.value() : known.displayName();
    }

    /** The game's word for a state if any step carries one (ADR 0032), else the state itself. */
    private String state(EntityId entity, String state) {
        return definition.sinks().stream()
                .filter(sink -> sink instanceof Upgrade upgrade && upgrade.entity().equals(entity))
                .map(Upgrade.class::cast)
                .map(step -> step.toState().equals(state) ? step.labels().toName()
                        : step.fromState().equals(state) ? step.labels().fromName()
                        : null)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(state);
    }

    private String stacks(List<ItemStack> stacks) {
        return stacks(stacks, 1);
    }

    private String stacks(List<ItemStack> stacks, int times) {
        return stacks.stream().map(stack -> stack(stack, times)).collect(Collectors.joining(" + "));
    }

    private String stack(ItemStack stack) {
        return stack(stack, 1);
    }

    /** A stack taken {@code times} over, in a long: 1,200 Cogs bought 2 million times is not an int. */
    private String stack(ItemStack stack, int times) {
        return quantity((long) stack.quantity() * times) + " " + item(stack.item());
    }

    private String item(ItemId id) {
        Item known = items.get(id);
        return known == null ? id.value() : known.displayName();
    }

    /** Grouped, and the same on every server whatever its locale. */
    public static String quantity(long quantity) {
        return String.format(Locale.ROOT, "%,d", quantity);
    }
}
