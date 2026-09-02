package io.stormalmanac.common.id;

/**
 * Every identifier in the domain is a typed wrapper around a stable string slug,
 * never a bare {@code String} and never a database sequence.
 *
 * <p>Slugs come from the upstream game-data bundle, so they survive a re-import
 * and can appear in URLs. Typing them stops the entire class of bug where a
 * stage id is passed where an item id was expected.
 */
public sealed interface Identifier
        permits AccountId, BannerId, EntityId, GameId, ItemId, PlanId, ProfileId, StageId {

    String value();

    /** Shared validation for the record constructors below. */
    static String require(String value, String what) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(what + " must not be blank");
        }
        return value;
    }
}
