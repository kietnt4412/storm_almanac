# Benchmark: what the Reverse: 1999 community says the best stage is

**Purpose.** Phase 2's exit criterion is *"run the solver against goal sets the
community has already settled; if it disagrees with the accepted best stage,
find out why."* Every other test in this repository checks the optimizer against
itself. This is the only place its answers are compared with answers worked out
by somebody who does not know this project exists.

The claims below are transcribed once, here, and read by
`backend/app/src/test/java/io/stormalmanac/app/CommunityBenchmarkTest.java`.

---

## 1. Provenance

| | |
|---|---|
| Source | Prydwen Institute, *Insight Materials Cheat Sheet (2.7 patch)* |
| URL | <https://www.prydwen.gg/re1999/guides/insight-cheat-sheet> |
| Read | 2026-09-07 |
| Our data | Kornblume snapshot at commit `8b40541a9c42`, labelled patch 3.5, whose sampled drop table is `stages3_3_greedy.json` |

**Nothing from the guide is vendored.** What is recorded here is twenty short
factual claims — a material, a stage, a percentage — with their source, which is
a citation and not a dataset. [ADR 0009](../adr/0009-upstream-data-is-fetched-never-vendored.md)
governs the *data*, and this file does not weaken it.

**The two sides are not contemporaries and that is the first thing to check when
they differ.** The guide is written against patch 2.7; our drop tables were
resampled at 3.3. Chapters 10 to 12 did not exist when it was written, and
several older stages were retuned since.

**The guide answers a slightly different question, too.** It ranks stages by drop
*rate*, and only Hard stages, and it weighs what else a run drops. Our model
ranks by Activity per unit of the material asked for. Those coincide often and
not always, and where they do not, neither is wrong.

---

## 2. The claims

Rates are quantities per run, not probabilities — a value above 1.0 is a stage
that drops more than one, which is [prior-art.md §4.1](../prior-art.md) and the
reason `Drop` holds a yield.

Names differ where the guide and this upstream translate the same item
differently. The rates are what identify them: the guide's *Fox Tail* at 22.52%
is this upstream's *Alopecurus Pratensis* at 20.9% — the same foxtail grass.

| Material (ours) | Guide's name | Best stage | Rate quoted |
|---|---|---|---|
| Bifurcated Skeleton | | 10-13H | 44.99% |
| Biting Box | | 7-26H | 31.03% |
| Clawed Pendulum | | 11-5H | 45.94% |
| Holy Silver | | 10-9H | 47.97% |
| Prophetic Bird | | 2-6H | 46.30% |
| Salted Mandrake | | 3-13H | 34.13% |
| Winged Key | | 9-15H | 32.55% |
| Goose Neck | | 5-4H | 29.56% |
| Red Lacquer Tablet | Red Lacquer Slab | 9-1H | 32.97% |
| Golden Herb Incense | Golden Grass Incense | 11-21H | 38.82% |
| Perpetual Cog | | 7-16H | 27.16% |
| Pyroxene Ore | Luminite Ore | 9-1H | 30.66% |
| Alopecurus Pratensis | Fox Tail | 8-18H | 22.52% |
| Milled Magnesia | | 5-8H | 27.79% |
| Rough Silver Ingot | | 9-6H | 58.95% |
| Esoteric Bones | | 5-7H | 29.40% |
| Liquefied Terror | | 9-3H | 21.39% |
| Silver Ore | | 7-19H | 210.66% |
| Spell of Banishing | | 4-20H | 208.55% |
| Golden Beetle | | 10-2H | 31.30% |

**One claim is deliberately not in the test.** The guide names *2-9 Hard* for
Magnesia Crystal at 100%. `2-9H` is not in the upstream's sampled stage table at
all — it is in the older `stages.json` and not in `stages3_3_greedy.json` — so
this project has no opinion about it to compare, and inventing one from the older
table would mix two samplings of a game that retuned its drops in between. Left
out, and written down rather than dropped quietly.

**Two claims are of a different kind and are not tested yet:** *Golden Beetle —
craft it, do not farm it* and *Solidus — craft it*. Comparing those means costing
a craft route against a farm route, which the optimizer does internally on the
shadow price. Worth adding when there is a reason to.

---

## 3. What the comparison found

Run it with `./gradlew :app:test --tests '*CommunityBenchmarkTest'` after
`tools/fetch-upstream.sh`. It skips without a snapshot, like everything else that
touches real data.

### 3.1 A defect, before it ran once

The guide names stages this project's data did not contain. `KornblumeAdapter`
was reading `stages.json`, the upstream's older table, which at both pinned
commits carries **chapters 1 to 4 only** — while the upstream's own planner reads
the newest `stages<major>_<minor>_greedy.json`, which carries all twelve. Two
thirds of the game were missing, and nothing said so: the bundle was well-formed,
the round trip held, the solver was fast, and every plan was computed against a
third of the content.

It also invalidated a finding this project had written down as a fact about the
game: *"45 of 118 characters' Insight 2 cannot be planned from patch 3.5."* With
the table the upstream actually uses, **all 118 can**.

### 3.2 Five exact agreements

The cheapest stage this project computes **is** the stage the guide names, for
Holy Silver (10-9H), Salted Mandrake (3-13H), Perpetual Cog (7-16H), Pyroxene Ore
(9-1H) and Alopecurus Pratensis (8-18H). That is Phase 2's five benchmark answers.

Of the remaining fifteen, seven more agree within 8% — close enough that the
guide's stage and ours are both defensible advice — and where our first choice
differs it is usually a chapter 10-to-12 stage the guide predates.

### 3.3 The rates themselves agree

Seventeen of the guide's quoted rates land within **3 percentage points** of a
sample this project had never seen when the guide was written. That is two
independent measurements of the same game agreeing, and it is the strongest
evidence so far that the pipeline is carrying real numbers rather than
plausible ones.

### 3.4 Every disagreement is a sample-size disagreement

The upstream publishes how many runs each stage's drop counts were observed
over — from **105 to 41 212** — and the model throws that number away, so a mean
over 105 runs is treated exactly like a mean over 41 212.

Every disagreement over 25% is one of these:

| Material | We prefer | Runs | Guide says | Runs | Gap |
|---|---|---|---|---|---|
| Milled Magnesia | 4-5H | **105** | 5-8H | 367 | 154% |
| Liquefied Terror | 4-4H | **113** | 9-3H | 496 | 224% |
| Rough Silver Ingot | 4-5H | **105** | 9-6H | 2 680 | 29% |
| Silver Ore | 2-3H | 1 270 | 7-19H | **228** | 31% |

A noisy mean is a high mean about as often as a low one, and `argmin` picks
whichever stage got lucky. **This is [open question Q8](../../TRACKER.md#open-questions)
ceasing to be a formality: the missing sample size is currently choosing the
plan.** Note the last row — the small sample is on the *guide's* side there,
which is also why its quoted 210.66% is the one rate we cannot reproduce.

### 3.5 The plan beats the advice

Over the five-character Insight 2 goal set, the solver's plan costs **3 624
Activity**; following the guide material by material, for the eleven benchmark
materials that appear in that goal set alone, costs **4 017** — and does not
cover the rest of the demand. That is the product claim, measured: one run drops
five materials and a per-material plan pays for each of them separately.

---

## 4. What would change this file

- **A new snapshot.** Both the rates and the stage list move. The test reads
  sample sizes from the snapshot, so it reports rather than assumes.
- **A newer guide.** This one is patch 2.7 against 3.3 data; a 3.x cheat sheet
  would remove the largest single caveat here.
- **Sample sizes reaching the model** (Q8). The four disagreements in §3.4 should
  mostly disappear, because a 105-run mean would stop outranking a 2 680-run one.
  If they do not, the disagreement is real and belongs to the model.
- **A second source.** One guide is one opinion. A second, independent list would
  turn "agrees with the community" from a claim into a measurement.
