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
- Last updated: **2026-09-08** (twelfth session)

---

## Status

- **Phase 3 — Identity and player state — is CLOSED**, 2026-09-08, criterion met.
  A plan is computed end-to-end from stored state on a real account: a person
  signs in, the sign-in creates the account, the profile is given an inventory, a
  roster and goals over HTTP, and the plan route reads all three back out of
  Postgres. **Read [what the tick does not cover](#what-is-still-unverified)
  before treating Phase 3's scope as finished — sync is in its scope and was not
  built.**
- **Phase 2 stays closed, criterion and scope both.** Nothing this session touched
  the model, the solver or the yields, and `RealUpstreamPlanTest` passed unchanged
  — which includes its p95 assertion.
- **Phase 4 — Frontend v1 and launch — is the next phase to open**, and it is the
  one that reverses [D1](#d1--deployment-deferred-2026-09-02) by its own terms.
- **Phase 0 stays closed by exception** — deploy deferred by D1 — and its box
  stays unticked, because nothing is deployed.
- **Track B: not started, and gated.** See [the gate](#the-gate).
- **Open right now:** [PR #13](https://github.com/kietnt4412/storm_almanac/pull/13),
  carrying N21 and N22. **Green and unmerged** — run `34208848060` on `5ad2462`,
  0 failed, 16 skipped and they are exactly the three snapshot-gated classes.
  `main` is at `eb2894a`. Merging it is the first action next session, and
  **write that line with the push rather than after it** — three sessions running
  have opened on a next action the remote had already done.
- **The optimizer has now been asked a question by something other than a test**,
  which had been true of nothing in this repository until today. What has *not*
  happened is a real OAuth exchange: no provider is configured and no client
  secret exists, so login is installed only when one is.
- **The time axis still has no real data to eat**, checked rather than assumed;
  N14 is proven on the fixture alone. The reason is better than "the upstream is
  silent" — **R1999 has no weekday rotation at all**, so `Availability.ALWAYS` is
  *correct* rather than a gap, and its daily income **is conditional on spending
  Activity**, so entering it as a `Reward` would make every plan systematically
  too cheap. Read [the economy facts](docs/game-facts/reverse-1999-economy.md)
  before treating either as an omission.

### Two standing caveats, read them every session

1. **The strongest tests in this repository are ones CI does not run.** Upstream
   data is fetched and never committed
   ([ADR 0009](docs/adr/0009-upstream-data-is-fetched-never-vendored.md)), so
   `RealUpstreamPatchTest`, `RealUpstreamPlanTest` and `CommunityBenchmarkTest`
   skip on the runner — 16 of the 228 tests. **Every performance number and every
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
   push before writing the entry that describes the push — but **write the
   tracker line in the same breath as the push.** Three sessions running have
   opened on a next action the remote had already done, because the session that
   pushed ran out of room before it wrote the line.
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
  document. **Keep it under about 550 lines** — roughly what it is now, and
  roughly what a session can read before starting work. If a session adds more
  than it removes, it has moved the problem rather than done the work.

---

## Current state

**What exists:** the repo skeleton, the domain model, a green backend build, a
game data pipeline that works end to end, an API that serves it, a real game's
data going through all of it, an optimizer that turns that data into a plan, a
reason to believe the plan, and — since this session — an account that can own
one, ask for it over HTTP, and be refused when it asks for somebody else's.

**The load-bearing claim:** on **nine** benchmark materials the cheapest stage
this project computes is the stage a published community guide tells players to
farm, and the optimizer's plan for a real goal set is cheaper than following that
guide — **3 880 Activity against 4 017**, and the 4 017 does not cover the whole
demand. Seventeen of the guide's twenty quoted drop rates land within three
percentage points of a sample this project had never seen. See
[the benchmark](docs/benchmarks/reverse-1999-community-answers.md).

Nine and not five because a drop yield carries how many runs it was observed over
and the solver uses the conservative end of a 95% interval on it
([ADR 0011](docs/adr/0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md)) —
7% dearer and better agreement at once.

Still true from earlier phases: **the solver says how much it does not know**
([ADR 0010](docs/adr/0010-a-plan-is-the-best-provable-in-the-budget.md)), **the
pipeline has met somebody else's data and held**, and **both of Phase 1's exit
questions are answered over HTTP rather than by a test calling a repository**.

Toolchain on this machine: JDK 21.0.12 (Temurin), Gradle **9.6.0** via the
committed wrapper. Remote is HTTPS at `github.com/kietnt4412/storm_almanac`.

| Area | State | The one thing to know |
|------|-------|-----------------------|
| Backend build | **Green** | **228 tests**, 0 failed, 0 skipped locally with snapshots present. **212 on CI**, because the same 16 snapshot-gated ones skip. Test tasks set `api.version=1.44` — [E2](#e2--docker-engine-29-refuses-testcontainers-api-version) |
| CI workflow | **Green on `dev`** | Run `34208848060` (PR #13, `5ad2462`): 0 failed, **16 skipped and they are exactly the three snapshot-gated classes** — `RealUpstreamPlanTest` 8, `CommunityBenchmarkTest` 5, `RealUpstreamPatchTest` 3. A pass there would mean a snapshot had been committed by accident. `main` is `eb2894a`. Action deprecations pending — **N5** |
| Domain model (`gamedata`) | **Persisted and round-tripped** | Record equality across the whole graph. `Drop` carries `sampledRuns`, where 0 means *declared*; equipment is an `Entity` (ADR 0007) |
| `gamedata` schema | **Applied, populated, round-tripped** | `V2` (28 tables), `V3` (a version is deletable), `V4` (`stage_drop.sampled_runs`). Seven invariants in `GameDataSchemaTest`, proven on the fixture and on two real R1999 patches |
| Ingest, write, read | **Done** | `CanonicalBundleParser` (14 tests, mostly refusal messages), `CanonicalBundleWriter` pinned to it by a round trip, JDBC both directions (ADR 0008) |
| Patch diff (`VersionDiff`) | **Done** | Three axes, subjects before fields, so a removed stage is one line. A drop reads as `0.21 over 105 runs` |
| `gamedata-cli` | **Done** | Onboarding a title is *adapt, preview, ingest, publish* — and publishing is a human approval, not a flag |
| Parser adapters | **One, reading what the upstream reads** | `:adapters:reverse-1999`, 25 tests. Newest `stages<major>_<minor>_greedy.json`, counts divided by the sampled run count and **that count carried onto every `Drop`**; `count: 1` converts as declared, because here it marks a fixed-reward stage |
| Game data API | **Served and verified** | Five game-data routes plus health, version-pinnable, every response carrying its version and attribution. 10 HTTP tests plus a hand check against `docker compose up` |
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
| `identity` / `player` schema | **Applied and round-tripped** | `V5` (6 tables). Four decisions in its header: **no FK crosses a schema**, nothing points into `gamedata` (version-scoped rows, an inventory outlives a patch), an identity is `(provider, subject)` and **never an email**, and absent means zero with a `CHECK` to keep the two representations from diverging |
| Sign-in | **Done, never exchanged a token** | Account created while the principal is built, not in a success handler. Everything provider-specific is `SignIn.from` — a pure function; OIDC says `sub`, Discord says `id`. **`oauth2Login` installs only when a provider is configured**, because no client secret exists (D1) and a blank client id fails a `ClientRegistration` outright. The deny is not conditional |
| Player API | **Served and authorized** | Everything under `/api/me`, so **no route takes an account id** and none's authorization can be forgotten. `OwnedProfiles.require` is the one check; another account's profile is **404, not 403**, so an id is not an enumeration oracle. PUT replaces a whole aggregate — there is no patch route, and no sync |
| Plan route | **Done — Phase 3's criterion** | `POST /api/me/profiles/{id}/plan`, synchronous because a two-second budget is a promise the optimizer can keep. Goals, inventory and roster come from Postgres; the body carries only `energyPerDay` and `horizonDays`. `?version=N` pins the patch |
| Bean wiring | **`Optimizer` and `SolveCache` are beans; `SolveCoordinator` is not** | Registered now because they finally sit on a path a real request takes — which is what N15's refusal was waiting for. The coordinator hands back a ticket for a solve that does not fit the synchronous budget, and there is no asynchronous surface for a ticket to be useful on |
| Architecture tests | **Passing** | `ModuleBoundaryTest` (Track B layers optional until they exist) and `GameAgnosticismTest` (source scan over planner/gacha/stats) |
| Docker Compose | **Verified** | `up --build` from cold: image builds, all three services healthy |
| Frontend | **Green locally, never served** | Typecheck and `vite build` pass, PWA SW generated. No page has been loaded in a browser |
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
- **The OAuth exchange has never run.** N22 replaced the older "the optimizer has
  never been asked a question by anything but a test" — it has now — but what
  makes it reachable is a principal, and **no principal has ever come from a real
  provider.** No client id, no secret, no redirect followed, because there is no
  URL to register one against (D1). Tested: the account, the principal, and every
  authorization rule around them. Untested: the token exchange. The first
  deployment is where this stops being theoretical.
- **The end-to-end test goes through MockMvc, not a socket** — a deliberate step
  down from `GameDataApiTest`'s real HTTP, because an authenticated session cannot
  be minted over one without an authorization server to redirect to. The filter
  chain, dispatcher, Jackson and database are exercised; **the servlet container
  is not, and that is the layer that caught Phase 0's 401.**
- **There is no sync, and it is in Phase 3's scope.** PUT replaces a whole
  aggregate: two devices do not merge, the second wins entirely, and a row the
  first added is gone. **Do not read Phase 3's tick as covering it** — the work
  is **N23**.
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
- **The frontend has never been served**, only typechecked and built. No page has
  been loaded in a browser and it has never spoken to the API.
- **Nothing has ever called the API under load.** Every request loads a whole
  version — fifteen queries — a deliberate deferral written into
  `GameDataReadModel`'s javadoc. The number to beat does not exist yet.
- **The time axis has never met real data, and on this game it never will.**
  Rewards, rotation and shops are modelled or refused on the strength of the
  synthetic fixture alone, and on the real 3.5 patch the whole of N14 reduces to
  one energy row. **Do not read "the optimizer has a calendar" as "the optimizer
  schedules real weeks."** The rotation machinery waits for a game that rotates —
  Phase 11 is the next chance. See
  [the economy facts](docs/game-facts/reverse-1999-economy.md).
- **Three shapes the model cannot express**, each of which would be a *silent*
  wrong answer if faked. Not defects in what shipped —
  [details and provenance](docs/game-facts/reverse-1999-economy.md):
  **(a) an item that restores energy** (Picrasma Candy, 60 Activity) — `Stage` is
  the only source touching the energy budget and only ever consumes;
  **(b) a reward conditional on spending energy** — R1999's Activeness is a rebate
  on farming, not income, which is why leaving it out is correct;
  **(c) a lifetime purchase limit** — `Shop` caps at "n per `Period`"; permanent
  stock is "five, ever".
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
- **`gacha` has no behaviour.** Every port in it is an interface with nothing
  behind it.

---

## Next actions

Ordered. Completed ones move to
[the archive](docs/history/tracker-archive.md#completed-next-actions).

- [ ] **Merge [PR #13](https://github.com/kietnt4412/storm_almanac/pull/13).**
      Green on `acd3efb` and CI-confirmed, unmerged, so `main` is a session
      behind. Do this first, and confirm the `main` push run goes green too.
- [ ] **N23 — Offline sync: the per-key patch Phase 3 owes.** Phase 3's scope
      names sync and Phase 3 did not build it, so this is a debt against a ticked
      box rather than a new idea. `PUT /inventory` replaces the whole map, which
      is correct for its signature and wrong for two devices: the second save
      wins entirely. What it needs is a second route with a different method
      (`PATCH`, per key), the `updated_at` column `V5` deliberately left out, and
      a merge that resolves per key rather than per aggregate. **The column and
      the merge go in together** — a timestamp nobody reads is a feature that
      only looks implemented, which is the reasoning that kept it out.
      Do it before Phase 4's offline PWA depends on it, not after.
- [ ] **Open Phase 4 — Frontend v1, and launch.** The frontend has been built and
      typechecked and **never served**; no page has been loaded in a browser and
      it has never spoken to the API. Phase 4 is also where
      [D1](#d1--deployment-deferred-2026-09-02) reverses by its own terms, so
      hosting is decided here rather than deferred again — and until there is a
      URL, **no OAuth provider can be registered**, which means the sign-in path
      stays unexercised end to end. Those two are the same task, not two.
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
- [ ] **B5 — Wire the real deploy.** Deferred to Phase 4 by
      [D1](#d1--deployment-deferred-2026-09-02). The `deploy` job stays
      `if: false` until there is a real URL to smoke.

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
- [x] **Phase 3 · Identity and player state** — **closed 2026-09-08.** A plan
      computed from stored state on a real account.
      **Sync is in its scope line and was not built** — **N23**. Two further
      qualifications in [the unverified list](#what-is-still-unverified).

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

**Gate status: CLOSED, and now structurally so — see
[D1](#d1--deployment-deferred-2026-09-02).** Nothing is deployed and nothing is
scheduled to be before Phase 4. Do not open `almanac-store`. When Phase 7 comes
round, re-read D1 and decide deliberately whether Track B on synthetic workloads
is still worth building.

### Track B — substrate

Scope for each is in [plan.html](plan.html); what matters here is the criterion
that says it is finished. **None of these may start before the gate opens.**

- [ ] **Phase 7 · almanac-store (LSM storage engine)** — 3 weeks. Wired behind
      `DropReportStore` alongside the Postgres one.
      **Exit:** crash-consistency fuzzing passes 10k randomized kills; benchmark
      vs Postgres published — including if Postgres wins.
- [ ] **Phase 8 · almanac-raft (consensus)** — 3 weeks. Exposed first as a
      replicated KV, so it is testable before anything depends on it.
      **Exit:** 5-node cluster survives repeated leader kills and partitions with
      no divergent log.
- [ ] **Phase 9 · Solver cluster** — 2 weeks. Replicated job log, leased work,
      idempotent completion, results over WebSocket.
      **Exit:** kill any node mid-solve — no lost solves, no duplicated solves,
      throughput recorded.
- [ ] **Phase 10 · Chaos and verification harness** — 1.5 weeks. Partitions,
      pauses, kills, disk corruption; linearizability checking; nightly runs with
      failing seeds saved as regression tests.
      **Exit:** nightly suite green for 7 consecutive nights, and one real bug
      found and written up.

### Track A — closing

- [ ] **Phase 11 · Punishing: Gray Raven** — 2 weeks. Data adapter, banner model,
      fodder economics, probabilistic goals. Whatever has to generalise,
      generalise in the model.
      **Exit:** PGR live with zero game-specific code in `planner`, `gacha` or
      `stats` — and the diff to prove it.
- [ ] **Phase 12 · Hardening and the writeups** — 1 week. Tracing, alerting, a
      backup actually restored from, a load test with published numbers,
      pre-rendered catalog pages.
      **Exit:** restore drill completed from a real backup; catalog pages
      indexed; three writeups published — the storage benchmark, the consensus
      verification, the multi-game diff.

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

**Decision:** no money will be spent on this project, so no hosting is
provisioned. The `deploy` job in `ci.yml` is `if: false`.

**What this costs, stated plainly:**

- **Phase 0 cannot meet its exit criterion.** "A green pipeline deploying a
  health endpoint to a real URL" is not achievable without a URL. Phase 0 is
  closed *with this exception noted*, not met. Do not tick it.
- **The Track B gate loses its meaning.** The gate exists so `almanac-store` and
  `almanac-raft` are shaped by real write volume, real read patterns and real
  failure modes. With nothing deployed there is no traffic to observe, and the
  plan is explicit that infrastructure built against imagined requirements is a
  toy.
- **The headline CV claim weakens.** "I run a live tool for two games with real
  users" is the sentence this project is arranged to earn.
- **Deploy problems get discovered late.** Phase 0 puts the deploy first
  precisely because that is when it is cheapest to fix.

**Mitigation, agreed:** revisit hosting at **Phase 4**, not at the end. Phases
1–3 need no server, so nothing is blocked between now and then.

**Reversal trigger:** the moment any free-tier host is acceptable, or the moment
Phase 4 is reached — whichever is sooner. Before starting Phase 7, re-read this
and decide consciously whether Track B is still worth doing on synthetic
workloads. It may be; that is a decision to make with open eyes, not by default.

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

**Fixed, committed, and only worth knowing if it comes back.**
`backend/build.gradle.kts` sets `systemProperty("api.version", "1.44")` on every
`Test` task — on the *task*, not an environment variable, and the distinction is
the whole fix. Why, and why the obvious alternatives do nothing:
[the archive](docs/history/tracker-archive.md#e2--the-full-account).

**Also:** Docker Desktop takes minutes to start on this machine and `docker info`
hangs rather than failing while it does. Give it time instead of concluding it is
broken.

### E3 · Git prompted for an account on every push

**Fixed 2026-09-08.** Windows Credential Manager held two GitHub logins —
`git:https://tuankiet4412@github.com` alongside `git:https://github.com`
(`kietnt4412`) — and Git Credential Manager shows an account picker whenever
there is more than one. Neither `git config` nor `gh auth` had a second identity,
so looking there finds nothing; the second credential is only visible to
`cmdkey /list`. The stray one was deleted and the surviving username pinned:

```bash
git config --global credential.https://github.com.username kietnt4412
```

The pin is what stops it coming back the next time a second account touches this
machine.

---

## Open questions

Carry these forward until answered; strike through with the answer when resolved,
then move the entry to
[the archive](docs/history/tracker-archive.md#answered-questions).

- **Q2 — Where the drop data should come from.** *Narrowed twice, still open.*
  Kornblume is not canonical — it is a presentation layer over Huiji Wiki
  (characters), 必要的记录 (drop data) and ArkPlanner (algorithm). But the
  sampling itself is in the repository, in `stages<major>_<minor>_greedy.json`:
  raw drop counts and the number of runs behind them, which the adapter now reads
  and carries. So **F1** in [docs/prior-art.md](docs/prior-art.md) — go to
  必要的记录 directly — changes shape: what it buys is *fresher and more*
  sampling, not different numbers, against the cost of a second upstream to
  adapt.
  **This is now the fix for the two surviving benchmark disagreements** (Milled
  Magnesia at 105 runs, Liquefied Terror at 113). ADR 0011 discounts a thin
  sample honestly; only more sampling makes it thick. Weigh it against Phase 6,
  where our own drop reports would do the same job with data we own.
  **It also owns everything the time axis cannot exercise.** After N14 the model
  prices rewards, weekday rotation and (in a dozen lines it does not have)
  shops — and this upstream publishes none of the three. A second source is now
  the only thing standing between a working calendar and a calendar that has met
  real data. See [ADR 0013](docs/adr/0013-the-horizon-is-a-scalar-not-an-index.md).
- **Q5 — Is our "3.5" the same 3.5 anyone else means?** *New 2026-09-08, and the
  same shape as the mistake that cost five sessions.* `fetch-upstream.sh` pins
  `8b40541a9c42`, message `update 3.5`, dated **2026-03-17** — confirmed through
  the GitHub API, not assumed. Global 3.5 ran **2026-05-28 to 2026-07-02**. Two
  and a half months apart, most likely because Kornblume tracks **CN**, which
  runs ahead. If so, everything here called "3.5" is CN 3.5. **This does not
  invalidate the nine agreements** — the guide was already known to be written
  for 2.7 against a 3.3 sample — but the version labels may not mean what a
  reader assumes. Resolve by checking content that differs between the two
  releases; until then do not write "3.5" publicly without saying which.
- **Q3 — Seed data provenance.** *Answered operationally, open on one point.*
  Kornblume has **no `LICENSE` file**, so it is all rights reserved by default —
  absence of a licence is not permission. Enforced since 2026-09-06 by
  [ADR 0009](docs/adr/0009-upstream-data-is-fetched-never-vendored.md): fetched
  at need, never committed, real-data tests skip when it is absent, and the
  committed fixtures are the synthetic `proving-ground` title.
  **What stays open:** the owner's scope is personal and portfolio use, and
  **F2 — ask the maintainer directly — becomes a release blocker the moment this
  is deployed publicly.** It needs a human to send a message; a session cannot.
  **Phase 4 is that moment**, so this is now a blocker on the next phase rather
  than a distant one. Nothing in this entry authorises a public deployment
  carrying upstream numbers.
- **Q4 — Rate verification.** The pity numbers in `PityRuleTest` come from the
  secondary sources the plan cites. They must be checked against in-game
  disclosure before the simulator ships (Phase 5).

---

## Session log index

Full entries are in [the archive](docs/history/tracker-archive.md#session-log),
newest first. **Write the entry there; add its line here.**

| Date | Session | What it was |
|---|---|---|
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
