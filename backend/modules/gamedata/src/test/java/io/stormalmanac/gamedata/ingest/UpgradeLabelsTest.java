package io.stormalmanac.gamedata.ingest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.gamedata.Upgrade;
import io.stormalmanac.gamedata.diff.Change;
import io.stormalmanac.gamedata.diff.VersionDiff;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What the game calls a step's states, and where its track sits (ADR 0032).
 *
 * <p>Found in the maintainer's rehearsal: the roster offered "promote-5" where
 * the game shows "Elite ★3", and thirteen tracks as one list where the game
 * groups them under four headings and tags each skill with its orb. The words
 * are readings, so they ride on the step; the claims worth testing are that
 * they survive every hop a step makes, that a ladder can say them once per
 * position, and that each way of writing one that would render as something
 * else is refused by name.
 */
class UpgradeLabelsTest {

    private final CanonicalBundleParser parser = new CanonicalBundleParser();

    @Test
    @DisplayName("a step's names, section and tag are read, and written back as they were")
    void labelsSurviveTheRoundTrip() {
        GameDataBundle bundle = parser.parse(bundle("""
                "sections": ["Growth", "Basic Skill"],
                "upgrades": [
                  { "id": "ann-promote-1", "entity": "ann", "fromState": "promote-0", "toState": "promote-1",
                    "fromName": "Private ★1", "toName": "Sergeant ★1", "section": "Growth" },
                  { "id": "ann-slash-2", "entity": "ann", "fromState": "slash-1", "toState": "slash-2",
                    "section": "Basic Skill", "tag": "Red Orb" }
                ]
                """));

        assertThat(upgrade(bundle, "ann-promote-1").labels())
                .isEqualTo(new Upgrade.Labels("Private ★1", "Sergeant ★1", "Growth", null));
        assertThat(upgrade(bundle, "ann-slash-2").labels())
                .isEqualTo(new Upgrade.Labels(null, null, "Basic Skill", "Red Orb"));
        assertThat(bundle.sections()).containsExactly("Growth", "Basic Skill");
        assertThat(parser.parse(new CanonicalBundleWriter().write(bundle))).isEqualTo(bundle);
    }

    @Test
    @DisplayName("a step with no words is exactly the step every earlier sequence published")
    void noWordsIsTheOldStep() {
        GameDataBundle bundle = parser.parse(bundle("""
                "upgrades": [
                  { "id": "ann-promote-1", "entity": "ann", "fromState": "promote-0", "toState": "promote-1" }
                ]
                """));

        assertThat(upgrade(bundle, "ann-promote-1").labels()).isEqualTo(Upgrade.Labels.NONE);
        assertThat(new CanonicalBundleWriter().write(bundle))
                .doesNotContain("fromName", "toName", "section", "tag");
    }

    @Test
    @DisplayName("a ladder says each position's section and tag once, and every construct's skill in it gets them")
    void positionsLabelEachSlot() {
        GameDataBundle bundle = parser.parse(bundle("""
                "sections": ["Basic Skill", "Common Effect"],
                "ladders": [ {
                  "id": "s-rank",
                  "upgrades": [
                    { "each": "skill",
                      "positions": [
                        { "section": "Basic Skill", "tag": "Red Orb" },
                        { "section": "Common Effect", "tag": "Class Skill" }
                      ],
                      "upgrades": [
                        { "id": "{skill}-2", "fromState": "{skill}-1", "toState": "{skill}-2" },
                        { "id": "{skill}-3", "fromState": "{skill}-2", "toState": "{skill}-3" }
                      ] }
                  ],
                  "appliesTo": [
                    { "entity": "ann", "with": { "skill": ["slash", "attacker"] } },
                    { "entity": "bea", "with": { "skill": ["parry", "attacker"] } }
                  ]
                } ]
                """));

        assertThat(upgrade(bundle, "ann-slash-3").labels())
                .isEqualTo(new Upgrade.Labels(null, null, "Basic Skill", "Red Orb"));
        assertThat(upgrade(bundle, "bea-parry-2").labels())
                .isEqualTo(new Upgrade.Labels(null, null, "Basic Skill", "Red Orb"));
        assertThat(upgrade(bundle, "bea-attacker-2").labels())
                .isEqualTo(new Upgrade.Labels(null, null, "Common Effect", "Class Skill"));
    }

    @Test
    @DisplayName("positions of another length than the list are refused, since the words would land on the wrong skills")
    void positionsMustCoverTheList() {
        assertThatThrownBy(() -> parser.parse(bundle("""
                "sections": ["Basic Skill"],
                "ladders": [ {
                  "id": "s-rank",
                  "upgrades": [
                    { "each": "skill", "positions": [ { "section": "Basic Skill" } ],
                      "upgrades": [ { "id": "{skill}-2", "fromState": "{skill}-1", "toState": "{skill}-2" } ] }
                  ],
                  "appliesTo": [ { "entity": "ann", "with": { "skill": ["slash", "parry"] } } ]
                } ]
                """)))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("positions has 1 entries")
                .hasMessageContaining("binds 2 'skill'");
    }

    @Test
    @DisplayName("a position may not say what its row already says")
    void positionsDoNotOverrideRows() {
        assertThatThrownBy(() -> parser.parse(bundle("""
                "sections": ["Basic Skill", "Special Skill"],
                "ladders": [ {
                  "id": "s-rank",
                  "upgrades": [
                    { "each": "skill", "positions": [ { "section": "Basic Skill" } ],
                      "upgrades": [ { "id": "{skill}-2", "fromState": "{skill}-1", "toState": "{skill}-2",
                                      "section": "Special Skill" } ] }
                  ],
                  "appliesTo": [ { "entity": "ann", "with": { "skill": ["slash"] } } ]
                } ]
                """)))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("sets 'section' itself and its position sets it too");
    }

    @Test
    @DisplayName("a section a step names must be declared, and a declared one must be used")
    void sectionsAreDeclaredAndUsed() {
        assertThatThrownBy(() -> parser.parse(bundle("""
                "sections": ["Growth", "Evolution Effect"],
                "upgrades": [
                  { "id": "ann-promote-1", "entity": "ann", "fromState": "promote-0", "toState": "promote-1",
                    "section": "Grwoth" }
                ]
                """)))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("sits under section 'Grwoth', which sections does not declare")
                .hasMessageContaining("section 'Growth' is declared but no upgrade sits under it")
                .hasMessageContaining("section 'Evolution Effect' is declared but no upgrade sits under it");
    }

    @Test
    @DisplayName("one state has one name, however many steps say it")
    void oneStateOneName() {
        assertThatThrownBy(() -> parser.parse(bundle("""
                "upgrades": [
                  { "id": "ann-promote-1", "entity": "ann", "fromState": "promote-0", "toState": "promote-1",
                    "toName": "Sergeant ★1" },
                  { "id": "ann-promote-2", "entity": "ann", "fromState": "promote-1", "toState": "promote-2",
                    "fromName": "Sergeant ★2" }
                ]
                """)))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("state 'promote-1' of 'ann' is called both 'Sergeant ★1' and 'Sergeant ★2'");
    }

    @Test
    @DisplayName("renaming a state is a change in the patch diff, not a silent one")
    void aRenameIsAChange() {
        GameDataBundle before = parser.parse(bundle("""
                "upgrades": [
                  { "id": "ann-promote-1", "entity": "ann", "fromState": "promote-0", "toState": "promote-1",
                    "toName": "Sergeant ★1" }
                ]
                """));
        GameDataBundle after = parser.parse(bundle("""
                "upgrades": [
                  { "id": "ann-promote-1", "entity": "ann", "fromState": "promote-0", "toState": "promote-1",
                    "toName": "Sergeant" }
                ]
                """));

        VersionDiff diff = VersionDiff.between(
                before.definitionApprovedAt(Instant.EPOCH), after.definitionApprovedAt(Instant.EPOCH));

        assertThat(diff.changes())
                .extracting(Change::detail, Change::before, Change::after)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("to name", "Sergeant ★1", "Sergeant"));
    }

    private static Upgrade upgrade(GameDataBundle bundle, String id) {
        return bundle.sinks().stream()
                .filter(sink -> sink.id().equals(id))
                .map(Upgrade.class::cast)
                .findFirst()
                .orElseThrow();
    }

    private static String bundle(String extra) {
        return """
                {
                  "game": { "id": "t", "displayName": "Test", "energyUnit": "Vigour" },
                  "sequence": 0,
                  "attribution": "hand-written",
                  "provenance": [
                    { "id": "sitting", "origin": "OBSERVED_IN_GAME", "detail": "d", "observedOn": "2026-09-25" }
                  ],
                  "sourcedBy": "sitting",
                  "items": [
                    { "id": "cogs", "displayName": "Cogs", "rarity": { "label": "5*", "rank": 5 }, "category": "currency" }
                  ],
                  "entities": [
                    { "id": "ann", "displayName": "Ann", "kind": "character", "rarity": { "label": "S", "rank": 3 } },
                    { "id": "bea", "displayName": "Bea", "kind": "character", "rarity": { "label": "S", "rank": 3 } }
                  ],
                  %s
                }
                """.formatted(extra.strip().replaceAll(",$", ""));
    }
}
