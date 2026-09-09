package io.stormalmanac.devsignin;

import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.identity.AuthenticatedAccount;
import java.security.Principal;

/**
 * The principal a development sign-in puts in the session.
 *
 * <p>It implements {@link AuthenticatedAccount} and nothing else that matters,
 * which is the point: {@code CurrentAccount} unwraps that interface and never
 * asks what is behind it, so every authorization rule in the application treats
 * this principal exactly as it treats one built from Google's claims. A
 * development principal that took a shortcut around that interface would be
 * developing the frontend against a security model the product does not have.
 *
 * @param accountId   the account this session is, and the only thing the rest of
 *                    the application is allowed to read
 * @param displayName what the developer typed, kept only so logs say who
 */
public record DevPrincipal(AccountId accountId, String displayName) implements AuthenticatedAccount, Principal {

    /**
     * The account id, not the display name — the same rule the OAuth principals
     * follow. What lands in an access log should be meaningless to anyone who
     * cannot already read the database.
     */
    @Override
    public String getName() {
        return accountId.value();
    }
}
