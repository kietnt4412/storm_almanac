-- Phase 0 baseline. One schema per module; no module reads another's tables.
-- Tables arrive with the phase that needs them, not up front.
--   gamedata  phase 1    identity/player  phase 3
--   planner   phase 2    stats            phase 6
CREATE SCHEMA IF NOT EXISTS gamedata;
CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS player;
CREATE SCHEMA IF NOT EXISTS planner;
CREATE SCHEMA IF NOT EXISTS gacha;
CREATE SCHEMA IF NOT EXISTS stats;
