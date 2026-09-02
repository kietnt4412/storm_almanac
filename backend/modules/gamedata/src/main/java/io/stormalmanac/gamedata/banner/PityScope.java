package io.stormalmanac.gamedata.banner;

/**
 * Where pity state is carried.
 *
 * <p>Punishing: Gray Raven carries pity within a banner <em>type</em> only —
 * character pity does not help a weapon banner — which is the reason this is a
 * dimension of the model rather than a global counter on the profile.
 */
public enum PityScope { GLOBAL, BANNER_TYPE, BANNER }
