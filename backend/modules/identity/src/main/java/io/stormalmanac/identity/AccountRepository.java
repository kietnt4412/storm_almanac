package io.stormalmanac.identity;

import io.stormalmanac.common.id.AccountId;
import java.util.Optional;

public interface AccountRepository {

    Optional<Account> find(AccountId id);

    /**
     * @param provider "google" or "discord"
     * @param subject  the provider's stable subject claim, never the email
     */
    Account upsertFromOidc(String provider, String subject, String displayName, String email);
}
