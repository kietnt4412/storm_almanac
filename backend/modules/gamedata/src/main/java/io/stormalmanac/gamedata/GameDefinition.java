package io.stormalmanac.gamedata;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.StageId;
import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.gamedata.catalog.Entity;
import java.util.Comparator;
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
        List<Entity> entities,
        List<ProgressKind> progressKinds,
        List<String> sections
) {

    /**
     * A version whose steps name no sections, which is every version published
     * before sequence 11.
     */
    public GameDefinition(
            Game game,
            GameDataVersion version,
            List<Item> items,
            List<Source> sources,
            List<Sink> sinks,
            List<BannerModel> banners,
            List<Entity> entities,
            List<ProgressKind> progressKinds) {
        this(game, version, items, sources, sinks, banners, entities, progressKinds, List.of());
    }

    public GameDefinition {
        // Copied, not sorted, unlike the names below: the order is the content.
        // It is the order the game's screens show the sections in, and a reader
        // finds their place by it (ADR 0032).
        sections = List.copyOf(sections);
        items = List.copyOf(items);
        sources = List.copyOf(sources);
        sinks = List.copyOf(sinks);
        banners = List.copyOf(banners);
        entities = List.copyOf(entities);
        // Sorted, not copied in the order it arrived. Names have no order — the
        // bundle lists them in the order they were read, the database returns
        // them however it returns them — and the same version written two ways
        // has to be one value, which is what lets a version read back from the
        // database equal the bundle it came from. The same normalisation
        // Upgrade applies to its gates and its progress costs, for the same
        // reason: reordering a bundle produces no changes in the patch diff.
        progressKinds = progressKinds.stream()
                .sorted(Comparator.comparing(ProgressKind::kind))
                .toList();
    }

    /**
     * A version whose progress kinds are unnamed, which is every version
     * published before they could be.
     */
    public GameDefinition(
            Game game,
            GameDataVersion version,
            List<Item> items,
            List<Source> sources,
            List<Sink> sinks,
            List<BannerModel> banners,
            List<Entity> entities) {
        this(game, version, items, sources, sinks, banners, entities, List.of());
    }

    /**
     * What to call a progress kind, falling back to the kind itself.
     *
     * <p>The fallback lives here, once, rather than at each call site: a page
     * that forgot it would print {@code character-exp} beside a properly named
     * {@code Cogs} and nothing would look broken. Naming is optional by design —
     * see {@link ProgressKind} — so the fallback is the normal path for every
     * version published before sequence 7, not an error case.
     */
    public String nameOfProgress(String kind) {
        return progressKinds.stream()
                .filter(named -> named.kind().equals(kind))
                .map(ProgressKind::displayName)
                .findFirst()
                .orElse(kind);
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
