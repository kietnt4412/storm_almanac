# 2. Modular monolith, not microservices

**Status:** Accepted · 2026-09-02

## Context

One developer, no users yet, and a system with genuinely separable concerns:
game data, player state, solving, gacha maths, drop statistics. The separability
is a real property, not a reason to deploy seven things.

## Decision

One deployable. Hard module boundaries, a schema per module, and no
cross-module database reads. Modules communicate through internal events
(`EventPublisher`), which is in-process today and a broker later without callers
noticing.

The boundaries are enforced by `ModuleBoundaryTest` (ArchUnit), not by
discipline.

## Consequences

Extraction later is a deployment change rather than a rewrite. The cost is that
the boundaries have to be maintained while there is no external pressure keeping
them honest — hence the test.

## Reversal trigger

Extract a module when it needs to scale independently *and* the numbers say so:
the solver fleet saturating the API process is the expected first case, which is
exactly what phase 9 addresses. Extract on measurement, never on aesthetics.
