# Storm Almanac

**Storm Almanac tells you what to farm next, and why.** You enter what you own
and who you want to raise; it solves an integer program against your actual
inventory and hands back the stages, the run counts, and the constraint that put
each one on the list.

It launches on **Punishing: Gray Raven**, where we found no open planner of this
kind. Reverse: 1999 follows on the same optimizer, and a second game with no
game-specific code is the only real proof that the first was modelled rather
than hardcoded. Reverse: 1999 already has a good planner: Kornblume solves a
linear program against the player's own warehouse, in the browser. What Storm
Almanac adds there is narrower, and worth stating exactly: whole runs instead of
fractional ones, the reason each stage is on the list, drop rates discounted by
how thin their sample is, and where every number was read. Arknights' ArkPlanner
has a real solver too, but it keeps its reasoning to itself and covers one game.

It runs on a storage engine and a consensus layer written from scratch — one
product to be used, one substrate to be understood.

- **Launch title:** Punishing: Gray Raven · **Second title:** Reverse: 1999
  (swapped 2026-09-13; everything built so far was built against Reverse: 1999)
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

### Signing in locally

No OAuth provider is configured, because a client registration is issued against
a redirect URI and nothing is deployed yet. To work on the signed-in half, start
the backend from source and use the development sign-in:

```bash
cd backend && ./gradlew :app:bootRun
```

Then `GET /dev/sign-in?as=<any name>` — it creates the account if it is new and
leaves a session behind. **It is a separate Gradle module that the deployable jar
does not contain**, so it exists on `bootRun` and cannot exist on a deployment;
`docker compose` builds that same jar and therefore has no sign-in either. See
[ADR 0017](docs/adr/0017-the-development-sign-in-is-absent-from-the-artifact.md),
and `DeployableJarTest`, which opens the artifact and proves it.

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
  adapters/reverse-1999     one title's upstream, converted — the whole cost of a game
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

### Where a bundle comes from

Nobody hand-writes a thousand rows. For a title whose data somebody else already
publishes, an adapter converts a snapshot into a canonical bundle *file*, which
then goes through the same three commands:

```bash
./tools/fetch-upstream.sh
java -jar storm-almanac.jar --gamedata=adapt reverse-1999 build/upstream-snapshots/3.5 1 3.5 out.json
```

The whole cost of a title is one Gradle module under `backend/adapters/` — the
build enforces that, since only `app` may depend on one. `adapt` prints what it
refused to convert as it goes, because a snapshot that silently yields half a
catalogue should be caught before anybody approves it.

**On the data itself, plainly:** the upstream sources carry no licence, so this
repository contains none of their data and never will. The script fetches into
an ignored directory; the tests that use it skip when it is absent, which is the
state CI is in. That is deliberate and it means **the strongest tests here are
ones the pipeline does not run** — see
[ADR 0009](docs/adr/0009-upstream-data-is-fetched-never-vendored.md). The
committed fixtures are a synthetic title, `proving-ground`, invented for this
repository.

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

## Planning

Given what a player owns and what they want, the planner finds the cheapest way
to close the gap: a mixed-integer program over stage runs and crafting
conversions, minimising energy, with whole-number runs because *"run 3-4 exactly
17.3 times"* is not advice. Crafting is not expanded recursively — every recipe
is a variable and every intermediate material is a constraint, so the solver
prices the whole chain and picks the route.

**There is no endpoint for it yet.** It is reachable from tests and nothing else,
because a plan needs a player's inventory and roster and those are the next
phase's job.

What it will say when it answers matters more than that it answers, so it is
worth stating up front:

- **A plan is the cheapest one the solver could prove inside two seconds**, and
  when that is not the cheapest plan it says so *and how much cheaper one could
  be* — measured, not hedged. On a five-character Reverse: 1999 goal set that is
  2 411 Activity, p95 1.8 s, and a plan within 2.95% of anything that could
  exist. [ADR 0010](docs/adr/0010-a-plan-is-the-best-provable-in-the-budget.md).
- **It refuses rather than invents.** A material with no source in the patch
  being planned against is named, not costed at zero — including the 45 of 118
  characters whose Insight 2 materials the current snapshot has no source for.
- **It says where its numbers came from.** Every drop rate today is the
  upstream's declared yield rather than a measurement, and the plan says that on
  every solve.

Shops, free income and weekday rotation are modelled. A shop offer is a
purchase capped by its reset, and it is priced by the energy its currency
costs. Fodder, EXP and level gates are not modelled yet. A goal that needs them
is costed without that part, and the gap is tracked rather than hidden.

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
static site with no server that nonetheless **solves per player**: it builds a
linear program from the reader's warehouse and solves it in the browser with
GLPK. Crafts are integer there and stage runs are not. It also imports an
inventory from a screenshot and syncs through Google. This line used to say its
routes were precomputed per patch. That was wrong, and
[docs/prior-art.md](docs/prior-art.md) says how the mistake happened.
[Penguin Statistics](https://penguin-stats.io) — crowdsourced Arknights drop
rates with a real backend, plus [ArkPlanner](https://github.com/penguin-statistics/ArkPlanner)'s
LP farming solver. Both are worth reading closely, and both are one game by
design.

Storm Almanac's bet is integer optimization that explains itself, over estimates
that carry their sample size, with a domain model that is game-agnostic by
construction, all three at once. Solving per player on its own is not a
difference: Kornblume already does it. Read [docs/prior-art.md](docs/prior-art.md) for what each of them
actually does, where this differs deliberately, and the one place their real
data proved our first model wrong.
