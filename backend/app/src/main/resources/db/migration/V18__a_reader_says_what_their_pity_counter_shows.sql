-- C1 — the pull planner: a reader's pity counter, stored.
--
-- Phase 5 left the engines answering from a PityState nobody stored, which
-- TRACKER.md put as "the gap is a schema and a screen rather than an engine".
-- This is the schema.
--
-- ONE ROW PER COUNTER, KEYED BY WHAT THE BANNER SAYS IT IS CARRIED BY.
--   scope_key is derived from the banner in the gacha module — "global",
--   "type:<banner type>" or "banner:<banner id>" — and is opaque here, exactly as
--   a roster state is. The player schema does not know what a banner is, and a
--   pity counter outlives every banner that shares it: Punishing: Gray Raven
--   carries it from one pool to the next of the same type. So it is not a
--   foreign key into gamedata either (V5's second decision).
--
-- NO SYNC CLOCK.
--   V6's per-key clock exists so an offline edit to a quantity does not delete
--   one made elsewhere. A pity counter is not edited, it is re-reported: the
--   number is on the reader's screen, the latest report of it is the truth, and
--   last write wins is the right rule rather than a shortcut. updated_at is kept
--   so that a screen can say when it was last reported.
--
-- ABSENT MEANS FRESH, and a fresh counter is stored like any other once reported:
--   0 and 0 is a real answer ("I just hit"), not a row to delete.

CREATE TABLE player.pity (
    profile_id         TEXT NOT NULL REFERENCES player.profile (id) ON DELETE CASCADE,
    scope_key          TEXT NOT NULL,
    pulls_since_hit    INT  NOT NULL,
    consecutive_losses INT  NOT NULL,
    updated_at         TIMESTAMPTZ NOT NULL,

    PRIMARY KEY (profile_id, scope_key),
    CONSTRAINT pity_scope_key_not_blank CHECK (length(btrim(scope_key)) > 0),
    CONSTRAINT pity_pulls_non_negative CHECK (pulls_since_hit >= 0),
    CONSTRAINT pity_losses_non_negative CHECK (consecutive_losses >= 0)
);
