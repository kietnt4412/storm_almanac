# Authoring a first-hand bundle

**Who this is for: whoever is holding the phone.**
[ADR 0015](../adr/0015-game-data-is-sourced-first-hand-not-adapted.md) says a
fact enters a published bundle because someone read it in the game or in the
publisher's disclosure. That someone cannot be an adapter, a scraper or a
session of Claude — all three can only reach what somebody else already wrote
down, and re-typing that is laundering rather than sourcing. **The machinery is
finished and the reading is not.** This page is the loop.

Written 2026-09-09 (fourteenth session), when the mechanism landed
([ADR 0016](../adr/0016-provenance-is-a-property-of-the-data.md)) and the first
real bundle did not.

---

## What is already done for you

- **The canonical JSON is the authoring format.** There is no adapter to write.
  `docs`-free, schema-free: copy the shape out of
  [`proving-ground-1.0.json`](../../backend/app/src/test/resources/gamedata/proving-ground-1.0.json),
  which exists to hold every shape the format has.
- **`gamedata-cli` already does *preview, ingest, publish*.** A hand-written file
  goes through exactly the path an adapter's output does, with no privileges
  either way.
- **Provenance is a field, and publishing checks it.** A bundle that does not
  say where its facts came from parses fine and will not publish.

## The loop

**1. Declare where you are reading from, before you read anything.**

```json
"provenance": [
  {
    "id": "stage-screens-3.5",
    "origin": "OBSERVED_IN_GAME",
    "detail": "Stage detail screens, Reverse: 1999 Global 3.5, account level 60, read in one sitting",
    "observedOn": "2026-09-09"
  }
],
"sourcedBy": "stage-screens-3.5"
```

`detail` is the field that does the work. Write it so that somebody else could
go and check: which client, which region, which patch, which screen. "Read in
the game" is not a record.

**2. Pick the origin honestly.** The set is small and closed
(`Provenance.Origin`):

| Origin | Use it when |
|---|---|
| `OBSERVED_IN_GAME` | You read it off a screen. Most of the catalog |
| `PUBLISHER_DISCLOSURE` | The publisher stated it — summon rules, patch notes. **Gacha rates are here** |
| `SAMPLED_IN_GAME` | You played the content and counted. Put the run count on the `Drop` too |
| `AUTHORED_FIXTURE` | You invented it. Synthetic titles only |
| `THIRD_PARTY` | Somebody else's number. **Will not publish without saying so** |

**3. Override per fact where the sourcing actually differs.** Only where it
differs — that is what keeps each entry a real claim:

```json
"factProvenance": { "banner:the-debut": "rules-screen-3.5" }
```

References are `kind:slug` — `stage:1-1`, `item:silver-ore`, `entity:sotheby`.

**4. Run the loop.** Every one of the first three prints the provenance
breakdown, so you can see what you are about to approve.

```bash
java -jar backend/app/build/libs/storm-almanac.jar --gamedata=validate bundle.json
```

```bash
java -jar backend/app/build/libs/storm-almanac.jar --gamedata=preview bundle.json
```

```bash
java -jar backend/app/build/libs/storm-almanac.jar --gamedata=ingest bundle.json
```

```bash
java -jar backend/app/build/libs/storm-almanac.jar --gamedata=publish reverse-1999 0
```

If the publish is refused, it names the facts. That is the mechanism working.

## Start with one stage and one character

Not with a backfill. The point of the first bundle is to find out **what
authoring one costs** before committing to roughly 2 700 of them — and the
answer changes what the rest of the plan should be. Record the wall-clock time
it took in the tracker; that number is the input to every decision after it.

Suggested first pass, and it is deliberately small:

- one item you can see dropping,
- one stage: its energy cost, its `Fixed` reward, and which of its other rewards
  the game grades `Common` and which `Possible`,
- one character: rarity, element, and the material lines for Insight 1.

## The two things that will be tempting and are not allowed

**Do not open Kornblume "just to check a number".** The adapter is kept as a
cross-check for exactly one purpose: diffing a *finished* self-sourced bundle
against an independent reading of the same patch, afterwards. Consulting it
while authoring makes the diff meaningless, because it stops being independent.

**Do not put a number on a `Common` or `Possible` drop you have not counted.**
The game does not disclose those rates — see
[the drop disclosure note](reverse-1999-drop-disclosure.md) — so the only honest
sources are your own runs, with the run count on the `Drop`.
[ADR 0011](../adr/0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md) is
what makes twenty runs usable rather than embarrassing: the solver discounts a
thin sample to the low end of a 95% interval and the plan says how much it does
not know. **A sample of twenty is fine. A guess is not.**

## What is deliberately still missing

The `Fixed` / `Common` / `Possible` grade has nowhere to live in the model. It is
the cheapest first-hand signal on the expensive axis and it should be recorded —
with Phase 6, which is the first thing that would consume it, and not before.
See the drop disclosure note.
