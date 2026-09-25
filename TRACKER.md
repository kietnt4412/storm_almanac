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
- Last updated: **2026-09-25** (forty-second session)

---

## Status

- **The launch title is Punishing: Gray Raven** since 2026-09-13
  ([D3](#d3--launch-title-swapped-to-punishing-gray-raven-2026-09-13)); R1999
  moves to Phase 11, because Kornblume solves per player and on R1999 this
  product overlapped a deployed tool almost feature for feature. **Everything
  below that says "R1999" was true when written and still is — it is just no
  longer the launch.**
- **PGR is read first-hand** ([the note](docs/game-facts/punishing-gray-raven-research-disclosure.md)); [its bundle](data/bundles/punishing-gray-raven-steering-by-light.json)
  is **sequence 11** (Global 4.8.0 "Anchored in Faith") — three constructs on one S-rank ladder, one weekly ladder, a 05:00 UTC
  reset, a pull price, and **since sequence 11 the game's own words for ranks and skill sections** (ADR 0032, S4). Every sequence
  has read back as *no changes*. **Reading the client overruled the guides four times** ([the four](docs/history/tracker-archive.md#what-status-carried-until-the-2026-09-21-rewrite));
  this bullet's longer form is [in the archive](docs/history/tracker-archive.md#the-pgr-status-bullet-until-2026-09-25).
- **Thirteen decisions are live, and each is its own account — read the ADR, never a summary.**
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
  [0029](docs/adr/0029-income-is-what-the-bundle-declares-not-a-rate-per-day.md) income · [0030](docs/adr/0030-the-development-sign-in-stays-because-the-hands-that-need-it-cannot-sign-in.md) dev sign-in stays ·
  [0031](docs/adr/0031-a-shared-upgrade-path-is-written-once-and-expanded-in-the-file.md) shared ladders. **0022's trigger is
  live:** `reach` becomes player state the moment a reader answers twice, which since 2026-09-21 means *a second
  device*. **Three loose ends sit outside all thirteen:** the **Themed Construct archetype is expressible and not
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
- **Phases 0–3 and 5 are closed; Phase 4 is open; Track B is gated.** Phase 0's box, unticked since 2026-09-05, was ticked 2026-09-24 when the pipeline deployed; Phase 5 closed **out of order on purpose** ([D2](#d2--phase-5-entered-before-phase-4-closed-2026-09-12)) and **nothing calls either gacha engine**. [The board](#track-a--product).
- **Deployed and signed into since 2026-09-24** ([D1 reversed](#d1--deployment-deferred-2026-09-02)) —
  `https://storm-almanac.vercel.app` rewrites to Render, which serves PGR sequence 8 from Neon; the first real OAuth
  exchange and the first production write ran the same day. Free tier, still no money. **CI deploys it:** a push to
  `main` that passes calls Render's deploy hook and waits for `/api/health` to report its SHA (~4½ min).
  **`backend/Dockerfile`'s COPY list is B5's path and drifts in silence** — it omitted `adapters/` from Phase 1
  until 2026-09-09, every image build failing in six seconds while this file called it verified, so **anything added
  beside `modules`, `adapters`, `substrate`, `app` needs a line there**.
- **The remote, last checked 2026-09-25 (forty-second) — re-check it, do not trust it, and check three things:**
  which PRs merged, **the last `main` run**, and **the SHA `/api/health` reports**.
  [PR #46](https://github.com/kietnt4412/storm_almanac/pull/46)'s `main` run went red on a flaky test, so
  **`deploy` never ran** and nothing showed it. [PR #47](https://github.com/kietnt4412/storm_almanac/pull/47)
  fixed it; #48 (D5, S1), #49 (S2, S3) and #50 (S4, V16) followed, and **production reports `61929c1`**, #50's merge.
  **Neon holds sequence 12** since 11:41Z on 2026-09-25, read back as *no changes*. Stale on eight consecutive checks, so re-check it rather than read it. B6 means a commit on `dev` with no PR open is still built.
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
- **Keep it under 550 lines, measured with `wc -l`** rather than carried forward. Trimming loses to a
  file that absorbs every session ([the ledger](docs/history/tracker-archive.md#the-line-count-ledger)):
  **past 550, rewrite a section or carry less — do not shave**, and suspect the row nobody can read in one breath.

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
| Backend build | **Green** | **475 tests** in full 2026-09-25 (forty-second), 0 skipped locally with the 2026-09-22 snapshots present; the last *measured* gated run is still [the dated one](docs/benchmarks/snapshot-gated-runs.md) of 2026-09-22 — nothing in this session touched the model; **459 expected on CI**, where those 16 skip. `:app:test` depends on `:app:bootJar`, and declares **two** directories outside every source set as inputs — `data/bundles`, without which `AuthoredBundlesTest` came back `FROM-CACHE` after a bundle changed, and `build/upstream-snapshots`, without which fetching upstream left the task `UP-TO-DATE` (N36). **The second was the identical bug ten lines below the first's fix.** `api.version=1.44` — [E2](#environment-notes-this-machine-only) |
| Authored game data | **One bundle, sequence 12 published in both databases; first-hand but for N42** | **Sequence 11 (2026-09-25) changed no number**: 328 changes, every one a word — the rank names, a section on every S-rank track, a tag on every skill, and the section order (ADR 0032). **Sequence 9 (2026-09-24) put Selena: Pianissimo and Lucia: Inverse Crown on the ladder** — a climber, a shard and two shard shop rows each, +129 facts; **sequence 10 changed no fact** and names the patch: readings to 2026-09-22 are 4.7.0 "Steering By Light", those of 2026-09-24 are **4.8.0 "Anchored in Faith"**, which sequences 8 and 9 got wrong. **Lucia's numbers are all the maintainer's every-S-rank report, none read on her screens.** **Since 2026-09-24 Lacrimosa's 57 rows are written as a ladder** (`s-rank-construct`, ADR 0031) — 30 entries, the skill curve once — which the parser expands back into the same rows; proven to diff against sequence 7 as *no changes*, same 117 facts, same provenance per fact. **Sequence 8, published 2026-09-24 locally and to Neon**, corrects her skills: eight levelled, not seven (N41). **Sequence 7 added one item and one provenance entry** — the Event Construct R&D Ticket, **5★ off a tile on 2026-09-22**, which is the fourth currency graded that way where its own item card grades nothing. It carries the banner's pull price (250, credited to the 2026-09-18 pool-screen sitting) and the three EXP pool names, which are **not facts and declare no provenance** (ADR 0028) and so show in the diff as three `progress '<kind>'` subjects and in no provenance count. **117 facts over eight provenance entries.** Helentine: Lacrimosa (level to 80 as a **thirteen-link chain, every gated level priced**, 13-step Promote **all gated**, 7 skills to 18, Evolve to SS), Hear the Bell, Samantha (Overclock, Upper Resonance at three prices), one stage, 11 shop rows, 2 box crafts, 5 fodder rules, the weekly Phantom Pain Cage's nine tiers, and the game's **05:00 UTC** day boundary — which is a game-level field and so adds no fact, exactly why `Facts` had to learn to flatten `Game` (ADR 0025). **What each sequence added and when it published is [in the archive](docs/history/tracker-archive.md#session-log)**; every one read back as *no changes*. Three Cage tiers are **written short** — a gold 5★ card, a 4★ chip and a portrait item were never opened, so those grants are absent, which makes plans dearer and never cheaper. `AuthoredBundlesTest` parses every file in `data/bundles` and fails on any fact the project may not publish, on a provenance mapping naming no fact, and on an **empty** directory. `AuthoredBundlePlanTest` plans from it, so a correction moves a plan: a skill to its cap is **150 Serum**, a Memory's Overclock **420**, her last rank **1 470**, Samantha's Resonance **90**, and Evolve to SS **30 shards from a stock that never resets** (ADR 0020) — or, for a reader who says they clear the whole Cage, **63 days and none at all** (ADR 0022) |
| CI workflow | **Green, Node 24; triggers on `dev`** | Since B6 (2026-09-21) it runs on push to `main` **and `dev`** as well as `pull_request`, so a push to the working branch with no PR open is built rather than silently ignored. **One annotation, and it is GitHub's rather than ours:** `ubuntu-latest` migrates to Ubuntu 26 from 2026-10-19 — nothing to fix, worth knowing before a green run starts carrying a warning nobody placed. **A push to `dev` while a PR is open from `dev` runs twice, on purpose** — the concurrency group stays keyed by ref, because collapsing push and PR into one group lets `cancel-in-progress` cancel the run the PR needs green. **Since 2026-09-24 `deploy` ships production**, gated to a push to `main` — ungated it would ship every commit on `dev` — via the `RENDER_DEPLOY_HOOK` secret; it fails loudly if the secret is gone, and waits for `/api/health` to report its SHA. First run `35954626572`, green. Last *executed* suite: run `34695206362`, 16 skipped, exactly the three snapshot-gated classes. **`gradle/actions` held at v5** — v6 needs Gradle's Terms of Use accepted, which is the maintainer's call. **Counting PASSED lines in a log undercounts**; read task outcomes |
| Provenance | **Written, enforced, and read** | [ADR 0016](docs/adr/0016-provenance-is-a-property-of-the-data.md). `V7` stores one row per declared fact; `publish` refuses a version that is not first-hand and **names the facts**. **`ProvenanceRepository` is a second port** — the solver cannot see where a number came from, so it cannot be made to prefer one. Silence is `UNRECORDED`: parses, cannot publish |
| Parser adapters | **One, demoted to a cross-check** | `:adapters:reverse-1999`, 25 tests. **Hard-codes `THIRD_PARTY`, so it fails a plain `publish`** — there is no call site to launder data through. Kept because diffing the first self-sourced bundle against it is worth more than it ever was as a source |
| The MIP (`EnergyMip`) | **Stages, crafts, shops, rewards and fodder** | ojAlgo, integer runs, inventory subtracted, every variable bounded — the bound is what makes a real patch solvable. A purchase is a conversion capped at limit × *whole* periods, or the whole allowance of one that never resets, which the plan says it assumed unspent (ADR 0020); feeding fodder is a conversion into a `progress:<kind>` item, and paying one of a step's several prices a conversion into a `choice:` item (ADR 0021). Gates are not in the model: `DemandResolver` turns them into demand (ADR 0019). **A grant behind a score the reader has not cleared is dropped before the model** and reported in the notes, so the counts in a plan and in a refusal are the game *that reader* plays (ADR 0022) |
| The time axis | **A scalar, not an index** | [ADR 0013](docs/adr/0013-the-horizon-is-a-scalar-not-an-index.md). Rotation is capacity shared over *subsets* of weekday restrictions; **no variable is indexed by day**, which is why p95 has held across three measurements — **1 807 ms, then 1 808 on 2026-09-21, then 1 812 on 2026-09-22** (max 1 814), a 7 ms band on a 2 000 ms budget with 188 ms of headroom. All three drifts are upward, which is worth watching and is not yet drift; the dated runs are in [the benchmark log](docs/benchmarks/snapshot-gated-runs.md). An expiring grant is **supply plus a reported deadline**, never a scheduled claim (ADR 0024). **Which weekday day zero is comes off the game** — `Game.dayBoundary`, a zone and an hour, and `null` means the midnight-UTC every version published before `V12` was planned by (ADR 0025) |
| `gacha` — engines | **Phase 5's criterion, and nothing calls them** | [ADR 0018](docs/adr/0018-the-gacha-engines-answer-one-question-about-one-rarity.md). An exact chain and 500 000 seeded trials sharing one validated `PullModel`, so both refuse the same banners for the same reasons. **53 tests, worst gap 0.110 points over 108 questions, at 2.22 standard errors.** Only the headline rarity is modelled. **Through a drawn guarantee they take different roads on purpose** — the chain integrates it out, the simulation draws it (ADR 0023) — which is what caught the simulation sampling the prior, 0.558 against the chain's right 0.382. **A generated question set is not automatically one that probes the band it generated:** every generic question for a wall of 100 lands below the drawn range or at certainty |
| `gacha` — income model | **Written, and nothing calls it** | [ADR 0029](docs/adr/0029-income-is-what-the-bundle-declares-not-a-rate-per-day.md), 2026-09-22. `BannerModel.pullPrice` (`V15`, nullable — **unstated is not free**, and `pricedPull()` refuses rather than answering zero) plus `DeclaredIncomeModel`: balance from the inventory, accrual from the `Reward` rows that grant the currency at their own cadence, `reach` applied and every dropped grant **named**. **There is deliberately no pulls-per-day constant** — that number is on no screen. **PGR's bundle declares no reward paying a ticket, so accrual is zero and a test asserts it**: the assertion is a marker for a missing reading, and the reading to take is what the dailies pay in Black Cards. Still no route, no screen, no bean, and `PityState` stored nowhere — N28 closed the model, not the wiring |
| Frontend | **Six screens, browser-driven, 53 tests** | Inventory editor, goal picker, plan view carrying **every one of the solver's notes**, catalog browse and search, the character page with the **personalized overlay** — which since 2026-09-24 asks with **the reader's profile for the page's game** (`useProfileFor`), offers to make one when there is none, and shows a refusal instead of retrying it — and — since 2026-09-21 — a **roster screen**, so a construct with no goal can be recorded at all. The plan form **asks how far the reader gets and sends `reach`**, off `GET /api/games/{game}/measures`, remembered per profile in the store (persist v2) and **not stored server-side** — ADR 0022 stands, and its reversal trigger is now "a second device asks again". Same-origin locally via the Vite proxy. **There is no prettier or eslint config in the repo** and the files are hand-formatted — do not run a formatter here until somebody commits one |
| The image | **Starts in ~60 s at 0.1 CPU, since 2026-09-24** | **A deploy failed with no error**: 147 s to start on Render's 0.1 CPU, and Render stopped waiting ~30 s before Tomcat bound its port, keeping the old instance — `deploy` went red, correctly; a manual redeploy landed by luck. Measured at `--cpus 0.1 --memory 512m`: fat jar **175.7 s**, extracted 133.6 s, extracted + **CDS archive 59–65 s**, + C1-only JIT 29.4 s. **The image now extracts the jar and trains a CDS archive at build time under `RUN --network=none`** (Flyway and Hibernate validation off *for that run only*), so the build can never need a database. **C1-only is not taken** — it trades peak speed and the solver has a 2 s budget; measure the solver under it first. **E1 blocks the full image build on this machine** (Gradle-in-Docker meets Avast's TLS); test the runtime stage with `--build-context build=<dir holding src/app/build/libs/storm-almanac.jar>`. A fresh database is empty until somebody publishes into it |
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
- **Three things have never run once, now that everything is deployed.** *(The image and the OAuth exchange both
  ran 2026-09-24.)* **The API under load** — every request loads a whole version, fifteen queries, a deferral in
  `GameDataReadModel`'s javadoc. **An offline *write* path**, which needs the built bundle (`web-built`, 4173). And
  **a release where page and API change together**: Vercel and Render deploy at different instants, and the one time
  that happened locally the page rendered as nothing — wire types now mark new fields optional. **Now live.**
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
  **Shadow prices first rendered against a real plan on 2026-09-24**, in a browser against sequence
  10: Resonance at **90.00** Serum (3 more runs, no slack), Cogs and Skill Point at 0.00 (the runs'
  slack covers one more). The stage was always there; nobody had driven such a goal. **Still raw
  ids on the plan page:** stage names and the *craft and buy* list. The naming half is closed:
  a `progress:` line rendered as its bare slug beside a properly named `Cogs` until 2026-09-22
  (N37, ADR 0028), and a `choice:` line's on 2026-09-24, named by its prices rather than by
  upgrade ids. Every R1999 catalog and drop number in this file comes from Kornblume.
- **Eight qualifications of the closed phases are
  [in the archive](docs/history/tracker-archive.md#qualifications-moved-out-of-the-live-tracker-2026-09-11-seventeenth-session)**
  — the benchmark being one guide, the two community disagreements, the three
  shapes the model cannot express. **Read before re-opening Phase 1, 2 or 11.**

---

## Next actions

**B5 closed 2026-09-24** — deployed, signed into, and shipped by the pipeline —
after N37 and N28's modelling half on 2026-09-22 and N30, N20, N33, B6, N34, N36
and N35's halves on 2026-09-21, all [in the archive](docs/history/tracker-archive.md#completed-next-actions).
**No Phase 4 item is left; its exit is.**

> **Resume here (2026-09-25, forty-second):** **D5's rehearsal is under way**; every stall is a line below. **Open:** N42 below. Seen, not yet a stall: stage and *craft and buy* rows on the plan page show raw ids.

### D5's rehearsal — each stall, fixed or cut

- [x] **S1 — a second profile on one game and server answered a bare 500** (2026-09-25). The rule is right; the refusal is now a 409 naming the holder, and the form says so before the click.
- [x] **S2 — "where she stands" was one dropdown of ~70 raw ids across 13 tracks** (2026-09-25). Now one dropdown per track, tracks read off the upgrade graph; a goal row shows its own track. **The labels ("Flaming chord · 4") are guessed from the ids**, because no bundle names a track — naming them is data work, and `roster/tracks.ts` is the one place to read it from.
- [x] **S4 — rank read as numbers, and the skills as one flat list** (2026-09-25). The game says "Elite ★3" and groups skills under Basic Skill, Special Skill, Evolution Effect and Common Effect, tagged by orb; players find a skill by its tag. Now the bundle carries those words on each step (ADR 0032, `V16`, sequence 11) and the roster shows the game's sections, tag first. Skill names are still guessed from ids.
- [x] **S3 — no way on from one step to the next** (2026-09-25). Four steps now — Inventory, Roster, Goals, Plan — in one list the step bar, each page's Next and the home page all read; the menu follows it. Goals' Next saves first.

### Before the next sequence — worth knowing

**Production is a second database.** Publishing a sequence now means doing it twice: into the local volume, which `preview` diffs against, and into Neon, which readers see — the same `preview`, `ingest`, `publish` with `DATABASE_URL`, `DATABASE_USER` and `DATABASE_PASSWORD` pointed at Neon. **The password lives in `~/.neon-storm-almanac` on this machine, kept there on purpose** (maintainer, 2026-09-24): read it into the variable, never print it, don't delete it. The host and role are deliberately not in this repo; the session log never had them, and the thirty-ninth session recovered them from an earlier transcript and kept them in the agent's local notes. Neon holds sequence 12, published at 11:41:06Z on 2026-09-25. **The auto-mode classifier refuses a Neon publish as a production deploy** until the maintainer approves it in chat — ask, do not work around it. **Name the patch by number** (the game shows one; 4.8.0 since the 2026-09-24 maintenance), and ask which patch a sitting was on.

**One thing worth knowing for the next sequence, learned doing this one:** the
CLI needs a **live Postgres carrying every previous sequence** for `preview` to
mean anything — a fresh database reports *"this bundle would be the first"*,
which is not the check. `docker compose up -d postgres` restores it from the
`storm_almanac_postgres-data` volume, which still holds every version published
since 2026-09-06. **Even `validate`, which touches no version, boots the whole
Spring context and fails without a database.**

### Held — Phase 4 scope, and the maintainer decides

**N30, N33, N20, B5 and N41 are done or cut** ([archive](docs/history/tracker-archive.md#completed-next-actions)); what is left is the exit, held by
[D5](#d5--the-maintainer-rehearses-the-stranger-test-before-strangers-are-found-2026-09-25) until the rehearsal's stall list is empty. **With strangers:** the Google client
is in *Testing* — list each one or publish the app. Watch, answer nothing, record where each stalls; those notes close the phase.

### Held — later phases, not Phase 4's business

- [ ] **N42 — Read Ultima Awaken on its own screen; the maintainer will come back to it.** Sequence 12 (2026-09-25) put it on every S-rank construct at the leader unlock's price, **3 Skill Points + 25 000 Cogs, on the maintainer's report and no screen** — its own provenance entry, `ultima-awaken-report`, says so and that it contradicts `skill-pages` (no cost on its screen). Read: its button's price, whether it is one unlock or levels, and its bracket tag. **Its gate, the fourth Awaken, is not in the model** — the bundle has no Awaken track — so a plan to unlock it does not count the awakenings.

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

- [x] **Phase 0 · Ground** — closed by exception 2026-09-05
      ([D1](#d1--deployment-deferred-2026-09-02)); **box ticked 2026-09-24**, when
      the pipeline's `deploy` job shipped `b8c21e6` and smoked it through the real URL.
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
      rendered PGR. **Deployed 2026-09-24** at `storm-almanac.vercel.app`, signed
      into with Google. Launch publicly even if ugly.
      **Exit:** five strangers complete a plan without asking for help, and a
      logged-in character page shows what that reader is short of. *The second
      clause is served; the first is **deferred by the maintainer** ([D4](#d4--public-launch-deferred-until-the-site-is-more-finished-2026-09-24), then [D5](#d5--the-maintainer-rehearses-the-stranger-test-before-strangers-are-found-2026-09-25)).*
      **Closing condition, set 2026-09-20:** the exit is necessary and not
      sufficient — N30, N33, N20 and B5 are each done or explicitly cut before
      this box is ticked, with the cut recorded in the session log. **All four are
      done** (B5 with one cut, ADR 0030).
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

### D5 · The maintainer rehearses the stranger test before strangers are found (2026-09-25)

**Phase 4's exit asks for five strangers, and the maintainer knows few people who play**, so asked
whether testing it themselves would do. **It cannot meet the exit, and this does not change the
exit:** the test is whether someone who has never seen the site plans without help, and the one
tester who cannot stall where a stranger would is the one who built it. **What is taken instead, for
now, is a rehearsal** — a private window, one construct planned from the home page, every hesitation
written down, each fixed or cut on the record; friends who do not play count as rehearsal, not as
strangers. Google's client stays in *Testing*. **A deferral, not a cut;** the Phase 4 box stays
unticked. **Cost:** D4's, and sharper — D4 waited on the site, which work fixes; this waits on an
audience, which no work on the site produces, so the Track B gate and Phase 6's data (N26) have no
date. **Bought:** the stalls a builder can find are gone before a stranger's first impression.
**Reversal trigger:** the rehearsal's stall list is empty — then strangers are found where players
already are, the game's subreddit and Discord servers, rather than among people the maintainer knows.

### D4 · Public launch deferred until the site is more finished (2026-09-24)

**The plan says launch publicly even if ugly; the maintainer chose not to yet**, the same day
everything a launch needs went live — deployed, signed into, shipped by CI. The switch held
back is Google's consent screen, left in *Testing*, so only listed accounts can sign in and so
plan. **The URL is public and the catalog reads without an account**; that is not hidden.
**This is a deferral, not a cut** — the cut list's "never cut the Phase 4 public launch"
stands. **Cost:** the reason to launch ugly — strangers' feedback before effort goes into
guessing — waits, and so does everything that needs users: Phase 4's exit, the Track B gate,
and Phase 6's community data, which is the bootstrap problem (N26) itself. **Bought:** a first
impression made on more than one construct. **Reversal trigger:** Q6's list, once written, is
met — a list rather than a feeling, so the deferral cannot quietly become permanent. **Written
2026-09-24 with four items; all four met the same day.** The trigger fired; the maintainer's answer is [D5](#d5--the-maintainer-rehearses-the-stranger-test-before-strangers-are-found-2026-09-25).

### D3 · Launch title swapped to Punishing: Gray Raven (2026-09-13)

**The plan launched on R1999; the maintainer swapped in PGR** once Kornblume's source showed it
*does* solve per player, which `prior-art.md` had denied. **Cost:** PGR started with no data, and
fodder and the calendar moved before launch. **Bought:** a launch that is not a second copy of a tool
players use. **Reversal trigger, half dead since 2026-09-18:** an established PGR planner that solves
per player turns up. [In full](docs/history/tracker-archive.md#d3--the-full-account).

### D2 · Phase 5 entered before Phase 4 closed (2026-09-12)

**The rule broken is this file's own**, knowingly, by the maintainer's choice. [In full](docs/history/tracker-archive.md#d2--the-full-account).
**Reversal trigger:** none, a phase cannot be un-entered. The one that matters is on the next: **do not
take Phase 6 early on this precedent** — it needs users, where Phase 5's criterion was a proof about a model.

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

- **Q6 — What makes the site finished enough to let strangers in?** *Open, and the maintainer's
  (D4).* **Four items, all agreed:** (1) three constructs with whole ladders — **met 2026-09-24**
  by Lacrimosa, Selena: Pianissimo and Lucia: Inverse Crown (N41; Lucia replaced Karenina, whom the
  maintainer does not own). **Added 2026-09-24 (forty-first):** (2) `choice:` lines
  render raw upgrade ids — **met the same day by naming a choice by its prices** (`DemandNames`:
  "one of: 150 5★ Memory Shard · 234 Special Support Token · 246 Simulation Score"), which
  are facts already, so no upgrade needed a name of its own; browser-checked at 1280 and 375 px;
  (3) no shadow price has ever rendered against a real plan — **met the same day by driving one**:
  Lacrimosa's skill to 18 plus Samantha's Resonance renders 90.00 for the Resonance line; the bundle's
  one stage was enough and no reading was needed; (4) sign-in lands on `/` rather than where
  the reader was — **done in code the same day** (`ReturnAfterSignIn`, `?then=` held in the
  session across Google), **run against Google on 2026-09-25** once `63a45b3` deployed, by the
  maintainer's report. **All four met, so D4's trigger fired** — the maintainer's answer is D5.

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
| 2026-09-25 | forty-second | **PR #46 never deployed**: its `main` run went red on a `findBy` that took 1 320 ms where the same commit had taken 486. Production stayed on #45, and nothing showed it until somebody looked. `asyncUtilTimeout` is now 5 s for every test. The remote check now includes the last `main` run and the health SHA. **D5**: the maintainer rehearses the stranger test first, and strangers wait until the stall list is empty. **S1**, found in minutes: a second profile on one server was a bare 500 — now a 409 by name, and the form says so first. **S2** and **S3** the same day: a dropdown per track instead of ~70 raw ids, and four steps with a way on from each. **S4**: the game's rank names and skill sections, carried by the step (ADR 0032, sequence 11) |
| 2026-09-24 | forty-first | Q6 written: the maintainer agreed the three "seen, not agreed" items. **Sign-in now returns the reader to their page** — `?then=` held in the session across Google, one open-redirect check shared with the dev sign-in; untried against Google until deployed. **A choice line is named by its prices**, which are facts already. **A shadow price rendered at last** — 90.00, driven, no reading needed. All four met. Then the character page asks with **its own game's profile**, and the route refuses a mismatch by name. 466 backend tests, 39 frontend |
| 2026-09-24 | fortieth | **N41 closed**, with Lucia: Inverse Crown in Karenina's place (not owned). Sequence 9 put her and Selena on the ladder, +129 facts. Then the maintainer named the patch: today's readings were **4.8.0 "Anchored in Faith"**, live since that morning's maintenance, where sequences 8 and 9 said 4.7.0 — sequence 10 relabels, no fact changes. A test went red with sequence 9 unnoticed; run the full build. Both in both databases. 452 tests |
| 2026-09-24 | thirty-ninth | N41's question answered first-hand: every S-rank construct shares Lacrimosa's EXP, Promote and skill costs. So the bundle writes that path **once** (`ladders`, ADR 0031) and expands it per construct in the parser; Lacrimosa's 57 rows became 30 entries and diff against sequence 7 as *no changes*, same provenance per fact. Then her four skill pages showed **eight** levelled skills where the bundle had seven, one of them a name on no page. Sequence 8 published locally and to Neon, −4/+8, each read back as *no changes*. 452 backend tests |
| 2026-09-24 | thirty-eighth | B5 started, and **the backend went live** on Render and Neon with PGR sequence 7 published. The maintainer settled both decisions — one origin, and their own keep-alive bot for the sleeping free tier. **The image built and ran end to end for the first time**: 15 migrations on an empty Postgres, and PGR sequence 7 published into it from the same image's CLI. The "slow download" was a 500 MB build context with no `.dockerignore`. Running it as Render will found two bugs, each pinned by a test that failed first: `/actuator/health` 503 on an unused Redis, and an OAuth redirect URI of `http://<render-host>` behind the proxy. Then a third, in the page: the PWA worker answered sign-in navigations from its cache. Then **the first real OAuth exchange**, a sign-out production never had, and a `deploy` job whose first run on `main` went green — **B5 closed, Phase 0 ticked**. The dev sign-in kept (ADR 0030). 441 backend tests, 33 frontend |
| 2026-09-22 | thirty-seventh | N37 and N28 closed as one sequence 7, published and read back as *no changes*. **The EXP pool names are the bundle's own word, not the game's** — PGR calls all three pools "EXP" (ADR 0028) — and the pull price is 250 tickets, writable at last because the maintainer opened the ticket's tile and it grades 5★, so the format change the research note argued for was never needed (ADR 0029). `IncomeModel` written, and nothing calls it. **Three stale claims fell out of doing the work**, including "no bundle declares a banner at all", which had been false since sequence 0. Whole-graph equality caught an ordering bug the field-by-field assertions missed. 435 tests, 16 gated ones run; p95 1 812 ms and the plan 3 877 Activity, both recorded rather than smoothed |
| 2026-09-21 | thirty-sixth | N35's two load-bearing halves closed, leaving N37. The plan form asks **how far do you get** and sends `reach`, off a new `/measures` route that collects the ladders ADR 0022 said were declared nowhere; answering nothing is refused *by name*, answering 1 100 000 plans **Evolve to SS in 63 days and 0 Serum** — the first plan a web client has ever asked for that counts a scored grant. The ladder summary computes **56 Scars a week**, agreeing with the figure ADR 0022 states in prose. A roster screen, sharing one state-editing component with Goals. 420 backend tests, frontend 16 → 28. ADR 0022 stands: the maintainer kept `reach` on the request, so its trigger is now a *second device*. **Do not run prettier here** — no config, and it reformatted 456 lines of a 269-line change |
| 2026-09-02 – 09-21 | first to thirty-fifth | **Collapsed into one row on 2026-09-24, when this file passed 550 again**, and the thirty-fourth and thirty-fifth folded in the same day when the thirty-ninth and forty-first each passed it by one; the rows are [verbatim in the archive](docs/history/tracker-archive.md#session-index-rows-collapsed-on-2026-09-24). From the scaffold to Phase 5 closing out of order (ADRs 0001–0018), then the launch title swapped to PGR (D3), the first first-hand bundle, and **every shape it refused made writable**: gates and fodder (0019), lifetime caps (0020), choices (0021), `reach` (0022), drawn walls (0023), deadlines (0024), the day boundary (0025), crossed gates (0026), roster states (0027) and CI on `dev` (B6) — sequences 0 to 6 published, each read back as *no changes* |
