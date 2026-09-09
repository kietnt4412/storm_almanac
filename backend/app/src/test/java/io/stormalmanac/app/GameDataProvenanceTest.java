package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.common.id.GameId;
import io.stormalmanac.gamedata.Provenance;
import io.stormalmanac.gamedata.ingest.CanonicalBundleParser;
import io.stormalmanac.gamedata.ingest.GameDataBundle;
import io.stormalmanac.gamedata.ingest.GameDataIngestRepository;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The mechanism that makes {@link io.stormalmanac.gamedata.Provenance ADR 0015}
 * enforceable rather than merely stated.
 *
 * <p>0015 says the shipped product carries only game data this project sourced
 * itself. The problem it leaves is that a number read off a stage screen and a
 * number copied out of an aggregator are byte-identical once typed, so the
 * decision cannot be checked by reading the data — which means it cannot be
 * checked at all unless the sourcing travels with it. ADR 0016 is that
 * mechanism, and this is where it is proven against a real database rather than
 * against a record's constructor.
 *
 * <p>The interesting assertions here are the refusals. A gate that lets the
 * right thing through is easy; the one worth testing is the one that stops the
 * wrong thing and says which fact was wrong.
 */
class GameDataProvenanceTest extends SharedDatabaseTest {

    private static final GameId PROVING_GROUND = new GameId("proving-ground");

    @Autowired
    private GameDataIngestRepository ingest;

    @Test
    @DisplayName("every fact of an ingested version has a provenance row, including the ones that took the default")
    void materialisesTheDefault() {
        GameDataBundle bundle = bundle("proving-ground-1.1.json");
        long version = ingest.ingestDraft(bundle);

        // The default is an authoring convenience and the database does not
        // inherit it: "where did this fact come from" has to be answerable
        // without the bundle beside it. See V7's header.
        Integer rows = jdbc.queryForObject(
                "SELECT count(*) FROM gamedata.fact_provenance WHERE version_id = ?",
                Integer.class, version);

        assertThat(rows)
                .as("one row per declared fact, not one per fact that overrode the default")
                .isEqualTo(bundle.factRefs().size());
        assertThat(bundle.factProvenance())
                .as("and the fixture really does leave almost all of them to the default")
                .hasSize(1);
    }

    @Test
    @DisplayName("a fact that overrode the default keeps its own provenance through the schema")
    void keepsTheOverride() {
        long version = ingest.ingestDraft(bundle("proving-ground-1.1.json"));

        assertThat(originOf(version, "banner:warden-debut"))
                .as("the banner's rates come from the publisher's rules screen")
                .isEqualTo("PUBLISHER_DISCLOSURE");
        assertThat(originOf(version, "stage:pg-1-1"))
                .as("and the stage beside it does not — which is the whole point of per-fact provenance")
                .isEqualTo("AUTHORED_FIXTURE");
    }

    @Test
    @DisplayName("a draft carrying somebody else's data is refused publication, and the refusal names the facts")
    void refusesSecondHandData() {
        ingest.ingestDraft(secondHand(bundle("proving-ground-1.0.json")));

        assertThatThrownBy(() -> ingest.publish(PROVING_GROUND, 0))
                .isInstanceOf(GameDataIngestRepository.SecondHandDataException.class)
                // Counted in full and listed in part. An operator told "this
                // draft is not first-hand" can do nothing; one told which facts
                // and which source can go and read the screen — and one told
                // only the first five still needs to know it is twenty and not
                // five, because those are different decisions.
                .hasMessageContaining("20 fact(s)")
                .hasMessageContaining("banner:warden-debut (borrowed)")
                .hasMessageContaining("and more")
                .hasMessageContaining("ADR 0015");
    }

    @Test
    @DisplayName("the same draft publishes when the caller says out loud that it is second-hand")
    void publishesSecondHandDataOnlyOnDemand() {
        ingest.ingestDraft(secondHand(bundle("proving-ground-1.0.json")));

        // The escape hatch exists because a cross-check has to reach a published
        // version to be diffed against one — ADR 0015 forbids shipping somebody
        // else's data, not handling it. What it must never be is the quiet path.
        assertThatCode(() -> ingest.publish(PROVING_GROUND, 0, true)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("a bundle that says nothing about where its facts came from cannot be published")
    void refusesSilence() {
        // Silence parses, so a throwaway bundle costs no ceremony, and it is not
        // first-hand, so it cannot reach anybody. An absent record is not a
        // record. See Provenance.Origin.UNRECORDED.
        GameDataBundle silent = strip(bundle("proving-ground-1.0.json"));
        assertThat(silent.provenanceOf("item:gold").origin())
                .isEqualTo(Provenance.Origin.UNRECORDED);

        ingest.ingestDraft(silent);

        assertThatThrownBy(() -> ingest.publish(PROVING_GROUND, 0))
                .isInstanceOf(GameDataIngestRepository.SecondHandDataException.class)
                .hasMessageContaining("unrecorded");
    }

    private String originOf(long version, String factRef) {
        return jdbc.queryForObject(
                """
                SELECT p.origin
                  FROM gamedata.fact_provenance fp
                  JOIN gamedata.provenance p
                    ON p.version_id = fp.version_id AND p.id = fp.provenance_id
                 WHERE fp.version_id = ? AND fp.fact_ref = ?
                """,
                String.class, version, factRef);
    }

    /** The same facts, sourced from somebody else. */
    private static GameDataBundle secondHand(GameDataBundle bundle) {
        return rewrite(bundle, List.of(new Provenance(
                "borrowed",
                Provenance.Origin.THIRD_PARTY,
                "Somebody else's numbers, retyped. Which is the thing ADR 0015 is about.",
                LocalDate.of(2026, 9, 9))), "borrowed");
    }

    /** The same facts, with nothing said about where they came from. */
    private static GameDataBundle strip(GameDataBundle bundle) {
        return rewrite(bundle, List.of(), "");
    }

    private static GameDataBundle rewrite(
            GameDataBundle bundle, List<Provenance> provenance, String sourcedBy) {
        return new GameDataBundle(
                bundle.game(), bundle.sequence(), bundle.label(), bundle.attribution(),
                provenance, sourcedBy, Map.of(),
                bundle.items(), bundle.sources(), bundle.sinks(),
                bundle.banners(), bundle.entities());
    }

    private static GameDataBundle bundle(String fixture) {
        try (InputStream in = GameDataProvenanceTest.class
                .getResourceAsStream("/gamedata/" + fixture)) {
            return new CanonicalBundleParser().parse(in);
        } catch (IOException e) {
            throw new IllegalStateException("fixture " + fixture + " is not readable", e);
        }
    }
}
