# 11. A yield is a mean per run, and the solver uses what its sample supports

**Status:** Accepted · 2026-09-08

Narrows [ADR 0006](0006-wilson-intervals-for-drop-rates.md). It is not superseded:
Wilson stays exactly right for the question it answers.

## Context

Two things came together in the eighth and ninth sessions.

**The measurement.** Twenty per-material "best stage" claims from a published
community guide were compared against what this project computes
([the benchmark](../benchmarks/reverse-1999-community-answers.md)). Five agreed
exactly, seventeen quoted drop rates landed within three points of a sample this
project had never seen — and **every disagreement over 25% had the same cause**:
a mean over about a hundred runs outranking a mean over thousands. The model
preferred 4-5H to 5-8H for Milled Magnesia by 154%, and 4-4H to 9-3H for
Liquefied Terror by 224%, on samples of 105 and 113 runs. A noisy mean is a high
mean about as often as it is a low one, and `argmin` over point estimates picks
whichever stage got lucky.

**The evidence was there and we were throwing it away.** The upstream's sampled
stage tables publish a raw drop count *and* the number of runs behind it —
between 105 and 41 212 across the 105 stages of the pinned snapshot. The adapter
divided one by the other and discarded the denominator, because `Drop` had
nowhere to put it and neither did the schema.

Behind both sits **Q8**, open since the seventh session: `DropEstimate` carries a
Wilson interval, which makes it a binomial *proportion* — the share of runs that
yielded the item. The optimizer's constraint is
`sum over stages of runs * yield >= demand`, whose coefficient is an expected
*quantity per run*, unbounded above. A stage yielding three copies a run has a
proportion of 1.0 and a mean of 3.0. The two questions are not the same question,
and only one of them is the one the solver asks.

## Decision

**A drop's yield is a mean per run, it carries the number of runs it was measured
over, and the solver is given what that sample supports rather than what it
happened to show.**

1. **`Drop` gains `sampledRuns`.** It travels the whole way — canonical bundle,
   parser, writer, schema (`V4`), JDBC both directions, patch diff — because a
   number's provenance is part of the data, not a planner concern. **Zero means
   *declared*, not "measured badly"**: a fixed-reward stage, or an upstream that
   publishes rates with no provenance. There is no such thing as a mean over no
   runs, so a bundle that writes `"sampledRuns": 0` is refused rather than
   quietly agreed with.
2. **The interval for a mean is not Wilson's.** `stats` gains
   `PoissonRateInterval`: the counting analogue of the Wilson score interval,
   `(C + z²/2 ± z·sqrt(C + z²/4)) / n`. It is unbounded above, non-negative
   everywhere, and at `C = 0` gives `[0, z²/n]` rather than collapsing to a
   point — the same properties that got Wilson chosen for proportions, for the
   question the solver actually asks.
3. **`YieldTable` gives the model the lower bound of the 95% interval** wherever
   a sample size exists, and the declared value wherever one does not. A stage
   sampled a hundred times must beat one sampled ten thousand times *by more than
   luck* before a plan will send a player there. The plan says so in its notes.
4. **A sample size of one is a declaration, and the adapter says so.** In both
   pinned Reverse: 1999 snapshots the stages carrying `count: 1` are exactly the
   twelve Insight and two Resource stages — flat payouts of 9 000 Sharpodonty or
   two Pages, never sampled at all — while every genuinely sampled stage carries
   at least 105 runs. Reading that 1 as a sample would put a 95% bound on a
   number nobody measured and make the only source of several currencies look
   unfarmable. Which upstream conventions mean "declared" is an **adapter's**
   judgement, made once, at the boundary, in the module allowed to know about a
   particular game.

Wilson stays where it belongs: `DropEstimate` is a proportion estimated from
player reports, and *"did it drop"* is a binomial question. When Phase 6 starts
publishing estimates, they should arrive as a mean and a sample size and take the
same path as a declared yield — at which point `YieldTable`'s two branches
collapse into one.

## Consequences

**Plans cost more, and are more likely to be achievable.** Every coefficient from
a sampled table is smaller than its point estimate, so the same goal set needs
more runs than it did yesterday. That is the correction, not a regression: the
old number was the middle of a distribution being read as a guarantee.

**How much more depends on the sample, which is the point.** At 41 212 runs the
discount is under one percent. At 105 runs a rate loses roughly a third to a
half of its value. Rare drops lose most: a rate resting on a handful of
observations is barely supported at 95%, and the model now says so instead of
planning around it.

**The ranking can disagree with a community guide in the other direction now.**
A guide quoting a raw observed rate is quoting the point estimate; this project
is quoting what the evidence supports. When those disagree, the benchmark must
record *which* is being compared, or it will read a correction as a regression.

**Poisson is an assumption, and it is wrong in a known direction.** A per-run
yield is not really Poisson — a stage dropping exactly one of an item on 40% of
runs has less variance, one dropping five at a time has more. So the interval is
mildly conservative for the first and mildly optimistic for the second. Neither
error is close to the hundred-fold difference in sample size it replaces, and
fixing it needs the per-run distribution, which no upstream publishes and only
our own drop reports could ever supply.

**A source never silently stops being one.** The score bound is strictly positive
wherever anything at all was observed, and reaches zero only when the observed
total does, so a discount can make an item expensive but cannot make it
unobtainable.

## Reversal trigger

Replace the Poisson score bound with a distribution estimated from real per-run
reports once Phase 6 has them — that is the honest version of this, and it needs
data this project does not have yet.

Reconsider the **lower bound** specifically, in favour of the point estimate or a
shrinkage estimator, if either shows up:

- the benchmark's exact agreements fall below five, or the plan stops beating
  the guide's per-material cost, because the discount has made the model too
  timid to prefer a genuinely better stage; or
- a well-sampled stage is passed over in favour of a worse one because a rare
  drop's bound collapsed — the failure mode this creates, where ADR 0006's was
  the opposite.

Measure it against the benchmark before changing it. That is what the benchmark
is for, and it is the only thing in this repository that has ever told us the
model was wrong about the game rather than about itself.
