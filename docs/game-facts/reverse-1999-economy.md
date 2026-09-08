# Reverse: 1999 — the economy facts the upstream does not publish

**Status: maintainer-declared, secondary sources, not verified against the
client by this project.** Nothing here is fetched, nothing here is in a bundle,
and nothing here is used by a test. It exists so that the next person to work on
rewards, shops or the time axis starts from what is known rather than from
scratch — and so that the difference between *known* and *implemented* stays
visible.

Recorded 2026-09-08 (eleventh session), supplied by the project maintainer and
checked by them against community references for the **Global 3.5** window.

## Provenance, stated first because it is the whole point

[ADR 0009](../adr/0009-upstream-data-is-fetched-never-vendored.md) governs the
Kornblume data: fetched at need, never committed. This file is a different kind
of claim and must not be filed beside that one. Three evidence classes, kept
apart:

| Class | Example | How it may be used |
|---|---|---|
| **A · Upstream-derived** | The Poussiere VI yields 12 500 Dust | Already in the fetched tables; the adapter reads it |
| **B · Operational fact about the service** | Global resets at 05:00 UTC−5 | Real, stable, citable — but not a row in any licensed dataset |
| **C · Maintainer observation** | A shop's stock resets monthly | Needs a `sourceType` marker if it ever enters a bundle |

If any of this ever becomes bundle data, it carries its own provenance through
the transformation rather than inheriting the upstream's:

```json
{
  "sourceType": "maintainer_declared",
  "source": "declared by project maintainer",
  "licence": null,
  "verifiedAt": "2026-09-08",
  "note": "Community references for Global 3.5; not fetched from licensed upstream data."
}
```

**The community references behind this page are secondary** — wikis and forum
documentation, reached through an AI search tool, so the citation trail is
one hop longer than it looks. That is the same standing the pity rates already
have under **Q4**, and it carries the same obligation: check against in-game
disclosure before anything ships that a player would act on.

---

## 1. There is no weekday rotation. This is the finding.

The six candidate stage families are **permanent and open every day**. They are
not a Monday/Tuesday rotation; the four Insight ones are Afflatus
specialisations that happen to be separate stages:

| Family | Kind | Specialisation | Days |
|---|---|---|---|
| Brutes Wilds II / IV / VI | Insight | **Beast** | all seven |
| Mountain Echoes II / IV / VI | Insight | **Mineral** | all seven |
| Starfall Locale II / IV / VI | Insight | **Star** | all seven |
| Sylvanus Shape II / IV / VI | Insight | **Plant** | all seven |
| The Poussiere VI | Resource | 12 500 Dust + 250 Sharpodonty, **25 Activity** | all seven |
| Mintage Aesthetics VI | Resource | 9 000 Sharpodonty, **25 Activity** | all seven |

The tiers of a family share a schedule. The two Resource yields and their 25
Activity cost are class A — already in the fetched table and already in the
bundle, so they are confirmation rather than new data.

**`Availability` already has the shape recommended for this.** Its `days` is
`Set<DayOfWeek>` where **empty means every day**, so a permanent stage needs no
declaration at all and `ALWAYS` is the default. No change needed.

**So `KornblumeAdapter` emitting `Availability.ALWAYS` for every source is
correct, not a gap.** The eleventh session recorded rotation as "data the
upstream fails to publish". It is not: **there is nothing to publish.** The
upstream is silent because the game has nothing to say.

What follows from that:

- The rotation machinery in `EnergyMip` — the subset capacity rows — has **no
  R1999 instance and never will**. It is proven on the acceptance fixture and
  waits for a game that rotates. Punishing: Gray Raven (Phase 11) is the next
  chance.
- `EXACT_ROTATION_GROUPS = 6` will not bind on this game. The concern that four
  Insight families plus two Resource stages might need seven distinct
  restrictions was misplaced: they all share one.

## 2. Server schedule — real, and not currently in the model

| | |
|---|---|
| Region | Global |
| Timezone | **UTC−05:00** |
| Daily reset | **05:00 local = 10:00 UTC** |
| Weekly reset | **Monday** 05:00 local |

`EnergyMip.matchingDays` currently reads weekdays in **UTC**, which is a game
assumption living in a game-agnostic module. It is inert today — nothing in
R1999 rotates, so the function is never consulted for it — but it is wrong by
default and the fix is data, not code: a reset zone and hour belong on the game
definition, which means a bundle field, a parser change and a Flyway migration.
Deferred deliberately; see the tracker's next actions.

## 3. Activity — regeneration and items are different things

- **Regeneration: 1 Cellular Activity per 6 minutes = 240 per day**, 1 680 a
  week. This is what `SolveRequest.energyPerDay` means, and **240 is the value
  `RealUpstreamPlanTest` and `CommunityBenchmarkTest` have been using all along**
  — so that number now has a source rather than being a plausible round figure.
- **Picrasma Candy restores 60 Activity; a Jar of Picrasma Candy restores 120.**
  These are *items*, earned and spent, and must never be folded into the
  regeneration rate.

**The model cannot express the second one.** Energy is not an item: `Stage` is
the only source that touches the energy budget and it only ever *consumes*. An
item that converts into Activity has no representation, so a plan cannot decide
whether to spend a candy. See the tracker.

## 4. "Free income" in this game is not free — it is a rebate on farming

This is the sharpest correction and it invalidates the obvious modelling.

**Daily Activeness** hands out Clear Drops and Wilderness Shells. **Weekly
Activeness** hands out Clear Drops, Wilderness Shells, Picrasma Candy (the Jar
specifically), Penumbra Can (a completion reward), **Re-Roar ×2** and **Track of
the Lost**, depending on the reward tier. The Re-Roar grant was added by the
post-3.2 system and exists to recover missed Roaring Calendar rewards — it is a
recovery token, not Activity income, and must be represented as its own item
rather than folded into an energy figure.

It is tempting to enter all of this as `Reward(DAILY, …)` and
`Reward(WEEKLY, …)`. **That would be wrong**, because some of the objectives that
earn Activeness *require spending Cellular Activity*. The income is conditional
on the farming, so a solver told it is unconditional would:

1. subtract the grants from the demand,
2. never pay for them,
3. and return a plan that is systematically **too cheap** —

which is the expensive direction to be wrong in, and silent.

`Reward` has no way to say "this arrives only if you spend N energy". Expressing
it needs either a precondition on the reward — the suggested field is
`requiresActivitySpend: boolean`, though a boolean only *flags* the problem and a
solver needs the quantity to price it — or a source that consumes energy and
produces items, a shape the model does not have. **Until it does, entering
Activeness as free income is a bug, and leaving it out is correct.**

**The Roaring Month is paid**, so it is not free income and does not belong in a
`Reward` whatever else it is. For the record, since somebody will ask: 300
Crystal Drops on purchase, then 90 Clear Drops and one time-limited Picrasma
Candy per day for 30 days — **300 Crystal Drops, 2 700 Clear Drops and 30 candy**
if every day is claimed.

**Event and mail grants** — for example 3.5's *Gift of the Stars*: 600 Clear
Drops, 5 Jars of Picrasma Candy, 10 000 Roar Decibel — are genuinely
unconditional, and are exactly what `Cadence.EVENT` with an availability window
is for. They are also one-off, which is why `EVENT` counts **once** however long
the horizon.

## 5. Shops — more data exists than the upstream table suggests

The tenth session concluded from `shops.json` that shop data was unusable. That
conclusion was right *about that file* and wrong as a statement about the game.
Six permanent shop structures exist, several with published prices and purchase
limits:

| Shop | Currency | Reset |
|---|---|---|
| Pawnshop — Bass Counter | Tracks of the Lost | rotating / permanent stock |
| Pawnshop — Treble Counter | character-specific | rotating stock |
| Fragment Shop — Psychube Shop | Thoughts in Entirety | permanent + monthly |
| Fragment Shop — Oneiric Shop | Oneiric Fluid | weekly + monthly + permanent |
| Wilderness Shop | Wilderness Shell | permanent / rotating |
| Teller Machine | Crystal / real money | offer-specific |
| Brainwave Shop *(later system)* | Primordial Pattern | mode / event-specific |

**`Shop` cannot represent half of these.** Its cap is
`(int periodLimit, Period period)` — "n per period", with `0` meaning
unlimited. A **permanent stock** — "five, ever" — is a lifetime limit and has no
`Period`. A real-money offer has no in-game currency at all. Both would have to
be forced into a shape that lies about them.

So the refusal stands, and its reason moves again: not "no time axis" (fixed),
not "no data" (there is some), but **the cap does not have the right shape and
the prices need verifying against the client rather than inventing**. The
maintainer's suggested shape is on record:

```
ShopOffer { shopId, currency, price, offer[], limit|null,
            limitPeriod: NONE|DAILY|WEEKLY|MONTHLY|PERMANENT|EVENT,
            availability, provenance }
```

## 6. A discrepancy nobody has resolved

**The pinned snapshot may not be the version its label claims.**

`fetch-upstream.sh` pins `8b40541a9c42`, whose commit message is `update 3.5`
and whose date is **2026-03-17** — verified through the GitHub API, not assumed.
The maintainer's references put **Global 3.5 at 2026-05-28 to 2026-07-02**.
Those are two and a half months apart.

The likeliest explanation is that Kornblume tracks the **CN** release, which
runs ahead of Global. If so, everything this project calls "3.5" is CN 3.5, and
the community guide it is benchmarked against may be describing a different
content set under the same number.

**This is not asserted, it is raised.** It is the same class of error as the
stale stage table that cost five sessions — a number that looked right and was
attached to the wrong thing — and it is carried in the tracker as an open
question rather than being quietly assumed either way.
