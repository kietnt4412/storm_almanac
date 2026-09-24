package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.stormalmanac.identity.ReturnAfterSignIn;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

/**
 * A reader who signs in from a page lands back on that page (Q6).
 *
 * <p>The flow has two ends and a provider in the middle. The first end is real
 * here: {@code /oauth2/authorization/google?then=…} goes through the application's
 * own filter chain. The provider is not — completing it would exchange a code
 * with Google — so the second end is {@link ReturnAfterSignIn} handed the same
 * session, and a separate test proves it is the handler the chain actually ends
 * a sign-in with. Between them that is the whole flow except Google.
 *
 * <p>Nothing here names the session attribute: every test goes in through the
 * sign-in URL and comes out through the redirect, so a change of key cannot
 * leave a test passing against a flow that no longer meets in the middle.
 */
@TestPropertySource(properties = {
    "spring.security.oauth2.client.registration.google.client-id=return-after-sign-in-test",
    "spring.security.oauth2.client.registration.google.client-secret=unused",
    "spring.security.oauth2.client.registration.google.scope=openid,profile,email",
})
class ReturnAfterSignInTest extends SharedDatabaseTest {

    private static final String READING = "/catalog/punishing-gray-raven/lucia-inverse-crown";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ClientRegistrationRepository registrations;

    @Autowired
    private List<SecurityFilterChain> chains;

    @Test
    @DisplayName("a sign-in started from a page ends on that page")
    void returnsToThePage() throws Exception {
        MockHttpSession session = startSignIn(READING, new MockHttpSession());

        assertThat(finishSignIn(session)).isEqualTo(READING);
    }

    @Test
    @DisplayName("a sign-in started with no page ends where it always did, on /")
    void noPageEndsOnRoot() throws Exception {
        MockHttpSession session = startSignIn(null, new MockHttpSession());

        assertThat(finishSignIn(session)).isEqualTo("/");
    }

    @Test
    @DisplayName("a destination off this site is refused, and the reader lands on /")
    void foreignDestinationsAreRefused() throws Exception {
        for (String foreign : List.of(
                "https://attacker.example/", "//attacker.example/", "/\\attacker.example/", "catalog")) {
            MockHttpSession session = startSignIn(foreign, new MockHttpSession());

            assertThat(finishSignIn(session)).as(foreign).isEqualTo("/");
        }
    }

    @Test
    @DisplayName("an abandoned sign-in's page is not inherited by the next one")
    void abandonedDestinationIsForgotten() throws Exception {
        // The reader clicks sign-in on a character page, backs out at Google,
        // and later signs in from the header on /. They should land on /.
        MockHttpSession session = startSignIn(READING, new MockHttpSession());
        startSignIn(null, session);

        assertThat(finishSignIn(session)).isEqualTo("/");
    }

    @Test
    @DisplayName("the page is used once: a second sign-in in the same session does not return to it")
    void destinationIsUsedOnce() throws Exception {
        MockHttpSession session = startSignIn(READING, new MockHttpSession());
        finishSignIn(session);

        assertThat(finishSignIn(session)).isEqualTo("/");
    }

    @Test
    @DisplayName("the application's sign-in ends with this handler, not Spring's default")
    void theChainUsesIt() {
        // The tests above hand the session to a ReturnAfterSignIn of their own,
        // because the real one is reached only through Google. This is the
        // half that proves the real one is the same class.
        Object handler = chains.stream()
                .flatMap(chain -> chain.getFilters().stream())
                .filter(OAuth2LoginAuthenticationFilter.class::isInstance)
                .map(filter -> ReflectionTestUtils.getField(filter, "successHandler"))
                .findFirst()
                .orElseThrow();

        assertThat(handler).isInstanceOf(ReturnAfterSignIn.class);
    }

    private MockHttpSession startSignIn(String then, MockHttpSession session) throws Exception {
        var request = get("/oauth2/authorization/google").session(session);
        if (then != null) {
            request.param("then", then);
        }
        mvc.perform(request).andExpect(status().is3xxRedirection());
        return session;
    }

    private String finishSignIn(MockHttpSession session) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();

        new ReturnAfterSignIn(registrations)
                .onAuthenticationSuccess(request, response, new TestingAuthenticationToken("reader", null));

        return response.getRedirectedUrl();
    }
}
