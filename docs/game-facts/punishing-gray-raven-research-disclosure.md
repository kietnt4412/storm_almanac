# Punishing: Gray Raven — the research pools, read off the client

**Working note.** Block 1 of [N27](../../TRACKER.md#next-actions) — the banner
reading — is **complete**, and block 2 (feeding) is done for the weapon. Block 3
(an event with its shop) is not started. **Block 4, the timed authoring pass, was
cut by the maintainer on 2026-09-18** — see *the cut* at the end. Read 2026-09-18.

This is the launch title's first first-hand data
([D3](../../TRACKER.md#d3--launch-title-swapped-to-punishing-gray-raven-2026-09-13)).
Before it, PGR had no facts in this repository at all.

## Provenance

| | |
|---|---|
| **Client** | English-language client, i.e. Global |
| **Patch** | **"Steering By Light"**. PGR records patches **by name, not number** — the maintainer's report; the name appears on the home screen |
| **Origin** | `PUBLISHER_DISCLOSURE` for everything under *the two pools* — it is the client's own Rules and Drop Details panels. `OBSERVED_IN_GAME` for the currency and energy readings |
| **Read** | 2026-09-18. The maintainer took screenshots; a Claude Code session transcribed the numbers below from them. **No screenshot is committed** — game assets are not allowed in this repository |
| **Account** | Commandant level 105. Account name and ID deliberately not recorded |

Two origins are kept separate throughout, because they are different claims:
**read off a screen**, and **reported by the maintainer from play**, which is
neither and is labelled as such every time.

PGR calls a character an **Omniframe** and a pull a **research attempt**.

---

## The two pools — both fully disclosed

The themed character banner has **two pools**, and they differ in both base rate
and guarantee mechanism while advertising the same overall rate.

| | **Arrival Construct** | **Themed Construct** |
|---|---|---|
| Also covers | **Fate Arrival Construct** | **Fate Themed Construct** |
| S-Rank base rate | **0.50%** | **1.50%** |
| Guarantee | **Hard: S-Rank within 60 attempts** | **Floating: a value drawn uniformly 80–100** |
| Counter as displayed | `0/60` | `8/80~100` |
| **Featured rate in S-Rank pool** | **70%** | **100%** |
| Per-10 floor | A-Rank or above every 10 attempts | same |
| Combined, as stated | **1.90%** | **1.90%** |
| Read on | Selena: Pianissimo, 09/17 – 10/01 06:59 | Karenina: Effulgence, 08/19 – 09/23 23:00 |

**The two axes pair; they are not independent.** All three of *70% featured*,
*0.50% base* and *the 60 counter* were read off the **same** banner screen, which
is what settles it — there are **two banner archetypes to model, not four**. The
maintainer adds that a **Fate** pool shares its sibling's drop rates, and that the
patch's themed character is the one moved into the 100% pool.

*One loose end, recorded rather than smoothed over:* the inheritance example text
differs between the two panels — `[Arrival Construct Pool]` on one,
`[Arrival Event Character Pool]` on the other — and the second name does not match
the "Themed Construct" label in the sidebar. Most likely generic example text, but
it is not the same word, so it is not treated as one.

**The Floating Guarantee, in the screen's own terms:** a value is generated
randomly between 80 and 100; if the value is 90, an S-Rank Omniframe is guaranteed
within 90 attempts; **the value is generated again once an S-Rank has arrived.**
The counter shows the *range*, not the drawn value — so **the player is not told
their own threshold.**

**Research Pool Inheritance.** The guarantee is inherited between pools of the
same type **"(Calibration included)"** — when a pool ends, its guarantee progress
carries to the next pool of that type, and so does a pending Calibration.

**This is the exact opposite of R1999's rule.** There, a limited banner's progress
*and* its earned guarantee are cleared when the event ends, which is why every
R1999 limited banner starts a player at `PityState.fresh`
([the R1999 note](reverse-1999-summon-disclosure.md)). PGR carries both forward.
So `PityScope.BANNER` is wrong here on both counts: the scope is the pool *type*,
and it spans pool instances rather than resetting between them.

### The featured rule — a per-banner rate, not a constant

**Corrected 2026-09-18, within the same reading.** The first banner seen was read
as "PGR guarantees the featured unit", and that was over-generalised from one
screen. It is a **per-banner value**:

| Banner | Research type | Window | **Rate in S-Rank pool** |
|---|---|---|---|
| Karenina: Effulgence | Themed Research | 08/19 – 09/23, 23:00 | **100%** |
| Selena: Pianissimo | Arrival Target / Arrival Construct | 09/17 – 10/01, 06:59 | **70%** |

The label sits above the *Switch* control on the banner screen, and the maintainer
reports **this line is what the Event Rules amount to** — the mechanism is
constant across banners and only the percentage moves.

**At 100%** the S-Rank pool is entirely the selected target: `chanceAtHit` = 1.0,
and there is no loss to recover from. Read twice — the banner label, and the
*Switch* panel's "Selected Member Rate Up … Rate in S-Rank pool: 100%".

**Below 100%, the recovery is published and named: the Calibration System.** From
the Basic Rules panel on the 70% banner — upon getting an S-Rank Omniframe other
than your target, Calibration triggers and **guarantees your target on the next
S-Rank Omniframe obtained**, with pool inheritance rules applying. So
`chanceAtHit` = 0.70 with `guaranteeAfterLoss` = 1.

*(First recorded as the maintainer's report, then found on the screen the same
day. It is `PUBLISHER_DISCLOSURE`, not an observation.)*

**So the second-hand sources were not simply wrong**
([prior-art §7](../prior-art.md#7-punishing-gray-raven--surveyed-2026-09-14)): one
said a loss carries a guarantee and one said it does not, and **both describe real
PGR banners**. The fixtures assuming a guarantee are modelling a sub-100% pool.
The lesson is that a PGR banner cannot be described without its own rate.

### Drop Details — identical on both pools except the S-Rank rows

| Outcome | Rate |
|---|---|
| S-Rank Omniframe (Including the Guarantee) | **1.90%** |
| S-Rank Omniframe (Base Drop) | **0.50%** / **1.50%** — the only row that differs |
| A, B-Rank Omniframe | 13.95% |
| Construct Shard | 22.11% |
| 4★ Equipment | 28.39% |
| Overclock Material | 14.42% |
| EXP Material | 4.81% |
| Cog Box | 14.42% |

## The rate does not reproduce, and that is the finding

R1999's disclosure **confirmed its own curve**: the stated overall 6★ rate of
2.36% came back out of the stated pity curve as 2.3592%, a check a curve one pull
off would fail. **PGR's does not.** Computed as a long-run share — pulls until a
hit or the guarantee, averaged over the cycle:

| Pool | Stated base | Stated guarantee | **Computed overall** | **Stated overall** |
|---|---|---|---|---|
| Construct | 0.50% | hard at 60 | **1.925%** | 1.90% |
| Event Character | 1.50% | floating 80–100 | **2.021%** | 1.90% |

To land on 1.90% the Construct pool would need `hardAt = 61` or a base of
0.4537%; the Event Character pool would need a 94–114 window or a base of 1.327%.

**What points away from "the publisher is wrong".** The non-S rows sum to
**98.10%**, and 98.10 + **1.90** = exactly **100.00**. The table is normalised on
the *combined* figure, not the base drop — so Drop Details is a **long-run outcome
share, not a per-pull probability vector**, and 1.90% is the number the pools were
designed to hit. The Event Character pool's own text says so: the overall chance
is *"the same as other pools"*. Two different mechanisms, one advertised headline.

**The likeliest gap is in our model of the mechanism, not in the disclosure.** The
obvious untested candidate is the **A-Rank-or-above floor every 10 attempts**,
which the calculation above ignores entirely and which consumes outcomes.

**This is not a blocker for authoring.** Every number above is recorded as read.
It is a question to settle against the engines, and it is the PGR equivalent of
the R1999 check — a disagreement found by doing the arithmetic rather than
transcribing.

## What this costs the gacha engines

**`PityRule` has no shape for a floating guarantee.** The record is
`(rarity, hardAt, softFrom, softJumpTo, softStep)`; PGR needs `hardAt` to be a
random variable, drawn uniformly per cycle and resampled on every hit. Under
[ADR 0018](../adr/0018-the-gacha-engines-answer-one-question-about-one-rarity.md)
`MarkovBannerEngine` is an exact chain over `(pulls since hit, losses carried,
copies held)`; modelling this exactly means carrying the drawn threshold in the
state, or mixing 21 chains. `MonteCarloBannerEngine` needs only an extra draw.

**`PityScope` needs a pool-type scope, and it must carry the loss too.** Progress
is inherited by the next pool of the same type — which is neither `BANNER` nor
anything the enum has — and the rules say **"(Calibration included)"**, so a
pending guarantee crosses the boundary with it. `PityState` already **counts
losses rather than flagging one**
([ADR 0018](../adr/0018-the-gacha-engines-answer-one-question-about-one-rarity.md)),
so the state has somewhere to put this; what is missing is a scope that does not
throw it away.

**This is the first game whose scope R1999's cannot be reused for.** R1999 clears
at event end and PGR inherits, so the two published titles now disagree on the one
question `PityScope` exists to answer — which is the best possible argument that
it deserved to be data rather than an assumption.

**`featured` is exercised for the first time by a real game.** `chanceAtHit` takes
1.0 on themed banners and 0.70 on Arrival ones, with `guaranteeAfterLoss` = 1
where the rate is below 1.0 — so both halves of the record finally carry weight,
and a PGR banner cannot be authored without reading its own rate.

**No new rarity is needed.** S / A / B fits `(label, rank)` as it stands.

## What a pull costs — the chain N28 was blocked on

[N28](../../TRACKER.md#next-actions) needed a banner to declare **which item is
pull currency and what a pull costs in it**. Both are now read:

- **1 pull = 250 Event Construct R&D Tickets** *(screen; 2500 for ten, so no
  ten-pull discount)*
- **1 Black Card = 1 Event Construct R&D Ticket** *(maintainer's report)*
- **1 Rainbow Card = 10 Black Cards** *(screen, all seven tiers)*
- **119 Rainbow Cards = $19.99** *(screen)*

Composed: **250 Black Cards a pull**, **25 Rainbow Cards a pull**, about
**$4.20 a pull and $42 a ten-pull** — a plausible gacha price, which is itself a
weak check on the 10:1 exchange.

**Four pools, each with its own ticket and counter**, all priced 250 / 2500:

| Pool | Ticket currency | Counter | Floor per 10 | Pool split |
|---|---|---|---|---|
| Event character | Event Construct R&D Ticket | S-Rank 0/60 or 8/80~100 | A-Rank or above | **100%** in S-Rank pool |
| Target weapon | Target Weapon R&D Ticket | 6★ 2/30 | 5★ or above | 80% in 6★ pool |
| CUB | CUB R&D Ticket | S-Rank 0/20 | A-Rank or above | 80% in S-Rank pool |
| Basic weapon | Basic Weapon R&D Ticket | 6★ 3/30 | 5★ or above | not shown |

---

## Feeding — the weapon, partially read

Block 2 of the reading order, **in progress**. A weapon carries **four** separate
upgrade systems, not one, and they consume different things.

### Enhance — levels, paid in EXP

Read off a weapon at **45/45** with the target slider on *Highest*:

| | |
|---|---|
| Level shown | **45 / 45**, with ATK 50 » 436 and CRIT 25 » 249 |
| **Enhancement Cost** | **EXP 24 000** |
| Side cost | **340 000 Cogs** |
| Fodder is chosen by rarity | filter buttons **5★** (greyed), **4★**, **1-3★**, **Item Material** |

So EXP is the progress unit and fodder is **selected by rarity band** rather than
by item. The 24 000 is the **total** for the slider's target, not a rate.

**`progressPerUnit` is read: one 4★ EXP material gives 300 EXP.** Off the
Enhancement Cost picker with exactly one unit selected — *"Obtained EXP 300"*,
against 1679 owned. The same screen estimated the result as **Lv 6**, so ~300 EXP
carries a fresh weapon from Level 1 to Level 6 — a first point on the level curve,
though the curve itself is unread.

**Fodder is not only materials — it is weapons.** The picker's grid is largely
**4★ weapons at Level 1**, offered alongside the stacked EXP material. So a weapon
is simultaneously an upgradeable entity and a consumable, which the domain model
has no precedent for: R1999 fodder is always a material. The same is true on
Resonance, whose picker has an explicit **Weapon** tab.

**And they are worth the same.** One 4★ *weapon* fed as fodder also reads
*"Obtained EXP 300"* — identical to the 4★ material. So `progressPerUnit` is a
function of **the fodder's rarity band alone**, not of whether the fodder is a
material or a weapon. That is one rule rather than two, and it is the single
biggest simplification block 2 produced.

*Rarity scaling is still unconfirmed.* The 5★ / 4★ / 1-3★ filter implies the EXP
value moves with the fodder's rarity, but only the 4★ figure has been read — the
maintainer holds no 3★ EXP material to compare against.

### Overclock — raising the level *cap*, paid in named materials

A separate cost block on the same screen, consumed alongside Enhance. Four
materials, each with a quantity required:

| Material | Rarity | Owned / needed | What the item says |
|---|---|---|---|
| Major Overclock Alloy | 4★ | 644 / **16** | "Used to exceed equipment level limit for **high-rank** equipment" |
| Weapon Overclock Core II | 4★ | 379 / **16** | "Key material used to increase weapon level limit. Useful for **high-rank** equipment" |
| Minor Overclock Alloy | 3★ | 3114 / **20** | "Used to exceed equipment level limit for **low-rank** equipment" |
| Weapon Overclock Core I | 3★ | 868 / **28** | "Key material used to increase weapon level limit. Useful for **low-rank** equipment" |

**This is not fodder — it is an upgrade with a fixed recipe**, and it maps onto
`upgrades` with `costs`, exactly like an R1999 insight step. The EXP half maps
onto `Fodder`. **A weapon therefore needs both shapes at once**, which is not how
either was designed.

### Harmony — a tiered upgrade, paid in one material

| | |
|---|---|
| Cost of Level 1 | **25 Harmony Accelerator** (6★), 96 owned |
| What Level 1 grants | "Harmony Absorption: Piece count for Memory Set effects **+2**" |
| Levels 2 and 3 | shown as **"Not available"** — locked, cost not displayed |
| Gate it opens | "Upgrade to Lv.1 to unlock **Memory Bind**" |

A clean `upgrades` chain with opaque state labels — the shape the model already
has. **The locked tiers matter**: a cost that is not displayed until unlocked
means a bundle cannot be authored complete from one reading.

### Resonance — three slots, fed with materials *or weapons*

The panel is **member-bound** (read against *Helentine: Lacrimosa*), has **three
empty slots**, and each takes *"Spend materials or weapons"* followed by
*"Choose Skill"*. The consume picker offers a **Weapon** tab and an **Item** tab —
so **weapons are themselves fodder here**, which no other system in this model
does.

**Resonance is deterministic, and this corrects the tracker.**
[N30](../../TRACKER.md#next-actions) records Resonance as *"PGR's other new shape —
probabilistic goals"*, which demand resolution refuses by name. **It is not
probabilistic.** Read on a maxed weapon: the three slots hold named skills
(*Dead Line Timing*, *Glorious Afterglow*, *Incandescence*), each marked **Bind**
with a **Swap Resonance** control; and consuming a weapon opens a list of that
weapon's own named skills — for *Pianissimo Selena*: Matrix Lightning, Shock
Saturation, Nsec Transmission, Shock Echo — from which **the player picks one**
and confirms. Nothing is rolled.

So Resonance is an ordinary `upgrades` step whose cost is a consumed weapon and
whose `toState` is the chosen skill. **N30 does not need probabilistic goals for
launch on this account**, which removes the largest speculative item from its
scope. *Residual, and smaller:* whether the offered skill list is fixed by the
consumed weapon's identity — the panel is headed with that weapon's name, which
suggests it is.

### The character — structure only, no costs yet

Read off *Helentine: Lacrimosa*, BP 422. **Four upgrade axes plus three equipment
slots**, and none of their costs have been opened:

- **Train** — the level axis, shown as **Lv. 1 / 80**.
- **Evolve** — a rank ladder. The roster shows ranks **A → S → SS**, and this
  character's Evolve panel displays **SS** as its target. *Note the gacha pools
  speak of S / A / B ranks, so **SS is reached by Evolve, not pulled** — the
  rarity a unit is acquired at and the rank it can reach are different facts.*
- **Awaken** — a third axis, unopened.
- **Phylotree** — a fourth, unopened.
- Equipment: **Weapon** (Hear the Bell, Lv 1), **CUB**, and **Memory** ("Memory
  not equipped", so the Memory system is unread).

This is the same picture as the weapon: several independent ladders on one entity
rather than a single chain. `upgrades` handles that — `fromState`/`toState` are
opaque strings and nothing requires one sequence per entity — but a PGR bundle
will carry noticeably more upgrade rows per character than R1999's does.

### What is still missing from the weapon

1. **Whether EXP scales with the fodder's rarity.** The 5★/4★/1-3★ filter implies
   it does; only the 4★ value (300) has been read, and it is the same for a
   material and for a weapon.
2. **Overflow.** Untested — though the screen's framing of EXP as a pool that is
   *Obtained* and then converted into an estimated level is weak evidence that it
   banks rather than truncates.
3. **The level curve.** One point is known (≈300 EXP reaches Lv 6 from 1) and
   24 000 is the total to the top; nothing between.
4. **The Memory system is entirely unread** — the character screen showed "Memory
   not equipped", so the third of block 2's three feeding systems has not been
   opened at all.
5. **The character's four axes** — Train, Evolve, Awaken, Phylotree — have their
   shapes but none of their costs.

---

## Energy — read, and it checks itself

**Serum, measured in millilitres.** Cap **240 ml** at level 105, observed at
37/240 and 39/240.

**The regeneration rate is printed on screen**, as a live countdown on the
injection panel: **"1ml of Serum recovered in 05:56"**. The maintainer reports the
period as **6 minutes**, which the countdown is consistent with, and that
**regeneration stops at the cap — no overflow**. They also report the cap **rises
with account level**, so 240 is their cap and not necessarily the game's.

**The two numbers agree, and the identity is exact.** 1 per 6 minutes is 10 per
hour is **240 per 24 hours**, which is precisely the cap: the cap is exactly one
day of regeneration. So `energyPerDay` is **240** for a player who drains it
daily — and strictly less for one who does not, because the cap truncates rather
than banks. A planner that assumes 240 is assuming a daily login.

**Serum can also be injected from items.** One "1 day(s)" supply item injects
**60 ml**. Those items carry their own expiry windows — stacks labelled 1 through
6 days and 1 week — so a serum item is a *dated* resource, not a standing one.

*Not yet established:* the cap-by-level schedule, and whether injection can push
past the cap.

## Currencies — read

- **Cogs**, 13 317 678. Converted from 4★ Supply crates; one conversion screen
  read **200 000 Cogs obtained**. *Ambiguous:* the screen showed one crate
  selected of 66 owned and a second stack of 76, so the per-crate rate cannot be
  derived from it.
- **Black Cards**, 19 323. **Rainbow Cards**, 2.

**Black Card Exchange**, all seven tiers: **10 / 50 / 280 / 340 / 590 / 710 /
1190** Black Cards for **1 / 5 / 28 / 34 / 59 / 71 / 119** Rainbow Cards — exactly
**10 Black per 1 Rainbow** throughout. The **first purchase of each tier doubles
it**.

**Recharge**, real money: 5 RC $0.99 · 28 $4.99 · 34 $5.99 · 59 $9.99 ·
71 $11.99 · 119 $19.99.

## Reported by the maintainer, not read off a screen

Recorded as their observation. Useful for knowing what to go and read; not
publishable as-is.

- PGR has **seven currencies**.
- **Black Card** is the main currency spent on event and skin gacha, and
  **exchanges 1:1 into Event Construct R&D Tickets**.
- **Rainbow Card** is the purchased one; it converts to Black Cards, and buying a
  skin directly requires Rainbow Cards.
- **Event Construct Ticket** pulls the current event character; **Basic ticket**
  pulls A-rank members; **Basic Weapon ticket** pulls non-event weapons; plus the
  **Weapon** and **CUB** tickets.
- Serum's cap rises with account level; it refills 1 per 6 minutes and stops at
  the cap.
- The *Switch* control selects which target the pull is aimed at.

## Closed — the one contradiction this reading raised

**Rainbow → Black conversion rate.** Reported as 1:1, read off the screen as
10:1 on all seven tiers. **Resolved 2026-09-18: the maintainer confirms the 1:1
was theirs and belonged to the Black → Event Construct exchange.** So
rainbow→black is **10:1** and black→construct is **1:1**.

Worth keeping rather than deleting, because of *how* it surfaced. The screen and
the report disagreed; the price arithmetic independently favoured the screen — at
1:1 a ten-pull works out near $420 — and the reader was asked rather than the
cheaper number quietly chosen. That is the procedure ADR 0015 is for, and it
caught a real error on the first reading it was applied to.

## Gaps

1. **The 1.90% discrepancy above** — settle it against the engines, starting with
   whether the per-10 A-Rank floor closes the gap.
2. Whether the selectable target list is ever longer than one entry, and whether
   switching the target resets the counter or the pending Calibration.
3. What the **Weapon**, **CUB** and **Basic** pools state in their own rules —
   their counters (30, 20, 30) and pool splits (80%) are read, their base rates are
   not.
4. Block 3 of the reading order: one event with its farming stage, its currency,
   and its shop with prices and purchase limits.

## The event — and it is not shaped like a farm

Block 3, read on **Blazing Rhapsody**, the patch's main event, *Remaining Time:
4 day(s)*.

**The finding: PGR's event rewards are one-time mission completions, not
repeatable energy-priced runs.** The stages cost **no Serum**. Each grants a fixed
quantity once, and the gate is *capability*, not currency.

**The stage map** — eight stages, each with an objective counter and a fixed
Soundwave Coin grant:

| Stage | Objective | Grant |
|---|---|---|
| 01, 02 | 3/3 | 50 Soundwave Coin each |
| 03, 04 | 9/9 | 50 each |
| 05, 06, 07, 08 | 9/9 | 100 each |

Plus, on the map: *"Cleared waves will also be converted into token rewards at
settlement."*

**The missions**, split **NORMAL** and **CHALLENGE**, each a one-shot with three
fixed rewards:

| Mission | Condition | State |
|---|---|---|
| Underground Soundcheck | Clear Stage 01 | 1/1, Claimed |
| Rock the Block | Clear Stage 02 | 1/1, Claimed |
| Making Noise | Clear Stage 03 | 1/1, Claimed |
| Encore Time | **Reach Endless Wave 11** when clearing Stage 04 | 0/1, Incomplete |
| Platinum After Dark | **Reach Endless Wave 11** when clearing Stage 08 | 0/1, Incomplete |
| No More Awkward Dancing | Defeat 6 Sharky: Fixers | 6/6, Claimed |

Quantities are fixed and deterministic — 125/100/20, 125/100/60, 100/200/20 and so
on. **No sampling, no yield, no variance.**

**Soundwave Coin**, the event currency, 75 owned: *"Coins can be used to enhance
character stats in out-of-battle training."* **It buys nothing.** The *Upgrade*
screen is a node tree in phases — Phase 2, Phase 3 — whose nodes are event-local
combat buffs, e.g. *Showtime: Fever Duration +2s, Fever DMG +50%, Fever Armor +6,
Fever Lifesteal Efficiency +1%*. Confirmed by the maintainer: the coin upgrades
character power for the event, and is not a purchasing currency.

**So the event currency is a capability sink, not a material source** — and that is
a dependency this planner has no concept of. The real loop is: *spend event
currency on combat power → clear harder stages → satisfy gated missions → receive
one-time rewards.* Nothing in `EnergyMip` models combat capability, and nothing
should: **the tractable scope is to treat "can this player clear stage N" as an
input the reader supplies, not something the solver derives.** That is a scoping
decision N30 should make deliberately rather than discover.

### Why this breaks the optimizer's assumption

`EnergyMip` models a stage as *run it N times at energy cost C for expected yield
Y*, and the objective is least energy. **None of those terms exist here.** There is
no energy cost, no repetition, and no expected yield — there is a fixed grant, once,
behind a skill gate, inside a four-day window.

That is a different kind of node entirely: closer to a **one-time reward with a
precondition** than to a stage. It is not `Reward` either, whose shape is a
*cadence*. And because the constraint is the closing date rather than Serum, the
`LEAST_ENERGY` objective has little to say about an event at all —
**`FEWEST_DAYS` against `Availability.closesAt` is the question a PGR player
actually has.**

### It also contradicts the guides, for the second time

[Prior-art §7](../prior-art.md#7-punishing-gray-raven--surveyed-2026-09-14)
recorded every guide agreeing that PGR is farmed through *"event stages whose
currency buys materials in event shops"*, and **N30 was scoped from that**. On this
event the currency buys a training buff, and the materials come from one-shot
missions. **No shop was seen at all** — the event menu offers Codex, Character,
Mission, Upgrade and Battle.

**The maintainer states this holds for every PGR event, not just this one**, on
both counts: *"mostly event method to earn are the same — do mission to get
reward, not cost anything"*, and **"event in PGR don't have shop"** — *"every event
is so, not only this."* Blazing Rhapsody is the instance that was read; the
generalisation is their report.

**This is the second time the client has contradicted the second-hand record**, the
first being the featured-rate rule. It is a direct argument for
[ADR 0015](../adr/0015-game-data-is-sourced-first-hand-not-adapted.md).

**N30's shop half should be dropped.** Its premise — that PGR is farmed through
event stages whose currency buys materials in event shops — came entirely from
guides, and the maintainer states flatly that PGR events have no shops. What
`EnergyMip` refuses today it can go on refusing for PGR's launch.

*What survives:* **R1999 does have a shop** — the Limited Shop, where 200 Cassettes
of the Lost buy Rhiannon and cassettes cut six copies from 840 pulls to 560
([the R1999 note](reverse-1999-summon-disclosure.md)). So the **shop-exchange field
in [N28](../../TRACKER.md#next-actions) is still owed**; it is the *solver-side*
shop work in N30 that PGR does not justify.

### Open on the event

1. **What the mission reward items are.** The art matches the Event Construct R&D
   Ticket (orange, *Limited*) and a gold ticket resembling the Black Card — which
   would make event missions a **pull income source** and feed `IncomeModel`
   directly. **Not confirmed; the icons were not opened.**
2. **Does any PGR event have a materials shop?** N30 assumes so on guide evidence
   that has now failed once.
3. What **Upgrade** on the event menu does, and whether it is the Soundwave Coin
   training sink.
4. Whether the "cleared waves converted into token rewards at settlement" path is
   repeatable, which would be the only farm-shaped thing here.

## The cut — block 4, the timed authoring pass

**Cut by the maintainer, 2026-09-18.** N27 as written asked for one stage, one
item and one character step to be read **timed**, because ~2 700 catalog facts a
patch multiplied by an unmeasured per-fact cost is the project's largest
unquantified risk. The maintainer's decision is not to measure it.

Recorded here so the gap is visible rather than forgotten: **the cost of
first-hand authoring remains unknown**, and any later claim that a patch is
affordable to source is an estimate with nothing behind it. The three readings
themselves are still worth having as ordinary facts; it is only the stopwatch that
is dropped.
