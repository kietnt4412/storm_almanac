# 13. The planning horizon is a scalar, not an index

**Status:** Accepted · 2026-09-08

Gives the optimizer a time axis without giving it a calendar. Supersedes nothing;
it fills the hole [ADR 0010](0010-a-plan-is-the-best-provable-in-the-budget.md)
and the phase-2 close both named — "the model has no time axis" — and it decides
the one thing that hole could have been filled several ways.

## Context

Three things a plan should know are all facts about time, and the model knew none
of them:

- **Energy is finite per day.** Without that, "cheapest" has no bound and a plan
  is a number rather than a schedule.
- **Some income is free and arrives on a cadence.** Dailies, weeklies, monthlies.
  A model that cannot see them tells a player to farm what they were going to be
  given.
- **Some stages are only open some days.** A stage open on Tuesdays and Fridays
  is not the same as a stage open always, and treating it as one is how a plan
  becomes unexecutable rather than merely dear.

It also left `Objective.FEWEST_DAYS` answering with the least-energy plan and a
note admitting it, because with no time in the model days were energy divided by
a constant.

**The obvious formulation is to index every variable by day.** `x[s,d]` for each
stage and each day of the horizon. It is exact, it is standard, and it is what
the domain model's own comment on `Availability` anticipated: *"which is what
makes the optimizer time-indexed rather than a single static LP"*.

The obvious formulation was measured against what phase 2 actually closed on.
The p95 on a real patch is **1 805 ms against a 2 000 ms budget** — 90% of it —
with roughly a hundred stage variables. A thirty-day index makes that three
thousand, and branch-and-bound does not degrade linearly in the number of integer
variables. There was no plausible reading under which the exit criterion phase 2
was closed on would survive.

The second fact is what makes the alternative available. Every consequence of
time listed above is **linear in a fixed horizon**:

| What time makes true | As a constraint on a fixed `D` |
|---|---|
| Energy is finite | `sum_s x_s * energy_s <= D * energyPerDay` |
| A cadence is a count | `z_r <= occurrences(cadence, D)`, exact because `z_r` is an integer |
| Rotation is a shared capacity | `sum_{s in S} x_s * energy_s <= (matching days in D) * energyPerDay` |

None of those needs to know *which* day anything happens on. They need to know
how many of each kind of day the window holds, which is arithmetic on the start
date and the length — not a decision the solver has to make.

## Decision

**The horizon is a scalar parameter of the model, and every consequence of time
is a capacity computed from it.** `SolveRequest` carries `horizonDays`;
`EnergyMip` reads it and writes rows. No variable is indexed by day.

- **`LEAST_ENERGY` is solved at the horizon the caller asked for**, and takes all
  of it. That is not laziness: waiting is free, so the cheapest plan always
  spends the whole window. The horizon is therefore not a formality — **without
  it "wait" is free and unbounded, and least energy has no answer.**
- **`FEWEST_DAYS` is solved by binary search over the horizon**, because
  feasibility is monotone in it: another day adds energy, may add a cadence
  occurrence, may add a day a rotating stage is open, and takes nothing away.
  Five probes bisect thirty days. The final answer is then a least-energy solve
  pinned to the horizon the search settled on — so the two objectives are two
  searches over one model, not two models.
- **Rotation is enforced over subsets of restrictions, not group by group.** Two
  sets of stages can each fit the days they are open and between them want more
  Tuesdays than the window holds. The condition that rules that out is one row
  per *subset* of distinct restrictions — `2^k` in the number of distinct
  restrictions, which is one for a game with no rotation and three for the
  acceptance fixture, not `2^stages`. Past six distinct restrictions it degrades
  to one row per restriction plus a total, and the plan says so.
- **Reward claims are variables with a cadence cap**, and every cadence rounds
  against the player: a month is 31 days, and an event or one-off handout counts
  once however long the horizon, because its schedule is not in the bundle.
- **Shops are still not variables.** The cap they need now has somewhere to live;
  what is missing is data. See the consequences.

## Consequences

- **The phase-2 p95 survives, measured rather than hoped.** On the real patch the
  time axis adds one capacity row and no integer variables, because that upstream
  declares no rewards and no rotation. p95 went **1 805 ms → 1 807 ms**, and the
  benchmark's load-bearing claim — 3 880 Activity against the guide's 4 017, nine
  agreements — is unchanged to the unit.
- **The two objectives are now genuinely different plans**, and the acceptance
  fixture shows the trade: Insight 1 costs **nothing over 28 days** or **370
  energy over 2**. Neither was reachable before; the old model returned one plan
  and a note apologising for it.
- **What is given up is joint reasoning about *which* day.** Two stages both
  closing on Friday are each bounded by the days they have left and are not
  bounded together; energy is allowed to split across days as if continuous, so a
  plan is not asked to prove each individual run fits inside one day's budget. On
  real numbers — a run costs tens, a day supplies hundreds — that is slack nobody
  can act on. It would stop being slack for a game whose stages cost a
  meaningful fraction of a day.
- **A plan now depends on the player logging in.** Free income is the reason a
  plan is cheap, so the plan reports the grants it leans on by name and count.
  Reward claims carry the same millionth-of-a-unit tie-break that keeps pointless
  crafts out, paid only by games that declare rewards at all — so the real patch
  that once overflowed ojAlgo's stack still gets an integral objective.
- **Shops are unblocked in the model and still blocked in the data.** A `Shop`
  variable is now a dozen lines: a cap of `periodLimit * periods(period, D)` and a
  currency coefficient. It is not written because the one upstream this project
  reads publishes a shop table with six opaque keys and 69 `{Material, Quantity}`
  rows — no currency, no price, no reset period, no way to tell an offer from its
  cost. **The refusal is now a data refusal, not a modelling one**, and its
  message says so.
- **Rewards and rotation are in exactly the same data position as shops**, and
  this should be said plainly rather than discovered later: `KornblumeAdapter`
  emits `Availability.ALWAYS` for every source and no rewards at all, so on the
  only real upstream read so far this whole change is exercised by the energy
  budget alone. It is proven end to end on the synthetic acceptance fixture,
  which is the same standing as the catalog axis has had since phase 1.

## Reversal trigger

**Index the variables by day when a game arrives whose data makes a
same-day-shared constraint bind** — a stage whose runs cost a meaningful
fraction of a day's energy, two expiring stages competing for the same final
days, or an income source whose amount depends on which day it is collected. Any
of those makes the continuous-energy approximation above wrong rather than
slack, and no amount of subset rows fixes it.

The cost of that reversal is the phase-2 budget, so it comes with a second
obligation: **re-measure the p95 before adopting it, and if it cannot be met,
that is the trigger for the OR-Tools fallback [ADR 0004](0004-solver-ojalgo.md)
names — not for quietly widening the budget.**

Separately: **write the `Shop` variable the moment an upstream publishes a price,
a currency and a reset period for one.** The model is ready and the refusal is
tested; only the data is missing.
