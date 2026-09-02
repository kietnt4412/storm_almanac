# 6. Wilson score intervals for drop rates

**Status:** Accepted · 2026-09-02

## Context

Drop-rate samples start tiny and rates are near zero. The normal approximation
misbehaves in exactly that corner: negative lower bounds, and a zero-width
interval at zero observed successes — which would tell the optimizer an item
never drops after twenty runs.

## Decision

Wilson score intervals everywhere a proportion is estimated. Zero trials yields
`[0, 1]`, not a point at zero. The interval width drives
`DropEstimate.confidenceWeight()`, and both the sample size and the provenance
(`SEEDED` or `COMMUNITY`) are rendered next to every number in the UI.

## Consequences

Early plans are visibly uncertain rather than invisibly wrong. That is the
intended trade: a player is about to spend three weeks acting on this.

## Reversal trigger

Move to a Bayesian posterior with a per-item-tier prior if seeded estimates turn
out to be systematically biased against community data — the interval alone
cannot express "this prior is suspect". Measure that first; do not assume it.
