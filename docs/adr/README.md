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
| [0009](0009-upstream-data-is-fetched-never-vendored.md) | Upstream game data is fetched, never vendored | **Superseded in part by [0015](0015-game-data-is-sourced-first-hand-not-adapted.md)** |
| [0010](0010-a-plan-is-the-best-provable-in-the-budget.md) | A plan is the cheapest one provable inside the budget | Accepted |
| [0011](0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md) | A yield is a mean per run, and the solver uses what its sample supports | Accepted |
| [0012](0012-the-solve-cache-is-in-process-until-there-is-a-second-node.md) | The solve cache is in-process until there is a second node | Accepted |
| [0013](0013-the-horizon-is-a-scalar-not-an-index.md) | The planning horizon is a scalar, not an index | Accepted |
| [0014](0014-sync-is-last-write-wins-per-key-against-a-clock-that-outlives-the-value.md) | Sync is last-write-wins per key, against a clock that outlives the value | Accepted |
| [0015](0015-game-data-is-sourced-first-hand-not-adapted.md) | Game data is sourced first-hand, not adapted from a community aggregator | Accepted |
| [0016](0016-provenance-is-a-property-of-the-data.md) | Provenance is a property of the data, and publishing enforces it | Accepted |
| [0017](0017-the-development-sign-in-is-absent-from-the-artifact.md) | The development sign-in is absent from the artifact, not disabled in it | Accepted |
| [0018](0018-the-gacha-engines-answer-one-question-about-one-rarity.md) | The gacha engines answer one question about one rarity, and the trial count follows from the tolerance | Accepted |
| [0019](0019-a-gate-is-a-goal-inside-a-goal-and-progress-is-demanded-as-an-item.md) | A gate is a goal inside a goal, and progress is demanded as an item | Accepted |
| [0020](0020-a-limit-that-never-resets-is-offered-whole.md) | A limit that never resets is offered whole, and the plan says so | Accepted |
| [0021](0021-one-step-at-several-prices-is-a-choice-the-solver-makes.md) | One step at several prices is a choice the solver makes | Accepted |
