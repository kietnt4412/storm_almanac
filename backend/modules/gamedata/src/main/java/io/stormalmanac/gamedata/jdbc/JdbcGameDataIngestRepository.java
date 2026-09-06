package io.stormalmanac.gamedata.jdbc;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gamedata.Craft;
import io.stormalmanac.gamedata.Fodder;
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
import io.stormalmanac.gamedata.catalog.StatCurve;
import io.stormalmanac.gamedata.catalog.Talent;
import io.stormalmanac.gamedata.ingest.GameDataBundle;
import io.stormalmanac.gamedata.ingest.GameDataIngestRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes a whole snapshot, in one transaction, in dependency order.
 *
 * <p>Two things about this class are deliberate and should survive editing.
 *
 * <p><b>Every insert carries {@code version_id} explicitly.</b> That column is
 * half of every composite foreign key in the schema, and it is what makes a
 * reference across a version boundary impossible rather than merely unlikely. It
 * looks like repetition; it is the invariant. See ADR 0008 and the header of
 * {@code V2__gamedata_canonical_schema.sql}.
 *
 * <p><b>Surrogate ids come back from {@code INSERT … RETURNING id}</b> and are
 * held in slug-to-id maps for the duration of one ingest. The domain speaks in
 * upstream slugs — {@link ItemId}, {@link EntityId} — and the schema keys on
 * generated bigints, so translating between them is this class's actual job.
 */
@Repository
public class JdbcGameDataIngestRepository implements GameDataIngestRepository {

    private final JdbcTemplate jdbc;

    public JdbcGameDataIngestRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public long ingestDraft(GameDataBundle bundle) {
        GameId game = bundle.game().id();
        upsertGame(bundle);
        replaceExistingDraft(game, bundle.sequence());

        long version = jdbc.queryForObject(
                """
                INSERT INTO gamedata.game_data_version
                    (game_id, sequence, label, status, published_at, attribution)
                VALUES (?, ?, ?, 'DRAFT', NULL, ?)
                RETURNING id
                """,
                Long.class,
                game.value(), bundle.sequence(), bundle.label(), bundle.attribution());

        Map<ItemId, Long> items = writeItems(version, bundle.items());
        Map<EntityId, Long> entities = writeEntities(version, bundle.entities(), items);
        writeSources(version, bundle.sources(), items);
        writeSinks(version, bundle.sinks(), items, entities);
        writeBanners(version, bundle.banners());
        return version;
    }

    @Override
    @Transactional
    public GameDataVersion publish(GameId game, long sequence) {
        // Stamped by the database's clock, not the caller's: it is the one clock
        // every row in this table shares, and two application instances ordering
        // approvals by their own clocks is a bug that waits for a second replica.
        List<GameDataVersion> approved = jdbc.query(
                """
                UPDATE gamedata.game_data_version
                   SET status = 'PUBLISHED', published_at = now()
                 WHERE game_id = ? AND sequence = ? AND status = 'DRAFT'
                RETURNING label, published_at, attribution
                """,
                (rs, row) -> new GameDataVersion(
                        game, sequence, rs.getString("label"),
                        Timestamps.instant(rs, "published_at"), rs.getString("attribution")),
                game.value(), sequence);

        if (approved.isEmpty()) {
            throw new NoDraftToPublishException(
                    "no draft of " + game.value() + " at sequence " + sequence + " to publish"
                            + " (it may already be published, which is not something to redo)");
        }
        return approved.getFirst();
    }

    @Override
    public List<DraftVersion> drafts(GameId game) {
        return jdbc.query(
                """
                SELECT id, sequence, label, attribution, ingested_at
                  FROM gamedata.game_data_version
                 WHERE game_id = ? AND status = 'DRAFT'
                 ORDER BY sequence DESC
                """,
                (rs, row) -> new DraftVersion(
                        rs.getLong("id"),
                        game,
                        rs.getLong("sequence"),
                        rs.getString("label"),
                        rs.getString("attribution"),
                        Timestamps.instant(rs, "ingested_at")),
                game.value());
    }

    // ── Versions ────────────────────────────────────────────────────────────

    /** A title outlives any patch of its data, so this row is not versioned. */
    private void upsertGame(GameDataBundle bundle) {
        jdbc.update(
                """
                INSERT INTO gamedata.game (id, display_name, energy_unit)
                VALUES (?, ?, ?)
                ON CONFLICT (id) DO UPDATE
                    SET display_name = EXCLUDED.display_name,
                        energy_unit  = EXCLUDED.energy_unit
                """,
                bundle.game().id().value(), bundle.game().displayName(), bundle.game().energyUnit());
    }

    /**
     * A scheduled fetch re-runs, and a failed ingest must not wedge the
     * sequence, so re-ingesting replaces a draft. It must not replace a
     * published version: every plan and drop estimate that recorded that
     * sequence did so on the promise it still means the same thing.
     */
    private void replaceExistingDraft(GameId game, long sequence) {
        String status;
        try {
            status = jdbc.queryForObject(
                    "SELECT status FROM gamedata.game_data_version WHERE game_id = ? AND sequence = ?",
                    String.class, game.value(), sequence);
        } catch (EmptyResultDataAccessException nothingThere) {
            return;
        }
        if ("PUBLISHED".equals(status)) {
            throw new PublishedVersionIsImmutableException(
                    game.value() + " sequence " + sequence + " is published and cannot be re-ingested."
                            + " Ingest the next sequence instead.");
        }
        // ON DELETE CASCADE reaches every versioned table from here.
        jdbc.update(
                "DELETE FROM gamedata.game_data_version WHERE game_id = ? AND sequence = ?",
                game.value(), sequence);
    }

    // ── Items and the catalog axis ──────────────────────────────────────────

    private Map<ItemId, Long> writeItems(long version, List<Item> items) {
        Map<ItemId, Long> ids = HashMap.newHashMap(items.size());
        for (Item item : items) {
            ids.put(item.id(), jdbc.queryForObject(
                    """
                    INSERT INTO gamedata.item
                        (version_id, slug, display_name, rarity_label, rarity_rank, category)
                    VALUES (?, ?, ?, ?, ?, ?)
                    RETURNING id
                    """,
                    Long.class,
                    version, item.id().value(), item.displayName(),
                    item.rarity().label(), item.rarity().rank(), item.category()));
        }
        return ids;
    }

    private Map<EntityId, Long> writeEntities(long version, List<Entity> entities, Map<ItemId, Long> items) {
        Map<EntityId, Long> ids = HashMap.newHashMap(entities.size());
        for (Entity entity : entities) {
            long id = jdbc.queryForObject(
                    """
                    INSERT INTO gamedata.entity
                        (version_id, slug, display_name, kind, rarity_label, rarity_rank, element)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    RETURNING id
                    """,
                    Long.class,
                    version, entity.id().value(), entity.displayName(), entity.kind(),
                    entity.rarity().label(), entity.rarity().rank(), entity.element());
            ids.put(entity.id(), id);

            List<String> tags = entity.tags();
            for (int ordinal = 0; ordinal < tags.size(); ordinal++) {
                jdbc.update(
                        "INSERT INTO gamedata.entity_tag (entity_id, version_id, ordinal, tag)"
                                + " VALUES (?, ?, ?, ?)",
                        id, version, ordinal, tags.get(ordinal));
            }
            for (StatCurve curve : entity.statCurves()) {
                writeStatCurve(version, id, curve);
            }
            for (Skill skill : entity.skills()) {
                writeSkill(version, id, skill, items);
            }
            for (Talent talent : entity.talents()) {
                jdbc.update(
                        """
                        INSERT INTO gamedata.talent
                            (version_id, entity_id, slug, display_name, unlock_condition, effect)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """,
                        version, id, talent.id(), talent.displayName(),
                        talent.unlockCondition(), talent.effect());
            }
        }
        return ids;
    }

    private void writeStatCurve(long version, long entity, StatCurve curve) {
        long id = jdbc.queryForObject(
                "INSERT INTO gamedata.stat_curve (version_id, entity_id, stat) VALUES (?, ?, ?) RETURNING id",
                Long.class, version, entity, curve.stat());
        for (StatCurve.Breakpoint point : curve.breakpoints()) {
            jdbc.update(
                    """
                    INSERT INTO gamedata.stat_curve_point
                        (curve_id, version_id, ascension_tier, level, value)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    id, version, point.ascensionTier(), point.level(), point.value());
        }
    }

    private void writeSkill(long version, long entity, Skill skill, Map<ItemId, Long> items) {
        long id = jdbc.queryForObject(
                """
                INSERT INTO gamedata.skill (version_id, entity_id, slug, display_name)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """,
                Long.class, version, entity, skill.id(), skill.displayName());

        for (Skill.Rank rank : skill.ranks()) {
            long rankId = jdbc.queryForObject(
                    """
                    INSERT INTO gamedata.skill_rank (version_id, skill_id, rank, description)
                    VALUES (?, ?, ?, ?)
                    RETURNING id
                    """,
                    Long.class, version, id, rank.rank(), rank.description());

            // One row per key rather than a JSON blob, so a patch diff can say
            // "damage went from 1.32 to 1.28" instead of "the object changed".
            rank.values().forEach((key, value) -> jdbc.update(
                    "INSERT INTO gamedata.skill_rank_value (rank_id, version_id, key, value)"
                            + " VALUES (?, ?, ?, ?)",
                    rankId, version, key, value));

            writeStacks(version, rank.upgradeCost(), items,
                    "INSERT INTO gamedata.skill_rank_cost (rank_id, version_id, ordinal, item_id, quantity)"
                            + " VALUES (?, ?, ?, ?, ?)",
                    rankId);
        }
    }

    // ── Sources ─────────────────────────────────────────────────────────────

    private void writeSources(long version, List<Source> sources, Map<ItemId, Long> items) {
        for (Source source : sources) {
            switch (source) {
                case Stage stage -> writeStage(version, stage, items);
                case Craft craft -> writeCraft(version, craft, items);
                case Shop shop -> writeShop(version, shop, items);
                case Reward reward -> writeReward(version, reward, items);
            }
        }
    }

    private void writeStage(long version, Stage stage, Map<ItemId, Long> items) {
        long id = jdbc.queryForObject(
                """
                INSERT INTO gamedata.stage
                    (version_id, slug, display_name, energy_cost, available_days, opens_at, closes_at)
                VALUES (?, ?, ?, ?, ?::text[], ?, ?)
                RETURNING id
                """,
                Long.class,
                version, stage.id(), stage.displayName(), stage.energyCost(),
                Availabilities.days(stage.availability()),
                Timestamps.at(stage.availability().opensAt()),
                Timestamps.at(stage.availability().closesAt()));

        stage.drops().forEach(drop -> jdbc.update(
                "INSERT INTO gamedata.stage_drop (stage_id, version_id, item_id, expected_yield)"
                        + " VALUES (?, ?, ?, ?)",
                id, version, items.get(drop.item()), drop.expectedYield()));
    }

    private void writeCraft(long version, Craft craft, Map<ItemId, Long> items) {
        long id = jdbc.queryForObject(
                """
                INSERT INTO gamedata.craft (version_id, slug, available_days, opens_at, closes_at)
                VALUES (?, ?, ?::text[], ?, ?)
                RETURNING id
                """,
                Long.class,
                version, craft.id(),
                Availabilities.days(craft.availability()),
                Timestamps.at(craft.availability().opensAt()),
                Timestamps.at(craft.availability().closesAt()));

        writeStacks(version, craft.consumes(), items,
                "INSERT INTO gamedata.craft_input (craft_id, version_id, ordinal, item_id, quantity)"
                        + " VALUES (?, ?, ?, ?, ?)",
                id);
        writeStacks(version, craft.produces(), items,
                "INSERT INTO gamedata.craft_output (craft_id, version_id, ordinal, item_id, quantity)"
                        + " VALUES (?, ?, ?, ?, ?)",
                id);
    }

    private void writeShop(long version, Shop shop, Map<ItemId, Long> items) {
        jdbc.update(
                """
                INSERT INTO gamedata.shop
                    (version_id, slug, currency_item_id, price, offer_item_id, offer_quantity,
                     period_limit, period_iso, available_days, opens_at, closes_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::text[], ?, ?)
                """,
                version, shop.id(), items.get(shop.currency()), shop.price(),
                items.get(shop.offer().item()), shop.offer().quantity(),
                shop.periodLimit(), shop.period().toString(),
                Availabilities.days(shop.availability()),
                Timestamps.at(shop.availability().opensAt()),
                Timestamps.at(shop.availability().closesAt()));
    }

    private void writeReward(long version, Reward reward, Map<ItemId, Long> items) {
        long id = jdbc.queryForObject(
                """
                INSERT INTO gamedata.reward
                    (version_id, slug, cadence, available_days, opens_at, closes_at)
                VALUES (?, ?, ?, ?::text[], ?, ?)
                RETURNING id
                """,
                Long.class,
                version, reward.id(), reward.cadence().name(),
                Availabilities.days(reward.availability()),
                Timestamps.at(reward.availability().opensAt()),
                Timestamps.at(reward.availability().closesAt()));

        writeStacks(version, reward.grants(), items,
                "INSERT INTO gamedata.reward_grant (reward_id, version_id, ordinal, item_id, quantity)"
                        + " VALUES (?, ?, ?, ?, ?)",
                id);
    }

    // ── Sinks ───────────────────────────────────────────────────────────────

    private void writeSinks(
            long version, List<Sink> sinks, Map<ItemId, Long> items, Map<EntityId, Long> entities) {
        for (Sink sink : sinks) {
            switch (sink) {
                case Upgrade upgrade -> {
                    long id = jdbc.queryForObject(
                            """
                            INSERT INTO gamedata.upgrade
                                (version_id, slug, entity_id, from_state, to_state)
                            VALUES (?, ?, ?, ?, ?)
                            RETURNING id
                            """,
                            Long.class,
                            version, upgrade.id(), entities.get(upgrade.entity()),
                            upgrade.fromState(), upgrade.toState());

                    writeStacks(version, upgrade.costs(), items,
                            "INSERT INTO gamedata.upgrade_cost"
                                    + " (upgrade_id, version_id, ordinal, item_id, quantity)"
                                    + " VALUES (?, ?, ?, ?, ?)",
                            id);
                }
                case Fodder fodder -> {
                    long id = jdbc.queryForObject(
                            """
                            INSERT INTO gamedata.fodder
                                (version_id, slug, consumes_category, min_rarity_label, min_rarity_rank,
                                 progress_per_unit)
                            VALUES (?, ?, ?, ?, ?, ?)
                            RETURNING id
                            """,
                            Long.class,
                            version, fodder.id(), fodder.consumesCategory(),
                            fodder.minimumRarity().label(), fodder.minimumRarity().rank(),
                            fodder.progressPerUnit());

                    writeStacks(version, fodder.costs(), items,
                            "INSERT INTO gamedata.fodder_cost"
                                    + " (fodder_id, version_id, ordinal, item_id, quantity)"
                                    + " VALUES (?, ?, ?, ?, ?)",
                            id);
                }
            }
        }
    }

    // ── Banners ─────────────────────────────────────────────────────────────

    private void writeBanners(long version, List<BannerModel> banners) {
        for (BannerModel banner : banners) {
            long id = jdbc.queryForObject(
                    """
                    INSERT INTO gamedata.banner
                        (version_id, slug, display_name, banner_type, pity_scope,
                         featured_chance_at_hit, featured_guarantee_after_loss,
                         available_days, opens_at, closes_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?::text[], ?, ?)
                    RETURNING id
                    """,
                    Long.class,
                    version, banner.id().value(), banner.displayName(), banner.bannerType(),
                    banner.pityScope().name(),
                    banner.featuredRule().chanceAtHit(), banner.featuredRule().guaranteeAfterLoss(),
                    Availabilities.days(banner.window()),
                    Timestamps.at(banner.window().opensAt()),
                    Timestamps.at(banner.window().closesAt()));

            banner.baseRates().forEach((rarity, rate) -> jdbc.update(
                    "INSERT INTO gamedata.banner_base_rate"
                            + " (banner_id, version_id, rarity_label, rarity_rank, rate)"
                            + " VALUES (?, ?, ?, ?, ?)",
                    id, version, rarity.label(), rarity.rank(), rate));

            banner.pityRules().forEach((rarity, rule) -> jdbc.update(
                    """
                    INSERT INTO gamedata.banner_pity_rule
                        (banner_id, version_id, rarity_label, rarity_rank,
                         hard_at, soft_from, soft_jump_to, soft_step)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    id, version, rarity.label(), rarity.rank(),
                    rule.hardAt(), rule.softFrom(), rule.softJumpTo(), rule.softStep()));

            List<io.stormalmanac.gamedata.banner.Floor> floors = banner.floors();
            for (int ordinal = 0; ordinal < floors.size(); ordinal++) {
                var floor = floors.get(ordinal);
                jdbc.update(
                        """
                        INSERT INTO gamedata.banner_floor
                            (banner_id, version_id, ordinal, every_n, min_rarity_label, min_rarity_rank)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """,
                        id, version, ordinal, floor.everyN(),
                        floor.minimumRarity().label(), floor.minimumRarity().rank());
            }
        }
    }

    // ── Shared ──────────────────────────────────────────────────────────────

    /**
     * Every cost, craft input and grant in the schema is the same five columns
     * with a different parent, so they share one writer. The ordinal is the
     * list position: it is the primary key alongside the parent, and it is what
     * makes the round trip return the stacks in the order the bundle wrote them.
     */
    private void writeStacks(
            long version, List<ItemStack> stacks, Map<ItemId, Long> items, String sql, long parent) {
        List<Object[]> rows = new ArrayList<>(stacks.size());
        for (int ordinal = 0; ordinal < stacks.size(); ordinal++) {
            ItemStack stack = stacks.get(ordinal);
            rows.add(new Object[] {parent, version, ordinal, items.get(stack.item()), stack.quantity()});
        }
        jdbc.batchUpdate(sql, rows);
    }
}
