# 20. A limit that never resets is offered whole, and the plan says so

**Status:** Accepted · 2026-09-19

## Context

Punishing: Gray Raven sells each character's shards in the Phantom Pain shop:
thirty per character, ever, the first ten at 10 Scars and the next twenty at 20.
Thirty is exactly what Evolve S → SS costs. `Shop` capped a purchase at
`periodLimit` per `java.time.Period`, and a limit that never resets has no
period, so sequence 1 of the launch bundle held the Evolve row and not the shop
that pays for it (N32 (3) in the tracker). Reverse: 1999's permanent stocks
("five, ever") had hit the same wall a week earlier.

Two questions, one about the format and one about the plan.

**How to write "never".** A separate enum (`limitPeriod: DAILY|WEEKLY|PERMANENT`)
was on record as the maintainer's suggestion. But a `Period` already says
everything the other values say, so the only missing value is the absence of one.
It is a `null` period, written `"never"` in a bundle and in `period_iso`, because
ISO-8601 has no word for it and the column is text already. **No migration**:
the column takes the new word as it is, and every version published before this
reads exactly as it did.

**How much of it is left.** `Shop.purchasesIn` rounds against the player: only
whole periods count, because nothing says how much of the current period is
spent. Applied to a stock that never refills, the same rule gives zero for
everybody, which is the same as not having the row. Nothing a player records says
how much of a lifetime allowance they have bought. The inventory holds what they
own, not what they bought, and the roster holds states, not purchases.

## Decision

**A limit that never resets offers its whole allowance to any horizon of a day or
more, and every plan that buys from one says so in its notes**, naming the offer
and the count, and saying that whatever has been bought must come from
elsewhere. This is the one place a plan rounds *for* the player. The note is what
makes that honest. A reader who bought ten shards last month knows it, and the
note tells them the plan does not.

A price that rises partway through a limit is **two offers**, the cheap one
capped at the discounted count. They share no cap and need none, because a
least-cost plan buys the cheap one out first without being told.

## Consequences

- The launch bundle's sequence 2 carries both shard rows, and Evolve S → SS plans:
  a reader holding 2 shards and 460 Scars is sold 10 + 18 and farms nothing
  (`AuthoredBundlePlanTest`).
- Nothing in the bundle pays Scars yet (N32 (5), the score-sized weekly Cage),
  so the plan works only for a reader who already holds them. **A reader one Scar
  short is refused with the solver's generic "no combination" message.** The
  item-by-item diagnosis looks for items with no source, and a held stock is a
  source. That was already true of any held currency; this is the first plan
  where it is the usual case.
- One cap over several offers still cannot be written. Neither can an order the
  game enforces that cost does not, such as a dear tier that has to be bought before
  a cheap one.
- A player who has spent part of an allowance is over-served until the player
  model can hold it.

**Reverse** when the player model holds purchase history, or a "bought so far"
count per offer. Then the allowance is the limit minus that count, rounded
against the player like every other limit, and the note goes away.
