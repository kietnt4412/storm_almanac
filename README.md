# Storm Almanac

**Storm Almanac tells you what to farm next, and why.** You enter what you own
and who you want to raise; it solves an integer program against your actual
inventory and hands back the stages, the run counts, and the constraint that put
each one on the list. The tools that exist today have pieces of this and not the
whole. Reverse: 1999's community planner ships one greedy route per patch,
precomputed for nobody in particular, because a static site has nothing to solve
against. Arknights' ArkPlanner has a real solver, but it keeps its reasoning to
itself and covers one game. Storm Almanac solves per player, against
crowdsourced drop rates that carry their own confidence intervals, shows its
working, and does all of it behind a model with no game-specific code in it —
Punishing: Gray Raven runs on the same optimizer as Reverse: 1999, which is the
only real proof that the first game was modelled rather than hardcoded.

It runs on a storage engine and a consensus layer written from scratch — one
product to be used, one substrate to be understood.

- **Launch title:** Reverse: 1999 · **Second title:** Punishing: Gray Raven
- **Stack:** Java 21 · Spring Boot 3 · React 19 · Postgres · Redis
- **Plan:** [plan.html](plan.html) — 13 phases, two tracks
- **Progress:** [TRACKER.md](TRACKER.md) — current phase, what is done, what is next

## The two tracks

**Track A — product.** The optimizer, the gacha engine, the drop-statistics
platform, two games, a public deployment with real users. Postgres and Redis
underneath, chosen because they work.

**Track B — substrate.** An LSM-tree storage engine for the drop-report log, a
Raft implementation coordinating the solver fleet, and a fault-injection harness
that tries to break both.

Track A ships first and completely. Track B starts only once the product is
publicly deployed with real traffic, because infrastructure written for a system
with no users is infrastructure written against imaginary requirements.

Every hand-built component sits behind an interface that already has a boring
implementation. Both stay in the codebase, selectable by config, and the
benchmark gets published — including if the boring one wins. See
[ADR 0003](docs/adr/0003-the-honesty-rule.md).

## Running it

```bash
docker compose up
```

Postgres, Redis and the API, from a clean clone. `GET /api/health` answers on
`localhost:8080`.

Backend only:

```bash
cd backend && ./gradlew build
```

Frontend only (proxies `/api` to `localhost:8080`):

```bash
cd frontend && npm install && npm run dev
```

The Gradle wrapper is committed and pins Gradle 9.6.0, so a JDK 21 is the only
prerequisite.

## Layout

```
backend/
  modules/common      shared kernel — typed ids, versioning, the event contract
  modules/gamedata    canonical catalog, versioned publishing, ingestion, diffing
  modules/identity    accounts, OAuth, sessions, profiles
  modules/player      inventory, roster, goals, sync
  modules/planner     the MIP optimizer, plans, explanations
  modules/gacha       banner models, Markov + Monte Carlo engines, income model
  modules/stats       drop reports, aggregation, estimate publication
  modules/api         REST + WebSocket edge
  app                 the single deployable
  substrate/almanac-store   Track B · LSM storage engine        (phase 7)
  substrate/almanac-raft    Track B · consensus                 (phase 8)
  substrate/almanac-chaos   Track B · fault injection           (phase 10)
frontend/             React 19 + TypeScript + Vite, PWA with offline editing
docs/adr/             every decision names the trigger that would reverse it
```

## Onboarding a game

A title is **a data bundle plus a parser adapter**, and no backend code. The
same jar the server runs from is also `gamedata-cli`:

```bash
java -jar storm-almanac.jar --gamedata=preview proving-ground-1.1.json
```

`preview` says what approving that file would change, against what is published
now, without writing a row — because publishing is a human approval and an
approval nobody could review is a rubber stamp. Then `ingest` writes it as a
draft and `publish` makes it live.

A version is a full snapshot rather than a delta, so a plan computed last patch
stays correct, and a patch report is an ordinary set comparison:

```
proving-ground: 1.0 → 1.1

progression · 5 change(s)
  + item 'sigil-radiant'
  ~ stage 'pg-1-1' · drop ore-rough: 1.4 → 1.6
  + stage 'pg-3-1'
  - stage 'pg-event-1'
  ~ upgrade 'warden-insight-2' · cost gold: 20000 → 18000

catalog · 2 change(s)
  ~ entity 'warden' · atk at tier 1 level 40: 415.0 → 430.0
  ~ entity 'warden' · skill warden-strike rank 2 damage: 1.32 → 1.28
```

The two axes are separate because a balance patch moves combat multipliers
without touching a single material cost, and a reader who came for one does not
want to page through the other.

The format, the commands and what gets rejected:
[docs/game-data-bundles.md](docs/game-data-bundles.md).

## Reading it back

Published game data is served read-only and without an account — reference
numbers a stranger arriving from a search should not have to sign up to read:

```
GET /api/games/{game}/versions
GET /api/games/{game}/entities                    [?version=N]
GET /api/games/{game}/entities/{entity}           [?version=N]
GET /api/games/{game}/entities/{entity}/upgrades  [?version=N]
GET /api/games/{game}/diff?from=N&to=M
```

`?version=N` pins a read to one published snapshot; without it you get the
latest approved one. Every response carries the version its numbers came from
and the attribution that came with them, because a page that cannot say which
patch it describes is the failure the whole versioning model exists to prevent.

`/diff` answers with structured changes, or with the report above verbatim if
you ask for `text/plain` — one renderer, so what a reviewer approved and what a
reader sees cannot drift apart.

Publishing has no endpoint. It is a human approval through the CLI, so only
`GET` is public here and everything else is denied.

## The rules the build enforces

Two tests in `:app` turn the project's central claims into failing builds rather
than good intentions:

- `ModuleBoundaryTest` — modules do not reach into each other. No cross-module
  database reads; extraction later stays a deployment change.
- `GameAgnosticismTest` — no game-specific code in `planner`, `gacha` or
  `stats`. A new game is a `GameDefinition` bundle plus a parser adapter, and
  nothing else. Phase 11 exists to prove the line held.

## Guardrails

- **No game assets.** Numbers and text only, attributed, with links out.
- **Never touch the client.** No automation, no credentials, no packet capture.
  Screenshot OCR runs on the user's own uploads, client-side where possible.
- **No claimed affiliation** with Bluepoch or Kuro Games; visible disclaimer.
- **Modest monetization.** Publishers tolerate fan tools; they do not tolerate
  commercialising their IP.

## License

Code is [MIT](LICENSE).

That covers the code only. Ingested game data — item names, stage costs, upgrade
requirements, stat curves, skill values — is the property of its publishers and
is redistributed here under fan-tool norms, attributed, with links out. It is
not licensed by this project and is not MIT. Anyone reusing this repository
inherits the code, not the right to redistribute that data.

Storm Almanac is an unofficial fan tool with no affiliation with Bluepoch or
Kuro Games.

## Prior art, credited

[Kornblume](https://github.com/windbow27/kornblume) — Reverse: 1999 planner, a
static site with no server, whose farming routes are precomputed per patch.
[Penguin Statistics](https://penguin-stats.io) — crowdsourced Arknights drop
rates with a real backend, plus [ArkPlanner](https://github.com/penguin-statistics/ArkPlanner)'s
LP farming solver. Both are worth reading closely, and both are one game by
design.

Storm Almanac's bet is server-side integer optimization over crowdsourced
estimates with a domain model that is game-agnostic by construction — all three
at once. Read [docs/prior-art.md](docs/prior-art.md) for what each of them
actually does, where this differs deliberately, and the one place their real
data proved our first model wrong.
