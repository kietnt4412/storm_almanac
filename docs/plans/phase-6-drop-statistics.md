# Phase 6 — drop statistics on Postgres

**Status:** drafted 2026-09-30 (fifty-fifth session). **Moved into Phase 11 the
same day** ([ADR 0038](../adr/0038-drop-statistics-move-into-the-second-title.md)):
the maintainer reports PGR's farming is all fixed payouts, so there is nothing to
estimate on the live title. Kept as drafted, questions unanswered, for R1999.
The proposed ADR 0038 below (an estimate is a mean per run) is now unnumbered;
it takes the next free number when this plan is picked up.

Phase 6's exit, from [plan.html](../../plan.html): *a community-derived estimate
supersedes a seeded one in a live plan.*

## What is true today, and why it shapes the plan

- **Production has nothing to supersede.** The PGR bundle has one stage,
  Simulated Battlefield, and its only drop is 82 Simulation Score a run,
  **declared**. A fixed payout has no sampling error, so there is nothing for
  reports to estimate. The "seeded" data plan.html imagined was Kornblume's,
  and [ADR 0015](../adr/0015-game-data-is-sourced-first-hand-not-adapted.md)
  means it never ships. **The exit needs a PGR stage whose drop is random,
  which the planner can use, with a first-hand count of runs behind it.** That
  is a reading, and only the maintainer can take it (question 1).
- **The code is half there.** `stats` has the ports (`DropReportStore`,
  `DropEstimateRepository`, `ReportValidator`), `DropReport`, `DropEstimate`,
  and both intervals. `YieldTable` already reads a `DropEstimateRepository`,
  and `PlannerConfiguration` passes it in when a bean exists. No bean exists,
  no table exists (the `stats` schema is empty since `V1`), and nothing
  submits a report.
- **`DropEstimate` answers the wrong question.** It carries a Wilson interval,
  which is a proportion. The solver needs a mean per run.
  [ADR 0011](../adr/0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md)
  already said Phase 6's estimates "should arrive as a mean and a sample size
  and take the same path as a declared yield". `YieldTable`'s javadoc says the
  same: at that point its two branches become one rule.

## Q7, answered

*What does `DropReportStore.scan(stage, item, version)` return?*

**Every report for the stage, whether or not it saw the item.** A report is one
stage's runs and everything they dropped, and an item missing from
`observedDrops` dropped zero times. So a report that did not see the item is
still `runs` trials of it, and leaving it out would push every rate up. Only
the reports that saw nothing are the ones that bring a rate down.

So the `item` parameter does nothing, and the port should say so:
`scan(StageId stage, GameDataVersion version)`. Nothing implements the port yet.
`almanac-store` mentions it only in a javadoc (Track B is parked, ADR 0036), so
the change breaks nothing. ADR 0035's decision 4 said to answer Q7 on this side
of the seam, and this is that answer.

## What gets built — four slices

### P6.1 · A reader can file a report

- **`V20`**: `stats.drop_report` (a sequence, profile, game, bundle sequence,
  stage, runs, time) and `stats.drop_report_item` (report, item, count).
  There is no FK to `player`, because no FK crosses a schema.
- **`PostgresDropReportStore`**: `append`, `scan(stage, version)`, `since`,
  `highestSequence`, behind `storm-almanac.substrate.drop-report-store: postgres`,
  a key `application.yml` has carried since the scaffold.
- **`POST /api/me/profiles/{p}/reports`**: `{stage, runs, drops: {item: count}}`,
  filed against the latest sequence. The profile must belong to the reader,
  checked the way `/plan` checks it.
- **The validator refuses, by name:** an unknown stage, a stage with no
  sampled drop (nothing to estimate), an item the stage cannot drop, runs
  below 1 or over the per-report cap, and a negative count.
- **A per-profile rate limit**: a daily cap on runs per profile per game,
  counted in Postgres, with no Redis. Past the cap, the report is refused and
  the refusal says when the day rolls over (`Game.dayBoundary`).

### P6.2 · Reports become an estimate, and the solver uses it

- **ADR 0038: a drop estimate is a mean per run.** `DropEstimate` carries a
  mean, runs, reporters and a `PoissonRateInterval` in place of the Wilson
  interval. This supersedes ADR 0006 for estimates. Wilson stays in the module
  for any genuine proportion.
- **Aggregation**: when a report is accepted, it publishes a `ReportFiled`
  event through `EventPublisher`. A listener in `stats` recomputes that
  stage's estimates and writes them to `stats.drop_estimate`. At this project's
  traffic, a full rescan of one stage is cheap. `since` stays for the day it
  is not.
- **`PostgresDropEstimateRepository`** is the bean `PlannerConfiguration`
  already looks for.
- **`YieldTable` becomes one rule.** For each drop, take the evidence (a
  declaration, the bundle's sample, or the community's), pick one by the
  supersede rule (question 2), and give the solver the Poisson lower bound of
  whatever was picked. A declared yield is still used as it stands.
- **N18, in this same change**: the estimates a solve used go into `SolveKey`,
  so a cached plan cannot outlive the rates it was solved on.
- **The plan says where each stage number came from**: `seeded · 40 runs` or
  `community · 212 runs from 4 readers`, on the wire and in its notes (ADR
  0006's rule that provenance and sample size go next to every number).

### P6.3 · The screens

- **A report form**, reached from the plan's stage line and from the profile.
  The reader picks a stage that has a sampled drop, enters the runs and a count
  per item, and files it. It is phone-first, because the reader is next to the
  game. After filing, it shows the estimate as it now stands.
- **The plan's stage lines** show the provenance and the sample size.
- Driven at 375 and 1280 px, like every screen since C2's criterion.

### P6.4 · The exit, on production

1. The maintainer's reading (question 1) goes into a bundle sequence as a
   drop with `sampledRuns`. That is the **seed**. It can go in the same
   sequence as the next banner.
2. The phase deploys, and the sequence publishes to Neon after a yes.
3. Readers file reports until the community's evidence passes the supersede
   rule (question 2) under the "community" rule (question 3).
4. **A live plan on production** shows that stage's number as `community`,
   not `seeded`. The exit is the maintainer's screen showing that.

## Questions for the maintainer

1. **Which PGR stage has a random drop the planner can use, and will you count
   runs on it for the seed?** The planner needs an item that a modelled goal
   demands and that a stage drops at random, not as a fixed payout. For the
   seed, count *n* runs and the total of each item. Tell me how you read it
   (the result screen after each run, or a before-and-after inventory count).
   About 30 runs is enough to start. **No recommendation: I don't know PGR's
   stages first-hand, and the tracker's rule is not to guess game facts.** If
   PGR has no such stage the planner can use, say so. Then the exit needs
   rethinking, not a workaround.
2. **When does the community's number replace the seed?** **Recommended: once
   the community's runs reach the seed's**, so its interval is at least as
   narrow and the switch never makes the plan less sure. The other options are
   to pool the two (statistically fine, but the provenance label stops
   meaning anything), or to switch on the first report (one lucky run would
   move a plan).
3. **What counts as "community"?** **Recommended: at least three distinct
   profiles, and no single profile more than half the runs.** Without that
   rule, the maintainer filing alone would pass the exit with one person's
   data, which is the seed with extra steps. The cost is that the exit waits
   on three readers reporting on the same stage.
4. **Do reports carry across sequences?** Sequences publish often (18 in four
   weeks), mostly for words and new rows. plan.html says an estimate never
   crosses a version boundary, and read literally, that empties every estimate
   at every publish. **Recommended: pool a stage's reports across every
   sequence where that stage's list of drop items is unchanged.** When the
   list changes, the stage starts from zero, and the change report (C3.2)
   already shows that change. What this misses is a patch that changes a rate
   but leaves the item list alone, and that needs a reading to notice either
   way.
5. **Which abuse controls come now?** **Recommended: validation, the
   per-profile daily cap, and question 3's share cap. Cut reputation weighting
   and outlier down-weighting to later, and record the cut.** At a handful of
   readers there is no history to weigh a reputation with, and the share cap
   already limits what one bad reporter can do. Both are easy to explain in an
   interview, and both are real work with nothing yet to test them against.
6. **One report is several runs' totals, not one line per run.**
   **Recommended: totals**, because nobody types thirty lines on a phone. The
   cost is that "what share of runs dropped it" can't be computed, which is
   exactly why ADR 0038 moves estimates to a mean per run. Per-run lines would
   keep Wilson usable and make every report thirty times longer.

## What this plan does not do

- **R1999** stays in Phase 11. Its drop grades (`Fixed` / `Common` /
  `Possible`, the N26 note) could become a report field then.
- **A report does not become a sequence.** Estimates move between sequences,
  so C3's change report does not show them. Saying "the community's numbers
  moved your plan" is a later C-track item, if anyone asks for it.
- **Track B**: the Postgres store is the boring implementation, and it is the
  only one. `almanac-store` stays parked.
