-- Phase 1 — the canonical gamedata schema.
--
-- Three decisions shape everything below. They are worth stating once here
-- rather than re-deriving them from the DDL.
--
-- 1. A VERSION IS A FULL SNAPSHOT, NOT A DELTA.
--    Every row of game data belongs to exactly one game_data_version. Publishing
--    a patch inserts a complete new set of rows; nothing is ever mutated in
--    place. This is what lets an old plan stay correct after a patch lands, and
--    it makes a patch diff an ordinary set comparison between two version ids
--    rather than a bespoke history walk. Game data is small — thousands of rows
--    per version — so the storage this costs is not worth optimising away.
--
-- 2. A REFERENCE CAN NEVER CROSS A VERSION BOUNDARY.
--    Every versioned table carries version_id and a UNIQUE (version_id, id), and
--    every child references its parent through a composite (version_id, ...)
--    foreign key. A v2 stage therefore *cannot* be linked to a v1 item: the
--    database refuses it. The alternative — a plain foreign key on the surrogate
--    key alone — leaves the whole diff feature one ingest bug away from quietly
--    lying, and this is the phase whose headline feature is the diff.
--
-- 3. DRAFT AND PUBLISHED ARE DIFFERENT THINGS.
--    Ingestion is automated; publishing is a deliberate human approval. A draft
--    version has no published_at and is invisible to "latest". See
--    GameDefinitionRepository: "latest" means latest approved, never latest
--    fetched.
--
-- Column types mirror the domain records exactly; where a record's compact
-- constructor validates something, the same rule is a CHECK here. Guarding an
-- invariant in two places is deliberate: the database is the one an ingest
-- adapter cannot talk its way around.

-- ── Titles ──────────────────────────────────────────────────────────────────
-- Not versioned. A game's identity outlives any patch of its data.

CREATE TABLE gamedata.game (
    id           TEXT PRIMARY KEY,
    display_name TEXT NOT NULL,
    -- What the game calls the stamina it gates farming with: "Activity",
    -- "Serum". Naming it here is what keeps the word out of the planner.
    energy_unit  TEXT NOT NULL,
    CONSTRAINT game_id_not_blank CHECK (length(btrim(id)) > 0)
);

-- ── Versions ────────────────────────────────────────────────────────────────

CREATE TABLE gamedata.game_data_version (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    game_id      TEXT   NOT NULL REFERENCES gamedata.game (id),
    -- Monotonic within a game; the ordering key. Not a timestamp, because two
    -- snapshots fetched in either order must still have one defined sequence.
    sequence     BIGINT NOT NULL,
    -- The upstream patch label, e.g. "1.9". Not unique: hotfixes reuse labels.
    label        TEXT   NOT NULL,
    status       TEXT   NOT NULL,
    -- When a human approved this snapshot, not when it was fetched.
    published_at TIMESTAMPTZ,
    ingested_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Where these numbers came from. NOT NULL because "numbers and text only,
    -- attributed" is a project invariant, and an unattributed row is one nobody
    -- can defend later. See docs/prior-art.md and open question Q3.
    attribution  TEXT   NOT NULL,

    CONSTRAINT game_data_version_sequence_unique UNIQUE (game_id, sequence),
    CONSTRAINT game_data_version_id_scoped UNIQUE (game_id, id),
    CONSTRAINT game_data_version_status_known CHECK (status IN ('DRAFT', 'PUBLISHED')),
    CONSTRAINT game_data_version_sequence_non_negative CHECK (sequence >= 0),
    CONSTRAINT game_data_version_attribution_not_blank CHECK (length(btrim(attribution)) > 0),
    -- Published means approved, and approval has a timestamp; a draft has none.
    -- GameDataVersion requires a non-null publishedAt, so only PUBLISHED rows
    -- have a representation in the domain at all.
    CONSTRAINT game_data_version_published_has_timestamp CHECK (
        (status = 'PUBLISHED' AND published_at IS NOT NULL)
        OR (status = 'DRAFT' AND published_at IS NULL)
    )
);

CREATE INDEX game_data_version_latest_idx
    ON gamedata.game_data_version (game_id, sequence DESC)
    WHERE status = 'PUBLISHED';

-- ── Items ───────────────────────────────────────────────────────────────────
-- Anything that can sit in an inventory: material, currency, or a copy of a
-- character. A copy is an item because Resonance and Memory enhancement consume
-- them, which makes the same thing a resource and a sink at once.

CREATE TABLE gamedata.item (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id   BIGINT NOT NULL REFERENCES gamedata.game_data_version (id) ON DELETE CASCADE,
    -- The upstream slug. Stable across a re-import and safe in a URL.
    slug         TEXT   NOT NULL,
    display_name TEXT   NOT NULL,
    -- Rarity is (label, rank), never an enum: one game counts stars, the other
    -- uses letter grades. Only the ordering of rank is meaningful.
    rarity_label TEXT   NOT NULL,
    rarity_rank  INT    NOT NULL,
    -- Free-form and game-supplied ("insight", "resonance-material"). Groups the
    -- UI and names the fodder classes.
    category     TEXT   NOT NULL,

    CONSTRAINT item_slug_unique UNIQUE (version_id, slug),
    CONSTRAINT item_id_scoped UNIQUE (version_id, id),
    CONSTRAINT item_slug_not_blank CHECK (length(btrim(slug)) > 0)
);

CREATE INDEX item_category_idx ON gamedata.item (version_id, category);

-- ── Entities: characters and equipment ──────────────────────────────────────
-- Equipment is an Entity, not a concept of its own. Upgrade and Goal both key
-- on EntityId, so anything upgradeable that can be a planning goal already has
-- to be one. See docs/adr/0007-equipment-is-an-entity.md.

CREATE TABLE gamedata.entity (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id   BIGINT NOT NULL REFERENCES gamedata.game_data_version (id) ON DELETE CASCADE,
    slug         TEXT   NOT NULL,
    display_name TEXT   NOT NULL,
    -- "character" | "equipment", game-supplied and opaque. Routes and groups the
    -- catalog. ADR 0007: planner, gacha and stats must never read this.
    kind         TEXT   NOT NULL,
    rarity_label TEXT   NOT NULL,
    rarity_rank  INT    NOT NULL,
    -- The game's own axis — Afflatus, Class — kept as a string, and empty for
    -- equipment, which sits on no such axis. Empty rather than null: the field
    -- is always present, its value is sometimes nothing.
    element      TEXT   NOT NULL DEFAULT '',

    CONSTRAINT entity_slug_unique UNIQUE (version_id, slug),
    CONSTRAINT entity_id_scoped UNIQUE (version_id, id),
    CONSTRAINT entity_slug_not_blank CHECK (length(btrim(slug)) > 0),
    CONSTRAINT entity_kind_not_blank CHECK (length(btrim(kind)) > 0)
);

CREATE INDEX entity_kind_idx ON gamedata.entity (version_id, kind);

CREATE TABLE gamedata.entity_tag (
    entity_id  BIGINT NOT NULL,
    version_id BIGINT NOT NULL,
    ordinal    INT    NOT NULL,
    tag        TEXT   NOT NULL,

    PRIMARY KEY (entity_id, ordinal),
    CONSTRAINT entity_tag_entity_fk FOREIGN KEY (version_id, entity_id)
        REFERENCES gamedata.entity (version_id, id) ON DELETE CASCADE,
    CONSTRAINT entity_tag_ordinal_non_negative CHECK (ordinal >= 0)
);

-- ── The catalog axis ────────────────────────────────────────────────────────
-- Stat curves, skills and talents are modelled and ingested now rather than
-- retrofitted, because adding a second data axis to a published schema later is
-- miserable. The planner needs none of this; the funnel needs all of it.

CREATE TABLE gamedata.stat_curve (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id BIGINT NOT NULL,
    entity_id  BIGINT NOT NULL,
    stat       TEXT   NOT NULL,

    CONSTRAINT stat_curve_entity_fk FOREIGN KEY (version_id, entity_id)
        REFERENCES gamedata.entity (version_id, id) ON DELETE CASCADE,
    CONSTRAINT stat_curve_unique UNIQUE (entity_id, stat),
    CONSTRAINT stat_curve_id_scoped UNIQUE (version_id, id)
);

-- Breakpoints plus linear interpolation between them, because that is how the
-- community repositories publish it. Inventing a closed form would mean
-- inventing numbers.
CREATE TABLE gamedata.stat_curve_point (
    curve_id       BIGINT NOT NULL,
    version_id     BIGINT NOT NULL,
    ascension_tier INT    NOT NULL,
    level          INT    NOT NULL,
    value          DOUBLE PRECISION NOT NULL,

    PRIMARY KEY (curve_id, ascension_tier, level),
    CONSTRAINT stat_curve_point_curve_fk FOREIGN KEY (version_id, curve_id)
        REFERENCES gamedata.stat_curve (version_id, id) ON DELETE CASCADE,
    CONSTRAINT stat_curve_point_tier_non_negative CHECK (ascension_tier >= 0),
    CONSTRAINT stat_curve_point_level_positive CHECK (level >= 1),
    -- Rejects NaN as well as the infinities: in Postgres NaN sorts above every
    -- number, so "< Infinity" is false for it. A plain ">= 0" would let NaN in.
    CONSTRAINT stat_curve_point_value_finite CHECK (
        value > '-Infinity'::double precision AND value < 'Infinity'::double precision
    )
);

CREATE TABLE gamedata.skill (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id   BIGINT NOT NULL,
    entity_id    BIGINT NOT NULL,
    slug         TEXT   NOT NULL,
    display_name TEXT   NOT NULL,

    CONSTRAINT skill_entity_fk FOREIGN KEY (version_id, entity_id)
        REFERENCES gamedata.entity (version_id, id) ON DELETE CASCADE,
    CONSTRAINT skill_slug_unique UNIQUE (version_id, slug),
    CONSTRAINT skill_id_scoped UNIQUE (version_id, id),
    CONSTRAINT skill_slug_not_blank CHECK (length(btrim(slug)) > 0)
);

CREATE TABLE gamedata.skill_rank (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id  BIGINT NOT NULL,
    skill_id    BIGINT NOT NULL,
    rank        INT    NOT NULL,
    description TEXT   NOT NULL,

    CONSTRAINT skill_rank_skill_fk FOREIGN KEY (version_id, skill_id)
        REFERENCES gamedata.skill (version_id, id) ON DELETE CASCADE,
    CONSTRAINT skill_rank_unique UNIQUE (skill_id, rank),
    CONSTRAINT skill_rank_id_scoped UNIQUE (version_id, id),
    CONSTRAINT skill_rank_positive CHECK (rank >= 1)
);

-- Named multipliers: {"damage": 1.32}. A table rather than a JSON column,
-- because the patch diff has to report "damage went from 1.32 to 1.28" per key,
-- and diffing opaque blobs would report the whole object as changed.
CREATE TABLE gamedata.skill_rank_value (
    rank_id    BIGINT NOT NULL,
    version_id BIGINT NOT NULL,
    key        TEXT   NOT NULL,
    value      DOUBLE PRECISION NOT NULL,

    PRIMARY KEY (rank_id, key),
    CONSTRAINT skill_rank_value_rank_fk FOREIGN KEY (version_id, rank_id)
        REFERENCES gamedata.skill_rank (version_id, id) ON DELETE CASCADE,
    CONSTRAINT skill_rank_value_finite CHECK (
        value > '-Infinity'::double precision AND value < 'Infinity'::double precision
    )
);

CREATE TABLE gamedata.skill_rank_cost (
    rank_id    BIGINT NOT NULL,
    version_id BIGINT NOT NULL,
    ordinal    INT    NOT NULL,
    item_id    BIGINT NOT NULL,
    quantity   INT    NOT NULL,

    PRIMARY KEY (rank_id, ordinal),
    CONSTRAINT skill_rank_cost_rank_fk FOREIGN KEY (version_id, rank_id)
        REFERENCES gamedata.skill_rank (version_id, id) ON DELETE CASCADE,
    CONSTRAINT skill_rank_cost_item_fk FOREIGN KEY (version_id, item_id)
        REFERENCES gamedata.item (version_id, id),
    CONSTRAINT skill_rank_cost_ordinal_non_negative CHECK (ordinal >= 0),
    CONSTRAINT skill_rank_cost_quantity_non_negative CHECK (quantity >= 0)
);

CREATE TABLE gamedata.talent (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id       BIGINT NOT NULL,
    entity_id        BIGINT NOT NULL,
    slug             TEXT   NOT NULL,
    display_name     TEXT   NOT NULL,
    -- A state on the entity's upgrade graph. Opaque to us by design.
    unlock_condition TEXT   NOT NULL,
    effect           TEXT   NOT NULL,

    CONSTRAINT talent_entity_fk FOREIGN KEY (version_id, entity_id)
        REFERENCES gamedata.entity (version_id, id) ON DELETE CASCADE,
    CONSTRAINT talent_slug_unique UNIQUE (version_id, slug),
    CONSTRAINT talent_id_scoped UNIQUE (version_id, id),
    CONSTRAINT talent_slug_not_blank CHECK (length(btrim(slug)) > 0)
);

-- ── Sources: how an item enters an account ──────────────────────────────────
-- The Source interface is sealed over exactly four shapes, and they are four
-- tables rather than one with a discriminator: their columns have almost
-- nothing in common, and a shared table would be mostly nulls.
--
-- Availability repeats across all four (available_days, opens_at, closes_at).
-- Stages rotate by weekday and event stages expire, which is what makes the
-- optimizer time-indexed rather than one static LP. An empty day array means
-- every day; a null opens_at means "since forever"; a null closes_at means
-- "no announced end".
--
-- Known non-guarantee: Source.id() is unique per table, not across all four.
-- Nothing in the domain requires global uniqueness, and enforcing it would mean
-- a shared parent table existing only to hold a key.

CREATE TABLE gamedata.stage (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id   BIGINT NOT NULL REFERENCES gamedata.game_data_version (id) ON DELETE CASCADE,
    slug         TEXT   NOT NULL,
    display_name TEXT   NOT NULL,
    -- The only source that consumes the energy budget, and therefore the only
    -- one that appears in the optimizer's objective function.
    energy_cost  INT    NOT NULL,

    available_days TEXT[]      NOT NULL DEFAULT '{}',
    opens_at       TIMESTAMPTZ,
    closes_at      TIMESTAMPTZ,

    CONSTRAINT stage_slug_unique UNIQUE (version_id, slug),
    CONSTRAINT stage_id_scoped UNIQUE (version_id, id),
    CONSTRAINT stage_slug_not_blank CHECK (length(btrim(slug)) > 0),
    CONSTRAINT stage_energy_cost_non_negative CHECK (energy_cost >= 0),
    CONSTRAINT stage_days_are_weekdays CHECK (available_days <@ ARRAY[
        'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'
    ]::TEXT[])
);

-- One row of a stage's drop table. This is an EXPECTED YIELD, not a
-- probability: a stage can drop several copies in one run, so the value is
-- unbounded above and real upstream data carries values well past 1.0. See
-- Drop.java and docs/prior-art.md section 4.1.
--
-- This is the *declared* yield. What the optimizer consumes is a measured
-- DropEstimate from the stats module, with a sample size and an interval. The
-- declaration stays load-bearing: a report claiming an item that cannot drop
-- here is rejected on ingest rather than averaged in.
CREATE TABLE gamedata.stage_drop (
    stage_id       BIGINT NOT NULL,
    version_id     BIGINT NOT NULL,
    item_id        BIGINT NOT NULL,
    expected_yield DOUBLE PRECISION NOT NULL,

    PRIMARY KEY (stage_id, item_id),
    CONSTRAINT stage_drop_stage_fk FOREIGN KEY (version_id, stage_id)
        REFERENCES gamedata.stage (version_id, id) ON DELETE CASCADE,
    CONSTRAINT stage_drop_item_fk FOREIGN KEY (version_id, item_id)
        REFERENCES gamedata.item (version_id, id),
    -- Mirrors Drop's constructor: finite and non-negative. The upper bound
    -- rejects NaN too, which every ordinary comparison would let through.
    CONSTRAINT stage_drop_yield_finite CHECK (
        expected_yield >= 0 AND expected_yield < 'Infinity'::double precision
    )
);

CREATE INDEX stage_drop_item_idx ON gamedata.stage_drop (version_id, item_id);

-- A deterministic conversion, recursive by nature: a tier-3 material is crafted
-- from tier-2 materials that are themselves farmed or crafted.
CREATE TABLE gamedata.craft (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id BIGINT NOT NULL REFERENCES gamedata.game_data_version (id) ON DELETE CASCADE,
    slug       TEXT   NOT NULL,

    available_days TEXT[]      NOT NULL DEFAULT '{}',
    opens_at       TIMESTAMPTZ,
    closes_at      TIMESTAMPTZ,

    CONSTRAINT craft_slug_unique UNIQUE (version_id, slug),
    CONSTRAINT craft_id_scoped UNIQUE (version_id, id),
    CONSTRAINT craft_slug_not_blank CHECK (length(btrim(slug)) > 0),
    CONSTRAINT craft_days_are_weekdays CHECK (available_days <@ ARRAY[
        'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'
    ]::TEXT[])
);

CREATE TABLE gamedata.craft_input (
    craft_id   BIGINT NOT NULL,
    version_id BIGINT NOT NULL,
    ordinal    INT    NOT NULL,
    item_id    BIGINT NOT NULL,
    quantity   INT    NOT NULL,

    PRIMARY KEY (craft_id, ordinal),
    CONSTRAINT craft_input_craft_fk FOREIGN KEY (version_id, craft_id)
        REFERENCES gamedata.craft (version_id, id) ON DELETE CASCADE,
    CONSTRAINT craft_input_item_fk FOREIGN KEY (version_id, item_id)
        REFERENCES gamedata.item (version_id, id),
    CONSTRAINT craft_input_ordinal_non_negative CHECK (ordinal >= 0),
    CONSTRAINT craft_input_quantity_non_negative CHECK (quantity >= 0)
);

CREATE TABLE gamedata.craft_output (
    craft_id   BIGINT NOT NULL,
    version_id BIGINT NOT NULL,
    ordinal    INT    NOT NULL,
    item_id    BIGINT NOT NULL,
    quantity   INT    NOT NULL,

    PRIMARY KEY (craft_id, ordinal),
    CONSTRAINT craft_output_craft_fk FOREIGN KEY (version_id, craft_id)
        REFERENCES gamedata.craft (version_id, id) ON DELETE CASCADE,
    CONSTRAINT craft_output_item_fk FOREIGN KEY (version_id, item_id)
        REFERENCES gamedata.item (version_id, id),
    CONSTRAINT craft_output_ordinal_non_negative CHECK (ordinal >= 0),
    CONSTRAINT craft_output_quantity_non_negative CHECK (quantity >= 0)
);

CREATE INDEX craft_output_item_idx ON gamedata.craft_output (version_id, item_id);

-- A purchase, capped per period. The cap is the interesting part: an uncapped
-- shop entry would let the solver buy its way out of every constraint.
CREATE TABLE gamedata.shop (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id       BIGINT NOT NULL REFERENCES gamedata.game_data_version (id) ON DELETE CASCADE,
    slug             TEXT   NOT NULL,
    currency_item_id BIGINT NOT NULL,
    price            INT    NOT NULL,
    offer_item_id    BIGINT NOT NULL,
    offer_quantity   INT    NOT NULL,
    -- How many times per period. 0 means unlimited, matching Shop.periodLimit.
    period_limit     INT    NOT NULL,
    -- java.time.Period as ISO-8601: "P1D", "P1M". Stored as text because
    -- Postgres INTERVAL normalises in ways that would not round-trip.
    period_iso       TEXT   NOT NULL,

    available_days TEXT[]      NOT NULL DEFAULT '{}',
    opens_at       TIMESTAMPTZ,
    closes_at      TIMESTAMPTZ,

    CONSTRAINT shop_slug_unique UNIQUE (version_id, slug),
    CONSTRAINT shop_id_scoped UNIQUE (version_id, id),
    CONSTRAINT shop_currency_fk FOREIGN KEY (version_id, currency_item_id)
        REFERENCES gamedata.item (version_id, id),
    CONSTRAINT shop_offer_fk FOREIGN KEY (version_id, offer_item_id)
        REFERENCES gamedata.item (version_id, id),
    CONSTRAINT shop_slug_not_blank CHECK (length(btrim(slug)) > 0),
    CONSTRAINT shop_price_non_negative CHECK (price >= 0),
    CONSTRAINT shop_offer_quantity_non_negative CHECK (offer_quantity >= 0),
    CONSTRAINT shop_period_limit_non_negative CHECK (period_limit >= 0),
    CONSTRAINT shop_days_are_weekdays CHECK (available_days <@ ARRAY[
        'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'
    ]::TEXT[])
);

-- Free income on a cadence: dailies, weeklies, event handouts. Feeds the
-- planner as supply the player does not farm, and the gacha income model as
-- pull currency accruing over time.
CREATE TABLE gamedata.reward (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id BIGINT NOT NULL REFERENCES gamedata.game_data_version (id) ON DELETE CASCADE,
    slug       TEXT   NOT NULL,
    cadence    TEXT   NOT NULL,

    available_days TEXT[]      NOT NULL DEFAULT '{}',
    opens_at       TIMESTAMPTZ,
    closes_at      TIMESTAMPTZ,

    CONSTRAINT reward_slug_unique UNIQUE (version_id, slug),
    CONSTRAINT reward_id_scoped UNIQUE (version_id, id),
    CONSTRAINT reward_slug_not_blank CHECK (length(btrim(slug)) > 0),
    CONSTRAINT reward_cadence_known CHECK (
        cadence IN ('DAILY', 'WEEKLY', 'MONTHLY', 'EVENT', 'ONE_OFF')
    ),
    CONSTRAINT reward_days_are_weekdays CHECK (available_days <@ ARRAY[
        'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'
    ]::TEXT[])
);

CREATE TABLE gamedata.reward_grant (
    reward_id  BIGINT NOT NULL,
    version_id BIGINT NOT NULL,
    ordinal    INT    NOT NULL,
    item_id    BIGINT NOT NULL,
    quantity   INT    NOT NULL,

    PRIMARY KEY (reward_id, ordinal),
    CONSTRAINT reward_grant_reward_fk FOREIGN KEY (version_id, reward_id)
        REFERENCES gamedata.reward (version_id, id) ON DELETE CASCADE,
    CONSTRAINT reward_grant_item_fk FOREIGN KEY (version_id, item_id)
        REFERENCES gamedata.item (version_id, id),
    CONSTRAINT reward_grant_ordinal_non_negative CHECK (ordinal >= 0),
    CONSTRAINT reward_grant_quantity_non_negative CHECK (quantity >= 0)
);

-- ── Sinks: how an item leaves an account ────────────────────────────────────

-- A deterministic advance of one entity from one state to the next: Insight 1
-- to Insight 2, rank 3 to rank 4. States are opaque strings supplied by the
-- bundle; the planner only needs the graph they form, never their meaning.
CREATE TABLE gamedata.upgrade (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id BIGINT NOT NULL REFERENCES gamedata.game_data_version (id) ON DELETE CASCADE,
    slug       TEXT   NOT NULL,
    entity_id  BIGINT NOT NULL,
    from_state TEXT   NOT NULL,
    to_state   TEXT   NOT NULL,

    CONSTRAINT upgrade_slug_unique UNIQUE (version_id, slug),
    CONSTRAINT upgrade_id_scoped UNIQUE (version_id, id),
    CONSTRAINT upgrade_entity_fk FOREIGN KEY (version_id, entity_id)
        REFERENCES gamedata.entity (version_id, id) ON DELETE CASCADE,
    -- One edge per (entity, from, to). A second row would be a duplicate cost.
    CONSTRAINT upgrade_edge_unique UNIQUE (entity_id, from_state, to_state),
    CONSTRAINT upgrade_slug_not_blank CHECK (length(btrim(slug)) > 0),
    CONSTRAINT upgrade_states_differ CHECK (from_state <> to_state)
);

CREATE INDEX upgrade_entity_idx ON gamedata.upgrade (version_id, entity_id);

CREATE TABLE gamedata.upgrade_cost (
    upgrade_id BIGINT NOT NULL,
    version_id BIGINT NOT NULL,
    ordinal    INT    NOT NULL,
    item_id    BIGINT NOT NULL,
    quantity   INT    NOT NULL,

    PRIMARY KEY (upgrade_id, ordinal),
    CONSTRAINT upgrade_cost_upgrade_fk FOREIGN KEY (version_id, upgrade_id)
        REFERENCES gamedata.upgrade (version_id, id) ON DELETE CASCADE,
    CONSTRAINT upgrade_cost_item_fk FOREIGN KEY (version_id, item_id)
        REFERENCES gamedata.item (version_id, id),
    CONSTRAINT upgrade_cost_ordinal_non_negative CHECK (ordinal >= 0),
    CONSTRAINT upgrade_cost_quantity_non_negative CHECK (quantity >= 0)
);

CREATE INDEX upgrade_cost_item_idx ON gamedata.upgrade_cost (version_id, item_id);

-- Advancing one item by consuming other items of a class: Memory enhancement.
-- The case that breaks a naive resource graph, in the model from day one — an
-- item is simultaneously a resource and a sink, so the optimizer cannot treat
-- "items owned" as a pure supply.
CREATE TABLE gamedata.fodder (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id        BIGINT NOT NULL REFERENCES gamedata.game_data_version (id) ON DELETE CASCADE,
    slug              TEXT   NOT NULL,
    -- The item category eligible as fodder, matched against item.category.
    consumes_category TEXT   NOT NULL,
    min_rarity_label  TEXT   NOT NULL,
    min_rarity_rank   INT    NOT NULL,
    progress_per_unit INT    NOT NULL,

    CONSTRAINT fodder_slug_unique UNIQUE (version_id, slug),
    CONSTRAINT fodder_id_scoped UNIQUE (version_id, id),
    CONSTRAINT fodder_slug_not_blank CHECK (length(btrim(slug)) > 0),
    CONSTRAINT fodder_progress_positive CHECK (progress_per_unit >= 1)
);

CREATE TABLE gamedata.fodder_cost (
    fodder_id  BIGINT NOT NULL,
    version_id BIGINT NOT NULL,
    ordinal    INT    NOT NULL,
    item_id    BIGINT NOT NULL,
    quantity   INT    NOT NULL,

    PRIMARY KEY (fodder_id, ordinal),
    CONSTRAINT fodder_cost_fodder_fk FOREIGN KEY (version_id, fodder_id)
        REFERENCES gamedata.fodder (version_id, id) ON DELETE CASCADE,
    CONSTRAINT fodder_cost_item_fk FOREIGN KEY (version_id, item_id)
        REFERENCES gamedata.item (version_id, id),
    CONSTRAINT fodder_cost_ordinal_non_negative CHECK (ordinal >= 0),
    CONSTRAINT fodder_cost_quantity_non_negative CHECK (quantity >= 0)
);

-- ── Banners ─────────────────────────────────────────────────────────────────
-- Rates and pity are per rarity, so they are child tables rather than columns.
-- The published rates of both shipped games are acceptance fixtures: if the
-- gacha engines cannot reproduce them from these rows alone, the model is wrong
-- and no amount of engine work fixes it.

CREATE TABLE gamedata.banner (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    version_id   BIGINT NOT NULL REFERENCES gamedata.game_data_version (id) ON DELETE CASCADE,
    slug         TEXT   NOT NULL,
    display_name TEXT   NOT NULL,
    -- The pity bucket when pity_scope is BANNER_TYPE.
    banner_type  TEXT   NOT NULL,
    -- Where pity state is carried. One shipped game carries it per banner type
    -- rather than globally, which is why this is a dimension and not a constant.
    pity_scope   TEXT   NOT NULL,
    -- FeaturedRule, inlined: it is two scalars and never repeats.
    featured_chance_at_hit        DOUBLE PRECISION NOT NULL,
    featured_guarantee_after_loss INT NOT NULL,

    available_days TEXT[]      NOT NULL DEFAULT '{}',
    opens_at       TIMESTAMPTZ,
    closes_at      TIMESTAMPTZ,

    CONSTRAINT banner_slug_unique UNIQUE (version_id, slug),
    CONSTRAINT banner_id_scoped UNIQUE (version_id, id),
    CONSTRAINT banner_slug_not_blank CHECK (length(btrim(slug)) > 0),
    CONSTRAINT banner_pity_scope_known CHECK (pity_scope IN ('GLOBAL', 'BANNER_TYPE', 'BANNER')),
    CONSTRAINT banner_featured_chance_is_probability CHECK (
        featured_chance_at_hit >= 0 AND featured_chance_at_hit <= 1
    ),
    CONSTRAINT banner_featured_guarantee_positive CHECK (featured_guarantee_after_loss >= 1),
    CONSTRAINT banner_days_are_weekdays CHECK (available_days <@ ARRAY[
        'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'
    ]::TEXT[])
);

-- The sum of a banner's base rates must not exceed 1.0. That is a whole-group
-- invariant, which a row-level CHECK cannot express; BannerModel's constructor
-- enforces it. Named here so the gap is on the record rather than a surprise.
CREATE TABLE gamedata.banner_base_rate (
    banner_id    BIGINT NOT NULL,
    version_id   BIGINT NOT NULL,
    rarity_label TEXT   NOT NULL,
    rarity_rank  INT    NOT NULL,
    rate         DOUBLE PRECISION NOT NULL,

    PRIMARY KEY (banner_id, rarity_label),
    CONSTRAINT banner_base_rate_banner_fk FOREIGN KEY (version_id, banner_id)
        REFERENCES gamedata.banner (version_id, id) ON DELETE CASCADE,
    CONSTRAINT banner_base_rate_is_probability CHECK (rate >= 0 AND rate <= 1)
);

-- One pity curve per rarity, general enough for both shipped shapes: a rising
-- soft-pity curve and a flat wall, from the same four columns.
CREATE TABLE gamedata.banner_pity_rule (
    banner_id    BIGINT NOT NULL,
    version_id   BIGINT NOT NULL,
    rarity_label TEXT   NOT NULL,
    rarity_rank  INT    NOT NULL,
    hard_at      INT    NOT NULL,
    soft_from    INT,
    soft_jump_to DOUBLE PRECISION,
    soft_step    DOUBLE PRECISION,

    PRIMARY KEY (banner_id, rarity_label),
    CONSTRAINT banner_pity_rule_banner_fk FOREIGN KEY (version_id, banner_id)
        REFERENCES gamedata.banner (version_id, id) ON DELETE CASCADE,
    CONSTRAINT banner_pity_rule_hard_at_positive CHECK (hard_at >= 1),
    -- Mirrors PityRule's constructor exactly: soft pity needs both of its
    -- numbers and must precede the wall.
    CONSTRAINT banner_pity_rule_soft_is_complete CHECK (
        soft_from IS NULL
        OR (soft_jump_to IS NOT NULL AND soft_step IS NOT NULL AND soft_from < hard_at)
    ),
    CONSTRAINT banner_pity_rule_soft_jump_is_probability CHECK (
        soft_jump_to IS NULL OR (soft_jump_to >= 0 AND soft_jump_to <= 1)
    )
);

-- "At least one 4-star per 10-pull." Independent of pity, and evaluated after.
CREATE TABLE gamedata.banner_floor (
    banner_id        BIGINT NOT NULL,
    version_id       BIGINT NOT NULL,
    ordinal          INT    NOT NULL,
    every_n          INT    NOT NULL,
    min_rarity_label TEXT   NOT NULL,
    min_rarity_rank  INT    NOT NULL,

    PRIMARY KEY (banner_id, ordinal),
    CONSTRAINT banner_floor_banner_fk FOREIGN KEY (version_id, banner_id)
        REFERENCES gamedata.banner (version_id, id) ON DELETE CASCADE,
    CONSTRAINT banner_floor_ordinal_non_negative CHECK (ordinal >= 0),
    CONSTRAINT banner_floor_every_n_positive CHECK (every_n >= 1)
);
