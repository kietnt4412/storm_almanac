-- A pity rule can now say that its guarantee is drawn rather than fixed: a
-- threshold generated uniformly over a range at the start of every pity cycle
-- and generated again on every hit.
--
-- FOUND BY: reading Punishing: Gray Raven off the client (N31 in TRACKER.md).
-- Its Themed Construct pool displays the counter as `8/80~100` and its rules
-- panel says a value is generated randomly between 80 and 100, that an S-Rank
-- is guaranteed within that value, and that the value is generated again once
-- an S-Rank has arrived. The player is never told their own threshold.
--
-- Without this column the archetype has no honest spelling. hard_at = 80
-- promises a wall the game does not honour; hard_at = 100 forgets the twenty
-- pulls in which it usually arrives. So the banner was absent from the first
-- bundle rather than approximated, which is the one outcome a data model is
-- allowed to have and a rounded number is not.
--
-- drawn_from is the BOTTOM of the range and hard_at keeps its meaning exactly:
-- the pull at which the rarity is certain. So every row written before this
-- reads back unchanged, and a NULL here means what every one of them claimed
-- when it was published — a wall at hard_at and nowhere else. A version is
-- immutable and has to stay readable.
ALTER TABLE gamedata.banner_pity_rule
    ADD COLUMN drawn_from INT;

-- Mirrors PityRule's constructor exactly, including the refusal. A range of one
-- is a fixed wall said the long way, and letting it in would put two spellings
-- of the same banner into the schema.
ALTER TABLE gamedata.banner_pity_rule
    ADD CONSTRAINT banner_pity_rule_drawn_from_precedes_hard_at CHECK (
        drawn_from IS NULL OR (drawn_from >= 1 AND drawn_from < hard_at)
    );
