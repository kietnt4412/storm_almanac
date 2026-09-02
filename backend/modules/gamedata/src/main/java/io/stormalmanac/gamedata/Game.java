package io.stormalmanac.gamedata;

import io.stormalmanac.common.id.GameId;

/**
 * A title. Everything game-specific lives in data hanging off this record;
 * nothing game-specific lives in planner, gacha or stats.
 *
 * @param energyUnit what the game calls the stamina it gates farming with —
 *                   "Activity" in Reverse: 1999, "Serum" in Punishing: Gray Raven
 */
public record Game(GameId id, String displayName, String energyUnit) {}
