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
