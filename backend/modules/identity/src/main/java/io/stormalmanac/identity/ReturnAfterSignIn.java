package io.stormalmanac.identity;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 * Sends a reader back to the page they signed in from.
 *
 * <pre>
 * GET /oauth2/authorization/google?then=/catalog/punishing-gray-raven/lucia
 * </pre>
 *
 * <p>Until this existed every sign-in ended on {@code /}, wherever the reader
 * clicked it — the catalog page that told them what signing in would add
 * forgot them the moment they did (Q6). The development sign-in has taken a
 * {@code then} since phase 3; the provider's flow could not, because the
 * request that ends it is the provider's redirect back, which carries nothing
 * of ours but {@code state}.
 *
 * <p><b>So the destination is held in the session across the round trip.</b>
 * The authorization request is already stored there by Spring for the same
 * reason, and the session survives sign-in: session-fixation protection
 * changes its id and keeps its attributes. The two halves are one class
 * because they share one key and neither means anything without the other.
 *
 * <p>With no {@code then}, the success handler is Spring's own, unchanged —
 * the saved-request behaviour every sign-in had before.
 */
public final class ReturnAfterSignIn implements OAuth2AuthorizationRequestResolver, AuthenticationSuccessHandler {

    static final String PARAMETER = "then";

    static final String ATTRIBUTE = ReturnAfterSignIn.class.getName() + ".then";

    private final OAuth2AuthorizationRequestResolver authorization;
    private final AuthenticationSuccessHandler otherwise = new SavedRequestAwareAuthenticationSuccessHandler();
    private final RedirectStrategy redirect = new DefaultRedirectStrategy();

    public ReturnAfterSignIn(ClientRegistrationRepository registrations) {
        this.authorization = new DefaultOAuth2AuthorizationRequestResolver(
                registrations, OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return remember(request, authorization.resolve(request));
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return remember(request, authorization.resolve(request, clientRegistrationId));
    }

    /**
     * Only once the request is known to be a sign-in, so no other path can
     * write the attribute; and a sign-in without a destination clears one an
     * abandoned earlier attempt left behind.
     */
    private static OAuth2AuthorizationRequest remember(HttpServletRequest request, OAuth2AuthorizationRequest signIn) {
        if (signIn != null) {
            String then = request.getParameter(PARAMETER);
            if (then == null) {
                var session = request.getSession(false);
                if (session != null) {
                    session.removeAttribute(ATTRIBUTE);
                }
            } else {
                request.getSession().setAttribute(ATTRIBUTE, LocalDestination.of(then));
            }
        }
        return signIn;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        var session = request.getSession(false);
        Object then = session == null ? null : session.getAttribute(ATTRIBUTE);
        if (then == null) {
            otherwise.onAuthenticationSuccess(request, response, authentication);
            return;
        }
        session.removeAttribute(ATTRIBUTE);
        // What Spring's handler would have cleared: a failure from an earlier
        // attempt in the same session, which is no longer true.
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        // Checked again on the way out, not only on the way in: the session is
        // ours, but nothing else about a stored string promises where it came from.
        redirect.sendRedirect(request, response, LocalDestination.of(then.toString()));
    }
}
