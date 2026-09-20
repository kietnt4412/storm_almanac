# 22. A grant sized by the player is an answer the reader supplies

**Status:** Accepted · 2026-09-20

## Context

Punishing: Gray Raven's weekly **Phantom Pain Cage** pays Phantom Pain Scars in
nine claim tiers, from 4 Scars at 30 000 progress to 9 at 1 100 000
([the reading](../game-facts/punishing-gray-raven-research-disclosure.md)). The
tiers add to **56 a week**, and the Scar is the only currency the Phantom Pain
shop takes: Evolve to SS is 30 shards, 500 Scars, **at least nine perfect
weeks**. That is N32 (5) in the tracker, and it was the last shape the first
first-hand bundle had read and could not write down.

Every way of writing it as game data is wrong, because **the size of the grant
is not a fact about the game**:

- **The 56.** Promises every reader the top tier. A reader who clears 30 000 is
  handed a nine-week plan that is really a seventy-week one.
- **The bottom tier.** A different reader's week, and it buries the mode: 4
  Scars a week says Evolve is out of reach when for many readers it is not.
- **Leaving the mode out**, which is what sequence 3 did. A plan for Evolve
  charges 500 Scars and names no way to earn one, and the refusal — measured in
  this session — did not mention the Cage, the Scar or the score at all.

The project already has a fact of exactly this kind and already knows where it
goes: **`energyPerDay`**. Nothing the client shows tells this project how much
energy a given account regenerates or is willing to buy, so the account's owner
says, on the request, and `PlanController` refuses to guess it. A weekly score
is the same thing one step further out.

## Decision

1. **A `Reward` may carry a `Requirement(measure, atLeast)`.** The measure is an
   **opaque label** the bundle supplies — `phantom-pain-cage-score` — matched
   against what the reader says and never interpreted, like a state and like
   `Progress.kind`. It is deliberately **not an item**: nobody holds a stack of
   it, nothing converts into it, and spending it is not a thing that happens.
   `V10` adds two nullable columns, both or neither.
2. **A ladder is one reward per rung.** Nine tiers are nine `WEEKLY` rewards
   with nine bars, and the format learns nothing about ladders. A reader at
   120 000 clears three of them, which falls out of the comparison rather than
   out of a rule.
3. **The reader answers on the request.** `SolveRequest.reach` is a map from
   measure to score, `PlanRequest.reach` carries it over the wire, and
   `SolveKey` hashes it — two readers with the same items and goals who clear
   different weeklies are asking different questions and must not share a cached
   plan.
4. **Silence counts nothing, and is never an error.** An unanswered measure is
   zero, a grant behind a bar the reader has not cleared is **dropped before the
   model** rather than bounded at zero inside it, and the reported counts are
   therefore the game this reader plays. Being wrong this way makes a plan
   **dearer** than the truth, which is the same direction `Cadence.MONTHLY`
   rounds and the same direction ADR 0011 discounts a small sample.
5. **What was withheld is reported.** Any grant the plan could have used and was
   not allowed to count appears in the notes with its bar and what the plan was
   told. A refusal follows the same thread: an item granted only out of reach,
   or a shop whose **currency** is granted only out of reach, names the lowest
   bar and what the reader said, instead of "nothing available can supply it".

## Consequences

- `EnergyMip` gains no variables. The change is a filter and a message.
- **A measure is not declared anywhere.** There is no list of them, no display
  name and no description, so a screen that wants to ask "how far do you get in
  the Phantom Pain Cage?" has to collect the measures off the rewards it can see
  and show the reader a slug. That is the same trade the opaque state made, and
  the moment a screen asks the question it is the first thing to hurt.
- **No frontend sends `reach`.** The field is absent from the web client, so
  every plan it asks for today counts no scored grant and carries the note
  saying so. The note renders, because the plan view renders every note.
- **The reader is not asked how confident they are.** "I reach 1 100 000" is
  taken as "every week, for the whole horizon". A reader who clears it in a good
  week and not a bad one is promised income that may not arrive — the one
  direction this decision can be generous in, and it is the reader's own claim.
- Nothing stores the answer. It is retyped per request, and a second device
  retypes it again.

**Reverse this** — and make `reach` player state alongside the inventory, with
its own schema, sync clock and API — the moment a reader has to give the same
answer twice, which is the moment a screen asks for it. Reverse the whole shape
if a game turns up whose payout is a *function* of the score rather than a
ladder of bars, because a function is not a set of rungs and cannot be written
as one.
