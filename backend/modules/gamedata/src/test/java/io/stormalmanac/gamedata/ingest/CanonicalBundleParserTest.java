package io.stormalmanac.gamedata.ingest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.gamedata.Availability;
import io.stormalmanac.gamedata.Stage;
import java.time.DayOfWeek;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The bundle format, and — mostly — what it says when a bundle is wrong.
 *
 * <p>Ingestion is a tool a human runs against a file somebody else published,
 * so the rejections matter more than the acceptances. Every assertion below is
 * on the <em>message</em>: a parser that refuses a bad bundle without saying
 * which line is bad has moved the work rather than done it. The composite
 * foreign keys would catch most of this too, several seconds later, and report
 * a constraint name.
 */
class CanonicalBundleParserTest {

    private final CanonicalBundleParser parser = new CanonicalBundleParser();

    @Test
    @DisplayName("a well-formed bundle parses into the domain records unchanged")
    void parsesTheHappyPath() {
        GameDataBundle bundle = parser.parse(
                """
                {
                  "game": { "id": "t", "displayName": "Test", "energyUnit": "Vigour" },
                  "sequence": 3,
                  "label": "1.4",
                  "attribution": "hand-written",
                  "items": [
                    { "id": "ore", "displayName": "Ore", "rarity": { "label": "2*", "rank": 2 },
                      "category": "material" }
                  ],
                  "stages": [
                    { "id": "1-1", "displayName": "First", "energyCost": 10,
                      "drops": [ { "item": "ore", "expectedYield": 1.4 } ],
                      "availability": { "days": ["MONDAY", "THURSDAY"] } }
                  ]
                }
                """);

        assertThat(bundle.game().energyUnit()).isEqualTo("Vigour");
        assertThat(bundle.sequence()).isEqualTo(3);
        assertThat(bundle.label()).isEqualTo("1.4");
        assertThat(bundle.items()).singleElement()
                .satisfies(item -> assertThat(item.rarity().rank()).isEqualTo(2));

        Stage stage = (Stage) bundle.sources().getFirst();
        assertThat(stage.drops().getFirst().expectedYield()).isEqualTo(1.4);
        assertThat(stage.availability().days()).containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.THURSDAY);
    }

    @Test
    @DisplayName("an omitted availability means the source is always open")
    void availabilityDefaultsToAlways() {
        // Nearly every source in a real bundle is always available. Spelling it
        // out on each one would bury the handful that actually rotate or expire.
        GameDataBundle bundle = parser.parse(minimal("""
                "stages": [ { "id": "1-1", "displayName": "First", "energyCost": 10, "drops": [] } ]
                """));

        assertThat(bundle.sources().getFirst().availability()).isEqualTo(Availability.ALWAYS);
    }

    @Test
    @DisplayName("a reference to an item the bundle does not define names the item and the referrer")
    void danglingItemReferenceNamesBoth() {
        assertThatThrownBy(() -> parser.parse(minimal("""
                "stages": [ { "id": "1-1", "displayName": "First", "energyCost": 10,
                              "drops": [ { "item": "sulfr", "expectedYield": 1.0 } ] } ]
                """)))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("stage '1-1' drops unknown item 'sulfr'");
    }

    @Test
    @DisplayName("every dangling reference is reported, not just the first one")
    void everyDanglingReferenceIsReported() {
        // An ingest that fails one typo at a time costs an operator one round
        // trip per typo. The database would do exactly that.
        assertThatThrownBy(() -> parser.parse(minimal("""
                "stages": [ { "id": "1-1", "displayName": "First", "energyCost": 10,
                              "drops": [ { "item": "nope-one", "expectedYield": 1.0 } ] } ],
                "rewards": [ { "id": "daily", "cadence": "DAILY",
                               "grants": [ { "item": "nope-two", "quantity": 1 } ] } ]
                """)))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("references 2 thing(s)")
                .hasMessageContaining("nope-one")
                .hasMessageContaining("nope-two");
    }

    @Test
    @DisplayName("an upgrade of an entity the bundle does not define is rejected")
    void danglingEntityReference() {
        assertThatThrownBy(() -> parser.parse(minimal("""
                "upgrades": [ { "id": "u1", "entity": "ghost", "fromState": "a", "toState": "b",
                                "costs": [] } ]
                """)))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("upgrade 'u1' advances unknown entity 'ghost'");
    }

    @Test
    @DisplayName("a malformed field is reported with its position in the file")
    void malformedFieldNamesItsPosition() {
        assertThatThrownBy(() -> parser.parse(minimal("""
                "stages": [ { "id": "1-1", "displayName": "First", "energyCost": 10, "drops": [] },
                            { "id": "1-2", "displayName": "Second", "energyCost": "ten", "drops": [] } ]
                """)))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("stages[1].energyCost must be a whole number");
    }

    @Test
    @DisplayName("a domain record's own validation is reported against the element that broke it")
    void domainValidationIsAttributedToItsElement() {
        // Drop's constructor rejects a negative yield with a good message. What
        // it cannot know is which of four hundred stages carried the value.
        assertThatThrownBy(() -> parser.parse(minimal("""
                "stages": [ { "id": "1-1", "displayName": "First", "energyCost": 10,
                              "drops": [ { "item": "ore", "expectedYield": -1.0 } ] } ]
                """)))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("stages[0].drops[0]")
                .hasMessageContaining("expectedYield must be finite and non-negative");
    }

    @Test
    @DisplayName("an unattributed bundle is refused, because attribution is a project invariant")
    void attributionIsRequired() {
        assertThatThrownBy(() -> parser.parse(
                """
                {
                  "game": { "id": "t", "displayName": "Test", "energyUnit": "Vigour" },
                  "sequence": 0,
                  "items": []
                }
                """))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("attribution is required");
    }

    @Test
    @DisplayName("two items sharing a slug are refused before the unique index has to say so")
    void duplicateSlugsAreRefused() {
        assertThatThrownBy(() -> parser.parse(
                """
                {
                  "game": { "id": "t", "displayName": "Test", "energyUnit": "Vigour" },
                  "sequence": 0,
                  "attribution": "hand-written",
                  "items": [
                    { "id": "ore", "displayName": "Ore", "rarity": { "label": "2*", "rank": 2 },
                      "category": "material" },
                    { "id": "ore", "displayName": "Ore, again", "rarity": { "label": "3*", "rank": 3 },
                      "category": "material" }
                  ]
                }
                """))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("duplicate item 'ore'");
    }

    @Test
    @DisplayName("an entity without a kind is refused rather than defaulted")
    void kindIsRequired() {
        // ADR 0007: kind routes the catalog and is game-supplied. A bundle that
        // omits it produces pages nothing can group or link to, and defaulting
        // it to "character" would file every psychube under the wrong shape.
        assertThatThrownBy(() -> parser.parse(minimal("""
                "entities": [ { "id": "e1", "displayName": "Nameless",
                                "rarity": { "label": "5*", "rank": 5 } } ]
                """)))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("entities[0].kind is required");
    }

    @Test
    @DisplayName("a bundle becomes a GameDefinition only once an approval time exists")
    void aBundleIsNotYetADefinition() {
        // The type system saying what the schema's published_has_timestamp
        // constraint says: an unapproved snapshot has no definition at all.
        GameDataBundle bundle = parser.parse(minimal(""));
        Instant approvedAt = Instant.parse("2026-09-05T12:00:00Z");

        assertThat(bundle.definitionApprovedAt(approvedAt).version().publishedAt()).isEqualTo(approvedAt);
    }

    @Test
    @DisplayName("a craft that consumes nothing is refused, because free supply is unbounded supply")
    void aCraftMustConsumeSomething() {
        // Found by the optimizer's first solve over a real upstream, not by
        // reading the format. Kornblume lists base materials in the same file as
        // its recipes, with an empty material array; converted faithfully, one of
        // them became a craft that made currency out of nothing at zero energy,
        // and the solver ran it 294 250 times. The refusal belongs here rather
        // than in the solver: a bundle carrying such a row is wrong before
        // anybody solves anything with it.
        assertThatThrownBy(() -> parser.parse(minimal("""
                "crafts": [ { "id": "mint", "consumes": [],
                              "produces": [ { "item": "ore", "quantity": 1 } ] } ]
                """)))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("crafts[0].consumes is empty")
                .hasMessageContaining("unbounded free supply");
    }

    @Test
    @DisplayName("a craft that produces nothing is refused too, for the mirror-image reason")
    void aCraftMustProduceSomething() {
        assertThatThrownBy(() -> parser.parse(minimal("""
                "crafts": [ { "id": "burn", "consumes": [ { "item": "ore", "quantity": 1 } ],
                              "produces": [] } ]
                """)))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("crafts[0].produces is empty");
    }

    @Test
    @DisplayName("a file that is not JSON is reported as that, not as a missing field")
    void notJsonAtAll() {
        assertThatThrownBy(() -> parser.parse("<html>404 Not Found</html>"))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("not valid JSON");
    }

    /** A bundle with one item defined, plus whatever the test wants to add. */
    private static String minimal(String extra) {
        return """
                {
                  "game": { "id": "t", "displayName": "Test", "energyUnit": "Vigour" },
                  "sequence": 0,
                  "attribution": "hand-written",
                  "items": [
                    { "id": "ore", "displayName": "Ore", "rarity": { "label": "2*", "rank": 2 },
                      "category": "material" }
                  ]%s
                }
                """.formatted(extra.isBlank() ? "" : "," + System.lineSeparator() + extra);
    }
}
