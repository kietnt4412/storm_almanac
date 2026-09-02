package io.stormalmanac.gamedata;

import java.util.List;

/**
 * How an item enters an account. Sealed, because the optimizer switches over
 * exactly these four shapes and a fifth one must not be added quietly.
 *
 * <ul>
 *   <li>{@link Stage}  — costs energy, yields a probability distribution
 *   <li>{@link Craft}  — deterministic conversion, no energy
 *   <li>{@link Shop}   — costs currency, capped per period
 *   <li>{@link Reward} — free, capped by cadence, may expire
 * </ul>
 */
public sealed interface Source permits Stage, Craft, Shop, Reward {

    String id();

    Availability availability();

    /** What one use of this source can produce. Used for pre-solve reachability pruning. */
    List<ItemStack> potentialOutput();
}
