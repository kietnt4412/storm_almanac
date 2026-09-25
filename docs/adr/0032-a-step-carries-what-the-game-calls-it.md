# 32. A step carries what the game calls it

**Status:** Accepted · 2026-09-25

## Context

The maintainer's D5 rehearsal reached the roster and could not use it, twice.

The first time (S2) it offered one construct's states as one dropdown of about
seventy raw ids. That was fixed in the page by splitting the upgrade graph into
its tracks and guessing a label for each from its ids: `flaming-chord-4` became
"Flaming chord · 4". The second time the maintainer said what they had meant:

- **The game doesn't number rank.** The Promote tab shows PRIVATE, SERGEANT,
  ELITE, TASK FORCE, ACE and HERO, each with stars. A player knows they are at
  "Elite, two stars", not at `promote-4`. No guess from the id can produce that.
- **The game groups a construct's skills, and players find them by the group.**
  The skill screen has four headings: Basic Skill, Special Skill, Evolution
  Effect and Common Effect. Each skill carries a bracketed tag such as
  [Yellow Orb], [Core Passive] or [Class Skill]. Most players never read a
  skill's name. They know "the blue orb one".

Neither is in the model. `Upgrade` is an edge between two opaque state ids, the
bundle names no track and no state, and the entity carries no skills. Putting
the words in the page would mean game words in code, which the non-negotiables
forbid for the backend and which would be just as wrong in the frontend: onboarding a title is a bundle and
an adapter, and a second game would need its own strings compiled in.

[ADR 0028](0028-a-name-for-a-progress-kind-is-the-bundles-word-not-the-games.md)
gave progress kinds a display name and declared it **not a fact**, because
"Character EXP" is this project's word and not the game's. That argument does
not carry over. "Elite ★3" and "[Yellow Orb]" are text read off the client, and
CLAUDE.md's rule is that text is attributed.

## Decision

1. **The words ride on the step.** `Upgrade` gains `Labels`:
   - `fromName` and `toName`: what the game calls the two states.
   - `section`: the heading the step's track sits under.
   - `tag`: the track's bracketed kind.

   All four are optional and independent. A rank ladder names its states and
   has no tag; a skill has a tag and numbered states. Absent is
   `Labels.NONE`, which is every step of every version before sequence 11.
2. **They are facts, covered by the step's provenance.** They are part of the
   step the way its costs are, so the step's `factProvenance` row covers them.
   There is no second fact kind and no per-field provenance. A name read at a
   later sitting of the same screens is recorded in the ladder's comment, not
   in a new entry.
3. **The order of the headings is a list, and not a fact.** The bundle's
   `sections` lists them in the order the client shows them. Each step names
   its own heading, and that is the reading. The list is only where each goes,
   and it may hold the bundle's own word for a group the game leaves untitled.
   PGR's "Growth" (Level, Promote, Evolve) is the one such word, and the bundle
   says so. The order can't come from the steps: the leader skill's step comes
   before the skills', and its heading comes after theirs.
4. **A ladder says per-position words once.** An `each` group may carry
   `positions`, one object per element of the list it walks, whose fields go on
   that element's rows. The skill in slot two is the Yellow Orb on every S-rank
   construct, whatever it is called. `positions` of the wrong length, or one
   that sets a field its row sets, is refused: either would label a skill with
   another's words.
5. **What `GameDataBundle` refuses:**
   - a section a step names that the bundle doesn't declare;
   - a declared section no step names;
   - one state called two things.

   Each would render as something other than what the author wrote, and the
   page would show it without complaint.
6. **The words stay out of the planner.** Nothing in `planner`, `gacha` or
   `stats` reads `Labels`. The upgrades route serves them with the game's
   section order. Where a version has none, the page falls back to its guess from
   the ids, so R1999 and every earlier PGR sequence render as before.

## Consequences

- **Schema:** `V16` adds four nullable columns to `gamedata.upgrade` and a
  `gamedata.section` table (version, position, name). Nothing stored moves, and
  every published version stays readable.
- **The patch diff sees them.** A renamed state, a moved skill or a reordered
  heading is a change, not a silent "no changes" (the same closure ADR 0025 made
  for the day boundary).
- **PGR sequence 11 carries:**
  - fourteen rank names;
  - a heading on every S-rank track;
  - a tag on every skill, the leader skill and the SS passive;
  - the five headings in the client's order.

  The ranks are the Promote tab (`character-screens`, and screenshots of
  2026-09-25). The tags are the four skill pages (`skill-pages`, 2026-09-24).
  That the leader sits under Common Effect, that Private starts with its star
  filled, and that the layout is shared by every S-rank construct are the
  maintainer's readings of 2026-09-25.
- **Skill names are still guessed from ids.** "Withering spiral" is the slug
  read back. The bundle has no display name for a skill, and the tag is what a
  player finds it by. A skill's name could become a `Labels` field if a reader
  ever needs it exact.
- **Weapons and memories have no headings yet.** Their tracks render in one
  untitled group after the headed ones, which is what they did before.

**Reverse this** if a game's words for a track turn out to depend on something a
step can't see, such as a name that changes with another track's state. Names
would then need a model of their own rather than a field on an edge. **Reverse
decision 3** if a game shows the same heading in different orders for different
entities. The order would then belong to the entity, not the bundle.
