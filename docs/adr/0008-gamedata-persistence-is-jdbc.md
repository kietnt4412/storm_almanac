# 8. The gamedata module persists with JDBC, not JPA

**Status:** Accepted · 2026-09-05

Settles the two questions TRACKER.md's **N6** left open before the persistence
layer was written: how `available_days TEXT[]` maps, and how the composite
`(version_id, …)` foreign keys map. Both settle at the same answer.

## Context

`V2__gamedata_canonical_schema.sql` is shaped by three decisions, all recorded
in the migration's own header:

1. a version is a full snapshot, never a delta;
2. a reference can never cross a version boundary, enforced by
   `UNIQUE (version_id, id)` on every parent and a composite
   `FOREIGN KEY (version_id, parent_id)` on every child;
3. draft and published are different states.

The plan of record said "JPA entities and a `GameDefinitionRepository`
implementation". Writing them out against this schema is what showed the fit is
bad, and the mismatch is not one awkward mapping but four:

- **The composite foreign keys.** Every child association shares the
  `version_id` column with its own table's primary key mapping. In JPA that is
  `@JoinColumns` with `insertable = false, updatable = false` on the shared
  column, repeated on roughly twenty associations. It maps, but the invariant
  that the schema states in one line becomes twenty annotations, each of which
  is silently wrong if the flags are omitted.
- **`TEXT[]`.** Needs a Hibernate `UserType` or an `AttributeConverter`.
  Through JDBC it is `rs.getArray(…)` and `connection.createArrayOf(…)` — no
  extension point, and the column stays readable in `psql`, which was the reason
  it is an array rather than a `smallint` bitmask.
- **The write path is a bulk insert of a whole snapshot.** Nothing is ever
  mutated, so dirty checking, the first-level cache and cascading all cost
  without buying anything. A batch insert is what this is.
- **The read path is "load one entire version".** Fifteen-plus collection
  associations under one root is `MultipleBagFetchException` by construction,
  and the workarounds — `@OrderColumn`, `Set` semantics, split queries — all
  distort the model to suit the fetch plan.

Underneath all four: **the domain is already records**. `GameDefinition` and
everything it holds are immutable, validated in compact constructors, and use
typed identifiers. JPA entities cannot be those records, so they would be a
second parallel model translated to the first — the mapping work JPA exists to
save is work this module has to do either way.

## Decision

**`modules/gamedata` reads and writes its schema with Spring's
`JdbcTemplate`.** Row mapping is explicit and lives beside the SQL.

`spring-boot-starter-data-jpa` stays on the classpath (it is what the
`springBootStarterJdbc` catalog alias resolves to today) and
`ddl-auto: validate` stays set. Neither is load-bearing for this module; both
are left for `identity` and `player`, which have not chosen yet, and removing
the starter is a change that belongs to whichever module makes that call.

Consequently:

- **`available_days` stays `TEXT[]`.** JDBC reads and writes it natively. The
  `smallint` bitmask fallback N6 held in reserve is not needed and is dropped.
- **The composite foreign keys stay exactly as written.** They are the
  invariant, and with hand-written SQL they cost nothing to honour: the
  `version_id` is a bind parameter in every insert already.

### Rejected alternatives

- **JPA anyway, for consistency with modules that do not exist yet.**
  Consistency with an unwritten module is not a constraint. If `player` later
  wants JPA it may have it; ADR 0002 gives each module its own schema precisely
  so this choice is local.
- **jOOQ.** A better fit than either, and genuinely tempting for a schema this
  relational. Rejected on scope: it adds a code-generation step to the build and
  a dependency to defend, to save row mappers for one module. Revisit if a
  second module reaches for the same shape.
- **JSONB blob per version.** Would make the snapshot write trivial. Rejected
  because it destroys the headline feature of this phase: the patch diff is a
  set comparison over rows, and diffing opaque blobs reports the whole object as
  changed. The migration already refused this for `skill_rank_value` and the
  same reasoning applies to the whole schema.

## Consequences

- Row mappers and insert statements are hand-written and are the bulk of the
  module's code. That is the cost, and it is paid in the open: the SQL that runs
  is the SQL in the file.
- `GameDataSchemaTest` keeps its value. It tests the schema through plain JDBC
  already, so it and the production code now exercise the same access path.
- Ingest validates the bundle **before** touching the database, naming the
  offending slug. The composite foreign keys still catch anything that slips
  through, but a constraint violation names a column, not a mistake, and an
  ingest tool whose error message is `stage_drop_item_fk` is not usable by the
  person approving a publish.
- The module gains a Jackson dependency for the canonical bundle format, and
  nothing else.

## Reversal trigger

If a second module chooses JPA and the two persistence styles start to diverge
in ways that cost more than the mapping would have — or if the row mappers here
grow a bug class that an ORM's identity map would have prevented — this is
reversed and `gamedata` moves to whatever the rest of the codebase settled on.
Equally: if a third module reaches for hand-written SQL over a schema this
relational, that is the evidence for jOOQ, and this ADR is superseded by that
one rather than patched.
