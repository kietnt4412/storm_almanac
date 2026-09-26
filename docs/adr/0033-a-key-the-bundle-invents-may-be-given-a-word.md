# 33. A key the bundle invents may be given a word

**Status:** Accepted · 2026-09-26

## Context

A bundle invents keys so that its facts can point at one another. A `Reward`'s
bar names a *measure* (`phantom-pain-cage-score`, ADR 0022), a `Fodder` rule
matches an item *category* (`character-exp-pod-l`), and an entity has a *kind*
(`character`). None of them is a reading — no screen says
`phantom-pain-cage-score` — and until sequence 13 none of them had anything else
to be shown as.

D5's second rehearsal (2026-09-26, S8 in TRACKER.md) found each on a screen:

| Where | What it showed |
|---|---|
| The plan's "how far do you get" | `phantom-pain-cage-score` over the question |
| The plan's *Not counted* note | nine `phantom-pain-cage-90000 (needs 90000 of phantom-pain-cage-score; …)` in id order |
| A grant line | "Weekly, score 90,000+", naming no weekly |
| The inventory | `HARMONY-MATERIAL`, and one `CHARACTER-EXP-POD-*` heading per Pod size |
| The goal and roster pickers | "Lucia: Inverse Crown (character)", where the game says construct |

[ADR 0028](0028-a-name-for-a-progress-kind-is-the-bundles-word-not-the-games.md)
already settled the same question for one key, the progress kind: a name for a
key the bundle invents is the bundle's word, declares no provenance, is
optional, and is flattened into the patch diff anyway. The question here is
only whether three more keys get the same treatment, and in what shape.

## Decision

1. **A `Word` names one key: a subject, the key, a display name.** The
   subjects are `measure`, `category` and `entity-kind`. One record and one
   table (`gamedata.word`, V17) for all three, because they are one idea; a
   fourth subject is a constraint change, not a migration per key.
2. **A word is not a fact.** No `FactRef`, no `factProvenance` row, exactly as
   ADR 0028's names and the energy unit. This holds even where the word is also
   the game's — "Phantom Pain Cage" is what its screen is called — because
   what a word does is rename a key and change no number, and that is what
   decides whether something needs provenance here. Where a word is the
   bundle's own ("Character EXP" over three Pod sizes the game gives no
   heading), it would be false to claim it was read.
3. **Several keys may share one word, and for a category that is the point.**
   The three Pod sizes are three categories because a fodder rule has to tell
   them apart, and one heading because a reader does not. The page groups by
   the word and still matches by the key.
4. **Every word must name a key the bundle uses, and one key has one word.**
   `GameDataBundle` refuses the rest, listing the keys in use — a word for a
   typo'd key would rename nothing and fail nowhere, which is the check ADR
   0028 gave progress names for the same reason.
5. **A key with no word is itself.** `GameDefinition.nameOf` falls back to
   the key, which is every version before sequence 13; those are immutable and
   stay readable. The API sends the key and the word side by side
   (`categoryName`, `kindName`, `displayName` on a measure), and the page falls
   back the same way for a server older than itself.
6. **The words are in the diff.** One subject per key, `measure
   'phantom-pain-cage-score'`, so a sequence that renames a heading does not
   report "no changes".

## Consequences

- The Cage is named where a grant is: "Phantom Pain Cage, weekly, score
  90,000+", and the *Not counted* note reads "Phantom Pain Cage at 30,000,
  90,000, … (this plan was asked for 0)", grouped by measure and ordered by bar.
- **Progress kinds are not folded in**, though they are the same idea. ADR 0028
  is accepted, `gamedata.progress_kind` holds published rows, and moving them
  would be churn with a migration attached. If a fourth kind of key arrives, it
  goes here; progress names stay where they are.
- `GameDefinition` gains a tenth component and a nine-argument constructor for
  versions without words — the trap ADR 0028 names, kept for the same reason.
- `GameDataReadModel` loads one more query per version.

**Reverse this** if a game gives one of these keys a name on its own screens
that a player would look for by — then that word is a reading, it moves into
the fact ledger with provenance, and this record is superseded for that subject.
Reverse decision 3 if a reader ever needs two categories that share a word told
apart on the inventory screen; the key is still on the wire.
