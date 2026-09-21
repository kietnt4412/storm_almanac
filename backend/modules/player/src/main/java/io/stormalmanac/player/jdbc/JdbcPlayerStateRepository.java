package io.stormalmanac.player.jdbc;

import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gamedata.Goal;
import io.stormalmanac.player.Goals;
import io.stormalmanac.player.Inventory;
import io.stormalmanac.player.InventoryEdit;
import io.stormalmanac.player.MergeOutcome;
import io.stormalmanac.player.PlayerProfile;
import io.stormalmanac.player.PlayerStateRepository;
import io.stormalmanac.player.Roster;
import io.stormalmanac.player.RosterEdit;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * The player module's door, with Postgres behind it.
 *
 * <p><b>Each save replaces a whole aggregate; each merge does not.</b>
 * {@link Inventory}, {@link Roster} and {@link Goals} are records holding a
 * complete map or list, so "save this inventory" means "this is now the
 * inventory" and the only implementation matching that signature is
 * delete-then-insert. Two devices saving concurrently therefore do not merge:
 * the second wins entirely and a row the first added is gone. That is correct
 * for the contract as written and wrong for a phone that was offline for an
 * hour, which is why {@link #mergeInventory} and {@link #mergeRoster} are
 * separate operations with separate signatures rather than something the saves
 * quietly half-do.
 *
 * <p>The two are not independent. A save marks the whole aggregate as stated in
 * full, just now — see {@link #settleWholeAggregate} — because otherwise a patch
 * carrying an older edit would beat a save that had just replaced everything.
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

    /**
     * The merge clock's two namespaces. Goals are absent on purpose — see
     * {@link RosterEdit} and V6.
     */
    private static final String INVENTORY = "inventory";

    private static final String ROSTER = "roster";

    /**
     * Claim one key's clock, if and only if this edit is newer than everything
     * already known about that key.
     *
     * <p>All three rules that make the merge safe are in this one statement,
     * which is why it is a constant rather than a select and some Java around
     * it. Deciding them in the database is also what makes it correct under two
     * devices syncing at once: read-then-write across two statements is a lost
     * update waiting for a scheduler to find it. The row count is the answer —
     * 1 means this edit won and its value should be written, 0 means it lost and
     * nothing else should happen.
     *
     * <ul>
     *   <li>{@code LEAST(..., now())} is the clamp. An edit cannot claim to have
     *       happened later than the server heard it, so a device with a fast
     *       clock cannot pin a key against every later write forever.
     *   <li>The {@code NOT EXISTS} is the whole-aggregate watermark: an edit
     *       older than the last full save loses even when it names a key that
     *       has never existed, which is the case a per-key clock cannot see.
     *   <li>The {@code WHERE} on the conflict is the per-key tiebreak, and it is
     *       {@code <} rather than {@code <=} so that an edit arriving with
     *       exactly the stored timestamp leaves the stored value alone.
     * </ul>
     */
    private static final String CLAIM_CLOCK =
            """
            WITH edit AS (SELECT LEAST(CAST(? AS timestamptz), now()) AS at)
            INSERT INTO player.sync_clock (profile_id, aggregate, entry_key, updated_at)
            SELECT ?, ?, ?, edit.at
              FROM edit
             WHERE NOT EXISTS (
                   SELECT 1
                     FROM player.sync_watermark mark
                    WHERE mark.profile_id = ?
                      AND mark.aggregate = ?
                      AND mark.replaced_at >= edit.at)
            ON CONFLICT (profile_id, aggregate, entry_key)
            DO UPDATE SET updated_at = EXCLUDED.updated_at
                    WHERE sync_clock.updated_at < EXCLUDED.updated_at
            """;

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

        settleWholeAggregate(inventory.profile(), INVENTORY);
    }

    @Override
    @Transactional(readOnly = true)
    public Roster rosterOf(ProfileId profile) {
        // An entity is several rows since V13, so this groups. Ordered by slug
        // then state so the map and each set come back in a stable order: a
        // solve key hashes this, and a key that depends on row order would miss
        // the cache for reasons nobody could see.
        Map<EntityId, Set<String>> states = new LinkedHashMap<>();
        jdbc.query(
                """
                SELECT entity_slug, current_state
                  FROM player.roster_entry
                 WHERE profile_id = ?
                 ORDER BY entity_slug, current_state
                """,
                rs -> {
                    states.computeIfAbsent(EntityId.of(rs.getString("entity_slug")), e -> new LinkedHashSet<>())
                            .add(rs.getString("current_state"));
                },
                profile.value());
        return new Roster(profile, states);
    }

    @Override
    @Transactional
    public void saveRoster(Roster roster) {
        jdbc.update("DELETE FROM player.roster_entry WHERE profile_id = ?", roster.profile().value());

        List<Object[]> rows = roster.currentStates().entrySet().stream()
                .flatMap(e -> e.getValue().stream()
                        .map(state -> new Object[] {roster.profile().value(), e.getKey().value(), state}))
                .toList();

        jdbc.batchUpdate(
                """
                INSERT INTO player.roster_entry (profile_id, entity_slug, current_state)
                VALUES (?, ?, ?)
                """,
                rows);

        settleWholeAggregate(roster.profile(), ROSTER);
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

    // ── Offline sync ────────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>One round trip per key rather than one set-based statement for the
     * batch, and the reason is that the value write is conditional on the clock
     * write's outcome: a key whose edit lost must not be touched at all. A
     * single statement could express that with a CTE, and would express it
     * unreadably. The volume this route sees is a sync batch — a handful of keys
     * that changed while a device was away, a few hundred at the very worst on a
     * first upload — not a hot path, and every statement here is a primary-key
     * lookup inside one transaction.
     */
    @Override
    @Transactional
    public MergeOutcome<ItemId> mergeInventory(ProfileId profile, Collection<InventoryEdit> edits) {
        List<ItemId> applied = new ArrayList<>();
        List<ItemId> rejected = new ArrayList<>();

        for (InventoryEdit edit : edits) {
            if (!claim(profile, INVENTORY, edit.item().value(), edit.editedAt())) {
                rejected.add(edit.item());
                continue;
            }

            if (edit.isRemoval()) {
                // The value goes and the clock stays. That asymmetry is the
                // whole tombstone: a device that has been offline since before
                // this removal now has something to lose against.
                jdbc.update(
                        "DELETE FROM player.inventory_item WHERE profile_id = ? AND item_slug = ?",
                        profile.value(),
                        edit.item().value());
            } else {
                jdbc.update(
                        """
                        INSERT INTO player.inventory_item (profile_id, item_slug, quantity)
                        VALUES (?, ?, ?)
                        ON CONFLICT (profile_id, item_slug) DO UPDATE SET quantity = EXCLUDED.quantity
                        """,
                        profile.value(),
                        edit.item().value(),
                        edit.quantity());
            }
            applied.add(edit.item());
        }

        return new MergeOutcome<>(applied, rejected);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MergeOutcome<EntityId> mergeRoster(ProfileId profile, Collection<RosterEdit> edits) {
        List<EntityId> applied = new ArrayList<>();
        List<EntityId> rejected = new ArrayList<>();

        for (RosterEdit edit : edits) {
            if (!claim(profile, ROSTER, edit.entity().value(), edit.editedAt())) {
                rejected.add(edit.entity());
                continue;
            }

            // The entity's whole set moves together, so both branches start by
            // clearing it. An UPSERT per state would leave behind any state the
            // edit dropped, which would make "I am no longer at this state" —
            // a correction, or a state the game took back — impossible to say.
            jdbc.update(
                    "DELETE FROM player.roster_entry WHERE profile_id = ? AND entity_slug = ?",
                    profile.value(),
                    edit.entity().value());

            if (!edit.isRemoval()) {
                jdbc.batchUpdate(
                        """
                        INSERT INTO player.roster_entry (profile_id, entity_slug, current_state)
                        VALUES (?, ?, ?)
                        """,
                        edit.states().stream()
                                .map(state -> new Object[] {profile.value(), edit.entity().value(), state})
                                .toList());
            }
            applied.add(edit.entity());
        }

        return new MergeOutcome<>(applied, rejected);
    }

    /** True when this edit beat both clocks and now owns the key. */
    private boolean claim(ProfileId profile, String aggregate, String key, Instant editedAt) {
        return jdbc.update(
                        CLAIM_CLOCK,
                        OffsetDateTime.ofInstant(editedAt, ZoneOffset.UTC),
                        profile.value(),
                        aggregate,
                        key,
                        profile.value(),
                        aggregate)
                > 0;
    }

    /**
     * Record that this whole aggregate was stated in full, just now.
     *
     * <p>One row, not one per key, and that is the point: a save is a claim
     * about every key including the ones it left out and the ones the player has
     * never owned. Stamping only the keys it wrote would leave a patch free to
     * introduce a key the save had implicitly denied, so a stale edit would lose
     * for an item the save mentioned and win for one it did not — a rule with no
     * explanation.
     *
     * <p>Skipping this call altogether is the subtle way to get sync wrong: the
     * save would land, a patch carrying an hour-old edit would arrive with
     * nothing to lose against, and the older value would silently win.
     */
    private void settleWholeAggregate(ProfileId profile, String aggregate) {
        jdbc.update(
                """
                INSERT INTO player.sync_watermark (profile_id, aggregate, replaced_at)
                VALUES (?, ?, now())
                ON CONFLICT (profile_id, aggregate) DO UPDATE SET replaced_at = now()
                """,
                profile.value(),
                aggregate);
    }
}
