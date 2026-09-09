package io.stormalmanac.player;

import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.ProfileId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** The player module's only door. Unglamorous and load-bearing. */
public interface PlayerStateRepository {

    List<PlayerProfile> profilesOf(AccountId account);

    Optional<PlayerProfile> findProfile(ProfileId id);

    /**
     * Create a profile, or rename an existing one.
     *
     * <p>The caller owns the {@link ProfileId}, as it does for every other
     * aggregate here. Nothing in this module invents identity: a profile is
     * created by whoever holds the account that will own it.
     */
    void saveProfile(PlayerProfile profile);

    Inventory inventoryOf(ProfileId profile);

    Roster rosterOf(ProfileId profile);

    Goals goalsOf(ProfileId profile);

    /**
     * Replace the whole inventory, and record that it was stated in full just
     * now.
     *
     * <p>The second half is not decoration. A save that left the merge clock
     * alone would be beaten afterwards by a patch carrying an older edit — the
     * browser types a fresh inventory, the phone syncs an hour-old one, and the
     * phone wins. Replacing an aggregate is a statement about every key at once,
     * including keys it left out and keys the player has never owned, so it is
     * recorded once for the aggregate rather than key by key.
     */
    void saveInventory(Inventory inventory);

    /** Replace the whole roster, on the same terms as {@link #saveInventory}. */
    void saveRoster(Roster roster);

    void saveGoals(Goals goals);

    /**
     * Merge edits into the inventory one key at a time, the newest edit per key
     * winning.
     *
     * <p>This is the operation offline sync needs and {@link #saveInventory} is
     * not: it says something about the keys it names and nothing at all about
     * the others, so a device that has been away does not delete what it never
     * saw. Three rules decide it, and all three are load-bearing:
     *
     * <ol>
     *   <li><b>Strictly newer wins.</b> An edit is applied only if it is later
     *       than the stored one for that key; a tie leaves the stored value
     *       alone. Ties happen — two devices saving within the same second is
     *       not exotic — and settling them towards what is already there makes
     *       the outcome a property of the data rather than of which request the
     *       server happened to schedule first.
     *   <li><b>No edit may come from the future.</b> {@code editedAt} is clamped
     *       to the server's clock. Without the clamp a device whose clock is a
     *       year fast pins every key it touches against every later edit from
     *       anywhere, permanently, and nothing in the system could correct it.
     *   <li><b>A removal is remembered.</b> Clearing a key deletes its value and
     *       keeps its clock, so an older device re-adding it loses rather than
     *       resurrecting it. See V6 for why that forces a table and not a
     *       column.
     * </ol>
     *
     * <p>A fourth rule comes from the other side: an edit older than the last
     * {@link #saveInventory} loses whatever key it names, even one that has
     * never existed. A per-key clock has no row on which to record "and the
     * player has none of anything else", and without that the merge would reject
     * a stale edit for a key the save mentioned and accept it for one the save
     * left out.
     *
     * @return which keys were taken and which lost, so the caller can tell the
     *         client which of its values are not the server's
     */
    MergeOutcome<ItemId> mergeInventory(ProfileId profile, Collection<InventoryEdit> edits);

    /** Merge roster edits one key at a time, on the same three rules. */
    MergeOutcome<EntityId> mergeRoster(ProfileId profile, Collection<RosterEdit> edits);
}
