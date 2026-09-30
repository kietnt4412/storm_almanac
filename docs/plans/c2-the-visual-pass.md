# C2 — the visual pass

**Status:** drafted 2026-09-30 (fifty-fifth session). **Agreed the same day**:
direction **A · Storm**, all four visuals, explanations cut to one sentence
plus "Why?". **Navigation is the maintainer's own answer, not the
recommendation:** *a side panel with a hide button*, in place of both the top
links and the proposed bottom tab bar. The exit wording (question 5) is still
proposed.

**What C2 means, in the maintainer's words (2026-09-30):** make the product look
better: colour, theme, visuals. The rehearsal's leftover hesitations are
secondary.

C2's exit, as proposed on 2026-09-26 and still not agreed: *every screen driven at
375 and 1280 px, and a self-run rehearsal over every screen leaves no stall
unfixed or uncut.* **Proposed addition: in both themes, light and dark.**

## What the screens look like today

Seen 2026-09-30, locally, on the rehearsal account:

- **One colour does everything.** A teal accent on a flat teal-grey ground,
  with amber for warnings. Every card is the same bordered box, so the plan's
  headline numbers look as important as its footnotes.
- **System font, mostly `text-sm`, and long paragraphs.** Each screen opens
  with two or three sentences of explanation before its first control.
- **No visuals.** The plan is text and one table. The pull planner's answer is
  one percentage. The character page's stat curve is a list of numbers. The
  four steps are pills, and the header is a row of links that wraps on a phone.
- **The theme follows the system and can't be changed.** Both schemes are
  defined in `index.css` as CSS variables, which is the right foundation: a
  new palette is one block per scheme.
- **No game assets, ever** (CLAUDE.md). So the look has to come from colour,
  type, layout and icons, not character art.

## What gets built — four slices

### C2.1 · The foundation

- **A palette**, the direction the maintainer picks (question 1), as CSS
  variables for both schemes. It keeps the names `index.css` already uses, so
  every screen changes at once.
- **A light / dark / system switch** in the header, remembered in the browser.
- **A type scale.** Big, tabular numbers for the things a reader acts on
  (serum, days, chance). Body text a step larger, and explanations shorter
  and quieter.
- **Surfaces with depth**: a page, a card, and a raised "answer" card. The
  plan's result and the pull odds stop looking like the form above them.
- **Icons**: a small inline SVG set (stage, shop, craft, claim, pull, goal),
  written by hand or from an icon package, with no game art.

### C2.2 · The shell

- **A side panel** holding the brand mark, the navigation and the account,
  with a button that hides it (**agreed 2026-09-30, the maintainer's answer**).
  On a wide screen it sits beside the page and collapses to a slim rail. On a
  phone it's hidden by default and slides over the page from a menu button.
  Whether it's hidden is remembered in the browser.
- **The four steps** become a progress bar that says how far through the
  reader is.

### C2.3 · The visuals

Charts are hand-written SVG, with no chart library:

- **Pulls: the chance-by-pull curve.** Chance of the featured unit against
  pulls, with the reader's pity and budget marked on it, and the headline
  percentage large above it. **This needs one backend field**: the engine
  already answers any number of pulls, so the server sends the curve beside
  the one number it sends today.
- **Plan: stat tiles** for serum, days and objective, and **where the currency
  goes**, one bar split by what it buys (Cogs, Skill Points, EXP). Each goal
  track shows its from → to as a progress bar.
- **Character page: the stat curve as a line chart** in place of the list.
- **Home**: the profile card shows the reader's saved plan in one line (serum,
  days, and whether it's current).

### C2.4 · Every screen, both themes, both widths

Drive every screen at 375 and 1280 px in light and dark, then run a self-run
rehearsal (the maintainer and Claude, as agreed 2026-09-26). This slice is the
exit.

## Questions for the maintainer

1. **Which direction?** The three mockups shown in the session:
   **A · Storm** (deep navy, electric cyan, violet), **B · Almanac** (warm
   charcoal, brass, a serif for numbers), **C · today's teal, refined**.
   **Recommended: A.** It suits a sci-fi gacha title, and it is the clearest
   visible change from today for a CV screenshot. B is the most distinctive.
   C is the safest. Each gets a light scheme too.
2. **A bottom tab bar on phones?** **Recommended: yes.** The readers are on a
   phone next to the game, and today's header wraps.
3. **Which visuals?** **Recommended: all four in C2.3, pull curve first.** The
   pull curve is the only one that needs the backend, and it is the most
   striking.
4. **Shorter explanations?** **Recommended: yes.** Keep the first sentence on
   screen and move the rest behind a "Why?" toggle. The wording itself stays.
   It is what made the strangers trust the numbers.
5. **Agree the exit** as proposed, plus "in both themes".

## As built (2026-09-30, fifty-fifth)

C2.1, C2.2 and three of C2.3's four visuals are built, with the
explanations cut to one sentence plus "Why?". All of it is on `dev`, not
deployed. What differs from the plan:

- **The stat curve chart is not built.** No PGR entity in the bundle carries
  a stat curve (none has been read from the client), so the chart would draw
  nothing on the live game. It waits for a reading, or for Phase 11's R1999.
- **The pull curve** is `curve` on `/pulls`: the chance at every pull count
  to the worst case, rounded to four decimals. `MarkovBannerEngine.curveOfFeatured`
  walks the chain once, and `probabilityOfFeatured` is now that curve's last
  point, so the number and the chart are one answer by construction.
- **Where a currency goes** needed numbers too: a priced shop row's
  `ConversionView` carries `spends` (currency, quantity, what it buys) beside
  its sentence. On the rehearsal account the plan's 3,362 Simulation Score
  (41 runs × 82) split 74% EXP Pod (L), 16% Cogs, 10% Skill Point.
- **Goal-track progress bars** were not built. `payingFor` gives from → to
  per track, but drawing it as progress needs each track's full ladder, and
  that is a larger change than the rest of C2.3.
- **Icons are hand-drawn** (`ui/Icon.tsx`, fifteen shapes): no game art, and
  no new dependency to fetch through E1.
- **Theme and panel** are two plain `localStorage` keys, not the planner
  store, so its persisted version and outbox migration are untouched.

**Driven** locally as `rehearsal-s6`: the plan and the pull planner at 1280 px
in dark, Home and the rail in light; at 375 px, every screen has no
horizontal overflow, the panel starts hidden, the menu opens it and a
navigation closes it. Screenshots were unreliable (the app window was
minimised for much of it), so most checks were read from the page's layout
rather than looked at. **The self-run rehearsal (C2.4) has not happened.**
