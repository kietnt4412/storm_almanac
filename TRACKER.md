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
- Last updated: **2026-09-09** (fourteenth session)

---

## Status

- **Phase 3 — Identity and player state — is CLOSED**, 2026-09-08, criterion met,
  and **its scope is now closed too**: N23 built the sync the phase named and did
  not deliver. A plan is computed end-to-end from stored state on a real account,
  and two devices editing that state merge per key rather than overwriting each
  other. Read [what is still unverified](#what-is-still-unverified) before
  treating any of it as proven in production.
- **Phase 2 stays closed, criterion and scope both.** Nothing this session touched
  the model, the solver or the yields.
- **Phase 4 — Frontend v1 and launch — is OPEN**, 2026-09-09.
  **[D1 is reversed](#d1--deployment-deferred-2026-09-02)** — Vercel and Render,
  free tier, still no money — and **the frontend has been served and has spoken
  to the API**, which had never happened. Nothing is deployed: the decision is
  made, the wiring is **B5**.
  **Two blockers, and they are different in kind.** *The code:* no provider is
  configured, so there is no login URL and every `/api/me` route is 401 —
  Phase 4's whole signed-in product has nothing to develop against (**N24**).
  *The data:* see the next line — F2 is gone, and what replaced it is bigger.
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
- **The remote, checked 2026-09-09 — re-check it, do not trust it.** `main` is
  `2864c95` (PR #13 merged, run `34217574439`). `dev` is `6376b33` as
  [PR #14](https://github.com/kietnt4412/storm_almanac/pull/14), carrying N23 and
  the Phase 4 opening — green, run `34298619283`, 0 failed and 16 skipped and
  they are exactly the three snapshot-gated classes. This is a fact here rather
  than a next action because four sessions running opened on a "merge PR #n" the
  maintainer had already clicked between sessions.
- **The optimizer has been asked a question by something other than a test.**
  What has *not* happened is a real OAuth exchange: no provider is configured and
  no client secret exists, so login is installed only when one is.
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
  document. **Keep it under about 550 lines** — roughly what it is now, and
  roughly what a session can read before starting work. If a session adds more
  than it removes, it has moved the problem rather than done the work.

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
| Backend build | **Green** | **257 tests**, 0 failed, 0 skipped locally with snapshots present. **241 on CI**, because the same 16 snapshot-gated ones skip. Test tasks set `api.version=1.44` — [E2](#e2--docker-engine-29-refuses-testcontainers-api-version) |
| CI workflow | **Green on `main`** | Run `34217574439` (`2864c95`, PR #13 merged): 0 failed, **16 skipped and they are exactly the three snapshot-gated classes** — `RealUpstreamPlanTest` 8, `CommunityBenchmarkTest` 5, `RealUpstreamPatchTest` 3. A pass there would mean a snapshot had been committed by accident. Action deprecations pending — **N5** |
| Game data pipeline (Phase 1) | **Closed and stable** | Model, schema, ingest, diff and CLI, [described in full in the archive](docs/history/tracker-archive.md#closed-phases-in-full). `V2`–`V4`, 28 tables, seven invariants in `GameDataSchemaTest` proven on two real R1999 patches; parser and writer pinned to each other by a round trip (ADR 0008); onboarding a title is *adapt, preview, ingest, publish* and publishing is a human approval, not a flag. `Drop` carries `sampledRuns`, where 0 means *declared* |
| Parser adapters | **One — and demoted to a cross-check by [ADR 0015](docs/adr/0015-game-data-is-sourced-first-hand-not-adapted.md), now in code** | Still reads what the upstream reads and **its output is no longer what the product will ship**. Since ADR 0016 it **hard-codes `THIRD_PARTY` provenance and cannot be told otherwise**, so what it produces fails a plain `publish` — there is no call site to launder data through. Kept, not deleted: diffing the first self-sourced bundle against an independent reading of the same patch is worth more as a check than it ever was as a source. `:adapters:reverse-1999`, 25 tests. Newest `stages<major>_<minor>_greedy.json`, counts divided by the sampled run count and **that count carried onto every `Drop`**; `count: 1` converts as declared, because here it marks a fixed-reward stage |
| Provenance | **Done — written, enforced, not yet read back out** | [ADR 0016](docs/adr/0016-provenance-is-a-property-of-the-data.md). A bundle declares `Provenance` records, defaults every fact to one (`sourcedBy`) and overrides per `FactRef` (`kind:slug`); `V7` materialises **one row per declared fact**, because a default is an authoring convenience and a database that stored it could not answer the question alone. **`publish` refuses a version that is not first-hand and names the facts**; `publish(…, true)` and the CLI's `second-hand` word are the explicit exception. The first-hand policy lives in `Provenance.Origin` and nowhere else — the migration constrains the set and says nothing about which count. **Silence parses and cannot publish** (`UNRECORDED`). Nothing serves it yet: that is N25's catalog page |
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
| `identity` / `player` schema | **Applied and round-tripped** | `V5` (6 tables). Four decisions in its header: **no FK crosses a schema**, nothing points into `gamedata` (version-scoped rows, an inventory outlives a patch), an identity is `(provider, subject)` and **never an email**, and absent means zero with a `CHECK` to keep the two representations from diverging. `V6` adds the two sync tables |
| Sign-in | **Done, never exchanged a token** | Account created while the principal is built, not in a success handler. Everything provider-specific is `SignIn.from` — a pure function; OIDC says `sub`, Discord says `id`. **`oauth2Login` installs only when a provider is configured**, because no client secret exists (D1) and a blank client id fails a `ClientRegistration` outright. The deny is not conditional |
| Player API | **Served and authorized** | Everything under `/api/me`, so **no route takes an account id** and none's authorization can be forgotten. `OwnedProfiles.require` is the one check; another account's profile is **404, not 403**, so an id is not an enumeration oracle. PUT replaces a whole aggregate; PATCH merges per key |
| Offline sync | **Done — inventory and roster** | `PATCH`, last-write-wins per key ([ADR 0014](docs/adr/0014-sync-is-last-write-wins-per-key-against-a-clock-that-outlives-the-value.md)). The clock is a **table, not a column**, because a removal deletes its row and a tombstone cannot live on the row it outlives; a `PUT` also writes a whole-aggregate **watermark**, because a full save speaks for keys that have never existed and no per-key row can say that. Client timestamps, clamped to the server's clock. The response names the keys that lost. **Goals have no PATCH** — an ordered list has no per-key merge |
| Plan route | **Done — Phase 3's criterion** | `POST /api/me/profiles/{id}/plan`, synchronous because a two-second budget is a promise the optimizer can keep. Goals, inventory and roster come from Postgres; the body carries only `energyPerDay` and `horizonDays`. `?version=N` pins the patch |
| Bean wiring | **`Optimizer` and `SolveCache` are beans; `SolveCoordinator` is not** | Registered now because they finally sit on a path a real request takes — which is what N15's refusal was waiting for. The coordinator hands back a ticket for a solve that does not fit the synchronous budget, and there is no asynchronous surface for a ticket to be useful on |
| Architecture tests | **Passing** | `ModuleBoundaryTest` (Track B layers optional until they exist) and `GameAgnosticismTest` (source scan over planner/gacha/stats) |
| Docker Compose | **Verified** | `up --build` from cold: image builds, all three services healthy |
| Frontend | **Served, and still the Phase 0 shell** | 108 lines, one page, one call — it renders `/api/health` from a live backend and nothing else. `npm run dev --prefix frontend` (`.claude/launch.json`) proxies `/api` to `localhost:8080`, so local is same-origin. **Serving it found the two defects five sessions of green builds had not** — see [unverified](#what-is-still-unverified) |
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
- **No two real devices have ever synced.** N23's every scenario is one MockMvc
  request following another inside one JVM. The concurrency the merge SQL is
  shaped for — two requests interleaving on the same key — is argued for and not
  measured, and **the client half does not exist at all**: no queue, no retry, no
  offline store. That is Phase 4's PWA, and it has never been served.
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
- **The frontend is served and is still the Phase 0 shell.** One page, one call.
  That the plumbing works is not that there is a product. *(The old "never
  served" entry, and the two defects retiring it turned up, are in
  [the session log](docs/history/tracker-archive.md#session-log).)*
- **No authenticated page has ever been rendered, and cannot be yet.** With no
  provider configured there is no login URL and every `/api/me` route is 401, so
  the whole signed-in half of the frontend has nothing to develop against —
  **N24**.
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
- **Provenance is written and never read.** The publish gate queries it; no API
  response carries it. It becomes the honesty claim the moment a catalog page
  exists — **N25**.
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
- [ ] **N24 — Make a signed-in page developable.** Phase 4's product is all
      behind `/api/me` and **nothing can sign in**: no provider configured, so no
      `oauth2Login`, no login URL, 401 everywhere. Two ways out, not equivalent.
      **A real provider** needs the deployed URL, so it lands with **B5** and
      finally runs the exchange that never has. **A development-only sign-in**
      unblocks the UI now and is the more dangerous: it must be impossible to
      enable in production *by construction*, not by configuration, and it proves
      nothing about the exchange. **The second is not an excuse to skip the
      first.**
- [ ] **N25 — Build Phase 4's screens.** Inventory editor for fast bulk entry,
      goal picker, plan view with the per-stage breakdown, catalog browse and
      search with the personalized overlay on every character page — **the
      overlay is the whole argument for having a catalog**, so it ships with it.
      Blocked on **N24** for everything signed-in; the catalog half is public.
      **The client half of N23 is here too:** the merge exists on the server and
      nothing queues, retries or stores an edit offline, which is the difference
      between a PWA that is offline and one that is merely installable.
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
      redirect URI needs this. Ties to **N24**.

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
      Landed so far: the app is served, a page has been loaded in a browser and
      it speaks to a live API, and hosting is decided (Vercel + Render, D1
      reversed). Nothing is deployed and nothing signed-in can be built yet —
      **N24**.
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
first, and nothing has tested it since.

**Nothing is deployed yet.** The decision is made, the wiring is not — **B5**.
Two consequences of the split to settle before writing any of it:

- **Two origins or one.** The backend's session model is same-origin throughout:
  a cookie session, CSRF in a cookie the page reads, an OAuth redirect landing
  back where it started. A Vercel rewrite of `/api/*` to Render preserves all of
  it for one extra hop; two real origins cost CORS, `SameSite=None; Secure`, and
  a redirect that has to cross back. **The rewrite is cheaper and should be taken
  deliberately** — that hop sits on the path of a two-second solve promise.
- **The free tier sleeps.** A cold start is tens of seconds against an optimizer
  budgeted at two. Warm-up ping, an honest loading state, or accept it — decide
  before five strangers meet it.

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
**One command tells the two apart:**

```bash
powershell -NoProfile -Command "wsl -l -v; Get-Service com.docker.service"
```

`com.docker.service` = `Stopped` means somebody has to start Docker Desktop by
hand and accept the elevation prompt. A session cannot — `Start-Service` from a
non-elevated shell fails with *Cannot open com.docker.service service on
computer '.'* — so **ask rather than keep waiting.**

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
