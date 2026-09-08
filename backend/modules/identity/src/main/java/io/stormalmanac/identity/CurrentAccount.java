package io.stormalmanac.identity;

import io.stormalmanac.common.id.AccountId;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Who is asking.
 *
 * <p>One place unwraps the security context, and everything that needs to know
 * whose data it is holding asks here. The alternative — each controller casting
 * the principal itself — spreads a cast across the exact set of methods where
 * getting it wrong means serving one person another person's inventory.
 *
 * <p>{@link #require()} throws rather than returning a default. There is no
 * anonymous account and no "public" profile: a route that reaches this class
 * without an authenticated caller is a route the filter chain should have
 * refused, and turning that into a null or an empty account id would convert a
 * misconfiguration into silently reading somebody's data as nobody.
 */
@Component
public class CurrentAccount {

    public Optional<AccountId> find() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return authentication.getPrincipal() instanceof AuthenticatedAccount account
                ? Optional.of(account.accountId())
                : Optional.empty();
    }

    public AccountId require() {
        return find().orElseThrow(() -> new NotSignedInException(
                "this request reached an account-scoped route without an account behind it"));
    }

    /** Answered as 401 at the API edge. */
    public static class NotSignedInException extends RuntimeException {
        public NotSignedInException(String message) {
            super(message);
        }
    }
}
