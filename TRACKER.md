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
- **Rewritten end to end on 2026-09-18**, after six sessions failed to hold it under 550 lines by trimming.
  [The 826-line version it replaces is in the archive, verbatim](docs/history/tracker-archive.md#the-tracker-as-it-stood-before-the-2026-09-18-compression) —
  go there for anything this file no longer carries, and decide whether it is
  still operative rather than assuming it was lost.
- Last updated: **2026-09-21** (thirty-third session)

---

## Status

- **The launch title is Punishing: Gray Raven** since 2026-09-13
  ([D3](#d3--launch-title-swapped-to-punishing-gray-raven-2026-09-13)); R1999
  moves to Phase 11, because Kornblume solves per player and on R1999 this
  product overlapped a deployed tool almost feature for feature. **Everything
  below that says "R1999" was true when written and still is — it is just no
  longer the launch.**
- **PGR is read first-hand ([the note](docs/game-facts/punishing-gray-raven-research-disclosure.md)),
  and [its bundle](data/bundles/punishing-gray-raven-steering-by-light.json)
  plans, EXP and one gate included ([ADR 0019](docs/adr/0019-a-gate-is-a-goal-inside-a-goal-and-progress-is-demanded-as-an-item.md)).**
  Simulated Battlefield pays Score, and Score buys every material and Pod.
  **Sequences 4, 5 and 6 are published** (2026-09-20T00:13:59Z, 2026-09-21T01:11:15Z and
  2026-09-21T02:49:27Z, all read back as *no changes*): a reader who says they clear the weekly Phantom Pain Cage is planned
  **Evolve to SS in 63 days and no Serum** and one who says nothing is refused *by name*
  ([ADR 0022](docs/adr/0022-a-grant-sized-by-the-player-is-an-answer-the-reader-supplies.md)), and
  the game declares its **05:00 UTC** reset. The rest of the prices are in
  [the table below](#what-the-next-work-touches). **Since sequence 6 the level ladder is priced
  end to end and every one of the thirteen Promote gates is a `requires`** (N33, 2026-09-21):
  thirteen cumulative EXP figures from 1 000 to 497 000, each pinned by the Pod selection
  that reaches it and the one 1 000 lower that does not. **A partial Promote plan used to be
  too cheap** — stopping at step 7 paid Cogs and no EXP at all — and is not any more.
  What the client overruled in the guides: the featured rule is a **per-banner rate**; **events
  have no shops** and pay one-time grants, which the plan reports as a deadline (ADR 0024); the Themed pool's wall is **drawn** (ADR 0023);
  and its **`PityScope` inherits Calibration across pools**, where R1999 clears — the two published
  games disagree on the one question that enum exists to answer.
- **Four decisions closed since 2026-09-20 — [0023](docs/adr/0023-a-drawn-guarantee-is-a-rate-curve-not-a-state-dimension.md)
  (`V11`), [0024](docs/adr/0024-an-expiring-grant-is-a-deadline-the-plan-reports-not-a-schedule-it-builds.md),
  [0025](docs/adr/0025-the-day-boundary-is-a-property-of-the-game.md) (`V12`) and
  [0026](docs/adr/0026-a-crossed-gate-is-a-reached-state.md). Each ADR is its own account; read it
  rather than a summary.** What still bites elsewhere in this file: both PGR pity archetypes are
  expressible and **neither is authored** (**N28**); **`Availability.opensAt` is read by nobody**,
  deliberately, the last of that record nothing reads; **`GameAgnosticismTest` is blind to a
  constant that is right for no game** — `UTC` is not a game name, and a javadoc caught 0025's bug,
  no test did; and **R1999's day boundary is Phase 11's problem**, with a second-hand number, a
  rotating stage table and nine benchmark agreements at stake.
- **Going first-hand on game data**, 2026-09-09
  ([ADR 0015](docs/adr/0015-game-data-is-sourced-first-hand-not-adapted.md)),
  superseding 0009 and closing Q2, Q3, F1 and F2. Kornblume is unlicensed.
  **Nothing is deleted yet and the order matters:** the adapter stays as a
  never-shipped cross-check until a replacement exists, because removing it first
  leaves the project with no real data at all. **The cost on patch 3.5:** ~2 700
  catalog facts, and **595 drop-rate facts** — the bootstrap problem.
- **The bootstrap problem is the main risk in the project, and the cheap way out
  is not there** (N26). No yield means no ranking means no plan; own drop data
  means Phase 6, which means users, which means a working plan. **The publisher
  does not disclose stage drop rates** — the screen grades a reward `Fixed`,
  `Common` or `Possible` and prices only the first, so **15 of 779 drop facts are
  declared and 764 sampled**, ~2% free. Two consolations: **gacha rates *are*
  disclosed**, and the grade is itself a free first-hand fact for all 595 pairs
  with nowhere to live until Phase 6
  ([the note](docs/game-facts/reverse-1999-drop-disclosure.md)).
- **Phases 0–3 and 5 are closed; Phase 4 is open; Track B is gated.** Phase 0 closed *by exception*, box unticked, because nothing is deployed; Phase 5 closed **out of order on purpose** ([D2](#d2--phase-5-entered-before-phase-4-closed-2026-09-12)) and **nothing calls either gacha engine**. [The board](#track-a--product).
- **Nothing is deployed, and [D1 is reversed](#d1--deployment-deferred-2026-09-02)** —
  Vercel and Render, free tier, still no money. The wiring is **B5**, the only
  thing that can unblock a real OAuth exchange. **`backend/Dockerfile`'s COPY list
  is B5's path and drifts in silence** — it omitted `adapters/` from Phase 1 until
  2026-09-09, every image build failing in six seconds while this file called it
  verified, so **anything added beside `modules`, `adapters`, `substrate`, `app`
  needs a line there**.
- **The remote, last checked 2026-09-21 (thirty-second) — re-check it, do not trust it.**
  **[PR #31](https://github.com/kietnt4412/storm_almanac/pull/31) is MERGED** — the thirty-first
  session's work went up and came back down while this file still named #30. `dev` was level with
  `origin/dev` and its tree identical to `origin/main` at the start of this session; `dev` sits
  behind by merge commits that never come back down, which is harmless and *not* a reason to
  rebase. **The trap stands:** CI runs on `pull_request` and on push to `main` only, so **a push to
  `dev` with no open PR runs nothing, silently**; every merge re-arms it, nine times now, each
  caught by looking. And PR #25 merged *before its own run finished*, green by luck (archive,
  twenty-sixth). **Run `gh pr list`, open the PR, and wait for the run before merging.**

### Two standing caveats, read them every session

1. **The strongest tests here are ones CI does not run.** Upstream data is fetched
   and never committed ([ADR 0009](docs/adr/0009-upstream-data-is-fetched-never-vendored.md)),
   so `RealUpstreamPatchTest`, `RealUpstreamPlanTest` and `CommunityBenchmarkTest`
   skip on the runner — 16 tests. **Every performance number and every comparison
   with an outside answer in this file comes from a test the pipeline does not
   run.** Run `backend/tools/fetch-upstream.sh` before trusting a green build to
   mean the pipeline handles real data.
2. **A green build says nothing about whether the data going into it is what the
   upstream publishes.** The adapter spent five sessions reading a stale stage
   table with two thirds of the game missing, green throughout. What caught it was
   going to look for somebody else's answer.

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

**What exists:** a game data pipeline that works end to end, an API that serves
it, a real game's data going through all of it, an optimizer that turns that into
a plan *and a reason to believe the plan*, an account that can own one and be
refused somebody else's, two devices that edit what it owns without deleting each
other's work, five screens a browser has driven, a page that says where its
numbers were read, an app that renders with its server switched off, two gacha
engines that agree about both published games — and one hand-typed bundle of the
launch title's own data, which the solver plans from, EXP and one gate included.

**The load-bearing claim, and it is a number to re-earn.** On **nine** benchmark
materials the cheapest stage this project computes is the one a published guide
tells players to farm, and the plan for a real goal set costs **3 880 Activity
against the guide's 4 017** — which does not even cover the whole demand. Nine and
not five because a yield carries its sample size (ADR 0011). **Every one of those
numbers comes from Kornblume-fed inputs**, which ADR 0015 says the product will
not ship, so until a self-sourced bundle reproduces them they are evidence about
somebody else's numbers run through our solver. **The method survives untouched**,
because the guide is a separate published artifact: a claim to re-earn, not a test
to delete. [The benchmark](docs/benchmarks/reverse-1999-community-answers.md).

Toolchain on this machine: JDK 21.0.12 (Temurin), Gradle **9.6.0** via the
committed wrapper. Remote is HTTPS at `github.com/kietnt4412/storm_almanac`.

### What the next work touches

| Area | State | The one thing to know |
|------|-------|-----------------------|
| Backend build | **Green** | **411 tests** in full 2026-09-21, 0 skipped locally **and all 16 snapshot-gated ones actually ran and passed**, so standing caveat 1 is satisfied for that build rather than assumed; **395 expected on CI**, where those 16 skip. `:app:test` depends on `:app:bootJar`, and declares `data/bundles` as an input — without that, `AuthoredBundlesTest` came back `FROM-CACHE` after a bundle changed. `api.version=1.44` — [E2](#environment-notes-this-machine-only) |
| Authored game data | **One bundle, sequence 6 published, first-hand** | Helentine: Lacrimosa (level to 80 **as a thirteen-link chain, every gated level priced**, 13-step Promote **all gated**, 7 skills to 18, Evolve to SS), Hear the Bell, Samantha (Overclock, Upper Resonance at three prices), one stage, 11 shop rows, 2 box crafts, 5 fodder rules, and — since **sequence 4, published 2026-09-20T00:13:59Z** — the weekly Phantom Pain Cage's nine tiers, and — since **sequence 5, published 2026-09-21T01:11:15Z** — the game's own **05:00 UTC** day boundary, and — since **sequence 6, published 2026-09-21T02:49:27Z** — the whole level ladder as a thirteen-link chain. **116 facts, all first-hand**, over seven provenance entries: the boundary is a game-level field and so adds no fact, which is exactly why `Facts` had to learn to flatten `Game` (ADR 0025). Preview and ingest read **exactly the changes intended** — 26 of them for sequence 6 — and each published version reads back as *no changes*. **Every sequence so far has been published and read back clean** ([the loop, per sequence, in the archive](docs/history/tracker-archive.md#session-log)); the next correction is a sequence 7. Three Cage tiers are **written short** — a gold 5★ card, a 4★ chip and a portrait item were never opened, so those grants are absent, which makes plans dearer and never cheaper. `AuthoredBundlesTest` parses every file in `data/bundles` and fails on any fact the project may not publish, on a provenance mapping naming no fact, and on an **empty** directory. `AuthoredBundlePlanTest` plans from it, so a correction moves a plan: a skill to its cap is **150 Serum**, a Memory's Overclock **420**, her last rank **1 470**, Samantha's Resonance **90**, and Evolve to SS **30 shards from a stock that never resets** (ADR 0020) |
| CI workflow | **Green, no warnings, Node 24** | Last *executed* suite: run `34695206362`, 16 skipped, exactly the three snapshot-gated classes. **`gradle/actions` held at v5** — v6 needs Gradle's Terms of Use accepted, which is the maintainer's call. **Counting PASSED lines in a log undercounts**; read task outcomes |
| Provenance | **Written, enforced, and read** | [ADR 0016](docs/adr/0016-provenance-is-a-property-of-the-data.md). `V7` stores one row per declared fact; `publish` refuses a version that is not first-hand and **names the facts**. **`ProvenanceRepository` is a second port** — the solver cannot see where a number came from, so it cannot be made to prefer one. Silence is `UNRECORDED`: parses, cannot publish |
| Parser adapters | **One, demoted to a cross-check** | `:adapters:reverse-1999`, 25 tests. **Hard-codes `THIRD_PARTY`, so it fails a plain `publish`** — there is no call site to launder data through. Kept because diffing the first self-sourced bundle against it is worth more than it ever was as a source |
| The MIP (`EnergyMip`) | **Stages, crafts, shops, rewards and fodder** | ojAlgo, integer runs, inventory subtracted, every variable bounded — the bound is what makes a real patch solvable. A purchase is a conversion capped at limit × *whole* periods, or the whole allowance of one that never resets, which the plan says it assumed unspent (ADR 0020); feeding fodder is a conversion into a `progress:<kind>` item, and paying one of a step's several prices a conversion into a `choice:` item (ADR 0021). Gates are not in the model: `DemandResolver` turns them into demand (ADR 0019). **A grant behind a score the reader has not cleared is dropped before the model** and reported in the notes, so the counts in a plan and in a refusal are the game *that reader* plays (ADR 0022) |
| The time axis | **A scalar, not an index** | [ADR 0013](docs/adr/0013-the-horizon-is-a-scalar-not-an-index.md). Rotation is capacity shared over *subsets* of weekday restrictions; **no variable is indexed by day**, which is why p95 held at **1 807 ms**. An expiring grant is **supply plus a reported deadline**, never a scheduled claim (ADR 0024). **Which weekday day zero is comes off the game** — `Game.dayBoundary`, a zone and an hour, and `null` means the midnight-UTC every version published before `V12` was planned by (ADR 0025) |
| `gacha` — engines | **Phase 5's criterion, and nothing calls them** | [ADR 0018](docs/adr/0018-the-gacha-engines-answer-one-question-about-one-rarity.md). An exact chain and 500 000 seeded trials sharing one validated `PullModel`, so both refuse the same banners for the same reasons. **53 tests, worst gap 0.110 points over 108 questions, at 2.22 standard errors.** Only the headline rarity is modelled. **Through a drawn guarantee they take different roads on purpose** — the chain integrates it out, the simulation draws it (ADR 0023) — which is what caught the simulation sampling the prior, 0.558 against the chain's right 0.382. **A generated question set is not automatically one that probes the band it generated:** every generic question for a wall of 100 lands below the drawn range or at certainty |
| `gacha` — income model | **Interface only, unwritable** | `projectedPulls` needs to know which item is pull currency and what a pull costs in it. **Neither `BannerModel` nor the `banner` table declares either** — **N28**, whose numbers now exist |
| Frontend | **Five screens, browser-driven, 15 tests** | Inventory editor, goal picker, plan view carrying **every one of the solver's notes**, catalog browse and search, and the character page with the **personalized overlay**. Same-origin locally via the Vite proxy. **Never rendered PGR** — every screen was built against R1999 |
| Docker Compose | **Repaired, and no image built end to end** | The fixed tree builds the jar locally and that jar contains no development sign-in. **The image itself is unproven** — the in-container Gradle download was abandoned at 10% after twenty minutes |
| Development sign-in | **Done, and absent from the artifact** | [ADR 0017](docs/adr/0017-the-development-sign-in-is-absent-from-the-artifact.md). `:modules:identity-dev` is `testAndDevelopmentOnly`, so **no property or profile can reach it**; `DeployableJarTest` opens the jar and proves the absence on every build. **Do not add a switch that turns it on** |

### Done and stable

Game data pipeline (Phase 1) · game data API · demand resolution · `YieldTable` ·
`MipOptimizer` · the two objectives · solve caching · `SolveCoordinator` · the
community benchmark · statistics primitives · the `identity`/`player` schema ·
sign-in · player API · offline sync · the plan route · bean wiring · the three
architecture tests · Track B (empty). **One row each, with the decision behind
it, [in the archive](docs/history/tracker-archive.md#the-tracker-as-it-stood-before-the-2026-09-18-compression)
under *Current state* — read the row before re-opening any of them.**

**The four most often misremembered:** solve caching has **no invalidation
method**, because a patch is a different key rather than a stale entry;
`SolveCoordinator` **does not survive a restart, deliberately**; **no FK crosses a
schema** and an identity is `(provider, subject)`, never an email; and **goals
have no PATCH**, because an ordered list has no per-key merge.

### What is still unverified

Be precise about this, because the temptation is to read "build green" as "it
works". It does not mean that:

- **CI does not run the tests that matter most.** The three snapshot-gated classes
  are the only evidence the optimizer is fast enough or right about anything real.
  The stale stage table that cost five sessions of plans was caught by no test at
  all — a person went looking.
- **Two layers are barely exercised.** Most authenticated tests go through
  MockMvc; `DevSignInTest` is the exception — a real port, a hand-kept cookie jar
  — so the container is exercised for sign-in and **not for the plan or the
  merge**. And jsdom computes no layout, which let a `display: block` fold a table
  header past the frontend's 15 tests: **appearance is a person's job, and driving
  a browser before shipping a screen is still required.**
- **Five things have never run once, and every one waits on B5.** The **OAuth
  exchange** — no client id, no secret, no redirect followed, and **the
  development sign-in does not narrow it by one inch**, so reading N24 as
  "sign-in works" is the misreading ADR 0017 exists to prevent. **A jar from a
  Dockerfile that works.** **The API under load** — every request loads a whole
  version, fifteen queries, a deferral written into `GameDataReadModel`'s javadoc.
  **An offline *write* path**, which needs the **built** bundle (`web-built`, port
  4173) because the dev server has no worker worth the name. And **a page and an
  API deployed at different instants** — that happened once and the page rendered
  as nothing, fixed by wire types marking the field optional so the compiler
  points at every call site. **B5's problem in miniature.**
- **No two real devices have ever synced.** One browser's stale edit has lost to a
  newer value and said so; two requests interleaving on the same key at the same
  instant is argued rather than measured. And **a merge publishes nothing**, so a
  cached plan survives the inventory under it moving — harmless until Phase 6.
- **Nothing has ever asked the gacha engines a question on behalf of a player.**
  No route, no screen, no bean, and **`PityState` is stored nowhere**: the gap is a
  schema and a screen rather than an engine. **Three banners are first-hand and
  five are not (Q4)**, **no bundle declares a banner at all** (**N28**), and
  **multi-copy answers are too pessimistic** — 200 Cassettes of the Lost buy a
  copy and nothing models it, so at two copies the engines say 280 pulls and the
  truth is 200.
- **One character is not a catalog, and its plans are still partial — but the
  worst half of it closed 2026-09-21
  ([ADR 0026](docs/adr/0026-a-crossed-gate-is-a-reached-state.md)).** The roster
  still holds **one state per entity**, so a reader records where they are on one
  track and nothing about the others. **What changed is that the planner no
  longer needs them to.** A gate is a condition the game enforced, so a player
  standing past a gated step demonstrably met it: `achieved` now credits the
  `requires` of every upgrade behind them, not just its `fromState`. A reader at
  `promote-6` asking for step 7 pays the **20 000 EXP between Lv 45 and Lv 50**
  instead of 60 000 from Lv 1 — **90 Serum against 180**.
  **What is still wrong, and needs the schema:** a reader whose recorded state is
  *behind* the gate gets no credit. Someone at `promote-0` who levelled to 80
  anyway — which PGR permits, levels are not capped by rank — is still charged
  the whole ladder. **That half wants `Roster` to hold a set of states**, which
  is a migration, a breaking change to the roster wire format, the offline sync
  patch shape and the frontend. Nobody has written it. **No
  `progress:` line or shadow price has ever rendered, and no frontend sends
  `reach`** — so every plan the web
  client asks for counts no scored grant and says which it left out (ADR 0022).
  Every R1999 catalog and drop number in this file comes from Kornblume.
- **Eight qualifications of the closed phases are
  [in the archive](docs/history/tracker-archive.md#qualifications-moved-out-of-the-live-tracker-2026-09-11-seventeenth-session)**
  — the benchmark being one guide, the two community disagreements, the three
  shapes the model cannot express. **Read before re-opening Phase 1, 2 or 11.**

---

## Next actions

**N30, N20 and N33 all closed 2026-09-21** and are
[in the archive](docs/history/tracker-archive.md#completed-next-actions). **B5 is
the only Phase 4 item left**, and it needs the maintainer's own accounts.

**The four items under *Before B5* below are new on 2026-09-21.** Each was
already described somewhere in this file — in *Status*, in the two standing
caveats, or in *What is still unverified* — and **none of them was an action
anybody could pick up.** A known defect that is only ever *described* is one
nobody is going to fix; these are the ones worth paying before the deploy rather
than after, and each says why it is on this side of B5. **A session can take any
of them alone.**

### Before B5 — debt worth paying first

- [ ] **B6 — Make CI run on a push to `dev`.** **A push to `dev` with no open PR
      runs nothing, silently** — CI triggers on `pull_request` and on push to
      `main` only. Nine merges have re-armed this and every one was caught by a
      person looking; PR #25 once merged *before its own run finished* and was
      green by luck (archive, twenty-sixth). **This is the cheapest item in the
      file** — a `push: branches: [dev]` trigger — and it is on this side of B5
      because B5 adds a `deploy` job that must never fire from an untested
      commit. **Exit:** a push to `dev` with no PR open produces a run.
- [ ] **N34 — Let `Roster` hold a set of states, not one.** The half of the
      roster flaw [ADR 0026](docs/adr/0026-a-crossed-gate-is-a-reached-state.md)
      deliberately did not fix. A reader whose recorded state is *behind* a gate
      gets no credit: someone at `promote-0` who levelled to 80 anyway is charged
      the whole ladder, and **PGR permits exactly that** because levels are not
      capped by rank. **Five pieces:** a migration adding `current_state` to
      `roster_entry`'s primary key; `Roster.currentState` becoming
      `Map<EntityId, Set<String>>`; `achieved` seeded from all of them;
      the roster wire format, which is a **breaking** change to `GET`/`PUT`/`PATCH`
      `/api/me/profiles/{profile}/roster`; and the offline sync patch shape,
      where the per-key clock stays per-entity because an entity's whole state
      set is the merge unit. **Before B5 because the wire format is published the
      moment a stranger loads the page.**
- [ ] **N35 — Render PGR, and send `reach`.** Two faults on the same surface.
      **No screen has ever rendered PGR** — all five were built against R1999,
      and PGR has been the launch title since D3 on 2026-09-13. **And no frontend
      sends `reach`**, so every plan the web client asks for counts **no scored
      grant** and reports which it left out in a note nothing displays
      ([ADR 0022](docs/adr/0022-a-grant-sized-by-the-player-is-an-answer-the-reader-supplies.md));
      no `progress:` line or shadow price has ever rendered either. **This is
      Phase 4's own exit criterion in disguise** — five strangers completing a
      plan means five strangers seeing these screens, so it cannot wait for after
      the deploy. **jsdom computes no layout**, so a browser has to be driven.
- [ ] **N36 — Fetch upstream before trusting a green build, once, and write down
      what it proves.** Standing caveat 1 says the strongest tests here are ones
      CI does not run: `RealUpstreamPatchTest`, `RealUpstreamPlanTest` and
      `CommunityBenchmarkTest`, **16 tests**, skip on the runner because upstream
      data is fetched and never vendored
      ([ADR 0009](docs/adr/0009-upstream-data-is-fetched-never-vendored.md)).
      Every performance number and every outside comparison in this file comes
      from them. **Run `backend/tools/fetch-upstream.sh` and record the numbers
      with a date**, so "p95 1 807 ms" stops being a figure of unknown age.
      **Not a code change** — an hour, and it either confirms the file or
      contradicts it.

### Held — Phase 4 scope, and the maintainer decides

**Phase 4 does not close until each is done or explicitly cut, on the record** —
a cut is a decision and goes in the session log; a silence is not a cut. **Each
was planned in full on 2026-09-20,
[in the archive](docs/history/tracker-archive.md#session-log)** — read the plan
rather than re-deriving one. **N33 closed 2026-09-21 and is
[in the archive](docs/history/tracker-archive.md#completed-next-actions); B5 is
the last one, and it is not started.**

- [ ] **B5 — Wire the real deploy: Vercel and Render.** **Settle two things
      first.** *One origin or two:* a Vercel rewrite of `/api/*` to Render keeps
      the same-origin session, CSRF and OAuth redirect the backend was built
      around; two real origins do not. *The free tier sleeps:* a cold start is
      tens of seconds against a two-second solve promise. **Build the image
      locally first** — never done end to end, and nothing else is testable until
      it is. **Ends with `:modules:identity-dev` deleted** (ADR 0017's trigger),
      not merely with a URL.

### Held — later phases, not Phase 4's business

- [ ] **N28 — Give a banner a pull currency and a price, then write `IncomeModel`.
      Unblocked — the numbers exist** and are in
      [the research note](docs/game-facts/punishing-gray-raven-research-disclosure.md):
      **1 pull = 250 Event Construct R&D Tickets**, about **$4.20 a pull**. A
      schema change rather than a decision, the same five pieces N20 walked on
      2026-09-21 — and N20 found a sixth, that `Facts` must flatten whatever the
      field hangs off or the preview reports nothing (ADR 0025).
      **Plus a decision the first bundle forced** — the ticket must be an `Item`
      and `Item.rarity` is required; tiles grade Cogs, Score and Scars, so try the
      ticket's tile before making rarity optional. **Two more fields belong in the
      same change**, both R1999: a **shop exchange for the featured unit** —
      cassettes cut six copies from 840 pulls to 560 — and a **cap on copies**.
      **This is the only thing keeping a banner out of a bundle**, so N28 ends in a
      **sequence 7** rather than in a schema — 6 was N33's. **The multi-copy
      pessimism belongs to this item**: 200 Cassettes of the Lost buy a copy,
      nothing models it, so at two copies the engines say 280 pulls when the
      truth is 200, and the shop exchange above is the fix.
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
      and **N25 is closed** — five screens driven in a browser including the
      overlay, provenance read back onto the page, the PWA loaded with its server
      killed, a frontend suite in CI. **Nothing is deployed**, and since D3 the
      launch title is PGR, which **no screen has ever rendered** — every one was
      built against R1999. Launch publicly even if ugly.
      **Exit:** five strangers complete a plan without asking for help, and a
      logged-in character page shows what that reader is short of. *The second
      clause is served; the first needs **B5**.*
      **Closing condition, set 2026-09-20:** the exit is necessary and not
      sufficient — **N30, N33, N20 and B5 are each either done or explicitly cut
      before this box is ticked**, with the cut recorded in the session log.
      Nothing here closes by having been forgotten. **N30 (ADR 0024), N20
      (ADR 0025) and N33 (sequence 6) are all done, 2026-09-21; B5 is the only
      one left.**
- [x] **Phase 5 · Gacha engine** — closed 2026-09-12 out of order
      ([D2](#d2--phase-5-entered-before-phase-4-closed-2026-09-12)); **one banner
      first-hand (Q4)**, income model and shop exchange are **N28**.
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

**Gate status: CLOSED, and structurally so** — nothing is deployed and nothing is
scheduled to be before Phase 4. Do not open `almanac-store`. When Phase 7 comes
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

**REVERSED 2026-09-09.** Heading kept verbatim so every link still lands. Both halves of its own trigger fired at once — Phase 4 reached, a free tier accepted — so **Vercel for the frontend, Render for the backend**, still no money ([in full](docs/history/tracker-archive.md#d1--the-deferral-in-full)). **What it cost is what B5 buys back:** Phase 0's box is unticked, the gate has no meaning without real traffic, and deploy problems really were found late.

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
| 2026-09-21 | thirty-third (cont.) | PR #33 opened and green. The deferred-defect list audited into *Next actions* as **B6, N34, N35, N36** — every one was already described somewhere in this file and none was an action anybody could pick up. One archived qualification found stale: a lifetime purchase limit has been expressible since ADR 0020 |
| 2026-09-21 | thirty-third (cont.) | The roster flaw N33 widened, half closed the same day (ADR 0026): a crossed gate is a reached state, so `achieved` credits the `requires` of every upgrade behind the player. A reader at Promote 6 pays 90 Serum for step 7 where they were billed 180. No migration, no wire change — the other half, a reader *behind* the gate, still wants a set of states on `Roster` |
| 2026-09-21 | thirty-third | N33 closed: the level ladder is priced end to end and all thirteen Promote gates are `requires`. Twenty-two Level Up previews on a Lv 1 construct, nothing spent and nobody levelled; thirteen cumulative figures, each pinned by the selection 1 000 below it falling short. The track became a chain because spokes from `level-1` would double-charge. Sequence 6 published 2026-09-21T02:49:27Z and read back as *no changes*; 411 tests, 0 skipped |
| 2026-09-21 | thirty-second | N20 closed: the day boundary is a property of the game (ADR 0025, V12). A zone and an hour on `Game`, one `DayOfWeek` moved and no row written; null is *unstated*, not midnight. The preview found a sixth piece the plan never named — `Facts` flattened `Game` not at all, so the sequence that declares a boundary read as *no changes*. PGR sequence 5 published and read back clean |
| 2026-09-21 | thirty-first | N30 closed: an expiring grant is a deadline the plan reports, not a schedule it builds (ADR 0024). No time index, no new variables — two lists, three notes and a refusal that names the window instead of blaming the cadence. The plan's `FEWEST_DAYS` sentence was backwards and the ADR carries the correction |
| 2026-09-20 | thirtieth | N31: a drawn guarantee is a rate curve, not a state dimension (ADR 0023, V11). The exact chain gained nothing; the simulation draws anyway, and caught itself sampling the prior — 0.558 against the chain's right 0.382. Then all four Phase 4 items planned: N30 decided as a deadline, N33's premise found already recorded, three held |
| 2026-09-20 | twenty-ninth | N32 closed by its fifth shape: a grant behind a score the reader supplies (ADR 0022, V10). Sequence 4, the Phantom Pain Cage: Evolve to SS in 63 days and no Serum. The EXP reading leaves as N33 |
| 2026-09-19 | twenty-eighth | N32 (4): one step at several prices is a choice the solver makes (ADR 0021, V9). Sequence 3: Samantha's Resonance, 90 Serum |
| 2026-09-19 | twenty-seventh | N32 (3): a shop limit that never resets (ADR 0020). Sequence 2, the shard shop, published; Evolve to SS plans |
| 2026-09-19 | twenty-sixth | N32 (1)+(2): gates become demand and fodder feeds EXP (ADR 0019, V8). Sequence 1 published; plans 240 → 420 and 180 → 1 470 |
| 2026-09-19 | twenty-fifth | Shops in the solver: the first-hand bundle plans, 150 and 240 Serum, worked out by hand first. Too cheap until N32 |
| 2026-09-19 | twenty-fourth | N27 done: one character, weapon and Memory, first-hand. PGR is farmed through a shop, so N30 needs shops; five refused shapes become N32; the reset is 05:00 UTC |
| 2026-09-18 | twenty-third | The first first-hand bundle — draft 0, nine facts — and the three *required* fields the format refused. `AuthoredBundlesTest` and the Gradle input hole it found. This file rewritten: 826 → 549 lines, 74 604 → 39 981 bytes, with the old one kept verbatim in the archive |
| 2026-09-18 | twenty-second | PGR read off the client, and the guides wrong twice: a per-banner featured rate, and events with no shops. N28 unblocked, N30 rescoped, N31's cause found |
| 2026-09-14 | twenty-first | The PGR survey D3 was missing. Nothing found plans PGR farming from an inventory; its one finding with teeth came from guides and was false |
| 2026-09-13 | twentieth | Launch title swapped to PGR (D3) — Kornblume solves per player. Q4 answered for one banner off the client. N5: every CI action onto Node 24 |
| 2026-09-12 | nineteenth | Phase 5 closed out of order (D2): two engines, worst gap 0.110 points over 84 questions. Four decisions in ADR 0018 |
| 2026-09-12 | eighteenth | N4: ADR 0007's own open gap becomes a failing build — `EntityKindBoundaryTest`, an allowlist over bytecode |
| 2026-09-11 | seventeenth | N25's three debts paid: provenance read back through a second port, the PWA loaded with the server killed, 15 frontend tests |
| 2026-09-11 | sixteenth | N25: five screens driven in a browser, and a character page that says what the reader is short of |
| 2026-09-09 | fifteenth | N24: a browser signs in, through a module the deployable jar does not contain (ADR 0017). Found a CSRF cookie never issued |
| 2026-09-09 | fourteenth | N26 answered — no, drop rates are not disclosed. Provenance becomes a field and `publish` enforces it (ADR 0016) |
| 2026-09-09 | thirteenth | N23 pays Phase 3's sync debt (ADR 0014); then the largest decision — go first-hand on game data (ADR 0015) |
| 2026-09-08 | twelfth | Phase 3 opened and closed: V5, sign-in creating the account, a plan from goals nobody handed the optimizer |
| 2026-09-08 | eleventh | N14: the horizon as a scalar (ADR 0013), p95 held at 1 807 ms, the two objectives finally disagree |
| 2026-09-08 | tenth · ninth | N15's solve cache, 1 806 ms → 2 ms; then N16 and N17 close Phase 2 and yields learn their sample size |
| 2026-09-07 | eighth · seventh | The community's answers, and the stage table missing two thirds of the game; the optimizer says how much it does not know (ADR 0010) |
| 2026-09-02 – 09-06 | first to sixth | Scaffold, first green build, first CI, prior art, D1 · the equipment question (ADR 0007) · Phase 1's schema · V3 · the API answering Phase 1's exit · ADR 0009 |
