# 34. Pull income walks conversions into the pull currency

**Status:** Accepted · 2026-09-28 · **supersedes decision 4 of
[ADR 0029](0029-income-is-what-the-bundle-declares-not-a-rate-per-day.md)**

## Context

ADR 0029's fourth decision counted accrual as the `Reward` rows that grant the
pull currency itself, and named its own reversal: *"a currency conversion chain
like PGR's own Rainbow Card → Black Card → ticket … `Craft` may already express
it, at which point accrual has to walk conversions rather than sum grants."*

On 2026-09-28 the maintainer read what Punishing: Gray Raven's missions pay, for
C1 (the pull planner): **30 Black Cards for all the dailies, 1 000 for all the
weeklies**, and the Phantom Pain Cage's 1 000 000 tier pays 15 more. None of it
is the Event Construct R&D Ticket a pull spends. **Direct Exchange** on the pool
screen trades Black Cards for tickets one for one. Under decision 4 every one of
those grants counts zero, and so does the balance: an account holding 21 893
Black Cards and no tickets affords no pulls. That is not "dearer than the
truth"; it is a different game.

## Decision

1. **A conversion is a `Craft` with one input, one output, different items and
   no closing date.** That is exactly what an exchange is. A craft with several
   inputs or outputs is a recipe, and opening a box that yields a ticket and
   something else is not a price anyone would quote. A craft that closes is not
   counted on, because the horizon can outlast it.
2. **An item with a chain of conversions ending at the pull currency is counted
   as currency** — what the inventory holds of it, and what counted grants pay
   of it. Where several chains exist, the one yielding most currency per unit is
   taken. The walk goes backwards from the currency and never revisits an item
   on one chain, so a pair of crafts trading two items back and forth neither
   loops nor mints anything.
3. **Whole lots only, at every step, held and accrued apart.** A remainder buys
   nothing further on, and what an account holds today and what it will earn are
   two exchanges. Both round against the reader, as ADR 0029's whole pulls do.
4. **The budget says what it converted.** `PullBudget.converted` lists each item
   counted, the crafts it went through, and how much of it was held and granted,
   so a screen can say "includes 21 893 Black Cards at 1:1" and a reader who
   disagrees can see with what.
5. **`reach` applies to a converted grant exactly as to a direct one** (ADR
   0022): the weekly behind every weekly mission is dropped and named until the
   reader says they finish them.

## Consequences

- **The exchange is a bundle row like any other**, so it has provenance and a
  sequence that corrects its rate moves every budget. It is also visible to the
  planner, which costs conversions at zero energy; nothing demands a ticket, so
  no plan moves.
- **Paid currency is still not income.** Rainbow Cards convert into Black Cards
  at 10:1 and would be walked if the bundle carried that exchange, but only as a
  *held* balance: no reward grants them, and nothing here models buying them.
- **Nothing calls it yet** beyond the tests; C1's route is the first caller.

**Reverse decision 1** if a game's exchange is capped per period, which a
`Craft` cannot say — then it is a `Shop`-shaped conversion and the walk has to
honour the limit. **Reverse decision 2's best-rate choice** if a game ever
offers two standing exchanges for one item whose better rate a reader cannot
actually use.
