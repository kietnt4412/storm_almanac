-- C3.1 — a saved plan: the last plan a profile was shown, kept.
--
-- Until now a plan was computed, rendered and forgotten. That was harmless while
-- the only question was "what should I farm", and it is not harmless for C3's:
-- "what did the new sequence change for me" needs to know which sequence the
-- reader last planned on, and nothing recorded it. ADR 0037.
--
-- ONE ROW PER PROFILE, REPLACED BY EVERY PLAN.
--   The last plan run is the one saved — the maintainer's choice on 2026-09-29,
--   over a save button. Keeping several is later work and would be a second
--   table, not a second key on this one.
--
-- THE PLAN IS STORED AS THE READER SAW IT, NOT RECOMPUTED.
--   Re-solving the same request does not give the same plan back: the inventory
--   under it moves, and the solver may land on a different, equally good answer
--   inside its budget (3 880 against 3 877 on the same data, ADR 0010). So what
--   is kept is the response, in its wire shape, as JSON the player module does
--   not read. The api module owns that shape, and the player module is not
--   allowed to know what a plan is — the same reason player.pity's scope_key is
--   opaque here.
--
-- THE REQUEST IS KEPT BESIDE IT, reach included.
--   Energy a day, horizon, objective and how far the reader gets: what C3.2
--   re-solves with. ADR 0022 said nothing stores reach; this stores it as part
--   of a request that was made, which is not the same as making it player state
--   with a sync clock — ADR 0037 says where the line is.
--
-- game_version IS A COLUMN, NOT ONLY A FIELD IN THE JSON.
--   It is the anchor C3.2 compares the latest sequence against, and it must
--   survive the JSON becoming unreadable to a later wire shape. Not a foreign
--   key into gamedata — V5's second decision.
--
-- NO SYNC CLOCK. A plan is not edited, it is replaced, and the latest one run
-- anywhere is the one the reader last saw.

CREATE TABLE player.saved_plan (
    profile_id   TEXT        PRIMARY KEY REFERENCES player.profile (id) ON DELETE CASCADE,
    -- The sequence of the published version the plan was solved against.
    game_version BIGINT      NOT NULL,
    request      JSONB       NOT NULL,
    plan         JSONB       NOT NULL,
    saved_at     TIMESTAMPTZ NOT NULL,

    CONSTRAINT saved_plan_version_positive CHECK (game_version >= 0)
);
