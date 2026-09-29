# 37. A saved plan is a snapshot, and a change report re-solves

**Status:** Accepted · 2026-09-29

## Context

C3's exit, agreed by the maintainer on 2026-09-29: *a reader returning after a
new sequence sees what it changed for their goals and plan, without asking.*
Until now a plan was computed, rendered and forgotten. The server had no record
of which sequence a reader last planned on, so "since you were last here" had
nothing to be measured from.

Two things make the obvious fix, storing the request and re-solving on demand,
wrong as the *record* of what a reader saw:

- **The inventory moves.** A re-solve of last week's request against today's
  inventory is today's plan, not last week's.
- **The solver is not a function of its inputs alone.** It stops at a two-second
  budget and returns the best answer it has proven, so the same data can give
  plans of equal cost that differ line by line. It has happened: 3 880 and
  3 877 Activity on unchanged data (ADR 0010).

C3 also has to answer a different question, *what did the patch change*, and a
comparison of last week's saved plan with a fresh one answers it badly. The
difference would be the patch mixed with everything the reader farmed, spent or
retargeted since.

## Decision

1. **Every plan the server answers is saved, one per profile, replacing the
   last** (`V19`, `player.saved_plan`). A refusal saves nothing, so the last
   plan stands. The maintainer chose saving automatically over a save button,
   and one plan over several. Several saved plans would be a second table, not a
   second key on this one.
2. **The plan is stored as the reader saw it, in its wire shape**, and
   `GET /api/me/profiles/{p}/plan` returns it byte for byte. The request is
   stored beside it with every default filled in. Both are JSON the player
   module never reads: the plan's shape belongs to the api module, and the
   player module may not know what a plan is. The same rule keeps a pity
   counter's scope key opaque (`V18`).
3. **The sequence is a column**, not only a field inside the JSON. It is the
   anchor C3.2 measures from, and it has to survive the JSON becoming
   unreadable to a later wire shape.
4. **The request keeps `reach`** (maintainer, 2026-09-29). The plan screen fills
   its form from the saved request. It seeds `reach` only when the browser holds
   no answer of its own for the profile, because a local answer may be newer
   than any plan it has been sent with.
5. **"What the patch changed" is two fresh solves, not a comparison with the
   snapshot.** C3.2 solves the saved sequence and the latest one against the
   reader's *current* state with the saved request, and reports the difference.
   With everything but the patch held still, the difference is the patch's.

## Consequences

- **This changes one line of ADR 0022, and does not reverse it.** 0022's last
  consequence, "nothing stores the answer", is no longer true: the answer is
  stored as part of a request that was made. 0022's decision 3 stands, because
  `reach` still travels on every plan request and the browser's store is still
  where the form reads it from. What 0022's trigger asks for, `reach` as player
  state with its own schema, sync clock and API, is still not built. A reader
  on a second device now gets their last answer back through the saved plan,
  which removes most of the pressure to build it.
- **An old saved plan must read under a newer wire shape.** It is read with
  the application's own mapper, which ignores unknown fields and leaves missing
  ones null. This is the same rule the page's wire types already follow by
  marking new fields optional. Renaming or retyping a `PlanResponse` field
  breaks this, and it needs a migration of the stored JSON in the same change.
- **A saved plan can fall out of step with the goals.** Changing the goals does
  not touch the saved plan. The screen says when the plan was worked out and
  that it counts what the reader owned then, and the next plan replaces it.
- **C3.2 costs two solves.** That is up to four seconds on the synchronous
  path, and the saved sequence must still load. A published version has
  stopped loading here once before (see the tracker's warning under Phase 6).
- **`POST /plan` now writes.** It was already a POST, so nothing about
  caching or CSRF changes.

## Reversal trigger

Keep more than one plan per profile when a reader asks to compare plans, or
when progress over time (C3's postponed half) needs a history. That is a new
table keyed by plan, and this row becomes the pointer to the latest. Make
`reach` player state, as ADR 0022 describes, the first time a reader has to
re-answer it even though a saved plan exists.
