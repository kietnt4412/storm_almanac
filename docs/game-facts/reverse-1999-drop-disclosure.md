# Reverse: 1999 — does the publisher disclose its own drop rates?

**This is the answer to [N26](../../TRACKER.md#next-actions), and it decides how
hard the rest of [ADR 0015](../adr/0015-game-data-is-sourced-first-hand-not-adapted.md)
is.** Recorded 2026-09-09 (fourteenth session).

**Status: secondary sources, not verified against the client by this project.**
Class B/C evidence in the sense of
[the economy facts](reverse-1999-economy.md#provenance-stated-first-because-it-is-the-whole-point) —
reached through a web search tool, so the citation trail is one hop longer than
it looks. Nothing here is in a bundle and nothing here is used by a test. **It is
a question about a question**, which is why secondary sourcing is acceptable for
it and would not be acceptable for a rate itself: it tells the project which of
two plans to make, and the plan it selects is the one where every number is read
first-hand anyway.

---

## The short answer

**No, for the half that matters. Yes, for the half that does not cost anything.**

| Axis | Disclosed? | Consequence |
|---|---|---|
| **Gacha / summon rates** | **Yes** — stated per rarity in the game's own summons rules, and the pity counter with them | `PUBLISHER_DISCLOSURE`. Static facts, first-hand, free. Answers half of **Q4** |
| **Stage drop rates** | **No numeric rate.** The stage screen labels each reward `Fixed`, `Common` or `Possible` and puts a number on none but the first | Only the `Fixed` rows are declared facts. Everything else is Phase 6 |

**So the bootstrap problem in ADR 0015 stands.** The cheap way out was that the
publisher discloses stage rates and 595 statistical facts collapse into 595
static ones. It does not, and they do not.

## What the game actually shows, and why the label is not nothing

A stage's reward list is a three-way qualitative grade, not a percentage:

- **Fixed** — the guaranteed drop. This *is* a numeric disclosure: 100%, every
  run, and the quantity is on the screen. It is readable first-hand in seconds.
- **Common** — drops often. No number.
- **Possible** — drops sometimes. No number.

Two things follow, and the second is the useful one.

**Every published percentage for a `Common` or `Possible` drop is somebody's
sample.** The community guides that carry them — and the aggregators behind
them — are crowdsourced run counts, translated and remixed from a Chinese
community spreadsheet. That is not a criticism of them; it is what the numbers
are, and it is why [ADR 0011](../adr/0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md)
insisting a yield carry its sample size turns out to have been the load-bearing
decision. It is also why *the twenty published rates this project benchmarks
against* were never a disclosure to check ourselves against; they are another
estimate.

**The label is itself a first-hand fact, and the model cannot currently hold
it.** `Drop` carries an expected yield and a sample size. It has nowhere to put
"the game says this one is `Common`" — which is a real observation, free to
collect, available for all 595 stage-item pairs before a single run is farmed,
and enough to *order* drops within a stage and to reject an estimate that
contradicts it. That is not a gap this session closed; see *What this changes*.

## How much of the current drop data is already declared

Measured, not estimated, against the pinned 3.5 snapshot's
`stages3_3_greedy.json`, where a stage's `count` of `1` marks a fixed-reward
stage rather than a sample:

```
105 stages, 779 drop facts:  15 declared (count == 1),  764 sampled
```

So **roughly 2% of the drop axis is free** and 98% has to be counted. The
reprieve is real and it is small.

## What this changes

1. **N26 is closed and the answer is "no".** The interim has to be chosen
   deliberately rather than waited out. ADR 0015's other two ways out are the
   live ones: launch the catalog first — it is public, needs no yields, and is
   already in Phase 4's scope — or seed thin, honest samples from the
   maintainer's own play, which ADR 0011 makes usable rather than embarrassing.
2. **Gacha rates are cheap and first-hand.** Phase 5 needs published R1999 and
   PGR rates to reproduce, and for R1999 the publisher states them in the
   client. That is a `PUBLISHER_DISCLOSURE` provenance and one screenshot's worth
   of reading. **Half of Q4 is answerable the moment somebody opens the game.**
3. **The `Fixed`/`Common`/`Possible` label wants a home in the model, and does
   not have one.** It is the cheapest first-hand signal on the expensive axis.
   Recording it is not this session's change and should not be bolted on
   speculatively — it costs a bundle field, a parser, a writer, a migration and
   a JDBC round trip, and it is only worth that if something consumes it. The
   consumer is Phase 6: a submitted report claiming a drop the game grades
   `Fixed` at 40% is wrong on its face, and a stage whose `Possible` row outranks
   its `Common` one on twenty runs is a sample to distrust. **Do it with Phase 6,
   not before it** — the same reasoning that deferred N20 and N18.

## Sources

Secondary, and listed so the next reader can go further than this did. None of
them is a disclosure; the first three describe one.

- [Material Farming Guide — dotgg.gg](https://dotgg.gg/reverse-1999/material-farming-guide/)
- [Reverse 1999 Material Drop Rate Guide — Gamezebo](https://www.gamezebo.com/walkthroughs/reverse-1999-material-drop-rate/)
- [Insight Materials Cheat Sheet — Prydwen Institute](https://www.prydwen.gg/re1999/guides/insight-cheat-sheet)
- [The Complete Gacha System Guide — GamingOnPhone](https://gamingonphone.com/guides/reverse-1999-the-complete-gacha-system-guide/)

**The obligation this leaves open is the same one Q4 already carried:** every
number above about the gacha side is secondary until somebody reads the rules
screen. The difference is that now there is somewhere in the data to record that
they did — see [ADR 0016](../adr/0016-provenance-is-a-property-of-the-data.md).
