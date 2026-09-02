# Storm Almanac

A progression optimizer for live-service games, running on a storage engine and
a consensus layer written from scratch — one product to be used, one substrate
to be understood.

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

> **Bootstrap note:** the Gradle wrapper is not committed yet. With a JDK 21 and
> Gradle 8.10+ on the path, run `cd backend && gradle wrapper` once to generate
> it, then use `./gradlew` from then on.

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

## Prior art, credited

[Kornblume](https://github.com/kachiyo/Kornblume) — Reverse: 1999 planner, a
static site with no server. [Penguin Statistics](https://penguin-stats.io) —
crowdsourced Arknights drop rates with a real backend, plus ArkPlanner's LP
farming solver. Both are worth reading closely, and both are one game by design.
Storm Almanac's bet is server-side integer optimization over crowdsourced
estimates with a domain model that is game-agnostic by construction — all three
at once.
