# Prior art

Notes from reading Kornblume, Penguin Statistics and ArkPlanner, as Phase 0
instructs. Written 2026-09-02.

This exists to answer two blocking questions — **Q2** (which upstream data
source is canonical) and **Q3** (whether seed data is redistributable) — and it
surfaced one outright defect in our domain model. That defect is the single most
valuable thing on this page.

---

## 1. Kornblume

**Repository:** https://github.com/windbow27/kornblume — this is upstream. The
several other `Kornblume` repositories on GitHub (`yagochi`, `frantw`,
`zeronotion0`, `ochukai`) are forks. *Our README originally cited the wrong one;
corrected.*

**Stack:** Vue 3 + TypeScript + Vite + Tailwind, static site, no server.

**Its own cited sources**, per its README: Huiji Wiki for Arcanist information,
必要的记录 for drop data, and ArkPlanner for the farming-route algorithm. So
Kornblume is a presentation layer over three upstreams rather than a data
origin — which matters for Q2: **it is not canonical, it is downstream.**

### Data layout

Everything lives in `public/data/` as flat JSON, shipped with the site:

| File | Holds | Our equivalent |
|------|-------|----------------|
| `items.json` | materials and currencies | `Item` |
| `stages.json` | stages, energy cost, drop tables | `Stage` + `Drop` |
| `formulas.json` | crafting recipes | `Craft` |
| `shops.json` | shop offers | `Shop` |
| `arcanists.json` | characters | `Entity` |
| `psychubes.json` | equipment | **nothing — see 4.3** |
| `stages<patch>_greedy.json` | precomputed farming routes per patch | — |

### Observed shapes

`stages.json`, keyed by stage name:

```
"1-1": { "id": 0, "name": "1-1", "category": "Story", "cost": 8,
         "drops": { "<item name>": 0.312, ... } }
```

`formulas.json`, parallel arrays rather than pairs:

```
{ "Id": 1, "Name": "<output>", "Rarity": 6,
  "Material": ["<name>", ...], "Quantity": [1, 1, 4, 800] }
```

### What this tells us

- **There is no server-side solving.** The `stages<patch>_greedy.json` files are
  *precomputed* routes baked per patch, and the algorithm is greedy, borrowed
  from ArkPlanner. This is the wedge, confirmed by inspection rather than
  assumption: a real MIP solved per player against their own inventory is
  something Kornblume structurally cannot do from a static host.
- **Patch versioning is unavoidable.** Kornblume encodes it crudely, in
  filenames — `stages1_4`, `stages2_8`, `stages3_3`. Our `GameDataVersion` is
  the same insight done properly, and the presence of eight patch-stamped files
  is evidence the problem is real rather than anticipated.
- **Everything is keyed by display name**, not by a stable slug. `"Silver Ore"`
  is the primary key. See 4.2 — this is a hazard for us.

---

## 2. Penguin Statistics

**Repositories:** https://github.com/penguin-statistics — `backend-next` is the
current v3 backend; `backend` is retired.

**Stack:** Go, fiber, bun, `go.uber.org/fx`. NATS JetStream as the message
queue, PostgreSQL for storage, Redis for rate limiting and state sync.

### Architecture

Reports arrive at a `/report` endpoint, are queued into a NATS stream, and a
**Report Worker** validates and persists them asynchronously. A separate
**Calculation Worker** periodically runs a `DropMatrixService` that computes
drop-rate matrices per stage. Rate limiting is Redis-backed.

### What we take, and where we deliberately differ

- **Take: asynchronous ingest.** Reports queue and are validated by a worker,
  not inline in the request. Our `DropReportStore.append` should sit behind a
  queue for the same reason — submission latency should not depend on
  aggregation.
- **Take: periodic recomputation.** Estimates are a materialised read model
  refreshed on a schedule, not computed per query. Our
  `DropEstimateRepository.publish` already assumes this shape.
- **Differ: versioning.** Penguin separates data by **time range**
  (`TimeRangeService`). We separate by **explicit `GameDataVersion`**. Time
  ranges are simpler but wrong at the boundary: a patch lands at an instant and
  players report against both old and new tables for hours either side. Our
  approach costs more bookkeeping and is more correct. Worth keeping, and worth
  an ADR if it ever looks expensive.
- **Unknown: the statistics.** The available documentation does not state which
  estimator `DropMatrixService` uses, whether it produces confidence intervals,
  or how sample size is surfaced. **Do not assume they use Wilson intervals, and
  do not cite them as precedent for ours.** If this matters later, read the Go
  source directly rather than the generated wiki.

---

## 3. ArkPlanner

**Repository:** https://github.com/penguin-statistics/ArkPlanner

The farming solver Kornblume borrows. Reading its API surface:

- **Returns integer stage counts** (`'count': int`, "times to farm"), so the
  integrality requirement is not our invention — the established tool already
  concluded fractional runs are useless to a player.
- **Exposes `values`: item valuations.** No explicit duals or shadow prices, but
  an item-value vector out of a farming LP *is* the dual in all but name. This
  is real validation for `Explanation.shadowPrice`: the useful output already
  exists in prior art, it is simply not presented as an explanation.
- Request surface maps cleanly onto our `Source` hierarchy: `owned` and
  `required` (inventory and demand), `exclude` (banned stages), `syntheses`
  (crafting), `store` (shop purchases).

**Our differentiation is narrower and more honest than "we have a solver":** we
solve per-player against live crowdsourced estimates with confidence intervals,
we surface the explanation rather than hiding it, and we do it behind one
game-agnostic model. ArkPlanner has the solver. It does not have the other three.

---

## 4. Consequences for our model

### 4.1 `Drop` is wrong — drop values are expected yield, not probability

**This is a defect, not a refinement.** Our record is:

```java
public record Drop(ItemId item, double declaredProbability, int quantityPerHit) {
    // validates declaredProbability in [0, 1]
}
```

Real drop values in `stages.json` **range above 1.0** — observed up to 2.187.
They are *expected quantity per run*, because a stage can drop several of an
item. Our validation would reject roughly the entire upstream dataset on ingest.

It is also the wrong quantity mathematically. The MIP constraint is

```
sum over stages s of  x_s * drop[s,i]  >=  demand_i
```

which needs **expected yield per run**. Probability times quantity is that
number; storing the two factors separately and multiplying at solve time buys
nothing and invites the bug where one is used without the other.

**Fix:** replace `declaredProbability` and `quantityPerHit` with a single
`expectedYield` (a non-negative double, unbounded above). Where a true
probability is genuinely needed — a drop that is binary per run — it is
recoverable as `min(1, expectedYield)` only when yield is at most 1, so record
the distinction explicitly if it ever matters rather than inferring it.

### 4.2 Upstream keys on display names, so the parser adapter carries real risk

Every upstream record identifies items and stages by their English display name.
Our `ItemId`/`StageId` are stable slugs, which is right, but it means the parser
adapter owns a name-to-slug mapping that is:

- **breakable by a rename** — a localisation fix upstream silently orphans an id;
- **breakable by a collision** — two items sharing a display name across
  categories;
- **the natural place for a patch diff to fail loudly**, which is what we want.

The mapping must be persisted and versioned, not recomputed per ingest. An
unrecognised name is a **failed ingest**, never a new item created silently.

### 4.3 Psychubes are an equipment axis we do not model

`psychubes.json` is gear: equippable, upgradeable, with its own material costs.
Our model has `Entity` (characters) and `Item` (materials), and equipment is
neither — it is an upgradeable thing that is *owned* rather than a character.

Two options, and this needs deciding before Phase 1 ingestion:

1. **Treat equipment as an `Entity`** with its own upgrade graph. `Entity` is
   already a game-agnostic "thing with an identity, a rarity and upgrade
   states"; only the name is character-flavoured.
2. **Add a third top-level concept.** More honest, more code, and it risks
   inventing a distinction the second game does not share.

**Leaning toward (1)**, renaming nothing yet — Punishing: Gray Raven's Memories
are also equipment-like, and if one concept covers both games' equipment then
the abstraction is doing its job. Decide with the second game's shape in view,
not just the first.

### 4.4 Confirmations, not changes

- `Craft` (consumes[] → produces[]) matches `formulas.json` exactly.
- `Shop`, `Stage`, `Item`, `Entity` all have direct upstream counterparts.
- Integer decision variables are established practice, not our idea.
- Patch versioning is a real, load-bearing problem, independently discovered by
  Kornblume.

---

## 5. Answers

### Q2 — which upstream source is canonical?

**Not Kornblume.** It is a downstream presentation layer over Huiji Wiki
(characters), 必要的记录 (drop data) and ArkPlanner (algorithm). Its
`public/data/*.json` is nonetheless the most *convenient* consolidated shape we
have found, and it maps cleanly onto our model.

Recommended position: treat Kornblume's JSON as a **reference shape and a
cross-check**, and go to its upstreams for the data itself. That is unresolved
and is now the follow-up below — Q2 is narrowed, not closed.

### Q3 — is seed data redistributable?

**Assume no, for now.** There is **no `LICENSE` file** in the Kornblume
repository at the conventional path, which means all rights reserved by default,
not public domain. Absence of a licence is not permission.

Practical consequence: we may read it to validate our schema and our numbers, we
may not vendor it into our repository as seed data. This makes the plan's advice
— credit and talk to the maintainers early, be a participant before a vendor —
operationally necessary rather than merely polite.

---

## 6. Follow-ups

- **F1** — Identify and evaluate 必要的记录 as the actual drop-data upstream.
  Blocks Q2 closing. **Deferred 2026-09-06:** Kornblume is now ingested through
  `:adapters:reverse-1999` and its drop table is credited to 必要的记录 in every
  bundle attribution. Going direct is a second adapter, worth doing when the
  consolidated numbers are shown to be stale or wrong — not on principle.
- **F2** — Ask the Kornblume maintainer directly about data licensing and
  whether they would object to a derived tool. Blocks Q3 closing.
  **Still open, and now scoped by [ADR 0009](adr/0009-upstream-data-is-fetched-never-vendored.md)
  (2026-09-06):** the adapter was built and the data is read, never vendored.
  F2 stops being a follow-up and becomes a blocker at the moment this service is
  deployed publicly — not before.
- **F3** — Read `backend-next` Go source for the real `DropMatrixService`
  estimator, before claiming anywhere that our statistics are better.
- ~~**F4** — Decide the equipment question (4.3) before Phase 1 ingestion.~~
  **Closed 2026-09-05: equipment is an `Entity`**, per
  [ADR 0007](adr/0007-equipment-is-an-entity.md). §4.3's option (1) won, though
  on a different argument than §4.3 gives — see the ADR. Phase 1 ingestion is
  unblocked.
