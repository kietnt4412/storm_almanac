-- A bundle's own keys can now carry a word a person reads: a scored measure, an
-- item category, an entity kind.
--
-- FOUND BY: D5's second rehearsal (S8 in TRACKER.md, 2026-09-26). The plan page
-- asked "how far do you get" over the slug phantom-pain-cage-score, and the
-- inventory put one heading per EXP Pod size, CHARACTER-EXP-POD-L among them,
-- because an item category was a key a fodder rule matched on and nothing else.
--
-- NOT a fact, and no row in gamedata.fact_provenance, for the reason V14 gives
-- progress kind names (ADR 0033). A word renames a key and changes no number.
--
-- One table for three subjects rather than three tables, because the three are
-- one idea — a key and what to show instead — and a fourth would otherwise be a
-- fourth migration. The subject is TEXT with a CHECK, not an enum type, so that
-- adding one is a constraint change and not an ALTER TYPE.
--
-- Optional, and a loosening: a key with no row renders as itself, which is what
-- every version published before this migration does, and no published version
-- becomes unloadable.
CREATE TABLE gamedata.word (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id   BIGINT NOT NULL REFERENCES gamedata.game_data_version (id) ON DELETE CASCADE,
    subject      TEXT   NOT NULL,
    -- Matched against reward.requires_measure, item.category and entity.kind,
    -- all TEXT. No foreign key, as with progress_kind.kind: none of those is a
    -- table of keys. GameDataBundle refuses a word for a key nothing uses.
    key          TEXT   NOT NULL,
    display_name TEXT   NOT NULL,

    CONSTRAINT word_unique UNIQUE (version_id, subject, key),
    CONSTRAINT word_subject_known CHECK (subject IN ('measure', 'category', 'entity-kind')),
    CONSTRAINT word_key_not_blank CHECK (length(btrim(key)) > 0),
    CONSTRAINT word_display_name_not_blank CHECK (length(btrim(display_name)) > 0)
);
