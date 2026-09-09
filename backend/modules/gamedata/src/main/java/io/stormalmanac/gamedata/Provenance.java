package io.stormalmanac.gamedata;

import java.time.LocalDate;

/**
 * Where a fact came from, as a property of the data rather than of the README.
 *
 * <p><b>Why this record exists.</b>
 * {@code docs/adr/0015-game-data-is-sourced-first-hand-not-adapted.md} says the
 * shipped product carries only game data this project sourced itself: a fact
 * enters a published bundle because someone observed it in the game or read it
 * from the publisher's own disclosure. Re-typing an aggregator's numbers into
 * our own JSON would be laundering rather than sourcing — and, crucially,
 * <em>the two are byte-identical once typed</em>. Without something like this
 * record, "self-sourced" is a claim in a document that nothing can contradict.
 * With it, a bundle can be asked the question and can name the facts that fail.
 *
 * <p><b>Why it is not a field on {@code Stage} or {@code Item}.</b> Provenance
 * is a property of the act of sourcing, not of the game. Hanging it on the
 * domain records would put it one field access away from the solver, and a
 * solver that can read where a number came from is a solver that can eventually
 * be made to prefer one — which is a bias nobody asked for and nobody would
 * notice. It lives on {@link io.stormalmanac.gamedata.ingest.GameDataBundle}
 * instead, keyed by a reference to the fact it covers, so {@code planner}
 * cannot see it even by accident.
 *
 * <p>{@code Drop.sampledRuns} is the one deliberate exception and stays where it
 * is: the solver <em>does</em> read it, because ADR 0011 makes the width of a
 * yield's confidence interval part of the answer. That is a statistical fact
 * about the number, not a claim about who typed it.
 *
 * @param id         the slug a bundle's facts point at; unique within a bundle
 * @param origin     what kind of sourcing this was — the one field with teeth
 * @param detail     how somebody could check it: the screen, the disclosure, the
 *                   account, the run count. Required and non-blank, because an
 *                   origin without a detail is an assertion rather than a record
 * @param observedOn the day it was read. Required: game data goes stale, and a
 *                   provenance that cannot be dated cannot be re-checked against
 *                   a later patch
 */
public record Provenance(String id, Origin origin, String detail, LocalDate observedOn) {

    /**
     * The ways a fact can arrive, and whether each one is ours to publish.
     *
     * <p>An enum rather than data, unlike almost everything else in this module.
     * The project convention is that anything <em>the game</em> varies is data —
     * rarity is a {@code (label, rank)} pair and not an enum for exactly that
     * reason. This does not vary by game. It is this project's sourcing policy,
     * and adding a member should be a deliberate code change that forces
     * somebody to answer {@link #isFirstHand()} for it.
     */
    public enum Origin {

        /** Someone read it off a screen in the game. The catalog axis, mostly. */
        OBSERVED_IN_GAME(true),

        /**
         * Someone played the content and counted, over a stated number of runs.
         *
         * <p>The drop axis, and the expensive one — see the bootstrap problem in
         * ADR 0015. A yield sourced this way should also carry its
         * {@code sampledRuns}; this says who did the counting, that says how much
         * it is worth.
         */
        SAMPLED_IN_GAME(true),

        /**
         * The publisher stated it: an in-client rules screen, patch notes, a
         * regulatory disclosure.
         *
         * <p>First-hand in the sense that matters, because the chain has no
         * intermediary. Gacha rates arrive this way. Stage drop rates do not —
         * Reverse: 1999 labels a drop {@code Fixed}, {@code Common} or
         * {@code Possible} and puts a number on none but the first. See
         * {@code docs/game-facts/reverse-1999-drop-disclosure.md}.
         */
        PUBLISHER_DISCLOSURE(true),

        /**
         * Invented by this project: a synthetic title that exists to exercise the
         * schema.
         *
         * <p>First-hand because nobody else authored it, which is the only sense
         * in which the question is being asked. It is not a claim that the number
         * is true of anything — {@code proving-ground} is not a real game and its
         * numbers are not any real game's numbers.
         */
        AUTHORED_FIXTURE(true),

        /**
         * Somebody else's data: an aggregator, a community guide, another
         * project.
         *
         * <p>Not first-hand, and the reason the enum exists. Data marked this way
         * may be ingested and diffed locally — that is what keeps the Kornblume
         * adapter useful as a cross-check — but publishing it takes a second,
         * explicit approval that says so out loud.
         */
        THIRD_PARTY(false),

        /**
         * Nobody said.
         *
         * <p>What a bundle that declares no provenance at all gets, so that
         * silence has a defined meaning instead of being a hole. It is
         * <b>not</b> first-hand, and that direction is the whole point: an
         * absent record is not a record, and the failure mode worth designing
         * against is a bundle that says nothing and is read as saying
         * everything is fine.
         *
         * <p>This is why the format does not simply require the field. A parser
         * that rejected a bundle without provenance would make every throwaway
         * test bundle carry six lines of ceremony, and the pressure would be to
         * make the ceremony meaningless. Letting silence parse and refusing to
         * <em>publish</em> it puts the check where the decision is.
         */
        UNRECORDED(false);

        private final boolean firstHand;

        Origin(boolean firstHand) {
            this.firstHand = firstHand;
        }

        /** Whether a fact from here is this project's to publish. */
        public boolean isFirstHand() {
            return firstHand;
        }
    }

    public Provenance {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("provenance id is required");
        }
        if (origin == null) {
            throw new IllegalArgumentException("provenance '" + id + "' has no origin");
        }
        if (detail == null || detail.isBlank()) {
            throw new IllegalArgumentException(
                    "provenance '" + id + "' has no detail: an origin nobody can check is not a record");
        }
        if (observedOn == null) {
            throw new IllegalArgumentException("provenance '" + id + "' has no observation date");
        }
        id = id.strip();
        detail = detail.strip();
    }

    /** The id {@link #unrecorded()} carries, and the one slug a bundle may not reuse. */
    public static final String UNRECORDED_ID = "unrecorded";

    /**
     * What a bundle that declares no provenance is treated as having declared.
     *
     * <p>Dated to the epoch rather than to today, because there is no reading to
     * date. A plausible-looking date here would be the one piece of this record
     * that lies convincingly.
     */
    public static Provenance unrecorded() {
        return new Provenance(
                UNRECORDED_ID,
                Origin.UNRECORDED,
                "This bundle did not say where its facts came from."
                        + " Declare a provenance before publishing it: see ADR 0015.",
                LocalDate.EPOCH);
    }

    /** Whether facts sourced this way are this project's to publish. */
    public boolean isFirstHand() {
        return origin.isFirstHand();
    }
}
