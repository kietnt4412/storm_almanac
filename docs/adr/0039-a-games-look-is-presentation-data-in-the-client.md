# 39. A game's look is presentation data in the client

**Status:** Accepted · 2026-10-01

## Context

The maintainer asked (2026-10-01) to replace the light / dark / system switch
with a switch between the games. Picking one would change the whole site to
that game: its palette, its Home and every screen's data. They also asked for
each game's app icon beside the site's name.

Until now the client had held itself to the same rule as the backend's
`planner`, `gacha` and `stats` modules. It named no game. Everything a screen
showed about a game came from the server: its name, its energy word, its
categories. `Inventory.groupByCategory` says so in its javadoc. A switch that
dresses the site *per game* has to know, somewhere, which colours belong to
which game. The server has no place for that, because colour is not a fact
about a game. A bundle carries numbers read from the client, with provenance,
and a palette has neither.

Three further constraints:

- **No game assets** (CLAUDE.md, and every page's attribution line). The
  games' app icons are game art. The maintainer chose hand-drawn lettering
  instead ("PGR", "1999") when asked.
- **Reverse: 1999 is not published on production.** Its data is third-party
  (ADR 0015) until Phase 11 sources it first-hand. The maintainer still wants it
  listed there, greyed, as coming soon.
- **The light / dark choice goes**, and the device's scheme decides. Each game
  needs both schemes.

## Decision

1. **One file, `frontend/src/ui/gameChoice.ts`, is the only place the client
   names a game.** It holds a list of looks: a game id, the lettering for its
   mark, a fallback name, and whether it is upcoming. Nothing branches on a
   game anywhere else. The id selects a palette in `index.css` through the
   `data-game` attribute. A game with no look gets the Storm palette and the
   bolt, as the synthetic Proving Ground does.
2. **A palette is the existing tokens, redefined** under
   `[data-game='<id>']` once per scheme. No screen changed to support it. The
   attribute sits on the root for the page, and on any element that must wear
   another game's colours. The switch draws each game's mark in that game's
   own colours this way.
3. **The switch decides which game every screen is about.** `useActiveGame`
   picks the game the reader switched to, if the server publishes it; otherwise
   their selected profile's game; otherwise the first game the server publishes.
   `useSelectedProfile` then answers with the reader's profile *for that game*,
   or none. With none, the gate offers to make one, rather than showing another
   game's data under this game's colours.
4. **"Coming soon" is the only thing the client says that the server does
   not.** A look marked `upcoming` is listed and greyed while the server does
   not publish it. Once the server does, it is offered like any other game, with
   no change to the client.
5. **The choice lives in `localStorage`**, like the panel's, and is applied
   before the first render so no other game's colours flash first.

## Consequences

- The client's game-agnosticism changes from *names no game* to *names games
  in one data file and branches on none*. Adding the second title's look is one
  entry and two CSS blocks. Forgetting to add it costs nothing worse than the
  Storm palette.
- A reader can no longer force light on a dark device. The maintainer accepted
  that as the cost of the game switch.
- `GameAgnosticismTest` scans backend source only, so nothing enforces rule 1.
  It holds by review, the same way the client's rule held before it.

## Reversal trigger

A third game whose look the maintainer wants without a client release. That
would move the look into the server's game record, as a presentation field
outside the bundle's facts, and this file would go.
