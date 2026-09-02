package io.stormalmanac.player;

import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.common.id.ProfileId;
import java.util.List;
import java.util.Optional;

/** The player module's only door. Unglamorous and load-bearing. */
public interface PlayerStateRepository {

    List<PlayerProfile> profilesOf(AccountId account);

    Optional<PlayerProfile> findProfile(ProfileId id);

    Inventory inventoryOf(ProfileId profile);

    Roster rosterOf(ProfileId profile);

    Goals goalsOf(ProfileId profile);

    void saveInventory(Inventory inventory);

    void saveRoster(Roster roster);

    void saveGoals(Goals goals);
}
