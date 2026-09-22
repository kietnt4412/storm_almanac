-- A progress kind can now carry a name a person reads, so that a demand line
-- for one stops being a slug.
--
-- FOUND BY: the roster work of 2026-09-21 put the first progress line in front
-- of a browser (N37 in TRACKER.md). It rendered "character-exp" on the shortfall
-- table, directly beside a properly named "Cogs". Nothing was broken — there was
-- simply no name anywhere in the model to render, because a progress kind is an
-- opaque label the bundle invents and nobody had ever needed to say it out loud.
--
-- The name is NOT a fact and carries no row in gamedata.fact_provenance. In
-- Punishing: Gray Raven the character Level Up screen, the weapon Enhancement
-- Cost picker and the Memory Enhancement Cost picker all label their pool "EXP",
-- so the game's own words would put three lines reading "EXP" in one plan and
-- leave the reader unable to tell which one is short. "Character EXP" is this
-- project's word for this project's slug, in the same way "character-exp" is,
-- and claiming it was read off a screen would be false. It sits here beside
-- game.energy_unit and game.day_rollover_*, which declare no provenance either.
--
-- Naming is optional and stays optional: a kind with no row renders as its slug,
-- which is what every version published before this migration does. A version is
-- immutable and has to stay readable, so this is a loosening — nothing stored
-- moves, and no existing version becomes unloadable.
CREATE TABLE gamedata.progress_kind (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id   BIGINT NOT NULL REFERENCES gamedata.game_data_version (id) ON DELETE CASCADE,
    -- Matched against upgrade_progress.kind and fodder.progress, both TEXT and
    -- both opaque. No foreign key: neither of those is a table of kinds, and
    -- inventing one would make a kind a thing to declare before it can be used.
    -- GameDataBundle refuses a name for a kind nothing uses, which is the check
    -- that actually catches the typo.
    kind         TEXT   NOT NULL,
    display_name TEXT   NOT NULL,

    CONSTRAINT progress_kind_unique UNIQUE (version_id, kind),
    CONSTRAINT progress_kind_not_blank CHECK (length(btrim(kind)) > 0),
    CONSTRAINT progress_kind_display_name_not_blank CHECK (length(btrim(display_name)) > 0)
);
