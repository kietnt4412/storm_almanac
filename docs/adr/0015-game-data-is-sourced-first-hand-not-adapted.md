# 15. Game data is sourced first-hand, not adapted from a community aggregator

**Status:** Accepted · 2026-09-09 ·
**Supersedes [ADR 0009](0009-upstream-data-is-fetched-never-vendored.md)** in
part — see *What survives from 0009* below.

Closes **Q3** and **F2** by removing the question they were about, rather than by
answering it.

## Context

ADR 0009 settled a narrower version of this. Kornblume carries **no `LICENSE`
file** — verified again on 2026-09-09 through the GitHub API: `license: null`, no
such file in the tree, owner `windbow27`, not a fork, still actively maintained.
Absence of a licence is not permission. 0009's answer was to read the data,
vendor none of it, attribute everything, and treat **F2** — ask the maintainer —
as a blocker that bites *at the moment this service is deployed publicly, not
before*.

Phase 4 arrived, D1 reversed, and hosts were chosen. So F2 stopped being distant.
Presented with the three ways forward — ask, launch on the synthetic title only,
or source the data first-hand — the project owner chose the third.

**The legal picture that prompted 0009 is more mixed than 0009 says, in both
directions.** Raw facts are not copyrightable in the US (*Feist*, 1991), so a
drop rate is thin ground for Kornblume to stand on; but the EU/UK *sui generis*
database right (Directive 96/9/EC) protects substantial extraction regardless of
originality, and **the publisher's rights in the names and text sit underneath
all of it and are unaffected by anything Kornblume could say**. None of that is
legal advice and none of it is why this decision was taken. It was taken because
a project whose central claim is that it does the data properly should not have
somebody else's unlicensed scrape as its only source of truth.

## Decision

**The shipped product carries only game data this project sourced itself.**

A fact enters a published bundle because someone **observed it in the game, or
read it from the publisher's own disclosure** — not because they copied it out of
an aggregator. That distinction is the whole decision, and it is the one that is
easy to lose: re-typing Kornblume's numbers into our own JSON would be laundering
rather than sourcing, and would leave every claim in this repository resting on
the same source while pretending otherwise.

Consequently **provenance becomes a property of the data**, not of the README.
`Drop` already carries `sampledRuns`, where 0 means *declared*; the catalog axis
carries nothing equivalent, and it needs to. A bundle should be able to say, per
fact or at worst per source, where it came from.

**The two halves of the data are not the same problem and are not on the same
schedule:**

- **The static catalog** — items, stages, energy costs, recipes, upgrade graphs,
  characters, equipment — is readable off a game screen. For patch 3.5 that is
  roughly **2 700 facts**: 91 items, 100 stages, 50 recipes over 113 material
  lines, 118 released arcanists carrying ~972 insight material lines and ~1 502
  resonance entries, 37 psychubes. Tedious, tractable, and mostly **additive**
  after the first backfill — a patch adds a handful of characters and some
  stages rather than replacing everything.
- **Drop rates cannot be read off a screen.** Each of the **595** drop facts in
  patch 3.5 is a statistical estimate over many runs. This half is Phase 6 —
  our own reports — and it is the one that costs time rather than typing.

**Kornblume stays permitted as a local cross-check that is never shipped**, on
exactly 0009's read-never-vendor terms. That use was never what F2 blocked: F2
was scoped to *public deployment carrying upstream numbers*, and a test that runs
on one machine and publishes nothing is not that. Keeping it is what lets the
first self-sourced bundle be diffed against an independent reading of the same
patch, which is worth more as a check than it ever was as a source.

**Nothing is deleted yet, and the order matters.** The adapter stays until the
replacement exists. Removing it first would leave the project with no real data
at all and would destroy the current evidence before there is a substitute — see
*Consequences*.

## What survives from 0009

Most of it. 0009 is superseded on its *conclusion* — "read it, ship it,
attribute it, ask before launch" — and stands on its mechanics:

- upstream snapshots are fetched, never committed;
- an adapter lives in its own module and converts less than the upstream
  publishes, saying so;
- every bundle carries an `attribution`, and it travels to the API on every
  response. **A self-sourced bundle still needs one** — Reverse: 1999 is
  © Bluepoch whoever typed the numbers in, which is why *numbers and text only,
  no game assets* is a project invariant rather than a courtesy.

## Consequences

- **The load-bearing claim goes with the data, and must not be quietly kept.**
  The nine benchmark agreements, **3 880 Activity against 4 017**, and
  "seventeen of twenty published rates within three percentage points" are all
  computed from Kornblume-fed inputs. Until a self-sourced bundle reproduces
  them they are evidence about somebody else's numbers run through our solver.
  The benchmark *method* survives untouched — the published community guide it
  compares against is a separate artifact — so this is a number to re-earn, not
  a test to delete.
- **The bootstrap problem is real and is the main risk.** The optimizer cannot
  rank a stage without a yield, so no drop data means no plan; own drop data
  needs users; users need a working plan. Three ways out, and the first would
  collapse the hard half entirely: **find out whether the game discloses its own
  drop rates** — many titles in this market do, under disclosure rules — in which
  case they are static facts and there is no bootstrap problem. Otherwise:
  **launch the catalog first**, which is public, already in Phase 4's scope and
  needs no yields; or **seed thin, honest samples** from the maintainer's own
  play.
- **ADR 0011 turns out to be the asset that makes this survivable.** A yield
  carries the runs behind it and is discounted to the lower end of a 95% Poisson
  interval, so a hand-collected sample of twenty runs is *usable and honest*
  rather than unusable — the plan simply says how much it does not know. A model
  that stored a bare rate would have made self-sourcing impossible to do
  truthfully.
- **The pipeline does not care.** Schema, parser, writer, `gamedata-cli`,
  diffing, the optimizer and the statistics are all game-agnostic by
  construction; what changes is only where a bundle comes from. **A hand-authored
  bundle needs no adapter at all** — the canonical JSON *is* the authoring
  format, and *preview, ingest, publish* already exists. That the machinery was
  built for a claim about second games turns out to cover this too.
- **The 16 tests CI skips are now doubly stranded.** They were skipped because
  the snapshot is not committed; they are now also testing a source the product
  will not ship. They should be repointed at the self-sourced bundle as it grows,
  at which point they can finally be committed fixtures and run in CI — which is
  the outcome 0009 wanted from a *yes* to F2, reached the other way.
- **Phase 11 gets easier to argue and harder to do.** PGR will need the same
  first-hand sourcing, so the second game costs data entry as well as an
  adapter — but the abstraction claim gets stronger, because neither game's data
  arrives through somebody else's schema.
- **Phase 6 may have to move up the order.** It was scheduled after the gacha
  engine; it is now on the critical path for the product working at all.

## Reversal trigger

**If first-hand sourcing stalls** — a backfill that does not finish, or a patch
cadence that outruns entry — the honest move is not to quietly re-adopt the
aggregator. It is to **send F2 after all** and adopt with permission, which was
always available and was declined rather than foreclosed.

**If the publisher discloses rates officially**, the statistical half stops being
statistical and this decision gets much cheaper; revisit the Phase 6 ordering at
that point.

**If a properly licensed community source appears** — one that actually carries a
licence permitting derived use — this ADR does not forbid it. What it forbids is
depending on a source that has not said yes.
