package io.stormalmanac.gamedata.ingest;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gamedata.Craft;
import io.stormalmanac.gamedata.Fodder;
import io.stormalmanac.gamedata.Game;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.Item;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Reward;
import io.stormalmanac.gamedata.Shop;
import io.stormalmanac.gamedata.Sink;
import io.stormalmanac.gamedata.Source;
import io.stormalmanac.gamedata.Stage;
import io.stormalmanac.gamedata.Upgrade;
import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.gamedata.catalog.Entity;
import io.stormalmanac.gamedata.catalog.Skill;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * One snapshot of a game's data as an adapter produced it, before anyone
 * approved it.
 *
 * <p>This is deliberately <em>not</em> a {@link GameDefinition}. A definition
 * carries a {@link GameDataVersion}, and that record requires a non-null
 * {@code publishedAt} — so an unapproved snapshot has no representation as a
 * definition at all, and the type system says what the schema's
 * {@code game_data_version_published_has_timestamp} constraint says. Ingest
 * produces a bundle; approval turns it into a definition.
 *
 * <p>Onboarding a title is writing something that produces one of these. That
 * is the whole contract a parser adapter has to meet, and the reason the
 * validation below lives here rather than in any one adapter: every adapter
 * gets it, and none of them can skip it.
 *
 * @param sequence    monotonic within a game; the ordering key, supplied by
 *                    whoever fetched the snapshot rather than by the upstream
 * @param attribution where these numbers came from. Required, because "numbers
 *                    and text only, attributed" is a project invariant and an
 *                    unattributed snapshot is one nobody can defend later
 */
public record GameDataBundle(
        Game game,
        long sequence,
        String label,
        String attribution,
        List<Item> items,
        List<Source> sources,
        List<Sink> sinks,
        List<BannerModel> banners,
        List<Entity> entities
) {

    public GameDataBundle {
        if (game == null) throw new BundleFormatException("game is required");
        if (sequence < 0) throw new BundleFormatException("sequence must not be negative");
        if (attribution == null || attribution.isBlank()) {
            throw new BundleFormatException("attribution is required: an unattributed snapshot is not publishable");
        }
        label = label == null || label.isBlank() ? String.valueOf(sequence) : label;

        items = List.copyOf(items);
        sources = List.copyOf(sources);
        sinks = List.copyOf(sinks);
        banners = List.copyOf(banners);
        entities = List.copyOf(entities);

        // Checked here, before a connection is opened, because the composite
        // foreign keys will reject a dangling reference with a message naming a
        // constraint. "stage_drop_item_fk" is not something the person
        // approving a publish can act on; "stage '1-1' drops unknown item
        // 'sulfr'" is. See ADR 0008.
        validate(items, sources, sinks, entities);
    }

    /**
     * The same data as a {@link GameDefinition}, as of the moment a human
     * approved it.
     *
     * @param approvedAt when the approval happened, never when the fetch did
     */
    public GameDefinition definitionApprovedAt(Instant approvedAt) {
        return new GameDefinition(
                game,
                new GameDataVersion(game.id(), sequence, label, approvedAt),
                items,
                sources,
                sinks,
                banners,
                entities);
    }

    private static void validate(
            List<Item> items, List<Source> sources, List<Sink> sinks, List<Entity> entities) {

        Set<ItemId> knownItems = new LinkedHashSet<>();
        for (Item item : items) {
            if (!knownItems.add(item.id())) {
                throw new BundleFormatException("duplicate item '" + item.id() + "'");
            }
        }
        Set<EntityId> knownEntities = new LinkedHashSet<>();
        for (Entity entity : entities) {
            if (!knownEntities.add(entity.id())) {
                throw new BundleFormatException("duplicate entity '" + entity.id() + "'");
            }
        }

        // Slugs are unique per table, not across all of them: the schema says so
        // deliberately, and nothing in the domain requires a Source.id() to be
        // unique across the four source shapes. So each kind is checked against
        // its own kind and no further.
        Map<Class<?>, Set<String>> slugsByKind = new LinkedHashMap<>();
        sources.forEach(source -> uniqueSlug(slugsByKind, source.getClass(), source.id()));
        sinks.forEach(sink -> uniqueSlug(slugsByKind, sink.getClass(), sink.id()));

        Set<String> skillSlugs = new LinkedHashSet<>();
        Set<String> talentSlugs = new LinkedHashSet<>();
        for (Entity entity : entities) {
            // Unique per *version*, not per entity: gamedata.skill and
            // gamedata.talent both carry UNIQUE (version_id, slug).
            entity.skills().forEach(skill -> {
                if (!skillSlugs.add(skill.id())) {
                    throw new BundleFormatException("duplicate skill '" + skill.id() + "'");
                }
            });
            entity.talents().forEach(talent -> {
                if (!talentSlugs.add(talent.id())) {
                    throw new BundleFormatException("duplicate talent '" + talent.id() + "'");
                }
            });
        }

        List<String> dangling = new ArrayList<>();
        for (Source source : sources) {
            switch (source) {
                case Stage stage -> stage.drops().forEach(drop ->
                        require(knownItems, drop.item(), dangling, "stage '" + stage.id() + "' drops"));
                case Craft craft -> {
                    requireStacks(knownItems, craft.consumes(), dangling, "craft '" + craft.id() + "' consumes");
                    requireStacks(knownItems, craft.produces(), dangling, "craft '" + craft.id() + "' produces");
                }
                case Shop shop -> {
                    require(knownItems, shop.currency(), dangling, "shop '" + shop.id() + "' is priced in");
                    require(knownItems, shop.offer().item(), dangling, "shop '" + shop.id() + "' offers");
                }
                case Reward reward ->
                        requireStacks(knownItems, reward.grants(), dangling, "reward '" + reward.id() + "' grants");
            }
        }
        for (Sink sink : sinks) {
            requireStacks(knownItems, sink.costs(), dangling, sinkKind(sink) + " '" + sink.id() + "' costs");
            if (sink instanceof Upgrade upgrade && !knownEntities.contains(upgrade.entity())) {
                dangling.add("upgrade '" + upgrade.id() + "' advances unknown entity '" + upgrade.entity() + "'");
            }
        }
        for (Entity entity : entities) {
            for (Skill skill : entity.skills()) {
                for (Skill.Rank rank : skill.ranks()) {
                    requireStacks(knownItems, rank.upgradeCost(), dangling,
                            "skill '" + skill.id() + "' rank " + rank.rank() + " costs");
                }
            }
        }

        if (!dangling.isEmpty()) {
            throw new BundleFormatException(
                    "bundle references " + dangling.size() + " thing(s) it does not define:"
                            + System.lineSeparator() + String.join(System.lineSeparator(), dangling));
        }
    }

    private static void uniqueSlug(Map<Class<?>, Set<String>> byKind, Class<?> kind, String slug) {
        if (!byKind.computeIfAbsent(kind, k -> new LinkedHashSet<>()).add(slug)) {
            throw new BundleFormatException(
                    "duplicate " + kind.getSimpleName().toLowerCase(Locale.ROOT) + " '" + slug + "'");
        }
    }

    private static void requireStacks(
            Set<ItemId> known, List<ItemStack> stacks, List<String> dangling, String what) {
        stacks.forEach(stack -> require(known, stack.item(), dangling, what));
    }

    private static void require(Set<ItemId> known, ItemId item, List<String> dangling, String what) {
        if (!known.contains(item)) {
            dangling.add(what + " unknown item '" + item + "'");
        }
    }

    /** Only for the message; the sealed hierarchy is what actually matters. */
    private static String sinkKind(Sink sink) {
        return sink instanceof Fodder ? "fodder" : "upgrade";
    }
}
