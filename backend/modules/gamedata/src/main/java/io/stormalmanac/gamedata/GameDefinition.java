package io.stormalmanac.gamedata;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.StageId;
import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.gamedata.catalog.Entity;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Everything the rest of the system knows about one game at one version.
 *
 * <p>Onboarding a title is producing one of these plus a parser adapter. That
 * is the entire acceptance test for phase 11: Punishing: Gray Raven goes live
 * with zero new lines in planner, gacha or stats, and the diff goes in the
 * README. If the diff is not clean, the week spent making it clean is the most
 * valuable week in the project.
 */
public record GameDefinition(
        Game game,
        GameDataVersion version,
        List<Item> items,
        List<Source> sources,
        List<Sink> sinks,
        List<BannerModel> banners,
        List<Entity> entities
) {

    public GameDefinition {
        items = List.copyOf(items);
        sources = List.copyOf(sources);
        sinks = List.copyOf(sinks);
        banners = List.copyOf(banners);
        entities = List.copyOf(entities);
    }

    public Map<ItemId, Item> itemsById() {
        return items.stream().collect(Collectors.toMap(Item::id, Function.identity()));
    }

    public Map<EntityId, Entity> entitiesById() {
        return entities.stream().collect(Collectors.toMap(Entity::id, Function.identity()));
    }

    public List<Stage> stages() {
        return sources.stream().filter(Stage.class::isInstance).map(Stage.class::cast).toList();
    }

    public List<Craft> crafts() {
        return sources.stream().filter(Craft.class::isInstance).map(Craft.class::cast).toList();
    }

    public List<Shop> shops() {
        return sources.stream().filter(Shop.class::isInstance).map(Shop.class::cast).toList();
    }

    public List<Reward> rewards() {
        return sources.stream().filter(Reward.class::isInstance).map(Reward.class::cast).toList();
    }

    /** Stages that declare a drop for this item, used to prune the solver's variable set. */
    public List<StageId> stagesDropping(ItemId item) {
        return stages().stream()
                .filter(s -> s.drops().stream().anyMatch(d -> d.item().equals(item)))
                .map(Stage::stageId)
                .toList();
    }
}
