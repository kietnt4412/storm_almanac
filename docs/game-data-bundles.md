# Game data bundles

Onboarding a title is **a bundle plus a parser adapter, and nothing else**. This
file is the contract for the bundle half: the canonical JSON format, and the
commands that get it from a file into the application.

No backend code is involved. If onboarding a game ever requires editing
`planner`, `gacha` or `stats`, the abstraction has failed and
`GameAgnosticismTest` will say so.

---

## The loop

Publishing is a deliberate human approval, so it is four steps rather than one.
Everything runs off the same jar the server runs from; `--gamedata=…` starts it
without a web server, so this is safe on a host that is already serving.

```bash
java -jar storm-almanac.jar --gamedata=validate proving-ground-1.1.json
```

```bash
java -jar storm-almanac.jar --gamedata=preview proving-ground-1.1.json
```

```bash
java -jar storm-almanac.jar --gamedata=ingest proving-ground-1.1.json
```

```bash
java -jar storm-almanac.jar --gamedata=publish proving-ground 1
```

`validate` parses and checks the file with no database at all. `preview` diffs
it against whatever is currently published and writes nothing — it is the
question somebody actually has before they ingest, which is *what would
approving this change?* `ingest` writes it as a draft, invisible to every
reader. `publish` is the approval.

The rest: `drafts <game>` lists what is awaiting approval, `versions <game>`
lists what is published, `diff <game> <from> <to>` compares two published
versions — the one that goes in a release note.

Exit codes are `0` success, `1` refused (a bad bundle, an already-published
sequence, a draft that is not there) and `2` a wrong command line. The
difference matters to a script: retrying on a bad file is reasonable, retrying
on your own typo is not.

A sample report:

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

Three axes — `progression`, `catalog`, `gacha` — because a balance patch moves
combat multipliers without touching a material cost, and a reader who came for
one does not want to page through the other.

---

## The format

A bundle is one JSON object: a whole snapshot of one game at one version.
Snapshots are complete, never deltas, so publishing a patch inserts a fresh set
of rows and mutates nothing. That is what keeps a plan computed against an old
version correct after a new one lands.

Every identifier is an upstream **slug** — stable across a re-import and safe in
a URL. Nothing in a bundle refers to a database id.

### Top level

| Field | Required | Notes |
|---|---|---|
| `game` | yes | `{ id, displayName, energyUnit }`. `energyUnit` is what the game calls the stamina it gates farming with. Naming it here is what keeps the word out of the planner. |
| `sequence` | yes | Monotonic within a game; the ordering key. Supplied by whoever fetched the snapshot, not by the upstream — two snapshots fetched in either order still need one defined order. |
| `label` | no | The upstream patch label, `"1.9"`. Defaults to the sequence. Not unique: hotfixes reuse labels. |
| `attribution` | **yes** | Where these numbers came from. Required and never defaulted: *numbers and text only, attributed* is a project invariant, and an unattributed snapshot is one nobody can defend later. |
| `items`, `entities`, `stages`, `crafts`, `shops`, `rewards`, `upgrades`, `fodder`, `banners` | no | Each defaults to empty. |

### Items

Anything that can sit in an inventory: material, currency, or a copy of a
character. A copy is an item because duplicate-consuming systems make the same
thing a resource and a sink at once.

```json
{ "id": "sigil-lesser", "displayName": "Lesser Sigil",
  "rarity": { "label": "3*", "rank": 3 }, "category": "insight" }
```

`rarity` is `(label, rank)` and never an enum — one game counts stars, another
uses letter grades, and only the ordering of `rank` is meaningful. `category` is
free-form and game-supplied; it groups the UI and names the classes `fodder`
consumes.

### Entities

Characters **and** equipment. Equipment is an `Entity` rather than a concept of
its own — see [ADR 0007](adr/0007-equipment-is-an-entity.md).

```json
{
  "id": "warden", "displayName": "The Warden", "kind": "character",
  "rarity": { "label": "5*", "rank": 5 }, "element": "Ember",
  "tags": ["vanguard"],
  "statCurves": [
    { "stat": "atk", "breakpoints": [ { "ascensionTier": 0, "level": 1, "value": 84.0 } ] }
  ],
  "skills": [
    { "id": "warden-strike", "displayName": "Kindling Strike",
      "ranks": [ { "rank": 1, "description": "…",
                   "values": { "damage": 1.20 },
                   "upgradeCost": [ { "item": "gold", "quantity": 3000 } ] } ] }
  ],
  "talents": [
    { "id": "warden-poise", "displayName": "Poise",
      "unlockCondition": "insight-1", "effect": "…" }
  ]
}
```

`kind` is **required and never defaulted** — `"character"`, `"equipment"`, or
whatever the game draws. It routes and groups the catalog, and a bundle that
omits it produces pages nothing can link to. It is opaque: `planner`, `gacha`
and `stats` must never read it.

`element` is the game's own axis and is empty for things that sit on no such
axis. `statCurves` are breakpoints with linear interpolation between them,
because that is how the community repositories publish them and inventing a
closed form would mean inventing numbers. `skill` and `talent` slugs are unique
across the whole bundle, not just within their entity.

### Sources — how an item enters an account

Four shapes, four arrays. They are separate because their fields have almost
nothing in common.

- **`stages`** — `{ id, displayName, energyCost, drops[] }`. The only source
  that spends energy, and therefore the only one in the optimizer's objective.
  A drop is `{ item, expectedYield }`, and **that is a yield, not a
  probability**: a stage can drop several copies in one run, so real data
  carries values well past 1.0.
- **`crafts`** — `{ id, consumes[], produces[] }`. Deterministic and recursive;
  the planner expands them transitively.
- **`shops`** — `{ id, currency, price, offer, periodLimit, period }`.
  `period` is an ISO-8601 duration (`"P7D"`); `periodLimit` of `0` means
  unlimited. The cap is the interesting part — an uncapped entry lets a solver
  buy its way out of every constraint.
- **`rewards`** — `{ id, cadence, grants[] }`, cadence one of `DAILY`,
  `WEEKLY`, `MONTHLY`, `EVENT`, `ONE_OFF`.

Every source takes an optional `availability`:

```json
"availability": { "days": ["TUESDAY", "FRIDAY"],
                  "opensAt": "2026-08-01T00:00:00Z",
                  "closesAt": "2026-09-15T00:00:00Z" }
```

Omit it entirely for "always" — which is nearly every source, and spelling it
out on each one would bury the handful that rotate or expire. Empty `days`
means every day; a null `opensAt` means since forever; a null `closesAt` means
no announced end. Rotation and expiry are what make the optimizer time-indexed
rather than one static LP.

### Sinks — how an item leaves

- **`upgrades`** — `{ id, entity, fromState, toState, costs[] }`. States are
  opaque strings; the planner needs the graph they form, never their meaning.
  One edge per `(entity, fromState, toState)` — a second is a duplicate cost.
- **`fodder`** — `{ id, consumesCategory, minimumRarity, progressPerUnit,
  costs[] }`. Advancing one item by consuming others of a class. This is the
  case that breaks a naive resource graph, and it is in the model from day one.

### Banners

```json
{
  "id": "warden-debut", "displayName": "…", "bannerType": "character",
  "pityScope": "BANNER_TYPE",
  "baseRates": [ { "rarity": { "label": "5*", "rank": 5 }, "rate": 0.015 } ],
  "pityRules": [ { "rarity": { "label": "5*", "rank": 5 },
                   "hardAt": 70, "softFrom": 60,
                   "softJumpTo": 0.04, "softStep": 0.025 } ],
  "floors": [ { "everyN": 10, "minimumRarity": { "label": "4*", "rank": 4 } } ],
  "featured": { "chanceAtHit": 1.0, "guaranteeAfterLoss": 1 }
}
```

`pityScope` is `GLOBAL`, `BANNER_TYPE` or `BANNER`. Omit `softFrom` for a flat
wall with no soft pity; if you give it, `softJumpTo` and `softStep` are required
too and `softFrom` must precede `hardAt`. Base rates must sum to at most 1.0.
`featured` defaults to "always the featured unit".

**A guarantee the game draws instead of fixing** gets `drawnFrom`, the bottom of
the range; `hardAt` stays what it always was, the pull at which the rarity is
certain. Punishing: Gray Raven's Themed Construct pool draws uniformly over
80–100 and redraws on every hit, so it is `"drawnFrom": 80, "hardAt": 100`. Omit
`drawnFrom` for a fixed wall — that is what every banner written before this said
— and it must be at least 1 and strictly below `hardAt`, because a range of one
is a fixed wall spelled the long way. It is a property of the *data*: neither
engine needs telling, and the exact chain gains no state
([ADR 0023](adr/0023-a-drawn-guarantee-is-a-rate-curve-not-a-state-dimension.md)).

---

## What gets rejected, and how

Everything is checked before a database connection is opened, and every message
names the thing at fault. The composite foreign keys in the schema catch the
same class of error, one at a time, seconds later, saying `stage_drop_item_fk` —
which is not something the person approving a publish can act on.

```
stages[1].energyCost must be a whole number
entities[0].kind is required
duplicate item 'ore'
attribution is required: an unattributed snapshot is not publishable
bundle references 2 thing(s) it does not define:
stage '1-1' drops unknown item 'nope-one'
reward 'daily' grants unknown item 'nope-two'
```

Every dangling reference is reported at once. Failing one typo at a time costs
an operator a round trip per typo.

Two refusals come from the database rather than the file:

- **An approved version cannot be re-ingested.** Every plan and drop estimate
  that recorded that sequence did so on the promise it still means the same
  thing. Ingest the next sequence instead.
- **Re-ingesting a draft replaces it.** A scheduled fetch re-runs, and a failed
  ingest must not wedge the sequence.

---

## Where the numbers come from

Bundles are read from upstream sources, never copied from them. The consolidated
community repositories carry no licence, and absence of a licence is all rights
reserved rather than permission — see open question **Q3** in
[TRACKER.md](../TRACKER.md) and [prior-art.md](prior-art.md).

The acceptance fixtures in this repository are therefore a **synthetic title**,
`proving-ground`, invented here and built to exercise every shape above. It is a
real answer rather than a placeholder — the plan's own cut list says a synthetic
test game proves the abstraction.

### Adapters, and the step before the loop

Nobody hand-writes a thousand rows. For a title somebody else already publishes
data for, a **parser adapter** converts a snapshot into a canonical bundle file,
which then goes through the four commands above unchanged:

```bash
java -jar storm-almanac.jar --gamedata=adapters
```

```bash
java -jar storm-almanac.jar --gamedata=adapt reverse-1999 ./snapshot 1 3.5 out.json
```

An adapter lives in its own Gradle module under `backend/adapters/`, implements
`UpstreamAdapter`, and is the only place a game-specific quirk is allowed to
exist. `ModuleBoundaryTest` enforces that: only `:app` may depend on the
adapters layer, so a core module reaching for one fails the build.

`adapt` writes a file rather than ingesting directly, and that is the point —
the file is what a person reads, diffs and approves, and an adapter gets no
privileges for being code. It also prints what it refused to convert as it goes:

```
  skipped 1 stage(s) costing no Activity: a free source is an unbounded one
  skipped 4 unreleased character(s)
  skipped 590 resonance-pattern cost row(s): they are alternatives for one step
  converted 91 items, 99 stages, 50 crafts, 151 entities, 2012 upgrades
```

Read those lines. A snapshot that quietly yields half a catalogue should be
caught here rather than after publishing.

**The data itself is fetched, never committed** —
[ADR 0009](adr/0009-upstream-data-is-fetched-never-vendored.md).
`backend/tools/fetch-upstream.sh` downloads into an ignored directory, and
`RealUpstreamPatchTest` skips when it is absent, which is the state CI is in.
The pipeline has met somebody else's data; this repository holds none of it.
