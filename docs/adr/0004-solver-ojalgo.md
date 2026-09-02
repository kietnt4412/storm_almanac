# 4. ojAlgo for the mixed-integer program

**Status:** Accepted · 2026-09-02

## Context

The planner is a genuine MIP: stage runs and conversions must be whole numbers,
because "run 3-4 exactly 17.3 times" is useless to a player. Candidates were
ojAlgo, OR-Tools, and a hand-rolled branch-and-bound.

## Decision

ojAlgo's `ExpressionsBasedModel`. Pure Java, so there are no native binaries to
fight inside a container image, and it covers LP, QP and MIP.

Solves are deterministic given `(game version, demand, estimates, constraints)`,
so that tuple is hashed into a cache key. Budget is two seconds synchronous;
past that the request is queued and the result pushed over WebSocket — the same
queue phase 9 replicates.

## Consequences

A pure-Java solver is slower than a native one on large models. The model is
expected to stay small (hundreds of stages, hundreds of items) so this should
not bind, but it is unmeasured until phase 2.

## Reversal trigger

Move to OR-Tools if p95 solve time exceeds two seconds on a realistic goal set
after the model has been pruned by reachability. Take the native-dependency and
container-size cost at that point, and record the measured before-and-after.
