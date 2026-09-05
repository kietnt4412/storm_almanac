package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * The gamedata schema's load-bearing invariants, asserted against a real
 * Postgres rather than described in a comment.
 *
 * <p>A migration that applies cleanly is not a migration that is correct.
 * Everything below is a rule {@code V2__gamedata_canonical_schema.sql} claims to
 * enforce, and each one is checked in both directions — the violation is
 * refused <em>and</em> the legitimate case still goes through. A constraint that
 * only ever rejects is indistinguishable from a broken table.
 *
 * <p>No Spring context: this tests the schema, and booting the application to do
 * it would only make it slower. {@link ApplicationBootTest} covers the other job.
 */
@Testcontainers
class GameDataSchemaTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeAll
    static void migrate() {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    @Test
    @DisplayName("a reference cannot cross a version boundary, and the same-version one still works")
    void referencesAreConfinedToTheirVersion() throws SQLException {
        try (Connection db = open()) {
            String game = game(db, "cross-version");
            long v1 = version(db, game, 0);
            long v2 = version(db, game, 1);

            long itemInV1 = item(db, v1, "sulfur");
            long itemInV2 = item(db, v2, "sulfur");
            long stageInV2 = stage(db, v2, "1-1");

            // The whole point of the composite foreign keys. Without them this
            // insert succeeds, the v2 stage silently cites a v1 item, and every
            // patch diff computed afterwards is wrong in a way nobody can see.
            assertThatThrownBy(() -> exec(db,
                    "INSERT INTO gamedata.stage_drop (stage_id, version_id, item_id, expected_yield)"
                            + " VALUES (?, ?, ?, ?)",
                    stageInV2, v2, itemInV1, 0.5))
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("stage_drop_item_fk");

            // Positive control: the identical row, pointing at v2's own copy of
            // the item, is accepted.
            exec(db,
                    "INSERT INTO gamedata.stage_drop (stage_id, version_id, item_id, expected_yield)"
                            + " VALUES (?, ?, ?, ?)",
                    stageInV2, v2, itemInV2, 0.5);

            assertThat(count(db, "SELECT count(*) FROM gamedata.stage_drop WHERE stage_id = " + stageInV2))
                    .isEqualTo(1);
        }
    }

    @Test
    @DisplayName("a published version carries its approval timestamp and a draft carries none")
    void publishingIsApproval() throws SQLException {
        try (Connection db = open()) {
            String game = game(db, "approval");

            // "Latest" must mean latest approved, never latest fetched. A
            // PUBLISHED row with no approval time would make that unanswerable.
            assertThatThrownBy(() -> exec(db,
                    "INSERT INTO gamedata.game_data_version"
                            + " (game_id, sequence, label, status, published_at, attribution)"
                            + " VALUES (?, 0, '1.0', 'PUBLISHED', NULL, 'test')",
                    game))
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("game_data_version_published_has_timestamp");

            // And the other direction: a draft has not been approved by anyone,
            // so it must not carry a timestamp claiming otherwise.
            assertThatThrownBy(() -> exec(db,
                    "INSERT INTO gamedata.game_data_version"
                            + " (game_id, sequence, label, status, published_at, attribution)"
                            + " VALUES (?, 1, '1.0', 'DRAFT', now(), 'test')",
                    game))
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("game_data_version_published_has_timestamp");

            exec(db,
                    "INSERT INTO gamedata.game_data_version"
                            + " (game_id, sequence, label, status, published_at, attribution)"
                            + " VALUES (?, 2, '1.0', 'PUBLISHED', now(), 'test')",
                    game);
            exec(db,
                    "INSERT INTO gamedata.game_data_version"
                            + " (game_id, sequence, label, status, published_at, attribution)"
                            + " VALUES (?, 3, '1.1', 'DRAFT', NULL, 'test')",
                    game);

            assertThat(count(db,
                    "SELECT count(*) FROM gamedata.game_data_version WHERE game_id = '" + game + "'"))
                    .isEqualTo(2);
        }
    }

    @Test
    @DisplayName("every version records where its numbers came from")
    void attributionIsMandatory() throws SQLException {
        try (Connection db = open()) {
            String game = game(db, "attribution");

            // "Numbers and text only, attributed" is a project invariant. A row
            // nobody can source is a row nobody can defend.
            assertThatThrownBy(() -> exec(db,
                    "INSERT INTO gamedata.game_data_version"
                            + " (game_id, sequence, label, status, attribution)"
                            + " VALUES (?, 0, '1.0', 'DRAFT', '   ')",
                    game))
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("game_data_version_attribution_not_blank");
        }
    }

    @Test
    @DisplayName("a drop yield may exceed 1.0 but may not be NaN")
    void dropYieldIsAFiniteQuantity() throws SQLException {
        try (Connection db = open()) {
            String game = game(db, "yield");
            long version = version(db, game, 0);
            long item = item(db, version, "sulfur");
            long stage = stage(db, version, "1-1");

            // A stage can drop several copies in one run, so real upstream data
            // carries yields well past 1.0. Rejecting those would have thrown
            // out most of the dataset — see docs/prior-art.md section 4.1.
            exec(db,
                    "INSERT INTO gamedata.stage_drop (stage_id, version_id, item_id, expected_yield)"
                            + " VALUES (?, ?, ?, 3.7)",
                    stage, version, item);

            long other = item(db, version, "gold");

            // The subtle one. In Postgres NaN sorts above every number, so a
            // plain "expected_yield >= 0" would wave it straight through; the
            // upper bound is what actually catches it.
            assertThatThrownBy(() -> exec(db,
                    "INSERT INTO gamedata.stage_drop (stage_id, version_id, item_id, expected_yield)"
                            + " VALUES (?, ?, ?, 'NaN')",
                    stage, version, other))
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("stage_drop_yield_finite");
        }
    }

    @Test
    @DisplayName("an upgrade graph rejects a duplicate edge between the same two states")
    void upgradeEdgesAreUnique() throws SQLException {
        try (Connection db = open()) {
            String game = game(db, "upgrades");
            long version = version(db, game, 0);
            long entity = entity(db, version, "six-fingers", "character");

            exec(db,
                    "INSERT INTO gamedata.upgrade (version_id, slug, entity_id, from_state, to_state)"
                            + " VALUES (?, 'insight-1', ?, 'insight-0', 'insight-1')",
                    version, entity);

            // Two rows for the same edge would be two costs for one advance, and
            // the planner would have no way to choose between them.
            assertThatThrownBy(() -> exec(db,
                    "INSERT INTO gamedata.upgrade (version_id, slug, entity_id, from_state, to_state)"
                            + " VALUES (?, 'insight-1-again', ?, 'insight-0', 'insight-1')",
                    version, entity))
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("upgrade_edge_unique");
        }
    }

    @Test
    @DisplayName("equipment and characters are the same table, told apart by kind")
    void equipmentIsAnEntity() throws SQLException {
        try (Connection db = open()) {
            String game = game(db, "equipment");
            long version = version(db, game, 0);

            // ADR 0007. Both are entities; both can therefore carry an upgrade
            // graph and be named as a goal, which is the reason for the decision.
            long character = entity(db, version, "six-fingers", "character");
            long psychube = entity(db, version, "hopscotch", "equipment");

            exec(db,
                    "INSERT INTO gamedata.upgrade (version_id, slug, entity_id, from_state, to_state)"
                            + " VALUES (?, 'char-insight-1', ?, 'insight-0', 'insight-1')",
                    version, character);
            exec(db,
                    "INSERT INTO gamedata.upgrade (version_id, slug, entity_id, from_state, to_state)"
                            + " VALUES (?, 'gear-level-30', ?, 'level-1', 'level-30')",
                    version, psychube);

            assertThat(count(db,
                    "SELECT count(*) FROM gamedata.entity WHERE version_id = " + version
                            + " AND kind = 'equipment'"))
                    .isEqualTo(1);
            assertThat(count(db, "SELECT count(*) FROM gamedata.upgrade WHERE version_id = " + version))
                    .isEqualTo(2);
        }
    }

    // ── fixtures ────────────────────────────────────────────────────────────
    // Each test seeds its own game so that nothing it inserts can be seen by
    // another; the container is shared and the tables are not truncated between
    // tests.

    private Connection open() throws SQLException {
        return DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private String game(Connection db, String id) throws SQLException {
        exec(db, "INSERT INTO gamedata.game (id, display_name, energy_unit) VALUES (?, ?, 'Activity')", id, id);
        return id;
    }

    private long version(Connection db, String game, long sequence) throws SQLException {
        return insertReturningId(db,
                "INSERT INTO gamedata.game_data_version"
                        + " (game_id, sequence, label, status, published_at, attribution)"
                        + " VALUES (?, ?, 'test', 'PUBLISHED', now(), 'fixture') RETURNING id",
                game, sequence);
    }

    private long item(Connection db, long version, String slug) throws SQLException {
        return insertReturningId(db,
                "INSERT INTO gamedata.item (version_id, slug, display_name, rarity_label, rarity_rank, category)"
                        + " VALUES (?, ?, ?, '3*', 3, 'material') RETURNING id",
                version, slug, slug);
    }

    private long entity(Connection db, long version, String slug, String kind) throws SQLException {
        return insertReturningId(db,
                "INSERT INTO gamedata.entity"
                        + " (version_id, slug, display_name, kind, rarity_label, rarity_rank, element)"
                        + " VALUES (?, ?, ?, ?, '6*', 6, '') RETURNING id",
                version, slug, slug, kind);
    }

    private long stage(Connection db, long version, String slug) throws SQLException {
        return insertReturningId(db,
                "INSERT INTO gamedata.stage (version_id, slug, display_name, energy_cost)"
                        + " VALUES (?, ?, ?, 20) RETURNING id",
                version, slug, slug);
    }

    private void exec(Connection db, String sql, Object... params) throws SQLException {
        try (PreparedStatement statement = db.prepareStatement(sql)) {
            bind(statement, params);
            statement.executeUpdate();
        }
    }

    private long insertReturningId(Connection db, String sql, Object... params) throws SQLException {
        try (PreparedStatement statement = db.prepareStatement(sql)) {
            bind(statement, params);
            try (ResultSet keys = statement.executeQuery()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    private long count(Connection db, String sql) throws SQLException {
        try (PreparedStatement statement = db.prepareStatement(sql);
                ResultSet rows = statement.executeQuery()) {
            rows.next();
            return rows.getLong(1);
        }
    }

    private void bind(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }
}
