-- N34 — a roster entry holds the states an entity has reached, not one state.
--
-- V5 gave roster_entry the primary key (profile_id, entity_slug) and a single
-- current_state column. That shape says an entity is somewhere, once. It is not
-- what either published game looks like: a PGR construct has a level, a rank,
-- an evolution and six skills, and the game ties none of them to each other — a
-- player may stand at Lv 80 and rank 0, and does.
--
-- WHAT THE OLD SHAPE COST.
--   A reader recorded on one track was charged the whole of every other. ADR
--   0026 recovered the tracks sitting *behind* a recorded state, through the
--   gates that state implies: standing past a gated step proves the gate was
--   met, so the planner may credit it. That is inference, and it only ever
--   reaches backwards. The tracks *beside* a recorded state are not implied by
--   anything and never can be — only the player knows them — so the schema has
--   to be able to hold them. This is that change, and it is the half ADR 0026
--   said it was not doing.
--
-- WHY THE PRIMARY KEY AND NOT AN ARRAY COLUMN.
--   current_state TEXT[] would be one migration statement and no new rows. It
--   loses the two things this table already has for free: the not-blank CHECK
--   applies per state rather than to a formatted blob, and a state is
--   queryable — "who is at promote-6" stays a WHERE rather than an unnest. An
--   array would also make the empty array and the absent row two spellings of
--   the same thing, which is exactly the ambiguity V5's decision 4 spent a
--   paragraph removing for the inventory.
--
--   The cost is that an entity is now several rows, so reading a roster groups
--   and writing one deletes before it inserts. Both are already how this table
--   is used: saveRoster has always deleted the profile's rows and rewritten
--   them, because a PUT is a whole-aggregate statement.
--
-- WHY THE SYNC CLOCK IS NOT TOUCHED.
--   V6 keys the clock by (profile_id, aggregate, entry_key) and entry_key is
--   the entity slug. It stays that way. The merge unit is the entity and not
--   the state: an edit says where an entity stands, in full, as of its
--   timestamp, and the whole set moves together. Per-state clocks would be the
--   finer grain and the wrong one — two devices that each advanced the same
--   construct on a different track would both win, and the server would
--   assemble a roster neither device has ever held out of two half-truths.
--
-- MIGRATING WHAT IS THERE.
--   Every existing row becomes a one-element set, which is exactly what it
--   meant. Nothing is lost and nothing is invented: a player who had recorded
--   one state has told us one state, and the planner keeps inferring the rest
--   backwards through gates as it did before. The primary key is widened in
--   place, so no data moves.

ALTER TABLE player.roster_entry
    DROP CONSTRAINT roster_entry_pkey;

ALTER TABLE player.roster_entry
    ADD PRIMARY KEY (profile_id, entity_slug, current_state);

-- The read path groups by entity, and the merge path deletes a whole entity's
-- rows before inserting the new set. Both want the profile's rows for one slug
-- together; the primary key's leading columns already serve that, so this is a
-- note rather than an index: nothing here needs one that the key does not give.
COMMENT ON COLUMN player.roster_entry.current_state IS
    'One state this entity has reached. An entity has a row per state, and at '
    'least one — an owned entity is always somewhere, and absence of every row '
    'is how "not owned" is said.';
