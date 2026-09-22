package io.stormalmanac.gamedata;

/**
 * A name for one {@link Progress#kind()}, so that a demand line for it can be
 * read by a person.
 *
 * <p><b>This is the bundle's own word, not the game's, and it carries no
 * provenance.</b> A progress kind is already something the bundle invents — the
 * slug {@code character-exp} appears on no screen — and a name for it is the
 * same invention spelled for a reader. Punishing: Gray Raven is the case that
 * settles it: the character Level Up screen, the weapon Enhancement Cost picker
 * and the Memory Enhancement Cost picker all call their pool "EXP", so the
 * game's own labels would put three lines reading "EXP" in one plan and leave
 * the reader unable to tell which pool they are short of. The ambiguity is real
 * in the client and is not one this project has to reproduce.
 *
 * <p>Because it is not a reading, it declares no {@link FactRef} and no
 * {@code factProvenance} row, exactly as {@link Game#energyUnit()} and
 * {@link Game#dayBoundary()} declare none. It is flattened into the patch diff
 * all the same: a correction to it moves what every shortfall table says, and a
 * sequence that renamed a pool while reporting "no changes" would be the same
 * silence ADR 0025 closed for the day boundary.
 *
 * <p>Declaring one is optional and stays optional. A kind nobody has named
 * renders as its slug, which is what every version published before this record
 * existed does — those versions are immutable and have to stay readable.
 *
 * @param kind        the opaque label an {@link Upgrade}'s {@link Progress} and
 *                    a {@link Fodder} rule both name
 * @param displayName what to put in front of a reader instead
 */
public record ProgressKind(String kind, String displayName) {

    public ProgressKind {
        if (kind == null || kind.isBlank()) {
            throw new IllegalArgumentException("progress kind must not be blank");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException(
                    "progress kind '" + kind + "' must have a display name, or not be declared at all");
        }
    }
}
