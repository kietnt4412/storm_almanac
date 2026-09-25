-- A step can now say what the game calls the states it joins, and which heading
-- on the game's screens its track sits under.
--
-- FOUND BY: the maintainer's D5 rehearsal of 2026-09-25 (S2 and its follow-up in
-- TRACKER.md). The roster offered "promote-5" where the game shows "Elite ★3",
-- and one construct's thirteen tracks as a flat list where the game groups them
-- under Basic Skill, Special Skill, Evolution Effect and Common Effect and tags
-- each skill with its orb. A player finds their place by those words and that
-- grouping, and most do not read a skill's name at all.
--
-- These ARE facts, unlike progress_kind's names (V14): they are text read off
-- the game's screens. So they live on the step, and the step's row in
-- gamedata.fact_provenance covers them the way it covers the step's costs
-- (ADR 0032). Every column is nullable and NULL means the game gave the step no
-- words, which is every step of every version published before this migration;
-- nothing stored moves and no version becomes unloadable.
ALTER TABLE gamedata.upgrade
    ADD COLUMN from_name TEXT,
    ADD COLUMN to_name   TEXT,
    ADD COLUMN section   TEXT,
    ADD COLUMN tag       TEXT,
    ADD CONSTRAINT upgrade_names_not_blank CHECK (
        (from_name IS NULL OR length(btrim(from_name)) > 0)
        AND (to_name IS NULL OR length(btrim(to_name)) > 0)
        AND (section IS NULL OR length(btrim(section)) > 0)
        AND (tag IS NULL OR length(btrim(tag)) > 0));

-- The order the headings are shown in. Not a fact: each step names its own
-- heading, which is the reading; this is only where each goes, and it may hold
-- the bundle's own word for a group the game leaves untitled ("Growth" for level,
-- rank and evolution). No foreign key from upgrade.section to here, for the same
-- reason progress_kind has none: GameDataBundle refuses a heading nothing sits
-- under and a heading that is not declared, which is the check that catches the
-- typo, and a composite key on free text would only restate it.
CREATE TABLE gamedata.section (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id BIGINT NOT NULL REFERENCES gamedata.game_data_version (id) ON DELETE CASCADE,
    position   INT    NOT NULL,
    name       TEXT   NOT NULL,

    CONSTRAINT section_position_unique UNIQUE (version_id, position),
    CONSTRAINT section_name_unique UNIQUE (version_id, name),
    CONSTRAINT section_position_not_negative CHECK (position >= 0),
    CONSTRAINT section_name_not_blank CHECK (length(btrim(name)) > 0)
);
