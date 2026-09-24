package io.stormalmanac.api.player;

import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.Item;
import io.stormalmanac.gamedata.Upgrade;
import io.stormalmanac.planner.Demand;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * What to call a line of demand on a page.
 *
 * <p>A demand line is one of three things, and only one of them has a name in
 * the item table: a catalog item; a progress kind such as EXP, named by the
 * bundle (ADR 0028); or <b>one step offered at several prices</b> (ADR 0021),
 * which until 2026-09-24 went out as {@code one of: <upgrade id>, <upgrade id>}.
 * Q6 made fixing that a condition of launch.
 *
 * <p><b>A choice is named by its prices, not by a name for each upgrade.</b> An
 * upgrade has no display name, and giving it one would mean a new word read off
 * the game, so a new fact with provenance. The prices are facts already, each
 * with its provenance, and they are what the reader is choosing between:
 * "150 5★ Memory Shard · 234 Special Support Token" says more than any label
 * would.
 *
 * <p>One class for the character page's shortfall and the plan's shadow prices.
 * Each used to name lines on its own, and both fell back to the raw id for a
 * choice.
 */
final class DemandNames {

    private DemandNames() {}

    static String of(GameDefinition definition, Map<ItemId, Item> items, ItemId id) {
        if (Demand.isChoiceItem(id)) {
            List<String> prices = definition.sinks().stream()
                    .filter(sink -> sink instanceof Upgrade upgrade && Demand.choiceItem(upgrade).equals(id))
                    .map(sink -> priceOf(definition, items, (Upgrade) sink))
                    .toList();
            return prices.isEmpty() ? id.value() : "one of: " + String.join(" · ", prices);
        }
        if (Demand.isProgressItem(id)) {
            return definition.nameOfProgress(Demand.progressKind(id));
        }
        Item known = items.get(id);
        return known == null ? id.value() : known.displayName();
    }

    /**
     * One price, every part of it: "10 Cogs + 18,000 Memory EXP". A gate is not
     * a price and is left out; the step's other prices share it.
     */
    private static String priceOf(GameDefinition definition, Map<ItemId, Item> items, Upgrade price) {
        return Stream.concat(
                        price.costs().stream().map(cost -> quantity(cost.quantity()) + " " + of(definition, items, cost.item())),
                        price.progress().stream().map(p -> quantity(p.quantity()) + " " + definition.nameOfProgress(p.kind())))
                .reduce((a, b) -> a + " + " + b)
                .orElse(price.id());
    }

    /** Grouped, and the same on every server whatever its locale. */
    private static String quantity(int quantity) {
        return String.format(Locale.ROOT, "%,d", quantity);
    }
}
