# 24. An expiring grant is a deadline the plan reports, not a schedule it builds

**Status:** Accepted · 2026-09-21

## Context

Punishing: Gray Raven's events are not farms. The twenty-second session read the
client and found the guides wrong about this: an event pays **one-time grants**
rather than selling materials through a shop
([the reading](../game-facts/punishing-gray-raven-research-disclosure.md)), and
it ends on a date. That is the shape N30 was left holding after its shop half
was cut, and it is the last thing in the launch title's economy the planner had
never been told how to say.

The arithmetic was already right. `EnergyMip.occurrences` truncates a reward's
cadence against its own `closesAt`, so a weekly that shuts on day nine of a
sixty-three-day plan supplies one claim and not nine, and `Cadence.EVENT` and
`ONE_OFF` cap at one however long the horizon. `Availability.isExpiring()` has
existed since phase 1.

**What was missing is that nobody was told.** Two silences, and the second is
the one that matters:

- **A grant the plan leans on that closes inside the horizon.** The count is
  correct and the date is nowhere. A reader is handed a plan that has already
  spent income they have three days to collect, and the plan's only mention of
  time is an ETA in days.
- **A grant that closes too early to pay out once.** `claimable()` filters it to
  nothing and the model never sees it. A plan made dearer by an event that ended
  on day four is **indistinguishable from a plan that was always that dear** —
  the reader is not told they missed anything, because nothing in the pipeline
  remembers that there was anything to miss. This is the exact mirror of ADR
  0022's `withheldGrants`, except that a withheld grant at least had a reader to
  ask.

Two candidates were put to the maintainer on 2026-09-20 and the second was
chosen:

1. **Give the event grant a cost in days and let `FEWEST_DAYS` schedule the
   claim.** This wants to know *which* day a claim happens on, which is a time
   index, which is the thing [ADR 0013](0013-the-horizon-is-a-scalar-not-an-index.md)
   exists to refuse. p95 sits at 1 807 ms because no variable is indexed by day.
2. **Leave it as supply that `occurrences` already truncates, and report the
   deadline.**

## Decision

1. **An event never becomes a solver decision.** No time index, no per-day
   variable, no scheduling of a claim. ADR 0013 stands untouched, and this
   record is the place a future session finds out that the alternative was
   considered and refused on purpose.
2. **`Outcome` gains `expiringClaims`.** For each grant the plan actually leans
   on whose window shuts inside the horizon: the id, how many claims, the
   instant, and the whole days from the start of the plan until then. Only
   grants the plan claims — a date for something nobody was asked to do is how a
   note stops being read.
3. **`Outcome` gains `lapsedGrants`**, computed over the game's rewards before
   `open()` and `claimable()` remove them, and filtered to those granting
   something the goal set actually needs — the same relevance filter
   `withheldGrants` uses, for the same reason.
4. **A lapse is a window, not a cadence.** The test is *not* "has zero
   occurrences": a monthly reward in a seven-day horizon has zero of those and
   has lapsed nothing. It is that **the close is what removed it** — the same
   cadence over the untruncated horizon would have paid. Without that clause the
   note blames a deadline for a cadence, and tells a reader to hurry over a
   window that runs for another year.
5. **The refusal follows the same thread.** An item whose only source is a grant
   whose window has shut used to be refused with "does not come round inside a
   30-day horizon" — true, and it sends the reader off to wait for something
   that is never coming back. It now names the date and says the horizon held
   four of them and the window held none.
6. **`FEWEST_DAYS` says which way the horizon moves a deadline.** An expiring
   grant is capped by its own end date, so a longer plan collects no more of it,
   and a shorter one — which is what this objective searches for — may collect
   fewer, if the horizon it settles on ends before the window does. Saying that
   is what this record does *instead of* modelling it.
7. **One arithmetic, one place.** Every truncation against a `closesAt` goes
   through `EnergyMip.daysUntil`. Two copies of that rounding is a plan telling
   a reader they have three days to collect something it counted four of.

## Consequences

- `EnergyMip` gains **no variables and no constraints**. The change is two lists
  and four sentences, and the p95 the horizon-as-a-scalar bought is not touched.
- **No migration, no bundle change, nothing game-specific.** `closesAt` has been
  a parsed, stored, round-tripped field since phase 1; this record only reads it.
- **The plan view renders it for free**, because it renders every note. The
  frontend has still never rendered PGR.
- **The tracker's framing of the `FEWEST_DAYS` sentence was backwards, and this
  is the correction.** It read "a shorter horizon is the one thing that keeps an
  expiring grant in reach". It is not: `occurrences` takes
  `min(horizonDays, daysUntil(closesAt))`, so shortening the horizon never
  raises an expiring grant's claims and can lower them. The true sentence is the
  one in decision 6, and it is the one the code says.
- **One silence is deliberately left, and it is `opensAt` rather than
  `closesAt`.** A reward whose window has not opened is dropped whole by
  `open()`, including one that opens on day two of a sixty-three-day plan, and
  nothing says so. The direction is at least the safe one — the plan counts no
  income and is dearer than the truth, the same way `Cadence.MONTHLY` rounds —
  but it is the same kind of silence this record is about, one step to the left.
  It belongs to the unread half of `Availability` that **N20** and the banner's
  opening time also wait on, and pulling it in here would have made the change
  about the calendar instead of about the deadline. **`Lapsed` deliberately does
  not cover it:** "this closed" and "this has not opened" are different
  sentences, and one record reporting both under one name would be the third
  silence rather than the end of the second.
- **A lapsed grant is reported and cannot be acted on.** That is the point: it
  is there so the price of the plan is not a mystery, not so the reader can do
  something about it.

**Reverse this** the moment a game pays an event grant **on a schedule the
bundle can express** — a claim that is worth more on day one than on day thirty,
or a grant whose payout is a function of when it is collected. A deadline that
changes the *value* of a claim rather than the *count* of them is a scheduling
problem, and no note can stand in for one. Reverse decision 6 specifically if
`FEWEST_DAYS` ever searches upward as well as downward, because then a longer
horizon is a thing the reader can ask for and the sentence stops being a
statement about arithmetic and starts being advice.
