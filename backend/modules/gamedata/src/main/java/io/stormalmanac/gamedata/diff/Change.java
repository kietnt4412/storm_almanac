package io.stormalmanac.gamedata.diff;

/**
 * One difference between two versions.
 *
 * <p>Deliberately flat and stringly-typed on the value side. A diff is read, not
 * computed against: it renders in a report, goes in a release note, and is the
 * thing a human looks at before approving a publish. Typing the values would
 * mean a sum type over every field in the model, and every consumer would
 * immediately format them back to strings.
 *
 * <p><b>The subject is two fields, not its label.</b> Until C3.2 a change carried
 * only {@code stage 'pg-1-1'}, which is all a report needs; narrowing a diff to
 * one reader's plan needs to ask "is this the stage the plan runs", and that is
 * a question about the slug. The label is derived, so a report reads as it did.
 *
 * @param about  what sort of thing changed, as the bundle spells it — {@code stage}
 * @param slug   which one — {@code pg-1-1}
 * @param detail which part of it, for {@link Kind#CHANGED}; null otherwise
 * @param before the old value, or null when the subject did not exist
 * @param after  the new value, or null when the subject no longer exists
 */
public record Change(Axis axis, Kind kind, String about, String slug, String detail, String before, String after)
        implements Comparable<Change> {

    public enum Kind { ADDED, REMOVED, CHANGED }

    static Change added(Axis axis, String about, String slug) {
        return new Change(axis, Kind.ADDED, about, slug, null, null, null);
    }

    static Change removed(Axis axis, String about, String slug) {
        return new Change(axis, Kind.REMOVED, about, slug, null, null, null);
    }

    static Change changed(Axis axis, String about, String slug, String detail, String before, String after) {
        return new Change(axis, Kind.CHANGED, about, slug, detail, before, after);
    }

    /** What changed, as a report names it: {@code stage 'pg-1-1'}. */
    public String subject() {
        return label(about, slug);
    }

    public static String label(String about, String slug) {
        return about + " '" + slug + "'";
    }

    /** One line, for a report a human reads. */
    public String render() {
        return switch (kind) {
            case ADDED -> "+ " + subject();
            case REMOVED -> "- " + subject();
            case CHANGED -> "~ " + subject() + " · " + detail + ": " + before + " → " + after;
        };
    }

    /** Grouped by axis, then subject, so a rendered report is stable between runs. */
    @Override
    public int compareTo(Change other) {
        int byAxis = axis.compareTo(other.axis);
        if (byAxis != 0) return byAxis;
        int bySubject = subject().compareTo(other.subject());
        if (bySubject != 0) return bySubject;
        int byKind = kind.compareTo(other.kind);
        if (byKind != 0) return byKind;
        return String.valueOf(detail).compareTo(String.valueOf(other.detail));
    }
}
