# Architecture decision records

Every record names the **trigger that would reverse it**. A decision without a
reversal condition is a preference, and interviewers read this folder first.

Format: context, decision, consequences, reversal trigger. Short. One file per
decision, numbered, never edited after acceptance — superseded instead.

| # | Decision | Status |
|---|----------|--------|
| [0001](0001-record-architecture-decisions.md) | Record architecture decisions | Accepted |
| [0002](0002-modular-monolith.md) | Modular monolith, not microservices | Accepted |
| [0003](0003-the-honesty-rule.md) | Every hand-built component sits behind a boring one | Accepted |
| [0004](0004-solver-ojalgo.md) | ojAlgo for the mixed-integer program | Accepted |
| [0005](0005-game-agnostic-domain-model.md) | No game-specific code outside game data | Accepted |
| [0006](0006-wilson-intervals-for-drop-rates.md) | Wilson score intervals for drop rates | Accepted |
| [0007](0007-equipment-is-an-entity.md) | Equipment is an `Entity` | Accepted |
| [0008](0008-gamedata-persistence-is-jdbc.md) | The gamedata module persists with JDBC, not JPA | Accepted |
