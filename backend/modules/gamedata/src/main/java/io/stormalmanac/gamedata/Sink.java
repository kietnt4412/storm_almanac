package io.stormalmanac.gamedata;

import java.util.List;

/**
 * How an item leaves an account. Sealed for the same reason {@link Source} is.
 */
public sealed interface Sink permits Upgrade, Fodder {

    String id();

    /** What one application of this sink costs. */
    List<ItemStack> costs();
}
