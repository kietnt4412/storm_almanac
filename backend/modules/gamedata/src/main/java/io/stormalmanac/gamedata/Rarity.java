package io.stormalmanac.gamedata;

/**
 * Rarity is a slug plus an ordering, never an enum.
 *
 * <p>Reverse: 1999 counts stars, Punishing: Gray Raven uses letter grades. An
 * enum here would be the first game-specific branch, and the whole thesis is
 * that there isn't one.
 *
 * @param label how the game writes it, e.g. {@code "6*"} or {@code "S"}
 * @param rank  higher is rarer; only the ordering is meaningful
 */
public record Rarity(String label, int rank) implements Comparable<Rarity> {
    @Override
    public int compareTo(Rarity other) {
        return Integer.compare(rank, other.rank);
    }
}
