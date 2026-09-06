# 9. Upstream game data is fetched, never vendored

**Status:** Accepted · 2026-09-06

Settles the ingestion half of **N8** in TRACKER.md, and makes **Q3**'s
provisional answer operational instead of advisory.

## Context

Phase 1's exit criterion has two halves. The API half was met on 2026-09-06.
The other half — *tests over real patch data, including a patch that changes
something* — could not be met by `proving-ground`, the synthetic title this
repository invented. That fixture exercises every shape the canonical format
has, which is exactly why it cannot meet this criterion: it was written by the
same people who wrote the parser, so it cannot surprise them, and the
interesting failures in a data pipeline are all surprises.

Meeting it needs somebody else's data. That runs into Q3: the consolidated
community source, [Kornblume](https://github.com/windbow27/kornblume), carries
**no `LICENSE` file**, which under copyright is all rights reserved rather than
public domain. Absence of a licence is not permission. **F2** — ask the
maintainer directly — needs a human to send a message, and no session can.

Three ways out were considered, and the project owner chose the third:

1. **Ask F2 and wait.** Correct, and open-ended. The phase stays open on a
   message nobody has sent.
2. **Close the phase by exception**, the way Phase 0 was closed on D1, and ship
   with the synthetic fixture. Honest, but it leaves the largest unverified
   claim in the repository unverified: that the schema can hold data this
   project did not author.
3. **Build the adapter now, read the data, redistribute none of it.** The
   decision recorded here.

## Decision

**An upstream's data is read to produce a bundle and is never committed to this
repository.** Concretely:

- A parser adapter lives in its own Gradle module under `backend/adapters/`,
  one per title. `:adapters:reverse-1999` is the first.
- `backend/tools/fetch-upstream.sh` downloads snapshots into
  `backend/build/upstream-snapshots`, which `.gitignore` keeps out of the index
  twice over — once as `build/`, once as an explicit `upstream-snapshots/` rule
  that says why. That second rule started life as `**/upstream/` and had to be
  narrowed: it silently stopped tracking the adapter's own test fixtures under
  `src/test/resources/upstream/`, which would have reached CI as a compile
  failure. A broad ignore rule fails by omission.
- `RealUpstreamPatchTest` runs against those snapshots and **skips itself when
  they are absent**, which is the state CI is in.
- Every bundle an adapter produces carries an `attribution` naming the upstream,
  its own upstreams and the game's publisher, and that string travels to the API
  on every response.

The scope is explicitly personal and portfolio use. **Publishing this service
publicly requires asking first — F2 becomes a blocker at that point, not
before.** This ADR is the record that the question was deferred deliberately
rather than missed.

## Consequences

- **The strongest test in the repository is the one CI does not run.** Three
  cases in `RealUpstreamPatchTest` are skipped on every pipeline run. That is a
  real hole and is stated in the test's own javadoc, in TRACKER.md and here,
  because a green tick that quietly omits the best evidence is worse than a
  red one.
- Reproducing it is one command and a flag, both documented in the script's own
  output. What CI cannot do, a person can do in about a minute.
- **It found two defects in its first run**, which is the entire argument for
  having done it:
  - `Names.slug` folded to ASCII before slugging. A character named *Зима* folds
    to the empty string, and the whole ingest was refused. The rule is now
    Unicode-aware — letters and digits survive whatever script they are in — and
    an accent is still stripped so `Café Crème` reads as `cafe-creme`.
  - The adapter read a row's `Name` before checking its release flag. The real
    upstream ships unreleased equipment as placeholder rows whose every field
    but `Id` and `Rarity` is `null`, so a conversion was refused by rows it was
    about to discard. Validate what you keep, not what you drop.
- The adapter converts less than the upstream publishes, and says so on every
  run. Shop offers, alternative resonance-pattern costs, a synthetic zero-cost
  stage and unreleased content are all dropped, each for a reason written into
  `KornblumeAdapter`'s javadoc. Guessing a shop's currency would put invented
  prices into the optimizer's source set — a wrong number gets used, a missing
  one gets noticed.
- The name-to-slug mapping is the riskiest part of the adapter and is
  concentrated in one class, `Names`, which refuses a collision at declaration
  and refuses an unresolvable citation before a bundle exists.

## Reversal trigger

If F2 comes back with permission, the snapshots become ordinary test fixtures,
this ADR is superseded, and `RealUpstreamPatchTest` stops skipping — which is
the outcome to want, because it puts the best test in the pipeline.

If F2 comes back with a refusal, or if this service is ever deployed publicly
without an answer, the adapter stays and the deployment does not: no upstream
data reaches a public URL until somebody has said yes.
