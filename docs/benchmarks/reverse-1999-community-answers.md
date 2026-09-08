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

### 3.2 Nine exact agreements — five of them, then four more

**Per unit of what.** Since
[ADR 0011](../adr/0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md) a
yield carries the number of runs it was measured over, and the solver is given
the lower end of a 95% interval on the mean rather than the observed mean itself.
So there are two rankings and this file reports both, because reading one as the
other turns a correction into an apparent regression:

- **on the yields the solver uses** — what a player is actually told;
- **on raw point estimates** — the observed means, which is what a guide quotes,
  and what this project ranked on before ADR 0011.

**On raw point estimates: five exact agreements** — Holy Silver (10-9H), Salted
Mandrake (3-13H), Perpetual Cog (7-16H), Pyroxene Ore (9-1H) and Alopecurus
Pratensis (8-18H). That was Phase 2's five, measured 2026-09-07.

**On the yields the solver uses: nine** — those five, plus **Bifurcated Skeleton
(10-13H), Clawed Pendulum (11-5H), Goose Neck (5-4H) and Red Lacquer Tablet
(9-1H)**, measured 2026-09-08. In each of the four, this project used to prefer a
thinly sampled stage whose observed mean was higher; discounting each stage by
what its own sample supports moved the answer onto the one the community names.
**Agreement with the community nearly doubled, from a change made for a reason
that had nothing to do with agreeing with the community**, which is the only kind
of agreement worth having.

Of the remaining eleven, five more agree within 11% — close enough that the
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
over — from **105 to 41 212**. Until 2026-09-08 the model threw that number away,
so a mean over 105 runs was treated exactly like a mean over 41 212, and a noisy
mean is a high mean about as often as a low one: `argmin` picked whichever stage
got lucky. That was the finding, it became **N17**, and it is now fixed —
[ADR 0011](../adr/0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md).

**On raw point estimates, four disagreements over 25% (2026-09-07):**

| Material | We prefer | Runs | Guide says | Runs | Gap |
|---|---|---|---|---|---|
| Milled Magnesia | 4-5H | **105** | 5-8H | 367 | 154% |
| Liquefied Terror | 4-4H | **113** | 9-3H | 496 | 224% |
| Rough Silver Ingot | 4-5H | **105** | 9-6H | 2 680 | 29% |
| Silver Ore | 2-3H | 1 270 | 7-19H | **228** | 31% |

**On the yields the solver now uses, three (2026-09-08):**

| Material | We prefer | Runs | Guide says | Runs | Gap |
|---|---|---|---|---|---|
| Milled Magnesia | 4-5H | **105** | 5-8H | 367 | 145% |
| Liquefied Terror | 4-4H | **113** | 9-3H | 496 | 210% |
| Silver Ore | 2-3H | 1 270 | 7-19H | **228** | 39% |

Rough Silver Ingot fell out of the list — from a 29% disagreement to **7.9%** —
because 4-5H's 105-run mean lost more to the discount than 9-6H's 2 680-run one.
The other two shrank and did not resolve, and **that is the honest result rather
than a disappointing one**: a 95% bound is not a cure for a sample of 105 runs
that happens to have gone well, it is a statement of how little such a sample
supports. Both remaining rows still have a small sample on one side, so the
assertion that no large disagreement rests on well-sampled data on both sides
holds, which is what would indicate a defect in the model rather than in the
evidence.

Note the last row — the small sample is on the *guide's* side there, which is
also why its quoted 210.66% is the one rate we cannot reproduce, and why the
discount moves that row the wrong way. Discounting our own well-sampled stage
against their thin one widens the gap. Nothing is wrong with either number.

### 3.5 The plan beats the advice

Over the five-character Insight 2 goal set, the solver's plan costs **3 880
Activity**; following the guide material by material, for the eleven benchmark
materials that appear in that goal set alone, costs **4 017** — and does not
cover the rest of the demand. That is the product claim, measured: one run drops
five materials and a per-material plan pays for each of them separately.

**The plan got more expensive on 2026-09-08 and that was the intent.** It cost
3 624 Activity when every yield was taken at its observed mean; planning against
what those samples support costs 7% more, and the margin over the guide's own
advice narrowed from 393 Activity to 137. A plan built on optimistic
coefficients is cheap on paper and short in the inventory. See ADR 0011's
consequences, and note that a future snapshot with thinner sampling could push
this number past 4 017 — at which point the honest report is that the guide is
cheaper on the materials it covers, not that the solver regressed.

---

## 4. What would change this file

- **A new snapshot.** Both the rates and the stage list move. The test reads
  sample sizes from the snapshot, so it reports rather than assumes.
- **A newer guide.** This one is patch 2.7 against 3.3 data; a 3.x cheat sheet
  would remove the largest single caveat here.
- ~~**Sample sizes reaching the model** (Q8). The four disagreements in §3.4
  should mostly disappear, because a 105-run mean would stop outranking a
  2 680-run one. If they do not, the disagreement is real and belongs to the
  model.~~ **Done 2026-09-08 (N17, ADR 0011), and the prediction was half
  right.** One of the four disappeared and two shrank without resolving; exact
  agreements went from five to nine. What the prediction missed is that a 95%
  lower bound discounts a thin sample *proportionally* rather than dismissing it,
  so a 105-run mean that is four times higher still wins — correctly, on this
  evidence. The remaining disagreements belong to the sample, not to the model:
  the fix for them is more sampling (Q2/F1), or our own drop reports in Phase 6.
- **A second source.** One guide is one opinion. A second, independent list would
  turn "agrees with the community" from a claim into a measurement.
