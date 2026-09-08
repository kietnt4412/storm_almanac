-- Phase 3 — who the player is, and what they own.
--
-- Two schemas, two modules, and four decisions worth stating once here rather
-- than re-deriving them from the DDL.
--
-- 1. NO FOREIGN KEY CROSSES A SCHEMA.
--    player.profile.account_id names a row in identity.account and there is no
--    REFERENCES clause on it, on purpose. A cross-schema key is a cross-module
--    coupling the database enforces, and this project's standing claim is that
--    extracting a module later is a deployment change rather than a rewrite —
--    which stops being true the moment one module's DDL cannot be applied
--    without the other's. ModuleBoundaryTest enforces the same rule in Java;
--    this comment is the half of it a test cannot see. The cost is real and is
--    accepted: a deleted account leaves orphan profiles until something
--    reacts to the event, and nothing publishes that event yet.
--
-- 2. NO FOREIGN KEY CROSSES INTO GAMEDATA EITHER, AND HERE IT IS NOT EVEN A
--    CHOICE. Every gamedata row is scoped to one game_data_version and is
--    replaced wholesale by the next patch (see V2). A player's inventory
--    outlives every patch, so it cannot point at rows that a publish deletes.
--    Items and entities are therefore stored as the upstream *slug* — the same
--    stable string ItemId and EntityId carry — and resolve against whichever
--    version a plan is solved against. An item that disappears from the game
--    stays in the inventory and simply stops being demanded, which is the
--    behaviour a player expects and the one a foreign key would forbid.
--
-- 3. AN IDENTITY IS (PROVIDER, SUBJECT), NEVER AN EMAIL.
--    Providers let people change their email; some let people change it to one
--    they do not control. Keying an account on the email claim is the standard
--    account-takeover route into a service like this one, and it costs nothing
--    to avoid. Email is stored because it is useful to show, and it is
--    deliberately not unique.
--
-- 4. ABSENT MEANS ZERO.
--    An inventory row with quantity 0 and no row at all are the same state, and
--    Inventory.with(item, 0) already collapses them in Java. The CHECK below
--    makes the database agree, so the two representations cannot diverge.

-- ── Accounts ────────────────────────────────────────────────────────────────

CREATE TABLE identity.account (
    id           TEXT PRIMARY KEY,
    display_name TEXT NOT NULL,
    -- Shown, not trusted, and not a key. See decision 3 above.
    email        TEXT NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT account_id_not_blank CHECK (length(btrim(id)) > 0)
);

-- One row per provider a person has signed in with. Two rows may point at one
-- account — that is what linking a second provider means — but nothing in this
-- phase creates the second one, because linking has to be done by an already
-- authenticated user and there is no such flow yet.
CREATE TABLE identity.account_identity (
    -- "google" | "discord". Free-form: a new provider is configuration, not a
    -- migration.
    provider   TEXT NOT NULL,
    -- The provider's stable subject claim. Opaque and never displayed.
    subject    TEXT NOT NULL,
    account_id TEXT NOT NULL REFERENCES identity.account (id) ON DELETE CASCADE,
    linked_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (provider, subject),
    CONSTRAINT account_identity_provider_not_blank CHECK (length(btrim(provider)) > 0),
    CONSTRAINT account_identity_subject_not_blank CHECK (length(btrim(subject)) > 0)
);

CREATE INDEX account_identity_account_idx ON identity.account_identity (account_id);

-- ── Profiles ────────────────────────────────────────────────────────────────
-- One account on one game in one region. People run a main, an alt and a second
-- region, so this is an entity rather than a column on the account, and every
-- inventory, roster and goal below hangs off it.

CREATE TABLE player.profile (
    id           TEXT PRIMARY KEY,
    -- identity.AccountId. No REFERENCES — decision 1.
    account_id   TEXT NOT NULL,
    -- gamedata.GameId. No REFERENCES — decision 2.
    game_id      TEXT NOT NULL,
    display_name TEXT NOT NULL,
    -- "global" | "cn". Free-form and game-supplied. It matters more than it
    -- looks: the two regions run different patches, and N20's day boundary is a
    -- property of the region rather than of the game.
    region       TEXT NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT profile_id_not_blank CHECK (length(btrim(id)) > 0),
    CONSTRAINT profile_display_name_not_blank CHECK (length(btrim(display_name)) > 0),
    -- One person, one profile per game per region. A second "main" on the same
    -- server is the same account, and letting it exist twice would silently
    -- split an inventory in half.
    CONSTRAINT profile_unique_per_account UNIQUE (account_id, game_id, region)
);

CREATE INDEX profile_owner_idx ON player.profile (account_id);

-- ── What the profile owns ───────────────────────────────────────────────────

CREATE TABLE player.inventory_item (
    profile_id TEXT NOT NULL REFERENCES player.profile (id) ON DELETE CASCADE,
    -- The upstream slug an ItemId wraps. Not a foreign key — decision 2.
    item_slug  TEXT NOT NULL,
    quantity   INT  NOT NULL,

    PRIMARY KEY (profile_id, item_slug),
    CONSTRAINT inventory_item_slug_not_blank CHECK (length(btrim(item_slug)) > 0),
    -- Strictly positive, not merely non-negative — decision 4.
    CONSTRAINT inventory_item_quantity_positive CHECK (quantity > 0)
);

-- No updated_at here yet. Inventory's javadoc commits to last-write-wins per key
-- for offline sync, and that merge needs a per-key timestamp — but nothing
-- merges anything today, and a timestamp column nobody reads is a feature that
-- only looks implemented. It arrives with the merge, in the same change.

CREATE TABLE player.roster_entry (
    profile_id    TEXT NOT NULL REFERENCES player.profile (id) ON DELETE CASCADE,
    -- The upstream slug an EntityId wraps. Not a foreign key — decision 2.
    entity_slug   TEXT NOT NULL,
    -- The same opaque string the upgrade graph uses. The planner walks from here
    -- to the goal state without interpreting either, which is what keeps
    -- "insight 2" and "rank 3" out of a game-agnostic module.
    current_state TEXT NOT NULL,

    PRIMARY KEY (profile_id, entity_slug),
    CONSTRAINT roster_entry_slug_not_blank CHECK (length(btrim(entity_slug)) > 0),
    CONSTRAINT roster_entry_state_not_blank CHECK (length(btrim(current_state)) > 0)
);

CREATE TABLE player.goal (
    profile_id     TEXT NOT NULL REFERENCES player.profile (id) ON DELETE CASCADE,
    -- Goals is an ordered list and the order is the player's own priority, so it
    -- is stored rather than recomputed from the priority column: two goals may
    -- share a priority and still have a settled order on screen.
    ordinal        INT  NOT NULL,
    entity_slug    TEXT NOT NULL,
    target_state   TEXT NOT NULL,
    satisfiability TEXT NOT NULL,
    priority       INT  NOT NULL,

    PRIMARY KEY (profile_id, ordinal),
    -- The same target twice is not a stronger wish, it is a duplicate row that
    -- would be demanded twice by anything that forgot to deduplicate.
    CONSTRAINT goal_unique_target UNIQUE (profile_id, entity_slug, target_state),
    CONSTRAINT goal_ordinal_non_negative CHECK (ordinal >= 0),
    CONSTRAINT goal_entity_slug_not_blank CHECK (length(btrim(entity_slug)) > 0),
    CONSTRAINT goal_target_state_not_blank CHECK (length(btrim(target_state)) > 0),
    CONSTRAINT goal_satisfiability_known
        CHECK (satisfiability IN ('DETERMINISTIC', 'PROBABILISTIC'))
);
