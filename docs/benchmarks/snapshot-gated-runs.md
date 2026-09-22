# The snapshot-gated tests, run by hand, with dates

**Purpose.** Standing caveat 1 in [TRACKER.md](../../TRACKER.md) says the
strongest tests in this repository are the ones CI does not run. Upstream data is
fetched and never vendored ([ADR 0009](../adr/0009-upstream-data-is-fetched-never-vendored.md)),
so `RealUpstreamPatchTest`, `RealUpstreamPlanTest` and `CommunityBenchmarkTest` —
**16 tests** — skip themselves on the runner. Every performance number and every
comparison with an outside answer in the tracker comes from them.

A number with no date is a number of unknown age. **This file is the dated
record.** Append a section per run; never edit an older one. If a figure moves,
say so in the newer section rather than correcting the older — the drift is the
interesting part.

**How to produce a run.** From `backend/`:

```bash
bash tools/fetch-upstream.sh
```

```bash
./gradlew :app:test
```

**That second line needed `--rerun` until 2026-09-21, and the reason is the
finding of the first run.** Read the section below before trusting a run from
before that date. The numbers are read out of
`backend/app/build/test-results/test/TEST-*.xml`, whose `<system-out>` carries
what the tests print; counting `PASSED` lines in a console log undercounts.

---

## 2026-09-21 — the first dated run (N36)

Ran because the tracker's figures had no age. **Every load-bearing number it
carries is confirmed**, two are one notch off, and following the documented
workflow exactly turned out to prove nothing — which is the finding.

### The workflow was broken in two places, and both were silent

Standing caveat 1 says to run `fetch-upstream.sh` before trusting a green build.
Doing that on this machine produced a `BUILD SUCCESSFUL` in five seconds with
`:app:test` `UP-TO-DATE`, from the run that had no snapshots at all. Two
independent faults, each of which on its own makes the caveat's instruction a
no-op:

1. **`-Dstorm-almanac.upstream` never reached the test worker.** `RealUpstream`
   reads it with `System.getProperty`, but nothing in the build forwarded it, and
   a Gradle test worker does not inherit the CLI's system properties. So the
   script's own documented custom-directory mode —
   `./tools/fetch-upstream.sh /some/other/dir` — sent the tests to the *default*
   path, where a custom run has nothing, and all 16 skipped themselves silently.
   The script printed that command as its closing advice.
2. **The snapshot directory was not a task input.** It lands under
   `backend/build/`, outside every source set, so fetching it changed nothing
   Gradle could see. This is the same hole as `data/bundles` and
   `AuthoredBundlesTest`, which the build script already documents ten lines
   above — the fix was applied there and the identical case next to it was
   missed.

**Both are fixed in `backend/app/build.gradle.kts`, and the fix is proven by
measurement rather than by reading it:**

| Check | Expected | Measured |
|---|---|---|
| Property pointed at an empty directory | 16 skip | **16 skipped** — forwarding works; before the fix this run passed on the default path |
| Default path, snapshots present | 0 skip | **0 skipped** |
| Immediate repeat, nothing changed | `UP-TO-DATE` | **`UP-TO-DATE`** in 957 ms |
| Snapshots moved aside, no `--rerun` | task re-runs, 16 skip | **16 skipped** — before the fix it stayed `UP-TO-DATE` and reported 0 |

The first row is worth keeping: pointing the property at an empty directory is
now the cheapest way to reproduce CI's own behaviour locally, and it names the
three classes while doing it. **The input is declared `optional`** — absent
snapshots must stay a skip and not a build failure, which is what ADR 0009 is
for.

### Conditions

| | |
|---|---|
| Machine | `YOUNGKIET`, JDK 21.0.12 (Temurin), Gradle 9.6.0 via the committed wrapper |
| Docker Engine | 29.8.0, answering; Testcontainers at `api.version=1.44` ([E2](../../TRACKER.md#environment-notes-this-machine-only)) |
| Snapshots | 3.3 at `d49efab2a18f`, 3.5 at `8b40541a9c42` — 12 files, 6 per snapshot, fetched clean on the first attempt |
| Largest file | `3.5/arcanists.json`, 749 007 bytes; the sampled stage table is `3.5/stages3_3_greedy.json`, 37 219 bytes |
| Command | `./gradlew :app:test --rerun`, 3m 47s — `--rerun` because the numbers below were measured *before* the input declaration above existed; a run today does not need it |

### Counts

| | Tests | Skipped | Failures | Time |
|---|---|---|---|---|
| `:app:test` | **165** | **0** | 0 | — |
| `CommunityBenchmarkTest` | 5 | 0 | 0 | 3.485 s |
| `RealUpstreamPatchTest` | 3 | 0 | 0 | 11.072 s |
| `RealUpstreamPlanTest` | 8 | 0 | 0 | 175.952 s |
| **Whole build, all modules** | **419** | **0** | 0 | — |

The three gated classes are **16 tests**, exactly the number the tracker names as
skipping on CI, and all 16 ran. 419 across all modules matches the tracker's
figure to the test.

### What the tests printed

```
RealUpstreamPlanTest: 50 solves over reverse-1999 3.5 — median 1805 ms, p95 1808 ms, max 1813 ms
RealUpstreamPlanTest: solve 1806 ms, cached repeat 0 ms
RealUpstreamPlanTest: The search stopped on its time budget: this is the cheapest
  plan found, and no plan can be more than 2.40% cheaper.
RealUpstreamPlanTest: 118 insight-2 goals in 3.5, 0 unreachable, 0 unsolved inside 250 ms
CommunityBenchmarkTest — reverse-1999 3.5, 105 stages
CommunityBenchmarkTest: the plan costs 3880 Activity; the guide's own advice
  costs 4017 for the 11 benchmark materials in it
disagreements over 25%: 3 on a small sample, 0 not
```

**The nine agreements, on the yields the solver uses** — Bifurcated Skeleton,
Clawed Pendulum, Holy Silver, Salted Mandrake, Goose Neck, Red Lacquer Tablet,
Perpetual Cog, Pyroxene Ore, Alopecurus Pratensis. **Five of those agree on raw
point estimates too** — Holy Silver, Salted Mandrake, Perpetual Cog, Pyroxene
Ore, Alopecurus Pratensis — which is the five-to-nine gap
[ADR 0011](../adr/0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md) bought.

**The three disagreements over 25% all rest on a small sample**, and none does
not: Milled Magnesia (4-5h, 105 runs, over 5-8h, 367 runs — 145%), Liquefied
Terror (4-4h, 113 runs, over 9-3h, 496 runs — 210%), Silver Ore (2-3h, 1 270
runs, over 7-19h, 228 runs — 39%). On raw point estimates, before ADR 0011, there
were four: Rough Silver Ingot joined them.

### The two figures that moved

Neither changes a conclusion; both are recorded so the next run has something
honest to compare against.

- **p95 is 1 808 ms, where the tracker carries 1 807 ms.** One millisecond, on a
  median of 1 805 and a max of 1 813 — this is measurement noise on a 2 000 ms
  budget, not drift. What matters is that the budget is still met, by 192 ms.
- **The cached repeat is 0 ms, where the tracker carries 2 ms.** The solve it
  repeats was 1 806 ms, which is the tracker's number exactly. The cache is
  faster than the clock can see, so `0` and `2` are the same claim; the test
  deliberately asserts "fast enough not to re-solve" rather than a number, and
  that is why.

### What this run does not establish

- **It is one machine, once.** Nothing here is a regression guard; the next run
  is what makes it one.
- **It says nothing about PGR.** All 16 tests are Reverse: 1999 against
  Kornblume data, which [ADR 0015](../adr/0015-game-data-is-sourced-first-hand-not-adapted.md)
  says the product will not ship. The tracker's framing stands: these are
  *evidence about somebody else's numbers run through our solver*.
- **It does not answer Q5.** The 3.5 pinned here is the commit dated 2026-03-17,
  and whether that is anyone else's 3.5 is still open.

---

## Run 2 — 2026-09-22, thirty-seventh session

**Why it was run.** Not to re-measure. The session added two nullable columns to
`gamedata.banner`, a new table, a component to `GameDefinition` and a new list on
every bundle, so the question was whether anything in the optimizer's real-data
path moved. The figures below are a by-product of asking that.

| | |
|---|---|
| Snapshots | `bash tools/fetch-upstream.sh`, default location, same two pinned commits (3.3 `d49efab2a18f`, 3.5 `8b40541a9c42`) |
| Command | `./gradlew build` — no `--rerun`, and the task was out of date on its own because the source changed |
| Gated tests | **16, 0 skipped**: `RealUpstreamPlanTest` 8, `RealUpstreamPatchTest` 3, `CommunityBenchmarkTest` 5 |
| Whole suite | **435 tests, 0 skipped locally**, up 15 from 2026-09-21 and every one of the 15 new |

### What held

- **Nine benchmark agreements, and three disagreements over 25% all on a small
  sample, none not.** Identical to run 1, down to which three.
- **The guide's own advice still costs 4 017** for the 11 benchmark materials.
- **The two-second budget is still met.**

### The two figures that moved, again

- **p95 is 1 812 ms** (max 1 814), against run 1's 1 808 and the tracker's
  original 1 807. Three runs, three values inside 7 ms on a 2 000 ms budget, with
  188 ms of headroom — this is the machine, not the code. **Worth watching only
  if a later run leaves that band**, and worth noting that all three drifts have
  been upward.
- **The plan costs 3 877 Activity**, against run 1's 3 880 and the tracker's
  3 880. **Three Activity on 3 880 is 0.08%**, and the comparison it exists to
  make — cheaper than the guide's 4 017, which does not even cover the whole
  demand — is untouched. The likely cause is the solver's branch-and-bound
  landing on a different equally-good answer within its budget
  ([ADR 0010](../adr/0010-a-plan-is-the-best-provable-in-the-budget.md): a plan
  is the best *provable* in the budget, not the optimum), and nothing in this
  session's change touches the model. **It is recorded rather than explained** —
  a fourth run that moves it again in the same direction would be a different
  story from one that moves it back.

### What this run does not establish

Everything run 1 disclaimed still applies, unchanged: one machine, nothing about
PGR, and no answer to Q5. One thing to add — **nothing here exercises the columns
this session added.** The pull price and the progress names are PGR's, and every
one of these 16 tests is Reverse: 1999. What proves those is
`GameDataIngestTest`'s whole-graph equality and the sequence 7 publish reading
back as *no changes*, both of which are ordinary tests CI runs.
