-- An upgrade can now say what it needs besides items: a state it is gated on,
-- and an amount of something no inventory holds, such as EXP. A fodder rule can
-- now say which of those amounts it pays.
--
-- FOUND BY: the first first-hand Punishing: Gray Raven bundle (N32 in
-- TRACKER.md). Every Promote step is gated on a character level, and a level is
-- paid in EXP, which the Pods the bundle already carried had nowhere to go. So
-- a plan for her last rank charged 100 000 Cogs and not the 497 000 EXP that
-- the gate behind it costs, and was cheaper than the truth by most of the goal.
--
-- Gates are states of the SAME entity. That is what every gate read so far is:
-- a rank on a level, a passive on an evolution. A gate on another entity (an
-- Awaken tier on twelve equipped skills) is not a cost to the entity gated, and
-- is not expressible here on purpose.
CREATE TABLE gamedata.upgrade_requirement (
    upgrade_id BIGINT NOT NULL,
    version_id BIGINT NOT NULL,
    state      TEXT   NOT NULL,

    PRIMARY KEY (upgrade_id, state),
    CONSTRAINT upgrade_requirement_upgrade_fk FOREIGN KEY (version_id, upgrade_id)
        REFERENCES gamedata.upgrade (version_id, id) ON DELETE CASCADE,
    CONSTRAINT upgrade_requirement_state_not_blank CHECK (length(btrim(state)) > 0)
);

-- Keyed by kind rather than by ordinal: an upgrade that needs the same kind of
-- progress twice needs it once, summed.
CREATE TABLE gamedata.upgrade_progress (
    upgrade_id BIGINT NOT NULL,
    version_id BIGINT NOT NULL,
    kind       TEXT   NOT NULL,
    quantity   INT    NOT NULL,

    PRIMARY KEY (upgrade_id, kind),
    CONSTRAINT upgrade_progress_upgrade_fk FOREIGN KEY (version_id, upgrade_id)
        REFERENCES gamedata.upgrade (version_id, id) ON DELETE CASCADE,
    CONSTRAINT upgrade_progress_kind_not_blank CHECK (length(btrim(kind)) > 0),
    CONSTRAINT upgrade_progress_quantity_positive CHECK (quantity >= 1)
);

-- Nullable, and this is the one place NULL is the honest value: every fodder
-- row published before this migration was read without anything to name what
-- it feeds, so it feeds nothing, which is exactly what it did when it was
-- published. A version is immutable and has to stay readable (see "a published
-- version that stopped being readable" in the tracker archive); a NOT NULL
-- with an invented default would rewrite what those rows claim.
ALTER TABLE gamedata.fodder
    ADD COLUMN progress TEXT;

ALTER TABLE gamedata.fodder
    ADD CONSTRAINT fodder_progress_not_blank CHECK (progress IS NULL OR length(btrim(progress)) > 0);

COMMENT ON COLUMN gamedata.fodder.progress IS
    'The upgrade_progress.kind this rule pays; NULL for a rule published before it could say, which feeds nothing.';
