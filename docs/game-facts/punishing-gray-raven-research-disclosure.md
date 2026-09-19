# Punishing: Gray Raven — the research pools, read off the client

**Working note.** Block 1 of [N27](../../TRACKER.md#next-actions) — the banner
reading — is **complete**, and block 2 (feeding) is done for the weapon. Block 3
(an event) is done too, and found that PGR events have no shop to read —
*the event* below. **Block 4, the timed authoring pass, was cut by the maintainer
on 2026-09-18** — see *the cut*. Read 2026-09-18. **Authored into a bundle the
same day** — *authored, and the format refused three things*, at the end.

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

---

## Authored — and the format refused three things

**2026-09-18, same session.** The reading above was turned into
[`data/bundles/punishing-gray-raven-steering-by-light.json`](../../data/bundles/punishing-gray-raven-steering-by-light.json)
and taken through *validate, preview, ingest*. **It is the first bundle in this
repository whose every fact is first-hand** — nine facts, `3 PUBLISHER_DISCLOSURE`
and `6 OBSERVED_IN_GAME`, and the provenance breakdown prints no
`<- NOT ours to publish` line for the first time on anything that is not a
synthetic fixture.

What landed: five Overclock and Harmony materials with their read rarities, two
S-Rank Omniframes, **the Arrival Construct banner archetype in full**, and the
weapon EXP fodder rule at 300 per 4★ unit.

**Nine facts out of a reading this long is the finding.** Three things were read
clearly and could not be written down, and each names a specific gap:

1. **The Themed Construct banner.** `PityRule.hardAt` is an `int`; PGR draws the
   threshold uniformly 80–100 per cycle and redraws it on every S-Rank. There is
   no number to put there that is not an invention, so the banner is absent from
   the bundle rather than approximated. **This is the floating-guarantee cost,
   now paid in data rather than argued in prose.**
2. **The weapon's Overclock recipe (16/16/20/28) and its Harmony Lv 1 cost (25
   Harmony Accelerator).** An `Upgrade` must name the entity it advances, and the
   weapon these were read off was at 45/45 with its **name not recorded**. The
   numbers are here in this note and are one screenshot away from being
   authorable. *Cheapest thing to fix in the next reading.*
3. **Every currency** — Cog, Black Card, Rainbow Card, Event Construct R&D
   Ticket, Soundwave Coin. `Item.rarity` is required and **no screen that was
   read grades a currency**. That is not a gap in the reading; it is the format
   asserting that everything in an inventory has a rarity, which a currency may
   simply not have. Worth deciding deliberately before **N28**, which needs the
   ticket to exist as an item.

Two smaller shapes were bent rather than broken, and both are recorded in the
bundle's own comments:

- **The fodder band is carried by `consumesCategory`, not by `minimumRarity`.**
  `Fodder` has no upper bound, so `minimumRarity: 4★` would claim that 5★ fodder
  also gives 300 — and the 5★/4★/1-3★ filter buttons are evidence it does not.
  The category `weapon-exp-fodder-4-star` is the band. **The rule is inert**: no
  item carries that category, because the fodder stack's own item name was not
  recorded either.
- **No availability window on the banner.** The dates 09/17 – 10/01 06:59 were
  read; the **zone they are in was not**, and `Availability` takes instants.
  Writing UTC would be inventing a day boundary — which is exactly
  [N20](../../TRACKER.md#next-actions).

**Not published.** Ingest is a draft; `--gamedata=publish` is a human approval by
design, and the approval for the first first-hand version of a title belongs to
whoever did the reading.

```bash
java -jar backend/app/build/libs/storm-almanac.jar --gamedata=publish punishing-gray-raven 0
```

### One second-hand fixture the reading contradicts

`Banners.grayRavenFloating()` in the gacha tests is the "floating guarantee"
variant — 1.5% base, wall at 80, **featured 0.70**. The first-hand reading pairs
that 1.50% base with a **100%** featured rate, not 70%: the two axes pair, and
they were read off the same screen. The fixture is second-hand and is not wrong
about any rate it was given, but it describes a banner PGR does not appear to
run. **Left alone here** — correcting a gacha acceptance fixture is Phase 5 work
and moves a published-rates test, not an authoring step.

## 2026-09-19 — the weapon named, and the first farm route

Read off the same client and account, patch "Steering By Light". Screen readings
unless marked *(maintainer)*.

**The weapon is Hear the Bell**, 6★, base ATK 50 / CRIT 25 — the base stats the
Enhance screen showed on 2026-09-18, and confirmed by the maintainer as the weapon
the Overclock and Harmony screens were read on. **Overclock is one step** that
takes its level range to the maximum, 45 *(maintainer)*. Both recipes are now
bundle rows.

**The EXP fodder is Weapon Enhancer IV**, 4★ (graded on its shop tile; the card
shows none). The card's "Owned 999" is a per-stack display cap *(maintainer)*,
which reconciles it with the 1679 the picker read.

**Simulated Battlefield — PGR's first farm stage, and it is shop-shaped.**

| | |
|---|---|
| Serum cost | **30** a run, Auto Clear the same *(maintainer)* |
| Open | "Open all day"; no daily run cap *(maintainer)* |
| Yield | **164 Simulation Score** on one run, tile marked **Double** → **82** base |
| Bonus | "Daily 2x Reward Attempts 2/10" — a limited event, 10 doubled runs a day *(maintainer)* |
| Authority Level | 25, "Max Level Reached" — **whether it scales the score is unread** |

**Simulation Score** is graded **4★** on the Battle Results tile — the first
currency a screen has graded, so the first one the bundle can carry.

**Simulation Shop**, prices per stack, **no purchase limits** on the Material
tab *(maintainer)*:

| Item | Grade | Stack | Price |
|---|---|---|---|
| 6★ Memory Shard | 6★ | ×10 | 46 |
| 5★ Memory Shard | 5★ | ×25 | 41 |
| Overclock Material Box (α) | 3★ | ×10 | 38 |
| Overclock Material Box (β) | 4★ | ×10 | 150 |
| Memory Enhancer IV | 4★ | ×10 | 87 |
| Special Support Token | 4★ | ×100 | 136 |
| Weapon Enhancer IV | 4★ | ×10 | 262 |

The Memory tab sells 6★ Memory pieces one at a time (Catherine, Patton, slots
01–06) at **613** each. The Version Limited tab, *"Time Left for Limited Item
Redemption: 4 day(s)"*, sells the current themed character's Memory (Santiago
01–06, 6★, **539** each), Prototype Engine (5★, **49**, *Available: 20*) and dorm
furniture (**196**, *Available: 0*). So that tab **does** have limits;
*(maintainer)* it usually carries the themed rate-up character's Memory, dorm
items and affinity gifts.

**Overclock Material Box (β)** — one stack of ten opened into **3 + 3 + 4** of
three different 4★ materials, names not read; (α) is the 3★ equivalent
*(maintainer)*. **Ten boxes gave ten items across three kinds**, which looks like
one random material per box — unconfirmed, and it would make the box a
probabilistic source rather than an exchange.

### This corrects N30

N30 dropped shops because PGR's *event* shops did not exist. **This one is
standing, paid for in a currency a Serum stage yields**, so the route to weapon
EXP is **30 Serum → 82 Score, and 262 Score → 10 Weapon Enhancer IV →
3 000 EXP**. The solver needs shops for PGR after all — and it has no shops today.

### The day boundary, half read

The home clock shows **server time** *(maintainer)*: **00:04, Saturday 09/19**,
while the Resources menu counted down **"Reset Time: 04:58:18"** — so the **daily
reset is 05:00 server time**, inferred from two screens taken minutes apart.
**The server's UTC offset is still unread**, which is what N20 needs.

### Settled the same day

- **The server clock is UTC**, and the **daily reset is 05:00 UTC** — 12:00 in
  the maintainer's UTC+7 *(maintainer)*. It also checks: the home clock read
  00:04 server time, and the session's own machine clock read 07:14 UTC+7 ten
  minutes later. **This is N20's missing number.** Presumably the banner's
  "10/01 06:59" is UTC as well; its *opening* time was never read, so the banner
  still has no window.
- **The Simulated Battlefield score is fixed** — the maintainer runs it daily —
  and **Authority Level does not move it**: its panel lists damage buffs and
  Super Armor per level at EXP 55 / 80 / 105 / 130 / 155 / 180 / 200 for levels
  1–7, and *(maintainer)* affects only buffs and EXP gained. The yield is now a
  declared one.
- **Overclock Material Box (β) opens into a fixed split** *(maintainer)*: one
  purchase of ten gave **4 Major Overclock Alloy, 3 Weapon Overclock Core II and
  3 Memory Overclock Circuit II** (4★, read on the reward tile), and the tracked
  stacks moved 644 → 648 and 379 → 382 between readings, which agrees. Written as
  a craft over the stack of ten. The (α) box's contents are unread.

## 2026-09-19 — the character, and the two upgrade axes the model cannot hold

Read on **Helentine: Lacrimosa** (TBA-04) unless stated. Her menu has **Level
Up, Promote and Skill** tabs, plus Awaken and Phylotree elsewhere.

### Train (Level Up) — fodder, no side cost

Lv **1 / 80**, EXP 0/20. Each Pod selected alone from Lv 1:

| Pod | Grade | Owned | EXP | Estimated result |
|---|---|---|---|---|
| EXP Pod (M) | 3★ | 129 | **+1 000** | Lv 10 |
| EXP Pod (L) | 4★ | 398 | **+3 000** | Lv 17 |
| EXP Pod (XL) | 4★ | 30 | **+20 000** | Lv 37 |

**No Cog cost** (maintainer; none on screen). Three `Fodder` rows, **one category
per Pod**: L and XL share a grade and differ five-fold, so unlike weapon EXP the
value is *not* a function of the rarity band. The three estimates are the only
points on the level curve, so **a level goal cannot yet become an EXP demand**.

### Evolve — shards, and a shop with tiered prices

The Evolution screen ranks **S → SS → SSS → SSS+**, with sub-nodes inside a rank
(SS1, SS2; one shown granting *"Phase 3 DEF +5.00, Styx Reflection Lv1"*) and an
**Activate** button over **"Consume 2 / 9"** of her shard — so one node costs
**9 shards** and she holds 2. *(Maintainer:)* the cost is **the character's own
shards only**, obtained from **duplicates** or the **Phantom Pain shop**, which
sells **30 shards per character** for **500 Phantom Pain currency in total: the
first 10 for 100, the next 20 for 400** — 10 each, then 20 each — enough to
reach SS.

The tiered price **is** expressible: two `Shop` rows, a 10-unit cap at 10 and a
20-unit cap at 20, and a least-cost solver buys the cheap tier first unaided.
**What is not expressible is the cap's period** — a limit that never resets has
no `java.time.Period`. Duplicates are a gacha outcome, so shard supply is the
first place the gacha engines and the planner meet.

### Awaken — gates, not costs

Four tiers, each **Awaken Method** a set of *conditions*, not a price:

| Tier | Level | BP | Also | Reward |
|---|---|---|---|---|
| Elementary | 35 | 1 800 | — | coating (2★) |
| Advanced | 50 | 3 500 | — | coating (2★) |
| Ultimate | 65 | 5 000 | **Equip 6★ Resonance Skills ×12** | portrait (4★) + **Ultima: Lacrimosa** (5★: 3 Signal Orbs at battle start) |
| Infinitas | 80 | 6 500 | **Equip Hypertuned Resonance Skill ×12** | Halo upgrade: choose the orb colour, station in Sequence Pact (read on Liv: Limpidity) |

*(Maintainer:)* the first two are cosmetic. **`Upgrade` has costs and no
preconditions**, so none of this is a row. The real price of Ultimate is the
twelve Resonance skills, **paid on the equipment, not the character**: a goal on
one entity whose cost is spent on others. **BP is a derived combat stat**, which
no planner should try to model, so an Awaken goal can at best be *"meet the
item-denominated conditions"*.

### Phylotree

*(Maintainer:)* a codex, not an upgrade. Out of scope.

### Settled later the same day

**The level curve, bounded.** From Lv 1, **MAX on EXP Pod (XL) selects 25 —
+500 000 EXP — and reaches Lv 80 / 80.** MAX picks the fewest Pods that reach the
cap, so **the EXP from 1 to 80 is more than 480 000 and at most 500 000**. Not
exact yet; a level-80 goal written as 500 000 over-states it by at most 20 000.

**Promote is a separate axis from Evolve** *(maintainer)*: a rank ladder, read at
its first step — rank **PRIVATE**, one of four stars lit, preview HP +43 / ATK +8
/ DEF +6 / CRIT +3, **gated on "Member reaches Lv.2"**, **Consume 5 000 Cogs**.
A cost *and* a gate, like Awaken without the BP. The rest of the ladder is unread.

**Skill** — the leader skill *Leader – Fading Fern*, locked, Total Level 0:
**Unlock costs 3 Skill Points + 25 000 Cogs**. One node of a tree; the rest are
unread.

**Evolve, S → SS: 30 shards** *(maintainer)* — exactly the Phantom Pain shop's
lifetime allowance. The node read at "Consume 2 / 9" is one of the steps inside.

**Phantom Pain Scar** — the Phantom Pain shop's currency, 1 121 owned, *"Obtained
from Phantom Pain Cage"*, **a weekly mode** that paid **56** this week
*(maintainer)*. Not a bundle item yet: its card shows no grade.

**The Simulation Shop's Material tab, second page** — no tile shows *"Available"*,
which is how a limit displays elsewhere:

| Item | Grade | Stack | Price |
|---|---|---|---|
| Aura Chip | 4★ | ×10 | 123 |
| Aura Basic Unit | 4★ | ×10 | 123 |
| **EXP Pod (L)** | 4★ | ×5 | **103** |
| Cog Pack (XL) | 4★ | ×1 | 82 |
| **Skill Point** | 3★ | ×15 | **69** |
| **Cogs** | 3★ | ×1 200 | **1** |
| Support Skill Com… (cut off) | — | — | 107 |
| Support Overclock Bundle (S) | — | — | 67 |

**Cogs are graded 3★ on their tile**, so the project's most-used currency can be a
bundle item. With the stage, **every cost read on the character so far —
Level Up, Promote's first step, a skill unlock — is purchasable with Simulation
Score**, so one Serum stage and one shop cover the whole character except Evolve.

### The character rows, and one number the screen and the report disagree on

**Helentine: Lacrimosa was pulled at S rank** *(maintainer)*, so she is now a
bundle entity, with three upgrade rows: Promote step 1 (5 000 Cogs), the leader
skill unlock (3 SP + 25 000 Cogs), and one skill level — *Astral Armament*
[Core Passive] Lv 1 → 2, **1 Skill Point + 2 000 Cogs**, read on its button.
*(Maintainer:)* every skill level costs 1 SP + 2 000 and every unlock 3 + 25 000.
The skill level cap is unread — another node on the same screen stands at
**Lv 18** — so the ladder is one row, not a guessed sequence.

**The EXP to Lv 80 — the screen and the report disagree.** MAX on **EXP Pod (L)**
from Lv 1 selects **166 — +498 000 — and shows Lv 80 / 80**. MAX picks the fewest
Pods that reach the cap, so the total is **more than 495 000 and at most
498 000**; with the XL reading, the interval is **(495 000, 498 000]**. The
maintainer reports **exactly 500 000**, which the L reading rules out: at
500 000, 498 000 would not reach 80. **Recorded as the interval, not 500 000**,
until a reading pins it (165 L, then MAX on M, bounds it to 1 000).

**Phantom Pain Cage — Weekly Reward**, *"Resets weekly. Reach enough progress to
claim."* Reward tiles grade the **Scar 3★**, which its item card does not:

| Progress | Scar | Also |
|---|---|---|
| 30 000 | 4 | Skill Point ×3, EXP Pod (M) ×1, Cogs ×6 000 |
| 90 000 | 5 | Major Overclock Alloy ×1, EXP Pod (M) ×1, Cogs ×6 000 |
| 120 000 | 6 | Memory Overclock Circuit II ×1, EXP Pod (M) ×1, Cogs ×8 000 |
| 360 000 | 8 | Weapon Overclock Core II ×1, EXP Pod (M) ×1, Cogs ×18 000 |
| 500 000 | 10 | gold 5★ card ×10, chip 4★ ×5, Skill Point ×2 |
| 700 000 | 3 | a character-portrait 4★ item ×1, EXP Pod (L) ×1, Cogs ×30 000 |
| 900 000 | 5 | Cogs ×40 000 |
| 1 000 000 | 6 | gold 5★ card ×15, chip 4★ ×5, Skill Point ×2 |
| 1 100 000 | 9 | Cogs ×50 000 |

**The Scars sum to 56, the maximum the maintainer reports**, so no tier is
missing between the screenshots. The gold card looks like the Black Card and
the chip like the shop's "Support Skill Com…"; neither was opened.

**Not a bundle row, deliberately.** A weekly grant sized by the player's own
score is the same shape as the event missions: **a grant behind a capability
gate**, which `Reward` cannot express, and writing the 56-Scar maximum would
promise every reader the top tier. This belongs to N30's *"can this player clear
stage N is an input"* decision, and it is now the case that forces it: **the
Evolve route to SS is 30 shards, costing 500 Scars, which is at least nine weeks
of a perfect Cage.**

**Pinned, the same sitting:** 165 EXP Pod (L) plus MAX on EXP Pod (M) selects
**2 M — +497 000 — and reaches Lv 80 / 80**, so 165 L + 1 M (496 000) does not.
**The EXP from Lv 1 to 80 is in (496 000, 497 000].** Every Pod is a multiple of
1 000, so **no combination of Pods can deliver less than 497 000 and still reach
80**: as a demand in Pods, **497 000 is exact**, whatever the curve's last
digits are. The maintainer's 500 000 is most likely the XL reading (25 × 20 000) —
an upper bound, not the requirement; that is an inference, not their account.

**The Promote ladder, whole.** Read one Rank Up screen at a time, PRIVATE to
**HERO — "Max Level Reached"**:

| Step | From rank | Gate | Cogs |
|---|---|---|---|
| 1 | Private | Lv 2 | 5 000 |
| 2–3 | Sergeant | Lv 10, 20 | 10 000, 15 000 |
| 4–6 | Elite | Lv 30, 40, 45 | 20 000, 25 000, 30 000 |
| 7–9 | Task Force | Lv 50, 55, 60 | 35 000, 42 500, 50 000 |
| 10–13 | Ace | Lv 65, 70, 75, 80 | 60 000, 70 000, 80 000, 100 000 |

**Thirteen steps, Cogs only, 542 500 in total** — 453 Simulation Score at
1 200 Cogs a point, about **six runs of Simulated Battlefield**. Step 11's gate
was read through the weapon model overlapping it; Lv 70 fits the sequence. The
previews also give the stat gained per step (HP 0 → 1 444 at HERO, ATK → 278,
DEF → 212, CRIT → 128) — catalog data, not planner data, so not in the bundle.

**Every gate is a level, and every level gate is below the level cap** —
so for Promote the missing precondition costs nothing *if the plan levels the
character first*, which a plan that reaches the goal must do anyway. The gate
only matters for a partial plan.

**Skill levels — the report corrected by the screens.** The maintainer reported
every skill level at 1 SP + 2 000 Cogs. Three readings say otherwise:

| Skill | Step | Skill Points | Cogs |
|---|---|---|---|
| Astral Armament [Core Passive] | 1 → 2 | 1 | 2 000 |
| Delusional Spin [Red Orb] | 2 → 3 | 1 | 3 000 |
| Delusional Spin [Red Orb] | 3 → 4 | 1 | 4 000 |
| a skill at Lv 1, via the upgrade-all prompt | **1 → 18 (Max)** | **44** | **206 000** |

**The skill cap is 18.** Cogs climb 1 000 a level at the bottom, but that cannot
continue: a straight climb to 18 totals 170 000 Cogs and 17 SP, against the
prompt's 206 000 and 44, so **both costs steepen somewhere above Lv 4**, and
the per-level curve is unread. The rows written are the three steps read one at
a time. The 1 → 18 total is not written as a row until it is known **which
skill** the prompt was for and whether every skill shares one curve.

**Settled: one skill curve.** The level-to-max prompt on **Seeker System** at
Lv 1 also reads **1 → 18 for 44 SP and 206 000 Cogs**, and 1 → 2 reads 1 SP +
2 000 on Astral Armament, Seeker System, Withering Spiral and Amplifier alike.
*(Maintainer:)* every basic, special and common-effect skill shares the curve;
**Evolution Effect skills differ** and sit behind Evolve — *Vestige* [SS Rank
Passive] unlocks for **2 SP + 20 000 Cogs**, its Lv 1 "Unlocked at S5 Rank",
Lv 2 "at SS Rank". (Delusional Spin now shows Lv 18, so the account has since
paid the curve once.)

Written as **one chain per named skill**: 1→2, 2→3, 3→4 as read, then **4→18
derived as the remainder — 41 SP and 197 000 Cogs**. Not a 1 → 18 shortcut
beside the single steps: `DemandResolver` throws when two upgrades reach one
state, so a shortcut would have made every goal on that skill unanswerable. Her
blue-orb skill and a fourth basic skill were seen but not named, so they have no
rows.

**The last two skills named.** *Blight Excision* [Blue Orb] and *Link
Dissolution* [Basic Attack], both Lv 1, both 1 SP + 2 000 for 1 → 2 — so all
seven of her levelled skills now carry the curve. The leader skill is an unlock
row only.

**The shard is named: Inver-Shard – Lacrimosa**, 2 owned, *"Collect a sufficient
amount to link with the Construct"*; its flavour text says **Construct Memory
recycles into Inver-Shards**, which is the duplicate route. **Its card shows no
grade**, so it is not a bundle item yet, and Evolve stays out with it.

**Overclock Material Box (α)** — one stack of ten opened into **5 + 5 of two
3★ items**, names not read. Two kinds where (β) gave three. By icon, one is a
silver ore like Major Overclock Alloy's and the other a crossed pair of rods like
Memory Overclock Circuit II's, **which would make it Minor Overclock Alloy and a
Memory Overclock Circuit I — and no Weapon Overclock Core I**. That is a guess
from pictures, so it is not written.

**Settled — the α box, the shard's grade, the Scar's grade.** The α box's two
items are **Minor Overclock Alloy and Memory Overclock Circuit I** — guessed from
the icons, **confirmed by the maintainer**, not read off cards. So **nothing in
the Simulation Shop yields Weapon Overclock Core I**, and Hear the Bell's 28 have
no source in the bundle. The Phantom Pain shop tile grades **Inver-Shard –
Lacrimosa 5★**: *"Available: 30"*, **10 Scars each at "66% Off" (was 30)**,
which agrees with the maintainer's first-10-for-100. The **Scar is 3★** (blue),
on its owned-count panel and the Cage's reward tiles. Evolve S → SS is now one
row, 30 shards; the shop that sells them is not, because its limit never
resets.

## 2026-09-19 — Memory, read on one 6★ Memory

Read on **Samantha**, a **5★** Memory (Lv 1 / 25 before Overclock) — first taken for 6★ from its frame colour, corrected by its own detail screen. *(Maintainer:)* the
figures are the same for every Memory of its grade, and Memory takes **the same
four Overclock materials as a weapon**.

**Enhance to 45, slider on Highest** — HP 134 → 1 208, CRIT 5 → 51:

| | |
|---|---|
| Enhancement Cost | **EXP 18 000** (fodder filters 5★ greyed / 4★ / 1-3★ / Item Material) |
| Overclock Cost | Major Overclock Alloy **6**, Memory Overclock Circuit II **6**, Minor Overclock Alloy **7**, Memory Overclock Circuit I **10** |
| Consume | **260 000 Cogs** |

**Every one of those is purchasable with Simulation Score** — the α box yields
Minor Alloy and Circuit I, the β box Major Alloy and Circuit II — so, unlike Hear
the Bell, **a Memory's Overclock has a complete route**. The Memory EXP item's
value per unit is unread.

The weapon's 340 000 Cogs, read 2026-09-18 on the same kind of screen, now sits
on Hear the Bell's Overclock row — the row that stands for the advance to 45.

**Resonance — a choice of payment, and a random result.** *Upper Resonance*,
*"Spend materials or Memories"*, then *Choose Skill*. The Token tab offers
**one of** three prices:

| Pay | Grade | Held / needed |
|---|---|---|
| a 5★ chip item (the 5★ Memory Shard's icon) | 5★ | 4 300 / **150** |
| Special Support Token | 4★ | 7 716 / **234** |
| **Simulation Score** | 4★ | 366 401 / **246** |

with Memory and Item tabs as further alternatives. *(Maintainer:)* the skill
received is **random**. **Not a row, for two reasons**: three prices for one
state is exactly the choice `DemandResolver` refuses, and a random skill is the
probabilistic-goal shape N30 dropped. **It matters anyway**: Awaken Ultimate
needs *"Equip 6★ Resonance Skills ×12"*, presumably **six Memories × upper and
lower** (an inference from "Upper Resonance", not read) — at 246 Score each, about **2 950 Score, or 36 runs** of Simulated
Battlefield, if any skill counts.

**Cross-check:** Simulation Score read 366 425, then 366 401 — +164 for one
doubled run, −150 for a β stack, −38 for an α stack. The shop prices and the
run agree to the point.

**Memory EXP settled.** One **Memory Enhancer IV** (4★) reads *"Obtained EXP
300"*, estimated Lv 9 — the weapon's figure exactly, as the maintainer said.
Its card's "Owned 999" is the stack cap; the picker read 1 096. **Samantha** is
now a bundle entity with her Overclock row, and Memory Enhancer IV a fodder rule
with a Simulation Shop row (10 for 87).
