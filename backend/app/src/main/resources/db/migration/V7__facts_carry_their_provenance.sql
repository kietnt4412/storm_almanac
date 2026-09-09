-- Phase 4 / N27 — where each fact came from, stored beside the fact.
--
-- ADR 0015 says the shipped product carries only game data this project sourced
-- itself. That decision has a hole in it that prose cannot close: a number
-- somebody read off a game screen and a number somebody copied out of an
-- aggregator are byte-identical once typed. Nothing downstream can tell them
-- apart, so "self-sourced" is unfalsifiable unless the sourcing travels with the
-- data.
--
-- game_data_version.attribution is not that. It is one sentence per version,
-- written for a reader, and it is a credit line rather than a record — it cannot
-- say that a banner's rates came from the publisher's own rules screen while the
-- stage next to it came from somebody counting runs. These two tables can.
--
-- WHY THE ROWS ARE MATERIALISED RATHER THAN DEFAULTED.
--    A bundle names one default provenance and overrides it per fact, because
--    the realistic bundle is one sitting, one screen, one reader, and asking a
--    person to type the same string 2 700 times buys no truth. Storage does not
--    inherit that shortcut: every declared fact gets its own row here. The
--    default is an authoring convenience, and a database that stored it would
--    make "where did this fact come from" a question you need the bundle to
--    answer. Game data is small (V2, decision 1) and this costs one narrow row
--    per fact.
--
-- WHY THE POLICY IS NOT IN THIS FILE.
--    origin is stored as text and constrained to the known set, and nothing here
--    says which origins count as first-hand. That decision lives in
--    Provenance.Origin, in one place, where adding a member forces somebody to
--    answer the question. A CHECK encoding it as well would be a second copy
--    that can disagree with the first.

-- ── The sourcing records ────────────────────────────────────────────────────
-- Version-scoped like everything else: a provenance is a claim about one
-- snapshot read on one day, and the same slug in the next patch is a new
-- reading, not the old one still being true.

CREATE TABLE gamedata.provenance (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id  BIGINT NOT NULL REFERENCES gamedata.game_data_version (id) ON DELETE CASCADE,
    -- The id a bundle's facts point at, e.g. 'stage-screens-3.5'.
    slug        TEXT   NOT NULL,
    origin      TEXT   NOT NULL,
    -- How somebody could go and check it: the screen, the disclosure, the run
    -- count. NOT NULL and non-blank, because an origin with no detail is an
    -- assertion rather than a record.
    detail      TEXT   NOT NULL,
    -- The day it was read. A date and not a timestamp: nobody knows the minute,
    -- and a precision the data does not have is a lie the schema tells for you.
    observed_on DATE   NOT NULL,

    CONSTRAINT provenance_slug_unique UNIQUE (version_id, slug),
    -- What every child in this schema needs to reference a parent without being
    -- able to cross a version boundary. See V2, decision 2.
    CONSTRAINT provenance_id_scoped UNIQUE (version_id, id),
    CONSTRAINT provenance_slug_not_blank CHECK (length(btrim(slug)) > 0),
    CONSTRAINT provenance_detail_not_blank CHECK (length(btrim(detail)) > 0),
    CONSTRAINT provenance_origin_known CHECK (origin IN (
        'OBSERVED_IN_GAME',
        'SAMPLED_IN_GAME',
        'PUBLISHER_DISCLOSURE',
        'AUTHORED_FIXTURE',
        'THIRD_PARTY',
        'UNRECORDED'
    ))
);

-- ── One row per declared fact ───────────────────────────────────────────────

CREATE TABLE gamedata.fact_provenance (
    version_id    BIGINT NOT NULL,
    -- 'kind:slug' — 'stage:1-1', 'item:silver-ore', 'entity:sotheby'. The kind
    -- prefix is load-bearing: slugs are unique per table and deliberately not
    -- across them, so a bare slug is not an identifier and this key would
    -- silently merge a stage with a craft that shares its name. See FactRef.
    fact_ref      TEXT   NOT NULL,
    provenance_id BIGINT NOT NULL,

    -- A fact has exactly one provenance. Two would mean the bundle claimed a
    -- number came from two places, which is not a richer record — it is an
    -- unresolved contradiction, and the place to fail is here.
    PRIMARY KEY (version_id, fact_ref),
    CONSTRAINT fact_provenance_version_fk
        FOREIGN KEY (version_id) REFERENCES gamedata.game_data_version (id) ON DELETE CASCADE,
    CONSTRAINT fact_provenance_provenance_fk
        FOREIGN KEY (version_id, provenance_id)
        REFERENCES gamedata.provenance (version_id, id) ON DELETE CASCADE,
    CONSTRAINT fact_provenance_ref_not_blank CHECK (length(btrim(fact_ref)) > 0),
    CONSTRAINT fact_provenance_ref_is_kind_scoped CHECK (fact_ref LIKE '%:%')
);

-- The question the publish gate asks: "does this draft carry anything that is
-- not ours to publish?" It runs once per approval over one version's rows, so
-- the index is on the version rather than on the origin.
CREATE INDEX fact_provenance_version_idx
    ON gamedata.fact_provenance (version_id);
