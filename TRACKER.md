# Storm Almanac — build tracker

**Single source of truth for progress across chat sessions. Read this first,
update it last.** If a session ends without this file reflecting what happened,
the next session starts from a lie.

**History lives in [docs/history/tracker-archive.md](docs/history/tracker-archive.md)** —
the session log, completed actions, closed phases, answered questions, and the
full text of everything this file has stopped carrying. Nothing is ever deleted;
it moves. What stays here is what is still *operative*: age is not the criterion,
being finished with is.

- The plan is [plan.html](plan.html) (13 phases, two tracks); [README.md](README.md)
  is the public face and [CLAUDE.md](CLAUDE.md) the working agreement.
- **Rewritten four times when it passed 550 lines** — end to end
  [2026-09-18](docs/history/tracker-archive.md#the-tracker-as-it-stood-before-the-2026-09-18-compression) (826),
  *Status* [2026-09-21](docs/history/tracker-archive.md#what-status-carried-until-the-2026-09-21-rewrite) (614),
  the session index 2026-09-22 (566) and again 2026-09-24 (561). Anything it no longer carries is in the archive verbatim — go and
  decide whether it is still operative rather than assuming it was lost.
- Last updated: **2026-09-24** (thirty-eighth session)

---

## Status

- **The launch title is Punishing: Gray Raven** since 2026-09-13
  ([D3](#d3--launch-title-swapped-to-punishing-gray-raven-2026-09-13)); R1999
  moves to Phase 11, because Kornblume solves per player and on R1999 this
  product overlapped a deployed tool almost feature for feature. **Everything
  below that says "R1999" was true when written and still is — it is just no
  longer the launch.**
- **PGR is read first-hand** ([the note](docs/game-facts/punishing-gray-raven-research-disclosure.md)) and
  [its bundle](data/bundles/punishing-gray-raven-steering-by-light.json) is **published at sequence 7** — 117
  facts over eight provenance entries, the level ladder, thirteen Promote gates, one weekly ladder, a
  **05:00 UTC** reset, and since 2026-09-22 a **pull price**. Every sequence has published and
  read back as *no changes*, sequence 7 included. **Sequence 7 closed N37 and N28's modelling half** and is the
  first sequence carrying something that is not a fact at all (the EXP pool names, ADR 0028). What it holds is
  [in the table below](#what-the-next-work-touches). **Reading the client overruled the guides four times** — a
  per-banner featured rate, events with no shops, a drawn wall, a `PityScope` inheriting across pools where R1999
  clears — the standing argument for the client over a wiki
  ([the four](docs/history/tracker-archive.md#what-status-carried-until-the-2026-09-21-rewrite)).
- **Eleven decisions are live, and each is its own account — read the ADR, never a summary.**
  [0019](docs/adr/0019-a-gate-is-a-goal-inside-a-goal-and-progress-is-demanded-as-an-item.md) gates ·
  [0020](docs/adr/0020-a-limit-that-never-resets-is-offered-whole.md) lifetime caps ·
  [0021](docs/adr/0021-one-step-at-several-prices-is-a-choice-the-solver-makes.md) choices ·
  [0022](docs/adr/0022-a-grant-sized-by-the-player-is-an-answer-the-reader-supplies.md) `reach` ·
  [0023](docs/adr/0023-a-drawn-guarantee-is-a-rate-curve-not-a-state-dimension.md) drawn walls ·
  [0024](docs/adr/0024-an-expiring-grant-is-a-deadline-the-plan-reports-not-a-schedule-it-builds.md) deadlines ·
  [0025](docs/adr/0025-the-day-boundary-is-a-property-of-the-game.md) day boundary ·
  [0026](docs/adr/0026-a-crossed-gate-is-a-reached-state.md) ·
  [0027](docs/adr/0027-a-roster-entry-holds-the-states-an-entity-has-reached.md) roster states ·
  [0028](docs/adr/0028-a-name-for-a-progress-kind-is-the-bundles-word-not-the-games.md) progress names ·
  [0029](docs/adr/0029-income-is-what-the-bundle-declares-not-a-rate-per-day.md) income. **0022's trigger is
  live:** `reach` becomes player state the moment a reader answers twice, which since 2026-09-21 means *a second
  device*. **Three loose ends sit outside all eleven:** the **Themed Construct archetype is expressible and not
  authored** — its wall is drawn 80–100 and ADR 0023 made that writable on 2026-09-20, but nobody has written it;
  **`Availability.opensAt` is read by nobody**, deliberately; and **`GameAgnosticismTest` is blind
  to a constant right for no game** — `UTC` is not a game name, and a javadoc caught 0025's bug where no test did.
- **Going first-hand on game data**, 2026-09-09
  ([ADR 0015](docs/adr/0015-game-data-is-sourced-first-hand-not-adapted.md)), superseding 0009 and closing Q2, Q3,
  F1 and F2. Kornblume is unlicensed. **Nothing is deleted yet and the order matters:** the adapter stays as a
  never-shipped cross-check until a replacement exists, because removing it first leaves the project with no real
  data at all.
- **The bootstrap problem is the main risk in the project, and the cheap way out is not there** (N26). No yield
  means no ranking means no plan; own drop data means Phase 6, which means users, which means a working plan.
  **The publisher does not disclose stage drop rates** — the screen grades a reward `Fixed`, `Common` or `Possible`
  and prices only the first, so **15 of 779 drop facts are declared and 764 sampled**, ~2% free. Two consolations:
  **gacha rates *are* disclosed**, and the grade is itself a free first-hand fact for all 595 pairs, with nowhere to
  live until Phase 6 ([the note](docs/game-facts/reverse-1999-drop-disclosure.md)).
- **Phases 0–3 and 5 are closed; Phase 4 is open; Track B is gated.** Phase 0 closed *by exception*, box unticked until the pipeline deploys; Phase 5 closed **out of order on purpose** ([D2](#d2--phase-5-entered-before-phase-4-closed-2026-09-12)) and **nothing calls either gacha engine**. [The board](#track-a--product).
- **The backend is deployed and the page is not** ([D1 reversed](#d1--deployment-deferred-2026-09-02)) — since
  2026-09-24 `https://storm-almanac.onrender.com` serves PGR sequence 7 from Neon; Vercel is next. Free tier, still no
  money. **Phase 0's box stays unticked** — a URL answers 200, but Render deploys it and the pipeline's `deploy` job is
  still `if: false`. The rest of the wiring is **B5**, the only thing that can unblock a real OAuth exchange.
  **`backend/Dockerfile`'s COPY list is B5's path and drifts in silence** — it omitted `adapters/` from Phase 1
  until 2026-09-09, every image build failing in six seconds while this file called it verified, so **anything added
  beside `modules`, `adapters`, `substrate`, `app` needs a line there**.
- **The remote, last checked 2026-09-24 (thirty-eighth) — re-check it, do not trust it.**
  **[PR #39](https://github.com/kietnt4412/storm_almanac/pull/39) is MERGED** (02:39Z, all runs green first);
  at session start this file named #37 while #38 had merged — **stale on five consecutive checks**, never once
  right, so re-check rather than read.
  B6 means a commit on `dev` with no PR open is still built.
  **What the trigger does not do is watch the merge** — PR #25 merged *before its own run finished*
  and was green by luck (archive, twenty-sixth) — so **wait for the run before merging** still
  stands, and is now the only half of this a person has to remember. `dev` sits behind `main` by
  merge commits that never come back down, which is harmless and *not* a reason to rebase.

### Two standing caveats, read them every session

1. **The strongest tests here are ones CI does not run.** Upstream data is fetched and never committed
   ([ADR 0009](docs/adr/0009-upstream-data-is-fetched-never-vendored.md)), so `RealUpstreamPatchTest`,
   `RealUpstreamPlanTest` and `CommunityBenchmarkTest` skip on the runner — 16 tests. **Every performance number and
   every comparison with an outside answer in this file comes from a test the pipeline does not run.** Run
   `backend/tools/fetch-upstream.sh` before trusting a green build to mean the pipeline handles real data — **and
   until 2026-09-21 doing exactly that proved nothing**, because the property was not forwarded to the test worker
   and the directory was not a task input, so the fetch changed nothing Gradle could see and `:app:test` came back
   `UP-TO-DATE` from the run that had no snapshots. Both are fixed and the fix is measured (N36). **The dated
   numbers live in [docs/benchmarks/snapshot-gated-runs.md](docs/benchmarks/snapshot-gated-runs.md) — append a run
   there rather than re-asserting a figure here.**
2. **A green build says nothing about whether the data going into it is what the upstream publishes.** The adapter
   spent five sessions reading a stale stage table with two thirds of the game missing, green throughout. What
   caught it was going to look for somebody else's answer.

---

## How to use this file

1. **At session start:** read *Status*, *Current state*, *Next actions* and *Open
   questions*. Do not re-derive them from the code.
2. **During the session:** tick boxes as work lands, not when it is planned.
   **Never write "merge PR #n" or "publish version n" as a next action** — both
   are clicks that happen between sessions, so the line is stale before it is
   read. The state of the remote goes in *Status*, checked rather than trusted.
3. **At session end:** update *Last updated*, *Status* and *Current state*, prune
   *Next actions*, **append the session entry to
   [the archive](docs/history/tracker-archive.md#session-log)**, and add one
   **short** line to the [index](#session-log-index) here.

Rules that keep this file honest:

- A box is ticked only when its exit criterion is met, not when the code exists.
  "Done" means tested and committed; "scaffolded" means the shape exists and the
  behaviour does not — say which one.
- Never delete a session-log entry; supersede it, and say what the older one got
  wrong. **When an item closes, move it to the archive** rather than striking it
  through in place: this file reached 2 412 lines by keeping every corpse in the
  room, and a handoff nobody reads is worse than none.
- **Keep it under 550 lines, measured with `wc -l`** rather than carried forward.
  Six sessions trimmed and it still went 747 → 742 → 749 → 769 → 790 → 826
  ([the ledger](docs/history/tracker-archive.md#the-line-count-ledger)): trimming
  cannot beat a document that has to absorb every session's findings, so **the
  twenty-third rewrote it instead. If this file passes 550 again, rewrite a
  section — do not shave it**, and suspect the row nobody can read in one breath,
  which `wc -l` cannot see.

---

## Current state

**What exists:** a game data pipeline that works end to end, an API that serves it, a real game's data going
through all of it, an optimizer that turns that into a plan *and a reason to believe the plan*, an account that can
own one and be refused somebody else's, two devices that edit what it owns without deleting each other's work,
**six** screens a browser has driven, a page that says where its numbers were read, an app that renders with its
server switched off, two gacha engines that agree about both published games — and one hand-typed bundle of the
launch title's own data, which the solver plans from, EXP, one gate and one scored weekly included.

**The load-bearing claim, and it is a number to re-earn.** On **nine** benchmark materials the cheapest stage this
project computes is the one a published guide tells players to farm, and the plan for a real goal set costs
**3 877 Activity against the guide's 4 017** — which does not even cover the whole demand. Nine and not five
because a yield carries its sample size (ADR 0011). **Every one of those numbers comes from Kornblume-fed inputs**,
which ADR 0015 says the product will not ship, so until a self-sourced bundle reproduces them they are evidence
about somebody else's numbers run through our solver. **The method survives untouched**, because the guide is a
separate published artifact: a claim to re-earn, not a test to delete
([the benchmark](docs/benchmarks/reverse-1999-community-answers.md)). **Re-measured 2026-09-21 and again
2026-09-22 against freshly fetched snapshots** ([the dated runs](docs/benchmarks/snapshot-gated-runs.md)) —
which confirms the same Kornblume data still says what this file says it says, and re-earns nothing.
**The plan cost moved 3 880 → 3 877 between those two runs**, 0.08%, with nothing in the change touching the
model: branch-and-bound landing on a different equally-good answer inside its budget is the expected shape
(ADR 0010), and the figure is carried here as the measured one rather than the remembered one.

Toolchain on this machine: JDK 21.0.12 (Temurin), Gradle **9.6.0** via the
committed wrapper. Remote is HTTPS at `github.com/kietnt4412/storm_almanac`.

### What the next work touches

| Area | State | The one thing to know |
|------|-------|-----------------------|
| Backend build | **Green** | **438 tests** in full 2026-09-24, 0 skipped locally with the 2026-09-22 snapshots present; the last *measured* gated run is still [the dated one](docs/benchmarks/snapshot-gated-runs.md) of 2026-09-22 — nothing in this session touched the model; **422 expected on CI**, where those 16 skip. `:app:test` depends on `:app:bootJar`, and declares **two** directories outside every source set as inputs — `data/bundles`, without which `AuthoredBundlesTest` came back `FROM-CACHE` after a bundle changed, and `build/upstream-snapshots`, without which fetching upstream left the task `UP-TO-DATE` (N36). **The second was the identical bug ten lines below the first's fix.** `api.version=1.44` — [E2](#environment-notes-this-machine-only) |
| Authored game data | **One bundle, sequence 7 published, first-hand** | **Sequence 7 added one item and one provenance entry** — the Event Construct R&D Ticket, **5★ off a tile on 2026-09-22**, which is the fourth currency graded that way where its own item card grades nothing. It carries the banner's pull price (250, credited to the 2026-09-18 pool-screen sitting) and the three EXP pool names, which are **not facts and declare no provenance** (ADR 0028) and so show in the diff as three `progress '<kind>'` subjects and in no provenance count. **117 facts over eight provenance entries.** Helentine: Lacrimosa (level to 80 as a **thirteen-link chain, every gated level priced**, 13-step Promote **all gated**, 7 skills to 18, Evolve to SS), Hear the Bell, Samantha (Overclock, Upper Resonance at three prices), one stage, 11 shop rows, 2 box crafts, 5 fodder rules, the weekly Phantom Pain Cage's nine tiers, and the game's **05:00 UTC** day boundary — which is a game-level field and so adds no fact, exactly why `Facts` had to learn to flatten `Game` (ADR 0025). **What each sequence added and when it published is [in the archive](docs/history/tracker-archive.md#session-log)**; every one read back as *no changes*. Three Cage tiers are **written short** — a gold 5★ card, a 4★ chip and a portrait item were never opened, so those grants are absent, which makes plans dearer and never cheaper. `AuthoredBundlesTest` parses every file in `data/bundles` and fails on any fact the project may not publish, on a provenance mapping naming no fact, and on an **empty** directory. `AuthoredBundlePlanTest` plans from it, so a correction moves a plan: a skill to its cap is **150 Serum**, a Memory's Overclock **420**, her last rank **1 470**, Samantha's Resonance **90**, and Evolve to SS **30 shards from a stock that never resets** (ADR 0020) — or, for a reader who says they clear the whole Cage, **63 days and none at all** (ADR 0022) |
| CI workflow | **Green, Node 24; triggers on `dev`** | Since B6 (2026-09-21) it runs on push to `main` **and `dev`** as well as `pull_request`, so a push to the working branch with no PR open is built rather than silently ignored. **One annotation, and it is GitHub's rather than ours:** `ubuntu-latest` migrates to Ubuntu 26 from 2026-10-19 — nothing to fix, worth knowing before a green run starts carrying a warning nobody placed. **A push to `dev` while a PR is open from `dev` runs twice, on purpose** — the concurrency group stays keyed by ref, because collapsing push and PR into one group lets `cancel-in-progress` cancel the run the PR needs green. **The `deploy` job is still `if: false` and must be gated to `refs/heads/main` when B5 turns it on**, or it ships every commit landing on `dev`. Last *executed* suite: run `34695206362`, 16 skipped, exactly the three snapshot-gated classes. **`gradle/actions` held at v5** — v6 needs Gradle's Terms of Use accepted, which is the maintainer's call. **Counting PASSED lines in a log undercounts**; read task outcomes |
| Provenance | **Written, enforced, and read** | [ADR 0016](docs/adr/0016-provenance-is-a-property-of-the-data.md). `V7` stores one row per declared fact; `publish` refuses a version that is not first-hand and **names the facts**. **`ProvenanceRepository` is a second port** — the solver cannot see where a number came from, so it cannot be made to prefer one. Silence is `UNRECORDED`: parses, cannot publish |
| Parser adapters | **One, demoted to a cross-check** | `:adapters:reverse-1999`, 25 tests. **Hard-codes `THIRD_PARTY`, so it fails a plain `publish`** — there is no call site to launder data through. Kept because diffing the first self-sourced bundle against it is worth more than it ever was as a source |
| The MIP (`EnergyMip`) | **Stages, crafts, shops, rewards and fodder** | ojAlgo, integer runs, inventory subtracted, every variable bounded — the bound is what makes a real patch solvable. A purchase is a conversion capped at limit × *whole* periods, or the whole allowance of one that never resets, which the plan says it assumed unspent (ADR 0020); feeding fodder is a conversion into a `progress:<kind>` item, and paying one of a step's several prices a conversion into a `choice:` item (ADR 0021). Gates are not in the model: `DemandResolver` turns them into demand (ADR 0019). **A grant behind a score the reader has not cleared is dropped before the model** and reported in the notes, so the counts in a plan and in a refusal are the game *that reader* plays (ADR 0022) |
| The time axis | **A scalar, not an index** | [ADR 0013](docs/adr/0013-the-horizon-is-a-scalar-not-an-index.md). Rotation is capacity shared over *subsets* of weekday restrictions; **no variable is indexed by day**, which is why p95 has held across three measurements — **1 807 ms, then 1 808 on 2026-09-21, then 1 812 on 2026-09-22** (max 1 814), a 7 ms band on a 2 000 ms budget with 188 ms of headroom. All three drifts are upward, which is worth watching and is not yet drift; the dated runs are in [the benchmark log](docs/benchmarks/snapshot-gated-runs.md). An expiring grant is **supply plus a reported deadline**, never a scheduled claim (ADR 0024). **Which weekday day zero is comes off the game** — `Game.dayBoundary`, a zone and an hour, and `null` means the midnight-UTC every version published before `V12` was planned by (ADR 0025) |
| `gacha` — engines | **Phase 5's criterion, and nothing calls them** | [ADR 0018](docs/adr/0018-the-gacha-engines-answer-one-question-about-one-rarity.md). An exact chain and 500 000 seeded trials sharing one validated `PullModel`, so both refuse the same banners for the same reasons. **53 tests, worst gap 0.110 points over 108 questions, at 2.22 standard errors.** Only the headline rarity is modelled. **Through a drawn guarantee they take different roads on purpose** — the chain integrates it out, the simulation draws it (ADR 0023) — which is what caught the simulation sampling the prior, 0.558 against the chain's right 0.382. **A generated question set is not automatically one that probes the band it generated:** every generic question for a wall of 100 lands below the drawn range or at certainty |
| `gacha` — income model | **Written, and nothing calls it** | [ADR 0029](docs/adr/0029-income-is-what-the-bundle-declares-not-a-rate-per-day.md), 2026-09-22. `BannerModel.pullPrice` (`V15`, nullable — **unstated is not free**, and `pricedPull()` refuses rather than answering zero) plus `DeclaredIncomeModel`: balance from the inventory, accrual from the `Reward` rows that grant the currency at their own cadence, `reach` applied and every dropped grant **named**. **There is deliberately no pulls-per-day constant** — that number is on no screen. **PGR's bundle declares no reward paying a ticket, so accrual is zero and a test asserts it**: the assertion is a marker for a missing reading, and the reading to take is what the dailies pay in Black Cards. Still no route, no screen, no bean, and `PityState` stored nowhere — N28 closed the model, not the wiring |
| Frontend | **Six screens, browser-driven, 31 tests** | Inventory editor, goal picker, plan view carrying **every one of the solver's notes**, catalog browse and search, the character page with the **personalized overlay**, and — since 2026-09-21 — a **roster screen**, so a construct with no goal can be recorded at all. The plan form **asks how far the reader gets and sends `reach`**, off `GET /api/games/{game}/measures`, remembered per profile in the store (persist v2) and **not stored server-side** — ADR 0022 stands, and its reversal trigger is now "a second device asks again". Same-origin locally via the Vite proxy. **There is no prettier or eslint config in the repo** and the files are hand-formatted — do not run a formatter here until somebody commits one |
| The image | **Built end to end 2026-09-24, and run as Render will run it** | ~2 min, 143 MB. Against an **empty** Postgres with **no Redis**: 15 migrations in 0.58 s, started in 8.6 s, `/api/health` 200, `/dev/sign-in` 401. The CLI from the same image `preview` → `ingest` → `publish`ed PGR sequence 7 into it and `/api/games` served it — **a fresh deployed database is empty until somebody does that**. The download that stalled at 10% was the context, not the network: no `.dockerignore`, so ~500 MB (`node_modules`, every `build/`, the snapshots) went up on every build; now a whitelist, 965 kB. **Compose itself was not re-run** |
| Development sign-in | **Done, and absent from the artifact** | [ADR 0017](docs/adr/0017-the-development-sign-in-is-absent-from-the-artifact.md). `:modules:identity-dev` is `testAndDevelopmentOnly`, so **no property or profile can reach it**; `DeployableJarTest` opens the jar and proves the absence on every build. **Do not add a switch that turns it on** |

### Done and stable

Game data pipeline (Phase 1) · game data API · demand resolution · `YieldTable` · `MipOptimizer` · the two
objectives · solve caching · `SolveCoordinator` · the community benchmark · statistics primitives · the
`identity`/`player` schema · sign-in · player API · offline sync · the plan route · bean wiring · the three
architecture tests · Track B (empty). **One row each, with the decision behind it,
[in the archive](docs/history/tracker-archive.md#the-tracker-as-it-stood-before-the-2026-09-18-compression) under
*Current state* — read the row before re-opening any of them.**

**The four most often misremembered:** solve caching has **no invalidation method**, because a patch is a different
key rather than a stale entry; `SolveCoordinator` **does not survive a restart, deliberately**; **no FK crosses a
schema** and an identity is `(provider, subject)`, never an email; and **goals have no PATCH**, because an ordered
list has no per-key merge.

### What is still unverified

Be precise about this, because the temptation is to read "build green" as "it
works". It does not mean that:

- **CI does not run the tests that matter most.** The three snapshot-gated classes are the only evidence the
  optimizer is fast enough or right about anything real. The stale stage table that cost five sessions of plans was
  caught by no test at all — a person went looking.
- **Two layers are barely exercised.** Most authenticated tests go through MockMvc; `DevSignInTest` is the
  exception — a real port, a hand-kept cookie jar — so the container is exercised for sign-in and **not for the plan
  or the merge**. And jsdom computes no layout, which let a `display: block` fold a table header past the frontend's
  tests: **appearance is a person's job, and driving a browser before shipping a screen is still required.**
- **Four things have never run once, and every one waits on B5.** The **OAuth exchange** — no client id, no secret,
  no redirect followed, and **the development sign-in does not narrow it by one inch**, so reading N24 as "sign-in
  works" is the misreading ADR 0017 exists to prevent. *(A fifth, the image, ran 2026-09-24.)* **The API under
  load** — every request loads a whole version, fifteen queries, a deferral written into `GameDataReadModel`'s
  javadoc. **An offline *write* path**, which needs the **built** bundle (`web-built`, port 4173) because the dev
  server has no worker worth the name. And **a page and an API deployed at different instants** — that happened
  once and the page rendered as nothing, fixed by wire types marking the field optional so the compiler points at
  every call site. **B5's problem in miniature.**
- **No two real devices have ever synced.** One browser's stale edit has lost to a newer value and said so; two
  requests interleaving on the same key at the same instant is argued rather than measured. And **a merge publishes
  nothing**, so a cached plan survives the inventory under it moving — harmless until Phase 6.
- **Nothing has ever asked the gacha engines a question on behalf of a player.** No route, no screen, no bean, and
  **`PityState` is stored nowhere**: the gap is a schema and a screen rather than an engine. **Three banners are
  first-hand and five are not (Q4)**, and **multi-copy answers are
  too pessimistic** — 200 Cassettes of the Lost buy a copy and nothing models it, so at two copies the engines say
  280 pulls and the truth is 200 (**N38**). *This bullet also said "no bundle declares a banner at all" until
  2026-09-22; the PGR bundle has declared one since sequence 0 on 2026-09-19, and what N28 was actually missing was
  the banner's price.* **Since sequence 7 an account's budget can be computed** (ADR 0029) and nothing calls that
  either.
- **One character is not a catalog.** The overcharge that made its plans wrong is closed at both ends —
  [ADR 0026](docs/adr/0026-a-crossed-gate-is-a-reached-state.md) inferred what sits *behind* a recorded state,
  [ADR 0027](docs/adr/0027-a-roster-entry-holds-the-states-an-entity-has-reached.md) (`V13`) let a reader say
  what sits *beside* it — and since 2026-09-21 there is a roster screen and a plan form that sends `reach`.
  **What is still wrong here:** **no shadow price has ever rendered against a real plan** — the
  reason is known rather than suspected (on the Evolve to SS plan the one demanded item is at its
  bound, so the list is legitimately empty and the notes say so), and what it wants is a goal set
  with a farmable stage in it, which one PGR bundle does not yet have. The naming half is closed:
  a `progress:` line rendered as its bare slug beside a properly named `Cogs` until 2026-09-22
  (N37, ADR 0028). **A `choice:` line still renders as `one of: <upgrade id>, <upgrade id>`**, and
  that one is *not* the same fix — an upgrade's name would be the game's word, so it is a fact with
  provenance. Every R1999 catalog and drop number in this file comes from Kornblume.
- **Eight qualifications of the closed phases are
  [in the archive](docs/history/tracker-archive.md#qualifications-moved-out-of-the-live-tracker-2026-09-11-seventeenth-session)**
  — the benchmark being one guide, the two community disagreements, the three
  shapes the model cannot express. **Read before re-opening Phase 1, 2 or 11.**

---

## Next actions

**N37 and N28's modelling half closed 2026-09-22** (ADR 0028, ADR 0029) — with
N30, N20, N33, B6, N34, N36 and N35's two load-bearing halves on 2026-09-21,
[in the archive](docs/history/tracker-archive.md#completed-next-actions).
**B5 is the only Phase 4 item left**, and it needs the maintainer's own accounts.

**Every defect turned into an action on 2026-09-21 is now closed**, and the
pattern from that batch held into this one: **an entry is wrong until somebody
runs it.** N28 said "no bundle declares a banner at all" and one had since
sequence 0; N37 was scoped as one sequence and turned out to be two decisions
that share a file and nothing else. **Three stale claims were corrected in this
file and one in the research note as a side effect of doing the work** — which is
the argument for doing it rather than re-reading it.

### Before B5 — debt worth paying first

**Nothing.** N37 was the last of it and closed 2026-09-22.

**One thing worth knowing for the next sequence, learned doing this one:** the
CLI needs a **live Postgres carrying every previous sequence** for `preview` to
mean anything — a fresh database reports *"this bundle would be the first"*,
which is not the check. `docker compose up -d postgres` restores it from the
`storm_almanac_postgres-data` volume, which still holds every version published
since 2026-09-06. **Even `validate`, which touches no version, boots the whole
Spring context and fails without a database.**

### Held — Phase 4 scope, and the maintainer decides

**Phase 4 does not close until each is done or explicitly cut, on the record** —
a cut is a decision and goes in the session log; a silence is not a cut. **B5 is
the last one, started 2026-09-24**; the rest are
[in the archive](docs/history/tracker-archive.md#completed-next-actions), planned
in full on 2026-09-20 — read the plan rather than re-deriving one.

- [ ] **B5 — Wire the real deploy: Vercel, Render and Neon.** **Started 2026-09-24**;
      [the six steps](docs/history/tracker-archive.md#session-log) (thirtieth entry) stand.
      **Settled by the maintainer:** *one origin*, a Vercel rewrite to Render; *the sleeping
      tier*, their own keep-alive bot, **which must ping `/api/health`** — it touches no
      database; *Postgres on Neon*, because Render's free one expires (direct host, not
      `-pooler`, Singapore, `?sslmode=require`). **Step 1 is done** (the image, above), with
      `/actuator/health` no longer waiting on an unused Redis and `X-Forwarded-*` honoured so
      `{baseUrl}` is the public origin (`ForwardedOriginTest`; *whether the hosts deliver those
      headers is unmeasured*). **What the plan missed:** the rewrite needs `/api/*`,
      **`/oauth2/*` and `/login/oauth2/*`**, then an `index.html` fallback for `BrowserRouter`;
      `DATABASE_URL` is `jdbc:postgresql://…` with user and password apart. **Step 2 is done:**
      `https://storm-almanac.onrender.com` answers, `/actuator/health` UP through Neon, and
      **PGR sequence 7 published into Neon 2026-09-24T02:56:41Z** at the maintainer's request,
      after a preview identical to the local rehearsal. **The PWA would have broken sign-in:**
      its worker answered *every* navigation with `index.html`, so a returning reader's
      `/oauth2/…` never reached Spring — now denylisted, proven in a browser on the built
      bundle. `frontend/vercel.json` carries the three rewrites and the fallback. **Unproven:**
      Hikari's pool may keep Neon awake — `MINIMUM_IDLE=0`, `IDLE_TIMEOUT=60000`,
      `KEEPALIVE_TIME=0` are Render env only, to move into `application.yml` once Neon is seen
      suspended. **Left:** the Vercel project, the Google client, `deploy` gated to `main`, the
      first exchange. **Ends with `:modules:identity-dev` deleted** (ADR 0017's trigger).

### Held — later phases, not Phase 4's business

- [ ] **N38 — A shop exchange for the featured unit, and both engines spending
      it.** **Cut out of N28 on 2026-09-22 deliberately, and the cut is on the
      record.** R1999 sells a copy for **200 Cassettes of the Lost**, one per
      pull, so six copies cost **560 pulls and not 840** — and at two copies the
      engines say 280 where the truth is 200. **This is not a field, it is an
      engine change**: the exchange goes on `BannerModel`, and then
      `probabilityOfFeatured(…, copies)` has to spend it in *both* engines, which
      grows the chain's state space. **And it has nothing to exercise it** —
      the reading is R1999's, R1999 has no first-hand bundle, and a column no
      bundle can fill is `Availability.opensAt` again. **Do it with Phase 11's
      R1999 sourcing**, or earlier if a PGR pool turns out to sell copies.
- [ ] **N39 — Read what the dailies and weeklies pay in Black Cards.** The one
      reading that would make `DeclaredIncomeModel` say something. PGR's bundle
      declares no reward granting a research ticket, so accrual is **zero and a
      test asserts it** — an honest answer and a visibly incomplete one. Two
      shapes to watch for: the chain is **Rainbow Card → 10 Black Cards → 1
      ticket**, and ADR 0029 says a conversion chain may want `Craft` rather than
      a grant; and paid income is not income this project should model at all.
- [ ] **N18 — Put drop estimates into `SolveKey` in the same change that first
      publishes one.** The moment Phase 6 does, a plan cached against yesterday's
      rates is served as today's — the one staleness bug the key's design cannot
      catch, and it is silent. **In the Phase 6 change itself, not after it.**
- [ ] **N19 — Write `RedisSolveCache` when there is a second node** (ADR 0012's
      trigger), **and before any benchmark against the Phase 8 replicated KV**
      whatever the node count: a replicated cache measured against an in-process
      map is measuring the network, which ADR 0003 forbids.

---

## Phase board

Exit criteria are copied from the plan verbatim. Do not start a phase until the
previous one's criterion is met. The "Landed" record for closed phases is
[in the archive](docs/history/tracker-archive.md#closed-phases-in-full).

### Track A — product

- [ ] **Phase 0 · Ground** — **closed by exception 2026-09-05, box deliberately
      unticked** ([D1](#d1--deployment-deferred-2026-09-02)). Everything landed
      except the deploy; the box is ticked when a real URL answers 200.
- [x] **Phase 1 · Game data foundation** — closed 2026-09-06, CI-confirmed (N10).
- [x] **Phase 2 · Optimizer core** — closed 2026-09-08. **Stopped by its budget,
      not finished by it** (ADR 0010).
- [x] **Phase 3 · Identity and player state** — closed 2026-09-08; OAuth never
      exchanged, no two real devices synced.
- [ ] **Phase 4 · Frontend v1 — and launch** — 2.5 weeks — **OPEN 2026-09-09.**
      Landed: the app is served, hosting decided, a browser has signed in (N24),
      **N25 is closed** (five screens driven in a browser, provenance read back
      onto the page, the PWA loaded with its server killed, a frontend suite in
      CI), and since 2026-09-21 there are **six**, every one of which has now
      rendered PGR. **Only the backend is deployed.** Launch publicly even if ugly.
      **Exit:** five strangers complete a plan without asking for help, and a
      logged-in character page shows what that reader is short of. *The second
      clause is served; the first needs **B5**.*
      **Closing condition, set 2026-09-20:** the exit is necessary and not
      sufficient — N30, N33, N20 and B5 are each done or explicitly cut before
      this box is ticked, with the cut recorded in the session log. Nothing here
      closes by having been forgotten. **The first three are done; B5 is the only
      one left.**
- [x] **Phase 5 · Gacha engine** — closed 2026-09-12 out of order
      ([D2](#d2--phase-5-entered-before-phase-4-closed-2026-09-12)); **one banner first-hand (Q4)**;
      income model landed 2026-09-22 (ADR 0029), shop exchange is **N38**.
- [ ] **Phase 6 · Drop statistics on Postgres** — 1.5 weeks. Report submission,
      Wilson intervals, provenance, abuse controls, estimates feeding the
      optimizer — on the boring implementation first, because this is the interface
      Track B later swaps. **Read before tightening any rule over data already
      published:** a version is immutable and the rules for reading one are not, so
      a published snapshot can stop being loadable without anything touching it —
      [it has happened here](docs/history/tracker-archive.md#a-published-version-that-stopped-being-readable).
      **Exit:** a community-derived estimate supersedes a seeded one in a live plan.

### The gate

> **Track B starts only when the product is publicly deployed with real users and
> real traffic.** If Phase 4 has not landed, go back and land it. Infrastructure
> built against imagined requirements is a toy; infrastructure built against six
> weeks of your own production traffic is engineering.

**Gate status: CLOSED, and structurally so** — no real users and no real traffic, and none
is possible before Phase 4 launches. Do not open `almanac-store`. When Phase 7 comes
round, re-read D1 and decide deliberately whether Track B on synthetic workloads
is still worth building.

### Track B — substrate, and Track A's close

Scope is in [plan.html](plan.html). **None of 7–10 may start before the gate opens.**

| | Phase | Shape | **Exit** |
|---|---|---|---|
| [ ] | **7 · almanac-store** (LSM), 3w | Behind `DropReportStore`, alongside the Postgres one | Crash-consistency fuzzing survives 10k randomized kills; benchmark vs Postgres published — **including if Postgres wins** |
| [ ] | **8 · almanac-raft**, 3w | Exposed first as a replicated KV, so it is testable before anything depends on it | 5-node cluster survives repeated leader kills and partitions with no divergent log |
| [ ] | **9 · Solver cluster**, 2w | Replicated job log, leased work, idempotent completion, results over WebSocket | Kill any node mid-solve — no lost solves, no duplicated solves, throughput recorded |
| [ ] | **10 · Chaos harness**, 1.5w | Partitions, pauses, kills, disk corruption; linearizability checking; failing seeds kept as regression tests | Nightly suite green for 7 consecutive nights, **and one real bug found and written up** |
| [ ] | **11 · Reverse: 1999 as the second title**, 2w — *was PGR until [D3](#d3--launch-title-swapped-to-punishing-gray-raven-2026-09-13)* | R1999 keeps the adapter as a cross-check, two imported patches, the benchmark and one first-hand banner; what it still costs is **first-hand sourcing of the catalog** and its own day boundary | R1999 live with **zero game-specific code** added to `planner`, `gacha` or `stats` after PGR launched — and the diff to prove it |
| [ ] | **12 · Hardening and the writeups**, 1w | Tracing, alerting, a backup actually restored from, a load test with published numbers, pre-rendered catalog pages | Restore drill completed from a real backup; catalog pages indexed; three writeups published — the storage benchmark, the consensus verification, the multi-game diff |

---

## Cut list, and the seams

Consult this before descoping anything, and record it in the session log if a cut
is taken. **Never cut** the Phase 4 public launch, the Phase 2 optimizer, or
completing at least one of Phase 7 or 8 properly — half an LSM tree and half a
Raft are worth nothing, one finished engine a great deal.

1. **Cut first:** Phase 10 as a separate phase — fold minimal fault injection into
   8 and 9. This loses the strongest evidence, so cut only under real pressure.
2. **Cut second:** Phase 9. Keep `almanac-raft` as a verified standalone
   replicated KV and leave the solver single-node.
3. **Cut third:** Phase 11's real second game — but still prove the abstraction
   against a synthetic test game. That proof is the product thesis.

**The invariants live in [CLAUDE.md](CLAUDE.md#non-negotiables)**; two of the six
are enforced by `GameAgnosticismTest` and `ModuleBoundaryTest`, the ones a change
is most likely to trip.

| Port | Defined in | Boring impl | Hand-built impl |
|------|-----------|-------------|-----------------|
| `DropReportStore` | `modules/stats` | Postgres — phase 6 | `almanac-store` — phase 7 |
| `SolveCoordinator` | `modules/planner` | single-node — phase 2 | `almanac-raft` — phase 9 |
| solve cache | `modules/planner` | Redis — phase 2 | replicated KV — phase 8 |

**Keep these interfaces narrow: a port shaped to flatter the hand-built side proves nothing.**

---

## Deviations from the plan

Decision, cost, and what would reverse it. **The full accounts are
[in the archive](docs/history/tracker-archive.md#the-tracker-as-it-stood-before-the-2026-09-18-compression).**

### D3 · Launch title swapped to Punishing: Gray Raven (2026-09-13)

**The plan launched on R1999 and brought PGR in at Phase 11; the maintainer
swapped them**, after reading Kornblume's source to answer "is this a clone?"
showed **`prior-art.md`'s central claim was wrong** — it *does* solve per player,
in the browser, since 2024-03, with OCR inventory import. The wedge left is
narrower: whole runs, explanations, sample-size-aware yields, provenance, two
games on one model. **Cost:** PGR starts with no data, no benchmark and
second-hand gacha fixtures, and fodder and the first real calendar move from
Phase 11 to before launch (**N30**, **N20**) — the launch gets further away, not
closer. **Bought:** a launch that is not a second copy of a tool players already
use, with the abstraction's hardest shapes load-bearing from day one.

**Reversal trigger, and half of it can no longer fire** (2026-09-18): the *timed*
PGR reading shows sourcing is not feasible for one maintainer — **that pass was
cut**, so this half has no measurement behind it — or an established PGR planner
that solves per player turns up.

### D2 · Phase 5 entered before Phase 4 closed (2026-09-12)

**The rule broken is this file's own.** Phase 4 needs a deployment (**B5**) and
**N27** is the maintainer's, so the choice was put to them and Phase 5 taken
knowingly: it bought the one large piece of Track A a session can finish alone and
cost nothing on the launch. [In full](docs/history/tracker-archive.md#d2--the-full-account).
**Reversal trigger:** none, a phase cannot be un-entered. The one that matters is
on the next: **do not take Phase 6 early on this precedent** — it needs users,
where Phase 5's criterion was a proof about a model.

### D1 · Deployment deferred (2026-09-02)

**REVERSED 2026-09-09**, heading kept verbatim so every link still lands. Both halves of its trigger fired at once — Phase 4 reached, a free tier accepted — so **Vercel for the frontend, Render for the backend**, still no money ([in full](docs/history/tracker-archive.md#d1--the-deferral-in-full)). **What it cost is what B5 buys back:** Phase 0's box is unticked, the gate has no meaning without real traffic, and deploy problems really were found late.

---

## Environment notes (this machine only)

Not deviations — local facts that cost time to rediscover.

**E1 · Avast intercepts TLS, so Gradle cannot fetch new dependencies.** Live again
since 2026-09-08: Avast re-signs every TLS connection with its own root CA,
Windows trusts it and **the JDK's `cacerts` does not**, so any download of an
artifact not already cached fails with `PKIX path building failed` — invisible
until a **new** dependency is added. The flag below points the JVM at the Windows
certificate store and grants **no new trust**; not committed to
`gradle.properties`, because `Windows-ROOT` does not exist on the Linux runner.

```bash
cd backend && ./gradlew -Djavax.net.ssl.trustStoreType=Windows-ROOT build
```

**E2 · Docker Engine 29 refuses Testcontainers' API version.** Fixed and
committed — `systemProperty("api.version", "1.44")` on every `Test` task, on the
*task* and not as an environment variable, which is the whole fix.
[The account](docs/history/tracker-archive.md#e2--the-full-account).

**E4 · A dead Docker engine looks exactly like a slow one.** `docker info` hangs
rather than failing while Docker Desktop starts. **Do not use `com.docker.service`
as the test** — on the WSL2 backend the Windows service is not the engine, and a
session has read `Stopped` while the engine answered all day. **Ask the engine**;
if it does not answer, **start the application**, which a session *can* do
unelevated and which brought the engine up in under a minute on 2026-09-18. Only
if that fails is it a human's problem.

```bash
docker info --format '{{.ServerVersion}}'
```

```powershell
Start-Process 'C:\Program Files\Docker\Docker\Docker Desktop.exe'
```

---

## Open questions

Carry forward until answered, then move the entry [to the archive](docs/history/tracker-archive.md#answered-questions).

- **Q5 — Is our "3.5" the same 3.5 anyone else means?** *Open for the existing
  data; **dissolved for everything after ADR 0015**.* `fetch-upstream.sh` pins a
  commit dated **2026-03-17** while Global 3.5 ran **2026-05-28 to 2026-07-02**,
  most likely because Kornblume tracks **CN**. It does not invalidate the nine
  agreements. **Self-sourcing ends the ambiguity by construction.** Until then, do
  not write "3.5" publicly without saying which.
- **Q4 — Rate verification.** *Answered for one R1999 banner and, since
  2026-09-18, for both PGR archetypes; open for the rest.* R1999's rules screen
  **confirms its pity curve twice** — word for word, and an overall 6★ rate of
  **2.36% that the curve reproduces as 2.3592%**, a check a curve one pull off
  would fail. It **contradicts the featured rule** (50/50 with a guarantee, where
  `reverseDebut` hands her over outright) and finds a **Limited Shop exchange no
  engine models**. **PGR's advertised 1.90% does *not* reproduce** from its own
  numbers — 1.925% and 2.021% computed — and the note argues the disclosure is a
  long-run outcome share rather than a per-pull vector, with **the per-10 A-Rank
  floor the untested candidate**. **The 2.021% is now the model's own answer**
  (2.0207%, from a chain that knows nothing about the note, 2026-09-20), so the
  disagreement is no longer a spreadsheet's and is pinned by a test — which
  sharpens the question rather than answering it. Still second-hand: R1999's
  beginner banner, and PGR's weapon, CUB and basic pools.

---

## Session log index

Full entries are in [the archive](docs/history/tracker-archive.md#session-log),
newest first. **Write the entry there; add one short line here.**

| Date | Session | What it was |
|---|---|---|
| 2026-09-24 | thirty-eighth | B5 started, and **the backend went live** on Render and Neon with PGR sequence 7 published. The maintainer settled both decisions — one origin, and their own keep-alive bot for the sleeping free tier. **The image built and ran end to end for the first time**: 15 migrations on an empty Postgres, and PGR sequence 7 published into it from the same image's CLI. The "slow download" was a 500 MB build context with no `.dockerignore`. Running it as Render will found two bugs, each pinned by a test that failed first: `/actuator/health` 503 on an unused Redis, and an OAuth redirect URI of `http://<render-host>` behind the proxy. Then a third, in the page: the PWA worker answered sign-in navigations from its cache. 438 backend tests, 31 frontend |
| 2026-09-22 | thirty-seventh | N37 and N28 closed as one sequence 7, published and read back as *no changes*. **The EXP pool names are the bundle's own word, not the game's** — PGR calls all three pools "EXP" (ADR 0028) — and the pull price is 250 tickets, writable at last because the maintainer opened the ticket's tile and it grades 5★, so the format change the research note argued for was never needed (ADR 0029). `IncomeModel` written, and nothing calls it. **Three stale claims fell out of doing the work**, including "no bundle declares a banner at all", which had been false since sequence 0. Whole-graph equality caught an ordering bug the field-by-field assertions missed. 435 tests, 16 gated ones run; p95 1 812 ms and the plan 3 877 Activity, both recorded rather than smoothed |
| 2026-09-21 | thirty-sixth | N35's two load-bearing halves closed, leaving N37. The plan form asks **how far do you get** and sends `reach`, off a new `/measures` route that collects the ladders ADR 0022 said were declared nowhere; answering nothing is refused *by name*, answering 1 100 000 plans **Evolve to SS in 63 days and 0 Serum** — the first plan a web client has ever asked for that counts a scored grant. The ladder summary computes **56 Scars a week**, agreeing with the figure ADR 0022 states in prose. A roster screen, sharing one state-editing component with Goals. 420 backend tests, frontend 16 → 28. ADR 0022 stands: the maintainer kept `reach` on the request, so its trigger is now a *second device*. **Do not run prettier here** — no config, and it reformatted 456 lines of a 269-line change |
| 2026-09-21 | thirty-fifth | N36 closed, and it was not the paperwork it was scoped as. Every load-bearing figure held — nine agreements, 3 880 against 4 017, 419 tests and all 16 gated ones run — but **following the documented workflow proved nothing**: the property never reached the test worker and the snapshot directory was not a task input, so a fetch left `:app:test` `UP-TO-DATE`. The second is the identical bug to `data/bundles`, ten lines above its own fix. Both fixed, both proven by measurement; numbers now dated in `docs/benchmarks/snapshot-gated-runs.md` |
| 2026-09-21 | thirty-fourth (cont.) | N34 closed: a roster entry holds a set of states (ADR 0027, `V13`). A reader at `promote-6` who says they are also at `level-80` pays 127 500 Cogs and **no EXP** where they were charged 90 000 over six steps. The merge unit stays the entity. **The frontend shipped wrong twice with 16 green tests each time** — a `select multiple` nobody could use, then chips styled as buttons the maintainer looked straight at and did not see. Two live claims died: a `progress:` line *has* rendered (as a bare slug), and PGR *has* been rendered, so N35 was rescoped rather than ticked |
| 2026-09-21 | thirty-fourth | B6 closed the day after it was written: CI triggers on a push to `dev`, proven by a run on `cfa6fe4` with no PR open. The session-start check found the trap live — the previous session's own commit had sat on `dev` unbuilt. The concurrency group stays keyed by ref *on purpose*: deduping push and PR would let a push cancel the check the PR needs green. Deploy must be gated to `main` when B5 turns it on |
| 2026-09-02 – 09-21 | first to thirty-third | **Collapsed into one row on 2026-09-24, when this file passed 550 again**; the rows are [verbatim in the archive](docs/history/tracker-archive.md#session-index-rows-collapsed-on-2026-09-24). From the scaffold to Phase 5 closing out of order (ADRs 0001–0018), then the launch title swapped to PGR (D3), the first first-hand bundle, and **every shape it refused made writable**: gates and fodder (0019), lifetime caps (0020), choices (0021), `reach` (0022), drawn walls (0023), deadlines (0024), the day boundary (0025), crossed gates (0026) — sequences 0 to 6 published, each read back as *no changes* |
