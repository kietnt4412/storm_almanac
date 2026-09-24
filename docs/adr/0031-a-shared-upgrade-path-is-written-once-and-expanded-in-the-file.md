# 31. A shared upgrade path is written once, and expanded in the file

**Status:** Accepted · 2026-09-24

## Context

N41 is reading Selena: Pianissimo and Karenina: Effulgence in full, so that
the launch has three constructs with complete ladders (Q6). Helentine:
Lacrimosa's ladder is **57 of the bundle's 63 upgrade rows**: thirteen level
links, thirteen Promote steps, Evolve to SS, a leader-skill unlock, seven skills
at four rows each, and an SS passive's unlock.

The tracker said the first of the two new constructs "measures what is shared",
and nobody knew. On 2026-09-24 the maintainer answered it from two sources:
they **opened Selena's and Karenina's Level Up, Promote and Skill screens and
compared them with Lacrimosa's**, and they have levelled many S-rank
constructs. Their report: **the EXP per level, Cogs per Promote step, and Skill
Points and Cogs per skill level are the same for every S-rank construct.**
Levels had already been checked once: the level curve was read on Luna: Oblivion
on 2026-09-21 and written onto Lacrimosa, because the two constructs agreed at
three points.

**What stays different per construct is words, not numbers.** The shard that
Evolve spends (`inver-shard-lacrimosa`), the SS passive's name (`vestige`), and
the levelled skills' names, which appear in every state string (`delusional-spin-18`).

Without this change, adding the two constructs means pasting about 114 rows. A
typo in a pasted number is a valid bundle: it parses, it publishes, and plans
are silently wrong for one construct. If the game ever changes the ladder, the
same correction has to be made three times.

**The model is right to hold one row per construct, and this record does not
touch it.** A goal is "Selena to level 80". A roster entry holds the states *one*
entity has reached (ADR 0027), and the merge unit is the entity. The solver
needs to tell Lacrimosa's `promote-6` apart from Selena's. And some of the cost
differs per construct: each Evolve spends that construct's own shard.

## Decision

1. **A bundle may declare `ladders`: upgrade rows written once, with an
   `appliesTo` list of the entities that climb them.** The parser expands each
   ladder into ordinary `Upgrade` rows, one per entity per step, **before any
   of them is parsed**. It feeds them through the same `upgrade` parser a
   hand-written row goes through. `GameDataBundle`, the schema, the diff, the
   API and the planner never see a ladder, and are not meant to.
2. **What differs per entity is a placeholder, bound where the entity climbs.**
   A row says `{shard}`; each climber says `"with": { "shard":
   "inver-shard-lacrimosa" }`. An expanded row's id is the entity's id, a
   hyphen, and the row's id: exactly the ids Lacrimosa's rows already had.
3. **A list is walked by an `each` group, one copy of the whole group per
   word, in order.** The skill curve is four rows written once inside
   `{ "each": "skill", "upgrades": [...] }`, and a climber binds `skill` to its
   seven names. One level of grouping, no nesting.
4. **Five shapes are refused by name, because each reads as intended and does
   something else.**
   - A placeholder that is not bound.
   - A binding that no row mentions: it was meant for a row that is saying
     something else.
   - A list used outside the group that walks it.
   - A ladder row that names an entity.
   - A ladder with nobody in `appliesTo`: those are rows the author meant to
     publish that would silently never be published.

   Everything else, such as a shard that isn't declared as an item or ids that
   collide, is caught by the checks every row already gets. It is reported on
   the expanded row.
5. **Provenance is still per row, and the narrowest statement wins.** The
   order is: the climber's `sourcedBy`, then the row's, then its group's, then
   the ladder's, then the bundle's default. Each expanded row is written into
   `factProvenance` under its own `upgrade:` ref, so the ledger still holds one
   provenance per fact (ADR 0016). If a `factProvenance` entry *also* names a
   laddered row, the bundle is refused rather than settled by order: two
   statements can only agree by luck, and the one that loses is the one
   somebody believes.

   The climber comes first because of what it claims. A row's `sourcedBy` says
   where its *numbers* were read. A climber's says the sitting in which those
   numbers were seen to be *that construct's too*, and that sitting is the
   first-hand claim for every row it expands to.
6. **The ladder is not a fact and declares no provenance of its own.** It is
   this project's shorthand, like a progress kind's name (ADR 0028), and its id
   (`s-rank-construct`) is the bundle's word. That every S-rank construct climbs
   it is not in the model. It is true for exactly the entities listed in
   `appliesTo`, one at a time, each with its own provenance.

## Consequences

- **PGR's bundle holds Lacrimosa as a ladder, and it is still sequence 7.**
  The ladder parses to the same data as the file that was published:
  `VersionDiff` reports *no changes*, the same 117 facts, and every fact keeps
  its provenance. This was checked against the published file before the
  laddered one replaced it. Her 57 rows are now 30 ladder entries: the four skill
  rows are written once, not seven times.
- **Sequence 8 then fixed the skill list in one line.** Reading all four of
  her skill pages the same day found eight levelled skills, not seven: one
  recorded name (`astral-armament`) was on no page, and the Signature Move and
  the QTE were missing. Fixing that meant editing the climber's `skill` list.
  `preview` reported exactly four rows removed and eight added.
- **The round trip loses the shorthand, on purpose.** `CanonicalBundleWriter`
  writes what `GameDataBundle` holds, which is expanded rows. A bundle
  exported from the database is the long form. It means the same thing and
  parses to the same data.
- **Adding Selena or Karenina now costs one climber**: an entity, a shard
  item, seven skill names, an SS passive's name, and a provenance entry for the
  comparison sitting. That is N41, minus the numbers.
- **The ladder's rows for Evolve and the SS passive are claims for every
  climber too.** The maintainer's report covered levels, Promote and skill
  levels. That Evolve to SS is 30 shards for every S-rank construct, and that
  the SS passive unlocks at 2 Skill Points and 20 000 Cogs for every one, has
  to be confirmed before a second climber is added. If a construct differs,
  that row moves out of the ladder and back into the per-construct `upgrades`.
  The ladder doesn't need to grow a way to say "except".
- **A ladder can be wrong for everybody at once**, which is the price of it
  being right for everybody at once. A typo in the ladder moves every
  construct's plan the same way. `AuthoredBundlePlanTest` pins Lacrimosa's
  figures, so it would go red.

**Reverse this** if two constructs that are meant to share a ladder turn out to
differ in more than one or two rows, so that climbers need exceptions more
often than they need the ladder. The shared rows then go back to being written
per construct, and this record is superseded. **Reverse decision 5's refusal**
if a bundle legitimately needs one laddered row sourced differently from its
siblings for the same climber. That would be a per-row override on the climber,
not a second place to say the same thing.
