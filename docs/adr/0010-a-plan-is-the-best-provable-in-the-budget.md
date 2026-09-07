# 10. A plan is the cheapest one provable inside the budget

**Status:** Accepted · 2026-09-07

## Context

[ADR 0004](0004-solver-ojalgo.md) chose ojAlgo and said the model was expected to
stay small enough that a pure-Java solver would not bind, and that this was
"unmeasured until phase 2". Phase 2 measured it.

On the acceptance fixture the mixed-integer program is trivial: three stages, one
craft, an answer in single-digit milliseconds and provably optimal. On a real
Reverse: 1999 patch — 93 stage variables, 11 crafts, 23 item constraints, a
currency demand in the hundreds of thousands satisfied by stages yielding nine
thousand a run — ojAlgo's branch-and-bound **does not close the optimality gap**.
Left to run it dived depth-first until it exhausted the JVM stack, which is an
`Error` out of its own worker pool rather than a state a caller can read.

Three model changes fixed the crash and most of the cost, and each is worth more
than the tuning it replaced:

1. **Every variable is bounded.** Nobody runs a stage more times than would
   supply the whole goal set on its own, and an unbounded integer variable is an
   unbounded dive. The bound is computed from the demand expanded through the
   craft graph, so it excludes no optimal solution.
2. **The objective stays integral.** A millionth-weight tie-break on conversions,
   added to stop a lossless craft cycle spinning, cost the objective the
   integrality that lets branch-and-bound discard a node whose bound is within
   one of the incumbent. It is now applied only when the craft graph actually has
   a cycle.
3. **The gap tolerance is four significant digits, not seven.** Proving that no
   arrangement saves a hundredth of an Activity point is most of the running time
   and none of the value.

With those, a five-character goal set answers in **p95 1.8 s** — inside the
budget, and inside it because the budget stops the search, not because the search
finishes. The relaxation says the returned plan is **within 2.95%** of anything
that could exist.

## Decision

**A plan is the cheapest plan the solver could prove inside its budget, and it
says so when that is not the same as the cheapest plan.**

- The synchronous budget is two seconds for the whole answer. The search gets
  half; the explanation's re-solves get what is left, each bounded by the
  remainder, so the last one cannot run past the promise.
- When the search stops on the budget, the plan carries a note saying so **and
  the size of the doubt**, measured against the linear relaxation: *"no plan can
  be more than 2.95% cheaper."* Computing that bound is one extra linear solve
  over a model already built.
- Shadow prices are reported only when both the base solve and the marginal
  re-solve were proven optimal. The difference between two time-limited answers
  is noise with a number on it, and a number a player would act on.
- A solver failure is not an infeasible goal. The two are distinguished, and
  "the solver fell over" says so.

## Consequences

The product's promise is now "as cheap as we can prove in two seconds, and here
is how much cheaper anything could be", not "optimal". That is a weaker claim and
a more defensible one, and it is the claim the interface makes rather than a
caveat in a document.

A plan is still deterministic given its inputs — the bound, the tie-break rule
and the gap tolerance are all functions of the model — so
[the solve key](../../backend/modules/planner/src/main/java/io/stormalmanac/planner/SolveKey.java)
still identifies a plan. But a plan computed under a *different* budget may
differ, so the budget is part of the deployment's behaviour and not a knob to
vary per request.

Phase 2's exit criterion has two halves and this ADR only settles one of them.
p95 under two seconds is met and measured. **Agreement with community-accepted
answers on five benchmark goal sets is not**, and until it is, a 2.95% bound says
the plan is near-optimal *for the model*, which is a different claim from being
right about the game.

## Reversal trigger

Move to OR-Tools, as ADR 0004 anticipated, when either:

- the measured gap on a realistic goal set exceeds **5%**, which is a run or two
  of a stage on a week-long plan and the point at which a player could notice; or
- the benchmark goal sets show the returned plan disagreeing with the accepted
  answer in a way traceable to the search stopping early rather than to the
  model's contents.

Take the native-dependency and container-size cost then, and publish the measured
before-and-after — the gap and the p95, not just the p95.

Reverse the budget split instead of the solver if the explanation is the part
that binds: the shadow-price re-solves are a fixed multiple of the search, and
dropping to a dual-value approximation would buy back the whole of their share at
the cost of the number being about the relaxation rather than about the plan.
