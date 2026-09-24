package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.identity.AccountRepository;
import io.stormalmanac.identity.oauth.AccountUserServices.AccountOidcUser;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;

/**
 * A reader can sign out, and signing out ends the session rather than hiding it.
 *
 * <p>Until B5 the only sign-out this application had ever had was the
 * development one, {@code /dev/sign-out}, which the deployable jar does not
 * contain — so the deployed product had no way out at all, and the frontend
 * rendered no link for it. Spring Security's own {@code POST /logout} was there
 * all along and answered with a redirect to {@code /login?logout}, a page this
 * application has never had. It now answers 204 and the page decides where to go.
 *
 * <p>The session is put in a real {@link MockHttpSession} rather than supplied
 * per request by {@code oidcLogin()}, because the claim is about the session: a
 * post-processor's principal would be there on the next request whatever
 * sign-out did.
 */
class SignOutTest extends SharedDatabaseTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AccountRepository accounts;

    @Test
    @DisplayName("signing out answers 204 and invalidates the session it was sent with")
    void signOutEndsTheSession() throws Exception {
        MockHttpSession session = signedInSession("sign-out-reader");
        mvc.perform(get("/api/me").session(session)).andExpect(status().isOk());

        mvc.perform(post("/logout").session(session).with(csrf())).andExpect(status().isNoContent());

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    @DisplayName("a sign-out without the CSRF token is refused, and the reader stays signed in")
    void signOutNeedsTheToken() throws Exception {
        // Otherwise any page on the web could sign a reader out with a form.
        MockHttpSession session = signedInSession("sign-out-forged");

        mvc.perform(post("/logout").session(session)).andExpect(status().isForbidden());

        assertThat(session.isInvalid()).isFalse();
        mvc.perform(get("/api/me").session(session)).andExpect(status().isOk());
    }

    private MockHttpSession signedInSession(String subject) {
        AccountId account = accounts
                .upsertFromOidc("google", subject, "Reader", subject + "@example.com")
                .id();
        DefaultOidcUser delegate = new DefaultOidcUser(
                List.of(),
                new OidcIdToken("token", Instant.now(), Instant.now().plusSeconds(3600), Map.of("sub", subject)));
        AccountOidcUser principal = new AccountOidcUser(delegate, account);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                new SecurityContextImpl(new OAuth2AuthenticationToken(principal, List.of(), "google")));
        return session;
    }
}
