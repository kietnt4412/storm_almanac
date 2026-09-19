package io.stormalmanac.gamedata;

/**
 * An amount of something an upgrade needs that is not an item: character EXP,
 * weapon EXP, anything a game fills by feeding other things into it.
 *
 * <p>It is not an {@link ItemStack} because nothing holds it. No inventory has
 * a stack of EXP, and a player cannot be short of it the way they are short of
 * a material. They can only be short of the fodder that makes it. So it is
 * paid by the {@link Fodder} rules whose {@link Fodder#progress()} names the
 * same kind, and by nothing else.
 *
 * @param kind     an opaque label the bundle supplies, matched against
 *                 {@link Fodder#progress()} and never interpreted
 * @param quantity how much of it the upgrade needs
 */
public record Progress(String kind, int quantity) {

    public Progress {
        if (kind == null || kind.isBlank()) throw new IllegalArgumentException("progress kind must not be blank");
        if (quantity < 1) throw new IllegalArgumentException("progress quantity must be >= 1");
    }
}
