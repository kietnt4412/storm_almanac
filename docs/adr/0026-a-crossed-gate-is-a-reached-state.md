# 26. A crossed gate is a reached state

**Status:** Accepted · 2026-09-21

## Context

`Roster` holds **one state per entity** — `Map<EntityId, String>`, and
`player.roster_entry` keys on `(profile_id, entity_slug)` with a single
`current_state` column. An entity has several tracks: a level, a Promote rank,
skill ranks, an evolution. A player can therefore record where they are on
exactly one of them.

`DemandResolver.achieved` walked backwards from that one state through
`fromState` only. Everything on every other track read as unreached, so a goal
on a gated track was charged its gate's entire ladder from the beginning.

**[N33](../history/tracker-archive.md#completed-next-actions) turned this from a
latent flaw into a live wrong answer.** Before PGR sequence 6 only the last
Promote step carried a `requires`, so only a reader asking for HERO paid a level
they might already hold. Sequence 6 gates all thirteen steps. A reader recorded
at `promote-6` asking for step 7 was billed **60 000 character EXP** — the whole
ladder from Lv 1 — for a Lv 50 the game had already made them reach.

The obvious fix is to let the roster hold a state per track. It is also the
expensive one: a migration, a breaking change to the `GET`/`PUT`/`PATCH`
`/api/me/profiles/{profile}/roster` wire format, the offline sync patch shape,
and a frontend that has never rendered this game. That work may still be right.
It is not what this decision is about.

**The cheaper fix is exact rather than approximate, which is why it comes
first.** A gate is not a hint about a player; it is a condition the game itself
enforced before letting them take a step. A player standing past that step
*demonstrably* satisfied it. The roster never needed to say so — the upgrade
graph already knows.

## Decision

**`achieved` credits the gates of every upgrade behind the player, not only the
states those upgrades came from.** Walking back from the recorded state, each
upgrade crossed contributes its `fromState` as before *and* the states in its
`requires`, which are then walked back in turn.

1. **Only what every parent demands is claimed.** Several upgrades arriving at
   one state are either one step at several prices — which share their gates by
   `pathTo`'s own rule, so the intersection is that shared set — or different
   routes, where which one was taken is unknown. The intersection is the most
   that is certain. `pathTo` refuses the second shape outright, so this is a
   safeguard rather than a live case.
2. **Nothing game-specific enters the planner.** There is no notion of a track
   in the code, before or after. A track is a connected run of the graph, and
   crediting a gate is one more edge to follow.
3. **The roster, the schema and the wire format are untouched.** No migration,
   no API change, no frontend change, no published version moves.

## Consequences

- **The bug this was written for is gone.** A reader at `promote-6` asking for
  step 7 now pays the **20 000 EXP between Lv 45 and Lv 50**, not 60 000 — three
  runs instead of six, 90 Serum instead of 180. `AuthoredBundlePlanTest` states
  it against the published bundle.
- **It only helps a player whose recorded state is *ahead* of the gate.** A
  reader at `promote-0` who has levelled to 80 anyway is still charged the level
  ladder, because nothing they have recorded implies it and PGR does not cap
  level by rank — the maintainer confirmed that on 2026-09-21. **That half needs
  the roster to hold more than one state, and this ADR does not do it.** The
  live tracker carries it as an open flaw rather than a closed one.
- **A plan can now be cheaper than before for the same inputs.** That is the
  point, and it is the direction that needs the most care: this project would
  rather overcharge than promise a reader something they cannot afford. The
  claim is safe only because a gate is a fact about what the game enforced, not
  an inference about what a player probably did.
- **It assumes a gated state cannot be lost.** Every state in both published
  games is monotone — a level, a rank, an evolution. Nothing checks this, and
  nothing can: states are opaque strings and the planner does not get to
  interpret them.

**Reverse this** if a published game has a state that can be given up after
being used as a gate — a rank that decays, a resource-backed level that can be
refunded, a seasonal track that resets — because then standing past a step stops
being proof the gate is still met, and the walk has to stop at the gate rather
than pass through it. Reverse decision 1 if a route ever becomes a modelled
choice: the intersection is only the safe answer while `pathTo` refuses to walk
a state reachable two ways, and a modelled route would let the resolver know
which gates were actually crossed instead of guessing conservatively.
