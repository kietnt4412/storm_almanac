# Storm Almanac — build tracker

**Single source of truth for progress across chat sessions.** Read this first,
update it last. If a session ends without this file reflecting what happened,
the next session starts from a lie.

- Source of the plan: [plan.html](plan.html) (13 phases, two tracks)
- Last updated: **2026-09-08** (ninth session)
- **This session also closed N17** — drop yields now carry the number of runs
  they were sampled over, and the solver is given what that sample supports
  rather than what it happened to show
  ([ADR 0011](docs/adr/0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md)).
  **Agreement with the published community guide went from five exact matches to
  nine**, and the plan got 7% more expensive, which is the same fact said twice.
  **Q8 is answered and closed.** Local build green at **171 tests**; CI has not
  yet seen it — that is the first thing next session, and Phase 2's box does not
  depend on it.
- Current phase: **Phase 2 — Optimizer core — CLOSED on its exit criterion
  2026-09-08 (N16), opened 2026-09-07.** Both halves are met and measured —
  p95 1.8 s against a 2 s budget, and five benchmark goal sets where the
  cheapest stage this project computes is the stage a published community guide
  names ([the benchmark](docs/benchmarks/reverse-1999-community-answers.md),
  N12) — and **CI has now confirmed the tree**: run `34116854550` on `93f4de4`
  and run `34116880372` on `d34a31b` (PR #8 merged to `main`), both success,
  **139 passed, 15 skipped, 0 failed**, with the adapter's 24 tests executing on
  the runner. That was the last condition N16 held the box for, so the box is
  ticked.
  **What the tick does not say:** the phase's scope is not exhausted. **N17 closed
  later the same day** — sample sizes now reach the solver — and **N14** (the time
  axis) and **N15** (the cache behind `SolveKey`) are still open. Phase 3 is
  therefore not opened yet.
  Phase 1 closed 2026-09-06 the same way (N10: runs `34035918992` and
  `34035923889`).
  Phase 0 stays closed by exception (deploy deferred by D1) and its box stays
  unticked, because nothing is deployed.
- Track B status: **not started, and gated** — see [the gate](#the-gate)
- Standing caveat, read it every session: **the strongest tests in this
  repository are ones CI does not run.** Upstream data is fetched and never
  committed ([ADR 0009](docs/adr/0009-upstream-data-is-fetched-never-vendored.md)),
  so `RealUpstreamPatchTest`, `RealUpstreamPlanTest` **and now
  `CommunityBenchmarkTest`** skip on the runner — 15 of the 171 tests. Run
  `backend/tools/fetch-upstream.sh` before trusting a green build to mean the
  pipeline handles real data, and note that the optimizer's entire performance
  evidence, and every comparison with an outside answer, lives in those three.
- **Second standing caveat, new this session and the harder one: a green build
  says nothing about whether the data going into it is the data the upstream
  actually publishes.** The adapter spent five sessions reading a stale stage
  table with two thirds of the game missing, and everything downstream — the
  bundle, the round trip, the schema, the solver, the p95 — was green the whole
  time. What caught it was going to look for somebody else's answer. See N12.

---

## How to use this file

1. **At session start:** read *Current state*, *Next actions*, and *Open
   questions*. Do not re-derive them from the code.
2. **During the session:** tick boxes as work actually lands, not when it is
   planned.
3. **At session end:** update *Last updated*, *Current state*, *Next actions*,
   and append to *Session log*. Move anything unresolved into *Open questions*.

Rules that keep this file honest:

- A box is ticked only when its exit criterion is met, not when the code exists.
- "Done" means tested and committed. "Scaffolded" means the shape exists and the
  behaviour does not — say so explicitly.
- Never delete a *Session log* entry. Supersede it.

---

## Current state

**What exists:** the repo skeleton, the domain model, a green backend build, a
game data pipeline that works end to end, an API that serves it, a real game's
data going through all of it, an optimizer that turns that data into a plan, and
— as of this session — **a reason to believe the plan**.

The load-bearing sentence, and it replaces the previous one: **on nine benchmark
materials the cheapest stage this project computes is the stage a published
community guide tells players to farm, and the optimizer's plan for a real goal
set is cheaper than following that guide** — 3 880 Activity against 4 017, and
the 4 017 does not even cover the whole demand. Twenty claims were compared;
seventeen of the guide's quoted drop rates land within three percentage points
of a sample this project had never seen. See
[the benchmark](docs/benchmarks/reverse-1999-community-answers.md).

**Nine, where the previous session measured five, and the four extra were bought
by N17 rather than by tuning.** A drop yield now carries how many runs it was
observed over, and the solver is handed the lower end of a 95% interval on that
mean instead of the mean itself
([ADR 0011](docs/adr/0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md)).
Four materials where this project used to prefer a thinly sampled stage with a
flattering mean now name the stage the community names. **The plan also got 7%
more expensive** — 3 624 to 3 880 — and that is the same fact stated from the
other side: the old number was optimistic, not cheap. The change was made because
the evidence demanded it, and the agreement is what happened next; that ordering
is the only reason the agreement means anything.

The previous load-bearing sentences still hold: **the solver answers a real goal
set over somebody else's real numbers and says how much it does not know**
([ADR 0010](docs/adr/0010-a-plan-is-the-best-provable-in-the-budget.md)), **the
pipeline has met somebody else's data and held**, and **both of Phase 1's exit
questions are answered by the API rather than by a test calling a repository**.

**The benchmark found a defect before it ran once, and it is the largest one this
project has had.** The adapter was reading `stages.json`, and the upstream's own
planner reads the newest `stages<major>_<minor>_greedy.json`. At both pinned
commits `stages.json` carries **chapters 1 to 4 only** — 99 stages of the 105 the
game has, overlapping the live table by 40. **Two thirds of the game were missing
for five sessions and nothing noticed:** the bundle was well-formed, the parser
accepted it, the schema round-tripped it, the solver was fast, and the plans were
computed against a third of the content. What caught it was looking up what the
community says the best stage is and finding stages our data had never heard of.
Fixed in `KornblumeAdapter` (newest sampled table wins, counts divided by the
sampled run count, and it says which file it read) and in
`tools/fetch-upstream.sh` (which now pins the sampled table per snapshot).

**A "fact about the game" from last session was a fact about that bug.** *45 of
118 characters' Insight 2 cannot be planned from patch 3.5* is **withdrawn**.
Against the table the upstream actually uses, **all 118 can**, and the test that
asserted the old number now asserts the opposite as a canary — a released
character with no source for a material means the stage table is short again.

**And the benchmark found the thing to do next, which this session then did.**
Every disagreement with the community over 25% was a sample-size disagreement:
the upstream publishes how many runs each stage's drop table was observed over —
from **105 to 41 212** — and the model threw that number away, so a mean over 105
runs outranked a mean over 2 680. **Fixed 2026-09-08 as N17** (ADR 0011): the
sample size travels the whole pipeline, and `YieldTable` hands the solver a 95%
lower bound on the mean instead of the mean. **Q8 is answered and closed.**
What the fix bought, measured rather than hoped: **nine exact agreements instead
of five**, one of the four large disagreements gone (Rough Silver Ingot, 29% to
7.9%), and the other three shrunk without resolving — because a 95% bound
discounts a thin sample in proportion rather than dismissing it, so a 105-run
mean four times higher still wins, correctly, on this evidence. Those remaining
belong to the sample, not the model, and the fix for them is more sampling.

**The caveat that goes everywhere this claim goes:** the upstream carries no
licence, so its data is fetched and never committed
([ADR 0009](docs/adr/0009-upstream-data-is-fetched-never-vendored.md)) and
`RealUpstreamPatchTest` skips on CI. The evidence above is real and it is
*local*. Reproducing it is `backend/tools/fetch-upstream.sh` and one flag.

Toolchain as verified on this machine: JDK 21.0.12 (Temurin, via winget),
Gradle **9.6.0** via the committed wrapper (generated by IntelliJ, not by a
standalone Gradle install — there is none). Git repo initialised, remote on
HTTPS at `github.com/kietnt4412/storm_almanac`, two commits in.

| Area | State | Notes |
|------|-------|-------|
| Backend build | **Green** | `./gradlew build` — **171 tests**, counted from this run's XML rather than carried forward: `:app` 73, `gamedata` 25, `planner` 35, `stats` 13, the adapter 25. On CI it is **156**, because the 15 snapshot-gated ones skip: `RealUpstreamPatchTest` 3, `RealUpstreamPlanTest` 7, `CommunityBenchmarkTest` 5. `storm-almanac.jar` produced. **The Test tasks now set `api.version=1.44`** — without it every container-backed test fails on a machine that has taken the Docker 29 update, see environment note E2 |
| Repo layout | Done | Gradle multi-module backend, Vite frontend, ADR folder |
| Domain model (`gamedata`) | **Persisted and round-tripped** | Records and sealed hierarchies complete, and now written and read back by record equality across the whole graph. **`Drop` carries `sampledRuns` since N17** — 0 means the data declares the yield rather than measuring it, and a bundle claiming a measurement over zero runs is refused rather than defaulted. Equipment settled as an `Entity` with a `kind` field — ADR 0007, now proven through the schema too |
| `gamedata` schema | **Applied, populated, round-tripped** | `V2` (28 tables), `V3` (a version can now actually be deleted) and `V4` (`stage_drop.sampled_runs`, defaulting to 0 = declared). Seven invariants proven by `GameDataSchemaTest`; the round trip proven by `GameDataIngestTest` on the synthetic fixture and by `RealUpstreamPatchTest` on two real Reverse: 1999 patches. **It has now held a real upstream** — the caveat is only that CI has not, see ADR 0009 |
| Ingest (`CanonicalBundleParser`) | **Done** | Canonical JSON → `GameDataBundle`, validated before a connection opens; 14 tests, mostly on the rejection messages. A drop's optional `sampledRuns` reads through it, the writer emits it only when there is one, and absence is the canonical spelling of "declared" |
| Persistence (`gamedata.jdbc`) | **Done** | `JdbcGameDataIngestRepository` (write, one transaction) and `JdbcGameDefinitionRepository` (read, published only). JDBC not JPA — ADR 0008 |
| Patch diff (`VersionDiff`) | **Done** | Three axes; subjects diffed before fields. 9 tests over the two fixture versions, plus one over versions loaded from the database |
| `gamedata-cli` | **Done** | `--gamedata=<adapters\|adapt\|validate\|preview\|ingest\|drafts\|publish\|versions\|diff>` on the same jar. `adapt` converts an upstream snapshot into a canonical file, so the loop is *adapt, preview, ingest, publish* and an adapter gets no privileges for being code. 6 CLI tests plus the full onboarding walkthrough |
| Parser adapters | **One, and it now reads what the upstream reads** | `:adapters:reverse-1999` converts a Kornblume snapshot: 25 tests over upstream-shaped fixtures, plus `RealUpstreamPatchTest` over two real patches. **It takes the newest `stages<major>_<minor>_greedy.json`, divides drop counts by the sampled run count as the upstream's own planner does, and since N17 carries that run count through onto every `Drop`** — with `count: 1` converted as a declared yield, because in this upstream that marks a fixed-reward stage rather than a sample of one. It falls back to `stages.json` and says which it read either way — reading the stale one silently cost five sessions of plans over a third of the game. `ModuleBoundaryTest` lets only `:app` reach the adapters layer, so a core module depending on a title fails the build. ADR 0009 |
| `CanonicalBundleWriter` | **Done** | The parser read backwards, so an adapter's output is a file a human approves. Pinned to the parser by `CanonicalBundleRoundTripTest`, including idempotence |
| Game data API | **Done — served and verified** | `GameDataController` + `GameDataReadModel` over five routes: versions, catalog index, catalog page, upgrade costs, patch diff (JSON or `text/plain`). Version-pinnable with `?version=N`; every response carries its version and attribution. 10 HTTP tests, plus a hand check against `docker compose up` |
| Module ports | Partly implemented | `GameDefinitionRepository` has one, and its `find` now keys on a bare sequence rather than a fabricated `GameDataVersion`. **`Optimizer` now has `MipOptimizer` behind it.** `SolveCoordinator`, `DropReportStore`, `BannerEngine` are still interfaces with nothing behind them |
| Demand resolution (`DemandResolver`) | **Done** | Goals + roster + upgrade graph → an item demand vector, by walking the DAG *backwards* from the target so an unowned entity and a part-levelled one are the same traversal. Handles multi-track entities, already-met goals, shared steps. Refuses by name: an unreachable state, an unknown entity, a probabilistic goal, an ambiguous two-route state. 10 tests |
| The MIP (`EnergyMip`) | **Done for stages and crafts** | ojAlgo `ExpressionsBasedModel`, integer runs and conversions, inventory subtracted, reachability pruning, and every variable **bounded** by what could ever be useful — the bound is what makes a real patch solvable at all. Crafting recursion is an absence, not a feature: crafts are variables and intermediates are constraint rows. 12 tests, every expected number worked out by hand in its comment. **Shops, rewards, fodder and weekday rotation are not in the model**, and items whose only source is one of those are refused by name |
| Yield source (`YieldTable`) | **Done, and it now carries the sample** | Declared `Drop.expectedYield` everywhere, **discounted to the lower end of a 95% `PoissonRateInterval` wherever the drop says how many runs it was measured over** — ADR 0011, N17. A yield with no sample behind it (a fixed-reward stage) is used as declared, because there is no sampling error to discount. A measured `DropEstimate` still overrides only where the units provably agree, because `pointEstimate` is a *proportion* and the constraint needs an expected *quantity*; that branch collapses into the first one when Phase 6 publishes estimates as means. 5 tests in `YieldTableTest`, plus 7 on the interval itself in `stats` |
| `Optimizer` (`MipOptimizer`) | **Done — least energy** | Wires repositories to the resolver and the model, pins the plan to its game-data version, fingerprints the request (`SolveKey`, the Phase 2 cache key without the cache yet), and explains itself: shadow prices by re-solve, binding stages, and a note saying where the numbers came from. Budget split between search and explanation; ADR 0010. 8 tests, plus 5 in `PlannerAcceptanceTest` over the parsed fixture, 7 in `RealUpstreamPlanTest` over a real patch and 5 in `CommunityBenchmarkTest` against a published guide |
| Objectives | **One model, both answered** | `LEAST_ENERGY` and `FEWEST_DAYS` are the same plan under an untimed model, because days are energy over a constant. Said out loud in the plan's notes rather than implied. They separate when the model gains a time axis, which is the same work that adds shops, rewards and rotation |
| Solve caching | **Key only** | `SolveKey` hashes a canonical rendering of (version, goals, objective, energy/day, inventory, roster). Nothing caches on it yet — no Redis, no `SolveCoordinator` implementation |
| Community benchmark | **Done — Phase 2's other half, and now the regression test for ADR 0011** | `CommunityBenchmarkTest`: twenty per-material "best stage" claims from a published guide, compared with what this model computes. **Nine exact agreements** (five before N17), seventeen quoted rates reproduced within three points, and every disagreement over 25% traced to a small sample on one side. It now ranks on the yields the **solver** uses and prints the raw-point-estimate ranking beside them, because comparing a discounted number against a quoted one silently would read a correction as a regression. Skips without a snapshot. [The document](docs/benchmarks/reverse-1999-community-answers.md) carries the provenance |
| `WilsonInterval` | **Done** | 6 tests passing |
| `PityRule` | **Done** | 9 tests passing against both games' published rates |
| `ModuleBoundaryTest` | **Passing** | ArchUnit; Track B layers declared optional until they exist |
| `GameAgnosticismTest` | **Passing** | Source scan over planner/gacha/stats |
| Health endpoint | **Done — served and verified** | `GET /api/health` → 200 from a real container. Was 401; see the session log |
| Docker Compose | **Verified** | `docker compose up --build` from cold: image builds, all three services healthy |
| CI workflow | **Green on the current tree** | Runs `34072743411` (`dev` PR) and `34072752969` (`main` push, PR #7), both success. All 30 planner tests PASSED on the runner; `RealUpstreamPlanTest` (7) and `RealUpstreamPatchTest` (3) skipped, which is the designed state — see N13. Four seconds slower for a new source set and 51 tests. Deprecation warnings still pending — see **N5** |
| Frontend | **Green locally** | 415 deps resolved clean, typecheck + `vite build` pass, PWA SW generated |
| Track B | Package docs only | Deliberately empty — see the gate |

### What is still unverified

Be precise about this, because the temptation is to read "build green" as "it
works". It does not mean that:

- ~~**CI has not run on this session's work.**~~ **Resolved 2026-09-08 (N16).**
  The eighth session's work was committed, pushed and merged as PR #8, and CI
  ran on it: `34116854550` (`dev` PR, `93f4de4` — the tree this file describes)
  and `34116880372` (`main` push, `d34a31b`), both success. The log was read
  rather than the tick: **139 passed, 15 skipped, 0 failed**, the adapter's 24
  tests executed on the runner, and the 15 skips are exactly the three
  snapshot-gated classes (`CommunityBenchmarkTest` 5, `RealUpstreamPatchTest` 3,
  `RealUpstreamPlanTest` 7). The local half was green on this same tree — 154
  tests — and the tree had not changed at that point, so it was not re-run.
  **That statement is about the tree N16 closed on, and the tree has moved since:
  N17 landed in the same session and CI has not seen it.** The local build is
  green on it — 171 tests, all five modules, nothing skipped because the
  snapshots are present here — and **the first action next session is to push
  and read the log**, expecting 156 passed and the same 15 skipped. Phase 2's
  box does not depend on that run; N17's honesty does.
- **CI does not run the tests that matter most, and never will as things
  stand.** Two classes now touch data this project did not author, and both skip
  on the runner because the snapshots are not committed — ADR 0009, deliberately.
  Three classes now touch data this project did not
  author and all three skip on the runner, which is 15 tests: `RealUpstreamPatchTest`,
  `RealUpstreamPlanTest` and `CommunityBenchmarkTest`. They are the ones that
  would catch an upstream-shape surprise, **and the only evidence the optimizer is
  fast enough or right about anything real.** Every performance number in this
  file — p95 1.8 s, a 2.80% gap — and every comparison with an outside answer
  comes from a test the pipeline does not run. **A green pipeline is therefore
  weaker evidence than it looks, and it got weaker again this session.** This is
  no longer a theoretical worry: the stale stage table that cost five sessions of
  plans over a third of the game would not have been caught by any test CI runs,
  and was not caught by any test at all — a person went looking. Before trusting
  a green build, run `backend/tools/fetch-upstream.sh` and the suite again.
- **The optimizer has never been asked a question by anything but a test.**
  There is no HTTP route, no `SolveCoordinator` implementation, no cache behind
  `SolveKey`, and no player state to solve against — `PlayerStateRepository` is
  still an interface, which is Phase 3's job. Every solve in this repository is
  driven by a hand-built fake profile.
- ~~**The plan has never been checked against an answer somebody else worked
  out.**~~ **Done 2026-09-07 (eighth session), and it was worth every hour.**
  Twenty per-material "best stage" claims from a published community guide,
  compared with what this model computes: five exact agreements, seventeen quoted
  drop rates reproduced within three percentage points, and a plan that costs
  3 624 Activity against 4 017 for following the guide material by material. See
  [the benchmark](docs/benchmarks/reverse-1999-community-answers.md) and
  `CommunityBenchmarkTest`.
  **What is genuinely still unverified about it:** it is *one* guide, written for
  patch 2.7 against a 3.3 sample, and it answers "which stage for this material"
  rather than "what should I do this week". A second, independent source would
  turn "agrees with the community" from a claim into a measurement. And the
  agreement is on the stage *ranking*, which is arithmetic on the data — the
  search itself is still checked only against itself.
- **Nothing is deployed.** By decision — see deviation D1. The `deploy` job is
  `if: false` and there is no URL to smoke.
- **The frontend has never been served**, only typechecked and built. No page
  has been loaded in a browser and it has never spoken to the API.
- ~~**The API still serves one endpoint.**~~ **Done 2026-09-06.** Six routes
  now, five of them game data, and both exit questions are answered over HTTP
  by an anonymous caller. Checked in tests *and* by hand against
  `docker compose up`. What is genuinely still unverified about it, and worth
  keeping honest: **nothing has ever called it under load, and no browser has
  ever rendered it.** Every request loads a whole version — fifteen queries — and
  that is a deliberate deferral written into `GameDataReadModel`'s javadoc, not
  an oversight. The number to beat does not exist yet.
- ~~**The schema has never held data from a real upstream.**~~ **Done
  2026-09-06.** It has now held two real patches of Reverse: 1999 — 91 items,
  99 stages, 151 entities, 2 012 upgrades — ingested, published, read back by
  record equality and diffed. It surprised us twice on first contact, which was
  the entire point: a Cyrillic character name that ASCII slugging erased, and
  placeholder rows whose `Name` is `null`. Both fixed, both now have tests.
  What genuinely remains: **the adapter converts less than the upstream
  publishes** — no shop offers, no alternative resonance-pattern costs, no
  unreleased content, **and no sample sizes** — each with a reason in
  `KornblumeAdapter`'s javadoc and a line printed on every run. And **it has met
  one upstream, not two.** The second game is Phase 11's job and is where the
  abstraction is actually tested.
  **Corrected 2026-09-07 (eighth session):** the sentence above used to be filed
  under "smaller", and one item on that list was not small at all. Until this
  session the adapter also converted the wrong stage file — the upstream's older
  table, chapters 1 to 4 of twelve — and the schema was faithfully round-tripping
  a third of the game. **"The adapter converts less than the upstream publishes"
  is only a reassuring sentence when somebody has checked what it converts
  against what the upstream actually reads.**
- **No skills or talents have ever been ingested from a real upstream.** The
  catalog axis is proven end to end on the synthetic fixture only, because
  Kornblume does not publish skill text. The API answers *"what does her S2 do
  at rank 3?"* for `proving-ground` and has no real data to answer it with for
  `reverse-1999`. Not a defect — an upstream gap — but do not let the phase
  board imply otherwise.
- **The optimizer, gacha and stats modules still have no behaviour.** Every port
  in them is an interface with nothing behind it.

---

## Next actions

Ordered. Do them in this order.

- [x] ~~**B0 — Make it build.**~~ Done. JDK 21 via
      `winget install EclipseAdoptium.Temurin.21.JDK`; note the MSI does **not**
      set `PATH`/`JAVA_HOME` under winget, so both were set by hand in
      `HKCU:\Environment`. **Gradle is not in winget** and there is no
      choco/scoop here — the wrapper was generated by IntelliJ instead, pinning
      **Gradle 9.6.0**. One real failure, now fixed: ArchUnit rejects empty
      layers, and the three Track B layers are empty by design, so the rule now
      declares `withOptionalLayers(true)`.
- [x] ~~**B1 — `git init`.**~~ Done. Two commits, HTTPS remote. SSH was
      abandoned: the local ed25519 key is not registered with GitHub.
- [x] ~~**B1a — Commit the Gradle wrapper.**~~ Done in `c14e37b`, with the exec
      bit set in the index by `73afcde`. `backend/gradlew` is mode `100755` and
      `frontend/package-lock.json` is tracked. *This entry was already stale when
      the session opened — the work had landed and been pushed. Ticking boxes
      last is what let it rot; tick them as the work lands.*
- [x] ~~**B2 — Boot the app for real.**~~ Done. `docker compose up --build`
      brings all three services up healthy; Spring starts in ~5s, Hikari opens
      the pool, Flyway applies `V1__baseline.sql` and all six schemas exist.
      **It found exactly the class of defect it existed to find:**
      `/api/health` answered **401**. `modules/identity` puts
      `spring-boot-starter-security` on the runtime classpath, and with no
      `SecurityFilterChain` anywhere Boot's auto-configuration secured every
      route with HTTP Basic — invisible to a unit test that calls the controller
      method directly. Fixed in `63d76f1`. Verified live: `/api/health` 200,
      `/actuator/health` 200, unmapped paths 401, no session cookie.
- [x] ~~**B3 — Verify the frontend.**~~ Done. 415 packages resolved with no peer
      conflicts, `npm run typecheck` and `npm run build` both clean, PWA service
      worker generated. Tailwind warns "no utility classes detected" — expected,
      the Phase 0 shell uses inline styles; it resolves itself at Phase 4.
- [x] ~~**B4 — Get CI green.**~~ **Done: `ci` run #5 passed on `1a58dd5`**,
      2m 58s, both jobs. Confirmed from the Actions tab on 2026-09-05 (it took
      five runs; #1–#4 are written up below and each was a distinct failure).
      Also confirmed: the three `nightly-chaos` runs in the tab are **skips, not
      failures** — that workflow is `if: false` until Phase 10 has a suite, and
      a skipped scheduled run is the correct appearance. Do not chase them.
      **Caveat, and it is a real one:** #5 predates this session's three
      commits, and `build` now starts a Testcontainers Postgres. That has never
      run on the runner. B4 is green for the pipeline *as it stood* — see
      item 5 below.
      The four failures that got it there:
      1. `backend/gradlew` was committed mode `100644`. Git on Windows does not
         track the exec bit, so `./gradlew` was "Permission denied" on Ubuntu.
         Fixed with `git update-index --chmod=+x backend/gradlew`. **Watch for
         this on every future shell script added from Windows.**
      2. No `frontend/package-lock.json`, which broke the job twice over —
         `npm ci` requires a lockfile, and `setup-node`'s `cache-dependency-path`
         could not resolve. Generated; commit it.
      3. Run #4: **invalid YAML in `ci.yml` line 67** — no jobs ran at all. An
         unquoted `run:` scalar contained `: ` (colon-space) inside
         `echo "TODO(phase-0): wire..."`, which YAML reads as a nested mapping.
         Fixed with block scalars. The `deploy` job is now `if: false` until Q1
         is answered, because a job that echoes a TODO and exits 0 would paint
         the pipeline green while nothing deploys.
      4. Runs #1–#4 all failed for the reasons above. **Run #5 is the green
         one**, on `1a58dd5`.
      5. **Run #6+ — result not yet seen.** `dev` was pushed and merged to
         `main` via **PR #1** (`73007df`) at the end of the 2026-09-05 session,
         which fires the pipeline twice (the PR, then the push to `main`).
         Neither result was observed: `gh` is installed but not authenticated,
         so `gh run list` still refuses. **First action next session:** run
         `gh auth login`, then `gh run list --limit 5`.
         What is new in that run and has never executed on the runner:
         `ApplicationBootTest` starts a **Testcontainers Postgres**.
         `ubuntu-latest` ships a Docker daemon so it should pass. If it is flaky
         there, tag it and split it into its own job — **do not delete it.** It
         is the only test in this repo that has ever caught a real defect.
      **Validate YAML locally before pushing** — `js-yaml` in the scratchpad
      parses all five files in seconds and is cheaper than a CI round trip.
      A green pipeline is Phase 0's exit criterion, and a workflow that has never
      passed is not evidence.
- [ ] **B5 — Wire the real deploy.** **Deferred to Phase 4** — see deviation
      **D1**. No hosting will be provisioned before then. The `deploy` job stays
      `if: false` until there is a real URL to smoke.
- [x] ~~**B6 — Read the prior art.**~~ Done — see
      [docs/prior-art.md](docs/prior-art.md). Found a real defect in our `Drop`
      record (fixed) and narrowed Q2/Q3. Four follow-ups F1–F4 recorded there;
      **F4 blocks Phase 1 ingestion.**
- [x] ~~**B7 — Write the positioning paragraph.**~~ Drafted and placed at the top
      of the README. It is deliberately narrow, and takes its claims from
      [docs/prior-art.md](docs/prior-art.md) rather than from ambition: the
      differentiator is not "we have a solver" — ArkPlanner has one — it is
      *per-player, against estimates with confidence intervals, with the
      reasoning shown, behind a game-agnostic model.*
      **Re-read it before Phase 4's launch.** If the product cannot yet do
      everything the paragraph claims, the paragraph is a promise, not
      positioning, and one of the two has to change.
### Done 2026-09-05 — N1, N2 and N3
### Next session starts here

- [x] ~~**N1 — Confirm the pipeline is green on the current tree.**~~ **Done —
      green, twice.** Run `33963222427` (`ci` on the `dev` PR, 1m43s) and run
      `33963295323` (`ci` on the `main` push merging PR #1, 2m24s). Both
      success. **The specific thing B4 item 5 was worried about is confirmed,
      not assumed:** the log shows `ApplicationBootTest` executing on the runner
      — *"the health endpoint answers 200 to an anonymous request" PASSED* and
      *"anything that is not explicitly public is denied"* PASSED — so the
      Testcontainers Postgres really does start on `ubuntu-latest`. It did not
      skip. No need to tag or split it.
      Also re-confirmed from the CLI: the three `nightly-chaos` entries are
      `skipped`, exactly as B4 said. Do not chase them.
      **What it took:** `gh` is installed (2.100.0) but **not on `PATH`** — the
      same winget-MSI behaviour already recorded for the JDK under B0. It is at
      `C:\Program Files\GitHub CLI\gh.exe`. In PowerShell a quoted path is not a
      command; it needs the call operator:
      `& "C:\Program Files\GitHub CLI\gh.exe" run list --limit 8`.
- [x] ~~**N2 — Answer Q6 / F4: how is equipment modelled?**~~ **Done —
      [ADR 0007](docs/adr/0007-equipment-is-an-entity.md): equipment is an
      `Entity`.** The leaning on record held, but the deciding argument turned
      out not to be the PGR-Memories one. It is that **`Upgrade` and `Goal` both
      key on `EntityId`**, so anything upgradeable already has to be an
      `Entity` or force both records to change — and that the Entity/Item dual
      identity gear needs is *already in the model on purpose*, since `Item`'s
      javadoc puts a character's copies in the inventory while the character is
      an `Entity`. Also established, and it removes a whole axis from the
      argument: **gacha is untouched** — `BannerModel` and `FeaturedRule` are
      rarity-shaped and neither mentions `EntityId`.
      **Code change:** `Entity` gains a required `kind` field
      (`"character"` / `"equipment"`), opaque and game-supplied, for catalog
      routing only. Backend build green with it. It cost nothing to add now —
      **nothing in the repo constructs an `Entity` yet**, so the shape was still
      free; in Phase 1 it would have meant touching the ingest adapter and every
      fixture.
      **Gap carried, not waved away:** nothing stops a later commit reading
      `Entity.kind` inside `planner`, and `GameAgnosticismTest` scans for game
      slugs, not field reads. See **N4**.
- [x] ~~**N3 — Then open Phase 1.**~~ **Open. The canonical schema has landed**
      as `V2__gamedata_canonical_schema.sql` — 28 tables, 7 indexes, applied for
      real against PostgreSQL 16.15 (*"Migrating schema to version 2 — gamedata
      canonical schema"*, `Successfully applied 2 migrations`) and covered by
      six tests in `GameDataSchemaTest`, all passing. Three decisions are
      written into it and are worth carrying forward:
      1. **A version is a full snapshot, not a delta.** Every row belongs to one
         `game_data_version`; publishing inserts a fresh set and mutates
         nothing. That is what keeps an old plan correct after a patch, and it
         makes the diff an ordinary set comparison rather than a history walk.
         Game data is thousands of rows per version, so the duplication is not
         worth optimising away.
      2. **A reference cannot cross a version boundary — structurally.** Every
         versioned table carries `version_id` with a `UNIQUE (version_id, id)`,
         and every child uses a composite `(version_id, …)` foreign key, so a v2
         stage citing a v1 item is *refused by the database*. Costs one column
         per table. Without it the headline feature of this phase sits one
         ingest bug away from quietly lying, and nothing would show.
      3. **Draft and published are different states**, with a CHECK that a
         published row has an approval timestamp and a draft has none. Backs
         `GameDefinitionRepository`'s promise that "latest" means latest
         approved, never latest fetched.
      Also: **`attribution` is `NOT NULL` on every version.** "Numbers and text
      only, attributed" is a project invariant, so an unattributed snapshot
      should not be representable. Ties to **Q3**.
      **Still to do in Phase 1:** ingestion, versioned publishing with diffs,
      `gamedata-cli`, and tests over real patch data including a patch that
      changes something. See **N6**. **F1** (evaluate 必要的记录 as the real
      drop upstream) and **Q3** (assume nothing is redistributable — read to
      validate, never vendor) both still gate the ingest work.

### Done 2026-09-05 (fourth session) — N6

- [x] ~~**N6 — Ingestion, then publishing with diffs.**~~ **Done, all four
      parts: parser, persistence, diff, `gamedata-cli`.** 65 tests green, up
      from 26. **The schema has now held real data and given it back
      unchanged** — `roundTripsWithoutLoss` ingests a bundle, publishes it,
      loads it and asserts record equality across the whole graph. That was the
      single largest unverified claim in this file and it no longer is.
      **The two questions this entry left open are settled, and they settled
      together: [ADR 0008](docs/adr/0008-gamedata-persistence-is-jdbc.md) —
      the module persists with `JdbcTemplate`, not JPA.** Writing the mappings
      out is what showed the fit is bad in four separate ways at once: twenty
      composite `@JoinColumns` with `insertable=false`, a `UserType` for
      `TEXT[]`, a write path that is a bulk insert with nothing to dirty-check,
      and a read path of fifteen collections under one root. Underneath all
      four, the domain is already records, so JPA entities would have been a
      second parallel model — the mapping work an ORM exists to save is work
      this module does either way. Consequently **`available_days` stays
      `TEXT[]`** (JDBC reads it natively; the bitmask fallback is dropped) and
      **the composite foreign keys stay exactly as written** — with hand-written
      SQL, honouring them costs nothing.
      **What landed:**
      - **The canonical bundle format** and `CanonicalBundleParser` — Jackson
        tree model, not data-binding, so the domain records keep no mapping
        annotations and every rejection names its own position
        (`stages[1].energyCost must be a whole number`).
      - **`GameDataBundle`, which is deliberately not a `GameDefinition`.**
        A definition carries a `GameDataVersion`, which requires a non-null
        `publishedAt` — so an unapproved snapshot has *no representation* as a
        definition. The type system now says what the schema's
        `published_has_timestamp` constraint says.
      - **Validation before the connection opens.** Dangling references are
        reported all at once with the referrer named. The composite FKs would
        catch them too, one at a time, several seconds later, saying
        `stage_drop_item_fk` — which is not something the person approving a
        publish can act on.
      - `JdbcGameDataIngestRepository` (write, one transaction, draft
        replacement, published-is-immutable) and `JdbcGameDefinitionRepository`
        (read, one query per table, grouped in memory, published only).
      - **`VersionDiff`** over three axes — `PROGRESSION`, `CATALOG`, `GACHA`.
        Three, not the two the plan names: banners are neither material costs
        nor combat numbers. Subjects are diffed before fields, so a removed
        stage is one line rather than six.
      - **`gamedata-cli`**, as `--gamedata=<command>` on the same jar:
        `validate`, `preview`, `ingest`, `drafts`, `publish`, `versions`,
        `diff`. `preview` is the one that matters — it answers "what would this
        file change?" without writing a row, which is the question somebody has
        *before* they ingest. Exit codes separate a bad file (1) from a bad
        command line (2).
      - **Fixtures are a synthetic title, `proving-ground`, at 1.0 and 1.1.**
        Q3 stands: the community bundles carry no licence, so they are read to
        validate shape and never vendored. The 1.1 fixture carries one change
        of every kind on both axes and its header lists them.
      **It found a real schema defect, which was the point of doing it:**
      **a version could not be deleted.** `DELETE FROM game_data_version`
      cascades to `item` down one chain and to `skill_rank_cost` down another,
      Postgres does not order the two against each other, and the NO ACTION
      foreign key on the item side refuses. That is the *ordinary* draft-replace
      path, not an edge case. Fixed by
      `V3__gamedata_version_is_deletable.sql`: the nine item-referencing keys
      gain `ON DELETE CASCADE`. The tradeoff is written into the migration
      rather than buried — deleting one item row now silently deletes what
      referenced it, acceptable only because a version is written once and
      never edited.
      **Second surprise, smaller:** Spring's `@Repository` exception translation
      rewrites a bare `IllegalStateException` into
      `InvalidDataAccessApiUsageException`. Both refusals are named types now
      (`PublishedVersionIsImmutableException`, `NoDraftToPublishException`) so a
      domain refusal survives the trip out of the repository saying what it was.
      **Test-time note:** the two new database classes share one container and
      one Spring context through `GameDataDatabaseTest` rather than starting
      their own. `ApplicationBootTest` deliberately keeps its own — it exists to
      prove a cold start against an untouched database.

### Done 2026-09-06 (fifth session) — N7

- [x] ~~**N7 — Serve it. Phase 1's exit criterion is an API answer, not a
      test.**~~ **Done. Both exit questions are answered over HTTP**, by an
      anonymous caller, against a real database. Five routes under
      `/api/games/{game}`: `versions`, `entities`, `entities/{entity}`,
      `entities/{entity}/upgrades`, and `diff?from=&to=`. 75 tests green, up
      from 65.
      **Verified twice on purpose.** Ten tests in `GameDataApiTest` go over real
      HTTP through the real security chain, and then the whole loop was run by
      hand: `docker compose up --build`, both fixture versions ingested and
      published with `gamedata-cli` against the compose Postgres, then every
      route curled. That second check is not ceremony — the last time this
      project served something for the first time, the thing that broke was
      invisible to every test that called a controller method directly.
      **Four decisions worth carrying forward:**
      1. **A read model, not the domain, on the wire.** Serialising the records
         directly would have shipped `{"value":"gold"}` for every typed id, made
         every client join the item table by hand to turn a cost into "6 ×
         Greater Sigil", and made every future domain change a breaking API
         change nobody noticed making. `GameDataView` is the wire format and
         `GameDataReadModel` is the only thing that maps to it.
      2. **`GameDataVersion` gained `attribution`, and every response carries
         it.** "Numbers and text only, attributed" was an invariant about
         ingestion for as long as nothing served the numbers. The moment they go
         to a stranger, the attribution has to travel with them, and the read
         path could not say where they came from — the field existed only on the
         schema row and on `DraftVersion`. Cost: one field on a `common` record
         and one column in three queries. It also strengthened
         `roundTripsWithoutLoss` for free, which now proves provenance survives
         the round trip too.
      3. **`GameDefinitionRepository.find` keys on a bare sequence.** It took a
         `GameDataVersion`, so every caller fabricated one with a dummy label and
         `Instant.EPOCH` to ask a question — a port shaped for its implementation
         rather than its callers. Adding `attribution` would have made that
         hack uglier in a third place, which is what surfaced it.
      4. **Version-pinning is on every read**, as `?version=N`, defaulting to
         latest approved. A plan computed against 1.0 has to stay explainable
         after 1.1, and a URL that cannot name its patch cannot be cited in a
         bug report either.
      **Also:** `/diff` serves structured changes as JSON and the exact text
      `gamedata-cli diff` prints under `Accept: text/plain` — one renderer, so
      the report a reviewer approved and the report a reader sees cannot drift.
      `SecurityConfig` permits `GET /api/games/**` and nothing else, so a POST
      there is denied rather than 405'd; publishing stays a human approval
      through the CLI and gets no endpoint. Refusals are RFC 9457 problem
      documents whose detail is written to be acted on — *"no published version
      99 of proving-ground"*, not a bare 404.
      **One thing to know before touching the tests:**
      `GameDataDatabaseTest` is now `WebEnvironment.RANDOM_PORT`, so the shared
      gamedata context serves HTTP. A second context differing only in that
      setting would have meant a second container and a second boot on every
      build. `ApplicationBootTest` still keeps its own container, for the same
      cold-start reason as before.

### Done 2026-09-06 (sixth session) — N9 and N8

- [x] ~~**N9 — Confirm the pipeline is green on this session's work.**~~ **Done
      — green, twice, on the exact commit.** Run `34002586027` (`ci` on the `dev`
      PR, head `e2239b3`, 1m26s) and run `34002589214` (`ci` on the `main` push
      merging PR #5, head `dc6ddcd`, 2m8s). Both success; `deploy` skipped as
      designed.
      **The specific worry is answered, not assumed:** `GameDataApiTest` executed
      on the runner and all ten cases PASSED, so the `RANDOM_PORT` context really
      does start Tomcat there. **No slowdown either** — the PR job went 1m43s →
      1m26s against the previous run, so the extra servlet containers cost
      nothing measurable. Nothing to absorb and nothing to split.
- [x] ~~**N8 — Decide whether a real upstream is ingested in Phase 1 at all.**~~
      **Decided by the project owner: build the adapter now, personal and
      portfolio use, ask about licensing before publishing.** Written up as
      [ADR 0009](docs/adr/0009-upstream-data-is-fetched-never-vendored.md).
      **Phase 1's second exit criterion is met: real patch data, in tests.**
      `:adapters:reverse-1999` converts a Kornblume snapshot; two real snapshots
      from the upstream's own history (3.3 at `d49efab`, 3.5 at `8b40541`) are
      ingested, published, read back and diffed. 93 tests green, up from 75.
      **The shape of the decision, because it constrains everything after it:**
      the data is *read* and never committed. `tools/fetch-upstream.sh` downloads
      into `build/upstream-snapshots`; `.gitignore` refuses it twice over. So
      **`RealUpstreamPatchTest` skips itself on CI, and the strongest test in the
      repository is the one the pipeline does not run.** Do not let a green tick
      imply otherwise — it is said in the test's javadoc, in the README, in the
      ADR and here.
      **It found two defects on its first contact with real data, which is the
      whole argument for having done it:**
      1. **`Names.slug` folded to ASCII first.** A character named *Зима* folds
         to the empty string and the entire ingest was refused. The rule is
         Unicode-aware now — letters and digits survive whatever script they are
         in — while an accent is still stripped so `Café Crème` reads
         `cafe-creme`. Nothing downstream had an opinion: ids are `TEXT` and
         `Identifier` asks only that they not be blank. The ASCII assumption
         lived in one method and would have lived there indefinitely.
      2. **The adapter read a row's `Name` before its release flag.** The real
         upstream ships unreleased psychubes as placeholder rows whose every
         field but `Id` and `Rarity` is `null`, so a conversion was refused by
         rows it was about to discard. Validate what you keep, not what you drop.
      **What landed:**
      - **`:adapters:reverse-1999`**, a Gradle module. `ModuleBoundaryTest` gains
        an `adapters` layer that only `:app` may reach, so a core module
        depending on a title is a build failure rather than a code review.
        `UpstreamAdapters` in `:app` is the only place in the product that names
        an adapter — one entry, one module, and that is the whole cost of a game.
      - **`UpstreamAdapter`**, the port, in `gamedata.ingest`. Files in, bundle
        out; no Spring, no database, no opinion about whether the result is fit
        to publish.
      - **`CanonicalBundleWriter`**, the parser read backwards, so an adapter's
        output becomes a *file* a human approves rather than an object that
        skips the approval. Pinned to the parser by
        `CanonicalBundleRoundTripTest` over both `proving-ground` fixtures, plus
        an idempotence case — a writer whose output drifted would fill every
        patch diff with noise.
      - **`gamedata-cli` gains `adapters` and `adapt`**, so the loop is now
        *adapt, preview, ingest, publish* and the adapter gets no privileges for
        being code.
      - **`tools/fetch-upstream.sh`** (committed mode `100755` — the B4 lesson),
        pinned to two upstream commits so the patch is a real interval of
        somebody else's history rather than one we composed.
      **The adapter converts less than the upstream publishes and says so on
      every run** — `skipped 590 resonance-pattern cost row(s)`, `skipped 4
      unreleased character(s)`, `skipped 1 stage(s) costing no Activity`. Each
      exclusion has a reason in `KornblumeAdapter`'s javadoc. The two worth
      remembering: `shops.json` states no currency and no price, and inventing
      them would put fake prices in the optimizer's source set (*a wrong number
      gets used, a missing one gets noticed*); and Frequency rows are
      alternatives for one step, which an `Upgrade` cannot express without
      charging a player for all four.
      **Verified twice, as usual, and the hand check is what proves it.** The
      whole loop was run against `docker compose` Postgres from the jar:
      `adapt` → `ingest` → `publish` for 3.3, then 3.5, then `diff`, then the API
      curled. Real answers: `GET /entities/regulus/upgrades` says Insight 2 costs
      40 000 Sharpodonty and 10 Scroll of Starlit Ascent, *in named items*;
      `GET /entities/igor?version=0` is a 404 reading *"no entity 'igor' in
      reverse-1999 3.3"*, because Igor was released in 3.5 — version pinning
      doing exactly what it exists for, against data nobody here authored. The
      published snapshot is 91 items, 99 stages, 50 crafts, 151 entities and
      2 012 upgrades.
      **Also settled:** **F1** deferred with a reason (Kornblume credits 必要的记录
      in every attribution; going direct is a second adapter, worth doing when
      the consolidated numbers are shown to be wrong, not on principle). **F2**
      is no longer a follow-up — it is a *release blocker*, and the ADR says so.

- [x] ~~**N10 — Confirm the pipeline is green on this session's work.**~~ **Done
      — green, twice, and both specific things checked in the log rather than
      inferred from a tick.** Run `34035918992` (`ci` on the `dev` PR, head
      `9b592b4`, 1m32s) and run `34035923889` (`ci` on the `main` push merging
      PR #6, head `5e4b9f4`, backend job 1m49s). Both success; `deploy` skipped
      as designed.
      1. **The new Gradle module builds on the runner.** All 18
         `KornblumeAdapterTest` cases PASSED there, including the two that
         mattered most for a first run on Linux — the Cyrillic slug case and the
         resource-directory lookup — plus all 3 `CanonicalBundleRoundTripTest`
         cases.
      2. **`RealUpstreamPatchTest` shows as three SKIPPED**, on both runs. Not
         passed, so no snapshot leaked onto the runner; not failed, so the skip
         condition is right. This is the designed state and it is the one to
         keep re-checking: the day those three turn green on CI without somebody
         deciding to make them, upstream data has been committed by accident.
      **No slowdown from the extra module** — the PR job went 1m26s → 1m32s and
      the main backend job went 2m8s → 1m49s. Six seconds for a module and 18
      tests.

### Done 2026-09-07 (seventh session) — N11 and N13

- [x] ~~**N11 — Open Phase 2, the optimizer core.**~~ **Opened and most of the
      way through it.** The MIP, demand resolution, crafting recursion, integer
      runs, explanation output and the cache key all landed; **both halves of the
      phase's exit criterion are not met, and only one of them is close** — see
      N12. 51 new tests, 93 → 144.
      **The warning in the old text was right and was worth writing down:**
      `Stage.potentialOutput()` rounds a yield up to an integer and is a
      catalogue figure, not a constraint coefficient. `YieldTable` reads
      `Drop.expectedYield` and nothing in the model touches `potentialOutput()`
      except the reachability check, where a rounded quantity cannot mislead
      because only the item's identity is read.
      **What landed:**
      - **`DemandResolver`** — the goal-to-items graph walk, backwards from the
        target so that an entity the player does not own and one part-way up a
        track are the same traversal. Refuses by name rather than guessing:
        unreachable state, unknown entity, probabilistic goal, a state reachable
        two ways (that is a choice, and a choice belongs to the solver).
      - **`EnergyMip`** — ojAlgo, integer runs and conversions, inventory
        subtracted, reachability pruning, and **variable bounds**, which is the
        single change that made a real patch solvable rather than a crash.
      - **`YieldTable`** — measured-or-declared, and the place a genuine unit
        mismatch between `stats` and `planner` is held at arm's length. **Q8.**
      - **`MipOptimizer`** and **`SolveKey`** — the port, the version pinning,
        the plan fingerprint, and an explanation with real marginal costs.
      - **[ADR 0010](docs/adr/0010-a-plan-is-the-best-provable-in-the-budget.md)**
        — what a plan actually promises, and the sharpened reversal trigger for
        ADR 0004.
      **Two defects found by real data, which is the argument for the real-data
      tests existing at all:**
      1. **A craft that consumes nothing is a mint.** The upstream lists base
         materials in `formulas.json` with an empty ingredient array; the adapter
         converted them into zero-cost unbounded sources, and the optimizer's
         first real solve crafted **294 250 Sharpodonty out of nothing**. Fixed
         in three places, deliberately: `Craft` refuses the shape (every future
         upstream), `CanonicalBundleParser` refuses it with a located message (a
         human approving a bundle), and `KornblumeAdapter` skips those twelve
         rows and prints that it did. The adapter *already* had this rule for
         zero-cost stages — *a free source is an unbounded one* — and nobody had
         applied it to conversions.
      2. **ojAlgo blew the JVM stack on a real patch.** Branch-and-bound recurses
         per node and dives depth-first with no incumbent, and an unbounded
         integer variable is an unbounded dive. Three fixes, in order of how much
         they bought: bound every variable, keep the objective integral (the
         millionth-weight conversion tie-break is now applied only when the craft
         graph actually has a cycle), and loosen the gap tolerance from seven
         significant digits to four. A solver failure is now distinguished from
         an infeasible goal, because "impossible" and "the solver fell over" are
         different answers.
      **And a fact about the game rather than about us:** **45 of 118**
      characters' Insight 2 cannot be planned from patch 3.5, because their
      materials appear only against the upstream's synthetic `Unreleased` stage.
      The optimizer refuses those by name. That number will move with every
      snapshot and is not a defect in anything.
      **Measured, on real data, and only reproducible locally:** p95 **1.8 s**
      over 50 solves of a five-character goal set (93 stage variables, 11 crafts,
      23 constraints), total 2 411 Activity, and the plan states it is **within
      2.95%** of anything that could exist. The search does *not* prove
      optimality inside the budget — it is stopped by it, every time. That is
      ADR 0010's whole subject and it is the number to re-read before deciding
      ojAlgo has passed.

- [x] ~~**N13 — Confirm the pipeline is green on this session's work.**~~ **Done
      — green, twice, and both checks read out of the log rather than inferred
      from a tick.** Run `34072743411` (`ci` on the `dev` PR, backend 1m37s) and
      run `34072752969` (`ci` on the `main` push merging PR #7, backend 1m56s).
      Both success; `deploy` skipped as designed. No slowdown worth the name for
      a whole new source set and 51 tests — the main backend job went 1m52s →
      1m56s.
      1. **The planner module's 30 tests executed on the runner.** All 30, all
         PASSED. This was the specific worry: a new test source set in an
         existing module that silently is not picked up looks identical to a
         green build.
      2. **`RealUpstreamPlanTest` shows as seven SKIPPED**, and
         `RealUpstreamPatchTest` as three — ten in all. Not passed, so no
         snapshot leaked onto the runner; not failed, so the skip conditions are
         right. **This is the state to keep re-checking**: the day any of those
         ten turn green on CI without somebody deciding to make them, upstream
         data has been committed by accident.
      **One correction it forced, and it is the reason to read the log:** this
      entry predicted *six* skips and the truth is seven. The local suite is
      **144** tests, not the 143 written down at the end of the session — the
      count was taken before the last test was added and then repeated three
      times without being recomputed. Fixed everywhere above. A number carried
      forward is a number nobody re-derived.

### Done 2026-09-07 (eighth session) — N12

- [x] ~~**N12 — Close Phase 2: the five benchmark goal sets.**~~ **Done, and it
      found a bigger thing than it was looking for.** The community answers are
      transcribed, cited and compared in
      [docs/benchmarks/reverse-1999-community-answers.md](docs/benchmarks/reverse-1999-community-answers.md);
      the comparison is `CommunityBenchmarkTest`, five tests, skipping without a
      snapshot like everything else that touches real data.
      **The source** is Prydwen's *Insight Materials Cheat Sheet (2.7 patch)*,
      read 2026-09-07 — twenty claims of the form *material → best stage → drop
      rate*. Nothing is vendored: twenty short factual claims with a URL is a
      citation, and ADR 0009 is about data files.
      **The defect it found before it ran once, which is the session's headline:**
      `KornblumeAdapter` was reading `stages.json`, and the upstream's own planner
      reads the newest `stages<major>_<minor>_greedy.json`. At both pinned commits
      `stages.json` carries **chapters 1 to 4 only** — 99 stages, overlapping the
      live 105-stage table by 40, with a drop-to-stage mapping the game has since
      retuned. **Two thirds of the game were missing for five sessions and every
      green thing stayed green.** The community names stages our data had never
      heard of, which is how it surfaced.
      Fixed in three places: the adapter picks the highest-versioned sampled table
      and divides drop counts by the sampled run count (as `normalizeDrops` does
      upstream), falls back to `stages.json`, and **says which file it read either
      way**; `tools/fetch-upstream.sh` pins the sampled table per snapshot
      (3.3 → `stages3_0_greedy.json`, 3.5 → `stages3_3_greedy.json`, because the
      drop data is resampled on its own schedule and the filename lags the label);
      and four new adapter tests cover preference, numeric version ordering
      (`stages10_0_greedy` must beat `stages3_3_greedy`), the fallback note, and a
      `count` of zero, which is a denominator and therefore a free source.
      **A previous "fact about the game" is withdrawn.** *45 of 118 characters'
      Insight 2 cannot be planned from patch 3.5* was a fact about the stale file.
      All 118 can. The test that asserted it now asserts the opposite, as a canary
      for the stage table going short again.
      **What the comparison itself says:**
      - **Five exact agreements** — Holy Silver (10-9H), Salted Mandrake (3-13H),
        Perpetual Cog (7-16H), Pyroxene Ore (9-1H), Alopecurus Pratensis (8-18H).
        That is the exit criterion, on its own terms. Seven more agree within 8%.
      - **Seventeen of the guide's quoted rates land within three percentage
        points** of a sample this project had never seen. Two independent
        measurements of the same game agreeing is the strongest evidence yet that
        the pipeline carries real numbers rather than plausible ones.
      - **The plan beats the advice**: 3 624 Activity against 4 017 for following
        the guide material by material — and the 4 017 does not cover the whole
        demand. One run drops five materials; a per-material plan pays for each.
      - **Four items in the guide's list are the same items under different
        English names**, and the rates are what identified them: Red Lacquer Slab
        is Red Lacquer Tablet, Golden Grass Incense is Golden Herb Incense,
        Luminite Ore is Pyroxene Ore, and Fox Tail is Alopecurus Pratensis, which
        is literally foxtail grass.
      - **Every disagreement over 25% is a sample-size disagreement.** See N17.
      **A third defect, found by the suite rather than by the benchmark.** With
      the new stage file the schema round trip failed on drop *ordering*:
      `Stage.drops()` is a `List`, the adapter emitted the upstream's JSON key
      order, and `JdbcGameDefinitionRepository` reads drops back ordered by
      `stage_drop.item_id` — a surrogate handed out in catalogue-ingest order.
      They coincided under `stages.json` and do not under the sampled tables, so
      *"the schema gives the data back unchanged"* had been true by coincidence.
      Both ends now sort by item id and an adapter test pins it.
      **The instruction in the old text was right and is worth keeping:** judge a
      disagreement against the model before the solver. Not one of these turned
      out to be the search. They were the input file, the sample size, and what
      the word "best" means.

### Done 2026-09-08 (ninth session) — N16

- [x] ~~**N16 — Get a clean local run and a green CI, then tick Phase 2.**~~
      **Done. Phase 2's box is ticked.** The eighth session's work had in fact
      been committed and pushed after this entry was written — `dev` is
      `93f4de4`, merged to `main` as **PR #8** (`d34a31b`) — so what this session
      owed was the half the entry insisted on: reading the log rather than the
      tick.
      **What the log says**, for run `34116854550` (`dev` PR, head `93f4de4`,
      1m 24s) and run `34116880372` (`main` push, head `d34a31b`, 1m 46s), both
      success: **139 PASSED, 15 SKIPPED, 0 FAILED**, `BUILD SUCCESSFUL in 1m 5s`.
      Both checks the entry named are satisfied — `:adapters:reverse-1999:test`
      ran and **all 24 of its tests PASSED on the runner**, and the 15 skips are
      exactly the three snapshot-gated classes and nothing else:
      `CommunityBenchmarkTest` 5, `RealUpstreamPatchTest` 3, `RealUpstreamPlanTest`
      7. Nothing that touches upstream data passed on CI, which is the state ADR
      0009 requires; a pass there would have meant a snapshot had been committed
      by accident.
      **The local half was not re-run and did not need to be.** It was green on
      this tree last session (154 tests) and the working tree is clean at the
      same commit — re-running proves the machine, not the code.
      **Phase 2 is closed on its criterion, and Phase 3 is not opened.** The
      criterion is two measurements and both are made; the phase's scope still
      has N17, N14 and N15 in it, and N17 goes first because a sample size the
      model throws away is currently picking stages.
      **Worth keeping:** the tracker said "nothing is committed" and the
      repository disagreed. Both were written by the same session. Push before
      writing the entry that describes the push, or the file lies in the one
      direction it is meant to prevent.

### Next session starts here

- [x] ~~**N17 — Give `stats` a mean per run and a sample size, and let the
      optimizer see it. This is Q8.**~~ **Done 2026-09-08, and it bought more
      than it was asked for.** The sample size now travels the whole pipeline —
      `Drop.sampledRuns`, the canonical bundle (optional; absent means
      *declared*), `V4` on `stage_drop`, both JDBC directions, the patch diff —
      and `YieldTable` hands the solver the lower end of a 95%
      `PoissonRateInterval` on the mean instead of the mean itself.
      [**ADR 0011**](docs/adr/0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md)
      records it and narrows ADR 0006 rather than superseding it: Wilson stays
      right for *"did it drop"*, and *"how many dropped"* is a rate, unbounded
      above, and needs the counting analogue.
      **What it measurably changed**, all from `CommunityBenchmarkTest` and
      `RealUpstreamPlanTest` on the real snapshot:
      - **Exact agreements with the community guide went from five to nine.**
        Bifurcated Skeleton (10-13H), Clawed Pendulum (11-5H), Goose Neck (5-4H)
        and Red Lacquer Tablet (9-1H) joined the five, in every case because this
        project had been preferring a thinly sampled stage with a flattering
        mean. **The change was made for the evidence, and the agreement followed;
        that ordering is the only thing that makes it worth reporting.**
      - **Disagreements over 25% went from four to three.** Rough Silver Ingot
        fell from 29% to 7.9%. Milled Magnesia (145%) and Liquefied Terror (210%)
        shrank and did not resolve, and **that is the honest outcome**: a 95%
        bound discounts a thin sample in proportion, it does not dismiss it, so a
        105-run mean four times higher still wins. Both remaining rows still have
        a small sample on one side, which is what the test asserts.
      - **The plan costs 7% more** — 3 624 → 3 880 Activity on the same goal set,
        against the guide's 4 017. The old number was optimistic rather than
        cheap. p95 is **1 805 ms** (budget 2 s) and the optimality gap **2.37%**.
      **Three things worth carrying forward:**
      1. **The benchmark was ranking on numbers the solver does not use.** It
         computed Activity-per-unit from `Drop.expectedYield()` directly, so with
         N17 in place it would have gone on reporting the old ranking forever. It
         now ranks on `YieldTable`'s coefficients and prints the raw-point-estimate
         ranking beside them. A benchmark that does not consume what production
         consumes measures the wrong thing quietly.
      2. **`count: 1` in this upstream is a declaration, not a sample.** All 14
         such stages are the Insight and Resource ones with flat payouts, and
         every sampled stage carries at least 105 runs. Reading that 1 as a
         sample would have put a 95% bound on a number nobody measured and made
         the only source of several currencies look unfarmable. The judgement
         lives in the adapter, which is the module allowed to know a game.
      3. **`stats` owns the statistics and `gamedata` only carries the number.**
         `PoissonRateInterval` is beside `WilsonInterval`; `Drop` holds a count
         and says in prose where the decision about it lives.
- [ ] **N14 — Give the solver a time axis, and with it shops, rewards and
      rotation.** The largest thing the model does not do, and the one that makes
      `FEWEST_DAYS` a different plan from `LEAST_ENERGY` rather than the same one
      divided by a constant. It is also what unblocks the three source kinds
      currently refused by name: a shop's cap is "n per period" and a reward's is
      "once per day", and neither has an honest place in a model with no days in
      it. Expect this to be the expensive half of Phase 2.
      **Note for whoever starts it:** the upstream does publish `shops.json`, and
      the fetch script does not fetch it. It states `{Material, Quantity}` under an
      opaque shop key with no currency, no unit price and no reset period, which
      is why `KornblumeAdapter` refuses it — but read it before designing the time
      axis rather than after.
      ~~**Do N17 first.**~~ N17 is done (2026-09-08), so this is next — with one
      thing it inherits: yields are now discounted for their sample, so a time
      axis is being added over coefficients that are conservative rather than
      central. Do not "fix" that by taking the point estimate back when the model
      grows; ADR 0011 has the reversal trigger and it is a measurement, not a
      preference.
- [ ] **N15 — Cache a solve on its key, and implement `SolveCoordinator`.**
      `SolveKey` exists and nothing uses it. The single-node coordinator is
      explicitly Phase 2's ("a single-node implementation ships in phase 2 and
      stays"), and it is the queue Phase 9 later replicates. Note before
      building: **drop estimates are deliberately not in the key yet** — nothing
      publishes any — and they must go in the moment Phase 6 does, or a plan
      cached against yesterday's rates is served as today's.
- [ ] **N4 — Enforce that `Entity.kind` is never read outside the catalog.**
      ADR 0007 asserts it and nothing checks it. `GameAgnosticismTest` scans the
      guarded sources for game slugs, not for field reads, so a `kind`-switch in
      `planner` would pass today. The natural home is an ArchUnit rule beside
      `ModuleBoundaryTest`. **Not urgent and deliberately not written yet** — the
      guarded modules are empty, so the rule would pass vacuously and prove
      nothing. Write it with the first real planner code, in Phase 2.
- [ ] **N5 — Upgrade the CI actions before they break.** Green runs, warning
      twice, and both warnings are on a clock:
      1. **Node 20 is deprecated.** Six actions target it and are already being
         *forced* onto Node 24 by the runner: `actions/checkout@v4`,
         `setup-java@v4`, `upload-artifact@v4`, `gradle/actions/setup-gradle@v4`,
         `gradle/actions/wrapper-validation@v4` in the backend job, and
         `actions/setup-node@v4` in the frontend job. Forced today, unsupported
         tomorrow.
      2. **`setup-java@v4` is deprecated outright** — migrate to `@v5`.
      Not urgent, and deliberately kept out of both the Phase 0 close and the
      schema commit: bumping six actions at once is a change that wants its own
      green run to attribute a failure to. Do it as a standalone PR while the
      pipeline is quiet, not tangled into the ingestion work.

---

## Phase board

Exit criteria are copied from the plan verbatim. Do not start a phase until the
previous one's criterion is met.

### Track A — product

- [ ] **Phase 0 · Ground** — 1 week — **closed by exception 2026-09-05, box
      deliberately left unticked**
      Repo, CI, Docker Compose, ADR folder, hello-world deployed to production
      before any real code. Read Kornblume and Penguin Statistics. Write the
      positioning paragraph.
      **Exit:** a green pipeline deploying a health endpoint to a real URL.
      **⚠ Not met as written — see deviation D1.** Closing on "green pipeline,
      deploy deferred" is an exception, not the criterion, and the box stays
      unticked to say so. A ticked box here would be the tracker telling the
      next session a lie about what this project has actually shipped.
      **Landed:** repo, git, ADRs 1–7, compose file, domain model, a green local
      backend build, a verified frontend build, the prior-art read, the
      positioning paragraph, **CI green on the current tree** (`a82da1e`, runs
      `33964292888` and `33964297527`, with `ApplicationBootTest` executing on
      the runner), and **an application that actually boots and serves
      `/api/health`**.
      **Still missing, and only this:** a real deploy. Deferred by D1, not
      coming before Phase 4. Every other item in this phase that could be met,
      has been.
      **The box gets ticked when, and only when, a real URL answers 200.**

- [x] **Phase 1 · Game data foundation** — 2.5 weeks — **closed 2026-09-06**,
      opened 2026-09-05. Two days, not 2.5 weeks, and that is worth being
      suspicious of rather than pleased about — the estimate assumed hunting for
      data and negotiating for it, and what actually happened is that the
      licensing question was deferred rather than answered (ADR 0009). The
      engineering was the small half.
      Canonical schema, R1999 ingestion (items, stages, characters, upgrade
      costs), versioned publishing with diffs, `gamedata-cli`. Tests over real
      patch data, including a patch that changes something. Includes the catalog
      axis — stat curves, skills, talents — modelled and ingested *now*, because
      retrofitting a second data axis into a published schema later is miserable.
      **Exit:** the API answers "what does Insight 2 cost?" and "what does her S2
      do at rank 3?"; a patch diff report renders for both axes.
      **Landed:** the canonical schema (`V2`, 28 tables, plus `V3` making a
      version deletable), the catalog axis, `GameDataSchemaTest`, the canonical
      bundle format and its parser, JDBC ingest and read (ADR 0008), versioned
      publishing with a draft/approve split, the three-axis patch diff,
      `gamedata-cli`, the API that serves all of it, and — 2026-09-06, sixth
      session — **the R1999 ingestion the phase is actually named after**:
      `:adapters:reverse-1999`, `CanonicalBundleWriter`, `--gamedata=adapt`, and
      two real patches through the whole pipeline.
      **The schema holds data and returns it unchanged**
      (`GameDataIngestTest.roundTripsWithoutLoss`, record equality across the
      whole graph, and `RealUpstreamPatchTest` over real data), **the API answers
      both exit questions** (`GameDataApiTest`, over real HTTP), and **the data
      is now somebody else's**.
      **Both halves of the criterion are met:**
      - *the API answers "what does Insight 2 cost?" and "what does her S2 do at
        rank 3?"; a patch diff report renders for both axes* — **met, 2026-09-06.
        N7.** `GET /api/games/{game}/entities/{entity}/upgrades`, `GET
        .../entities/{entity}`, `GET .../diff?from=&to=` in JSON or plain text.
      - *tests over real patch data* — **met, 2026-09-06. N8.** Kornblume
        snapshots at 3.3 and 3.5, taken from the upstream's own history, through
        adapt, ingest, publish and diff. The patch changes something on both
        axes: four characters released, one material and four recipes added,
        euphoria and mastery tracks appearing on existing characters.
      **CI confirmed the tree on 2026-09-06 (N10)** — runs `34035918992` and
      `34035923889`, both success, with all 18 adapter tests executing on the
      runner and `RealUpstreamPatchTest` correctly skipping. **The box is ticked
      on that.**
      **Two qualifications the tick does not erase, and they travel with the
      phase rather than closing with it:**
      1. **CI does not run the real-data tests** — the snapshots are not
         redistributable, ADR 0009. The criterion is met locally and
         reproducibly, not on the runner. Unlike Phase 0's D1, this is *not* an
         exception to the criterion: the criterion says "tests over real patch
         data", not "tests CI runs". But it does mean a regression in the
         adapter can reach `main` green, and that is a standing risk, not a
         solved problem.
      2. **The catalog half is proven on real data only for stat curves.**
         Kornblume publishes no skill or talent text, so *"what does her S2 do at
         rank 3?"* is answered from the synthetic fixture. The schema, the API
         and the diff all handle it; this upstream has nothing to put in it.

- [x] **Phase 2 · Optimizer core** — 2 weeks — **closed 2026-09-08**, opened
      2026-09-07
      ojAlgo MIP model, crafting recursion, integer runs, solve caching,
      explanation output, both objectives.
      **Exit:** agrees with community-accepted answers on 5 benchmark goal sets;
      p95 solve under 2s.
      **Landed:** `DemandResolver`, `EnergyMip`, `YieldTable`, `MipOptimizer`,
      `SolveKey`, and [ADR 0010](docs/adr/0010-a-plan-is-the-best-provable-in-the-budget.md).
      Crafting recursion, integer runs, explanation output and both objectives
      are done; solve caching has its key and no cache.
      **Both halves of the exit criterion are met and measured, and CI has
      confirmed the tree (N16, 2026-09-08): runs `34116854550` (`93f4de4`) and
      `34116880372` (`d34a31b`, PR #8), both success, 139 passed / 15 skipped /
      0 failed, the adapter's 24 tests executing on the runner. The box is
      ticked on that.**
      **Two things the tick does not close, and they travel with the phase:**
      *the phase's own scope is not exhausted* — solve caching has a key and no
      cache (**N15**), there is no time axis (**N14**), and drop rates carry no
      sample size (**N17**) — and *the criterion was measured by tests CI does
      not run*, exactly as in Phase 1. Both halves of the criterion rest on
      `RealUpstreamPlanTest` and `CommunityBenchmarkTest`, which skipped on both
      of the runs above. Reproducing it is `backend/tools/fetch-upstream.sh` and
      one flag; it is not something a green pipeline will ever tell you.
      - *p95 solve under 2s* — **met, 2026-09-07.** 1.8 s over 50 solves of a
        five-character Reverse: 1999 goal set. Measured on real data, locally
        only, by a test CI does not run. **Read it as the budget rather than as a
        measurement of difficulty:** median, p95 and max all land within 5 ms of
        each other, because the search is stopped by its budget every time. The
        number that means something is the 2.80% optimality gap beside it.
      - *agrees with community-accepted answers on 5 benchmark goal sets* —
        **met, 2026-09-07 (eighth session).** Five materials where the cheapest
        stage this project computes is the stage a published guide names, out of
        twenty claims compared, with every disagreement traced to a cause. **N12**,
        and [the benchmark](docs/benchmarks/reverse-1999-community-answers.md).
      **Four things the tick, when it comes, must not be read as covering:**
      1. **The model has no time axis**, so no shops, no rewards, no weekday
         rotation, and `FEWEST_DAYS` is `LEAST_ENERGY` with a division. Items
         whose only source is a shop or a reward are refused by name rather than
         costed. **N14.**
      2. **The search is stopped by its budget, not finished by it.** The plan
         says so and says the size of the doubt (2.95% on the measured goal set).
         ADR 0010.
      3. ~~**Drop rates carry no sample size, and it is currently choosing
         stages.**~~ **Fixed the same day, 2026-09-08, N17 and ADR 0011.** Yields
         carry the runs behind them and the solver gets a 95% lower bound rather
         than the observed mean. What replaces this caveat is smaller and still
         real: **the discount is only as good as its Poisson assumption**, which
         is conservative for a single-drop stage and optimistic for a multi-drop
         one, and **it cannot rescue a 105-run sample** — two of the four large
         disagreements with the community survived it, both on thin evidence.
      4. **Fodder is in the domain model and not in the solver.** Advancing an
         item by consuming other items of a class is a sink the demand vector
         does not express yet, and it is Punishing: Gray Raven's whole
         progression system — so this is Phase 11 work arriving early or a real
         surprise late.

- [ ] **Phase 3 · Identity and player state** — 1 week
      OAuth, inventory, roster, goals, multiple profiles, sync.
      **Exit:** a plan computed end-to-end from stored state on a real account.

- [ ] **Phase 4 · Frontend v1 — and launch** — 2.5 weeks
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

**Gate status: CLOSED, and now structurally so — see deviation D1.** Nothing is
deployed and nothing is scheduled to be before Phase 4. Do not open
`almanac-store`. When Phase 7 comes round, re-read D1 and decide deliberately
whether Track B on synthetic workloads is still worth building.

### Track B — substrate

- [ ] **Phase 7 · almanac-store (LSM storage engine)** — 3 weeks
      WAL, skip-list memtable, SSTable with sparse index and bloom filters,
      levelled compaction, range iterators, crash recovery. Wired behind
      `DropReportStore` alongside the Postgres implementation.
      **Exit:** crash-consistency fuzzing passes 10k randomized kills; benchmark
      vs Postgres published.

- [ ] **Phase 8 · almanac-raft (consensus)** — 3 weeks
      Leader election, log replication, snapshotting, joint-consensus membership.
      Exposed first as a replicated KV so it can be tested in isolation before
      anything depends on it.
      **Exit:** 5-node cluster survives repeated leader kills and partitions with
      no divergent log.

- [ ] **Phase 9 · Solver cluster** — 2 weeks
      Replicated job log, leader-assigned work, lease-based liveness, idempotent
      completion, backpressure, results over WebSocket.
      **Exit:** kill any node mid-solve — no lost solves, no duplicated solves,
      throughput recorded.

- [ ] **Phase 10 · Chaos and verification harness** — 1.5 weeks
      Partitions, delays, process pauses, kills, disk corruption. Linearizability
      checking on the replicated register. Nightly randomized runs in CI, failing
      seeds saved as regression tests.
      **Exit:** nightly suite green for 7 consecutive nights, and one real bug
      found and written up.

### Track A — closing

- [ ] **Phase 11 · Punishing: Gray Raven** — 2 weeks
      Data adapter, banner model configuration, fodder economics, probabilistic
      goals. Whatever the model has to generalise, generalise in the model.
      **Exit:** PGR live with zero game-specific code in `planner`, `gacha` or
      `stats` — and the diff to prove it.

- [ ] **Phase 12 · Hardening and the writeups** — 1 week
      Tracing, alerting, a backup actually restored from, load test with
      published numbers. Pre-rendered catalog pages, stable URLs, sitemap and
      structured data. Then three writeups: the storage benchmark, the consensus
      verification, the multi-game diff.
      **Exit:** restore drill completed from a real backup; catalog pages
      indexed; three writeups published in the repo.

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

1. **No game-specific code in `planner`, `gacha` or `stats`.** Not one
   `if (game == ...)`. Enforced by `GameAgnosticismTest`.
2. **No cross-module database reads.** Modules talk through `EventPublisher`.
   Enforced by `ModuleBoundaryTest`.
3. **Every hand-built component keeps its boring implementation**, selectable by
   `storm-almanac.substrate.*`, and the benchmark gets published either way —
   including if the boring one wins.
4. **Everything is versioned by `GameDataVersion`.** Plans, estimates and catalog
   pages record what they were computed against. A patch invalidates loudly.
5. **Ingestion is automated; publishing is a manual approval.**
6. **No game assets, ever.** Numbers and text only, attributed. Never touch the
   game client.

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

**Decision:** no money will be spent on this project, so no hosting is
provisioned. The `deploy` job in `ci.yml` is `if: false`.

**What this costs, stated plainly:**

- **Phase 0 cannot meet its exit criterion.** "A green pipeline deploying a
  health endpoint to a real URL" is not achievable without a URL. Phase 0 is
  therefore closed *with this exception noted*, not met. Do not tick it.
- **The Track B gate loses its meaning.** The gate exists so that
  `almanac-store` and `almanac-raft` are shaped by real write volume, real read
  patterns and real failure modes. With nothing deployed there is no traffic to
  observe, and the plan is explicit that infrastructure built against imagined
  requirements is a toy.
- **The headline CV claim weakens.** "I run a live tool for two games with real
  users" is the sentence this project is arranged to earn.
- **Deploy problems get discovered late.** Phase 0 puts the deploy first
  precisely because that is when it is cheapest to fix.

**Mitigation, agreed:** revisit hosting at **Phase 4**, not at the end. Phase 4's
public launch is on the plan's own "never cut" list, and it still puts real
traffic ahead of Track B. Phases 1-3 need no server, so nothing is blocked
between now and then.

**Reversal trigger:** the moment any free-tier host is acceptable, or the moment
Phase 4 is reached — whichever is sooner. Before starting Phase 7, re-read this
entry and decide consciously whether Track B is still worth doing on synthetic
workloads. It may be; that is a decision to make with open eyes, not by default.

---

## Environment notes (this machine only)

Not deviations — nothing about the design changed. These are local facts that
cost time to rediscover.

### E2 · Docker Engine 29 refuses Testcontainers' API version — fixed in the build

> **Not machine-specific in the end, which is why the fix is committed.** Every
> machine that takes the Docker 29 update hits this, and the GitHub runner will
> when it does. It sits here because that is where it was found.

Docker Engine 29 raised the minimum client API version to **1.40**. Spring Boot
3.5.6's BOM manages Testcontainers **1.21.3**, whose docker-java 3.4.2 defaults
to **1.32**, so every container-backed test dies at startup with:

```
Status 400: client version 1.32 is too old. Minimum supported API version is 1.40
```

**It looks like a broken machine, not a broken build.** 33 tests failed across
six classes with `NoClassDefFoundError`, `ExceptionInInitializerError` and
`ContainerLaunchException` — three different symptoms of one cause, none of them
naming it — while `docker ps` worked fine and the non-container tests passed.

**The fix, committed in `backend/build.gradle.kts`:**

```kotlin
tasks.withType<Test>().configureEach { systemProperty("api.version", "1.44") }
```

`api.version` is docker-java's own config key. It is a **system property on the
test task** and not an environment variable, because a Gradle test worker
inherits the *daemon's* environment rather than the shell's — `DOCKER_API_VERSION=…`
and `API_VERSION=…` on the command line both do nothing, which costs a while to
work out.

**What was tried and rejected:** upgrading Testcontainers. 1.21.4 still pins
docker-java 3.4.2, and 2.0.5 pins 3.7.1 but **renamed the module artifacts** —
`org.testcontainers:postgresql` stops at 1.21.4 — so it is a migration rather
than a version bump, and one that wants its own change and its own green run.
Note also that the docker-java *core* is shaded into the Testcontainers jar, so
forcing the external `com.github.docker-java` artifacts newer changes nothing.

**E1 came back while doing this** and the recorded workaround still works:
`-Djavax.net.ssl.trustStoreType=Windows-ROOT` for any build that resolves a new
dependency. Avast's HTTPS scanning is on again.

### E1 · Avast intercepted TLS, so Gradle could not fetch new dependencies

> **Resolved 2026-09-05, and BACK as of 2026-09-08 (ninth session).** A build
> that resolved a new dependency failed with the PKIX error below, and
> `-Djavax.net.ssl.trustStoreType=Windows-ROOT` fixed it again — so the note is
> live, not historical. The 2026-09-05 text follows.
>
> Avast's HTTPS scanning was turned off. Verified by
> `./gradlew --refresh-dependencies` with **no** truststore flag: fresh metadata
> resolved over TLS and Testcontainers came back at 1.21.3. No workaround is
> needed and none is committed. Kept below because the symptom is baffling if it
> ever returns — and it returns silently, since everything already cached keeps
> working.


Avast's HTTPS scanning is a man-in-the-middle: it re-signs every TLS connection
with its own root CA. Windows trusts that CA, so `curl` and node work
(`NODE_EXTRA_CA_CERTS` is set to Avast's `wscert.pem`). **The JDK's own
`cacerts` does not**, so any Gradle download of an artifact not already cached
fails with:

```
PKIX path building failed ... unable to find valid certification path
```

This is invisible until a **new** dependency is added — every existing one is
already in `~/.gradle/caches`, which is why builds looked fine for four
sessions. Adding Testcontainers is what surfaced it, and Phase 1 will add more.

**Workaround, used for this session's builds:**

```bash
./gradlew -Djavax.net.ssl.trustStoreType=Windows-ROOT build
```

This points the JVM at the Windows certificate store instead of `cacerts`. It
grants **no new trust** — Windows already trusts that CA — and modifies nothing.
It is not committed to `backend/gradle.properties`, because `Windows-ROOT` does
not exist on the Linux CI runner. **Open decision:** make it permanent in
`~/.gradle/gradle.properties` as
`systemProp.javax.net.ssl.trustStoreType=Windows-ROOT` (user-level, uncommitted,
machine-specific), or turn off Avast's HTTPS scanning instead. Until one of
those happens, every new dependency needs the flag.

---

## Open questions

Carry these forward until answered; strike through with the answer when resolved.

- ~~**Q1 — Hosting target.**~~ **Answered 2026-09-02: VPS, but deferred.** The
  constraint is no spend on this project. Consequence recorded under
  *Deviations from the plan* below — this is not a neutral scheduling change.
  If the constraint softens, Oracle Cloud Always Free is the option to try
  first: ARM instance with real block storage, free indefinitely rather than a
  trial, card required for identity only. Real disks matter for phase 7 anyway.
- **Q2 — Upstream data source for R1999.** *Narrowed, not closed
  (2026-09-02).* **Kornblume is not canonical** — it is a presentation layer
  over Huiji Wiki (characters), 必要的记录 (drop data) and ArkPlanner
  (algorithm). Its `public/data/*.json` is still the best consolidated *shape*
  found and maps cleanly onto our model, so treat it as a reference schema and
  cross-check. Remaining work is **F1** in `docs/prior-art.md`: evaluate
  必要的记录 as the real drop-data upstream.
  **Narrowed again 2026-09-07 (eighth session).** The drop data is not hidden
  behind Kornblume — it is in the repository, in the files this project was not
  reading. `stages<major>_<minor>_greedy.json` carries **raw drop counts and the
  number of sampled runs behind them**, which is the sampling itself rather than
  a presentation of it, and the adapter now reads it. So F1's question changes
  shape: what going to the drop-data upstream directly would buy is *fresher*
  sampling and *more* of it, not different numbers — against the cost of a second
  upstream to adapt. Do not start it before **N17** has somewhere to put a sample
  size, because that is what would make more sampling worth having.
- **Q3 — Seed data provenance.** *Answered provisionally: assume not
  redistributable.* The Kornblume repository has **no `LICENSE` file**, so it is
  all rights reserved by default — absence of a licence is not permission. We
  may read it to validate schema and numbers; we may not vendor it as seed data.
  **F2** is to ask the maintainer directly.
  **Consequence, as of the fourth session:** the acceptance fixtures are a
  synthetic title (`proving-ground`), invented here and built to exercise every
  shape the canonical format has. That is a real answer, not a stand-in — the
  plan's own cut list says a synthetic test game proves the abstraction. But it
  means the pipeline has never met somebody else's data, and **F2 is now the
  thing that unblocks the rest.** It needs a human to send a message; a session
  cannot. See **N8**.
  **Settled operationally, sixth session (2026-09-06), by
  [ADR 0009](docs/adr/0009-upstream-data-is-fetched-never-vendored.md):**
  the answer above stands unchanged and is now *enforced* rather than intended.
  Upstream data is **fetched at need and never committed** —
  `backend/tools/fetch-upstream.sh` writes into an ignored directory, and
  `RealUpstreamPatchTest` skips when it is absent. The pipeline has now met
  somebody else's data without this repository holding any of it. The committed
  fixtures are still `proving-ground` and still should be.
  **Q3 stays open in one respect only, and it is the one that matters later:**
  the project owner's scope is personal and portfolio use, and **F2 becomes a
  release blocker the moment this is deployed publicly.** Nothing about this
  entry authorises a public deployment carrying upstream numbers.
- ~~**Q6 — How is equipment modelled?**~~ **Answered 2026-09-05: it is an
  `Entity`.** [ADR 0007](docs/adr/0007-equipment-is-an-entity.md). The leaning
  was right; the reason was not the one written here. What actually decides it
  is that `Upgrade` and `Goal` key on `EntityId`, so an upgradeable thing has to
  be an `Entity` already, and the Entity/Item dual identity gear needs is the
  same one `Item`'s javadoc already grants a character's copies. Gacha turned
  out to be indifferent — `BannerModel` never mentions `EntityId`. `Entity`
  gained a required opaque `kind`, catalog-only; enforcing "catalog-only" is
  **N4**. **F4 is closed and Phase 1 ingestion is unblocked.**
- ~~**Q8 — A drop estimate is a proportion; the optimizer needs a quantity.**~~
  **Answered and closed 2026-09-08 (ninth session) by N17 and
  [ADR 0011](docs/adr/0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md).**
  The answer is the one this entry kept circling: a yield is a **mean per run**,
  it carries the **number of runs** behind it, and the interval for it is the
  Poisson score interval rather than Wilson's. `PoissonRateInterval` is in
  `stats`, `Drop.sampledRuns` is in the domain and the schema, and `YieldTable`
  gives the solver the lower bound. ADR 0006 is **narrowed, not superseded** —
  Wilson is still right for a proportion, and `DropEstimate` is still one until
  Phase 6 publishes means instead. **What is left of it is Phase 6 work and is
  written into `YieldTable`'s javadoc:** a measured estimate is still a
  proportion, still usable only where the units agree, and still not discounted
  for its own sample. The history below is kept because it is how the answer was
  arrived at.
  *Original entry:*
  *Opened 2026-09-07, seventh session.* `DropEstimate.pointEstimate` carries a
  Wilson score interval, which makes it a binomial **proportion**: the share of
  runs that yielded the item. The MIP constraint is
  `sum over stages of runs * yield >= demand`, so its coefficient is an
  **expected quantity per run**, and `Drop.expectedYield` already is one —
  unbounded above, and real upstream tables carry values well past 1.0
  (`docs/prior-art.md` §4.1). The two are equal only for an item that drops at
  most once per run.
  **Held at the boundary for now.** `YieldTable` prefers a measured estimate only
  where `Drop.isExpressibleAsProbability()` says the units agree, and uses the
  declaration everywhere else. That is correct and it is a workaround: it means a
  multi-drop stage can never benefit from being measured, which is exactly the
  stage where the declared number is least trustworthy.
  **The real answer is that `stats` owes `planner` a mean per run and a sample
  size, not only a proportion** — and a mean per run needs a different interval
  than Wilson's, which is a statement about a binomial. It may supersede
  [ADR 0006](docs/adr/0006-wilson-intervals-for-drop-rates.md) in part: Wilson
  stays right for "did it drop", and "how many dropped" is a different question.
  **Escalated 2026-09-07 (eighth session): this is no longer a boundary
  formality to settle in Phase 6, it is choosing plans today.** Two things
  changed. First, the sample size exists and we are throwing it away: the
  upstream's sampled stage tables carry a `count` per stage — the number of runs
  its drop counts were observed over, from **105 to 41 212** — and the adapter
  divides by it and drops it, because `Drop` has nowhere to put it. Second, the
  community benchmark showed what that costs: **every disagreement with a
  published guide over 25% is a hundred-run mean outranking a well-sampled one.**
  Milled Magnesia prefers 4-5H (105 runs) to the guide's 5-8H by 154%; Liquefied
  Terror prefers 4-4H (113 runs) to 9-3H by 224%. A noisy mean is a high mean
  about as often as a low one, and `argmin` picks whichever stage got lucky.
  Now **N17**, and it is ahead of the time axis in the queue.
- **Q4 — Rate verification.** The pity numbers in `PityRuleTest` come from the
  secondary sources the plan cites. They must be checked against in-game
  disclosure before the simulator ships (Phase 5).
- ~~**Q5 — Repository host.**~~ **Answered: GitHub**, HTTPS remote at
  `github.com/kietnt4412/storm_almanac`. Superseded by Q7 below.
- ~~**Q7 — How is CI status checked from a session?**~~ **Answered
  2026-09-05: authenticated `gh`, by full path.** The user ran `gh auth login`
  by hand (interactive, handles credentials — a session cannot drive it); the
  account is `kietnt4412`, HTTPS, token scopes `repo`, `workflow`, `read:org`,
  `gist`. **Two gotchas, both now paid for:** `gh` is not on `PATH`, so use
  `C:\Program Files\GitHub CLI\gh.exe`; and PowerShell will not execute a
  quoted path without the call operator `&`. The recipe that works:

  ```powershell
  & "C:\Program Files\GitHub CLI\gh.exe" run list --limit 8
  & "C:\Program Files\GitHub CLI\gh.exe" run view <id> --log
  ```

  Confirmed independently this session: the repo is private, so
  `api.github.com` **404s anonymously** — there is no unauthenticated read-only
  path, and screenshots were the only prior option. No more screenshots.

---

## Session log

Append one entry per session. Newest first.

### 2026-09-08 (ninth session) — the pipeline confirms the tree, and a number the model was throwing away

**Two items: N16 and N17.** Taking them in order.

#### N16 — the pipeline confirms the tree

The eighth session left Phase 2's box unticked with a single
condition attached — CI green on this tree, read from the log rather than
assumed. That condition is now met and the box is ticked.

**The tracker and the repository disagreed about the starting state, and the
repository was right.** N16 said "nothing is committed or pushed". In fact `dev`
was at `93f4de4` on the remote and had been merged to `main` as PR #8
(`d34a31b`): the eighth session did the push *after* writing the entry that
described it as outstanding. Nothing was lost, but the file spent a day telling
the next session a smaller lie than it exists to prevent. **Tick and write as the
work lands.**

**What CI actually says**, both runs success:

| Run | Trigger | Head | Time |
|-----|---------|------|------|
| `34116854550` | `dev` pull request | `93f4de4` | 1m 24s |
| `34116880372` | push to `main` (PR #8) | `d34a31b` | 1m 46s |

Counted out of the downloaded log rather than from the green check: **139
PASSED, 15 SKIPPED, 0 FAILED**, `BUILD SUCCESSFUL in 1m 5s`. The two checks N16
named:

- `:adapters:reverse-1999:test` ran and **all 24 of its tests passed on the
  runner** — the adapter rewritten last session for the sampled stage tables is
  exercised by the pipeline, not only by this machine.
- The 15 skips are **exactly** the three snapshot-gated classes:
  `CommunityBenchmarkTest` 5, `RealUpstreamPatchTest` 3, `RealUpstreamPlanTest`
  7. None of them passed, which is the state
  [ADR 0009](docs/adr/0009-upstream-data-is-fetched-never-vendored.md) requires:
  a pass there would mean upstream data had been committed by accident.

The local half was not re-run. It was green on this tree last session (154
tests), the working tree is clean at the same commit, and re-running it would
have measured the machine rather than the code.

**Phase 2 is closed on its criterion. It is not finished, and the distinction is
the point.** The criterion is two measurements — p95 under 2 s, agreement with
community answers on five goal sets — and both are made. The phase's *scope*
still contains a cache that does not exist (N15), a model with no time in it
(N14), and — when this was written, an hour before N17 closed — drop rates with
no sample size. The box records the criterion, the caveats beneath it record the
rest, and **Phase 3 is deliberately not opened**.

**The standing caveat got no weaker.** Both halves of the criterion this tick
rests on were measured by tests that skipped on both of those green runs. A
green pipeline confirms the tree compiles and the game-agnostic parts hold; it
has never once confirmed that this project's numbers are right.

#### N17 — the sample size reaches the solver

**The finding it acts on, restated once:** the upstream publishes how many runs
each stage's drop counts were observed over — 105 to 41 212 — the adapter divided
by it and discarded it, and every disagreement over 25% between this project's
stage ranking and a published guide was a hundred-run mean outranking a
well-sampled one. `argmin` cannot tell a good stage from a lucky one.

**What was built**, in the order the old N17 entry laid out and it was the right
order:

1. **`Drop` carries `sampledRuns`**, and it travels: canonical bundle (optional,
   and absent means *declared*), `V4` on `stage_drop` (default 0, a non-negative
   check, and a comment saying why 0 cannot mean "measured over nothing"), both
   JDBC directions, the patch diff (one fact, `0.21 over 105 runs`, so a resample
   does not read as two unrelated changes).
2. **`PoissonRateInterval` in `stats`** — the counting analogue of Wilson's score
   interval, `(C + z²/2 ± z·sqrt(C + z²/4)) / n`. Unbounded above, non-negative,
   and `[0, z²/n]` when nothing has been seen.
3. **`YieldTable` gives the solver the lower bound** wherever a sample exists and
   the declared value where none does. The plan says so in its notes.

[**ADR 0011**](docs/adr/0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md)
records the decision and **narrows ADR 0006 rather than superseding it**: Wilson
is still right for *"did it drop"*; *"how many dropped"* is a different question.
**Q8 is closed.**

**What it changed, measured on the real snapshot:**

| | before | after |
|---|---|---|
| Exact agreements with the guide | 5 | **9** |
| Disagreements over 25% | 4 | **3** |
| Plan cost, five characters to Insight 2 | 3 624 | **3 880** (guide: 4 017) |
| p95 solve / optimality gap | 1 804 ms / 2.80% | 1 805 ms / **2.37%** |

Bifurcated Skeleton, Clawed Pendulum, Goose Neck and Red Lacquer Tablet joined
the five, each because this project had been preferring a thin sample with a
flattering mean. **The plan getting 7% dearer is the same fact from the other
side** — the old number was optimistic, not cheap — and the margin over the
guide's own advice narrowed from 393 Activity to 137.

**Two of the four large disagreements survived, and that is the honest result.**
A 95% bound discounts a thin sample in proportion; it does not dismiss it, so a
105-run mean four times higher still wins. Milled Magnesia (145%) and Liquefied
Terror (210%) belong to the evidence, not to the model, and the fix for them is
more sampling — which is now what Q2/F1 would buy.

**Three things this session learned that are worth more than the feature:**

1. **The benchmark was not measuring what production does.** It ranked stages on
   `Drop.expectedYield()` directly, so N17 could have landed in full and the
   benchmark would have gone on printing the old table forever, agreeing with
   itself. It now ranks on `YieldTable`'s own coefficients and prints the raw
   ranking beside them. **A benchmark that does not consume what the product
   consumes measures the wrong thing, quietly** — the same failure shape as the
   stale stage file, one level up.
2. **`count: 1` is a declaration, not a sample.** All 14 such stages in both
   pinned snapshots are the Insight and Resource ones paying a flat 9 000
   Sharpodonty or 2 Pages; every sampled stage carries at least 105 runs. Reading
   the 1 as a sample would have put a 95% bound on a number nobody measured and
   made the only source of several currencies look unfarmable. Adapters are where
   an upstream's conventions get translated, and this is one.
3. **A "green build" needed a build fix before it could be green at all.** Docker
   Engine 29 refuses the API version Testcontainers 1.21.3 speaks, so 33 tests
   failed across six classes with three unrelated-looking symptoms while
   `docker ps` worked fine. Fixed by `systemProperty("api.version", "1.44")` on
   the test tasks; upgrading Testcontainers is a migration, not a bump, because
   2.x renamed the artifacts. **Environment note E2**, and **E1 came back** while
   working on it — the Windows-ROOT truststore flag is needed again.

### 2026-09-07 (eighth session) — the community's answers, and the two thirds of the game we were missing

**N12 closed, and Phase 2's second exit criterion with it.** The session's one
task was to find out what the community says the best stage is and compare. It
took about ten minutes of reading to discover that the comparison could not be
made at all, because the stages players are told to farm were not in our data.

**What was wrong.** `KornblumeAdapter` read `stages.json`. The upstream's own
planner reads the newest `stages<major>_<minor>_greedy.json` — `getDrops()` in
`src/composables/planner.ts` returns `dataStore.stages3_3_greedy`, and
`normalizeDrops` divides each drop count by the stage's `count`. `stages.json` is
the older shape: a bare proportion, no sample size, and **chapters 1 to 4 only**.
99 stages against the live table's 105, overlapping by 40, with a drop-to-stage
mapping the game has since retuned — 1-14H drops Liquefied Terror in the old file
and Sharp Needle in the new one.

**Five sessions of work sat on top of that and nothing went red.** The bundle was
well-formed. The parser accepted it. The schema round-tripped it by record
equality. The optimizer solved it in 1.8 s and explained itself. The CI pipeline
was green twice. Every one of those was true of a third of the game. **The only
thing that caught it was a person going to look at what somebody else said the
answer was** — which is precisely the argument the plan makes for having a
benchmark at all, arriving before the benchmark was written.

**A "fact about the game" is withdrawn.** Last session recorded *45 of 118
characters' Insight 2 cannot be planned from patch 3.5, because their materials
appear only against the synthetic `Unreleased` stage.* That was a fact about the
stale file. Against the table the upstream actually uses, **all 118 can be
planned**, and none of them needs `Unreleased`. `RealUpstreamPlanTest` now
asserts the reverse as a canary: a released character with no source for a
material means the stage table has gone short again.

**The fix, in three places.**
- The adapter takes the highest-versioned sampled table it finds, compares
  versions numerically (`stages10_0_greedy` must beat `stages3_3_greedy`, which
  no listing order gives you), divides counts by the sampled run count, falls
  back to `stages.json`, and **says which file it read either way**. Silence is
  what let this run for five sessions.
- `tools/fetch-upstream.sh` pins the sampled table per snapshot: 3.3 →
  `stages3_0_greedy.json`, 3.5 → `stages3_3_greedy.json`. The version in the
  filename lags the patch label because the drop data is resampled on its own
  schedule, so it cannot be derived from the label and is written down instead.
- Four new adapter tests: preference over `stages.json`, numeric version
  ordering, the fallback note, and a `count` of zero — which is a denominator,
  and an infinite yield is a free source, the same defect this adapter has now
  met three ways.

**Then the benchmark, which is what N12 was actually for.** Twenty claims from
Prydwen's *Insight Materials Cheat Sheet (2.7 patch)*, transcribed with their
source into `docs/benchmarks/reverse-1999-community-answers.md` and compared by
`CommunityBenchmarkTest`:

- **Five exact agreements** — Holy Silver (10-9H), Salted Mandrake (3-13H),
  Perpetual Cog (7-16H), Pyroxene Ore (9-1H), Alopecurus Pratensis (8-18H) — the
  exit criterion's five, on its own terms. Seven more agree within 8%.
- **Seventeen of the guide's quoted drop rates land within three percentage
  points** of a sample this project had never seen. Two independent measurements
  of the same game agreeing is the best evidence so far that the pipeline is
  carrying real numbers and not plausible ones.
- **The plan beats the advice**: 3 624 Activity against 4 017 for following the
  guide material by material, and the 4 017 does not cover the whole demand.
- **Four of the guide's items are ours under different English names**, and the
  rates identified them rather than the names: Red Lacquer Slab is Red Lacquer
  Tablet, Golden Grass Incense is Golden Herb Incense, Luminite Ore is Pyroxene
  Ore, and Fox Tail is Alopecurus Pratensis — literally foxtail grass.
- **One claim is deliberately not tested**: the guide's Magnesia Crystal pick,
  2-9H, is not in the upstream's sampled table at all. Written down rather than
  dropped.

**And the benchmark's own finding, which is the thing to do next.** Every
disagreement over 25% is a sample-size disagreement. The upstream publishes how
many runs each stage's drop counts came from — **105 to 41 212** — the adapter
divides by it and throws it away, and the model then treats a hundred-run mean
exactly like a forty-thousand-run one. Milled Magnesia prefers 4-5H (105 runs) to
the guide's 5-8H by 154%; Liquefied Terror prefers 4-4H (113 runs) to 9-3H by
224%. A noisy mean is a high mean about as often as a low one, and `argmin` picks
whichever stage got lucky. **Q8 has stopped being a boundary formality for
Phase 6 and is now N17, ahead of the time axis.**

**And a third defect, which only the full suite could find.** Docker would not
start for most of the session, so the Testcontainers tests did not run at first.
When it finally came up, `RealUpstreamPatchTest` failed — the schema round trip,
on the same stages it had been round-tripping happily for two sessions. **The
drops were all there and in a different order.** `Stage.drops()` is a `List`, so
record equality is order-sensitive; the adapter emitted drops in the upstream's
JSON key order and `JdbcGameDefinitionRepository` reads them back ordered by
`stage_drop.item_id`, a surrogate handed out in catalogue-ingest order. Those two
coincided under `stages.json` and stop coinciding under the sampled tables.
**"The schema gives somebody else's numbers back unchanged" was true by luck**,
and the luck ran out the moment the input file changed. Both ends now sort by
item id, and an adapter test pins it.
That is worth remembering as a pattern rather than a bug: **this session's three
defects were all invariants that had never actually been tested, only
coincidentally satisfied.**

**Where the build ended up:** green. 154 tests locally, 139 on CI once the 15
snapshot-gated ones skip. Nothing is committed or pushed, so **Phase 2's box
stays unticked** even though both halves of its criterion are met. That is N16,
and it is now only the pipeline half.

**Housekeeping.** `RealUpstreamPlanTest`'s fixture — the snapshot load and the
two fake repositories — moved into a shared `RealUpstream` class, because the
benchmark needed the same one and two copies of a fake drift.


**Phase 2 opened.** The optimizer went from four interfaces with nothing behind
them to a working mixed-integer program that answers a real goal set over a real
patch. 93 tests to 144. The phase's box stays unticked because half its exit
criterion is untouched — see N12 — and that half is the one that decides whether
the answers are *right* rather than merely fast.

**What was built, and the shape of it.** Three pieces that do one thing each,
because the alternative is a solver that cannot be tested without a database:

- `DemandResolver` turns goals into an item vector by walking the upgrade DAG.
  The choice worth recording is that it walks **backwards** from the target.
  A forward walk needs a starting state and a player who does not own the entity
  has none; walking back until it meets either something already achieved or a
  track with nothing before it answers both cases in one traversal, and handles
  a multi-track entity without ever enumerating tracks.
- `EnergyMip` is the model and nothing else — inputs in, an answer or a refusal
  out. **Crafting recursion turned out to be an absence rather than a feature:**
  every craft is a variable and every intermediate material is a constraint row,
  so the solver walks a tier-3-from-tier-2-from-farmed chain because the algebra
  makes it. A recursive expansion would have had to pick a depth and choose
  between routes the solver can simply price.
- `MipOptimizer` does the fetching and the narrating.

**Two defects, both found by real data on first contact, both the argument for
`RealUpstreamPlanTest` existing.**

1. **The optimizer crafted 294 250 Sharpodonty out of nothing.** Kornblume lists
   base materials in the same file as its recipes, as rows with an empty
   `Material` array. Converted faithfully those become crafts that consume
   nothing — free unbounded supply at zero energy — and the solver did exactly
   what it should with the model it was handed. The galling part: the adapter
   already refused zero-cost *stages* with the comment *"a free source is an
   unbounded one"*, and nobody had thought to apply the same sentence to
   conversions. Fixed at three levels on purpose: `Craft`'s constructor (so every
   future upstream inherits the rule), `CanonicalBundleParser` (so a human
   approving a bundle gets a located message naming the entry), and
   `KornblumeAdapter` (so the twelve rows are skipped and announced).
2. **ojAlgo blew the JVM stack.** Not slowly — a `StackOverflowError` out of its
   own worker pool, which arrives as a wrapped `RuntimeException` rather than a
   solver state. Branch-and-bound recurses per node and dives depth-first while
   it has no incumbent, and **an unbounded integer variable is an unbounded
   dive**. Three fixes, in descending order of what they bought:
   - **Bound every variable.** Nobody runs a stage more times than would supply
     the whole goal set on its own. The bound is the demand expanded through the
     craft graph, so it excludes no optimal solution. This is the one that
     mattered.
   - **Keep the objective integral.** A millionth-weight tie-break had been added
     to stop a lossless craft cycle spinning; it cost the objective the
     integrality that lets branch-and-bound discard a node whose bound is within
     one of the incumbent. It is now applied only when the craft graph actually
     has a cycle — which real recipe trees never do, because a thing is made from
     cheaper things. **A tuning constant added for a case that does not occur, at
     the cost of a property that does.**
   - **Loosen the gap from seven significant digits to four.** Proving no
     arrangement saves a hundredth of an Activity point is most of the runtime.

**And a fact about the game rather than about the code:** 45 of 118 characters'
Insight 2 cannot be planned from patch 3.5 at all. Their materials appear only
against Kornblume's synthetic `Unreleased` stage, which the adapter refuses for
costing nothing. The optimizer names them — *"nothing available can produce
winged-key (no stage drops it and the recipe(s) that make it — craft-winged-key —
cannot themselves be supplied)"*. That is the correct answer and it only exists
because free sources are refused.

**What the solver actually promises now, written down as
[ADR 0010](docs/adr/0010-a-plan-is-the-best-provable-in-the-budget.md).** The
search is stopped by its budget every time on a real goal set; it does not
finish. Rather than leave "we ran out of time" as the whole story, the plan
carries the size of the doubt, measured against the linear relaxation with one
extra solve over a model already built: **"no plan can be more than 2.95%
cheaper."** That is also the number ADR 0004's reversal trigger should be read
against — a wall clock inside budget because a timer stopped the search says very
little on its own. Shadow prices are reported only when both ends of the
comparison were proven optimal, because the difference between two time-limited
answers is noise with a number on it.

**Measured, five characters to Insight 2, Reverse: 1999 3.5:** 2 411 Activity
over twenty stages and six crafts, ten days at 240 Activity/day, p95 **1.8 s**
over 50 solves, within **2.95%** of the relaxation's bound. Ninety-three stage
variables, eleven crafts, twenty-three constraints.

**Verified twice, as usual.** Every expected number in `EnergyMipTest` is worked
out by hand in its own comment — *"6 ingots need 18 ore and 300 gold; 18 / 2.0 =
9 runs at 10 = 90"* — so a disagreement is the solver's fault and not the
fixture's. Then `PlannerAcceptanceTest` runs the same solver over the acceptance
bundle parsed off disk, where the hand-computed answers (390 Activity for
Insight 1; 350 for the amulet, taking the craft route the solver was never told
about) all matched on the first run, shadow price included. Then
`RealUpstreamPlanTest` over a real patch, which is where both defects were.

**Environment note:** Docker Desktop was not running at the start of the session,
which shows up as `initializationError` on every Testcontainers class and looks
alarming out of context. It is not a defect in the tree — start Docker and rerun.

**CI confirmed the tree the same day (N13).** Runs `34072743411` and
`34072752969`, both success, PR #7 merged to `main`. The two things worth
checking were checked in the log: all 30 planner tests executed on the runner —
a new test source set that is silently not picked up looks exactly like a green
build — and the ten real-data tests showed as SKIPPED rather than passed or
failed. **Reading the log also caught a lie in this file:** the entry predicted
six skips and there are seven, because the local suite is 144 tests and not the
143 written down three times without being recounted. Corrected. The test count
is exactly the kind of number that gets carried forward instead of re-derived,
which is what this file exists to prevent.

**Not done, and deliberately:** no HTTP route for a solve, no cache behind
`SolveKey`, no `SolveCoordinator` implementation, no time axis and therefore no
shops, rewards or weekday rotation, and no fodder. Each is a next action rather
than an oversight.

### 2026-09-06 (sixth session) — the pipeline meets somebody else's data

**Did:** N9 (confirmed green) and N8 (decided, then built). A parser adapter for
Reverse: 1999, a canonical bundle writer, `--gamedata=adapt`, and two real
patches through the whole pipeline. 75 tests → 93.

**N9 first, and it was clean.** Runs `34002586027` and `34002589214`, both
success on `e2239b3`. The thing the last session flagged — three test classes
newly starting Tomcat — cost nothing: the PR job went 1m43s → 1m26s. Nothing to
absorb, nothing to split.

**N8 was a decision, and it was not mine to make.** The tracker had framed it as
*ask F2 and wait* versus *close the phase by exception on the synthetic
fixture*. The project owner picked a third option that neither of those covered:
build the adapter now, read the data, redistribute none of it, and ask about
licensing before publishing anything. That is [ADR
0009](docs/adr/0009-upstream-data-is-fetched-never-vendored.md), and it is the
right call for a project whose current audience is a CV.

**The consequence is uncomfortable and is written everywhere it belongs.**
Because the snapshots cannot be committed, `RealUpstreamPatchTest` skips on CI.
**The strongest test in this repository is the one the pipeline does not run.**
It is said in the test's javadoc, the README, the ADR, the tracker header and
the *what is still unverified* list — five places, because a green tick that
quietly omits the best evidence is worse than a red one, and the temptation to
forget this will grow with every green run.

**It found two defects on first contact, which is the entire argument for having
done it.** Neither was findable by any amount of thinking about the synthetic
fixture:

1. **A character named Зима.** `Names.slug` normalised to NFD, stripped
   combining marks and kept `[a-z0-9]` — which for Cyrillic keeps nothing, so
   the slug was empty and the whole ingest was refused. Fixed by making the rule
   Unicode-aware: letters and digits survive whatever script they are in, while
   an accent is still stripped so `Café Crème` reads `cafe-creme`. Worth noting
   *why* it survived design: ids are `TEXT` in the schema and `Identifier` asks
   only that they not be blank, so nothing downstream had an opinion. The ASCII
   assumption lived in one method and nothing else could contradict it.
2. **Placeholder rows.** The upstream ships unreleased psychubes as rows whose
   every field but `Id` and `Rarity` is `null`, and the adapter read `Name`
   before checking the release flag — so a conversion was refused by rows it was
   about to throw away. The rule that falls out of it: *validate what you keep,
   not what you drop.*

**What the adapter refuses to do is the part I would defend hardest.** It
converts less than the upstream publishes and prints what it dropped on every
run — `skipped 590 resonance-pattern cost row(s)`, `skipped 4 unreleased
character(s)`, `skipped 1 stage(s) costing no Activity`. Two of those are
judgement calls worth carrying forward. `shops.json` has a material and a
quantity but no currency, no unit price and no reset period, and our `Shop`
needs all three; inventing them would put fake prices into the optimizer's
source set, and **a wrong number gets used while a missing one gets noticed**.
Frequency rows are four alternative patterns for one resonance step, and an
`Upgrade` is a required cost of one transition — mapping all four would charge a
player for every pattern at once. The model has no "or" and the adapter will not
fake one.

**Structure, because the claim needed a receipt.** The adapter is its own Gradle
module, `:adapters:reverse-1999`, and `ModuleBoundaryTest` gained an `adapters`
layer that only `:app` may reach. So "a new game is a bundle plus a parser
adapter, and nothing else" is now a build failure rather than a promise, and the
cost of a title is visible as a directory. `UpstreamAdapters` in `:app` is the
only place in the product that names one.

**`CanonicalBundleWriter` was not on the plan and earns its place.** An adapter
could have handed a `GameDataBundle` straight to the repository, and that would
have skipped the step this phase is built around: a human reading the thing
before approving it. Writing the file keeps the loop at *adapt, preview, ingest,
publish* and gives an adapter no privileges for being code. It is pinned to the
parser by a round trip over both fixtures plus an idempotence case — a writer
whose output drifted would fill every patch diff with noise that means nothing.

**Verified twice, and the hand check is the half that convinces.** From the
packaged jar against `docker compose` Postgres: adapt 3.3, ingest, publish;
adapt 3.5, ingest (which printed the patch report), publish; diff; then the API
curled. `GET /entities/regulus/upgrades` says Insight 2 costs 40 000 Sharpodonty
and 10 Scroll of Starlit Ascent, in named items.
`GET /entities/igor?version=0` is a 404 reading *"no entity 'igor' in
reverse-1999 3.3"* — Igor was released in 3.5, so version pinning did the exact
thing it was built for, against data nobody here wrote.

**One near-miss, caught at `git add` and worth the paragraph.** The first
`.gitignore` rule for the snapshots was `**/upstream/`, which is exactly the
pattern a reasonable person writes — and it silently stopped tracking the
adapter's *own* test fixtures under `src/test/resources/upstream/`. Locally
everything passed, because the files were on disk. On the runner it would have
been a compile failure in a module that had never built there, and the obvious
suspect would have been the new Gradle module rather than a line in
`.gitignore`. The fetch target is now `build/upstream-snapshots` and the ignore
rule names it specifically. **A broad ignore rule fails by omission, and an
omission is invisible in `git status`** — the only reason this surfaced is that
the staged file list was read rather than skimmed.

**Then pushed, merged as PR #6, and N10 closed in the same session.** Runs
`34035918992` and `34035923889`, both green; the adapter module built on the
runner and `RealUpstreamPatchTest` skipped, which is the designed state rather
than a disappointment. **Phase 1 is closed and its box is ticked.**

**Left for next session:** N11 — open Phase 2. One thing found in passing that
Phase 2 must not trip over:
`Stage.potentialOutput()` rounds an expected yield *up* to a whole item, which
is right for a catalogue listing and wrong for the MIP constraint, which needs
the yield itself.

### 2026-09-06 (fifth session) — the API answers, and the numbers say where they came from

**Did:** N7. The gamedata API — five routes, a read model, ten HTTP tests, and a
hand check through `docker compose up`. 65 tests → 75, all green.

**What the work actually was.** The data was already in the schema and the two
exit questions were already answerable; `GameDataIngestTest` had been asking
them of the repository since the fourth session. What was missing was every
inch between a repository call and an HTTP response, and this project has been
bitten there before — the 401 on `/api/health` was invisible to a test that
called the controller method directly. So the endpoints came with tests that go
over real HTTP through the real security chain, and then the whole operator loop
was run by hand anyway: compose up, `gamedata-cli ingest` and `publish` both
fixture versions against the compose Postgres from the packaged jar, curl every
route. It all worked first time, which is a less interesting session log than
the last two and is what the previous two sessions' work bought.

**The decision that was not on the list, and why it was made anyway.**
`GameDataVersion` gained an `attribution` field. "Numbers and text only,
attributed" is a project invariant, and it had been an invariant about
*ingestion* for as long as nothing served the numbers — the string was on the
schema row and on `DraftVersion`, and the read path could not see it. Serving a
catalog page to a stranger with no way to say where its numbers came from would
have been shipping a violation of the invariant on the first day the invariant
could actually be violated. Cost: one field on a `common` record, one column in
three queries. It also strengthens `roundTripsWithoutLoss`, which now proves
provenance survives the round trip as well.

**And the wart it exposed.** `GameDefinitionRepository.find` took a
`GameDataVersion`, so every caller built one with a dummy label and
`Instant.EPOCH` just to name a sequence — three sites, each with an apologetic
comment. Adding a fifth field would have made the hack uglier in a third place,
which is what made it worth fixing rather than tolerating: `find(GameId, long)`
now, and the three apologies are gone. A port that makes its callers fabricate
data to ask a question is shaped for its implementation.

**Read model, not the domain, on the wire.** The domain records would almost
serialise. Almost: every typed id would go out as `{"value":"gold"}`, every cost
would be an item slug and a number that each client joins by hand, and every
future domain change would be a breaking API change nobody noticed making. The
first two are annoyances; the third is the one that costs a year. `GameDataView`
is the wire format and `GameDataReadModel` is the only thing that maps to it.

**Cost knowingly deferred, written into the code rather than into a hope.**
Every request loads a whole version — fifteen queries — to answer a question
about one entity. Fine now, not fine at phase 4. The fix is a cache keyed on
`(game, sequence)`, correct by construction because a published version is
immutable. Not written, because a cache added before there is a number to
compare against is a guess; the javadoc on `GameDataReadModel` says so, so the
next reader does not mistake it for an oversight.

**Test-harness change to know about:** `GameDataDatabaseTest` is now
`RANDOM_PORT`. Three classes that never started a servlet container now do. The
alternative was a second Spring context differing only in that flag, which means
a second Postgres and a second boot on every build forever. Suite went 22s → 31s
locally. **On the runner it is unproven — N9.**

**Not done, deliberately:** N8 (a real upstream — still gated on F2, which needs
a human to send a message), N4 (the `Entity.kind` rule — still vacuous until
planner has code), N5 (the CI action bumps — still wants its own quiet PR).

### 2026-09-05 (fourth session) — the schema holds data, and could not be deleted

**N6, all four parts.** Parser, persistence, diff, CLI. 26 tests to 65, all
green locally, `./gradlew build` clean. Nothing pushed yet.

**The decision that shaped the session: JDBC, not JPA
([ADR 0008](docs/adr/0008-gamedata-persistence-is-jdbc.md)).** N6 said "the JPA
entities" and left two questions open before writing them — how `TEXT[]` maps
and how the composite foreign keys map. Both settle at the same answer, and it
is not the one the entry assumed. Writing the mappings out showed four
mismatches at once: twenty composite `@JoinColumns` with `insertable=false`
carrying an invariant the schema states in one line; a `UserType` for a column
that JDBC reads with `getArray`; a write path that is a bulk insert of an
immutable snapshot, so dirty checking and the first-level cache cost without
buying; and a read path of fifteen collections under one root, which is
`MultipleBagFetchException` by construction. Underneath all four, the domain is
already records — JPA entities would have been a second parallel model, so the
mapping work an ORM exists to save is work this module does either way.
Consequences: `available_days` stays `TEXT[]` and the bitmask fallback is
dropped; the composite keys stay exactly as written. The starter stays on the
classpath for `identity` and `player`, which have not chosen.

**The defect this session existed to find: a version could not be deleted.**
`DELETE FROM gamedata.game_data_version` cascades to `item` down one chain and,
via `entity → skill → skill_rank`, to `skill_rank_cost` down another. Postgres
does not order those two chains against each other, so if `item` goes first the
NO ACTION foreign key on `skill_rank_cost.item_id` is checked immediately, sees
rows that still exist, and refuses:

```
ERROR: update or delete on table "item" violates foreign key constraint
       "skill_rank_cost_item_fk" on table "skill_rank_cost"
```

That is not an edge case. Re-ingesting a draft is the ordinary path — a
scheduled fetch re-runs, and a failed ingest must not wedge the sequence — and
it is a delete. **V2's tests never saw it because they only ever inserted.**
Fixed in `V3__gamedata_version_is_deletable.sql`: the nine item-referencing
keys gain `ON DELETE CASCADE`. Their job is the composite `(version_id, id)`
reference and that is unaffected. The tradeoff is written into the migration
rather than left implicit — deleting one item row now silently deletes the
costs and drops that cited it, which is acceptable *only* because a version is
written once and never edited. If anything ever edits a published version in
place, that rule is the first thing to reconsider.

**The second surprise, smaller and worth remembering.** `publish` threw
`IllegalStateException` for "there is no draft here", and the test caught
`InvalidDataAccessApiUsageException` instead. Spring's `@Repository`
persistence-exception translation rewrites a bare `IllegalStateException` into a
data-access failure, which this is not. Both refusals are named types now —
`PublishedVersionIsImmutableException` and `NoDraftToPublishException` — so a
domain refusal survives the trip out of the repository saying what it was. This
is a general trap for anything annotated `@Repository`, not a gamedata quirk.

**Design decisions worth carrying forward:**

1. **A bundle is not a definition.** `GameDataBundle` is what a parser adapter
   produces; `GameDefinition` is what the repository returns. They are different
   types because `GameDataVersion` requires a non-null `publishedAt`, so an
   unapproved snapshot has no representation as a definition at all. The type
   system now says what the schema's `published_has_timestamp` check says, and
   there is no read method that could return a draft.
2. **Validation happens before the connection opens**, and reports every
   dangling reference at once with the referrer named:
   `stage '1-1' drops unknown item 'sulfr'`. The composite foreign keys catch
   the same things one at a time, seconds later, saying `stage_drop_item_fk` —
   which tells the person approving a publish that something is wrong and
   nothing about what. Most of `CanonicalBundleParserTest` asserts on messages
   for that reason.
3. **The diff has three axes, not the two the plan names.** `PROGRESSION`,
   `CATALOG`, `GACHA`. Banners are neither material costs nor combat numbers,
   and filing them under either would be a lie told to keep the enum small.
   Subjects are diffed before fields, so a removed stage is one line rather than
   six. The rendered report over the two fixtures:

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

   A balance change to one multiplier reports that multiplier and not the cost
   beside it. That is the whole argument for versioning both axes together,
   rendered.
4. **`preview` is the CLI command that matters.** It answers "what would this
   file change?" against the currently published version, without writing a
   row. `ingest` prints the same report over what it just wrote. Publishing is a
   human approval, and an approval nobody could have reviewed is a rubber stamp.
5. **The fixture is synthetic on purpose, not as a placeholder.** Q3 says
   assume the community bundles are not redistributable — no licence is not
   permission — so `proving-ground` was invented to exercise every shape the
   format has: all four source kinds, both sinks, the catalog axis, a rotating
   stage, an expiring event, a banner with soft pity and a floor. Its 1.1
   version carries one change of every kind on both axes and its header lists
   them, so a broken flattener fails a test rather than silently omitting a
   field from every patch report.

**Also:** `GameDataDatabaseTest` gives the two new database classes one shared
container and one shared Spring context, started in a static initialiser and
reaped by Ryuk. Per-class containers would have added two more to every CI run
forever. `ApplicationBootTest` keeps its own deliberately — it exists to prove a
cold start against a database nothing has touched.

**Left undone, deliberately:** no HTTP route reaches any of this. Phase 1's exit
criterion says *your API* answers the two questions, and the API still serves
`/api/health` alone — that is **N7**. And the pipeline has not seen this tree.

### 2026-09-05 (third session) — Phase 1 opens: the canonical schema

- **`V2__gamedata_canonical_schema.sql` landed.** 28 tables, 7 indexes, the
  whole domain including the catalog axis. Verified rather than assumed: Flyway
  reports *"Migrating schema to version 2 — gamedata canonical schema"* and
  `Successfully applied 2 migrations` against PostgreSQL 16.15 in a
  Testcontainers run. Build green, 26 tests.
- **The three decisions the schema is built on**, in case a later session is
  tempted to undo one:
  1. *A version is a full snapshot, not a delta.* Publishing inserts a complete
     new row set and mutates nothing, so an old plan stays correct after a patch
     and the diff is a set comparison rather than a history walk. Game data is
     thousands of rows; the duplication is not worth optimising away.
  2. *A reference cannot cross a version boundary, structurally.* Every child
     uses a composite `(version_id, …)` foreign key against a
     `UNIQUE (version_id, id)` on its parent, so a v2 stage citing a v1 item is
     refused by Postgres. One extra column per table. The alternative leaves the
     diff — this phase's headline feature — one ingest bug from silently lying,
     with nothing to show for it.
  3. *Draft and published are different states*, enforced by a CHECK, because
     `GameDefinitionRepository` promises that "latest" means latest approved.
- **`attribution` is `NOT NULL` on every version.** "Numbers and text only,
  attributed" is a project invariant, so an unattributed snapshot should not be
  representable at all. Cheap now; a migration later.
- **`GameDataSchemaTest` — six tests, and they check both directions.** A
  constraint that only ever rejects is indistinguishable from a broken table, so
  each one also proves the legitimate case still goes through. The one worth
  keeping in mind: **in Postgres, NaN sorts above every number**, so the obvious
  `expected_yield >= 0` would have waved NaN straight through — it is the upper
  bound against `Infinity` that actually catches it. `Drop.java` already got
  this right in Java; the schema now matches, and there is a test that fails if
  either drifts.
  Deliberately **not** a Spring test: it tests the schema, and booting the
  application to do it would only make it slower.
- **One domain fix on the way in.** `Skill.Rank.upgradeCost` was
  `List<String>` — item ids as bare strings, against the project's own rule that
  identifiers are typed, and the only cost in the model that was not
  `List<ItemStack>`. Now `List<ItemStack>`, which also expresses "four of these
  and one of those", which a list of ids cannot. Same reasoning as ADR 0007's
  `kind` field and the same free window: nothing constructs a `Skill` yet.
  **The window is closing** — once ingestion writes fixtures, shape changes stop
  being free.
- **Tooling note that cost several retries:** the Bash heredoc broke repeatedly
  on the large SQL and Java content. Writing the file directly worked first try.
  For anything over ~100 lines, write the file rather than piping a heredoc.
- **CI green on the schema, first try, and the unknown resolved.** PR #3 merged
  as `662d999`; runs `33965175789` (PR) and `33965237825` (push to `main`), both
  success. All six `GameDataSchemaTest` cases PASSED **on the runner**, which
  answers the one thing worth watching: `GameDataSchemaTest` starts a *second*
  Testcontainers Postgres alongside `ApplicationBootTest`'s, and two of them run
  side by side on `ubuntu-latest` with no slowdown — `BUILD SUCCESSFUL in 1m 1s`,
  faster than the previous run with one.
- **The frontend job showed a sixth deprecated action** that the backend-only
  reading of N5 had missed: `actions/setup-node@v4`. N5 corrected from five to
  six.
- **What Phase 1 still needs, and it is most of it:** ingestion, the JPA layer,
  publishing with diffs, `gamedata-cli`, and the API that answers the two exit
  questions. The schema is applied and **empty** — it has never held a row of
  real game data, and that first ingest is where a modelling mistake will
  actually surface. See **N6**, which also flags the two things to settle before
  the JPA work: whether Hibernate validates the `TEXT[]` column, and that the
  composite foreign keys will be awkward to map and must not be "simplified"
  away.

### 2026-09-05 (second session) — the equipment question, answered from the code

- **N2 done: [ADR 0007](docs/adr/0007-equipment-is-an-entity.md) — equipment is
  an `Entity`.** The conclusion matches the leaning already on record, but the
  argument that decides it is not the one that was written down, and that is
  worth keeping. The prior-art note argued from PGR's Memories being
  equipment-like. The stronger reason was sitting in our own model: **`Upgrade`
  and `Goal` both key on `EntityId`**, so anything upgradeable that can be a
  planning goal *already* has to be an `Entity` — the alternative is not "a
  third concept", it is "a third concept plus a widened key on two records plus
  a second shape of upgradeable thing for the planner to know about". And the
  Entity/Item dual identity gear needs is not new either: `Item`'s javadoc
  already puts a character's *copies* in the inventory while the character is an
  `Entity`. Gear reuses that pattern exactly.
- **Gacha is indifferent, which was worth checking before arguing.**
  `BannerModel` and `FeaturedRule` are rarity-shaped; neither mentions
  `EntityId` anywhere. Whatever equipment is, the gacha module does not change.
  That removed a whole axis from the decision.
- **`Entity` gained a required `kind`** (`"character"` / `"equipment"`), opaque
  and game-supplied, for catalog routing and grouping. Backend build green
  with it. **The timing was free and will not be again:** nothing in the repo
  constructs an `Entity` yet, so adding a record component cost zero call sites.
  In Phase 1 it would have meant the ingest adapter and every fixture.
- **The honest gap, recorded as N4:** the ADR asserts that `planner`, `gacha`
  and `stats` never read `kind`, and nothing enforces that.
  `GameAgnosticismTest` scans for game slugs, not field reads. The rule is
  deliberately *not* written yet — the guarded modules are empty, so it would
  pass vacuously and prove nothing. It goes in with the first real planner code.
- **N1 done, and Phase 0 is closed but for the deferred deploy.** The wall was
  the same one as last session and it came down mid-session: `gh` is installed
  (2.100.0, winget) but **not on `PATH`** — the same MSI behaviour already
  recorded for the JDK — and `gh auth login` is interactive and handles
  credentials, so it cannot be driven from a session. Handed to the user, who
  ran it. Re-confirmed independently first that there was no way around it: the
  repo is private, so `api.github.com/repos/kietnt4412/storm_almanac` **404s
  anonymously**. **Cost me a round trip:** I handed over the command in a
  `bash` block and the user ran it in PowerShell, where a quoted path is not a
  command — it needs the call operator `&`. Give PowerShell commands in
  PowerShell form on this machine.
- **The result: green twice, and green for the right reason.** Runs
  `33963222427` (PR) and `33963295323` (merge to `main`). B4 item 5 asked
  something narrower than "did the run pass" — did the new Testcontainers test
  actually execute, or silently skip? Read the log, not the badge: both
  `ApplicationBootTest` cases show `PASSED` on the runner. Testcontainers works
  on `ubuntu-latest`. Nothing to tag, nothing to split.
- **Then this session's own commits went through it too.** Pushed to `dev`, and
  **nothing fired** — which was not a failure but the workflow doing what it
  says: `ci.yml` triggers on `push: branches: [main]` and `pull_request`, so a
  push to `dev` with no open PR runs nothing at all. Worth knowing before
  reading silence as breakage. PR #2 merged as `a82da1e`; runs `33964292888`
  and `33964297527` both green, `ApplicationBootTest` PASSED on both. Every
  commit made this session is now verified by CI.
- **Two deprecation warnings surfaced on that run**, recorded as **N5**: Node 20
  is deprecated and five actions are already being force-migrated to Node 24 by
  the runner, and `setup-java@v4` is deprecated outright. Green today, on a
  clock. Left for its own PR rather than bundled into a Phase-0 closing commit.
- **Git and `gh` are authenticated separately, and only `gh` is.** `git fetch`
  fails with "could not read Username" — no credential helper is wired for git
  itself, so pushes stay manual. `gh auth setup-git` would fix it.
  `branch.dev.remote` is also unset, so `git status` cannot report ahead/behind
  on `dev`; `git push -u origin dev` once would fix that.
- **Also re-learned, cheaply:** `2>&1` on a native exe in PowerShell 5.1 wraps
  the JVM's stderr banner as a `NativeCommandError` that *looks* like a build
  failure. `./gradlew build` exited 0. Read the exit code, not the red text.

### 2026-09-05 — the app runs; booting it found a 401 on the one endpoint

- **B2 done, and it paid for itself immediately.** Started Docker Desktop and
  ran `docker compose up --build` from cold. The image built, all three services
  came up healthy, Spring started in 5.2s, Hikari opened a pool, Flyway created
  `flyway_schema_history` and applied `V1__baseline.sql`, and Hibernate
  validated against the migrated schema. All six module schemas exist.
- **Then `curl localhost:8080/api/health` returned 401.** `modules/identity`
  declares `spring-boot-starter-security`, which puts it on the runtime
  classpath of `:app`. With no `SecurityFilterChain` bean anywhere, Boot's
  default auto-configuration secures every route with HTTP Basic. Phase 0's
  entire product surface was unreachable, and the pipeline's smoke step
  (`curl --fail "$DEPLOY_URL/api/health"`) would have failed the first real
  deploy. **`HealthControllerTest` passed throughout** — it calls the controller
  method directly, so no amount of it could ever have found this.
- Fixed with `SecurityConfig` in `modules/identity` — the module that owns the
  dependency, not `app`. Permits `/api/health` and the actuator health group,
  denies everything else, answers 401 rather than redirecting a probe to a login
  form, and is stateless so anonymous probes stop minting a `JSESSIONID` each
  (a health check every ten seconds would otherwise leak sessions forever).
- Added `ApplicationBootTest`: `@SpringBootTest(RANDOM_PORT)` over a
  Testcontainers Postgres, hitting real HTTP. **Verified it fails without the
  fix** — both cases red, not just compiled and assumed. It covers the four
  things a unit test structurally cannot: the datasource opening, Flyway
  migrating, Hibernate validating, and the filter chain. Its second case asserts
  an unmapped path is *401, not 200*, so a chain that permits everything cannot
  pass by accident in phase 3.
- **The lesson generalises past this bug.** Every defect found in the last two
  sessions — the `gradlew` exec bit, the missing lockfile, the invalid YAML,
  and now this — was invisible to a green local `gradlew build`. The build
  signal is compile-and-unit-test and nothing more. Boot the thing.
- Found **E1**: Avast MITMs TLS and the JDK truststore does not trust its CA, so
  Gradle cannot download any *new* dependency. Cached for four sessions, so it
  only surfaced on adding Testcontainers. Worked around with
  `-Djavax.net.ssl.trustStoreType=Windows-ROOT`; a permanent fix is an open
  decision recorded under E1.
- Housekeeping: the catalog pinned Testcontainers to 1.20.4 while Boot's BOM
  pulled the shared core to 1.21.3 transitively — direct artifacts and core on
  different versions. Dropped the pin and let the BOM manage it, matching how
  every Spring entry in the catalog is already declared.
- **B1a was already done before this session opened** and the tracker still
  showed it open. Corrected. The file says "tick boxes as work lands"; this is
  what happens when that slips.
- Backend green: 17 tests. Left the compose stack running.
- **B4 answered from a screenshot: `ci` run #5 passed on `1a58dd5`**, 2m 58s,
  both jobs. Five runs to get there, each failing for a different reason, and
  none of the four failures was a code defect — exec bits, a missing lockfile,
  invalid YAML, and a fake-green deploy job. Also settled that the three
  `nightly-chaos` entries are skips (`if: false`), not failures.
  Worth noting how this was confirmed: the repo is private and there is no `gh`
  here, so the Actions API 404s and the answer had to come through a human with
  a browser. That is Q7, and it should be fixed before Phase 1.
- **E1 resolved the same day it was found** — Avast's HTTPS scanning turned off
  at the source rather than worked around in the build. Verified with
  `--refresh-dependencies` and no flag. Nothing was committed for it, which is
  the right outcome: a machine-specific TLS workaround in a shared build file
  is a trap for the next person, and for CI.
- **B7 written**, from `docs/prior-art.md` rather than from ambition. The
  temptation was "we have a solver"; ArkPlanner has one, so the paragraph claims
  the four narrower things that are actually differentiated.
- Closed the session by pushing `dev` and merging **PR #1** into `main`
  (`73007df`). `gh` was installed, but not authenticated — so the resulting
  pipeline runs were *not* observed, and `ApplicationBootTest` has still never
  executed on the runner. **That is N1, and it is the only thing between Phase 0
  and closure.** Recording it as open rather than assuming it passed: assuming
  is how B1a sat ticked-but-stale for three sessions.
- Phase 0 is otherwise complete, with its exit criterion deliberately unmet per
  D1. Phase 1 opens on **N2 — Q6/F4, equipment modelling**, which blocks
  ingestion and deserves an ADR rather than an inline decision.

**State at session end:** `main` at `73007df`, working tree clean, 17 tests
green locally, compose stack verified and left running.

### 2026-09-02 — prior-art read; first real model defect found

- Read Kornblume, Penguin Statistics `backend-next` and ArkPlanner. Written up
  in [docs/prior-art.md](docs/prior-art.md).
- **Found a genuine defect in the domain model.** `Drop` stored
  `declaredProbability` validated to `[0,1]` plus a separate `quantityPerHit`.
  Real upstream drop values are **expected yield per run and exceed 1.0** (up to
  2.187 observed) — our validation would have rejected most of the dataset on
  ingest. Replaced with a single unbounded `expectedYield`, which is also the
  quantity the MIP constraint actually needs. This is exactly what the reading
  was for, and it was found before any ingestion code existed rather than after.
- Confirmed by inspection rather than assumption: **Kornblume does no
  server-side solving** — its farming routes are precomputed greedy results
  baked per patch. The wedge is real.
- ArkPlanner already returns integer stage counts and exposes item `values`,
  which is a farming LP's dual in all but name. Integrality and shadow-price
  explanation are validated prior art, not our inventions.
- Corrected the README's Kornblume URL — it cited a fork, not upstream — and
  removed the now-stale wrapper bootstrap note.
- New open question **Q6** (equipment modelling) and follow-ups F1–F4.
- Backend still green after the model change: 15 tests passing.

### 2026-09-02 — hosting deferred; Phase 0 closed with an exception

- Q1 answered: VPS in principle, but **no money will be spent**, so nothing is
  provisioned. Logged as deviation **D1** with its full cost rather than as a
  scheduling note — it removes the Track B gate, which is the plan's central
  structural bet.
- Agreed mitigation: revisit hosting at **Phase 4** (on the plan's "never cut"
  list) rather than at the end. Phases 1-3 need no server.
- Fixed run #4: `ci.yml` was invalid YAML at line 67 — an unquoted `run:` scalar
  containing `: `. No jobs ran at all. Also caught, on re-reading, that the
  `deploy` job would have echoed a TODO and exited 0, painting the pipeline
  green while nothing deployed. Now `if: false` — skipped is honest, fake-green
  is not.
- Validated all five repo YAML files locally with `js-yaml` under node. Cheaper
  than a CI round trip; do this before every workflow push.

### 2026-09-02 — first CI runs, both jobs fixed locally

- Pushed; all three workflow runs went red. #1 and #2 predate the wrapper and
  could never have passed. #3 is the informative one, and it failed in both jobs
  for unrelated reasons.
- **Backend:** `gradlew` committed as mode `100644`. Git on Windows does not
  track the executable bit, so Ubuntu refused to run it. `git update-index
  --chmod=+x` sets the mode in the index without needing a Unix filesystem.
  This will recur for every shell script added from this machine.
- **Frontend:** no `package-lock.json`. That broke the job twice — `npm ci`
  requires a lockfile, and `setup-node`'s cache path could not resolve.
- Generating the lockfile doubled as B3: 415 packages resolved with no peer
  conflicts, so the `package.json` versions written from memory hold. Typecheck
  and `vite build` both pass; PWA service worker generates.
- Added `gradle/actions/wrapper-validation@v4` to CI, since `gradle-wrapper.jar`
  is now a committed binary that every build executes.
- Lesson worth keeping: the two failures were both *environment* mismatches
  between Windows and the Linux runner, not code defects. A green local build
  says nothing about either.

### 2026-09-02 — first green build

- Installed JDK 21 (Temurin 21.0.12 via winget). Two friction points worth
  recording: winget's Temurin package does **not** register `PATH` or
  `JAVA_HOME` (set manually in `HKCU:\Environment`), and **Gradle is not
  available in winget at all** — no choco or scoop on this machine either. The
  wrapper was generated by IntelliJ, which pinned Gradle **9.6.0**.
- Ran `./gradlew build`. Gradle 9.6.0 accepted the 8.x-era build scripts without
  complaint — the version-catalog lookup in the root `subprojects` block and the
  Spring dependency-management plugin both worked, which is more than expected.
- **One genuine failure:** ArchUnit rejects empty layers, and `store`, `raft` and
  `chaos` are empty by design. Fixed with `withOptionalLayers(true)` rather than
  by deleting the rules — the rules must be live before the code lands, not
  written at the moment the temptation to skip them peaks.
- Build now green: 35 tasks, 15 tests passing, `storm-almanac.jar` produced.
- Git initialised, MIT licensed, two commits. SSH push failed (local ed25519 key
  not registered with GitHub) so the remote is HTTPS via Git Credential Manager.
- **Still unverified and worth repeating:** no process has ever been started, no
  container has ever run, `npm install` has never executed, and CI has never
  fired. A green `gradlew build` is a compile-and-unit-test signal, nothing more.

### 2026-09-02 — scaffold from plan

- Read `plan.html` in full.
- Created the repo skeleton: Gradle multi-module backend (8 product modules,
  3 substrate modules, 1 app), Vite/React frontend, Docker Compose, CI workflow,
  nightly chaos workflow (disabled), `.gitignore`, `.editorconfig`, `.env.example`.
- Wrote the domain model in `modules/gamedata` as records and sealed
  hierarchies: `Source` (Stage/Craft/Shop/Reward), `Sink` (Upgrade/Fodder),
  `BannerModel` with `PityRule`/`FeaturedRule`/`PityScope`/`Floor`, the catalog
  axis (`Entity`, `StatCurve`, `Skill`, `Talent`), and `GameDefinition`.
  `Fodder` and `Goal.Satisfiability.PROBABILISTIC` are in from day one, per the
  plan's insistence that the second game's shape be designed for rather than
  retrofitted.
- Wrote real, tested implementations for the two pieces that are pure functions
  and cheap to get right early: `WilsonInterval` and `PityRule.rateAt`. The pity
  tests are written as acceptance fixtures against both games' published rates.
- Wrote `ModuleBoundaryTest` (ArchUnit) and `GameAgnosticismTest` (source scan)
  so the two central invariants fail the build rather than a review.
- Wrote ADRs 0001–0006, each with a reversal trigger.
- **Did not build or run anything.** No JDK on the machine, Docker daemon not
  running. Every "written, unverified" row above is genuinely unverified.
- Left the Gradle wrapper ungenerated (it needs a Gradle install to produce the
  binary `gradle-wrapper.jar`). Documented the one-time bootstrap in the README.
