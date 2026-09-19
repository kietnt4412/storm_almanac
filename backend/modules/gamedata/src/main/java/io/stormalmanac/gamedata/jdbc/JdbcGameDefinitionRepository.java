package io.stormalmanac.gamedata.jdbc;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.BannerId;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.StageId;
import io.stormalmanac.gamedata.Craft;
import io.stormalmanac.gamedata.Drop;
import io.stormalmanac.gamedata.Fodder;
import io.stormalmanac.gamedata.Game;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.gamedata.Item;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Progress;
import io.stormalmanac.gamedata.Rarity;
import io.stormalmanac.gamedata.Reward;
import io.stormalmanac.gamedata.Shop;
import io.stormalmanac.gamedata.Sink;
import io.stormalmanac.gamedata.Source;
import io.stormalmanac.gamedata.Stage;
import io.stormalmanac.gamedata.Upgrade;
import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.gamedata.banner.FeaturedRule;
import io.stormalmanac.gamedata.banner.Floor;
import io.stormalmanac.gamedata.banner.PityRule;
import io.stormalmanac.gamedata.banner.PityScope;
import io.stormalmanac.gamedata.catalog.Entity;
import io.stormalmanac.gamedata.catalog.Skill;
import io.stormalmanac.gamedata.catalog.StatCurve;
import io.stormalmanac.gamedata.catalog.Talent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads one whole published version.
 *
 * <p>Every collection is fetched as one query over the version and grouped by
 * parent in memory. Fifteen queries and no joins beats one query per parent,
 * and a version is a few thousand rows — small enough that assembling it in
 * Java is the cheap part.
 *
 * <p>Reads are confined to {@code status = 'PUBLISHED'}. A draft has been
 * fetched, not approved, and {@code GameDefinitionRepository}'s contract is
 * that "latest" means latest approved. There is no method here that can return
 * one; that is the point.
 */
@Repository
public class JdbcGameDefinitionRepository implements GameDefinitionRepository {

    private final JdbcTemplate jdbc;

    public JdbcGameDefinitionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GameDefinition> findLatest(GameId game) {
        return versionRow(
                """
                SELECT v.id, v.sequence, v.label, v.published_at, v.attribution,
                       g.id AS game_id, g.display_name AS game_name, g.energy_unit
                  FROM gamedata.game_data_version v
                  JOIN gamedata.game g ON g.id = v.game_id
                 WHERE v.game_id = ? AND v.status = 'PUBLISHED'
                 ORDER BY v.sequence DESC
                 LIMIT 1
                """,
                game.value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GameDefinition> find(GameId game, long sequence) {
        return versionRow(
                """
                SELECT v.id, v.sequence, v.label, v.published_at, v.attribution,
                       g.id AS game_id, g.display_name AS game_name, g.energy_unit
                  FROM gamedata.game_data_version v
                  JOIN gamedata.game g ON g.id = v.game_id
                 WHERE v.game_id = ? AND v.sequence = ? AND v.status = 'PUBLISHED'
                """,
                game.value(), sequence);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameDataVersion> versions(GameId game) {
        return jdbc.query(
                """
                SELECT sequence, label, published_at, attribution
                  FROM gamedata.game_data_version
                 WHERE game_id = ? AND status = 'PUBLISHED'
                 ORDER BY sequence DESC
                """,
                (rs, row) -> new GameDataVersion(
                        game, rs.getLong("sequence"), rs.getString("label"),
                        Timestamps.instant(rs, "published_at"), rs.getString("attribution")),
                game.value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublishedGame> publishedGames() {
        // DISTINCT ON rather than a group-by with a second lookup: one row per
        // game, and the row is the newest published version of it. Postgres
        // orders inside the group, so the "newest" here is the same definition
        // of newest that findLatest uses rather than a second one that could
        // drift from it.
        return jdbc.query(
                """
                SELECT DISTINCT ON (v.game_id)
                       v.game_id, v.sequence, v.label, v.published_at, v.attribution,
                       g.display_name AS game_name, g.energy_unit
                  FROM gamedata.game_data_version v
                  JOIN gamedata.game g ON g.id = v.game_id
                 WHERE v.status = 'PUBLISHED'
                 ORDER BY v.game_id, v.sequence DESC
                """,
                (rs, row) -> {
                    GameId id = new GameId(rs.getString("game_id"));
                    return new PublishedGame(
                            new Game(id, rs.getString("game_name"), rs.getString("energy_unit")),
                            new GameDataVersion(
                                    id,
                                    rs.getLong("sequence"),
                                    rs.getString("label"),
                                    Timestamps.instant(rs, "published_at"),
                                    rs.getString("attribution")));
                });
    }

    private Optional<GameDefinition> versionRow(String sql, Object... args) {
        List<Header> headers = jdbc.query(sql, (rs, row) -> new Header(
                rs.getLong("id"),
                new Game(new GameId(rs.getString("game_id")), rs.getString("game_name"),
                        rs.getString("energy_unit")),
                new GameDataVersion(
                        new GameId(rs.getString("game_id")),
                        rs.getLong("sequence"),
                        rs.getString("label"),
                        Timestamps.instant(rs, "published_at"),
                        rs.getString("attribution"))),
                args);

        return headers.stream().findFirst().map(this::load);
    }

    /** The version row itself, before any of the data hanging off it. */
    private record Header(long id, Game game, GameDataVersion version) {}

    private GameDefinition load(Header header) {
        long version = header.id();

        // Surrogate id back to the upstream slug, built on the same pass that
        // builds the items themselves. Every child table keys on the bigint; the
        // domain speaks only slugs, so nothing else can be assembled until this
        // map exists.
        List<Item> items = new ArrayList<>();
        Map<Long, ItemId> itemSlugs = new HashMap<>();
        jdbc.query(
                """
                SELECT id, slug, display_name, rarity_label, rarity_rank, category
                  FROM gamedata.item WHERE version_id = ? ORDER BY id
                """,
                rs -> {
                    ItemId slug = new ItemId(rs.getString("slug"));
                    itemSlugs.put(rs.getLong("id"), slug);
                    items.add(new Item(slug, rs.getString("display_name"),
                            rarity(rs, "rarity_label", "rarity_rank"), rs.getString("category")));
                },
                version);

        List<Source> sources = new ArrayList<>();
        sources.addAll(stages(version, itemSlugs));
        sources.addAll(crafts(version, itemSlugs));
        sources.addAll(shops(version, itemSlugs));
        sources.addAll(rewards(version, itemSlugs));

        List<Sink> sinks = new ArrayList<>();
        sinks.addAll(upgrades(version, itemSlugs));
        sinks.addAll(fodder(version, itemSlugs));

        return new GameDefinition(
                header.game(), header.version(), items, sources, sinks,
                banners(version), entities(version, itemSlugs));
    }

    // ── Entities and the catalog axis ───────────────────────────────────────

    private List<Entity> entities(long version, Map<Long, ItemId> items) {
        Map<Long, List<String>> tags = grouped(version,
                "SELECT entity_id, tag FROM gamedata.entity_tag WHERE version_id = ? ORDER BY entity_id, ordinal",
                "entity_id", (rs, row) -> rs.getString("tag"));
        Map<Long, List<StatCurve>> curves = statCurves(version);
        Map<Long, List<Skill>> skills = skills(version, items);
        Map<Long, List<Talent>> talents = grouped(version,
                """
                SELECT entity_id, slug, display_name, unlock_condition, effect
                  FROM gamedata.talent WHERE version_id = ? ORDER BY entity_id, id
                """,
                "entity_id", (rs, row) -> new Talent(
                        rs.getString("slug"), rs.getString("display_name"),
                        rs.getString("unlock_condition"), rs.getString("effect")));

        return jdbc.query(
                """
                SELECT id, slug, display_name, kind, rarity_label, rarity_rank, element
                  FROM gamedata.entity WHERE version_id = ? ORDER BY id
                """,
                (rs, row) -> {
                    long id = rs.getLong("id");
                    return new Entity(
                            new EntityId(rs.getString("slug")), rs.getString("display_name"),
                            rs.getString("kind"), rarity(rs, "rarity_label", "rarity_rank"),
                            rs.getString("element"),
                            tags.getOrDefault(id, List.of()),
                            curves.getOrDefault(id, List.of()),
                            skills.getOrDefault(id, List.of()),
                            talents.getOrDefault(id, List.of()));
                },
                version);
    }

    private Map<Long, List<StatCurve>> statCurves(long version) {
        Map<Long, List<StatCurve.Breakpoint>> points = grouped(version,
                """
                SELECT curve_id, ascension_tier, level, value
                  FROM gamedata.stat_curve_point WHERE version_id = ?
                 ORDER BY curve_id, ascension_tier, level
                """,
                "curve_id", (rs, row) -> new StatCurve.Breakpoint(
                        rs.getInt("ascension_tier"), rs.getInt("level"), rs.getDouble("value")));

        Map<Long, List<StatCurve>> byEntity = new LinkedHashMap<>();
        jdbc.query("SELECT id, entity_id, stat FROM gamedata.stat_curve WHERE version_id = ? ORDER BY entity_id, id",
                rs -> {
                    byEntity.computeIfAbsent(rs.getLong("entity_id"), key -> new ArrayList<>())
                            .add(new StatCurve(rs.getString("stat"),
                                    points.getOrDefault(rs.getLong("id"), List.of())));
                },
                version);
        return byEntity;
    }

    private Map<Long, List<Skill>> skills(long version, Map<Long, ItemId> items) {
        Map<Long, Map<String, Double>> values = new LinkedHashMap<>();
        jdbc.query("SELECT rank_id, key, value FROM gamedata.skill_rank_value WHERE version_id = ? ORDER BY key",
                rs -> {
                    values.computeIfAbsent(rs.getLong("rank_id"), key -> new LinkedHashMap<>())
                            .put(rs.getString("key"), rs.getDouble("value"));
                },
                version);
        Map<Long, List<ItemStack>> costs = stacks(version, "skill_rank_cost", "rank_id", items);

        Map<Long, List<Skill.Rank>> ranks = grouped(version,
                """
                SELECT id, skill_id, rank, description
                  FROM gamedata.skill_rank WHERE version_id = ? ORDER BY skill_id, rank
                """,
                "skill_id", (rs, row) -> {
                    long id = rs.getLong("id");
                    return new Skill.Rank(
                            rs.getInt("rank"),
                            values.getOrDefault(id, Map.of()),
                            rs.getString("description"),
                            costs.getOrDefault(id, List.of()));
                });

        Map<Long, List<Skill>> byEntity = new LinkedHashMap<>();
        jdbc.query(
                """
                SELECT id, entity_id, slug, display_name
                  FROM gamedata.skill WHERE version_id = ? ORDER BY entity_id, id
                """,
                rs -> {
                    byEntity.computeIfAbsent(rs.getLong("entity_id"), key -> new ArrayList<>())
                            .add(new Skill(rs.getString("slug"), rs.getString("display_name"),
                                    ranks.getOrDefault(rs.getLong("id"), List.of())));
                },
                version);
        return byEntity;
    }

    // ── Sources ─────────────────────────────────────────────────────────────

    private List<Stage> stages(long version, Map<Long, ItemId> items) {
        // Ordered by the item's id and not by the row's, which is the difference
        // between a canonical order and an accidental one. `stage_drop.item_id`
        // is a surrogate handed out in the order items were ingested, so ordering
        // by it returns whatever order the catalogue happened to arrive in — and
        // `Stage.drops()` is a List, so two orders of the same drops are two
        // unequal stages. That made "the schema gives the data back unchanged"
        // true only while an upstream listed a stage's drops in the same order it
        // listed its items. The Reverse: 1999 sampled tables do not, and the
        // round-trip test failed the moment the adapter started reading them.
        Map<Long, List<Drop>> drops = grouped(version,
                """
                SELECT stage_id, item_id, expected_yield, sampled_runs
                  FROM gamedata.stage_drop WHERE version_id = ? ORDER BY stage_id, item_id
                """,
                "stage_id", (rs, row) -> new Drop(
                        items.get(rs.getLong("item_id")),
                        rs.getDouble("expected_yield"),
                        rs.getLong("sampled_runs")));
        drops.values().forEach(list -> list.sort(Comparator.comparing(drop -> drop.item().value())));

        return jdbc.query(
                """
                SELECT id, slug, display_name, energy_cost, available_days, opens_at, closes_at
                  FROM gamedata.stage WHERE version_id = ? ORDER BY id
                """,
                (rs, row) -> new Stage(
                        new StageId(rs.getString("slug")), rs.getString("display_name"),
                        rs.getInt("energy_cost"),
                        drops.getOrDefault(rs.getLong("id"), List.of()),
                        Availabilities.read(rs)),
                version);
    }

    private List<Craft> crafts(long version, Map<Long, ItemId> items) {
        Map<Long, List<ItemStack>> inputs = stacks(version, "craft_input", "craft_id", items);
        Map<Long, List<ItemStack>> outputs = stacks(version, "craft_output", "craft_id", items);

        return jdbc.query(
                """
                SELECT id, slug, available_days, opens_at, closes_at
                  FROM gamedata.craft WHERE version_id = ? ORDER BY id
                """,
                (rs, row) -> {
                    long id = rs.getLong("id");
                    return new Craft(
                            rs.getString("slug"),
                            inputs.getOrDefault(id, List.of()),
                            outputs.getOrDefault(id, List.of()),
                            Availabilities.read(rs));
                },
                version);
    }

    private List<Shop> shops(long version, Map<Long, ItemId> items) {
        return jdbc.query(
                """
                SELECT slug, currency_item_id, price, offer_item_id, offer_quantity,
                       period_limit, period_iso, available_days, opens_at, closes_at
                  FROM gamedata.shop WHERE version_id = ? ORDER BY id
                """,
                (rs, row) -> new Shop(
                        rs.getString("slug"),
                        items.get(rs.getLong("currency_item_id")),
                        rs.getInt("price"),
                        new ItemStack(items.get(rs.getLong("offer_item_id")), rs.getInt("offer_quantity")),
                        rs.getInt("period_limit"),
                        Period.parse(rs.getString("period_iso")),
                        Availabilities.read(rs)),
                version);
    }

    private List<Reward> rewards(long version, Map<Long, ItemId> items) {
        Map<Long, List<ItemStack>> grants = stacks(version, "reward_grant", "reward_id", items);

        return jdbc.query(
                """
                SELECT id, slug, cadence, available_days, opens_at, closes_at
                  FROM gamedata.reward WHERE version_id = ? ORDER BY id
                """,
                (rs, row) -> new Reward(
                        rs.getString("slug"),
                        Reward.Cadence.valueOf(rs.getString("cadence")),
                        grants.getOrDefault(rs.getLong("id"), List.of()),
                        Availabilities.read(rs)),
                version);
    }

    // ── Sinks ───────────────────────────────────────────────────────────────

    private List<Upgrade> upgrades(long version, Map<Long, ItemId> items) {
        Map<Long, List<ItemStack>> costs = stacks(version, "upgrade_cost", "upgrade_id", items);
        Map<Long, List<String>> requires = new HashMap<>();
        jdbc.query(
                "SELECT upgrade_id, state FROM gamedata.upgrade_requirement WHERE version_id = ?"
                        + " ORDER BY upgrade_id, state",
                (RowCallbackHandler) rs -> requires
                        .computeIfAbsent(rs.getLong("upgrade_id"), k -> new ArrayList<>())
                        .add(rs.getString("state")),
                version);
        Map<Long, List<Progress>> progress = new HashMap<>();
        jdbc.query(
                "SELECT upgrade_id, kind, quantity FROM gamedata.upgrade_progress WHERE version_id = ?"
                        + " ORDER BY upgrade_id, kind",
                (RowCallbackHandler) rs -> progress
                        .computeIfAbsent(rs.getLong("upgrade_id"), k -> new ArrayList<>())
                        .add(new Progress(rs.getString("kind"), rs.getInt("quantity"))),
                version);

        return jdbc.query(
                """
                SELECT u.id, u.slug, e.slug AS entity_slug, u.from_state, u.to_state
                  FROM gamedata.upgrade u
                  JOIN gamedata.entity e ON e.id = u.entity_id AND e.version_id = u.version_id
                 WHERE u.version_id = ? ORDER BY u.id
                """,
                (rs, row) -> new Upgrade(
                        rs.getString("slug"),
                        new EntityId(rs.getString("entity_slug")),
                        rs.getString("from_state"), rs.getString("to_state"),
                        costs.getOrDefault(rs.getLong("id"), List.of()),
                        requires.getOrDefault(rs.getLong("id"), List.of()),
                        progress.getOrDefault(rs.getLong("id"), List.of())),
                version);
    }

    private List<Fodder> fodder(long version, Map<Long, ItemId> items) {
        Map<Long, List<ItemStack>> costs = stacks(version, "fodder_cost", "fodder_id", items);

        return jdbc.query(
                """
                SELECT id, slug, consumes_category, min_rarity_label, min_rarity_rank, progress,
                       progress_per_unit
                  FROM gamedata.fodder WHERE version_id = ? ORDER BY id
                """,
                (rs, row) -> new Fodder(
                        rs.getString("slug"), rs.getString("consumes_category"),
                        rarity(rs, "min_rarity_label", "min_rarity_rank"),
                        rs.getString("progress"),
                        rs.getInt("progress_per_unit"),
                        costs.getOrDefault(rs.getLong("id"), List.of())),
                version);
    }

    // ── Banners ─────────────────────────────────────────────────────────────

    private List<BannerModel> banners(long version) {
        Map<Long, Map<Rarity, Double>> baseRates = new LinkedHashMap<>();
        jdbc.query(
                """
                SELECT banner_id, rarity_label, rarity_rank, rate
                  FROM gamedata.banner_base_rate WHERE version_id = ? ORDER BY banner_id, rarity_rank
                """,
                rs -> {
                    baseRates.computeIfAbsent(rs.getLong("banner_id"), key -> new LinkedHashMap<>())
                            .put(rarity(rs, "rarity_label", "rarity_rank"), rs.getDouble("rate"));
                },
                version);

        Map<Long, Map<Rarity, PityRule>> pityRules = new LinkedHashMap<>();
        jdbc.query(
                """
                SELECT banner_id, rarity_label, rarity_rank, hard_at, soft_from, soft_jump_to, soft_step
                  FROM gamedata.banner_pity_rule WHERE version_id = ? ORDER BY banner_id, rarity_rank
                """,
                rs -> {
                    // soft_from is null when the banner has no soft pity, and the
                    // other two follow it. getInt/getDouble would read those as 0.
                    Integer softFrom = (Integer) rs.getObject("soft_from");
                    pityRules.computeIfAbsent(rs.getLong("banner_id"), key -> new LinkedHashMap<>())
                            .put(rarity(rs, "rarity_label", "rarity_rank"),
                                    new PityRule(
                                            rs.getInt("hard_at"),
                                            softFrom,
                                            (Double) rs.getObject("soft_jump_to"),
                                            (Double) rs.getObject("soft_step")));
                },
                version);

        Map<Long, List<Floor>> floors = grouped(version,
                """
                SELECT banner_id, every_n, min_rarity_label, min_rarity_rank
                  FROM gamedata.banner_floor WHERE version_id = ? ORDER BY banner_id, ordinal
                """,
                "banner_id", (rs, row) -> new Floor(
                        rs.getInt("every_n"), rarity(rs, "min_rarity_label", "min_rarity_rank")));

        return jdbc.query(
                """
                SELECT id, slug, display_name, banner_type, pity_scope,
                       featured_chance_at_hit, featured_guarantee_after_loss,
                       available_days, opens_at, closes_at
                  FROM gamedata.banner WHERE version_id = ? ORDER BY id
                """,
                (rs, row) -> {
                    long id = rs.getLong("id");
                    return new BannerModel(
                            new BannerId(rs.getString("slug")), rs.getString("display_name"),
                            rs.getString("banner_type"),
                            baseRates.getOrDefault(id, Map.of()),
                            pityRules.getOrDefault(id, Map.of()),
                            floors.getOrDefault(id, List.of()),
                            new FeaturedRule(
                                    rs.getDouble("featured_chance_at_hit"),
                                    rs.getInt("featured_guarantee_after_loss")),
                            PityScope.valueOf(rs.getString("pity_scope")),
                            Availabilities.read(rs));
                },
                version);
    }

    // ── Shared ──────────────────────────────────────────────────────────────

    /**
     * Every cost, craft input and grant is the same five columns with a
     * different parent, so one loader serves them all.
     *
     * <p>The table and column names are string-concatenated into the SQL. They
     * are compile-time constants from the call sites in this file and can never
     * be anything else; the alternative is nine near-identical queries.
     */
    private Map<Long, List<ItemStack>> stacks(
            long version, String table, String parentColumn, Map<Long, ItemId> items) {
        return grouped(version,
                "SELECT " + parentColumn + ", item_id, quantity FROM gamedata." + table
                        + " WHERE version_id = ? ORDER BY " + parentColumn + ", ordinal",
                parentColumn,
                (rs, row) -> new ItemStack(items.get(rs.getLong("item_id")), rs.getInt("quantity")));
    }

    /** One query over the whole version, bucketed by parent id in memory. */
    private <T> Map<Long, List<T>> grouped(long version, String sql, String parentColumn, RowMapper<T> mapper) {
        Map<Long, List<T>> byParent = new LinkedHashMap<>();
        jdbc.query(sql, rs -> {
            byParent.computeIfAbsent(rs.getLong(parentColumn), key -> new ArrayList<>())
                    .add(mapper.mapRow(rs, 0));
        }, version);
        return byParent;
    }

    private static Rarity rarity(ResultSet rs, String label, String rank) throws SQLException {
        return new Rarity(rs.getString(label), rs.getInt(rank));
    }
}
