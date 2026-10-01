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

## C2.5 · The second pass, and the game switch (2026-10-01, fifty-seventh)

**Agreed 2026-10-01.** Claude drove every screen and proposed five changes as
mockups. The maintainer took all five and answered the three questions: ticks
are **saved with the saved plan**, the bag is **tied to the plan**, and all of
it lands **before the C2.4 rehearsal**. They then added the game switch, and
answered three questions on it: **hand-drawn lettering, not the games' icons**
(icons are game art), **the switch replaces the light / dark button** (the
scheme follows the device), and **Reverse: 1999 is listed as coming soon on
production**.

### As built

1. **Plan: answer first, one checklist.** Once there is a plan, the inputs fold
   to one line: "Planned with 160 serum a day · 30 days · least serum · Phantom
   Pain Cage 120,000+ · Change". Runs, purchases, feeds and claims are one
   *Do this* list. The tiles are serum, days ("28", not "28.0") and runs.
   **Ticks are kept with the saved plan** (`V20`, `player.saved_plan.done`;
   `PUT /api/me/profiles/{p}/plan/done` names the plan by `savedAt`). A tick
   against a plan that has since been re-run is a 409, and a new plan starts
   unticked.
2. **Pulls: the answer beside the form** from a laptop's width, held in view.
   Open pools are cards with their time left, ordered so **the one open longest
   comes first**. Until now the page opened on a pool closing in hours and
   answered "0% within 0 days". Zero days now says "what you hold now".
3. **Goals: a from → to bar on every track being moved**, with the S2
   dropdowns kept. A saved row shows only the tracks with a target, and folds
   the rest behind "+ 10 more tracks, left as they are". A row being set up
   stays whole while the reader works on it. **"Max"** sets every track in a
   section to its end. This is the goal-track bar C2.3 left unbuilt.
4. **Roster: one line per construct**: "Promote Elite ★3 · Level 60 · Red Orb
   4 · Core Passive 4 · 10 untouched", with a bar for how far up every track
   together. A chevron opens it to edit. Someone just added opens on their own.
5. **Inventory: tiles**, two to four across. A stripe colours rarity by its
   place among the ranks present, not by any game's scale. Each tile says what
   the saved plan spends or buys of it ("plan buys 654,000"). That needed
   `boughtItem` and `boughtQuantity` on a purchase's `spends`. A plan saved
   before them marks only what it spends.
6. **Smaller:** Home's setup is one line once all four steps are done. The
   catalog's account of each reading folds behind "How it was read", while
   what was read, when, and every warning stay in the open.
7. **The game switch** ([ADR 0039](../adr/0039-a-games-look-is-presentation-data-in-the-client.md)).
   It sits in the side panel where the theme switch was. Every published game
   is offered in its own colours, and an upcoming one is greyed as "Soon".
   Picking a game re-dresses the site: PGR is near-black and crimson, R1999
   aged paper and amber, each in light and dark. Every screen becomes about the
   reader's profile for that game. With none, a signed-in reader is taken Home
   with the add-profile form open on it. The brand mark is the game's lettering.

**Driven locally** on `rehearsal-s6`: the plan's ticks survive a reload and
a re-plan clears them. Pulls opens on Adelyde (34 days). The goal row folds 10
tracks. The roster line reads as above. Inventory shows plan spends and buys
after a re-plan. The switch goes PGR → R1999 (with no R1999 profile, it opens
Home's form on Reverse: 1999) → PGR, in light and dark. At 375 px no screen
scrolls sideways, and the switch sits in the drawer. **Not yet driven:** the
R1999 screens with an R1999 profile. The local R1999 banner route fails
on its own data ("craft 'craft-spell-of-banishing' consumes nothing"). That is
a version published before a rule tightened, not this change.

## C2.6 · Motion, and the top bar (2026-10-01, fifty-eighth)

**Agreed 2026-10-01.** The maintainer shared a screen recording of a product
site (Gcore's) and asked for its opening and animation. Claude pulled frames
from it and proposed five adaptations. The maintainer took four: **A** an
opening, **B** a hero entrance, **C** scroll reveals, **D** the plan as a
circuit. They agreed to skip **E**, the dotted 3D globe: it needs a library,
and there is nothing global to show. They answered: the opening plays **once
per browser session**. They then asked for **the recording's nav bar** in place
of C2's side panel, and chose **grouped menus** (Home · Planner ▾ · Pulls ·
Catalog) and **the same scroll behaviour** (full width at the top, a floating
pill once scrolled).

### As built

1. **The opening** (`ui/Intro.tsx`). The game's mark turns in, "Storm
   Almanac" slides out from behind it, then the curtain is clipped away from
   its bottom edge, so the page rises over it. About 2.2 s; any key or click
   skips to the wipe. It is kept once per session by `sessionStorage`. It is
   never shown under `prefers-reduced-motion`, or where `matchMedia` is
   missing (jsdom), so no other test sees it. While it plays, `data-intro` on
   the root holds every entrance paused, so the hero's words rise as the
   curtain lifts.
2. **Hero entrance** (`ui/motion.tsx`). `Words` splits a heading into words
   that rise one after another. The sentence stays whole in an `sr-only` copy,
   so a screen reader and `getByRole('heading', { name })` hear it once. Home's
   hero gets a pale streak of the brand colour in its corner, which sweeps in
   and then breathes. The facts, plan card and banner rise in a stagger, and
   each page rises in when opened, keyed on the first part of its path.
3. **Scroll reveals.** `Reveal` slides a block up the first time it enters the
   view, through `IntersectionObserver`. Content is hidden only where the
   browser can bring it back, so a missing observer or reduced motion leaves
   it visible. Used on Home's setup and profiles, and the plan's checklist and
   prices.
4. **The plan as a circuit** (`ui/PlanCircuit.tsx`). What the reader does is
   on the left: the dearest stages, then one node for the purchases and one
   for the claims. Each character the plan pays for is on the right. The cost
   sits on a glowing chip in the middle, with traces and a travelling pulse.
   **Every trace runs through the chip, never stage → goal**, because the plan
   does not say which run pays for which goal. The traces are measured off the
   laid-out nodes with a `ResizeObserver`. On a phone the columns stack and
   draw no traces. It is `aria-hidden`, because the checklist and "What this
   pays for" say all of it in words. The right side groups by
   `payingFor.entity`, so it reads "Lucia: Inverse Crown · 43 upgrades" even
   before track names load. Grouping by the named tracks first showed 43
   separate steps.
5. **The top bar** (`App.tsx`, `motion.css`). It replaces C2's side panel and
   its hide button (`ui/preferences.ts` is gone). It is full width and
   see-through at the top. After 24 px of scroll it gathers into a centred
   pill with a blurred background, its width animated, and the name folds
   away beside the mark. **Planner ▾** holds the four steps, each with its
   icon and step title. The game switch and the account (profile picker,
   backend status, sign out) are chips on the right. A signed-out reader gets
   a "Sign in" pill. Menus open on click, or on hover with a mouse. The backend
   status line moved to a new footer. On a phone, a menu button drops a sheet
   with every page, the game switch and the account.
6. **Still twins.** Every effect has a `prefers-reduced-motion` rule that
   leaves it where it ends, in `src/motion.css`. That file sits outside
   Tailwind's layers (`@layer` needs `@tailwind` in the same file), so its
   classes beat a utility on the same element. That is why the phone sheet's
   `md:hidden` sits on a wrapper.

**A bug the tests found:** a mouse hover opened a menu, and the click that
followed toggled it shut, so for a mouse it never stayed open. A click after a
hover now keeps it open, and leaving closes it after 150 ms, so a pointer
slipping off the edge does not lose it.

**Driven locally** as `rehearsal-s6`, at 1280 and 375 px, dark and light: the
opening's lockup and its wipe, the full-width bar and the pill (832 px, fully
round, the name folded), the Planner menu over the hero, the circuit on the
real plan (one stage, five buys, three claims → Lucia), the stacked circuit
on a phone with no sideways scroll, and the phone sheet. No console errors.
**The Browser pane advances CSS animations only while it is painting**, so
the opening was checked by seeking its animations rather than timing
screenshots. Its timing needs a person to watch it once in a real browser.

## C2.7 · A feel per game, wordmarks, and the reader's picture (2026-10-01, fifty-ninth)

**Agreed 2026-10-01.** The maintainer found PGR and Reverse: 1999 "too simple":
a palette swap and three letters in a square. Asked whether they meant the
look, the switch or the data, they chose the look. Claude mocked up both games
in both schemes. The maintainer agreed to everything except R1999's mark, and
sent the game's logo as the idea for it. Then they sent PGR's for the same
treatment, "but creative in our own", and asked for the words to animate.
They also asked to show their Google picture, stored as Claude proposed.
[ADR 0040](../adr/0040-a-games-look-is-a-drawing-a-wordmark-and-a-dress.md)
supersedes 0039's decisions 1 and 2.

### As built

1. **Wordmarks** (`ui/Wordmark.tsx`, `looks.css`). PGR: heavy italic
   PUNISHING, skewed, cut through by two speed bands, with streaks trailing
   off its left. Its letters slam in from the right, a hatched rule draws
   out, and GRAY RAVEN types in, spread across the width. Every 7 s it
   glitches for a moment. R1999: REVERSE in spaced serif capitals, rising in
   letter by letter from a blur. A hairline with a diamond draws out from the
   centre, and 1999 flips in digit by digit like a clock's leaves. Every 9 s
   a light passes along the word. **Our own type:** system faces, nothing
   traced from the logos.
2. **Where they sit.** In the top bar, with "Storm Almanac" small under it
   and **no mark beside it** (the maintainer, after seeing it). As the bar
   gathers into the pill, the word squeezes towards its left edge and fades
   while the mark turns in where it stood, with a flash as it lands; at the
   top again, the reverse. 768 px up only, since a phone's pill keeps the word. Keyed by game, so a switch
   plays it again. In the opening, large, which now holds 2.1 s rather than
   1.5 s for a game with a look, so the letters land first.
3. **Marks** (`ui/GameMark.tsx`). PGR keeps the agreed plate: cut corners,
   a crimson slash, "PGR" stencilled. R1999 gets a seal: a double rule, a
   serif R, a rule, and 1999 spaced under. Both are drawn in the game's tokens,
   so they follow the scheme.
4. **The page's dress** (`looks.css`, on `:root[data-game]` only). PGR: a
   blueprint grid fading out under the top of the page; cards with a crimson
   corner tab; a hero with cut corners, scanlines and a hazard stripe; slanted
   uppercase buttons; italic uppercase headings; monospace labels, chips and
   figures; skewed step segments. R1999: paper grain; serif headings and
   old-style figures; a second rule inside every card; a hero with an outer
   rule and **rain that rises** in place of C2.6's streak; pill buttons in
   serif; italic chips.
5. **The reader's picture.** `V21` adds `identity.account.picture_url`,
   https only (a CHECK, and `SignIn` drops anything else). It is refreshed on
   every sign-in like the name, and a sign-in that sends none clears it.
   Google's `picture` claim fills it; Discord's `avatar` is a hash, so
   Discord shows initials. `/api/me` carries `pictureUrl`, optional on the
   wire. The account chip shows it with `referrerpolicy="no-referrer"`, and
   falls back to the initial if it fails to load. The worker caches game data
   only, so the photo is never cached.

6. **No server status for readers** (the maintainer, from production: a
   commit hash in the account menu and footer). Both lines are gone. Only
   "Can't reach the server — showing what this device remembers" is said, and
   only then. The version stays on `/api/health` for us.
7. **A footer with a credit** (`ui/Footer.tsx`): what this is, "a fan-made
   planner, not affiliated with any game's publisher", and **Thel** written by
   a pen. Four hand-drawn strokes (crossbar, stem, "hel", swash), each drawn in
   turn over 5 s, held, faded, and written again every 9 s. Under it is the
   maintainer's Discord handle with a copy button, Discord being the contact
   they chose. The handle is one constant, `DISCORD_HANDLE`.

**Tests:** 542 backend (+3: https only, Discord has none, refreshed and
cleared), 140 frontend (+5: the opening holds for a wordmark, the picture
and its fallback, and the footer three times: the credit with no status, the
offline notice, the copy).

**Driven locally** with the API on sequence 18, at 375 and 1280 px, dark and
light, both games: the openings mid-animation, Home, the game menu, the pill,
and a dev account given a picture link by hand, loaded in the chip. As in
C2.6, the Browser pane advances animations only while it paints, so a first
screenshot of a page can catch it mid-entrance. **The animations need a
person to watch them in a real browser.**
