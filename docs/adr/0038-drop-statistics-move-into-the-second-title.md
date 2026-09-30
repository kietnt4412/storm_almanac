# 38. Drop statistics move into the second title

**Status:** Accepted · 2026-09-30

## Context

Phase 6's exit is *a community-derived estimate supersedes a seeded one in a
live plan*. Drafting its plan ([docs/plans/phase-6-drop-statistics.md](../plans/phase-6-drop-statistics.md))
found that the live title cannot meet it:

- The PGR bundle has one stage, Simulated Battlefield, and its only drop is a
  **declared** 82 Simulation Score a run. A fixed payout has no sampling error,
  so there is nothing for reports to estimate.
- **The maintainer reports that PGR's farming is all fixed payouts** — no
  stage the planner could use drops at random. This is the maintainer's report
  as a daily player, not a screen read for this decision; one random-drop stage
  found later is the reversal trigger below.
- The seeds plan.html imagined were Kornblume's, which ADR 0015 keeps out of
  the product, so no seed exists either.

Reverse: 1999 is the opposite case. 764 of its 779 drop facts are sampled, the
publisher does not disclose stage rates, and the tracker's N26 names exactly
this as the main risk: without its own drop data, R1999 cannot be planned
first-hand. Phase 6 is that data.

## Decision

1. **Phase 6 is not built now.** It moves into Phase 11 and is built as part of
   bringing R1999 in, where its exit can be met.
2. **Its plan is kept as drafted**, with its six questions unanswered, and Q7
   stays open with it. Its proposed answer (`scan` returns every report for the
   stage, so the `item` parameter goes) still stands as the recommendation.
3. **Nothing in `stats` is removed.** The ports, the intervals and the
   `drop-report-store` key stay as they are; `YieldTable` keeps taking an
   estimate repository that nothing provides.
4. **N18 moves with it** — estimates go into `SolveKey` in the same change that
   first publishes one, whenever that is.

## Consequences

The product keeps planning PGR from declared yields only, which is correct for
a game whose farming pays fixed amounts. Phase 11 grows by Phase 6's 1.5 weeks.
The project's "data platform" story waits for the second title, and "a planner
whose statistics layer is game-agnostic" becomes something Phase 11 shows by
being the first game to need it.

## Reversal trigger

A PGR stage the planner can use turns out to drop at random. Then Phase 6
comes back on PGR with that stage as its seed, and its plan's question 1 is
answered.
