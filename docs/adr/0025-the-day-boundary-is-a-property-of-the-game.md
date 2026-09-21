# 25. The day boundary is a property of the game

**Status:** Accepted · 2026-09-21

## Context

`EnergyMip.matchingDays` decided which weekday a plan started on by reading
`from.atZone(ZoneOffset.UTC).getDayOfWeek()`. Its own javadoc said, in as many
words, that this was a placeholder rather than a decision. It had said so since
[ADR 0013](0013-the-horizon-is-a-scalar-not-an-index.md) made the horizon a
scalar, and nothing had moved it since.

**A game's day is not the calendar's.** Punishing: Gray Raven resets at **05:00
on a server clock that runs UTC**, read off the client on 2026-09-19 — the home
screen showed 00:04 server time while the Resources menu counted down 04:58:18
to reset ([the reading](../game-facts/punishing-gray-raven-research-disclosure.md)).
Reverse: 1999 Global resets at 05:00 UTC−5. So 03:00 UTC on a Monday is Monday
to the planner and **still Sunday** to a PGR player, and a plan that starts
there and farms a Monday-only stage is farming a day that has not begun.

Three things made this worth doing before Phase 4 closes rather than after.

- **It is invisible.** A plan built on the wrong weekday is a plan: it solves, it
  prints, it quotes a number. Rotation is a *shared capacity* over subsets of
  weekday restrictions, so a wrong start day does not fail — it moves a whole
  day of energy into the wrong bucket. Nothing in the pipeline notices.
- **`GameAgnosticismTest` cannot see it.** That test scans the guarded source
  roots for game names. `UTC` is not a game name, so the one invariant that
  would have caught a game assumption in a game-agnostic module was blind to
  this one. The rule it enforces is *no `if (game == …)`*; this was the other
  shape — a constant that is right for no game in particular.
- **It was inert, and inertness expires.** No bundle this project has ingested
  declares a rotating stage, so the placeholder has never yet produced a wrong
  answer. The first PGR stage that rotates makes it wrong, silently, on the day
  it lands.

## Decision

1. **`DayBoundary(ZoneId zone, int hour)`, hanging off `Game`.** The zone and
   the hour are data about a title, the same way `energyUnit` is, and the
   planner holds no opinion about when a day starts. One method,
   `dayOfWeekAt(Instant)`, and `EnergyMip` calls it.
2. **A zone, not an offset.** An offset cannot summer: a game whose servers keep
   a civil timezone shifts with it, and a stored −5 would be an hour wrong for
   half the year. A fixed offset is a `ZoneId` too — `UTC`, or the id `UTC+7` —
   so a game really on one loses nothing. The arithmetic is done on the zoned
   value (`atZone(zone).minusHours(hour)`) so a zone that changes offset across
   the boundary moves the boundary with it.
3. **Null means unstated, and unstated plans as midnight UTC.** Not the same
   thing, and they are kept distinguishable through the parser, the writer, the
   schema and the reader. A version is immutable: one published before this
   field existed claimed nothing about a reset and must read back claiming
   nothing, while still producing exactly the plan it produced when it was
   published. The fallback lives in one place, `Game.dayBoundaryOrDefault()`,
   because a call site that forgot it would plan a 05:00 game as a midnight one
   and nothing would look wrong.
4. **Still not a day index.** This moves one `DayOfWeek` and writes no
   constraint rows. ADR 0013 stands: no variable in the model is indexed by day,
   which is what holds p95 at 1 807 ms.
5. **The title is flattened into the patch diff.** `Facts` never looked at
   `Game` at all — harmless while the only field was the energy unit, and not
   harmless for a field that moves every rotating stage's capacity. Without this
   the sequence that first declares a boundary previews as *no changes* while
   the plans under it move. **Found by running the preview, not by a test**; the
   test came after.
6. **PGR's bundle declares it, as sequence 5.** Nothing new was read — the
   reading is from 2026-09-19 and had nowhere to live, which is the same reason
   sequence 1 existed. It moves no plan in that file, because its one stage is
   open every day; it is written so that the first rotating one does not have to
   remember.

## Consequences

- **Five pieces and the work is the fifth**, as planned: domain record, parser,
  writer, `V12` migration, JDBC round trip — then the planner. `EnergyMip` gains
  no variables, no constraints and no rows.
- **Nothing published moves.** Every version written before `V12` reads back with
  a null boundary and plans exactly as it did. `GameDataIngestTest`'s
  whole-graph equality over `proving-ground` proves the absent case and over the
  PGR bundle proves the declared one, through two nullable columns that have to
  come back as one object or as nothing.
- **`GameAgnosticismTest` still cannot see this class of bug**, and that is worth
  writing down rather than pretending otherwise. A constant that is wrong for
  every game is not a game name, and no source scan will find the next one. What
  found this one was a javadoc a previous session was honest enough to write.
- **R1999 is left alone on purpose.** Its 05:00 UTC−5 is not a first-hand
  reading, the Kornblume adapter is a never-shipped cross-check, and its stage
  table does rotate — so declaring a boundary there would move the **nine
  benchmark agreements**, which are the project's load-bearing claim, on the
  strength of a second-hand number. It belongs to Phase 11, with the rest of
  R1999's first-hand sourcing.
- **The fact ledger cannot carry this fact.** `GameDataBundle.factRefs()` covers
  items, entities, sources, sinks and banners — not `Game` — so the boundary has
  no `factProvenance` row, exactly as `energyUnit` has none. The reading behind
  PGR's is `equipment-and-resource-screens`, and only the bundle's comment says
  so. [ADR 0016](0016-provenance-is-a-property-of-the-data.md) says provenance is
  a property of the data; for game-level fields it is not yet, and this record is
  where that gap is admitted rather than discovered later.
- **`Availability.opensAt` is still read by nobody**, and this change deliberately
  does not fix it — [ADR 0024](0024-an-expiring-grant-is-a-deadline-the-plan-reports-not-a-schedule-it-builds.md)
  left it in the same place. A rollover hour and an unread banner window are
  different problems that happen to share a package.

**Reverse this** if a game rolls its day over somewhere a zone and an hour
cannot express — a reset that moves with a patch schedule, a per-region reset
inside one published version, or a game with two resets a day. Any of those is a
different shape, not a bigger `DayBoundary`. Reverse decision 2 if a published
game turns out to keep a fixed offset that its own client displays as an offset,
and storing a civil zone makes the stored value harder to check against the
screen than the thing that was read.
