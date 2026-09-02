# Storm Almanac — build tracker

**Single source of truth for progress across chat sessions.** Read this first,
update it last. If a session ends without this file reflecting what happened,
the next session starts from a lie.

- Source of the plan: [plan.html](plan.html) (13 phases, two tracks)
- Last updated: **2026-09-02**
- Current phase: **Phase 0 — Ground** (in progress)
- Track B status: **not started, and gated** — see [the gate](#the-gate)

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

**What exists:** the repo skeleton and the domain model. Phase 0 infrastructure
is written but not yet verified end to end.

| Area | State | Notes |
|------|-------|-------|
| Repo layout | Done | Gradle multi-module backend, Vite frontend, ADR folder |
| Domain model (`gamedata`) | Scaffolded | Records and sealed hierarchies complete; no persistence |
| Module ports | Scaffolded | Interfaces only — `Optimizer`, `SolveCoordinator`, `DropReportStore`, `BannerEngine`, repositories |
| `WilsonInterval` | **Done** | Real implementation, six tests |
| `PityRule` | **Done** | Real implementation, tests against both games' published rates |
| Boundary enforcement | Written, unverified | `ModuleBoundaryTest`, `GameAgnosticismTest` |
| Health endpoint | Written, unverified | `GET /api/health` |
| Docker Compose | Written, unverified | Postgres + Redis + api |
| CI workflow | Written, deploy step stubbed | `.github/workflows/ci.yml` |
| Frontend | Scaffolded | Renders health; PWA config present, no offline logic yet |
| Track B | Package docs only | Deliberately empty — see the gate |

### ⚠️ Nothing has been compiled

The machine this was scaffolded on had **no JDK installed and no running Docker
daemon**, so no Gradle build, no test run, and no container build has ever
executed. Everything above marked "written, unverified" is exactly that.

**First job of the next session** is [B0](#next-actions) — make it build. Expect
to fix real errors; treat every unverified item as suspect until a green build
says otherwise.

---

## Next actions

Ordered. Do them in this order.

- [ ] **B0 — Make it build.** Install JDK 21 (Temurin), run `cd backend &&
      gradle wrapper` to generate the wrapper, commit it, then `./gradlew build`.
      Fix whatever breaks. Likely suspects: the version-catalog lookup in the
      root `build.gradle.kts` `subprojects` block, the ArchUnit layer rules
      (they will flag dependencies that do not exist yet), and the relative
      source paths in `GameAgnosticismTest` (they assume the test runs with
      `backend/app` as the working directory).
- [ ] **B1 — `git init`.** This is not a git repository yet. Initial commit,
      then push to a remote so CI can run at all.
- [ ] **B2 — Verify `docker compose up` from a clean clone.** Stated deliverable,
      and the first thing any reader will try.
- [ ] **B3 — Wire the real deploy** in `ci.yml` (Fly.io or a small VPS) and make
      the smoke step actually `curl --fail` the deployed `/api/health`. That is
      Phase 0's exit criterion.
- [ ] **B4 — Read the prior art.** Kornblume and Penguin Statistics source, as
      the plan instructs. Write notes into `docs/`.
- [ ] **B5 — Write the positioning paragraph.** One paragraph. If it is not
      sharp, the project is not either. Put it at the top of the README.

---

## Phase board

Exit criteria are copied from the plan verbatim. Do not start a phase until the
previous one's criterion is met.

### Track A — product

- [ ] **Phase 0 · Ground** — 1 week — *in progress*
      Repo, CI, Docker Compose, ADR folder, hello-world deployed to production
      before any real code. Read Kornblume and Penguin Statistics. Write the
      positioning paragraph.
      **Exit:** a green pipeline deploying a health endpoint to a real URL.
      **Landed so far:** repo, ADRs 1–6, compose, CI skeleton, health endpoint,
      domain model. **Missing:** a build, a git repo, a real deploy, the reading,
      the paragraph.

- [ ] **Phase 1 · Game data foundation** — 2.5 weeks
      Canonical schema, R1999 ingestion (items, stages, characters, upgrade
      costs), versioned publishing with diffs, `gamedata-cli`. Tests over real
      patch data, including a patch that changes something. Includes the catalog
      axis — stat curves, skills, talents — modelled and ingested *now*, because
      retrofitting a second data axis into a published schema later is miserable.
      **Exit:** the API answers "what does Insight 2 cost?" and "what does her S2
      do at rank 3?"; a patch diff report renders for both axes.

- [ ] **Phase 2 · Optimizer core** — 2 weeks
      ojAlgo MIP model, crafting recursion, integer runs, solve caching,
      explanation output, both objectives.
      **Exit:** agrees with community-accepted answers on 5 benchmark goal sets;
      p95 solve under 2s.

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

**Gate status: CLOSED.** Nothing is deployed. Do not open `almanac-store`.

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

## Open questions

Carry these forward until answered; strike through with the answer when resolved.

- **Q1 — Hosting target.** Fly.io or a small VPS? Blocks B3 and Phase 0's exit.
  Fly.io is faster to a green deploy; a VPS is cheaper and gives real disks,
  which Track B will want by phase 7.
- **Q2 — Upstream data source for R1999.** Which community repository is
  canonical for items, stages, upgrade costs *and* the combat axis? Blocks
  Phase 1. Kornblume's data directory is the obvious first read.
- **Q3 — Seed data provenance.** Where do day-one drop estimates come from, and
  is their licence compatible with redistribution? Blocks Phase 6's cold start.
- **Q4 — Rate verification.** The pity numbers in `PityRuleTest` come from the
  secondary sources the plan cites. They must be checked against in-game
  disclosure before the simulator ships (Phase 5).
- **Q5 — Repository host.** No git remote chosen yet. Blocks B1/B3.

---

## Session log

Append one entry per session. Newest first.

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
