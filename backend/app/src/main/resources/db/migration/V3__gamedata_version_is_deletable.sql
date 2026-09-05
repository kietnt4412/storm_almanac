-- A game data version must be deletable as a unit. In V2 it was not.
--
-- FOUND BY: the first ingest that wrote real data and then re-ingested it.
-- Re-ingesting a draft is the ordinary path — a scheduled fetch re-runs, and a
-- failed ingest must not wedge the sequence — and it deletes the version row and
-- lets ON DELETE CASCADE clear everything underneath. That delete fails:
--
--   ERROR: update or delete on table "item" violates foreign key constraint
--          "skill_rank_cost_item_fk" on table "skill_rank_cost"
--
-- WHY. Every versioned table cascades from game_data_version, so deleting the
-- version deletes both `item` and, along a different chain
-- (entity -> skill -> skill_rank -> skill_rank_cost), the rows that reference
-- it. Postgres does not order those two chains against each other. If `item` is
-- reached first, the NO ACTION foreign key on skill_rank_cost.item_id is
-- checked immediately, sees rows that still exist, and refuses. The same race
-- exists on all nine item-referencing constraints; which one fires is an
-- implementation detail of the delete plan, which is why V2's tests never saw
-- it — they inserted, and never deleted a populated version.
--
-- THE FIX. The item-referencing foreign keys gain ON DELETE CASCADE.
--
-- What they are for is unaffected. Their job is the composite (version_id, id)
-- reference: a v2 stage cannot cite a v1 item, and that is enforced by the
-- columns, not by the delete rule. GameDataSchemaTest continues to prove it.
--
-- The tradeoff, stated rather than buried: deleting a single item row now
-- silently deletes the costs and drops that referenced it, where before the
-- database would have refused. That is acceptable here and nowhere else,
-- because a version is written once and never edited — nothing in the codebase
-- deletes one item, and the only delete that exists removes a whole draft. If
-- a future feature ever edits a published version in place, this rule is the
-- first thing that has to be reconsidered.
--
-- The alternative was to delete the twenty-odd child tables by hand in
-- dependency order before the version row. That keeps the stricter rule and
-- puts the schema's shape into a Java method that has to be updated every time
-- a table is added, silently wrong until the day a draft is replaced.

ALTER TABLE gamedata.stage_drop
    DROP CONSTRAINT stage_drop_item_fk,
    ADD  CONSTRAINT stage_drop_item_fk FOREIGN KEY (version_id, item_id)
         REFERENCES gamedata.item (version_id, id) ON DELETE CASCADE;

ALTER TABLE gamedata.craft_input
    DROP CONSTRAINT craft_input_item_fk,
    ADD  CONSTRAINT craft_input_item_fk FOREIGN KEY (version_id, item_id)
         REFERENCES gamedata.item (version_id, id) ON DELETE CASCADE;

ALTER TABLE gamedata.craft_output
    DROP CONSTRAINT craft_output_item_fk,
    ADD  CONSTRAINT craft_output_item_fk FOREIGN KEY (version_id, item_id)
         REFERENCES gamedata.item (version_id, id) ON DELETE CASCADE;

ALTER TABLE gamedata.shop
    DROP CONSTRAINT shop_currency_fk,
    ADD  CONSTRAINT shop_currency_fk FOREIGN KEY (version_id, currency_item_id)
         REFERENCES gamedata.item (version_id, id) ON DELETE CASCADE,
    DROP CONSTRAINT shop_offer_fk,
    ADD  CONSTRAINT shop_offer_fk FOREIGN KEY (version_id, offer_item_id)
         REFERENCES gamedata.item (version_id, id) ON DELETE CASCADE;

ALTER TABLE gamedata.reward_grant
    DROP CONSTRAINT reward_grant_item_fk,
    ADD  CONSTRAINT reward_grant_item_fk FOREIGN KEY (version_id, item_id)
         REFERENCES gamedata.item (version_id, id) ON DELETE CASCADE;

ALTER TABLE gamedata.upgrade_cost
    DROP CONSTRAINT upgrade_cost_item_fk,
    ADD  CONSTRAINT upgrade_cost_item_fk FOREIGN KEY (version_id, item_id)
         REFERENCES gamedata.item (version_id, id) ON DELETE CASCADE;

ALTER TABLE gamedata.fodder_cost
    DROP CONSTRAINT fodder_cost_item_fk,
    ADD  CONSTRAINT fodder_cost_item_fk FOREIGN KEY (version_id, item_id)
         REFERENCES gamedata.item (version_id, id) ON DELETE CASCADE;

ALTER TABLE gamedata.skill_rank_cost
    DROP CONSTRAINT skill_rank_cost_item_fk,
    ADD  CONSTRAINT skill_rank_cost_item_fk FOREIGN KEY (version_id, item_id)
         REFERENCES gamedata.item (version_id, id) ON DELETE CASCADE;
