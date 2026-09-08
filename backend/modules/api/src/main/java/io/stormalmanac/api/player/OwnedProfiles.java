package io.stormalmanac.api.player;

import io.stormalmanac.api.ResourceNotFoundException;
import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.identity.CurrentAccount;
import io.stormalmanac.player.PlayerProfile;
import io.stormalmanac.player.PlayerStateRepository;
import org.springframework.stereotype.Component;

/**
 * The one place that decides whether the caller may touch a profile.
 *
 * <p>Every account-scoped route goes through {@link #require} before it reads or
 * writes anything, and it is a single method for the same reason
 * {@link CurrentAccount} is: this is the check whose absence hands one player
 * another player's inventory, and a check that is copied into eight controller
 * methods is a check that will be missing from the ninth.
 *
 * <p><b>Somebody else's profile is 404, not 403.</b> A 403 confirms the profile
 * exists, which turns a guessable id into an oracle for enumerating them.
 * "Not found" is also true from the caller's position: profiles they do not own
 * are not part of the resource tree they can address.
 */
@Component
public class OwnedProfiles {

    private final PlayerStateRepository players;
    private final CurrentAccount currentAccount;

    public OwnedProfiles(PlayerStateRepository players, CurrentAccount currentAccount) {
        this.players = players;
        this.currentAccount = currentAccount;
    }

    public AccountId account() {
        return currentAccount.require();
    }

    public PlayerProfile require(String profileId) {
        AccountId account = account();
        ProfileId id = ProfileId.of(profileId);
        return players.findProfile(id)
                .filter(profile -> profile.owner().equals(account))
                .orElseThrow(() -> new ResourceNotFoundException("no profile " + profileId));
    }
}
