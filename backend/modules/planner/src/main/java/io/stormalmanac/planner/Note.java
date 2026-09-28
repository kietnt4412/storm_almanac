package io.stormalmanac.planner;

import java.util.Objects;

/**
 * One thing a plan says about itself, and what kind of thing it is.
 *
 * <p>Until 2026-09-28 a note was a sentence and nothing else, and the page gave
 * every one the same weight: "this cannot be finished in under 14 days" in the
 * same colour and size as "worked out from 1 stage, 3 shop offers and 3
 * recipes". The five strangers who closed Phase 4 read that list as a wall
 * (T3). What a note is for is known where it is written — the optimizer knows
 * that a time budget running out changes what the number means and that a count
 * of recipes does not — so it is said there, and the page decides what to show
 * first.
 *
 * <p><b>A kind orders notes and never hides one.</b> {@link Kind#DETAIL} may sit
 * behind a toggle; a warning may not, and a stopped search is a warning — a
 * plan shown without its gap is a confident number hiding one.
 */
public record Note(Kind kind, String text) {

    public enum Kind {
        /** Changes what the answer means or whether it can be followed: read before acting on it. */
        WARNING,
        /** Something the plan takes on trust about the reader, which only they can check. */
        ASSUMPTION,
        /** Goals needing nothing: said, and not a problem. */
        DONE,
        /** How the answer was worked out. True, and read past on almost every plan. */
        DETAIL
    }

    public Note {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(text, "text");
    }

    public static Note warning(String text) {
        return new Note(Kind.WARNING, text);
    }

    public static Note assumption(String text) {
        return new Note(Kind.ASSUMPTION, text);
    }

    public static Note done(String text) {
        return new Note(Kind.DONE, text);
    }

    public static Note detail(String text) {
        return new Note(Kind.DETAIL, text);
    }
}
