# Storm Almanac — working notes for Claude

## Start here

1. **Read [TRACKER.md](TRACKER.md) first, all of it.** It carries the current
   phase, what is actually done versus merely written, the next actions, and the
   open questions. It is the handoff between sessions — trust it over re-deriving
   from the code. It is deliberately kept short enough to read in full; if it
   stops being that, fix it rather than skimming it.
2. **History is in [docs/history/tracker-archive.md](docs/history/tracker-archive.md)** —
   the session log, closed next actions, closed phases, answered questions. Read
   it when you need to know *why* something is the way it is, not at session
   start. Nothing is ever deleted from it.
3. **Update both before the session ends.** Tick a box only when the exit
   criterion is met. **Append the session-log entry to the archive** and add one
   line to the tracker's index. Move anything unresolved into *Open questions*,
   and move anything finished out of the tracker into the archive — the tracker
   grows by the length of what it stops carrying.
4. The full design is [plan.html](plan.html). `README.md` is the public face.

## Non-negotiables

These are project invariants, not preferences. Violating one needs an ADR, not a
comment.

- **No game-specific code in `planner`, `gacha` or `stats`.** Not one
  `if (game == ...)`. A new game is a `GameDefinition` bundle plus a parser
  adapter, and nothing else. `GameAgnosticismTest` enforces this by scanning
  source; prose in comments naming a game to explain *why* an abstraction has
  its shape is fine and is not scanned.
- **No cross-module database reads.** One schema per module; modules talk
  through `EventPublisher`. `ModuleBoundaryTest` enforces this.
- **Every hand-built (Track B) component keeps its boring implementation**,
  selectable by `storm-almanac.substrate.*` in `application.yml`. Publish the
  benchmark either way — concluding that Postgres won is an allowed and
  respectable outcome.
- **Track B is gated.** Do not write code in `substrate/` until the product is
  publicly deployed with real traffic (end of Phase 4, realistically Phase 6).
  Check the gate status in TRACKER.md.
- **No game assets, no client automation.** Numbers and text only, attributed.

## Conventions

- Java 21, records and sealed interfaces for the domain. Identifiers are typed
  (`ItemId`, `StageId`, …), never bare strings.
- Anything the game varies is *data*: rarity is `(label, rank)`, not an enum;
  pity is a `PityRule` record; upgrade states are opaque strings.
- Flyway owns the schema; Hibernate is `ddl-auto: validate` and never writes DDL.
- Ports live in the module that needs them, adapters in the module that provides
  them. Keep ports narrow — a port shaped to flatter a hand-built implementation
  proves nothing.
- ADRs in `docs/adr/`, numbered, each ending with the trigger that would reverse
  it. Never edit an accepted ADR; supersede it.
- Tests state the behaviour in `@DisplayName`, and the two published games' rates
  are acceptance fixtures rather than examples.

## Build

The wrapper is committed and pins Gradle 9.6.0.

```bash
cd backend && ./gradlew build
```

```bash
docker compose up
```

Working directory matters for `GameAgnosticismTest`: it resolves guarded source
roots relative to `backend/app`.

The tests need a Docker daemon: several start a real Postgres through
Testcontainers, because a schema is not proven by a migration that applies.

The same jar is also `gamedata-cli`. Onboarding a title is a bundle plus a
parser adapter, and publishing it is a human approval, so the loop is
*preview, ingest, publish*:

```bash
java -jar backend/app/build/libs/storm-almanac.jar --gamedata=preview bundle.json
```
