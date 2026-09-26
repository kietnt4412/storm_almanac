package io.stormalmanac.gamedata;

import java.util.Locale;

/**
 * What to call one of the bundle's own labels in front of a reader: a scored
 * measure, an item category, an entity kind.
 *
 * <p>Each of these is a key the bundle invents so that facts can refer to one
 * another — a {@link Reward}'s bar names a measure, a {@link Fodder} rule an
 * item category — and until sequence 13 the page printed the key:
 * {@code phantom-pain-cage-score} over the plan's "how far do you get", one
 * {@code CHARACTER-EXP-POD-L} heading per Pod size over the inventory. D5's
 * second rehearsal found both (S8).
 *
 * <p><b>The bundle's word, not a reading, and it carries no provenance</b> — the
 * rule {@link ProgressKind} set, for the same reason (ADR 0033). Some of these
 * words are also the game's; the Phantom Pain Cage is called that on its own
 * screen. Some are not: the game has no heading for a Pod size. What makes
 * them one kind of thing is not where they came from but what they do, which is
 * rename a key and change no number. So none declares a {@link FactRef}, and
 * each is flattened into the patch diff all the same, because a sequence that
 * renamed one while reporting "no changes" would be a silence.
 *
 * <p>Two keys may share one word, and for a category that is the point: three
 * Pod sizes are three categories because a fodder rule has to tell them apart,
 * and one heading because a reader does not.
 *
 * @param subject     which kind of key this names
 * @param key         the key exactly as the bundle's facts use it
 * @param displayName what to show instead
 */
public record Word(Subject subject, String key, String displayName) {

    public enum Subject {
        /** A {@link Reward.Requirement#measure()}: what a scored mode is scored in. */
        MEASURE,
        /** An {@link Item#category()}. */
        CATEGORY,
        /** A catalog entity's kind: character, weapon, memory. */
        ENTITY_KIND;

        /** As a bundle spells it: {@code measure}, {@code category}, {@code entity-kind}. */
        public String spelled() {
            return name().toLowerCase(Locale.ROOT).replace('_', '-');
        }

        public static Subject spelled(String spelled) {
            for (Subject subject : values()) {
                if (subject.spelled().equals(spelled)) return subject;
            }
            throw new IllegalArgumentException("no such subject '" + spelled + "'; expected one of"
                    + " measure, category, entity-kind");
        }
    }

    public Word {
        if (subject == null) throw new IllegalArgumentException("a word must say what it names");
        if (key == null || key.isBlank()) throw new IllegalArgumentException("a word must name a key");
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException(
                    subject.spelled() + " '" + key + "' must have a display name, or not be declared at all");
        }
    }
}
