# Storm Almanac — tracker archive

**This is where [TRACKER.md](../../TRACKER.md) puts things it has finished with.**
Nothing here is deleted, superseded or summarised away: the tracker's own rule is
that a session-log entry is never deleted, only superseded, and moving the
history out of the live file is how that rule survives the file staying
readable.

Split out on **2026-09-08 (ninth session)**, when the tracker reached 2 412 lines
and had become a document nobody would read at the start of a session — which is
the one job it has.

**What lives here**

| Section | What it holds |
|---|---|
| [Session log](#session-log) | Every session, newest first. **New entries are appended here**, and the tracker keeps a one-line index. |
| [Completed next actions](#completed-next-actions) | B0–B7 and N1–N17 as they were written when they closed, including the reasoning and the things that went wrong. |
| [Closed phases, in full](#closed-phases-in-full) | The phase board's "Landed" prose for Phases 0, 1 and 2. The live tracker keeps the criteria and the caveats. |
| [Answered questions](#answered-questions) | Q1, Q5, Q6, Q7 and Q8, with the answers and how they were reached. |

**What does not live here** — anything still true and still operative. Open
questions, live caveats, the invariants, the gate, the deviations and the
environment notes all stay in the tracker, however old they are. Age is not the
criterion; being finished with is.

---

## Completed next actions

Ordered as they were done. A ticked box here means the exit criterion in the
entry was met, not that the code exists.

- [x] ~~**S1–S5 — D5's rehearsal, its first five stalls.**~~ **Moved out of the tracker 2026-09-26 (forty-fourth session)** to make room for S6–S8, which the second run found. Each closed in the session named in its line; the full accounts are in the session log, forty-second and forty-third. The lines as they stood:
      - [x] **S1 — a second profile on one game and server answered a bare 500** (2026-09-25). The rule is right; the refusal is now a 409 naming the holder, and the form says so before the click.
      - [x] **S2 — "where she stands" was one dropdown of ~70 raw ids across 13 tracks** (2026-09-25). Now one dropdown per track, tracks read off the upgrade graph; a goal row shows its own track. **The labels ("Flaming chord · 4") are guessed from the ids**, because no bundle names a track — naming them is data work, and `roster/tracks.ts` is the one place to read it from.
      - [x] **S4 — rank read as numbers, and the skills as one flat list** (2026-09-25). The game says "Elite ★3" and groups skills under Basic Skill, Special Skill, Evolution Effect and Common Effect, tagged by orb; players find a skill by its tag. Now the bundle carries those words on each step (ADR 0032, `V16`, sequence 11) and the roster shows the game's sections, tag first. Skill names are still guessed from ids.
      - [x] **S5 — the plan page said what to do in ids** (2026-09-26). Every row and four notes now read "Buy 10 Memory Enhancer IV for 87 Simulation Score", named from facts already published (`StepNames`); rows in reading order. Still raw: three grant notes, the refusal text, the ladder's slug.
      - [x] **S3 — no way on from one step to the next** (2026-09-25). Four steps now — Inventory, Roster, Goals, Plan — in one list the step bar, each page's Next and the home page all read; the menu follows it. Goals' Next saves first.

- [x] ~~**S7, S8 and the four smaller hesitations — D5's rehearsal, its second run.**~~ **Done 2026-09-26 (forty-fifth session)**, in [PR #54](https://github.com/kietnt4412/storm_almanac/pull/54); the account is the forty-fifth session-log entry. S8's first and last halves were named from facts already published (the plan's steps travel as data, the home page reads the game's name); the Cage's and the categories' took a bundle word, sequence 13 and [ADR 0033](../adr/0033-a-key-the-bundle-invents-may-be-given-a-word.md). S7 was agreed as proposed. S6 stays open: it is a reading. The lines as they stood:
      - [x] **S7 — one goal row holds one target.** "Lucia fully built" is ~12 rows, each picked from one flat list of 61. **Proposed, not agreed:** one target per track on a row, reusing the roster's `TrackPicker` — a screen change, so plan first.
      - [x] **S8 — raw ids still on screen.** The plan's *Paying for* note ("to Ace ★1" beside "to level-65", "to abyssal-lament-18"); the reach label `phantom-pain-cage-score` and the *Not counted* tier ids; inventory headings (`HARMONY-MATERIAL`, one `CHARACTER-EXP-POD-*` heading per size); the home page's `punishing-gray-raven · global`. The first and last are named from facts already published; the Cage's and the categories' want a bundle word.
      - [x] **Smaller, not yet stalls:** "(character)" where the game says construct; Karenina offered on the roster with nothing to record; "Weekly, score 30,000+" names no weekly; *Make one* from Inventory lands on home, not back.

- [x] ~~**S9 and S10 — D5's rehearsal, its third run.**~~ **Done 2026-09-26 (forty-sixth session)**, both as proposed; the account is the forty-sixth session-log entry. S9's totals are a new wire field beside the old name, so a page and a server a deploy apart cannot disagree by a factor. The lines as they stood:
      - [x] **S9 — a purchase reads as one cheap buy.** "Buy 1,200 Cogs for 1 Simulation Score × 429" is 514 800 Cogs for 429 Score; the reader has to multiply. Say the total (or the times first).
      - [x] **S10 — "What each material is costing you" all 0.00.** Every price is zero when nothing binds but the stage, and a panel of zeros reads as broken. Say why, or hide it when every price is zero.
      - [x] **Smaller** (the same session, asked for before closing): the notes open with solver-speak ("7 item constraint(s)"); rarity reads "6*" where the game draws ★; EXP Pods list L, XL, M. *Fixed in the run:* "Add a character" over a list with a weapon in it, and step 3's blurb. — Now plain words and last; ★ by sequence 14, on Neon; Pods XL, L, M by what they feed.

- [x] ~~**N41 — Put Selena: Pianissimo and Karenina: Effulgence on the S-rank ladder.**~~ **Done 2026-09-24 (fortieth session), with Lucia: Inverse Crown in Karenina's place** — the maintainer does not own Karenina and so cannot read her. Selena and Lucia each climb `s-rank-construct` with their own eight skill names, SS passive and shard, and each shard has the two Phantom Pain shop rows (10 at 10 Scars, 20 at 20). Published as sequence 9, locally and to Neon, each read back as *no changes*; the live API serves 61 upgrade rows for each of the three. The entry as it stood:
      **Every number is shared and confirmed** for all S-rank (maintainer, screens compared
      2026-09-24), so the path is written once ([ADR 0031](../adr/0031-a-shared-upgrade-path-is-written-once-and-expanded-in-the-file.md)).
      **Left is words, per construct** (the list is in *Resume here*), plus a provenance entry
      for the comparison. Sequence 8 is published in both databases, fixing Lacrimosa's skills.

- [x] ~~**N40 — See Neon suspend, then move the pool settings into `application.yml`.**~~ **Done 2026-09-24**, the same session it was opened: the maintainer read **SUSPENDED** in Neon's console with the keep-alive bot still pinging, and the first request after it took 2.06 s against 0.57–0.75 s warm — about 1.4 s per idle spell. The three settings moved into `spring.datasource.hikari` with that measurement beside them. The entry as it stood:
      `/api/health` touches no database, so the keep-alive bot wakes Render and not
      Neon — but Hikari's pool might. `MINIMUM_IDLE=0`, `IDLE_TIMEOUT=60000` and
      `KEEPALIVE_TIME=0` are **Render environment only, and unproven**. Leave the site
      idle ~10 minutes and read the compute status in Neon's console: *Idle* means
      they work and belong in the config file with a comment; *Active* means find
      what holds it awake before the free compute-hours run out.

- [x] ~~**B5 — Wire the real deploy: Vercel, Render and Neon.**~~ **Done 2026-09-24**: the `deploy` job's first run on `main` (run `35954626572`, `b8c21e6`) called the hook, saw `/api/health` report its SHA after 4m 27s — **so Render does expose `RENDER_GIT_COMMIT` at runtime** — and smoked through Vercel in 2s. The entry as it stood at close: **Started 2026-09-24**;
      [the six steps](#session-log) (thirtieth entry) stand.
      **Settled by the maintainer:** *one origin*, a Vercel rewrite to Render; *the sleeping
      tier*, their own keep-alive bot, **which must ping `/api/health`** — it touches no
      database; *Postgres on Neon*, because Render's free one expires (direct host, not
      `-pooler`, Singapore, `?sslmode=require`). **Live since 2026-09-24:** Render at
      `storm-almanac.onrender.com`, **PGR sequence 7 published into Neon at 02:56:41Z**, the page
      at `storm-almanac.vercel.app` rewriting `/api/*`, `/oauth2/*`, `/login/oauth2/*` to Render
      with an `index.html` fallback. **Found by running it:** `/actuator/health` 503 on an unused
      Redis; a PWA worker answering sign-in navigations from its cache; and **Vercel forwards the
      proto but not the host** — measured: Render passes `X-Forwarded-Host` through and Spring
      honours it, yet through Vercel the redirect URI named Render. So the Google
      `REDIRECT_URI` is **pinned in Render env**, and every redirect is **relative**
      (`use-relative-redirects`, or a reader who had just signed in landed on Render). Each
      has a test that failed first. **Unproven:** Hikari's pool may keep Neon awake —
      `MINIMUM_IDLE=0`, `IDLE_TIMEOUT=60000`, `KEEPALIVE_TIME=0` are Render env only, to move
      into `application.yml` once Neon is seen suspended. **The first real OAuth exchange ran
      2026-09-24**, the maintainer signed in and created a profile — writes and CSRF through the
      rewrite. **Production had no sign-out** (the only one was `/dev/sign-out`): `POST /logout`
      now answers 204, with a button, driven in a browser. **`deploy` is gated to a push to
      `main`**, calls a Render deploy hook (`RENDER_DEPLOY_HOOK` secret; Render's auto-deploy
      off, so a red build never ships) and waits for `/api/health` to report its SHA — *that
      Render exposes `RENDER_GIT_COMMIT` at runtime is unverified until it runs*. Phase 0's box
      is ticked by that run. **Cut, on the record: `:modules:identity-dev` is not deleted**
      ([ADR 0030](../adr/0030-the-development-sign-in-stays-because-the-hands-that-need-it-cannot-sign-in.md)) —
      automated sessions cannot sign in to Google and drive every signed-in screen through it.
      **B5 closes when `deploy` first goes green on `main`.**

- [x] ~~**N37 — A demand line that is not a catalog item renders as a slug.**~~
      Done 2026-09-22, ADR 0028. The entry said the fix was "a bundle field, a
      parser change and a sequence 7 — the same five pieces N20 and N33 each
      walked", and it was, plus a sixth the entry could not have known: **what
      the name should say.** The game calls all three PGR pools "EXP", so a
      first-hand name would have made the defect worse. The name is the bundle's
      own word and carries no provenance, which makes it the first text in a
      bundle here that is deliberately not a reading. The shadow-price half of
      the entry became a wire change — `Map<String, Double>` to a list of
      `{item, displayName, price}` — because a map has nowhere to put a name.
      **Not done and not the same fix:** a `choice:` line still renders its
      upgrade ids, and an upgrade's name would be the *game's* word, so a fact
      with provenance.

- [x] ~~**N28 — Give a banner a pull currency and a price, then write
      `IncomeModel`.**~~ Done 2026-09-22, ADR 0029. Two things in the entry were
      wrong and one decision in it was never needed.

      **Wrong:** "this is the only thing keeping a banner out of a bundle" — a
      banner had been in the PGR bundle since sequence 0 on 2026-09-19, and what
      was missing was its price. **Never needed:** "the ticket must be an `Item`
      and `Item.rarity` is required; tiles grade Cogs, Score and Scars, so try
      the ticket's tile before making rarity optional." The tile was tried. It
      grades the ticket **5★**, and the format change the research note had
      argued for since 2026-09-18 turned out to rest on which screens had been
      opened rather than on the game.

      **`IncomeModel`'s own signature was the real obstacle**, which the entry
      did not name: `projectedPulls(ProfileId, LocalDate)` names no banner, and
      PGR's four pools have four different tickets. It became
      `affordableWithin(game, banner, held, days, reach)`.

      **Cut on the record**, per the entry's "two more fields belong in the same
      change": the R1999 shop exchange and the copy cap are **N38**, because they
      are an engine change rather than fields and because R1999 has no first-hand
      bundle to exercise them.

- [x] ~~**N36 — Fetch upstream before trusting a green build, once, and write down
      what it proves.**~~ Done 2026-09-21. The entry as it stood is in the tracker's
      2026-09-21 revision; it said **"not a code change — an hour, and it either
      confirms the file or contradicts it."** It was a code change, and it did both.

      **Exit met, and the numbers are in
      [docs/benchmarks/snapshot-gated-runs.md](../benchmarks/snapshot-gated-runs.md)**,
      a file built to be appended to per run rather than edited, so a figure never
      again has no date. Both snapshots fetched clean (3.3 at `d49efab2a18f`, 3.5 at
      `8b40541a9c42`, 12 files), Docker 29.8.0 answering, `:app:test` 165 tests and
      **419 across all modules, 0 skipped, 0 failures** — and **all 16 snapshot-gated
      tests ran**: `CommunityBenchmarkTest` 5, `RealUpstreamPatchTest` 3,
      `RealUpstreamPlanTest` 8.

      **Every load-bearing figure in the tracker held.** Nine benchmark agreements on
      the yields the solver uses and five on raw point estimates — the gap ADR 0011
      bought, unchanged. 3 880 Activity against the guide's 4 017 over 11 materials.
      All three disagreements over 25% rest on a small sample and **none does not**.
      118 insight-2 goals in 3.5, 0 unreachable, 0 unsolved. The budget note still
      names its own gap, at 2.40%. **Two figures moved by a notch and neither means
      anything:** p95 is **1 808 ms** where the file carried 1 807, on a median of
      1 805 and a max of 1 813 — noise on a 2 000 ms budget; and the cached repeat is
      **0 ms** where the file carried 2, off the same 1 806 ms solve, which is the
      cache being faster than the clock rather than faster than before.

      **What it contradicted is the workflow itself, and this is the part worth
      keeping.** Standing caveat 1 has told every session since it was written to run
      `fetch-upstream.sh` before trusting a green build. Doing that produced
      `BUILD SUCCESSFUL in 5s` with `:app:test` `UP-TO-DATE` — from the run that had
      no snapshots at all. **Two independent faults, either of which alone makes the
      caveat's instruction a no-op:**

      1. **`-Dstorm-almanac.upstream` never reached the test worker.** `RealUpstream`
         reads it with `System.getProperty`, nothing forwarded it, and a Gradle test
         worker does not inherit the CLI's properties. So `fetch-upstream.sh /somewhere`
         — the script's own documented custom-directory mode, printed as its closing
         advice — pointed the tests at the *default* path, where a custom run has
         nothing, and all 16 skipped in silence.
      2. **The snapshot directory was not a task input.** It lands under
         `backend/build/`, outside every source set, so fetching changed nothing
         Gradle could see.

      **The second is the same bug as `data/bundles` and `AuthoredBundlesTest`, whose
      fix sits ten lines above it in the same file and whose comment explains the
      exact failure mode.** The lesson is not that the hole existed; it is that
      patching one instance of it did not prompt anyone to look for the next, and the
      next was adjacent. Both are fixed in `backend/app/build.gradle.kts`, and the
      input is **`optional`** on purpose — absent snapshots must stay a skip rather
      than become a build failure, which is what ADR 0009 is for.

      **The fix is proven by measurement, not by reading it.** Property pointed at an
      empty directory → **16 skipped** (before the fix, that run passed on the default
      path); default path → **0 skipped**; immediate repeat → **`UP-TO-DATE` in
      957 ms**; snapshots moved aside with no `--rerun` → **task re-ran, 16 skipped**,
      where before it stayed `UP-TO-DATE` and reported 0. **That first row is a keeper:
      pointing the property at an empty directory is now the cheapest way to reproduce
      CI's own behaviour locally**, and it names the three classes while doing it.

      **What the run does not establish**, recorded so the next one is not oversold:
      it is one machine, once, and not a regression guard until there is a second
      entry; all 16 tests are R1999 against Kornblume data that ADR 0015 says the
      product will not ship, so the tracker's framing stands — evidence about somebody
      else's numbers through our solver; and it does not touch Q5, since whether this
      3.5 is anyone else's 3.5 is still open.

- [x] ~~**N34 — Let `Roster` hold a set of states, not one.**~~ Done 2026-09-21,
      [ADR 0027](../adr/0027-a-roster-entry-holds-the-states-an-entity-has-reached.md), `V13`.
      `Roster.currentStates` is `Map<EntityId, Set<String>>` and `DemandResolver` seeds its walk
      from all of them. **The five pieces the item named were the five pieces it took**, which is
      worth recording because the last three items each found a sixth: the migration, the record,
      `achieved`, the wire format and the sync patch shape, and nothing else. **The decision inside
      it that is not obvious:** the primary key widens to
      `(profile_id, entity_slug, current_state)` rather than the column becoming `TEXT[]`. An array
      is one statement and no new rows, and it loses the per-state not-blank CHECK, makes a state
      unqueryable without `unnest`, and reintroduces the empty-array/absent-row ambiguity V5's
      fourth decision spent a paragraph removing. **The merge unit stayed the entity** and V6's
      clock was not touched: an edit states an entity in full, so a state left out of a newer edit
      is given up. Per-state clocks would let two devices that each advanced a different track both
      win, leaving a roster neither device ever held — `OfflineSyncTest` states that as its own
      case. **The frontend was the piece most easily got wrong:** the goal row's single `select`
      would have made recording a second track erase the first, which is the overcharge this item
      exists to remove, reintroduced at the last layer. It is a `select multiple` now. **What N34
      did not do:** states are still edited only on a goal row, so a reader cannot record a
      construct they have no goal for. That surface is N35's.
- [x] ~~**B6 — Make CI run on a push to `dev`.**~~ Done 2026-09-21. `.github/workflows/ci.yml`
      triggers on `push: branches: [main, dev]`. Exit criterion — *a push to `dev` with no PR open
      produces a run* — met by run `35576845184` on `cfa6fe4`, `gh pr list` empty at the moment of
      the push, green. **The trap was live while the item was being read:** `cc9e7d1`, the previous
      session's own commit, was sitting on `dev` unbuilt, which is the tenth occurrence and the
      first caught in the tree rather than recalled. **One decision inside it that a future session
      will be tempted to undo:** the concurrency group stays `ci-${{ github.ref }}`, so a push to
      `dev` while a PR is open from `dev` runs the workflow twice. Keying it
      `github.head_ref || github.ref_name` instead would dedupe them into one group — and
      `cancel-in-progress` would then cancel the older of the pair, which in practice means a push
      cancelling the PR's own required check. A cancelled run is not a passing one. **Two honest
      runs beat one that sometimes vanishes**, and the duplicate is the price of the trigger.
      **What B6 does not buy:** it does not watch the merge, so PR #25's failure mode — merged
      before its own run finished, green by luck — is untouched, and *wait for the run before
      merging* is still a human step. **The `deploy` job carries B6's other half as a written
      condition:** it is `if: false`, and turning it on in B5 must gate it to `refs/heads/main`,
      because an ungated deploy under this trigger ships every commit that lands on `dev`.
- [x] ~~**N33 — Price the twelve Promote gates. A reading, and the maintainer's.**~~
      Done 2026-09-21, PGR **sequence 6**, published 2026-09-21T02:49:27Z and read back as *no changes*.
      All thirteen gated levels priced and all thirteen Promote steps carry a
      `requires`. **The item's premise was wrong in a way worth remembering:** it
      asked for the per-level EXP curve, and the bundle never wanted one — a gate
      needs the smallest *Pod-expressible* total that reaches the level, which
      the Level Up preview gives without spending anything. Twenty-two readings,
      each gate bracketed by the total 1 000 below it falling short. **The level
      track became a chain in the same change**, because `DemandResolver` sums
      what it walks and spokes from `level-1` would have charged the ladder twice
      for a plan naming two gated levels; the total to Lv 80 is unchanged at
      497 000 and no published plan moved. Exit criterion — *the twelve gates are
      `requires` rather than prose* — met in full rather than by a prefix.
- [x] ~~**N20 — Put the game's day boundary on the game, not in the planner.**~~
      Done 2026-09-21,
      [ADR 0025](../adr/0025-the-day-boundary-is-a-property-of-the-game.md), `V12`.
      `DayBoundary(ZoneId, int hour)` on `Game`, the five pieces exactly as the
      thirtieth session planned them, and `EnergyMip.matchingDays` reading the
      game instead of `ZoneOffset.UTC`. **No variable indexed by day** — one
      `DayOfWeek` moves and no constraint row is written, so ADR 0013 stands.
      **Null is *unstated*, not midnight**, so every version published before
      `V12` reads back claiming nothing and plans exactly as it did. **A sixth
      piece the plan did not name:** `Facts` never flattened `Game`, so the first
      preview of the sequence that declares a boundary said *no changes* — found
      by running the tool, not by a test. PGR **sequence 5** carries the 05:00
      UTC reset, published and read back clean. **R1999's own boundary is Phase
      11's**, because its number is second-hand and its stage table rotates.
- [x] ~~**N30 — An expiring grant is a deadline the plan reports, not a schedule
      it builds (D3).**~~ Done 2026-09-21,
      [ADR 0024](../adr/0024-an-expiring-grant-is-a-deadline-the-plan-reports-not-a-schedule-it-builds.md).
      `Outcome` gained `expiringClaims` and `lapsedGrants`, `MipOptimizer` three
      notes, `whyNot` a window-aware refusal, and `EnergyMip` a `daysUntil` that
      every `closesAt` truncation now shares. **No time index, no per-day
      variable, and no new variables at all** — ADR 0013 untouched. Eight tests.
      **The option that was refused is in the ADR**, so nobody re-opens it: the
      alternative was to give the claim a cost in days and let `FEWEST_DAYS`
      schedule it. The session log entry has the one thing the plan got
      backwards.
- [x] ~~**B0 — Make it build.**~~ Done. JDK 21 via
      `winget install EclipseAdoptium.Temurin.21.JDK`; note the MSI does **not**
      set `PATH`/`JAVA_HOME` under winget, so both were set by hand in
      `HKCU:\Environment`. **Gradle is not in winget** and there is no
      choco/scoop here — the wrapper was generated by IntelliJ instead, pinning
      **Gradle 9.6.0**. One real failure, now fixed: ArchUnit rejects empty
      layers, and the three Track B layers are empty by design, so the rule now
      declares `withOptionalLayers(true)`.
- [x] ~~**B1 — `git init`.**~~ Done. Two commits, HTTPS remote. SSH was
      abandoned: the local ed25519 key is not registered with GitHub.
- [x] ~~**B1a — Commit the Gradle wrapper.**~~ Done in `c14e37b`, with the exec
      bit set in the index by `73afcde`. `backend/gradlew` is mode `100755` and
      `frontend/package-lock.json` is tracked. *This entry was already stale when
      the session opened — the work had landed and been pushed. Ticking boxes
      last is what let it rot; tick them as the work lands.*
- [x] ~~**B2 — Boot the app for real.**~~ Done. `docker compose up --build`
      brings all three services up healthy; Spring starts in ~5s, Hikari opens
      the pool, Flyway applies `V1__baseline.sql` and all six schemas exist.
      **It found exactly the class of defect it existed to find:**
      `/api/health` answered **401**. `modules/identity` puts
      `spring-boot-starter-security` on the runtime classpath, and with no
      `SecurityFilterChain` anywhere Boot's auto-configuration secured every
      route with HTTP Basic — invisible to a unit test that calls the controller
      method directly. Fixed in `63d76f1`. Verified live: `/api/health` 200,
      `/actuator/health` 200, unmapped paths 401, no session cookie.
- [x] ~~**B3 — Verify the frontend.**~~ Done. 415 packages resolved with no peer
      conflicts, `npm run typecheck` and `npm run build` both clean, PWA service
      worker generated. Tailwind warns "no utility classes detected" — expected,
      the Phase 0 shell uses inline styles; it resolves itself at Phase 4.
- [x] ~~**B4 — Get CI green.**~~ **Done: `ci` run #5 passed on `1a58dd5`**,
      2m 58s, both jobs. Confirmed from the Actions tab on 2026-09-05 (it took
      five runs; #1–#4 are written up below and each was a distinct failure).
      Also confirmed: the three `nightly-chaos` runs in the tab are **skips, not
      failures** — that workflow is `if: false` until Phase 10 has a suite, and
      a skipped scheduled run is the correct appearance. Do not chase them.
      **Caveat, and it is a real one:** #5 predates this session's three
      commits, and `build` now starts a Testcontainers Postgres. That has never
      run on the runner. B4 is green for the pipeline *as it stood* — see
      item 5 below.
      The four failures that got it there:
      1. `backend/gradlew` was committed mode `100644`. Git on Windows does not
         track the exec bit, so `./gradlew` was "Permission denied" on Ubuntu.
         Fixed with `git update-index --chmod=+x backend/gradlew`. **Watch for
         this on every future shell script added from Windows.**
      2. No `frontend/package-lock.json`, which broke the job twice over —
         `npm ci` requires a lockfile, and `setup-node`'s `cache-dependency-path`
         could not resolve. Generated; commit it.
      3. Run #4: **invalid YAML in `ci.yml` line 67** — no jobs ran at all. An
         unquoted `run:` scalar contained `: ` (colon-space) inside
         `echo "TODO(phase-0): wire..."`, which YAML reads as a nested mapping.
         Fixed with block scalars. The `deploy` job is now `if: false` until Q1
         is answered, because a job that echoes a TODO and exits 0 would paint
         the pipeline green while nothing deploys.
      4. Runs #1–#4 all failed for the reasons above. **Run #5 is the green
         one**, on `1a58dd5`.
      5. **Run #6+ — result not yet seen.** `dev` was pushed and merged to
         `main` via **PR #1** (`73007df`) at the end of the 2026-09-05 session,
         which fires the pipeline twice (the PR, then the push to `main`).
         Neither result was observed: `gh` is installed but not authenticated,
         so `gh run list` still refuses. **First action next session:** run
         `gh auth login`, then `gh run list --limit 5`.
         What is new in that run and has never executed on the runner:
         `ApplicationBootTest` starts a **Testcontainers Postgres**.
         `ubuntu-latest` ships a Docker daemon so it should pass. If it is flaky
         there, tag it and split it into its own job — **do not delete it.** It
         is the only test in this repo that has ever caught a real defect.
      **Validate YAML locally before pushing** — `js-yaml` in the scratchpad
      parses all five files in seconds and is cheaper than a CI round trip.
      A green pipeline is Phase 0's exit criterion, and a workflow that has never
      passed is not evidence.
- [ ] **B5 — Wire the real deploy.** **Deferred to Phase 4** — see deviation
      **D1**. No hosting will be provisioned before then. The `deploy` job stays
      `if: false` until there is a real URL to smoke.
- [x] ~~**B6 — Read the prior art.**~~ Done — see
      [docs/prior-art.md](docs/prior-art.md). Found a real defect in our `Drop`
      record (fixed) and narrowed Q2/Q3. Four follow-ups F1–F4 recorded there;
      **F4 blocks Phase 1 ingestion.**
- [x] ~~**B7 — Write the positioning paragraph.**~~ Drafted and placed at the top
      of the README. It is deliberately narrow, and takes its claims from
      [docs/prior-art.md](docs/prior-art.md) rather than from ambition: the
      differentiator is not "we have a solver" — ArkPlanner has one — it is
      *per-player, against estimates with confidence intervals, with the
      reasoning shown, behind a game-agnostic model.*
      **Re-read it before Phase 4's launch.** If the product cannot yet do
      everything the paragraph claims, the paragraph is a promise, not
      positioning, and one of the two has to change.
### Done 2026-09-05 (third session) — N1, N2 and N3

- [x] ~~**N1 — Confirm the pipeline is green on the current tree.**~~ **Done —
      green, twice.** Run `33963222427` (`ci` on the `dev` PR, 1m43s) and run
      `33963295323` (`ci` on the `main` push merging PR #1, 2m24s). Both
      success. **The specific thing B4 item 5 was worried about is confirmed,
      not assumed:** the log shows `ApplicationBootTest` executing on the runner
      — *"the health endpoint answers 200 to an anonymous request" PASSED* and
      *"anything that is not explicitly public is denied"* PASSED — so the
      Testcontainers Postgres really does start on `ubuntu-latest`. It did not
      skip. No need to tag or split it.
      Also re-confirmed from the CLI: the three `nightly-chaos` entries are
      `skipped`, exactly as B4 said. Do not chase them.
      **What it took:** `gh` is installed (2.100.0) but **not on `PATH`** — the
      same winget-MSI behaviour already recorded for the JDK under B0. It is at
      `C:\Program Files\GitHub CLI\gh.exe`. In PowerShell a quoted path is not a
      command; it needs the call operator:
      `& "C:\Program Files\GitHub CLI\gh.exe" run list --limit 8`.
- [x] ~~**N2 — Answer Q6 / F4: how is equipment modelled?**~~ **Done —
      [ADR 0007](docs/adr/0007-equipment-is-an-entity.md): equipment is an
      `Entity`.** The leaning on record held, but the deciding argument turned
      out not to be the PGR-Memories one. It is that **`Upgrade` and `Goal` both
      key on `EntityId`**, so anything upgradeable already has to be an
      `Entity` or force both records to change — and that the Entity/Item dual
      identity gear needs is *already in the model on purpose*, since `Item`'s
      javadoc puts a character's copies in the inventory while the character is
      an `Entity`. Also established, and it removes a whole axis from the
      argument: **gacha is untouched** — `BannerModel` and `FeaturedRule` are
      rarity-shaped and neither mentions `EntityId`.
      **Code change:** `Entity` gains a required `kind` field
      (`"character"` / `"equipment"`), opaque and game-supplied, for catalog
      routing only. Backend build green with it. It cost nothing to add now —
      **nothing in the repo constructs an `Entity` yet**, so the shape was still
      free; in Phase 1 it would have meant touching the ingest adapter and every
      fixture.
      **Gap carried, not waved away:** nothing stops a later commit reading
      `Entity.kind` inside `planner`, and `GameAgnosticismTest` scans for game
      slugs, not field reads. See **N4**.
- [x] ~~**N3 — Then open Phase 1.**~~ **Open. The canonical schema has landed**
      as `V2__gamedata_canonical_schema.sql` — 28 tables, 7 indexes, applied for
      real against PostgreSQL 16.15 (*"Migrating schema to version 2 — gamedata
      canonical schema"*, `Successfully applied 2 migrations`) and covered by
      six tests in `GameDataSchemaTest`, all passing. Three decisions are
      written into it and are worth carrying forward:
      1. **A version is a full snapshot, not a delta.** Every row belongs to one
         `game_data_version`; publishing inserts a fresh set and mutates
         nothing. That is what keeps an old plan correct after a patch, and it
         makes the diff an ordinary set comparison rather than a history walk.
         Game data is thousands of rows per version, so the duplication is not
         worth optimising away.
      2. **A reference cannot cross a version boundary — structurally.** Every
         versioned table carries `version_id` with a `UNIQUE (version_id, id)`,
         and every child uses a composite `(version_id, …)` foreign key, so a v2
         stage citing a v1 item is *refused by the database*. Costs one column
         per table. Without it the headline feature of this phase sits one
         ingest bug away from quietly lying, and nothing would show.
      3. **Draft and published are different states**, with a CHECK that a
         published row has an approval timestamp and a draft has none. Backs
         `GameDefinitionRepository`'s promise that "latest" means latest
         approved, never latest fetched.
      Also: **`attribution` is `NOT NULL` on every version.** "Numbers and text
      only, attributed" is a project invariant, so an unattributed snapshot
      should not be representable. Ties to **Q3**.
      **Still to do in Phase 1:** ingestion, versioned publishing with diffs,
      `gamedata-cli`, and tests over real patch data including a patch that
      changes something. See **N6**. **F1** (evaluate 必要的记录 as the real
      drop upstream) and **Q3** (assume nothing is redistributable — read to
      validate, never vendor) both still gate the ingest work.

### Done 2026-09-05 (fourth session) — N6

- [x] ~~**N6 — Ingestion, then publishing with diffs.**~~ **Done, all four
      parts: parser, persistence, diff, `gamedata-cli`.** 65 tests green, up
      from 26. **The schema has now held real data and given it back
      unchanged** — `roundTripsWithoutLoss` ingests a bundle, publishes it,
      loads it and asserts record equality across the whole graph. That was the
      single largest unverified claim in this file and it no longer is.
      **The two questions this entry left open are settled, and they settled
      together: [ADR 0008](docs/adr/0008-gamedata-persistence-is-jdbc.md) —
      the module persists with `JdbcTemplate`, not JPA.** Writing the mappings
      out is what showed the fit is bad in four separate ways at once: twenty
      composite `@JoinColumns` with `insertable=false`, a `UserType` for
      `TEXT[]`, a write path that is a bulk insert with nothing to dirty-check,
      and a read path of fifteen collections under one root. Underneath all
      four, the domain is already records, so JPA entities would have been a
      second parallel model — the mapping work an ORM exists to save is work
      this module does either way. Consequently **`available_days` stays
      `TEXT[]`** (JDBC reads it natively; the bitmask fallback is dropped) and
      **the composite foreign keys stay exactly as written** — with hand-written
      SQL, honouring them costs nothing.
      **What landed:**
      - **The canonical bundle format** and `CanonicalBundleParser` — Jackson
        tree model, not data-binding, so the domain records keep no mapping
        annotations and every rejection names its own position
        (`stages[1].energyCost must be a whole number`).
      - **`GameDataBundle`, which is deliberately not a `GameDefinition`.**
        A definition carries a `GameDataVersion`, which requires a non-null
        `publishedAt` — so an unapproved snapshot has *no representation* as a
        definition. The type system now says what the schema's
        `published_has_timestamp` constraint says.
      - **Validation before the connection opens.** Dangling references are
        reported all at once with the referrer named. The composite FKs would
        catch them too, one at a time, several seconds later, saying
        `stage_drop_item_fk` — which is not something the person approving a
        publish can act on.
      - `JdbcGameDataIngestRepository` (write, one transaction, draft
        replacement, published-is-immutable) and `JdbcGameDefinitionRepository`
        (read, one query per table, grouped in memory, published only).
      - **`VersionDiff`** over three axes — `PROGRESSION`, `CATALOG`, `GACHA`.
        Three, not the two the plan names: banners are neither material costs
        nor combat numbers. Subjects are diffed before fields, so a removed
        stage is one line rather than six.
      - **`gamedata-cli`**, as `--gamedata=<command>` on the same jar:
        `validate`, `preview`, `ingest`, `drafts`, `publish`, `versions`,
        `diff`. `preview` is the one that matters — it answers "what would this
        file change?" without writing a row, which is the question somebody has
        *before* they ingest. Exit codes separate a bad file (1) from a bad
        command line (2).
      - **Fixtures are a synthetic title, `proving-ground`, at 1.0 and 1.1.**
        Q3 stands: the community bundles carry no licence, so they are read to
        validate shape and never vendored. The 1.1 fixture carries one change
        of every kind on both axes and its header lists them.
      **It found a real schema defect, which was the point of doing it:**
      **a version could not be deleted.** `DELETE FROM game_data_version`
      cascades to `item` down one chain and to `skill_rank_cost` down another,
      Postgres does not order the two against each other, and the NO ACTION
      foreign key on the item side refuses. That is the *ordinary* draft-replace
      path, not an edge case. Fixed by
      `V3__gamedata_version_is_deletable.sql`: the nine item-referencing keys
      gain `ON DELETE CASCADE`. The tradeoff is written into the migration
      rather than buried — deleting one item row now silently deletes what
      referenced it, acceptable only because a version is written once and
      never edited.
      **Second surprise, smaller:** Spring's `@Repository` exception translation
      rewrites a bare `IllegalStateException` into
      `InvalidDataAccessApiUsageException`. Both refusals are named types now
      (`PublishedVersionIsImmutableException`, `NoDraftToPublishException`) so a
      domain refusal survives the trip out of the repository saying what it was.
      **Test-time note:** the two new database classes share one container and
      one Spring context through `GameDataDatabaseTest` rather than starting
      their own. `ApplicationBootTest` deliberately keeps its own — it exists to
      prove a cold start against an untouched database.

### Done 2026-09-06 (fifth session) — N7

- [x] ~~**N7 — Serve it. Phase 1's exit criterion is an API answer, not a
      test.**~~ **Done. Both exit questions are answered over HTTP**, by an
      anonymous caller, against a real database. Five routes under
      `/api/games/{game}`: `versions`, `entities`, `entities/{entity}`,
      `entities/{entity}/upgrades`, and `diff?from=&to=`. 75 tests green, up
      from 65.
      **Verified twice on purpose.** Ten tests in `GameDataApiTest` go over real
      HTTP through the real security chain, and then the whole loop was run by
      hand: `docker compose up --build`, both fixture versions ingested and
      published with `gamedata-cli` against the compose Postgres, then every
      route curled. That second check is not ceremony — the last time this
      project served something for the first time, the thing that broke was
      invisible to every test that called a controller method directly.
      **Four decisions worth carrying forward:**
      1. **A read model, not the domain, on the wire.** Serialising the records
         directly would have shipped `{"value":"gold"}` for every typed id, made
         every client join the item table by hand to turn a cost into "6 ×
         Greater Sigil", and made every future domain change a breaking API
         change nobody noticed making. `GameDataView` is the wire format and
         `GameDataReadModel` is the only thing that maps to it.
      2. **`GameDataVersion` gained `attribution`, and every response carries
         it.** "Numbers and text only, attributed" was an invariant about
         ingestion for as long as nothing served the numbers. The moment they go
         to a stranger, the attribution has to travel with them, and the read
         path could not say where they came from — the field existed only on the
         schema row and on `DraftVersion`. Cost: one field on a `common` record
         and one column in three queries. It also strengthened
         `roundTripsWithoutLoss` for free, which now proves provenance survives
         the round trip too.
      3. **`GameDefinitionRepository.find` keys on a bare sequence.** It took a
         `GameDataVersion`, so every caller fabricated one with a dummy label and
         `Instant.EPOCH` to ask a question — a port shaped for its implementation
         rather than its callers. Adding `attribution` would have made that
         hack uglier in a third place, which is what surfaced it.
      4. **Version-pinning is on every read**, as `?version=N`, defaulting to
         latest approved. A plan computed against 1.0 has to stay explainable
         after 1.1, and a URL that cannot name its patch cannot be cited in a
         bug report either.
      **Also:** `/diff` serves structured changes as JSON and the exact text
      `gamedata-cli diff` prints under `Accept: text/plain` — one renderer, so
      the report a reviewer approved and the report a reader sees cannot drift.
      `SecurityConfig` permits `GET /api/games/**` and nothing else, so a POST
      there is denied rather than 405'd; publishing stays a human approval
      through the CLI and gets no endpoint. Refusals are RFC 9457 problem
      documents whose detail is written to be acted on — *"no published version
      99 of proving-ground"*, not a bare 404.
      **One thing to know before touching the tests:**
      `GameDataDatabaseTest` is now `WebEnvironment.RANDOM_PORT`, so the shared
      gamedata context serves HTTP. A second context differing only in that
      setting would have meant a second container and a second boot on every
      build. `ApplicationBootTest` still keeps its own container, for the same
      cold-start reason as before.

### Done 2026-09-06 (sixth session) — N9 and N8

- [x] ~~**N9 — Confirm the pipeline is green on this session's work.**~~ **Done
      — green, twice, on the exact commit.** Run `34002586027` (`ci` on the `dev`
      PR, head `e2239b3`, 1m26s) and run `34002589214` (`ci` on the `main` push
      merging PR #5, head `dc6ddcd`, 2m8s). Both success; `deploy` skipped as
      designed.
      **The specific worry is answered, not assumed:** `GameDataApiTest` executed
      on the runner and all ten cases PASSED, so the `RANDOM_PORT` context really
      does start Tomcat there. **No slowdown either** — the PR job went 1m43s →
      1m26s against the previous run, so the extra servlet containers cost
      nothing measurable. Nothing to absorb and nothing to split.
- [x] ~~**N8 — Decide whether a real upstream is ingested in Phase 1 at all.**~~
      **Decided by the project owner: build the adapter now, personal and
      portfolio use, ask about licensing before publishing.** Written up as
      [ADR 0009](docs/adr/0009-upstream-data-is-fetched-never-vendored.md).
      **Phase 1's second exit criterion is met: real patch data, in tests.**
      `:adapters:reverse-1999` converts a Kornblume snapshot; two real snapshots
      from the upstream's own history (3.3 at `d49efab`, 3.5 at `8b40541`) are
      ingested, published, read back and diffed. 93 tests green, up from 75.
      **The shape of the decision, because it constrains everything after it:**
      the data is *read* and never committed. `tools/fetch-upstream.sh` downloads
      into `build/upstream-snapshots`; `.gitignore` refuses it twice over. So
      **`RealUpstreamPatchTest` skips itself on CI, and the strongest test in the
      repository is the one the pipeline does not run.** Do not let a green tick
      imply otherwise — it is said in the test's javadoc, in the README, in the
      ADR and here.
      **It found two defects on its first contact with real data, which is the
      whole argument for having done it:**
      1. **`Names.slug` folded to ASCII first.** A character named *Зима* folds
         to the empty string and the entire ingest was refused. The rule is
         Unicode-aware now — letters and digits survive whatever script they are
         in — while an accent is still stripped so `Café Crème` reads
         `cafe-creme`. Nothing downstream had an opinion: ids are `TEXT` and
         `Identifier` asks only that they not be blank. The ASCII assumption
         lived in one method and would have lived there indefinitely.
      2. **The adapter read a row's `Name` before its release flag.** The real
         upstream ships unreleased psychubes as placeholder rows whose every
         field but `Id` and `Rarity` is `null`, so a conversion was refused by
         rows it was about to discard. Validate what you keep, not what you drop.
      **What landed:**
      - **`:adapters:reverse-1999`**, a Gradle module. `ModuleBoundaryTest` gains
        an `adapters` layer that only `:app` may reach, so a core module
        depending on a title is a build failure rather than a code review.
        `UpstreamAdapters` in `:app` is the only place in the product that names
        an adapter — one entry, one module, and that is the whole cost of a game.
      - **`UpstreamAdapter`**, the port, in `gamedata.ingest`. Files in, bundle
        out; no Spring, no database, no opinion about whether the result is fit
        to publish.
      - **`CanonicalBundleWriter`**, the parser read backwards, so an adapter's
        output becomes a *file* a human approves rather than an object that
        skips the approval. Pinned to the parser by
        `CanonicalBundleRoundTripTest` over both `proving-ground` fixtures, plus
        an idempotence case — a writer whose output drifted would fill every
        patch diff with noise.
      - **`gamedata-cli` gains `adapters` and `adapt`**, so the loop is now
        *adapt, preview, ingest, publish* and the adapter gets no privileges for
        being code.
      - **`tools/fetch-upstream.sh`** (committed mode `100755` — the B4 lesson),
        pinned to two upstream commits so the patch is a real interval of
        somebody else's history rather than one we composed.
      **The adapter converts less than the upstream publishes and says so on
      every run** — `skipped 590 resonance-pattern cost row(s)`, `skipped 4
      unreleased character(s)`, `skipped 1 stage(s) costing no Activity`. Each
      exclusion has a reason in `KornblumeAdapter`'s javadoc. The two worth
      remembering: `shops.json` states no currency and no price, and inventing
      them would put fake prices in the optimizer's source set (*a wrong number
      gets used, a missing one gets noticed*); and Frequency rows are
      alternatives for one step, which an `Upgrade` cannot express without
      charging a player for all four.
      **Verified twice, as usual, and the hand check is what proves it.** The
      whole loop was run against `docker compose` Postgres from the jar:
      `adapt` → `ingest` → `publish` for 3.3, then 3.5, then `diff`, then the API
      curled. Real answers: `GET /entities/regulus/upgrades` says Insight 2 costs
      40 000 Sharpodonty and 10 Scroll of Starlit Ascent, *in named items*;
      `GET /entities/igor?version=0` is a 404 reading *"no entity 'igor' in
      reverse-1999 3.3"*, because Igor was released in 3.5 — version pinning
      doing exactly what it exists for, against data nobody here authored. The
      published snapshot is 91 items, 99 stages, 50 crafts, 151 entities and
      2 012 upgrades.
      **Also settled:** **F1** deferred with a reason (Kornblume credits 必要的记录
      in every attribution; going direct is a second adapter, worth doing when
      the consolidated numbers are shown to be wrong, not on principle). **F2**
      is no longer a follow-up — it is a *release blocker*, and the ADR says so.

- [x] ~~**N10 — Confirm the pipeline is green on this session's work.**~~ **Done
      — green, twice, and both specific things checked in the log rather than
      inferred from a tick.** Run `34035918992` (`ci` on the `dev` PR, head
      `9b592b4`, 1m32s) and run `34035923889` (`ci` on the `main` push merging
      PR #6, head `5e4b9f4`, backend job 1m49s). Both success; `deploy` skipped
      as designed.
      1. **The new Gradle module builds on the runner.** All 18
         `KornblumeAdapterTest` cases PASSED there, including the two that
         mattered most for a first run on Linux — the Cyrillic slug case and the
         resource-directory lookup — plus all 3 `CanonicalBundleRoundTripTest`
         cases.
      2. **`RealUpstreamPatchTest` shows as three SKIPPED**, on both runs. Not
         passed, so no snapshot leaked onto the runner; not failed, so the skip
         condition is right. This is the designed state and it is the one to
         keep re-checking: the day those three turn green on CI without somebody
         deciding to make them, upstream data has been committed by accident.
      **No slowdown from the extra module** — the PR job went 1m26s → 1m32s and
      the main backend job went 2m8s → 1m49s. Six seconds for a module and 18
      tests.

### Done 2026-09-07 (seventh session) — N11 and N13

- [x] ~~**N11 — Open Phase 2, the optimizer core.**~~ **Opened and most of the
      way through it.** The MIP, demand resolution, crafting recursion, integer
      runs, explanation output and the cache key all landed; **both halves of the
      phase's exit criterion are not met, and only one of them is close** — see
      N12. 51 new tests, 93 → 144.
      **The warning in the old text was right and was worth writing down:**
      `Stage.potentialOutput()` rounds a yield up to an integer and is a
      catalogue figure, not a constraint coefficient. `YieldTable` reads
      `Drop.expectedYield` and nothing in the model touches `potentialOutput()`
      except the reachability check, where a rounded quantity cannot mislead
      because only the item's identity is read.
      **What landed:**
      - **`DemandResolver`** — the goal-to-items graph walk, backwards from the
        target so that an entity the player does not own and one part-way up a
        track are the same traversal. Refuses by name rather than guessing:
        unreachable state, unknown entity, probabilistic goal, a state reachable
        two ways (that is a choice, and a choice belongs to the solver).
      - **`EnergyMip`** — ojAlgo, integer runs and conversions, inventory
        subtracted, reachability pruning, and **variable bounds**, which is the
        single change that made a real patch solvable rather than a crash.
      - **`YieldTable`** — measured-or-declared, and the place a genuine unit
        mismatch between `stats` and `planner` is held at arm's length. **Q8.**
      - **`MipOptimizer`** and **`SolveKey`** — the port, the version pinning,
        the plan fingerprint, and an explanation with real marginal costs.
      - **[ADR 0010](docs/adr/0010-a-plan-is-the-best-provable-in-the-budget.md)**
        — what a plan actually promises, and the sharpened reversal trigger for
        ADR 0004.
      **Two defects found by real data, which is the argument for the real-data
      tests existing at all:**
      1. **A craft that consumes nothing is a mint.** The upstream lists base
         materials in `formulas.json` with an empty ingredient array; the adapter
         converted them into zero-cost unbounded sources, and the optimizer's
         first real solve crafted **294 250 Sharpodonty out of nothing**. Fixed
         in three places, deliberately: `Craft` refuses the shape (every future
         upstream), `CanonicalBundleParser` refuses it with a located message (a
         human approving a bundle), and `KornblumeAdapter` skips those twelve
         rows and prints that it did. The adapter *already* had this rule for
         zero-cost stages — *a free source is an unbounded one* — and nobody had
         applied it to conversions.
      2. **ojAlgo blew the JVM stack on a real patch.** Branch-and-bound recurses
         per node and dives depth-first with no incumbent, and an unbounded
         integer variable is an unbounded dive. Three fixes, in order of how much
         they bought: bound every variable, keep the objective integral (the
         millionth-weight conversion tie-break is now applied only when the craft
         graph actually has a cycle), and loosen the gap tolerance from seven
         significant digits to four. A solver failure is now distinguished from
         an infeasible goal, because "impossible" and "the solver fell over" are
         different answers.
      **And a fact about the game rather than about us:** **45 of 118**
      characters' Insight 2 cannot be planned from patch 3.5, because their
      materials appear only against the upstream's synthetic `Unreleased` stage.
      The optimizer refuses those by name. That number will move with every
      snapshot and is not a defect in anything.
      **Measured, on real data, and only reproducible locally:** p95 **1.8 s**
      over 50 solves of a five-character goal set (93 stage variables, 11 crafts,
      23 constraints), total 2 411 Activity, and the plan states it is **within
      2.95%** of anything that could exist. The search does *not* prove
      optimality inside the budget — it is stopped by it, every time. That is
      ADR 0010's whole subject and it is the number to re-read before deciding
      ojAlgo has passed.

- [x] ~~**N13 — Confirm the pipeline is green on this session's work.**~~ **Done
      — green, twice, and both checks read out of the log rather than inferred
      from a tick.** Run `34072743411` (`ci` on the `dev` PR, backend 1m37s) and
      run `34072752969` (`ci` on the `main` push merging PR #7, backend 1m56s).
      Both success; `deploy` skipped as designed. No slowdown worth the name for
      a whole new source set and 51 tests — the main backend job went 1m52s →
      1m56s.
      1. **The planner module's 30 tests executed on the runner.** All 30, all
         PASSED. This was the specific worry: a new test source set in an
         existing module that silently is not picked up looks identical to a
         green build.
      2. **`RealUpstreamPlanTest` shows as seven SKIPPED**, and
         `RealUpstreamPatchTest` as three — ten in all. Not passed, so no
         snapshot leaked onto the runner; not failed, so the skip conditions are
         right. **This is the state to keep re-checking**: the day any of those
         ten turn green on CI without somebody deciding to make them, upstream
         data has been committed by accident.
      **One correction it forced, and it is the reason to read the log:** this
      entry predicted *six* skips and the truth is seven. The local suite is
      **144** tests, not the 143 written down at the end of the session — the
      count was taken before the last test was added and then repeated three
      times without being recomputed. Fixed everywhere above. A number carried
      forward is a number nobody re-derived.

### Done 2026-09-07 (eighth session) — N12

- [x] ~~**N12 — Close Phase 2: the five benchmark goal sets.**~~ **Done, and it
      found a bigger thing than it was looking for.** The community answers are
      transcribed, cited and compared in
      [docs/benchmarks/reverse-1999-community-answers.md](docs/benchmarks/reverse-1999-community-answers.md);
      the comparison is `CommunityBenchmarkTest`, five tests, skipping without a
      snapshot like everything else that touches real data.
      **The source** is Prydwen's *Insight Materials Cheat Sheet (2.7 patch)*,
      read 2026-09-07 — twenty claims of the form *material → best stage → drop
      rate*. Nothing is vendored: twenty short factual claims with a URL is a
      citation, and ADR 0009 is about data files.
      **The defect it found before it ran once, which is the session's headline:**
      `KornblumeAdapter` was reading `stages.json`, and the upstream's own planner
      reads the newest `stages<major>_<minor>_greedy.json`. At both pinned commits
      `stages.json` carries **chapters 1 to 4 only** — 99 stages, overlapping the
      live 105-stage table by 40, with a drop-to-stage mapping the game has since
      retuned. **Two thirds of the game were missing for five sessions and every
      green thing stayed green.** The community names stages our data had never
      heard of, which is how it surfaced.
      Fixed in three places: the adapter picks the highest-versioned sampled table
      and divides drop counts by the sampled run count (as `normalizeDrops` does
      upstream), falls back to `stages.json`, and **says which file it read either
      way**; `tools/fetch-upstream.sh` pins the sampled table per snapshot
      (3.3 → `stages3_0_greedy.json`, 3.5 → `stages3_3_greedy.json`, because the
      drop data is resampled on its own schedule and the filename lags the label);
      and four new adapter tests cover preference, numeric version ordering
      (`stages10_0_greedy` must beat `stages3_3_greedy`), the fallback note, and a
      `count` of zero, which is a denominator and therefore a free source.
      **A previous "fact about the game" is withdrawn.** *45 of 118 characters'
      Insight 2 cannot be planned from patch 3.5* was a fact about the stale file.
      All 118 can. The test that asserted it now asserts the opposite, as a canary
      for the stage table going short again.
      **What the comparison itself says:**
      - **Five exact agreements** — Holy Silver (10-9H), Salted Mandrake (3-13H),
        Perpetual Cog (7-16H), Pyroxene Ore (9-1H), Alopecurus Pratensis (8-18H).
        That is the exit criterion, on its own terms. Seven more agree within 8%.
      - **Seventeen of the guide's quoted rates land within three percentage
        points** of a sample this project had never seen. Two independent
        measurements of the same game agreeing is the strongest evidence yet that
        the pipeline carries real numbers rather than plausible ones.
      - **The plan beats the advice**: 3 624 Activity against 4 017 for following
        the guide material by material — and the 4 017 does not cover the whole
        demand. One run drops five materials; a per-material plan pays for each.
      - **Four items in the guide's list are the same items under different
        English names**, and the rates are what identified them: Red Lacquer Slab
        is Red Lacquer Tablet, Golden Grass Incense is Golden Herb Incense,
        Luminite Ore is Pyroxene Ore, and Fox Tail is Alopecurus Pratensis, which
        is literally foxtail grass.
      - **Every disagreement over 25% is a sample-size disagreement.** See N17.
      **A third defect, found by the suite rather than by the benchmark.** With
      the new stage file the schema round trip failed on drop *ordering*:
      `Stage.drops()` is a `List`, the adapter emitted the upstream's JSON key
      order, and `JdbcGameDefinitionRepository` reads drops back ordered by
      `stage_drop.item_id` — a surrogate handed out in catalogue-ingest order.
      They coincided under `stages.json` and do not under the sampled tables, so
      *"the schema gives the data back unchanged"* had been true by coincidence.
      Both ends now sort by item id and an adapter test pins it.
      **The instruction in the old text was right and is worth keeping:** judge a
      disagreement against the model before the solver. Not one of these turned
      out to be the search. They were the input file, the sample size, and what
      the word "best" means.

### Done 2026-09-08 (ninth session) — N16 (and N17, below)

- [x] ~~**N16 — Get a clean local run and a green CI, then tick Phase 2.**~~
      **Done. Phase 2's box is ticked.** The eighth session's work had in fact
      been committed and pushed after this entry was written — `dev` is
      `93f4de4`, merged to `main` as **PR #8** (`d34a31b`) — so what this session
      owed was the half the entry insisted on: reading the log rather than the
      tick.
      **What the log says**, for run `34116854550` (`dev` PR, head `93f4de4`,
      1m 24s) and run `34116880372` (`main` push, head `d34a31b`, 1m 46s), both
      success: **139 PASSED, 15 SKIPPED, 0 FAILED**, `BUILD SUCCESSFUL in 1m 5s`.
      Both checks the entry named are satisfied — `:adapters:reverse-1999:test`
      ran and **all 24 of its tests PASSED on the runner**, and the 15 skips are
      exactly the three snapshot-gated classes and nothing else:
      `CommunityBenchmarkTest` 5, `RealUpstreamPatchTest` 3, `RealUpstreamPlanTest`
      7. Nothing that touches upstream data passed on CI, which is the state ADR
      0009 requires; a pass there would have meant a snapshot had been committed
      by accident.
      **The local half was not re-run and did not need to be.** It was green on
      this tree last session (154 tests) and the working tree is clean at the
      same commit — re-running proves the machine, not the code.
      **Phase 2 is closed on its criterion, and Phase 3 is not opened.** The
      criterion is two measurements and both are made; the phase's scope still
      has N17, N14 and N15 in it, and N17 goes first because a sample size the
      model throws away is currently picking stages.
      **Worth keeping:** the tracker said "nothing is committed" and the
      repository disagreed. Both were written by the same session. Push before
      writing the entry that describes the push, or the file lies in the one
      direction it is meant to prevent.

- [x] ~~**N17 — Give `stats` a mean per run and a sample size, and let the
      optimizer see it. This is Q8.**~~ **Done 2026-09-08, and it bought more
      than it was asked for.** The sample size now travels the whole pipeline —
      `Drop.sampledRuns`, the canonical bundle (optional; absent means
      *declared*), `V4` on `stage_drop`, both JDBC directions, the patch diff —
      and `YieldTable` hands the solver the lower end of a 95%
      `PoissonRateInterval` on the mean instead of the mean itself.
      [**ADR 0011**](docs/adr/0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md)
      records it and narrows ADR 0006 rather than superseding it: Wilson stays
      right for *"did it drop"*, and *"how many dropped"* is a rate, unbounded
      above, and needs the counting analogue.
      **What it measurably changed**, all from `CommunityBenchmarkTest` and
      `RealUpstreamPlanTest` on the real snapshot:
      - **Exact agreements with the community guide went from five to nine.**
        Bifurcated Skeleton (10-13H), Clawed Pendulum (11-5H), Goose Neck (5-4H)
        and Red Lacquer Tablet (9-1H) joined the five, in every case because this
        project had been preferring a thinly sampled stage with a flattering
        mean. **The change was made for the evidence, and the agreement followed;
        that ordering is the only thing that makes it worth reporting.**
      - **Disagreements over 25% went from four to three.** Rough Silver Ingot
        fell from 29% to 7.9%. Milled Magnesia (145%) and Liquefied Terror (210%)
        shrank and did not resolve, and **that is the honest outcome**: a 95%
        bound discounts a thin sample in proportion, it does not dismiss it, so a
        105-run mean four times higher still wins. Both remaining rows still have
        a small sample on one side, which is what the test asserts.
      - **The plan costs 7% more** — 3 624 → 3 880 Activity on the same goal set,
        against the guide's 4 017. The old number was optimistic rather than
        cheap. p95 is **1 805 ms** (budget 2 s) and the optimality gap **2.37%**.
      **Three things worth carrying forward:**
      1. **The benchmark was ranking on numbers the solver does not use.** It
         computed Activity-per-unit from `Drop.expectedYield()` directly, so with
         N17 in place it would have gone on reporting the old ranking forever. It
         now ranks on `YieldTable`'s coefficients and prints the raw-point-estimate
         ranking beside them. A benchmark that does not consume what production
         consumes measures the wrong thing quietly.
      2. **`count: 1` in this upstream is a declaration, not a sample.** All 14
         such stages are the Insight and Resource ones with flat payouts, and
         every sampled stage carries at least 105 runs. Reading that 1 as a
         sample would have put a 95% bound on a number nobody measured and made
         the only source of several currencies look unfarmable. The judgement
         lives in the adapter, which is the module allowed to know a game.
      3. **`stats` owns the statistics and `gamedata` only carries the number.**
         `PoissonRateInterval` is beside `WilsonInterval`; `Drop` holds a count
         and says in prose where the decision about it lives.

### Done 2026-09-08 (tenth session) — PR #9 and N15

- [x] ~~**Merge PR #9.**~~ **Already merged** when the session opened, at
      `2026-09-08T01:17Z`, and its `main` push run `34180192277` was green in
      2m 12s. `main` is `176f151`. The ninth session pushed the merge and the
      tracker recorded the intent rather than the outcome — the mirror image of
      the mistake the ninth session's own entry warns about, and the reason the
      first act of a session should be reading the remote rather than the file.

- [x] ~~**N15 — Cache a solve on its key, and implement `SolveCoordinator`.**~~
      **Done, in a shape the entry did not ask for and an ADR explains.**
      `SolveCache` is the port — `get` and `put`, and deliberately no
      invalidation method, because a key carries the game-data version, so a
      patch does not stale an entry, it makes a different key under which nothing
      is stored. `InProcessSolveCache` is a bounded LRU with hit and miss
      counters. `SingleNodeSolveCoordinator` is the queue: one execution per
      idempotency key however sixteen threads interleave, a pollable ticket, an
      honest queue depth.
      **The measurement that justifies it:** on the real 3.5 patch a repeat
      question goes **1 806 ms → 2 ms**, measured in `RealUpstreamPlanTest`
      beside the p95 rather than on the toy fixture, where a hit and a solve are
      both too fast to tell apart.
      **Not Redis**, which is what ADR 0003's table says — see
      [ADR 0012](../adr/0012-the-solve-cache-is-in-process-until-there-is-a-second-node.md).
      Carried forward as **N19**.
      **Drop estimates are still not in the key**, as the entry required; carried
      forward as **N18** so that it is read before Phase 6 rather than after.

### Done 2026-09-08 (eleventh session) — PR #10 and N14

- [x] ~~**Merge PR #10.**~~ **Already merged** when the session opened, and its
      `main` push run `34183236122` was green in 1m 55s. `main` is `9bad5aa`.
      Second session running where the tracker recorded the intent and the remote
      already held the outcome — **read the remote before the file.**

- [x] ~~**N14 — Give the solver a time axis, and with it rewards and rotation.**~~
      **Done, and the formulation is the decision worth remembering**
      ([ADR 0013](../adr/0013-the-horizon-is-a-scalar-not-an-index.md)). The
      horizon is a **scalar**, not an index: day-indexing every stage variable
      would have taken a hundred integer variables to three thousand and broken
      the p95 Phase 2 was closed on. Every consequence of time is linear in a
      fixed horizon instead — an energy cap, a cadence count, and rotation as a
      capacity shared over *subsets* of distinct weekday restrictions.
      `FEWEST_DAYS` is a binary search over the horizon, because feasibility is
      monotone in it, and not a variable the branch-and-bound has to carry.
      **The number that mattered held: p95 1 805 ms → 1 807 ms**, and the
      benchmark is unchanged to the unit — 3 880 Activity against 4 017, nine
      agreements.
      **The two objectives are now different plans.** On the acceptance fixture,
      Insight 1 costs **0 energy over 28 days** or **370 energy over 2**.
      **What the entry got wrong, and it matters:** it scoped N14 as "rewards
      plus rotation" on the grounds that shops had no upstream data. Rewards and
      rotation have none either — `KornblumeAdapter` emits `Availability.ALWAYS`
      for every source and no rewards at all. So on real data this change is
      exercised by the energy budget alone and everything else is proven on the
      synthetic fixture. Carried into the tracker's *unverified* list rather than
      left to be rediscovered.
      **Shops stayed out**, per the tenth session's decision, but their refusal
      changed meaning: the cap now has somewhere to live, and what is missing is
      a price, a currency and a reset period the upstream does not publish. A
      `Shop` variable is about a dozen lines the day one does.

---

### Done 2026-09-08 (twelfth session) — PRs #11 and #12, N21 and N22

- [x] ~~**Merge PR #11.**~~ **Already merged** when the session opened, and so
      was **PR #12** behind it. `main` is `eb2894a`; runs `34203224613` and
      `34205089646` both green, **16 skipped and exactly the three
      snapshot-gated classes**. **Third session running** where the tracker
      recorded the intent and the remote already held the outcome. The lesson is
      not "check the remote" — the last two entries said that. It is that a
      session which pushes and then runs out of room to write the line leaves
      this exact residue, and the cheap fix is to write the tracker line *with*
      the push rather than after it.

- [x] ~~**Open Phase 3 — Identity and player state.**~~ **Opened and its exit
      criterion met**, 2026-09-08, in two commits.
      **N21** gave `identity` and `player` their schemas and put an
      implementation behind both ports. The four decisions in `V5`'s header are
      the part worth keeping: no foreign key crosses a schema (a cross-schema key
      is a cross-module coupling the database enforces, and it is what makes
      extraction a rewrite); nothing points into `gamedata` either, and there it
      is not a choice, because gamedata rows are version-scoped and an inventory
      outlives every patch; an identity is `(provider, subject)` and never an
      email; and absent means zero, with a `CHECK` so the two representations
      cannot diverge.
      **N22** wired the rest: sign-in that creates the account while the
      principal is being built, `oauth2Login` installed only when a provider is
      configured, `IF_REQUIRED` sessions and CSRF back on — both phase 0 decisions
      that carried "phase 3 revisits this" — one authorization method every
      account-scoped route goes through, and a synchronous plan route reading
      goals, inventory and roster from Postgres.
      **What the tick does not cover, and it is a lot:** the OAuth token exchange
      has never run against a provider; the end-to-end test is MockMvc rather
      than a socket, for a stated reason; and **there is no sync** — Phase 3's
      scope names it, PUT replaces, and no per-key patch route exists. All three
      are in the tracker's unverified list.
      **`Optimizer` and `SolveCache` are beans now**, which is what N15's refusal
      to register them was waiting for: they finally sit on a path a real request
      takes. `SolveCoordinator` is still not one, on the same reasoning — there is
      no asynchronous surface for a ticket to be useful on.

### Done 2026-09-09 (thirteenth session) — PR #13 and N23

- [x] ~~**Merge PR #13.**~~ **Already merged** when the session opened — `main`
      is `2864c95`, run `34217574439` green. **Fourth session running.** The
      previous entry diagnosed this correctly and the fix it named did not take:
      the twelfth session did write its tracker line with its push, and the *PR
      merge* still happened on the remote afterwards. So the residue is not
      caused by running out of room; it is that merging a PR is a click that
      happens outside the session, and a next action of the form "merge PR #n"
      is stale the moment the maintainer opens GitHub. **Stop writing it as a
      next action.** Check the remote at session start and record what it says —
      which is what the *Status* line now asks for.

- [x] ~~**N23 — Offline sync: the per-key patch Phase 3 owes.**~~ **Done**,
      2026-09-09, in one commit. `PATCH` on inventory and roster, merged
      last-write-wins per key; `PUT` keeps its replace-everything contract.
      [ADR 0014](../adr/0014-sync-is-last-write-wins-per-key-against-a-clock-that-outlives-the-value.md)
      carries the argument. Three things worth keeping here:
      **the timestamp is a table and not a column**, because V5's fourth
      decision makes clearing an item delete its row and a clock on that row
      dies with it — after which a device offline since before the delete
      re-adds the item, finds nothing to lose against, and the item comes back
      with nobody told;
      **a full save needed a watermark of its own**, which a failing test found
      rather than the design — a `PUT` claims something about every slug in the
      game including the ones the player has none of, and no per-key table has a
      row to write that on, so without it a stale patch loses for a key the save
      mentioned and wins for one it did not;
      **and the timestamp is per key rather than per request**, because a device
      that spent an hour offline changed one item at 09:00 and another at 11:00
      and one batch stamp would have to lie about one of them.
      16 new tests, **244 total**, 0 failed and 0 skipped locally.

### Done 2026-09-09 (fourteenth session) — N26

- [x] ~~**N26 — Find out whether the game publishes its own drop rates.**~~
      **Answered 2026-09-09, and the answer is no** for the half that matters.
      Written up in
      [the drop disclosure note](../game-facts/reverse-1999-drop-disclosure.md).

      **Stage drop rates are not disclosed numerically.** The stage screen grades
      each reward `Fixed`, `Common` or `Possible`, and only `Fixed` carries a
      number — it means 100%. Every published percentage for a `Common` or
      `Possible` drop, including the twenty this project benchmarks against, is
      somebody's crowdsourced run count. So the cheapest way out of ADR 0015's
      bootstrap problem does not exist, and the interim has to be chosen
      deliberately: catalog-first launch, or thin honest samples.

      **Measured rather than estimated**, on the pinned 3.5 snapshot's
      `stages3_3_greedy.json` where `count: 1` marks a fixed-reward stage:
      **105 stages, 779 drop facts, 15 declared and 764 sampled.** ~2% of the
      drop axis is free. The reprieve is real and it is small.

      **Two things came back that were not asked for, and both are worth more
      than the answer.** *Gacha rates are disclosed* — Bluepoch states per-rarity
      summon rates and the pity counter in the client's own rules screen — which
      makes the live half of **Q4** one screen's worth of reading rather than a
      research question, and makes it `PUBLISHER_DISCLOSURE` rather than a
      sample. And *the `Fixed`/`Common`/`Possible` grade is itself a first-hand
      fact*, available for all 595 stage-item pairs before a single run is
      farmed, enough to order drops within a stage and to reject an estimate that
      contradicts it — and the model has nowhere to put it. **Deliberately not
      built this session**: it costs a bundle field, a parser, a writer, a
      migration and a JDBC round trip, and its only consumer is Phase 6. Same
      reasoning that deferred N20 and N18.

      **The sourcing is secondary and says so.** This is a question *about* a
      question, reached through a web search tool, and it selects which plan to
      make rather than entering any bundle. Under the plan it selects, every
      number is read first-hand anyway.

### Done 2026-09-09 (fifteenth session) — N24

- [x] ~~**N24 — Make a signed-in page developable.**~~ **Done 2026-09-09.**
      [ADR 0017](../adr/0017-the-development-sign-in-is-absent-from-the-artifact.md).
      A browser has signed in, read `/api/me`, created a profile and signed out.

      **The way out taken was the development sign-in, and the entry was right
      that it is the more dangerous one** — so the guard is not configuration.
      `:modules:identity-dev` is a Gradle module `:app` takes through
      `testAndDevelopmentOnly`: on `bootRun` and on the test classpath, excluded
      from `bootJar`. **The deployable artifact does not contain the classes**,
      so no profile, property or environment variable can reach them. The
      entry's "impossible to enable in production *by construction*, not by
      configuration" is met literally.

      **`DeployableJarTest` is the guard on the guard.** The whole mechanism is
      one word in a build file — `implementation` instead of
      `testAndDevelopmentOnly` breaks nothing and nothing else in the build would
      notice — so a test opens `storm-almanac.jar`, walks every nested dependency
      jar, and asserts no class under `io/stormalmanac/devsignin/` is inside it.
      It reads the archives rather than their names, so moving the classes into
      `:modules:identity` fails it too. And the same scan must find
      `SecurityConfig`: an absence test looking in the wrong place passes
      forever.

      **`SecurityConfig` does not know it exists.** The development chain is a
      second `SecurityFilterChain`, declared in the module production does not
      ship, `@Order(1)` with a `/dev/**` matcher ahead of the product's
      catch-all. No `permitAll` for `/dev/**` in the product's matcher, and
      nothing to remove when the module goes.

      **The second half — and the entry did not ask for it.** The other reason
      to prefer a development sign-in whose sessions are ordinary sessions is
      that it exercises the real thing, and on its first run it found a defect
      that had been in the product since Phase 3: **Spring Security 6 loads the
      CSRF token lazily, so no `XSRF-TOKEN` cookie was ever issued and the first
      write from any browser would have been refused.** Invisible for two phases
      because every test of a write used MockMvc's `csrf()` post-processor, which
      hands the request the token production had not handed out.
      `SecurityConfig.browserCsrf` opts out of the deferred load and both chains
      share it. **Confirmed the defect first, then fixed it**: the assertion
      failed, the fix made it pass, and a real browser then wrote through the API.

      **A gap in the tracker's own list closed with it.** "The end-to-end test
      goes through MockMvc, not a socket" had stood since Phase 3 because a
      session could not be minted over real HTTP without an authorization server.
      It can now: `DevSignInTest` runs against a real port with a cookie jar kept
      by hand and no Spring Security test post-processor in it — **the first
      authenticated request this project has made over a socket.**

      **What it does not prove, and the entry said this too.** No token has been
      exchanged with a provider. The exchange still lands with **B5**, and this
      is not an excuse to skip it — ADR 0017's reversal trigger is that
      module being deleted the moment a real provider works locally.

      **Two things found on the way, both recorded because they cost time.**
      `TestRestTemplate` on Boot 3.5 resolves to a `JdkClientHttpRequestFactory`
      that **follows redirects**, so the first run reported 401 for a sign-in
      that had worked perfectly and redirected to a page needing the session the
      client had not yet been handed — a test of a redirect has to be able to see
      the redirect. And a `@Bean` method named after its `@Configuration` class
      collides with it: `BeanDefinitionOverrideException`, not an obvious message.

### Done 2026-09-11 (sixteenth and seventeenth sessions) — N25

- [x] ~~**N25 — Phase 4's screens.**~~ **Done 2026-09-11.** The five screens
      landed on the sixteenth session — inventory editor, goal picker, plan view,
      catalog browse and search, and the character page with the personalized
      overlay, which is **Phase 4's second exit clause served** — along with the
      client half of N23's offline outbox. The box stayed unticked for three
      named debts, and the seventeenth session paid them:

      1. **Provenance is read back out.** ADR 0016 wrote it at ingest and enforced
         it at publish, and nobody the promise was made to could ask for it.
         `ProvenanceRepository` is deliberately a **second port** rather than a
         field on `GameDefinition`: `Provenance`'s own javadoc says a solver that
         can read where a number came from can eventually be made to prefer one,
         so serving it to a reader must not put it on the path `planner` loads.
         Four responses carry a `sourcing` block — the records once, the fact
         references beside them, because a page's hundred facts point at a
         handful of readings. `firstHand` travels as an answer rather than being
         re-derived in TypeScript, which would be a second copy of
         `Provenance.Origin.isFirstHand()` in a language that cannot be made to
         fail to compile when the Java one gains a member.
         **Three new HTTP tests**, and the interesting one is the refusal again:
         a fact whose row is deleted — which is what a version published before
         ADR 0016 looks like — is *absent* from the sourcing rather than given a
         fabricated record, and the page still serves its numbers.
      2. **The PWA has been loaded offline**, and the method is the evidence: the
         built bundle served on 4173, the service worker allowed to precache, and
         then **the origin server killed** — `curl` refusing the connection — and
         a *deep* route reloaded. The shell rendered through the navigation
         fallback and the catalog list rendered out of the `game-data` runtime
         cache. A CDP offline flag would have proved less.
      3. **The frontend has tests, and a decision about them.** Vitest and
         Testing Library in jsdom, 15 tests, wired into CI. The argument is
         written beside the config in `vite.config.ts` and rests on the four
         defects a browser found on the sixteenth session: three were behaviour
         and are catchable — the re-rendering store selector is now pinned by a
         test **verified to fail**, with React's own "Maximum update depth
         exceeded", when the defect is reintroduced — and the fourth,
         `display: block` folding a table header into a column, is layout, which
         **jsdom will never catch because it computes no layout**. So behaviour
         is the pipeline's and appearance is a person's, stated rather than left
         to the next session to infer.

      **The exit criterion was never this action's to meet:** five strangers
      completing a plan needs a deployment, which is **B5**.

---

### Done 2026-09-12 (nineteenth session) — Phase 5 and N29

- [x] ~~**N29 — Phase 5's two engines.**~~ **Done 2026-09-12.** Opened and closed
      in the same session, because the work was one criterion with two halves and
      neither half is useful alone. Entered out of order at the maintainer's
      direction — see **D2**.

      **Both halves of the exit criterion, measured.** `MarkovBannerEngine` walks
      the distribution forward over `(pulls since hit, losses carried, copies
      held)` and reads the answer off the mass that never arrived;
      `MonteCarloBannerEngine` runs 500 000 seeded trials across virtual threads.
      Over **84 questions on all seven published banners, every gap is inside
      0.110 percentage points against a criterion of 0.300, worst case 1.84
      standard errors.** Both games' published rates come back out of
      `BannerModel` alone: the 70-pull wall, the curve biting at pull 61 rather
      than 60, the 42.39-pull average, the 120-pull worst case, and 30 / 40 / 80
      for the weapon, beginner and floating-guarantee variants.

      **The plan's 100 000 trials and its 0.3% tolerance do not fit together**, and
      one run of the cross-check was enough to show it — the debut banner over
      thirty pulls, exact 0.139616 against simulated 0.136550, 0.31 points at 2.80
      standard errors, both engines correct. The trial count now follows from the
      tolerance rather than from a round number. The criterion is unchanged.

      Four design decisions, all in
      [ADR 0018](../adr/0018-the-gacha-engines-answer-one-question-about-one-rarity.md):
      `PullResult` carries no `Rarity` because the model does not pin one while
      pity is active; a floor below the headline rarity is ignored and one that
      reaches it is refused by name; `PityState` carries a loss count rather than
      a flag, with the three transitions on the record so both engines share them;
      and the exact chain reads the complement so that a wall is exactly 1.0.

      **Deliberately not done:** no bean, no route, no screen — the reasoning that
      kept `SolveCoordinator` unwired through Phase 2. And `IncomeModel` stays an
      interface, because `BannerModel` and the `banner` table declare neither a
      pull currency nor a price, so the question has nothing to compute from.
      That is **N28**.

---

### Done 2026-09-13 (twentieth session) — N5

- [x] ~~**N5 — Upgrade the CI actions before they break.**~~ **Done 2026-09-13**,
      [PR #20](https://github.com/kietnt4412/storm_almanac/pull/20), green on
      `ef51844`, run `34731880389`, **with no annotations** — the Node 20 and
      `setup-java@v4` deprecation warnings are gone.

      The action as written asked for `setup-java@v5`. By the time it was done
      every action had moved two or three majors: checkout **v7**, setup-java
      **v6**, setup-node **v7**, upload-artifact **v7** — all taken, after reading
      the breaking notes of every major in between, none of which touches this
      pipeline. Both workflows, including the dormant `nightly-chaos.yml`.

      **`gradle/actions` stopped at v5, not v6, and that is a decision left open
      rather than taken.** v6 extracted setup-gradle's caching into a proprietary
      component, and upgrading accepts Gradle's Terms of Use. That is a licence
      question for the maintainer, not a version bump. v5 runs on Node 24, which
      is all N5 needed — **but v5's last release was 2026-02-23, so it is frozen
      rather than maintained.** A comment in `ci.yml` says why, so it is not bumped
      by reflex.

      **The green run executed no tests, and says so here so nobody reads it as
      more.** A YAML-only change leaves every Gradle input identical to PR #19's,
      so all seven test tasks came back `FROM-CACHE` (50 tasks, 22 executed, 28
      from cache). What the run proves is the *mechanics* under the new actions:
      checkout, JDK install, wrapper validation, cache restore, artifact upload,
      Node. The first PR that changes source is the first run of the suite under
      them.

---

### Rows compressed out of *Current state*, 2026-09-12 (nineteenth session)

The live tracker's table had three cells that were each a session-log entry. The
rows are one line each now; what they said is here. All of it is still true.

**Game data pipeline (Phase 1)** — *was:* "Model, schema, ingest, diff and CLI,
in full in the archive. `V2`–`V4`, 28 tables, seven invariants proven on two real
R1999 patches; parser and writer pinned by a round trip (ADR 0008); publishing is
a human approval, not a flag. `Drop` carries `sampledRuns`, where 0 means
*declared*."

**Parser adapters** — *was:* "One — and demoted to a cross-check by ADR 0015, now
in code. `:adapters:reverse-1999`, 25 tests. **Its output is no longer what the
product will ship**, and since ADR 0016 it hard-codes `THIRD_PARTY` provenance
and cannot be told otherwise, so it fails a plain `publish` — there is no call
site to launder data through. Kept, not deleted: diffing the first self-sourced
bundle against an independent reading of the same patch is worth more as a check
than it ever was as a source."

**Provenance** — *was:* "Done — written, enforced, and now read. ADR 0016. A
bundle declares `Provenance` records, defaults every fact to one (`sourcedBy`)
and overrides per `FactRef` (`kind:slug`); `V7` materialises **one row per
declared fact**, because a default is an authoring convenience and a database
that stored it could not answer the question alone. **`publish` refuses a version
that is not first-hand and names the facts**; `publish(…, true)` and the CLI's
`second-hand` word are the explicit exception. The first-hand policy lives in
`Provenance.Origin` and nowhere else — the migration constrains the set and says
nothing about which count. **Silence parses and cannot publish** (`UNRECORDED`).
**Served since 2026-09-11**: `ProvenanceRepository` is a *second* port, not a
field on `GameDefinition`, so the solver still cannot see where a number came
from and be made to prefer one. Four responses carry `sourcing`, and `firstHand`
travels as an answer rather than being re-derived in TypeScript."

The two facts from these three cells that a session still acts on are kept in the
live tracker: the adapter cannot publish, and provenance is a second port the
solver cannot reach.

### Done 2026-09-12 (eighteenth session) — N4

- [x] ~~**N4 — Enforce that `Entity.kind` is never read outside the catalog.**~~
      **Done 2026-09-12.** `EntityKindBoundaryTest`, an ArchUnit rule beside
      `ModuleBoundaryTest`, which is where ADR 0007 itself said the rule belonged
      when it recorded the gap against its own decision.

      **The deferral had expired rather than been forgotten.** ADR 0007 gave the
      reason for waiting — the guarded modules were empty, so the rule would pass
      vacuously — and that reason stopped being true somewhere around Phase 2.
      `planner` is ~2 000 lines across a resolver, a MIP and an optimizer, all of
      which take a `GameDefinition` and could reach an `Entity` in one hop.

      Three decisions inside a twenty-line test:

      1. **Bytecode, not an extension of `GameAgnosticismTest`'s source scan.**
         A grep for `kind()` cannot tell `entity.kind()` from `change.kind()` —
         `gamedata.diff` has one and `GameDataReadModel` reads both within a
         hundred lines of each other — so a scan either misses the read or fails
         on a sibling. ArchUnit resolves the declaring type, which is the
         question.
      2. **An allowlist, not a ban on the three modules ADR 0007 names.** The
         ADR's normative sentence names `planner`, `gacha` and `stats`, but its
         decision says the field exists "for the catalog surface and for ingest
         validation, nowhere else" — so the rule permits `gamedata`, `api` and
         `adapters` and denies everything else. A denylist of three would pass
         vacuously again the day `player` or the CLI grows a read. The allowlist
         is by module because the module is the boundary enforced everywhere else
         here, and because "the catalog surface" is not a line the package
         structure draws finely enough to guess at.
      3. **Verified by putting the defect back**, in both spellings a reader
         would write: `e.kind()` and `.map(Entity::kind)`. The rule failed on
         each and named the file and the line. **The method reference did not
         need a condition of its own** — it was written, and then deleted when
         the measurement said `accessTargetWhere` already catches it, because
         ArchUnit 1.3's `getAccessesFromSelf` carries references as well as
         calls. The test's javadoc says so, so the next reader does not add back
         the condition this one removed.

      **ADR 0007 was not edited.** It is accepted, and the convention is that an
      accepted ADR is superseded rather than corrected. Its "Open gap" paragraph
      is now historical: the gap is closed, the decision it recorded is unchanged,
      and a reader who wants to know when belongs here rather than in the ADR.

---

### Done 2026-09-19 (twenty-fourth session) — N27

- [x] ~~**N27 — Finish the first PGR bundle. What it could not hold is the next
      reading order.**~~ Done 2026-09-19. The entry as it stood, verbatim:
      Draft 0 is ingested and awaits the approval, which is one
      command and is the maintainer's: `--gamedata=publish punishing-gray-raven 0`.
      **Still to read:** the **weapon's name**, all that stands between the note's
      Overclock recipe (16/16/20/28) and Harmony cost (25 Accelerators) and a
      bundle row — *the cheapest gap in the project*; the **fodder item's name**,
      without which the authored fodder rule is inert; the **Memory** system
      entirely; and the costs behind the character's four axes. **Block 4, the
      timed authoring pass, was cut**, so the cost of sourcing a patch stays
      unmeasured and any later claim that one is affordable is an estimate with
      nothing behind it. **Ask how each value was read before recording it** — a
      relayed web-search answer and a screen reading look identical in chat.

      **Exit met:** every item on that list was read and is a bundle row with a
      named provenance — Hear the Bell (Overclock with its 340 000 Cogs, Harmony
      Lv 1), Weapon Enhancer IV and Memory Enhancer IV (300 EXP each), Samantha's
      Overclock, and Helentine: Lacrimosa's Level (three Pod fodder rules),
      Promote (13 steps, 542 500 Cogs), seven skills to 18, the leader and
      Vestige unlocks, and Evolve to SS (30 Inver-Shards). Plus what the list
      did not foresee: **Simulated Battlefield and nine Simulation Shop rows**,
      which are how PGR is farmed. **Not met by this session, by design:** the
      publish, which is the maintainer's, and which needs a re-ingest first
      because the file changed after draft 0 was ingested. Awaken and the
      Phantom Pain shop were read and **refused by the format** — now **N32**.

- [x] ~~**N32 (1)–(5) — Give the format the shapes the first full bundle
      refused.**~~ Done across four sessions, 2026-09-19 to 2026-09-20. Each one
      was read off Punishing: Gray Raven, refused by the model, and then written:

      1. **A gate is a state, and a level is a cost in EXP.** ADR 0019, `V8`.
         Her last Promote step went from 180 Serum to **1 470** — the gate was
         88% of the goal and the old plan charged none of it.
      2. **Fodder pays progress.** Same ADR and migration: a `Fodder` rule names
         the `progress:<kind>` it feeds, so Pods have somewhere to go. Samantha's
         Overclock went 240 → **420 Serum**.
      3. **A limit that never resets is offered whole.** ADR 0020, a shop period
         of `"never"`; the plan says it assumed none of the allowance spent,
         because nothing a reader records says otherwise. Evolve to SS became
         plannable for a reader holding Scars.
      4. **One step at several prices is a choice the solver makes.** ADR 0021,
         `V9` dropping `upgrade_edge_unique`. Several upgrades making the same
         move are one step, demanded as a `choice:` item. Samantha's Resonance
         is **90 Serum** through the one price that can be farmed.
      5. **A grant sized by the player is an answer the reader supplies.** ADR
         0022, `V10`. A `Reward` may stand behind a score on an opaque measure,
         and `SolveRequest.reach` is what the reader says they reach. The weekly
         Phantom Pain Cage is nine tiers, and Evolve to SS is **63 days and no
         Serum** for a reader who clears it.

      **Exit met:** every one of the five is in the bundle as a row, published
      (sequences 1–4) and read back as *no changes*, with a plan whose arithmetic
      was worked out by hand before it was run. **N32 is closed on that
      criterion**, and the one part of it that was never code — the **EXP at the
      other twelve Promote gates** — leaves as **N33**, because a reading is not
      a shape the format refused and keeping it under this number would have
      made a finished item look unfinished for as long as it took somebody to
      open a screen.

- [x] ~~**N31 — Give `PityRule` a guarantee that is drawn, not fixed.**~~ Done
      2026-09-20, thirtieth session.
      [ADR 0023](../adr/0023-a-drawn-guarantee-is-a-rate-curve-not-a-state-dimension.md),
      `V11`, one nullable column, **393 tests green and 0 skipped locally**.

      **The cost the item warned about was not real.** Carrying PGR's drawn
      80–100 threshold exactly looked like a fourth dimension on the exact chain
      or twenty-one mixed chains. It is neither: conditioned on `c` misses, the
      posterior over the threshold is uniform on
      `{max(drawnFrom, c+1) .. hardAt}` — the curve rolls are independent of the
      draw, so `c` misses rule out every threshold at or below `c` and nothing
      else. It depends on the pity counter alone, so **the marginal is a rising
      hazard curve**, the shape soft pity already had, and `MarkovBannerEngine`
      was not touched. `hardAt` keeps its meaning — the pull at which the rarity
      is certain — so every rule written before this reads back unchanged.

      **The simulation draws anyway, and that is the point.** A Monte Carlo
      engine reusing the integrated curve would have agreed with the chain about
      the marginalisation by construction. It draws a threshold per pity cycle
      and redraws on every headline hit, won or lost.

      **It found a real bug on its first run, which is the first time the two
      engines have disagreed about anything but rounding since Phase 5 closed.**
      A player carrying 85 misses against a threshold drawn from 80–100 *cannot
      have drawn 80*; the simulation was sampling the prior and handing that
      player a forced hit about a quarter of the time. Exact `0.381856` against
      simulated `0.557824` — 17.6 points, and **the chain was right**. `drawWall`
      now draws from `{max(drawnFrom, pullsSinceHit + 1) .. hardAt}`.

      **The agreement test had to be told where to look.** Its generic questions
      are 1, 10, half the wall, the wall, the worst case — for a wall of 100 that
      is 1, 10, **50**, 99, 100, every one below the drawn range or at
      certainty's doorstep. The one band where the roads can part was never
      sampled. Questions across the band are now generated for any drawn banner:
      96 → **108 questions**, worst gap unchanged at **0.110 points at 2.22
      standard errors**.

      **The fixture correction, which the item asked for, was wrong in both
      halves.** `Banners.grayRavenFloating()` paired 1.50% with a 70% featured
      rate and a fixed wall at 80; the client pairs 1.50% with **100%** and the
      drawn range, on one screen. So the worst case is one wall and not two, and
      ten pulls is ten base rolls. Renamed to the Themed Construct pool.

      **And it reproduces the note's arithmetic from the other end.** The
      research disclosure computed the Themed pool's long-run share by hand as
      **2.021%** against the advertised 1.90%; the chain says 49.488 pulls, which
      is **2.0207%**. The disagreement with the publisher is now the model's and
      not a spreadsheet's — which is what **Q4** needs before anything is
      concluded from it.

      **Proved against Postgres, not only the parser.** The proving-ground
      fixture gained a second banner with a drawn guarantee, so the column
      crosses the writer, parser, migration and JDBC round trip. `Facts` carries
      it too, or a patch turning a fixed wall into a drawn one would report
      `hard at 80 -> 100` and read as a nerf. One brittle assertion moved with
      it: the second-hand refusal now names 21 facts, not 20.

      **What it does not do is put a PGR banner in the bundle.** The archetype is
      expressible; a banner still has no pull currency and no price, which is
      **N28**.
## Closed phases, in full

The live tracker keeps each phase's exit criterion, its status and the
qualifications that still travel with it. This is the full "Landed" prose as it
was written when each closed.

- [ ] **Phase 0 · Ground** — 1 week — **closed by exception 2026-09-05, box
      deliberately left unticked**
      Repo, CI, Docker Compose, ADR folder, hello-world deployed to production
      before any real code. Read Kornblume and Penguin Statistics. Write the
      positioning paragraph.
      **Exit:** a green pipeline deploying a health endpoint to a real URL.
      **⚠ Not met as written — see deviation D1.** Closing on "green pipeline,
      deploy deferred" is an exception, not the criterion, and the box stays
      unticked to say so. A ticked box here would be the tracker telling the
      next session a lie about what this project has actually shipped.
      **Landed:** repo, git, ADRs 1–7, compose file, domain model, a green local
      backend build, a verified frontend build, the prior-art read, the
      positioning paragraph, **CI green on the current tree** (`a82da1e`, runs
      `33964292888` and `33964297527`, with `ApplicationBootTest` executing on
      the runner), and **an application that actually boots and serves
      `/api/health`**.
      **Still missing, and only this:** a real deploy. Deferred by D1, not
      coming before Phase 4. Every other item in this phase that could be met,
      has been.
      **The box gets ticked when, and only when, a real URL answers 200.**

- [x] **Phase 1 · Game data foundation** — 2.5 weeks — **closed 2026-09-06**,
      opened 2026-09-05. Two days, not 2.5 weeks, and that is worth being
      suspicious of rather than pleased about — the estimate assumed hunting for
      data and negotiating for it, and what actually happened is that the
      licensing question was deferred rather than answered (ADR 0009). The
      engineering was the small half.
      Canonical schema, R1999 ingestion (items, stages, characters, upgrade
      costs), versioned publishing with diffs, `gamedata-cli`. Tests over real
      patch data, including a patch that changes something. Includes the catalog
      axis — stat curves, skills, talents — modelled and ingested *now*, because
      retrofitting a second data axis into a published schema later is miserable.
      **Exit:** the API answers "what does Insight 2 cost?" and "what does her S2
      do at rank 3?"; a patch diff report renders for both axes.
      **Landed:** the canonical schema (`V2`, 28 tables, plus `V3` making a
      version deletable), the catalog axis, `GameDataSchemaTest`, the canonical
      bundle format and its parser, JDBC ingest and read (ADR 0008), versioned
      publishing with a draft/approve split, the three-axis patch diff,
      `gamedata-cli`, the API that serves all of it, and — 2026-09-06, sixth
      session — **the R1999 ingestion the phase is actually named after**:
      `:adapters:reverse-1999`, `CanonicalBundleWriter`, `--gamedata=adapt`, and
      two real patches through the whole pipeline.
      **The schema holds data and returns it unchanged**
      (`GameDataIngestTest.roundTripsWithoutLoss`, record equality across the
      whole graph, and `RealUpstreamPatchTest` over real data), **the API answers
      both exit questions** (`GameDataApiTest`, over real HTTP), and **the data
      is now somebody else's**.
      **Both halves of the criterion are met:**
      - *the API answers "what does Insight 2 cost?" and "what does her S2 do at
        rank 3?"; a patch diff report renders for both axes* — **met, 2026-09-06.
        N7.** `GET /api/games/{game}/entities/{entity}/upgrades`, `GET
        .../entities/{entity}`, `GET .../diff?from=&to=` in JSON or plain text.
      - *tests over real patch data* — **met, 2026-09-06. N8.** Kornblume
        snapshots at 3.3 and 3.5, taken from the upstream's own history, through
        adapt, ingest, publish and diff. The patch changes something on both
        axes: four characters released, one material and four recipes added,
        euphoria and mastery tracks appearing on existing characters.
      **CI confirmed the tree on 2026-09-06 (N10)** — runs `34035918992` and
      `34035923889`, both success, with all 18 adapter tests executing on the
      runner and `RealUpstreamPatchTest` correctly skipping. **The box is ticked
      on that.**
      **Two qualifications the tick does not erase, and they travel with the
      phase rather than closing with it:**
      1. **CI does not run the real-data tests** — the snapshots are not
         redistributable, ADR 0009. The criterion is met locally and
         reproducibly, not on the runner. Unlike Phase 0's D1, this is *not* an
         exception to the criterion: the criterion says "tests over real patch
         data", not "tests CI runs". But it does mean a regression in the
         adapter can reach `main` green, and that is a standing risk, not a
         solved problem.
      2. **The catalog half is proven on real data only for stat curves.**
         Kornblume publishes no skill or talent text, so *"what does her S2 do at
         rank 3?"* is answered from the synthetic fixture. The schema, the API
         and the diff all handle it; this upstream has nothing to put in it.

- [x] **Phase 2 · Optimizer core** — 2 weeks — **closed 2026-09-08**, opened
      2026-09-07
      ojAlgo MIP model, crafting recursion, integer runs, solve caching,
      explanation output, both objectives.
      **Exit:** agrees with community-accepted answers on 5 benchmark goal sets;
      p95 solve under 2s.
      **Landed:** `DemandResolver`, `EnergyMip`, `YieldTable`, `MipOptimizer`,
      `SolveKey`, and [ADR 0010](docs/adr/0010-a-plan-is-the-best-provable-in-the-budget.md).
      Crafting recursion, integer runs, explanation output and both objectives
      are done; solve caching has its key and no cache.
      **Both halves of the exit criterion are met and measured, and CI has
      confirmed the tree (N16, 2026-09-08): runs `34116854550` (`93f4de4`) and
      `34116880372` (`d34a31b`, PR #8), both success, 139 passed / 15 skipped /
      0 failed, the adapter's 24 tests executing on the runner. The box is
      ticked on that.**
      **Two things the tick does not close, and they travel with the phase:**
      *the phase's own scope is not exhausted* — solve caching has a key and no
      cache (**N15**), there is no time axis (**N14**), and drop rates carry no
      sample size (**N17**) — and *the criterion was measured by tests CI does
      not run*, exactly as in Phase 1. Both halves of the criterion rest on
      `RealUpstreamPlanTest` and `CommunityBenchmarkTest`, which skipped on both
      of the runs above. Reproducing it is `backend/tools/fetch-upstream.sh` and
      one flag; it is not something a green pipeline will ever tell you.
      - *p95 solve under 2s* — **met, 2026-09-07.** 1.8 s over 50 solves of a
        five-character Reverse: 1999 goal set. Measured on real data, locally
        only, by a test CI does not run. **Read it as the budget rather than as a
        measurement of difficulty:** median, p95 and max all land within 5 ms of
        each other, because the search is stopped by its budget every time. The
        number that means something is the 2.80% optimality gap beside it.
      - *agrees with community-accepted answers on 5 benchmark goal sets* —
        **met, 2026-09-07 (eighth session).** Five materials where the cheapest
        stage this project computes is the stage a published guide names, out of
        twenty claims compared, with every disagreement traced to a cause. **N12**,
        and [the benchmark](docs/benchmarks/reverse-1999-community-answers.md).
      **Four things the tick, when it comes, must not be read as covering:**
      1. **The model has no time axis**, so no shops, no rewards, no weekday
         rotation, and `FEWEST_DAYS` is `LEAST_ENERGY` with a division. Items
         whose only source is a shop or a reward are refused by name rather than
         costed. **N14.**
      2. **The search is stopped by its budget, not finished by it.** The plan
         says so and says the size of the doubt (2.95% on the measured goal set).
         ADR 0010.
      3. ~~**Drop rates carry no sample size, and it is currently choosing
         stages.**~~ **Fixed the same day, 2026-09-08, N17 and ADR 0011.** Yields
         carry the runs behind them and the solver gets a 95% lower bound rather
         than the observed mean. What replaces this caveat is smaller and still
         real: **the discount is only as good as its Poisson assumption**, which
         is conservative for a single-drop stage and optimistic for a multi-drop
         one, and **it cannot rescue a 105-run sample** — two of the four large
         disagreements with the community survived it, both on thin evidence.
      4. **Fodder is in the domain model and not in the solver.** Advancing an
         item by consuming other items of a class is a sink the demand vector
         does not express yet, and it is Punishing: Gray Raven's whole
         progression system — so this is Phase 11 work arriving early or a real
         surprise late.

---

- [x] **Phase 3 · Identity and player state** — 1 week — **closed 2026-09-08**
      OAuth, inventory, roster, goals, multiple profiles, sync.
      **Exit:** a plan computed end-to-end from stored state on a real account.
      **Met.** `PlanFromStoredStateTest` signs a person in, the sign-in creates
      the account, the account creates a profile, the profile is given an
      inventory, a roster and goals over HTTP, and `POST
      /api/me/profiles/{id}/plan` reads all three back out of Postgres. The
      request body carries `energyPerDay` and `horizonDays` and nothing else,
      because those are the two things that are facts about the sitting rather
      than about the account. **Every plan in this repository before that one was
      computed from a goal set a test handed the optimizer.**
      **Landed:** `V5` (six tables across two schemas), `JdbcAccountRepository`,
      `JdbcPlayerStateRepository`, `SignIn` and the two user services,
      `CurrentAccount`, `OwnedProfiles`, `PlayerController`, `PlanController`,
      `PlannerConfiguration`, `oauth2Login` behind a configured-provider check,
      `IF_REQUIRED` sessions and CSRF re-enabled — both phase 0 decisions that
      carried "phase 3 revisits this" — and 28 tests.
      **What the tick does not cover.** **Sync was not built, and it is in the
      scope line above**: PUT replaces a whole aggregate, there is no per-key
      patch route and no merge, and the `updated_at` column last-write-wins would
      need was deliberately left out of `V5` so that it arrives with the merge
      rather than looking implemented without it. Tracked as **N23**.
      Two further qualifications, both in the live tracker's unverified list:
      the **OAuth token exchange has never run** against a real provider, because
      no client secret exists and no redirect URI can be registered without a URL
      (D1); and the end-to-end test goes through **MockMvc rather than a socket**,
      because an authenticated session cannot be minted over one without an
      authorization server to redirect to.

- [x] **Phase 5 · Gacha engine** — 1.5 weeks — **closed 2026-09-12 on its
      criterion; scope incomplete, and entered out of order (D2)**
      Generic `BannerModel`, Markov and Monte Carlo engines, income model, the
      "can I guarantee her" answer.
      **Exit:** both engines agree within 0.3% and reproduce published R1999 and
      PGR rates.
      **Landed:** `MarkovBannerEngine`, an exact forward chain over `(pulls since
      hit, losses carried, copies held)`; `MonteCarloBannerEngine`, 500 000 seeded
      trials across virtual threads, deterministic under parallelism because the
      generators are split sequentially before any task starts and the counts are
      summed as integers in chunk order; `PullModel`, the validated slice of a
      banner both engines share, so they refuse the same banners for the same
      reasons. 46 tests, 324 in the suite, 0 failed and 0 skipped locally.
      **Both halves of the criterion, measured:** over **84 questions on all seven
      published banners, every gap is inside 0.110 percentage points against 0.300,
      worst case 1.84 standard errors**; and both games' published rates come back
      out of `BannerModel` alone — the 70-pull wall, the curve biting at pull 61
      rather than 60, the 42.39-pull average, the 120-pull worst case, 30 / 40 / 80
      for the weapon, beginner and floating variants, and 0.7030 at the 30-pull
      weapon wall because a wall guarantees the rarity and not the unit.
      Four decisions in
      [ADR 0018](../adr/0018-the-gacha-engines-answer-one-question-about-one-rarity.md),
      and one finding about the plan: **its 100 000 trials and its 0.3% tolerance
      do not fit together**, so the trial count now follows from the tolerance.
      **Two pieces of the scope were not built, both with reasons rather than
      excuses.** The **income model** is still an interface: `projectedPulls` needs
      a pull currency and a price, and neither `BannerModel` nor the `banner` table
      declares either — **N28**. And **nothing calls either engine** — no bean, no
      route, no screen — on the reasoning that kept `SolveCoordinator` unwired
      through Phase 2.
      **The qualification that travels with this phase:** every rate it reproduces
      is **second-hand**, from the sources the plan cites rather than either
      publisher's disclosure (**Q4**). What the acceptance fixtures prove is that
      the model reproduces the figures it was given.

### Qualifications moved out of the live tracker, 2026-09-11 (seventeenth session)

Verbatim, and still true. They left *What is still unverified* because they
qualify phases that are closed and because none of them bears on a next action —
which is the tracker's own criterion for moving something: being finished with
it, not its age. The tracker had reached 759 lines against a 550-line budget and
the sixteenth session's own note named these as the candidates.

Read them when you re-open Phase 1, Phase 2 or Phase 11. **The one with live
teeth is the time axis**, and its warning is restated in one line on the live
Phase 11 row rather than being left only here.

- **The benchmark is one guide.** Written for patch 2.7 against a 3.3 sample, and
  it answers "which stage for this material" rather than "what should I do this
  week". A second independent source would turn "agrees with the community" from
  a claim into a measurement. The agreement is also on the stage *ranking*, which
  is arithmetic on the data — **the search itself is still checked only against
  itself.**
- **Two large disagreements with the community survive the sample-size fix**, on
  105 and 113 runs. A 95% bound discounts a thin sample in proportion; it does
  not rescue you from one. Those belong to the evidence, not the model, and the
  fix is more sampling.
- **The time axis has never met real data, and on this game it never will.**
  Rewards, rotation and shops are modelled or refused on the synthetic fixture
  alone, and on the real 3.5 patch the whole of N14 reduces to one energy row.
  **Do not read "the optimizer has a calendar" as "the optimizer schedules real
  weeks."** This is not the upstream being silent: **R1999 has no weekday
  rotation at all**, so `Availability.ALWAYS` is *correct*, and its daily income
  **is conditional on spending Activity**, so entering it as a `Reward` would make
  every plan systematically too cheap. The rotation machinery waits for a game
  that rotates — Phase 11 is the next chance. See
  [the economy facts](../game-facts/reverse-1999-economy.md).
- **Three shapes the model cannot express**, each a *silent* wrong answer if
  faked, none a defect in what shipped: **an item that restores energy**
  (`Stage` is the only source touching the budget and only ever consumes),
  **a reward conditional on spending energy**, and **a lifetime purchase limit**
  (`Shop` caps at "n per `Period`", not "five, ever"). Examples and provenance in
  [the economy facts](../game-facts/reverse-1999-economy.md).

  **Correction, 2026-09-21: the third of those is closed and this entry was
  stale.** [ADR 0020](../adr/0020-a-limit-that-never-resets-is-offered-whole.md)
  made a lifetime limit expressible, and PGR sequence 2 published two shop rows
  that use it — 10 Inver-Shards at 10 Scars then 20 at 20, **30 ever**. The
  first two shapes are still unexpressible and still have no ADR. Left in place
  rather than edited away, because an archived qualification that quietly
  changes is worse than one that says when it stopped being true; the rule that
  nothing here is deleted cuts both ways.
- **The adapter converts less than the upstream publishes** — no shop offers, no
  alternative resonance-pattern costs, no unreleased content — each with a reason
  in `KornblumeAdapter`'s javadoc. That sentence is only reassuring when somebody
  has checked what it converts against what the upstream actually *reads*; the
  last time nobody had, two thirds of the game was missing.
- **No skills or talents have ever been ingested from a real upstream.** The
  catalog axis is proven end to end on the synthetic fixture only, because
  Kornblume publishes no skill text. An upstream gap, not a defect — but do not
  let the phase board imply otherwise.
- **The pipeline has met one upstream, not two.** The second game is Phase 11 and
  is where the abstraction is actually tested.
- **`gacha` has no behaviour.** Every port in it is an interface with nothing
  behind it. (Phase 5's own row says the same thing.)

### Closed out of *what is still unverified*, 2026-09-12 (eighteenth session)

Both had been struck through in place on the seventeenth session, which the
tracker's own rule forbids: an item that closes moves here rather than staying as
a corpse with a line through it. Verbatim, minus the strikethrough.

- **The PWA is installable and has never been loaded offline.** **Loaded offline
  2026-09-11**, and the way it was tested is the reason to believe it: the built
  bundle was served, the service worker allowed to precache, and then **the
  origin server was killed** — `curl` refused — and a *deep* route reloaded. The
  shell rendered, the navigation fallback resolved `/catalog/proving-ground` to
  the precached `index.html`, and the catalog list rendered from the `game-data`
  runtime cache. Not a CDP offline flag: the server was actually gone. **It needs
  the built bundle** (`preview_start` on the `web-built` config, port 4173),
  because the dev server has no worker worth the name. **What is still unproven
  is an offline *write path* end to end** — the outbox was proven against a
  refusing network, not against a dead origin on a cold start. *That half stays
  in the live tracker.*
- **Provenance is written and never read.** **Read back 2026-09-11.** Four
  catalog responses carry a `sourcing` block and both catalog pages render it.
  **What it says today is the uncomfortable part and it is supposed to be:** on
  the fixture it reads *"Invented for this project — not any real game"*, and on
  a version published before ADR 0016 it reads *"Nobody recorded where these
  numbers were read"*. That is the feature working. It becomes a claim worth
  making only when **N27** puts a real reading behind it.

### Lessons moved out of the live tracker, 2026-09-12 (eighteenth session)

Verbatim, both of them *Status* bullets, and both still true. They left because
they are lessons rather than live state, which is what the seventeenth session's
own size note named as the next candidates. The tracker stood at 747 lines
against a 550-line budget.

#### CSRF is `SecurityConfig.browserCsrf`, and it is not to be changed back

> **CSRF is `SecurityConfig.browserCsrf`, resolved eagerly and shared by both
> filter chains — do not change it back.** Why, and the two phases of green
> builds it hid behind, are in
> [the fifteenth session](#2026-09-09-fifteenth-session--a-page-that-knows-who-is-reading-it-and-the-write-that-would-have-been-refused).

The imperative survives the move and is the reason this heading exists rather
than a plain deletion: the deferred `CsrfTokenRequestAttributeHandler` is
Spring's default, so "restore the default" looks like tidying and is the defect.
The account of how it was found is in that session's entry.

#### A published version that stopped being readable

> **A published version in the local database cannot be read back, and the shape
> of that is worth more than the row.** Every catalog route on the locally
> published Reverse: 1999 3.5 answers **400**: a `craft` row has zero
> `craft_input` rows and `Craft`'s constructor refuses to build one. The row
> predates the invariant, so it is a write made before a rule that came later
> rather than a defect in today's code — but **a version is immutable and the
> rules for reading one are not**, so a published snapshot can stop being
> loadable without anything having touched it. Not repaired and no action opened:
> [ADR 0015](../adr/0015-game-data-is-sourced-first-hand-not-adapted.md) says
> that data will not ship.

The live tracker keeps the one sentence with teeth — **read this before Phase 6
tightens a rule over anything already published** — on the Phase 6 row, which is
where somebody about to do it will be looking.

## Answered questions

Struck through with the answer, as the tracker's rule requires. Kept in full
because how an answer was reached is what tells the next reader whether it still applies.

### Q6 — what makes the site finished enough to let strangers in

*Answered 2026-09-25, moved here 2026-09-26 (forty-fourth session) to make room for Track C.*
All four items were met and D4's trigger fired; the maintainer's answer to that was D5, a
rehearsal before strangers, which the tracker still carries. The entry as it stood:

- ~~**Q6 — What makes the site finished enough to let strangers in?**~~ *Open, and the maintainer's
  (D4).* **Four items, all agreed:** (1) three constructs with whole ladders — **met 2026-09-24**
  by Lacrimosa, Selena: Pianissimo and Lucia: Inverse Crown (N41; Lucia replaced Karenina, whom the
  maintainer does not own). **Added 2026-09-24 (forty-first):** (2) `choice:` lines
  render raw upgrade ids — **met the same day by naming a choice by its prices** (`DemandNames`:
  "one of: 150 5★ Memory Shard · 234 Special Support Token · 246 Simulation Score"), which
  are facts already, so no upgrade needed a name of its own; browser-checked at 1280 and 375 px;
  (3) no shadow price has ever rendered against a real plan — **met the same day by driving one**:
  Lacrimosa's skill to 18 plus Samantha's Resonance renders 90.00 for the Resonance line; the bundle's
  one stage was enough and no reading was needed; (4) sign-in lands on `/` rather than where
  the reader was — **done in code the same day** (`ReturnAfterSignIn`, `?then=` held in the
  session across Google), **run against Google on 2026-09-25** once `63a45b3` deployed, by the
  maintainer's report. **All four met, so D4's trigger fired** — the maintainer's answer is D5.

### Q2 and Q3 — where the data comes from, and whether it may be redistributed

*Both closed 2026-09-09 by [ADR 0015](../adr/0015-game-data-is-sourced-first-hand-not-adapted.md),
which removed the question rather than answering it: the owner chose to source
game data first-hand instead of sending F2. **F1 and F2 in
[prior-art.md](../prior-art.md) close with them.** These are the entries as they
stood.*

- **Q2 — Where the drop data should come from.** *Narrowed twice, still open.*
  Kornblume is not canonical — it is a presentation layer over Huiji Wiki
  (characters), 必要的记录 (drop data) and ArkPlanner (algorithm). But the
  sampling itself is in the repository, in `stages<major>_<minor>_greedy.json`:
  raw drop counts and the number of runs behind them, which the adapter now reads
  and carries. So **F1** in [docs/prior-art.md](docs/prior-art.md) — go to
  必要的记录 directly — changes shape: what it buys is *fresher and more*
  sampling, not different numbers, against the cost of a second upstream to
  adapt.
  **This is now the fix for the two surviving benchmark disagreements** (Milled
  Magnesia at 105 runs, Liquefied Terror at 113). ADR 0011 discounts a thin
  sample honestly; only more sampling makes it thick. Weigh it against Phase 6,
  where our own drop reports would do the same job with data we own.
  **It also owns everything the time axis cannot exercise.** After N14 the model
  prices rewards, weekday rotation and (in a dozen lines it does not have)
  shops — and this upstream publishes none of the three. A second source is now
  the only thing standing between a working calendar and a calendar that has met
  real data. See [ADR 0013](docs/adr/0013-the-horizon-is-a-scalar-not-an-index.md).

- **Q3 — Seed data provenance.** ~~**CLOSED 2026-09-09**~~, by removing the
  question rather than answering it. Kornblume still has **no `LICENSE`**
  (re-verified through the GitHub API that day: `license: null`). Rather than
  send **F2**, the owner chose to source game data first-hand —
  [ADR 0015](docs/adr/0015-game-data-is-sourced-first-hand-not-adapted.md),
  which supersedes 0009 on its conclusion and keeps its mechanics. **F2 closes
  with it**: it blocked deploying *upstream* numbers publicly, and there will not
  be any. The publisher’s rights in names and text are untouched by this and are
  why *numbers and text only, no game assets* stays an invariant.


- ~~**Q1 — Hosting target.**~~ **Answered 2026-09-02: VPS, but deferred.** The
  constraint is no spend on this project. Consequence recorded under
  *Deviations from the plan* below — this is not a neutral scheduling change.
  If the constraint softens, Oracle Cloud Always Free is the option to try
  first: ARM instance with real block storage, free indefinitely rather than a
  trial, card required for identity only. Real disks matter for phase 7 anyway.
- ~~**Q6 — How is equipment modelled?**~~ **Answered 2026-09-05: it is an
  `Entity`.** [ADR 0007](docs/adr/0007-equipment-is-an-entity.md). The leaning
  was right; the reason was not the one written here. What actually decides it
  is that `Upgrade` and `Goal` key on `EntityId`, so an upgradeable thing has to
  be an `Entity` already, and the Entity/Item dual identity gear needs is the
  same one `Item`'s javadoc already grants a character's copies. Gacha turned
  out to be indifferent — `BannerModel` never mentions `EntityId`. `Entity`
  gained a required opaque `kind`, catalog-only; enforcing "catalog-only" is
  **N4**. **F4 is closed and Phase 1 ingestion is unblocked.**
- ~~**Q8 — A drop estimate is a proportion; the optimizer needs a quantity.**~~
  **Answered and closed 2026-09-08 (ninth session) by N17 and
  [ADR 0011](docs/adr/0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md).**
  The answer is the one this entry kept circling: a yield is a **mean per run**,
  it carries the **number of runs** behind it, and the interval for it is the
  Poisson score interval rather than Wilson's. `PoissonRateInterval` is in
  `stats`, `Drop.sampledRuns` is in the domain and the schema, and `YieldTable`
  gives the solver the lower bound. ADR 0006 is **narrowed, not superseded** —
  Wilson is still right for a proportion, and `DropEstimate` is still one until
  Phase 6 publishes means instead. **What is left of it is Phase 6 work and is
  written into `YieldTable`'s javadoc:** a measured estimate is still a
  proportion, still usable only where the units agree, and still not discounted
  for its own sample. The history below is kept because it is how the answer was
  arrived at.
  *Original entry:*
  *Opened 2026-09-07, seventh session.* `DropEstimate.pointEstimate` carries a
  Wilson score interval, which makes it a binomial **proportion**: the share of
  runs that yielded the item. The MIP constraint is
  `sum over stages of runs * yield >= demand`, so its coefficient is an
  **expected quantity per run**, and `Drop.expectedYield` already is one —
  unbounded above, and real upstream tables carry values well past 1.0
  (`docs/prior-art.md` §4.1). The two are equal only for an item that drops at
  most once per run.
  **Held at the boundary for now.** `YieldTable` prefers a measured estimate only
  where `Drop.isExpressibleAsProbability()` says the units agree, and uses the
  declaration everywhere else. That is correct and it is a workaround: it means a
  multi-drop stage can never benefit from being measured, which is exactly the
  stage where the declared number is least trustworthy.
  **The real answer is that `stats` owes `planner` a mean per run and a sample
  size, not only a proportion** — and a mean per run needs a different interval
  than Wilson's, which is a statement about a binomial. It may supersede
  [ADR 0006](docs/adr/0006-wilson-intervals-for-drop-rates.md) in part: Wilson
  stays right for "did it drop", and "how many dropped" is a different question.
  **Escalated 2026-09-07 (eighth session): this is no longer a boundary
  formality to settle in Phase 6, it is choosing plans today.** Two things
  changed. First, the sample size exists and we are throwing it away: the
  upstream's sampled stage tables carry a `count` per stage — the number of runs
  its drop counts were observed over, from **105 to 41 212** — and the adapter
  divides by it and drops it, because `Drop` has nowhere to put it. Second, the
  community benchmark showed what that costs: **every disagreement with a
  published guide over 25% is a hundred-run mean outranking a well-sampled one.**
  Milled Magnesia prefers 4-5H (105 runs) to the guide's 5-8H by 154%; Liquefied
  Terror prefers 4-4H (113 runs) to 9-3H by 224%. A noisy mean is a high mean
  about as often as a low one, and `argmin` picks whichever stage got lucky.
  Now **N17**, and it is ahead of the time axis in the queue.
- ~~**Q5 — Repository host.**~~ **Answered: GitHub**, HTTPS remote at
  `github.com/kietnt4412/storm_almanac`. Superseded by Q7 below.
- ~~**Q7 — How is CI status checked from a session?**~~ **Answered
  2026-09-05: authenticated `gh`, by full path.** The user ran `gh auth login`
  by hand (interactive, handles credentials — a session cannot drive it); the
  account is `kietnt4412`, HTTPS, token scopes `repo`, `workflow`, `read:org`,
  `gist`. **Two gotchas, both now paid for:** `gh` is not on `PATH`, so use
  `C:\Program Files\GitHub CLI\gh.exe`; and PowerShell will not execute a
  quoted path without the call operator `&`. The recipe that works:

  ```powershell
  & "C:\Program Files\GitHub CLI\gh.exe" run list --limit 8
  & "C:\Program Files\GitHub CLI\gh.exe" run view <id> --log
  ```

  Confirmed independently this session: the repo is private, so
  `api.github.com` **404s anonymously** — there is no unauthenticated read-only
  path, and screenshots were the only prior option. No more screenshots.

---

## E2 — the full account

Moved out of the tracker on 2026-09-08 (tenth session). The operative half — the
one line in `build.gradle.kts` and why it is a system property — stays there.
This is the part that only matters if the problem comes back.

Docker Engine 29 raised the minimum client API version to **1.40**. Spring Boot
3.5.6's BOM manages Testcontainers **1.21.3**, whose docker-java 3.4.2 defaults
to **1.32**, so every container-backed test died at startup with
`Status 400: client version 1.32 is too old`.

**It looked like a broken machine rather than a broken build:** 33 tests failed
across six classes with three different-looking symptoms while `docker ps` worked
fine.

The fix, in `backend/build.gradle.kts`:

```kotlin
tasks.withType<Test>().configureEach { systemProperty("api.version", "1.44") }
```

`api.version` is docker-java's own config key. It is a **system property on the
test task**, not an environment variable, because a Gradle test worker inherits
the *daemon's* environment and not the shell's — `DOCKER_API_VERSION=…` on the
command line does nothing.

**Rejected:** upgrading Testcontainers. 1.21.4 still pins docker-java 3.4.2, and
2.0.5 pins 3.7.1 but **renamed the module artifacts** (`org.testcontainers:postgresql`
stops at 1.21.4), so it is a migration wanting its own change and its own green
run. Forcing the external `com.github.docker-java` artifacts newer does nothing
either: the core is shaded into the Testcontainers jar.

---

## D1 — the deferral, in full

*Moved out of the tracker 2026-09-09, when it was reversed. The live file keeps
the reversal and what the deferral still costs to buy back; this is the entry as
it stood for seven sessions.*


**Decision:** no money will be spent on this project, so no hosting is
provisioned. The `deploy` job in `ci.yml` is `if: false`.

**What this costs, stated plainly:**

- **Phase 0 cannot meet its exit criterion.** "A green pipeline deploying a
  health endpoint to a real URL" is not achievable without a URL. Phase 0 is
  closed *with this exception noted*, not met. Do not tick it.
- **The Track B gate loses its meaning.** The gate exists so `almanac-store` and
  `almanac-raft` are shaped by real write volume, real read patterns and real
  failure modes. With nothing deployed there is no traffic to observe, and the
  plan is explicit that infrastructure built against imagined requirements is a
  toy.
- **The headline CV claim weakens.** "I run a live tool for two games with real
  users" is the sentence this project is arranged to earn.
- **Deploy problems get discovered late.** Phase 0 puts the deploy first
  precisely because that is when it is cheapest to fix.

**Mitigation, agreed:** revisit hosting at **Phase 4**, not at the end. Phases
1–3 need no server, so nothing is blocked between now and then.

**Reversal trigger:** the moment any free-tier host is acceptable, or the moment
Phase 4 is reached — whichever is sooner. Before starting Phase 7, re-read this
and decide consciously whether Track B is still worth doing on synthetic
workloads. It may be; that is a decision to make with open eyes, not by default.

---

## E3 — Git prompted for an account on every push

*Moved out of the tracker 2026-09-09: fixed, pinned, and finished with.*

**Fixed 2026-09-08.** Windows Credential Manager held two GitHub logins —
`git:https://tuankiet4412@github.com` alongside `git:https://github.com`
(`kietnt4412`) — and Git Credential Manager shows an account picker whenever
there is more than one. Neither `git config` nor `gh auth` had a second identity,
so looking there finds nothing; the second credential is only visible to
`cmdkey /list`. The stray one was deleted and the surviving username pinned:

```bash
git config --global credential.https://github.com.username kietnt4412
```

The pin is what stops it coming back the next time a second account touches this
machine.

---

## The tracker as it stood before the 2026-09-18 compression

**Why this is here.** The twenty-third session rewrote `TRACKER.md` end to end
rather than trimming it again: six sessions had tried to hold it under 550 lines
and it had gone 747 → 742 → 749 → 769 → 790 → 826. The rewrite is the answer to
the question the previous five kept deferring — *Current state* stopped carrying
prose that the notes it links to already hold, the thirty-row component table
became ten live rows and a stable list, and the session-log index became what an
index is.

**Nothing was deleted, and this is where the proof lives.** The whole file is
reproduced below at its full 826 lines, unaltered except that every heading is
demoted two levels so it does not collide with this document's own structure. If
a later session finds a fact the new tracker does not carry, it is in here, and
the right response is to decide whether it is still operative — not to assume it
was lost.

<details>
<summary><b>TRACKER.md, 826 lines, as of 2026-09-18 (twenty-third session)</b></summary>

### Storm Almanac — build tracker

**Single source of truth for progress across chat sessions. Read this first,
update it last.** If a session ends without this file reflecting what happened,
the next session starts from a lie.

**History lives in [docs/history/tracker-archive.md](docs/history/tracker-archive.md)** —
the session log, the completed next actions, the closed phases in full, the
answered questions. Nothing was deleted when this file was split on 2026-09-08;
it moved. What stays here is what is still *operative*: age is not the criterion,
being finished with is.

- Source of the plan: [plan.html](plan.html) (13 phases, two tracks).
  [README.md](README.md) is the public face; [CLAUDE.md](CLAUDE.md) is the
  working agreement.
- Last updated: **2026-09-18** (twenty-third session)

---

#### Status

- **The launch title is now Punishing: Gray Raven, 2026-09-13 ([D3](#d3--launch-title-swapped-to-punishing-gray-raven-2026-09-13)).**
  Reverse: 1999 moves to second. The reason: Kornblume turned out to solve per
  player, so on R1999 this product overlapped a deployed tool almost feature for
  feature, and `prior-art.md` had said otherwise. **PGR has no data here yet**, and
  its fodder shape is not in the solver, so the next actions are **N27** (read
  PGR) and **N30** (fodder). Everything below this bullet that says "R1999" was
  true when written and still is. It is just no longer the launch.
- **PGR has been read, and it corrected the plan twice** — 2026-09-18,
  [the note](docs/game-facts/punishing-gray-raven-research-disclosure.md). **Both
  banner archetypes are first-hand and complete**, the weapon's feeding numbers are
  read, and **N28's pull-currency chain exists at last**. Two things the *guides*
  said turned out false against the client: PGR's featured rule is a **per-banner
  rate** (70% with a published Calibration guarantee, or 100% with none), not a
  constant; and **PGR events have no shops**, are **not energy-priced**, and pay
  **one-time mission grants** rather than farmable yields. **N30 shrank by two
  features and grew by one**, and the new one does not fit `EnergyMip`. Three engine
  costs fall out: a **floating `hardAt`** drawn per cycle, a **`PityScope` for pool
  type that carries Calibration across pool boundaries** — R1999 clears at event
  end and PGR inherits, so the two published games now disagree on the one question
  that enum exists to answer — and `featured` finally carrying weight. **Block 4,
  the timed authoring pass, was cut by the maintainer**, so the cost of sourcing a
  patch is still unmeasured.
- **Phases 1, 2 and 3 stay closed, criterion and scope.** One line each on the
  [phase board](#track-a--product); the qualifications that matter are in
  [what is still unverified](#what-is-still-unverified).
- **Phase 5 — Gacha engine — is CLOSED on its criterion, 2026-09-12, out of order
  on purpose ([D2](#d2--phase-5-entered-before-phase-4-closed-2026-09-12)).**
  Numbers in the `gacha` row below, and decisions in
  [ADR 0018](docs/adr/0018-the-gacha-engines-answer-one-question-about-one-rarity.md).
  **Nothing calls either engine**, and the income model is **N28**. One R1999 banner
  is first-hand (Q4); PGR's are not.
- **Phase 4 — Frontend v1 and launch — is OPEN**, 2026-09-09.
  **[D1 is reversed](#d1--deployment-deferred-2026-09-02)** — Vercel and Render,
  free tier, still no money. Nothing is deployed: the decision is made, the
  wiring is **B5**.
  **All five screens exist and N25 is closed, 2026-09-11**, driven in a browser
  against a real database; its three debts are paid. **Phase 4's second exit clause
  is served**: a logged-in character page says what that reader is short of, from
  where their roster actually stands. **The first clause is not and cannot be until
  B5** — five strangers cannot complete a plan against something that is not
  deployed. *The data blocker stands:* see the bootstrap bullet below.
- **The first self-sourced bundle exists — and what the format would not take is
  worth more than what it took.** 2026-09-18,
  [`data/bundles/punishing-gray-raven-steering-by-light.json`](data/bundles/punishing-gray-raven-steering-by-light.json),
  ingested as **draft 0 with nine first-hand facts** and **not published** — the
  approval is the maintainer's by design. Of a reading that filled 545 lines,
  **three blocks were read clearly and could not be written down**: the Themed
  Construct banner, because `PityRule.hardAt` is an `int` and PGR draws it
  (**N31**); the weapon's Overclock and Harmony recipes, because an `Upgrade`
  names an entity and **the weapon's own name was not recorded**; and **every
  currency**, because `Item.rarity` is required and nothing read grades one
  (**N28**). Until it publishes, the catalog pages still read "Invented for this
  project" or "Nobody recorded where these numbers were read", **and that is the
  feature working**. [The loop](docs/game-facts/authoring-a-first-hand-bundle.md).
- **The project is going first-hand on game data**, decided 2026-09-09 —
  [ADR 0015](docs/adr/0015-game-data-is-sourced-first-hand-not-adapted.md),
  superseding 0009 on its conclusion and closing **Q2, Q3, F1 and F2** at once.
  Kornblume is unlicensed. **Nothing is deleted yet and the order matters:** the
  adapter stays as a never-shipped cross-check until a replacement exists, because
  removing it first leaves the project with no real data at all.
  **The cost, measured on patch 3.5:** ~2 700 static catalog facts (91 items, 100
  stages, 50 recipes, 118 arcanists, 37 psychubes) and **595 drop-rate facts**. The
  first half is typing and is mostly additive per patch. **The second half is the
  problem** — see the bootstrap below.
- **The bootstrap problem is the main risk in the project, and N26 confirmed the
  cheap way out is not there.** The optimizer cannot rank a stage without a
  yield, so no drop data means no plan; own drop data means Phase 6, which means
  users, which means a working plan. **The publisher does not disclose stage drop
  rates** — the stage screen grades a reward `Fixed`, `Common` or `Possible` and
  prices only the first, and every published percentage for the other two is
  somebody's crowdsourced sample. Measured on the pinned snapshot: **15 of 779
  drop facts are declared, 764 sampled** — ~2% free. Two real consolations:
  **gacha rates *are* disclosed** (half of **Q4**, one screen's reading, and Phase 5
  closing has raised what it is worth), and **the `Fixed`/`Common`/`Possible` grade
  is itself a free first-hand fact** for all 595 pairs, with nowhere in the model to
  live until Phase 6 consumes it.
  [The drop disclosure note](docs/game-facts/reverse-1999-drop-disclosure.md).
- **Phase 0 stays closed by exception** — deploy deferred by D1 — and its box
  stays unticked, because nothing is deployed.
- **Track B: not started, and gated.** See [the gate](#the-gate).
- **The remote, checked 2026-09-18 (twenty-third session) — re-check it, do not
  trust it.** `gh pr list` is **empty** and `dev` tracks `origin/dev` clean, so
  **the sixth push to `dev` with no open PR is the one this session makes**;
  whatever lands here runs no CI until a PR exists. Earlier: at the twentieth
  session's start **PR #19 was MERGED** (Phase 5 is on `main`),
  `origin/main` and `origin/dev` were equal, and `dev` had no open PR.
  **[PR #20](https://github.com/kietnt4412/storm_almanac/pull/20) (N5) was then
  opened and is green on `ef51844`, run `34731880389`** — and **every test task in
  that run was `FROM-CACHE`**, correctly, because only YAML changed. It proves the
  new actions' mechanics, not the suite under them. **Run `34746241622` on `85d0538` did execute `:modules:gacha:test`**, green.
  **Counting PASSED lines in a CI log undercounts** — read the task outcomes and
  the totals. **The trap:** CI runs on `pull_request` and on push to `main` only,
  so **a push to `dev` with no open PR runs nothing, silently.** Every merge re-arms
  it; five times now, each caught by looking. **Check `gh pr list` and open the PR
  before trusting a push to `dev`.** It is a fact here rather than a next action
  because a "merge PR #n" line is stale before it is read.
- **The optimizer has been asked a question by something other than a test.**
  What has *not* happened is a real OAuth exchange: no provider is configured and
  no client secret exists, so login is installed only when one is. The
  development sign-in does not change that and is not meant to — it builds a
  principal directly, and ADR 0017's reversal trigger is deleting it the moment a
  real provider works locally.
- **`backend/Dockerfile`'s COPY list is a second copy of the module list and
  drifts in silence.** It had omitted `adapters/` since Phase 1 — every image
  build failing in six seconds while the table below called it verified — and was
  fixed 2026-09-09. **Anything added beside `modules`, `adapters`, `substrate`,
  `app` needs a line there**, and Render builds from this file, so it is **B5**'s
  path.
- **The time axis still has no real data to eat**, checked rather than assumed —
  and the reason is better than "the upstream is silent". See
  [the unverified list](#what-is-still-unverified) before treating it as a gap.

##### Two standing caveats, read them every session

1. **The strongest tests in this repository are ones CI does not run.** Upstream
   data is fetched and never committed
   ([ADR 0009](docs/adr/0009-upstream-data-is-fetched-never-vendored.md)), so
   `RealUpstreamPatchTest`, `RealUpstreamPlanTest` and `CommunityBenchmarkTest`
   skip on the runner — 16 of the 257 tests. **Every performance number and every
   comparison with an outside answer in this file comes from a test the pipeline
   does not run.** Run `backend/tools/fetch-upstream.sh` before trusting a green
   build to mean the pipeline handles real data.
2. **A green build says nothing about whether the data going into it is the data
   the upstream actually publishes.** The adapter spent five sessions reading a
   stale stage table with two thirds of the game missing, and everything
   downstream was green the whole time. What caught it was going to look for
   somebody else's answer.

---

#### How to use this file

1. **At session start:** read *Status*, *Current state*, *Next actions* and
   *Open questions*. Do not re-derive them from the code.
2. **During the session:** tick boxes as work lands, not when it is planned, and
   write the tracker line in the same breath as the push. **Never write "merge
   PR #n" as a next action** — merging is a click that happens between sessions,
   so the line is stale before it is read. Four sessions running opened on one.
   The state of the remote goes in *Status*, checked rather than trusted.
3. **At session end:** update *Last updated* and *Current state*, prune *Next
   actions*, and **append the session entry to
   [the archive](docs/history/tracker-archive.md#session-log)** — then add one
   line to the [index](#session-log-index) here.

Rules that keep this file honest:

- A box is ticked only when its exit criterion is met, not when the code exists.
- "Done" means tested and committed. "Scaffolded" means the shape exists and the
  behaviour does not — say so explicitly.
- Never delete a session-log entry. Supersede it, and say what the older one got
  wrong.
- **When an item closes, move it to the archive rather than striking it through
  in place.** This file reached 2 412 lines and 138 KB by keeping every corpse in
  the room, and a handoff document nobody reads is worse than no handoff
  document. **Keep it under about 550 lines** — roughly what a session can read
  before starting work. If a session adds more than it removes, it has moved the
  problem rather than done the work.
  **Measure it with `wc -l` rather than carrying the last number forward** — the
  drift has twice run in the flattering direction, by seven lines once. **Six
  sessions have now tried and the file has gone down once, by five lines**:
  747 → 742 → 749 → 769 → 790 → 826, measured each time
  ([what each cut, and why it did not show](docs/history/tracker-archive.md#the-line-count-ledger)).
  **That is a pattern, not six failures of will**: a session that finds something
  has to write it down, and a handoff document cannot also be a fixed-size one.
  **Settle it rather than reporting the miss a seventh time** — either the 550
  is wrong, or *Current state* stops carrying prose that belongs in the notes it
  already links to.
  **The metric is not measuring the thing.** The twenty-second session removed
  ~2 700 characters and **zero lines**, because the rows it compressed were each
  one very long line. A row nobody can read in one breath costs a session as much
  as twenty short ones. So: keep measuring lines, **and read the long rows as
  their own debt** — the remaining ones are in *What is still unverified*.

---

#### Current state

**What exists:** a game data pipeline that works end to end, an API that serves it,
a real game's data going through all of it, an optimizer that turns that data into
a plan and a reason to believe the plan, an account that can own one and be refused
somebody else's, two of that account's devices that can edit what it owns without
deleting each other's work, five screens a browser has driven, a page that says
where its numbers were read, an app that still renders with its server switched
off — and, since this session, two gacha engines that agree with each other about
both published games.

**The load-bearing claim — and it is a number to re-earn.** On **nine** benchmark
materials the cheapest stage this project computes is the one a published community
guide tells players to farm, and the optimizer's plan for a real goal set costs
**3 880 Activity against the guide's 4 017** — which does not even cover the whole
demand. Nine and not five because a yield carries its sample size
([ADR 0011](docs/adr/0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md)).
**Every one of those numbers is computed from Kornblume-fed inputs**, which
[ADR 0015](docs/adr/0015-game-data-is-sourced-first-hand-not-adapted.md) says the
product will not ship — so until a self-sourced bundle reproduces them they are
evidence about somebody else's numbers run through our solver. **The method
survives untouched**, because the guide it compares against is a separate published
artifact: a claim to re-earn, not a test to delete.
See [the benchmark](docs/benchmarks/reverse-1999-community-answers.md).

Still true from earlier phases: **the solver says how much it does not know**
([ADR 0010](docs/adr/0010-a-plan-is-the-best-provable-in-the-budget.md)), **the
pipeline has met somebody else's data and held**, and **both of Phase 1's exit
questions are answered over HTTP rather than by a test calling a repository**.

Toolchain on this machine: JDK 21.0.12 (Temurin), Gradle **9.6.0** via the
committed wrapper. Remote is HTTPS at `github.com/kietnt4412/storm_almanac`.

| Area | State | The one thing to know |
|------|-------|-----------------------|
| Backend build | **Green** | **330 tests**, run in full 2026-09-18, 0 skipped locally with snapshots present; **314 on CI**, because 16 snapshot-gated ones skip. `:app:test` depends on `:app:bootJar` — `DeployableJarTest` reads the artifact — and, since 2026-09-18, declares `data/bundles` as an input: without it `AuthoredBundlesTest` was `FROM-CACHE` after a bundle changed, **measured by dropping a second-hand bundle in and watching the build stay green**. Test tasks set `api.version=1.44` — [E2](#e2--docker-engine-29-refuses-testcontainers-api-version) |
| Authored game data | **One bundle, draft, first-hand** | `data/bundles/punishing-gray-raven-steering-by-light.json` — nine facts off the PGR client, ingested at sequence 0 and **not published**. `AuthoredBundlesTest` parses every file there and fails on any fact the project is not entitled to publish; it fails on an **empty** directory too, because a scan with nothing to scan is the vacuous pass this repository has already been bitten by. Nothing else in the build reads that directory |
| CI workflow | **Green, no warnings, on Node 24** | Run `34731880389` on `ef51844` ([PR #20](https://github.com/kietnt4412/storm_almanac/pull/20), N5): zero annotations. **Test results cached from #19's run `34695206362`**, where the suite last *executed*: 16 skipped, exactly the three snapshot-gated classes (`RealUpstreamPlanTest` 8, `CommunityBenchmarkTest` 5, `RealUpstreamPatchTest` 3) — a pass there would mean a snapshot had been committed by accident. **`gradle/actions` is held at v5**: v6 needs Gradle's Terms of Use accepted, which is the maintainer's call, and v5 has been frozen since 2026-02-23 |
| Game data pipeline (Phase 1) | **Closed and stable** | `V2`–`V4`, 28 tables, seven invariants on two real patches, a round trip pinning parser against writer (ADR 0008), publishing as a human approval. `Drop` carries `sampledRuns`, where 0 means *declared*. [In full in the archive](docs/history/tracker-archive.md#closed-phases-in-full) |
| Parser adapters | **One, and demoted to a cross-check by [ADR 0015](docs/adr/0015-game-data-is-sourced-first-hand-not-adapted.md)** | `:adapters:reverse-1999`, 25 tests. **It hard-codes `THIRD_PARTY` and so fails a plain `publish`** — there is no call site to launder data through. Kept rather than deleted because diffing the first self-sourced bundle against it is worth more than it ever was as a source. [The prose it used to carry](docs/history/tracker-archive.md#rows-compressed-out-of-current-state-2026-09-12-nineteenth-session) |
| Provenance | **Done — written, enforced, and read** | [ADR 0016](docs/adr/0016-provenance-is-a-property-of-the-data.md). `V7` stores one row per *declared* fact; `publish` refuses a version that is not first-hand and names the facts, and the exception is a word the operator types. **`ProvenanceRepository` is a second port, not a field on `GameDefinition`** — so the solver cannot see where a number came from and cannot be made to prefer one. Silence is `UNRECORDED`, which parses and cannot publish. [The prose it used to carry](docs/history/tracker-archive.md#rows-compressed-out-of-current-state-2026-09-12-nineteenth-session) |
| Game data API | **Served and verified** | Seven game-data routes plus health, version-pinnable, every response carrying its version, attribution and — on the four that carry facts — **where each fact was read**. 15 HTTP tests plus a hand check against `docker compose up`. [In full in the archive](docs/history/tracker-archive.md#rows-compressed-out-of-current-state-2026-09-18-twenty-second-session) |
| Demand resolution | **Done** | Goals + roster + upgrade graph → a demand vector, walking the DAG backwards. Refuses by name rather than guessing: unreachable states, unknown entities, probabilistic goals, ambiguous routes |
| The MIP (`EnergyMip`) | **Done for stages, crafts and rewards** | ojAlgo, integer runs, inventory subtracted, every variable bounded — the bound is what makes a real patch solvable. **No shops and no fodder**; items sourced only from those are refused by name |
| The time axis | **Done — as a scalar, not an index** | [ADR 0013](docs/adr/0013-the-horizon-is-a-scalar-not-an-index.md). An energy cap, cadence counts, and rotation as a capacity shared over *subsets* of distinct weekday restrictions. **No variable is indexed by day**, which is why the p95 survived: 1 805 → **1 807 ms**. Proven on the fixture; the real upstream declares nothing time-varying |
| Yield source (`YieldTable`) | **Done, and it carries the sample** | Declared yields, discounted to the lower end of a 95% `PoissonRateInterval` wherever a sample size exists (ADR 0011). No sample means used as declared |
| `Optimizer` (`MipOptimizer`) | **Done — least energy** | Pins the plan to its version, fingerprints the request (`SolveKey`), explains itself with shadow prices by re-solve. Budget split between search and explanation — ADR 0010 |
| Objectives | **Two searches over one model** | `LEAST_ENERGY` takes the whole horizon; `FEWEST_DAYS` binary-searches for the shortest one that fits, then solves for least energy inside it. On the fixture, Insight 1 is **0 energy over 28 days or 370 over 2**. They still coincide wherever a game's data has nothing on a cadence, and the plan says which case it is |
| Solve caching | **Done, in-process** | `SolveCache` is get and put over a `SolveKey` and has **no invalidation method** — a patch is a different key, not a stale entry. On the real 3.5 patch a repeat question goes **1 806 ms → 2 ms**. Not Redis, and [ADR 0012](docs/adr/0012-the-solve-cache-is-in-process-until-there-is-a-second-node.md) says why |
| `SolveCoordinator` | **Single-node, done** | One execution per idempotency key however submits interleave; a ticket to poll; an honest queue depth. **Does not survive a restart, deliberately** — making it durable here would answer the question Phase 9 exists to ask |
| Community benchmark | **Done, and now ADR 0011's regression test** | Twenty published claims against what this model computes, ranked on the yields the **solver** uses with the raw ranking printed beside them |
| Statistics primitives | **Done** | `WilsonInterval` (6 tests) for "did it drop", `PoissonRateInterval` (7) for "how many dropped", `PityRule` (9) against both games' published rates |
| `identity` / `player` schema | **Applied and round-tripped** | `V5` (6 tables). Four decisions in its header: **no FK crosses a schema**, nothing points into `gamedata` (version-scoped rows, an inventory outlives a patch), an identity is `(provider, subject)` and **never an email**, and absent means zero with a `CHECK` to keep the two representations from diverging. `V6` adds the two sync tables |
| Sign-in | **Done, never exchanged a token** | Account created while the principal is built, not in a success handler. Everything provider-specific is `SignIn.from` — a pure function; OIDC says `sub`, Discord says `id`. **`oauth2Login` installs only when a provider is configured**, because no client secret exists (D1) and a blank client id fails a `ClientRegistration` outright. The deny is not conditional. CSRF is `SecurityConfig.browserCsrf`, shared with the development chain: the token is resolved eagerly, because the deferred default never issues the cookie a page needs to write |
| Player API | **Served and authorized** | Everything under `/api/me`, so **no route takes an account id** and none's authorization can be forgotten. `OwnedProfiles.require` is the one check; another account's profile is **404, not 403**, so an id is not an enumeration oracle. PUT replaces a whole aggregate; PATCH merges per key |
| Offline sync | **Done on both sides — inventory and roster** | `PATCH`, last-write-wins per key ([ADR 0014](docs/adr/0014-sync-is-last-write-wins-per-key-against-a-clock-that-outlives-the-value.md)). The clock is a **table, not a column**, because a removal deletes its row and a tombstone cannot live on the row it outlives; a `PUT` also writes a whole-aggregate **watermark**, because a full save speaks for keys that have never existed and no per-key row can say that. Client timestamps, clamped to the server's clock. The response names the keys that lost. **Goals have no PATCH** — an ordered list has no per-key merge, which is also why the client saves them only while connected rather than queueing a whole-list write that would clobber a second device. **The client half landed with N25**: a persisted per-profile, per-key outbox stamped at the moment of typing, flushed debounced over PATCH, surfacing the keys that lost. Proven in a browser against a refusing network and against a deliberately stale edit |
| Plan route | **Done — Phase 3's criterion** | `POST /api/me/profiles/{id}/plan`, synchronous because a two-second budget is a promise the optimizer can keep. Goals, inventory and roster come from Postgres; the body carries only `energyPerDay` and `horizonDays`. `?version=N` pins the patch |
| Bean wiring | **`Optimizer` and `SolveCache` are beans; `SolveCoordinator` is not** | Registered now because they finally sit on a path a real request takes — which is what N15's refusal was waiting for. The coordinator hands back a ticket for a solve that does not fit the synchronous budget, and there is no asynchronous surface for a ticket to be useful on |
| Architecture tests | **Passing — three of them** | `ModuleBoundaryTest` (Track B layers optional until they exist), `GameAgnosticismTest` (source scan over planner/gacha/stats) and, since 2026-09-12, `EntityKindBoundaryTest` — ADR 0007's own open gap, closed: `Entity.kind` may be read in `gamedata`, `api` and `adapters` and nowhere else. Bytecode rather than a source scan because a grep cannot tell `entity.kind()` from `change.kind()`, and an allowlist rather than a ban on three modules because a denylist passes vacuously the moment a fourth module grows a read |
| Docker Compose | **Repaired 2026-09-09, and no image has ever been built end to end** | The Dockerfile had never learned about `adapters/`; the fixed tree builds the jar locally and that jar contains no development sign-in. **The image itself is still unproven** — the in-container Gradle download was abandoned at 10% after twenty minutes, so `up --build` has not completed. See the COPY-list warning in *Status* |
| Development sign-in | **Done, and absent from the artifact** | [ADR 0017](docs/adr/0017-the-development-sign-in-is-absent-from-the-artifact.md). `:modules:identity-dev` is `testAndDevelopmentOnly` on `:app` — on `bootRun` and the test classpath, **excluded from `bootJar`**, so no property or profile can reach it. `GET /dev/sign-in?as=<name>` mints an ordinary `AuthenticatedAccount` through the same `upsertFromOidc` the OAuth services use; its filter chain lives in that module, so `SecurityConfig` has no hook for it. `DeployableJarTest` opens the jar and proves the absence on every build. The frontend picks its sign-in URL behind `import.meta.env.DEV`, and the production bundle was checked for the string: **zero occurrences** |
| Frontend | **Five screens, driven in a browser, and 15 tests** | Inventory editor, goal picker, plan view (with **every one of the solver's notes**), catalog browse and search, and the character page with the **personalized overlay**. Local is same-origin via the Vite proxy; the development sign-in URL is behind `import.meta.env.DEV` and absent from a production bundle. **Never rendered PGR** — every screen was built against R1999. [In full in the archive](docs/history/tracker-archive.md#rows-compressed-out-of-current-state-2026-09-18-twenty-second-session) |
| `gacha` — the two engines | **Done — Phase 5's criterion, and nothing calls them** | [ADR 0018](docs/adr/0018-the-gacha-engines-answer-one-question-about-one-rarity.md). `MarkovBannerEngine` is an exact forward chain over `(pulls since hit, losses carried, copies held)` reading the mass that **never arrived**, so a wall is exactly 1.0 rather than 0.9999999999999895; `MonteCarloBannerEngine` is 500 000 seeded trials on virtual threads, deterministic because the generators are split before any task starts and counts are summed as integers in chunk order. `PullModel` is the validated slice both share, so they refuse the same banners for the same reasons. **51 tests. Worst gap 0.110 points over 96 questions (eight banners since the first-hand one joined), at 2.22 standard errors** — was 84 and 1.84 at closing. Only the headline rarity is modelled, and `PullResult` carries no `Rarity` — the model pins no other rate while pity is active, so one would be invented per game |
| `gacha` — the income model | **Interface only, and it cannot be written yet** | `projectedPulls` needs to know which item is pull currency and what a pull costs in it. **Neither `BannerModel` nor the `banner` table declares either**, so the question has nothing to compute from — **N28** |
| Track B | **Empty** | Package docs. Track B is [gated](#the-gate) |

##### What is still unverified

Be precise about this, because the temptation is to read "build green" as "it
works". It does not mean that:

- **CI does not run the tests that matter most, and never will as things stand.**
  Three classes touch data this project did not author and all three skip on the
  runner — 16 tests. They are the ones that would catch an upstream-shape
  surprise, **and the only evidence the optimizer is fast enough or right about
  anything real.** The stale stage table that cost five sessions of plans would
  not have been caught by any test CI runs, and was not caught by any test at
  all — a person went looking.
- **The OAuth exchange has never run.** No principal has ever come from a real
  provider: no client id, no secret, no redirect followed, because there is no
  URL to register one against (D1). Tested: the account, the principal, and every
  authorization rule around them, now including over a socket. Untested: the
  token exchange. **The development sign-in does not narrow this by one inch** —
  it builds a principal directly — and reading N24 as "sign-in works" is exactly
  the misreading ADR 0017 is written to prevent. The first deployment is where
  this stops being theoretical.
- **Most authenticated tests still go through MockMvc**, which is a deliberate
  step down from real HTTP and no longer a forced one. `DevSignInTest` is the
  exception and the first of its kind — a real port, a hand-kept cookie jar, no
  security post-processor — so the servlet container is now exercised on the
  signed-in path it never was. `PlanFromStoredStateTest` and `OfflineSyncTest`
  have not been moved: they could be now, and until they are, **the layer that
  caught Phase 0's 401 is covered for sign-in and profiles and not for the plan
  or the merge.**
- **No two real devices have ever synced, but one device has now lost to one.**
  N23's every scenario was one MockMvc request following another inside one JVM.
  N25's outbox adds a real second clock: a browser held edits through a refusing
  network, flushed them on reconnect, and an hour-old edit lost to a newer value
  and said so. **What is still argued rather than measured** is the concurrency
  the merge SQL is actually shaped for — two requests interleaving on the same
  key at the same instant — and **two genuinely separate devices have still never
  been in the same account at once**.
- **A merge publishes nothing.** Nothing downstream can react to a synced edit,
  so a cached plan is not invalidated when the inventory under it moves. Harmless
  today because nothing caches on player state across requests; **read this
  before Phase 6 makes anything depend on an inventory being current.**
- **`SolveCoordinator` is still not wired to anything**, deliberately; see the
  table row above.
- **Nothing has ever asked the gacha engines a question on behalf of a player.**
  Phase 5's criterion is a proof about a model — two methods agreeing, and both
  reproducing published figures — and it is met. What no test can tell you is
  whether the answer is the one a player wants: there is no route, no screen and no
  bean, and the `PityState` a real reader would be asked for is not stored anywhere
  (`player` has an inventory, a roster and goals, and no pity counters). **So
  "probability of guaranteeing her" is computable and unanswerable**, and the gap
  is a schema and a screen rather than an engine.
- **One gacha banner is first-hand and six are not (Q4).** `PublishedRatesTest.Disclosed`
  is evidence about the game. Every other fixture proves only that the model
  reproduces the figures it was *given*. **Multi-copy "can I guarantee her" answers
  are too pessimistic**, because 200 Cassettes of the Lost buy a copy and nothing
  models that. At two copies the engines say 280 pulls and the truth is 200; one
  copy is unaffected — see **N28**.
- **Nothing has measured whether the cache is worth having in production**, only
  that a hit is 900× cheaper than a solve. Hit *rate* depends on whether two
  players ever ask the same question, which needs users. The counters are there
  so that this stays a measurement rather than a belief.
- **Nothing is deployed.** By decision — [D1](#d1--deployment-deferred-2026-09-02).
  The `deploy` job is `if: false` and there is no URL to smoke.
- **The PWA has been loaded with its origin server dead, and an offline *write
  path* has not.** The outbox was proven against a refusing network, not against
  a dead origin on a cold start. The load itself and how it was tested are
  [in the archive](docs/history/tracker-archive.md#closed-out-of-what-is-still-unverified-2026-09-12-eighteenth-session);
  reproducing it needs the **built** bundle (`web-built`, port 4173), because the
  dev server has no worker worth the name.
- **The frontend has 15 tests, and one class of defect they will never catch.**
  Vitest and Testing Library in jsdom, in CI, argued beside the config in
  `vite.config.ts`. Of the four defects a browser found, three were behaviour and a
  component test catches them; the fourth was `display: block` folding a table
  header, which **jsdom will never catch because it computes no layout**. So:
  behaviour is the pipeline's, appearance is a person's, and **driving a browser
  before shipping a screen is still required**.
- **A new page and an old API do not deploy at the same instant, and the first
  time that happened the page rendered as nothing.** A freshly built bundle read a
  response from a backend that predated `sourcing` and threw before React painted
  anything (2026-09-11). Fixed, and the wire types now mark the field optional so
  the compiler points at every call site that has to cope. **This is B5's problem
  in miniature** — Vercel and Render are separate hosts and every release has a
  window like it — and the lesson is wider than one field: a client that treats a
  new response field as guaranteed blanks its own page on every deploy.
- **Nothing has ever run against a jar built from a Dockerfile that works.** The
  image build was repaired this session and the compose stack has not been stood
  up end to end since. The **absence** of the development sign-in from that jar
  *is* proven, by `DeployableJarTest`, on every build.
- **Nothing has ever called the API under load.** Every request loads a whole
  version — fifteen queries — a deliberate deferral written into
  `GameDataReadModel`'s javadoc. The number to beat does not exist yet.
- **Eight qualifications of the closed phases are
  [in the archive](docs/history/tracker-archive.md#qualifications-moved-out-of-the-live-tracker-2026-09-11-seventeenth-session)**
  — the benchmark being one guide, the two large community disagreements, the three
  shapes the model cannot express, what the adapter does not convert, and the rest.
  All still true. **Read them before re-opening Phase 1, 2 or 11.** (One has
  expired: `gacha` had no behaviour, and now does.)
- **One bundle carries first-hand facts, it is a draft, and it is nine facts
  wide.** `punishing-gray-raven` draft 0, 2026-09-18 — five materials, two
  Omniframes, one banner archetype and one fodder rule, `3 PUBLISHER_DISCLOSURE`
  and `6 OBSERVED_IN_GAME`, the first provenance breakdown outside a synthetic
  fixture with no *NOT ours to publish* line. **Nothing is published**, so ADR
  0016's gate is still only ever passed by `AUTHORED_FIXTURE`, and **every
  Reverse: 1999 catalog and drop number in this file still comes from
  Kornblume**. **Nine facts is not a catalog** — no stage, no craft, no reward,
  no upgrade — so nothing can be planned from it and the frontend has still
  never rendered PGR.
  (Provenance stopped being write-only on 2026-09-11;
  [what it says and why that is the feature working](docs/history/tracker-archive.md#closed-out-of-what-is-still-unverified-2026-09-12-eighteenth-session)
  is in the archive.)
---

#### Next actions

Ordered. Completed ones move to
[the archive](docs/history/tracker-archive.md#completed-next-actions).

- [ ] **N27 — Finish the first PGR bundle. It is authored, ingested and awaiting
      the approval; what it could not hold is the next reading order.**
      [`data/bundles/punishing-gray-raven-steering-by-light.json`](data/bundles/punishing-gray-raven-steering-by-light.json)
      is **draft 0, nine facts, all first-hand** — the first in this repository
      that is not a synthetic fixture. **Publishing is one command and is the
      maintainer's**, so it is a fact here rather than an action:
      `--gamedata=publish punishing-gray-raven 0`.
      **Nine facts out of that reading is the finding.** Three things were read
      clearly and the format refused them
      ([the account](docs/game-facts/punishing-gray-raven-research-disclosure.md)):
      the **Themed Construct banner** (**N31**); the **Overclock recipe
      16/16/20/28 and Harmony Lv 1's 25 Accelerators**, because an `Upgrade` names
      an entity and the weapon they were read off **was not named** — *the
      cheapest thing in this list to fix*; and **every currency**, because
      `Item.rarity` is required and no screen read grades one (**N28**).
      **Still unread:** the **Memory** system entirely, the costs behind the
      character's four axes (Train, Evolve, Awaken, Phylotree), and the
      **fodder item's own name**, which is what makes the authored fodder rule
      inert. **Block 4, the timed authoring pass, was cut by the maintainer** — so
      the cost of sourcing a patch stays unmeasured, and any later claim that a
      patch is affordable to source is an estimate with nothing behind it.
      **Ask how each value was read before recording it** — a relayed web-search
      answer and a screen reading look identical in chat. R1999's pass is Phase 11.
- [ ] **N31 — Give `PityRule` a guarantee that is drawn, not fixed.** PGR's Themed
      Construct pool draws its wall **uniformly 80–100 and redraws it on every
      S-Rank**; `hardAt` is an `int`, so **the archetype is absent from the first
      bundle rather than approximated** — a gap in published data, now, and not an
      argument in a note. Both engines are affected asymmetrically
      ([ADR 0018](docs/adr/0018-the-gacha-engines-answer-one-question-about-one-rarity.md)):
      Monte Carlo needs one extra draw, the exact chain needs the drawn threshold
      in its state or 21 mixed chains. **Do it with the fixture correction it
      implies** — `Banners.grayRavenFloating()` pairs the 1.50% base with a 70%
      featured rate, and the client pairs 1.50% with **100%**.
- [ ] **N30 — Put fodder and the event shape into the solver (D3). Shops are out.**
      **Rescoped 2026-09-18 against N27's reading, and it moved both ways.**
      **Dropped — the shop half**, whose premise was guide-derived and is false:
      **PGR events have no shops at all**, on every event and not just the one read,
      so what `EnergyMip` refuses today it can go on refusing. Also dropped,
      **probabilistic goals**: Resonance is **picked, not rolled**.
      **Confirmed — `Fodder`**, and it now has numbers: a 4★ unit is **300 EXP**
      whether it is a material *or a weapon*, so one rule covers both.
      **Added, and it is the larger job:** a PGR event is **a one-time grant behind
      a capability gate inside a closing window** — no energy cost, no repetition,
      no yield. `EnergyMip`'s "run stage N times for yield Y" cannot express it and
      `Reward`'s cadence cannot either, and **`LEAST_ENERGY` is not even the right
      question — `FEWEST_DAYS` against `Availability.closesAt` is.** Decide
      deliberately that *"can this player clear stage N"* is an input the reader
      supplies, not something the solver derives.
- [ ] **N28 — Give a banner a pull currency and a price, and then write
      `IncomeModel`. Unblocked 2026-09-18 — the numbers exist now.** PGR's chain was
      read off the client: **1 pull = 250 Event Construct R&D Tickets** (2500 for
      ten, no discount), **1 Black Card = 1 ticket**, **1 Rainbow Card = 10 Black**,
      **119 Rainbow = $19.99** — about $4.20 a pull
      ([the note](docs/game-facts/punishing-gray-raven-research-disclosure.md)).
      Phase 5's unbuilt scope, and it is a schema change rather
      than a decision: `projectedPulls(profile, date)` has to know which item is
      pull currency and what a pull costs in it, and **neither `BannerModel` nor
      `gamedata.banner` declares either**. So "she arrives in 40 days, can I
      guarantee her" — the feature the plan calls the one nobody ships well — has
      nothing to compute from, and no amount of engine work changes that. Costs a
      bundle field, parser, writer, a migration and the JDBC round trip, exactly
      like **N20** — **plus a decision the first bundle forced**: the ticket has to
      exist as an `Item`, `Item.rarity` is required, and **no screen that was read
      grades a currency at all**. Decide whether a rarity is optional or whether a
      currency is simply given one, rather than discovering it mid-migration. **Do it with a game whose income sources are actually
      ingested**: the accrual side reads `Reward` cadences, and nothing ingested
      has ever been checked for them. **Two more fields belong in the same change**,
      both found on R1999 2026-09-13 ([the note](docs/game-facts/reverse-1999-summon-disclosure.md)):
      a **shop exchange for the featured unit**, since cassettes cut six copies from
      840 pulls to 560, and a **cap on copies** at the most that changes anything,
      since `copies` is unbounded today. **Read whether PGR has either before
      designing them**, now that PGR launches first.
- [ ] **N20 — Put the game's day boundary on the game, not in the planner.**
      `EnergyMip.matchingDays` reads weekdays in **UTC** — a game assumption in a
      game-agnostic module. R1999 Global rolls over at **05:00 UTC−5, weekly
      Monday**. Inert today because nothing ingested rotates, so it is deferred on
      the same reasoning that kept unused beans out of N15. **It stops being inert
      with the first game that rotates, which is PGR and so now the launch** (D3),
      not Phase 11. Costs a bundle field, parser, writer, a migration and the JDBC
      round trip — do it *with* that game, not speculatively.
- [ ] **N18 — Put drop estimates into `SolveKey` in the same change that first
      publishes one.** Left out because nothing publishes any. The moment Phase 6
      does, a plan cached against yesterday's rates is served as today's — the one
      staleness bug the key's design cannot catch, and it is silent. **In the
      Phase 6 change itself, not after it.** `SolveKey` and `SolveCache` both say
      so in their javadoc; this line is so a session reads it before starting.
- [ ] **N19 — Write `RedisSolveCache` when there is a second node.** Deferred by
      [ADR 0012](docs/adr/0012-the-solve-cache-is-in-process-until-there-is-a-second-node.md);
      the reversal trigger is a second process that can serve the same profile.
      **Also owed before any benchmark against the Phase 8 replicated KV**,
      regardless of node count: a replicated cache measured against an in-process
      map is measuring the network, and ADR 0003 forbids a comparison shaped to
      flatter the hand-built side.
- [ ] **B5 — Wire the real deploy: Vercel and Render.** No longer deferred —
      [D1 is reversed](#d1--deployment-deferred-2026-09-02) and the hosts are chosen. The
      `deploy` job is still `if: false` and stays that way until there is a real
      URL to smoke, which is what this action produces. **Settle the two-origin
      question first**, in D1's reversal note: a Vercel rewrite of `/api/*` to
      Render keeps the same-origin session, CSRF and OAuth redirect the backend
      was built around; two real origins do not. **This is also what unblocks the
      OAuth exchange** — a provider registration needs a redirect URI, and a
      redirect URI needs this. **N24 is done and did not close it**: the sign-in
      it delivered is a development one, and
      [ADR 0017](docs/adr/0017-the-development-sign-in-is-absent-from-the-artifact.md)'s
      reversal trigger is deleting `:modules:identity-dev` once a real provider
      works locally. **This action ends with that module gone**, not merely with
      a URL. It is also where the image build stops being theoretical — Render
      builds from `backend/Dockerfile`, which had never copied `adapters/` and
      was repaired 2026-09-09.

---

#### Phase board

Exit criteria are copied from the plan verbatim. Do not start a phase until the
previous one's criterion is met. The full "Landed" record for closed phases is in
[the archive](docs/history/tracker-archive.md#closed-phases-in-full).

##### Track A — product

Phases 0 to 3 are closed; what stays here is one line each and the qualification
a session would be wrong not to read.

- [ ] **Phase 0 · Ground** — **closed by exception 2026-09-05, box deliberately
      unticked.** Everything landed except the deploy.
      **Not met as written** — [D1](#d1--deployment-deferred-2026-09-02). The box
      gets ticked when, and only when, a real URL answers 200.
- [x] **Phase 1 · Game data foundation** — closed 2026-09-06, CI-confirmed (N10).
- [x] **Phase 2 · Optimizer core** — closed 2026-09-08. **Stopped by its budget, not finished by it** (ADR 0010).
- [x] **Phase 3 · Identity and player state** — closed 2026-09-08; OAuth never exchanged, no two real devices synced.

- [ ] **Phase 4 · Frontend v1 — and launch** — 2.5 weeks — **OPEN 2026-09-09.**
      Landed so far: the app is served, hosting is decided (Vercel + Render, D1
      reversed), a browser has signed in (N24, ADR 0017), and **N25 is closed** —
      the five screens driven in a browser including the overlay, which is the
      second half of the exit below; provenance read back onto the page; the PWA
      loaded with its server killed; and a frontend test suite in CI. **Nothing
      is deployed, so the first half of the exit is untouched.**
      **Since 2026-09-13 the launch title is Punishing: Gray Raven ([D3](#d3--launch-title-swapped-to-punishing-gray-raven-2026-09-13))**,
      so B5 is no longer the only thing in the way. Strangers cannot plan PGR
      without a PGR bundle (**N27**) and fodder in the solver (**N30**). Every
      screen above was built and driven against R1999 data and has never rendered
      PGR.
      Inventory editor built for fast bulk entry, goal picker, plan view with
      per-stage breakdown, offline PWA. Plus catalog browse and search with the
      personalized overlay on every character page — that overlay is the whole
      argument for having a catalog, so it ships *with* it, not after. Launch
      publicly at the end even if it is ugly.
      **Exit:** five strangers complete a plan without asking for help, and a
      logged-in character page shows what that reader is short of.

- [x] **Phase 5 · Gacha engine** — closed 2026-09-12 out of order ([D2](#d2--phase-5-entered-before-phase-4-closed-2026-09-12)); **one banner first-hand (Q4)**, income model and shop exchange are **N28**.

- [ ] **Phase 6 · Drop statistics on Postgres** — 1.5 weeks
      Report submission, Wilson intervals, provenance, abuse controls, estimates
      feeding the optimizer. Built on the boring implementation first — this is
      the interface Track B later swaps.
      **Read before tightening any rule over data already published:** a version
      is immutable and the rules for reading one are not, so a published snapshot
      can stop being loadable without anything having touched it. It has already
      happened here, and the shape of it is worth more than the row —
      [the account](docs/history/tracker-archive.md#a-published-version-that-stopped-being-readable).
      **Exit:** a community-derived estimate supersedes a seeded one in a live plan.

##### The gate

> **Track B starts only when the product is publicly deployed with real users and
> real traffic.** If Phase 4 has not landed, go back and land it. Infrastructure
> built against imagined requirements is a toy; infrastructure built against six
> weeks of your own production traffic is engineering.

**Gate status: CLOSED, and now structurally so — see
[D1](#d1--deployment-deferred-2026-09-02).** Nothing is deployed and nothing is
scheduled to be before Phase 4. Do not open `almanac-store`. When Phase 7 comes
round, re-read D1 and decide deliberately whether Track B on synthetic workloads
is still worth building.

##### Track B — substrate

Scope for each is in [plan.html](plan.html); what matters here is the criterion
that says it is finished, and the one architectural note that shapes the phase.
**None of these may start before the gate opens**, which is why they are a table
rather than the paragraphs the live phases get.

| | Phase | Shape | **Exit** |
|---|---|---|---|
| [ ] | **7 · almanac-store** (LSM), 3w | Behind `DropReportStore`, alongside the Postgres one | Crash-consistency fuzzing survives 10k randomized kills; benchmark vs Postgres published — **including if Postgres wins** |
| [ ] | **8 · almanac-raft** (consensus), 3w | Exposed first as a replicated KV, so it is testable before anything depends on it | 5-node cluster survives repeated leader kills and partitions with no divergent log |
| [ ] | **9 · Solver cluster**, 2w | Replicated job log, leased work, idempotent completion, results over WebSocket | Kill any node mid-solve — no lost solves, no duplicated solves, throughput recorded |
| [ ] | **10 · Chaos harness**, 1.5w | Partitions, pauses, kills, disk corruption; linearizability checking; failing seeds saved as regression tests | Nightly suite green for 7 consecutive nights, **and one real bug found and written up** |

##### Track A — closing

| | Phase | Shape | **Exit** |
|---|---|---|---|
| [ ] | **11 · Reverse: 1999 as the second title**, 2w — *was Punishing: Gray Raven until [D3](#d3--launch-title-swapped-to-punishing-gray-raven-2026-09-13)* | R1999 already has most of what Phase 11 used to cost PGR: an adapter kept as a cross-check, two imported patches, the community benchmark, and one first-hand banner. What it still costs is **first-hand sourcing of the catalog** (ADR 0015) and its own day boundary. Fodder, probabilistic goals and **the time axis's first real data** all moved to the launch with PGR ([why the time axis](docs/history/tracker-archive.md#qualifications-moved-out-of-the-live-tracker-2026-09-11-seventeenth-session)) | R1999 live with **zero game-specific code** in `planner`, `gacha` or `stats` added after PGR launched — and the diff to prove it |
| [ ] | **12 · Hardening and the writeups**, 1w | Tracing, alerting, a backup actually restored from, a load test with published numbers, pre-rendered catalog pages | Restore drill completed from a real backup; catalog pages indexed; three writeups published — the storage benchmark, the consensus verification, the multi-game diff |

---

#### Cut list, in order

Consult this before descoping anything, and record in the session log if a cut
is taken.

1. **Cut first:** Phase 10 as a separate phase — fold minimal fault injection
   into 8 and 9. This loses the strongest evidence, so cut only under real pressure.
2. **Cut second:** Phase 9. Keep `almanac-raft` as a verified standalone
   replicated KV and leave the solver single-node.
3. **Cut third:** Phase 11's real second game — but still prove the abstraction
   against a synthetic test game. That proof is the product thesis.
4. **Never cut:** the Phase 4 public launch, the Phase 2 optimizer, and
   completing at least one of Phase 7 or 8 properly. Half an LSM tree and half a
   Raft is worth nothing; one finished engine is worth a great deal.

---

#### Invariants — do not violate without an ADR

**They live in [CLAUDE.md](CLAUDE.md#non-negotiables) and are not repeated here.**
That file is read at the start of every session by definition, so a second copy
is a second thing to keep in sync and a place for the two to disagree. Two of the
six are enforced by tests — `GameAgnosticismTest` and `ModuleBoundaryTest` — and
those are the ones a change is most likely to trip.

---

#### Key seams (where the two tracks meet)

| Port | Defined in | Boring impl | Hand-built impl |
|------|-----------|-------------|-----------------|
| `DropReportStore` | `modules/stats` | Postgres — phase 6 | `almanac-store` — phase 7 |
| `SolveCoordinator` | `modules/planner` | single-node — phase 2 | `almanac-raft` — phase 9 |
| solve cache | `modules/planner` | Redis — phase 2 | replicated KV — phase 8 |

Keep these interfaces narrow. A port shaped to flatter the hand-built side
proves nothing.

---

#### Deviations from the plan

Record every departure here with its cost, so nobody has to reconstruct the
reasoning later — including you, in month six.

##### D3 · Launch title swapped to Punishing: Gray Raven (2026-09-13)

**The plan launched on Reverse: 1999 and brought in Punishing: Gray Raven at Phase 11
as proof the model is game-agnostic. The maintainer swapped them.** The trigger
was a question: is this a clone of Kornblume? Checking Kornblume's source to
answer it showed that **`docs/prior-art.md`'s central claim was wrong**. Kornblume
*does* solve per player, with a linear program in the browser (GLPK, since
2024-03). It also imports inventories by OCR and syncs through Google. So on R1999
the product overlaps an established, deployed tool almost feature for feature, and
the wedge that is left is narrower: whole runs, explanations, sample-size-aware
yields, provenance, and two games on one model. A GitHub search found **no open
PGR planner of this kind**. That is not proof none exists, since closed sites and
spreadsheets would not show up. The maintainer plays PGR on Global, which is what
makes first-hand sourcing possible at all.

**What it cost.** Everything built so far was built and verified against R1999:
the adapter, two imported patches, the nine-agreement benchmark, the first-hand
banner, and every screen. **PGR starts with no data in the repository**, no
benchmark to compare against, and gacha fixtures that are all second-hand.
**Fodder, probabilistic goals and the first real calendar** move from Phase 11 to
before launch (**N30**, **N20**). So the launch gets further away, not closer, and
Phase 4's exit now waits on **N27** and **N30** as well as **B5**. The README
opening, `plan.html`'s title facts and wedge card, and `prior-art.md` §1 were
corrected the same day.

**What it bought.** A launch that is not a second copy of a tool players already
use. It also makes the abstraction's hardest shapes load-bearing from day one, not
bolted on at the end. Phase 11's proof survives with the roles reversed: R1999
has to go live with no game-specific code added after PGR.

**Reversal trigger — and half of it can no longer fire** (2026-09-18). It was:
the *timed* PGR reading in **N27** shows first-hand sourcing is not feasible for
one maintainer, or an established PGR planner that solves per player turns up.
**The timed pass was cut by the maintainer**, so the feasibility half has no
measurement behind it and cannot trip on evidence — only on somebody's judgment
that the authoring has stalled. The second half stands unchanged. Either one puts
R1999 back on top, with the Kornblume overlap stated honestly.

##### D2 · Phase 5 entered before Phase 4 closed (2026-09-12)

**The rule broken is this file's own:** do not start a phase until the previous
one's criterion is met. Phase 4's needs a deployment (**B5**) and **N27** is the
maintainer's, so the choice was put to them and Phase 5 taken knowingly.
[In full in the archive](docs/history/tracker-archive.md#d2--the-full-account).

**It bought** the one large piece of Track A a session can finish alone; **it cost
nothing on the launch**, which B5 still blocks. Phase 5 got **no bean, no route,
no screen** and an income model left unwritten rather than guessed (**N28**).

**Reversal trigger:** none — a phase cannot be un-entered. The one that matters
is on the *next* phase: **do not take Phase 6 early on this precedent.** Phase 6
feeds estimates into the optimizer and needs users; Phase 5 was safe to take
early only because its criterion is a proof about a model rather than a
measurement of real traffic.

##### D1 · Deployment deferred (2026-09-02)

**REVERSED 2026-09-09.** Heading kept verbatim so every link to it still lands.

**Was:** no money spent, so no hosting; the `deploy` job is `if: false`.
**Now:** both halves of its own reversal trigger fired at once — Phase 4 reached,
and a free tier accepted. **Vercel for the frontend, Render for the backend.**
Still no money, so the premise stands; what changed is that a free tier is
acceptable. [The entry in full is in the archive](docs/history/tracker-archive.md#d1--the-deferral-in-full).

**What it cost is what the next sessions buy back:** Phase 0's box is unticked, the
Track B gate has no meaning without real traffic, and deploy problems really were
discovered late — the Dockerfile broken since Phase 1 with nobody noticing is that
cost arriving.

**Nothing is deployed yet.** The decision is made, the wiring is not — **B5**,
which carries two consequences to settle before writing any of it: **one origin or
two** (a Vercel rewrite of `/api/*` preserves the same-origin cookie, CSRF and
redirect the backend is built around; two real origins do not, and the hop sits on
a two-second solve promise), and **the free tier sleeps** (a cold start is tens of
seconds against a budget of two — warm-up ping, honest loading state, or accept
it, decided before five strangers meet it).

---

#### Environment notes (this machine only)

Not deviations — nothing about the design changed. These are local facts that
cost time to rediscover.

##### E1 · Avast intercepts TLS, so Gradle cannot fetch new dependencies

**Live again as of 2026-09-08** (it was turned off on 2026-09-05 and is back).
Avast's HTTPS scanning re-signs every TLS connection with its own root CA.
Windows trusts that CA; **the JDK's `cacerts` does not**, so any Gradle download
of an artifact not already cached fails with `PKIX path building failed`. It is
invisible until a **new** dependency is added, because everything already in
`~/.gradle/caches` keeps working.

```bash
cd backend && ./gradlew -Djavax.net.ssl.trustStoreType=Windows-ROOT build
```

This points the JVM at the Windows certificate store. It grants **no new trust**
and modifies nothing. Not committed to `gradle.properties`, because
`Windows-ROOT` does not exist on the Linux runner.

##### E2 · Docker Engine 29 refuses Testcontainers' API version

**Fixed and committed** — `systemProperty("api.version", "1.44")` on every `Test`
task in `backend/build.gradle.kts`, on the *task* and not as an environment
variable, which is the whole fix. Only worth reading if it comes back:
[the archive](docs/history/tracker-archive.md#e2--the-full-account).

##### E4 · A dead Docker engine looks exactly like a slow one

Docker Desktop takes minutes to start here and `docker info` hangs rather than
failing while it does — **but waiting is only sometimes the answer**. **Do not use
`com.docker.service` as the test** (corrected 2026-09-09): on the WSL2 backend the
Windows service is not the engine, and a session has read `Stopped` while the
engine answered and Testcontainers ran all day. **Ask the engine:**

```bash
docker info --format '{{.ServerVersion}}'
```

A version means it is up, whatever the service says. If that hangs or errors,
*then* look at the service: `Stopped` there is a reason to ask a human, because
a session cannot start it — `Start-Service` from a non-elevated shell fails with
*Cannot open com.docker.service service on computer '.'*.

**But a session can start the *application*, and that is enough** (2026-09-18).
The engine was down, the API pipe did not exist, and this brought it up without
elevation and without a human — running in well under a minute:

```powershell
Start-Process 'C:\Program Files\Docker\Docker\Docker Desktop.exe'
```

So the escalation is: ask the engine, and if it is dead, start Docker Desktop and
ask again. Only then is it a human's problem.

---

#### Open questions

Carry these forward until answered; strike through with the answer when resolved,
then move the entry to
[the archive](docs/history/tracker-archive.md#answered-questions).

- **Q5 — Is our "3.5" the same 3.5 anyone else means?** *Open for the existing
  data; **dissolved for everything after ADR 0015**.* `fetch-upstream.sh` pins a
  commit dated **2026-03-17** while Global 3.5 ran **2026-05-28 to 2026-07-02** —
  two and a half months apart, most likely because Kornblume tracks **CN**. So
  everything here labelled "3.5" is probably CN 3.5, and the labels may not mean
  what a reader assumes. It does not invalidate the nine agreements (that guide
  was already known to be 2.7 against a 3.3 sample). **Self-sourcing ends the
  ambiguity by construction** — you know which region and patch you read, because
  you read it — so this is a caveat on the old numbers rather than a question
  about the new ones. Until then do not write "3.5" publicly without saying which.
- **Q4 — Rate verification.** *Answered for one R1999 banner, 2026-09-13; open for
  the rest.* The maintainer's screenshots of the Global client's rules screen for
  *A Newly Hatched Chapter* ([the note](docs/game-facts/reverse-1999-summon-disclosure.md))
  **confirm the pity curve twice.** The screen states it word for word, and it
  also prints **an overall 6★ rate of 2.36%, which the curve reproduces as
  2.3592%.** A curve one pull early or late would print 2.38% or 2.34%.
  **They contradict the featured rule**: 50/50 with a guarantee, where
  `reverseDebut` hands her over outright. **They also found a Limited Shop exchange
  that no engine models**: one Cassette of the Lost per summon, 200 for her. That
  is past the 140-pull worst case, so it never binds for one copy, but with no limit
  until Portrait 5 it cuts **six copies from 840 pulls to 560**.
  Still second-hand: the beginner banner and all of Gray Raven. Drop rates are a
  separate question, closed with the answer no, in
  [the drop disclosure note](docs/game-facts/reverse-1999-drop-disclosure.md).

---

#### Session log index

Full entries are in [the archive](docs/history/tracker-archive.md#session-log),
newest first. **Write the entry there; add its line here.**

| Date | Session | What it was |
|---|---|---|
| 2026-09-18 | twenty-third | **The first first-hand bundle exists — draft 0, nine facts, not published.** The 545-line PGR reading became `data/bundles/punishing-gray-raven-steering-by-light.json` and went through *validate, preview, ingest*; the provenance breakdown prints no *NOT ours to publish* line for the first time outside a synthetic fixture. **Nine facts is the finding: three blocks were read clearly and the format refused them**, and all three are *required* fields the reading could not fill — the Themed Construct banner (`PityRule.hardAt` is an `int`, PGR draws it 80–100: **N31**), the weapon's Overclock and Harmony recipes (an `Upgrade` names an entity and **the weapon was never named**), and **every currency** (`Item.rarity` is required and nothing read grades one: **N28**). R1999 never contradicted any of those assumptions; PGR contradicted all three on day one. Fodder authored with the band on `consumesCategory` and **inert**, because the fodder item's name was not recorded. **E4 gains a way out:** a session cannot start `com.docker.service`, but `Start-Process 'Docker Desktop.exe'` brings the engine up unelevated. **790 → 826 lines, up 36 and the sixth session running** — decide whether the 550-line rule is wrong or whether *Current state* must stop carrying prose that belongs in the notes it links to |
| 2026-09-18 | twenty-second | **PGR was read (N27 blocks 1–3), and the client contradicted the guides twice.** Both banner archetypes complete and paired; the featured rule is **per-banner**, and below 100% the recovery is published as the **Calibration System**, inherited across pools — **R1999 clears where PGR inherits**, so the two games now disagree on what `PityScope` is for. **PGR events are not farms and have no shops**, so **N30 loses its shop half and probabilistic goals and gains an event shape `EnergyMip` cannot express**. **PGR's advertised 1.90% does not reproduce** from its own numbers, unlike R1999's 2.36%. **N28 unblocked**; Serum pinned at 240 ml, +1/6 min, cap = exactly one day. **Block 4 cut by the maintainer.** Nothing authored into a bundle yet |
| 2026-09-14 | twenty-first | **The PGR survey D3 was missing, and the plan's wedge rewritten from it.** Every tool found was opened rather than judged from a snippet; nothing found plans PGR farming from an inventory. The finding with teeth was in the *guides* — that PGR is farmed through event stages whose currency buys materials in event shops — which **N30 was scoped from and which the twenty-second session found false against the client** |
| 2026-09-13 | twentieth | **The launch title swapped to Punishing: Gray Raven ([D3](#d3--launch-title-swapped-to-punishing-gray-raven-2026-09-13))**, because asking "is this a Kornblume clone?" found `prior-art.md` wrong: **Kornblume solves per player with GLPK in the browser**, and has since 2024. README, plan and prior-art corrected; N27 retargeted to PGR; **N30** puts fodder in the solver. Before that, **Q4 answered for one banner, off the client itself:** the maintainer's screenshots of the rules screen confirm the pity curve twice — stated word for word, and an overall 6★ rate of **2.36% the curve reproduces as 2.3592%**, a check a curve one pull off would fail — **contradict the featured rule** (50/50 with a guarantee, not outright), and **find a Limited Shop exchange no engine models**. First first-hand fixture and five tests; 96 questions, worst gap still 0.110. An AI web-search answer was offered first and not recorded. **N5: every CI action onto Node 24, zero annotations, green on run `34731880389`** ([PR #20](https://github.com/kietnt4412/storm_almanac/pull/20)). Taken to the latest majors (checkout v7, setup-java v6, setup-node v7, upload-artifact v7) — **except `gradle/actions`, held at v5** because v6 needs Gradle's Terms of Use accepted, which is the maintainer's call, and v5 has been frozen since February. **The green run executed no tests**: a YAML-only change leaves every Gradle input identical, so all seven test tasks came from cache. N27 handed to the maintainer as a reading form, deliberately without the fixture values so the reading stays blind. PR #19 already merged and `dev` had no open PR — the fifth time. 749 → 769 lines |
| 2026-09-12 | nineteenth | **Phase 5 closes on its criterion, out of order and on purpose (D2).** Two engines sharing three branches and nothing else: an exact chain and 500 000 seeded trials, **worst gap 0.110 percentage points over 84 questions against a criterion of 0.300, at 1.84 standard errors** — and both games' published rates out of `BannerModel` alone. **The plan's own two numbers do not fit together:** at its 100 000 trials, 0.3 points is under two standard errors, and the cross-check failed on its first run with both engines correct, so the trial count now follows from the tolerance. Four decisions in [ADR 0018](docs/adr/0018-the-gacha-engines-answer-one-question-about-one-rarity.md) — `PullResult` can carry no `Rarity`, a floor reaching the headline is refused rather than ignored, `PityState` counts losses instead of flagging one, and the chain reads the mass that never arrived so a wall is exactly 1.0. Two of the first failures were **in the tests**: a 30-pull wall is 0.7030 and not certainty, because a wall guarantees the rarity and not her. 324 tests locally and 308 on CI, **green on run `34695206362`** ([PR #19](https://github.com/kietnt4412/storm_almanac/pull/19)). **PR #18 was already merged and `dev` had no open PR — the fourth time, caught by looking** |
| 2026-09-12 | eighteenth | **N4: ADR 0007's own open gap becomes a failing build.** `EntityKindBoundaryTest` allows a read of `Entity.kind` in `gamedata`, `api` and `adapters` and denies it everywhere else — an allowlist, because the denylist of three modules the ADR names is what would pass vacuously again. Bytecode rather than a source scan, because a grep cannot tell `entity.kind()` from `change.kind()`. Verified by putting the defect back in both spellings. **A condition for method references was written, measured, and deleted** — `accessTargetWhere` already catches them, and the measurement is in the javadoc so nobody adds it back. ADR 0007 not edited: the decision did not change, only the account of what enforces it. Two lessons moved to the archive and two struck-through entries closed out, 747 → 742 lines (and the previous session's count was seven short — measure it). 278 tests, CI-confirmed on run `34691539392` ([PR #18](https://github.com/kietnt4412/storm_almanac/pull/18), which now carries N25 *and* N4) |
| 2026-09-11 | seventeenth | **N25's three debts paid, so it closes.** Provenance is read back out through a *second* port — the solver still cannot see it — and both catalog pages now say where their numbers were read, which on today's data reads "invented for this project" or "nobody recorded it", and that is the feature working. The PWA was loaded with **the origin server killed** rather than with a flag flipped. The frontend gets 15 tests and a written argument for what they are and are not for. A browser found one more defect on the way: a new bundle reading an old API's response **blanked the whole page**, which is B5's two-host deploy window in miniature. 277 backend tests, 15 frontend, CI-confirmed on run `34597211345` ([PR #18](https://github.com/kietnt4412/storm_almanac/pull/18)) |
| 2026-09-11 | sixteenth | N25: the five screens exist and a browser has driven all of them — and **a logged-in character page now says what that reader is short of**, which is half of Phase 4's exit. The offline outbox gives N23's merge a second real clock: a stale edit lost on purpose and the interface named the key. Three routes were missing underneath (`/api/games`, `/items`, `/shortfall`) and four defects turned up that only a browser could find. Owed: provenance read-back, an offline *load*, and any frontend test at all. 274 tests |
| 2026-09-09 | fifteenth | N24: a browser signs in, reads its account, creates a profile and signs out. The way in is a Gradle module the deployable jar does not contain — **[ADR 0017](docs/adr/0017-the-development-sign-in-is-absent-from-the-artifact.md)**, absence rather than configuration, with `DeployableJarTest` reading the artifact to prove it. On its first run it found a defect two phases old: **the CSRF cookie was never issued**, so any browser's first write would have been refused. Also the first authenticated request over a socket, and a Dockerfile broken since Phase 1. 264 tests locally, CI-confirmed on run `34327487736` |
| 2026-09-09 | fourteenth | N26 answered and the answer was no — the game grades a drop `Fixed`/`Common`/`Possible` and prices only the first, so 764 of 779 drop facts still have to be counted; gacha rates *are* disclosed, which is half of Q4. Then N27 split in two: the authoring is the maintainer's by ADR 0015's own integrity rule, so the session built the mechanism instead — **[ADR 0016](docs/adr/0016-provenance-is-a-property-of-the-data.md), provenance is a field and `publish` enforces it**. 257 tests, 0 failed, 0 skipped locally |
| 2026-09-09 | thirteenth | N23 pays Phase 3's sync debt (ADR 0014: last-write-wins per key, a clock that outlives its value, a watermark a failing test found). Phase 4 opened — the frontend served for the first time, which found a 401 where a 404 belonged. Then the largest decision of the session: **go first-hand on game data** (ADR 0015), closing Q2, Q3, F1 and F2 — ~3 300 facts a patch, and a bootstrap problem to solve |
| 2026-09-08 | twelfth | Phase 3 opened and closed: V5 gives identity and player their schemas with four decisions in its header, sign-in creates the account while the principal is built, and a plan is computed from goals nobody handed the optimizer — 228 tests, and sync is the piece of the scope that was not built (N23) |
| 2026-09-08 | eleventh | N14: the plan gets a calendar — the horizon as a scalar rather than an index (ADR 0013), p95 held at 1 807 ms, the two objectives finally disagree (0 energy / 28 days against 370 / 2) — then the maintainer supplied what the game actually does, and two of the answers were corrections |
| 2026-09-08 | tenth | N15: a solve is cached on its key (1 806 ms → 2 ms on the real patch) and a queue runs it once, CI-confirmed on `3fdc277`; and the read that took shops out of N14 |
| 2026-09-08 | ninth | N16 and N17: CI confirmed the tree and Phase 2 closed; then drop yields learned how many runs they were measured over, and agreement with the community went from five to nine |
| 2026-09-07 | eighth | N12: the community's answers — and the discovery that the adapter had been reading a stage table missing two thirds of the game |
| 2026-09-07 | seventh | N11 and N13: the optimizer answers a real goal set and says how much it does not know (ADR 0010) |
| 2026-09-06 | sixth | N9 and N8: the pipeline meets somebody else's data, and ADR 0009 settles how |
| 2026-09-06 | fifth | N7: the API answers both of Phase 1's exit questions, attributed and version-pinned |
| 2026-09-05 | fourth | N6: the schema holds data — and could not be deleted, which V3 fixed |
| 2026-09-05 | third | Phase 1 opens: the canonical schema |
| 2026-09-05 | second | The equipment question, answered from the code (ADR 0007) |
| 2026-09-05 | first | The app runs; booting it found a 401 on the one endpoint it had |
| 2026-09-02 | four entries | Scaffold, first green build, first CI runs, prior art read, hosting deferred (D1) |

</details>

---

## D2 — the full account

Compressed out of the live tracker on 2026-09-18 (twenty-third session); the
operative half — the trigger on Phase 6 — stays there.

### D2 · Phase 5 entered before Phase 4 closed (2026-09-12)

**The rule broken is this file's own:** do not start a phase until the previous
one's criterion is met. Phase 4's criterion needs five strangers completing a plan
against a deployment, which is **B5**, whose last step needs accounts and an OAuth
registration no session has. **N27** above it is the maintainer's by ADR 0015's
integrity rule. So the choice was put to the maintainer — Phase 5, B5's wiring, or
**N5** — and Phase 5 was chosen deliberately, knowing the order.

**What it cost:** Phase 4 is still open and B5 is still the only thing between it
and its criterion, so this bought no progress on the launch. The gate is
untouched. **What it bought:** the one large piece of Track A a session can finish
alone and verify without a browser, a provider or a person reading a game screen.

**What to watch.** The risk in taking a phase early is building against imagined
requirements, which is the whole argument for the Track B gate — so note what this
phase did *not* get: no bean, no route, no screen, and an income model left
unwritten rather than guessed at (**N28**). The engines answer questions a test
asks. **Nothing has asked them on behalf of a player**, and that is the same
qualification `SolveCoordinator` has carried since Phase 2.

**Reversal trigger:** none — a phase cannot be un-entered. The trigger that
matters is the one on the *next* phase: **do not take Phase 6 early on this
precedent.** Phase 6 feeds estimates into the optimizer and needs users, so
starting it without them is exactly the mistake the gate exists to prevent, and
Phase 5 was safe to take early only because its exit criterion is a proof about a
model rather than a measurement of real traffic.

---

## D3 — the full account

Compressed out of the live tracker on 2026-09-25 (forty-second session), to make
room for D5 when the tracker stood at 550 lines. Verbatim as it stood; the
operative half — the reversal trigger that can still fire — stays there.

### D3 · Launch title swapped to Punishing: Gray Raven (2026-09-13)

**The plan launched on R1999 and brought PGR in at Phase 11; the maintainer
swapped them**, after reading Kornblume's source to answer "is this a clone?"
showed **`prior-art.md`'s central claim was wrong** — it *does* solve per player,
in the browser, since 2024-03, with OCR inventory import. The wedge left is
narrower: whole runs, explanations, sample-size-aware yields, provenance, two
games on one model. **Cost:** PGR starts with no data, no benchmark and
second-hand gacha fixtures, and fodder and the first real calendar move from
Phase 11 to before launch (**N30**, **N20**) — the launch gets further away, not
closer. **Bought:** a launch that is not a second copy of a tool players already
use, with the abstraction's hardest shapes load-bearing from day one.

**Reversal trigger, and half of it can no longer fire** (2026-09-18): the *timed*
PGR reading shows sourcing is not feasible for one maintainer — **that pass was
cut**, so this half has no measurement behind it — or an established PGR planner
that solves per player turns up.

---

## The line-count ledger

Moved out of the tracker's *How to use this file* on 2026-09-18 (twenty-third
session), where it had grown into five sessions of self-accounting about a rule
that fits in three lines. The rule stays there; this is what each attempt
actually did.

**747 on 2026-09-11 (seventeenth).** The first measurement after the split.

**742 on 2026-09-12 (eighteenth).** Two lessons moved here, two struck-through
entries closed out — and the previous session's carried-forward number was seven
short, which is why the rule now says to run `wc -l`.

**749 on 2026-09-12 (nineteenth).** Ten blocks cut: the *Current state* rows for
Phase 1, the parser adapter and provenance that three sessions had flinched from,
the Status bullets for the offline outbox and N4 that a table row already
carried, two closed next actions, the benchmark re-argument, D1's restatement and
E4's ([the five largest](#rows-compressed-out-of-current-state-2026-09-12-nineteenth-session)).
**It still ended seven lines up**, because it closed a phase, opened a deviation
and added an action in the same session. By the file's own rule that is moving
the problem, and the flat number is the only honest way to say so.

**769 on 2026-09-13 (twentieth).** N5 out, the Phase board's closed entries cut
to one line each, the remote bullet halved. Still ~170 over.

**790 on 2026-09-18 (twenty-second).** Up 21. The compression it bought **did not
show at all**: the *Game data API* and *Frontend* rows — the candidates the
previous entry had named — were each a **single very long line**, so summarising
them and linking here removed ~2 700 characters and zero lines. Bytes fell
71 045 → 70 369 while the line count held. That is the observation that survives:
`wc -l` cannot see the row nobody can read in one breath.

**826 on 2026-09-18 (twenty-third), and then 550.** The session added ~80 lines
of findings against ~50 moved out, which is the sixth miss in a row; the
maintainer then asked for the file itself and it was **rewritten rather than
trimmed a seventh time** — 826 → **549 lines, 74 604 → 39 981 bytes**. What the
rewrite actually did is in that session's log entry below, and the file it
replaced is [here](#the-tracker-as-it-stood-before-the-2026-09-18-compression),
verbatim. **The ledger's conclusion, after six failures and one success:
trimming cannot beat a document that has to absorb every session's findings.
Rewrite a section instead.**

**549 → 551 → 549 on 2026-09-19 (twenty-fourth).** N27 left and N32 arrived at roughly the same length; one line over, closed by tightening N30 and the Phase 4 paragraph rather than dropping anything, and the index line brings it to 550. The rewrite is holding, one session in.

**549 → 550 → 549 on 2026-09-19 (twenty-seventh).** N32 (3) left and its finding took the room back; the index line was paid for by tightening the PGR status bullet.

**549 → 550 → 549 on 2026-09-19 (twenty-eighth).** N32 (4) closed; the index line was paid for by rewriting N32's own entry one line shorter.

**549 → 606, unrecorded, over sessions twenty-ninth to thirty-fifth.** Seven
sessions, five ADRs, four closed actions and no ledger entry for any of them. The
rule was never argued with; it was simply not run, and `wc -l` is one command.
**The thirty-fifth left the file 56 lines over its own limit and said nothing**,
which is the failure mode the rule exists to make visible and did not. Worth
naming because the rewrite *was* holding while anybody was measuring: three
sessions in a row landed on 549.

**614 → 550 on 2026-09-21 (thirty-sixth): Status rewritten, and the first honest
accounting of how it works.** The rewrite of *Status* — the section that had
become a narrative, carrying every sequence's publication timestamp and an index
of five ADRs with their migrations — is
[here](#what-status-carried-until-the-2026-09-21-rewrite), and **on its own it
saved two lines.** 95 → 93. That is the finding, and it is the same one the
twenty-second session recorded and nobody acted on: *moving prose to the archive
and writing shorter prose in its place does not change `wc -l`, because the
prose rewraps to the same height.* **What actually moved the number was
rewrapping** — the file's paragraphs were set at ~78 columns and its table rows
at no limit at all, so eight of its densest blocks were reflowed to ~118 and the
file lost 62 lines without losing a word. **So the rule as written is only half
right.** "Rewrite a section, do not shave it" is the right instruction for
*legibility* and it is what made `Status` readable again; it is **not** what
holds the line count, and a session that follows it expecting the number to move
will be disappointed by 60 lines. The cheap lever is the column width, it is
worth about 12% of the file, and **it can only be pulled once** — after which
the only thing left is genuinely carrying less.

**555 on 2026-09-25 (forty-second), and carried less.** D5 went in at 550, and
moving D3's full text here was not enough. So the tracker stopped carrying two
things that already lived here in substance: the ledger's own history inside the
line-limit rule, and the tracker's compressed D2. Both are kept below as they
stood.

The line-limit rule, verbatim until 2026-09-25:

> - **Keep it under 550 lines, measured with `wc -l`** rather than carried forward.
>   Six sessions trimmed and it still went 747 → 742 → 749 → 769 → 790 → 826
>   ([the ledger](docs/history/tracker-archive.md#the-line-count-ledger)): trimming
>   cannot beat a document that has to absorb every session's findings, so **the
>   twenty-third rewrote it instead. If this file passes 550 again, rewrite a
>   section — do not shave it**, and suspect the row nobody can read in one breath,
>   which `wc -l` cannot see.

The tracker's D2, verbatim until 2026-09-25 (the fuller original is
[above](#d2--the-full-account)):

> ### D2 · Phase 5 entered before Phase 4 closed (2026-09-12)
>
> **The rule broken is this file's own.** Phase 4 needs a deployment (**B5**) and
> **N27** is the maintainer's, so the choice was put to them and Phase 5 taken
> knowingly: it bought the one large piece of Track A a session can finish alone and
> cost nothing on the launch. [In full](docs/history/tracker-archive.md#d2--the-full-account).
> **Reversal trigger:** none, a phase cannot be un-entered. The one that matters is
> on the next: **do not take Phase 6 early on this precedent** — it needs users,
> where Phase 5's criterion was a proof about a model.

**Then 551 the same day, after the third run's S9 and S10 went in, and 549 after carrying less:**
the unverified bullet on naming stopped retelling it. As it stood:

> slack covers one more). The stage was always there; nobody had driven such a goal. **The plan
> page's rows stopped being raw ids on 2026-09-26** (S5). The naming half is closed:
> a `progress:` line rendered as its bare slug beside a properly named `Cogs` until 2026-09-22
> (N37, ADR 0028), and a `choice:` line's on 2026-09-24, named by its prices rather than by
> upgrade ids. Every R1999 catalog and drop number in this file comes from Kornblume.

**550 on 2026-09-26 (forty-fifth), measured with `wc -l`.** S7, S8 and the four smaller
hesitations moved here verbatim under *Completed next actions*; the rehearsal
list shrank to S6 and a pointer to the third run.

**555 on 2026-09-26 (forty-fourth), and carried less again.** Track C and D6
went in at 549 alongside S6–S8, with S1–S5 and the answered Q6 already moved
here. Two more finished things left the tracker, kept below as they stood.

The header bullet on the tracker's own rewrites, verbatim until 2026-09-26:

> - **Rewritten four times when it passed 550 lines** — end to end
>   [2026-09-18](docs/history/tracker-archive.md#the-tracker-as-it-stood-before-the-2026-09-18-compression) (826),
>   *Status* [2026-09-21](docs/history/tracker-archive.md#what-status-carried-until-the-2026-09-21-rewrite) (614),
>   the session index 2026-09-22 (566) and again 2026-09-24 (561). Anything it no longer carries is in the archive verbatim — go and
>   decide whether it is still operative rather than assuming it was lost.

Phase 4's closing condition on the board, verbatim until 2026-09-26 (met, so
the tracker now says only that):

> **Closing condition, set 2026-09-20:** the exit is necessary and not
> sufficient — N30, N33, N20 and B5 are each done or explicitly cut before
> this box is ticked, with the cut recorded in the session log. **All four are
> done** (B5 with one cut, ADR 0030).

---

## The PGR status bullet until 2026-09-25

Rewritten in the forty-second session when S4 had to be recorded with the
tracker at 549 lines. Verbatim as it stood:

> - **PGR is read first-hand** ([the note](docs/game-facts/punishing-gray-raven-research-disclosure.md)) and
>   [its bundle](data/bundles/punishing-gray-raven-steering-by-light.json) is **published at sequence 10** (2026-09-24, **Global 4.8.0
>   "Anchored in Faith"**) — 250 facts over eleven provenance entries, **three constructs on one S-rank ladder**, one weekly ladder, a
>   **05:00 UTC** reset, and since 2026-09-22 a **pull price**. Every sequence has published and
>   read back as *no changes*, sequence 10 included — in both databases. **Sequence 7 closed N37 and N28's modelling half** and is the
>   first sequence carrying something that is not a fact at all (the EXP pool names, ADR 0028). What it holds is
>   [in the table below](#what-the-next-work-touches). **Reading the client overruled the guides four times** — a
>   per-banner featured rate, events with no shops, a drawn wall, a `PityScope` inheriting across pools where R1999
>   clears — the standing argument for the client over a wiki
>   ([the four](docs/history/tracker-archive.md#what-status-carried-until-the-2026-09-21-rewrite)).

---

## What *Status* carried until the 2026-09-21 rewrite

The four *Status* bullets the thirty-sixth session rewrote, as they stood.
**Kept verbatim**, because the rule this file runs on is that nothing is deleted
and a later session decides for itself whether something is still operative.
Most of what is below is *true* — it is here because it stopped being something a
session needs before it starts work, not because it stopped being so.

> - **PGR is read first-hand, and its bundle plans, EXP and one gate included
>   (ADR 0019).** Simulated Battlefield pays Score, and Score buys every material
>   and Pod. **Sequences 4, 5 and 6 are published** (2026-09-20T00:13:59Z,
>   2026-09-21T01:11:15Z and 2026-09-21T02:49:27Z, all read back as *no
>   changes*): a reader who says they clear the weekly Phantom Pain Cage is
>   planned **Evolve to SS in 63 days and no Serum** and one who says nothing is
>   refused *by name* (ADR 0022), and the game declares its **05:00 UTC** reset.
>   The rest of the prices are in the table below. **Since sequence 6 the level
>   ladder is priced end to end and every one of the thirteen Promote gates is a
>   `requires`** (N33, 2026-09-21): thirteen cumulative EXP figures from 1 000 to
>   497 000, each pinned by the Pod selection that reaches it and the one 1 000
>   lower that does not. **A partial Promote plan used to be too cheap** —
>   stopping at step 7 paid Cogs and no EXP at all — and is not any more. What the
>   client overruled in the guides: the featured rule is a **per-banner rate**;
>   **events have no shops** and pay one-time grants, which the plan reports as a
>   deadline (ADR 0024); the Themed pool's wall is **drawn** (ADR 0023); and its
>   **`PityScope` inherits Calibration across pools**, where R1999 clears — the
>   two published games disagree on the one question that enum exists to answer.
> - **Five decisions closed since 2026-09-20 — 0023 (`V11`), 0024, 0025 (`V12`),
>   0026 and 0027 (`V13`). Each ADR is its own account; read it rather than a
>   summary.** What still bites elsewhere in this file: both PGR pity archetypes
>   are expressible and **neither is authored** (**N28**); **`Availability.opensAt`
>   is read by nobody**, deliberately, the last of that record nothing reads;
>   **`GameAgnosticismTest` is blind to a constant that is right for no game** —
>   `UTC` is not a game name, and a javadoc caught 0025's bug, no test did; and
>   **R1999's day boundary is Phase 11's problem**, with a second-hand number, a
>   rotating stage table and nine benchmark agreements at stake.
> - **Going first-hand on game data**, 2026-09-09 (ADR 0015), superseding 0009 and
>   closing Q2, Q3, F1 and F2. Kornblume is unlicensed. **Nothing is deleted yet
>   and the order matters:** the adapter stays as a never-shipped cross-check until
>   a replacement exists, because removing it first leaves the project with no real
>   data at all. **The cost on patch 3.5:** ~2 700 catalog facts, and **595
>   drop-rate facts** — the bootstrap problem.

Three of those four are carried forward in the rewritten section in shorter
words. **The one to come back here for is the second bullet's list of what the
PGR guides got wrong**, which is the only place it is written down outside the
research note, and which is the standing argument for reading the client rather
than a wiki.

---

## Session index rows collapsed on 2026-09-24

*The two thirty-fourth rows joined these later the same day, in the thirty-ninth session, when adding its row took the tracker to 551.*

*The thirty-fifth row joined them in the forty-first session, for the same reason and at the same count, 551.*

The tracker reached **561 lines** in the thirty-eighth session, eleven over its
limit, before that session's own row was added. Its rule says rewrite a section
rather than shave one, so the session index was rewritten a second time: every
row older than the thirty-fourth session became one row. **The rows are kept
here verbatim** — the 2026-09-22 collapse of the first nineteen did not keep
its one-liners, and this file's promise is that nothing is deleted. Each still
has its full entry under [Session log](#session-log).

| Date | Session | What it was |
|---|---|---|
| 2026-09-21 | thirty-fifth | N36 closed, and it was not the paperwork it was scoped as. Every load-bearing figure held — nine agreements, 3 880 against 4 017, 419 tests and all 16 gated ones run — but **following the documented workflow proved nothing**: the property never reached the test worker and the snapshot directory was not a task input, so a fetch left `:app:test` `UP-TO-DATE`. The second is the identical bug to `data/bundles`, ten lines above its own fix. Both fixed, both proven by measurement; numbers now dated in `docs/benchmarks/snapshot-gated-runs.md` |
| 2026-09-21 | thirty-fourth (cont.) | N34 closed: a roster entry holds a set of states (ADR 0027, `V13`). A reader at `promote-6` who says they are also at `level-80` pays 127 500 Cogs and **no EXP** where they were charged 90 000 over six steps. The merge unit stays the entity. **The frontend shipped wrong twice with 16 green tests each time** — a `select multiple` nobody could use, then chips styled as buttons the maintainer looked straight at and did not see. Two live claims died: a `progress:` line *has* rendered (as a bare slug), and PGR *has* been rendered, so N35 was rescoped rather than ticked |
| 2026-09-21 | thirty-fourth | B6 closed the day after it was written: CI triggers on a push to `dev`, proven by a run on `cfa6fe4` with no PR open. The session-start check found the trap live — the previous session's own commit had sat on `dev` unbuilt. The concurrency group stays keyed by ref *on purpose*: deduping push and PR would let a push cancel the check the PR needs green. Deploy must be gated to `main` when B5 turns it on |
| 2026-09-21 | thirty-third (cont.) | PR #33 opened and green. The deferred-defect list audited into *Next actions* as **B6, N34, N35, N36** — every one was already described somewhere in this file and none was an action anybody could pick up. One archived qualification found stale: a lifetime purchase limit has been expressible since ADR 0020 |
| 2026-09-21 | thirty-third (cont.) | The roster flaw N33 widened, half closed the same day (ADR 0026): a crossed gate is a reached state, so `achieved` credits the `requires` of every upgrade behind the player. A reader at Promote 6 pays 90 Serum for step 7 where they were billed 180. No migration, no wire change — the other half, a reader *behind* the gate, still wants a set of states on `Roster` |
| 2026-09-21 | thirty-third | N33 closed: the level ladder is priced end to end and all thirteen Promote gates are `requires`. Twenty-two Level Up previews on a Lv 1 construct, nothing spent and nobody levelled; thirteen cumulative figures, each pinned by the selection 1 000 below it falling short. The track became a chain because spokes from `level-1` would double-charge. Sequence 6 published 2026-09-21T02:49:27Z and read back as *no changes*; 411 tests, 0 skipped |
| 2026-09-21 | thirty-second | N20 closed: the day boundary is a property of the game (ADR 0025, V12). A zone and an hour on `Game`, one `DayOfWeek` moved and no row written; null is *unstated*, not midnight. The preview found a sixth piece the plan never named — `Facts` flattened `Game` not at all, so the sequence that declares a boundary read as *no changes*. PGR sequence 5 published and read back clean |
| 2026-09-21 | thirty-first | N30 closed: an expiring grant is a deadline the plan reports, not a schedule it builds (ADR 0024). No time index, no new variables — two lists, three notes and a refusal that names the window instead of blaming the cadence. The plan's `FEWEST_DAYS` sentence was backwards and the ADR carries the correction |
| 2026-09-20 | thirtieth | N31: a drawn guarantee is a rate curve, not a state dimension (ADR 0023, V11). The exact chain gained nothing; the simulation draws anyway, and caught itself sampling the prior — 0.558 against the chain's right 0.382. Then all four Phase 4 items planned: N30 decided as a deadline, N33's premise found already recorded, three held |
| 2026-09-20 | twenty-ninth | N32 closed by its fifth shape: a grant behind a score the reader supplies (ADR 0022, V10). Sequence 4, the Phantom Pain Cage: Evolve to SS in 63 days and no Serum. The EXP reading leaves as N33 |
| 2026-09-19 | twenty-eighth | N32 (4): one step at several prices is a choice the solver makes (ADR 0021, V9). Sequence 3: Samantha's Resonance, 90 Serum |
| 2026-09-19 | twenty-seventh | N32 (3): a shop limit that never resets (ADR 0020). Sequence 2, the shard shop, published; Evolve to SS plans |
| 2026-09-19 | twenty-sixth | N32 (1)+(2): gates become demand and fodder feeds EXP (ADR 0019, V8). Sequence 1 published; plans 240 → 420 and 180 → 1 470 |
| 2026-09-19 | twenty-fifth | Shops in the solver: the first-hand bundle plans, 150 and 240 Serum, worked out by hand first. Too cheap until N32 |
| 2026-09-19 | twenty-fourth | N27 done: one character, weapon and Memory, first-hand. PGR is farmed through a shop, so N30 needs shops; five refused shapes become N32; the reset is 05:00 UTC |
| 2026-09-18 | twenty-third | The first first-hand bundle — draft 0, nine facts — and the three *required* fields the format refused. `AuthoredBundlesTest` and the Gradle input hole it found. This file rewritten: 826 → 549 lines, 74 604 → 39 981 bytes, with the old one kept verbatim in the archive |
| 2026-09-18 | twenty-second | PGR read off the client, and the guides wrong twice: a per-banner featured rate, and events with no shops. N28 unblocked, N30 rescoped, N31's cause found |
| 2026-09-14 | twenty-first | The PGR survey D3 was missing. Nothing found plans PGR farming from an inventory; its one finding with teeth came from guides and was false |
| 2026-09-13 | twentieth | Launch title swapped to PGR (D3) — Kornblume solves per player. Q4 answered for one banner off the client. N5: every CI action onto Node 24 |
| 2026-09-02 – 09-12 | first to nineteenth | **Collapsed into one row on 2026-09-22, when this file passed 550 lines again.** Nineteen sessions from the scaffold to Phase 5 closing out of order: the first green build and CI, prior art and D1, the equipment question (ADR 0007), Phase 1's schema and the API answering its exit, ADR 0009, the community's answers and the stage table missing two thirds of the game (ADR 0010), yields learning their sample size (ADR 0011), the solve cache, the horizon as a scalar (ADR 0013), Phase 3, the sync debt (ADR 0014), **going first-hand (ADR 0015)** and provenance (ADR 0016), a browser signing in (ADR 0017), five screens driven in a browser, and two gacha engines agreeing (ADR 0018). **Each still has its own full entry [in the archive](docs/history/tracker-archive.md#session-log), newest first** — these one-liners were a second index over a file that is itself an index |

---

## Session log

**Append one entry per session, newest first, here — not in the tracker.** The
tracker keeps a one-line index pointing at these. Never delete an entry;
supersede it, and say in the newer one what the older one got wrong.

An entry is worth writing when it records something a future session would
otherwise have to rediscover: what was measured, what broke, what the numbers
were, and which assumption turned out to be false. A list of files touched is
what `git log` is for.

**2026-09-26 (forty-sixth) — S9 and S10: a purchase says its totals, and a zero says why; then the smaller three, and sequence 14.**

**The remote, checked first:** PR #55 (the forty-fifth's docs and S7's wording)
merged at 11:07Z, its `main` run `36238224339` green, `/api/health` reporting
`3bfa8a9`, #55's merge. No PR open. Neon not re-checked; nothing here publishes.

**The plan was put to the maintainer before any code**, and they said proceed
on both as proposed.

**S9 — "Buy 1,200 Cogs for 1 Simulation Score × 429".** The line named one
purchase and left 429 of them to arithmetic. `StepNames.step(id, times)` now
says the totals — "Buy 514,800 Cogs for 429 Simulation Score" — and
`repeat(id, times)` how a purchase is made up, "429 × 1,200 for 1", for a shop
row bought more than once and nothing else: a box opened, an item fed ("Feed 19
EXP Pod (XL) into Character EXP") and a price paid carry their count in the
total. **The total is a new field on `ConversionView` (`total`, `repeat`), not a
new meaning for `displayName`**, because Vercel and Render deploy at different
instants: a page from before it would print a total beside "× 429", wrong by a
factor of 429; this way an old page prints what it always did and a new page
meeting an old server does too. A price paid more than once multiplies each part
rather than prefixing "2 ×" to a sum. Quantities are formatted from a `long` now
— a stack times a count can pass an `int`. **Adding the overload made
`this::step` ambiguous in `stepOrder()`**, and the first test run reported a
stale green: `| tail` swallowed the exit code and the result XML was the last
run's. Read the exit code, not the XML's age.

**S10 — "What each material is costing you" all 0.00.** A price is
`marginal.totalEnergy() - base.totalEnergy()` over two proven-optimal integer
solves (`MipOptimizer`), so it is **0 or at least 1, never 0.004** — the
fixtures that said 0.004 and 0.12 were ones no server could send, and one
failed the new rule; they are whole numbers now rather than the rule loosened.
The zeros are one sentence — "No extra serum for one more of Cogs, Character
EXP, Skill Point — this plan already makes a spare, or gets them without
spending serum" — and the list keeps the prices that are not zero; when all
are, the panel is the sentence.

**Driven in a browser** as `rehearsal-3` against its third-run plan (180 Serum,
0.8 days), at 1280 and 375 px. **Another session held 8080 and 5173**, so this
one ran an API on 8081 and Vite on 5174 through a scratch config that reuses
the project's with the proxy moved; launch entries for it were added and
removed, not committed. At 375 the repeat broke as "429 / × 1,200 for 1" and is
now one unbreakable unit. **A cached plan showed the totals too**: the names
are built when the plan is served, not when it is solved, so plans solved
before the change read the new way. 486 backend tests (+1), 74 frontend (+4).

**Then the smaller three, at the maintainer's ask before closing.** Two had a
choice in them, so both were asked first; they took the recommended option each
time.

- **Solver-speak.** The first note, "Minimised energy over 1 stage(s), 3
  craft(s), 3 shop offer(s) and 0 reward(s), against 7 item constraint(s), inside
  a 14-day horizon at 240 energy a day", is now "Worked out from 1 stage, 3 shop
  offers and 3 recipes that could help, over 14 days at 240 Serum a day." — the
  game's energy word, kinds with none left out, the constraint count dropped
  (it described the model; no reader can move it). **It and the drop-rate note
  now come last**, after the notes about this reader's plan. "Recipe" because a
  fodder rule and a step's price are recipes to the model, and "craft" read as
  crafting.
- **Pods L, XL, M.** The items route sorted rarest first, then by name, and L
  and XL are both 4★. The tie-break is now **what an item feeds at its best
  fodder rule** (`GameDataReadModel.inventoryOrder`), the one number a bundle
  gives that ranks two items of one grade — XL, L, M, with no word of any game in
  the rule. Tested against the real bundle; the API test's synthetic game has no
  fodder and cannot show it.
- **"6*" for ★ — sequence 14.** The bundle's grades were ASCII `4*` while its
  item names said `5★ Memory Shard`. The maintainer chose the data fix over the
  page redrawing `*`, so the bundle says it. **31 changes in the diff, every one
  a label, every rank equal** — the ranks are what every rule compares. Published
  locally at 11:53Z and, with the maintainer's yes in chat, **to Neon at
  12:06:14Z**, each read back as *no changes*; production's `/items` served
  sequence 14 with ★ at once, ahead of #56's code, which nothing in it needs.

488 backend tests (+3 in the session), 74 frontend. The window stopped drawing
partway through the second browser check, so clicks timed out and a screenshot
came back blank; the form was submitted from script and read back as text, which
is what the evidence was anyway. **PR #56 opened** from `dev`, CI running.

**Left from the rehearsal: S6 alone**, a reading. When it closes the stall list is
empty and D5's trigger fires.

**2026-09-26 (forty-fifth) — S7 and S8: one goal row per construct, and every id on screen named.**

**The remote, checked first:** PR #53 (the forty-fourth's docs) merged at
01:10Z, its `main` run `36207626344` green, and `/api/health` reporting
`8470ddf`, #53's merge. No PR open. Neon not re-checked.

**The plan was put to the maintainer before any code** (the standing rule for
screen changes): S8 in two halves, the first from facts already published and
the second wanting a bundle word; S7 as one row per construct reusing the
roster's picker; S6 as a reading only the maintainer can take; the four smaller
hesitations as one-answer fixes. **They said proceed**, which agreed the S7
row shape and the S8 split. They did not answer whether "Phantom Pain Cage" is
the mode's exact name; it is written as the bundle's word, not a reading, so a
correction is a new sequence and not a provenance change.

**S8, first half — the page names what it already could.** The plan's *Paying
for* note printed `level-65` and `abyssal-lament-18` because `StepNames`
deliberately guesses no name the game never gave. **The fix moves the guess, not
the rule:** the steps now travel as data (`Explanation#payingFor`,
`PlanResponse.payingFor`) and the page names each state the way the goal screen
already did, "Level · 65, Red Orb · 18, Promote · Ace ★1", through one shared
`stateLabel`. The server's own unguessed sentence rides along as
`displayName` for anything else. **A snapshot-gated test caught the one caller
still reading the old note** — `RealUpstreamPlanTest`, which CI never runs;
the first full local build went red on it and nothing else did. Also: the home
page names a profile's game, the roster offers only someone with a track
(Karenina was offered with nothing), and *Make one* returns through `?then=`
with a local-path check.

**S8, second half — sequence 13 and ADR 0033.** Three keys the bundle invents
had nothing but themselves to show: a measure, an item category, an entity
kind. ADR 0028 had settled the same question for progress kinds, so this
follows it: a `Word` (subject, key, display name), one table for all three
(`V17`), no provenance, optional, refused when it names a key nothing uses,
flattened into the diff. **Several keys may share a word, and for categories
that is the point** — three Pod sizes are three categories to a fodder rule and
one heading to a reader. Progress names were deliberately not folded in. The
Cage now reads "Phantom Pain Cage, weekly, score 90,000+", and *Not counted*
reads "Phantom Pain Cage at 30,000, 90,000, … (this plan was asked for 0)"
instead of nine tier ids; the inventory went from 13 headings to 11. **Sequence
13 previewed as exactly 17 word subjects and no fact**, published locally, read
back as *no changes*. **Not on Neon**: that needs #54 deployed and the
maintainer's go-ahead.

**S7 — one goal row per construct.** A row is every track under the game's
headings, "leave as is" by default, offering only states ahead of where the
reader stands; saving writes one goal per track set, in track order, so the API
and the stored list are unchanged. Adding someone opens an empty row rather than
guessing a target. Where they stand is a button on the row, the roster's own
`TrackPicker`. **One thing decided without asking:** two old goals on one
track collapse to the further on save — the same plan, since the further pays
for the nearer.

**Driven locally** with the dev sign-in, as `rehearsal-s6` and a fresh
`rehearsal-s8`: the named *Paying for* line, the Cage's question and note, 11
inventory headings, "(construct)", the roster without Karenina, *Make one* back
to Inventory, four goals saved from one row and a plan from them (1 320 Serum),
and the Goals screen at 375 px with no horizontal scroll. **The pane twice
refused a click because the window was not drawing**; those steps went through
the DOM instead, which proves the handler and not the hit target.

**Tests:** 485 backend (0 skipped, snapshots present; 469 expected on CI), 70
frontend. PR #54 carries three commits.

**Then the maintainer merged #54** (10:58Z) and said to proceed. Its `main` run
`36237367444` went green with `deploy`, and `/api/health` reported
`c5bd578` before anything touched Neon — the old server does not know `V17`,
and waiting for the new one meant the migration never met it. **Sequence 13 was
then previewed against Neon** as the same 17 word subjects and no fact,
ingested, published at 11:06:47Z, and read back as *no changes*; the live
`/measures` route answers "Phantom Pain Cage" and `/items` the 11 headings.

**The rehearsal's third run**, locally with the dev sign-in as a fresh
`rehearsal-3`: a PGR profile, three counts in the inventory (150 000 Cogs, 60
Skill Points, 20 EXP Pod XL), Lucia on the roster at Elite ★3, Lv 60, two
skills at 4, then **one goal row with three targets** (Hero, Lv 80, Red Orb 18)
saved through "Save and get the plan", and a plan: 180 Serum, 0.8 days, six runs
of Simulated Battlefield, every line and note named. **It completes.** Two things
still read wrong and are screen changes, so they went in as S9 (a purchase line
reads as one cheap buy — "Buy 1,200 Cogs for 1 Simulation Score × 429") and S10
(the shadow-price panel is all 0.00) for the maintainer to agree a fix. Three
wording slips from S7 itself were one-answer fixes and were made in the run:
"Add a character" over a list holding a weapon and a memory, "her catalog page"
with nobody chosen, and step 3's blurb still describing one goal per row. The
pane stayed hidden (a 0×0 viewport) for the whole run, so every step went
through the DOM; the flow is proven, the hit targets are not.

**2026-09-26 (forty-fourth) — the rehearsal's second run, and Track C.**

**The remote, checked first:** PR #52 (S5) merged at 00:44Z, its `main` run
`36206127418` green in 5m53s, and `/api/health` reporting `32cd5b0`, #52's
merge. So S5 is live.

**The rehearsal ran a second time, and it did not close.** It ran locally, as
the earlier sittings did: Postgres from the volume, `api` and `web` from
`launch.json`, and the dev sign-in as a new account, `rehearsal-s6`. Production
signs in only through Google, which Claude cannot do. The run went from home to
sign-in, a profile, inventory (150 000 Cogs), Lucia: Inverse Crown on the roster
(Elite ★3, Lv 60, two skills at 4), and two goals (Promote to Hero, Red Orb to
18). The plan came back at 1 140 Serum and 38 runs of Simulated Battlefield,
with every buy and claim named, reach answered at 360 000. **The flow
completes, and it stalls three ways.** **S6:** skill tracks offer only 1, 2, 3,
4 and 18, because `{skill}-18` is one row *derived* from the 1 → 18 prompt with
levels 5–17 unread. A mid-curve player has to pick 4 and is charged for levels
already paid. It needs a reading, not code. **S7:** a goal row holds one target
from a flat list of 61, so "Lucia fully built" is about twelve rows. One target
per track on a row, reusing `TrackPicker`, was proposed and is not agreed.
**S8:** raw ids are left in the plan's *Paying for* note (`level-65`,
`abyssal-lament-18` beside "Ace ★1"), in the reach label and the *Not counted*
tier ids, in the inventory headings (`HARMONY-MATERIAL`, one heading per EXP Pod
size) and in the home page's profile line. Smaller hesitations went in as one
line. **"The Proving Ground" as the default game is local only**: production
publishes PGR alone. The pane twice drew nothing after a scroll, and
`get_page_text` read the page correctly, so this was the pane and not the app.

**Track C, by the maintainer (D6).** After Track B was explained, the maintainer
asked for a track before it for UI and new features. The one question put back
was *before or after the launch*. Before, it would be the third launch deferral
after D4 and D5. **They chose after:** it starts when Phase 4's box is ticked,
runs beside Phase 6, and closes before Track B. It is named Track C, with phases
C1–C3 rather than renumbering 6–12, because the archive links phase numbers
hundreds of times. Of four offered scopes they took three: **C1 pull planner**
(route, screen and stored `PityState` over the engines nothing calls), **C2 UI
polish**, and **C3 account features**. They declined "more constructs". The
exits are Claude's proposals and are marked not yet agreed, except that **C2
has no strangers**: the maintainer said "we will test ourself", so its exit is
a self-run rehearsal plus every screen at 375 and 1280 px. Phase 4's
five-stranger exit is unchanged. `plan.html` was not edited; the change is D6,
as D2 and D3 were.

**Line count:** 548, then 555 with Track C and D6. S1–S5, the answered Q6, the
header's rewrite history and Phase 4's met closing condition moved here
verbatim; see the ledger. No code changed and no build was run.

**2026-09-26 (forty-third) — S5: the plan page said what to do in ids.**

**The remote, checked first:** PR #51 (sequence 12) merged at 11:43Z on
2026-09-25, its `main` run `36130990100` green, and `/api/health` reporting
`1ee33fa`, #51's merge. `dev` level with `main`, no PR open. Neon was not
re-checked.

**S5, the stall the forty-second saw and had not counted.** *What to run*, *What to
craft and buy* and *What to claim* printed their ids —
`simulation-shop-memory-enhancer-iv`, `phantom-pain-cage-90000` — and so did four of
the solver's notes. **No word was read off the game to fix it.** A stage's name was
in the bundle all along and the view dropped it; everything else is named by what it
does, in item names and quantities already published: *Buy 10 Memory Enhancer IV for
87 Simulation Score*, *Open 10 Overclock Material Box (α) → 5 Minor Overclock Alloy +
…*, *Feed Memory Enhancer IV into Memory EXP*, *Pay 246 Simulation Score*, *Weekly,
score 90,000+: 5 Phantom Pain Scar + …*. **The maintainer agreed the wording before
any code** ("Open" for a box craft included). Two things the plan proposed and the
facts would not carry: a claim tier is **not** called "Phantom Pain Cage", because
the measure is an opaque slug nobody read a name for (ADR 0022) — it is named by its
cadence, bar and payout instead; and a step's state keeps its id where the bundle has
no word of the game's (*Helentine: Lacrimosa to evolve-ss*, but *… to Hero* on the
Promote ladder), because guessing a name from an id is the page's business and the
page says it is guessing (`roster/tracks.ts`).

**Where the naming lives, and why there.** `StepNames`, in `planner`, because the
notes are written there and `planner` cannot see `api`. It absorbed `DemandNames`
whole — the choice line's "one of: …" and the plan's "Pay …" now share one price
formatter rather than two that could drift. The wire carries a `displayName` beside
every id (the id is still what a bug report quotes and what `bindingStages` matches),
optional on the page's side because Vercel and Render deploy at different instants.

**Then the order, which the names exposed.** Rows came sorted by id, which read as
no order once they had names: *Feed* before *Open* before *Buy*, and a tier at
1 000 000 above one at 120 000, because the id sorts as text. `PlanResponse` now
sorts buy, open, feed, pay, and a ladder bottom rung up — still one order per plan,
so two reads stay byte-identical. **`Plan` itself is untouched**: the planner's
tests pin its id order, and a sort a reader wants is the view's.

**Two tests pinned an id and moved with it, correctly:** `MipOptimizerTest` looked for
`i1` in the "Paying for" note and `PlannerAcceptanceTest` for `amulet-level-30`; both
now assert the named step. **Still raw, deliberately left:** the deadline, lapsed and
withheld-grant notes (they carry the bar and dates, and their tests pin the ids), the
solver's refusal text (it names `punishing-gray-raven` and item ids), and the ladder's
own `phantom-pain-cage-score` label on the plan form.

**Driven in a browser** against sequence 12 on the local stack, a goal set producing
every row kind — Samantha's Overclock and Resonance, Lacrimosa's Evolve, the Cage
cleared — at 1280 and 375 px: every row named, the ladder bottom-up, no horizontal
scroll at 375. **478 backend tests** in full (475 + 3), 0 skipped with the snapshots present;
55 frontend (53 + 2).

**2026-09-25 (forty-second) — The launch question waited on a red `main`.**

The handoff said to ask the maintainer about launch. Checking the remote first
found **PR #46's merge run on `main` red** (run `36009244866`), and so **`deploy`
never ran**: `/api/health` reported `fac0ce6`, PR #45's merge, the morning after.
Nothing #46 carried was live: not return-after-sign-in, not choices named by
price, not the character page's own-game profile. **The forty-first's "not yet run
against Google: that is the first sign-in after it deploys" was waiting on a
deploy that had not happened**, and nothing in the tracker would have shown it.
The fourth Q6 item stays met in code and unchecked against Google.

**The failure was a flake, measured rather than assumed.** One test,
`EntityPage.test.tsx` › *asks for the shortfall with the profile for the game on the
page*, could not find the `Cogs` cell. The DOM dump showed "Working it out…". The
same commit passed on `dev` push (whole file 366 ms) and on the PR run (that test
486 ms), then took **1 320 ms** on `main`, past Testing Library's 1 000 ms `findBy`
limit. It is the file's first test to mount the whole shell, so it pays the cold
imports on top of a chain of requests: account, profiles, roster, then the shortfall,
whose key moves when the roster lands. Locally the whole file runs in ~330 ms, three
times out of three. **Fix: `configure({ asyncUtilTimeout: 5000 })` in
`src/test/setup.ts`**, for every test rather than this one, because every test that
mounts `App` pays the same cold start. A passing query still returns as soon as its
element appears. 39 frontend tests, green.

**What this says about the pipeline:** the rule "wait for the run before merging"
held and still was not enough, because the run that failed was the one *after* the
merge, on `main`. A red `main` blocks the deploy without a sound, so the remote check
at session start now has to include **the last `main` run, and the SHA
`/api/health` reports**, not only which PRs merged.

**D5: the launch question answered with a question.** Asked about launch, the
maintainer said they know few people who play and asked whether they could test
it themselves. The answer given: a self-test is a good rehearsal and cannot meet
the exit. The exit tests whether a newcomer can plan without help, and the builder
is the one tester who cannot stall where a newcomer would. It also leaves the
Track B gate and Phase 6's community data (N26) with nothing to open them. The
maintainer chose to write that up as D5: rehearse first, strangers after. **Its
reversal trigger, "the rehearsal's stall list is empty, then strangers from the
game's subreddit and Discord servers", was Claude's proposal**, written in because
a deviation needs one and following D4's "a list, not a feeling". The maintainer
may still change it. Making room for D5 took the tracker from 555 lines to under
550, by carrying less rather than rewrapping (see the ledger).

**The rehearsal began and found S1 within minutes.** The maintainer signed in on
the live site from a catalog page and reported it done. That is Q6's fourth item
run against Google, on their word; the screenshot they sent shows them signed in,
not the landing page. The same screenshot showed *"Could not create it:
/api/me/profiles responded 500"*. They had one profile, "Thel" on PGR/global, and
the add form's defaults are **the first published game on "global"**, which is
exactly that place. V5's `profile_unique_per_account UNIQUE (account_id, game_id,
region)` refused the second, correctly and deliberately (its comment: a second
"main" on one server splits an inventory in half). But nothing mapped
`DuplicateKeyException`, so the refusal arrived as a 500 with no sentence. **Fixed
at both ends.** The controller checks `profilesOf` before saving and throws a new
`ConflictException`, which `ApiExceptionHandler` makes a 409 naming the holder. It
checks rather than catching the key violation because the api module has no
spring-tx on its classpath. Two requests for one place at the same instant still
reach the constraint and a 500, which the javadoc says, and the button is disabled
while one is in flight. The form says *"You already have a Punishing: Gray Raven
profile on global: Thel."* and disables Add until the game or server changes.
Both tests failed first, the backend one with the production `DuplicateKeyException`.
Driven in a browser against a local API: the sentence, the disabled button, and a
forced POST answered 409 with the server's sentence. 467 backend tests, 41 frontend.

**S2 and S3, from two screenshots of #48 live (`8430fae`).** The maintainer
asked what "and also…" was, and why saving goals led nowhere. **S2:** the roster
and goal screens' `StateChips` offered every state of every track in one
dropdown. From the live data, Selena alone has **13 tracks and ~70 states**
(promote 0–13, level 1–80, evolve S→SS, leader skill, eight skills 1→18, one
unlock), shown as raw ids beside chips. The maintainer asked for a tick list and
sections. **The plan was put to them before building, and they chose one
dropdown per track over ticks.** Ticks would be ~70 per construct, and a crossed
gate already counts as reached (ADR 0026), so one answer per track says the same.
`roster/tracks.ts` splits the upgrade graph into its connected pieces, each walked
from its base. **The tracks come from the graph, so no game is named. The labels
are a guess from the ids** (the words every state on a track shares), because no
bundle names a track and neither the entity nor the step carries a name. Naming
them properly is data work, left for when a bundle can. `TrackPicker` replaces
`StateChips` and the `.chip` styles. It shows the furthest recorded state per
track, records the base when chosen, and keeps (and names) states on no track. A
goal row shows only its own track, with a link to the roster. **S3:** the four
steps (Inventory, Roster, Goals, Plan; Roster had not been one of the home page's
three) are one list in `steps/Steps.tsx`, read by a step bar in the shell, a
back/Next strip on each page, and the home cards. The menu now follows the same
order. Goals' Next is "Save and get the plan →" when there is a draft, and stays
put if the save fails. **The first browser run found two defects in the new
code:** the goal row passed only its own track, so it called the reader's other
answers "on no track" (pinned by a test shown failing against the old behaviour);
and a new goal defaulted to the last listed state, a skill unlock, and now takes
the end of the first track. Driven against a local API: 13 labelled tracks, both
answers stored one per track, a goal defaulting to Promote · 13, and Save and get
the plan saving and landing on step 4. At 375 px nothing overflows on any of the
five pages. **No screenshot**: the pane would not draw all session. 50 frontend
tests. The tracker is at 549 lines, so the next session starts by rewriting a
section.

**S4: S2's fix was the right shape with the wrong words.** Looking at #49 live,
the maintainer made two points. The game doesn't number rank: it shows ELITE
with stars. And "sections" had meant the game's own groups, not one dropdown
per track: Basic Skill, Special Skill, Evolution Effect and Common Effect, each
skill tagged by its orb or kind, which is how players find a skill. **The
plan went to them first** (a new memory records that they want that for every
screen fix) and they answered from screenshots of Lacrimosa's Promote tab
(fourteen panels, one per state) and her four skill pages. Both readings were
already in the bundle as prose. The ladder's `character-screens` detail says
"her Promote tab at every step from PRIVATE to HERO", and the skills group's
comment lists every slot's tag from `skill-pages` (2026-09-24). **No new
provenance entry was needed.** Today's screenshots and the maintainer's answers
settled the star counts (Private starts with its star filled), that the Leader
sits under Common Effect, and that the layout is shared by every S-rank
construct. **ADR 0032:** the words ride on the step as `Upgrade.Labels`
(fromName, toName, section, tag). They are facts covered by the step's
provenance, unlike ADR 0028's progress names, because they are the game's text.
The section *order* is the bundle's `sections` list, not a fact. "Growth" is
the one bundle word, since Level, Promote and Evolve share no heading in the
client. The leader's step arrives before the skills' and its heading comes
after theirs, which is why the order couldn't come from the steps. A ladder
group can now carry `positions`, one object per element of its list, so slot
two is the Yellow Orb on every construct and is written once. `V16` adds four
nullable columns to `gamedata.upgrade` and a `gamedata.section` table. The
diff flattens all of it, the upgrades route serves it, and `GameDataBundle`
refuses an undeclared or unused section and a state with two names. **Sequence
11 previewed as 328 changes, all words** (checked by filtering the preview for
anything else: nothing). It was published locally and read back as *no
changes*. **Neon still holds 10**: that publish waits for the PR to deploy V16
and for the maintainer's OK. The roster now shows Growth / Basic Skill /
Special Skill / Evolution Effect / Common Effect, each skill row led by its tag
with the skill name small beside it. A saved `promote-1` reads "Sergeant ★1",
and a goal reads "Promote · Hero". Goals' targets follow the same section
order; they had followed step order, putting the Leader before the skills. **A
wrong assertion of mine** (`promote-5` as Elite ★2; states 3–5 are Elite ★1–3)
was caught writing the test, not by it. The first screenshot of the session
worked at the end. Skill names are still guessed from slugs ("Withering
spiral"). The maintainer offered to read **Ultima Awaken**'s costs; the
bundle's comment has it with no Skill Point or Cog cost on its screen, so the
reading will say what it does cost. The PGR Status bullet was rewritten to
make room (archived above). 475 backend tests, 53 frontend.

**Sequence 11 published to Neon** at 11:32:55Z, on the maintainer's word in chat,
after #50 had deployed (`61929c1`, V16 applied by its startup). The Neon preview showed the same
328 changes, none of them a number; it read back as *no changes*, and the live
upgrades route serves Selena's `promote-5` as "Elite ★3" and her second skill as
Basic Skill / Yellow Orb. **Ultima Awaken**, the maintainer reports, costs the same
as the leader unlock. That contradicts the skills group's note of 2026-09-24,
"reached by the Ultima awakening, with no Skill Point or Cog cost on its
screen", so it went back as a question rather than into the bundle. **The maintainer's answer was to put
it in now and come back to it later** — every construct has it, it opens after
the fourth Awaken — so **sequence 12** adds three hand-written rows (`ultima-awaken-locked`
→ `ultima-awaken-1`, 3 SP + 25 000 Cogs, Common Effect, no tag), credited to a new
provenance entry `ultima-awaken-report` whose detail says it is a report from
playing, read on no screen, and names the note it contradicts. Hand-written
rather than laddered because a climber's `sourcedBy` beats a row's on a ladder,
which would have credited Selena's and Lucia's rows to their screen sittings.
The Awaken gate is not expressible — no Awaken track exists — and the row says so.
Preview: three additions and nothing else, locally and on Neon; published to both
(Neon at 11:41:06Z) and read back as *no changes*; the live route serves it
credited to the report. N42 holds the re-reading.

**2026-09-24 (forty-first) — Q6 written down, and its smallest item done.**

The session opened as the fortieth's handoff asked: is Q6's list complete, or
launch? **The maintainer added all three "seen, not agreed" items**, so D4 now
reverses on four: three whole ladders (met), `choice:` lines naming their
upgrades, a shadow price rendered against a real plan, and sign-in returning
the reader where they were. The second is not a label fix — an upgrade's name
is the game's word and so a fact with provenance. The third needs a goal set
with a farmable stage, which the bundle's one stage does not give, so it may be
data work before it is screen work. **The remote line was stale a seventh
time**: it named #44 and "9 ahead"; PR #45 had merged at 12:47Z and `dev` had no
commit `main` lacked.

**Sign-in returns the reader to their page (Q6's fourth item).** The provider's
flow could not take a destination, because the request that ends it is Google's
redirect back and carries nothing of ours but `state`. `ReturnAfterSignIn` wraps
Spring's authorization-request resolver to keep `?then=` in the session, and is
the success handler that reads it once and redirects. With no `then`, Spring's
saved-request handler runs as before. The session survives sign-in because
fixation protection changes the id and keeps the attributes. **The open-redirect
check the dev sign-in wrote in phase 3** ("the next endpoint that takes a
redirect target may not be development-only") **is now shared as
`LocalDestination`**, and it also refuses control characters. It is a class of its
own because `identity-dev` has no OAuth client on its classpath: the first try put
the check on `ReturnAfterSignIn` and `identity-dev` failed to compile against its
interfaces. `identity` gained `compileOnly` servlet API, which needed E1's flag to
download. **The Google leg is not tested**, because completing it would exchange a
code with Google. The test drives the real authorization endpoint, hands that
session to a `ReturnAfterSignIn`, and separately proves the chain's success handler
is that class. With the wiring removed, the two tests that depend on it went red.
The production bundle carries `?then=` and no `/dev/sign-in`. 462 backend tests,
33 frontend.

**A choice line is named by its prices (Q6's second item).** The tracker had
said an upgrade's name "would be the game's word, so it is a fact with
provenance". Correct, and it turned out not to be needed. The prices are
facts already, and they are what the reader is choosing between: Samantha's
line now reads "one of: 150 5★ Memory Shard · 234 Special Support Token · 246
Simulation Score". A price in several parts is joined with `+`, and quantities are
grouped with `Locale.ROOT` so every server writes them the same way. **Two views
had named demand lines separately**, the shortfall and the plan's shadow prices,
and both fell back to the raw id for a choice. `DemandNames` now names both.
`AuthoredBundlePlanTest` pins Samantha's line against the real bundle. The planner's
own refusal text (`whyNot`) still lists upgrade ids; that is a refusal message,
not a page line, and it was left alone. Driven in a browser against the local
database, the line wraps to three lines in a 186 px cell at 375 px and the page
does not scroll sideways.

**Driving it found a bug nobody had met.** The dev account's selected profile
was R1999's, and the character page used it for a PGR construct. The shortfall
route answered 400, because the local R1999 version no longer parses ("craft
consumes nothing"; see *A published version that stopped being readable*), and
the page retried under "Working it out…" forever. The page should use the
profile whose game it is showing, and should say something when a request fails.
A stranger holds one PGR profile and will not meet it, so it is recorded rather
than fixed. 464 backend tests.

**A shadow price rendered against a real plan (Q6's third item), with no reading
taken.** The tracker had said the bundle lacked a goal set with a farmable
stage, and that was wrong. `simulated-battlefield` has been farmable since the
skill-to-cap test, and nobody had driven such a goal in a browser. Nothing
filters the section: `MipOptimizer` re-solves every demanded item, zero
included, and the page draws the list whenever it is non-empty. Lacrimosa's
seeker-system to 18 alone rendered Cogs and Skill Point at **0.00**, which is
correct: 5 runs pay 410 Score against 379, and that slack buys one more of
either. Adding Samantha's Resonance (246 Score, exactly 3 runs, no slack)
rendered its line at **90.00**, under the new choice name. Checked at 375 px,
where the name wraps to three lines, the price stays right, and the page does
not scroll sideways. **All four Q6 items met, so D4's trigger has fired.**
Seen and not on the list: stage names and the *craft and buy* rows still show
raw ids (`simulation-shop-cogs × 172`).

**The character page asks with its own game's profile.** This is the bug found
driving item 2, fixed in the same session, and there were three faults, not
two. (1) The overlay asked with whichever profile was selected; it now uses
`profileForGame`, which takes the selected profile if it plays the page's game,
otherwise the first that does. With none it says so and offers **Make a
<game> profile**. The shortfall route takes an optional `game` and refuses a
mismatch *before loading anything*, 400 "profile … plays reverse-1999, not
punishing-gray-raven". It is optional so that an old shortfall link still
answers. (2) The retry rule was "retry unless 422", and React Query reads a
retry function that returns true as *forever*. `ApiError.isRefusal` (any 4xx)
now ends it, with one retry for a fault, and the error renders as an alert.
(3) **Found only in the browser: the offer made the profile and the shell took
it away again.** `App` re-selects the first profile when the selected id is not
in the account, and the new id was selected before `/api/me` was re-read. The
home screen's *Add a profile* had the same race, never noticed because a
first profile *is* the first. `useCreateProfile` puts the profile into the
cached account, then selects it, then re-reads, and both screens use it. The
first version of the page test rendered `EntityPage` on its own and passed with
the race in place, so it now renders inside `App`. Each of the three faults,
put back, fails its own test. Driven in a browser with fresh dev accounts that
hold only an R1999 profile: offer, create, the selection stays, and the
shortfall is asked with `game=punishing-gray-raven`. With the backend stopped,
two tries, then "Could not work this out: … responded 500". **Left alone:** a
published version that no longer parses reaches the reader as **400** ("craft …
consumes nothing"), because `ApiExceptionHandler` maps every
`IllegalArgumentException` to the caller's mistake. That is a server-side data
fault and should be a 5xx; no route a stranger uses meets it now. 466 backend
tests, 39 frontend.

**The tracker reached 554, and *Held — Phase 4 scope* was rewritten** to four
lines, since D4 and the resume note now say most of it. Its text until then:

> **Every item is done or cut, on the record: N30, N33, N20, B5 and N41**
> ([in the archive](docs/history/tracker-archive.md#completed-next-actions)); B5 cut the
> dev sign-in's deletion (ADR 0030). **What is left of Phase 4 is its exit** — five
> strangers, which is the maintainer's to arrange, not a session's to build.
> **Deferred by the maintainer until the site is more finished — [D4](#d4--public-launch-deferred-until-the-site-is-more-finished-2026-09-24),
> and [Q6](#open-questions) asks what "finished" means.** When it comes: planning is under
> `/api/me`, so a stranger must sign in, and **the Google client is in *Testing***, where only
> listed users can — list each stranger or publish the app. Watch each session, answer
> nothing, and record where each one stalls; those notes are what closes the phase.

**2026-09-24 (fortieth) — N41 closed with a different third construct, and
the day's readings turned out to be on a patch that did not exist when they
were labelled.**

**Selena: Pianissimo went in from ten screenshots.** Eight skill pages, her SS
passive (Rainbow-Hued Melody) and her shard card (Inver-Shard - Pianissimo).
One climber, one item, and `preview` against sequence 8 read +62 and nothing
else. **ADR 0031 held two rows back for confirmation per climber** (Evolve at
30 shards, the SS passive's unlock at 2 SP + 20 000 Cogs), because the
maintainer's comparison had not covered them. Asked, the maintainer confirmed
both, said the shard's 5★ was read on its shop tile (the card shows none), and
that **every S-rank shard tile sells 30 ever, 10 at 10 Scars then 20 at 20** —
which added two shop rows per construct. **Her Core Passive reads 18 (+6)**:
a bonus from an equipped Memory's Resonance (maintainer), not a level the
curve prices, and not modelled.

**Karenina: Effulgence was swapped out, by the maintainer, because they do not
own her.** Lucia: Inverse Crown (S-rank, maintainer; SS and Lv 80 on her
character screen) took her place in Q6's three. Karenina stays in the bundle as
her banner's target entity with nothing to climb. Lucia arrived as a new
entity, a shard (Inver-Shard - Inverse Crown, 'Owned 0'), two shop rows and a
climber; her SS passive is Bloomtide: Genesis. **Her numbers were not read on
her screens at all** — every one is the maintainer's report that it holds for
every S-rank construct, and her provenance entry says so rather than implying a
reading. Sequence 9 carried both: **129 facts added, 121 → 250**, nothing
changed or removed.

**The auto-mode classifier refused the Neon publish as a production deploy**,
and it was not worked around; the maintainer approved it in chat and it ran
after. That delay is what made the next finding cheap.

**The patch was wrong.** Mid-session the maintainer offered a version number
instead of a patch name, and asked, said **4.8.0 "Anchored in Faith" went live
at the 2026-09-24 maintenance, and every screen read that day was read after
it** — the thirty-ninth session's Lacrimosa skill pages included, which
sequences 8 and 9 had labelled "Steering By Light". The older patch is 4.7.0.
**Sequence 10 changes no fact:** it relabels the version "Anchored in Faith
(Global 4.8.0)", names 4.7.0 or 4.8.0 in every provenance entry, and says in the
three affected entries that the earlier sequences were wrong. What carries the
4.7.0 numbers across is the maintainer's report of noticing no cost change,
plus **one point read on both patches**: a skill's 1 -> 2 at 1 SP + 2 000 Cogs,
on 2026-09-19 and again 2026-09-24. **Sequence 8 on Neon still says "Steering
By Light" for 4.8.0 readings, and stays that way** — published versions are
immutable, and sequence 10 is the correction. The file keeps its name; five
tests open it by path, and a file name is not a label.

**A test went red with sequence 9 and was missed.** Only the `AuthoredBundle*`
tests were run before committing it; `GameDataIngestTest` summed every level
link in the bundle to 497 000 and listed only Lacrimosa's never-resetting shop
rows. The sum is now per construct, the list names all six. **Run the full
build before calling a bundle change green** — the bundle is read by more tests
than the ones with its name on them. 452 tests, 0 skipped.

**Published:** sequences 9 and 10 locally (12:23Z, 12:37Z) and to Neon
(12:40:52Z, 12:41:47Z), each read back as *no changes*; Neon got 9 from its
commit (`fd8ea67`) so both databases hold the same history. The live
`/api/games` reports sequence 10. `dev` pushed; PR #44 had merged before the
session, which the tracker still called #43.

**2026-09-24 (thirty-ninth) — N41's open question answered by the maintainer,
and the answer turned into a ladder rather than 114 pasted rows.**

**The question N41 was scoped to measure** — whether a second construct shares
Lacrimosa's level, Promote and skill costs — **was answered in chat before a
screen was transcribed.** The maintainer reports every S-rank construct pays the
same EXP, Cogs and Skill Points. Asked how that was read (the standing memory
note about relayed answers), they said: **opened Selena: Pianissimo's and
Karenina: Effulgence's screens and compared them with Lacrimosa's**, plus
levelling many constructs over a long time. First-hand, and recorded as such.
The level curve had already been cross-checked on Luna: Oblivion (2026-09-21).

**The maintainer asked whether dividing the data per construct was wise.** The
answer split in two, and the split is ADR 0031. **The model keeps one row per
construct:** goals, roster states (ADR 0027) and the solver are all per
entity, and Evolve spends each construct's own shard. **The file stops copying
it:** `ladders` writes an upgrade path once. Per-construct words are
`{placeholders}` bound by each climber, and a list such as seven skill names is
walked by an `each` group. `UpgradeLadders` expands the ladder into ordinary
JSON rows before the existing `upgrade` parser sees them, so nothing downstream
changed.

**Provenance was the part with a real choice in it.** Each expanded row still
gets its own `factProvenance` entry. The narrowest statement wins: climber, then
row, then group, then ladder, then default. A `factProvenance` entry that *also*
names a laddered row is refused rather than ordered. The climber wins because
its claim is different: a row says where the numbers were read, and a climber
says those numbers are *this construct's too*.

**Lacrimosa converted, and proven not to move.** A one-off script rewrote her
57 rows as 30 ladder entries: 13 Promote, 13 level links (each
`sourcedBy: level-bracket-screens`), Evolve with `{shard}`, the leader unlock,
one four-row skill group, and `{ss-passive}-unlock`. It also dropped her 57
`factProvenance` lines. A throwaway test parsed the published sequence-7 file
and the laddered one. `VersionDiff` reported **no changes**, the sink sets were
equal, and all **117 facts had identical provenance**. The test was deleted
after the run. The file still says sequence 7, because its content is sequence 7.

**One bug, caught by the first test run:** the first expansion flattened a
group into independent rows, so the output order was `slash-2, parry-2, slash-3,
parry-3` rather than one skill's rows together. The diff compares as sets, so it
would not have noticed. The test that asserts order did.

**Then asking for the skill names found that Lacrimosa's own list was
wrong.** The maintainer said there are 13 skills, not 7. They confirmed Evolve
at 30 shards and the SS passive at 2 SP + 20 000 for all S-rank, and sent all
four skill pages (Basic, Special, Common Effect, Evolution Effect). There are
**eight skills on the level curve**, not seven. The **Signature Move** (Allegory
of the Wondrous Night) and the **QTE** (Fluid Emotions) had never been
recorded, and **"Astral Armament", recorded on 2026-09-19 as her Core Passive,
is on no page**: the Core Passive reads Seeker System. The maintainer then
identified it as **another construct's skill**, read off the wrong screen on
2026-09-19. The lesson for readings: a skill screen doesn't say whose it is
unless the name is in the text, so a transcription can't catch a screenshot
from the wrong character. The other five are the
leader unlock, the SS passive unlock, Ultima Awaken (no SP or Cog cost), and
the SSS and SSS+ passives (gated on Evolve ranks the bundle does not model,
prices hidden). The maintainer's first reaction, "why am I five short, did I
duplicate something", was the right instinct pointed at the wrong list: the
duplicate was ours.

**Sequence 8 written, and the ladder earned its keep on its first day.** The
fix was one edit to the climber's `skill` list, plus a `skill-pages` provenance
entry for the group, now dated 2026-09-24. `preview` against the local volume
showed −4 Astral Armament rows and +8 for the Signature Move and QTE, nothing
else: 121 facts over nine provenance entries. The research note got a dated
correction appended rather than its 2026-09-19 text rewritten.

**Published on the maintainer's word, in both databases.** Locally at
08:35:00Z, then Neon at 08:36:52Z. Each read back as *no changes*, and
the live API reports `sequence: 8`. Before writing to Neon, production was
checked for a goal or roster entry naming an Astral Armament state and held
**no goals at all**, so nothing could break. The Neon host and role came from
the thirty-eighth session's transcript, and the password from
`~/.neon-storm-almanac`, which was supposed to be deleted after that session
and was still there. It was read into an environment variable, never printed,
and the maintainer was told it's still there. **They chose to keep it for
future publishes.** That supersedes the thirty-eighth session's "to be deleted
after". The tracker's claim that the session log had "the exact commands" was
false: the host and role were never written down. Committed as `99683a8` before
the Neon publish, then the tracker was updated for the publish.

**What N41 still needs is words, not numbers.** For each construct: eight
levelled skill names, the SS passive's name, and the Evolve shard item.

452 backend tests (441 + 11 in `UpgradeLaddersTest`), 0 skipped locally, 0
failed. Docker Desktop had to be started first (E4). Nothing committed or
pushed in this session unless the maintainer asks. The remote was not
re-checked.

**2026-09-24 (thirty-eighth) — B5 started: the image runs, and running it the
way Render will found two bugs no test could.**

**Session start, the remote check.** PR #38 merged; `dev`, `origin/dev` and
`main` all held `4c693cd`, both runs green, no PR open. The tracker named #37 —
**stale for the fifth consecutive check.**

**Both B5 decisions, settled by the maintainer.** *One origin*: Vercel rewrites
to Render, keeping the same-origin session, CSRF cookie and OAuth redirect the
backend was built around. *The free tier sleeps*: the maintainer runs their own
keep-alive bot, which the tracker now says must ping `/api/health`.

**Step 1, the image, done for the first time.** The COPY list matched the
module tree, and `:app:bootJar` reads nothing outside `backend/`. **What stalled
the earlier attempt at 10% looked like the network and was the build context:**
there was no `.dockerignore`, so every build uploaded ~500 MB (189 MB of
`node_modules`, 217 MB of `backend/build` with the upstream snapshots, every
module's `build/`). A whitelist cut it to 965 kB, after which the Gradle
distribution downloaded in about three seconds and the jar built in 1m 46s.
143 MB image.

**Run as Render will run it**: a throwaway network, an **empty** Postgres,
**no Redis**. 15 migrations in 0.58 s — the first time Flyway met an empty
database outside Testcontainers — and started in 8.6 s. `/api/health` 200,
liveness and readiness UP, `/api/me` and `/dev/sign-in` 401. **`/api/games`
answered `[]`**, which is the step the archived six-step plan never named: a
deployed database is empty until somebody publishes into it. The CLI from the
same image ran `preview` (117 facts over eight provenance entries, matching the
tracker), `ingest` and `publish` of sequence 7 against it, and the API served
it. That is the rehearsal; the real publish is the maintainer's approval.

**Two bugs, each written as a test that failed before the fix.**
- **`/actuator/health` answered 503** because the Redis starter is on the
  classpath for the solve cache's `redis` option, and its health indicator is
  installed whether or not that option is selected. A platform health check
  reads that as a dead service. `management.health.redis.enabled: false`, to be
  reversed in the change that selects `solve-cache: redis` (ADR 0012's
  trigger). `ApplicationBootTest` now points Redis at `localhost:1`, because a
  developer running compose has one on 6379 and the test would pass there for
  the wrong reason.
- **The OAuth redirect URI would have been `http://<render-host>/…`.** Nothing
  honoured `X-Forwarded-*`, so `{baseUrl}` came from the host the request
  reached. `ForwardedOriginTest` registers a made-up Google client (Boot knows
  Google's endpoints, so nothing is fetched) and reads `redirect_uri` off the
  302: it failed with `http://localhost:54771/…` against
  `https://almanac.example/…`, exactly the bug. `server.forward-headers-strategy:
  framework`, not `native`, because Tomcat's valve trusts only private-address
  proxies and nobody has measured what Vercel's hop looks like to Render. **The
  cost is recorded in the config:** a forwarded client address can be claimed
  by anyone, so Phase 6's abuse controls must not treat the remote address as
  an identity. **Whether the two hosts actually deliver those headers is
  unmeasured** until the first exchange.

**What else the plan missed, now in the tracker's B5 entry:** the rewrite needs
`/oauth2/*` and `/login/oauth2/*` as well as `/api/*` — the frontend sends
sign-in to `/oauth2/authorization/google` — plus an `index.html` fallback,
because the app uses `BrowserRouter`; and Render's `DATABASE_URL` must be
written in `jdbc:postgresql://` form, since the one it displays is not.

**Numbers:** 438 backend tests (435 + 3), 0 failed, 0 skipped with the
2026-09-22 snapshots present; the gated suite was not re-dated, because nothing
here touched the model. The rebuilt image answered `/actuator/health` 200 UP
with no Redis. **Compose itself was not re-run.** The tracker passed 550 lines
(561) before this entry's row, and its session index was rewritten — the rows
are [verbatim above](#session-index-rows-collapsed-on-2026-09-24).

**The database moved to Neon mid-session**, the maintainer's call once
Render's free Postgres was found to expire. It needed no code: a JDBC URL with
`?sslmode=require`, the direct host rather than the pooler. The open question it
brings is whether the application keeps Neon awake. `/api/health` touches no
database, so the keep-alive bot does not; Hikari's pool might, and the three
settings that should let it go are Render environment variables until Neon is
seen suspending. **No object store is needed before Phase 12** — no game assets
by rule, OCR runs on the reader's device, and the first thing that wants one is
an off-provider backup for the restore drill.

**The backend went live the same session.** PR #39 merged at 02:39Z with every
run green first; Render built `main` and `https://storm-almanac.onrender.com`
answered `/api/health` 200, `/actuator/health` UP through Neon, `/api/games`
empty, `/dev/sign-in` and `/api/me` 401. **PGR sequence 7 was published into
Neon at 02:56:41Z**, run by the session at the maintainer's request. The
password never passed through the chat: the maintainer wrote it to a file in
their home directory, the commands read it from there, and the file was to be
deleted after. The preview was identical to the local rehearsal (first bundle,
22 items, 5 entities, 117 facts over eight entries) before anything was
written, and the live API read it back. **Phase 0's box stays unticked**: a URL
answers 200, but Render deploys it, not the pipeline.

**Writing `vercel.json` found the PWA would have broken sign-in.** The generated
service worker registered a `NavigationRoute` to `index.html` with no denylist,
so once a worker controls the page, *every* navigation is answered from the
cache — including `/oauth2/authorization/google` and the provider's return to
`/login/oauth2/code/google`. A first visit would sign in, because no worker is
in control yet; a returning reader would get the app shell and no error. It was
invisible because sign-in had only ever run on the dev server, which registers
no worker. `navigateFallbackDenylist` now covers `/api`, `/oauth2`, `/login` and
`/dev`. **Proven on the built bundle in a browser:** with a fresh worker in
control, `/plan` got the app shell and `/dev/sign-in` reached the preview's
proxy (logged `ECONNREFUSED`, no backend running). The failing half was read
off the old `sw.js` rather than reproduced in the browser. 31 frontend tests.

**The page went live at `https://storm-almanac.vercel.app`** after PR #40 merged
(03:01Z, every run green first). Through Vercel: `/` and `/plan` the app,
`/api/games` sequence 7, `/api/me` and `/oauth2/authorization/google` 401 from
Spring — not the app shell, so the OAuth rewrite reaches Render — and
`/actuator/health` and `/dev/sign-in` the app shell, by design. The CSRF cookie
came back `Secure` through Vercel, so **the proto arrives**.

**The host does not, and that was the one thing `ForwardedOriginTest` said it
could not prove.** With the Google client registered, the authorization
redirect through Vercel named `redirect_uri=https://storm-almanac.onrender.com/…`
— the same as calling Render directly. Three hand-set requests to Render
(`X-Forwarded-Host`, with and without the proto, and RFC 7239 `Forwarded`) each
came back naming the host sent, so **Render passes the headers through and
Spring honours them; Vercel's external rewrite does not send the original
host.** The config from PR #39 was right and the assumption about Vercel was
not. Nobody signed in before this was found.

**Two fixes, because it breaks two things.** The redirect URI sent to Google is
pinned as a Render environment variable
(`SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_REDIRECT_URI`). Pinning is
safe because Spring Security 6.5.5's `OAuth2AuthorizationCodeAuthenticationProvider`
compares only `state` on the way back — read off its bytecode, where the one
`String.equals` feeds `invalid_state_parameter` — and the token exchange reuses
the stored URI. **The second break the pin does not reach:** the success
handler's `sendRedirect("/")` is made absolute by Boot on the host the
application believes it is on, so a reader who had just signed in would land
on Render with no page and none of their cookies. `server.tomcat.use-relative-redirects:
true`. A probe in `ForwardedOriginTest` redirects exactly as the success handler
does; with Render's headers it failed as `https://localhost/` with the setting
off and passed with it on. **Side effect, recorded in the test:** with forwarded
headers present, Spring's relative redirect answers 303 rather than 302 —
harmless, every redirect in these flows is a GET.

**A trap found on the way:** `StormAlmanacApplication` declares
`@ComponentScan("io.stormalmanac")`, whose scan does not apply Boot's filter
that keeps test classes out. The probe's first version, a `@RestController`
nested in a `@TestConfiguration`, registered twice. **Anything stereotyped in
the test sources is in every test context**; the probe is now a plain class
arriving by `@Import`. Also on the way: the Docker engine died mid-session, and
E4's recipe brought it back in six seconds.

**The first real OAuth exchange ran at about 03:40Z**, after PR #41 merged
(03:38:36Z, the last check finished 03:37:57Z) and Render redeployed. With the
pinned variable the authorization redirect through Vercel named
`https://storm-almanac.vercel.app/login/oauth2/code/google`; the maintainer
signed in with Google, landed back on `vercel.app` as themselves, and created a
profile — **the first production write**, so the session cookie, the CSRF
cookie and the CSRF header all survive Vercel's rewrite. The line this file
carried since Phase 3, "the OAuth exchange has never run", is closed.

**Production had no sign-out.** `signOutUrl()` returned `null` outside `DEV`, so
the only sign-out that had ever existed was `/dev/sign-out`, in the module the
jar does not contain. Spring Security's `POST /logout` was there all along and
answered 302 to `/login?logout`, a page this application never had —
`SignOutTest` failed on exactly that before `SecurityConfig` gave it a 204. The
test holds the session in a real `MockHttpSession`, because the claim is about
the session: an `oidcLogin()` principal would be there on the next request
whatever sign-out did. The page's button POSTs with the CSRF header and then
does a full navigation, so nothing fetched as the leaving reader stays on
screen. Driven in a browser through the Vite proxy: 204, reload, `/api/me` 401.

**The deploy job exists, and CI now ships.** The maintainer chose CI-triggered
deploys over Render's own: auto-deploy off, `RENDER_DEPLOY_HOOK` as a GitHub
secret, the job gated to a push to `main`, and it **fails loudly if the secret
is missing** rather than skipping to green. It waits for `/api/health` to report
its own SHA — the application's version now reads `RENDER_GIT_COMMIT` — then
smokes through Vercel. **That Render exposes that variable at runtime is
believed, not measured**, and the first run on `main` is the measurement.

**ADR 0017's reversal has two conditions, and only one is met.** "A real
provider configured against a deployed URL" — yes. "A developer can sign in
locally against it" — not yet: the Vite proxy now forwards `/oauth2`,
`/login/oauth2` and `/logout` keeping the Host header, and `bootRun` alone reads
`~/.storm-almanac/` for the client secret, but the maintainer has to register
`http://localhost:5173/login/oauth2/code/google` and sign in. **Measured the
deletion's size against the ADR's own budget** (one settings line, one
dependency line, a directory, two tests): the backend fits; the frontend does
not — four screens import `signInUrl`, and sign-out existed only there.

**The deploy hook went in** — Render's auto-deploy set to Off, `RENDER_DEPLOY_HOOK`
set as a repository secret at 04:04:30Z (checked by name; GitHub never shows a
value). The maintainer noticed Render also offers **"After CI Checks Pass"** and
asked; it was not taken, because the `deploy` job is itself a check that waits
for Render, and Render would wait for it. PR #42 opened; its workflow parsed and
ran, `deploy` skipped off `main` as gated.

**Cut, on the record: `:modules:identity-dev` is not deleted**
([ADR 0030](../adr/0030-the-development-sign-in-stays-because-the-hands-that-need-it-cannot-sign-in.md)).
The maintainer asked whether local sign-in was needed at all and said they would
not use it. Following that through overturned B5's last step rather than just
skipping it: ADR 0017's trigger pictured the developer as someone who can type a
Google password, and the hands doing most of the signed-in work here are
automated sessions that may not. Every signed-in screen, sign-out included,
was driven through `/dev/sign-in` before it shipped, and `DevSignInTest` is the
only socket-level authenticated test — the one that found the CSRF cookie was
never issued. The safety 0017 bought is absence from the jar, which
`DeployableJarTest` proves every build and Render confirmed from outside (`/dev/sign-in`
401). 0030 supersedes only 0017's reversal trigger, and replaces it with two:
signed-in screens drivable without it, or absence no longer provable. The local
OAuth wiring stays, optional and inert. **B5 now closes on the first green
`deploy` run on `main`.**

**It did.** PR #42 merged at 04:11:53Z, after its last check at 04:11:19Z. Run
`35954626572` on `main`: frontend and backend green, then `deploy` — the hook
accepted at 04:13:38Z, `/api/health` still said `dev` a minute later (the old
instance, built before the version fallback existed), and at 04:18:05Z it
reported `b8c21e68137a…`: **Render does expose `RENDER_GIT_COMMIT` at runtime**,
the one thing the job's design took on trust. Four minutes twenty-seven seconds
from hook to new commit serving, two seconds of smoke through Vercel, and both
origins report the SHA where they used to say `dev`.

**Phase 0's box is ticked**, nineteen days after the phase closed by exception:
its criterion was "a green pipeline deploying a health endpoint to a real URL",
and that is now literally what happens on every merge. **B5 moved to *Completed
next actions*** verbatim, and N40 — watching Neon suspend before the pool
settings leave Render's environment — was split out of it, because it is the one
claim in the entry nobody has measured. **Phase 4 has no items left, only its
exit**: five strangers completing a plan, which is the maintainer's to arrange.

**N40 closed within the hour.** The maintainer left the site idle and read
**SUSPENDED** on Neon's compute with UptimeRobot still pinging `/api/health` —
so the bot wakes Render and not Neon, and the pool lets go. The cost, measured
through Vercel right after: the first `/api/games` took **2.06 s**, the next two
0.57 s and 0.75 s — about 1.4 s of Neon waking and a connection opening, paid
once per idle spell. The three settings moved from Render's environment into
`spring.datasource.hikari` with that measurement beside them; the Render
variables are now redundant and harmless.

**Asked whether Phase 4 could close: no, and not only because nobody has
tried.** The plan route is `/api/me/profiles/{profile}/plan` — an anonymous POST
is refused — and the Google client is still in *Testing*, where only listed test
users can sign in. As things stand a stranger cannot reach a plan at all. The
maintainer either lists each stranger or publishes the app; and one construct is
the whole catalog, which satisfies the criterion's letter and is the likeliest
first complaint.

**The maintainer deferred the public launch (D4)**: they do not want strangers
in until the site is more finished, and the Google client stays in *Testing*.
It is recorded as a deferral against the plan's "launch publicly even if ugly",
not a cut — the cut list forbids cutting it — with its cost named (Phase 4's
exit, the Track B gate and Phase 6 all wait on users) and its trigger made a
list rather than a feeling: **Q6** asks what "finished" means, seeded with what
this session saw. Worth saying plainly because it is easy to miss: the URL is
public and the catalog reads without an account; only signing in is held back.

**Q6's first item was agreed within the session: three constructs with whole
ladders** — Lacrimosa, and the two the bundle already names only as banner
featured units, Selena: Pianissimo and Karenina: Effulgence (**N41**). Three was
chosen over five or ten because the second construct read is the measurement
that prices every later one: if the level brackets, skill costs and Promote
gates turn out to be one shared ladder, constructs are cheap; nobody has read a
second one to know. Lacrimosa alone is 57 of the bundle's 63 upgrades.

**Moved out of the tracker's *Next actions* on 2026-09-24** to keep it under 550, verbatim — true when written, history now:

> **Every defect turned into an action on 2026-09-21 is now closed**, and the
> pattern from that batch held into this one: **an entry is wrong until somebody
> runs it.** N28 said "no bundle declares a banner at all" and one had since
> sequence 0; N37 was scoped as one sequence and turned out to be two decisions
> that share a file and nothing else. **Three stale claims were corrected in this
> file and one in the research note as a side effect of doing the work** — which is
> the argument for doing it rather than re-reading it.

**The second pipeline deploy failed, and nothing was wrong with it.** PR #43
merged at 04:38:31Z; run `35956512041` called the hook at 04:40:06Z and timed
out red at 05:05:19Z, never seeing `4e9c12d`. The maintainer pasted Render's
log: a clean build, then Spring taking **147 s** to start on 0.1 CPU — 44 s for
the web context alone — and `==> Timed Out` from Render about thirty seconds
before `Tomcat started on port 10000`. Render kept the old instance, so nothing
went down; the maintainer's manual redeploy then landed. **The first deploy had
fitted in the same window by a margin nobody measured**, so every deploy was a
coin flip that would get worse as the application grew. The `deploy` job going
red was the pipeline working: it refused to call a deploy that had not landed
green.

**Measured before changing anything**, running the jar at `--cpus 0.1 --memory
512m` against a live schema: fat jar **175.7 s**; fat jar with
`-XX:TieredStopAtLevel=1` 76.0 s; the jar extracted to a plain classpath 133.6 s;
extracted plus a CDS archive **65.4 s**; all three 29.4 s. One pair of runs
first reported 133.6 s and 54.9 s *as CDS* — the log showed the archive had not
loaded (`Required classpath entry does not exist`), because a dynamic archive
records its classpath as written and the run used a different working
directory; those numbers were kept as what they actually measured.

**The image now extracts and trains an archive at build time.** The training
run refreshes the context and exits, under `RUN --network=none`, with Flyway and
Hibernate's schema validation off for that run only — tried first with
networking disabled, and it exits 0, so the build can never come to need a
database. Built from the real Dockerfile with `--build-context build=…` standing
in for the Gradle stage, which E1 blocks on this machine, and run at Render's
limits: **59.2 s**, no CDS warnings, Flyway still validating the 15 migrations
at real startup, running as `almanac`, and no `devsignin` class in any extracted
jar — the dev sign-in's absence re-checked in the image itself, since the image
no longer holds the jar `DeployableJarTest` opens. **C1-only was not taken**: it
halves startup again but gives up peak speed, and the solver has a two-second
budget.

**The Docker engine died three times in this session**, each time between one test
run and the next with nothing touching it, and both times Testcontainers
reported it as "Could not find a valid Docker environment" before a single
assertion ran. E4's recipe brought it back in three to six seconds each time.
**A whole suite failing in five seconds is the engine, not the change** — read
`Caused by` before reading the diff. Then a session writes `vercel.json` against the
real Render URL, gates `deploy` to `main`, and runs the first exchange.

**2026-09-22 (thirty-seventh) — one bundle sequence, two decisions that share
nothing but a file.** N37 and N28, which the tracker said "should land together"
because both wanted a sequence 7. They did land together, and the only thing
they had in common was the sequence number.

**Session start, the remote check.** PR #37 merged; `dev` and `origin/dev` both
clean at `4abf8ae`; no PR open. The file named #36 as newest merged — **stale for
the fourth consecutive check.** The check has now never once found the file
right, which is a stronger statement than "re-check rather than read" and is
worth saying in those words.

**N37 turned on a question the code could not answer: what *is* a progress
kind's name?** The obvious answer — write down what the game calls it — is
wrong, and PGR is the case that proves it. The character Level Up screen reads
`EXP 0/20`, the weapon Enhancement Cost picker `EXP 24 000`, the Memory picker
`Obtained EXP 300`. **All three pools are called EXP**, so a first-hand name puts
three identical lines in one plan and leaves the reader unable to tell which one
they are short of. Put to the maintainer, who chose a bundle-authored label with
no provenance: `Character EXP`, `Weapon EXP`, `Memory EXP`. **ADR 0028**, and it
is the first text in a bundle of this project's that is deliberately not a
reading — `V14`, `progressKinds`, flattened into the diff anyway because a rename
moves every shortfall table.

**N28's blocker was never the reading.** Both numbers — 250 tickets a pull, 2 500
for ten — were read on 2026-09-18 in the same sitting as the rates. What kept
them out was that a price names an `Item`, `Item.rarity` is required, and the
research note recorded that no screen read that year graded a currency, so the
note argued for making rarity optional. **The premise was a statement about which
screens had been opened.** The maintainer opened the ticket's tile during this
session: **5★**. That is the fourth currency graded on a tile whose own item card
grades nothing, after Simulation Score, Phantom Pain Scar and Cogs. **The format
change the note argued for was never needed and is now recorded as not needed**,
with the same doubt transferred to Black and Rainbow Cards: try the tile first.

**`IncomeModel` was an interface with no implementation for the whole of Phase
5, and writing it meant refusing the easy number.** `projectedPulls(ProfileId,
LocalDate)` could not answer its own question — it named no banner, and PGR's
four pools have four different tickets — so it became
`affordableWithin(game, banner, held, days, reach)`: days not a date (ADR 0013's
argument), and accrual **summed from the `Reward` rows that grant the currency at
their own cadence** rather than from a pulls-per-day constant. **PGR's bundle
declares no reward paying a ticket, so accrual is zero and a test asserts that**
— the assertion is a marker for a missing reading, and the reading to take is
what the dailies pay in Black Cards (now N39). **ADR 0029**, `V15`, nullable
columns where null is *unstated* and `pricedPull()` refuses rather than answering
zero: "cannot afford" and "nobody read the price" are different answers, and a
screen showing the first for the second is wrong in the one way a reader cannot
detect.

**What was cut, on the record.** N28's entry asked for two more fields, both
R1999: a shop exchange for the featured unit and a cap on copies. **Not done, and
cut deliberately rather than forgotten** — it is not a field, it is an engine
change (`probabilityOfFeatured(…, copies)` has to spend the exchange in *both*
engines, growing the chain's state space), and it has nothing to exercise it
because R1999 has no first-hand bundle. A column no bundle can fill is
`Availability.opensAt` again. It is now **N38**, held for Phase 11.

**Three stale claims fell out of doing the work**, which is the argument for
doing it rather than re-reading it:

1. **"No bundle declares a banner at all" (N28, the tracker, the research
   note).** The PGR bundle has declared one since sequence 0 on 2026-09-19. What
   N28 was missing was the banner's *price*.
2. **"Both PGR pity archetypes are expressible and neither is authored."** The
   Arrival Construct one is authored. The Themed Construct one — the wall drawn
   80–100 — is what nobody has written.
3. **ADR 0027 was never added to `docs/adr/README.md`** by the session that wrote
   it. Added.

**The one failure worth recording, because it was caught by the right thing.**
`GameDataIngestTest`'s whole-graph equality failed: the bundle lists its progress
kinds in reading order and the database returned them sorted by kind. Fixed by
normalising in the record — the same thing `Upgrade` does to its gates and its
progress costs, and for the reason its javadoc already gives. **What found it was
a test that compares the whole graph rather than the fields somebody remembered
to assert.** The convenience constructor added in this change proved the same
point immediately: `GameDataDiffTest`'s `withRollover` helper used the
seven-argument `GameDefinition`, silently dropped the fixture's names, and
reported a rename the test never made.

**Measured, not assumed.** 435 tests, 0 skipped locally, **all 16 snapshot-gated
ones run against freshly fetched upstream data** — run 2 in
`docs/benchmarks/snapshot-gated-runs.md`. Nine benchmark agreements held. **Two
figures moved and both are recorded rather than smoothed over:** p95 1 808 →
1 812 ms (third measurement, 7 ms band, 188 ms of headroom, all three drifts
upward), and the plan cost **3 880 → 3 877 Activity**, 0.08%, with nothing in the
change touching the model — branch-and-bound landing on a different equally-good
answer inside its budget, which ADR 0010 says is the expected shape. Frontend
28 → 31.

**Sequence 7 published**, 2026-09-22T02:44:15Z, and read back as *no changes*.
The preview against published sequence 6 was exactly five lines — one item, three
`progress '<kind>'` subjects, and `pull price: unstated → 250 ×
event-construct-rd-ticket` — and 117 facts over eight provenance entries, all
first-hand. **A thing the next sequence will need:** the CLI boots the whole
Spring context and needs a live Postgres for *every* command, `validate`
included, and `preview` is only meaningful against a database carrying the
previous sequences. `docker compose up -d postgres` restores that from the
`storm_almanac_postgres-data` volume, which still holds every version published
since 2026-09-06.


**2026-09-21 (thirty-sixth) — N35, most of it: the reader is asked how far they
get, and there is a screen for where they stand.** The last of the four deferred
defects, and the first of them that turned out to be roughly what its entry said
— which is itself worth recording after three in a row that did not.

**Session start, the remote check the tracker demands.** PR #36 merged at
`26e4345`; `dev` clean and equal to `origin/dev` at `6cdf520`; no PR open. The
file named #35 as newest merged, so it was one session stale **for the third
consecutive check**. The instruction to re-check rather than read is now
three-for-three.

**The fork, and it was put to the maintainer.** ADR 0022 put `reach` on the plan
request beside `energyPerDay` and wrote its own reversal trigger: make it player
state "the moment a screen asks for it", which is the moment this session
arrived at. The maintainer chose to keep it on the request and have the screen
remember the answer locally, so **ADR 0022 stands unreversed** and its trigger
is still live — what fires it is a reader having to answer twice, which now
means a second device. The store holds `reach` per profile at persist version 2,
migrated by *adding the field* rather than resetting, because resetting would
throw away an unsent inventory to gain a field nobody has filled in.

**The measures had to be served before anything could ask.** ADR 0022 predicted
this precisely — "a measure is not declared anywhere ... so a screen that wants
to ask 'how far do you get in the Phantom Pain Cage?' has to collect the measures
off the rewards it can see" — so `GET /api/games/{game}/measures` does the
collecting once on the server. It carries each rung's **grants with names
resolved**, and that is load-bearing rather than tidy: the measure is a slug no
player has ever seen, and what makes the question answerable is the payout.
Ordered lowest bar first; an empty list is a 200, because a game that scores
nothing is an answer.

**What the browser showed, and one number that checks another.** Driven against
the real PGR bundle, signed in, roster recorded through the new screen at
`promote-6` and `level-80`, goal Evolve to SS, 63 days. Answering *nothing*:
refused, and the refusal names the measure, the lowest bar and what the reader
said — ADR 0022 clause 5, **rendered to a reader for the first time**. Answering
*1 100 000*: **63 days, 0 Serum**, the plan `AuthoredBundlePlanTest` has asserted
since the twenty-ninth session and which no web client had ever been able to ask
for. The ladder summary computes **56 Phantom Pain Scars a week** from the
published rungs, which is the figure ADR 0022 states in prose — two independent
routes to one number, and they agree.

**A dropdown of the bars, not a number box.** The bars are the question's whole
domain: a reader at 150 000 on a ladder whose rungs are 120 000 and 360 000 is
answering 120 000, and a free field invites them to type the truth and then
wonder why the plan did not move. Zero stays on the list after it is chosen,
because "I don't get there" is an answer and not a placeholder.

**The roster screen, and the component both screens now share.** States were
editable only on a goal row, so a construct with no goal could not be recorded —
and the planner charges for every track it has not been told about. The new
screen fetches upgrade graphs **only for the entities on it**, unlike the goal
screen, which fetches every graph in the catalog: fine for one construct, not
fine for a second game with hundreds. The chips-and-dropdown control is now one
component used by both, deliberately — two editors of one aggregate that drift
apart is how a reader gets two answers to "where am I". An opened row with no
states is local only, because an entity with no states *is* one that is not on
the roster (V13), and a placeholder row would invent a third thing between owned
and not.

**Measured.** 420 backend tests, 0 skipped, 0 failures (419 before; the new one
is the measures route, asserting the empty case and the sort together). Frontend
**16 → 28**: three on the remembered answer, four on the state control, five on
the ladder. Both new screens driven in a browser at desktop and phone width, the
roster surviving a reload — which is the outbox flushing and the server handing
it back, not local state.

**What is left of N35, and it is not nothing.** A `progress:` line still renders
as its bare slug — `character-exp` beside a properly named `Cogs` — and so would
a `choice:` line; that needs a display name from somewhere, which is a bundle
field and a sequence 7 rather than a frontend fix. **And no shadow price has
still ever rendered** — but this session found out *why*, which is progress of a
kind: on this plan the pricing re-solve reported `One more inver-shard-lacrimosa
is not obtainable`, so the item is at its bound and the map is legitimately
empty. The code path is not dead; it has never been given a plan with slack in
it.

**A mistake worth recording so the next session does not repeat it.** `npx
prettier` was run on three source files with no config in the repo, so it applied
prettier's defaults — double quotes, 80 columns — and reformatted 456 lines of a
269-line change. **This project has no prettier config and its files are
hand-formatted at roughly 100 columns with single quotes**; `npm run lint` names
eslint and there is no eslint config either. Recovered with
`git show HEAD:<path> >` per file and the edits redone by hand. Do not run a
formatter here until somebody commits a config for it.

**And the tracker was found 56 lines over its own limit, unrecorded.** 606 at
session start, 614 after this session's findings. The ledger had not been written
in since the twenty-eighth. *Status* was rewritten as the rule instructs and the
file is back at **550** — but **the rewrite is not what did it**, and the
[ledger entry](#the-line-count-ledger) says so in detail: rewriting `Status`
saved two lines, and reflowing eight over-narrow blocks saved sixty-two. Worth
reading before the next session trusts the rule to hold the number.

**2026-09-21 (thirty-fifth) — N36: the verification the file kept asking for, and
the two reasons it had never worked.** The third of the four deferred defects,
closed within two days of the list being written. It was scoped as paperwork —
*"not a code change, an hour"* — and the hour was right for the paperwork. The
code change was the finding.

**Session start, the remote check the tracker demands.** The file named PR #33 as
the newest merged; #34 (B6) and #35 (N34) were merged too, so it was one session
stale in exactly the way the instruction anticipates. `dev` was clean and equal to
`origin/dev` at `6414441`, and the unbuilt-commit trap was **not** sprung —
though B6 now means a bare `dev` push is built anyway, so that check has less to
catch than it did.

**The paperwork half, and it confirms the file.** Both snapshots fetched clean on
the first attempt; Docker answered 29.8.0. 419 tests across all modules, 0 skipped,
0 failures, and all 16 snapshot-gated tests ran — 5, 3 and 8 across the three
classes. Nine benchmark agreements on the solver's yields, five on raw point
estimates, 3 880 Activity against the guide's 4 017, three disagreements over 25%
and every one on a small sample. p95 **1 808 ms** against the recorded 1 807, and a
cached repeat of **0 ms** against the recorded 2 — both noise, both written down as
noise rather than quietly corrected, because the point of a dated record is that
drift is visible and this is not drift. It all lives in
[docs/benchmarks/snapshot-gated-runs.md](../benchmarks/snapshot-gated-runs.md),
shaped to be appended to per run.

**The half that was not paperwork.** Running the documented workflow produced
`BUILD SUCCESSFUL in 5s`, `:app:test` `UP-TO-DATE` — from the run that had no
snapshots. The property was never forwarded to the test worker, so the script's own
custom-directory mode sent the tests to the default path and all 16 skipped
silently; and the snapshot directory was not a task input, so fetching it changed
nothing Gradle could see. **Standing caveat 1 has been instructing sessions to do
something that could not work since it was written.**

**The uncomfortable part.** The second fault is the identical bug as `data/bundles`
and `AuthoredBundlesTest` — and that fix sits *ten lines above it in the same
file*, with a comment spelling out the failure mode and noting it was measured
rather than feared. Fixing one instance of a bug did not cause anyone to look one
screen down for the next. **This is the third item in a row whose real content was
not what its entry described** — N33 widened a flaw it did not name, N34 falsified
half of N35, N36 turned out to be a code change. The pattern is worth naming: **a
defect that has only ever been described stays true; going and running it is what
turns it into something else.**

**Both fixed and both proven by measurement**, because a build fix that is only
read is the same category of thing as a caveat that is only followed: empty
directory → 16 skipped; default path → 0 skipped; repeat → `UP-TO-DATE` in 957 ms;
snapshots moved aside → the task re-ran and 16 skipped, where before it would have
stayed up to date and reported 0. **Pointing the property at an empty directory is
now the cheapest local reproduction of CI's behaviour**, and the input is `optional`
so that absent snapshots stay a skip — ADR 0009's whole point — rather than a
build failure.

**What was deliberately not done.** No attempt to make CI run these 16; that would
need vendoring the data and ADR 0009 forbids it. And the run is one machine once,
so it is a dated baseline and not yet a regression guard — the second entry in that
file is what makes it one.

**2026-09-21 (thirty-fourth, continued) — N34: a roster entry holds a set.**
[ADR 0027](../adr/0027-a-roster-entry-holds-the-states-an-entity-has-reached.md),
`V13`. The second of the four deferred defects, closed the day after the list
was written.

- **The five pieces the item named were the five it took.** Migration, record,
  `achieved`, wire format, sync patch shape. Worth recording because N20, N32
  and N33 each found a sixth piece their plan had not named; this one did not,
  and the difference is that N34's plan was written by a session that had just
  finished walking the same five for N20.
- **Measured end to end, through a browser and a real Postgres.** A reader at
  `promote-6` asking for `promote-9` was charged **127 500 Cogs and 90 000
  character EXP over six steps**. The same reader, having said they are also at
  `level-80`: **127 500 Cogs, no EXP, three steps.** The EXP vanishes because
  the three level steps it paid for were already climbed. That is the charge
  ADR 0026 explicitly could not remove.
- **The first measurement was wrong and the mistake is worth keeping.** Testing
  with `promote-6` + `level-40` showed **no change at all**, which looked like a
  broken feature for a minute. It is not: `level-40` sits *behind* the
  `level-45` that ADR 0026 already credits through `promote-6`'s gate, so it
  adds nothing. **The gain only appears when the recorded state is ahead of what
  the gates imply**, and a future session testing this will reach for a low
  level first exactly as this one did.
- **The frontend shipped wrong twice before it shipped right, and no test ever
  failed.** First a `select multiple`: 4 visible rows out of 68 states, "don't
  own her" gone because clearing a multi-select needs an undiscoverable
  ctrl-click, and no way to see what was chosen without scrolling. Replaced with
  chips plus an add-one dropdown. Then the chips were styled `btn-quiet` — the
  same class as the `↑ ↓ ×` controls at the end of the same row — and **the
  maintainer looked straight at `promote-6 ×` and reported seeing no chip.** A
  value that looks like a control is not a value. They are `.chip` now, filled
  and rounded with a dimmed `✕`. **All 16 frontend tests passed at every one of
  those three stages**, which is the whole of standing caveat 2's frontend
  cousin: jsdom computes no layout, so appearance is a person's job.
- **Two claims in the live tracker died today and were retired rather than left
  standing.** "No `progress:` line has ever rendered" — one did, `character-exp
  90,000`, in the maintainer's browser; it renders as the **bare slug** beside a
  properly named `Cogs`, which is a real defect and now N35's. And "no screen
  has ever rendered PGR" — three did. **N35 was rescoped rather than ticked**:
  what is left of it is `reach`, the slug, the shadow price and the absent
  roster screen.
- **What N34 did not do:** states are editable only on a goal row, so a reader
  cannot record a construct they have no goal for. Named in the ADR and in N35
  rather than left implied.
- **Build:** 419 tests, 0 skipped, 0 failures — up from 411, the 8 new ones
  being this change's. 0 skipped means the 16 snapshot-gated tests actually ran,
  so standing caveat 1 is satisfied for this build rather than assumed. **The
  first full run failed 4**, all test-side fallout from the deliberate wire
  break, one of them a PUT correctly answering 400 to the old shape.
**2026-09-21 (thirty-fourth) — B6: a push to `dev` builds.** One workflow file,
twenty lines, and the shortest gap on record between a defect becoming an action
and being fixed — the list that named it was written the day before.

- **The check that opens every session found the thing the session was about to
  fix, already broken.** *Status* named PR #31 and was one session stale: #33 had
  merged at 03:09:07Z. Worse, `cc9e7d1` — the thirty-third session's own last
  commit, the one that *wrote B6 down* — was sitting on `dev` with no PR open and
  **had never been built**. Tenth occurrence, first one found in the working tree
  instead of recounted from the archive. **The instruction to re-check the remote
  rather than trust the file earned its place again.**
- **The fix is `push: branches: [main, dev]`.** Exit criterion met by run
  `35576845184` on `cfa6fe4`, pushed with `gh pr list` returning `[]`, green.
  Before it, no run in the history has event `push` and branch `dev` — the
  absence is as much the evidence as the presence.
- **The decision worth defending is the one that looks like a mistake.** The
  concurrency group was left keyed by `github.ref`, which means a push to `dev`
  while a PR is open from `dev` now runs CI **twice**. The obvious tidy-up —
  `github.head_ref || github.ref_name`, so push and PR share a group — is wrong
  here, because `cancel-in-progress: true` would then have the push cancel the
  older run of the pair, and the older run is routinely *the PR's own required
  check*. **A cancelled run is not a passing one.** The duplicate is the price of
  the trigger and it is cheaper than a check that intermittently disappears.
  Written into the workflow as a comment, because the next person to see two runs
  will reach for exactly that fix.
- **B6's other half is a condition, not code.** The `deploy` job is still
  `if: false`; when B5 turns it on it **must** be gated to `refs/heads/main`, or
  an ungated deploy under this trigger ships every commit that lands on `dev`.
  That hazard is the entire reason B6 was placed before B5, so the requirement
  now sits in the deploy job's own comment rather than in this file alone.
- **What B6 does not buy, and the tracker now says so:** the trigger does not
  watch the *merge*. PR #25 merged before its own run finished and was green by
  luck; that is untouched. *Wait for the run before merging* is still a person's
  job, and is now the only half of the old warning that is.
- **One thing left undone and named here so it is not discovered again:** this
  file is **605 lines** against its own 550-line limit, and the rule says to
  **rewrite a section, not shave it**. The thirty-third session's four new
  actions pushed it over; B6 gave three lines back. The next session that touches
  *Next actions* or *What is still unverified* should take the rewrite rather
  than adding to them.

**2026-09-21 (thirty-third, continued) — the deferred defects, audited into
actions.** No code. [PR #33](https://github.com/kietnt4412/storm_almanac/pull/33)
opened with the session's two commits; backend and frontend green, `deploy`
correctly skipping.

- **The finding is about the shape of this file, not about any one defect.**
  Asked to list everything skipped since the beginning, the list came almost
  entirely *out of* the tracker — *Status*, the two standing caveats, *What is
  still unverified*, and the archived qualifications. Every item was written
  down. **Not one of them was an action.** A defect that is only ever described
  is one nobody picks up, and four of them had been described for weeks.
- **Four became next actions**, each with a stated reason for being on this side
  of B5 rather than after it: **B6**, CI running nothing on a push to `dev` — the
  cheapest fix in the file, and B5 adds a `deploy` job that must never fire from
  an untested commit; **N34**, `Roster` holding a set of states, the half
  [ADR 0026](../adr/0026-a-crossed-gate-is-a-reached-state.md) left, whose wire
  format is published the moment a stranger loads the page; **N35**, rendering
  PGR and sending `reach`, which is Phase 4's own exit criterion in disguise;
  **N36**, running the upstream fetch once so the performance numbers stop being
  of unknown age.
- **One item turned out to belong to an existing action rather than a new one.**
  The gacha multi-copy pessimism — 280 pulls quoted where the truth is 200 — is
  fixed by the shop exchange N28 already carries, so it was written into N28
  instead of duplicated beside it.
- **One archived qualification was stale.** The seventeenth session recorded
  three shapes the model cannot express; **a lifetime purchase limit has been
  expressible since [ADR 0020](../adr/0020-a-limit-that-never-resets-is-offered-whole.md)**,
  and PGR sequence 2 publishes two rows that use it. Corrected in place by
  appending rather than by editing the original, because an archived claim that
  quietly changes is worse than one that says when it stopped being true. The
  other two shapes are still unexpressible and still have no ADR.

**2026-09-21 (thirty-third, continued) — the roster flaw, half closed.**
[ADR 0026](../adr/0026-a-crossed-gate-is-a-reached-state.md). No migration, no
wire change, no published version moved.

- **N33 turned a latent flaw into a live wrong answer, and this is the same
  session paying for it.** Gating all thirteen Promote steps meant a reader
  recorded at `promote-6` asking for step 7 was billed 60 000 character EXP —
  the ladder from Lv 1 — for a Lv 50 the game had already made them reach.
- **The expected fix was a schema change and it was not needed for this half.**
  A gate is not a guess about a player; it is a condition the game enforced
  before letting them take a step, so standing past that step *is* proof the gate
  was met. `achieved` now follows `requires` as well as `fromState` when walking
  back from the recorded state. **The roster, the schema, the wire format and the
  frontend are all untouched.**
- **Only what every parent demands is claimed.** Several upgrades into one state
  are either one step at several prices, which share their gates, or different
  routes, where which was taken is unknown. `pathTo` refuses the second shape, so
  the intersection is a safeguard rather than a live case — but it is the
  difference between an exact claim and a probable one, and this project does not
  ship probable ones.
- **The number, against the published bundle:** step 7 from `promote-6` costs the
  **20 000 EXP between Lv 45 and Lv 50** — 7 Pod (L), 2 shop purchases, 30 Cogs
  purchases, **3 runs and 90 Serum**. Charged from Lv 1 it was 60 000 EXP, 20
  Pods, 4 purchases, 6 runs, 180 Serum. **The reader was being billed double.**
  The test computed the arithmetic by hand first and passed on the first run.
- **What is still broken, stated so nobody reads this as done.** A reader whose
  recorded state is *behind* the gate gets no credit — someone at `promote-0` who
  levelled to 80 anyway is still charged the ladder, and **PGR permits exactly
  that**, because levels are not capped by rank (maintainer, 2026-09-21, and it
  is why the thirteen level gates exist at all). That half wants `Roster` to hold
  a **set** of states, which is a migration, a breaking roster wire format, the
  offline sync patch shape and a frontend that has never rendered this game.
- **A plan can now be cheaper than before for the same inputs**, which is the
  direction this project is normally careful about. It is safe only because the
  claim is about what the game enforced rather than what a player probably did.

**2026-09-21 (thirty-third) — N33: the level ladder is priced end to end, and a
gate is a link in a chain.** Sequence 6 authored, **not yet published**. The
reading was the maintainer's and took one sitting.

- **The premise the item was written on was wrong, and finding out cost one
  screenshot.** N33 said the twelve unpriced gates needed the per-level EXP
  curve. They did not. The bundle records *a demand in Pods*, not raw EXP — the
  Lv 80 row was already pinned that way — so what a gate needs is **the smallest
  multiple of 1 000 whose Level Up preview shows that level**, because a Pod (M)
  is 1 000 and nothing smaller can be spent. The maintainer's first screen (one
  Pod M reaching Lv 10) priced two gates by itself, and their worry that the
  smallest Pod overshoots Lv 2 turned out to be the answer rather than the
  obstacle: **Lv 2 and Lv 10 both cost 1 000, so the link between them is free.**
- **Twenty-two readings, none of them destructive.** The Level Up preview reports
  the level a selection *would* reach, so the whole ladder was bracketed on a
  fresh Lv 1 construct without pressing the button once. Each gate is pinned by a
  pair — the total that reaches it and the total 1 000 lower that does not.
  Cumulative from Lv 1: **1 000, 1 000, 4 000, 10 000, 25 000, 40 000, 60 000,
  89 000, 130 000, 186 000, 263 000, 365 000, 497 000.**
- **Five gates were first bracketed 2 000 or 3 000 wide and were deliberately not
  written.** A Pod (L) is 3 000, so crossing a threshold by adding whole M pods
  to a run of L overshoots. One more selection each closed them, and **all five
  landed at the top of their range** — which is what writing the upper bound
  would have assumed. They are in the bundle because they were read, not because
  the guess would have been right.
- **The track had to become a chain, and that is a `DemandResolver` fact, not a
  preference.** `pathTo` walks backwards through `fromState`, summing every row
  it crosses, and `counted` skips rows already paid. Thirteen rows each hanging
  off `level-1` with a cumulative figure would have charged the ladder **twice**
  for a plan wanting Lv 40 for one Promote step and Lv 80 for another. As a
  chain it is paid once. The published `level-1 -> level-80` row is now the last
  link, `level-75 -> level-80` at 132 000; **the sum is still 497 000 and no
  published plan moved** — `AuthoredBundlePlanTest`'s *"her last rank is gated on
  level 80 … 1 470 Serum"* passes at the same number, which is the proof it was a
  reshape and not a repricing.
- **What this actually fixes is the partial plan.** A reader going to HERO paid
  the ladder either way. A reader stopping at Promote step 7 paid Cogs and **no
  EXP at all**, because the level its screen demands lived in a JSON comment.
  **That also makes the roster's one-state-per-entity flaw worse rather than
  better** — now every step charges a level the reader may already hold, where
  before only step 13 did. The gates are right; the roster is wrong, and unfixed.
- **The readings were taken on Luna: Oblivion, not on Helentine: Lacrimosa**, and
  transferred on the maintainer's report that S-rank constructs share a level
  curve. That report was **cross-checked, not taken**: the 2026-09-19 Lacrimosa
  readings of +1 000 -> Lv 10 and +3 000 -> Lv 17 reproduced exactly, and the
  (496 000, 497 000] bracket for Lv 80 reproduced in both directions. Three
  agreement points at both ends of the ladder. Luna is not added as an entity —
  she would arrive with a level track and no Promote, Skill or Evolve.
- **A screen field was read and rejected.** The Level Up bar shows `EXP 0/20` at
  Lv 10, Lv 20 and Lv 80 alike. It does not vary with level, so it cannot be a
  per-level requirement, and it is recorded in the provenance entry as *not
  used* rather than left to be rediscovered and chased.
- **One test had to change, and the change is the point.** `GameDataIngestTest`
  pinned `helentine-lacrimosa-level-80` at 497 000 — fine when the ladder was one
  row, wrong the moment it was thirteen. It now asserts that **the links sum to
  497 000**, which survives every future split of the chain instead of breaking
  on it. 411 tests, 0 skipped, `BUILD SUCCESSFUL`.
- **The loop ran and was clean.** Preview reported **26 changes and nothing
  else** — twelve new level rows, `level-80` re-pointed `level-1 → level-80` to
  `level-75 → level-80` and re-priced 497 000 → 132 000, and twelve promote steps
  gaining a `requires`; step 13 unchanged, because it already had one. The new
  provenance entry carries **13 facts**, taking the bundle to **116, all
  first-hand**. Published 2026-09-21T02:49:27Z as sequence 6, and the re-preview
  read back as *no changes* — seven for seven.

**2026-09-21 (thirty-second) — N20: the day boundary is a property of the game.**
[ADR 0025](../adr/0025-the-day-boundary-is-a-property-of-the-game.md), `V12`,
and **PGR sequence 5 published and read back as no changes**. No new reading.
The thirtieth session's plan was followed as written, and its "five pieces, and
the work is the fifth" held — with one piece it did not name, found by running
the tool rather than by a test.

- **The remote at start:** `dev` level with `origin/dev`, its tree identical to
  `origin/main`, no open PRs. **PR #31 has merged since the tracker's note about
  #30** — the trap re-armed for the ninth time and was caught by looking.
- **The maintainer picked N20 from the three held items**, which is what the
  tracker's *Held* section asks for. N33 needs a reading only they can take and
  B5 needs their accounts; N20 was the one a session could finish alone.
- **What was wrong.** `EnergyMip.matchingDays` read `atZone(ZoneOffset.UTC)`,
  with a javadoc that said in as many words that this was a placeholder rather
  than a decision. PGR resets at **05:00 UTC**, so 03:00 UTC on a Monday is
  Monday to the planner and **still Sunday** to a player. Rotation is a shared
  capacity over subsets of weekday restrictions, so a wrong start day does not
  fail — it moves a whole day of energy into the wrong bucket, silently.
- **`GameAgnosticismTest` was always blind to this, and still is.** It scans the
  guarded source roots for game names; `UTC` is not a game name. The invariant is
  *no `if (game == …)`*, and this was the other shape — a constant right for no
  game in particular. **What caught it was a javadoc a previous session was
  honest enough to write**, which is worth more here than the source scan was.
- **The shape.** `DayBoundary(ZoneId zone, int hour)` on `Game`, with
  `dayOfWeekAt(Instant)` doing `atZone(zone).minusHours(hour)`. **A zone rather
  than an offset**, because an offset cannot summer — there is a test that pins a
  civil zone at 09:00 UTC in July and 10:00 UTC in December.
- **Null is *unstated*, not midnight**, through parser, writer, schema and
  reader, and the fallback lives once in `Game.dayBoundaryOrDefault()`. Every
  version published before `V12` reads back claiming nothing and plans exactly as
  it did. **No variable is indexed by day** — the change moves one `DayOfWeek`
  and writes no constraint rows, so ADR 0013 is untouched.
- **The piece the plan did not name, and the preview found it.** `Facts` never
  flattened `Game` at all. So the first preview of sequence 5 said **"no
  changes"** — a version whose one difference moves every rotating stage's
  capacity, reported as nothing. The title is now one subject on the progression
  axis, `energy unit` and `day rollover`, and the second preview read exactly
  *`~ game 'punishing-gray-raven' · day rollover: unstated → 05:00 UTC`*. The
  test for it was written after the tool found it, not before.
- **`ZoneId.of("UTC+7")` is valid** and normalises to `UTC+07:00`; the first
  draft of the parser test assumed it was not and failed. A mistyped *region* —
  `America/New_Yrok` — is the refusal worth having, because it looks right.
- **Sequence 5 published 2026-09-21T01:11:15Z and read back as no changes**, the
  sixth sequence in a row to do so. It moves no plan in that bundle: nothing in
  it rotates by weekday. It is written so the first rotating stage does not have
  to remember.
- **The fact ledger cannot carry this fact.** `factRefs()` covers items,
  entities, sources, sinks and banners — not `Game` — so the boundary has no
  `factProvenance` row, exactly as `energyUnit` has none. Its reading is
  `equipment-and-resource-screens`, and only the bundle comment and ADR 0025 say
  so. **104 facts, unchanged**, which is why the provenance table did not move.
- **R1999 was left alone on purpose.** Its 05:00 UTC−5 is second-hand, the
  Kornblume adapter is a never-shipped cross-check, and **its stage table does
  rotate** — so declaring a boundary there would move the nine benchmark
  agreements on the strength of a number nobody here read. Phase 11's business.
- **Build:** 401 → **411 tests**, 0 skipped locally, green in 4m 04s. **All 16
  snapshot-gated tests ran and passed**, so standing caveat 1 is satisfied for
  this session rather than assumed. CI will show **395**.
- **Not done, and named:** `Availability.opensAt` is still read by nobody, and
  this change deliberately does not fix it — a rollover hour and an unread banner
  window are different problems that share a package. Nothing renders any of this
  in a browser, and **no screen has ever rendered PGR**.

**2026-09-21 (thirty-first) — N30: an expiring grant is a deadline the plan
reports, not a schedule it builds.**
[ADR 0024](../adr/0024-an-expiring-grant-is-a-deadline-the-plan-reports-not-a-schedule-it-builds.md).
No new reading, no migration, no bundle change, and **`EnergyMip` gained no
variables and no constraints** — the change is two lists on `Outcome` and four
sentences. The thirtieth session's plan was followed as written, except on one
point where it was backwards, below.

- **The remote at start:** `dev` level with `origin/dev`, trees identical to
  `origin/main`, no open PRs. **PR #30 has merged since the tracker's note about
  #28** — the trap re-armed for the eighth time and was caught by looking, which
  is the only thing that has ever caught it.
- **What was already true, and is why this was cheap.** `occurrences` has
  truncated a reward's cadence against its own `closesAt` since the time axis
  landed, so the *supply* was never wrong. `Availability.isExpiring()` has
  existed since phase 1 and was read by nothing.
- **The two silences, and the second is the one that mattered.** A grant the
  plan leans on that closes inside the horizon had its count right and its date
  nowhere. A grant closing *before* it can pay once was removed by `claimable()`
  with nothing said at all — so a plan made dearer by an event that ended on day
  four was **indistinguishable from a plan that was always that dear**. That is
  the exact mirror of ADR 0022's `withheldGrants`, and the two are now computed
  four lines apart on purpose.
- **A lapse is a window, not a cadence, and the predicate is the whole trick.**
  "Has zero occurrences" is the wrong test: a monthly reward in a seven-day
  horizon has zero of those and has lapsed nothing. The test is that the close
  is *what* removed it — `occurrences(...) == 0` **and**
  `cadence.occurrencesIn(horizonDays) > 0`. Without the second clause the note
  blames a deadline for a cadence and tells a reader to hurry over a window that
  runs for another year. There is a test named after exactly that.
- **The refusal was wrong too, and it was free to fix.** An item whose only
  source is a grant whose window has shut was refused with *"does not come round
  inside a 30-day horizon"* — true, and it sends the reader off to wait for
  something that is never coming back. It now names the date: *"the horizon
  holds 4 of them and the window holds none"*.
- **The tracker's `FEWEST_DAYS` framing was backwards, and this is the
  correction.** It read *"a shorter horizon is the one thing that keeps an
  expiring grant in reach"*. It is not. `occurrences` takes
  `min(horizonDays, daysUntil(closesAt))`, so **shortening the horizon never
  raises an expiring grant's claims and can lower them** — when the horizon the
  search settles on ends before the window does. The sentence the code says is
  the true one: a longer plan collects no more of them, a shorter one may
  collect fewer. Written up as a consequence of ADR 0024 so the wrong version
  does not come back.
- **One arithmetic, one place.** `daysUntil` is new, and every truncation
  against a `closesAt` — rewards, shops, the stage closing cap — now goes
  through it. Two copies of that rounding is a plan telling a reader they have
  three days to collect something it counted four of.
- **Left deliberately, and named in the ADR:** `opensAt`. A reward whose window
  has not opened is dropped whole by `open()` with nothing said, including one
  that opens on day two of a sixty-three-day plan. The direction is the safe one
  — the plan counts no income and is dearer than the truth — and it belongs with
  **N20** and the banner's unread opening time rather than here. `Lapsed`
  deliberately does not cover it: "this closed" and "this has not opened" are
  different sentences, and one record for both would be the third silence rather
  than the end of the second.
- **Build:** 393 → **401 tests**, 0 skipped locally, green in 3m 50s. **All 16
  snapshot-gated tests ran and passed**, so standing caveat 1 is satisfied for
  this session rather than assumed. CI will show **385**.
- **Not done, and named:** nothing renders this in a browser. The notes reach a
  reader through the plan view, which renders every note and **has still never
  rendered PGR**.

**2026-09-20 (thirtieth) — N31: a drawn guarantee is a rate curve, not a state
dimension.** [ADR 0023](../adr/0023-a-drawn-guarantee-is-a-rate-curve-not-a-state-dimension.md),
`V11`. No new reading — this is the 2026-09-18 PGR reading finally written down.
**N33 was skipped deliberately**: it is a reading of the game client and the
maintainer's, not a session's.

- **The remote at start:** `1f451b2` on `dev`, tree clean, PR #28 merged and
  nothing outstanding. Not re-checked against `gh` — no push was made.
- **The expensive option was not needed, and the argument is three lines.**
  N31 said the exact chain needed the drawn threshold in its state or 21 mixed
  chains. Condition on `c` misses: a miss is a failed curve roll *and* an absent
  wall, and the roll is independent of the draw, so `c` misses are impossible for
  every `W <= c` and equally likely for every `W > c`. The posterior is uniform
  on `{max(drawnFrom, c+1) .. hardAt}`, depends on `c` alone, and the chance this
  pull is the forced one is `1/(hardAt - c)`. **`MarkovBannerEngine` was not
  touched.** The state space kept three dimensions and got a hundred deep.
- **`hardAt` was not repurposed**, which is why nothing published had to move.
  It still means the pull at which the rarity is certain; `drawnFrom` is the
  bottom of the range. A `drawnFrom` equal to `hardAt` is refused by name — a
  range of one is a fixed wall spelled the long way, and two spellings of one
  banner is a diff nobody can read.
- **The cross-check paid for itself, and it is the first disagreement since
  Phase 5 closed that was not rounding.** The simulation draws rather than
  reusing the integrated curve — deliberately, or the agreement would be
  circular — and it drew from the *prior*. A player carrying 85 misses against a
  threshold drawn from 80–100 cannot have drawn 80; the simulation handed that
  player a forced hit a quarter of the time. **Exact 0.381856 against simulated
  0.557824**, 17.6 points, and **the chain was right**. The fix is one `max` in
  `drawWall`. Note the shape of the bug: plausible, monotone, and wrong in the
  direction that flatters the player — what a single engine ships.
- **The test suite had to be told where to look, and this generalises.** The
  agreement questions are generated at 1, 10, half the wall, the wall and the
  worst case. For a wall of 100 that is 1, 10, **50**, 99, 100 — all below the
  drawn range or at certainty's doorstep, so the only band where the two roads
  can disagree was never asked about. **A question set generated from a banner's
  parameters is not automatically a question set that probes them.** 96 → 108
  questions; worst gap unchanged at 0.110 points at 2.22 standard errors.
- **Two numbers now agree that did not have to.** The research note computed the
  Themed pool's long-run share by hand as **2.021%** against the publisher's
  advertised 1.90%. The chain, which knows nothing about the note, says 49.488
  pulls on average — **2.0207%**. Q4's PGR disagreement is now the model's rather
  than a spreadsheet's, which is what it has to be before anything is concluded
  from it. It does **not** answer Q4; it makes the question sharper.
- **The fixture correction N31 asked for was wrong in both halves.**
  `Banners.grayRavenFloating()` had 1.50% with a 70% featured rate and a fixed
  wall at 80. The client pairs 1.50% with **100%** and the drawn range, on one
  screen — the axes pair. So ten pulls is now ten base rolls (no split to lose)
  and certainty arrives at 100, not 80. Three published-rates tests moved.
- **The column was proved against Postgres, not only the parser.** The
  proving-ground fixture gained a second banner, `warden-return`, whose guarantee
  is drawn 40–50, so `drawn_from` crosses the writer, the parser, `V11` and the
  JDBC round trip like every other field. `Facts` carries it too — without that a
  patch turning a fixed wall into a drawn one reports `hard at 80 -> 100` and
  reads as a nerf. One brittle assertion moved with the fixture: the second-hand
  refusal names **21** facts, not 20. That count is hand-written and will move
  again the next time the fixture grows; it is asserted because the difference
  between "not first-hand" and "twenty facts are not first-hand" is a different
  decision for whoever reads it.
- **Build:** 384 → **393 tests**, 0 skipped locally, green. CI will show **377**,
  the 16 snapshot-gated ones skipping as always.
- **Not done, and named:** no PGR banner is in the bundle. The archetype is
  expressible now and a banner still declares no pull currency and no price —
  **N28** — so nothing was published and there is no sequence 5.

**Then all four Phase 4 items were planned, one decided and three held.** The
maintainer asked for a plan per numbered action, chose option 2 on N30, and held
the rest to decide later — with the condition that **Phase 4 does not close until
each is done or explicitly cut on the record**. That condition is now on the
Phase 4 board entry. The plans, so no session re-derives one:

- **N30 — decided: a deadline, not a schedule.** The two candidates were (1) give
  the event grant a cost in days and let `FEWEST_DAYS` schedule it, which wants a
  time index and so collides with ADR 0013, and (2) let it stay supply that
  `occurrences` already truncates, and report the deadline. **(2) was chosen.**
  What is already true: `occurrences` truncates against `closesAt`,
  `Cadence.EVENT`/`ONE_OFF` cap at one, and `Availability.isExpiring()` exists
  and is read by nothing. Three gaps: a **deadline note**; a **lapsed note**,
  because `claimable()` filters a reward with zero occurrences out *silently*, so
  a grant that shut on day 4 of a 63-day plan vanishes rather than being reported
  — the exact mirror of ADR 0022's `withheldGrants`; and a **`FEWEST_DAYS`
  sentence**, since a shorter horizon is the only thing that keeps an expiring
  grant in reach. Shape: `Outcome` gains `expiringClaims` and `lapsedGrants`, two
  notes in `MipOptimizer` beside the existing pair, **ADR 0024**, and tests
  against `pg-event-1` — present in proving-ground 1.0, gone from 1.1 because the
  event ended, which is a ready-made fixture pair. No migration, no bundle
  change, nothing game-specific. **Explicitly out of scope, so nobody re-opens
  it:** no time index, no per-day variables, no scheduling of the claim.
- **N33 — the premise was wrong and the finding is worth more than the plan.**
  The maintainer asked to skip the per-level EXP and record "the level and the
  Cogs" instead. **Both are already recorded.** The thirteen level gates are in
  the bundle's own Promote comment — Lv 2, 10, 20, 30, 40, 45, 50, 55, 60, 65,
  70, 75, 80, with step 11's Lv.70 read through an overlapping weapon model — and
  the thirteen Cog costs are rows, 5 000 up to 100 000, **542 500 in total, Cogs
  only**. What is missing is only the EXP, and **the bundle comment already
  explains why that blocks the gates; the code confirms it.**
  `DemandResolver.payUpTo(required, false)` passes `mustBeReachable = false` for
  a gate, deliberately, because a gate may name the start of a track
  (`promote-0`). So a required state that **no upgrade produces resolves to an
  empty path and costs nothing**: `"requires": ["level-40"]` today would be a
  gate every plan meets for free — which is *worse* than prose, because prose is
  honest about being unread and a free gate looks priced. So the twelve gates
  need the EXP or they need nothing. **Two things make it cheaper than it looks.**
  It is **incremental** — the ladder is a chain, so any prefix priced unlocks
  exactly that many gates, and Lv 80 is already pinned at 497 000; one sitting
  need not do twelve. And there is **a code-only piece available now**: a state
  required by a gate but appearing as neither a `toState` nor any upgrade's
  `fromState` can never be produced by anything, and today that is
  indistinguishable from a legitimate track start. Refusing it by name in
  `AuthoredBundlesTest` turns the trap into a build failure — at present the
  guard is a human holding it up in a JSON comment.
- **N20 — five pieces, and the work is the fifth.** `EnergyMip.matchingDays`
  (line 682) calls `from.atZone(ZoneOffset.UTC).getDayOfWeek()`. The schema half
  is exactly the **V11** shape just executed: two fields on `Game` (rollover zone
  and hour), parser, writer, migration, JDBC round trip, optional so every
  published bundle reads back meaning what it claimed. **The real work is step
  five**: a 05:00 boundary changes *which weekday a plan starting before 05:00
  begins on*, and the rotation grouping shares capacity over subsets of weekday
  restrictions, so a wrong start day moves a whole day of energy into the wrong
  bucket. **Two traps:** do not let a rollover hour become a per-day variable
  (ADR 0013 is what holds p95 at 1 807 ms), and do not try to finish
  `Availability.opensAt` in the same change — the banner's opening time is still
  unread. Do it with PGR, whose 05:00 UTC reset was read 2026-09-19.
- **B5 — the sequence, and what a session cannot do alone.** Current state
  checked: `deploy` is `if: false` at `ci.yml:76`, its body still echoes
  *"TODO(phase-0) wire Fly.io or the VPS deploy here"* then `exit 1`, with
  `DEPLOY_URL: https://example.invalid`. Keep the skipped-is-honest property that
  comment argues for. **Two decisions first.** *One origin or two:* `SecurityConfig`
  writes the CSRF token into a cookie the page reads and the commented OAuth
  stanza uses `{baseUrl}/login/oauth2/code/{registrationId}`; locally the Vite
  proxy forwards `/api` and `/dev` to `localhost:8080`, so everything has been
  same-origin from day one — a Vercel rewrite preserves that and two real origins
  break session cookie, CSRF cookie and OAuth redirect at once. *The free tier
  sleeps:* decide warm-up ping, honest loading state, or acceptance **before**
  five strangers meet it. **Then:** (1) build the image locally, end to end, which
  has never been done — the in-container Gradle download was abandoned at 10%
  after twenty minutes — and check the `COPY` list against the module tree first,
  because it omitted `adapters/` for a whole phase; (2) Render service plus
  Postgres, where Flyway's **eleven** migrations meet an empty database outside
  Testcontainers for the first time; (3) Vercel with the `/api/*` rewrite; (4)
  register the OAuth client against the real redirect URI, the first exchange
  ever; (5) flip `deploy` to `main`, replace the TODO, keep the `curl --fail`
  smoke; (6) **delete `:modules:identity-dev`** — ADR 0017's trigger, and the
  actual end of B5. **Steps 2 to 4 need the maintainer's accounts**; a session can
  do 1, 5, 6 and the rewrite config.

**2026-09-20 (twenty-ninth) — N32 (5): a grant sized by the player is an answer
the reader supplies.** [ADR 0022](../adr/0022-a-grant-sized-by-the-player-is-an-answer-the-reader-supplies.md).
No new reading. **This closes N32**: all five shapes the first full bundle
refused are now writable. The EXP reading it was also carrying leaves as
**N33** — the item's criterion was the shapes, and it was met.

- **The remote at start:** PR #28 merged, `dev` and `main` with identical trees,
  nothing outstanding.
- **The problem, stated as three bad options.** The weekly Phantom Pain Cage
  pays Scars in nine tiers, 4 to 10 each, behind scores from 30 000 to
  1 100 000, and they add to the 56 a week the maintainer reports. Writing the
  56 promises every reader the top tier; writing the bottom tier is a different
  reader's week; leaving it out — which sequence 3 did — charges 500 Scars for
  Evolve to SS and names no way to earn one. **The size of the grant is not a
  fact about the game.**
- **So it is a fact about the reader, and the project already had one of those:
  `energyPerDay`.** `PlanController` refuses to guess it, on the grounds that a
  guess produces a plan that is wrong in days without being wrong in any way the
  reader can see. A weekly score is the same argument one step out, which is why
  `reach` went on `SolveRequest` rather than into the player schema.
- **One reward per rung.** `Reward.Requirement(measure, atLeast)`, the measure an
  opaque label like a state or a `Progress.kind`. Nine tiers are nine `WEEKLY`
  rewards, and the format learns nothing about ladders: a reader at 120 000
  clears three of them by comparison, not by rule. `V10` adds two nullable
  columns with a both-or-neither check — a measure read back without its bar
  would turn a tier some readers collect into one everybody does.
- **Withheld grants are dropped before the model, not bounded at zero inside
  it**, so every count a plan or a refusal reports is the game *that reader*
  plays. The refusal for the one tier they do clear says "1 reward(s)", not 9.
- **Three things the session measured rather than assumed.**
  1. **The solver claims 8 of the bottom tier, not 9.** Nine weeks of all nine
     tiers is 504 Scars against the 500 the shards cost, so it drops one claim
     of the 4-Scar tier and lands on exactly 500. That is the reward tie-break
     doing what it was written for, and the first assertion was wrong, not the
     code.
  2. **The refusal still named nothing**, because the demanded item is the
     *shard* and the Scar is the shop's *currency* — and `whyNotBuyable` stopped
     at "nothing available can supply it". So it now follows the currency one
     level down and names the **lowest** bar of the nine plus how many are
     above it. One level only: a refusal nobody finishes reading is a refusal
     that did not happen.
  3. **Existing plans did not move.** Every other test in
     `AuthoredBundlePlanTest` passed untouched, because an unanswered measure
     counts nothing — but the Cage grants Cogs and Skill Points, so those plans
     now carry the note saying what was not counted.
- **Sequence 4 of the bundle:** nine `phantom-pain-cage-*` rewards under a new
  provenance, `phantom-pain-cage-screen` (OBSERVED_IN_GAME, the maintainer's
  2026-09-19 screenshots of the Weekly Reward screen). *Preview* read **exactly
  nine additions**, 104 facts over six provenances, all first-hand. *Ingest*
  made draft 4 with the same nine. *Publish*: **2026-09-20T00:13:59Z**, and a
  re-preview against it reads **no changes**. The next correction is a
  sequence 5.
- **Three tiers are written short, always downward.** The 500 000 and 1 000 000
  tiles also pay a gold 5★ card and a 4★ chip, and the 700 000 tile a 4★
  portrait item; none has been opened, so none is a bundle item and none is in
  the rows. A tier that pays less than it does makes the plan dearer than the
  truth.
- **What it buys, worked out by hand first and matched on the first run:** a
  reader who says they reach 1 100 000 is planned Evolve S → SS in **63 days
  and no Serum** — 30 shards at 10 and 20 Scars is 500, nine weekly resets pay
  504. A reader who says 30 000 is refused. A reader who says nothing is refused
  *by name*, which is the sentence the session before could not produce.
- **384 backend tests (+9), 0 skipped locally**; 368 expected on CI, where the
  16 snapshot-gated ones skip. The upstream snapshot is present on this machine,
  so the local run included them.
- **Left undone, deliberately.** No screen asks the reader what they reach, so
  `reach` is retyped per request and no frontend sends it at all. A measure has
  no display name, so the screen that does ask will have a slug to show. Both
  are in ADR 0022's consequences, and the second is what would hurt first.

**2026-09-19 (twenty-eighth) — N32 (4): one step at several prices is a choice
the solver makes.** [ADR 0021](../adr/0021-one-step-at-several-prices-is-a-choice-the-solver-makes.md). No new reading.

- **The remote at start:** PR #27 merged with its PR run green (`35432700666`);
  the push-to-`main` run `35432960832` still in progress.
- **Parallel upgrades, not a new shape.** Two upgrades with the same entity,
  from-state and to-state are one step at two prices. So the record, the bundle
  format and the writer are unchanged. The one thing refusing it was V2's
  `upgrade_edge_unique`, which **`V9` drops**. Its comment ("a second row
  would be a duplicate cost") had been true of every game read until PGR.
- **The choice is demanded as an item**, as EXP is in ADR 0019:
  `choice:<entity>/<from>/<to>`, one unit, which `EnergyMip` makes from any one
  price. A price paid in EXP pulls fodder in the usual way. The plan's
  conversions name the upgrade paid, and the steps list reads `a or b`.
- **A route is still refused.** Several upgrades into one state from
  *different* states, or behind *different* gates, would change which gates get
  paid, and a demand vector cannot branch.
- **Two database tests asserted the old rule.** `GameDataSchemaTest` now proves
  a second price is stored. `GameDataIngestTest`'s all-or-nothing case needed
  a different constraint that the parser cannot see; it uses
  `upgrade_states_differ`, an upgrade from a state to itself. **The parser
  does not refuse that**, and only the schema does.
- **Worked out by hand before running, and matched on the first run:** a step
  priced as 3 ingots or 1 relic costs 40 energy through the relic, against 60
  through the ingots. Holding 3 ingots, it costs 0. A price in EXP beats 2 000
  gold, at 60 against 100.
- **Not in the bundle.** Resonance's three prices are 150 of a 5★ chip item,
  234 Special Support Token and 246 Simulation Score. The chip item was read
  only by its icon ("the 5★ Memory Shard's icon"), and the Memory and Item tabs
  were never read. The row waits for the maintainer.
- **Later the same session, the row landed.** The maintainer first described
  "the blue one" as the farming stage's points. That would have been Simulation
  Score, the third price, which was already named. So they were asked which
  row they meant. They sent a screenshot of the item card: **5★ Memory Shard,
  Owned 4300**, the same count the Resonance screen showed beside the unnamed
  price. It is recorded under a new provenance, `memory-shard-card`
  (OBSERVED_IN_GAME).
- **Sequence 3 of the bundle:** two items (5★ Memory Shard, Special Support
  Token) and three `samantha-upper-resonance-by-*` rows,
  `upper-resonance-0 → upper-resonance-1`. No gate is written, because none was
  read. `AuthoredBundlePlanTest`, worked out by hand first: holding nothing,
  the only price that can be farmed is 246 Score, **exactly 3 runs, 90 Serum**.
  Holding 150 shards, the plan pays in shards and costs 0.
- **Published, at the maintainer's explicit request, with PR #28 still open.**
  The local database held sequence 2 as published at V8. The jar applied `V9`
  on startup. *Preview* read **exactly five changes**: the two items and the
  three rows. It showed 95 facts over five provenances, all first-hand, with no
  *NOT ours to publish* line. *Ingest* made draft 3 with the same five changes.
  Then came *publish*: **`punishing-gray-raven` Steering By Light, sequence 3,
  at 2026-09-19T09:18:31Z**. A second preview read *no changes*, which is the
  read-back. So a database holding versions published before V9 still reads
  them after it. The next correction is a sequence 4.
- **No gate, confirmed afterwards.** The maintainer reports that Resonance
  requires neither Overclock nor level. That is a report from playing, not a
  screen reading. The published rows have no gate, so nothing changes, and 90
  Serum stands as the whole price.
- 375 backend tests (+11 over the session), 0 skipped locally.
- **Not driven in a browser.** The shortfall page's `one of: …` line is tested
  on the arithmetic (`ShortfallChoiceTest`, a bundle parsed from text). No
  frontend has rendered a `choice:` line.
- 373 backend tests (+9) at the first commit.
**2026-09-19 (twenty-seventh) — N32 (3): a shop limit that never resets, and
Evolve S → SS plans.** [ADR 0020](../adr/0020-a-limit-that-never-resets-is-offered-whole.md).

- **The remote at start:** PR #26 merged, `dev` and `main` identical trees, the
  push-to-`main` run `35431786680` still in progress.
- **"Never" is a `null` period**, written `"never"` in a bundle and in
  `period_iso`. **No migration**, because the column is text and every older
  version reads unchanged. `Shop.purchasesIn` gives the whole allowance to any
  horizon of a day or more. That is the one place a plan rounds *for* the
  player. Rounding against the player would give zero, and nothing records how
  much a player has bought. So `MipOptimizer` adds a note whenever a plan buys
  from such an offer. The maintainer's `limitPeriod` enum was not taken: a
  `Period` already says everything the other values would.
- **A tiered price is two offers**, the cheap one capped at the discounted count.
  They need no shared cap, because least cost buys the cheap tier out first.
  `EnergyMipTest` proves both points on paper: 15 relics at 100 energy, and a
  thirty-first refused at a 365-day horizon.
- **A reading conflict, put to the maintainer before anything was written.** The
  tile says *"66% Off (was 30)"*; the maintainer had reported 20 each after the
  first ten. Asked how they knew, they answered **"20 each, seen in game"**. The
  bundle's provenance detail says the second tier is their reading of the tile,
  not the one transcribed. The note says no second screen has checked it.
- **Sequence 2 of the bundle:** two Phantom Pain shop rows, 10 at 10 Scars and
  20 at 20, both `"never"`. `AuthoredBundlePlanTest`: holding 2 shards and 460
  Scars, Evolve buys 10 + 18 and farms nothing; the note names both offers.
- **Found: one Scar short gives the generic refusal**, *"no combination of … 2
  shop offer(s)"*, with no item named. The per-item diagnosis looks for items
  with *no source*, and a held stock is a source that runs out. That was true
  before for any held currency. Here it is the usual case until the weekly Cage
  pays Scars (N32 (5)). The test pins the behaviour as it is, not as it should
  be.
- **`GameDataIngestTest` already round-tripped the whole authored bundle
  through Postgres** by record equality, so `"never"` was proven on the first
  run. An explicit assertion on the two rows was added so the proof is visible.
- **Previewed, then ingested and published at the maintainer's request.** The local database held sequence 1
  as published; the preview read exactly **two changes**, the two shop rows, and
  90 facts over four provenances, all first-hand. *Ingest* showed the same two
  changes, then *publish*: **`punishing-gray-raven` Steering By Light, sequence
  2, at 2026-09-19T08:40:51Z**. A second preview read *no changes*, which is the
  read-back. The next correction is a sequence 3.
- 364 backend tests (+6), 0 skipped locally.

**2026-09-19 (twenty-sixth) — N32 (1) and (2): gates are paid and EXP is fed,
and the first-hand plans got honest by 75% and 717%.** No new reading. The
bundle becomes **sequence 1** by writing down what sequence 0 had read and could
not hold. [ADR 0019](../adr/0019-a-gate-is-a-goal-inside-a-goal-and-progress-is-demanded-as-an-item.md).

- **PR #25 merged green, and merged before its own run finished.** The PR's
  `backend` job completed 07:46:05Z; the merge was 07:44:47Z. The push-to-`main`
  run `35430156822` went green afterwards, and the PR run `35430127397`
  executed `:app:test` rather than reading it from cache. So nothing was lost —
  but that was luck, not a check. The previous two merges had waited.
- **A gate is a goal inside a goal.** `Upgrade.requires` names states of the
  same entity; `DemandResolver` pays the path to each before the gated step,
  recursively, once. A step gated on a state reachable only through itself is
  refused by name rather than looped on. Checking a gate would say "not yet",
  and a whole-run plan is never *yet* — what a gate changes is the price.
- **Progress is demanded as an item.** `Upgrade.progress` is `(kind, quantity)`;
  `Fodder.progress` names the kind it pays; the demand line is
  `progress:<kind>`, which no kebab-case slug can collide with. Each fodder rule
  becomes one conversion per eligible item in `EnergyMip`, so "buy Pods, feed
  Pods" is priced exactly like "buy boxes, open boxes", and `SolveKey` covers
  progress for free because it already covers the demand map. A rule with no
  `progress` is **inert, not refused**: every rule in the published sequence 0
  has none, and `V8`'s column is nullable for the same reason.
- **Sequence 1 of the bundle:** `helentine-lacrimosa-level-80` (Lv 1 → 80,
  497 000 character EXP, the Pods-exact figure from the reading), `requires:
  ["level-80"]` on promote-13 only, 24 000 weapon EXP on Hear the Bell's
  Overclock row and 18 000 memory EXP on Samantha's, and a kind on all five
  fodder rules. **Only one of thirteen Promote gates is written**, because the
  EXP to any level below 80 is unread and a gate is only worth writing where its
  state has a price. So promote-12 still costs none of the level 75 it needs.
- **The new plans, each worked out by hand first, and each matched on the first
  run:** Samantha's Overclock **240 → 420 Serum** (60 Enhancer IV, six packs);
  Helentine's promote-13 **180 → 1 470** (166 Pod L, 34 packs); the same holding
  25 Pod XL, **180**, Cogs only. The old numbers were short by 43% and 88% of
  the goal. The goals had been picked from the ones the bundle could express in
  full, which was a way of not looking.
- **The shortfall page's EXP line counts held fodder at face value** — without
  it every reader reads "EXP owned 0" however many Pods they hold. Tested on the
  arithmetic (`ShortfallProgressTest`), **not driven in a browser**, and the
  frontend has never rendered a `progress:` line or shadow price.
- **Previewed at the maintainer's request, not ingested or published.** The
  local database was at V7 holding PGR sequence 0 (published 01:40:38Z). The
  jar's startup applied `V8` in 45 ms, then the preview read sequence 0 back
  through the new schema and diffed sequence 1 against it: **exactly nine
  changes** — five fodder kinds, two Overclock EXP costs, the promote-13 gate,
  one new upgrade — and 88 facts over four provenances, all first-hand, with no
  *NOT ours to publish* line. So a database holding a published version before
  `V8` still reads it after. PR [#26](https://github.com/kietnt4412/storm_almanac/pull/26)
  opened, and went green on `19c44f7` (run `35431309259`, `:app:test`
  executed).
- **Published, at the maintainer's explicit request:** *ingest* (the same nine
  changes), then *publish* — **`punishing-gray-raven` Steering By Light,
  sequence 1, at 2026-09-19T08:13:20Z** — then a second preview read *no
  changes*, which is the read-back. Both sequences are `PUBLISHED`; the next
  correction is a sequence 2.
- 358 backend tests (+15), 0 skipped locally.

**2026-09-19 (twenty-fifth) — Shops are in the solver, and the first-hand
bundle plans.** This is N30's shop half. The twenty-fourth session found that
nothing in the PGR bundle could be planned without shops, because Simulated
Battlefield pays a currency and everything else is bought with it. So shops
came before N32's gates and EXP, against the order N32's own text gives. Gates
and EXP make a plan more complete. Shops are what make a plan possible at all.

- **A purchase is a conversion, and only its cap is new.** Inside `EnergyMip`,
  crafts and shop offers are both `Exchange`s: consume something, make
  something, spend no energy. A purchase spends `price` of the currency on the
  whole offered stack. Its variable is capped at `periodLimit` × *whole*
  periods in the horizon (or until the shop closes). That rounds against the
  player, as reward cadences do: nothing says how much of this week's
  allowance is already spent, and a month counts as 31 days. **So a weekly shop
  gives nothing to a horizon under seven days.** That is conservative on
  purpose, and the refusal names the shop and says so. It may prove too
  conservative once a real weekly shop is ingested.
- **`Shop` now validates itself.** A free *and* unlimited offer is refused as
  unbounded supply, the same rule `Craft` applies to an empty recipe. A free
  offer with a limit (a daily pack) is kept, and gets the tie-break weight so
  a plan does not report a pack it never needed.
- **The first plans ever made from first-hand data, each worked out by hand
  first, and each matched the solver on the first run.** Helentine's Seeker
  System 1 → 18 is 172 Cog purchases, 3 Skill Point purchases and 5 runs:
  **150 Serum**. Samantha's Overclock goes stage → shop → box → open: 217 Cog
  purchases, 2 β and 2 α box purchases and opens, 8 runs: **240 Serum**.
  Hear the Bell's is refused, naming `weapon-overclock-core-i`. These are
  `AuthoredBundlePlanTest`. It reads the published bundle, so **a data
  correction now shows up in a plan**, and the test's arithmetic has to be
  redone whenever the bundle changes.
- **Those plans are cheaper than the truth, and the tests say so.**
  Samantha's 18 000 EXP and every Promote level gate are missing (N32 (1),
  (2)). The goals were picked from the ones the bundle can express in full.
- **The fixture's warden-insight-2 went from refused to 450 energy**
  (6 purchases, 8 crafts, 45 runs, all 30 logins and 4 quests; worked out
  in the test).
- **Decided without an ADR: purchases are reported as `Conversion`s under the
  shop's id**, not as a new `Plan` field, so the API and the page are unchanged
  apart from one heading ("What to craft and buy"). Reverse this if a reader
  needs to tell a purchase from a craft on the page. **Not driven in a
  browser:** the plan page has still never rendered PGR.
- 343 backend tests (+13), 0 skipped locally; frontend 15, typecheck clean.
  The repository fakes moved out of `PlannerAcceptanceTest` into
  `InMemoryPlanning` so both planner tests share them.

**2026-09-19 (twenty-fourth) — N27 done: PGR is farmed through a shop, and the
bundle grew from nine facts to a character.** The maintainer read the client
screen by screen for one sitting, and every value was asked after — screen or
report — before it was written. What landed is in N27's archive entry; what
matters for the next session is below.

- **PGR's farm is a shop, which reverses half of the twenty-second session's
  N30 rescope.** Events have no shops, but **Simulated Battlefield** (30 Serum,
  82 Simulation Score, fixed, no daily cap; a 2× event was running and was
  divided out) feeds a **standing Simulation Shop** that sells every Level,
  Promote, skill and Memory material the bundle names, with no purchase limits.
  Promote to HERO is 542 500 Cogs — about six runs. **Without shops in the
  solver, the bundle plans nothing.**
- **Screens overruled the maintainer twice and this session once, each caught
  by asking rather than choosing.** Skill levels do not all cost 1 SP + 2 000
  (they climb; 1 → 18 is 44 SP + 206 000 on the upgrade-all prompt); EXP to Lv
  80 is not 500 000 (165 L + 2 M reaches it: **497 000 is exact in Pods**); and a
  Memory *this session* took for 6★ from its frame is **5★** on its own screen. The maintainer
  was right about every structural claim — Overclock is one step, Authority
  Level does not move the score, the α and β boxes are fixed splits.
- **Currencies can be graded after all — on tiles, not cards.** Item cards
  show no stars; shop and reward tiles do. Cogs 3★, Simulation Score 4★,
  Phantom Pain Scar 3★. That takes most of the sting out of N28's rarity
  question.
- **The day boundary is read: server time is UTC, reset 05:00 UTC.** The home
  clock read 00:04 server time; this machine read 07:14 UTC+7 ten minutes later.
  That is **N20**'s number.
- **The format refused five shapes, now N32:** upgrade preconditions (every
  Promote step, Vestige, all of Awaken), an EXP demand for a level goal, a
  shop limit that never resets (30 shards), several prices for one state
  (Memory Resonance — and `DemandResolver` throws on a second route, which is
  also why skills are one chain with a derived **4 → 18 remainder** rather
  than a 1 → 18 shortcut), and a grant sized by the player's score (the weekly
  Cage, 0–56 Scars, which sum to exactly 56 across its tiers).
- **A cross-check worth copying:** Simulation Score went 366 425 → 366 401,
  which is +164 for one doubled run, −150 and −38 for the two box stacks. The
  shop prices and the run agree to the point.
- **Two slips of this session's own, both caught before a test ran green on
  them:** an awk `-v` with a Windows path silently inserted no rows while their
  provenance mappings went in — `AuthoredBundlesTest` failed on mappings naming
  no fact, which is a check nobody had written down it made; and two edits left
  a placeholder id and a duplicate entity in the file, removed by reading back.
- **Published, at the maintainer's explicit request, after PR #24 merged green:**
  Postgres started through `docker compose up -d postgres redis`, then
  *preview* (19 items, 5 entities, 87 facts over four provenances, no
  *NOT ours to publish* line), *ingest* (replacing the stale draft 0), and
  *publish* — **`punishing-gray-raven` Steering By Light, sequence 0, at
  2026-09-19T01:40:38Z**. A second preview then read *no changes* against the
  published version, which is the read-back. **The first published version in
  this repository whose every fact was read first-hand.** From here a
  correction is a sequence 1, never an edit. Block 4 stays cut, so this sitting
  is **not** a measurement of what sourcing a patch costs.

**2026-09-18 (twenty-third) — the first first-hand bundle exists, and it is nine
facts wide.** N27's other half: the 545-line PGR reading turned into
[`data/bundles/punishing-gray-raven-steering-by-light.json`](../../data/bundles/punishing-gray-raven-steering-by-light.json)
and taken through *validate, preview, ingest*. **Draft 0, nine facts, every one
first-hand** — `3 PUBLISHER_DISCLOSURE` (the research pool panels) and
`6 OBSERVED_IN_GAME` (the weapon upgrade screens). It is the first provenance
breakdown outside a synthetic fixture that prints no *NOT ours to publish* line.
**Not published**: the approval is a command the maintainer types, and publishing
an immutable version of data they had not read would be exactly the rubber stamp
`GameDataCli` is shaped to prevent.

**What landed:** five Overclock and Harmony materials with their read rarities,
two S-Rank Omniframes, the **Arrival Construct banner archetype in full**
(0.50% base, wall at 60, per-10 A-Rank floor, featured 0.70 with the Calibration
guarantee as `guaranteeAfterLoss: 1`, scope `BANNER_TYPE` on `arrival-construct`),
and the weapon EXP fodder rule at **300 per 4★ unit**.

**Nine facts out of that reading is the finding, and it is three refusals rather
than three omissions.**

1. **The Themed Construct banner is absent.** `PityRule.hardAt` is an `int`; PGR
   draws the wall uniformly 80–100 and redraws it on every S-Rank. Any single
   number there would be an invention. **The floating guarantee has stopped being
   an argument in a note and become a hole in published data** — opened as
   **N31**, together with the fixture it contradicts: `Banners.grayRavenFloating()`
   pairs the 1.50% base with a **70%** featured rate and the client pairs it
   with **100%**.
2. **The Overclock recipe (16/16/20/28) and Harmony Lv 1 (25 Accelerators) are
   absent**, because an `Upgrade` must name the entity it advances and **the
   weapon they were read off was never named**. The numbers are in the note. This
   is the cheapest gap in the project to close and it needs one screen.
3. **Every currency is absent** — Cog, Black Card, Rainbow Card, Event Construct
   R&D Ticket, Soundwave Coin — because `Item.rarity` is required and **no screen
   that was read grades a currency**. That is the format asserting something about
   the world rather than the reading falling short, and **N28 hits it first**,
   since the pull-currency work needs the ticket to exist as an item.

Two shapes were bent rather than broken, both recorded in the bundle's own
comments. **The fodder band is carried by `consumesCategory`, not
`minimumRarity`** — `Fodder` has no upper bound, so `minimumRarity: 4★` would
claim 5★ fodder also gives 300, and the 5★/4★/1-3★ filter buttons are evidence it
does not; the rule is **inert**, because the fodder stack's own item name was not
recorded either and no item carries the category. And **the banner carries no
availability window**: the dates 09/17 – 10/01 06:59 were read and **the zone was
not**, and `Availability` takes instants — which is N20, arriving as data rather
than as a deferral.

**A general lesson about the format, worth more than the bundle.** Every one of
the three refusals is a *required* field the reading could not fill, not an
optional one it skipped. A canonical format tuned on one game asserts that
everything has a rarity, that every upgrade belongs to a named thing, and that a
guarantee is a constant. Reverse: 1999 never contradicted any of those. PGR
contradicted all three on the first day of authoring.

**The bundle is now guarded, and guarding it found a hole in the build.**
`AuthoredBundlesTest` parses every file under `data/bundles` and fails on any
fact the project is not entitled to publish — verified by dropping a deliberately
`THIRD_PARTY` bundle in and watching it go red. It also **fails on an empty
directory**, because a scan with nothing to scan is the vacuous pass this
repository already learned about from `EntityKindBoundaryTest`'s denylist.

The hole: `data/bundles` is outside every source set, so **nothing made it an
input to anything**. The first run of the defect check stayed *green* — the task
came back `FROM-CACHE` — and only `--rerun-tasks` showed the failure. A guard
that does not re-run when the thing it guards changes is not a guard.
`backend/app/build.gradle.kts` now declares the directory with
`inputs.dir(...).withPathSensitivity(RELATIVE)`, and the defect check goes red
without any flag. **Worth generalising:** any future test that reads repository
data from outside a source set has this problem silently.

**330 tests, run in full, 0 failed and 0 skipped locally** (the snapshots are
present on this machine, so the three snapshot-gated classes executed). 4m 13s.

**Environment, and it corrects E4 in a useful direction.** Docker was down at
session start (`failed to connect to the docker API`, and the Windows service is
not the engine on the WSL2 backend). **A session cannot start `com.docker.service`
but it can start the application** — `Start-Process 'C:\Program Files\Docker\Docker\Docker Desktop.exe'`
from PowerShell brought the engine up, and `docker info --format '{{.ServerVersion}}'`
answered `29.7.2` within the minute. Nothing needed elevation.

**The remote.** At session start `gh pr list` was empty and `dev` clean — the
sixth time a push would have run no CI. It was then pushed and
**[PR #22](https://github.com/kietnt4412/storm_almanac/pull/22) opened and merged
green on `ba68d1f`, run `35353122893`.** Two things worth keeping from that run.
**`:app:test` executed rather than coming from cache**, which is the `data/bundles`
input declaration doing its job on a runner as well as locally, and
`AuthoredBundlesTest` passed there; every other module's test task was
`FROM-CACHE`, correctly, since no module source changed, and the three
snapshot-gated classes skipped as they always do. And **the PR was merged about
twenty seconds after it opened, before CI had finished** — so green here is a
fact about the merged commit rather than a gate anyone waited on. Also noticed
while checking: **`dev` sits 12 commits behind `main` with identical trees**,
the gap being merge commits from PRs #15–#22 that never come back down. Harmless,
and not a reason to rebase — but it means "behind by N" says nothing about content
on this repository.
**Lines: 790 → 826 by the session's own additions, then → 550 by rewriting the
file.** The first number is the one worth reading: trimming and moving two blocks
out (the line-count ledger itself, and D2's full account) took ~50 lines, while
N27's rewrite, **N31**, two Status bullets, E4's way out and D3's corrected
trigger put ~80 back. **That is the sixth session in a row to move the number the
wrong way, and it is not laziness** — a session that finds something has to write
it down, and a tracker that is a handoff cannot also be a fixed-size document.

**So the maintainer asked for the file itself, and the answer was a rewrite rather
than a seventh trim.** 826 → **549 lines, 74 604 → 39 981 bytes**, measured:

- ***Current state* stopped repeating the notes it links to.** The thirty-row
  component table became **twelve rows for what the next work touches** and a
  **one-paragraph list of what is merely done**, with the full rows in the
  archive. That is where most of the 35 kB went.
- **The session-log index became an index** — one short line per session, where
  several rows had grown into paragraphs duplicating the entries they point at.
- **D3, D2 and D1 keep their decision, cost and reversal trigger**; their
  argument is in the archive.
- **Nothing was deleted.** The whole 826-line file is reproduced verbatim at
  [the tracker as it stood before the compression](#the-tracker-as-it-stood-before-the-2026-09-18-compression),
  so a later session that misses a fact can find it and decide whether it is
  still operative.

**The rule that replaces "trim it":** if the tracker passes 550 again, **rewrite a
section, do not shave it** — and suspect the row nobody can read in one breath,
which `wc -l` cannot see and which the twenty-second session proved costs 2 700
characters and zero lines.
**2026-09-18 (twenty-second) — PGR was read, and the client contradicted the
guides twice.** N27's blocks 1 to 3, off the Global client, patch **"Steering By
Light"** — PGR names patches rather than numbering them. All of it is in
[the PGR note](../game-facts/punishing-gray-raven-research-disclosure.md). The
maintainer read; a session transcribed; no screenshot is committed.

**Both banner archetypes are complete and paired.** *Arrival Construct* — 0.50%
base, hard guarantee at 60, **70%** featured. *Themed Construct* — 1.50% base, a
**Floating Guarantee drawn uniformly 80–100 and resampled on every hit**, **100%**
featured. The two axes pair rather than varying independently, settled by reading
all three numbers off one banner, so there are **two shapes to model, not four**.

**The first correction: the featured rule is per-banner, and it was over-generalised
mid-reading.** The first banner seen was 100%, written up as "PGR guarantees the
featured unit"; the second was 70%, which made it a per-banner value. Below 100%
the recovery is published and named — **the Calibration System**, guaranteeing the
target on the next S-Rank after a miss — and the inheritance rule says
**"(Calibration included)"**, so a pending loss survives the pool ending.
**R1999 clears at event end and PGR inherits**, so the two published games now
disagree on exactly the question `PityScope` exists to answer — the best argument
yet that it deserved to be data.

**The second correction, and the bigger one: PGR events are not farms.** Read on
*Blazing Rhapsody*. The stages **cost no Serum**, pay **fixed one-time mission
grants** behind capability gates inside a four-day window, and the event currency
(Soundwave Coin) buys **event-local combat buffs, not materials**. **PGR events
have no shops — every event, not just this one.** `EnergyMip`'s "run stage N times
at cost C for yield Y" has no term that applies, and `LEAST_ENERGY` is not the
question; `FEWEST_DAYS` against `closesAt` is. **N30's entire shop half rested on
guide evidence and is dropped**; so are probabilistic goals, because **Resonance is
picked, not rolled**.

**What reproduces and what does not.** R1999's 2.36% came back out of its own curve
as 2.3592%. **PGR's does not**: both pools advertise **1.90%** overall, but 0.50%
with a hard 60 computes to **1.925%** and 1.50% with a floating 80–100 to
**2.021%**. The non-S rows sum to 98.10 and 98.10 + 1.90 = exactly 100.00, so the
table is normalised on the advertised figure and is a long-run outcome share rather
than a per-pull vector. Recorded as an open question against the engines — the
per-10 A-Rank floor is the untested term — not as the publisher being wrong.

**N28 is unblocked.** 1 pull = **250** Event Construct R&D Tickets (2500 for ten,
no discount), 1 Black Card = 1 ticket, 1 Rainbow = **10** Black, 119 Rainbow =
$19.99 — about **$4.20 a pull**. **Energy is pinned too:** Serum, cap **240 ml**,
**+1 per 6 minutes** printed as a countdown, no overflow — and the cap is *exactly*
one day of regeneration, so `energyPerDay` is 240 **for a daily player and less for
anyone else**. **Feeding:** a 4★ unit is **300 EXP** whether it is a material or a
weapon, so one rule covers both; weapons are simultaneously entities and fodder,
which the model has no precedent for.

**The provenance check caught a real error on its first live use.** The maintainer
reported the Rainbow→Black exchange as 1:1; the screen read 10:1 on all seven
tiers; asking rather than choosing the cheaper number found that the 1:1 belonged
to a different exchange. The price arithmetic agreed with the screen — at 1:1 a
ten-pull would cost about $420.

**Cut by the maintainer: block 4, the timed authoring pass.** N27 asked for three
readings taken against a stopwatch, because ~2 700 facts a patch times an unmeasured
per-fact cost is the project's largest unquantified risk. It will not be measured.
**So no claim that a patch is affordable to source has anything behind it.**

**Still unread:** the Memory system entirely, and the costs behind the character's
four axes (Train, Evolve, Awaken, Phylotree). **Nothing was authored into a bundle
yet** — N27's typing half remains. A stray duplicate import left in
`MonteCarloBannerEngine` before the session was reverted.

### 2026-09-13 (twentieth session) — six action bumps, one licence left unaccepted, and a green run that ran no tests

**The remote had moved again, as the tracker said it would.** PR #19 was merged,
`origin/main` and `origin/dev` were equal, and **`dev` had no open PR** — the fifth
time the trap has been armed, caught by looking. There was also an unlogged
uncommitted change in the working tree: a duplicated
`import java.util.function.ToLongFunction;` in `MonteCarloBannerEngine.java`.
Legal Java — the JLS ignores an identical duplicate single-type import — and not
this session's; left out of every commit and reported rather than reverted.

**N27 was handed to the maintainer as a form, not a pointer.** What to read off
the summon rules screen (per-rarity rates, where the curve starts and how it
climbs, the wall, the ten-pull floor, the featured chance and what a loss carries,
pity scope, dates) and off one stage, one item and one character, with a timer on
the second half. **Deliberately without the fixture values**: the authoring note
forbids opening Kornblume while reading because it makes the later diff
non-independent, and showing a reader the numbers `Banners.java` already assumes
does exactly the same thing. Read blind, then diff.

**N5 done** — [in full above](#done-2026-09-13-twentieth-session--n5). The tracker's
line asked for `setup-java@v5`; the latest was v6, and every other action had moved
as far. **The one thing worth carrying forward is `gradle/actions@v6`'s licence
change**: the caching moved into a proprietary component under Gradle's Terms of
Use, so accepting it is the maintainer's choice, and v5 is frozen at 2026-02-23 in
the meantime. **And the run was green on cached test results only** — 28 of 50
tasks from cache, every test task among them — which is correct Gradle behaviour
for a YAML-only change and still means the suite has not yet run under the new
actions.

**Tracker length:** N5's action came out and the Phase board's closed entries were
cut to one line each, which the nineteenth session named as the next candidate.

**Then Q4 was answered for one banner, and it took three attempts to get a reading
that counts.** The first reply filled in the form cleanly, but one bracketed line
read like a tool's output. Asked how the text got from the screen to the message,
the second reply was another AI assistant's web-search answer, citations
included. It said so itself: it had not read the screen. Nothing from it was
recorded. The third reply was **eight screenshots of the Global client's Details
panel** for *A Newly Hatched Chapter*. That is `PUBLISHER_DISCLOSURE`, and the
[note](../game-facts/reverse-1999-summon-disclosure.md) records that a session
transcribed them. **Two things claimed in the first reply were not on the screen**:
the open and close dates, and "3.7". Neither is recorded, and the reply that had
called the 04:59 close a confirmation of N20's day boundary was withdrawn.

**What the screen confirmed.** The pity curve, stated word for word, and **a
number it was not written from**: an overall 6★ rate of 2.36%, which the curve's
42.3869-pull wait reproduces as 2.3592%. It has teeth, too. A curve one pull early
prints 2.38%, one pull late prints 2.34%, and a bare wall at 70 prints 2.30%, so
the check rules out the near misses. It also shows "+2.5%" is percentage points.

**What it contradicted.** `reverseDebut` hands her over with every 6★. The screen
splits a 6★ 50/50, with a guarantee after a miss. On the disclosed banner, 70 pulls
is certain to give a 6★ but gives her only 0.6578 of the time, the no-shop worst
case is 140, and the average wait for her is 63.58 pulls. **Every number was
computed first by an independent awk chain.** That chain was validated by
reproducing the existing pinned 42.3868867154 before any new value was trusted.

**What it found.** Each summon grants a Cassette of the Lost, which buys her in the
Limited Shop. That is a guaranteed second road to her that neither engine models,
so every worst case is *without the shop*. It joins **N28**, since it is the same
kind of field, a currency and a price. There is also a one-off "first ten summons
give a 5★ or higher" floor, which `Floor` cannot express, and which could not
change a headline answer anyway.

**Landed:** `Banners.reverseAnniversaryLimited()`, the one fixture with the full
stated rate table, and five tests in `PublishedRatesTest.Disclosed`.
`reverseDebut`'s javadoc now says it is contradicted. The gacha module has 51
tests, all green. The cross-check now asks **96 questions over eight banners:
worst gap still 0.110 points, worst 2.22 standard errors** (was 1.84). CI
confirmed it on run `34746241622` (`85d0538`), and this time `:modules:gacha:test`
**executed**, so the suite has now run under N5's actions.

**The shop price came next, from one more screenshot: 200 cassettes.** The
question it answers turned out narrower than expected. A summon grants exactly one
cassette, so the shop cannot open before pull 200, and by then the 50/50 guarantee
has already delivered her at 140. **For one copy the shop never binds**, so the
engines' single-copy answers are exact. **For several copies it does**, because
the two roads add up: 2 copies drop from 280 pulls to 200, and 3 from 420 to 280.
From 4 copies up the answer depends on a purchase limit the screenshot does not
show. The test's claim was narrowed to one copy, with the price written beside
the 140 it beats.

**Then the limit, and a cap underneath it.** The maintainer reported from the game
that she can be bought without limit until Portrait 5, and sent her Portrait screen
as support. The screen shows portrait levels to Lv. 5, at one portrait item each.
The limit itself is not on a screenshot, so it is recorded as the maintainer's
in-game observation. **The portrait screen agrees with the duplicate table read
earlier from a different screen**: copies 2–6 give an Artifice, and copy 7 gives
none. So six copies is the most that changes anything, and a shop limited
exactly there never binds on a goal that means anything. With the shop, the
worst case for six copies is **560 pulls against 840** from the pulls alone. N28
now also carries capping `copies`, which both engines accept at any value from one
upwards.

**Last, the carry-over.** The screen does not say whether a guarantee earned by
missing the 50/50 survives the event. The maintainer reports from the game that it
does not: it is reset, like the pull count. So a limited banner always starts from
`PityState.fresh`, which the fixture already assumed, and no code changed. **Still
open for Q4:** every other banner.

**Then the question that turned the session: "is this another clone of
Kornblume?"** Answering it meant reading Kornblume's source rather than its data.
`docs/prior-art.md` §1 had said, "confirmed by inspection", that Kornblume
"structurally cannot" solve per player, because its `public/data/` holds
`stages<patch>_greedy.json` files. **`src/composables/glpkSolver.ts` says
otherwise, and has since 2024-03-05.** It builds a linear program from the
reader's warehouse store and minimises Activity with `glpk.js`, in the browser.
Crafts are integer and stage runs are not. `tesseract.js` imports an inventory from
a screenshot, and `vue3-google-signin` syncs it. **The 2026-09-02 note inferred the
planner from its data files without opening `src/`**, and the README's opening
paragraph had been built on that inference ever since. So the honest answer was
**yes**: on R1999 the built product overlaps a deployed tool almost feature for
feature. What differs is either invisible to a player (integer runs, shadow prices,
sample-discounted yields, provenance) or unbuilt (a second game, crowdsourced
drops).

**The maintainer's answer was to swap the titles, recorded as
[D3](../../TRACKER.md#d3--launch-title-swapped-to-punishing-gray-raven-2026-09-13).**
A GitHub search for PGR planners turned up an automation bot, a 2020 data dump and
a private server, and nothing that plans. The maintainer plays PGR on Global, and
without that the swap would have died on ADR 0015. R1999 is not deleted and
becomes Phase 11's proof. **Cost, stated so it is not discovered later:** PGR
starts with no data in the repository, and fodder, probabilistic goals and the
first real calendar move from Phase 11 to before launch. New action **N30** puts
fodder in the solver. **N27** was retargeted to PGR, in a deliberate order: a
banner, then the feeding screens, then a timed stage-and-character pass.

**Corrected the same day, and nowhere deleted:** the README opening, the README's
Kornblume credit and "bet" paragraph, `plan.html`'s title facts, Kornblume card,
§11 callout and Phase 11 heading, and `prior-art.md` §1, struck through with the
correction and how the mistake happened. The tracker grew by the D3 entry and
N30, and the Status bullet for Phase 5 and N28's cassette detail were cut to pay
for part of it.

**2026-09-14 — the survey D3 was missing, and the plan's wedge rewritten from it.**
The GitHub search behind D3 could not see closed sites or spreadsheets, so the
open web was searched too, and **every tool found was opened** rather than judged
from its snippet. That is the lesson of prior-art §1. There are two material-total
calculators (the newer last pushed 2024-01), a serum timer from 2021, and a pity
calculator that models hard pity only. Two community spreadsheets could not be
opened (the wiki returns 403 to a fetch) and are recorded as unverified, not as
lacking anything. **Nothing found plans PGR farming from an inventory**, with
Discord, Bilibili and closed CN sites named as not searched. **The finding with
teeth was in the guides, not the tools:** PGR is farmed through event stages whose
currency buys materials in event shops, and `EnergyMip` refuses an item only a
shop sells. So N30 became *fodder and shops*, and N27 gained an event reading.
`plan.html`'s wedge section was rewritten rather than patched. PGR's tools card
comes first, Kornblume's card says plainly that it solves per player, and a
callout names the four things the bet costs before launch. `prior-art.md` gained
§7 with the table. **Not rewritten:** the rest of the plan. Its architecture,
tracks and phases did not change, and D1–D3 carry the departures.

### 2026-09-12 (nineteenth session) — Phase 5: two engines, one question, and a criterion that did not fit its own sample size

**Phase 5 closed on its criterion, out of order and on purpose.** Phase 4 is open
and its remaining half is a deployment (**B5**), whose last step needs accounts
and an OAuth registration that no session has. N27 above it is the maintainer's by
ADR 0015's integrity rule. So the maintainer was asked which of three things this
session should do — Phase 5, B5's wiring, or N5 — and chose Phase 5 knowing it
breaks this file's own "do not start a phase until the previous criterion is met".
That is recorded as **D2**, with the cost, rather than quietly done.

**The exit criterion is met, both halves, and the headroom is the number worth
keeping.** 84 questions across all seven published banners, asked of an exact
Markov chain and of 500 000 simulated trials: **every gap inside 0.110 percentage
points against a criterion of 0.300, worst case 1.84 standard errors.** Both games'
published rates reproduced from `BannerModel` alone — the 70-pull wall, the rising
curve biting at pull 61 and not 60, the 42.39-pull average, the 120-pull worst
case, 30/40/80 for weapons, beginner and the floating variant. 46 new tests, 324
in the suite, 0 failed and 0 skipped locally.

**The criterion and the trial count the plan quotes do not fit together, and the
cross-check said so on its first run.** The plan asks for 100 000 trials *and*
agreement within 0.3%. At a hundred thousand trials the standard error of a
mid-range probability is about 0.16 points, so 0.3 points is under two of them — a
gap that size is ordinary sampling noise. Over 84 questions one duly turned up:
the debut banner over thirty pulls, **exact 0.139616 against simulated 0.136550, a
gap of 0.31 points at 2.80 standard errors, with nothing wrong with either
engine.** A test that fails one run in a few hundred for no reason is worse than
no test, because the failure arrives attached to an innocent commit. So the trial
count now follows from the tolerance: 500 000 puts the standard error at 0.07
points and makes the plan's 0.3 a four-sigma bound. **The criterion did not move;
the sample size did** — and the agreement is asserted twice, the second time
against three standard errors of the simulation, which is the assertion that
survives re-seeding.

**Three of the four design questions only became visible once something had to
compile**, and all four are [ADR 0018](../adr/0018-the-gacha-engines-answer-one-question-about-one-rarity.md):

- **`PullResult` could not carry a `Rarity`**, so it no longer does. `BannerModel`
  pins the headline rate at every pity count and says nothing about the rest of the
  table while pity is active — when the curve reaches 9%, the published numbers do
  not say which rarity gave up the other 7.5 points, and every game will have given
  it up differently. A `Rarity` there is an invented answer invented per game,
  which is the one thing `gacha` may not do. It reports the two facts the model
  determines: did it hit the rarity, and was the hit featured.
- **A floor below the headline is ignored and one that reaches it is refused by
  name.** A floor guarantees *at least* its minimum, so it raises low outcomes and
  never caps high ones, leaving the headline rate where the rate table put it.
  Sound, with a cliff: a floor whose minimum *is* the headline rarity is a second
  pity rule on a different period, and ignoring it would silently understate every
  answer. Both games' published floors are ignored, correctly.
- **`PityState.guaranteedFeatured` was a boolean and could not carry the state.**
  A flag is exactly right for a guarantee after one loss — both shipped games — and
  cannot describe a player one loss into a guarantee after two, which
  `FeaturedRule.guaranteeAfterLoss` permits. It is now `consecutiveLosses`, and the
  three transitions live on the record so both engines advance state through the
  same three lines. A mistake there is a mistake both engines make identically and
  the cross-check would agree enthusiastically about it, so the transitions are
  pinned separately against a generator handing out chosen numbers.
- **The exact chain reads the mass that never arrived, not the mass that did.**
  Accumulating arrivals over seventy steps left a residual near 1e-13: a wall the
  game guarantees came back as 0.9999999999999895, and one answer came back *above*
  1.0, which is not a number to put in front of a player. Every path out of the
  chain leaves the remaining mass exactly zero at certainty, so read it there —
  certainty is exactly 1.0, impossibility exactly 0.0, and the residual lands in
  between where it is 1e-16 against a number nobody reads past four decimals.

**`expectedPullsToFeatured` needs no linear solve, which is worth writing down
because reaching for one is the obvious move.** Hard pity makes the expected wait
to the next hit solvable backwards from the wall, because a miss can only move the
counter forwards; pity then resets on every hit, so the chain is cyclic with a
single entry point, and the unrolled sum over lost splits is geometric. The whole
answer is two scalars and a `Math.pow`. A game with no hard pity would not have
this property — and would also have an infinite worst case, which is the larger
problem.

**Two of the first eleven failures were in the tests, which is the cheap
direction.** Asserting that the 30-pull weapon wall was certainty failed at
**0.7030** — the wall guarantees the *rarity*, and only the guarantee after a loss
guarantees *her*; the rotational banner's version of the same claim had been
written correctly ten lines earlier. And the floating variant's ten-pull
probability is **0.1010**, not the 0.0982 that "one hit times the 70% split"
predicts, because losing the split early leaves room to hit again inside the same
ten pulls. Both are now pinned against a chain written separately, in awk, purely
to check this one — so a shared mistake has to be made twice in two languages.

**Nothing calls either engine, deliberately.** No bean, no route, no screen, on
the same reasoning that kept `SolveCoordinator` unwired through Phase 2. And
`IncomeModel` is still an interface for a harder reason: `projectedPulls` needs to
know which item is pull currency and what a pull costs in it, and **neither
`BannerModel` nor the `banner` table declares either**, so "how much will she have
accrued by Friday" has nothing to compute from. That is **N28**, and it is a bundle
field, a parser, a writer, a migration and a JDBC round trip — worth doing with a
game whose income sources are actually ingested rather than speculatively.

**The CI trap was armed for the fourth time and caught by looking, as the rule
says.** The tracker opened claiming `main` was `92c29cf` and PR #18 still open.
`gh pr list` returned nothing: **#18 merged, `main` is now `39bd72d`, and
`git rev-list --count origin/main..origin/dev` is 0** — so `dev` had no open PR and
a push to it would have run no pipeline at all. Checked rather than trusted, which
is the only thing that has ever caught this.

**The tracker took the cut three sessions had flinched from, and still got
longer.** The *Current state* rows for Phase 1, the parser adapter and provenance
had each grown into a session-log entry inside a table cell; they are one line each
now and their detail is below, along with the Status bullets for the offline outbox
and N4 that a table row already carried. Ten blocks cut in all — **and 742 → 749
anyway**, because closing a phase, opening a deviation and adding an action cost
more than the cutting saved. By the tracker's own rule that is moving the problem
rather than doing the work, and the flat number is recorded rather than rounded,
because **the drift has twice run in the flattering direction** when a session
carried a figure forward instead of measuring it.

### 2026-09-12 (eighteenth session) — the field nobody may read, and a condition written then deleted

**A short session with one action in it: N4.** It was the top item a session is
*allowed* to take, and that is worth saying plainly rather than treating as
scheduling. N27 sits above it and is the maintainer's by ADR 0015's integrity
rule — a fact enters because somebody read it in the game, and an AI session is
one of the three things that rule disqualifies by name. N20, N18 and N19 sit
between, each deferred on a trigger that has not fired: a game that rotates, a
published drop estimate, a second node. N4 was next and was owed.

#### What ADR 0007 had actually recorded against itself

ADR 0007 made equipment an `Entity` rather than a third top-level concept, and
paid one opaque, game-supplied string for it. Its own words: `planner`, `gacha`
and `stats` **must never read** `kind`, because the moment behaviour depends on
the distinction, the distinction was real and the ADR was wrong. That is the
reversal trigger, not a style note.

Then it did something better than most ADRs do — it recorded that the constraint
was asserted and **not enforced**, named the natural home (an ArchUnit rule beside
`ModuleBoundaryTest`), and gave the reason for waiting: the guarded modules were
empty and the rule would pass vacuously. That reason expired somewhere around
Phase 2 and nobody noticed for six sessions. `planner` is now a resolver, a MIP
and an optimizer, all of which take a `GameDefinition` and are one hop from an
`Entity`.

#### Three decisions in twenty lines of test

**Bytecode rather than a source scan.** The obvious move was to extend
`GameAgnosticismTest`, which already reads source and already guards those three
modules. It does not work here: a grep for `kind()` cannot tell `entity.kind()`
from `change.kind()`. `gamedata.diff` has one of the latter, and
`GameDataReadModel` reads both within a hundred lines of each other, so the scan
either misses the read or fails on a sibling. ArchUnit resolves the declaring
type, which is the entire question being asked. The two tests are not
interchangeable and the new one's javadoc says why, because the next person to
touch either will be tempted to merge them.

**An allowlist rather than a ban on the three named modules.** ADR 0007's
normative sentence names `planner`, `gacha` and `stats`; its decision paragraph
says the field exists "for the catalog surface and for ingest validation,
nowhere else". Those are different rules and the second is the one worth
enforcing. A denylist of three modules would pass vacuously *again* the first
time `player`, the CLI or a future module grew a read — the same failure mode
that let this gap sit open. So: `gamedata`, `api` and `adapters` may read it and
nothing else may. The allowlist is by module because the module is the boundary
this project enforces everywhere else, and because "the catalog surface" is not a
line the package structure draws finely enough to guess at. It is also the first
architecture test here that guards a **field** rather than a dependency.

**Verified by putting the defect back.** A rule that has never failed is a rule
nobody has checked. A throwaway class went into `planner` reading `kind` in both
spellings a person would actually write — `e.kind()` and `.map(Entity::kind)` —
and the rule failed on each, naming the file and the line.

#### The condition that was written, measured, and deleted

The method reference looked like a hole. ArchUnit files `Entity::kind` as a
`JavaMethodReference`, which is not a `JavaMethodCall`, and `JavaClass` exposes
`getMethodReferencesFromSelf()` separately from `getMethodCallsFromSelf()` — so
the reasonable inference is that `accessTargetWhere` misses it, and a custom
`ArchCondition` was written to cover it.

Then the inference was checked instead of trusted: the extra condition was
commented out and the run repeated. **Two violations, not one** — ArchUnit 1.3's
`getAccessesFromSelf()`, which `accessTargetWhere` walks, carries code-unit
references as well as calls. The condition was deleted and the measurement is in
the test's javadoc, so the next reader does not add back the twenty lines this
one removed. Worth recording as a habit rather than a fact about one library:
the cost of checking was one four-second build.

#### ADR 0007 was not edited

Its "Open gap" paragraph now describes a gap that is closed. The convention here
is that an accepted ADR is superseded, never corrected, and this is not a
decision that changed — the decision is identical and only the account of what
enforces it has moved on. Where a reader finds out *when* is the tracker, which
is what the tracker is for. Superseding 0007 to change one paragraph about
tooling would put a false claim in the record: that the equipment question was
re-decided.

#### Housekeeping, and the size debt

Two *Status* bullets moved to the archive — the CSRF one and the unreadable
published version — which the seventeenth session had named as the next
candidates. Both are lessons rather than live state. The published-version one
keeps its single sentence with teeth, moved from *Status* to the **Phase 6 row**,
because "read this before tightening a rule over data already published" wants to
be where somebody about to do that is looking. Two entries the seventeenth
session had **struck through in place** were archived as well — the offline PWA
load and provenance being write-only — which the live file's own rule asks for
and which it had just broken.

**747 → 742 lines**, and the interesting part is the first number: the
seventeenth session recorded 740, and `wc -l` says the file was 747 both then and
at the start of this one. A line count carried forward from the last session's
prose rather than measured drifts, and it drifts in the flattering direction. The
budget is 550, so the debt is **192 lines** and not 178. Count it.

#### The numbers, and the trap that did not fire

**278 tests locally**, 0 failed, 0 skipped with snapshots present. CI green on
`261d1cd` as run `34691539392` — `backend` and `frontend` both passing, `deploy`
skipped, **16 skipped and verified to be exactly** `RealUpstreamPlanTest` 8,
`CommunityBenchmarkTest` 5, `RealUpstreamPatchTest` 3, which makes it **262 on
CI**. `EntityKindBoundaryTest` was read in the runner's log rather than inferred
from a green job, and so were the 15 frontend tests. The PASSED-line count was
255 against 262 actually run, which is the log undercount the seventeenth session
warned about, arriving again.

**The push-to-`dev` trap did not fire, for the first time in four sessions**, and
the reason is that the rule was followed rather than that the situation improved:
`gh pr list` was checked first, PR #18 was still open, and the push therefore ran
a pipeline. The cost of it still being open is that **N4 is on the same PR as
N25** and that PR's title names only N25. Not tidy; the alternative was branching
off a `dev` that is itself unmerged, which trades a misleading title for a
misleading history.

### 2026-09-11 (seventeenth session) — the numbers say where they came from, and the answer is "nobody knows"

**N25's three debts are paid and the action closes.** Nothing new was started:
the session took the top item a session is *allowed* to take — N27 is the
maintainer's by ADR 0015's integrity rule and cannot be delegated — and finished
it.

**1. Provenance is read back out, and the architecture of that is the whole
decision.** ADR 0016 had written provenance at ingest and enforced it at publish,
and neither of those reaches a reader; for two phases the gate could ask the
question and nobody else could. The obvious implementation is a field on
`GameDefinition`, and it is the wrong one: `Provenance`'s own javadoc already
says that a solver which can read where a number came from is a solver that can
eventually be made to prefer one, and nobody would notice. So the read travels on
a **second port**, `ProvenanceRepository`, which `planner` does not know exists.
`api` may reach `gamedata` and does; `planner` loads a `GameDefinition` and still
cannot see an origin.

Two shape decisions worth keeping:

- **The lookup takes the facts, not the version.** Asking for a whole version's
  sourcing would be one simpler query and would let a caller render sourcing for
  facts that are not on the page. Taking the refs makes the response's claim
  narrow by construction: what comes back is the sourcing of the numbers this
  request actually answered with.
- **`firstHand` goes on the wire.** It is derivable from `origin`, and a client
  deriving it would be a second copy of `Provenance.Origin.isFirstHand()` in
  TypeScript, where nothing can be made to fail to compile when the Java enum
  gains a member. The policy is answered once and travels as an answer.

**The JDBC adapter uses named parameters and it is worth saying why**, because
the package next door does the opposite: `Availabilities` writes a Postgres array
*literal* and argues it is safe because `DayOfWeek` is seven names with no
quoting to escape. Fact refs are slugs out of a bundle, so that argument does not
survive being copied; `IN (:refs)` expands to one placeholder per ref and every
value stays a bind parameter.

**What the feature says on today's data is the uncomfortable part, and it is
supposed to be.** The fixture reads *"Invented for this project — not any real
game"*. A version published before ADR 0016 reads *"Nobody recorded where these
numbers were read. They may be right; nothing here says so."* That is the feature
working — **there is still no first-hand fact in this repository** — and it is why
the empty case is rendered loudly rather than as a blank space. A component that
showed nothing when it knew nothing would read as approval.

**2. The PWA has been loaded offline, and the method is the evidence.** The claim
owed was not "offline editing" — the sixteenth session proved that against a
refusing network — but "the app shell renders with no server at all". So: build
the bundle, serve it on 4173 (`web-built` in `.claude/launch.json`; the dev
server has no worker worth the name), let the service worker precache, then
**stop the server** and confirm `curl` is refused, then reload a *deep* route.
`/catalog/proving-ground` rendered: the navigation fallback resolved it to the
precached `index.html`, and the catalog list came out of the `game-data` runtime
cache — the cache whose rule the previous session had repaired and which nothing
had yet read from. A CDP offline toggle would have proved strictly less, because
the server would still have been there.

**3. The frontend has 15 tests and, more importantly, a written decision about
what they are for.** The question the tracker asked was to choose deliberately
between component tests, a smoke test against the built bundle, or explicitly
neither. The answer comes from the four defects the previous session found by
driving a browser:

| Defect | Catchable in jsdom? |
|---|---|
| Store selector re-rendering forever | **Yes** — React throws rather than hanging |
| Focus order not the order rows are drawn | **Yes** |
| Goal screen could not express the base of a track | **Yes** — it is logic over fixture data |
| `display: block` folding every table header | **No, and never** — jsdom computes no layout |

So: **behaviour is the pipeline's, appearance is a person's.** Driving a browser
before shipping a screen is still required; what it no longer has to do is
re-check the behavioural half by hand on every change. The re-rendering selector
is pinned by a test that was **verified to fail** — the defect was reintroduced
and both assertions broke, one on referential identity and one with React's own
*"Maximum update depth exceeded"*, which is the symptom that blanked the
inventory screen. A regression test nobody has watched fail is decoration.

`globals: true` was set and then removed: every test imports `describe`/`it`/
`expect` by name, which keeps `tsconfig`'s `types` the small closed list it is.
A half-configured global is how a suite ends up compiling in the editor and not
in the build.

**A browser found one more defect, and it is the most valuable thing here.** The
first time the built bundle was served, **the whole page rendered as nothing** —
`Cannot destructure property 'sources' of undefined`. The cause was not a bug in
the component: Vite's `preview.proxy` defaults to `server.proxy`, so the page
reached a backend from another session that had been built *before* `sourcing`
existed, and reading through an absent object throws before React paints
anything.

**That is B5's problem in miniature.** Vercel and Render are separate hosts and do
not deploy at the same instant, so every release has a window in which a new page
is talking to an old API. The fix is two-part and both parts matter: the
component treats an absent record as silence — which is its own thesis, not
defensiveness — and the **wire types now mark the field optional**, so the
compiler points at every call site that has to cope rather than leaving it to the
next person to remember. The general lesson is wider than one field: *a client
that treats a new response field as guaranteed is a client that blanks its own
page on every deploy.*

**The tracker paid its size debt before adding to itself**, which is its own
rule and had been deferred once. Eight qualifications of closed phases moved to
this file — 759 lines down to 740 while closing an action and shipping a feature.
The one with live teeth, the time axis never having met real data, was restated
in one line on the Phase 11 row rather than being left only here.

**Numbers:** 277 backend tests (274 + 3), 0 failed, 0 skipped locally with
snapshots present; 15 frontend tests. `main` was `92c29cf` and `gh pr list` was
**empty** at the start of the session — the third time the no-open-PR trap has
been found rather than triggered.

**CI-confirmed on run `34597211345`** ([PR #18](https://github.com/kietnt4412/storm_almanac/pull/18)),
both jobs green and `deploy` skipped. Two things were checked in the log rather
than inferred from a green tick, because this file has been wrong about both
before: the **15 frontend tests really ran on the runner** (a new step can pass
by doing nothing), and the **16 skips are exactly the three snapshot-gated
classes** — `RealUpstreamPlanTest` 8, `CommunityBenchmarkTest` 5,
`RealUpstreamPatchTest` 3 — which is what makes 261-on-CI a count rather than an
assumption. A pass in any of those three would mean a snapshot had been
committed by accident.

### 2026-09-11 (sixteenth session) — the screens, and the first edit that survived a tunnel

**N25 is most of the way in: the five screens Phase 4 is about exist, a browser
has driven all of them, and the offline half of the sync finally has a client.
What is not in is named at the bottom — the exit criterion is unmet and the
catalog still does not read provenance back out.**

#### What was built, and the three routes it turned out to need

The screens are the inventory editor, the goal picker, the plan view, catalog
browse and search, and the character page with the personalized overlay. Three
server routes were missing under them, and each one was missing for the same
reason: **every read before this session started from a slug the caller already
had.**

- **`GET /api/games`** — the index. A reader arriving with no account and no
  slug had nowhere to start, which made the public half of the catalog
  unreachable except by guessing a URL. It carries each game's newest published
  version and its `energyUnit`, so the plan form can ask for "Vigour a day" in
  the game's own noun rather than "energy".
- **`GET /api/games/{game}/items`** — the vocabulary an inventory is written in.
  Items reached a client only as resolved names inside a cost until now; a bulk
  editor cannot be built on that. Sorted rarest first, then by display name,
  which is the order a bag is counted in.
- **`GET /api/me/profiles/{p}/shortfall?entity=&target=`** — the overlay's
  engine, and **Phase 4's second exit clause served**. It asks `DemandResolver`
  rather than letting the page chain the upgrade steps it already has on screen:
  the rules about what a chain *means* — a state nothing reaches, a state
  already behind the player, a probabilistic target with no scalar cost — live in
  the resolver, and a second copy of them in TypeScript is a copy nothing tests.
  Three numbers travel per line (`required`, `owned`, `missing`) rather than the
  subtraction alone, because a reader who disputes a shortfall needs to see
  which half they disagree with. 7 tests.

#### The offline outbox is the client half of N23, and what makes it that is not the retry

N23 shipped a per-key merge whose every scenario was one MockMvc request
following another inside one JVM. The piece that was missing was never the
queue; it is that **an edit keeps the time it was made**. The store stamps an
edit when the player types, holds it per profile and per key, and flushes it
with PATCH — never PUT, because a PUT from a phone that has been offline
overwrites every key another device touched in the meantime, which is the exact
failure the merge exists to prevent, reintroduced in the client where no server
test would see it.

Measured in a browser rather than argued: edits made with the network refusing
stayed queued with the banner saying so; reconnecting flushed them in one
request; and **a deliberately stale edit — an hour old, against a value another
device had just written — lost, and the interface said which key lost rather
than showing a number the server does not hold.** That is ADR 0014's tiebreak
with real clocks on both sides for the first time.

#### Four defects the browser found that a typecheck could not

1. **The store's empty-outbox selector built a fresh object every call.** zustand
   reads through `useSyncExternalStore`, which compares against the last value, so
   every render scheduled another one. The page rendered nothing at all. It is a
   frozen constant now.
2. **Enter walked the wrong column.** The rows are sorted by rarity and *drawn*
   grouped by category, and the focus order was built from the flat list — so
   Enter on the second Sigil jumped a section. The order the hand moves through
   has to be the order the eye is reading, and that order only exists after the
   grouping.
3. **The goal screen could not say where a player actually stood.** Target states
   and current states were one list of every `toState`. The base of a track is a
   `fromState` and never a `toState`, so a player sitting on it had no way to
   select it. They are two lists now: you may only *aim* at a state something
   arrives at, and you may *be* at one nothing does.
4. **`.label` is `display: block`, which folds a table header row into a
   vertical stack.** It was right for a form label and silently wrong on every
   `th` in the shortfall and plan tables.

Also repaired: the service worker's runtime-caching rule named `/api/catalog/`,
a prefix this API has never served, and anchored it with `^`, which cannot match
a full request URL anyway. It was invisible because nothing read from the cache.

#### A published version in the local database cannot be read back

Found while pointing the screens at real data. **Every catalog route on the
locally published Reverse: 1999 3.5 answers 400**: a `craft` row exists with zero
`craft_input` rows, and `Craft`'s constructor refuses to build one — "consumes
nothing, which is unbounded free supply rather than a conversion". The row
predates the invariant, so this is a write made before a rule that came later
rather than a live defect in today's code. **The shape of it is what matters: a
version is immutable and the rules for reading one are not, so a published
snapshot can stop being loadable without anything having changed it.** Nothing
here is worth repairing — ADR 0015 says that data will not ship — but the same
hazard applies to anything published before Phase 6 tightens a rule. Recorded in
*[what is still unverified](#what-is-still-unverified)*; no action opened,
deliberately.

#### What N25 still owes

- **The exit criterion is untouched**: five strangers cannot complete a plan
  against a thing that is not deployed. That is **B5**.
- **Provenance is still written and never read.** It was named as part of N25's
  catalog half and is not done: no response carries it and no page shows it.
- **The PWA is installable and its offline *editing* is proven; its offline
  *loading* is not.** The outbox was exercised against a refusing network, which
  is a different thing from loading the app shell with no server at all — that
  needs the built bundle served, and was not done.
- **There is not one frontend test.** Every claim above was verified by driving
  a real browser, which is how four of the defects were found and is also
  exactly the evidence that does not survive into CI.

274 tests locally, 0 failed, 0 skipped.

### 2026-09-09 (fifteenth session) — a page that knows who is reading it, and the write that would have been refused

**N24 was the blocker on all of Phase 4's product, and it is gone: a browser has signed in, read its account, created a profile and signed out. The way in is a module the deployable jar does not contain — and building it found a defect in the product that two phases of green builds had not.**

#### The decision was not whether to have a back door, but how to make having one safe

The tracker's N24 offered two ways out and said they are not equivalent. A real
provider needs the deployed URL and lands with **B5**; a development-only sign-in
unblocks the interface now, proves nothing about the token exchange, and **is the
more dangerous of the two**. It is, after all, a back door in an authentication
system.

Every configuration-shaped guard was rejected for one reason: a Spring profile is
not activated until it is, `@ConditionalOnProperty` guards a property until
somebody sets it, an environment check reads an environment somebody supplies.
Each is one copied configuration away from a public service on which a URL is an
account.

**So the guard is absence.** `:modules:identity-dev` is a Gradle module that
`:app` takes through `testAndDevelopmentOnly` — on `bootRun`, on the test
classpath, excluded from `bootJar`. `storm-almanac.jar` does not contain the
classes, so there is nothing left to switch on.
[ADR 0017](../adr/0017-the-development-sign-in-is-absent-from-the-artifact.md)
is the record.

**The guard is one word in a build file, so a test reads the artifact.**
`implementation` instead of `testAndDevelopmentOnly` breaks nothing and nothing
else in the build would notice. `DeployableJarTest` opens the jar, walks every
nested dependency jar and asserts no class under `io/stormalmanac/devsignin/` is
anywhere inside — reading the archives rather than their names, because moving
the classes into `:modules:identity` is the mistake most available to somebody
who found the split inconvenient. It also asserts the same scan **finds**
`SecurityConfig`, because an absence test that looks in the wrong place passes
silently forever, and that the scan found more than a thousand classes at all.

`SecurityConfig` does not know any of this exists. Spring Security composes
chains, so the development one is declared entirely in the module production does
not ship: `@Order(1)`, a `/dev/**` matcher ahead of the product's catch-all, no
`permitAll` in the product's list and nothing to delete when the module goes.
The frontend makes the same move behind `import.meta.env.DEV`, which Vite
substitutes at build time — the development URL is not in a production bundle.

#### The defect it found on its first run

The argument for a development sign-in whose sessions are *ordinary* sessions is
that it exercises the real thing. It did, immediately.

**Spring Security 6 loads the CSRF token lazily.** The cookie is written only on
a response where something actually read the token, and nothing on a plain `GET`
does. So a single-page application reads everything successfully, never receives
an `XSRF-TOKEN` cookie, and **its first write is refused** — which presents to a
developer as a broken session rather than a missing header, on the first save any
new user would ever attempt.

It survived two phases because **every test of a write used MockMvc's `csrf()`
post-processor**, which hands the request the token production had not issued.
The tests were supplying the thing that was missing.

The sequence was deliberate: the assertion was written, it failed on a real
socket, and only then was `SecurityConfig.browserCsrf` written — the request
attribute name set to null, opting out of the deferred load. Both filter chains
share that one method rather than restating it, because a development sign-in
that skipped it would have handed the frontend a session it could read with and
not write with, and the difference would have been blamed on the frontend.

Then a real browser did it: **sign in → read `/api/me` → POST a profile → sign
out**, all of it same-origin through the Vite proxy, all of it observed rather
than asserted.

#### A gap in the tracker's own list, closed on the way

"The end-to-end test goes through MockMvc, not a socket" had stood in *what is
still unverified* since Phase 3, with the reason attached: an authenticated
session could not be minted over real HTTP without an authorization server to
redirect to. **It can now.** `DevSignInTest` runs against a real port with a
cookie jar kept by hand and no Spring Security test post-processor anywhere in
it — the first authenticated request this project has ever made over a socket.

What it still does not prove is unchanged and worth repeating: **no token has
been exchanged with a provider**. ADR 0017's reversal trigger is the module being
deleted the moment a real provider works locally.

#### The Dockerfile has been broken since Phase 1, and the tracker said it was verified

Reading the deploy path for **B5** turned up something else. `backend/Dockerfile`
was written at scaffold time and copies `modules`, `substrate` and `app`.
`adapters/` arrived with Phase 1; **nothing added a COPY line for it**, while
`settings.gradle.kts` includes `:adapters:reverse-1999` and `:app` compiles
against `KornblumeAdapter`. So every image build since Phase 1 has failed, and
the tracker's table has said *Docker Compose · Verified* the whole time — because
it was verified, once, before the directory existed.

**Reproduced rather than reasoned about**, because the in-container Gradle
download was going to take an hour: the exact tree the old Dockerfile copies was
assembled in a scratch directory and `:app:bootJar` run on it. It fails in **six
seconds**, and earlier than expected — Gradle 9 refuses to configure a project
whose `projectDir` does not exist, so it never reaches the compile error. The
same tree with `adapters/` added builds the jar in seventeen seconds, and that
jar contains no `devsignin` classes, which is the exclusion holding on the
deployment path too. **The image itself has still not been built end to end this
session** and the tracker says so.

This is the same shape as the stale stage table: a claim that was true when
written, never re-checked, and load-bearing for something else. It sits directly
on **B5**'s path, since Render builds from this file.

A COPY list is a second copy of the module list and it drifts in silence. The fix
is one line and a comment saying so.

#### Two hours of confusion worth writing down

**`TestRestTemplate` on Boot 3.5 follows redirects.** It resolves to a
`JdkClientHttpRequestFactory`, and the first run of `DevSignInTest` reported
`401 UNAUTHORIZED` for a sign-in that had worked perfectly — the client chased
the 302 to a page that needs the session it had not yet been handed, and reported
*that* response. Nothing about the message pointed at the client. A test of a
redirect has to be able to see the redirect; the class now builds a
`Redirects.DONT_FOLLOW` client and says why.

**A `@Bean` method named after its `@Configuration` class collides with it.**
`DevSignInSecurity#devSignInSecurity` produced a `BeanDefinitionOverrideException`
whose message names the same bean twice and does not say that is the problem.

#### E4's one-command test is wrong on the WSL2 backend

The environment note says `com.docker.service = Stopped` means somebody has to
start Docker Desktop by hand. This session read `Stopped` **and the engine
answered anyway** — `docker info` returned server version 29.7.2 and
Testcontainers ran all session. The Windows service is not the engine when Docker
Desktop runs on WSL2. `docker info` is the test; the service is a hint at best.

#### The workflow trap fired again, and the pipeline confirmed the tree

The previous session recorded that CI fires on `pull_request` and on push to
`main` only, so **a push to `dev` with no open PR runs nothing, silently**. It
fired twice more here. PR #15 had been merged between sessions, so the first
push to `dev` was watched by nothing at all — **PR #16** exists because somebody
looked. Then PR #16 was merged *during* the write-up, so the tracker commit that
followed it was unwatched too, and the Status line describing #16 as open was
stale within minutes of being written.

Both were caught by looking rather than by anything failing, which is the point:
there is no failure mode here, only silence. The rule that survives is **open
the PR before trusting the push, and run `gh pr list` rather than assuming last
session's PR is still open.**

Run `34327487736` on `0de2e50` is **green: 0 failed, 16 skipped**, and the skips
are exactly the three snapshot-gated classes (8 + 5 + 3) — a pass there would
mean a snapshot had been committed by accident. **`main` is green on the merge
too**, run `34327808367` on `19197d3`, which is the first time N24's tree has
been verified on the branch that would be deployed.

**One thing that is stronger than it was recorded as.** `DeployableJarTest`
passed on the runner, which means the development sign-in's absence from
`storm-almanac.jar` is proven against **a jar CI built from a clean checkout**,
not only against one this machine produced. The guard travels with the pipeline.

#### Where this leaves Phase 4

The signed-in half is developable, which is what N24 was for. What is now in
front of the screens is **N25** — inventory editor, goal picker, plan view,
catalog with the personalized overlay — and the client half of the sync, none of
which had anything to run against this morning.

---

### 2026-09-09 (fourteenth session) — the cheap way out was not there, and the decision grew teeth

**N26 asked the question that would have made ADR 0015 cheap, and the answer was no. Then N27 turned out to be two jobs, one of which no session can do — so this session built the other one, and it is the one that makes 0015 enforceable rather than merely stated.**

#### N26: the publisher discloses the rates that do not cost anything

ADR 0015 named three ways out of its bootstrap problem, and the first would have
collapsed the hard half entirely: many titles in this market disclose stage drop
rates under regulatory pressure, and if Reverse: 1999 did, **595 statistical
facts become 595 static ones** and Phase 6 goes back to being an improvement
rather than a prerequisite.

**It does not.** The stage screen grades each reward `Fixed`, `Common` or
`Possible`, and puts a number on none but the first. Every published percentage
for a `Common` or `Possible` drop is somebody's crowdsourced sample — including
the twenty this project benchmarks against, which were never a disclosure to
check ourselves against but another estimate.

**The size of the reprieve was measured, not guessed.** On the pinned 3.5
snapshot's `stages3_3_greedy.json`, where `count: 1` marks a fixed-reward stage:
105 stages, **779 drop facts, 15 declared and 764 sampled**. Two percent.

Two things came back that nobody asked for, and both are worth more than the
answer:

- **Gacha rates *are* disclosed**, per rarity, with the pity counter, in the
  client's own summon rules. That is the live half of **Q4** reduced to one
  screen's worth of reading, and it is `PUBLISHER_DISCLOSURE` rather than a
  sample. Phase 5 needs it.
- **The `Fixed`/`Common`/`Possible` grade is itself a first-hand fact.** It is
  free, it exists for all 595 pairs before a single run is farmed, and it is
  enough to order drops within a stage and to reject an estimate that
  contradicts it — a `Fixed` reported at 40% is wrong on its face. **The model
  has nowhere to put it, and this session deliberately did not build one.** It
  costs a bundle field, a parser, a writer, a migration and a JDBC round trip,
  and its only consumer is Phase 6. That is exactly the reasoning that deferred
  N20 and N18, and the discipline is worth more than the field.

#### N27 was two jobs, and one of them is not a session's to do

The next action read "author the first self-sourced bundle, and give a fact its
provenance". Working on it surfaced that those are not one task.

**The authoring cannot be delegated to a session, and the reason is ADR 0015
itself.** A fact enters because someone *read it in the game or in the
publisher's disclosure*. An aggregator, a web search and an AI session are the
same disqualified thing: none of them can reach a game screen, and all three can
only surface what somebody else already wrote down. Producing a plausible-looking
R1999 bundle here would have been laundering with extra steps — the precise
failure 0015 was written to prevent, committed by the tooling built to prevent
it. **So the honest output is that N27 is now the maintainer's**, with the loop
written down in
[authoring a first-hand bundle](../game-facts/authoring-a-first-hand-bundle.md)
so that the reading is the only thing left.

#### The half a session could do, and why it was the important half

**ADR 0015 had a hole in it that no amount of prose could close.** A number read
off a stage screen and a number copied out of Kornblume are *byte-identical once
typed*. Nothing downstream could tell them apart; neither could the person who
typed them, six months later. As written, 0015 was a promise about future
behaviour with no mechanism — and this repository already knows what that is
worth, because the adapter read a stale stage table for five sessions with two
thirds of the game missing and every test stayed green, because nothing was
checking the thing that was wrong.

[ADR 0016](../adr/0016-provenance-is-a-property-of-the-data.md) is the
mechanism. Four decisions in it, and the last two are the ones a future session
would otherwise re-litigate:

- **Provenance lives on the bundle, not on `Stage` or `Item`.** Sourcing is a
  property of the act of reading, not of the game — and hanging it on a domain
  record puts it one field access away from the solver. A solver that *can* read
  where a number came from is one that can eventually be made to prefer numbers
  from one place, which is a bias nobody asked for and nobody would notice.
  `Drop.sampledRuns` stays the deliberate exception, because ADR 0011 makes the
  solver genuinely need it.
- **A default with per-fact overrides**, not a field on every fact. Requiring
  2 700 identical strings produces 2 700 copy-pastes and no more truth, and the
  pressure would be to generate them.
- **Silence parses, and cannot publish.** A bundle declaring no provenance is
  `UNRECORDED`, which is not first-hand. Requiring the field would put six lines
  of ceremony on every throwaway test bundle, and ceremony under that pressure
  gets filled in with whatever passes. Absence gets a defined, conservative
  meaning instead — the same shape as `sampledRuns == 0` meaning *declared*.
- **The gate is on `publish`, not `ingestDraft`, and has an explicit escape
  hatch.** It has to: ADR 0015 keeps the Kornblume adapter as a cross-check, and
  a cross-check must reach a published version to be diffed against one. So
  `publish(game, sequence)` is the short call *and* the strict one;
  `publish(game, sequence, true)` and the CLI's literal `second-hand` word are
  what it costs to approve somebody else's data, on a command line that ends up
  in a shell history. `KornblumeAdapter` hard-codes `THIRD_PARTY` and cannot be
  told otherwise, so there is no call site to launder through.

**Storage materialises every fact's provenance** — one row per declared fact,
not one per override. The default is an authoring convenience and a database
that stored it could not answer "where did this come from" without the file
beside it. **The first-hand policy is in `Provenance.Origin` and nowhere else**:
`V7` constrains the origin set and says nothing about which of them count, and
the gate reads origins back as text and asks the enum, because a
`WHERE origin <> 'THIRD_PARTY'` would be a second copy of the policy in a
language that cannot fail to compile when the first one changes.

**The one publish of real upstream data in this repository now says so in code.**
`RealUpstreamPatchTest` passes `true`. That is the test admitting what the ADR
says in prose, and it is the sort of thing that would have been a comment before.

#### What this does not do, stated because it is tempting to overclaim

**It does not make anything in this repository first-hand.** The only bundles
that pass the gate are the synthetic fixtures, and they pass by declaring
`AUTHORED_FIXTURE` — honest, and evidence of nothing about a real game. Every
Reverse: 1999 number in the tracker still comes from Kornblume. The gate is what
will stop that shipping; it is not progress on replacing it.

**It does not stop a determined liar**, and is not meant to. Someone can mark a
laundered fact `OBSERVED_IN_GAME`. What is now impossible is doing it *silently*:
the default is honest, the exception is explicit, and both are in the file that
was approved.

**Provenance is written and never read.** The publish gate queries it; no API
response carries it. A catalog page saying where a number came from is the
honest end state and belongs with Phase 4's catalog screens (N25), not ahead of
them.

#### What the tests say, and the one that was wrong

**257 tests, 0 failed, 0 skipped locally** with snapshots present — up from 244.
Twelve new: `BundleProvenanceTest` (6, no database), `GameDataProvenanceTest`
(5, real Postgres), and the CLI's second-hand walkthrough.

**The gate's first real run failed the test rather than the code**, and the
failure was worth keeping. The refusal lists the first five offending facts
sorted and counts the rest; the assertion had picked `item:gold`, which sorts
after `fodder:` and fell outside the five. The fix was to assert on the count as
well — `20 fact(s)`, `banner:warden-debut (borrowed)`, `and more` — which is a
better test than the one that was written, because "twenty facts are somebody
else's" and "five are" are different decisions and only the count separates
them.

**CI confirmed the tree**: run `34308177004` on [PR #15](https://github.com/kietnt4412/storm_almanac/pull/15),
green, 0 failed and 16 skipped — 8 `RealUpstreamPlanTest`, 5
`CommunityBenchmarkTest`, 3 `RealUpstreamPatchTest`, which is exactly the
snapshot gate and nothing else.

**And a workflow fact worth not rediscovering.** The first push to `dev` ran no
CI at all, silently. `ci.yml` fires on `pull_request` and on push to `main`, so
**a push to `dev` with no open PR is unverified by the pipeline** — PR #14 had
been merged between sessions, which closed the only thing that was watching the
branch. Opening PR #15 is what produced a run.

#### E1 and E4: two environment notes

`-Djavax.net.ssl.trustStoreType=Windows-ROOT` was needed throughout — **E1 is
still live**.

**New, and worth recording because it cost twenty minutes:** Docker Desktop's
GUI processes were running, its WSL distro read `Stopped`, and
`com.docker.service` was `Stopped` too. That is *not* the "give it minutes" case
E2's note warns about — the engine was never going to come up on its own, and
`Start-Service` from a non-elevated session fails with `Cannot open
com.docker.service service on computer '.'`. **The distinguishing check is
`Get-Service com.docker.service`**: `Stopped` there means somebody has to click,
and no amount of waiting substitutes. The maintainer restarted it and the whole
suite ran.

### 2026-09-09 (thirteenth session) — the sync debt paid, and Phase 4 opens by being looked at

**Two halves. N23 pays the piece of Phase 3's scope Phase 3 did not build; then Phase 4 opened and four minutes of actually serving the frontend found two defects that five sessions of green builds had not.**
Phase 3 was closed on its exit criterion — a plan computed from stored state —
while its scope line also said sync, and the twelfth session wrote that down
honestly rather than quietly. This session paid it.

#### The next action was stale again, for a new reason

"Merge PR #13" was the first action. It was merged before the session opened,
`main` at `2864c95`, run `34217574439` green. That is **four sessions running**.

The eleventh and twelfth entries both diagnosed this as a session running out of
room to write its tracker line after pushing, and prescribed writing the line
*with* the push. The twelfth session did that. It happened anyway — which means
the diagnosis was wrong. **Merging a PR is a click on GitHub that happens between
sessions, so a next action of the form "merge PR #n" is stale as soon as the
maintainer opens the browser.** It is not a next action at all; it is a fact
about the remote, and facts about the remote belong in *Status*, checked at
session start. That is the change made here, and it is the fourth attempt at
this particular lesson.

#### What sync had to decide, and why each rule earns its place

`PUT /inventory` takes a complete map and stores it, and the implementation
matches the signature exactly: delete every row, insert the body. A phone that
was offline for an hour then saves, and every row a browser added meanwhile is
gone — not merged, not flagged, gone.

Three families of answer were weighed and two rejected in
[ADR 0014](../adr/0014-sync-is-last-write-wins-per-key-against-a-clock-that-outlives-the-value.md).
A **CRDT** is correct by construction and merges the wrong thing here: two
devices that each read 40 in the game did not observe 80, and a counter is not
an integer, so it costs the schema, the API and the client at once. A **revision
token** turns every concurrent edit into "reload and type it again", which is the
behaviour that makes people stop using a companion tool. **Last-write-wins per
key** is what `Inventory`'s javadoc has promised since Phase 3 and what V5 left
the timestamp column out for, saying it would arrive with the merge.

Four rules decide a key. Each is here because its absence is a *silent* wrong
answer:

1. **The client says when it edited, per key.** A server stamp makes an edit win
   for having arrived late, which is the bug rather than the fix. Per key and not
   per request, because a device offline for an hour changed one item at 09:00
   and another at 11:00 and one batch stamp would have to lie about one of them.
2. **No edit may claim the future.** This is the bill for trusting a client
   clock, and it is cheap: without the clamp a device set a year fast pins every
   key it touches against every later edit from anywhere, permanently, with
   nothing in the system able to correct it.
3. **A removal is remembered after its value is gone.** The one that shaped the
   schema. V5's decision 4 makes absent the only representation of zero, so
   clearing an item deletes the row — and a timestamp on that row dies with it.
   A device offline since before the delete re-adds the item, finds nothing to
   lose against, and five thousand gold comes back from the dead. **A clock that
   outlives its value cannot be a column on the row it outlives**, so V6 is a
   table and V5's decision 4 is untouched.
4. **A full save speaks for the keys it left out.** **This one was found by a
   failing test, not by the design.** The first cut had `saveInventory` stamp
   every key it wrote, which passes every obvious test and leaves a gap: a `PUT`
   claims something about every slug in the game *including the ones the player
   has none of*, and there is no row on which to record that and no way to
   enumerate the keys it would need. So a stale patch lost for a key the save
   mentioned and won for one it did not. `player.sync_watermark` is the fix — one
   row per profile per aggregate — and finding it this way is the argument for
   writing the adversarial test before believing the design.

All four are decided in **one SQL statement** rather than in Java around a
select. Read-then-write across two statements is a lost update waiting for a
scheduler to find it, and two devices syncing at once is precisely the case the
route exists for. The row count is the answer: 1 means the edit won and its value
is written, 0 means it lost.

#### What the response says, and what has no route

The merge hands back **the keys that lost**. A merge that silently drops the
losing half leaves the client showing a value the server does not hold, and the
player then edits from a screen that is quietly wrong until something makes them
reload.

**Goals get no PATCH.** An inventory and a roster are maps, so a key is a merge
unit. Goals are an ordered list whose *order* is what the player is editing, and
two devices that reordered it have no per-key answer; inventing one would mean a
plan computed against priorities nobody chose. The `CHECK` on both new tables
names `inventory` and `roster` and nothing else, so the decision is enforced
rather than merely documented.

#### The three fakes throw

Three `PlayerStateRepository` fakes exist in tests — two in `app`, one in
`planner` — and all three now throw `UnsupportedOperationException` on the merge
methods rather than implementing them. A fake that answered would be a second
copy of the merge rules, drifting from the real one until a test passed against a
merge nothing ships.

#### Numbers

**244 tests, 0 failed, 0 skipped locally** (228 before; the snapshot is present
on this machine, so the 16 snapshot-gated ones ran). 16 new, all in
`OfflineSyncTest`, and every one of them is a way the merge could be wrong
without saying so.

#### Then Phase 4 opened, and serving the thing found two bugs immediately

The tracker had said "the frontend has never been served" for five sessions. It
took about four minutes to stop being true — `docker compose up -d postgres`,
`bootRun`, `npm run dev`, load `localhost:5173` — and the page rendered
`Backend: ok (dev)` from a live API on the first try.

It also rendered it in near-black on near-black. **The app declares no colours at
all**: Tailwind's preflight sets neither, so both were inherited from the browser
and the page was legible on the machine it was written on and invisible under a
dark system theme, which is most phones. `vite.config.ts` had committed the PWA
manifest to a palette since Phase 0 and the stylesheet had never used it.

The second one was worse. Probing the routes a client would really ask for:

```
/api/games/reverse-1999/versions   200
/api/games/reverse-1999            401   <- should be 404
/api/games                         401   <- should be 404
/api/nonsense                      401   <- correct
```

**Boot renders a 404 by forwarding to `/error`, and `/error` was not on the
public list**, so the forward hit `anyRequest().authenticated()` and the caller
got 401. A stale catalog link would therefore tell a client its session was dead,
and a client acts on that by sending the reader to a login page — on a public
catalog whose entire purpose is that a stranger from a search engine reads it
without an account.

**Every one of the 244 tests passed with this in place**, because every one of
them asked for a path that exists. That is the same shape as Phase 0's 401 on
`/api/health` and the same shape as the stale stage table: a class of defect that
is structurally invisible to the tests being written, and visible in the first
minute of using the thing.

`/api/nonsense` staying 401 is *not* the same bug surviving — a path outside the
public list is refused without confirming whether it exists, which is the design.
The fix is one matcher (`/error`, permitted; the `DispatcherType.ERROR` form
needs the servlet API and `identity` deliberately has no web starter) plus a test
that asks for paths that do not exist.

**One existing assertion had to change, and it was the bug talking.** An
anonymous `POST` to a public game-data route asserted 401; it is now 403. CSRF
runs before authorization, so the POST is refused for having no token — that 403
was being forwarded to an error page that itself demanded an account, and the 401
everyone saw was the *second* refusal. Same denial either way; the test had been
pinning the wrong reason for it.

#### The wall the signed-in half runs into

`/oauth2/authorization/google` answers 401, because `oauth2Login` is installed
only when a provider is configured and none is. So **there is no way to sign in
at all locally**, and Phase 4's whole product — inventory editor, goal picker,
plan view, the personalized overlay — lives behind `/api/me`. That is **N24**,
and the two ways out are not equivalent: a real provider needs the deployed URL
(so it lands with **B5** and finally runs the exchange that has never run), while
a development-only sign-in unblocks the UI now and proves nothing about the
exchange. If the second is taken it has to be impossible to enable in production
by construction rather than by configuration.

#### Hosting, decided

**Vercel for the frontend, Render for the backend, both free tier.** D1's
reversal trigger fired on both halves at once — Phase 4 reached, and a free tier
accepted — and no money is spent, so D1's premise stands.

Two consequences to settle before writing any deploy config, both recorded under
D1's reversal note. **The backend's session model is same-origin throughout**: a
cookie session, a CSRF token in a cookie the page reads, an OAuth redirect
landing back where it started. A Vercel rewrite of `/api/*` to Render preserves
every bit of that for the price of one hop; two real origins cost CORS,
`SameSite=None; Secure` and a redirect that has to cross back. And **the free
tier sleeps** — a cold start is tens of seconds against an optimizer that
promises two, which is a product decision to make before five strangers meet it.

#### And then the data question, which was the largest decision of the session

Explaining why F2 exists turned into deciding not to send it.

**The facts, re-verified rather than recited.** Kornblume: `license: null`, no
LICENSE file in the tree, owner `windbow27`, not a fork, 90 stars, last pushed
2026-08-24. Its README credits Huiji Wiki, 必要的记录 and ArkPlanner. All checked
through the GitHub API that day rather than trusted from the seven-session-old
note in prior-art.md.

**The legal picture is more mixed than ADR 0009 implies, in both directions**, and
it is worth having written down once. Raw facts are not copyrightable in the US
(*Feist*, 1991), so a drop rate is thin ground for an aggregator to stand on —
what can be protected is selection and arrangement, and we re-model into our own
schema rather than copying theirs. But the EU/UK *sui generis* database right
protects substantial extraction regardless of originality, and **the publisher's
rights in names and text sit underneath everything and are unaffected by anything
Kornblume could say**. So permission from windbow27 would never have been a
complete answer, and its absence was never a complete prohibition.

**None of that is why the decision went the way it did.** Presented with ask /
launch on the synthetic title / source it first-hand, the owner chose the third.

**What it costs, measured rather than guessed** — patch 3.5, counted from the
local snapshot:

| | count |
|---|---|
| items | 91 |
| stages | 100 |
| **individual drop-rate facts** | **595** |
| recipes / material lines | 50 / 113 |
| released arcanists | 118 |
| insight material lines | ~972 |
| resonance entries | ~1 502 |
| psychubes | 37 |

**~3 300 facts per patch, split ~2 700 static to 595 statistical**, and the split
is the whole story. The static half is readable off a game screen: tedious,
tractable, and mostly *additive* per patch once backfilled. **The 595 cannot be
read off a screen at all** — each is an estimate over many runs.

**Which produces the bootstrap problem, now the main risk in the project.** The
optimizer cannot rank a stage without a yield → no drop data, no plan → own drop
data needs Phase 6 → Phase 6 needs users → users need a plan. Three ways out, and
the first would collapse the hard half entirely: **find out whether the game
discloses its own rates** (**N26**); otherwise launch the catalog alone, which is
public and already in Phase 4's scope; or seed thin, honest samples.

**ADR 0011 turns out to be what makes this survivable, which nobody planned.** A
yield carries the runs behind it and is discounted to a Poisson lower bound, so a
hand-collected sample of twenty runs is *usable and honest* — the plan simply says
how much it does not know. A model storing a bare rate would have made
self-sourcing impossible to do truthfully.

**Three things this deliberately did not do:**

- **Delete the adapter.** It stays as a never-shipped cross-check. Removing it
  first would leave the project with no real data at all and destroy the current
  evidence before a substitute exists — and diffing the first self-sourced bundle
  against an independent reading of the same patch is worth more as a check than
  it ever was as a source. F2 blocked *public deployment of upstream numbers*; a
  local test that ships nothing was never the blocked part.
- **Keep the load-bearing claim quietly.** The nine agreements and 3 880 against
  4 017 are computed from Kornblume-fed inputs. They are now a number to
  **re-earn**, and the tracker says so where the claim is made. The benchmark
  *method* survives — the guide it compares against is a separate artifact.
- **Pretend the pipeline has to change.** It does not: schema, parser, writer,
  CLI, diff, optimizer and statistics are game-agnostic by construction, and
  **a hand-authored bundle needs no adapter at all** — the canonical JSON *is*
  the authoring format and *preview, ingest, publish* already exists. Machinery
  built to support a claim about second games turns out to cover this too.

**The one thing that has to be added is provenance.** `Drop` carries
`sampledRuns` (0 meaning *declared*); the catalog axis carries nothing
equivalent, and without it "self-sourced" is unfalsifiable. **N27** starts with
one stage and one character end to end rather than a backfill, because the point
of the first bundle is to learn what authoring one costs before committing to
2 700 of them. And the integrity rule is the whole decision: **a fact enters
because someone read it in the game, not because they re-typed it out of an
aggregator.** Re-typing would be laundering.

Q2, Q3, F1 and F2 all close on this. Q5 dissolves for anything sourced after it —
you know which region and patch you read, because you read it.

#### What sync still does not do

- **No two real devices have ever synced.** Every scenario above is one MockMvc
  request following another inside one JVM. The concurrency the SQL is shaped for
  — two requests interleaving on the same key — is argued for and not measured.
- **A merge publishes nothing.** Nothing downstream can react to a synced edit,
  so a cached plan is not invalidated when an inventory moves under it. Not
  needed until something wants it; worth knowing before Phase 6 caches anything
  keyed on player state.
- **The client half does not exist.** There is no queue, no retry, no offline
  store — the PWA that would use this is Phase 4, and it has never been served.

### 2026-09-08 (twelfth session) — Phase 3 opens and closes: the optimizer finally meets a real account

**Two items, N21 and N22, and between them Phase 3's exit criterion is met:** a
plan computed end-to-end from stored state on a real account.

#### The tracker was a step behind, and the check took a minute

The first next action was "merge PR #11". It was already merged, and so was
**PR #12** behind it — both before this session started. `main` is `eb2894a`,
runs `34203224613` and `34205089646`, both green, **16 skipped and they are
exactly the three snapshot-gated classes** (`RealUpstreamPlanTest` 8,
`CommunityBenchmarkTest` 5, `RealUpstreamPatchTest` 3). Nothing was wrong; the
session that pushed them ended before it could write the line. Worth recording
only because the tracker's own first rule is that a session ending without this
file reflecting what happened starts the next one from a lie — and the cost of
that lie here was one `gh pr list`, which is the cheap version.

#### N21 — the schema, and four decisions that are not obvious afterwards

`V5` gives `identity` and `player` their tables. The DDL is unremarkable; the
four decisions written into its header are the part worth keeping.

**1. No foreign key crosses a schema.** `player.profile.account_id` names a row
in `identity.account` and has no `REFERENCES` clause. The invariant is "one
schema per module, modules talk through events", and its justification is that
extracting a module later is a deployment change rather than a rewrite — which
stops being true the moment one module's DDL cannot be applied without another's.
`ModuleBoundaryTest` enforces the Java half and can see nothing of this half, so
it is written down instead. **The cost is real and is stated rather than hidden:
a deleted account leaves orphan profiles until something reacts to an event, and
nothing publishes that event yet.**

**2. Nothing points into `gamedata` either, and there it is not a choice.** Every
gamedata row is scoped to one `game_data_version` and replaced wholesale by the
next patch (V2's first decision). A player's inventory outlives every patch, so it
*cannot* point at rows a publish deletes. Items and entities are therefore stored
as the upstream slug — the same string `ItemId` and `EntityId` wrap — and resolve
against whichever version a plan is solved against. **An item that disappears from
the game stays in the inventory and stops being demanded**, which is the behaviour
a player expects and the one a foreign key would forbid.

**3. An identity is `(provider, subject)`, never an email.** Providers let people
change their email and some let people change it to one they do not control.
Keying an account on the email claim is the standard account-takeover route into
a service like this one, it costs nothing to avoid, and it is now asserted in two
places rather than commented in one: `SignInTest` refuses a provider payload
carrying an email and no subject, and `PlayerStateDatabaseTest` shows two
providers claiming the same email producing two accounts.

**4. Absent means zero.** An inventory row with quantity 0 and no row at all are
the same state, `Inventory.with(item, 0)` already collapses them in Java, and a
`CHECK (quantity > 0)` makes the database agree so the two cannot diverge. There
is a test that bypasses Java and inserts the zero directly, because a constraint
nobody has watched refuse anything is a comment.

**Each save replaces a whole aggregate**, which is what
`saveInventory(Inventory)` says and not a placeholder for something cleverer. The
javadoc names what that costs: two devices saving concurrently do not merge, the
second wins entirely, and a row the first added is gone. Correct for the contract
as written, wrong for a phone that was offline for an hour — which is why the
per-key timestamp last-write-wins would need is **deliberately not in the
schema**. It arrives with the merge, in the same change, on the same reasoning
that keeps drop estimates out of `SolveKey` until phase 6 publishes one.

`GameDataDatabaseTest` became **`SharedDatabaseTest`**. The rename is the whole
point of the change: a base class named for one module is one the next session
reads as not applying to them, and that reading costs a second container and a
second context boot on every push forever.

#### N22 — sign-in, the routes, and the first plan nobody handed a goal set

**What actually changed.** Every plan in this repository before today was
computed from a goal set a test passed to the optimizer. `PlanFromStoredStateTest`
passes it nothing: a person signs in, the sign-in creates the account, the account
creates a profile, the profile is given an inventory, a roster and goals over
HTTP, and `POST /api/me/profiles/{id}/plan` reads all three back out of Postgres.
The body carries `energyPerDay` and `horizonDays` and nothing else, because those
are the two things that are facts about the sitting rather than about the account.

**Where the principal is minted, and why it matters.** Sign-in is the only event
that carries a provider subject, so it is the only place an account can be
created — but doing it in an authentication success handler would leave a window:
by the time a handler runs the principal is already in the security context, and a
request arriving on that session would find a principal that cannot name its
account. Creating the account *while the principal is being built* closes it.

**Everything provider-specific is one pure function.** `SignIn.from(provider,
attributes)` is the whole of it, and the only difference between providers that is
not cosmetic is which key holds the stable subject: OIDC says `sub`, Discord says
`id` because Discord is OAuth2 without the OIDC half. That is also why `identity`
carries two user services rather than one. Seven tests over the attribute maps
Google and Discord actually return, and no authorization server stood up to get
them — a mock provider would test Spring's protocol implementation rather than
ours.

**Login is conditional and the deny is not.** `oauth2Login` installs only when a
`ClientRegistrationRepository` exists. No client secrets exist: nothing is
deployed (D1) and a registration is issued against a redirect URI that has no
URL to be issued against. Declaring the registrations with empty
`${GOOGLE_CLIENT_ID:}` placeholders is **not** a harmless default — a
`ClientRegistration` refuses a blank client id, so the application would fail to
start everywhere the secret is absent, which is everywhere including CI. So
`application.yml` documents the properties in a comment and declares none of
them. What is *not* conditional is the authorization: an unconfigured deployment
serves the public catalog and refuses everything else, which is the correct
behaviour for one, and `ApplicationBootTest` still passes unchanged.

**Two phase 0 decisions came due.** `SessionCreationPolicy.STATELESS` and the
disabled CSRF both carried "phase 3 revisits this" in their comments, and both
were revisited: `IF_REQUIRED`, so a `permitAll` probe still mints no session and a
signed-in browser gets one; and CSRF back on with `CookieCsrfTokenRepository`,
with a test that a write without a token is 403.

**Authorization lives in one method.** `OwnedProfiles.require` is what every
account-scoped route goes through, for the same reason `CurrentAccount` is one
class: this is the check whose absence hands one player another player's
inventory, and a check copied into eight controller methods will be missing from
the ninth. **Somebody else's profile answers 404 and not 403** — a 403 confirms
the profile exists, which turns a guessable id into an oracle for enumerating
them. And every route is under `/api/me`, so **no route takes an account id**,
which means there is no route whose authorization can be forgotten.

**The route is synchronous, and `SolveCoordinator` is still not a bean.**
`MipOptimizer` is bounded at two seconds and returns the best it has proven
rather than running long; a two-second budget is a promise a synchronous route
can keep. The coordinator exists to hand back a ticket for a solve that does
*not* fit that budget, and there is no asynchronous surface for a ticket to be
useful on — the WebSocket push that would make one is phase 9's neighbourhood.
`SolveCache` and `Optimizer` *are* beans now, and the reason is exactly the one
N15 gave for not registering them then: they finally sit on a path a real request
takes. This is what "registering beans nothing consumes would have been ceremony"
was waiting for.

#### What this does not cover, said before anyone reads the tick as more than it is

- **The OAuth token exchange has never run.** No provider is configured, no
  client secret exists, and no redirect has ever been followed. What is tested is
  the principal, the account it creates, and every authorization rule around it.
  The exchange itself is Spring's code and remains unexercised until there is a
  deployment to register a redirect URI for.
- **The end-to-end test goes through MockMvc, not a socket.** That is a
  deliberate step down from `GameDataApiTest`'s real HTTP and is stated in the
  test's javadoc. It is there because an authenticated session cannot be minted
  over a socket without an authorization server to redirect to. The real filter
  chain, dispatcher, Jackson and database are all exercised — the anonymous and
  cross-account cases both fail *at the filter chain* — but the servlet container
  is not, and that is the layer that caught phase 0's 401.
- **There is no sync.** Phase 3's scope names it and this session did not build
  it. PUT replaces; there is no per-key patch route and no merge. Anything the
  plan says about offline editing is still unimplemented, not merely untested.
- **Nothing is deployed, and the frontend has still never been served.** Both
  unchanged from last session.

#### The machine, not the project

The user asked to stop git prompting for an account on every push. Windows
Credential Manager held two GitHub logins —
`git:https://tuankiet4412@github.com` alongside `git:https://github.com`
(`kietnt4412`) — and Git Credential Manager shows a picker whenever there is more
than one. Deleted the stray one and pinned
`credential.https://github.com.username = kietnt4412`. The push at the end of the
session went through with no prompt. Recorded as **E3**.

#### Numbers

**228 tests, 0 failed, 0 skipped locally** with snapshots present. Twenty-eight
new across the session: 200 → 214 on N21 (`PlayerStateDatabaseTest`, 14) → 228 on
N22 (`PlanFromStoredStateTest` 7, `SignInTest` 7). **CI runs 212**, since the
same 16 snapshot-gated ones skip there as always — a pass there would mean a
snapshot had been committed by accident.

The p95 was not re-measured and did not need to be: nothing in either commit
touches the model, the solver or the yields. `RealUpstreamPlanTest` passed
unchanged, which includes its p95 assertion.

**The tracker came out at 610 lines against its own "about 550", and that is a
decision rather than drift.** It was 584; closing a phase added a Status
rewrite, five table rows and four unverified entries, and about forty lines were
cut elsewhere to pay for it — the closed phases on the board collapsed to one
line each with their full prose moved here, three statistics rows merged into
one, E2 and Q3 tightened, N18 and N19 compressed. What was *not* cut is the
unverified list, which is where the pressure now is: seventeen entries, and it is
the most valuable section in the file. **The next session that needs room should
take it from the `Current state` table's Phase 1 rows** — the gamedata pipeline
is closed, stable and fully described here — and not from the list of things this
project has not proven.

### 2026-09-08 (eleventh session) — the plan gets a calendar, and the horizon turns out to be load-bearing

**One item: N14, the last thing in Phase 2's scope.** Phase 2's exit criterion
was met three sessions ago; this is what the tick did not cover.

#### The formulation decision, and why it was made before any code

The tracker's own warning was the constraint: **p95 1 808 ms against a 2 000 ms
assertion**, 90% of the budget, on roughly a hundred stage variables. The
obvious time axis — index every stage variable by day, `x[s,d]` — makes that
three thousand variables over a thirty-day horizon, and branch-and-bound does
not degrade linearly in integer variables. There was no reading under which the
number Phase 2 closed on survives that.

So the horizon is a **scalar parameter**, not an index, and every consequence of
time is a capacity computed from it.
[ADR 0013](../adr/0013-the-horizon-is-a-scalar-not-an-index.md) has the full
argument and the reversal trigger. The three rows:

- energy is finite: `sum_s x_s * energy_s <= D * energyPerDay`
- a cadence is a count: `z_r <= occurrences(cadence, D)` — **exact**, because
  `z_r` is an integer, so `7 * z <= D` is `z <= floor(D/7)` with no rounding
- rotation is a **shared** capacity, one row per *subset* of distinct weekday
  restrictions

**The subset part is the one thing here that is not obvious and is not
optional.** Capping each rotation group against its own days is wrong: a stage
open Tuesdays only and a stage open Tuesdays-or-Fridays can each fit their own
cap while between them demanding more Tuesdays than the window holds. The
condition that rules that out is one row per subset — `2^k` in the number of
*distinct restrictions* (one for a game with no rotation, three for the fixture),
not `2^stages`. There is a test that fails without it: 20 ore and 30 relic over a
week is 100 against 100 and 150 against 200 group by group, and 250 against 200
jointly.

#### FEWEST_DAYS is a search, not a variable

Making `D` a variable would couple every capacity row and put the day count into
the branch-and-bound. It is not needed: **feasibility is monotone in the
horizon** — another day adds energy, may add a cadence occurrence, may add an
open day, and takes nothing away. So five probes bisect thirty days, and the
answer is a least-energy solve pinned to the horizon the search settled on. A
timed-out probe counts as infeasible, which can only make the answer longer than
the true shortest plan, never shorter.

#### The thing that was not anticipated: the horizon is load-bearing

Free income accrues at no energy cost, so **with an unbounded horizon the
cheapest plan is always "wait", without limit.** `LEAST_ENERGY` has no answer
until something bounds the calendar. That is why `SolveRequest` carries
`horizonDays` and why it is not a formality — it is what makes the objective
well-posed. It also means `LEAST_ENERGY` always spends the whole horizon, which
is correct rather than lazy.

On the acceptance fixture this is vivid, and both numbers were derived on paper
before the test was run and came out exactly:

| Insight 1, same goal | Energy | Days |
|---|---|---|
| `LEAST_ENERGY`, 30-day horizon | **0** | 28 |
| `FEWEST_DAYS` | **370** | 2 |

Four weekly quests grant 8 sigil-lesser and 4 000 gold; four daily logins close
the last 1 000. Nothing is farmed. Asked the other way: NOW is a Monday, a
two-day horizon holds exactly one Tuesday, `pg-2-3` is open on it, 9 runs at 20
is 180 of the 240 that Tuesday supplies, and 19 runs of `pg-1-1` cover the gold.
A one-day horizon holds no Tuesday and no Friday and is refused. **The old model
returned one plan and a note apologising for it.**

#### What it cost on real data: nothing, and that cuts both ways

**p95 1 805 ms → 1 807 ms.** The benchmark is unchanged to the unit: 3 880
Activity against the guide's 4 017, nine agreements. The optimality gap moved
2.37% → 2.30%.

The reason is worth stating plainly rather than being pleased about.
`KornblumeAdapter` emits `Availability.ALWAYS` for **every** source and **no
rewards at all** — checked by grep, not assumed — and the upstream publishes no
weekday field anywhere. So on the only real upstream this project has read, the
time axis adds one capacity row and no integer variables, and **rewards and
rotation turn out to be in exactly the same data position as shops were found to
be in the tenth session.** The whole change is exercised on real data by the
energy budget alone; the rest is proven on the synthetic fixture, which is the
same standing the catalog axis has had since Phase 1.

That is not an argument against having done it — the model is where a second
game (Phase 11) and our own drop reports (Phase 6) plug in, and `FEWEST_DAYS`
was returning a wrong answer with a note attached. It is an argument against
reading "N14 landed" as "the optimizer now schedules real weeks".

#### Two smaller decisions worth keeping

**Cadences round against the player.** `MONTHLY` is 31 days, not 30, because a
calendar month is 28 to 31 and a plan assuming the short one promises income that
may not arrive. `EVENT` and `ONE_OFF` count **once** however long the horizon,
because their schedule is not in the bundle. Same reasoning as ADR 0011's
sample-size discount, applied to time instead of to drop rates.

**Reward claims pay the tie-break weight, and only where rewards exist.** Free
income is free, so nothing in the objective distinguishes a plan leaning on four
weekly quests from one leaning on thirty dailies it never needed — and the second
would be reported to a player as something the plan depends on. The millionth
that keeps pointless crafts out fixes it. It costs the objective its
integrality, which is what overflowed ojAlgo's stack on a real patch in the
seventh session, so it is paid **only by games that declare rewards** — and the
real patch declares none, so that patch still gets an integral objective. The
conversion tie-break already had exactly this shape; this follows it.

#### Shops: the refusal survived and changed meaning

A `Shop` variable is now about a dozen lines — the cap has somewhere to live. It
was **not** written, per the tenth session's recorded scope decision. What
changed is the message: it no longer says "not modelled until the plan has a time
axis" but names the actual gap, which is that the upstream gives no price, no
currency and no reset period. **The refusal is now a data refusal rather than a
modelling one**, and that is worth a sentence to whoever picks up Q2.

#### Numbers

189 tests → **200**, 0 failures, 0 skipped locally with the snapshots present.
One bug found by the tests rather than by reading: the rotation subset rows
indexed the group map by ordinal instead of by day-set, which NPE'd every solve.

#### Addendum, same session — the maintainer answered, and two of the answers were corrections

Everything above was written believing rewards and rotation were data the
upstream *fails* to publish. The maintainer was asked what the real game does and
supplied an answer with community references. Two corrections, one confirmation
and one new question came out of it. The full record, with its provenance
separated into evidence classes, is in
[docs/game-facts/reverse-1999-economy.md](../game-facts/reverse-1999-economy.md).

**Correction 1 — there is no weekday rotation, so there is nothing to publish.**
The four Afflatus Insight families (Brutes Wilds, Mountain Echoes, Starfall
Locale, Sylvanus Shape) and the two Resource stages (The Poussiere, Mintage
Aesthetics) are **permanent and open every day**. They are material
specialisations, not a rotation. So `KornblumeAdapter` emitting
`Availability.ALWAYS` is **correct rather than a gap**, and the sentence in the
entry above — "the upstream publishes no weekday field" — is true but was framed
as an omission when it is an accurate reflection of the game. Corrected in the
adapter's javadoc so the next reader does not try to close a gap that is not
there. The rotation machinery now waits on Phase 11 rather than on a second data
source. `EXACT_ROTATION_GROUPS = 6` will not bind on this game; the worry about
needing seven distinct restrictions was misplaced.

**Correction 2 — the "free income" is not free, and modelling it would have been
a silent bug.** Daily and Weekly Activeness hand out Clear Drops, Wilderness
Shells, Picrasma Candy and more, and it is tempting to enter them as
`Reward(DAILY, …)`. **Some of the objectives that earn Activeness require
spending Cellular Activity.** So the income is conditional on the farming, and a
solver told it is unconditional would subtract the grants, never pay for them,
and return plans that are systematically too cheap — silently, and in the
expensive direction. `Reward` cannot express a precondition, so **leaving
Activeness out is correct**, and that is now written down as a reason rather than
an accident. The Roaring Month is paid and is not income either. Event and mail
grants *are* unconditional and are exactly what `Cadence.EVENT` is for.

**Confirmation — 240 Activity a day.** Regeneration is 1 per 6 minutes, so 240 a
day and 1 680 a week. `RealUpstreamPlanTest` and `CommunityBenchmarkTest` have
been passing 240 all along; that number now has a source instead of being a
plausible round figure. Picrasma Candy (60) and the Jar (120) are **items**, and
must never be folded into the rate — which the model could not do anyway, because
energy is not an item.

**Three shapes the model cannot express**, all surfaced by this exchange and none
of them a defect in what shipped: an item that restores energy; a reward
conditional on spending energy; a lifetime ("five, ever") purchase limit, which
`Shop`'s `(periodLimit, Period)` cannot say. Each would be a silent wrong answer
if faked. In the tracker's unverified list.

**Shops moved again.** The tenth session concluded shop data was unusable; that
was right about `shops.json` and wrong as a claim about the game. Six permanent
shop structures exist and several publish prices and limits. The refusal now
stands on a third reason: **the cap has the wrong shape**, and the prices want
verifying against the client rather than inventing.

**New: Q5, and it is the shape of the mistake that cost five sessions.** The
pinned snapshot `8b40541a9c42` has commit message `update 3.5` and date
**2026-03-17** — confirmed through the GitHub API rather than assumed. Global 3.5
ran **2026-05-28 to 2026-07-02**. Two and a half months apart, most likely
because Kornblume tracks CN. If so, everything this repo calls "3.5" is CN 3.5.
It does not invalidate the nine agreements — the guide was already known to be
written for 2.7 against a 3.3 sample — but the version labels may not mean what a
reader assumes, and that is worth resolving before anything is published.

**On the sources.** The references behind all of this are community wikis and
forum documentation reached through an AI search tool, so the citation trail is
one hop longer than it looks. Same standing as the pity rates under **Q4**, and
the same obligation: check against in-game disclosure before shipping anything a
player would act on. Nothing from it entered a bundle, a test or the solver this
session — it is recorded, not adopted.

### 2026-09-08 (tenth session) — the cache that says what it is, and the shop data that is not there

**Two items: a prerequisite read that changed N14, and N15.**

#### The read that came first, and what it cost N14

N14's tracker entry carried an instruction: the upstream publishes `shops.json`,
the fetch script does not fetch it, **read it before designing the time axis
rather than after.** So that came first, and it was the right order.

Fetched at both pinned commits — `d49efab2a18f` (3.3) and `8b40541a9c42` (3.5).
**Identical, byte for byte:** 6 397 bytes, same MD5. So the file is static across
the range this project reads, which is itself information: it is not resampled
and not patch-tracked.

What is in it, in full:

- **Six opaque keys** — `jb1`, `jb2`, `1.21`, `1.22`, `1.31`, `1.32`.
- **69 rows**, each `{Material, Quantity}`.
- **Two field names in the entire file.** No currency, no unit price, no reset
  period, no stock limit, and **no marker distinguishing an offer from its
  cost** — `jb1` lists `Dust 75000` beside `Brief Cacophony 5` with nothing to
  say which is being bought and which is being paid.

Our `Shop` record needs currency, price, offer, `periodLimit`, `period` and
availability. Five of those six are simply absent upstream.

**What this does to N14.** Its entry claimed a time axis "unblocks the three
source kinds currently refused by name". That is true for rewards and rotation
and **false for shops**: a time axis gives a per-period cap somewhere honest to
live and leaves nothing to put in it. `KornblumeAdapter`'s refusal is not a
deferral to be cleared by modelling work — it is correct, and permanent for this
upstream. N14 has been rescoped in the tracker accordingly, and shops now hang
off **Q2** (a second data source) rather than off N14.

**Worth keeping:** the instruction to read first was written by a previous
session that suspected the data was thin. It was thinner than suspected, and the
cost of finding out afterwards would have been a day-indexed MIP built partly
around a source kind that cannot be populated.

#### N14 or N15 first — and why the order changed

With shops gone from N14, the session put the fork to the user rather than
guessing, because a second fact had turned up: **p95 is 1 808 ms against a
2 000 ms assertion**, 90% of the budget, with no time axis at all. Day-indexing
the stage variables multiplies the model by the horizon. N14 is the change most
likely to break the number Phase 2 was closed on, and N15's cache is the thing
that absorbs it. N15 went first, by decision.

#### N15 — a solve is cached on its key, and a queue runs it once

`SolveKey` had existed since the seventh session with nothing using it.

**`SolveCache`** is the port, and it is two methods. **There is no invalidation
method, and that is the design**: a key carries the game-data version, so a patch
does not stale an entry — it makes a different key under which nothing is stored.
The old entry is not refreshed, it is unreachable. Eviction is therefore a
memory-pressure concern and never a correctness one.

**`InProcessSolveCache`** is a bounded LRU (400 entries, a number labelled as the
guess it is) with hit and miss counters, so whether it earns its keep stays a
measurement.

**The number:** on the real 3.5 patch — 99 stages, 2 000-odd upgrades — a repeat
question goes **1 806 ms → 2 ms**. Measured in `RealUpstreamPlanTest` beside the
p95, deliberately: on the three-stage fixture a hit and a solve are both under a
millisecond and the test would prove nothing.

**Three judgements worth keeping.**

1. **Not Redis, which is what ADR 0003's table says.** Redis buys a cache shared
   between nodes; there is one node and under D1 not even one deployed. What it
   costs is a serialisation contract for `Plan` — written by one deploy, read by
   the next, failing quietly when a field moves — for a benefit currently equal
   to zero. [ADR 0012](../adr/0012-the-solve-cache-is-in-process-until-there-is-a-second-node.md)
   records it and the reversal trigger is **the second node, not a date**. It is
   also owed before any benchmark against the Phase 8 replicated KV, because a
   hand-built replicated cache measured against an in-process map is measuring
   the network — which is exactly the flattery ADR 0003 forbids.
2. **A cache hit re-stamps the profile and leaves `computedAt` alone.**
   `SolveKey` fingerprints a player's *state*, not their identity, so two
   profiles holding the same items with the same roster and goals share one
   solve — but the envelope has to name whoever is reading it. `computedAt` is
   not touched, because a plan computed half an hour ago did not become a plan
   computed now by being read, and re-stamping it would be a lie told by the one
   field that exists to prevent exactly that. The hit also adds a note saying so,
   and the note goes on the *copy* handed over, never on the stored plan — a
   cached plan whose explanation grew every time somebody read it was the obvious
   version of this bug, and there is a test for it.
3. **The coordinator does not survive a restart, on purpose.** The queue is heap;
   a kill loses every in-flight solve. That is written into the class as the
   honest bound of a single-node coordinator rather than patched, because making
   it durable here would answer the question Phase 9 exists to ask. Its
   exactly-once promise is real and narrow: sixteen threads racing one idempotency
   key produce one execution and a queue depth of one, and there is a latched test
   that holds the solve open to prove it.

**A trap found and defused.** `RealUpstream.optimizer` is what the p95 test uses,
and that test asks the *same question* fifty-five times. Hand it a cache and
fifty-four of those become hash-map lookups, the p95 drops to nothing, and Phase
2's exit criterion silently starts measuring a `LinkedHashMap`. The factory is
now explicitly cache-free with that written above it, and a separate
`cachingOptimizer` exists for tests that want one.

**Also done:** ADR 0011 was missing from `docs/adr/README.md` — a gap left by the
ninth session — and is now indexed alongside 0012.

**Numbers.** 189 tests, 171 before: planner 35 → 52, `:app` 73 → 74. Zero skipped
locally because the snapshots were present, so the 16 gated tests genuinely ran.

**CI confirmed it**, run `34182911475` on `3fdc277` ([PR #10](https://github.com/kietnt4412/storm_almanac/pull/10)),
`BUILD SUCCESSFUL in 1m 6s`: **0 failed, 16 skipped**, and the skips are exactly
the three snapshot-gated classes and nothing else — `RealUpstreamPlanTest` 8 (7
before, the new cache test is the eighth), `CommunityBenchmarkTest` 5,
`RealUpstreamPatchTest` 3. Nothing touching upstream data passed on the runner,
which is the state ADR 0009 requires; a pass there would have meant a snapshot
was committed by accident. The two Node-20 deprecation warnings and the
`setup-java@v4` one are still there — **N5**. p95 unchanged at 1 808 ms, which is the
point — the cache is not in that path.

### 2026-09-08 (ninth session) — the pipeline confirms the tree, and a number the model was throwing away

**Two items: N16 and N17.** Taking them in order.

#### N16 — the pipeline confirms the tree

The eighth session left Phase 2's box unticked with a single
condition attached — CI green on this tree, read from the log rather than
assumed. That condition is now met and the box is ticked.

**The tracker and the repository disagreed about the starting state, and the
repository was right.** N16 said "nothing is committed or pushed". In fact `dev`
was at `93f4de4` on the remote and had been merged to `main` as PR #8
(`d34a31b`): the eighth session did the push *after* writing the entry that
described it as outstanding. Nothing was lost, but the file spent a day telling
the next session a smaller lie than it exists to prevent. **Tick and write as the
work lands.**

**What CI actually says**, both runs success:

| Run | Trigger | Head | Time |
|-----|---------|------|------|
| `34116854550` | `dev` pull request | `93f4de4` | 1m 24s |
| `34116880372` | push to `main` (PR #8) | `d34a31b` | 1m 46s |

Counted out of the downloaded log rather than from the green check: **139
PASSED, 15 SKIPPED, 0 FAILED**, `BUILD SUCCESSFUL in 1m 5s`. The two checks N16
named:

- `:adapters:reverse-1999:test` ran and **all 24 of its tests passed on the
  runner** — the adapter rewritten last session for the sampled stage tables is
  exercised by the pipeline, not only by this machine.
- The 15 skips are **exactly** the three snapshot-gated classes:
  `CommunityBenchmarkTest` 5, `RealUpstreamPatchTest` 3, `RealUpstreamPlanTest`
  7. None of them passed, which is the state
  [ADR 0009](docs/adr/0009-upstream-data-is-fetched-never-vendored.md) requires:
  a pass there would mean upstream data had been committed by accident.

The local half was not re-run. It was green on this tree last session (154
tests), the working tree is clean at the same commit, and re-running it would
have measured the machine rather than the code.

**Phase 2 is closed on its criterion. It is not finished, and the distinction is
the point.** The criterion is two measurements — p95 under 2 s, agreement with
community answers on five goal sets — and both are made. The phase's *scope*
still contains a cache that does not exist (N15), a model with no time in it
(N14), and — when this was written, an hour before N17 closed — drop rates with
no sample size. The box records the criterion, the caveats beneath it record the
rest, and **Phase 3 is deliberately not opened**.

**The standing caveat got no weaker.** Both halves of the criterion this tick
rests on were measured by tests that skipped on both of those green runs. A
green pipeline confirms the tree compiles and the game-agnostic parts hold; it
has never once confirmed that this project's numbers are right.

#### N17 — the sample size reaches the solver

**The finding it acts on, restated once:** the upstream publishes how many runs
each stage's drop counts were observed over — 105 to 41 212 — the adapter divided
by it and discarded it, and every disagreement over 25% between this project's
stage ranking and a published guide was a hundred-run mean outranking a
well-sampled one. `argmin` cannot tell a good stage from a lucky one.

**What was built**, in the order the old N17 entry laid out and it was the right
order:

1. **`Drop` carries `sampledRuns`**, and it travels: canonical bundle (optional,
   and absent means *declared*), `V4` on `stage_drop` (default 0, a non-negative
   check, and a comment saying why 0 cannot mean "measured over nothing"), both
   JDBC directions, the patch diff (one fact, `0.21 over 105 runs`, so a resample
   does not read as two unrelated changes).
2. **`PoissonRateInterval` in `stats`** — the counting analogue of Wilson's score
   interval, `(C + z²/2 ± z·sqrt(C + z²/4)) / n`. Unbounded above, non-negative,
   and `[0, z²/n]` when nothing has been seen.
3. **`YieldTable` gives the solver the lower bound** wherever a sample exists and
   the declared value where none does. The plan says so in its notes.

[**ADR 0011**](docs/adr/0011-a-yield-is-a-mean-per-run-with-a-sample-behind-it.md)
records the decision and **narrows ADR 0006 rather than superseding it**: Wilson
is still right for *"did it drop"*; *"how many dropped"* is a different question.
**Q8 is closed.**

**What it changed, measured on the real snapshot:**

| | before | after |
|---|---|---|
| Exact agreements with the guide | 5 | **9** |
| Disagreements over 25% | 4 | **3** |
| Plan cost, five characters to Insight 2 | 3 624 | **3 880** (guide: 4 017) |
| p95 solve / optimality gap | 1 804 ms / 2.80% | 1 805 ms / **2.37%** |

Bifurcated Skeleton, Clawed Pendulum, Goose Neck and Red Lacquer Tablet joined
the five, each because this project had been preferring a thin sample with a
flattering mean. **The plan getting 7% dearer is the same fact from the other
side** — the old number was optimistic, not cheap — and the margin over the
guide's own advice narrowed from 393 Activity to 137.

**Two of the four large disagreements survived, and that is the honest result.**
A 95% bound discounts a thin sample in proportion; it does not dismiss it, so a
105-run mean four times higher still wins. Milled Magnesia (145%) and Liquefied
Terror (210%) belong to the evidence, not to the model, and the fix for them is
more sampling — which is now what Q2/F1 would buy.

**Three things this session learned that are worth more than the feature:**

1. **The benchmark was not measuring what production does.** It ranked stages on
   `Drop.expectedYield()` directly, so N17 could have landed in full and the
   benchmark would have gone on printing the old table forever, agreeing with
   itself. It now ranks on `YieldTable`'s own coefficients and prints the raw
   ranking beside them. **A benchmark that does not consume what the product
   consumes measures the wrong thing, quietly** — the same failure shape as the
   stale stage file, one level up.
2. **`count: 1` is a declaration, not a sample.** All 14 such stages in both
   pinned snapshots are the Insight and Resource ones paying a flat 9 000
   Sharpodonty or 2 Pages; every sampled stage carries at least 105 runs. Reading
   the 1 as a sample would have put a 95% bound on a number nobody measured and
   made the only source of several currencies look unfarmable. Adapters are where
   an upstream's conventions get translated, and this is one.
3. **A "green build" needed a build fix before it could be green at all.** Docker
   Engine 29 refuses the API version Testcontainers 1.21.3 speaks, so 33 tests
   failed across six classes with three unrelated-looking symptoms while
   `docker ps` worked fine. Fixed by `systemProperty("api.version", "1.44")` on
   the test tasks; upgrading Testcontainers is a migration, not a bump, because
   2.x renamed the artifacts. **Environment note E2**, and **E1 came back** while
   working on it — the Windows-ROOT truststore flag is needed again.

**The pipeline confirmed N17 too, in the same session this time.** Run
`34176134653` on `30a6c45` (PR #9): success, **156 passed, 15 skipped, 0
failed**, all 25 adapter tests on the runner, the same three classes skipping.
The `api.version` property changed nothing there — the runner's engine was never
the one refusing. **PR #9 is deliberately left unmerged**: `main` is a session
behind, and merging it is a decision rather than a step.

### 2026-09-07 (eighth session) — the community's answers, and the two thirds of the game we were missing

**N12 closed, and Phase 2's second exit criterion with it.** The session's one
task was to find out what the community says the best stage is and compare. It
took about ten minutes of reading to discover that the comparison could not be
made at all, because the stages players are told to farm were not in our data.

**What was wrong.** `KornblumeAdapter` read `stages.json`. The upstream's own
planner reads the newest `stages<major>_<minor>_greedy.json` — `getDrops()` in
`src/composables/planner.ts` returns `dataStore.stages3_3_greedy`, and
`normalizeDrops` divides each drop count by the stage's `count`. `stages.json` is
the older shape: a bare proportion, no sample size, and **chapters 1 to 4 only**.
99 stages against the live table's 105, overlapping by 40, with a drop-to-stage
mapping the game has since retuned — 1-14H drops Liquefied Terror in the old file
and Sharp Needle in the new one.

**Five sessions of work sat on top of that and nothing went red.** The bundle was
well-formed. The parser accepted it. The schema round-tripped it by record
equality. The optimizer solved it in 1.8 s and explained itself. The CI pipeline
was green twice. Every one of those was true of a third of the game. **The only
thing that caught it was a person going to look at what somebody else said the
answer was** — which is precisely the argument the plan makes for having a
benchmark at all, arriving before the benchmark was written.

**A "fact about the game" is withdrawn.** Last session recorded *45 of 118
characters' Insight 2 cannot be planned from patch 3.5, because their materials
appear only against the synthetic `Unreleased` stage.* That was a fact about the
stale file. Against the table the upstream actually uses, **all 118 can be
planned**, and none of them needs `Unreleased`. `RealUpstreamPlanTest` now
asserts the reverse as a canary: a released character with no source for a
material means the stage table has gone short again.

**The fix, in three places.**
- The adapter takes the highest-versioned sampled table it finds, compares
  versions numerically (`stages10_0_greedy` must beat `stages3_3_greedy`, which
  no listing order gives you), divides counts by the sampled run count, falls
  back to `stages.json`, and **says which file it read either way**. Silence is
  what let this run for five sessions.
- `tools/fetch-upstream.sh` pins the sampled table per snapshot: 3.3 →
  `stages3_0_greedy.json`, 3.5 → `stages3_3_greedy.json`. The version in the
  filename lags the patch label because the drop data is resampled on its own
  schedule, so it cannot be derived from the label and is written down instead.
- Four new adapter tests: preference over `stages.json`, numeric version
  ordering, the fallback note, and a `count` of zero — which is a denominator,
  and an infinite yield is a free source, the same defect this adapter has now
  met three ways.

**Then the benchmark, which is what N12 was actually for.** Twenty claims from
Prydwen's *Insight Materials Cheat Sheet (2.7 patch)*, transcribed with their
source into `docs/benchmarks/reverse-1999-community-answers.md` and compared by
`CommunityBenchmarkTest`:

- **Five exact agreements** — Holy Silver (10-9H), Salted Mandrake (3-13H),
  Perpetual Cog (7-16H), Pyroxene Ore (9-1H), Alopecurus Pratensis (8-18H) — the
  exit criterion's five, on its own terms. Seven more agree within 8%.
- **Seventeen of the guide's quoted drop rates land within three percentage
  points** of a sample this project had never seen. Two independent measurements
  of the same game agreeing is the best evidence so far that the pipeline is
  carrying real numbers and not plausible ones.
- **The plan beats the advice**: 3 624 Activity against 4 017 for following the
  guide material by material, and the 4 017 does not cover the whole demand.
- **Four of the guide's items are ours under different English names**, and the
  rates identified them rather than the names: Red Lacquer Slab is Red Lacquer
  Tablet, Golden Grass Incense is Golden Herb Incense, Luminite Ore is Pyroxene
  Ore, and Fox Tail is Alopecurus Pratensis — literally foxtail grass.
- **One claim is deliberately not tested**: the guide's Magnesia Crystal pick,
  2-9H, is not in the upstream's sampled table at all. Written down rather than
  dropped.

**And the benchmark's own finding, which is the thing to do next.** Every
disagreement over 25% is a sample-size disagreement. The upstream publishes how
many runs each stage's drop counts came from — **105 to 41 212** — the adapter
divides by it and throws it away, and the model then treats a hundred-run mean
exactly like a forty-thousand-run one. Milled Magnesia prefers 4-5H (105 runs) to
the guide's 5-8H by 154%; Liquefied Terror prefers 4-4H (113 runs) to 9-3H by
224%. A noisy mean is a high mean about as often as a low one, and `argmin` picks
whichever stage got lucky. **Q8 has stopped being a boundary formality for
Phase 6 and is now N17, ahead of the time axis.**

**And a third defect, which only the full suite could find.** Docker would not
start for most of the session, so the Testcontainers tests did not run at first.
When it finally came up, `RealUpstreamPatchTest` failed — the schema round trip,
on the same stages it had been round-tripping happily for two sessions. **The
drops were all there and in a different order.** `Stage.drops()` is a `List`, so
record equality is order-sensitive; the adapter emitted drops in the upstream's
JSON key order and `JdbcGameDefinitionRepository` reads them back ordered by
`stage_drop.item_id`, a surrogate handed out in catalogue-ingest order. Those two
coincided under `stages.json` and stop coinciding under the sampled tables.
**"The schema gives somebody else's numbers back unchanged" was true by luck**,
and the luck ran out the moment the input file changed. Both ends now sort by
item id, and an adapter test pins it.
That is worth remembering as a pattern rather than a bug: **this session's three
defects were all invariants that had never actually been tested, only
coincidentally satisfied.**

**Where the build ended up:** green. 154 tests locally, 139 on CI once the 15
snapshot-gated ones skip. Nothing is committed or pushed, so **Phase 2's box
stays unticked** even though both halves of its criterion are met. That is N16,
and it is now only the pipeline half.

**Housekeeping.** `RealUpstreamPlanTest`'s fixture — the snapshot load and the
two fake repositories — moved into a shared `RealUpstream` class, because the
benchmark needed the same one and two copies of a fake drift.

**Phase 2 opened.** The optimizer went from four interfaces with nothing behind
them to a working mixed-integer program that answers a real goal set over a real
patch. 93 tests to 144. The phase's box stays unticked because half its exit
criterion is untouched — see N12 — and that half is the one that decides whether
the answers are *right* rather than merely fast.

**What was built, and the shape of it.** Three pieces that do one thing each,
because the alternative is a solver that cannot be tested without a database:

- `DemandResolver` turns goals into an item vector by walking the upgrade DAG.
  The choice worth recording is that it walks **backwards** from the target.
  A forward walk needs a starting state and a player who does not own the entity
  has none; walking back until it meets either something already achieved or a
  track with nothing before it answers both cases in one traversal, and handles
  a multi-track entity without ever enumerating tracks.
- `EnergyMip` is the model and nothing else — inputs in, an answer or a refusal
  out. **Crafting recursion turned out to be an absence rather than a feature:**
  every craft is a variable and every intermediate material is a constraint row,
  so the solver walks a tier-3-from-tier-2-from-farmed chain because the algebra
  makes it. A recursive expansion would have had to pick a depth and choose
  between routes the solver can simply price.
- `MipOptimizer` does the fetching and the narrating.

**Two defects, both found by real data on first contact, both the argument for
`RealUpstreamPlanTest` existing.**

1. **The optimizer crafted 294 250 Sharpodonty out of nothing.** Kornblume lists
   base materials in the same file as its recipes, as rows with an empty
   `Material` array. Converted faithfully those become crafts that consume
   nothing — free unbounded supply at zero energy — and the solver did exactly
   what it should with the model it was handed. The galling part: the adapter
   already refused zero-cost *stages* with the comment *"a free source is an
   unbounded one"*, and nobody had thought to apply the same sentence to
   conversions. Fixed at three levels on purpose: `Craft`'s constructor (so every
   future upstream inherits the rule), `CanonicalBundleParser` (so a human
   approving a bundle gets a located message naming the entry), and
   `KornblumeAdapter` (so the twelve rows are skipped and announced).
2. **ojAlgo blew the JVM stack.** Not slowly — a `StackOverflowError` out of its
   own worker pool, which arrives as a wrapped `RuntimeException` rather than a
   solver state. Branch-and-bound recurses per node and dives depth-first while
   it has no incumbent, and **an unbounded integer variable is an unbounded
   dive**. Three fixes, in descending order of what they bought:
   - **Bound every variable.** Nobody runs a stage more times than would supply
     the whole goal set on its own. The bound is the demand expanded through the
     craft graph, so it excludes no optimal solution. This is the one that
     mattered.
   - **Keep the objective integral.** A millionth-weight tie-break had been added
     to stop a lossless craft cycle spinning; it cost the objective the
     integrality that lets branch-and-bound discard a node whose bound is within
     one of the incumbent. It is now applied only when the craft graph actually
     has a cycle — which real recipe trees never do, because a thing is made from
     cheaper things. **A tuning constant added for a case that does not occur, at
     the cost of a property that does.**
   - **Loosen the gap from seven significant digits to four.** Proving no
     arrangement saves a hundredth of an Activity point is most of the runtime.

**And a fact about the game rather than about the code:** 45 of 118 characters'
Insight 2 cannot be planned from patch 3.5 at all. Their materials appear only
against Kornblume's synthetic `Unreleased` stage, which the adapter refuses for
costing nothing. The optimizer names them — *"nothing available can produce
winged-key (no stage drops it and the recipe(s) that make it — craft-winged-key —
cannot themselves be supplied)"*. That is the correct answer and it only exists
because free sources are refused.

**What the solver actually promises now, written down as
[ADR 0010](docs/adr/0010-a-plan-is-the-best-provable-in-the-budget.md).** The
search is stopped by its budget every time on a real goal set; it does not
finish. Rather than leave "we ran out of time" as the whole story, the plan
carries the size of the doubt, measured against the linear relaxation with one
extra solve over a model already built: **"no plan can be more than 2.95%
cheaper."** That is also the number ADR 0004's reversal trigger should be read
against — a wall clock inside budget because a timer stopped the search says very
little on its own. Shadow prices are reported only when both ends of the
comparison were proven optimal, because the difference between two time-limited
answers is noise with a number on it.

**Measured, five characters to Insight 2, Reverse: 1999 3.5:** 2 411 Activity
over twenty stages and six crafts, ten days at 240 Activity/day, p95 **1.8 s**
over 50 solves, within **2.95%** of the relaxation's bound. Ninety-three stage
variables, eleven crafts, twenty-three constraints.

**Verified twice, as usual.** Every expected number in `EnergyMipTest` is worked
out by hand in its own comment — *"6 ingots need 18 ore and 300 gold; 18 / 2.0 =
9 runs at 10 = 90"* — so a disagreement is the solver's fault and not the
fixture's. Then `PlannerAcceptanceTest` runs the same solver over the acceptance
bundle parsed off disk, where the hand-computed answers (390 Activity for
Insight 1; 350 for the amulet, taking the craft route the solver was never told
about) all matched on the first run, shadow price included. Then
`RealUpstreamPlanTest` over a real patch, which is where both defects were.

**Environment note:** Docker Desktop was not running at the start of the session,
which shows up as `initializationError` on every Testcontainers class and looks
alarming out of context. It is not a defect in the tree — start Docker and rerun.

**CI confirmed the tree the same day (N13).** Runs `34072743411` and
`34072752969`, both success, PR #7 merged to `main`. The two things worth
checking were checked in the log: all 30 planner tests executed on the runner —
a new test source set that is silently not picked up looks exactly like a green
build — and the ten real-data tests showed as SKIPPED rather than passed or
failed. **Reading the log also caught a lie in this file:** the entry predicted
six skips and there are seven, because the local suite is 144 tests and not the
143 written down three times without being recounted. Corrected. The test count
is exactly the kind of number that gets carried forward instead of re-derived,
which is what this file exists to prevent.

**Not done, and deliberately:** no HTTP route for a solve, no cache behind
`SolveKey`, no `SolveCoordinator` implementation, no time axis and therefore no
shops, rewards or weekday rotation, and no fodder. Each is a next action rather
than an oversight.

### 2026-09-06 (sixth session) — the pipeline meets somebody else's data

**Did:** N9 (confirmed green) and N8 (decided, then built). A parser adapter for
Reverse: 1999, a canonical bundle writer, `--gamedata=adapt`, and two real
patches through the whole pipeline. 75 tests → 93.

**N9 first, and it was clean.** Runs `34002586027` and `34002589214`, both
success on `e2239b3`. The thing the last session flagged — three test classes
newly starting Tomcat — cost nothing: the PR job went 1m43s → 1m26s. Nothing to
absorb, nothing to split.

**N8 was a decision, and it was not mine to make.** The tracker had framed it as
*ask F2 and wait* versus *close the phase by exception on the synthetic
fixture*. The project owner picked a third option that neither of those covered:
build the adapter now, read the data, redistribute none of it, and ask about
licensing before publishing anything. That is [ADR
0009](docs/adr/0009-upstream-data-is-fetched-never-vendored.md), and it is the
right call for a project whose current audience is a CV.

**The consequence is uncomfortable and is written everywhere it belongs.**
Because the snapshots cannot be committed, `RealUpstreamPatchTest` skips on CI.
**The strongest test in this repository is the one the pipeline does not run.**
It is said in the test's javadoc, the README, the ADR, the tracker header and
the *what is still unverified* list — five places, because a green tick that
quietly omits the best evidence is worse than a red one, and the temptation to
forget this will grow with every green run.

**It found two defects on first contact, which is the entire argument for having
done it.** Neither was findable by any amount of thinking about the synthetic
fixture:

1. **A character named Зима.** `Names.slug` normalised to NFD, stripped
   combining marks and kept `[a-z0-9]` — which for Cyrillic keeps nothing, so
   the slug was empty and the whole ingest was refused. Fixed by making the rule
   Unicode-aware: letters and digits survive whatever script they are in, while
   an accent is still stripped so `Café Crème` reads `cafe-creme`. Worth noting
   *why* it survived design: ids are `TEXT` in the schema and `Identifier` asks
   only that they not be blank, so nothing downstream had an opinion. The ASCII
   assumption lived in one method and nothing else could contradict it.
2. **Placeholder rows.** The upstream ships unreleased psychubes as rows whose
   every field but `Id` and `Rarity` is `null`, and the adapter read `Name`
   before checking the release flag — so a conversion was refused by rows it was
   about to throw away. The rule that falls out of it: *validate what you keep,
   not what you drop.*

**What the adapter refuses to do is the part I would defend hardest.** It
converts less than the upstream publishes and prints what it dropped on every
run — `skipped 590 resonance-pattern cost row(s)`, `skipped 4 unreleased
character(s)`, `skipped 1 stage(s) costing no Activity`. Two of those are
judgement calls worth carrying forward. `shops.json` has a material and a
quantity but no currency, no unit price and no reset period, and our `Shop`
needs all three; inventing them would put fake prices into the optimizer's
source set, and **a wrong number gets used while a missing one gets noticed**.
Frequency rows are four alternative patterns for one resonance step, and an
`Upgrade` is a required cost of one transition — mapping all four would charge a
player for every pattern at once. The model has no "or" and the adapter will not
fake one.

**Structure, because the claim needed a receipt.** The adapter is its own Gradle
module, `:adapters:reverse-1999`, and `ModuleBoundaryTest` gained an `adapters`
layer that only `:app` may reach. So "a new game is a bundle plus a parser
adapter, and nothing else" is now a build failure rather than a promise, and the
cost of a title is visible as a directory. `UpstreamAdapters` in `:app` is the
only place in the product that names one.

**`CanonicalBundleWriter` was not on the plan and earns its place.** An adapter
could have handed a `GameDataBundle` straight to the repository, and that would
have skipped the step this phase is built around: a human reading the thing
before approving it. Writing the file keeps the loop at *adapt, preview, ingest,
publish* and gives an adapter no privileges for being code. It is pinned to the
parser by a round trip over both fixtures plus an idempotence case — a writer
whose output drifted would fill every patch diff with noise that means nothing.

**Verified twice, and the hand check is the half that convinces.** From the
packaged jar against `docker compose` Postgres: adapt 3.3, ingest, publish;
adapt 3.5, ingest (which printed the patch report), publish; diff; then the API
curled. `GET /entities/regulus/upgrades` says Insight 2 costs 40 000 Sharpodonty
and 10 Scroll of Starlit Ascent, in named items.
`GET /entities/igor?version=0` is a 404 reading *"no entity 'igor' in
reverse-1999 3.3"* — Igor was released in 3.5, so version pinning did the exact
thing it was built for, against data nobody here wrote.

**One near-miss, caught at `git add` and worth the paragraph.** The first
`.gitignore` rule for the snapshots was `**/upstream/`, which is exactly the
pattern a reasonable person writes — and it silently stopped tracking the
adapter's *own* test fixtures under `src/test/resources/upstream/`. Locally
everything passed, because the files were on disk. On the runner it would have
been a compile failure in a module that had never built there, and the obvious
suspect would have been the new Gradle module rather than a line in
`.gitignore`. The fetch target is now `build/upstream-snapshots` and the ignore
rule names it specifically. **A broad ignore rule fails by omission, and an
omission is invisible in `git status`** — the only reason this surfaced is that
the staged file list was read rather than skimmed.

**Then pushed, merged as PR #6, and N10 closed in the same session.** Runs
`34035918992` and `34035923889`, both green; the adapter module built on the
runner and `RealUpstreamPatchTest` skipped, which is the designed state rather
than a disappointment. **Phase 1 is closed and its box is ticked.**

**Left for next session:** N11 — open Phase 2. One thing found in passing that
Phase 2 must not trip over:
`Stage.potentialOutput()` rounds an expected yield *up* to a whole item, which
is right for a catalogue listing and wrong for the MIP constraint, which needs
the yield itself.

### 2026-09-06 (fifth session) — the API answers, and the numbers say where they came from

**Did:** N7. The gamedata API — five routes, a read model, ten HTTP tests, and a
hand check through `docker compose up`. 65 tests → 75, all green.

**What the work actually was.** The data was already in the schema and the two
exit questions were already answerable; `GameDataIngestTest` had been asking
them of the repository since the fourth session. What was missing was every
inch between a repository call and an HTTP response, and this project has been
bitten there before — the 401 on `/api/health` was invisible to a test that
called the controller method directly. So the endpoints came with tests that go
over real HTTP through the real security chain, and then the whole operator loop
was run by hand anyway: compose up, `gamedata-cli ingest` and `publish` both
fixture versions against the compose Postgres from the packaged jar, curl every
route. It all worked first time, which is a less interesting session log than
the last two and is what the previous two sessions' work bought.

**The decision that was not on the list, and why it was made anyway.**
`GameDataVersion` gained an `attribution` field. "Numbers and text only,
attributed" is a project invariant, and it had been an invariant about
*ingestion* for as long as nothing served the numbers — the string was on the
schema row and on `DraftVersion`, and the read path could not see it. Serving a
catalog page to a stranger with no way to say where its numbers came from would
have been shipping a violation of the invariant on the first day the invariant
could actually be violated. Cost: one field on a `common` record, one column in
three queries. It also strengthens `roundTripsWithoutLoss`, which now proves
provenance survives the round trip as well.

**And the wart it exposed.** `GameDefinitionRepository.find` took a
`GameDataVersion`, so every caller built one with a dummy label and
`Instant.EPOCH` just to name a sequence — three sites, each with an apologetic
comment. Adding a fifth field would have made the hack uglier in a third place,
which is what made it worth fixing rather than tolerating: `find(GameId, long)`
now, and the three apologies are gone. A port that makes its callers fabricate
data to ask a question is shaped for its implementation.

**Read model, not the domain, on the wire.** The domain records would almost
serialise. Almost: every typed id would go out as `{"value":"gold"}`, every cost
would be an item slug and a number that each client joins by hand, and every
future domain change would be a breaking API change nobody noticed making. The
first two are annoyances; the third is the one that costs a year. `GameDataView`
is the wire format and `GameDataReadModel` is the only thing that maps to it.

**Cost knowingly deferred, written into the code rather than into a hope.**
Every request loads a whole version — fifteen queries — to answer a question
about one entity. Fine now, not fine at phase 4. The fix is a cache keyed on
`(game, sequence)`, correct by construction because a published version is
immutable. Not written, because a cache added before there is a number to
compare against is a guess; the javadoc on `GameDataReadModel` says so, so the
next reader does not mistake it for an oversight.

**Test-harness change to know about:** `GameDataDatabaseTest` is now
`RANDOM_PORT`. Three classes that never started a servlet container now do. The
alternative was a second Spring context differing only in that flag, which means
a second Postgres and a second boot on every build forever. Suite went 22s → 31s
locally. **On the runner it is unproven — N9.**

**Not done, deliberately:** N8 (a real upstream — still gated on F2, which needs
a human to send a message), N4 (the `Entity.kind` rule — still vacuous until
planner has code), N5 (the CI action bumps — still wants its own quiet PR).

### 2026-09-05 (fourth session) — the schema holds data, and could not be deleted

**N6, all four parts.** Parser, persistence, diff, CLI. 26 tests to 65, all
green locally, `./gradlew build` clean. Nothing pushed yet.

**The decision that shaped the session: JDBC, not JPA
([ADR 0008](docs/adr/0008-gamedata-persistence-is-jdbc.md)).** N6 said "the JPA
entities" and left two questions open before writing them — how `TEXT[]` maps
and how the composite foreign keys map. Both settle at the same answer, and it
is not the one the entry assumed. Writing the mappings out showed four
mismatches at once: twenty composite `@JoinColumns` with `insertable=false`
carrying an invariant the schema states in one line; a `UserType` for a column
that JDBC reads with `getArray`; a write path that is a bulk insert of an
immutable snapshot, so dirty checking and the first-level cache cost without
buying; and a read path of fifteen collections under one root, which is
`MultipleBagFetchException` by construction. Underneath all four, the domain is
already records — JPA entities would have been a second parallel model, so the
mapping work an ORM exists to save is work this module does either way.
Consequences: `available_days` stays `TEXT[]` and the bitmask fallback is
dropped; the composite keys stay exactly as written. The starter stays on the
classpath for `identity` and `player`, which have not chosen.

**The defect this session existed to find: a version could not be deleted.**
`DELETE FROM gamedata.game_data_version` cascades to `item` down one chain and,
via `entity → skill → skill_rank`, to `skill_rank_cost` down another. Postgres
does not order those two chains against each other, so if `item` goes first the
NO ACTION foreign key on `skill_rank_cost.item_id` is checked immediately, sees
rows that still exist, and refuses:

```
ERROR: update or delete on table "item" violates foreign key constraint
       "skill_rank_cost_item_fk" on table "skill_rank_cost"
```

That is not an edge case. Re-ingesting a draft is the ordinary path — a
scheduled fetch re-runs, and a failed ingest must not wedge the sequence — and
it is a delete. **V2's tests never saw it because they only ever inserted.**
Fixed in `V3__gamedata_version_is_deletable.sql`: the nine item-referencing
keys gain `ON DELETE CASCADE`. Their job is the composite `(version_id, id)`
reference and that is unaffected. The tradeoff is written into the migration
rather than left implicit — deleting one item row now silently deletes the
costs and drops that cited it, which is acceptable *only* because a version is
written once and never edited. If anything ever edits a published version in
place, that rule is the first thing to reconsider.

**The second surprise, smaller and worth remembering.** `publish` threw
`IllegalStateException` for "there is no draft here", and the test caught
`InvalidDataAccessApiUsageException` instead. Spring's `@Repository`
persistence-exception translation rewrites a bare `IllegalStateException` into a
data-access failure, which this is not. Both refusals are named types now —
`PublishedVersionIsImmutableException` and `NoDraftToPublishException` — so a
domain refusal survives the trip out of the repository saying what it was. This
is a general trap for anything annotated `@Repository`, not a gamedata quirk.

**Design decisions worth carrying forward:**

1. **A bundle is not a definition.** `GameDataBundle` is what a parser adapter
   produces; `GameDefinition` is what the repository returns. They are different
   types because `GameDataVersion` requires a non-null `publishedAt`, so an
   unapproved snapshot has no representation as a definition at all. The type
   system now says what the schema's `published_has_timestamp` check says, and
   there is no read method that could return a draft.
2. **Validation happens before the connection opens**, and reports every
   dangling reference at once with the referrer named:
   `stage '1-1' drops unknown item 'sulfr'`. The composite foreign keys catch
   the same things one at a time, seconds later, saying `stage_drop_item_fk` —
   which tells the person approving a publish that something is wrong and
   nothing about what. Most of `CanonicalBundleParserTest` asserts on messages
   for that reason.
3. **The diff has three axes, not the two the plan names.** `PROGRESSION`,
   `CATALOG`, `GACHA`. Banners are neither material costs nor combat numbers,
   and filing them under either would be a lie told to keep the enum small.
   Subjects are diffed before fields, so a removed stage is one line rather than
   six. The rendered report over the two fixtures:

   ```
   proving-ground: 1.0 → 1.1

   progression · 5 change(s)
     + item 'sigil-radiant'
     ~ stage 'pg-1-1' · drop ore-rough: 1.4 → 1.6
     + stage 'pg-3-1'
     - stage 'pg-event-1'
     ~ upgrade 'warden-insight-2' · cost gold: 20000 → 18000

   catalog · 2 change(s)
     ~ entity 'warden' · atk at tier 1 level 40: 415.0 → 430.0
     ~ entity 'warden' · skill warden-strike rank 2 damage: 1.32 → 1.28
   ```

   A balance change to one multiplier reports that multiplier and not the cost
   beside it. That is the whole argument for versioning both axes together,
   rendered.
4. **`preview` is the CLI command that matters.** It answers "what would this
   file change?" against the currently published version, without writing a
   row. `ingest` prints the same report over what it just wrote. Publishing is a
   human approval, and an approval nobody could have reviewed is a rubber stamp.
5. **The fixture is synthetic on purpose, not as a placeholder.** Q3 says
   assume the community bundles are not redistributable — no licence is not
   permission — so `proving-ground` was invented to exercise every shape the
   format has: all four source kinds, both sinks, the catalog axis, a rotating
   stage, an expiring event, a banner with soft pity and a floor. Its 1.1
   version carries one change of every kind on both axes and its header lists
   them, so a broken flattener fails a test rather than silently omitting a
   field from every patch report.

**Also:** `GameDataDatabaseTest` gives the two new database classes one shared
container and one shared Spring context, started in a static initialiser and
reaped by Ryuk. Per-class containers would have added two more to every CI run
forever. `ApplicationBootTest` keeps its own deliberately — it exists to prove a
cold start against a database nothing has touched.

**Left undone, deliberately:** no HTTP route reaches any of this. Phase 1's exit
criterion says *your API* answers the two questions, and the API still serves
`/api/health` alone — that is **N7**. And the pipeline has not seen this tree.

### 2026-09-05 (third session) — Phase 1 opens: the canonical schema

- **`V2__gamedata_canonical_schema.sql` landed.** 28 tables, 7 indexes, the
  whole domain including the catalog axis. Verified rather than assumed: Flyway
  reports *"Migrating schema to version 2 — gamedata canonical schema"* and
  `Successfully applied 2 migrations` against PostgreSQL 16.15 in a
  Testcontainers run. Build green, 26 tests.
- **The three decisions the schema is built on**, in case a later session is
  tempted to undo one:
  1. *A version is a full snapshot, not a delta.* Publishing inserts a complete
     new row set and mutates nothing, so an old plan stays correct after a patch
     and the diff is a set comparison rather than a history walk. Game data is
     thousands of rows; the duplication is not worth optimising away.
  2. *A reference cannot cross a version boundary, structurally.* Every child
     uses a composite `(version_id, …)` foreign key against a
     `UNIQUE (version_id, id)` on its parent, so a v2 stage citing a v1 item is
     refused by Postgres. One extra column per table. The alternative leaves the
     diff — this phase's headline feature — one ingest bug from silently lying,
     with nothing to show for it.
  3. *Draft and published are different states*, enforced by a CHECK, because
     `GameDefinitionRepository` promises that "latest" means latest approved.
- **`attribution` is `NOT NULL` on every version.** "Numbers and text only,
  attributed" is a project invariant, so an unattributed snapshot should not be
  representable at all. Cheap now; a migration later.
- **`GameDataSchemaTest` — six tests, and they check both directions.** A
  constraint that only ever rejects is indistinguishable from a broken table, so
  each one also proves the legitimate case still goes through. The one worth
  keeping in mind: **in Postgres, NaN sorts above every number**, so the obvious
  `expected_yield >= 0` would have waved NaN straight through — it is the upper
  bound against `Infinity` that actually catches it. `Drop.java` already got
  this right in Java; the schema now matches, and there is a test that fails if
  either drifts.
  Deliberately **not** a Spring test: it tests the schema, and booting the
  application to do it would only make it slower.
- **One domain fix on the way in.** `Skill.Rank.upgradeCost` was
  `List<String>` — item ids as bare strings, against the project's own rule that
  identifiers are typed, and the only cost in the model that was not
  `List<ItemStack>`. Now `List<ItemStack>`, which also expresses "four of these
  and one of those", which a list of ids cannot. Same reasoning as ADR 0007's
  `kind` field and the same free window: nothing constructs a `Skill` yet.
  **The window is closing** — once ingestion writes fixtures, shape changes stop
  being free.
- **Tooling note that cost several retries:** the Bash heredoc broke repeatedly
  on the large SQL and Java content. Writing the file directly worked first try.
  For anything over ~100 lines, write the file rather than piping a heredoc.
- **CI green on the schema, first try, and the unknown resolved.** PR #3 merged
  as `662d999`; runs `33965175789` (PR) and `33965237825` (push to `main`), both
  success. All six `GameDataSchemaTest` cases PASSED **on the runner**, which
  answers the one thing worth watching: `GameDataSchemaTest` starts a *second*
  Testcontainers Postgres alongside `ApplicationBootTest`'s, and two of them run
  side by side on `ubuntu-latest` with no slowdown — `BUILD SUCCESSFUL in 1m 1s`,
  faster than the previous run with one.
- **The frontend job showed a sixth deprecated action** that the backend-only
  reading of N5 had missed: `actions/setup-node@v4`. N5 corrected from five to
  six.
- **What Phase 1 still needs, and it is most of it:** ingestion, the JPA layer,
  publishing with diffs, `gamedata-cli`, and the API that answers the two exit
  questions. The schema is applied and **empty** — it has never held a row of
  real game data, and that first ingest is where a modelling mistake will
  actually surface. See **N6**, which also flags the two things to settle before
  the JPA work: whether Hibernate validates the `TEXT[]` column, and that the
  composite foreign keys will be awkward to map and must not be "simplified"
  away.

### 2026-09-05 (second session) — the equipment question, answered from the code

- **N2 done: [ADR 0007](docs/adr/0007-equipment-is-an-entity.md) — equipment is
  an `Entity`.** The conclusion matches the leaning already on record, but the
  argument that decides it is not the one that was written down, and that is
  worth keeping. The prior-art note argued from PGR's Memories being
  equipment-like. The stronger reason was sitting in our own model: **`Upgrade`
  and `Goal` both key on `EntityId`**, so anything upgradeable that can be a
  planning goal *already* has to be an `Entity` — the alternative is not "a
  third concept", it is "a third concept plus a widened key on two records plus
  a second shape of upgradeable thing for the planner to know about". And the
  Entity/Item dual identity gear needs is not new either: `Item`'s javadoc
  already puts a character's *copies* in the inventory while the character is an
  `Entity`. Gear reuses that pattern exactly.
- **Gacha is indifferent, which was worth checking before arguing.**
  `BannerModel` and `FeaturedRule` are rarity-shaped; neither mentions
  `EntityId` anywhere. Whatever equipment is, the gacha module does not change.
  That removed a whole axis from the decision.
- **`Entity` gained a required `kind`** (`"character"` / `"equipment"`), opaque
  and game-supplied, for catalog routing and grouping. Backend build green
  with it. **The timing was free and will not be again:** nothing in the repo
  constructs an `Entity` yet, so adding a record component cost zero call sites.
  In Phase 1 it would have meant the ingest adapter and every fixture.
- **The honest gap, recorded as N4:** the ADR asserts that `planner`, `gacha`
  and `stats` never read `kind`, and nothing enforces that.
  `GameAgnosticismTest` scans for game slugs, not field reads. The rule is
  deliberately *not* written yet — the guarded modules are empty, so it would
  pass vacuously and prove nothing. It goes in with the first real planner code.
- **N1 done, and Phase 0 is closed but for the deferred deploy.** The wall was
  the same one as last session and it came down mid-session: `gh` is installed
  (2.100.0, winget) but **not on `PATH`** — the same MSI behaviour already
  recorded for the JDK — and `gh auth login` is interactive and handles
  credentials, so it cannot be driven from a session. Handed to the user, who
  ran it. Re-confirmed independently first that there was no way around it: the
  repo is private, so `api.github.com/repos/kietnt4412/storm_almanac` **404s
  anonymously**. **Cost me a round trip:** I handed over the command in a
  `bash` block and the user ran it in PowerShell, where a quoted path is not a
  command — it needs the call operator `&`. Give PowerShell commands in
  PowerShell form on this machine.
- **The result: green twice, and green for the right reason.** Runs
  `33963222427` (PR) and `33963295323` (merge to `main`). B4 item 5 asked
  something narrower than "did the run pass" — did the new Testcontainers test
  actually execute, or silently skip? Read the log, not the badge: both
  `ApplicationBootTest` cases show `PASSED` on the runner. Testcontainers works
  on `ubuntu-latest`. Nothing to tag, nothing to split.
- **Then this session's own commits went through it too.** Pushed to `dev`, and
  **nothing fired** — which was not a failure but the workflow doing what it
  says: `ci.yml` triggers on `push: branches: [main]` and `pull_request`, so a
  push to `dev` with no open PR runs nothing at all. Worth knowing before
  reading silence as breakage. PR #2 merged as `a82da1e`; runs `33964292888`
  and `33964297527` both green, `ApplicationBootTest` PASSED on both. Every
  commit made this session is now verified by CI.
- **Two deprecation warnings surfaced on that run**, recorded as **N5**: Node 20
  is deprecated and five actions are already being force-migrated to Node 24 by
  the runner, and `setup-java@v4` is deprecated outright. Green today, on a
  clock. Left for its own PR rather than bundled into a Phase-0 closing commit.
- **Git and `gh` are authenticated separately, and only `gh` is.** `git fetch`
  fails with "could not read Username" — no credential helper is wired for git
  itself, so pushes stay manual. `gh auth setup-git` would fix it.
  `branch.dev.remote` is also unset, so `git status` cannot report ahead/behind
  on `dev`; `git push -u origin dev` once would fix that.
- **Also re-learned, cheaply:** `2>&1` on a native exe in PowerShell 5.1 wraps
  the JVM's stderr banner as a `NativeCommandError` that *looks* like a build
  failure. `./gradlew build` exited 0. Read the exit code, not the red text.

### 2026-09-05 — the app runs; booting it found a 401 on the one endpoint

- **B2 done, and it paid for itself immediately.** Started Docker Desktop and
  ran `docker compose up --build` from cold. The image built, all three services
  came up healthy, Spring started in 5.2s, Hikari opened a pool, Flyway created
  `flyway_schema_history` and applied `V1__baseline.sql`, and Hibernate
  validated against the migrated schema. All six module schemas exist.
- **Then `curl localhost:8080/api/health` returned 401.** `modules/identity`
  declares `spring-boot-starter-security`, which puts it on the runtime
  classpath of `:app`. With no `SecurityFilterChain` bean anywhere, Boot's
  default auto-configuration secures every route with HTTP Basic. Phase 0's
  entire product surface was unreachable, and the pipeline's smoke step
  (`curl --fail "$DEPLOY_URL/api/health"`) would have failed the first real
  deploy. **`HealthControllerTest` passed throughout** — it calls the controller
  method directly, so no amount of it could ever have found this.
- Fixed with `SecurityConfig` in `modules/identity` — the module that owns the
  dependency, not `app`. Permits `/api/health` and the actuator health group,
  denies everything else, answers 401 rather than redirecting a probe to a login
  form, and is stateless so anonymous probes stop minting a `JSESSIONID` each
  (a health check every ten seconds would otherwise leak sessions forever).
- Added `ApplicationBootTest`: `@SpringBootTest(RANDOM_PORT)` over a
  Testcontainers Postgres, hitting real HTTP. **Verified it fails without the
  fix** — both cases red, not just compiled and assumed. It covers the four
  things a unit test structurally cannot: the datasource opening, Flyway
  migrating, Hibernate validating, and the filter chain. Its second case asserts
  an unmapped path is *401, not 200*, so a chain that permits everything cannot
  pass by accident in phase 3.
- **The lesson generalises past this bug.** Every defect found in the last two
  sessions — the `gradlew` exec bit, the missing lockfile, the invalid YAML,
  and now this — was invisible to a green local `gradlew build`. The build
  signal is compile-and-unit-test and nothing more. Boot the thing.
- Found **E1**: Avast MITMs TLS and the JDK truststore does not trust its CA, so
  Gradle cannot download any *new* dependency. Cached for four sessions, so it
  only surfaced on adding Testcontainers. Worked around with
  `-Djavax.net.ssl.trustStoreType=Windows-ROOT`; a permanent fix is an open
  decision recorded under E1.
- Housekeeping: the catalog pinned Testcontainers to 1.20.4 while Boot's BOM
  pulled the shared core to 1.21.3 transitively — direct artifacts and core on
  different versions. Dropped the pin and let the BOM manage it, matching how
  every Spring entry in the catalog is already declared.
- **B1a was already done before this session opened** and the tracker still
  showed it open. Corrected. The file says "tick boxes as work lands"; this is
  what happens when that slips.
- Backend green: 17 tests. Left the compose stack running.
- **B4 answered from a screenshot: `ci` run #5 passed on `1a58dd5`**, 2m 58s,
  both jobs. Five runs to get there, each failing for a different reason, and
  none of the four failures was a code defect — exec bits, a missing lockfile,
  invalid YAML, and a fake-green deploy job. Also settled that the three
  `nightly-chaos` entries are skips (`if: false`), not failures.
  Worth noting how this was confirmed: the repo is private and there is no `gh`
  here, so the Actions API 404s and the answer had to come through a human with
  a browser. That is Q7, and it should be fixed before Phase 1.
- **E1 resolved the same day it was found** — Avast's HTTPS scanning turned off
  at the source rather than worked around in the build. Verified with
  `--refresh-dependencies` and no flag. Nothing was committed for it, which is
  the right outcome: a machine-specific TLS workaround in a shared build file
  is a trap for the next person, and for CI.
- **B7 written**, from `docs/prior-art.md` rather than from ambition. The
  temptation was "we have a solver"; ArkPlanner has one, so the paragraph claims
  the four narrower things that are actually differentiated.
- Closed the session by pushing `dev` and merging **PR #1** into `main`
  (`73007df`). `gh` was installed, but not authenticated — so the resulting
  pipeline runs were *not* observed, and `ApplicationBootTest` has still never
  executed on the runner. **That is N1, and it is the only thing between Phase 0
  and closure.** Recording it as open rather than assuming it passed: assuming
  is how B1a sat ticked-but-stale for three sessions.
- Phase 0 is otherwise complete, with its exit criterion deliberately unmet per
  D1. Phase 1 opens on **N2 — Q6/F4, equipment modelling**, which blocks
  ingestion and deserves an ADR rather than an inline decision.

**State at session end:** `main` at `73007df`, working tree clean, 17 tests
green locally, compose stack verified and left running.

### 2026-09-02 — prior-art read; first real model defect found

- Read Kornblume, Penguin Statistics `backend-next` and ArkPlanner. Written up
  in [docs/prior-art.md](docs/prior-art.md).
- **Found a genuine defect in the domain model.** `Drop` stored
  `declaredProbability` validated to `[0,1]` plus a separate `quantityPerHit`.
  Real upstream drop values are **expected yield per run and exceed 1.0** (up to
  2.187 observed) — our validation would have rejected most of the dataset on
  ingest. Replaced with a single unbounded `expectedYield`, which is also the
  quantity the MIP constraint actually needs. This is exactly what the reading
  was for, and it was found before any ingestion code existed rather than after.
- Confirmed by inspection rather than assumption: **Kornblume does no
  server-side solving** — its farming routes are precomputed greedy results
  baked per patch. The wedge is real.
- ArkPlanner already returns integer stage counts and exposes item `values`,
  which is a farming LP's dual in all but name. Integrality and shadow-price
  explanation are validated prior art, not our inventions.
- Corrected the README's Kornblume URL — it cited a fork, not upstream — and
  removed the now-stale wrapper bootstrap note.
- New open question **Q6** (equipment modelling) and follow-ups F1–F4.
- Backend still green after the model change: 15 tests passing.

### 2026-09-02 — hosting deferred; Phase 0 closed with an exception

- Q1 answered: VPS in principle, but **no money will be spent**, so nothing is
  provisioned. Logged as deviation **D1** with its full cost rather than as a
  scheduling note — it removes the Track B gate, which is the plan's central
  structural bet.
- Agreed mitigation: revisit hosting at **Phase 4** (on the plan's "never cut"
  list) rather than at the end. Phases 1-3 need no server.
- Fixed run #4: `ci.yml` was invalid YAML at line 67 — an unquoted `run:` scalar
  containing `: `. No jobs ran at all. Also caught, on re-reading, that the
  `deploy` job would have echoed a TODO and exited 0, painting the pipeline
  green while nothing deployed. Now `if: false` — skipped is honest, fake-green
  is not.
- Validated all five repo YAML files locally with `js-yaml` under node. Cheaper
  than a CI round trip; do this before every workflow push.

### 2026-09-02 — first CI runs, both jobs fixed locally

- Pushed; all three workflow runs went red. #1 and #2 predate the wrapper and
  could never have passed. #3 is the informative one, and it failed in both jobs
  for unrelated reasons.
- **Backend:** `gradlew` committed as mode `100644`. Git on Windows does not
  track the executable bit, so Ubuntu refused to run it. `git update-index
  --chmod=+x` sets the mode in the index without needing a Unix filesystem.
  This will recur for every shell script added from this machine.
- **Frontend:** no `package-lock.json`. That broke the job twice — `npm ci`
  requires a lockfile, and `setup-node`'s cache path could not resolve.
- Generating the lockfile doubled as B3: 415 packages resolved with no peer
  conflicts, so the `package.json` versions written from memory hold. Typecheck
  and `vite build` both pass; PWA service worker generates.
- Added `gradle/actions/wrapper-validation@v4` to CI, since `gradle-wrapper.jar`
  is now a committed binary that every build executes.
- Lesson worth keeping: the two failures were both *environment* mismatches
  between Windows and the Linux runner, not code defects. A green local build
  says nothing about either.

### 2026-09-02 — first green build

- Installed JDK 21 (Temurin 21.0.12 via winget). Two friction points worth
  recording: winget's Temurin package does **not** register `PATH` or
  `JAVA_HOME` (set manually in `HKCU:\Environment`), and **Gradle is not
  available in winget at all** — no choco or scoop on this machine either. The
  wrapper was generated by IntelliJ, which pinned Gradle **9.6.0**.
- Ran `./gradlew build`. Gradle 9.6.0 accepted the 8.x-era build scripts without
  complaint — the version-catalog lookup in the root `subprojects` block and the
  Spring dependency-management plugin both worked, which is more than expected.
- **One genuine failure:** ArchUnit rejects empty layers, and `store`, `raft` and
  `chaos` are empty by design. Fixed with `withOptionalLayers(true)` rather than
  by deleting the rules — the rules must be live before the code lands, not
  written at the moment the temptation to skip them peaks.
- Build now green: 35 tasks, 15 tests passing, `storm-almanac.jar` produced.
- Git initialised, MIT licensed, two commits. SSH push failed (local ed25519 key
  not registered with GitHub) so the remote is HTTPS via Git Credential Manager.
- **Still unverified and worth repeating:** no process has ever been started, no
  container has ever run, `npm install` has never executed, and CI has never
  fired. A green `gradlew build` is a compile-and-unit-test signal, nothing more.

### 2026-09-02 — scaffold from plan

- Read `plan.html` in full.
- Created the repo skeleton: Gradle multi-module backend (8 product modules,
  3 substrate modules, 1 app), Vite/React frontend, Docker Compose, CI workflow,
  nightly chaos workflow (disabled), `.gitignore`, `.editorconfig`, `.env.example`.
- Wrote the domain model in `modules/gamedata` as records and sealed
  hierarchies: `Source` (Stage/Craft/Shop/Reward), `Sink` (Upgrade/Fodder),
  `BannerModel` with `PityRule`/`FeaturedRule`/`PityScope`/`Floor`, the catalog
  axis (`Entity`, `StatCurve`, `Skill`, `Talent`), and `GameDefinition`.
  `Fodder` and `Goal.Satisfiability.PROBABILISTIC` are in from day one, per the
  plan's insistence that the second game's shape be designed for rather than
  retrofitted.
- Wrote real, tested implementations for the two pieces that are pure functions
  and cheap to get right early: `WilsonInterval` and `PityRule.rateAt`. The pity
  tests are written as acceptance fixtures against both games' published rates.
- Wrote `ModuleBoundaryTest` (ArchUnit) and `GameAgnosticismTest` (source scan)
  so the two central invariants fail the build rather than a review.
- Wrote ADRs 0001–0006, each with a reversal trigger.
- **Did not build or run anything.** No JDK on the machine, Docker daemon not
  running. Every "written, unverified" row above is genuinely unverified.
- Left the Gradle wrapper ungenerated (it needs a Gradle install to produce the
  binary `gradle-wrapper.jar`). Documented the one-time bootstrap in the README.

### Rows compressed out of *Current state* (2026-09-18, twenty-second session)

Moved here to pay for the PGR reading's additions, per the tracker's own rule that
a session which adds more than it removes has moved the problem rather than done
the work. Nothing below changed; it stopped being the first thing a session needs.

**Game data API — served and verified.** Seven game-data routes plus health,
version-pinnable, every response carrying its version and attribution — and, since
2026-09-11, the four that carry facts also carry **where each fact was read**. 15
HTTP tests plus a hand check against `docker compose up`. Two arrived with N25 and
both existed because every read before them started from a slug the caller already
had: `GET /api/games` is the **index** — without it a reader with no account and no
slug could reach the public catalog only by guessing a URL — and
`GET /api/games/{game}/items` is **the vocabulary an inventory is written in**,
which reached a client only as resolved names inside a cost until something had to
render a few hundred quantities.

**Frontend — five screens, driven in a browser, and 15 tests.** Inventory editor
(bulk entry: filtered, grouped by the game's own categories, Enter walks the
column, **no save button** — a typed number is a queued edit), goal picker
(ordered, targets read off the upgrade graph, roster edited where the goal is),
plan view (stages, crafts, claims, shadow prices and **every one of the solver's
notes**, because a plan rendered without them is a confident number hiding a gap),
catalog browse and search, and the character page with the **personalized
overlay**. Routing is react-router; the game is in the URL for the catalog and
nowhere else, because a catalog page is the one thing here somebody sends a link
to. Both catalog pages end in **where their numbers were read** rather than one
grey credit line. **15 vitest/jsdom tests run in CI** — see the tracker's
unverified list for what they deliberately do not cover.
`npm run dev --prefix frontend` (`.claude/launch.json`, which also carries `api`
and **`web-built`**, the built bundle on 4173 that the offline test needs) proxies
`/api` **and `/dev`** to `localhost:8080`, so local is same-origin. The sign-in URL
is chosen behind `import.meta.env.DEV`, so the development one is not in a
production bundle.
