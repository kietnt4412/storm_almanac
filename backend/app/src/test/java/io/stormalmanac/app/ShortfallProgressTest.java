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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The character page's EXP line, on the launch bundle.
 *
 * <p>Nobody holds EXP, so the inventory alone would say every reader owns none
 * of it, including one sitting on a stack of Pods. The line counts fodder at
 * what its rule says a unit is worth, the way the game's own feed screen does.
 * No database: the arithmetic is the whole of what is being tested, and
 * {@code ShortfallTest} already drives the route.
 */
class ShortfallProgressTest {

    private static final ProfileId PROFILE = new ProfileId("shortfall-progress");
    private static final EntityId HELENTINE = new EntityId("helentine-lacrimosa");
    private static final ItemId CHARACTER_EXP = Demand.progressItem("character-exp");

    private final GameDefinition definition = load();

    @Test
    @DisplayName("EXP held is the Pods held at face value, and enough of them makes the line complete")
    void podsCountAsExp() {
        // 20 XL (400 000) + 30 L (90 000) + 5 M (5 000) = 495 000 against the
        // 497 000 that level 80 costs: 2 000 short. One more M would still be
        // 1 000 short, so the line is honest about Pods not dividing evenly.
        Inventory held = Inventory.empty(PROFILE)
                .with(new ItemId("exp-pod-xl"), 20)
                .with(new ItemId("exp-pod-l"), 30)
                .with(new ItemId("exp-pod-m"), 5);

        ShortfallLine line = lineFor(held, CHARACTER_EXP);

        // "Character EXP" and not "character-exp": the bundle names its own
        // progress kinds since sequence 7, and the reason the name is not the
        // game's own word is that the game's own word is "EXP" on all three of
        // its pools. N37.
        assertThat(line.displayName()).isEqualTo("Character EXP");
        assertThat(line.required()).isEqualTo(497_000);
        assertThat(line.owned()).isEqualTo(495_000);
        assertThat(line.missing()).isEqualTo(2_000);
    }

    @Test
    @DisplayName("EXP past the requirement is not a debt, and the line reads as met")
    void overshootIsMet() {
        ShortfallLine line = lineFor(
                Inventory.empty(PROFILE).with(new ItemId("exp-pod-xl"), 25), CHARACTER_EXP);

        assertThat(line.owned()).isEqualTo(500_000);
        assertThat(line.missing()).isZero();
    }

    private ShortfallLine lineFor(Inventory inventory, ItemId item) {
        Demand demand = new DemandResolver().resolve(
                definition, new Roster(PROFILE, Map.of()),
                List.of(Goal.deterministic(HELENTINE, "level-80")));
        ShortfallResponse response = ShortfallResponse.of(
                PROFILE.value(), definition, HELENTINE.value(), null, "level-80", demand, inventory);
        return response.items().stream()
                .filter(l -> l.item().equals(item.value()))
                .findFirst()
                .orElseThrow();
    }

    private static GameDefinition load() {
        Path bundle = Path.of("..", "..", "data", "bundles", "punishing-gray-raven-steering-by-light.json");
        try (InputStream in = Files.newInputStream(bundle)) {
            return new CanonicalBundleParser().parse(in).definitionApprovedAt(Instant.EPOCH);
        } catch (IOException e) {
            throw new IllegalStateException("could not read " + bundle.toAbsolutePath().normalize(), e);
        }
    }
}
