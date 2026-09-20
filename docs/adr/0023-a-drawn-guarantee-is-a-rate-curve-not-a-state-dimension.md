# 23. A drawn guarantee is a rate curve, not a state dimension

**Status:** Accepted · 2026-09-20

## Context

Punishing: Gray Raven's **Themed Construct** pool does not have a wall. Its rules
panel says a value is generated randomly between 80 and 100, that an S-Rank is
guaranteed within that value, and that the value is generated again once an
S-Rank has arrived; the counter on the banner screen reads `8/80~100`, so **the
player is never told their own threshold**
([the reading](../game-facts/punishing-gray-raven-research-disclosure.md)).

`PityRule` was `(hardAt, softFrom, softJumpTo, softStep)`, and none of those is a
random variable. The archetype therefore had **no honest spelling**: `hardAt = 80`
promises a wall the game does not honour, and `hardAt = 100` forgets the twenty
pulls in which it usually arrives. So the banner was left out of the first
first-hand bundle rather than approximated, and the gap has been sitting in the
tracker as N31 since 2026-09-18.

The cost looked structural. Under
[ADR 0018](0018-the-gacha-engines-answer-one-question-about-one-rarity.md)
`MarkovBannerEngine` is an exact chain over `(pulls since hit, losses carried,
copies held)`, and the obvious readings of a drawn threshold both cost real
money: carry the drawn value as a **fourth dimension**, multiplying the state
space by twenty-one, or **mix twenty-one chains** and average. The second is
wrong as well as expensive — the threshold is redrawn on every hit, so a
multi-copy run does not have *a* threshold to average over.

It also raised a question about the cross-check. If both engines read the same
curve, their agreement says nothing about whether the curve is the right one.

## Decision

**The draw is integrated out analytically, and the marginal is a rising hazard
curve over the pity counter alone.**

Condition on `c` consecutive misses. A miss is a failed curve roll *and* a
threshold that has not arrived, so `c` misses are impossible for any `W <= c` and
**equally likely for every `W > c`** — the curve rolls are independent of `W`.
The posterior is therefore uniform on `{max(drawnFrom, c+1) .. hardAt}`. It
depends on nothing but `c`, which is already a dimension of the chain, and the
chance that *this* pull is the forced one is `1 / (hardAt - c)`. So

```
rateAt(c) = f + (1 - f) * curveAt(c),   f = 1 / (hardAt - c)
```

for `c` inside the range, and `curveAt(c)` below it. That is the same shape soft
pity already had, and it composes with soft pity for the same reason the
derivation never mentions the curve.

1. **`PityRule` gains one nullable field, `drawnFrom`, and `hardAt` keeps its
   meaning exactly** — the pull at which the rarity is certain. Every rule
   written before this reads back unchanged and means what it claimed when it was
   published, which a version being immutable requires. A `drawnFrom` equal to
   `hardAt` is **refused by name**: a range of one is a fixed wall spelled the
   long way, and two spellings of one banner is a diff nobody can read.
2. **The exact chain is not touched.** Its state space keeps three dimensions and
   gets a hundred deep instead of sixty. The fourth dimension was never needed.
3. **The simulation draws, and that is deliberate redundancy.** A Monte Carlo
   engine that reused the integrated curve would agree with the chain about the
   marginalisation **by construction**, and the cross-check would be circular. So
   `PullModel` exposes both roads — `hitRateAt(c)` marginal, `hitRateAt(c, wall)`
   conditional — and `MonteCarloBannerEngine` draws a threshold per pity cycle,
   redrawing wherever the game redraws: on the rarity arriving, won or lost.
4. **A drawn threshold is conditioned on the misses already in the state.** This
   is the half that was wrong on the first run, and it is not a refinement. A
   player carrying 85 misses against a threshold drawn from 80–100 **cannot have
   drawn 80**; they would have hit it. `drawWall` therefore draws from
   `{max(drawnFrom, pullsSinceHit + 1) .. hardAt}`, and a fresh cycle — where
   `pullsSinceHit` is zero — is the unconditioned draw it always was.

## Consequences

**The cross-check earned its keep, on the question it was built for.** Sampling
the prior instead of the posterior put the engines 17.6 points apart on one
question — carrying 85 misses, five pulls, exact `0.381856` against simulated
`0.557824` — and the exact chain was right. That is the first time the two
methods have disagreed about anything but rounding since Phase 5 closed, and it
is exactly the class of bug a single engine would have shipped: the answer was
plausible, monotone, and wrong in the direction that flatters the player.

**The agreement test had to be told where to look.** Its generic question set
asks at 1, 10, half the wall, the wall and the worst case, and for a wall of 100
that is 1, 10, **50**, 99, 100 — every one of them either below the drawn range
or at certainty's doorstep. The one band where the two roads can disagree was
never sampled. Questions across 80 to 100 are now generated for any banner whose
guarantee is drawn, which took the suite from 96 questions to **108**; the worst
gap is unchanged at **0.110 points at 2.22 standard errors**.

**The model now reproduces the note's arithmetic from the other end.** The
research disclosure computed the Themed pool's long-run share by hand as
**2.021%** against the publisher's advertised 1.90%, and concluded the advertised
figure is an outcome share rather than a per-pull vector. The chain, which knows
nothing about the note, says **49.488 pulls on average — 2.0207%**. The
disagreement with the publisher is now the model's and not a spreadsheet's, which
is what **Q4** needs it to be before anything is concluded from it.

**The fixture was wrong in both halves and is corrected.**
`Banners.grayRavenFloating()` paired a 1.50% base with a 70% featured rate and a
fixed wall at 80, from second-hand sources. The client pairs 1.50% with **100%**
and the drawn range, on one screen — so the worst case is one wall and not two,
and ten pulls is ten base rolls rather than the split-shaped answer that used to
be pinned there. **The two axes pair**: there are two PGR archetypes, not four.

**The column is proved against Postgres, not only against the parser.** The
proving-ground fixture gains a second banner whose guarantee is drawn, so the new
field crosses the writer, the parser, the migration and the JDBC round trip the
way every other field does. `Facts` carries it too — without that, a patch that
turned a fixed wall into a drawn one would report `hard at 80 -> 100` and read as
a nerf.

**What this does not do is put the banner in a bundle.** The archetype is now
expressible and the launch title's bundle still has no banners at all, because a
banner needs its pull currency and price — **N28** — and those are a schema
change of their own.

## Reversal trigger

**Carry the threshold in the state when a game draws one that is not uniform, or
does not redraw it on every hit.** Both are load-bearing. A non-uniform prior
still has a closed-form posterior but it is no longer `1/(hardAt - c)`; a
threshold that survives a hit makes the posterior depend on the run's history
rather than on `c`, and then it really is a fourth dimension.

**Reconsider the refusal of `drawnFrom == hardAt` if a game publishes a range
that collapses to one** — say, a wall that narrows over a patch series. The
refusal exists to keep one banner from having two spellings, not to deny that a
range can have one member.
