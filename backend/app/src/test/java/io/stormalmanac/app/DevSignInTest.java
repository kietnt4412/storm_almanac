package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings.Redirects;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * <b>The first authenticated request this project has ever made over a socket.</b>
 *
 * <p>Every other test of the signed-in surface goes through MockMvc, and the
 * tracker has carried the reason as a known gap since phase 3: a session could
 * not be minted over real HTTP without an authorization server to redirect to,
 * so the one layer that caught phase 0's 401 — the servlet container — was the
 * one layer the authenticated tests skipped. The development sign-in removes
 * that obstacle, so this class takes the opposite of that step down: a real
 * port, a cookie jar kept by hand, and no Spring Security test post-processor
 * anywhere in it.
 *
 * <p><b>What it still does not prove.</b> No token has been exchanged with a
 * provider. What is exercised is everything a provider would hand off to: the
 * session cookie, the filter chain, CSRF as a browser actually performs it, and
 * the authorization rules underneath. The exchange itself lands with the first
 * deployment.
 *
 * <p>The cookie handling is deliberately by hand rather than through a client
 * that manages a jar. A browser sends {@code JSESSIONID} back and echoes the
 * CSRF cookie in a header, and writing those two moves out is what turned the
 * CSRF defect below from something a later session would hit into something this
 * one fixed.
 *
 * <p><b>The client must be told not to follow redirects</b>, and the injected
 * {@code TestRestTemplate} does follow them — on Boot 3.5 it resolves to a
 * {@code JdkClientHttpRequestFactory} whose default is to chase a 302. Left
 * alone, every assertion here would be made against whatever the redirect landed
 * on rather than against the sign-in: this class's first run reported
 * {@code 401} for a sign-in that had worked perfectly and redirected to a page
 * that needs a session the client had not yet been handed. A test of a redirect
 * has to be able to see the redirect.
 */
class DevSignInTest extends SharedDatabaseTest {

    @Autowired
    private TestRestTemplate following;

    @Autowired
    private ObjectMapper json;

    /** The same client, minus the redirect chasing. See the class note. */
    private TestRestTemplate http;

    @BeforeEach
    void aBrowserThatStopsAtTheRedirect() {
        http = following.withRequestFactorySettings(settings -> settings.withRedirects(Redirects.DONT_FOLLOW));
    }

    @Test
    @DisplayName("an anonymous browser is refused, signs in over HTTP, and is then itself")
    void signsInOverRealHttp() throws Exception {
        assertThat(http.getForEntity("/api/me", String.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<Void> signIn = http.exchange(
                "/dev/sign-in?as=vertin&then=/planner", HttpMethod.GET, HttpEntity.EMPTY, Void.class);

        // A redirect and a session, which is the shape an OAuth callback ends in.
        assertThat(signIn.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(signIn.getHeaders().getLocation()).hasToString("/planner");

        Cookies browser = Cookies.from(signIn);
        assertThat(browser.value("JSESSIONID")).isNotBlank();

        ResponseEntity<String> me = http.exchange(
                "/api/me", HttpMethod.GET, new HttpEntity<>(browser.headers()), String.class);
        assertThat(me.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode account = json.readTree(me.getBody());
        assertThat(account.get("displayName").asText()).isEqualTo("vertin");
        assertThat(account.get("accountId").asText()).isNotBlank();
        assertThat(account.get("profiles")).isEmpty();

        // The account is a real row created through the same repository call the
        // OAuth user services make, not a principal invented for the test.
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM identity.account_identity"
                                + " WHERE provider = 'dev' AND subject = 'vertin'",
                        Integer.class))
                .isEqualTo(1);
    }

    /**
     * <b>The defect this test was written to find.</b>
     *
     * <p>Spring Security 6 loads the CSRF token lazily: the cookie is written
     * only when something on the request actually reads the token, and nothing
     * on a plain {@code GET /api/me} does. So a single-page application does
     * every read successfully, has no {@code XSRF-TOKEN} cookie to echo, and its
     * first write is refused — which reads to a developer as a broken session
     * rather than a missing header. Nothing caught it before this session
     * because no browser had ever written anything: every previous test of a
     * write used MockMvc's {@code csrf()} post-processor, which supplies the
     * token production would not have handed out.
     *
     * <p>{@code SecurityConfig} opts out of the deferred load, so the cookie is
     * issued on the first response. This asserts the browser's half of the
     * contract end to end: given on the way out, echoed on the way back.
     */
    @Test
    @DisplayName("a signed-in browser can write, because it was given a CSRF token to echo")
    void aBrowserCanWrite() throws Exception {
        Cookies browser = Cookies.from(http.exchange(
                "/dev/sign-in?as=schneider", HttpMethod.GET, HttpEntity.EMPTY, Void.class));

        // Issued on the sign-in response itself, before any write is attempted —
        // that is the whole fix. A client should never have to make a throwaway
        // request to be given the token it needs.
        assertThat(browser.value("XSRF-TOKEN")).isNotBlank();

        HttpHeaders write = browser.headers();
        write.setContentType(MediaType.APPLICATION_JSON);
        write.set("X-XSRF-TOKEN", browser.value("XSRF-TOKEN"));

        ResponseEntity<String> created = http.exchange(
                "/api/me/profiles",
                HttpMethod.POST,
                new HttpEntity<>(
                        "{\"game\":\"proving-ground\",\"region\":\"global\",\"displayName\":\"Main\"}", write),
                String.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(json.readTree(created.getBody()).get("displayName").asText()).isEqualTo("Main");

        // And the same write without the header is still refused, so the
        // assertion above passes because of the token rather than in spite of
        // CSRF having been switched off somewhere.
        HttpHeaders unarmed = browser.headers();
        unarmed.setContentType(MediaType.APPLICATION_JSON);
        assertThat(http.exchange(
                                "/api/me/profiles",
                                HttpMethod.POST,
                                new HttpEntity<>("{\"game\":\"proving-ground\",\"region\":\"global\"}", unarmed),
                                String.class)
                        .getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("signing in as somebody else is a different account, not the same one renamed")
    void twoDevelopersAreTwoAccounts() throws Exception {
        String first = accountIdOf("vertin");
        String second = accountIdOf("sonetto");
        String again = accountIdOf("vertin");

        assertThat(second).isNotEqualTo(first);
        // Upserted on (provider, subject), so a developer's local inventory
        // survives a restart instead of arriving at a fresh empty account.
        assertThat(again).isEqualTo(first);
    }

    @Test
    @DisplayName("the sign-in redirect cannot be pointed off this application")
    void refusesAForeignRedirect() {
        for (String hostile : List.of("//evil.example", "https://evil.example", "/\\evil.example")) {
            ResponseEntity<Void> response = http.exchange(
                    "/dev/sign-in?as=vertin&then=" + hostile, HttpMethod.GET, HttpEntity.EMPTY, Void.class);
            assertThat(response.getHeaders().getLocation()).as(hostile).hasToString("/");
        }
    }

    @Test
    @DisplayName("signing out leaves a browser anonymous again")
    void signsOut() {
        Cookies browser = Cookies.from(
                http.exchange("/dev/sign-in?as=vertin", HttpMethod.GET, HttpEntity.EMPTY, Void.class));

        http.exchange("/dev/sign-out", HttpMethod.GET, new HttpEntity<>(browser.headers()), Void.class);

        assertThat(http.exchange("/api/me", HttpMethod.GET, new HttpEntity<>(browser.headers()), String.class)
                        .getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private String accountIdOf(String who) throws Exception {
        Cookies browser = Cookies.from(
                http.exchange("/dev/sign-in?as=" + who, HttpMethod.GET, HttpEntity.EMPTY, Void.class));
        ResponseEntity<String> me = http.exchange(
                "/api/me", HttpMethod.GET, new HttpEntity<>(browser.headers()), String.class);
        return json.readTree(me.getBody()).get("accountId").asText();
    }

    /** The cookie jar a browser keeps and {@code TestRestTemplate} does not. */
    private record Cookies(List<String> setCookie) {

        static Cookies from(ResponseEntity<?> response) {
            return new Cookies(
                    Objects.requireNonNullElse(response.getHeaders().get(HttpHeaders.SET_COOKIE), List.of()));
        }

        String value(String name) {
            return setCookie.stream()
                    .filter(cookie -> cookie.startsWith(name + "="))
                    .map(cookie -> cookie.substring(name.length() + 1).split(";", 2)[0])
                    .findFirst()
                    .orElse("");
        }

        HttpHeaders headers() {
            HttpHeaders headers = new HttpHeaders();
            setCookie.forEach(cookie -> headers.add(HttpHeaders.COOKIE, cookie.split(";", 2)[0]));
            return headers;
        }
    }
}
