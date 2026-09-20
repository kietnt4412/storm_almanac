-- A reward can now say what the player has to be able to do to collect it: a
-- named measure the game scores them on, and the score this grant stands
-- behind.
--
-- FOUND BY: the first first-hand Punishing: Gray Raven bundle (N32 (5) in
-- TRACKER.md). The weekly Phantom Pain Cage pays Phantom Pain Scars in nine
-- tiers, 4 to 10 each, behind scores from 30 000 to 1 100 000. Without this the
-- file had three choices and no good one: leave the mode out, so a plan for
-- Evolve to SS charges 500 Scars and names no way of earning one; write the
-- 56-Scar maximum, which promises every reader the top tier; or write the
-- bottom tier, which is a different reader's week. All three are the same
-- mistake — putting a fact about a player into the game's data.
--
-- The measure is NOT an item and gets no table of its own. Nobody holds a
-- stack of it, nothing converts into it, and spending it is not a thing that
-- happens; the planner only ever compares it with a number the reader supplies.
-- An opaque label, like upgrade_progress.kind and like a state.
--
-- Nullable, and NULL is the honest value: every reward published before this
-- was read without anything to say who could collect it, so it is collectable
-- by everybody, which is exactly what it claimed when it was published. A
-- version is immutable and has to stay readable.
ALTER TABLE gamedata.reward
    ADD COLUMN requires_measure  TEXT,
    ADD COLUMN requires_at_least INT;

-- Both or neither. Half a requirement is a row somebody meant to finish, and a
-- measure with no bar would silently read back as "collectable by everybody" —
-- the one wrong answer this migration exists to prevent.
ALTER TABLE gamedata.reward
    ADD CONSTRAINT reward_requirement_whole CHECK (
        (requires_measure IS NULL AND requires_at_least IS NULL)
        OR (requires_measure IS NOT NULL AND requires_at_least IS NOT NULL)
    ),
    ADD CONSTRAINT reward_requirement_measure_not_blank CHECK (
        requires_measure IS NULL OR length(btrim(requires_measure)) > 0
    ),
    ADD CONSTRAINT reward_requirement_bar_positive CHECK (
        requires_at_least IS NULL OR requires_at_least >= 1
    );

COMMENT ON COLUMN gamedata.reward.requires_measure IS
    'An opaque label for what the game scores the player on; NULL for a grant everybody collects.';
