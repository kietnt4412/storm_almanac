package io.stormalmanac.gamedata.jdbc;

import io.stormalmanac.common.id.GameId;
import io.stormalmanac.gamedata.Provenance;
import io.stormalmanac.gamedata.ProvenanceRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Where a published version's facts were read, one query.
 *
 * <p>Confined to {@code status = 'PUBLISHED'} for the same reason
 * {@link JdbcGameDefinitionRepository} is: a draft has been ingested, not
 * approved, and the sourcing of data nobody has agreed to publish is not a thing
 * a reader may request. The publish gate reads the draft's rows instead, through
 * its own query in the ingest repository — two different questions about the
 * same table, asked by the two callers that are allowed to ask them.
 *
 * <p><b>Named parameters, for the one line that needs them.</b> The refs are a
 * variable-length list and they are slugs out of a bundle rather than a closed
 * set of enum names, so neither of the shortcuts elsewhere in this package
 * applies: {@code Availabilities} may write an array literal because
 * {@link java.time.DayOfWeek} has seven values and no quoting to escape, and
 * that reasoning does not survive being copied here. Expanding {@code IN} to
 * one placeholder per ref keeps every value a bind parameter.
 *
 * <p><b>The unknown origin is not silently dropped.</b> A value the enum does not
 * know cannot come from a bundle — the parser refuses one and V7's CHECK refuses
 * one — so reaching that case means the constraint and {@link Provenance.Origin}
 * have drifted apart. The gate's reading of that is {@code THIRD_PARTY}: assume
 * what you cannot classify is not yours to publish. A reader's is
 * {@link Provenance.Origin#UNRECORDED}, which is the same conclusion phrased for
 * somebody who wanted to know who read it — nobody this code can name.
 */
@Repository
public class JdbcProvenanceRepository implements ProvenanceRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcProvenanceRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(readOnly = true)
    public Sourcing of(GameId game, long sequence, Collection<String> factRefs) {
        List<String> refs = List.copyOf(factRefs);
        if (refs.isEmpty()) {
            // Not a query that returns nothing: `IN ()` does not parse, and a
            // caller with no facts is asking nothing rather than asking badly.
            return Sourcing.empty();
        }

        // One query for the whole page. A catalog index asks about every entity
        // in the version, and a page that issued a hundred round trips to say
        // where its numbers came from is a feature nobody leaves switched on.
        List<Sourced> rows = jdbc.query(
                """
                SELECT fp.fact_ref, p.slug, p.origin, p.detail, p.observed_on
                  FROM gamedata.fact_provenance fp
                  JOIN gamedata.provenance p
                    ON p.version_id = fp.version_id AND p.id = fp.provenance_id
                  JOIN gamedata.game_data_version v
                    ON v.id = fp.version_id
                 WHERE v.game_id = :game AND v.sequence = :sequence AND v.status = 'PUBLISHED'
                   AND fp.fact_ref IN (:refs)
                """,
                new MapSqlParameterSource()
                        .addValue("game", game.value())
                        .addValue("sequence", sequence)
                        .addValue("refs", refs),
                (rs, row) -> new Sourced(
                        rs.getString("fact_ref"),
                        rs.getString("slug"),
                        rs.getString("origin"),
                        rs.getString("detail"),
                        rs.getDate("observed_on").toLocalDate()));

        Map<String, Sourced> found = new LinkedHashMap<>();
        rows.forEach(row -> found.put(row.factRef(), row));

        // Keyed in the caller's order rather than the database's. The caller's
        // order is the order the facts appear on the page; a response whose
        // sourcing came back ordered by whatever the planner chose would read as
        // arbitrary, and would change between two runs of the same query.
        Map<String, Provenance> byFact = new LinkedHashMap<>();
        for (String ref : refs) {
            Sourced row = found.get(ref);
            if (row != null) {
                byFact.put(ref, row.provenance());
            }
        }
        return new Sourcing(byFact);
    }

    /** One row, before {@link Provenance}'s constructor has had a say about it. */
    private record Sourced(
            String factRef, String slug, String originName, String detail, LocalDate observedOn) {

        Provenance provenance() {
            Provenance.Origin origin;
            try {
                origin = Provenance.Origin.valueOf(originName);
            } catch (IllegalArgumentException unclassified) {
                origin = Provenance.Origin.UNRECORDED;
            }
            return new Provenance(slug, origin, detail, observedOn);
        }
    }
}
