package io.stormalmanac.gamedata;

import io.stormalmanac.common.id.ItemId;

/**
 * Anything that can sit in an inventory: material, currency, or a copy of a
 * character. A copy is an item because Resonance and Memory enhancement consume
 * them, which makes the same thing a resource and a sink at once.
 *
 * @param category free-form and game-supplied ("insight", "resonance-material"),
 *                 used for grouping in the UI and for fodder classes in the model
 */
public record Item(ItemId id, String displayName, Rarity rarity, String category) {}
