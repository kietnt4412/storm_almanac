-- C2.5 — the plan as a checklist: what the reader has ticked off, kept with the plan.
--
-- The plan screen lists what to run, buy, feed and claim as one list to work
-- through, and the maintainer chose (2026-10-01) that the ticks live with the
-- saved plan rather than in one browser: a reader who ticks on a phone beside
-- the game sees the same list on a laptop.
--
-- THE TICKS BELONG TO ONE PLAN, AND A NEW PLAN STARTS UNTICKED.
--   Every plan run replaces the saved one (V19), and `savePlan` writes this
--   column back to empty. A tick against a plan that has since been replaced
--   is refused rather than carried over: "Run Simulated Battlefield × 41" done
--   is not "× 38" done.
--
-- THE KEYS ARE OPAQUE HERE, as the plan itself is. The page names a line by
--   what it does and to what ("run:<stage>", "step:<step>", "claim:<reward>");
--   the player module stores the list and never reads it.
--
-- NO SYNC CLOCK. The whole list is replaced on every tick, last write wins —
--   the same rule as the plan it hangs off, and the list is short.

ALTER TABLE player.saved_plan
    ADD COLUMN done JSONB NOT NULL DEFAULT '[]'::jsonb;
