package io.stormalmanac.gamedata;

import io.stormalmanac.common.id.EntityId;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A deterministic advance of one entity from one state to the next: Insight 1
 * to Insight 2, skill rank 3 to rank 4, level 40 to 50.
 *
 * <p>States are opaque strings supplied by the game bundle. The planner only
 * needs the graph they form, never their meaning.
 *
 * @param costs    the items spent
 * @param requires states of the <em>same</em> entity, usually on another
 *                 track, that must be reached before this step can be taken: a
 *                 rank that needs a level, a passive that needs an evolution.
 *                 Reaching them is paid for as part of this step. A gate on
 *                 another entity is not expressible, deliberately; no reading
 *                 so far has needed one that is also a cost
 * @param progress what this step needs that no inventory holds, such as EXP.
 *                 Paid by {@link Fodder}
 * @param labels   what the game calls the two states and where their track
 *                 sits on its screens; {@link Labels#NONE} for every step
 *                 published before a step could say (ADR 0032)
 */
public record Upgrade(
        String id,
        EntityId entity,
        String fromState,
        String toState,
        List<ItemStack> costs,
        List<String> requires,
        List<Progress> progress,
        Labels labels
) implements Sink {

    /**
     * The game's words for a step, which the planner never reads.
     *
     * <p>A state is an opaque id and stays one; these are what a reader is shown
     * instead of it. They are text read off the game's screens, so they are part
     * of the step and covered by the step's provenance, like its costs (ADR
     * 0032). Every field is optional and they are independent: a rank ladder
     * names its states and has no tag, a skill has a tag and numbered states.
     *
     * @param fromName what the game calls {@code fromState} — only worth saying
     *                 on a track's first step, since every other state is some
     *                 step's {@code toState}
     * @param toName   what the game calls {@code toState}
     * @param section  the heading the track sits under, one of the bundle's
     *                 declared sections
     * @param tag      the track's kind as the game brackets it beside its name,
     *                 such as an orb colour
     */
    public record Labels(String fromName, String toName, String section, String tag) {

        public static final Labels NONE = new Labels(null, null, null, null);

        public Labels {
            fromName = blankToNull(fromName, "fromName");
            toName = blankToNull(toName, "toName");
            section = blankToNull(section, "section");
            tag = blankToNull(tag, "tag");
        }

        private static String blankToNull(String value, String field) {
            if (value != null && value.isBlank()) {
                throw new IllegalArgumentException(field + " must not be blank; leave it out instead");
            }
            return value;
        }
    }

    /** A step whose game gave it no words, which is every step published before sequence 11. */
    public Upgrade(
            String id,
            EntityId entity,
            String fromState,
            String toState,
            List<ItemStack> costs,
            List<String> requires,
            List<Progress> progress) {
        this(id, entity, fromState, toState, costs, requires, progress, Labels.NONE);
    }

    /**
     * Neither a gate nor a progress cost has an order, so both are normalised
     * here: gates sorted and distinct, progress summed per kind and sorted. The
     * same upgrade written two ways is then one value, which is what lets a
     * version read back from the database equal the bundle it came from.
     */
    public Upgrade {
        labels = labels == null ? Labels.NONE : labels;
        costs = List.copyOf(costs);
        requires = requires.stream().distinct().sorted().toList();
        Map<String, Integer> byKind = new TreeMap<>();
        progress.forEach(p -> byKind.merge(p.kind(), p.quantity(), Math::addExact));
        progress = byKind.entrySet().stream().map(e -> new Progress(e.getKey(), e.getValue())).toList();
        if (requires.contains(toState)) {
            throw new IllegalArgumentException("upgrade '" + id + "' requires the state it reaches");
        }
    }

    /** A step with a price and nothing else, which is every step the first two readings produced. */
    public Upgrade(String id, EntityId entity, String fromState, String toState, List<ItemStack> costs) {
        this(id, entity, fromState, toState, costs, List.of(), List.of());
    }
}
