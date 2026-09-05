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
 * @param subject what changed, as {@code kind 'slug'} — {@code stage 'pg-1-1'}
 * @param detail  which part of it, for {@link Kind#CHANGED}; null otherwise
 * @param before  the old value, or null when the subject did not exist
 * @param after   the new value, or null when the subject no longer exists
 */
public record Change(Axis axis, Kind kind, String subject, String detail, String before, String after)
        implements Comparable<Change> {

    public enum Kind { ADDED, REMOVED, CHANGED }

    static Change added(Axis axis, String subject) {
        return new Change(axis, Kind.ADDED, subject, null, null, null);
    }

    static Change removed(Axis axis, String subject) {
        return new Change(axis, Kind.REMOVED, subject, null, null, null);
    }

    static Change changed(Axis axis, String subject, String detail, String before, String after) {
        return new Change(axis, Kind.CHANGED, subject, detail, before, after);
    }

    /** One line, for a report a human reads. */
    public String render() {
        return switch (kind) {
            case ADDED -> "+ " + subject;
            case REMOVED -> "- " + subject;
            case CHANGED -> "~ " + subject + " · " + detail + ": " + before + " → " + after;
        };
    }

    /** Grouped by axis, then subject, so a rendered report is stable between runs. */
    @Override
    public int compareTo(Change other) {
        int byAxis = axis.compareTo(other.axis);
        if (byAxis != 0) return byAxis;
        int bySubject = subject.compareTo(other.subject);
        if (bySubject != 0) return bySubject;
        int byKind = kind.compareTo(other.kind);
        if (byKind != 0) return byKind;
        return String.valueOf(detail).compareTo(String.valueOf(other.detail));
    }
}
