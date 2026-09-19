# 21. One step at several prices is a choice the solver makes

**Status:** Accepted · 2026-09-19

## Context

Punishing: Gray Raven's Memory Resonance is paid with **any one of** three
things, read side by side on one screen: 150 of a 5★ chip item, 234 Special
Support Tokens, or 246 Simulation Score
([the reading](../game-facts/punishing-gray-raven-research-disclosure.md)). That
is N32 (4) in the tracker: one state, several prices.

The model could not hold it at either end. `DemandResolver` threw on any state
with two incoming upgrades, saying that a choice was the solver's job. The schema
agreed from the other side: V2's `upgrade_edge_unique` allowed one row per
`(entity, from, to)`, on the grounds that "a second row would be a duplicate
cost". That was true of every game read before this one.

There were three ways to write it down:

- **A list of alternative cost sets on one `Upgrade`.** This means a new shape in
  the record, the format, the writer, the diff, and two new tables, and it
  changes the meaning of `costs` for every reader of it.
- **Pick one price in the bundle.** This hides a decision that depends on the
  reader. A reader with 7 716 Tokens and one with 366 401 Score should not be
  handed the same bill.
- **Several upgrades making the same move.** The record, the format and the
  writer do not change. The only thing refusing it was one constraint.

The third was taken. The same question comes up for the planner: where is the
choice made? The resolver cannot make it, because it knows nothing about stages
or inventories. The solver already makes exactly this kind of choice between
recipes. So the step is **demanded as an item**, the way ADR 0019 demands EXP.

## Decision

1. Two or more upgrades with the same entity, from-state and to-state are **one
   step at several prices**. `V9` drops `upgrade_edge_unique`, and the bundle
   format needs no change.
2. `DemandResolver` owes such a step as one unit of `Demand.choiceItem(step)`,
   a `choice:<entity>/<from>/<to>` id that no bundle slug can collide with. It
   writes the step as `a or b` in the plan's steps and counts it once across
   goals.
3. `EnergyMip` turns each price into a conversion that consumes the upgrade's
   costs and its progress, and makes that one item. The least-cost objective
   picks the price, holdings included. The plan's conversions name the upgrade
   that was paid.
4. **A route is not a price.** Upgrades that reach one state from *different*
   states, or behind *different* gates, are still refused. The states passed
   through, and so the gates paid, would change with the choice, and a demand
   vector cannot branch.
5. The shortfall page shows the step as one line, `one of: a, b, c`. The line is
   held when any one price is held in full.

## Consequences

- The solver adds one variable per price per step with several prices. These
  are pruned like any other conversion when nothing demands them.
- The shortfall line does not itemise the prices. A reader short of the step
  sees that they are short one step, and which prices would pay for it. How
  much of each price they are missing is not shown. The plan says which price
  it chose and what that costs to farm. **No frontend has rendered a `choice:`
  line.**
- The Memory Resonance row itself is **not** in the bundle yet. Two of its three
  prices are identified. The third is an item read only by its icon, "the 5★
  Memory Shard's icon". The screen also has Memory and Item tabs that were never
  read. Leaving out a price would make the plan dearer than the truth, not
  cheaper, but the row still waits for a reading of which item that is.
- The skill Resonance gives is random. The goal is written as reaching a
  Resonance state, so the plan is for *a* skill and not a particular one. That
  is honest only while every skill counts toward whatever the reader wants.

**Reverse this** if prices turn out to differ in something other than their
costs, such as a different result, a limit per price, or a cooldown. Then they
are different steps, and they need a representation that can say how.
