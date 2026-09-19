# 19. A gate is a goal inside a goal, and progress is demanded as an item

**Status:** Accepted · 2026-09-19

## Context

The first first-hand Punishing: Gray Raven bundle was published as sequence 0
with two things it had read and could not hold (N32 (1) and (2) in the tracker):

- **Gates.** Every Promote step is gated on a character level, Vestige on an
  Evolve rank. `Upgrade` had costs and no preconditions.
- **EXP.** A level is paid in EXP, and a weapon's or a Memory's Overclock carries
  an Enhance to 45 paid in EXP too. The bundle already had fodder rules saying
  what a Pod is worth, and nothing that could need what they make.

Both made plans cheaper than the truth. Samantha's Overclock planned at 240 Serum
without its 18 000 EXP; the true figure is 420. Helentine's last rank planned at
180 without the level 80 it is gated on; the true figure is 1 470. The goals in
`AuthoredBundlePlanTest` had been chosen from the ones the bundle could express
in full, which is a way of not looking.

Three shapes were available for each.

**A gate** could be a precondition the solver checks, a precondition the resolver
checks, or a cost. Checking only says "you cannot do that yet", and a whole-run
plan is never *yet*: it is going to do everything. What a gate changes about a
whole run is its price, because the state it names has to be reached and reaching
it costs something. So a gate is resolved as **a goal inside a goal**: paying for
a gated step first pays for the path to the state it requires, recursively,
counted once however many steps and goals share it.

**Progress** could be an item, a new sink type with its own model rows, or an
item in all but name. An item is wrong: nothing holds EXP, and an inventory
editor offering "character-exp: 0" invites a reader to type a number that means
nothing. A new row type means a second demand vector, a second balance row in
`EnergyMip`, and a second input to `SolveKey`, whose one job is to be a complete
description of the question. So progress is **demanded as an item** — a
`progress:<kind>` id in the same demand map, which no bundle slug can collide
with — and each fodder rule becomes one conversion per eligible item, making that
progress item out of a unit of fodder and its side costs. The solver then prices
"buy Pods, feed Pods" the same way it prices "buy boxes, open boxes", and the
cache key covers progress because it already covers the demand map.

## Decision

1. `Upgrade` gains `requires`, a list of states **of the same entity**, and
   `progress`, a list of `(kind, quantity)`. `Fodder` gains `progress`, the kind
   it pays. The bundle format gains the same three optional fields and `V8` the
   tables to hold them.
2. `DemandResolver` pays a gated step after the path to each state it requires.
   A step gated on a state reachable only through itself is refused by name.
3. Progress is a `Demand.progressItem(kind)` line; `EnergyMip` turns each fodder
   rule with a kind into conversions; the shortfall view counts held fodder at
   face value, because "EXP owned: 0" is true of the inventory and false of a
   reader holding a thousand Pods.
4. A fodder rule with no `progress` is **inert** rather than refused. Every rule
   published before this change has none, a version is immutable, and the rules
   for reading one must not tighten under it.

## Consequences

- A gate is only worth writing where its state has a price. Helentine's twelve
  lower Promote gates stay prose: the EXP to any level below 80 is unread, and a
  `level-40` no row reaches would be a gate every plan meets for free. **So
  promote-13 carries the whole level and promote-12 carries none of it**, though
  the game gates it on 75. A partial ladder is still too cheap.
- A gate on another entity is not expressible, on purpose. The one read so far —
  an Awaken tier wanting twelve equipped Resonance skills — is paid on the
  equipment, and an entity paying another's price is a different shape.
- The roster holds one state per entity, so a gate on a second track is
  satisfied only if the reader's one recorded state is on or past it. A reader
  recorded at `promote-12` is charged the whole level track again. This was
  already true of every multi-track entity; gates make it matter more.
- Anything reading a demand line as a catalog item has to ask
  `Demand.isProgressItem` first. The plan's shadow prices will name
  `progress:character-exp`, and the frontend has never rendered one.

**Reverse the progress half** if a game turns out to hold progress as a real
stackable thing a player owns and trades — then it is an item, and should be one.
**Reverse the gate half** if a gate appears whose state costs nothing to reach
but still orders steps in time; then it is a scheduling constraint, and belongs
in the solver's calendar rather than in the demand.
