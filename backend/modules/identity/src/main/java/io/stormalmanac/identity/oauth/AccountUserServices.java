package io.stormalmanac.identity.oauth;

import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.identity.Account;
import io.stormalmanac.identity.AccountRepository;
import io.stormalmanac.identity.AuthenticatedAccount;
import io.stormalmanac.identity.SignIn;
import java.util.Collection;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * Turns "this person authenticated with Google" into "this person is account
 * {@code 7f3a…}", at the one moment when both facts are in the same place.
 *
 * <p>Sign-in is the only event that carries a provider subject, so it is the
 * only place an account can be created. Doing it here rather than in a success
 * handler matters for a reason that is easy to miss: by the time a handler runs,
 * the principal is already in the security context, and a request arriving on
 * that session would find a principal that cannot name its account. Creating the
 * account while the principal is being built means there is no window in which
 * an authenticated request has no account behind it.
 *
 * <p>Two services because there are two protocols. Google is OIDC and its
 * principal must be an {@code OidcUser}; Discord is plain OAuth2 and has no id
 * token to be one with. Everything that differs between them is in
 * {@link SignIn#from}, which is a pure function over the attribute map and is
 * tested as one — what is left here is two wrappers and the delegation Spring
 * requires.
 */
@Configuration
public class AccountUserServices {

    private final AccountRepository accounts;

    public AccountUserServices(AccountRepository accounts) {
        this.accounts = accounts;
    }

    /** Google, and any other OIDC provider: the principal carries an id token. */
    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> accountOidcUserService() {
        OidcUserService delegate = new OidcUserService();
        return request -> {
            OidcUser user = delegate.loadUser(request);
            return new AccountOidcUser(user, register(registrationId(request), user.getAttributes()));
        };
    }

    /** Discord, which is OAuth2 without the OIDC half. */
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> accountOAuth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return request -> {
            OAuth2User user = delegate.loadUser(request);
            return new AccountOAuth2User(user, register(request.getClientRegistration().getRegistrationId(), user.getAttributes()));
        };
    }

    private static String registrationId(OidcUserRequest request) {
        return request.getClientRegistration().getRegistrationId();
    }

    private AccountId register(String provider, Map<String, Object> attributes) {
        SignIn signIn = SignIn.from(provider, attributes);
        Account account = accounts.upsertFromOidc(
                signIn.provider(), signIn.subject(), signIn.displayName(), signIn.email());
        return account.id();
    }

    /** An {@code OidcUser} that also knows which account it is. */
    public record AccountOidcUser(OidcUser delegate, AccountId accountId) implements OidcUser, AuthenticatedAccount {

        @Override
        public Map<String, Object> getClaims() {
            return delegate.getClaims();
        }

        @Override
        public OidcUserInfo getUserInfo() {
            return delegate.getUserInfo();
        }

        @Override
        public OidcIdToken getIdToken() {
            return delegate.getIdToken();
        }

        @Override
        public Map<String, Object> getAttributes() {
            return delegate.getAttributes();
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return delegate.getAuthorities();
        }

        /**
         * The account id, not the provider's name for this person.
         *
         * <p>This is what lands in the access log and in {@code Authentication}
         * dumps. A provider's display name is somebody's real name and does not
         * belong in either; the account id is meaningless to anyone who cannot
         * already read the database.
         */
        @Override
        public String getName() {
            return accountId.value();
        }
    }

    /** The same, for a provider with no id token. */
    public record AccountOAuth2User(OAuth2User delegate, AccountId accountId) implements OAuth2User, AuthenticatedAccount {

        @Override
        public Map<String, Object> getAttributes() {
            return delegate.getAttributes();
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return delegate.getAuthorities();
        }

        @Override
        public String getName() {
            return accountId.value();
        }
    }
}
