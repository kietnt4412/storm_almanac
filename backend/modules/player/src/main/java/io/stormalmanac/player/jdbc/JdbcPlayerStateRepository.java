package io.stormalmanac.player.jdbc;

import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gamedata.Goal;
import io.stormalmanac.player.Goals;
import io.stormalmanac.player.Inventory;
import io.stormalmanac.player.PlayerProfile;
import io.stormalmanac.player.PlayerStateRepository;
import io.stormalmanac.player.Roster;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * The player module's door, with Postgres behind it.
 *
 * <p><b>Each save replaces a whole aggregate.</b> {@link Inventory},
 * {@link Roster} and {@link Goals} are records holding a complete map or list,
 * so "save this inventory" means "this is now the inventory" and the only
 * implementation that matches that signature is delete-then-insert. It is not a
 * placeholder for a smarter one: a per-key patch is a different operation with a
 * different signature, and the offline merge {@code Inventory}'s javadoc
 * describes needs both that operation and a per-key timestamp the schema
 * deliberately does not have yet.
 *
 * <p>What it costs is worth naming, because it is the kind of thing that is
 * obvious in a review and invisible in production: two devices saving the same
 * inventory concurrently do not merge, the second one wins entirely, and a row
 * the first device added is gone. That is the correct behaviour for the contract
 * as written and the wrong behaviour for a phone that was offline for an hour,
 * which is why sync is a named piece of work rather than something this class
 * quietly half-does.
 *
 * <p>Slugs, not foreign keys. An {@link ItemId} is stored as the upstream slug
 * it wraps and resolves against whichever game-data version a plan is solved
 * against — see V5's second decision. An item that vanishes from the game stays
 * in the inventory and stops being demanded; nothing here validates that a slug
 * still exists, because the version that would answer that question is not this
 * module's to know.
 */
@Repository
public class JdbcPlayerStateRepository implements PlayerStateRepository {

    private static final RowMapper<PlayerProfile> PROFILE = (rs, row) -> new PlayerProfile(
            ProfileId.of(rs.getString("id")),
            AccountId.of(rs.getString("account_id")),
            GameId.of(rs.getString("game_id")),
            rs.getString("display_name"),
            rs.getString("region"));

    private static final String SELECT_PROFILE =
            "SELECT id, account_id, game_id, display_name, region FROM player.profile ";

    private final JdbcTemplate jdbc;

    public JdbcPlayerStateRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerProfile> profilesOf(AccountId account) {
        return jdbc.query(
                SELECT_PROFILE + "WHERE account_id = ? ORDER BY created_at, id", PROFILE, account.value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PlayerProfile> findProfile(ProfileId id) {
        List<PlayerProfile> rows = jdbc.query(SELECT_PROFILE + "WHERE id = ?", PROFILE, id.value());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    /**
     * Insert, or rename in place.
     *
     * <p>The owner and the game are not in the {@code DO UPDATE} list on purpose.
     * Moving a profile to another account is not a rename, and letting it happen
     * through the same call that fixes a typo is how an inventory ends up on
     * somebody else's account with no trace of when.
     */
    @Override
    @Transactional
    public void saveProfile(PlayerProfile profile) {
        jdbc.update(
                """
                INSERT INTO player.profile (id, account_id, game_id, display_name, region)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET display_name = EXCLUDED.display_name
                """,
                profile.id().value(),
                profile.owner().value(),
                profile.game().value(),
                profile.displayName(),
                profile.region());
    }

    @Override
    @Transactional(readOnly = true)
    public Inventory inventoryOf(ProfileId profile) {
        Map<ItemId, Integer> quantities = new LinkedHashMap<>();
        jdbc.query(
                """
                SELECT item_slug, quantity
                  FROM player.inventory_item
                 WHERE profile_id = ?
                 ORDER BY item_slug
                """,
                rs -> {
                    quantities.put(ItemId.of(rs.getString("item_slug")), rs.getInt("quantity"));
                },
                profile.value());
        return new Inventory(profile, quantities);
    }

    @Override
    @Transactional
    public void saveInventory(Inventory inventory) {
        jdbc.update("DELETE FROM player.inventory_item WHERE profile_id = ?", inventory.profile().value());

        // Zero-quantity entries never reach the database: absent is the single
        // representation of "none", and the CHECK in V5 refuses the other one.
        // Inventory.with already drops them, so this filter is a second guard
        // against a map built by hand rather than through it.
        List<Object[]> rows = inventory.quantities().entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue() > 0)
                .map(e -> new Object[] {inventory.profile().value(), e.getKey().value(), e.getValue()})
                .toList();

        jdbc.batchUpdate(
                """
                INSERT INTO player.inventory_item (profile_id, item_slug, quantity)
                VALUES (?, ?, ?)
                """,
                rows);
    }

    @Override
    @Transactional(readOnly = true)
    public Roster rosterOf(ProfileId profile) {
        Map<EntityId, String> states = new LinkedHashMap<>();
        jdbc.query(
                """
                SELECT entity_slug, current_state
                  FROM player.roster_entry
                 WHERE profile_id = ?
                 ORDER BY entity_slug
                """,
                rs -> {
                    states.put(EntityId.of(rs.getString("entity_slug")), rs.getString("current_state"));
                },
                profile.value());
        return new Roster(profile, states);
    }

    @Override
    @Transactional
    public void saveRoster(Roster roster) {
        jdbc.update("DELETE FROM player.roster_entry WHERE profile_id = ?", roster.profile().value());

        List<Object[]> rows = roster.currentState().entrySet().stream()
                .map(e -> new Object[] {roster.profile().value(), e.getKey().value(), e.getValue()})
                .toList();

        jdbc.batchUpdate(
                """
                INSERT INTO player.roster_entry (profile_id, entity_slug, current_state)
                VALUES (?, ?, ?)
                """,
                rows);
    }

    /** Ordered by the player's own ordering, which is why {@code ordinal} is stored. */
    @Override
    @Transactional(readOnly = true)
    public Goals goalsOf(ProfileId profile) {
        List<Goal> goals = jdbc.query(
                """
                SELECT entity_slug, target_state, satisfiability, priority
                  FROM player.goal
                 WHERE profile_id = ?
                 ORDER BY ordinal
                """,
                (rs, row) -> new Goal(
                        EntityId.of(rs.getString("entity_slug")),
                        rs.getString("target_state"),
                        Goal.Satisfiability.valueOf(rs.getString("satisfiability")),
                        rs.getInt("priority")),
                profile.value());
        return new Goals(profile, goals);
    }

    @Override
    @Transactional
    public void saveGoals(Goals goals) {
        jdbc.update("DELETE FROM player.goal WHERE profile_id = ?", goals.profile().value());

        List<Object[]> rows = new ArrayList<>();
        List<Goal> list = goals.goals();
        for (int ordinal = 0; ordinal < list.size(); ordinal++) {
            Goal goal = list.get(ordinal);
            rows.add(new Object[] {
                goals.profile().value(),
                ordinal,
                goal.entity().value(),
                goal.targetState(),
                goal.satisfiability().name(),
                goal.priority()
            });
        }

        jdbc.batchUpdate(
                """
                INSERT INTO player.goal
                       (profile_id, ordinal, entity_slug, target_state, satisfiability, priority)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                rows);
    }
}
