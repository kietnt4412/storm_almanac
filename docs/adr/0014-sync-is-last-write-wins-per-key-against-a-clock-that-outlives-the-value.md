# 14. Sync is last-write-wins per key, against a clock that outlives the value

**Status:** Accepted · 2026-09-09

Decides how two devices editing one profile are reconciled. It fills the hole
[phase 3's close](../history/tracker-archive.md) named and did not build: sync is
in phase 3's scope line, `PUT` was the only write, and `PUT` replaces an
aggregate.

## Context

`PUT /api/me/profiles/{id}/inventory` takes a complete map and stores it. The
signature is honest and the implementation matches it: delete every row, insert
the body. What it cannot do is the one thing a companion tool is for.

A player enters their inventory on a phone on the train, with no signal. A
browser tab at home, meanwhile, records the two hundred gold they spent. When the
phone reconnects and saves, the browser's edit is gone — not merged, not
flagged, gone — because the phone's body was a statement about the whole
inventory and the phone had never heard of the change. The reverse is equally
true. Whoever saves second wins entirely, and nobody is told anything.

Phase 4 makes this worse rather than better: an offline PWA is a device that is
*expected* to be behind.

Three families of answer were available.

**A conflict-free replicated data type.** Correct by construction, no
timestamps, no clock trust. It is also a per-value type change — a counter is
not an integer — and it costs the schema, the API shape and the client library
all at once. For a map of item slugs to quantities where the semantics are "the
player counted them and this is the number", a CRDT's merge (add both
increments) is not even the behaviour wanted: two devices that each read 40 in
the game did not observe 80.

**A revision token per aggregate, and refuse a stale write.** One number, easy to
reason about, and it turns every concurrent edit into a client-side retry. That
is a real answer for a document. For an inventory it means the phone that was
offline for an hour is told "reload and type it again", which is the behaviour
that makes people stop using a tool.

**Last-write-wins per key.** The map is the merge unit's natural home: two
devices that touched different items have an obvious answer, and two that
touched the same one need a tiebreak. `Inventory`'s javadoc has committed to this
since phase 3 — *"it can be patched one key at a time from an offline PWA and
merged last-write-wins per key"* — and V5 left the timestamp out deliberately,
saying it would arrive with the merge rather than before it.

## Decision

**A patch is a separate route with a separate method, and the merge is
last-write-wins per key.** `PATCH` on inventory and roster; `PUT` keeps its
replace-everything contract unchanged.

Four rules decide a key, and each exists because its absence is a *silent* wrong
answer rather than a visible one.

**1 · The client says when it edited, not the server.** A server-stamped time
makes an edit win for having arrived late, which is exactly the bug. So
`editedAt` is required per key — per key and not per request, because a device
that spent an hour offline changed one item at 09:00 and another at 11:00, and
one batch stamp would have to lie about one of them.

**2 · No edit may claim the future.** Trusting a client's clock costs something,
and this is the bill: a device set a year fast would pin every key it touched
against every later edit from anywhere, permanently, with no way for any device
to correct it. `editedAt` is clamped to the server's clock on write. Old is
allowed; future is not.

**3 · A removal is remembered after its value is gone.** V5's fourth decision is
that absent means zero, so clearing an item deletes the row — and a timestamp
living on that row dies with it. A device offline since before the delete then
re-adds the item, finds nothing to lose against, and wins: the item comes back
from the dead and nobody is told. **A clock that outlives its value cannot be a
column on the row it outlives**, so the clock is its own table and V5's decision
4 stands untouched.

**4 · A full save speaks for the keys it left out.** A `PUT` says "this is my
whole inventory", which is a claim about every slug in the game including the
ones the player has none of — and no per-key table can record that, because
there is no row to write it on and no way to enumerate the keys it would need.
Without a whole-aggregate watermark, a stale patch loses for a key the save
mentioned and wins for one it did not, which is a rule nobody could explain. So
`player.sync_watermark` holds one row per profile per aggregate, and an edit
older than it loses whatever key it names.

Ties go to the stored value. Two devices saving within the same second is not
exotic, and settling it towards what is already there makes the outcome a
property of the data rather than of which request the server happened to
schedule first.

**All four are decided in one SQL statement**, not in Java around a select.
Read-then-write across two statements is a lost update waiting for a scheduler
to find it, and two devices syncing at once is the case this exists for. The row
count is the answer: 1 means the edit won and its value is written, 0 means it
lost and nothing else happens.

**The response says which keys lost.** A merge that silently drops the losing
half leaves the client showing a value the server does not hold, and the player
then edits from a screen that is quietly wrong.

**Goals get no patch route.** An inventory and a roster are maps; goals are an
ordered list whose *order* is what the player is editing, and two devices that
reordered it have no per-key answer. A patch route here would have to invent
one, and inventing one means a plan computed against priorities nobody chose.

## Consequences

- **Two devices can both be wrong and the newer one still wins.** LWW does not
  find the truth, it picks a winner. For an inventory that is right — the newer
  count is the better estimate of a number the player read off a screen — and it
  would be wrong for anything where both edits should survive. Nothing here does.
- **A deleted key keeps its clock row forever.** No reaper, deliberately: a
  tombstone swept too early is rule 3's resurrection bug back with an extra step.
  For a few hundred slugs per profile the storage is nothing, and nothing reads
  either table on the read path — `inventoryOf` and `rosterOf` do not join to
  them — so the cost is not latency.
- **The clock trusts the client within a clamp.** A device with a clock an hour
  *slow* loses edits it should have won. That is the safe direction, it is
  visible in the `rejected` list, and it is a smaller failure than the
  alternative in rule 2.
- **One merge, one round trip per key.** The value write is conditional on the
  clock write's outcome, so a set-based single statement would need a CTE and
  would be unreadable. A sync batch is a handful of keys inside one transaction,
  each a primary-key lookup. If a batch ever gets large enough for that to
  matter, the fix is a set-based statement and not a different merge rule.
- **A merge is not an event.** Nothing publishes what changed, so nothing
  downstream can react to a synced edit. Not needed yet; it becomes needed the
  moment something wants to invalidate a plan when an inventory moves.

## Reversal trigger

**Replace last-write-wins the moment an aggregate arrives whose two concurrent
edits should both survive.** A note field, a tag list, anything where losing one
device's contribution is data loss rather than a stale count. That is the shape
CRDTs are for, and the argument above against them stops applying.

**Replace the clock's trust model if devices are observed writing implausible
times** — a `rejected` list that stays full for one profile is the observation.
The next step is a server-issued sync token carried by the client rather than a
wall clock, which costs a round trip and buys not having to trust the device.

**Add a tombstone reaper when a profile is observed carrying more dead keys than
live ones**, and not before. Reap only keys older than the longest plausible
offline period, which is a number this project does not have until it has users.
