# 40. A game's look is a drawing, a wordmark and a dress

**Status:** Accepted · 2026-10-01 · supersedes decisions 1 and 2 of [0039](0039-a-games-look-is-presentation-data-in-the-client.md)

## Context

ADR 0039 gave each game a look: a palette, and its lettering in a coloured
square ("PGR", "1999"). On the day it shipped, the maintainer found it too
simple. They asked for a feel per game, not only a colour. Claude mocked one up
and the maintainer agreed to it. They then asked for a wordmark in place of the
square for each game, after the games' own logos ("REVERSE / 1999",
"PUNISHING / GRAY RAVEN"), but in our own style, and for the words to animate.

Two of 0039's decisions no longer describe what was built:

- **Decision 1** says a look holds "the lettering for its mark". It now holds
  a wordmark and the choice of a drawn emblem too.
- **Decision 2** says a palette is "the existing tokens, redefined", and that no
  screen changed. Still no screen changed, but a game now restyles shapes,
  type and texture as well, which tokens alone cannot do.

The constraint from 0039 still holds: **no game assets**. The logos the
maintainer showed are game art. A wordmark copied from one would be the
game's logo in all but file format.

## Decision

1. **A look holds a wordmark and an emblem kind** in `gameChoice.ts`. The
   wordmark is two lines of text, a big word and a small line under it. The
   emblem kind is `plate` (cut corners, a slash, the lettering stencilled) or
   `seal` (a double rule round a serif initial, the lettering spaced under).
   **`gameChoice.ts` stays the only file that names a game.** `GameMark` and
   `Wordmark` choose a drawing by the look's data, and branch on no game.
2. **The type is ours.** It uses system faces only, with no downloaded font, so
   the app still works offline and adds no font source. The faces are skewed,
   cut through, spaced and animated in `looks.css`. The maintainer's references
   set the mood. No glyph is traced from them.
3. **A game dresses the page in `looks.css`**, under `:root[data-game=…]`:
   card and button shapes, heading type, a texture on the ground, the hero's
   flourish. These rules key on the root only, so a switch row that wears
   another game's colours does not take its shapes.
4. **Every animation has a still twin** under `prefers-reduced-motion`, as in
   C2.6.

## Consequences

- Adding a game's look is now one entry, two palette blocks and one block of
  dress. Forgetting the dress costs the Storm shapes, nothing worse.
- `looks.css` sits outside Tailwind's layers, so it beats a component class on
  the same element. A screen that wants a shape no game may change has to say
  so there.
- Nothing enforces decision 1 except review, as before.

## Reversal trigger

A publisher grants permission to use its logo, which would make the wordmark
the real one. Or the reversal trigger of 0039 fires, and the look moves into the
server's game record.
