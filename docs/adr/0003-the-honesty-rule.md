# 3. Every hand-built component sits behind a boring one

**Status:** Accepted · 2026-09-02

## Context

Track B exists to prove the substrate is understood, not to decorate a CV. The
failure mode of a project like this is a hand-rolled storage engine that is
load-bearing, unbenchmarked, and impossible to back out of.

## Decision

Each hand-built component sits behind an interface that already has a working,
boring implementation:

| Port | Boring implementation | Hand-built implementation |
|------|----------------------|---------------------------|
| `DropReportStore` | Postgres (phase 6) | `almanac-store` (phase 7) |
| `SolveCoordinator` | single-node (phase 2) | `almanac-raft` (phase 9) |
| solve cache | Redis (phase 2) | replicated KV (phase 8) |

Both implementations stay in the codebase, selectable by config
(`storm-almanac.substrate.*`), and the benchmark between them is published.

Track B does not start until the product is publicly deployed with real traffic.
Infrastructure written for a system with no users is written against imaginary
requirements.

## Consequences

The interfaces have to stay narrow enough that both implementations are honest —
a port shaped to flatter the hand-built side proves nothing. Carrying two
implementations costs maintenance forever.

**Concluding that Postgres won is an allowed outcome.** "My LSM tree is 40%
slower on this workload and here is exactly why" is the stronger signal: it says
the numbers were measured rather than assumed.

## Reversal trigger

Delete a boring implementation only after the hand-built one has served
production traffic for a full quarter with published p99s and a restore drill
completed against it. Nothing has met that bar yet.
