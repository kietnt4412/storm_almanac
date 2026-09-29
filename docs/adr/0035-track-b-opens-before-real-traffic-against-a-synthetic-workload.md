# 35. Track B opens before real traffic, against a synthetic workload

**Status:** Accepted · 2026-09-29 · supersedes the gate paragraph of [ADR 0003](0003-the-honesty-rule.md)

## Context

ADR 0003 said Track B does not start until the product is publicly deployed with
real traffic, because infrastructure written for a system with no users is
written against imaginary requirements. On 2026-09-29 the product is deployed
and has users — five strangers completed a plan, and a signed-in reader asked
the pull planner on production — but it has no traffic worth engineering
against, and Phase 6, which would produce the drop-report stream the storage
engine is meant to serve, has no reports because it has no submissions.

The maintainer chose to open the gate anyway, to give the project its Track B
shape now rather than at a date nobody can name. Phase 7 (`almanac-store`) was
chosen over Phase 8 because it is single-node and in-process, so it is the one
most likely to be *finished* — and the cut list's rule is that one finished
engine is worth more than half of two.

## Decision

1. **Phase 7 starts on 2026-09-29.** The engine is built in the order
   `plan.html` gives: WAL and memtable, SSTables, compaction, recovery, then
   the crash-consistency fuzzing that is its exit.
2. **The workload is synthetic, and says so.** Every benchmark, figure and
   writeup produced before real reports exist names its workload as generated,
   states the generator's parameters, and commits the generator. A number
   measured on a generated workload is never quoted without that label.
3. **The boring implementation is written first.** `DropReportStore` has no
   Postgres implementation yet — it is Phase 6's. Phase 7 writes it before the
   LSM adapter, because a benchmark with one side is not a benchmark, and ADR
   0003's rule that both stay selectable by `storm-almanac.substrate.*` is
   unchanged.
4. **The port is not reshaped for the engine.** Where `DropReportStore`'s
   meaning is unsettled, the question goes to Phase 6's side of the seam and is
   answered there, not by what an LSM tree finds convenient.
5. **Phases 8–10 stay gated.** This ADR opens Phase 7 only.

## Consequences

The engine is sized against a guess. Its memtable size, block size, bloom filter
bits per key and level fan-out are chosen from a generated distribution, and
the first real traffic may show every one of them wrong. **Concluding that
Postgres won remains an allowed outcome**, and on a synthetic workload it is a
likely one.

## Reversal trigger

When real drop reports arrive (Phase 6's submissions in production), re-run the
published benchmark on that stream, publish the result beside the synthetic one
rather than over it, and re-tune from the real numbers. If Phase 7 is still
unfinished when that happens, the remaining work is sized against the real
stream instead.
