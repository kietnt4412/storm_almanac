package io.stormalmanac.planner;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.StageId;
import io.stormalmanac.gamedata.Availability;
import io.stormalmanac.gamedata.Craft;
import io.stormalmanac.gamedata.Drop;
import io.stormalmanac.gamedata.Game;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.Item;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Rarity;
import io.stormalmanac.gamedata.Sink;
import io.stormalmanac.gamedata.Source;
import io.stormalmanac.gamedata.Stage;
import io.stormalmanac.gamedata.Upgrade;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A tiny title, built in code, whose arithmetic is checkable by hand.
 *
 * <p>The acceptance fixtures live in {@code :app} and are parsed from the
 * canonical bundle; these are something else and deliberately so. A unit test
 * for a solver needs a model small enough that the right answer can be worked
 * out on paper — "5 ore at 1.4 per run is 4 runs, 40 energy" — so that a
 * disagreement is the solver's fault and not the fixture's.
 *
 * <p>Nothing here is any real game. The names are chosen to be obviously
 * invented for exactly that reason.
 */
final class TestGame {

    static final GameId ID = GameId.of("unit-test-game");
    static final ItemId GOLD = ItemId.of("gold");
    static final ItemId ORE = ItemId.of("ore");
    static final ItemId INGOT = ItemId.of("ingot");
    static final ItemId RELIC = ItemId.of("relic");
    static final StageId ORE_STAGE = new StageId("s-ore");
    static final StageId GOLD_STAGE = new StageId("s-gold");
    static final StageId RELIC_STAGE = new StageId("s-relic");
    static final EntityId HERO = new EntityId("hero");

    private final List<Source> sources = new ArrayList<>();
    private final List<Sink> sinks = new ArrayList<>();

    private TestGame() {}

    static TestGame builder() {
        return new TestGame();
    }

    TestGame stage(StageId id, int energy, Object... itemsAndYields) {
        return stage(id, energy, Availability.ALWAYS, itemsAndYields);
    }

    TestGame stage(StageId id, int energy, Availability availability, Object... itemsAndYields) {
        List<Drop> drops = new ArrayList<>();
        for (int i = 0; i < itemsAndYields.length; i += 2) {
            drops.add(new Drop((ItemId) itemsAndYields[i], ((Number) itemsAndYields[i + 1]).doubleValue()));
        }
        sources.add(new Stage(id, id.value(), energy, drops, availability));
        return this;
    }

    TestGame craft(String id, List<ItemStack> consumes, List<ItemStack> produces) {
        sources.add(new Craft(id, consumes, produces, Availability.ALWAYS));
        return this;
    }

    TestGame source(Source source) {
        sources.add(source);
        return this;
    }

    TestGame upgrade(String id, EntityId entity, String from, String to, List<ItemStack> costs) {
        sinks.add(new Upgrade(id, entity, from, to, costs));
        return this;
    }

    TestGame sink(Sink sink) {
        sinks.add(sink);
        return this;
    }

    GameDefinition build() {
        return new GameDefinition(
                new Game(ID, "Unit Test Game", "Vigour"),
                new GameDataVersion(ID, 0, "1.0", Instant.parse("2026-01-01T00:00:00Z"),
                        "Synthetic fixture authored for this test. Not any published game's data."),
                List.of(
                        new Item(GOLD, "Gold", new Rarity("1*", 1), "currency"),
                        new Item(ORE, "Ore", new Rarity("2*", 2), "material"),
                        new Item(INGOT, "Ingot", new Rarity("3*", 3), "material"),
                        new Item(RELIC, "Relic", new Rarity("4*", 4), "material")),
                List.copyOf(sources),
                List.copyOf(sinks),
                List.of(),
                List.of());
    }

    static ItemStack stack(ItemId item, int quantity) {
        return new ItemStack(item, quantity);
    }
}
