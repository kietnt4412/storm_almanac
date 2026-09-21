package io.stormalmanac.gamedata;

import io.stormalmanac.common.id.GameId;

/**
 * A title. Everything game-specific lives in data hanging off this record;
 * nothing game-specific lives in planner, gacha or stats.
 *
 * @param energyUnit  what the game calls the stamina it gates farming with —
 *                    "Activity" in Reverse: 1999, "Serum" in Punishing: Gray Raven
 * @param dayBoundary when this game's day rolls over, or null when the bundle
 *                    does not say. Null is not "midnight UTC" — it is
 *                    <em>unstated</em>, and the two have to stay distinguishable
 *                    so that a version written before anybody read a reset time
 *                    round-trips as what it actually claimed. What to plan by is
 *                    {@link #dayBoundaryOrDefault()}
 */
public record Game(GameId id, String displayName, String energyUnit, DayBoundary dayBoundary) {

    /** A title whose reset hour nobody has read yet. */
    public Game(GameId id, String displayName, String energyUnit) {
        this(id, displayName, energyUnit, null);
    }

    /**
     * The boundary to plan by: what the bundle declared, or the placeholder
     * every plan used before a bundle could declare one.
     *
     * <p>Read this rather than the component. The fallback belongs here, once,
     * and not at each call site, because a call site that forgot it would plan a
     * 05:00 game as a midnight one and nothing would look wrong.
     */
    public DayBoundary dayBoundaryOrDefault() {
        return dayBoundary == null ? DayBoundary.UTC_MIDNIGHT : dayBoundary;
    }
}
