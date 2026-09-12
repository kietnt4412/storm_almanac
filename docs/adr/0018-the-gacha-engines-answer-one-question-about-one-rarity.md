# 18. The gacha engines answer one question about one rarity, and the trial count follows from the tolerance

**Status:** Accepted · 2026-09-12

## Context

Phase 5's exit criterion is two sentences: the two engines agree within 0.3%, and
they reproduce the published rates of both shipped games. Building them raised
four questions the plan does not answer, and three of them were only visible once
something had to compile.

**What does a pull return?** `PullResult` was scaffolded in Phase 0 as
`(Rarity, boolean featured, PityState)`, which reads naturally and cannot be
implemented. `BannerModel` pins the rate of the rarity being pulled for at every
pity count and says nothing whatsoever about the rest of the table while pity is
active. When a rising curve reaches 9%, the published numbers do not say which
rarity gave up the other 7.5 points — and every game will have given it up
differently. A `Rarity` on that record is an invented answer, and inventing it
per game is the one thing `gacha` is not allowed to do.

**What does a floor do to the answer?** Reverse: 1999 publishes "one 4-star per
10-pull". A floor guarantees *at least* its minimum, so it raises low outcomes and
never caps high ones, which leaves the 6-star rate on any given pull exactly where
the rate table put it — the floor is irrelevant to the question either engine is
asked. That argument is sound and it has a cliff: the moment a floor's minimum
reaches the rarity being pulled for, the floor *is* a second pity rule on a
different period, and an engine that ignored it would silently understate every
answer. Neither shipped game declares one.

**A boolean could not carry the state.** `PityState.guaranteedFeatured` was a
flag, which is exactly right for a rule that guarantees after one loss — both
shipped games — and cannot describe a player one loss into a rule that guarantees
after two. `FeaturedRule.guaranteeAfterLoss` is an `int` and permits that shape,
so either the state carries a count or the engines guess at the middle of it.

**And the criterion did not fit its own sample size.** The plan says 100 000
trials and it asks for agreement within 0.3%. At a hundred thousand trials the
standard error of a mid-range probability is about 0.16 percentage points, so 0.3
points is under two of them: a gap that size is ordinary sampling noise. Asked 84
questions, the cross-check found one on its first run — the debut banner over
thirty pulls, exact 0.139616 against simulated 0.136550, a gap of 0.31 points at
2.80 standard errors, with nothing wrong with either engine. A test that fails one
run in a few hundred for no reason is worse than no test: the failure arrives
attached to an innocent commit.

## Decision

**Both engines answer one question about one rarity, the simulation is seeded, and
the trial count is derived from the tolerance rather than quoted from the plan.**

1. **The headline rarity is the only rarity modelled, and `PullResult` carries no
   `Rarity`.** A pull is reported as the two facts the model determines: whether
   it hit the rarity being pulled for, and whether that hit was the featured unit.
   The headline pity counter measures pulls since the last *headline* hit, so a
   lower rarity arriving does not reset it and a lower rarity's pity rule cannot
   move the answer — which is a claim, and is therefore a test rather than a
   comment.
2. **A floor below the headline rarity is ignored; a floor that reaches it is
   refused by name.** The argument for ignoring it is written at the refusal, so
   the next person to read the code gets the reasoning and not the result. Both
   shipped games' floors are ignored, correctly.
3. **`PityState` carries `consecutiveLosses`, not a flag**, and the three
   transitions — miss, featured hit, lost split — live on the record rather than
   in either engine, so the exact chain and the simulation advance state through
   the same three lines. A mistake in them is a mistake both engines make
   identically, which the cross-check would agree enthusiastically about; so the
   transitions are pinned separately, against a generator handing out chosen
   numbers.
4. **The Monte Carlo engine is seeded, deterministic under parallelism, and runs
   500 000 trials by default.** Generators are split sequentially on the calling
   thread before any virtual thread starts, each chunk runs a fixed share of the
   trials, and counts are summed as integers in chunk order — so the answer does
   not depend on which thread finished first. Half a million trials puts the
   standard error at 0.07 points at worst, which makes the plan's 0.3 points a
   four-sigma bound rather than a two-sigma hope. **The criterion did not move;
   the sample size did.**
5. **Agreement is asserted twice, and the second assertion is the real one.** The
   0.3 points is the plan's criterion. Because the seed is fixed, asserting it is a
   regression test — the same trials every run, so a change in the answer is a
   change in the code. The claim that the engines *agree* is carried by the other
   tolerance: every gap is inside three standard errors of the simulation that
   produced it, which is a statement about sampling noise and not about this seed.
   The worst gap is printed on every run, because the number that matters is the
   headroom and not the pass.
6. **The exact chain reads the mass that never arrived, not the mass that did.**
   Accumulating arrivals over seventy steps leaves a rounding residual near 1e-13,
   which returned 0.9999999999999895 for a wall the game guarantees and, once, a
   probability above 1.0. Every path out of the chain leaves the remaining mass
   exactly zero at certainty, so certainty is exactly 1.0 and impossibility
   exactly 0.0, and the residual lands on the answers in between where it is 1e-16
   against a number nobody reads past four decimals. A wall is the one part of a
   pity system a player can check against the client, so it is the part an engine
   has no excuse to be approximately right about.

## Consequences

**The cross-check is worth what it was supposed to be worth.** 84 questions across
all seven published banners, every gap inside 0.110 percentage points against a
criterion of 0.300, worst case 1.84 standard errors. Two methods sharing three
branches and nothing else, landing there, is a correctness argument about the pity
model that neither could make alone.

**Two of the first failures were in the tests, which is the cheap direction.**
Asserting that Punishing: Gray Raven's 30-pull weapon wall was certainty failed at
0.7030 — the wall guarantees the *rarity*, and only the guarantee after a loss
guarantees *her*. And the floating variant's ten-pull probability is 0.1010 rather
than the 0.0982 that "one hit times the 70% split" predicts, because losing the
split early leaves room to hit again inside the same ten pulls. Both were pinned
afterwards against a chain written separately, in another language, to check this
one.

**`expectedPullsToFeatured` needs no linear solve, and that is a property of pity
rather than a trick.** Hard pity makes the expected wait to the next hit solvable
backwards from the wall, because a miss can only move the counter forwards; pity
then resets on every hit, so the chain is cyclic with a single entry point and the
unrolled sum over lost splits is geometric. The whole answer is two scalars and a
`Math.pow`. A game without hard pity would not have this property — and would also
have an infinite worst case, which is a larger problem than this one.

**The published rates are still second-hand, and this changes nothing about
that.** Every figure these engines reproduce comes from the sources the plan
cites, not from either publisher's disclosure — open question **Q4**, half-answered
by N26 only in the sense that the disclosure is known to exist. What the
acceptance fixtures prove is that the model reproduces the figures it was given.
Whether those figures are what Bluepoch states is a different question, and no
test in this repository is evidence about it.

**Nothing calls either engine.** There is no bean, no route and no screen, on the
same reasoning that kept `SolveCoordinator` unwired through Phase 2: a surface
built before there is a path a request takes answers a question nobody asked. The
`IncomeModel` interface is also still an interface, and for a harder reason — see
the reversal trigger.

## Reversal trigger

**Put a `Rarity` back on `PullResult` when a game publishes a rate table that
pins it** — that is, when the disclosure says what the non-headline rates are
*while pity is active*, rather than only at base. Then a full-rarity draw stops
being an invention and the record can carry one honestly.

**Model floors when a game declares one that reaches the headline rarity.** The
refusal exists to make that day loud. It is a second pity counter with its own
period, so it costs a state dimension in the exact chain and nothing in the
simulation.

**Revisit the trial count if the criterion changes, in either direction.** 500 000
is derived from 0.3 points at four sigma and from nothing else; a tighter
criterion needs a bigger sample, quadratically, and a looser one should shrink it
rather than bank the headroom.

**`IncomeModel` cannot be implemented against the current model, and the trigger
for writing it is a schema change, not a decision.** `projectedPulls` needs to
know which item is pull currency and what a pull costs in it. `BannerModel`
declares neither, and neither does the `banner` table — so "how much will she have
accrued by Friday" has nothing to compute from. Adding a currency item and a price
to the banner is a bundle field, a parser, a writer, a migration and a JDBC round
trip, and it should be done with a game whose income sources are actually
ingested rather than speculatively. Until then the interface states the question
and admits there is no answer.
