# 36. Track B is cut, and parked

**Status:** Accepted · 2026-09-29 · supersedes [ADR 0035](0035-track-b-opens-before-real-traffic-against-a-synthetic-workload.md)

## Context

ADR 0035 opened Phase 7 that morning so the project would have its Track B
shape before it went on the maintainer's CV. Its first slice was built: the
write-ahead log, the memtable and recovery (`8d0d891`). Once the four phases had
been laid out — an LSM storage engine, Raft, a solver cluster on Raft, and a
Jepsen-style chaos harness — the maintainer judged them senior-level work. For
someone just out of university, the risk is not difficulty. It is a CV line
that cannot be defended when an interviewer asks how it works.

The product side already carries the story a new graduate needs: deployed,
used by strangers, an optimizer, exact gacha probabilities, offline sync,
continuous deployment, and decision records for all of it.

## Decision

1. **Track B — Phases 7, 8, 9 and 10 — is cut from the project's scope.**
2. **It is parked, not deleted.** The tracker's text for it (the gate, the
   phase table, the Phase 7 slices, the cut-list entries, D7) moves verbatim
   to the archive under *Track B — parked 2026-09-29*, with a guide for coming
   back.
3. **P7.1's code stays in the tree.** `backend/substrate/almanac-store` keeps
   building and its tests keep running, so it cannot rot while nobody looks at
   it. `almanac-raft` and `almanac-chaos` stay as the empty modules they were.
4. **ADR 0003 stands.** Its honesty rule — every hand-built component behind a
   boring one, the benchmark published either way — is what applies if Track B
   returns. Its gate paragraph is still superseded (by 0035, which this
   supersedes in turn): returning needs an ADR, not traffic.
5. **Phase 12 loses two of its three writeups** — the storage benchmark and the
   consensus verification. The multi-game diff stays.

## Consequences

The project no longer claims hand-built infrastructure. `DropReportStore` stays
a port with, for now, no implementation; Phase 6 writes the Postgres one. The
build carries a small library nothing calls, on purpose.

## Reversal trigger

The maintainer chooses to return to Track B. An ADR superseding this one says
why, and the archive's resume guide is followed: Phase 7 first, resuming at
P7.2.
