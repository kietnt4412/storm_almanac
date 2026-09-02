# 5. No game-specific code outside game data

**Status:** Accepted · 2026-09-02

## Context

The product thesis is a domain model that is game-agnostic by construction:
things you want, things that produce them, things that consume them, and an
energy budget gating the producers. Every companion tool that lost this fight
did so one `if` at a time.

## Decision

No game-specific branching in `planner`, `gacha` or `stats`. Onboarding a title
is a `GameDefinition` bundle plus a parser adapter, and nothing else.

Consequences of taking this seriously up front:

- **Rarity is a `(label, rank)` record, not an enum.** One game counts stars,
  the other uses letter grades.
- **Pity is a `PityRule` record.** It expresses a rising soft-pity curve and a
  flat wall from the same four fields.
- **`PityScope` is a dimension.** One shipped game carries pity per banner type
  rather than globally, so a global counter would have been wrong.
- **`Fodder` is a `Sink` from day one.** Where an item is consumed to advance
  another item of its class, an item is simultaneously resource and sink, and a
  naive resource graph breaks.
- **`Goal.Satisfiability` admits `PROBABILISTIC`.** Where duplicates are
  consumed for a random outcome, a goal's cost is a distribution, not a number.

Enforced by `GameAgnosticismTest`, which scans the guarded module sources for
game slugs in code. Prose in comments explaining *why* an abstraction has its
shape is the point of the comment and is not scanned.

## Consequences

Some abstractions are more general than the launch title needs, which is a real
cost paid in phase 1 to avoid a much larger one in phase 11.

## Reversal trigger

If a third title cannot be expressed without a branch, the model is wrong —
generalise the model, do not add the branch. If two independent titles both need
the same branch, that is evidence of a missing dimension, and it goes in the
model with an ADR of its own.
