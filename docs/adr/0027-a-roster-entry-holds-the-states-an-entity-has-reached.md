# ADR 0027 — A roster entry holds the states an entity has reached, not one state

**Status:** accepted, 2026-09-21
**Supersedes:** nothing. **Completes** [ADR 0026](0026-a-crossed-gate-is-a-reached-state.md),
which closed half of this and said which half it was leaving.

## Context

`Roster` held `Map<EntityId, String>`: one entity, one state. That says an
entity is somewhere, once.

Neither published game looks like that. A PGR construct has a level, a rank, an
evolution and six skills, and the game ties none of them to each other — a
player may stand at **Lv 80 and rank 0**, and the maintainer confirmed on
2026-09-21 that PGR permits exactly that, because levels are not capped by rank.
R1999 is the same shape. One state per entity cannot write that down.

What it cost was a plan dearer than the truth. A reader who recorded a rank and
nothing else was charged the whole level ladder every time a rank step was gated
on a level, because nothing they had recorded said they had climbed it.

ADR 0026 removed half of that. A gate is a condition the game enforced, so a
player standing *past* a gated step demonstrably met it, and `achieved` credits
the `requires` of every upgrade behind them. That inference is sound and it only
ever reaches **backwards**. A track standing **beside** a recorded state — the
level track, when all they recorded was a rank — is implied by nothing, and no
amount of graph walking can reach it. Only the player knows, and the schema had
nowhere to put the answer.

## Decision

**A roster entry holds the set of states an entity has reached.**
`Roster.currentStates` is `Map<EntityId, Set<String>>`, and `DemandResolver`
seeds its walk from all of them.

1. **The primary key widens; there is no array column.** `roster_entry`'s key
   becomes `(profile_id, entity_slug, current_state)` and an entity is several
   rows. `current_state TEXT[]` would have been one statement and no new rows,
   and it loses two things this table already had for free: the not-blank CHECK
   applies per state rather than to a formatted blob, and a state stays
   queryable — "who is at promote-6" is a `WHERE` rather than an `unnest`. An
   array would also make the empty array and the absent row two spellings of
   one thing, which is the ambiguity V5's fourth decision spent a paragraph
   removing for the inventory.
2. **The merge unit is the entity, and the sync clock is untouched.** V6 keys
   the clock by `(profile_id, aggregate, entry_key)` with the entity slug as the
   key, and it stays that way. An edit states where an entity stands **in full**
   as of its timestamp, and the whole set moves together. Per-state clocks are
   the finer grain and the wrong one: two devices that each advanced the same
   construct on a different track would both win, and the server would assemble
   out of two half-truths a roster neither device has ever held. A state left
   out of a newer edit is a state given up.
3. **An empty set is refused, at every layer.** `Roster`, `RosterEdit` and both
   HTTP routes reject an entity present at no state. An owned entity is always
   somewhere; leaving the key out is how "not owned" is said, and admitting a
   second spelling would make every reader of the map handle a third case.
4. **The wire format changes, breaking.** `entities` is
   `Record<string, string[]>` on `GET`/`PUT`/`PATCH`
   `/api/me/profiles/{profile}/roster`, `RosterEditView.state` becomes
   `states`, and `ShortfallResponse.currentState` becomes `currentStates`. No
   compatibility shim and no versioned route: **nothing is deployed**, so the
   only clients are in this repository. That is the entire reason this was done
   before B5 rather than after it.
5. **The roster stores what the player said, not its closure.** Working out what
   else must be true — the states behind these, and the gates they imply —
   stays in the planner, which has the upgrade graph. `Roster` has no graph and
   does not get to guess. Two rosters holding different sets that mean the same
   thing are therefore different rosters, and hash differently in `SolveKey`;
   a player who says more has said something.
6. **The goal editor takes several states at once.** A single select would have
   made recording the second track erase the first — the exact overcharge this
   ADR exists to remove, reintroduced at the last layer.

## Consequences

- **The bug is gone in the direction that was left open.** A reader at
  `insight-1` whose rank step is gated on `level-40` was charged the whole
  ladder; having said they are at `level-40`, they are charged nothing for it.
  `DemandResolverTest` states both halves against one fixture, so the
  difference is the recording and not the graph.
- **A plan can again be cheaper than before for the same reader.** As with
  ADR 0026 this is the direction needing most care, and the safety argument is
  different and stronger: 0026 inferred, this one is told. The reader asserting
  where they stand is the most authoritative source available, and if they are
  wrong the plan is wrong in the way their own inventory being wrong makes it
  wrong.
- **A client must send the whole set.** A client that means to add one state
  sends every state it now holds. That is a real burden on a client and it is
  the price of decision 2; the alternative was a server that silently invents
  rosters. The route's javadoc and `RosterEditView` both say so, because this
  is the thing a future client author will get wrong.
- **`SolveKey` sorts each set before hashing.** Two devices reporting the same
  roster in a different order hold the same roster and must not miss each
  other's cached plan. The states are joined with `+` rather than the `,` that
  separates entries, so no state containing the entry separator can make two
  different rosters canonicalize identically.
- **Existing rows migrate to one-element sets**, which is exactly what they
  meant. Nothing is lost and nothing is invented.
- **The roster screen is still not a roster screen.** States are edited on the
  goal row, one entity at a time, next to the goal that needs them. A reader
  who wants to record a construct they have no goal for still cannot. That is
  N35's surface and this ADR does not touch it.

**Reverse this** if a game turns up whose entity genuinely has one state — a
single linear track with nothing beside it — for every entity it ships, because
then the set is ceremony and a column is honest. Reverse **decision 2** if a
client ever legitimately holds only part of an entity's state, which would be a
game where two tracks are edited on two screens that sync independently; the
merge unit would then have to be the track, and a track is a concept the
planner deliberately does not have — so that reversal is a much larger change
than it looks, and should start by asking whether the game really needs it.
