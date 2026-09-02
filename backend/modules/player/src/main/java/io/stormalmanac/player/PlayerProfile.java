package io.stormalmanac.player;

import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ProfileId;

/**
 * One account on one game, in one region. People run several — a main, an alt,
 * a second region — so this is a first-class entity rather than a column on the
 * user, and every plan, inventory and goal hangs off it.
 */
public record PlayerProfile(ProfileId id, AccountId owner, GameId game, String displayName, String region) {}
