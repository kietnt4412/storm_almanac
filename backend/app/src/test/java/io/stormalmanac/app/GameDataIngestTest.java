package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.gamedata.Upgrade;
import io.stormalmanac.gamedata.catalog.Entity;
import io.stormalmanac.gamedata.catalog.Skill;
import io.stormalmanac.gamedata.diff.Axis;
import io.stormalmanac.gamedata.diff.VersionDiff;
import io.stormalmanac.gamedata.ingest.CanonicalBundleParser;
import io.stormalmanac.gamedata.ingest.GameDataBundle;
import io.stormalmanac.gamedata.ingest.GameDataIngestRepository;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The first time the gamedata schema holds actual game data.
 *
 * <p>Until this test existed, "the schema is right" was a claim. Everything
 * before it proved that the migration applies and that its constraints reject
 * what they say they reject — neither of which is the same as a bundle going in
 * one side and coming out the other unchanged. A modelling mistake surfaces on
 * the first real ingest or it surfaces in phase 4 in front of a user.
 *
 * <p>The fixture is a synthetic title, invented here. Open question Q3 says to
 * assume the consolidated community bundles are not redistributable — they carry
 * no licence, and absence of a licence is not permission — so they are read to
 * validate the shape and never vendored. The fixture is built to exercise every
 * shape the format has instead: all four source kinds, both sink kinds, the
 * catalog axis, a rotating stage, an expiring event, and a banner with soft pity.
 */
class GameDataIngestTest extends GameDataDatabaseTest {

    static final GameId PROVING_GROUND = new GameId("proving-ground");

    @Autowired
    private GameDataIngestRepository ingest;

    @Autowired
    private GameDefinitionRepository definitions;

    @Test
    @DisplayName("a published version reads back as exactly the bundle that was ingested")
    void roundTripsWithoutLoss() {
        GameDataBundle bundle = bundle("proving-ground-1.0.json");
        ingest.ingestDraft(bundle);
        GameDataVersion published = ingest.publish(PROVING_GROUND, bundle.sequence());

        GameDefinition loaded = definitions.findLatest(PROVING_GROUND).orElseThrow();

        // Record equality across the whole graph. Anything the writer drops, the
        // reader mis-orders, or a column silently truncates fails right here,
        // which is worth more than twenty assertions on individual fields.
        assertThat(loaded).isEqualTo(bundle.definitionApprovedAt(published.publishedAt()));
    }

    @Test
    @DisplayName("a drop's sample size survives the schema, and an absent one stays absent")
    void sampleSizesRoundTrip() {
        // 1.1 is the fixture with a measured stage in it: pg-3-1's two drops say
        // they were observed over 4 200 runs, and every other drop in either
        // fixture declares its yield instead. Both readings have to survive, and
        // the failure mode is a column defaulting to zero and turning a
        // measurement into a declaration on the way through — which nothing
        // downstream would ever notice, because a declared yield is used as-is.
        GameDataBundle bundle = bundle("proving-ground-1.1.json");
        ingest.ingestDraft(bundle);
        GameDataVersion published = ingest.publish(PROVING_GROUND, bundle.sequence());

        GameDefinition loaded = definitions.findLatest(PROVING_GROUND).orElseThrow();

        assertThat(loaded).isEqualTo(bundle.definitionApprovedAt(published.publishedAt()));
        assertThat(dropOf(loaded, "pg-3-1", "shard").sampledRuns()).isEqualTo(4200);
        assertThat(dropOf(loaded, "pg-3-1", "sigil-radiant").sampledRuns()).isEqualTo(4200);
        assertThat(dropOf(loaded, "pg-1-1", "ore-rough").isSampled()).isFalse();
    }

    @Test
    @DisplayName("the schema answers what Insight 2 costs")
    void answersTheProgressionQuestion() {
        // Phase 1's exit criterion, asked of the data rather than of an endpoint.
        ingestAndPublish("proving-ground-1.0.json");
        GameDefinition data = definitions.findLatest(PROVING_GROUND).orElseThrow();

        Upgrade insightTwo = data.sinks().stream()
                .filter(Upgrade.class::isInstance).map(Upgrade.class::cast)
                .filter(u -> u.entity().equals(new EntityId("warden")) && u.toState().equals("insight-2"))
                .findFirst().orElseThrow();

        assertThat(insightTwo.costs()).containsExactly(
                stack("sigil-greater", 6), stack("ore-refined", 8), stack("gold", 20000));
    }

    @Test
    @DisplayName("the schema answers what a skill does at a given rank")
    void answersTheCatalogQuestion() {
        // The other half of the exit criterion, and the reason the catalog axis
        // was modelled now rather than retrofitted: it is a different question
        // over the same version, answered from the same load.
        ingestAndPublish("proving-ground-1.0.json");
        GameDefinition data = definitions.findLatest(PROVING_GROUND).orElseThrow();

        Skill.Rank rankTwo = data.entitiesById().get(new EntityId("warden"))
                .skills().getFirst().ranks().stream()
                .filter(rank -> rank.rank() == 2).findFirst().orElseThrow();

        assertThat(rankTwo.values()).containsEntry("damage", 1.32);
        assertThat(rankTwo.description()).contains("Scorch");
        assertThat(rankTwo.upgradeCost()).containsExactly(stack("sigil-lesser", 2), stack("gold", 3000));
    }

    @Test
    @DisplayName("equipment comes back as an Entity, with its kind intact")
    void equipmentIsAnEntity() {
        // ADR 0007 asserted this about the model. This is the first evidence it
        // survives a round trip through the schema.
        ingestAndPublish("proving-ground-1.0.json");
        GameDefinition data = definitions.findLatest(PROVING_GROUND).orElseThrow();

        Entity amulet = data.entitiesById().get(new EntityId("amulet-ember"));
        assertThat(amulet.kind()).isEqualTo("equipment");
        assertThat(amulet.element()).isEmpty();
        assertThat(amulet.statCurves()).singleElement()
                .satisfies(curve -> assertThat(curve.valueAt(0, 30)).isEqualTo(640.0));
    }

    @Test
    @DisplayName("a draft is invisible until a human approves it, however recently it was fetched")
    void latestMeansLatestApproved() {
        ingestAndPublish("proving-ground-1.0.json");
        ingest.ingestDraft(bundle("proving-ground-1.1.json"));

        // 1.1 is in the database, is newer, and is not the answer.
        assertThat(definitions.findLatest(PROVING_GROUND).orElseThrow().version().label()).isEqualTo("1.0");
        assertThat(definitions.versions(PROVING_GROUND)).extracting(GameDataVersion::label)
                .containsExactly("1.0");
        assertThat(ingest.drafts(PROVING_GROUND)).extracting(GameDataIngestRepository.DraftVersion::label)
                .containsExactly("1.1");

        ingest.publish(PROVING_GROUND, 1);

        assertThat(definitions.findLatest(PROVING_GROUND).orElseThrow().version().label()).isEqualTo("1.1");
        assertThat(definitions.versions(PROVING_GROUND)).extracting(GameDataVersion::label)
                .containsExactly("1.1", "1.0");
        assertThat(ingest.drafts(PROVING_GROUND)).isEmpty();
    }

    @Test
    @DisplayName("an old version stays readable and unchanged after a patch is published")
    void anOldVersionSurvivesThePatch() {
        // The reason a version is a full snapshot rather than a delta: a plan
        // computed against 1.0 has to still be answerable once 1.1 lands.
        ingestAndPublish("proving-ground-1.0.json");
        ingestAndPublish("proving-ground-1.1.json");

        GameDefinition old = definitions.find(PROVING_GROUND, 0).orElseThrow();

        assertThat(old.version().label()).isEqualTo("1.0");
        assertThat(yieldOf(old, "pg-1-1", "ore-rough")).isEqualTo(1.4);
        assertThat(yieldOf(definitions.findLatest(PROVING_GROUND).orElseThrow(), "pg-1-1", "ore-rough"))
                .isEqualTo(1.6);
        // 1.1 added an item; the old snapshot must not have grown one.
        assertThat(old.items()).hasSize(6);
        assertThat(definitions.findLatest(PROVING_GROUND).orElseThrow().items()).hasSize(7);
    }

    @Test
    @DisplayName("re-ingesting replaces a draft, and leaves nothing of the old one behind")
    void reIngestingReplacesADraft() {
        // A scheduled fetch re-runs. If a failed ingest wedged the sequence, the
        // only recovery would be manual SQL against a production schema.
        ingest.ingestDraft(bundle("proving-ground-1.0.json"));
        long itemsAfterFirst = rows("gamedata.item");

        ingest.ingestDraft(bundle("proving-ground-1.0.json"));

        assertThat(ingest.drafts(PROVING_GROUND)).hasSize(1);
        assertThat(rows("gamedata.item")).isEqualTo(itemsAfterFirst);
        assertThat(rows("gamedata.game_data_version")).isEqualTo(1);
    }

    @Test
    @DisplayName("an approved version cannot be re-ingested over")
    void publishedVersionsAreImmutable() {
        // Every plan and every drop estimate that recorded this sequence did so
        // on the promise that it still means what it meant.
        ingestAndPublish("proving-ground-1.0.json");

        assertThatThrownBy(() -> ingest.ingestDraft(bundle("proving-ground-1.0.json")))
                .isInstanceOf(GameDataIngestRepository.PublishedVersionIsImmutableException.class)
                .hasMessageContaining("sequence 0 is published");
    }

    @Test
    @DisplayName("publishing something that is not a draft fails loudly rather than quietly")
    void publishingTwiceIsRefused() {
        ingestAndPublish("proving-ground-1.0.json");

        assertThatThrownBy(() -> ingest.publish(PROVING_GROUND, 0))
                .isInstanceOf(GameDataIngestRepository.NoDraftToPublishException.class)
                .hasMessageContaining("no draft");
    }

    @Test
    @DisplayName("an ingest the database refuses leaves nothing behind, not even the version row")
    void ingestIsAllOrNothing() {
        // Two upgrades with different slugs describing the same edge. The parser
        // cannot object — the slugs are unique and every reference resolves — and
        // the schema's upgrade_edge_unique refuses it, because a second row is a
        // duplicate cost the planner would pay twice. The version row and the
        // items are written *before* the upgrades, so if the transaction is not
        // doing its job they survive.
        GameDataBundle contradictory = new CanonicalBundleParser().parse(
                """
                {
                  "game": { "id": "proving-ground", "displayName": "The Proving Ground",
                            "energyUnit": "Vigour" },
                  "sequence": 7,
                  "label": "broken",
                  "attribution": "hand-written",
                  "items": [
                    { "id": "gold", "displayName": "Gold", "rarity": { "label": "1*", "rank": 1 },
                      "category": "currency" }
                  ],
                  "entities": [
                    { "id": "warden", "displayName": "The Warden", "kind": "character",
                      "rarity": { "label": "5*", "rank": 5 } }
                  ],
                  "upgrades": [
                    { "id": "u-one", "entity": "warden", "fromState": "a", "toState": "b",
                      "costs": [ { "item": "gold", "quantity": 100 } ] },
                    { "id": "u-two", "entity": "warden", "fromState": "a", "toState": "b",
                      "costs": [ { "item": "gold", "quantity": 200 } ] }
                  ]
                }
                """);

        assertThatThrownBy(() -> ingest.ingestDraft(contradictory))
                .hasMessageContaining("upgrade_edge_unique");

        assertThat(rows("gamedata.game_data_version")).isZero();
        assertThat(rows("gamedata.item")).isZero();
        assertThat(rows("gamedata.entity")).isZero();
    }

    @Test
    @DisplayName("the patch diff over two versions loaded from the database is the same one")
    void theDiffSurvivesTheRoundTrip() {
        // GameDataDiffTest computes this from two files and asserts it in detail.
        // The claim here is narrower and is the one that could quietly stop being
        // true: the report a reviewer sees is computed from what the database
        // holds, so anything the schema loses shows up as a missing change here
        // rather than as a wrong plan in phase 2.
        ingestAndPublish("proving-ground-1.0.json");
        ingestAndPublish("proving-ground-1.1.json");

        GameDefinition one = definitions.find(PROVING_GROUND, 0).orElseThrow();
        GameDefinition two = definitions.findLatest(PROVING_GROUND).orElseThrow();

        VersionDiff fromTheDatabase = VersionDiff.between(one, two);
        VersionDiff fromTheFiles = VersionDiff.between(
                bundle("proving-ground-1.0.json").definitionApprovedAt(Instant.EPOCH),
                bundle("proving-ground-1.1.json").definitionApprovedAt(Instant.EPOCH));

        assertThat(fromTheDatabase.changes()).isEqualTo(fromTheFiles.changes());
        assertThat(fromTheDatabase.on(Axis.PROGRESSION)).isNotEmpty();
        assertThat(fromTheDatabase.on(Axis.CATALOG)).isNotEmpty();
    }

    @Test
    @DisplayName("a game with nothing published has no latest version, rather than an empty one")
    void unknownGameIsEmpty() {
        assertThat(definitions.findLatest(new GameId("no-such-game"))).isEmpty();
        assertThat(definitions.versions(new GameId("no-such-game"))).isEmpty();
    }

    // ── Fixtures ────────────────────────────────────────────────────────────

    private void ingestAndPublish(String fixture) {
        GameDataBundle bundle = bundle(fixture);
        ingest.ingestDraft(bundle);
        ingest.publish(PROVING_GROUND, bundle.sequence());
    }

    private static GameDataBundle bundle(String fixture) {
        try (InputStream in = GameDataIngestTest.class.getResourceAsStream("/gamedata/" + fixture)) {
            if (in == null) throw new IllegalStateException("fixture not on the classpath: " + fixture);
            return new CanonicalBundleParser().parse(in);
        } catch (IOException e) {
            throw new IllegalStateException("could not read " + fixture, e);
        }
    }

    private static io.stormalmanac.gamedata.ItemStack stack(String item, int quantity) {
        return new io.stormalmanac.gamedata.ItemStack(new ItemId(item), quantity);
    }

    private static double yieldOf(GameDefinition data, String stage, String item) {
        return dropOf(data, stage, item).expectedYield();
    }

    private static io.stormalmanac.gamedata.Drop dropOf(GameDefinition data, String stage, String item) {
        return data.stages().stream()
                .filter(s -> s.id().equals(stage))
                .flatMap(s -> s.drops().stream())
                .filter(d -> d.item().equals(new ItemId(item)))
                .findFirst().orElseThrow();
    }

    private long rows(String table) {
        return Optional.ofNullable(jdbc.queryForObject("SELECT count(*) FROM " + table, Long.class)).orElse(0L);
    }
}
