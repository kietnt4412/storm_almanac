package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import io.stormalmanac.api.gamedata.GameDataView.CostView;
import io.stormalmanac.api.gamedata.GameDataView.DiffResponse;
import io.stormalmanac.api.gamedata.GameDataView.EntitiesResponse;
import io.stormalmanac.api.gamedata.GameDataView.EntityResponse;
import io.stormalmanac.api.gamedata.GameDataView.GamesResponse;
import io.stormalmanac.api.gamedata.GameDataView.ItemsResponse;
import io.stormalmanac.api.gamedata.GameDataView.RankView;
import io.stormalmanac.api.gamedata.GameDataView.UpgradeStepView;
import io.stormalmanac.api.gamedata.GameDataView.UpgradesResponse;
import io.stormalmanac.api.gamedata.GameDataView.VersionsResponse;
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
    @DisplayName("a reader with no slug and no account can still find a game to read")
    void listsTheGamesWithSomethingPublished() {
        // The way in. Every read before phase 4 started from a slug the caller
        // already had, so a stranger arriving from a search had to guess a URL —
        // which is not a thing five strangers completing a plan would survive.
        publish("proving-ground-1.0.json");
        publish("proving-ground-1.1.json");

        GamesResponse games = ok(http.getForEntity("/api/games", GamesResponse.class));

        assertThat(games.games()).singleElement().satisfies(game -> {
            assertThat(game.id()).isEqualTo("proving-ground");
            assertThat(game.displayName()).isEqualTo("The Proving Ground");
            // The game's own word for energy, so a form can ask for it in the
            // noun the player reads on their own screen.
            assertThat(game.energyUnit()).isEqualTo("Vigour");
            // The version a reader lands on is the newest published one, not the
            // first, and it is attributed like every other answer.
            assertThat(game.latest().label()).isEqualTo("1.1");
            assertThat(game.latest().attribution()).isNotBlank();
        });
    }

    @Test
    @DisplayName("a game with nothing published is not on the index")
    void draftsAreNotAnIndexEntry() {
        // Same rule as every other read here: a draft has been fetched, not
        // approved, and an index that listed one would advertise data nobody
        // agreed to publish.
        ingest.ingestDraft(bundle("proving-ground-1.0.json"));

        assertThat(ok(http.getForEntity("/api/games", GamesResponse.class)).games()).isEmpty();
    }

    @Test
    @DisplayName("the item list is served rarest first, which is the order an inventory is counted in")
    void servesTheVocabularyAnInventoryIsWrittenIn() {
        // An inventory is a map of item slugs, and until phase 4 nothing served
        // the list those slugs come from: items reached a reader only as the
        // resolved name inside a cost. A bulk editor cannot be built on that.
        publish("proving-ground-1.0.json");

        ItemsResponse items = ok(http.getForEntity("/api/games/proving-ground/items", ItemsResponse.class));

        assertThat(items.version().label()).isEqualTo("1.0");
        // Rarest first, and inside a rarity by the name that is rendered rather
        // than the slug that is stored — "Ember Shard", "Lesser Sigil",
        // "Refined Ore" — because the order is for the eye reading the column.
        assertThat(items.items()).extracting("id")
                .containsExactly("sigil-greater", "shard", "sigil-lesser", "ore-refined", "ore-rough", "gold");
        assertThat(items.items().get(0).displayName()).isEqualTo("Greater Sigil");
        assertThat(items.items().get(0).rarity().rank()).isEqualTo(4);
        assertThat(items.items().get(0).category()).isEqualTo("insight");
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
        //
        // 403 and not 401, and the change is a correction rather than a
        // regression: CSRF runs before authorization, so an unauthenticated POST
        // is refused for having no token and never reaches the matcher. This
        // assertion read 401 until /error was permitted — the 403 was being
        // forwarded to an error page that itself demanded an account, and the
        // 401 anyone saw was the second refusal rather than the first. What
        // matters either way is that it is a denial and not a 405 or a 200.
        assertThat(http.postForEntity("/api/games/proving-ground/versions", "{}", String.class).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("a catalog page says where its numbers were read, and not merely who to credit")
    void carriesTheSourcingOfItsNumbers() {
        // ADR 0016 made a fact carry its provenance and made publish refuse a
        // version that is not ours. Both happen behind the maintainer's back, so
        // for two phases the sourcing was written and no reader could ask for
        // it. Attribution is not the same claim: it is one credit line per
        // version, and it cannot say that a banner's rates came from the
        // publisher's rules screen while the stage beside it came from somebody
        // counting runs.
        publish("proving-ground-1.0.json");

        EntityResponse warden = ok(http.getForEntity(
                "/api/games/proving-ground/entities/warden", EntityResponse.class));

        assertThat(warden.sourcing().facts())
                .as("the page's one fact, pointing at the record it was read under")
                .containsExactly(entry("entity:warden", "invented"));
        assertThat(warden.sourcing().sources()).singleElement().satisfies(source -> {
            assertThat(source.id()).isEqualTo("invented");
            assertThat(source.origin()).isEqualTo("AUTHORED_FIXTURE");
            // Derivable from the origin, and derived once. A client computing it
            // would be a second copy of Provenance.Origin.isFirstHand() in a
            // language that cannot be made to fail to compile when this one
            // gains a member.
            assertThat(source.firstHand()).isTrue();
            assertThat(source.detail()).contains("Written by hand for this repository");
            assertThat(source.observedOn()).isEqualTo(LocalDate.of(2026, 9, 5));
        });

        // The upgrade table is a different reading from the character page —
        // a levelling screen rather than a profile — so the response names both
        // facts rather than claiming the whole page at once. They happen to
        // share a record here because the fixture was written in one sitting.
        UpgradesResponse upgrades = ok(http.getForEntity(
                "/api/games/proving-ground/entities/warden/upgrades", UpgradesResponse.class));

        assertThat(upgrades.sourcing().facts())
                .containsKeys("entity:warden", "upgrade:warden-insight-1", "upgrade:warden-insight-2");
        assertThat(upgrades.sourcing().sources()).extracting("id").containsExactly("invented");

        // And the list an inventory is written in, which is where a reader
        // checking a quantity would look for it.
        ItemsResponse items = ok(http.getForEntity(
                "/api/games/proving-ground/items", ItemsResponse.class));
        assertThat(items.sourcing().facts()).containsKey("item:gold").hasSize(items.items().size());
    }

    @Test
    @DisplayName("a number this project did not source says so to the reader, in the same breath as the number")
    void namesDataThatIsNotOurs() {
        // The escape hatch exists so a cross-check can reach a published version
        // to be diffed against one (ADR 0015 forbids shipping somebody else's
        // data, not handling it). What it must not do is make the resulting page
        // indistinguishable from one carrying our own reading — which, before
        // this, it did: the gate refused at publish and said nothing afterwards.
        GameDataBundle borrowed = secondHand(bundle("proving-ground-1.0.json"));
        ingest.ingestDraft(borrowed);
        ingest.publish(PROVING_GROUND, borrowed.sequence(), true);

        EntityResponse warden = ok(http.getForEntity(
                "/api/games/proving-ground/entities/warden", EntityResponse.class));

        assertThat(warden.sourcing().sources()).singleElement().satisfies(source -> {
            assertThat(source.origin()).isEqualTo("THIRD_PARTY");
            assertThat(source.firstHand()).isFalse();
            assertThat(source.detail()).contains("Somebody else's numbers");
        });
    }

    @Test
    @DisplayName("a fact nobody sourced is absent from the sourcing rather than given an invented record")
    void silenceStaysSilent() {
        // Not a hypothetical. Every fact of a bundle ingested since V7 gets a
        // row, so the only way to reach this state is to have been published
        // before the rule existed — and TRACKER.md records a locally published
        // version in exactly that position. A version is immutable and the rules
        // for reading one are not, so a page has to survive meeting one.
        //
        // Deleting the row here is that older version, reproduced: the fact is
        // served, and nothing claims to know where it came from.
        publish("proving-ground-1.0.json");
        jdbc.update("DELETE FROM gamedata.fact_provenance WHERE fact_ref = 'entity:warden'");

        EntityResponse warden = ok(http.getForEntity(
                "/api/games/proving-ground/entities/warden", EntityResponse.class));

        assertThat(warden.entity().displayName())
                .as("the numbers are still served — an unsourced fact is not a broken page")
                .isEqualTo("The Warden");
        assertThat(warden.sourcing().facts())
                .as("absent, not present with a fabricated record: inventing an id would put a"
                        + " source in the list that nobody authored")
                .isEmpty();
        assertThat(warden.sourcing().sources()).isEmpty();
    }

    // ── Fixtures ────────────────────────────────────────────────────────────

    /** The same facts, sourced from somebody else. */
    private static GameDataBundle secondHand(GameDataBundle bundle) {
        return new GameDataBundle(
                bundle.game(), bundle.sequence(), bundle.label(), bundle.attribution(),
                List.of(new Provenance(
                        "borrowed",
                        Provenance.Origin.THIRD_PARTY,
                        "Somebody else's numbers, retyped. Which is the thing ADR 0015 is about.",
                        LocalDate.of(2026, 9, 9))),
                "borrowed",
                Map.of(),
                bundle.items(), bundle.sources(), bundle.sinks(),
                bundle.banners(), bundle.entities());
    }

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

    @Test
    @DisplayName("a missing page inside the public catalog is not found, not unauthorized")
    void aMissingPublicPathIsNotAnAuthenticationProblem() {
        // Boot renders a 404 by forwarding to /error, and until that path was
        // permitted the forward hit anyRequest().authenticated() and came back
        // 401. Every test in this suite passed anyway, because every one of them
        // asked for a path that exists — it took serving the frontend and
        // probing the routes a client would really ask for.
        //
        // The cost is not cosmetic: a client reads 401 as a dead session and
        // acts on it by sending the reader to a login page, so a catalog link
        // that has gone stale would log people out instead of 404ing.
        //
        // This asked for /api/games until phase 4, when that became the game
        // index and started answering 200. The example moved rather than the
        // assertion: what is under test is the forward to /error from inside the
        // public prefix, and any path in it that nothing serves proves it.
        assertThat(http.getForEntity("/api/games/proving-ground/nothing-here", String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(http.getForEntity("/api/games/no-such-game", String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        // Outside the public list the answer stays 401, and that is the design
        // rather than the same bug surviving: the matcher is the list of things
        // this application will answer to a stranger, so a path that is not on it
        // is refused without saying whether it exists.
        assertThat(http.getForEntity("/api/nonsense", String.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        // And permitting the error page does not make a real route public: one
        // that needs an account still answers 401, because that status is written
        // by the entry point rather than forwarded through /error.
        assertThat(http.getForEntity("/api/me", String.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
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
