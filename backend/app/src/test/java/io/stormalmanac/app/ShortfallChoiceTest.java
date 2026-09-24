package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;

import io.stormalmanac.api.player.ShortfallView.ShortfallLine;
import io.stormalmanac.api.player.ShortfallView.ShortfallResponse;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.Goal;
import io.stormalmanac.gamedata.ingest.CanonicalBundleParser;
import io.stormalmanac.planner.Demand;
import io.stormalmanac.planner.DemandResolver;
import io.stormalmanac.player.Inventory;
import io.stormalmanac.player.Roster;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The character page's line for a step with several prices.
 *
 * <p>The line is one step, not a bill per price: it is held when any one price
 * is, and short by the one step otherwise. The bundle is parsed from text, so
 * this also proves the format takes two upgrades making the same move, which
 * V9 made storable.
 */
class ShortfallChoiceTest {

    private static final ProfileId PROFILE = new ProfileId("shortfall-choice");
    private static final EntityId WARDEN = new EntityId("warden");

    private final GameDefinition definition = new CanonicalBundleParser().parse(
            """
            {
              "game": { "id": "proving-ground", "displayName": "The Proving Ground",
                        "energyUnit": "Vigour" },
              "sequence": 0,
              "label": "1.0",
              "attribution": "hand-written",
              "items": [
                { "id": "token", "displayName": "Token", "rarity": { "label": "4*", "rank": 4 },
                  "category": "currency" },
                { "id": "score", "displayName": "Score", "rarity": { "label": "4*", "rank": 4 },
                  "category": "currency" }
              ],
              "entities": [
                { "id": "warden", "displayName": "The Warden", "kind": "character",
                  "rarity": { "label": "5*", "rank": 5 } }
              ],
              "upgrades": [
                { "id": "resonate-by-token", "entity": "warden", "fromState": "base", "toState": "resonance",
                  "costs": [ { "item": "token", "quantity": 234 } ] },
                { "id": "resonate-by-score", "entity": "warden", "fromState": "base", "toState": "resonance",
                  "costs": [ { "item": "score", "quantity": 246 } ] }
              ]
            }
            """).definitionApprovedAt(Instant.EPOCH);

    @Test
    @DisplayName("a step with several prices is one line, named by its prices, and short by one step")
    void oneLinePerChoice() {
        ShortfallResponse response = shortfall(Inventory.empty(PROFILE).with(new ItemId("score"), 245));

        assertThat(response.steps()).containsExactly("resonate-by-token or resonate-by-score");
        assertThat(response.items()).singleElement().satisfies(line -> {
            assertThat(line.displayName()).isEqualTo("one of: 234 Token · 246 Score");
            assertThat(line.required()).isEqualTo(1);
            assertThat(line.owned()).isZero();
            assertThat(line.missing()).isEqualTo(1);
        });
        assertThat(response.complete()).isFalse();
    }

    @Test
    @DisplayName("holding any one price in full is holding the step")
    void anyPriceHeldIsHeld() {
        ShortfallLine line = shortfall(Inventory.empty(PROFILE).with(new ItemId("score"), 246))
                .items().getFirst();

        assertThat(line.owned()).isEqualTo(1);
        assertThat(line.missing()).isZero();
    }

    @Test
    @DisplayName("a price in several parts is written as one, joined with +, and large numbers are grouped")
    void aPriceInSeveralParts() {
        GameDefinition twoPartPrice = new CanonicalBundleParser().parse(
                """
                {
                  "game": { "id": "proving-ground", "displayName": "The Proving Ground",
                            "energyUnit": "Vigour" },
                  "sequence": 0,
                  "label": "1.0",
                  "attribution": "hand-written",
                  "items": [
                    { "id": "token", "displayName": "Token", "rarity": { "label": "4*", "rank": 4 },
                      "category": "currency" },
                    { "id": "score", "displayName": "Score", "rarity": { "label": "4*", "rank": 4 },
                      "category": "currency" }
                  ],
                  "entities": [
                    { "id": "warden", "displayName": "The Warden", "kind": "character",
                      "rarity": { "label": "5*", "rank": 5 } }
                  ],
                  "upgrades": [
                    { "id": "resonate-by-both", "entity": "warden", "fromState": "base", "toState": "resonance",
                      "costs": [ { "item": "token", "quantity": 12000 }, { "item": "score", "quantity": 10 } ] },
                    { "id": "resonate-by-score", "entity": "warden", "fromState": "base", "toState": "resonance",
                      "costs": [ { "item": "score", "quantity": 246 } ] }
                  ]
                }
                """).definitionApprovedAt(Instant.EPOCH);
        Demand demand = new DemandResolver().resolve(
                twoPartPrice, new Roster(PROFILE, Map.of(WARDEN, Set.of("base"))),
                List.of(Goal.deterministic(WARDEN, "resonance")));

        ShortfallLine line = ShortfallResponse.of(
                        PROFILE.value(), twoPartPrice, WARDEN.value(), List.of("base"), "resonance",
                        demand, Inventory.empty(PROFILE))
                .items().getFirst();

        assertThat(line.displayName()).isEqualTo("one of: 12,000 Token + 10 Score · 246 Score");
    }

    private ShortfallResponse shortfall(Inventory inventory) {
        Demand demand = new DemandResolver().resolve(
                definition, new Roster(PROFILE, Map.of(WARDEN, Set.of("base"))),
                List.of(Goal.deterministic(WARDEN, "resonance")));
        return ShortfallResponse.of(
                PROFILE.value(), definition, WARDEN.value(), List.of("base"), "resonance", demand, inventory);
    }
}
