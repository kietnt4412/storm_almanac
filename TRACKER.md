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
- Last updated: **2026-09-11** (sixteenth session)

---

## Status

- **Phases 1, 2 and 3 stay closed, criterion and scope.** One line each on the
  [phase board](#track-a--product); the qualifications that matter are in
  [what is still unverified](#what-is-still-unverified).
- **Phase 4 — Frontend v1 and launch — is OPEN**, 2026-09-09.
  **[D1 is reversed](#d1--deployment-deferred-2026-09-02)** — Vercel and Render,
  free tier, still no money. Nothing is deployed: the decision is made, the
  wiring is **B5**.
  **The screens exist as of 2026-09-11.** N25 built all five — inventory editor,
  goal picker, plan view, catalog browse and search, and the character page with
  the personalized overlay — and a browser has driven every one of them against
  a real database. **Phase 4's second exit clause is served**: a logged-in
  character page says what that reader is short of, from where their roster
  actually stands. **The first clause is not and cannot be until B5**: five
  strangers cannot complete a plan against something that is not deployed.
  **Three things N25 still owes** are on [its line](#next-actions): provenance is
  still written and never read, the PWA's offline *loading* is unproven (its
  offline *editing* is not), and there is not one frontend test.
  *The data blocker stands:* see the next line — F2 is gone, and what replaced it
  is bigger.
- **An edit made in a tunnel has now survived one, and lost a merge on purpose.**
  The offline outbox is the client half of N23: an edit is stamped when the
  player types, held per profile and per key, and flushed with **PATCH and never
  PUT** — a PUT from a phone that was offline overwrites every key another device
  touched, which is the failure the per-key merge exists to prevent,
  reintroduced in the client where no server test would see it. Verified in a
  browser against a refusing network, and against a deliberately stale edit that
  lost to a newer one and **said which key lost**. That is
  [ADR 0014](docs/adr/0014-sync-is-last-write-wins-per-key-against-a-clock-that-outlives-the-value.md)'s
  tiebreak with real clocks on both sides for the first time.
- **CSRF is `SecurityConfig.browserCsrf` — resolved eagerly, shared by both
  filter chains — and it must stay that way.** The deferred default issues no
  cookie, so every browser write was refused for two phases while every test
  passed: MockMvc's `csrf()` supplied the thing that was missing.
  [The full account](docs/history/tracker-archive.md#session-log) is in the
  fifteenth session.
- **The project is going first-hand on game data**, decided 2026-09-09 —
  [ADR 0015](docs/adr/0015-game-data-is-sourced-first-hand-not-adapted.md),
  superseding 0009 on its conclusion and closing **Q2, Q3, F1 and F2** at once.
  Kornblume is unlicensed, F2 was never sent, and rather than send it the owner
  chose independence. **Nothing is deleted yet and the order matters:** the
  adapter stays as a never-shipped cross-check until a replacement exists, because
  removing it first leaves the project with no real data at all.
  **The cost, measured on patch 3.5:** ~2 700 static catalog facts (91 items, 100
  stages, 50 recipes, 118 arcanists carrying ~972 insight lines and ~1 502
  resonance entries, 37 psychubes) and **595 drop-rate facts**. The first half is
  typing and is mostly additive per patch. **The second half is the problem** —
  see the bootstrap below.
- **The bootstrap problem is the main risk in the project, and N26 confirmed the
  cheap way out is not there.** The optimizer cannot rank a stage without a
  yield, so no drop data means no plan; own drop data means Phase 6, which means
  users, which means a working plan. **The publisher does not disclose stage drop
  rates** — the stage screen grades a reward `Fixed`, `Common` or `Possible` and
  prices only the first, and every published percentage for the other two is
  somebody's crowdsourced sample. Measured on the pinned snapshot: **15 of 779
  drop facts are declared, 764 sampled** — ~2% free. Two real consolations:
  **gacha rates *are* disclosed** (half of **Q4**, one screen's reading), and
  **the `Fixed`/`Common`/`Possible` grade is itself a free first-hand fact** for
  all 595 pairs, with nowhere in the model to live until Phase 6 consumes it.
  [The drop disclosure note](docs/game-facts/reverse-1999-drop-disclosure.md).
- **A fact now carries where it came from, and `publish` enforces it** —
  [ADR 0016](docs/adr/0016-provenance-is-a-property-of-the-data.md), the
  mechanism half of N27, because ADR 0015 had a hole prose could not close: a
  number read off a game screen and one copied out of an aggregator are
  byte-identical once typed. A version whose facts are not ours to publish is
  **refused by name**, and the exception is a word the operator types.
  `KornblumeAdapter` hard-codes `THIRD_PARTY`. Silence is `UNRECORDED`, which
  parses and cannot publish. **The other half of N27 no session can do**: the
  first self-sourced bundle needs somebody to *read the game*, and 0015's
  integrity rule disqualifies an aggregator, a web search and an AI session
  alike. **The machinery is finished and the reading is not** —
  [the loop](docs/game-facts/authoring-a-first-hand-bundle.md).
- **Phase 0 stays closed by exception** — deploy deferred by D1 — and its box
  stays unticked, because nothing is deployed.
- **Track B: not started, and gated.** See [the gate](#the-gate).
- **A published version in the local database cannot be read back, and the shape
  of that is worth more than the row.** Every catalog route on the locally
  published Reverse: 1999 3.5 answers **400**: a `craft` row has zero
  `craft_input` rows and `Craft`'s constructor refuses to build one. The row
  predates the invariant, so it is a write made before a rule that came later
  rather than a defect in today's code — but **a version is immutable and the
  rules for reading one are not**, so a published snapshot can stop being
  loadable without anything having touched it. Not repaired and no action opened:
  [ADR 0015](docs/adr/0015-game-data-is-sourced-first-hand-not-adapted.md) says
  that data will not ship. **Read this before Phase 6 tightens a rule over
  anything already published.**
- **The remote, checked 2026-09-11 — re-check it, do not trust it.** **`main` is
  still `19197d3`**, which is N24: nothing has merged since, and
  [PR #17](https://github.com/kietnt4412/storm_almanac/pull/17) — last session's
  tracker commits — **was still open at the start of this session**, so N25 is
  stacked on top of it on the same `dev` branch and the same PR carries both; its
  title and body were rewritten to say so. **N25 is green there: run
  `34593442639`**, 0 failed, 16 skipped — the three snapshot-gated classes again.
  **Counting PASSED lines in a CI log undercounts**, and it caught a session out
  here: `:modules:identity:test` came back `FROM-CACHE` and printed nothing, so
  `SignInTest`'s 7 results are in the green and not in the log.
  N24's tree was green on the push to `main` as run `34327808367`, and as run
  `34327487736` on the PR — 0 failed, 16 skipped, exactly the three
  snapshot-gated classes (8 + 5 + 3). Phrased without a commit count on purpose,
  because a line that names the commit describing it is stale the moment it is
  written, which happened twice on 2026-09-09. This is a fact here rather than a next action because six sessions
  running have now opened on a "merge PR #n" the maintainer had already clicked.
  **`DeployableJarTest` passed on the runner**, so the development sign-in's
  absence is proven against a jar CI built from a clean checkout rather than only
  one this machine did. The guard travels with the pipeline.
  **The trap fired twice in one session.** CI runs on `pull_request` and on push
  to `main` only, so **a push to `dev` with no open PR runs nothing, silently.**
  PR #15 had been merged before the first push, and PR #16 was merged during the
  second. Both were caught by looking, not by anything failing. **Open the PR
  before trusting a push to `dev`, and check `gh pr list` rather than assuming
  last session's PR is still open.**
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
  **It is over that now: ~750 lines as of 2026-09-11, and the sixteenth session
  is why.** Two Status bullets and two table rows were compressed against it and
  that was not enough to pay for what N25 added. **The next session to touch this
  file should move something out before it adds anything**, and the best
  candidates are in [what is still unverified](#what-is-still-unverified): several
  entries there qualify work that finished phases ago and belong beside their
  phase in the archive.

---

## Current state

**What exists:** the repo skeleton, the domain model, a green backend build, a
game data pipeline that works end to end, an API that serves it, a real game's
data going through all of it, an optimizer that turns that data into a plan, a
reason to believe the plan, an account that can own one and be refused somebody
else's, two of that account's devices that can edit what it owns without deleting
each other's work, and — since this session — a browser that has actually loaded
a page of it.

**The load-bearing claim — and it is now a number to re-earn.** On **nine**
benchmark materials the cheapest stage this project computes is the stage a
published community guide tells players to farm, and the optimizer's plan for a
real goal set is cheaper than following that guide — **3 880 Activity against
4 017**, with the 4 017 not covering the whole demand. Seventeen of the guide's
twenty quoted rates land within three percentage points of a sample this project
had never seen. Nine and not five because a yield carries how many runs it was
observed over and the solver uses the conservative end of a 95% interval on it
([ADR 0011](docs/adr/0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md)).
See [the benchmark](docs/benchmarks/reverse-1999-community-answers.md).

**Every one of those numbers is computed from Kornblume-fed inputs**, and
[ADR 0015](docs/adr/0015-game-data-is-sourced-first-hand-not-adapted.md) says the
product will not ship that data. Until a self-sourced bundle reproduces them they
are evidence about somebody else's numbers run through our solver. **The
benchmark method survives untouched** — the guide it compares against is a
separate published artifact — so this is a claim to re-earn, not a test to
delete.

Still true from earlier phases: **the solver says how much it does not know**
([ADR 0010](docs/adr/0010-a-plan-is-the-best-provable-in-the-budget.md)), **the
pipeline has met somebody else's data and held**, and **both of Phase 1's exit
questions are answered over HTTP rather than by a test calling a repository**.

Toolchain on this machine: JDK 21.0.12 (Temurin), Gradle **9.6.0** via the
committed wrapper. Remote is HTTPS at `github.com/kietnt4412/storm_almanac`.

| Area | State | The one thing to know |
|------|-------|-----------------------|
| Backend build | **Green** | **274 tests**, 0 failed, 0 skipped locally with snapshots present. **258 on CI**, because the same 16 snapshot-gated ones skip. `:app:test` now depends on `:app:bootJar` — `DeployableJarTest` reads the artifact. Test tasks set `api.version=1.44` — [E2](#e2--docker-engine-29-refuses-testcontainers-api-version) |
| CI workflow | **Green** | Latest is run `34327487736` on `dev` (PR #16); see *Status* for the remote, checked rather than trusted. **16 skipped and they are exactly the three snapshot-gated classes** — `RealUpstreamPlanTest` 8, `CommunityBenchmarkTest` 5, `RealUpstreamPatchTest` 3. A pass there would mean a snapshot had been committed by accident. Action deprecations pending — **N5** |
| Game data pipeline (Phase 1) | **Closed and stable** | Model, schema, ingest, diff and CLI, [in full in the archive](docs/history/tracker-archive.md#closed-phases-in-full). `V2`–`V4`, 28 tables, seven invariants proven on two real R1999 patches; parser and writer pinned by a round trip (ADR 0008); publishing is a human approval, not a flag. `Drop` carries `sampledRuns`, where 0 means *declared* |
| Parser adapters | **One — and demoted to a cross-check by [ADR 0015](docs/adr/0015-game-data-is-sourced-first-hand-not-adapted.md), now in code** | `:adapters:reverse-1999`, 25 tests. **Its output is no longer what the product will ship**, and since ADR 0016 it hard-codes `THIRD_PARTY` provenance and cannot be told otherwise, so it fails a plain `publish` — there is no call site to launder data through. Kept, not deleted: diffing the first self-sourced bundle against an independent reading of the same patch is worth more as a check than it ever was as a source |
| Provenance | **Done — written, enforced, not yet read back out** | [ADR 0016](docs/adr/0016-provenance-is-a-property-of-the-data.md). A bundle declares `Provenance` records, defaults every fact to one (`sourcedBy`) and overrides per `FactRef` (`kind:slug`); `V7` materialises **one row per declared fact**, because a default is an authoring convenience and a database that stored it could not answer the question alone. **`publish` refuses a version that is not first-hand and names the facts**; `publish(…, true)` and the CLI's `second-hand` word are the explicit exception. The first-hand policy lives in `Provenance.Origin` and nowhere else — the migration constrains the set and says nothing about which count. **Silence parses and cannot publish** (`UNRECORDED`). **Still nothing serves it** — N25 built the catalog page and did not do this half, so it stays owed |
| Game data API | **Served and verified** | **Seven** game-data routes plus health, version-pinnable, every response carrying its version and attribution. 12 HTTP tests plus a hand check against `docker compose up`. Two arrived with N25 and both existed because every read before them started from a slug the caller already had: `GET /api/games` is the **index** — without it a reader with no account and no slug could reach the public catalog only by guessing a URL — and `GET /api/games/{game}/items` is **the vocabulary an inventory is written in**, which reached a client only as resolved names inside a cost until something had to render a few hundred quantities |
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
| Architecture tests | **Passing** | `ModuleBoundaryTest` (Track B layers optional until they exist) and `GameAgnosticismTest` (source scan over planner/gacha/stats) |
| Docker Compose | **Repaired 2026-09-09, and no image has ever been built end to end** | The Dockerfile had never learned about `adapters/`; the fixed tree builds the jar locally and that jar contains no development sign-in. **The image itself is still unproven** — the in-container Gradle download was abandoned at 10% after twenty minutes, so `up --build` has not completed. See the COPY-list warning in *Status* |
| Development sign-in | **Done, and absent from the artifact** | [ADR 0017](docs/adr/0017-the-development-sign-in-is-absent-from-the-artifact.md). `:modules:identity-dev` is `testAndDevelopmentOnly` on `:app` — on `bootRun` and the test classpath, **excluded from `bootJar`**, so no property or profile can reach it. `GET /dev/sign-in?as=<name>` mints an ordinary `AuthenticatedAccount` through the same `upsertFromOidc` the OAuth services use; its filter chain lives in that module, so `SecurityConfig` has no hook for it. `DeployableJarTest` opens the jar and proves the absence on every build. The frontend picks its sign-in URL behind `import.meta.env.DEV`, and the production bundle was checked for the string: **zero occurrences** |
| Frontend | **Five screens, driven in a browser** | Inventory editor (bulk entry: filtered, grouped by the game's own categories, Enter walks the column, **no save button** — a typed number is a queued edit), goal picker (ordered, targets read off the upgrade graph, roster edited where the goal is), plan view (stages, crafts, claims, shadow prices and **every one of the solver's notes**, because a plan rendered without them is a confident number hiding a gap), catalog browse and search, and the character page with the **personalized overlay**. Routing is react-router; the game is in the URL for the catalog and nowhere else, because a catalog page is the one thing here somebody sends a link to. **Not one frontend test exists** — everything was verified by driving a browser, which is how four defects were found and is also evidence CI will never have. `npm run dev --prefix frontend` (`.claude/launch.json`, which now also carries `api`) proxies `/api` **and `/dev`** to `localhost:8080`, so local is same-origin. The sign-in URL is chosen behind `import.meta.env.DEV`, so the development one is not in a production bundle |
| `gacha`, Track B | **Empty** | Interfaces and package docs. Track B is [gated](#the-gate) |

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
- **Nothing has measured whether the cache is worth having in production**, only
  that a hit is 900× cheaper than a solve. Hit *rate* depends on whether two
  players ever ask the same question, which needs users. The counters are there
  so that this stays a measurement rather than a belief.
- **The benchmark is one guide.** Written for patch 2.7 against a 3.3 sample, and
  it answers "which stage for this material" rather than "what should I do this
  week". A second independent source would turn "agrees with the community" from
  a claim into a measurement. The agreement is also on the stage *ranking*, which
  is arithmetic on the data — **the search itself is still checked only against
  itself.**
- **Two large disagreements with the community survive the sample-size fix**, on
  105 and 113 runs. A 95% bound discounts a thin sample in proportion; it does
  not rescue you from one. Those belong to the evidence, not the model, and the
  fix is more sampling — see **Q2**.
- **Nothing is deployed.** By decision — [D1](#d1--deployment-deferred-2026-09-02).
  The `deploy` job is `if: false` and there is no URL to smoke.
- **The PWA is installable and has never been loaded offline.** N25 proved
  offline *editing* — the outbox survives a network that refuses — which is a
  different claim from the app shell rendering with no server at all. The service
  worker's runtime-caching rule was also repaired this session: it named
  `/api/catalog/`, a prefix this API has never served, and anchored it with `^`,
  which cannot match a full request URL. It was invisible because nothing read
  from the cache, and it is still unproven because nothing has read from it yet.
- **The frontend has five screens and no tests.** Every claim about them comes
  from a session driving a real browser — which found four defects a typecheck
  could not (a store selector that re-rendered forever, a focus order that was
  not the order rows are drawn in, a goal screen that could not express the base
  of a track, and `display: block` folding every table header into a column) and
  is exactly the kind of evidence that does not survive into CI. **Nothing in the
  pipeline renders a component.** *(The earlier "never served", "no authenticated
  page" and "one page that signs in" entries, and the defects retiring each of
  them turned up, are in
  [the session log](docs/history/tracker-archive.md#session-log).)*
- **Nothing has ever run against a jar built from a Dockerfile that works.** The
  image build was repaired this session and the compose stack has not been stood
  up end to end since. The **absence** of the development sign-in from that jar
  *is* proven, by `DeployableJarTest`, on every build.
- **Nothing has ever called the API under load.** Every request loads a whole
  version — fifteen queries — a deliberate deferral written into
  `GameDataReadModel`'s javadoc. The number to beat does not exist yet.
- **The time axis has never met real data, and on this game it never will.**
  Rewards, rotation and shops are modelled or refused on the synthetic fixture
  alone, and on the real 3.5 patch the whole of N14 reduces to one energy row.
  **Do not read "the optimizer has a calendar" as "the optimizer schedules real
  weeks."** This is not the upstream being silent: **R1999 has no weekday
  rotation at all**, so `Availability.ALWAYS` is *correct*, and its daily income
  **is conditional on spending Activity**, so entering it as a `Reward` would make
  every plan systematically too cheap. The rotation machinery waits for a game
  that rotates — Phase 11 is the next chance. See
  [the economy facts](docs/game-facts/reverse-1999-economy.md).
- **Three shapes the model cannot express**, each a *silent* wrong answer if
  faked, none a defect in what shipped: **an item that restores energy**
  (`Stage` is the only source touching the budget and only ever consumes),
  **a reward conditional on spending energy**, and **a lifetime purchase limit**
  (`Shop` caps at "n per `Period`", not "five, ever"). Examples and provenance in
  [the economy facts](docs/game-facts/reverse-1999-economy.md).
- **The adapter converts less than the upstream publishes** — no shop offers, no
  alternative resonance-pattern costs, no unreleased content — each with a reason
  in `KornblumeAdapter`'s javadoc. That sentence is only reassuring when somebody
  has checked what it converts against what the upstream actually *reads*; the
  last time nobody had, two thirds of the game was missing.
- **No skills or talents have ever been ingested from a real upstream.** The
  catalog axis is proven end to end on the synthetic fixture only, because
  Kornblume publishes no skill text. An upstream gap, not a defect — but do not
  let the phase board imply otherwise.
- **The pipeline has met one upstream, not two.** The second game is Phase 11 and
  is where the abstraction is actually tested.
- **No fact in this repository has ever been sourced first-hand**, and ADR 0016
  did not change that. The only bundles that pass its gate are the synthetic
  fixtures, passing by declaring `AUTHORED_FIXTURE` — honest, and evidence of
  nothing about a real game. **Every Reverse: 1999 number in this file still
  comes from Kornblume.** The gate is what will stop that shipping; it is not
  progress on replacing it (**N27**).
- **Provenance is written and never read, and the excuse for that has expired.**
  The publish gate queries it; no API response carries it. It was waiting on a
  catalog page to be a claim a reader could check — **and the catalog page exists
  now** (N25, 2026-09-11) with the numbers on it and nothing saying where they
  came from. Still owed on [N25's line](#next-actions).
- **`gacha` has no behaviour.** Every port in it is an interface with nothing
  behind it.

---

## Next actions

Ordered. Completed ones move to
[the archive](docs/history/tracker-archive.md#completed-next-actions).

- [ ] **N27 — Read the game, and author the first self-sourced bundle.**
      **This one is the maintainer's and cannot be delegated to a session** — that
      is not a scheduling fact, it is ADR 0015's integrity rule: a fact enters
      because someone *read it in the game or in the publisher's disclosure*, and
      an aggregator, a web search and an AI session are all the same
      disqualified thing. Everything a session could build is built: the canonical
      JSON is the authoring format, `gamedata-cli` does *preview, ingest, publish*,
      and provenance is a field the publish gate enforces
      ([ADR 0016](docs/adr/0016-provenance-is-a-property-of-the-data.md)).
      **Start with one stage and one character end to end**, not with a backfill:
      the point of the first bundle is to find out what authoring one costs before
      committing to ~2 700 of them, so **write down how long it took** — that
      number is the input to every decision after it. The loop, the origins and
      the two temptations are in
      [authoring a first-hand bundle](docs/game-facts/authoring-a-first-hand-bundle.md).
      **Cheapest first read: the summon rules screen**, which closes the live half
      of **Q4** and is `PUBLISHER_DISCLOSURE` rather than a sample.
- [ ] **N25 — Phase 4's screens. Mostly landed 2026-09-11; three named pieces
      are not.** The five screens exist and a browser has driven all of them —
      inventory editor, goal picker, plan view, catalog browse and search, and
      the character page with the personalized overlay, which is **Phase 4's
      second exit clause served**. The client half of N23 landed with them. What
      it still owes, and the reason the box is unticked:
      1. **Provenance is still written and never read.** No response carries it
         and no page shows it (ADR 0016). It belongs on the catalog page, beside
         the numbers it is a claim about, and it is the difference between "our
         data" as a README sentence and as something a reader can check.
      2. **The PWA has never been loaded offline.** Offline *editing* is proven;
         the app shell rendering against no server is not, and needs the built
         bundle served rather than the dev server.
      3. **There is not one frontend test.** Four defects this session were found
         by driving a browser and would not have been found by anything CI runs.
         Decide deliberately what the pipeline should assert about a screen — a
         component test, a smoke test against the built bundle, or explicitly
         neither — rather than leaving it to the next session's judgement.
      **The exit criterion is not N25's to meet:** five strangers completing a
      plan needs a deployment, which is **B5**.
- [ ] **N20 — Put the game's day boundary on the game, not in the planner.**
      `EnergyMip.matchingDays` reads weekdays in **UTC** — a game assumption in a
      game-agnostic module. R1999 Global rolls over at **05:00 UTC−5, weekly
      Monday**. Inert today because nothing ingested rotates, so it is deferred on
      the same reasoning that kept unused beans out of N15, and it **stops being
      inert at Phase 11**. Costs a bundle field, parser, writer, a migration and
      the JDBC round trip — do it *with* that game, not speculatively.
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
- [ ] **N4 — Enforce that `Entity.kind` is never read outside the catalog.**
      ADR 0007 asserts it and nothing checks it: `GameAgnosticismTest` scans for
      game slugs, not field reads, so a `kind`-switch in `planner` would pass
      today. Natural home is an ArchUnit rule beside `ModuleBoundaryTest`.
      **Its deferral has expired.** The entry said to write it "with the first
      real planner code"; `planner` is now ~2 000 lines across a resolver, a MIP
      and an optimizer, so the rule would no longer pass vacuously. It is cheap
      and it is owed.
- [ ] **N5 — Upgrade the CI actions before they break.** Green but warning twice,
      both on a clock: **Node 20 is deprecated** and six actions are already being
      forced onto Node 24 by the runner (`checkout@v4`, `setup-java@v4`,
      `upload-artifact@v4`, `gradle/actions/setup-gradle@v4`,
      `gradle/actions/wrapper-validation@v4`, `setup-node@v4`), and
      **`setup-java@v4` is deprecated outright** — migrate to `@v5`. Do it as a
      standalone PR while the pipeline is quiet: six action bumps at once want
      their own green run to attribute a failure to.
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
- [x] **Phase 1 · Game data foundation** — **closed 2026-09-06.** Both halves
      met, CI-confirmed (N10). Two qualifications, both in
      [the unverified list](#what-is-still-unverified).
- [x] **Phase 2 · Optimizer core** — **closed 2026-09-08, criterion and scope.**
      Nine agreements, p95 1 807 ms. **The search is stopped by its budget, not
      finished by it**, and says so with the size of the doubt (2.30%, ADR 0010).
- [x] **Phase 3 · Identity and player state** — **closed 2026-09-08, criterion;
      scope completed 2026-09-09.** A plan computed from stored state on a real
      account, and since N23 a per-key merge so two devices do not overwrite each
      other ([ADR 0014](docs/adr/0014-sync-is-last-write-wins-per-key-against-a-clock-that-outlives-the-value.md)).
      Qualifications in [the unverified list](#what-is-still-unverified) — the
      OAuth exchange has never run, and no two real devices have ever synced.

- [ ] **Phase 4 · Frontend v1 — and launch** — 2.5 weeks — **OPEN 2026-09-09.**
      Landed so far: the app is served, hosting is decided (Vercel + Render, D1
      reversed), a browser has signed in (N24, ADR 0017), and **the five screens
      exist and have been driven in one** (N25, 2026-09-11) — including the
      overlay, which is the second half of the exit below. Nothing is deployed,
      so the first half is untouched; three pieces of N25 are still owed and are
      named on [its line](#next-actions).
      Inventory editor built for fast bulk entry, goal picker, plan view with
      per-stage breakdown, offline PWA. Plus catalog browse and search with the
      personalized overlay on every character page — that overlay is the whole
      argument for having a catalog, so it ships *with* it, not after. Launch
      publicly at the end even if it is ugly.
      **Exit:** five strangers complete a plan without asking for help, and a
      logged-in character page shows what that reader is short of.

- [ ] **Phase 5 · Gacha engine** — 1.5 weeks
      Generic `BannerModel`, Markov and Monte Carlo engines, income model, the
      "can I guarantee her" answer.
      **Exit:** both engines agree within 0.3% and reproduce published R1999 and
      PGR rates.

- [ ] **Phase 6 · Drop statistics on Postgres** — 1.5 weeks
      Report submission, Wilson intervals, provenance, abuse controls, estimates
      feeding the optimizer. Built on the boring implementation first — this is
      the interface Track B later swaps.
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
| [ ] | **11 · Punishing: Gray Raven**, 2w | Data adapter, banner model, fodder economics, probabilistic goals. Whatever has to generalise, generalise in the model. **Now also costs first-hand sourcing** (ADR 0015), and **N20** lands here | PGR live with **zero game-specific code** in `planner`, `gacha` or `stats` — and the diff to prove it |
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

### D1 · Deployment deferred (2026-09-02)

**REVERSED 2026-09-09.** Heading kept verbatim so every link to it still lands.

**Was:** no money spent, so no hosting; the `deploy` job is `if: false`.
**Now:** both halves of its own reversal trigger fired at once — Phase 4 reached,
and a free tier accepted. **Vercel for the frontend, Render for the backend.**
Still no money, so the premise stands; what changed is that a free tier is
acceptable. [The entry in full is in the archive](docs/history/tracker-archive.md#d1--the-deferral-in-full).

**What the deferral cost is what the next sessions have to buy back:** Phase 0's
box is still unticked, the Track B gate has no meaning without real traffic, and
deploy problems really were discovered late — which is why Phase 0 put the deploy
first. The Dockerfile being broken since Phase 1 and nobody noticing is that cost
arriving.

**Nothing is deployed yet.** The decision is made, the wiring is not — **B5**,
which carries the two consequences to settle before writing any of it: **one
origin or two** (a Vercel rewrite of `/api/*` preserves the same-origin cookie,
CSRF and redirect the backend is built around; two real origins do not, and the
hop sits on a two-second solve promise), and **the free tier sleeps** (a cold
start is tens of seconds against a budget of two — warm-up ping, honest loading
state, or accept it, decided before five strangers meet it).

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
failing while it does — **but waiting is only sometimes the answer**, and on
2026-09-09 it was not: the GUI processes were up and the engine was never coming.

**Corrected 2026-09-09 (fifteenth session): `com.docker.service` is not the
test.** The earlier note said `Stopped` means somebody has to start Docker
Desktop by hand. That session read `Stopped` **and the engine answered anyway** —
`docker info` returned server version 29.7.2 and Testcontainers ran all session.
On the WSL2 backend the Windows service is not the engine, so `Stopped` is a hint
at best and a false alarm at worst. **Ask the engine:**

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
- **Q4 — Rate verification.** The pity numbers in `PityRuleTest` come from the
  secondary sources the plan cites. They must be checked against in-game
  disclosure before the simulator ships (Phase 5). **Half-answered 2026-09-09 by
  N26:** the disclosure exists — Bluepoch states per-rarity summon rates and the
  pity counter in the client's own rules screen — so this is one screen's worth
  of reading rather than an open research question, and there is now somewhere in
  the data to record that somebody did it (`PUBLISHER_DISCLOSURE`, ADR 0016).
  **What stays open is that nobody has read it yet.** The same question about
  *drop* rates is closed and the answer was no; that half moved to
  [the drop disclosure note](docs/game-facts/reverse-1999-drop-disclosure.md).

---

## Session log index

Full entries are in [the archive](docs/history/tracker-archive.md#session-log),
newest first. **Write the entry there; add its line here.**

| Date | Session | What it was |
|---|---|---|
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
