package io.stormalmanac.gamedata.ingest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.gamedata.Provenance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What a bundle says about where its facts came from, without a database.
 *
 * <p>{@code GameDataProvenanceTest} proves the same idea survives the schema and
 * stops a publish. This proves the reasoning underneath it — which fact resolves
 * to which record, and which half-finished edits are refused — at the speed a
 * failure is worth diagnosing at.
 */
class BundleProvenanceTest {

    private final CanonicalBundleParser parser = new CanonicalBundleParser();

    @Test
    @DisplayName("a bundle that declares nothing is unrecorded, which is not first-hand")
    void silenceIsNotAnAssertion() {
        // The failure mode this defends against: a bundle that says nothing is
        // read as saying everything is fine. Absence gets a meaning instead of
        // being a hole, and the meaning is the conservative one.
        GameDataBundle bundle = parser.parse(minimal(""));

        assertThat(bundle.provenanceOf("item:ore").origin()).isEqualTo(Provenance.Origin.UNRECORDED);
        assertThat(bundle.isFirstHand()).isFalse();
        assertThat(bundle.secondHandFacts()).containsExactly("item:ore", "stage:1-1");
    }

    @Test
    @DisplayName("a fact takes the bundle's default unless it names its own")
    void overridesTheDefaultPerFact() {
        GameDataBundle bundle = parser.parse(minimal(
                """
                "provenance": [
                  { "id": "screens", "origin": "OBSERVED_IN_GAME",
                    "detail": "Stage list, read on 3.5", "observedOn": "2026-09-09" },
                  { "id": "borrowed", "origin": "THIRD_PARTY",
                    "detail": "Somebody else's number", "observedOn": "2026-09-09" }
                ],
                "sourcedBy": "screens",
                "factProvenance": { "item:ore": "borrowed" },
                """));

        assertThat(bundle.provenanceOf("item:ore").id()).isEqualTo("borrowed");
        assertThat(bundle.provenanceOf("stage:1-1").id()).isEqualTo("screens");
        assertThat(bundle.secondHandFacts())
                .as("one fact is somebody else's, and the bundle can say which")
                .containsExactly("item:ore");
        assertThat(bundle.factsByProvenance().values()).containsExactly(1L, 1L);
    }

    @Test
    @DisplayName("an override naming a fact the bundle does not declare is refused")
    void refusesADanglingOverride() {
        // It reads as a deliberate exception and behaves as the default, so the
        // bundle would claim first-hand sourcing for a fact somebody had
        // explicitly marked otherwise. Silent, and in the direction that matters.
        assertThatThrownBy(() -> parser.parse(minimal(
                """
                "provenance": [
                  { "id": "screens", "origin": "OBSERVED_IN_GAME",
                    "detail": "Stage list", "observedOn": "2026-09-09" }
                ],
                "sourcedBy": "screens",
                "factProvenance": { "stage:9-9": "screens" },
                """)))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("stage:9-9")
                .hasMessageContaining("does not declare");
    }

    @Test
    @DisplayName("declaring provenance and then not saying which one covers a fact is refused")
    void refusesADeclarationWithNoDefault() {
        assertThatThrownBy(() -> parser.parse(minimal(
                """
                "provenance": [
                  { "id": "screens", "origin": "OBSERVED_IN_GAME",
                    "detail": "Stage list", "observedOn": "2026-09-09" }
                ],
                """)))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("sourcedBy");
    }

    @Test
    @DisplayName("'unrecorded' cannot be redefined as something first-hand")
    void refusesRedefiningTheReservedId() {
        // Otherwise the one slug whose meaning is fixed becomes the one meaning
        // whatever a bundle wants it to mean.
        assertThatThrownBy(() -> parser.parse(minimal(
                """
                "provenance": [
                  { "id": "unrecorded", "origin": "OBSERVED_IN_GAME",
                    "detail": "Nothing to see here", "observedOn": "2026-09-09" }
                ],
                "sourcedBy": "unrecorded",
                """)))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("reserved");
    }

    @Test
    @DisplayName("an origin outside the known set names the ones that exist")
    void refusesAnUnknownOrigin() {
        assertThatThrownBy(() -> parser.parse(minimal(
                """
                "provenance": [
                  { "id": "hearsay", "origin": "SOMEBODY_TOLD_ME",
                    "detail": "A friend said", "observedOn": "2026-09-09" }
                ],
                "sourcedBy": "hearsay",
                """)))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("OBSERVED_IN_GAME");
    }

    /** Two facts and nothing else, so the provenance block is what is under test. */
    private static String minimal(String provenance) {
        return """
                {
                  "game": { "id": "t", "displayName": "Test", "energyUnit": "Vigour" },
                  "sequence": 0,
                  "label": "1.0",
                  "attribution": "hand-written",
                  %s
                  "items": [
                    { "id": "ore", "displayName": "Ore", "rarity": { "label": "2*", "rank": 2 },
                      "category": "material" }
                  ],
                  "stages": [
                    { "id": "1-1", "displayName": "First", "energyCost": 10,
                      "drops": [ { "item": "ore", "expectedYield": 1.4 } ] }
                  ]
                }
                """.formatted(provenance);
    }
}
