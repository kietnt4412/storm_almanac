package io.stormalmanac.gamedata;

import io.stormalmanac.common.id.GameId;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Reading provenance back out — the half of ADR 0016 that was owed.
 *
 * <p>ADR 0016 made a fact carry where it came from and made {@code publish}
 * refuse a version that is not ours. Both of those happen behind the maintainer's
 * back, at ingest and at approval, and neither reaches a reader. For two phases
 * provenance was therefore written and never read: the gate could answer the
 * question and nobody else could ask it. A catalog page that shows a number
 * without saying where it was read is exactly the situation ADR 0015 exists to
 * fix, arriving one layer further out — "our data" as a sentence in a README
 * rather than as something a stranger can check.
 *
 * <p><b>Why this is a second port and not a field on {@link GameDefinition}.</b>
 * {@link Provenance}'s own javadoc gives the reason and it still holds: a solver
 * that can read where a number came from is a solver that can eventually be made
 * to prefer one, and nobody would notice. {@code planner} loads a
 * {@code GameDefinition}; it does not know this interface exists. Serving
 * provenance to a reader must not make it reachable from the search, so the
 * read travels on its own path to the one module that has a reader on the other
 * end.
 *
 * <p><b>Why the lookup takes the facts rather than the version.</b> Asking for a
 * whole version's sourcing would be one query and a simpler signature, and it
 * would let a caller render sourcing for facts that are not on the page. Taking
 * the refs makes the response's claim narrow by construction: what comes back is
 * the sourcing of the numbers this request actually answered with.
 */
public interface ProvenanceRepository {

    /**
     * Where each of these facts was read, in one published version.
     *
     * <p>A ref with no row is not an error and not an omission — it is
     * {@link Provenance#unrecorded()}, which is the defined meaning of silence
     * and the one that cannot be published. Versions ingested before ADR 0016
     * have no rows at all and read that way, which is the honest answer rather
     * than a page that quietly shows nothing.
     *
     * @param sequence a published version's sequence; a draft has no reader and
     *                 is not visible here, the same rule
     *                 {@link GameDefinitionRepository} keeps
     */
    Sourcing of(GameId game, long sequence, Collection<String> factRefs);

    /**
     * The sourcing behind one response's numbers.
     *
     * <p>A value rather than a bare map so that the rule about silence lives in
     * one place. Every caller would otherwise write the same
     * {@code map.getOrDefault(ref, unrecorded())}, and the first one to write
     * {@code null} instead would be serving a page that says nothing about a
     * fact nobody sourced.
     */
    record Sourcing(Map<String, Provenance> byFact) {

        /**
         * Insertion-ordered rather than {@link Map#copyOf}, which does not
         * promise an order. {@link #distinct()} says "in the order first met"
         * and a hash map's iteration order would make that sentence false in a
         * way only a reader comparing two pages would ever notice.
         */
        public Sourcing {
            byFact = Collections.unmodifiableMap(new LinkedHashMap<>(byFact));
        }

        public static Sourcing empty() {
            return new Sourcing(Map.of());
        }

        /** Where this fact was read, or the record that says nobody said. */
        public Provenance of(String factRef) {
            return byFact.getOrDefault(factRef, Provenance.unrecorded());
        }

        /**
         * Every distinct sourcing behind these facts, in the order first met.
         *
         * <p>The realistic bundle is one sitting, one screen, one reader, so a
         * page's few hundred facts point at a handful of records. Repeating the
         * whole record per fact on the wire would be mostly duplication; this is
         * what lets a response carry the records once and the references beside
         * the facts.
         */
        public List<Provenance> distinct() {
            return List.copyOf(new LinkedHashSet<>(byFact.values()));
        }
    }
}
