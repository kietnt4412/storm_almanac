package io.stormalmanac.gamedata.ingest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.Word;
import io.stormalmanac.gamedata.Word.Subject;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Words for the bundle's own keys (ADR 0033), and the ways of writing one that
 * are worse than writing none.
 *
 * <p>The failure worth a test is the one {@link ProgressKindTest} guards for
 * progress names: a word for a key nothing uses reads as a fix, is never looked
 * up, and the heading somebody wrote it for goes on rendering its slug.
 */
class WordTest {

    private final CanonicalBundleParser parser = new CanonicalBundleParser();

    /** A measure a reward is scored on, two item categories and an entity kind, and room for words. */
    private static String bundleWith(String words) {
        return """
                {
                  "game": { "id": "t", "displayName": "Test", "energyUnit": "Vigour" },
                  "sequence": 1,
                  "attribution": "hand-written",
                  "items": [
                    { "id": "pod-m", "displayName": "Pod (M)", "rarity": { "label": "3*", "rank": 3 },
                      "category": "pod-m" },
                    { "id": "pod-l", "displayName": "Pod (L)", "rarity": { "label": "4*", "rank": 4 },
                      "category": "pod-l" }
                  ],
                  "entities": [
                    { "id": "warden", "displayName": "Warden", "kind": "character",
                      "rarity": { "label": "5*", "rank": 5 }, "element": "none" }
                  ],
                  "rewards": [
                    { "id": "cage-100", "cadence": "WEEKLY",
                      "grants": [ { "item": "pod-m", "quantity": 1 } ],
                      "requires": { "measure": "cage-score", "atLeast": 100 } }
                  ]%s
                }
                """.formatted(words);
    }

    @Test
    @DisplayName("words are carried through, several keys may share one, and none at all is not an error")
    void wordsAreOptionalAndMayBeShared() {
        assertThat(parser.parse(bundleWith("")).words()).isEmpty();

        GameDataBundle bundle = parser.parse(bundleWith("""
                ,
                  "words": [
                    { "subject": "entity-kind", "key": "character", "displayName": "Construct" },
                    { "subject": "category", "key": "pod-m", "displayName": "EXP" },
                    { "subject": "category", "key": "pod-l", "displayName": "EXP" },
                    { "subject": "measure", "key": "cage-score", "displayName": "The Cage" }
                  ]"""));

        GameDefinition definition = bundle.definitionApprovedAt(Instant.EPOCH);
        assertThat(definition.nameOf(Subject.MEASURE, "cage-score")).isEqualTo("The Cage");
        assertThat(definition.nameOf(Subject.CATEGORY, "pod-m")).isEqualTo("EXP");
        assertThat(definition.nameOf(Subject.CATEGORY, "pod-l")).isEqualTo("EXP");
        assertThat(definition.nameOf(Subject.ENTITY_KIND, "character")).isEqualTo("Construct");
        // A key with no word is itself, which is every version before sequence 13.
        assertThat(definition.nameOf(Subject.CATEGORY, "unnamed")).isEqualTo("unnamed");
        // Sorted by subject and key, whatever order the file wrote them in, so
        // a bundle equals what the database hands back.
        assertThat(bundle.words()).extracting(Word::subject)
                .containsExactly(Subject.MEASURE, Subject.CATEGORY, Subject.CATEGORY, Subject.ENTITY_KIND);
    }

    @Test
    @DisplayName("a word for a key nothing uses is refused, and the message lists the keys in use")
    void refusesAWordForAnUnusedKey() {
        assertThatThrownBy(() -> parser.parse(bundleWith("""
                ,
                  "words": [ { "subject": "measure", "key": "cage-scor", "displayName": "The Cage" } ]""")))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("measure 'cage-scor' has a word but nothing in this bundle uses it")
                .hasMessageContaining("[cage-score]");
    }

    @Test
    @DisplayName("a word for the right key under the wrong subject is refused too")
    void refusesTheWrongSubject() {
        assertThatThrownBy(() -> parser.parse(bundleWith("""
                ,
                  "words": [ { "subject": "category", "key": "character", "displayName": "Construct" } ]""")))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("category 'character' has a word but nothing in this bundle uses it");
    }

    @Test
    @DisplayName("one key given two words is refused rather than resolved by order")
    void refusesTwoWordsForOneKey() {
        assertThatThrownBy(() -> parser.parse(bundleWith("""
                ,
                  "words": [
                    { "subject": "category", "key": "pod-m", "displayName": "EXP" },
                    { "subject": "category", "key": "pod-m", "displayName": "Character EXP" }
                  ]""")))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("category 'pod-m' has two words");
    }

    @Test
    @DisplayName("an unknown subject and a blank word are refused by name")
    void refusesNonsense() {
        assertThatThrownBy(() -> parser.parse(bundleWith("""
                ,
                  "words": [ { "subject": "stage", "key": "pod-m", "displayName": "EXP" } ]""")))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("words[0]")
                .hasMessageContaining("no such subject 'stage'");
        assertThatThrownBy(() -> parser.parse(bundleWith("""
                ,
                  "words": [ { "subject": "category", "key": "pod-m", "displayName": "  " } ]""")))
                .isInstanceOf(BundleFormatException.class);
    }

    @Test
    @DisplayName("the writer puts the words back, and the result reads back as the same bundle")
    void roundTrips() {
        GameDataBundle bundle = parser.parse(bundleWith("""
                ,
                  "words": [ { "subject": "measure", "key": "cage-score", "displayName": "The Cage" } ]"""));
        assertThat(parser.parse(new CanonicalBundleWriter().write(bundle))).isEqualTo(bundle);
    }
}
