-- N23 — offline sync: the per-key merge Phase 3's scope named and did not build.
--
-- V5 left a note where this belongs: "No updated_at here yet ... a timestamp
-- column nobody reads is a feature that only looks implemented. It arrives with
-- the merge, in the same change." This is that change; the route and the merge
-- land with it.
--
-- WHY A SEPARATE TABLE RATHER THAN A COLUMN.
--   The obvious shape is updated_at on inventory_item and roster_entry. It is
--   wrong for the one case sync exists to handle, and wrong silently.
--
--   V5's fourth decision is that absent means zero: clearing an item deletes the
--   row. A timestamp living on that row is deleted with it, so after a delete
--   there is nothing left to compare against — and a device that has been
--   offline since before the delete then re-adds the item and wins, because the
--   merge has no evidence the delete ever happened. The item comes back. Nobody
--   is told. That is precisely the class of bug this project refuses to ship.
--
--   A clock that outlives its value is a tombstone, and a tombstone cannot be a
--   column on the row it outlives. So the clock is its own table, V5's decision 4
--   stands untouched, and the value tables are exactly as they were.
--
-- WHY TWO TABLES AND NOT ONE.
--   A per-key clock answers "is this edit newer than what I hold for this key",
--   and it cannot answer anything about a key it has never held. That gap is
--   reachable: a PUT says "this is the whole inventory", which is a statement
--   about every slug in the game including the ones the player has none of — and
--   no per-key table can record that, because there is no row to record it on
--   and no way to enumerate the keys it would need.
--
--   Without the watermark below, a stale patch loses for a key the PUT mentioned
--   and wins for one it did not, which is a rule nobody could explain. The
--   watermark is the whole-aggregate half of the same clock: one row saying when
--   this inventory was last stated in full, and any edit older than that loses
--   whatever key it names.
--
-- WHAT IT COSTS, STATED.
--   A key that is deleted keeps its clock row forever. For an inventory of a few
--   hundred slugs that is nothing; there is no reaper and there should not be one
--   until a profile is observed carrying enough dead keys to matter, because a
--   tombstone swept too early is the resurrection bug back again with an extra
--   step. Nothing here is read on the read path — inventoryOf and rosterOf do not
--   join to either table — so the cost is storage, not latency.
--
-- WHY ONE TABLE FOR BOTH AGGREGATES.
--   A merge clock is per (profile, aggregate, key) and does not care what the key
--   means; two tables differing only in a column name would be the same code
--   twice. Goals are deliberately absent from the CHECKs below: they are an
--   ordered list whose order is itself the thing being edited, and there is no
--   per-key answer to two devices reordering it. Goals stay a whole-aggregate PUT
--   and say so.

CREATE TABLE player.sync_clock (
    profile_id TEXT NOT NULL REFERENCES player.profile (id) ON DELETE CASCADE,
    -- Which map this key belongs to. Free-form would let a typo create a silent
    -- second namespace that merges against nothing, so it is checked.
    aggregate  TEXT NOT NULL,
    -- item_slug or entity_slug. Deliberately not a foreign key to the value
    -- table: the whole point is that this row survives the value row's deletion.
    entry_key  TEXT NOT NULL,
    -- When the edit that produced the current value was made, as the client
    -- reported it and never later than the server saw it. The clamp is applied
    -- on write, so a device with a clock set to next year cannot pin a key
    -- against every future edit — see JdbcPlayerStateRepository.
    updated_at TIMESTAMPTZ NOT NULL,

    PRIMARY KEY (profile_id, aggregate, entry_key),
    CONSTRAINT sync_clock_aggregate_known CHECK (aggregate IN ('inventory', 'roster')),
    CONSTRAINT sync_clock_entry_key_not_blank CHECK (length(btrim(entry_key)) > 0)
);

-- When this aggregate was last stated in full by a PUT. One row per profile per
-- aggregate, and an edit older than it loses whatever key it names — including a
-- key that has never existed, which is the case the per-key table cannot see.
CREATE TABLE player.sync_watermark (
    profile_id  TEXT NOT NULL REFERENCES player.profile (id) ON DELETE CASCADE,
    aggregate   TEXT NOT NULL,
    replaced_at TIMESTAMPTZ NOT NULL,

    PRIMARY KEY (profile_id, aggregate),
    CONSTRAINT sync_watermark_aggregate_known CHECK (aggregate IN ('inventory', 'roster'))
);

-- Everything written before this migration is settled as of now. It is the only
-- honest answer available: those rows were saved by a PUT, which means "this is
-- the whole aggregate as of when I said it", and no earlier time is recorded
-- anywhere. The effect is that a patch carrying an edit made before this
-- deployment loses, which is the safe direction to be wrong in.
--
-- A watermark alone is enough to say that; the per-key table starts empty and
-- fills as the first patches arrive.
INSERT INTO player.sync_watermark (profile_id, aggregate, replaced_at)
SELECT id, 'inventory', now() FROM player.profile;

INSERT INTO player.sync_watermark (profile_id, aggregate, replaced_at)
SELECT id, 'roster', now() FROM player.profile;
