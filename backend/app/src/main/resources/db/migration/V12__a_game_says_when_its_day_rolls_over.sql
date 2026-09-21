-- A title can now say when its day rolls over, so that the weekday an instant
-- falls on is a property of the game rather than of the planner.
--
-- FOUND BY: EnergyMip.matchingDays read weekdays as UTC midnight (N20 in
-- TRACKER.md), with a javadoc saying in as many words that it was a placeholder
-- rather than a decision. GameAgnosticismTest cannot see an assumption like
-- that one: it scans for game names, and "UTC" is not a game name. It stops
-- being inert the moment a game with a rotating stage is ingested, and then it
-- is silent — a plan starting at 03:00 with a 05:00 reset begins on the wrong
-- weekday and moves a whole day of energy into the wrong bucket.
--
-- Punishing: Gray Raven resets at 05:00 on a server clock that runs UTC, read
-- off the client on 2026-09-19. Reverse: 1999 Global resets at 05:00 UTC-5.
--
-- The zone is a zone and not an offset because an offset cannot summer: a game
-- on a civil timezone moves with it, and a game really on a fixed offset writes
-- "UTC" or "-05:00" here at no cost.
--
-- Both columns are nullable, and NULL means UNSTATED rather than midnight UTC.
-- The planner falls back to midnight UTC, which is exactly what every version
-- published before this migration was planned by, so nothing stored moves. The
-- two have to stay distinguishable so that a version round-trips as what it
-- actually claimed rather than as what we later decided it meant: a version is
-- immutable and has to stay readable.
ALTER TABLE gamedata.game
    ADD COLUMN day_rollover_zone TEXT,
    ADD COLUMN day_rollover_hour INT;

-- Both or neither. A zone with no hour is not half a boundary, it is a row the
-- reader would have to invent an hour for, and an hour with no zone is not an
-- instant at all. Mirrors DayBoundary's constructor, including the range.
ALTER TABLE gamedata.game
    ADD CONSTRAINT game_day_rollover_is_whole CHECK (
        (day_rollover_zone IS NULL AND day_rollover_hour IS NULL)
        OR (day_rollover_zone IS NOT NULL
            AND day_rollover_hour IS NOT NULL
            AND day_rollover_hour BETWEEN 0 AND 23)
    );
