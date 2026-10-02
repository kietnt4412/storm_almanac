package io.stormalmanac.gacha;

import java.util.List;

/**
 * A banner's rules as tables, for a client that rolls its own dice against them
 * (C2.21) and must not write a game's rules a second time to do it.
 *
 * <p>Read straight off {@link PullModel}, so every number here is one an engine
 * already uses: the client draws a wall per cycle, as
 * {@link MonteCarloBannerEngine} does, and looks the hit rate up rather than
 * working it out. What the client still owns is the bookkeeping {@link PityState}
 * does — a miss counts up, a hit counts from zero, a lost split adds a loss and
 * the featured unit clears them — which is a counter and not a game's rule.
 *
 * <p><b>Every wall in a list is equally likely.</b> That is what
 * {@link PullModel#drawWall} does, and the day a game draws its guarantee from a
 * weighted range this record needs weights before a client can be told it.
 *
 * @param walls          every guarantee a fresh cycle can draw; one for a fixed wall
 * @param firstWalls     the walls the cycle in hand can have drawn, given the
 *                       misses it already carries
 * @param featuredChance the chance a hit is the featured unit, by the losses
 *                       carried into it; the last entry holds for any more
 */
public record PullTables(List<Wall> walls, List<Integer> firstWalls, List<Double> featuredChance) {

    public PullTables {
        walls = List.copyOf(walls);
        firstWalls = List.copyOf(firstWalls);
        featuredChance = List.copyOf(featuredChance);
    }

    /**
     * One guarantee and the hit rate at each miss count under it.
     *
     * @param hitRates the chance the next pull hits, at {@code n} misses for
     *                 element {@code n}; the last is certainty, and a count past
     *                 it reads the last, as {@link PullModel#hitRateAt(int, int)}
     *                 clamps
     */
    public record Wall(int wall, List<Double> hitRates) {
        public Wall {
            hitRates = List.copyOf(hitRates);
        }
    }
}
