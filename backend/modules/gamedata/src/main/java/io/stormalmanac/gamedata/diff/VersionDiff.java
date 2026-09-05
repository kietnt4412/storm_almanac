package io.stormalmanac.gamedata.diff;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.diff.Facts.Subject;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * What one patch changed, computed as a set comparison between two snapshots.
 *
 * <p>This is the feature the "a version is a full snapshot, not a delta"
 * decision was made for. Both sides are complete, so the diff is an ordinary
 * comparison of two flattened maps rather than a walk through a history of
 * edits — and it means any two published versions can be compared, not just
 * adjacent ones.
 *
 * <p>Comparison happens in two passes for a reason that shows in the output. A
 * removed stage is <em>one</em> line saying the stage is gone, not six lines
 * saying its name, energy cost and four drops are gone. So subjects are diffed
 * first for what appeared and disappeared, and only subjects present on both
 * sides have their fields compared.
 */
public record VersionDiff(GameDataVersion from, GameDataVersion to, List<Change> changes) {

    public VersionDiff {
        changes = List.copyOf(changes);
    }

    public static VersionDiff between(GameDefinition from, GameDefinition to) {
        if (!from.game().id().equals(to.game().id())) {
            throw new IllegalArgumentException(
                    "cannot diff across games: " + from.game().id() + " and " + to.game().id());
        }
        Map<Subject, Map<String, String>> before = Facts.of(from);
        Map<Subject, Map<String, String>> after = Facts.of(to);

        Set<Subject> everySubject = new LinkedHashSet<>(before.keySet());
        everySubject.addAll(after.keySet());

        List<Change> changes = new ArrayList<>();
        for (Subject subject : everySubject) {
            Map<String, String> was = before.get(subject);
            Map<String, String> now = after.get(subject);

            if (was == null) {
                changes.add(Change.added(subject.axis(), subject.label()));
            } else if (now == null) {
                changes.add(Change.removed(subject.axis(), subject.label()));
            } else {
                compareFields(subject, was, now, changes);
            }
        }
        changes.sort(null);
        return new VersionDiff(from.version(), to.version(), changes);
    }

    /**
     * A field that appears or disappears within a surviving subject is a change
     * to that subject, not a new subject — a stage that stops dropping an item
     * reads as {@code drop shard: 2.0 → (none)}, which is what a patch note
     * would say.
     */
    private static void compareFields(
            Subject subject, Map<String, String> was, Map<String, String> now, List<Change> changes) {

        Set<String> everyField = new LinkedHashSet<>(was.keySet());
        everyField.addAll(now.keySet());

        for (String field : everyField) {
            String before = was.getOrDefault(field, "(none)");
            String after = now.getOrDefault(field, "(none)");
            if (!before.equals(after)) {
                changes.add(Change.changed(subject.axis(), subject.label(), field, before, after));
            }
        }
    }

    public boolean isEmpty() {
        return changes.isEmpty();
    }

    /** The changes on one axis, for a report that shows one at a time. */
    public List<Change> on(Axis axis) {
        return changes.stream().filter(change -> change.axis() == axis).toList();
    }

    public Map<Axis, List<Change>> byAxis() {
        return changes.stream().collect(Collectors.groupingBy(
                Change::axis, () -> new EnumMap<>(Axis.class), Collectors.toList()));
    }

    /**
     * The report, as plain text.
     *
     * <p>Plain text because this is what a person reads before approving a
     * publish, and because it belongs in a terminal and a release note before it
     * belongs in a web page. Phase 4 can render the same {@link Change} list.
     */
    public String render() {
        StringBuilder out = new StringBuilder()
                .append(from.game().value()).append(": ")
                .append(from.label()).append(" → ").append(to.label())
                .append(System.lineSeparator());

        if (changes.isEmpty()) {
            return out.append("  no changes").append(System.lineSeparator()).toString();
        }
        byAxis().forEach((axis, onThisAxis) -> {
            out.append(System.lineSeparator())
                    .append(axis.name().toLowerCase(java.util.Locale.ROOT))
                    .append(" · ").append(onThisAxis.size()).append(" change(s)")
                    .append(System.lineSeparator());
            onThisAxis.forEach(change ->
                    out.append("  ").append(change.render()).append(System.lineSeparator()));
        });
        return out.toString();
    }
}
