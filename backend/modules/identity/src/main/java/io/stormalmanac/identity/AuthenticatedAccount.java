package io.stormalmanac.identity;

import io.stormalmanac.common.id.AccountId;

/**
 * What the rest of the application is allowed to know about whoever is making
 * this request: which account they are, and nothing else.
 *
 * <p>The principal Spring Security holds is a provider's user object — an
 * {@code OidcUser} full of Google's claims, or an {@code OAuth2User} full of
 * Discord's JSON. Handing that to a controller would put a provider's field
 * names into the code that decides who may read an inventory, which is both a
 * game of whack-a-mole across providers and the wrong place to be wrong. Every
 * principal this application mints implements this interface, and
 * {@link CurrentAccount} is the only thing that unwraps one.
 */
public interface AuthenticatedAccount {

    AccountId accountId();
}
