package io.stormalmanac.api.player;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.Item;
import io.stormalmanac.planner.Demand;
import io.stormalmanac.player.Inventory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * "What am I still short of for her?" — the catalog's personalized overlay.
 *
 * <p>This is the shape the character page reads, and it is the reason the
 * catalog is worth having at all. A catalog page that shows what Insight 2 costs
 * is a wiki page somebody else already wrote better; a catalog page that shows
 * what <em>this reader</em> is short of, from where their roster actually
 * stands, is the thing only an account can answer.
 *
 * <p><b>Three numbers per item, not one.</b> {@code required} is what the walk
 * over the upgrade graph says the goal costs, {@code owned} is what the profile
 * holds, and {@code missing} is what is left. Sending only the third would be
 * smaller and would make the number unauditable: a reader who disagrees with a
 * shortfall needs to see which half they disagree with, and a client that
 * subtracts for itself is a second implementation of the one subtraction.
 *
 * <p><b>{@code required} is gross, exactly as {@link Demand} reports it.</b> The
 * demand vector is shared across profiles by the solver's cache, so netting it
 * off an inventory happens at the edge and nowhere else.
 */
public final class ShortfallView {

    private ShortfallView() {}

    /**
     * @param currentState where the roster says this reader stands, or null if
     *                     they do not own the entity. Null rather than an
     *                     invented starting state: "not owned" is a real answer
     *                     and the demand walk already handles it by walking back
     *                     to a track with nothing before it
     * @param alreadyMet   the reader is at or past the target. Not an error and
     *                     not an empty list either — those are different facts,
     *                     and a page that renders "nothing needed" for both
     *                     tells a player who has already finished the same thing
     *                     it tells a player whose goal costs nothing
     * @param steps        the upgrade ids being paid for, so the total has
     *                     something to be a total of
     * @param complete     nothing is missing: either already met, or the
     *                     inventory covers every line
     */
    public record ShortfallResponse(
            String profile,
            String game,
            long version,
            String versionLabel,
            String attribution,
            String entity,
            String currentState,
            String targetState,
            boolean alreadyMet,
            List<String> steps,
            List<ShortfallLine> items,
            boolean complete) {

        public static ShortfallResponse of(
                String profile,
                GameDefinition definition,
                String entity,
                String currentState,
                String targetState,
                Demand demand,
                Inventory inventory) {

            Map<ItemId, Item> items = definition.itemsById();
            List<ShortfallLine> lines = new ArrayList<>();
            demand.quantities().forEach((item, required) -> {
                int owned = inventory.quantityOf(item);
                Item known = items.get(item);
                lines.add(new ShortfallLine(
                        item.value(),
                        known == null ? item.value() : known.displayName(),
                        required,
                        owned,
                        Math.max(0, required - owned)));
            });
            // Biggest gap first. A shortfall list is read to find out what to go
            // and farm, and the answer is at the top of that order rather than
            // wherever the demand map happened to insert it.
            lines.sort(Comparator.comparingInt(ShortfallLine::missing)
                    .reversed()
                    .thenComparing(ShortfallLine::item));

            GameDataVersion at = definition.version();
            return new ShortfallResponse(
                    profile,
                    at.game().value(),
                    at.sequence(),
                    at.label(),
                    at.attribution(),
                    entity,
                    currentState,
                    targetState,
                    !demand.alreadyMet().isEmpty(),
                    demand.steps(),
                    lines,
                    lines.stream().allMatch(line -> line.missing() == 0));
        }
    }

    /** One item's line of the answer: what it costs, what is held, what is left. */
    public record ShortfallLine(
            String item, String displayName, int required, int owned, int missing) {}
}
