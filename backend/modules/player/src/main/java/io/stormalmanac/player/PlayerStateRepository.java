package io.stormalmanac.player;

import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.common.id.ProfileId;
import java.util.List;
import java.util.Optional;

/** The player module's only door. Unglamorous and load-bearing. */
public interface PlayerStateRepository {

    List<PlayerProfile> profilesOf(AccountId account);

    Optional<PlayerProfile> findProfile(ProfileId id);

    /**
     * Create a profile, or rename an existing one.
     *
     * <p>The caller owns the {@link ProfileId}, as it does for every other
     * aggregate here. Nothing in this module invents identity: a profile is
     * created by whoever holds the account that will own it.
     */
    void saveProfile(PlayerProfile profile);

    Inventory inventoryOf(ProfileId profile);

    Roster rosterOf(ProfileId profile);

    Goals goalsOf(ProfileId profile);

    void saveInventory(Inventory inventory);

    void saveRoster(Roster roster);

    void saveGoals(Goals goals);
}
