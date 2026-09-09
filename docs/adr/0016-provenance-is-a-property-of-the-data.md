# 16. Provenance is a property of the data, and publishing enforces it

**Status:** Accepted · 2026-09-09 ·
Implements the sourcing half of
[ADR 0015](0015-game-data-is-sourced-first-hand-not-adapted.md).

## Context

ADR 0015 decided that the shipped product carries only game data this project
sourced itself: a fact enters a published bundle because someone observed it in
the game or read it from the publisher's own disclosure, and re-typing an
aggregator's numbers into our own JSON is laundering rather than sourcing.

**That decision has a hole in it that no amount of prose can close.** A number
read off a stage screen and a number copied out of Kornblume are *byte-identical
once typed*. Nothing downstream can tell them apart. Six months from now nobody
can tell them apart either, including the person who typed them. As written,
0015 was a promise about future behaviour with no mechanism, and this repository
already knows what that is worth: the adapter read a stale stage table for five
sessions with two thirds of the game missing, and every test stayed green,
because nothing was checking the thing that was wrong.

0015 says provenance "becomes a property of the data, not of the README" and asks
for it "per fact or at worst per source". This record settles what that means.

`game_data_version.attribution` is not it. It is one required sentence per
version, written for a reader, and it travels to the API on every response —
that is a credit line and it is doing its job. It cannot say that a banner's
rates came from the publisher's own rules screen while the stage beside it came
from somebody counting runs, and that distinction is the entire content of 0015.

## Decision

**Every declared fact in a bundle resolves to exactly one `Provenance`, and
`publish` refuses a version carrying facts this project did not source.**

Four choices make up that sentence.

**1. Provenance lives on the bundle, not on the domain records.** `Stage`,
`Item` and `Entity` gain nothing. A bundle carries a small dictionary of
`Provenance` records, a `sourcedBy` default, and a `factProvenance` map of
overrides keyed by `FactRef` — `stage:1-1`, `item:silver-ore`, `entity:sotheby`.

Sourcing is a property of the act of reading, not of the game, and the invariant
in play is the project's oldest: no game-specific code in `planner`, `gacha` or
`stats`. Hanging provenance on `Stage` would put it one field access away from
the solver, and a solver that *can* read where a number came from is one that
can eventually be made to prefer numbers from one place — a bias nobody asked
for and nobody would notice. Keeping it in the ingest layer means `planner`
cannot see it even by accident.

`Drop.sampledRuns` is the deliberate exception and stays where it is. The solver
genuinely reads it, because [ADR 0011](0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md)
makes the width of a yield's interval part of the answer. That is a statistical
fact about the number, not a claim about who typed it.

**2. A default with overrides, not a field on every fact.** The realistic
bundle is one sitting, one screen, one reader. Requiring 2 700 identical strings
would produce 2 700 copy-pastes and no more truth, and the pressure would be to
generate them — at which point the record means nothing. A default that is
stated once and overridden where it is actually different keeps every entry in
`factProvenance` a deliberate claim.

**3. Silence parses, and cannot publish.** A bundle that declares no provenance
is not rejected by the parser. It is treated as declaring `Origin.UNRECORDED`,
which is **not first-hand**, so it fails the publish gate and says so.

This is the choice most likely to look wrong later, so the reasoning is worth
keeping. A parser that *required* the field would make every throwaway test
bundle carry six lines of ceremony, and ceremony under that pressure becomes
meaningless — the field would get filled in with whatever passes. Letting
silence parse and refusing to *publish* it puts the check at the step that is
already a human approval, and gives absence a defined, conservative meaning
rather than leaving it a hole. It is the same shape as `sampledRuns == 0`
meaning *declared*.

**4. The gate is on `publish`, not on `ingestDraft`, and it has an explicit
escape hatch.** Ingesting somebody else's data is exactly how the Kornblume
adapter stays useful under 0015 — read, diffed against a self-sourced bundle,
never shipped — so a cross-check has to be able to reach a published version to
be diffed against one. `publish(game, sequence)` is the short call and the
strict one; `publish(game, sequence, true)` accepts second-hand data and the
caller owns the claim that it is not being shipped. The CLI spells it
`publish <game> <sequence> second-hand`, a word somebody has to type that ends
up in a shell history.

`KornblumeAdapter` hard-codes `THIRD_PARTY` and cannot be told otherwise, so
there is no call site to launder data through.

**Storage materialises every fact's provenance.** The default is an authoring
convenience; the database gets one row per declared fact, so "where did this
come from" is answerable without the file beside it. Game data is small — that
is [V2's first decision](../../backend/app/src/main/resources/db/migration/V2__gamedata_canonical_schema.sql) —
and this costs one narrow row per fact.

**The first-hand policy is in `Provenance.Origin` and nowhere else.** The
migration constrains `origin` to the known set and says nothing about which of
them count; the publish gate reads origins back as text and asks the enum. A
`WHERE origin <> 'THIRD_PARTY'` would be a second copy of the policy in a
language that cannot fail to compile when the first one changes.

## Consequences

- **"Self-sourced" becomes falsifiable.** `GameDataBundle.secondHandFacts()`
  names them; the CLI prints the breakdown on `validate`, `preview` and
  `ingest`, so the human approval 0015 depends on can actually see what it is
  approving. Counts rather than a yes/no, because one second-hand fact and 2 700
  are different decisions.
- **The one publish of real upstream data in this repository now says so in
  code.** `RealUpstreamPatchTest` passes `true`, which is the test admitting what
  the ADR says in prose.
- **Provenance is written but not yet read back out.** The publish gate queries
  it; nothing serves it. A catalog page saying where a number came from is the
  honest end state and belongs with Phase 4's catalog screens (**N25**), not
  ahead of them.
- **`Origin` is an enum in a module whose convention is that variation is data.**
  Rarity is a `(label, rank)` pair for exactly that reason. This does not vary by
  game — it is this project's sourcing policy — and adding a member should force
  somebody to answer `isFirstHand()` for it.
- **Granularity stops at the declared row.** A stage's energy cost and its drop
  table share one provenance. They are read off the same screen in the same
  sitting by the same person, and splitting them would multiply the typing
  without splitting the claim. If a bundle ever needs to say that a stage's drops
  were sampled while its cost was observed, that is the trigger to go finer.
- **It does not stop a determined liar**, and is not meant to. Someone can mark
  a laundered fact `OBSERVED_IN_GAME`. What this makes impossible is doing it
  *silently* — the default is honest, the exception is explicit, and both are in
  the file that was approved.

## Reversal trigger

**Go finer than the declared row** when a real bundle needs two provenances
inside one fact — most likely a stage whose `Fixed` rewards are observed and
whose `Common` rates are sampled. That is a plausible shape and it is not this
one; see [the drop disclosure note](../game-facts/reverse-1999-drop-disclosure.md).

**Drop the default and require it per fact** if `factProvenance` ever grows to
most of the bundle. At that point the default has stopped being the common case
and is only hiding how mixed the sourcing is.

**Move the gate earlier than `publish`** if second-hand data is ever found in a
deployed version. The escape hatch is the failure mode, and the next step is to
make a second-hand version incapable of being served rather than merely
awkward to approve.
