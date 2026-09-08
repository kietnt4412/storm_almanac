# 12. The solve cache is in-process until there is a second node

**Status:** Accepted · 2026-09-08

Narrows one row of [ADR 0003](0003-the-honesty-rule.md). The honesty rule itself
is untouched: the solve cache still sits behind a narrow port with a boring
implementation, and the phase-8 replicated KV still has something to be compared
against. What changes is *which* boring implementation ships in phase 2.

## Context

ADR 0003's table names Redis as the boring implementation of the solve cache,
and `docker-compose.yml` has run a Redis since the first session.
`spring-boot-starter-data-redis` has been on `:modules:planner`'s classpath just
as long. So the cheap thing, and the thing the plan says, was to write a
`RedisSolveCache` and be done.

Two facts argued against it, and neither is about effort.

**Redis buys one thing here, and that thing does not exist yet.** A shared cache
is worth having when more than one process can answer the same question. There
is one process. Under [D1](../../TRACKER.md#d1--deployment-deferred-2026-09-02)
there is not even one *deployed* — nothing is hosted, no request has ever arrived
from outside a test. The `SolveCoordinator` that ships in the same change is
explicitly single-node. A distributed cache in front of a single-node queue is
infrastructure answering a question nobody has asked, which is the failure mode
the Track B gate exists to prevent, appearing on Track A.

**What it costs is permanent and is not the code.** Putting a `Plan` in Redis
means serialising it, and a serialised `Plan` is a format: written by one deploy,
read by the next. Every field added to `Plan`, `Explanation`, `StageRun` or the
typed identifiers becomes a compatibility question, and the failure is quiet — a
plan that deserialises into something plausible and wrong is served as an answer.
That contract would have to be versioned and tested from the day it exists, for a
benefit currently equal to zero.

An in-process `LinkedHashMap` has no format, no compatibility surface, and is
empty after a restart. Emptiness after a restart is the correct behaviour for a
cache whose entries cost a two-second solve to rebuild and whose keys are
content-addressed.

## Decision

`SolveCache` is the port: `get` and `put` over a `SolveKey`, nothing else. No
invalidation method — a key carries the game-data version, so a patch does not
stale an entry, it makes a different key under which nothing is stored.

`InProcessSolveCache` is the implementation that ships in phase 2: a bounded LRU
map, evicted by entry count, thread-safe, with hit and miss counters so that
whether it earns its keep is a measurement rather than a belief.

**`RedisSolveCache` is not written yet.** It is written when there is a second
node to share with — not before, and not on a date.

`storm-almanac.substrate.solve-cache` keeps its meaning and gains `in-process`
as the value that is actually selected today.

## Consequences

- A restart empties the cache. Acceptable: the entries are recomputable and the
  process holding them is the one that would have used them.
- Nothing is shared between processes, so the moment a second one exists the hit
  rate silently halves rather than failing. That is the reversal trigger below,
  and it is why the counters exist — a hit rate that falls when a node is added
  is the observation that says this decision has expired.
- ADR 0003's table now has a third column in practice: boring-and-local,
  boring-and-shared, hand-built. The comparison phase 8 exists to publish is
  against the *shared* one, so `RedisSolveCache` is not optional forever — it is
  owed before the replicated KV can be honestly benchmarked. A hand-built
  replicated cache measured against an in-process map would be measuring the
  network, and would flatter the hand-built side, which is exactly what ADR 0003
  forbids.
- The cache saves the arithmetic and never a database round trip: the key is a
  fingerprint of player state, so that state is read before a hit can be
  recognised. Anyone reading a load-test result needs to know this before they
  read it, so it is written at the call site too.

## Reversal trigger

**Write `RedisSolveCache` when a second process can serve the same profile** —
a second API replica, a solver worker split out of the web node, or the phase-9
solver cluster — **whichever comes first.** It is also owed, regardless of node
count, before any benchmark is published against the phase-8 replicated KV,
because that comparison is meaningless without a shared boring implementation to
sit beside it.

The reverse trigger for the port's shape: if a caller ever needs to enumerate,
invalidate or expire an entry by hand, the key has stopped describing the
question, and the fix is the key rather than the port.
