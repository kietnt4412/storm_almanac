# 28. A name for a progress kind is the bundle's word, not the game's

**Status:** Accepted · 2026-09-22

## Context

A `Progress` kind is an opaque string the bundle supplies: `character-exp`,
`weapon-exp`, `memory-exp`. It is matched against `Fodder.progress()` and never
interpreted, which is the whole reason EXP could enter the model at all —
nothing holds it, so it is not an `ItemStack`, and
[ADR 0019](0019-a-gate-is-a-goal-inside-a-goal-and-progress-is-demanded-as-an-item.md)
spells it as an item only inside a plan.

Spelled as an item, it reaches a reader. `Demand.progressItem` produces
`progress:character-exp`, and `ShortfallView` rendered that as
`Demand.progressKind(item)` — the bare slug — **beside a properly named
`Cogs`**. The plan's shadow-price list did the same, because it was a
`Map<String, Double>` keyed by item id and a map has nowhere to put a name.
Found by driving a browser at the roster screen on 2026-09-21, not by a test:
every assertion about that line passed, because the line was exactly what the
code said it should be.

**There is no name in the model to render instead, and the obvious fix is
wrong.** The obvious fix is to write down what the game calls it. Punishing:
Gray Raven calls all three pools the same thing:

| Screen | What it reads |
|---|---|
| Helentine: Lacrimosa's Level Up | `EXP 0/20`, `+1 000` |
| Weapon Enhancement Cost picker | `EXP 24 000` |
| Memory Enhancement Cost picker | `Obtained EXP 300` |

So a first-hand name puts **three lines reading "EXP"** in one plan and leaves
the reader unable to tell which pool they are short of. The ambiguity is real in
the client — a player knows which screen they are on — and a plan has no screen.

## Decision

1. **`ProgressKind(kind, displayName)`, a list on `GameDefinition` and on the
   bundle.** One row per kind, `V14`, matched on the same opaque string both
   sides already use. No foreign key: neither `upgrade_progress.kind` nor
   `fodder.progress` is a reference to a table of kinds, and creating one would
   make a kind something to declare before it can be used.
2. **It is not a fact and declares no provenance.** It is this project's word
   for this project's slug, exactly as `character-exp` is one. It sits beside
   `Game.energyUnit` and `Game.dayBoundary`, which carry no `factProvenance` row
   either — but for a better reason than theirs: those two are readings the fact
   ledger cannot hold ([ADR 0025](0025-the-day-boundary-is-a-property-of-the-game.md)
   admits that gap), and this one is not a reading at all. Claiming "Character
   EXP" was read off a screen would be false.
3. **Flattened into the patch diff anyway**, as subject `progress '<kind>'` with
   one fact, `name`. Not being a fact and not being reportable are different
   things: a rename moves what every shortfall table says, and a sequence that
   renamed a pool while previewing as *no changes* is the silence ADR 0025
   closed for the day boundary.
4. **Naming stays optional, permanently.** A kind with no row renders as its
   slug. Sequences 0 to 6 of the PGR bundle are published and immutable and have
   no names in them; they have to go on loading and go on rendering what they
   always did.
5. **A name for a kind nothing uses is refused.** `GameDataBundle` checks every
   declared name against the kinds the upgrades cost and the fodder rules feed.
   That is the one failure worth a check: it reads as a fix, it is never looked
   up, and the line it was written for goes on showing its slug. *In use* rather
   than *demanded*, because a bundle can read a fodder rule in one sitting and
   the upgrade it pays in the next — this one did, at sequences 0 and 1.
6. **The plan's shadow prices become a list of `{item, displayName, price}`.**
   Same defect, same fix; the map could not carry a name. The item id stays
   beside the name because it is what a reader quotes in a bug report and what a
   client matches against a shortfall line.

## Consequences

- **PGR sequence 7 names all three pools.** The names are the only text in that
  file that was not read off a screen, and its comment says so.
- **A `choice:` line is still not named, and this does not fix it.** A step
  offered at several prices renders as `one of: <upgrade id>, <upgrade id>`,
  and an `Upgrade` has no display name anywhere in the model. That is a
  different decision — a step's name would be the *game's* word, so it would be
  a fact, with provenance — and it is left open rather than folded in here.
- **The seven-argument `GameDefinition` constructor is a trap and is kept
  anyway.** It exists because versions published before names existed genuinely
  have none. It caught itself immediately: `GameDataDiffTest`'s `withRollover`
  helper used it and silently dropped the fixture's names, reporting a rename
  the test never made. Kept, because the alternative is a null.
- **`GameDataReadModel` now loads sixteen queries per version rather than
  fifteen.** One more, for a table most versions have no rows in.

**Reverse this** if a game gives its progress pools distinct names on its own
screens — then the name is a reading, it moves into the fact ledger with
provenance, and this record is superseded rather than amended. Reverse decision
4 if a bundle is ever published in which every kind is named and the fallback
has gone unused for long enough that keeping it hides a missing name; that is a
lint, not a schema change. Reverse decision 6 if the shadow-price list grows a
second consumer that wants it keyed, at which point the list is built into a map
at the edge that wants one rather than at the one that does not.
