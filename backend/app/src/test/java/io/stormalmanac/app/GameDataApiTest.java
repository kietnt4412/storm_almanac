package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;

import io.stormalmanac.api.gamedata.GameDataView.CostView;
import io.stormalmanac.api.gamedata.GameDataView.DiffResponse;
import io.stormalmanac.api.gamedata.GameDataView.EntitiesResponse;
import io.stormalmanac.api.gamedata.GameDataView.EntityResponse;
import io.stormalmanac.api.gamedata.GameDataView.RankView;
import io.stormalmanac.api.gamedata.GameDataView.UpgradeStepView;
import io.stormalmanac.api.gamedata.GameDataView.UpgradesResponse;
import io.stormalmanac.api.gamedata.GameDataView.VersionsResponse;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.gamedata.ingest.CanonicalBundleParser;
import io.stormalmanac.gamedata.ingest.GameDataBundle;
import io.stormalmanac.gamedata.ingest.GameDataIngestRepository;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Phase 1's exit criterion, asked the way the criterion words it.
 *
 * <p>{@code GameDataIngestTest} asks the same two questions — what does Insight
 * 2 cost, what does her S2 do at rank 3 — of the repository, and answering them
 * there proved the schema. The criterion says <em>the API</em> answers them, and
 * everything between a repository call and an HTTP response is exactly the
 * ground that has caught defects in this project before: the security chain
 * answering 401 to the one route phase 0 existed to deploy was invisible to a
 * test that called the controller method directly.
 *
 * <p>So these go over real HTTP, anonymously, against a real database, and they
 * assert on the wire format rather than on the domain. If the read model stops
 * resolving an item's name, or a response stops carrying the version its
 * numbers came from, that is a broken promise to a reader and it fails here.
 */
class GameDataApiTest extends SharedDatabaseTest {

    private static final GameId PROVING_GROUND = new GameId("proving-ground");

    @Autowired
    private GameDataIngestRepository ingest;

    @Autowired
    private TestRestTemplate http;

    @Test
    @DisplayName("the API answers what Insight 2 costs, in items a reader can name")
    void answersTheProgressionQuestion() {
        publish("proving-ground-1.0.json");

        UpgradesResponse upgrades = ok(http.getForEntity(
                "/api/games/proving-ground/entities/warden/upgrades", UpgradesResponse.class));

        UpgradeStepView insightTwo = upgrades.steps().stream()
                .filter(step -> step.toState().equals("insight-2"))
                .findFirst().orElseThrow();

        // Names resolved, not item slugs: the read model exists so that every
        // client does not fetch the item table and join it by hand.
        assertThat(insightTwo.costs()).containsExactly(
                new CostView("sigil-greater", "Greater Sigil", 6),
                new CostView("ore-refined", "Refined Ore", 8),
                new CostView("gold", "Gold", 20000));

        // What the whole path costs, which is the question somebody actually has.
        assertThat(upgrades.totalCost()).containsExactly(
                new CostView("sigil-lesser", "Lesser Sigil", 4),
                new CostView("gold", "Gold", 25000),
                new CostView("sigil-greater", "Greater Sigil", 6),
                new CostView("ore-refined", "Refined Ore", 8));
    }

    @Test
    @DisplayName("the API answers what a skill does at a given rank")
    void answersTheCatalogQuestion() {
        publish("proving-ground-1.0.json");

        EntityResponse warden = ok(http.getForEntity(
                "/api/games/proving-ground/entities/warden", EntityResponse.class));

        RankView rankTwo = warden.entity().skills().getFirst().ranks().stream()
                .filter(rank -> rank.rank() == 2).findFirst().orElseThrow();

        assertThat(rankTwo.values()).containsEntry("damage", 1.32);
        assertThat(rankTwo.description()).contains("Scorch");
        assertThat(rankTwo.upgradeCost()).containsExactly(
                new CostView("sigil-lesser", "Lesser Sigil", 2), new CostView("gold", "Gold", 3000));

        // The catalog axis is served whole: a page needs the curve and the
        // talent as much as the skill, and they come from one load of one version.
        assertThat(warden.entity().statCurves()).singleElement()
                .satisfies(curve -> assertThat(curve.stat()).isEqualTo("atk"));
        assertThat(warden.entity().talents()).extracting("displayName").containsExactly("Poise");
    }

    @Test
    @DisplayName("every answer says which version its numbers came from, and who to credit")
    void everyAnswerIsAttributed() {
        // "Numbers and text only, attributed" is a project invariant. It stops
        // being a claim about ingestion the moment numbers are served to a
        // stranger, and this is the assertion that keeps it true at the edge.
        publish("proving-ground-1.0.json");

        EntitiesResponse catalog = ok(http.getForEntity(
                "/api/games/proving-ground/entities", EntitiesResponse.class));

        assertThat(catalog.version().label()).isEqualTo("1.0");
        assertThat(catalog.version().attribution()).contains("Synthetic fixture authored for Storm Almanac");
        assertThat(catalog.version().publishedAt()).isNotNull();
        assertThat(catalog.entities()).extracting("id").containsExactly("warden", "amulet-ember");
        assertThat(catalog.entities()).extracting("kind").containsExactly("character", "equipment");
    }

    @Test
    @DisplayName("an old version stays addressable after a patch lands, and still says the old numbers")
    void anOldVersionIsStillReadable() {
        // A plan computed against 1.0 has to remain explainable once 1.1 is out.
        // A URL that cannot name its patch cannot be cited in a bug report either.
        publish("proving-ground-1.0.json");
        publish("proving-ground-1.1.json");

        assertThat(goldCostOfInsightTwo("?version=0")).isEqualTo(20000);
        assertThat(goldCostOfInsightTwo("")).isEqualTo(18000);

        EntityResponse pinned = ok(http.getForEntity(
                "/api/games/proving-ground/entities/warden?version=0", EntityResponse.class));
        assertThat(pinned.version().label()).isEqualTo("1.0");
        assertThat(damageAtRankTwo(pinned)).isEqualTo(1.32);

        EntityResponse latest = ok(http.getForEntity(
                "/api/games/proving-ground/entities/warden", EntityResponse.class));
        assertThat(latest.version().label()).isEqualTo("1.1");
        assertThat(damageAtRankTwo(latest)).isEqualTo(1.28);
    }

    @Test
    @DisplayName("the version list is newest first and carries every published snapshot")
    void listsPublishedVersions() {
        publish("proving-ground-1.0.json");
        publish("proving-ground-1.1.json");

        VersionsResponse versions = ok(http.getForEntity(
                "/api/games/proving-ground/versions", VersionsResponse.class));

        assertThat(versions.versions()).extracting("label").containsExactly("1.1", "1.0");
    }

    @Test
    @DisplayName("the patch report renders over HTTP as the same text the CLI prints")
    void rendersTheDiffReport() {
        publish("proving-ground-1.0.json");
        publish("proving-ground-1.1.json");

        HttpHeaders wantsText = new HttpHeaders();
        wantsText.setAccept(java.util.List.of(MediaType.TEXT_PLAIN));
        ResponseEntity<String> report = http.exchange(
                "/api/games/proving-ground/diff?from=0&to=1", HttpMethod.GET,
                new HttpEntity<>(wantsText), String.class);

        assertThat(report.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(report.getBody())
                .contains("proving-ground: 1.0 → 1.1")
                .contains("progression")
                .contains("- stage 'pg-event-1'")
                .contains("~ upgrade 'warden-insight-2' · cost gold: 20000 → 18000")
                .contains("catalog")
                .contains("~ entity 'warden' · skill warden-strike rank 2 damage: 1.32 → 1.28");
    }

    @Test
    @DisplayName("the same diff is available as structured changes, on both axes")
    void servesTheDiffAsData() {
        publish("proving-ground-1.0.json");
        publish("proving-ground-1.1.json");

        DiffResponse diff = ok(http.getForEntity(
                "/api/games/proving-ground/diff?from=0&to=1", DiffResponse.class));

        assertThat(diff.from().label()).isEqualTo("1.0");
        assertThat(diff.to().label()).isEqualTo("1.1");
        assertThat(diff.changes()).extracting("axis").contains("PROGRESSION", "CATALOG");
        assertThat(diff.changes()).anySatisfy(change -> {
            assertThat(change.subject()).isEqualTo("upgrade 'warden-insight-2'");
            assertThat(change.detail()).isEqualTo("cost gold");
            assertThat(change.before()).isEqualTo("20000");
            assertThat(change.after()).isEqualTo("18000");
        });
    }

    @Test
    @DisplayName("what is not there is a 404 that names what was missing")
    void missingThingsSayWhatIsMissing() {
        // A bare 404 tells a caller to guess. These messages are written to be
        // acted on, the same way the CLI's refusals are.
        publish("proving-ground-1.0.json");

        assertThat(body("/api/games/proving-ground/entities/nobody"))
                .contains("no entity 'nobody' in proving-ground 1.0");
        assertThat(body("/api/games/proving-ground/entities/warden?version=99"))
                .contains("no published version 99 of proving-ground");
        assertThat(body("/api/games/no-such-game/versions"))
                .contains("nothing published for no-such-game");
    }

    @Test
    @DisplayName("a draft is not served, however recently it was fetched")
    void draftsAreNotServed() {
        // The whole point of the draft/approve split, checked at the edge:
        // findLatest is not the only way to reach the schema any more.
        ingest.ingestDraft(bundle("proving-ground-1.0.json"));

        assertThat(http.getForEntity("/api/games/proving-ground/versions", String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(http.getForEntity("/api/games/proving-ground/entities", String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("published data is readable with no account, and is not writable with one")
    void readableAnonymouslyAndReadOnly() {
        publish("proving-ground-1.0.json");

        // No credentials, and none needed: a stranger arriving from a search
        // does not have an account and the funnel does not survive a login wall.
        assertThat(http.getForEntity("/api/games/proving-ground/versions", String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);

        // Only GET is public. Publishing is a human approval through the CLI and
        // has no endpoint, so this is denied rather than merely unmapped.
        assertThat(http.postForEntity("/api/games/proving-ground/versions", "{}", String.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ── Fixtures ────────────────────────────────────────────────────────────

    private void publish(String fixture) {
        GameDataBundle bundle = bundle(fixture);
        ingest.ingestDraft(bundle);
        ingest.publish(PROVING_GROUND, bundle.sequence());
    }

    private static GameDataBundle bundle(String fixture) {
        try (InputStream in = GameDataApiTest.class.getResourceAsStream("/gamedata/" + fixture)) {
            if (in == null) throw new IllegalStateException("fixture not on the classpath: " + fixture);
            return new CanonicalBundleParser().parse(in);
        } catch (IOException e) {
            throw new IllegalStateException("could not read " + fixture, e);
        }
    }

    private static <T> T ok(ResponseEntity<T> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private String body(String path) {
        ResponseEntity<String> response = http.getForEntity(path, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        return response.getBody();
    }

    private int goldCostOfInsightTwo(String query) {
        UpgradesResponse upgrades = ok(http.getForEntity(
                "/api/games/proving-ground/entities/warden/upgrades" + query, UpgradesResponse.class));

        return upgrades.steps().stream()
                .filter(step -> step.toState().equals("insight-2"))
                .flatMap(step -> step.costs().stream())
                .filter(cost -> cost.item().equals("gold"))
                .mapToInt(CostView::quantity)
                .findFirst().orElseThrow();
    }

    private static double damageAtRankTwo(EntityResponse entity) {
        return entity.entity().skills().getFirst().ranks().stream()
                .filter(rank -> rank.rank() == 2)
                .findFirst().orElseThrow()
                .values().get("damage");
    }
}
