package io.stormalmanac.gamedata.ingest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Sink;
import io.stormalmanac.gamedata.Upgrade;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A ladder is shorthand in the file for rows an author would otherwise copy
 * once per entity (ADR 0031). The claim worth testing is that it is
 * <em>only</em> shorthand — a laddered bundle parses to exactly the bundle
 * written out by hand — and then that every way of writing one that reads as
 * meant and expands to something else is refused by name.
 */
class UpgradeLaddersTest {

    private final CanonicalBundleParser parser = new CanonicalBundleParser();

    @Test
    @DisplayName("a ladder parses to exactly the rows written out by hand, once per entity")
    void aLadderIsTheRowsItStandsFor() {
        GameDataBundle laddered = parser.parse(bundle("""
                "ladders": [ {
                  "id": "s-rank",
                  "upgrades": [
                    { "id": "promote-1", "fromState": "promote-0", "toState": "promote-1",
                      "costs": [ { "item": "cogs", "quantity": 5000 } ], "requires": ["level-2"] },
                    { "id": "level-2", "fromState": "level-1", "toState": "level-2",
                      "progress": [ { "kind": "character-exp", "quantity": 1000 } ] },
                    { "id": "evolve-ss", "fromState": "evolve-s", "toState": "evolve-ss",
                      "costs": [ { "item": "{shard}", "quantity": 30 } ] }
                  ],
                  "appliesTo": [
                    { "entity": "ann", "with": { "shard": "shard-ann" } },
                    { "entity": "bea", "with": { "shard": "shard-bea" } }
                  ]
                } ]
                """));

        GameDataBundle byHand = parser.parse(bundle("""
                "upgrades": [
                  { "id": "ann-promote-1", "entity": "ann", "fromState": "promote-0", "toState": "promote-1",
                    "costs": [ { "item": "cogs", "quantity": 5000 } ], "requires": ["level-2"] },
                  { "id": "ann-level-2", "entity": "ann", "fromState": "level-1", "toState": "level-2",
                    "progress": [ { "kind": "character-exp", "quantity": 1000 } ] },
                  { "id": "ann-evolve-ss", "entity": "ann", "fromState": "evolve-s", "toState": "evolve-ss",
                    "costs": [ { "item": "shard-ann", "quantity": 30 } ] },
                  { "id": "bea-promote-1", "entity": "bea", "fromState": "promote-0", "toState": "promote-1",
                    "costs": [ { "item": "cogs", "quantity": 5000 } ], "requires": ["level-2"] },
                  { "id": "bea-level-2", "entity": "bea", "fromState": "level-1", "toState": "level-2",
                    "progress": [ { "kind": "character-exp", "quantity": 1000 } ] },
                  { "id": "bea-evolve-ss", "entity": "bea", "fromState": "evolve-s", "toState": "evolve-ss",
                    "costs": [ { "item": "shard-bea", "quantity": 30 } ] }
                ]
                """));

        assertThat(laddered).isEqualTo(byHand);
        assertThat(upgrade(laddered, "bea-evolve-ss").costs())
                .containsExactly(new ItemStack(new ItemId("shard-bea"), 30));
    }

    @Test
    @DisplayName("an each group repeats its rows once per word in the list, in the list's order")
    void aGroupWalksItsList() {
        GameDataBundle bundle = parser.parse(bundle("""
                "ladders": [ {
                  "id": "s-rank",
                  "upgrades": [
                    { "each": "skill", "upgrades": [
                      { "id": "{skill}-2", "fromState": "{skill}-1", "toState": "{skill}-2",
                        "costs": [ { "item": "cogs", "quantity": 2000 } ] },
                      { "id": "{skill}-3", "fromState": "{skill}-2", "toState": "{skill}-3",
                        "costs": [ { "item": "cogs", "quantity": 3000 } ] }
                    ] }
                  ],
                  "appliesTo": [ { "entity": "ann", "with": { "skill": ["slash", "parry"] } } ]
                } ]
                """));

        assertThat(bundle.sinks()).extracting(Sink::id)
                .containsExactly("ann-slash-2", "ann-slash-3", "ann-parry-2", "ann-parry-3");
        assertThat(upgrade(bundle, "ann-parry-3").fromState()).isEqualTo("parry-2");
        assertThat(upgrade(bundle, "ann-parry-3").entity()).isEqualTo(new EntityId("ann"));
    }

    @Test
    @DisplayName("a climb's provenance beats its row's, which beats its group's, which beats the ladder's")
    void provenanceIsTheNarrowestOneSaid() {
        GameDataBundle bundle = parser.parse(bundle("""
                "ladders": [ {
                  "id": "s-rank",
                  "sourcedBy": "ladder-sitting",
                  "upgrades": [
                    { "id": "promote-1", "fromState": "promote-0", "toState": "promote-1",
                      "costs": [ { "item": "cogs", "quantity": 5000 } ] },
                    { "id": "level-2", "fromState": "level-1", "toState": "level-2",
                      "sourcedBy": "row-sitting", "costs": [ { "item": "cogs", "quantity": 1 } ] },
                    { "each": "skill", "sourcedBy": "group-sitting", "upgrades": [
                      { "id": "{skill}-2", "fromState": "{skill}-1", "toState": "{skill}-2",
                        "costs": [ { "item": "cogs", "quantity": 2000 } ] }
                    ] }
                  ],
                  "appliesTo": [
                    { "entity": "ann", "with": { "skill": ["slash"] } },
                    { "entity": "bea", "sourcedBy": "compared-later", "with": { "skill": ["parry"] } }
                  ]
                } ]
                """));

        assertThat(bundle.provenanceOf("upgrade:ann-promote-1").id()).isEqualTo("ladder-sitting");
        assertThat(bundle.provenanceOf("upgrade:ann-level-2").id()).isEqualTo("row-sitting");
        assertThat(bundle.provenanceOf("upgrade:ann-slash-2").id()).isEqualTo("group-sitting");
        assertThat(List.of("upgrade:bea-promote-1", "upgrade:bea-level-2", "upgrade:bea-parry-2"))
                .allSatisfy(ref -> assertThat(bundle.provenanceOf(ref).id()).isEqualTo("compared-later"));
    }

    @Test
    @DisplayName("a ladder silent about provenance leaves its rows to the bundle's default")
    void aSilentLadderUsesTheDefault() {
        GameDataBundle bundle = parser.parse(bundle(ladderOf("""
                { "id": "promote-1", "fromState": "promote-0", "toState": "promote-1",
                  "costs": [ { "item": "cogs", "quantity": 5000 } ] }
                """, """
                { "entity": "ann" }
                """)));

        assertThat(bundle.provenanceOf("upgrade:ann-promote-1").id()).isEqualTo("default-sitting");
    }

    @Test
    @DisplayName("a row sourced by a ladder and by factProvenance as well is refused, not settled by order")
    void provenanceIsSaidInOnePlace() {
        assertThatThrownBy(() -> parser.parse(bundle("""
                "factProvenance": { "upgrade:ann-promote-1": "row-sitting" },
                """ + ladderOf("""
                { "id": "promote-1", "fromState": "promote-0", "toState": "promote-1",
                  "sourcedBy": "ladder-sitting", "costs": [ { "item": "cogs", "quantity": 5000 } ] }
                """, """
                { "entity": "ann" }
                """))))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("upgrade:ann-promote-1")
                .hasMessageContaining("not both");
    }

    @Test
    @DisplayName("a placeholder the climber does not bind is refused, naming the row and the climber")
    void anUnboundPlaceholderIsRefused() {
        assertThatThrownBy(() -> parser.parse(bundle(ladderOf("""
                { "id": "evolve-ss", "fromState": "evolve-s", "toState": "evolve-ss",
                  "costs": [ { "item": "{shard}", "quantity": 30 } ] }
                """, """
                { "entity": "ann", "with": { "sahrd": "shard-ann" } }
                """))))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("ladders[0].upgrades[0]")
                .hasMessageContaining("(ann)")
                .hasMessageContaining("{shard}");
    }

    @Test
    @DisplayName("a binding no row mentions is refused, because it was meant for a row that says something else")
    void anUnusedBindingIsRefused() {
        assertThatThrownBy(() -> parser.parse(bundle(ladderOf("""
                { "id": "promote-1", "fromState": "promote-0", "toState": "promote-1",
                  "costs": [ { "item": "cogs", "quantity": 5000 } ] }
                """, """
                { "entity": "ann", "with": { "shard": "shard-ann" } }
                """))))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("binds [shard]")
                .hasMessageContaining("no row of ladder 's-rank' mentions");
    }

    @Test
    @DisplayName("a list is spent only by a group that walks it")
    void aListOutsideAGroupIsRefused() {
        assertThatThrownBy(() -> parser.parse(bundle(ladderOf("""
                { "id": "{skill}-2", "fromState": "{skill}-1", "toState": "{skill}-2",
                  "costs": [ { "item": "cogs", "quantity": 2000 } ] }
                """, """
                { "entity": "ann", "with": { "skill": ["slash", "parry"] } }
                """))))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("{skill}, which is a list");

        assertThatThrownBy(() -> parser.parse(bundle(ladderOf("""
                { "each": "skill", "upgrades": [
                  { "id": "{skill}-2", "fromState": "{skill}-1", "toState": "{skill}-2",
                    "costs": [ { "item": "cogs", "quantity": 2000 } ] } ] }
                """, """
                { "entity": "ann", "with": { "skill": "slash" } }
                """))))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("walks 'skill'")
                .hasMessageContaining("does not bind to a list");
    }

    @Test
    @DisplayName("a ladder row that names an entity, or a ladder nobody climbs, is refused")
    void aLadderOwnsItsEntities() {
        assertThatThrownBy(() -> parser.parse(bundle(ladderOf("""
                { "id": "promote-1", "entity": "ann", "fromState": "promote-0", "toState": "promote-1",
                  "costs": [ { "item": "cogs", "quantity": 5000 } ] }
                """, """
                { "entity": "bea" }
                """))))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("must not name one");

        assertThatThrownBy(() -> parser.parse(bundle("""
                "ladders": [ { "id": "s-rank", "upgrades": [
                  { "id": "promote-1", "fromState": "promote-0", "toState": "promote-1",
                    "costs": [ { "item": "cogs", "quantity": 5000 } ] } ], "appliesTo": [] } ]
                """)))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("climbed by nobody");
    }

    @Test
    @DisplayName("an expanded row still meets every check a hand-written one does")
    void expandedRowsAreValidatedLikeAnyOther() {
        // The shard a climber names is an item like any other: one the bundle
        // does not declare is a dangling reference, reported on the row it
        // became rather than on the ladder it came from.
        assertThatThrownBy(() -> parser.parse(bundle(ladderOf("""
                { "id": "evolve-ss", "fromState": "evolve-s", "toState": "evolve-ss",
                  "costs": [ { "item": "{shard}", "quantity": 30 } ] }
                """, """
                { "entity": "ann", "with": { "shard": "shard-nobody" } }
                """))))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("upgrade 'ann-evolve-ss' costs unknown item 'shard-nobody'");

        // And a climber the bundle does not declare.
        assertThatThrownBy(() -> parser.parse(bundle(ladderOf("""
                { "id": "promote-1", "fromState": "promote-0", "toState": "promote-1",
                  "costs": [ { "item": "cogs", "quantity": 5000 } ] }
                """, """
                { "entity": "cat" }
                """))))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("advances unknown entity 'cat'");
    }

    @Test
    @DisplayName("an expanded id that collides with a hand-written one is a duplicate")
    void expandedIdsShareTheNamespace() {
        assertThatThrownBy(() -> parser.parse(bundle("""
                "upgrades": [ { "id": "ann-promote-1", "entity": "ann", "fromState": "a", "toState": "b",
                                "costs": [ { "item": "cogs", "quantity": 1 } ] } ],
                """ + ladderOf("""
                { "id": "promote-1", "fromState": "promote-0", "toState": "promote-1",
                  "costs": [ { "item": "cogs", "quantity": 5000 } ] }
                """, """
                { "entity": "ann" }
                """))))
                .isInstanceOf(BundleFormatException.class)
                .hasMessageContaining("duplicate upgrade 'ann-promote-1'");
    }

    private static Upgrade upgrade(GameDataBundle bundle, String id) {
        return bundle.sinks().stream()
                .filter(sink -> sink.id().equals(id))
                .map(Upgrade.class::cast)
                .findFirst()
                .orElseThrow();
    }

    private static String ladderOf(String rows, String climbers) {
        return """
                "ladders": [ { "id": "s-rank", "upgrades": [ %s ], "appliesTo": [ %s ] } ]
                """.formatted(rows, climbers);
    }

    /** Two entities, their shards, Cogs, and five provenance entries to be sourced by. */
    private static String bundle(String extra) {
        return """
                {
                  "game": { "id": "t", "displayName": "Test", "energyUnit": "Vigour" },
                  "sequence": 0,
                  "attribution": "hand-written",
                  "provenance": [
                    { "id": "default-sitting", "origin": "OBSERVED_IN_GAME", "detail": "d", "observedOn": "2026-09-24" },
                    { "id": "ladder-sitting", "origin": "OBSERVED_IN_GAME", "detail": "d", "observedOn": "2026-09-24" },
                    { "id": "row-sitting", "origin": "OBSERVED_IN_GAME", "detail": "d", "observedOn": "2026-09-24" },
                    { "id": "group-sitting", "origin": "OBSERVED_IN_GAME", "detail": "d", "observedOn": "2026-09-24" },
                    { "id": "compared-later", "origin": "OBSERVED_IN_GAME", "detail": "d", "observedOn": "2026-09-24" }
                  ],
                  "sourcedBy": "default-sitting",
                  "items": [
                    { "id": "cogs", "displayName": "Cogs", "rarity": { "label": "5*", "rank": 5 }, "category": "currency" },
                    { "id": "shard-ann", "displayName": "Ann's Shard", "rarity": { "label": "5*", "rank": 5 }, "category": "shard" },
                    { "id": "shard-bea", "displayName": "Bea's Shard", "rarity": { "label": "5*", "rank": 5 }, "category": "shard" }
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
