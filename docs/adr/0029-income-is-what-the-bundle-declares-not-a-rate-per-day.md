# 29. Income is what the bundle declares, not a rate per day

**Status:** Accepted · 2026-09-22

## Context

`IncomeModel` has been an interface with no implementation since the gacha
module was written, and its javadoc named the question it existed to answer:
*"I have this much, she arrives in 40 days, what is my probability of
guaranteeing her?"* [ADR 0018](0018-the-gacha-engines-answer-one-question-about-one-rarity.md)
settled the second half to three decimals — two independent engines, worst gap
0.110 points over 108 questions — and the first half could not be attempted,
because **neither `BannerModel` nor the `banner` table said which item is pull
currency or what a pull costs in it**. That is N28, open since Phase 5 closed on
2026-09-12.

Both numbers have been read since 2026-09-18: **250 Event Construct R&D Tickets
a pull, 2 500 for ten**, off the pool screen's own buttons in the same sitting
as the rates
([the note](../game-facts/punishing-gray-raven-research-disclosure.md)). What
kept them out of the bundle was not the reading. It was that a price names an
`Item`, `Item.rarity` is required, and the 2026-09-18 sitting graded no
currency — the note lists the currencies among the things the format refused.
The ticket's tile was opened on 2026-09-22 and grades it **five stars**, which
is the fourth currency read that way after Simulation Score, Phantom Pain Scar
and Cogs, each of which carries a grade on a tile its own item card does not
show.

That leaves the other half: what accrues. The tempting implementation is a rate
per day — *"PGR gives about 30 pulls a patch"* — and it is exactly the number
this project does not ship. It appears on no screen, the best version of it is a
community estimate, and it would be a game-specific constant inside a module
that is not allowed one.

## Decision

1. **`PullPrice(ItemId currency, int perPull)`, nullable on `BannerModel`,
   `V15`.** Null means **unstated**, not free — the same distinction
   [ADR 0025](0025-the-day-boundary-is-a-property-of-the-game.md) draws for the
   day boundary — and unlike the boundary there is no default to fall back on.
   `BannerModel.pricedPull()` refuses by name rather than returning a zero:
   *"cannot be afforded"* and *"nobody read what it costs"* are different
   answers, and a screen showing the first for the second is wrong in the one
   way a reader cannot detect.
2. **One price, not a list.** PGR prices ten pulls at exactly ten times one, so
   there is no discount to express, and a second field for one would model a
   rule no read game has.
3. **The currency hangs off the banner, not off the game.** PGR has four pools,
   each with its own ticket, all priced 250 / 2 500. A column on `Game` would
   have had to pick one of the four.
4. **Accrual is the sum of the `Reward` rows that grant the currency, at their
   own cadence, over the horizon.** Every term is a fact somebody read.
   `Reward.Cadence.occurrencesIn` already rounds against the player, and this
   reuses it rather than writing a second calendar.
5. **A game whose grants nobody has written down accrues zero and says so.**
   That is PGR today: no reward in its bundle pays a research ticket, so
   `DeclaredIncomeModel` reports the balance and nothing more. The gap is a
   missing reading rather than a wrong number, and it is legible as one.
6. **`reach` applies here exactly as it does to a plan.** A grant behind a score
   the reader has not answered for is dropped and **named**, never assumed
   ([ADR 0022](0022-a-grant-sized-by-the-player-is-an-answer-the-reader-supplies.md)).
   A reader whose budget is short by exactly the weekly they did not answer for
   deserves to be told which question would move it.
7. **Days, not a date.** The interface took a `LocalDate`; it takes a horizon in
   days. Nothing in the computation is indexed by which day it is, only by how
   many there are — [ADR 0013](0013-the-horizon-is-a-scalar-not-an-index.md)'s
   argument, unchanged.
8. **The budget is three numbers and not one.** Held, accruing, price; the
   caller divides nothing. Same reason `ShortfallLine` carries three: a reader
   who disagrees with "four pulls" needs to see which half they disagree with.

## Consequences

- **PGR sequence 7 declares the ticket and the price**, and the ticket's grade
  gets its own provenance entry dated 2026-09-22, because it was read in its own
  sitting. The price is credited to `research-pool-panels`, whose detail line is
  widened to say that the pull buttons were on the same screen as the panels —
  a claim about what was in front of the reader on 2026-09-18, made now because
  there was nowhere to put it then.
- **No plan moves.** The solver does not pull, nothing in the bundle spends a
  ticket, and no route calls this. What changes is that a question which could
  not be asked can be.
- **Nothing calls it yet**, and that is unchanged from before: there is still no
  route, no screen and no bean for the gacha module, and `PityState` is stored
  nowhere. This closes the modelling half of N28 and not the wiring.
- **The multi-copy pessimism is *not* fixed here.** R1999 sells a copy of the
  featured unit for 200 Cassettes of the Lost, so at two copies the engines say
  280 pulls where the truth is 200. That wants a copy-exchange on the banner
  *and* both engines changed to spend it — the chain's state space grows — and
  it wants an R1999 bundle to exercise it, which does not exist first-hand. It
  is a separate piece of work rather than a field, and adding the field now
  would put a column in the schema that no bundle can fill.
- **Three of PGR's four tickets are not written down.** Target Weapon, CUB and
  Basic Weapon R&D Tickets are read and all priced 250 / 2 500, and none of
  their pools is a banner in this bundle. An item nothing spends is a row with
  no reader.

**Reverse decision 2** if a published game discounts a multi-pull — then the
price is a list of `(count, cost)` and the single field is a special case of it.
**Reverse decision 4** if a game's pull income is genuinely not expressible as
declared grants — a battle pass, a currency conversion chain like PGR's own
Rainbow Card → Black Card → ticket, or paid income. The conversion chain is the
likely one: it is read, it is real, and `Craft` may already express it, at which
point accrual has to walk conversions rather than sum grants. **Reverse decision
5** the moment somebody argues that zero accrual is worse than an estimate; the
answer in this repository has consistently been that it is not, and an estimate
that turns out low is a reader who missed a banner.
