package io.stormalmanac.gamedata.diff;

/**
 * Which kind of data a change touches.
 *
 * <p>The reason this exists is the observation the whole version model is built
 * on: a balance patch moves combat multipliers without touching a single
 * material cost, and a content patch does the reverse. A diff that mixed them
 * would bury the one a given reader came for — a planner does not care that a
 * skill multiplier moved, and a catalog page does not care that a stage's drop
 * rate did.
 *
 * <p>There are three rather than the two the plan names. Banners are neither
 * material costs nor combat numbers, and filing them under either would be a
 * lie told to make the enum smaller.
 */
public enum Axis {

    /** Items, stages, drops, crafts, shops, rewards, upgrades, fodder. */
    PROGRESSION,

    /** Entities and everything hanging off them: stat curves, skills, talents. */
    CATALOG,

    /** Banners: rates, pity, floors, featured rules. */
    GACHA
}
