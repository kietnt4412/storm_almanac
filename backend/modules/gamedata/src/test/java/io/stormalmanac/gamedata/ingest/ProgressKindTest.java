package io.stormalmanac.gamedata.ingest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.gamedata.ProgressKind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Naming a progress kind, and the one way of doing it that is worse than not.
 *
 * <p>A name for a kind nothing uses is the failure worth a test: it reads as a
 * fix, it is never looked up, and the line somebody wrote it for goes on
 * rendering its slug. Nothing else in the bundle would notice, because a
 * progress kind is an opaque string on both sides — the upgrade that costs it
 * and the fodder rule that feeds it — with no table of kinds for a foreign key
 * to point at.
 */
class ProgressKindTest {

    private final CanonicalBundleParser parser = new CanonicalBundleParser();

    /** An upgrade that costs EXP, a rule that feeds it, and room for a name. */
    private static String bundleWith(String progressKinds) {
        return """
                {
                  "game": { "id": "t", "displayName": "Test", "energyUnit": "Vigour" },
                  "sequence": 1,
                  "attribution": "hand-written",
                  "items": [
                    { "id": "pod", "displayName": "Pod", "rarity": { "label": "3*", "rank": 3 },
                      "category": "exp-fodder" }
                  ],
                  "entities": [
                    { "id": "warden", "displayName": "Warden", "kind": "character",
                      "rarity": { "label": "5*", "rank": 5 }, "element": "none" }
                  ],
                  "upgrades": [
                    { "id": "warden-level-10", "entity": "warden",
                      "fromState": "level-1", "toState": "level-10", "costs": [],
                      "progress": [ { "kind": "hero-exp", "quantity": 1000 } ] }
                  ],
                  "fodder": [
                    { "id": "feed", "consumesCategory": "exp-fodder",
                      "minimumRarity": { "label": "3*", "rank": 3 },
                      "progress": "hero-exp", "progressPerUnit": 100, "costs": [] }
                  ]%s
                }
                """.formatted(progressKinds);
    }

    @Test
    @DisplayName("a named kind is carried through, and an unnamed one is not an error")
    void namesAreOptional() {
        assertThat(parser.parse(bundleWith("")).progressKinds()).isEmpty();

        assertThat(parser.parse(bundleWith("""
                ,
                  "progressKinds": [ { "kind": "hero-exp", "displayName": "Hero EXP" } ]"""))
                .progressKinds())
                .containsExactly(new ProgressKind("hero-exp", "Hero EXP"));
    }

    @Test
    @DisplayName("a name for a kind nothing costs or feeds is refused, and the message lists the kinds in use")
    void refusesANameForAKindNobodyUses() {
        assertThatThrownBy(() -> parser.parse(bundleWith("""
                ,
                  "progressKinds": [ { "kind": "her-exp", "displayName": "Hero EXP" } ]""")))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("progress kind 'her-exp' is named but no upgrade costs it")
                .hasMessageContaining("[hero-exp]");
    }

    @Test
    @DisplayName("the same kind named twice is refused rather than resolved by order")
    void refusesADuplicate() {
        assertThatThrownBy(() -> parser.parse(bundleWith("""
                ,
                  "progressKinds": [
                    { "kind": "hero-exp", "displayName": "Hero EXP" },
                    { "kind": "hero-exp", "displayName": "Character EXP" }
                  ]""")))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("duplicate progress kind 'hero-exp'");
    }

    @Test
    @DisplayName("a blank name is refused: a kind is better unnamed than named nothing")
    void refusesABlankName() {
        assertThatThrownBy(() -> parser.parse(bundleWith("""
                ,
                  "progressKinds": [ { "kind": "hero-exp", "displayName": "   " } ]""")))
                .isInstanceOf(BundleFormatException.class);
    }

    @Test
    @DisplayName("a kind a fodder rule feeds and no upgrade costs may still be named")
    void aFedKindCounts() {
        // The rule is "in use", not "demanded". A bundle can read a fodder rule
        // in one sitting and the upgrade it pays in the next — this one did, at
        // sequences 0 and 1 — and a name that had to wait for the second would
        // be a name nobody could write down when they knew it.
        String withoutTheUpgrade = bundleWith("""
                ,
                  "progressKinds": [ { "kind": "hero-exp", "displayName": "Hero EXP" } ]""")
                .replace("""
                        "costs": [],
                      "progress": [ { "kind": "hero-exp", "quantity": 1000 } ] }""", "\"costs\": [] }");

        assertThat(parser.parse(withoutTheUpgrade).progressKinds())
                .containsExactly(new ProgressKind("hero-exp", "Hero EXP"));
    }
}
