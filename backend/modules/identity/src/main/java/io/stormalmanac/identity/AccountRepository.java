package io.stormalmanac.identity;

import io.stormalmanac.common.id.AccountId;
import java.util.Optional;

public interface AccountRepository {

    Optional<Account> find(AccountId id);

    /**
     * @param provider "google" or "discord"
     * @param subject  the provider's stable subject claim, never the email
     * @param pictureUrl the provider's picture, or null; refreshed like the name
     */
    Account upsertFromOidc(String provider, String subject, String displayName, String email, String pictureUrl);

    /** A sign-in with no picture, which is every caller but a real provider's. */
    default Account upsertFromOidc(String provider, String subject, String displayName, String email) {
        return upsertFromOidc(provider, subject, displayName, email, null);
    }
}
