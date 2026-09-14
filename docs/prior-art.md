# Prior art

Notes from reading Kornblume, Penguin Statistics and ArkPlanner, as Phase 0
instructs. Written 2026-09-02. **§1's central claim about Kornblume was wrong and
is corrected in place, 2026-09-13**: Kornblume does solve per player.

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

- ~~**There is no server-side solving.** The `stages<patch>_greedy.json` files are
  *precomputed* routes baked per patch, and the algorithm is greedy, borrowed
  from ArkPlanner. This is the wedge, confirmed by inspection rather than
  assumption: a real MIP solved per player against their own inventory is
  something Kornblume structurally cannot do from a static host.~~
  **Wrong, corrected 2026-09-13 (twentieth session).** Kornblume solves per
  player, in the browser. `src/composables/glpkSolver.ts` builds a linear program
  from the reader's warehouse store, with drops as stage variables, crafts as
  variables and inventory subtracted from demand. It minimises Activity with
  `glpk.js`, and the file dates from **2024-03-05**, eighteen months before this
  note. **Crafts are declared integer and stage runs are not**, and a second small
  integer program handles Sharpodonty and Dust. The site also imports an
  inventory with OCR (`tesseract.js`) and syncs through Google sign-in.
  **How the mistake happened:** this note read the `public/data/` directory,
  saw the `_greedy` files, and inferred the planner from its data without
  opening `src/`. "Confirmed by inspection" was true of the data and false of
  the claim. **What is left of the wedge is narrower**: whole runs, a plan that
  says why each stage is on it, yields discounted by their sample size,
  provenance on the page, and one model across two games. The last is why the
  launch title moved to Punishing: Gray Raven the same day.
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

**Assume no — and as of 2026-09-09 that became the settled position** via
[ADR 0015](adr/0015-game-data-is-sourced-first-hand-not-adapted.md): the project
sources its own data rather than asking. There is **no `LICENSE` file** in the Kornblume
repository at the conventional path, which means all rights reserved by default,
not public domain. Absence of a licence is not permission.

Practical consequence: we may read it to validate our schema and our numbers, we
may not vendor it into our repository as seed data. This makes the plan's advice
— credit and talk to the maintainers early, be a participant before a vendor —
operationally necessary rather than merely polite.

---

## 6. Follow-ups

- ~~**F1** — Identify and evaluate 必要的记录 as the actual drop-data upstream.~~
  **Closed 2026-09-09, not done**, by
  [ADR 0015](adr/0015-game-data-is-sourced-first-hand-not-adapted.md). Going
  direct would have bought fresher sampling from a source that has no licence
  either — a second adapter to the same problem. Our own drop reports (Phase 6)
  answer the question F1 was asking.
- ~~**F2** — Ask the Kornblume maintainer directly about data licensing and
  whether they would object to a derived tool.~~ **Closed 2026-09-09 unsent**, by
  the same ADR. F2 blocked *deploying upstream numbers publicly*; the project is
  sourcing its own data instead, so there will not be any. Re-verified that day:
  still `license: null`, still no LICENSE file, owner `windbow27`, actively
  maintained.
  **This is a decision to go independent, not a judgement about Kornblume.** It
  is a well-made hobby project that credits its own three upstreams properly, and
  0015's reversal trigger says plainly that if first-hand sourcing stalls, the
  honest move is to send F2 after all rather than quietly re-adopt the scrape.
  **Nothing here releases the publisher's rights** in names and text, which is
  why *numbers and text only, no game assets* stays an invariant either way.
- **F3** — Read `backend-next` Go source for the real `DropMatrixService`
  estimator, before claiming anywhere that our statistics are better.
- ~~**F4** — Decide the equipment question (4.3) before Phase 1 ingestion.~~
  **Closed 2026-09-05: equipment is an `Entity`**, per
  [ADR 0007](adr/0007-equipment-is-an-entity.md). §4.3's option (1) won, though
  on a different argument than §4.3 gives — see the ADR. Phase 1 ingestion is
  unblocked.

---

## 7. Punishing: Gray Raven — surveyed 2026-09-14

Done when the launch title swapped (D3), and done differently from §1: **every
tool below was opened or its source read**, not judged from a search snippet or a
data directory.

| Tool | What it actually does | Plans farming from an inventory? |
|------|-----------------------|------|
| [mcgalih/PGR-Calculator](https://github.com/mcgalih/PGR-Calculator) ([site](https://pgr-calculator.vercel.app)) | Totals the cogs, EXP pods and Memory enhancers a build costs. 0 stars, last pushed **2024-01-22** | No |
| [ravenkougu.github.io](https://ravenkougu.github.io/) | A serum refill timer. The other features are listed as planned. Last pushed **2021-07-23** | No |
| [pitycalculator.com — PGR](https://pitycalculator.com/punishing-gray-raven/pity-calculator) | Probability from current pity and available pulls. **Hard pity only**: no featured split, no copies, no income over time | No |
| Community spreadsheets (a "Resource Calculator V2" on the GRAY RAVENS wiki's guides page, and Rexlent's sheet) | Not opened: the wiki returned 403 to a fetch, and the sheets are only linked. **Unverified**, so neither is claimed to lack anything | Unknown |
| Guides and videos ([GRAY RAVENS beginner's guide](https://grayravens.com/wiki/Guides/Beginner's_Guide) and others) | Advice, not tools. The consistent message: from level 40, **farm event stages and spend their currency in event shops**, and fall back to resource stages between events | — |
| GitHub search ("punishing gray raven", "pgr planner", …) | An automation bot, a 2020 data dump, a private server | No |

**Not searched exhaustively:** Discord servers, Bilibili, and closed
Chinese-language sites. A Chinese-language web search found material-cost
write-ups and guides, not a planner. The fair summary is **nothing found plans PGR
farming from a player's inventory**, not "nothing exists".

### What it changes for the model, before any PGR data is read

- **Shops in the solver move up.** If the guides are right that event stages
  feed event shops, a PGR plan that cannot buy is the wrong plan. Today
  `EnergyMip` refuses an item sourced only from a shop. That is honest, but for
  PGR it refuses most of the answer.
- **Event windows stop being theoretical.** `Availability` already has
  `opensAt`/`closesAt`, and it has never met a real event.
- **Second-hand sources already disagree about PGR's featured guarantee.** One
  says a loss carries a guarantee, one says it does not, and the fixtures assume
  it does. That is the same reason Q4 was read off the R1999 client, and PGR's
  banner screen is first in N27 for it.
