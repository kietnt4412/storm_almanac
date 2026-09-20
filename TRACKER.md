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
- Last updated: **2026-09-20** (twenty-ninth session)

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
  Simulated Battlefield pays Score, and Score buys every material and Pod. A
  skill to its cap is **150 Serum**, a Memory's Overclock **420**, her
  last rank **1 470**, Evolve to SS **30 shards from a stock that never resets**
  (`AuthoredBundlePlanTest`, [ADR 0020](docs/adr/0020-a-limit-that-never-resets-is-offered-whole.md)).
  **Sequence 4 is published** (2026-09-20T00:13:59Z, read back as *no changes*): the weekly Phantom
  Pain Cage, nine tiers behind nine scores ([ADR 0022](docs/adr/0022-a-grant-sized-by-the-player-is-an-answer-the-reader-supplies.md)),
  so a reader who says they clear it is planned **Evolve to SS in 63 days and no Serum**, and one
  who says nothing is refused *by name*. Below the top rank plans are still too cheap:
  no level under 80 has a price, so twelve gates are prose. What the client
  overruled in the guides: the featured rule is a **per-banner rate** (70% with
  a Calibration guarantee, or 100%); **events have no shops** and pay one-time
  grants (**N30**); the Themed pool's wall floats (**N31**); and its
  **`PityScope` inherits Calibration across pools**, where R1999 clears — the two
  published games disagree on the one question that enum exists to answer.
- **Going first-hand on game data**, 2026-09-09
  ([ADR 0015](docs/adr/0015-game-data-is-sourced-first-hand-not-adapted.md)),
  superseding 0009 and closing Q2, Q3, F1 and F2. Kornblume is unlicensed.
  **Nothing is deleted yet and the order matters:** the adapter stays as a
  never-shipped cross-check until a replacement exists, because removing it first
  leaves the project with no real data at all. **The cost on patch 3.5:** ~2 700
  catalog facts — typing, mostly additive — and **595 drop-rate facts**, which is
  the bootstrap problem.
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
- **The remote, checked 2026-09-20 — re-check it, do not trust it.**
  **[PR #28](https://github.com/kietnt4412/storm_almanac/pull/28) is MERGED**, and `dev` and `main`
  had identical trees at the start of this session — `dev` sits behind by merge commits that never
  come back down, which is harmless and *not* a reason to rebase. **The trap stands:** CI runs on
  `pull_request` and on push to `main` only, so **a push to `dev` with no open PR runs nothing,
  silently**; every merge re-arms it, seven times now, each caught by looking. And PR #25 merged
  *before its own run finished*, green by luck (archive, twenty-sixth). **Run `gh pr list`, open the
  PR, and wait for the run before merging.**

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
| Backend build | **Green** | **384 tests** in full 2026-09-20, 0 skipped locally; **368 expected on CI**, where the 16 snapshot-gated ones skip. `:app:test` depends on `:app:bootJar`, and declares `data/bundles` as an input — without that, `AuthoredBundlesTest` came back `FROM-CACHE` after a bundle changed. `api.version=1.44` — [E2](#environment-notes-this-machine-only) |
| Authored game data | **One bundle, sequence 4 published, first-hand** | Helentine: Lacrimosa (level to 80, 13-step Promote, 7 skills to 18, Evolve to SS), Hear the Bell, Samantha (Overclock, Upper Resonance at three prices), one stage, 11 shop rows, 2 box crafts, 5 fodder rules, and — since **sequence 4, published 2026-09-20T00:13:59Z** — the weekly Phantom Pain Cage's nine tiers. **104 facts, all first-hand**, over six provenance entries; preview and ingest read **exactly the nine additions**, and the published version reads back as *no changes*. **Every sequence so far has been published and read back clean** ([the loop, per sequence, in the archive](docs/history/tracker-archive.md#session-log)); the next correction is a sequence 5. Three Cage tiers are **written short** — a gold 5★ card, a 4★ chip and a portrait item were never opened, so those grants are absent, which makes plans dearer and never cheaper. `AuthoredBundlesTest` parses every file in `data/bundles` and fails on any fact the project may not publish, on a provenance mapping naming no fact, and on an **empty** directory. `AuthoredBundlePlanTest` plans from it, so a correction moves a plan |
| CI workflow | **Green, no warnings, Node 24** | Last *executed* suite: run `34695206362`, 16 skipped, exactly the three snapshot-gated classes. **`gradle/actions` held at v5** — v6 needs Gradle's Terms of Use accepted, which is the maintainer's call. **Counting PASSED lines in a log undercounts**; read task outcomes |
| Provenance | **Written, enforced, and read** | [ADR 0016](docs/adr/0016-provenance-is-a-property-of-the-data.md). `V7` stores one row per declared fact; `publish` refuses a version that is not first-hand and **names the facts**. **`ProvenanceRepository` is a second port** — the solver cannot see where a number came from, so it cannot be made to prefer one. Silence is `UNRECORDED`: parses, cannot publish |
| Parser adapters | **One, demoted to a cross-check** | `:adapters:reverse-1999`, 25 tests. **Hard-codes `THIRD_PARTY`, so it fails a plain `publish`** — there is no call site to launder data through. Kept because diffing the first self-sourced bundle against it is worth more than it ever was as a source |
| The MIP (`EnergyMip`) | **Stages, crafts, shops, rewards and fodder** | ojAlgo, integer runs, inventory subtracted, every variable bounded — the bound is what makes a real patch solvable. A purchase is a conversion capped at limit × *whole* periods, or the whole allowance of one that never resets, which the plan says it assumed unspent (ADR 0020); feeding fodder is a conversion into a `progress:<kind>` item, and paying one of a step's several prices a conversion into a `choice:` item (ADR 0021). Gates are not in the model: `DemandResolver` turns them into demand (ADR 0019). **A grant behind a score the reader has not cleared is dropped before the model** and reported in the notes, so the counts in a plan and in a refusal are the game *that reader* plays (ADR 0022) |
| The time axis | **A scalar, not an index** | [ADR 0013](docs/adr/0013-the-horizon-is-a-scalar-not-an-index.md). Rotation is capacity shared over *subsets* of weekday restrictions; **no variable is indexed by day**, which is why p95 held at **1 807 ms**. Weekdays are read in **UTC** — a game assumption in a game-agnostic module (**N20**) |
| `gacha` — engines | **Phase 5's criterion, and nothing calls them** | [ADR 0018](docs/adr/0018-the-gacha-engines-answer-one-question-about-one-rarity.md). An exact chain and 500 000 seeded trials sharing one validated `PullModel`, so both refuse the same banners for the same reasons. **51 tests, worst gap 0.110 points over 96 questions, at 2.22 standard errors.** Only the headline rarity is modelled |
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
  are the ones that would catch an upstream-shape surprise, **and the only
  evidence the optimizer is fast enough or right about anything real.** The stale
  stage table that cost five sessions of plans was caught by no test at all — a
  person went looking.
- **The OAuth exchange has never run.** No client id, no secret, no redirect
  followed, because there is no URL to register one against (D1). **The
  development sign-in does not narrow this by one inch** — it builds a principal
  directly — and reading N24 as "sign-in works" is the misreading ADR 0017 exists
  to prevent.
- **Most authenticated tests still go through MockMvc.** `DevSignInTest` is the
  exception — a real port, a hand-kept cookie jar — so the servlet container is
  exercised for sign-in and profiles **and not for the plan or the merge**.
- **No two real devices have ever synced**, though one browser's stale edit has now lost to a newer
  value and said so. Still argued rather than measured: two requests interleaving on the same key at
  the same instant. And **a merge publishes nothing**, so a cached plan is not invalidated when the
  inventory under it moves — harmless today, **read it before Phase 6 depends on an inventory being
  current**.
- **Nothing has ever asked the gacha engines a question on behalf of a player.** No route, no
  screen, no bean, and **`PityState` is stored nowhere** — `player` has an inventory, a roster and
  goals, and no pity counters. "Probability of guaranteeing her" is computable and unanswerable,
  and the gap is a schema and a screen rather than an engine. **One banner is first-hand and six are not (Q4)**,
  so every other fixture proves only that the model reproduces figures it was
  *given* — and **multi-copy answers are too pessimistic**, because 200 Cassettes
  of the Lost buy a copy and nothing models it: at two copies the engines say 280
  pulls and the truth is 200.
- **One character is not a catalog, and its plans are partial.** Twelve of
  thirteen Promote gates are prose, because no level below 80 has a price; and
  the roster holds **one state per entity**, so a reader recorded at
  `promote-12` is charged the whole level track again (ADR 0019). **No
  `progress:` line or shadow price has ever rendered in the frontend, and no
  frontend sends `reach`** — so every plan the web client asks for counts no
  scored grant and carries the note saying which ones it left out (ADR 0022).
  Every R1999 catalog and drop number in this file still comes from Kornblume.
- **Nothing is deployed** ([D1](#d1--deployment-deferred-2026-09-02); the `deploy`
  job is `if: false`), **nothing has run against a jar from a Dockerfile that
  works**, and **nothing has called the API under load** — every request loads a
  whole version, fifteen queries, a deferral written into `GameDataReadModel`'s
  javadoc. The PWA has been loaded with its origin dead but **an offline *write*
  path has not**; reproducing the load needs the **built** bundle (`web-built`,
  port 4173), because the dev server has no worker worth the name.
- **The frontend's 15 tests will never catch one class of defect.** jsdom computes
  no layout — a `display: block` folding a table header got through. **Behaviour
  is the pipeline's, appearance is a person's, and driving a browser before
  shipping a screen is still required.** Relatedly, **a new page and an old API do
  not deploy at the same instant, and the first time that happened the page
  rendered as nothing**: fixed, with the wire types now marking the field optional
  so the compiler points at every call site. **That is B5's problem in miniature.**
- **Eight qualifications of the closed phases are
  [in the archive](docs/history/tracker-archive.md#qualifications-moved-out-of-the-live-tracker-2026-09-11-seventeenth-session)**
  — the benchmark being one guide, the two large community disagreements, the three shapes the
  model cannot express, and the rest. **Read them before re-opening Phase 1, 2 or 11.**

---

## Next actions

Ordered. Completed ones move to
[the archive](docs/history/tracker-archive.md#completed-next-actions).

- [ ] **N32 — Read the EXP to Lv 2, 10, 20 … 75.** **All five shapes the first
      full bundle refused are in** (ADR 0019–0022, sequences 1–4;
      [the account is in the archive](docs/history/tracker-archive.md#completed-next-actions)),
      so what is left of N32 is **a reading rather than code**, and it is the
      maintainer's: the EXP at each of the other twelve Promote gates, which is
      what turns them from prose into rows (MAX on one Pod size from each level,
      as Lv 80 was pinned). Two things to carry into it: the roster's **one state
      per entity** over-charges any reader whose recorded state is on a different
      track from the gate — read ADR 0019 first — and **no screen asks a reader
      what they reach**, so ADR 0022's answer is retyped per request until one
      does.
- [ ] **N31 — Give `PityRule` a guarantee that is drawn, not fixed.** PGR's Themed
      Construct pool draws its wall **uniformly 80–100, redrawn on every S-Rank**;
      `hardAt` is an `int`, so **the archetype is absent from the first bundle
      rather than approximated** — a gap in data now, not an argument in a note.
      Monte Carlo needs one extra draw; the exact chain needs the drawn threshold
      in its state, or 21 mixed chains (ADR 0018). **Do it with the fixture
      correction it implies** — `Banners.grayRavenFloating()` pairs the 1.50% base
      with 70% featured, and the client pairs 1.50% with **100%**.
- [ ] **N30 — Put the event shape into the solver (D3).** **Shops are done**
      (twenty-fifth session) **and so is fodder** (twenty-sixth). **Dropped** —
      **probabilistic goals**: weapon
      Resonance is picked; Memory Resonance is rolled but no goal names its skill.
      **Also:** a PGR event is **a one-time grant behind a capability gate inside
      a closing window** — no energy, no repetition, no yield; `EnergyMip` and
      `Reward`'s cadence cannot express it, and **`FEWEST_DAYS` against
      `Availability.closesAt`** is the question, not `LEAST_ENERGY`. **The gate
      half is answered:** *"can this player clear N"* is an input the reader
      supplies (ADR 0022), and an event mission is a `Reward` with a requirement
      and a `closesAt`. What is left is the **window**, and whether a one-time
      grant the reader must act on inside it is a plan or a deadline.
- [ ] **N28 — Give a banner a pull currency and a price, then write `IncomeModel`.
      Unblocked — the numbers exist.** **1 pull = 250 Event Construct R&D
      Tickets**, **1 Black Card = 1 ticket**, **1 Rainbow = 10 Black**, **119
      Rainbow = $19.99** — about $4.20 a pull. A schema change rather than a
      decision: a bundle field, parser, writer, a migration and the JDBC round
      trip, exactly like **N20**. **Plus a decision the first bundle forced** — the
      ticket must exist as an `Item` and `Item.rarity` is required. Shop and
      reward *tiles* turned out to grade Cogs, Simulation Score and Scars, so try
      the ticket's tile before making rarity optional. **Do it with a game whose income sources are
      ingested**: the accrual side reads `Reward` cadences and nothing ingested has
      been checked for them. **Two more fields belong in the same change**, both
      from R1999: a **shop exchange for the featured unit** — cassettes cut six
      copies from 840 pulls to 560 — and a **cap on copies**, since `copies` is
      unbounded.
- [ ] **N20 — Put the game's day boundary on the game, not in the planner.**
      `EnergyMip.matchingDays` reads weekdays in **UTC**, a game assumption in a
      game-agnostic module. R1999 Global rolls over at **05:00 UTC−5, weekly
      Monday**; **PGR's server clock is UTC and its daily reset 05:00 UTC** (read
      2026-09-19, cross-checked against this machine's clock). The banner still
      has no window: its *opening* time was never read. PGR rotates, and it is
      the launch. Same five pieces as
      N28; do it *with* that game, not speculatively.
- [ ] **N18 — Put drop estimates into `SolveKey` in the same change that first
      publishes one.** The moment Phase 6 does, a plan cached against yesterday's
      rates is served as today's — the one staleness bug the key's design cannot
      catch, and it is silent. **In the Phase 6 change itself, not after it.**
- [ ] **N19 — Write `RedisSolveCache` when there is a second node** (ADR 0012's
      trigger), **and before any benchmark against the Phase 8 replicated KV**
      whatever the node count: a replicated cache measured against an in-process
      map is measuring the network, which ADR 0003 forbids.
- [ ] **B5 — Wire the real deploy: Vercel and Render.** The `deploy` job stays
      `if: false` until there is a real URL to smoke, which is what this produces.
      **Settle two things first.** *One origin or two:* a Vercel rewrite of
      `/api/*` to Render keeps the same-origin session, CSRF and OAuth redirect the
      backend was built around; two real origins do not. *The free tier sleeps:* a
      cold start is tens of seconds against a two-second solve promise — warm-up
      ping, honest loading state, or acceptance, decided before five strangers meet
      it. **This also unblocks the OAuth exchange**, and it **ends with
      `:modules:identity-dev` deleted** (ADR 0017's trigger), not merely with a URL.

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
      launch title is PGR: its first bundle plans, EXP and one gate included,
      (**N30**, **N32**). Launch publicly even if ugly.
      **Exit:** five strangers complete a plan without asking for help, and a
      logged-in character page shows what that reader is short of. *The second
      clause is served; the first needs **B5**.*
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
  floor the untested candidate**. Still second-hand: R1999's beginner banner, and
  PGR's weapon, CUB and basic pools.

---

## Session log index

Full entries are in [the archive](docs/history/tracker-archive.md#session-log),
newest first. **Write the entry there; add one short line here.**

| Date | Session | What it was |
|---|---|---|
| 2026-09-20 | twenty-ninth | N32 (5) closes the format work: a grant behind a score the reader supplies (ADR 0022, V10). Sequence 4, the Phantom Pain Cage: Evolve to SS in 63 days and no Serum |
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
