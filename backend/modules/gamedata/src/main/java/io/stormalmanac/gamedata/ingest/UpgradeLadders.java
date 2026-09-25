package io.stormalmanac.gamedata.ingest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Expands a bundle's {@code ladders} into the ordinary upgrade rows they stand
 * for, before any of them is parsed.
 *
 * <p>A ladder is an upgrade path written once and climbed by several entities:
 * the rows are the same numbers, and what differs per entity is a handful of
 * words — an item, a skill's name — supplied as placeholders. See ADR 0031.
 * <strong>It exists only in the file.</strong> What comes out is exactly the
 * JSON an author would have written by hand, one row per entity per step, and it
 * goes through the same {@code upgrade} parser; nothing downstream of this class
 * can tell a laddered row from a hand-written one, and nothing is meant to.
 *
 * <p>Three rules, each refusing a shape that reads as intended and does
 * something else:
 * <ul>
 *   <li>every {@code {name}} a row mentions is bound by every entity that climbs
 *       the ladder, and every binding is mentioned — a binding nothing reads is a
 *       typo that leaves the row it was meant for saying something else;</li>
 *   <li>a row names no {@code entity}: the ladder supplies it, and a row that
 *       named one would be climbed by everybody on that entity's behalf;</li>
 *   <li>a list binding is spent only inside an {@code each} group that walks
 *       it, one copy of the group per element, in order.</li>
 * </ul>
 *
 * <p>A group may also say what differs by <em>position</em> rather than by word:
 * {@code positions} holds one object per element of the list, and its fields
 * are set on every row of that element's copy. The skill in slot two is a Yellow
 * Orb on every S-rank construct whatever it is called, so the slot's section and
 * tag are written once here rather than once per construct (ADR 0032). A
 * {@code positions} list whose length is not the list's, or a field a row
 * already sets, is refused — either would label a skill with another's words.
 */
final class UpgradeLadders {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([a-z0-9][a-z0-9-]*)}");

    /**
     * One upgrade row an entity's climb produced, still as JSON.
     *
     * @param row       the row with the entity set, its id prefixed and every
     *                  placeholder replaced
     * @param at        where it came from, for a parse failure to name
     * @param sourcedBy the provenance this fact is covered by, or null for the
     *                  bundle's default
     */
    record Row(JsonNode row, String at, String sourcedBy) {}

    private UpgradeLadders() {}

    static List<Row> expand(JsonNode root) {
        JsonNode ladders = root.get("ladders");
        if (ladders == null || ladders.isNull()) return List.of();
        if (!ladders.isArray()) throw new BundleFormatException("ladders must be an array");

        List<Row> rows = new ArrayList<>();
        Set<String> ids = new LinkedHashSet<>();
        for (int i = 0; i < ladders.size(); i++) {
            String at = "ladders[" + i + "]";
            JsonNode ladder = object(ladders.get(i), at);
            String id = text(ladder, "id", at + ".id");
            if (!ids.add(id)) throw new BundleFormatException("duplicate ladder '" + id + "'");
            rows.addAll(ladder(ladder, id, at));
        }
        return rows;
    }

    private static List<Row> ladder(JsonNode ladder, String id, String at) {
        String ladderSource = optionalText(ladder, "sourcedBy", at + ".sourcedBy");
        List<Block> blocks = blocks(ladder, at);

        JsonNode climbers = ladder.get("appliesTo");
        if (climbers == null || climbers.isNull() || !climbers.isArray() || climbers.isEmpty()) {
            // Refused rather than expanded to nothing: a ladder nobody climbs is
            // rows the author meant to publish and never will, and the bundle
            // would parse as if they were never written.
            throw new BundleFormatException(at + ".appliesTo must list at least one entity: ladder '"
                    + id + "' is climbed by nobody, so none of its rows would be published");
        }

        List<Row> rows = new ArrayList<>();
        for (int c = 0; c < climbers.size(); c++) {
            String climbAt = at + ".appliesTo[" + c + "]";
            JsonNode climber = object(climbers.get(c), climbAt);
            String entity = text(climber, "entity", climbAt + ".entity");
            String climbSource = optionalText(climber, "sourcedBy", climbAt + ".sourcedBy");
            Map<String, JsonNode> bindings = bindings(climber, climbAt);

            Set<String> used = new LinkedHashSet<>();
            for (Block block : blocks) {
                if (block.each() == null) {
                    for (Step step : block.steps()) {
                        rows.add(row(step, entity, Map.of(), bindings, used, climbAt, climbSource, ladderSource));
                    }
                    continue;
                }
                JsonNode values = bindings.get(block.each());
                if (values == null || !values.isArray()) {
                    throw new BundleFormatException(block.at() + " walks '" + block.each() + "', which "
                            + climbAt + " (" + entity + ") does not bind to a list");
                }
                used.add(block.each());
                if (block.positions() != null && block.positions().size() != values.size()) {
                    throw new BundleFormatException(block.at() + ".positions has " + block.positions().size()
                            + " entries and " + climbAt + " (" + entity + ") binds " + values.size() + " '"
                            + block.each() + "'; one entry per position, or the words land on the wrong rows");
                }
                // The whole group per word, so one skill's rows sit together
                // the way an author writing them out would have put them.
                for (int v = 0; v < values.size(); v++) {
                    Map<String, String> current = Map.of(block.each(), values.get(v).textValue());
                    for (Step step : block.steps()) {
                        Row made = row(step, entity, current, bindings, used, climbAt, climbSource, ladderSource);
                        if (block.positions() != null) {
                            place((ObjectNode) made.row(), block.positions().get(v), current, bindings, used,
                                    made.at());
                        }
                        rows.add(made);
                    }
                }
            }

            Set<String> unused = new LinkedHashSet<>(bindings.keySet());
            unused.removeAll(used);
            if (!unused.isEmpty()) {
                throw new BundleFormatException(climbAt + " (" + entity + ") binds " + unused
                        + ", which no row of ladder '" + id + "' mentions");
            }
        }
        return rows;
    }

    /** One ladder row, and the provenance its group gives it if it is in one. */
    private record Step(ObjectNode row, String at, String groupSource) {}

    /**
     * A plain row, as a block of one with no list; or an {@code each} group, the
     * list it walks, and what each position adds, if anything.
     */
    private record Block(String each, String at, List<Step> steps, List<ObjectNode> positions) {}

    private static List<Block> blocks(JsonNode ladder, String at) {
        JsonNode upgrades = ladder.get("upgrades");
        if (upgrades == null || !upgrades.isArray() || upgrades.isEmpty()) {
            throw new BundleFormatException(at + ".upgrades must list at least one row");
        }
        List<Block> blocks = new ArrayList<>();
        for (int u = 0; u < upgrades.size(); u++) {
            String rowAt = at + ".upgrades[" + u + "]";
            JsonNode node = object(upgrades.get(u), rowAt);
            JsonNode each = node.get("each");
            if (each == null || each.isNull()) {
                blocks.add(new Block(null, rowAt, List.of(step(node, rowAt, null)), null));
                continue;
            }
            String walks = text(node, "each", rowAt + ".each");
            String groupSource = optionalText(node, "sourcedBy", rowAt + ".sourcedBy");
            JsonNode group = node.get("upgrades");
            if (group == null || !group.isArray() || group.isEmpty()) {
                throw new BundleFormatException(rowAt + ".upgrades must list the rows repeated for each '"
                        + walks + "'");
            }
            List<Step> steps = new ArrayList<>();
            for (int g = 0; g < group.size(); g++) {
                String groupAt = rowAt + ".upgrades[" + g + "]";
                JsonNode inner = object(group.get(g), groupAt);
                if (inner.has("each")) {
                    throw new BundleFormatException(groupAt + " is a group inside a group; one level"
                            + " of 'each' is all a ladder has");
                }
                steps.add(step(inner, groupAt, groupSource));
            }
            blocks.add(new Block(walks, rowAt, steps, positions(node, rowAt)));
        }
        return blocks;
    }

    private static List<ObjectNode> positions(JsonNode group, String at) {
        JsonNode positions = group.get("positions");
        if (positions == null || positions.isNull()) return null;
        if (!positions.isArray() || positions.isEmpty()) {
            throw new BundleFormatException(at + ".positions must be a non-empty array, one object per position");
        }
        List<ObjectNode> each = new ArrayList<>();
        for (int p = 0; p < positions.size(); p++) {
            each.add((ObjectNode) object(positions.get(p), at + ".positions[" + p + "]"));
        }
        return each;
    }

    /** One position's fields onto one row, placeholders filled, never over a field the row sets. */
    private static void place(ObjectNode row, ObjectNode position, Map<String, String> current,
            Map<String, JsonNode> bindings, Set<String> used, String where) {
        position.properties().forEach(field -> {
            if (field.getKey().startsWith("_")) return;
            if (row.has(field.getKey())) {
                throw new BundleFormatException(where + " sets '" + field.getKey()
                        + "' itself and its position sets it too; say it in one place");
            }
            row.set(field.getKey(), substitute(field.getValue(), current, bindings, used, where));
        });
    }

    private static Step step(JsonNode node, String at, String groupSource) {
        if (node.has("entity")) {
            throw new BundleFormatException(at + " names an entity; a ladder row is climbed by every"
                    + " entity in appliesTo and must not name one");
        }
        text(node, "id", at + ".id");
        return new Step((ObjectNode) node, at, groupSource);
    }

    private static Row row(Step step, String entity, Map<String, String> current,
            Map<String, JsonNode> bindings, Set<String> used,
            String climbAt, String climbSource, String ladderSource) {

        String where = step.at() + " for " + climbAt + " (" + entity + ")";
        ObjectNode row = (ObjectNode) substitute(step.row(), current, bindings, used, where);
        row.remove("sourcedBy");
        row.put("id", entity + "-" + row.get("id").textValue());
        row.put("entity", entity);

        String rowSource = optionalText(step.row(), "sourcedBy", step.at() + ".sourcedBy");
        String sourcedBy = climbSource != null ? climbSource
                : rowSource != null ? rowSource
                : step.groupSource() != null ? step.groupSource()
                : ladderSource;
        return new Row(row, where, sourcedBy);
    }

    /**
     * A copy of {@code node} with every placeholder in every string replaced.
     * Fields whose name starts with an underscore are comments and are copied
     * untouched, so a comment may say {@code {skill}} without binding it.
     */
    private static JsonNode substitute(JsonNode node, Map<String, String> current,
            Map<String, JsonNode> bindings, Set<String> used, String where) {

        if (node.isTextual()) {
            return TextNode.valueOf(fill(node.textValue(), current, bindings, used, where));
        }
        if (node.isArray()) {
            ArrayNode copy = ((ArrayNode) node).arrayNode();
            node.forEach(element -> copy.add(substitute(element, current, bindings, used, where)));
            return copy;
        }
        if (node.isObject()) {
            ObjectNode copy = ((ObjectNode) node).objectNode();
            node.properties().forEach(field -> copy.set(field.getKey(),
                    field.getKey().startsWith("_")
                            ? field.getValue().deepCopy()
                            : substitute(field.getValue(), current, bindings, used, where)));
            return copy;
        }
        return node.deepCopy();
    }

    private static String fill(String text, Map<String, String> current,
            Map<String, JsonNode> bindings, Set<String> used, String where) {

        Matcher matcher = PLACEHOLDER.matcher(text);
        StringBuilder filled = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(1);
            String value = current.get(name);
            if (value == null) {
                JsonNode bound = bindings.get(name);
                if (bound == null) {
                    throw new BundleFormatException(where + " mentions {" + name + "}, which is not bound;"
                            + " bound: " + bindings.keySet());
                }
                if (!bound.isTextual()) {
                    throw new BundleFormatException(where + " mentions {" + name + "}, which is a list and"
                            + " can only be spent inside a group with \"each\": \"" + name + "\"");
                }
                value = bound.textValue();
                used.add(name);
            }
            matcher.appendReplacement(filled, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(filled);
        return filled.toString();
    }

    /** {@code with}: each name bound to a word, or to a list of words for an {@code each} group. */
    private static Map<String, JsonNode> bindings(JsonNode climber, String at) {
        JsonNode with = climber.get("with");
        if (with == null || with.isNull()) return Map.of();
        if (!with.isObject()) throw new BundleFormatException(at + ".with must be an object of name -> word");

        Map<String, JsonNode> bindings = new LinkedHashMap<>();
        with.properties().forEach(field -> {
            String name = field.getKey();
            JsonNode value = field.getValue();
            if (!PLACEHOLDER.matcher("{" + name + "}").matches()) {
                throw new BundleFormatException(at + ".with." + name
                        + " is not a placeholder name: lower-case letters, digits and hyphens");
            }
            boolean word = value.isTextual() && !value.textValue().isBlank();
            boolean words = value.isArray() && !value.isEmpty();
            if (words) {
                for (JsonNode element : value) {
                    if (!element.isTextual() || element.textValue().isBlank()) words = false;
                }
            }
            if (!word && !words) {
                throw new BundleFormatException(at + ".with." + name
                        + " must be a word or a non-empty list of words");
            }
            bindings.put(name, value);
        });
        return bindings;
    }

    private static JsonNode object(JsonNode node, String at) {
        if (node == null || !node.isObject()) throw new BundleFormatException(at + " must be an object");
        return node;
    }

    private static String text(JsonNode parent, String field, String at) {
        String value = optionalText(parent, field, at);
        if (value == null) throw new BundleFormatException(at + " is required");
        return value;
    }

    private static String optionalText(JsonNode parent, String field, String at) {
        JsonNode node = parent.get(field);
        if (node == null || node.isNull()) return null;
        if (!node.isTextual() || node.textValue().isBlank()) {
            throw new BundleFormatException(at + " must be a non-blank string");
        }
        return node.textValue();
    }
}
