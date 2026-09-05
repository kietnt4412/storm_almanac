# 7. Equipment is an `Entity`

**Status:** Accepted · 2026-09-05

Answers **Q6** and **F4** ([docs/prior-art.md](../prior-art.md) §4.3), which
blocked Phase 1 ingestion.

## Context

Kornblume ships `psychubes.json`: equippable, upgradeable gear with its own
material costs and its own level curve. The model has `Entity` (characters) and
`Item` (things in an inventory), and gear is obviously neither one at first
glance — it is an upgradeable thing that is *owned* rather than a character.

Punishing: Gray Raven has the same axis in Memories, so this is not a
launch-title quirk to be papered over. It has to be decided before a schema is
written, because the schema is where a wrong answer becomes expensive.

Three facts about the model as it stands decide this more than any argument
about what gear "is":

1. **`Upgrade` and `Goal` both key on `EntityId`.** Equipment is upgradeable,
   and "get this psychube to level 30" is a legitimate planning goal. Anything
   upgradeable must therefore carry an `EntityId` or force both records to
   change.
2. **The dual identity this needs is already in the model, deliberately.**
   `Item`'s javadoc: a *copy* of a character is an `Item` while the character
   is an `Entity`, because copies are consumed by Resonance and Memory
   enhancement. Gear needs exactly the same split — the psychube you own and
   upgrade against a goal, versus the duplicates fed to it as `Fodder`.
3. **Gacha does not care.** `BannerModel` and `FeaturedRule` are rarity-shaped;
   neither mentions `EntityId`. Whatever equipment turns out to be, the gacha
   module is untouched by it, which removes an entire axis from the argument.

## Decision

**Equipment is an `Entity`.** No third top-level concept.

`Entity`'s fields carry gear without being stretched: `rarity` and `statCurves`
are literal (gear has a level curve published the same way a character's is),
and a psychube's effect is a ranked effect with values and a description — which
is `Skill.Rank` exactly. `talents` is empty for gear, and so is `element`;
`element` is already a free-form string on "the game's own axis", so empty is a
legitimate value there rather than a lie told to a required field.

One field is added. `Entity` gains **`kind`** — an opaque, game-supplied string
(`"character"`, `"equipment"`) — because the catalog funnel has to know which
page shape to render and which URL to route, and something must tell a character
from a psychube. It is not a discriminator for behaviour:

> `planner`, `gacha` and `stats` must never read `Entity.kind`. It exists for
> the catalog surface and for ingest validation, nowhere else.

That constraint is asserted here and **not currently enforced by a test** —
`GameAgnosticismTest` scans for game slugs in code, not for reads of a
particular field. Recorded as a gap, not waved away; see *Consequences*.

### Rejected alternatives

- **A third top-level concept.** More honest to the surface reading and more
  code, but it forces `Upgrade` and `Goal` to grow a second key type or a
  parallel graph, which teaches `planner` a second shape of upgradeable thing.
  That is new branching in the module the non-negotiable exists to protect, paid
  for a distinction the second game does not draw either.
- **Keep the third concept but widen `Upgrade`/`Goal` to a sealed
  `UpgradableId`.** The tidy version of the above. Rejected because every
  implementation of both arms would be identical: a sum type whose cases never
  diverge is a cost with no reader.

## Consequences

- Ingest maps `psychubes.json` onto `Entity` with `kind = "equipment"`, and PGR
  Memories are expected to map the same way. If they do not, this ADR is the
  thing that was wrong.
- The catalog funnel groups and routes on `kind`. Because that string is
  game-supplied, a bundle that omits it produces an unroutable catalog — so
  `kind` is required at ingest, not defaulted.
- `Entity`'s javadoc no longer describes only characters, and is corrected in
  the same commit. The type is *not* renamed: it was already named against the
  character reading, and a rename now would churn every future call site to say
  something the javadoc already says.
- **Open gap:** nothing prevents a later commit reading `Entity.kind` inside
  `planner`. The natural home is an ArchUnit rule alongside
  `ModuleBoundaryTest`, or an extension of `GameAgnosticismTest`'s source scan.
  Carried as a follow-up rather than written now, because the guarded modules
  are still empty and a rule with nothing to guard passes vacuously.

## Reversal trigger

If a third title's equipment cannot be expressed as an `Entity` — if it has no
identity, no rarity, or no upgrade graph of its own — the abstraction has been
overloaded and equipment gets its own concept. Equally: if `kind` is ever found
being read in `planner`, `gacha` or `stats`, that read is the evidence that the
distinction is behavioural after all, and this ADR is superseded rather than
patched.
