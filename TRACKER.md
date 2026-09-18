# Storm Almanac — build tracker

**Single source of truth for progress across chat sessions. Read this first,
update it last.** If a session ends without this file reflecting what happened,
the next session starts from a lie.

**History lives in [docs/history/tracker-archive.md](docs/history/tracker-archive.md)** —
the session log, the completed next actions, the closed phases in full, the
answered questions. Nothing was deleted when this file was split on 2026-09-08;
it moved. What stays here is what is still *operative*: age is not the criterion,
being finished with is.

- Source of the plan: [plan.html](plan.html) (13 phases, two tracks).
  [README.md](README.md) is the public face; [CLAUDE.md](CLAUDE.md) is the
  working agreement.
- Last updated: **2026-09-18** (twenty-second session)

---

## Status

- **The launch title is now Punishing: Gray Raven, 2026-09-13 ([D3](#d3--launch-title-swapped-to-punishing-gray-raven-2026-09-13)).**
  Reverse: 1999 moves to second. The reason: Kornblume turned out to solve per
  player, so on R1999 this product overlapped a deployed tool almost feature for
  feature, and `prior-art.md` had said otherwise. **PGR has no data here yet**, and
  its fodder shape is not in the solver, so the next actions are **N27** (read
  PGR) and **N30** (fodder). Everything below this bullet that says "R1999" was
  true when written and still is. It is just no longer the launch.
- **PGR has been read, and it corrected the plan twice** — 2026-09-18,
  [the note](docs/game-facts/punishing-gray-raven-research-disclosure.md). **Both
  banner archetypes are first-hand and complete**, the weapon's feeding numbers are
  read, and **N28's pull-currency chain exists at last**. Two things the *guides*
  said turned out false against the client: PGR's featured rule is a **per-banner
  rate** (70% with a published Calibration guarantee, or 100% with none), not a
  constant; and **PGR events have no shops**, are **not energy-priced**, and pay
  **one-time mission grants** rather than farmable yields. **N30 shrank by two
  features and grew by one**, and the new one does not fit `EnergyMip`. Three engine
  costs fall out: a **floating `hardAt`** drawn per cycle, a **`PityScope` for pool
  type that carries Calibration across pool boundaries** — R1999 clears at event
  end and PGR inherits, so the two published games now disagree on the one question
  that enum exists to answer — and `featured` finally carrying weight. **Block 4,
  the timed authoring pass, was cut by the maintainer**, so the cost of sourcing a
  patch is still unmeasured.
- **Phases 1, 2 and 3 stay closed, criterion and scope.** One line each on the
  [phase board](#track-a--product); the qualifications that matter are in
  [what is still unverified](#what-is-still-unverified).
- **Phase 5 — Gacha engine — is CLOSED on its criterion, 2026-09-12, out of order
  on purpose ([D2](#d2--phase-5-entered-before-phase-4-closed-2026-09-12)).**
  Numbers in the `gacha` row below, and decisions in
  [ADR 0018](docs/adr/0018-the-gacha-engines-answer-one-question-about-one-rarity.md).
  **Nothing calls either engine**, and the income model is **N28**. One R1999 banner
  is first-hand (Q4); PGR's are not.
- **Phase 4 — Frontend v1 and launch — is OPEN**, 2026-09-09.
  **[D1 is reversed](#d1--deployment-deferred-2026-09-02)** — Vercel and Render,
  free tier, still no money. Nothing is deployed: the decision is made, the
  wiring is **B5**.
  **All five screens exist and N25 is closed, 2026-09-11**, driven in a browser
  against a real database; its three debts are paid. **Phase 4's second exit clause
  is served**: a logged-in character page says what that reader is short of, from
  where their roster actually stands. **The first clause is not and cannot be until
  B5** — five strangers cannot complete a plan against something that is not
  deployed. *The data blocker stands:* see the bootstrap bullet below.
- **Provenance is written, enforced and read, and what it says is uncomfortable.**
  [ADR 0016](docs/adr/0016-provenance-is-a-property-of-the-data.md), in full in the
  Provenance row above. **On today's data the catalog pages read "Invented for this
  project — not any real game" or "Nobody recorded where these numbers were read",
  and that is the feature working** — no bundle carries a first-hand fact yet. **The machinery is finished and the reading is not (N27)**: the first
  self-sourced bundle needs somebody to *read the game*, and ADR 0015's integrity
  rule disqualifies an aggregator, a web search and an AI session alike.
  [The loop](docs/game-facts/authoring-a-first-hand-bundle.md).
- **The project is going first-hand on game data**, decided 2026-09-09 —
  [ADR 0015](docs/adr/0015-game-data-is-sourced-first-hand-not-adapted.md),
  superseding 0009 on its conclusion and closing **Q2, Q3, F1 and F2** at once.
  Kornblume is unlicensed. **Nothing is deleted yet and the order matters:** the
  adapter stays as a never-shipped cross-check until a replacement exists, because
  removing it first leaves the project with no real data at all.
  **The cost, measured on patch 3.5:** ~2 700 static catalog facts (91 items, 100
  stages, 50 recipes, 118 arcanists, 37 psychubes) and **595 drop-rate facts**. The
  first half is typing and is mostly additive per patch. **The second half is the
  problem** — see the bootstrap below.
- **The bootstrap problem is the main risk in the project, and N26 confirmed the
  cheap way out is not there.** The optimizer cannot rank a stage without a
  yield, so no drop data means no plan; own drop data means Phase 6, which means
  users, which means a working plan. **The publisher does not disclose stage drop
  rates** — the stage screen grades a reward `Fixed`, `Common` or `Possible` and
  prices only the first, and every published percentage for the other two is
  somebody's crowdsourced sample. Measured on the pinned snapshot: **15 of 779
  drop facts are declared, 764 sampled** — ~2% free. Two real consolations:
  **gacha rates *are* disclosed** (half of **Q4**, one screen's reading, and Phase 5
  closing has raised what it is worth), and **the `Fixed`/`Common`/`Possible` grade
  is itself a free first-hand fact** for all 595 pairs, with nowhere in the model to
  live until Phase 6 consumes it.
  [The drop disclosure note](docs/game-facts/reverse-1999-drop-disclosure.md).
- **Phase 0 stays closed by exception** — deploy deferred by D1 — and its box
  stays unticked, because nothing is deployed.
- **Track B: not started, and gated.** See [the gate](#the-gate).
- **The remote, checked 2026-09-13 (twentieth session) — re-check it, do not
  trust it.** At session start **PR #19 was MERGED** (Phase 5 is on `main`),
  `origin/main` and `origin/dev` were equal, and `dev` had no open PR.
  **[PR #20](https://github.com/kietnt4412/storm_almanac/pull/20) (N5) was then
  opened and is green on `ef51844`, run `34731880389`** — and **every test task in
  that run was `FROM-CACHE`**, correctly, because only YAML changed. It proves the
  new actions' mechanics, not the suite under them. **Run `34746241622` on `85d0538` did execute `:modules:gacha:test`**, green.
  **Counting PASSED lines in a CI log undercounts** — read the task outcomes and
  the totals. **The trap:** CI runs on `pull_request` and on push to `main` only,
  so **a push to `dev` with no open PR runs nothing, silently.** Every merge re-arms
  it; five times now, each caught by looking. **Check `gh pr list` and open the PR
  before trusting a push to `dev`.** It is a fact here rather than a next action
  because a "merge PR #n" line is stale before it is read.
- **The optimizer has been asked a question by something other than a test.**
  What has *not* happened is a real OAuth exchange: no provider is configured and
  no client secret exists, so login is installed only when one is. The
  development sign-in does not change that and is not meant to — it builds a
  principal directly, and ADR 0017's reversal trigger is deleting it the moment a
  real provider works locally.
- **`backend/Dockerfile`'s COPY list is a second copy of the module list and
  drifts in silence.** It had omitted `adapters/` since Phase 1 — every image
  build failing in six seconds while the table below called it verified — and was
  fixed 2026-09-09. **Anything added beside `modules`, `adapters`, `substrate`,
  `app` needs a line there**, and Render builds from this file, so it is **B5**'s
  path.
- **The time axis still has no real data to eat**, checked rather than assumed —
  and the reason is better than "the upstream is silent". See
  [the unverified list](#what-is-still-unverified) before treating it as a gap.

### Two standing caveats, read them every session

1. **The strongest tests in this repository are ones CI does not run.** Upstream
   data is fetched and never committed
   ([ADR 0009](docs/adr/0009-upstream-data-is-fetched-never-vendored.md)), so
   `RealUpstreamPatchTest`, `RealUpstreamPlanTest` and `CommunityBenchmarkTest`
   skip on the runner — 16 of the 257 tests. **Every performance number and every
   comparison with an outside answer in this file comes from a test the pipeline
   does not run.** Run `backend/tools/fetch-upstream.sh` before trusting a green
   build to mean the pipeline handles real data.
2. **A green build says nothing about whether the data going into it is the data
   the upstream actually publishes.** The adapter spent five sessions reading a
   stale stage table with two thirds of the game missing, and everything
   downstream was green the whole time. What caught it was going to look for
   somebody else's answer.

---

## How to use this file

1. **At session start:** read *Status*, *Current state*, *Next actions* and
   *Open questions*. Do not re-derive them from the code.
2. **During the session:** tick boxes as work lands, not when it is planned, and
   write the tracker line in the same breath as the push. **Never write "merge
   PR #n" as a next action** — merging is a click that happens between sessions,
   so the line is stale before it is read. Four sessions running opened on one.
   The state of the remote goes in *Status*, checked rather than trusted.
3. **At session end:** update *Last updated* and *Current state*, prune *Next
   actions*, and **append the session entry to
   [the archive](docs/history/tracker-archive.md#session-log)** — then add one
   line to the [index](#session-log-index) here.

Rules that keep this file honest:

- A box is ticked only when its exit criterion is met, not when the code exists.
- "Done" means tested and committed. "Scaffolded" means the shape exists and the
  behaviour does not — say so explicitly.
- Never delete a session-log entry. Supersede it, and say what the older one got
  wrong.
- **When an item closes, move it to the archive rather than striking it through
  in place.** This file reached 2 412 lines and 138 KB by keeping every corpse in
  the room, and a handoff document nobody reads is worse than no handoff
  document. **Keep it under about 550 lines** — roughly what a session can read
  before starting work. If a session adds more than it removes, it has moved the
  problem rather than done the work.
  **Measure it with `wc -l` rather than carrying the last number forward** — the
  drift has twice run in the flattering direction, by seven lines once.
  **Still well over: 747 on 2026-09-11 → 742 on 2026-09-12 (eighteenth) → 749 on
  2026-09-12 (nineteenth), measured.** The nineteenth cut ten blocks — the
  *Current state* rows for Phase 1, the parser adapter and provenance that three
  sessions had flinched from, the Status bullets for the offline outbox and N4 that
  a table row already carried, two closed next actions, the benchmark
  re-argument, D1's restatement and E4's
  ([the five largest are in the archive](docs/history/tracker-archive.md#rows-compressed-out-of-current-state-2026-09-12-nineteenth-session)) —
  **and still ended seven lines up, because it closed a phase, opened a deviation and
  added an action in the same session.** By this file's own rule that is moving the
  problem, and the flat number is the only honest way to say so.
  **→ 769 on 2026-09-13 (twentieth), measured after this entry was written** — N5 out, the Phase board's closed
  entries cut to one line each, the remote bullet halved. **Still ~170 over.**
  **→ 790 on 2026-09-18 (twenty-second), measured.** Up 21, and the compression
  this session bought did not show: the *Game data API* and *Frontend* rows — the
  candidates the line above named — **were each a single very long line**, so
  cutting them to a summary and an archive link removed ~2 700 characters and
  **zero lines**. **The metric is not measuring the thing.** A row nobody can read
  in one breath costs a session as much as twenty short ones, and `wc -l` cannot
  see it; bytes fell 71 045 → 70 369 while lines held. Keep measuring lines, but
  **read the long rows as their own debt** — the remaining ones are in
  *What is still unverified*, whose closed-phase items could become one line and a
  link, as its last bullet already does for eight of them.

---

## Current state

**What exists:** a game data pipeline that works end to end, an API that serves it,
a real game's data going through all of it, an optimizer that turns that data into
a plan and a reason to believe the plan, an account that can own one and be refused
somebody else's, two of that account's devices that can edit what it owns without
deleting each other's work, five screens a browser has driven, a page that says
where its numbers were read, an app that still renders with its server switched
off — and, since this session, two gacha engines that agree with each other about
both published games.

**The load-bearing claim — and it is a number to re-earn.** On **nine** benchmark
materials the cheapest stage this project computes is the one a published community
guide tells players to farm, and the optimizer's plan for a real goal set costs
**3 880 Activity against the guide's 4 017** — which does not even cover the whole
demand. Nine and not five because a yield carries its sample size
([ADR 0011](docs/adr/0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md)).
**Every one of those numbers is computed from Kornblume-fed inputs**, which
[ADR 0015](docs/adr/0015-game-data-is-sourced-first-hand-not-adapted.md) says the
product will not ship — so until a self-sourced bundle reproduces them they are
evidence about somebody else's numbers run through our solver. **The method
survives untouched**, because the guide it compares against is a separate published
artifact: a claim to re-earn, not a test to delete.
See [the benchmark](docs/benchmarks/reverse-1999-community-answers.md).

Still true from earlier phases: **the solver says how much it does not know**
([ADR 0010](docs/adr/0010-a-plan-is-the-best-provable-in-the-budget.md)), **the
pipeline has met somebody else's data and held**, and **both of Phase 1's exit
questions are answered over HTTP rather than by a test calling a repository**.

Toolchain on this machine: JDK 21.0.12 (Temurin), Gradle **9.6.0** via the
committed wrapper. Remote is HTTPS at `github.com/kietnt4412/storm_almanac`.

| Area | State | The one thing to know |
|------|-------|-----------------------|
| Backend build | **Green** | **329 tests** (324 last run in full, plus five in `gacha` run 2026-09-13), 0 skipped locally with snapshots present; **313 on CI**, because the same 16 snapshot-gated ones skip. `:app:test` depends on `:app:bootJar` — `DeployableJarTest` reads the artifact. Test tasks set `api.version=1.44` — [E2](#e2--docker-engine-29-refuses-testcontainers-api-version) |
| CI workflow | **Green, no warnings, on Node 24** | Run `34731880389` on `ef51844` ([PR #20](https://github.com/kietnt4412/storm_almanac/pull/20), N5): zero annotations. **Test results cached from #19's run `34695206362`**, where the suite last *executed*: 16 skipped, exactly the three snapshot-gated classes (`RealUpstreamPlanTest` 8, `CommunityBenchmarkTest` 5, `RealUpstreamPatchTest` 3) — a pass there would mean a snapshot had been committed by accident. **`gradle/actions` is held at v5**: v6 needs Gradle's Terms of Use accepted, which is the maintainer's call, and v5 has been frozen since 2026-02-23 |
| Game data pipeline (Phase 1) | **Closed and stable** | `V2`–`V4`, 28 tables, seven invariants on two real patches, a round trip pinning parser against writer (ADR 0008), publishing as a human approval. `Drop` carries `sampledRuns`, where 0 means *declared*. [In full in the archive](docs/history/tracker-archive.md#closed-phases-in-full) |
| Parser adapters | **One, and demoted to a cross-check by [ADR 0015](docs/adr/0015-game-data-is-sourced-first-hand-not-adapted.md)** | `:adapters:reverse-1999`, 25 tests. **It hard-codes `THIRD_PARTY` and so fails a plain `publish`** — there is no call site to launder data through. Kept rather than deleted because diffing the first self-sourced bundle against it is worth more than it ever was as a source. [The prose it used to carry](docs/history/tracker-archive.md#rows-compressed-out-of-current-state-2026-09-12-nineteenth-session) |
| Provenance | **Done — written, enforced, and read** | [ADR 0016](docs/adr/0016-provenance-is-a-property-of-the-data.md). `V7` stores one row per *declared* fact; `publish` refuses a version that is not first-hand and names the facts, and the exception is a word the operator types. **`ProvenanceRepository` is a second port, not a field on `GameDefinition`** — so the solver cannot see where a number came from and cannot be made to prefer one. Silence is `UNRECORDED`, which parses and cannot publish. [The prose it used to carry](docs/history/tracker-archive.md#rows-compressed-out-of-current-state-2026-09-12-nineteenth-session) |
| Game data API | **Served and verified** | Seven game-data routes plus health, version-pinnable, every response carrying its version, attribution and — on the four that carry facts — **where each fact was read**. 15 HTTP tests plus a hand check against `docker compose up`. [In full in the archive](docs/history/tracker-archive.md#rows-compressed-out-of-current-state-2026-09-18-twenty-second-session) |
| Demand resolution | **Done** | Goals + roster + upgrade graph → a demand vector, walking the DAG backwards. Refuses by name rather than guessing: unreachable states, unknown entities, probabilistic goals, ambiguous routes |
| The MIP (`EnergyMip`) | **Done for stages, crafts and rewards** | ojAlgo, integer runs, inventory subtracted, every variable bounded — the bound is what makes a real patch solvable. **No shops and no fodder**; items sourced only from those are refused by name |
| The time axis | **Done — as a scalar, not an index** | [ADR 0013](docs/adr/0013-the-horizon-is-a-scalar-not-an-index.md). An energy cap, cadence counts, and rotation as a capacity shared over *subsets* of distinct weekday restrictions. **No variable is indexed by day**, which is why the p95 survived: 1 805 → **1 807 ms**. Proven on the fixture; the real upstream declares nothing time-varying |
| Yield source (`YieldTable`) | **Done, and it carries the sample** | Declared yields, discounted to the lower end of a 95% `PoissonRateInterval` wherever a sample size exists (ADR 0011). No sample means used as declared |
| `Optimizer` (`MipOptimizer`) | **Done — least energy** | Pins the plan to its version, fingerprints the request (`SolveKey`), explains itself with shadow prices by re-solve. Budget split between search and explanation — ADR 0010 |
| Objectives | **Two searches over one model** | `LEAST_ENERGY` takes the whole horizon; `FEWEST_DAYS` binary-searches for the shortest one that fits, then solves for least energy inside it. On the fixture, Insight 1 is **0 energy over 28 days or 370 over 2**. They still coincide wherever a game's data has nothing on a cadence, and the plan says which case it is |
| Solve caching | **Done, in-process** | `SolveCache` is get and put over a `SolveKey` and has **no invalidation method** — a patch is a different key, not a stale entry. On the real 3.5 patch a repeat question goes **1 806 ms → 2 ms**. Not Redis, and [ADR 0012](docs/adr/0012-the-solve-cache-is-in-process-until-there-is-a-second-node.md) says why |
| `SolveCoordinator` | **Single-node, done** | One execution per idempotency key however submits interleave; a ticket to poll; an honest queue depth. **Does not survive a restart, deliberately** — making it durable here would answer the question Phase 9 exists to ask |
| Community benchmark | **Done, and now ADR 0011's regression test** | Twenty published claims against what this model computes, ranked on the yields the **solver** uses with the raw ranking printed beside them |
| Statistics primitives | **Done** | `WilsonInterval` (6 tests) for "did it drop", `PoissonRateInterval` (7) for "how many dropped", `PityRule` (9) against both games' published rates |
| `identity` / `player` schema | **Applied and round-tripped** | `V5` (6 tables). Four decisions in its header: **no FK crosses a schema**, nothing points into `gamedata` (version-scoped rows, an inventory outlives a patch), an identity is `(provider, subject)` and **never an email**, and absent means zero with a `CHECK` to keep the two representations from diverging. `V6` adds the two sync tables |
| Sign-in | **Done, never exchanged a token** | Account created while the principal is built, not in a success handler. Everything provider-specific is `SignIn.from` — a pure function; OIDC says `sub`, Discord says `id`. **`oauth2Login` installs only when a provider is configured**, because no client secret exists (D1) and a blank client id fails a `ClientRegistration` outright. The deny is not conditional. CSRF is `SecurityConfig.browserCsrf`, shared with the development chain: the token is resolved eagerly, because the deferred default never issues the cookie a page needs to write |
| Player API | **Served and authorized** | Everything under `/api/me`, so **no route takes an account id** and none's authorization can be forgotten. `OwnedProfiles.require` is the one check; another account's profile is **404, not 403**, so an id is not an enumeration oracle. PUT replaces a whole aggregate; PATCH merges per key |
| Offline sync | **Done on both sides — inventory and roster** | `PATCH`, last-write-wins per key ([ADR 0014](docs/adr/0014-sync-is-last-write-wins-per-key-against-a-clock-that-outlives-the-value.md)). The clock is a **table, not a column**, because a removal deletes its row and a tombstone cannot live on the row it outlives; a `PUT` also writes a whole-aggregate **watermark**, because a full save speaks for keys that have never existed and no per-key row can say that. Client timestamps, clamped to the server's clock. The response names the keys that lost. **Goals have no PATCH** — an ordered list has no per-key merge, which is also why the client saves them only while connected rather than queueing a whole-list write that would clobber a second device. **The client half landed with N25**: a persisted per-profile, per-key outbox stamped at the moment of typing, flushed debounced over PATCH, surfacing the keys that lost. Proven in a browser against a refusing network and against a deliberately stale edit |
| Plan route | **Done — Phase 3's criterion** | `POST /api/me/profiles/{id}/plan`, synchronous because a two-second budget is a promise the optimizer can keep. Goals, inventory and roster come from Postgres; the body carries only `energyPerDay` and `horizonDays`. `?version=N` pins the patch |
| Bean wiring | **`Optimizer` and `SolveCache` are beans; `SolveCoordinator` is not** | Registered now because they finally sit on a path a real request takes — which is what N15's refusal was waiting for. The coordinator hands back a ticket for a solve that does not fit the synchronous budget, and there is no asynchronous surface for a ticket to be useful on |
| Architecture tests | **Passing — three of them** | `ModuleBoundaryTest` (Track B layers optional until they exist), `GameAgnosticismTest` (source scan over planner/gacha/stats) and, since 2026-09-12, `EntityKindBoundaryTest` — ADR 0007's own open gap, closed: `Entity.kind` may be read in `gamedata`, `api` and `adapters` and nowhere else. Bytecode rather than a source scan because a grep cannot tell `entity.kind()` from `change.kind()`, and an allowlist rather than a ban on three modules because a denylist passes vacuously the moment a fourth module grows a read |
| Docker Compose | **Repaired 2026-09-09, and no image has ever been built end to end** | The Dockerfile had never learned about `adapters/`; the fixed tree builds the jar locally and that jar contains no development sign-in. **The image itself is still unproven** — the in-container Gradle download was abandoned at 10% after twenty minutes, so `up --build` has not completed. See the COPY-list warning in *Status* |
| Development sign-in | **Done, and absent from the artifact** | [ADR 0017](docs/adr/0017-the-development-sign-in-is-absent-from-the-artifact.md). `:modules:identity-dev` is `testAndDevelopmentOnly` on `:app` — on `bootRun` and the test classpath, **excluded from `bootJar`**, so no property or profile can reach it. `GET /dev/sign-in?as=<name>` mints an ordinary `AuthenticatedAccount` through the same `upsertFromOidc` the OAuth services use; its filter chain lives in that module, so `SecurityConfig` has no hook for it. `DeployableJarTest` opens the jar and proves the absence on every build. The frontend picks its sign-in URL behind `import.meta.env.DEV`, and the production bundle was checked for the string: **zero occurrences** |
| Frontend | **Five screens, driven in a browser, and 15 tests** | Inventory editor, goal picker, plan view (with **every one of the solver's notes**), catalog browse and search, and the character page with the **personalized overlay**. Local is same-origin via the Vite proxy; the development sign-in URL is behind `import.meta.env.DEV` and absent from a production bundle. **Never rendered PGR** — every screen was built against R1999. [In full in the archive](docs/history/tracker-archive.md#rows-compressed-out-of-current-state-2026-09-18-twenty-second-session) |
| `gacha` — the two engines | **Done — Phase 5's criterion, and nothing calls them** | [ADR 0018](docs/adr/0018-the-gacha-engines-answer-one-question-about-one-rarity.md). `MarkovBannerEngine` is an exact forward chain over `(pulls since hit, losses carried, copies held)` reading the mass that **never arrived**, so a wall is exactly 1.0 rather than 0.9999999999999895; `MonteCarloBannerEngine` is 500 000 seeded trials on virtual threads, deterministic because the generators are split before any task starts and counts are summed as integers in chunk order. `PullModel` is the validated slice both share, so they refuse the same banners for the same reasons. **51 tests. Worst gap 0.110 points over 96 questions (eight banners since the first-hand one joined), at 2.22 standard errors** — was 84 and 1.84 at closing. Only the headline rarity is modelled, and `PullResult` carries no `Rarity` — the model pins no other rate while pity is active, so one would be invented per game |
| `gacha` — the income model | **Interface only, and it cannot be written yet** | `projectedPulls` needs to know which item is pull currency and what a pull costs in it. **Neither `BannerModel` nor the `banner` table declares either**, so the question has nothing to compute from — **N28** |
| Track B | **Empty** | Package docs. Track B is [gated](#the-gate) |

### What is still unverified

Be precise about this, because the temptation is to read "build green" as "it
works". It does not mean that:

- **CI does not run the tests that matter most, and never will as things stand.**
  Three classes touch data this project did not author and all three skip on the
  runner — 16 tests. They are the ones that would catch an upstream-shape
  surprise, **and the only evidence the optimizer is fast enough or right about
  anything real.** The stale stage table that cost five sessions of plans would
  not have been caught by any test CI runs, and was not caught by any test at
  all — a person went looking.
- **The OAuth exchange has never run.** No principal has ever come from a real
  provider: no client id, no secret, no redirect followed, because there is no
  URL to register one against (D1). Tested: the account, the principal, and every
  authorization rule around them, now including over a socket. Untested: the
  token exchange. **The development sign-in does not narrow this by one inch** —
  it builds a principal directly — and reading N24 as "sign-in works" is exactly
  the misreading ADR 0017 is written to prevent. The first deployment is where
  this stops being theoretical.
- **Most authenticated tests still go through MockMvc**, which is a deliberate
  step down from real HTTP and no longer a forced one. `DevSignInTest` is the
  exception and the first of its kind — a real port, a hand-kept cookie jar, no
  security post-processor — so the servlet container is now exercised on the
  signed-in path it never was. `PlanFromStoredStateTest` and `OfflineSyncTest`
  have not been moved: they could be now, and until they are, **the layer that
  caught Phase 0's 401 is covered for sign-in and profiles and not for the plan
  or the merge.**
- **No two real devices have ever synced, but one device has now lost to one.**
  N23's every scenario was one MockMvc request following another inside one JVM.
  N25's outbox adds a real second clock: a browser held edits through a refusing
  network, flushed them on reconnect, and an hour-old edit lost to a newer value
  and said so. **What is still argued rather than measured** is the concurrency
  the merge SQL is actually shaped for — two requests interleaving on the same
  key at the same instant — and **two genuinely separate devices have still never
  been in the same account at once**.
- **A merge publishes nothing.** Nothing downstream can react to a synced edit,
  so a cached plan is not invalidated when the inventory under it moves. Harmless
  today because nothing caches on player state across requests; **read this
  before Phase 6 makes anything depend on an inventory being current.**
- **`SolveCoordinator` is still not wired to anything**, deliberately; see the
  table row above.
- **Nothing has ever asked the gacha engines a question on behalf of a player.**
  Phase 5's criterion is a proof about a model — two methods agreeing, and both
  reproducing published figures — and it is met. What no test can tell you is
  whether the answer is the one a player wants: there is no route, no screen and no
  bean, and the `PityState` a real reader would be asked for is not stored anywhere
  (`player` has an inventory, a roster and goals, and no pity counters). **So
  "probability of guaranteeing her" is computable and unanswerable**, and the gap
  is a schema and a screen rather than an engine.
- **One gacha banner is first-hand and six are not (Q4).** `PublishedRatesTest.Disclosed`
  is evidence about the game. Every other fixture proves only that the model
  reproduces the figures it was *given*. **Multi-copy "can I guarantee her" answers
  are too pessimistic**, because 200 Cassettes of the Lost buy a copy and nothing
  models that. At two copies the engines say 280 pulls and the truth is 200; one
  copy is unaffected — see **N28**.
- **Nothing has measured whether the cache is worth having in production**, only
  that a hit is 900× cheaper than a solve. Hit *rate* depends on whether two
  players ever ask the same question, which needs users. The counters are there
  so that this stays a measurement rather than a belief.
- **Nothing is deployed.** By decision — [D1](#d1--deployment-deferred-2026-09-02).
  The `deploy` job is `if: false` and there is no URL to smoke.
- **The PWA has been loaded with its origin server dead, and an offline *write
  path* has not.** The outbox was proven against a refusing network, not against
  a dead origin on a cold start. The load itself and how it was tested are
  [in the archive](docs/history/tracker-archive.md#closed-out-of-what-is-still-unverified-2026-09-12-eighteenth-session);
  reproducing it needs the **built** bundle (`web-built`, port 4173), because the
  dev server has no worker worth the name.
- **The frontend has 15 tests, and one class of defect they will never catch.**
  Vitest and Testing Library in jsdom, in CI, argued beside the config in
  `vite.config.ts`. Of the four defects a browser found, three were behaviour and a
  component test catches them; the fourth was `display: block` folding a table
  header, which **jsdom will never catch because it computes no layout**. So:
  behaviour is the pipeline's, appearance is a person's, and **driving a browser
  before shipping a screen is still required**.
- **A new page and an old API do not deploy at the same instant, and the first
  time that happened the page rendered as nothing.** A freshly built bundle read a
  response from a backend that predated `sourcing` and threw before React painted
  anything (2026-09-11). Fixed, and the wire types now mark the field optional so
  the compiler points at every call site that has to cope. **This is B5's problem
  in miniature** — Vercel and Render are separate hosts and every release has a
  window like it — and the lesson is wider than one field: a client that treats a
  new response field as guaranteed blanks its own page on every deploy.
- **Nothing has ever run against a jar built from a Dockerfile that works.** The
  image build was repaired this session and the compose stack has not been stood
  up end to end since. The **absence** of the development sign-in from that jar
  *is* proven, by `DeployableJarTest`, on every build.
- **Nothing has ever called the API under load.** Every request loads a whole
  version — fifteen queries — a deliberate deferral written into
  `GameDataReadModel`'s javadoc. The number to beat does not exist yet.
- **Eight qualifications of the closed phases are
  [in the archive](docs/history/tracker-archive.md#qualifications-moved-out-of-the-live-tracker-2026-09-11-seventeenth-session)**
  — the benchmark being one guide, the two large community disagreements, the three
  shapes the model cannot express, what the adapter does not convert, and the rest.
  All still true. **Read them before re-opening Phase 1, 2 or 11.** (One has
  expired: `gacha` had no behaviour, and now does.)
- **No bundle carries a first-hand fact yet.** The first first-hand reading, the
  summon rules on 2026-09-13, is a test fixture and a note, not bundle data. The
  only bundles that pass ADR 0016's gate are the synthetic fixtures, which pass by
  declaring `AUTHORED_FIXTURE`. **Every Reverse: 1999 catalog and drop number in
  this file still comes from Kornblume** (**N27**).
  (Provenance stopped being write-only on 2026-09-11;
  [what it says and why that is the feature working](docs/history/tracker-archive.md#closed-out-of-what-is-still-unverified-2026-09-12-eighteenth-session)
  is in the archive. It becomes a claim worth making when **N27** puts a real
  reading behind it.)
---

## Next actions

Ordered. Completed ones move to
[the archive](docs/history/tracker-archive.md#completed-next-actions).

- [ ] **N27 — Author the first PGR bundle. The reading is done; the typing is not.**
      Blocks 1–3 landed 2026-09-18, off the Global client, and are in
      [the PGR note](docs/game-facts/punishing-gray-raven-research-disclosure.md):
      **both banner archetypes in full** (base rate, guarantee, counter, featured
      rate, inheritance), the **weapon's feeding numbers**, and the **event
      economy**. **Block 4, the timed authoring pass, was cut by the maintainer** —
      so the cost of sourcing a patch stays unmeasured, and any later claim that a
      patch is affordable to source is an estimate with nothing behind it.
      **Still unread:** the **Memory** system entirely, and the costs behind the
      character's four axes (Train, Evolve, Awaken, Phylotree).
      **Still to do:** turn the note into canonical JSON and run *preview, ingest,
      publish* ([the loop](docs/game-facts/authoring-a-first-hand-bundle.md)).
      **Ask how each value was read before recording it** — a relayed web-search
      answer and a screen reading look identical in chat; it caught one error this
      session. R1999's pass moves to Phase 11.
- [ ] **N30 — Put fodder and the event shape into the solver (D3). Shops are out.**
      **Rescoped 2026-09-18 against N27's reading, and it moved both ways.**
      **Dropped — the shop half**, whose premise was guide-derived and is false:
      **PGR events have no shops at all**, on every event and not just the one read,
      so what `EnergyMip` refuses today it can go on refusing. Also dropped,
      **probabilistic goals**: Resonance is **picked, not rolled**.
      **Confirmed — `Fodder`**, and it now has numbers: a 4★ unit is **300 EXP**
      whether it is a material *or a weapon*, so one rule covers both.
      **Added, and it is the larger job:** a PGR event is **a one-time grant behind
      a capability gate inside a closing window** — no energy cost, no repetition,
      no yield. `EnergyMip`'s "run stage N times for yield Y" cannot express it and
      `Reward`'s cadence cannot either, and **`LEAST_ENERGY` is not even the right
      question — `FEWEST_DAYS` against `Availability.closesAt` is.** Decide
      deliberately that *"can this player clear stage N"* is an input the reader
      supplies, not something the solver derives.
- [ ] **N28 — Give a banner a pull currency and a price, and then write
      `IncomeModel`. Unblocked 2026-09-18 — the numbers exist now.** PGR's chain was
      read off the client: **1 pull = 250 Event Construct R&D Tickets** (2500 for
      ten, no discount), **1 Black Card = 1 ticket**, **1 Rainbow Card = 10 Black**,
      **119 Rainbow = $19.99** — about $4.20 a pull
      ([the note](docs/game-facts/punishing-gray-raven-research-disclosure.md)).
      Phase 5's unbuilt scope, and it is a schema change rather
      than a decision: `projectedPulls(profile, date)` has to know which item is
      pull currency and what a pull costs in it, and **neither `BannerModel` nor
      `gamedata.banner` declares either**. So "she arrives in 40 days, can I
      guarantee her" — the feature the plan calls the one nobody ships well — has
      nothing to compute from, and no amount of engine work changes that. Costs a
      bundle field, parser, writer, a migration and the JDBC round trip, exactly
      like **N20**. **Do it with a game whose income sources are actually
      ingested**: the accrual side reads `Reward` cadences, and nothing ingested
      has ever been checked for them. **Two more fields belong in the same change**,
      both found on R1999 2026-09-13 ([the note](docs/game-facts/reverse-1999-summon-disclosure.md)):
      a **shop exchange for the featured unit**, since cassettes cut six copies from
      840 pulls to 560, and a **cap on copies** at the most that changes anything,
      since `copies` is unbounded today. **Read whether PGR has either before
      designing them**, now that PGR launches first.
- [ ] **N20 — Put the game's day boundary on the game, not in the planner.**
      `EnergyMip.matchingDays` reads weekdays in **UTC** — a game assumption in a
      game-agnostic module. R1999 Global rolls over at **05:00 UTC−5, weekly
      Monday**. Inert today because nothing ingested rotates, so it is deferred on
      the same reasoning that kept unused beans out of N15. **It stops being inert
      with the first game that rotates, which is PGR and so now the launch** (D3),
      not Phase 11. Costs a bundle field, parser, writer, a migration and the JDBC
      round trip — do it *with* that game, not speculatively.
- [ ] **N18 — Put drop estimates into `SolveKey` in the same change that first
      publishes one.** Left out because nothing publishes any. The moment Phase 6
      does, a plan cached against yesterday's rates is served as today's — the one
      staleness bug the key's design cannot catch, and it is silent. **In the
      Phase 6 change itself, not after it.** `SolveKey` and `SolveCache` both say
      so in their javadoc; this line is so a session reads it before starting.
- [ ] **N19 — Write `RedisSolveCache` when there is a second node.** Deferred by
      [ADR 0012](docs/adr/0012-the-solve-cache-is-in-process-until-there-is-a-second-node.md);
      the reversal trigger is a second process that can serve the same profile.
      **Also owed before any benchmark against the Phase 8 replicated KV**,
      regardless of node count: a replicated cache measured against an in-process
      map is measuring the network, and ADR 0003 forbids a comparison shaped to
      flatter the hand-built side.
- [ ] **B5 — Wire the real deploy: Vercel and Render.** No longer deferred —
      [D1 is reversed](#d1--deployment-deferred-2026-09-02) and the hosts are chosen. The
      `deploy` job is still `if: false` and stays that way until there is a real
      URL to smoke, which is what this action produces. **Settle the two-origin
      question first**, in D1's reversal note: a Vercel rewrite of `/api/*` to
      Render keeps the same-origin session, CSRF and OAuth redirect the backend
      was built around; two real origins do not. **This is also what unblocks the
      OAuth exchange** — a provider registration needs a redirect URI, and a
      redirect URI needs this. **N24 is done and did not close it**: the sign-in
      it delivered is a development one, and
      [ADR 0017](docs/adr/0017-the-development-sign-in-is-absent-from-the-artifact.md)'s
      reversal trigger is deleting `:modules:identity-dev` once a real provider
      works locally. **This action ends with that module gone**, not merely with
      a URL. It is also where the image build stops being theoretical — Render
      builds from `backend/Dockerfile`, which had never copied `adapters/` and
      was repaired 2026-09-09.

---

## Phase board

Exit criteria are copied from the plan verbatim. Do not start a phase until the
previous one's criterion is met. The full "Landed" record for closed phases is in
[the archive](docs/history/tracker-archive.md#closed-phases-in-full).

### Track A — product

Phases 0 to 3 are closed; what stays here is one line each and the qualification
a session would be wrong not to read.

- [ ] **Phase 0 · Ground** — **closed by exception 2026-09-05, box deliberately
      unticked.** Everything landed except the deploy.
      **Not met as written** — [D1](#d1--deployment-deferred-2026-09-02). The box
      gets ticked when, and only when, a real URL answers 200.
- [x] **Phase 1 · Game data foundation** — closed 2026-09-06, CI-confirmed (N10).
- [x] **Phase 2 · Optimizer core** — closed 2026-09-08. **Stopped by its budget, not finished by it** (ADR 0010).
- [x] **Phase 3 · Identity and player state** — closed 2026-09-08; OAuth never exchanged, no two real devices synced.

- [ ] **Phase 4 · Frontend v1 — and launch** — 2.5 weeks — **OPEN 2026-09-09.**
      Landed so far: the app is served, hosting is decided (Vercel + Render, D1
      reversed), a browser has signed in (N24, ADR 0017), and **N25 is closed** —
      the five screens driven in a browser including the overlay, which is the
      second half of the exit below; provenance read back onto the page; the PWA
      loaded with its server killed; and a frontend test suite in CI. **Nothing
      is deployed, so the first half of the exit is untouched.**
      **Since 2026-09-13 the launch title is Punishing: Gray Raven ([D3](#d3--launch-title-swapped-to-punishing-gray-raven-2026-09-13))**,
      so B5 is no longer the only thing in the way. Strangers cannot plan PGR
      without a PGR bundle (**N27**) and fodder in the solver (**N30**). Every
      screen above was built and driven against R1999 data and has never rendered
      PGR.
      Inventory editor built for fast bulk entry, goal picker, plan view with
      per-stage breakdown, offline PWA. Plus catalog browse and search with the
      personalized overlay on every character page — that overlay is the whole
      argument for having a catalog, so it ships *with* it, not after. Launch
      publicly at the end even if it is ugly.
      **Exit:** five strangers complete a plan without asking for help, and a
      logged-in character page shows what that reader is short of.

- [x] **Phase 5 · Gacha engine** — closed 2026-09-12 out of order ([D2](#d2--phase-5-entered-before-phase-4-closed-2026-09-12)); **one banner first-hand (Q4)**, income model and shop exchange are **N28**.

- [ ] **Phase 6 · Drop statistics on Postgres** — 1.5 weeks
      Report submission, Wilson intervals, provenance, abuse controls, estimates
      feeding the optimizer. Built on the boring implementation first — this is
      the interface Track B later swaps.
      **Read before tightening any rule over data already published:** a version
      is immutable and the rules for reading one are not, so a published snapshot
      can stop being loadable without anything having touched it. It has already
      happened here, and the shape of it is worth more than the row —
      [the account](docs/history/tracker-archive.md#a-published-version-that-stopped-being-readable).
      **Exit:** a community-derived estimate supersedes a seeded one in a live plan.

### The gate

> **Track B starts only when the product is publicly deployed with real users and
> real traffic.** If Phase 4 has not landed, go back and land it. Infrastructure
> built against imagined requirements is a toy; infrastructure built against six
> weeks of your own production traffic is engineering.

**Gate status: CLOSED, and now structurally so — see
[D1](#d1--deployment-deferred-2026-09-02).** Nothing is deployed and nothing is
scheduled to be before Phase 4. Do not open `almanac-store`. When Phase 7 comes
round, re-read D1 and decide deliberately whether Track B on synthetic workloads
is still worth building.

### Track B — substrate

Scope for each is in [plan.html](plan.html); what matters here is the criterion
that says it is finished, and the one architectural note that shapes the phase.
**None of these may start before the gate opens**, which is why they are a table
rather than the paragraphs the live phases get.

| | Phase | Shape | **Exit** |
|---|---|---|---|
| [ ] | **7 · almanac-store** (LSM), 3w | Behind `DropReportStore`, alongside the Postgres one | Crash-consistency fuzzing survives 10k randomized kills; benchmark vs Postgres published — **including if Postgres wins** |
| [ ] | **8 · almanac-raft** (consensus), 3w | Exposed first as a replicated KV, so it is testable before anything depends on it | 5-node cluster survives repeated leader kills and partitions with no divergent log |
| [ ] | **9 · Solver cluster**, 2w | Replicated job log, leased work, idempotent completion, results over WebSocket | Kill any node mid-solve — no lost solves, no duplicated solves, throughput recorded |
| [ ] | **10 · Chaos harness**, 1.5w | Partitions, pauses, kills, disk corruption; linearizability checking; failing seeds saved as regression tests | Nightly suite green for 7 consecutive nights, **and one real bug found and written up** |

### Track A — closing

| | Phase | Shape | **Exit** |
|---|---|---|---|
| [ ] | **11 · Reverse: 1999 as the second title**, 2w — *was Punishing: Gray Raven until [D3](#d3--launch-title-swapped-to-punishing-gray-raven-2026-09-13)* | R1999 already has most of what Phase 11 used to cost PGR: an adapter kept as a cross-check, two imported patches, the community benchmark, and one first-hand banner. What it still costs is **first-hand sourcing of the catalog** (ADR 0015) and its own day boundary. Fodder, probabilistic goals and **the time axis's first real data** all moved to the launch with PGR ([why the time axis](docs/history/tracker-archive.md#qualifications-moved-out-of-the-live-tracker-2026-09-11-seventeenth-session)) | R1999 live with **zero game-specific code** in `planner`, `gacha` or `stats` added after PGR launched — and the diff to prove it |
| [ ] | **12 · Hardening and the writeups**, 1w | Tracing, alerting, a backup actually restored from, a load test with published numbers, pre-rendered catalog pages | Restore drill completed from a real backup; catalog pages indexed; three writeups published — the storage benchmark, the consensus verification, the multi-game diff |

---

## Cut list, in order

Consult this before descoping anything, and record in the session log if a cut
is taken.

1. **Cut first:** Phase 10 as a separate phase — fold minimal fault injection
   into 8 and 9. This loses the strongest evidence, so cut only under real pressure.
2. **Cut second:** Phase 9. Keep `almanac-raft` as a verified standalone
   replicated KV and leave the solver single-node.
3. **Cut third:** Phase 11's real second game — but still prove the abstraction
   against a synthetic test game. That proof is the product thesis.
4. **Never cut:** the Phase 4 public launch, the Phase 2 optimizer, and
   completing at least one of Phase 7 or 8 properly. Half an LSM tree and half a
   Raft is worth nothing; one finished engine is worth a great deal.

---

## Invariants — do not violate without an ADR

**They live in [CLAUDE.md](CLAUDE.md#non-negotiables) and are not repeated here.**
That file is read at the start of every session by definition, so a second copy
is a second thing to keep in sync and a place for the two to disagree. Two of the
six are enforced by tests — `GameAgnosticismTest` and `ModuleBoundaryTest` — and
those are the ones a change is most likely to trip.

---

## Key seams (where the two tracks meet)

| Port | Defined in | Boring impl | Hand-built impl |
|------|-----------|-------------|-----------------|
| `DropReportStore` | `modules/stats` | Postgres — phase 6 | `almanac-store` — phase 7 |
| `SolveCoordinator` | `modules/planner` | single-node — phase 2 | `almanac-raft` — phase 9 |
| solve cache | `modules/planner` | Redis — phase 2 | replicated KV — phase 8 |

Keep these interfaces narrow. A port shaped to flatter the hand-built side
proves nothing.

---

## Deviations from the plan

Record every departure here with its cost, so nobody has to reconstruct the
reasoning later — including you, in month six.

### D3 · Launch title swapped to Punishing: Gray Raven (2026-09-13)

**The plan launched on Reverse: 1999 and brought in Punishing: Gray Raven at Phase 11
as proof the model is game-agnostic. The maintainer swapped them.** The trigger
was a question: is this a clone of Kornblume? Checking Kornblume's source to
answer it showed that **`docs/prior-art.md`'s central claim was wrong**. Kornblume
*does* solve per player, with a linear program in the browser (GLPK, since
2024-03). It also imports inventories by OCR and syncs through Google. So on R1999
the product overlaps an established, deployed tool almost feature for feature, and
the wedge that is left is narrower: whole runs, explanations, sample-size-aware
yields, provenance, and two games on one model. A GitHub search found **no open
PGR planner of this kind**. That is not proof none exists, since closed sites and
spreadsheets would not show up. The maintainer plays PGR on Global, which is what
makes first-hand sourcing possible at all.

**What it cost.** Everything built so far was built and verified against R1999:
the adapter, two imported patches, the nine-agreement benchmark, the first-hand
banner, and every screen. **PGR starts with no data in the repository**, no
benchmark to compare against, and gacha fixtures that are all second-hand.
**Fodder, probabilistic goals and the first real calendar** move from Phase 11 to
before launch (**N30**, **N20**). So the launch gets further away, not closer, and
Phase 4's exit now waits on **N27** and **N30** as well as **B5**. The README
opening, `plan.html`'s title facts and wedge card, and `prior-art.md` §1 were
corrected the same day.

**What it bought.** A launch that is not a second copy of a tool players already
use. It also makes the abstraction's hardest shapes load-bearing from day one, not
bolted on at the end. Phase 11's proof survives with the roles reversed: R1999
has to go live with no game-specific code added after PGR.

**Reversal trigger:** the timed PGR reading in **N27** shows first-hand sourcing
of PGR is not feasible for one maintainer, or an established PGR planner that
solves per player turns up. Either one puts R1999 back on top, with the Kornblume
overlap stated honestly.

### D2 · Phase 5 entered before Phase 4 closed (2026-09-12)

**The rule broken is this file's own:** do not start a phase until the previous
one's criterion is met. Phase 4's criterion needs five strangers completing a plan
against a deployment, which is **B5**, whose last step needs accounts and an OAuth
registration no session has. **N27** above it is the maintainer's by ADR 0015's
integrity rule. So the choice was put to the maintainer — Phase 5, B5's wiring, or
**N5** — and Phase 5 was chosen deliberately, knowing the order.

**What it cost:** Phase 4 is still open and B5 is still the only thing between it
and its criterion, so this bought no progress on the launch. The gate is
untouched. **What it bought:** the one large piece of Track A a session can finish
alone and verify without a browser, a provider or a person reading a game screen.

**What to watch.** The risk in taking a phase early is building against imagined
requirements, which is the whole argument for the Track B gate — so note what this
phase did *not* get: no bean, no route, no screen, and an income model left
unwritten rather than guessed at (**N28**). The engines answer questions a test
asks. **Nothing has asked them on behalf of a player**, and that is the same
qualification `SolveCoordinator` has carried since Phase 2.

**Reversal trigger:** none — a phase cannot be un-entered. The trigger that
matters is the one on the *next* phase: **do not take Phase 6 early on this
precedent.** Phase 6 feeds estimates into the optimizer and needs users, so
starting it without them is exactly the mistake the gate exists to prevent, and
Phase 5 was safe to take early only because its exit criterion is a proof about a
model rather than a measurement of real traffic.

### D1 · Deployment deferred (2026-09-02)

**REVERSED 2026-09-09.** Heading kept verbatim so every link to it still lands.

**Was:** no money spent, so no hosting; the `deploy` job is `if: false`.
**Now:** both halves of its own reversal trigger fired at once — Phase 4 reached,
and a free tier accepted. **Vercel for the frontend, Render for the backend.**
Still no money, so the premise stands; what changed is that a free tier is
acceptable. [The entry in full is in the archive](docs/history/tracker-archive.md#d1--the-deferral-in-full).

**What it cost is what the next sessions buy back:** Phase 0's box is unticked, the
Track B gate has no meaning without real traffic, and deploy problems really were
discovered late — the Dockerfile broken since Phase 1 with nobody noticing is that
cost arriving.

**Nothing is deployed yet.** The decision is made, the wiring is not — **B5**,
which carries two consequences to settle before writing any of it: **one origin or
two** (a Vercel rewrite of `/api/*` preserves the same-origin cookie, CSRF and
redirect the backend is built around; two real origins do not, and the hop sits on
a two-second solve promise), and **the free tier sleeps** (a cold start is tens of
seconds against a budget of two — warm-up ping, honest loading state, or accept
it, decided before five strangers meet it).

---

## Environment notes (this machine only)

Not deviations — nothing about the design changed. These are local facts that
cost time to rediscover.

### E1 · Avast intercepts TLS, so Gradle cannot fetch new dependencies

**Live again as of 2026-09-08** (it was turned off on 2026-09-05 and is back).
Avast's HTTPS scanning re-signs every TLS connection with its own root CA.
Windows trusts that CA; **the JDK's `cacerts` does not**, so any Gradle download
of an artifact not already cached fails with `PKIX path building failed`. It is
invisible until a **new** dependency is added, because everything already in
`~/.gradle/caches` keeps working.

```bash
cd backend && ./gradlew -Djavax.net.ssl.trustStoreType=Windows-ROOT build
```

This points the JVM at the Windows certificate store. It grants **no new trust**
and modifies nothing. Not committed to `gradle.properties`, because
`Windows-ROOT` does not exist on the Linux runner.

### E2 · Docker Engine 29 refuses Testcontainers' API version

**Fixed and committed** — `systemProperty("api.version", "1.44")` on every `Test`
task in `backend/build.gradle.kts`, on the *task* and not as an environment
variable, which is the whole fix. Only worth reading if it comes back:
[the archive](docs/history/tracker-archive.md#e2--the-full-account).

### E4 · A dead Docker engine looks exactly like a slow one

Docker Desktop takes minutes to start here and `docker info` hangs rather than
failing while it does — **but waiting is only sometimes the answer**. **Do not use
`com.docker.service` as the test** (corrected 2026-09-09): on the WSL2 backend the
Windows service is not the engine, and a session has read `Stopped` while the
engine answered and Testcontainers ran all day. **Ask the engine:**

```bash
docker info --format '{{.ServerVersion}}'
```

A version means it is up, whatever the service says. If that hangs or errors,
*then* look at the service: `Stopped` there is a reason to ask a human, because
a session cannot start it — `Start-Service` from a non-elevated shell fails with
*Cannot open com.docker.service service on computer '.'*.

---

## Open questions

Carry these forward until answered; strike through with the answer when resolved,
then move the entry to
[the archive](docs/history/tracker-archive.md#answered-questions).

- **Q5 — Is our "3.5" the same 3.5 anyone else means?** *Open for the existing
  data; **dissolved for everything after ADR 0015**.* `fetch-upstream.sh` pins a
  commit dated **2026-03-17** while Global 3.5 ran **2026-05-28 to 2026-07-02** —
  two and a half months apart, most likely because Kornblume tracks **CN**. So
  everything here labelled "3.5" is probably CN 3.5, and the labels may not mean
  what a reader assumes. It does not invalidate the nine agreements (that guide
  was already known to be 2.7 against a 3.3 sample). **Self-sourcing ends the
  ambiguity by construction** — you know which region and patch you read, because
  you read it — so this is a caveat on the old numbers rather than a question
  about the new ones. Until then do not write "3.5" publicly without saying which.
- **Q4 — Rate verification.** *Answered for one R1999 banner, 2026-09-13; open for
  the rest.* The maintainer's screenshots of the Global client's rules screen for
  *A Newly Hatched Chapter* ([the note](docs/game-facts/reverse-1999-summon-disclosure.md))
  **confirm the pity curve twice.** The screen states it word for word, and it
  also prints **an overall 6★ rate of 2.36%, which the curve reproduces as
  2.3592%.** A curve one pull early or late would print 2.38% or 2.34%.
  **They contradict the featured rule**: 50/50 with a guarantee, where
  `reverseDebut` hands her over outright. **They also found a Limited Shop exchange
  that no engine models**: one Cassette of the Lost per summon, 200 for her. That
  is past the 140-pull worst case, so it never binds for one copy, but with no limit
  until Portrait 5 it cuts **six copies from 840 pulls to 560**.
  Still second-hand: the beginner banner and all of Gray Raven. Drop rates are a
  separate question, closed with the answer no, in
  [the drop disclosure note](docs/game-facts/reverse-1999-drop-disclosure.md).

---

## Session log index

Full entries are in [the archive](docs/history/tracker-archive.md#session-log),
newest first. **Write the entry there; add its line here.**

| Date | Session | What it was |
|---|---|---|
| 2026-09-18 | twenty-second | **PGR was read (N27 blocks 1–3), and the client contradicted the guides twice.** Both banner archetypes complete and paired; the featured rule is **per-banner**, and below 100% the recovery is published as the **Calibration System**, inherited across pools — **R1999 clears where PGR inherits**, so the two games now disagree on what `PityScope` is for. **PGR events are not farms and have no shops**, so **N30 loses its shop half and probabilistic goals and gains an event shape `EnergyMip` cannot express**. **PGR's advertised 1.90% does not reproduce** from its own numbers, unlike R1999's 2.36%. **N28 unblocked**; Serum pinned at 240 ml, +1/6 min, cap = exactly one day. **Block 4 cut by the maintainer.** Nothing authored into a bundle yet |
| 2026-09-14 | twenty-first | **The PGR survey D3 was missing, and the plan's wedge rewritten from it.** Every tool found was opened rather than judged from a snippet; nothing found plans PGR farming from an inventory. The finding with teeth was in the *guides* — that PGR is farmed through event stages whose currency buys materials in event shops — which **N30 was scoped from and which the twenty-second session found false against the client** |
| 2026-09-13 | twentieth | **The launch title swapped to Punishing: Gray Raven ([D3](#d3--launch-title-swapped-to-punishing-gray-raven-2026-09-13))**, because asking "is this a Kornblume clone?" found `prior-art.md` wrong: **Kornblume solves per player with GLPK in the browser**, and has since 2024. README, plan and prior-art corrected; N27 retargeted to PGR; **N30** puts fodder in the solver. Before that, **Q4 answered for one banner, off the client itself:** the maintainer's screenshots of the rules screen confirm the pity curve twice — stated word for word, and an overall 6★ rate of **2.36% the curve reproduces as 2.3592%**, a check a curve one pull off would fail — **contradict the featured rule** (50/50 with a guarantee, not outright), and **find a Limited Shop exchange no engine models**. First first-hand fixture and five tests; 96 questions, worst gap still 0.110. An AI web-search answer was offered first and not recorded. **N5: every CI action onto Node 24, zero annotations, green on run `34731880389`** ([PR #20](https://github.com/kietnt4412/storm_almanac/pull/20)). Taken to the latest majors (checkout v7, setup-java v6, setup-node v7, upload-artifact v7) — **except `gradle/actions`, held at v5** because v6 needs Gradle's Terms of Use accepted, which is the maintainer's call, and v5 has been frozen since February. **The green run executed no tests**: a YAML-only change leaves every Gradle input identical, so all seven test tasks came from cache. N27 handed to the maintainer as a reading form, deliberately without the fixture values so the reading stays blind. PR #19 already merged and `dev` had no open PR — the fifth time. 749 → 769 lines |
| 2026-09-12 | nineteenth | **Phase 5 closes on its criterion, out of order and on purpose (D2).** Two engines sharing three branches and nothing else: an exact chain and 500 000 seeded trials, **worst gap 0.110 percentage points over 84 questions against a criterion of 0.300, at 1.84 standard errors** — and both games' published rates out of `BannerModel` alone. **The plan's own two numbers do not fit together:** at its 100 000 trials, 0.3 points is under two standard errors, and the cross-check failed on its first run with both engines correct, so the trial count now follows from the tolerance. Four decisions in [ADR 0018](docs/adr/0018-the-gacha-engines-answer-one-question-about-one-rarity.md) — `PullResult` can carry no `Rarity`, a floor reaching the headline is refused rather than ignored, `PityState` counts losses instead of flagging one, and the chain reads the mass that never arrived so a wall is exactly 1.0. Two of the first failures were **in the tests**: a 30-pull wall is 0.7030 and not certainty, because a wall guarantees the rarity and not her. 324 tests locally and 308 on CI, **green on run `34695206362`** ([PR #19](https://github.com/kietnt4412/storm_almanac/pull/19)). **PR #18 was already merged and `dev` had no open PR — the fourth time, caught by looking** |
| 2026-09-12 | eighteenth | **N4: ADR 0007's own open gap becomes a failing build.** `EntityKindBoundaryTest` allows a read of `Entity.kind` in `gamedata`, `api` and `adapters` and denies it everywhere else — an allowlist, because the denylist of three modules the ADR names is what would pass vacuously again. Bytecode rather than a source scan, because a grep cannot tell `entity.kind()` from `change.kind()`. Verified by putting the defect back in both spellings. **A condition for method references was written, measured, and deleted** — `accessTargetWhere` already catches them, and the measurement is in the javadoc so nobody adds it back. ADR 0007 not edited: the decision did not change, only the account of what enforces it. Two lessons moved to the archive and two struck-through entries closed out, 747 → 742 lines (and the previous session's count was seven short — measure it). 278 tests, CI-confirmed on run `34691539392` ([PR #18](https://github.com/kietnt4412/storm_almanac/pull/18), which now carries N25 *and* N4) |
| 2026-09-11 | seventeenth | **N25's three debts paid, so it closes.** Provenance is read back out through a *second* port — the solver still cannot see it — and both catalog pages now say where their numbers were read, which on today's data reads "invented for this project" or "nobody recorded it", and that is the feature working. The PWA was loaded with **the origin server killed** rather than with a flag flipped. The frontend gets 15 tests and a written argument for what they are and are not for. A browser found one more defect on the way: a new bundle reading an old API's response **blanked the whole page**, which is B5's two-host deploy window in miniature. 277 backend tests, 15 frontend, CI-confirmed on run `34597211345` ([PR #18](https://github.com/kietnt4412/storm_almanac/pull/18)) |
| 2026-09-11 | sixteenth | N25: the five screens exist and a browser has driven all of them — and **a logged-in character page now says what that reader is short of**, which is half of Phase 4's exit. The offline outbox gives N23's merge a second real clock: a stale edit lost on purpose and the interface named the key. Three routes were missing underneath (`/api/games`, `/items`, `/shortfall`) and four defects turned up that only a browser could find. Owed: provenance read-back, an offline *load*, and any frontend test at all. 274 tests |
| 2026-09-09 | fifteenth | N24: a browser signs in, reads its account, creates a profile and signs out. The way in is a Gradle module the deployable jar does not contain — **[ADR 0017](docs/adr/0017-the-development-sign-in-is-absent-from-the-artifact.md)**, absence rather than configuration, with `DeployableJarTest` reading the artifact to prove it. On its first run it found a defect two phases old: **the CSRF cookie was never issued**, so any browser's first write would have been refused. Also the first authenticated request over a socket, and a Dockerfile broken since Phase 1. 264 tests locally, CI-confirmed on run `34327487736` |
| 2026-09-09 | fourteenth | N26 answered and the answer was no — the game grades a drop `Fixed`/`Common`/`Possible` and prices only the first, so 764 of 779 drop facts still have to be counted; gacha rates *are* disclosed, which is half of Q4. Then N27 split in two: the authoring is the maintainer's by ADR 0015's own integrity rule, so the session built the mechanism instead — **[ADR 0016](docs/adr/0016-provenance-is-a-property-of-the-data.md), provenance is a field and `publish` enforces it**. 257 tests, 0 failed, 0 skipped locally |
| 2026-09-09 | thirteenth | N23 pays Phase 3's sync debt (ADR 0014: last-write-wins per key, a clock that outlives its value, a watermark a failing test found). Phase 4 opened — the frontend served for the first time, which found a 401 where a 404 belonged. Then the largest decision of the session: **go first-hand on game data** (ADR 0015), closing Q2, Q3, F1 and F2 — ~3 300 facts a patch, and a bootstrap problem to solve |
| 2026-09-08 | twelfth | Phase 3 opened and closed: V5 gives identity and player their schemas with four decisions in its header, sign-in creates the account while the principal is built, and a plan is computed from goals nobody handed the optimizer — 228 tests, and sync is the piece of the scope that was not built (N23) |
| 2026-09-08 | eleventh | N14: the plan gets a calendar — the horizon as a scalar rather than an index (ADR 0013), p95 held at 1 807 ms, the two objectives finally disagree (0 energy / 28 days against 370 / 2) — then the maintainer supplied what the game actually does, and two of the answers were corrections |
| 2026-09-08 | tenth | N15: a solve is cached on its key (1 806 ms → 2 ms on the real patch) and a queue runs it once, CI-confirmed on `3fdc277`; and the read that took shops out of N14 |
| 2026-09-08 | ninth | N16 and N17: CI confirmed the tree and Phase 2 closed; then drop yields learned how many runs they were measured over, and agreement with the community went from five to nine |
| 2026-09-07 | eighth | N12: the community's answers — and the discovery that the adapter had been reading a stage table missing two thirds of the game |
| 2026-09-07 | seventh | N11 and N13: the optimizer answers a real goal set and says how much it does not know (ADR 0010) |
| 2026-09-06 | sixth | N9 and N8: the pipeline meets somebody else's data, and ADR 0009 settles how |
| 2026-09-06 | fifth | N7: the API answers both of Phase 1's exit questions, attributed and version-pinned |
| 2026-09-05 | fourth | N6: the schema holds data — and could not be deleted, which V3 fixed |
| 2026-09-05 | third | Phase 1 opens: the canonical schema |
| 2026-09-05 | second | The equipment question, answered from the code (ADR 0007) |
| 2026-09-05 | first | The app runs; booting it found a 401 on the one endpoint it had |
| 2026-09-02 | four entries | Scaffold, first green build, first CI runs, prior art read, hosting deferred (D1) |
