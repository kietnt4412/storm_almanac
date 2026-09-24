package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings.Redirects;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

/**
 * The redirect URI the application sends a provider is the public origin, not
 * the address the request reached.
 *
 * <p>Deployed, a browser talks to Vercel and Vercel rewrites {@code /api/*},
 * {@code /oauth2/*} and {@code /login/oauth2/*} to Render (B5, one origin). So
 * the request the application sees arrived at Render's own host, and the only
 * record of where the reader actually is travels in {@code X-Forwarded-*}
 * headers. Spring builds {@code {baseUrl}} in the redirect URI from the request;
 * without the forwarded headers honoured, it would ask Google to send the reader
 * back to Render over plain http — a URI no client registration lists, so the
 * first sign-in anybody ever attempts would be refused by the provider, on a
 * page this application never renders.
 *
 * <p>This is the half that can be proven here. <b>Whether Vercel and Render
 * actually deliver those headers is not</b> — that is a property of two hosts,
 * and is checked against the deployment itself the first time the exchange runs.
 *
 * <p>A client is registered with a made-up id because the authorization
 * redirect only exists when one is, and Google's endpoints are built into Boot,
 * so nothing is fetched and nothing leaves the machine.
 */
@TestPropertySource(properties = {
    "spring.security.oauth2.client.registration.google.client-id=forwarded-origin-test",
    "spring.security.oauth2.client.registration.google.client-secret=unused",
    "spring.security.oauth2.client.registration.google.scope=openid,profile,email",
})
class ForwardedOriginTest extends SharedDatabaseTest {

    @Autowired
    private TestRestTemplate following;

    @LocalServerPort
    private int port;

    private TestRestTemplate http;

    @BeforeEach
    void aBrowserThatStopsAtTheRedirect() {
        http = following.withRequestFactorySettings(settings -> settings.withRedirects(Redirects.DONT_FOLLOW));
    }

    @Test
    @DisplayName("behind the proxy, the provider is asked to return the reader to the public origin")
    void redirectUriIsThePublicOrigin() {
        HttpHeaders proxied = new HttpHeaders();
        proxied.set("X-Forwarded-Proto", "https");
        proxied.set("X-Forwarded-Host", "almanac.example");

        assertThat(redirectUriSentToTheProvider(proxied))
                .isEqualTo("https://almanac.example/login/oauth2/code/google");
    }

    @Test
    @DisplayName("with no proxy in front, the redirect URI is the address the request reached")
    void nothingIsInventedWithoutAProxy() {
        // The other direction: an origin configured into the application rather
        // than read off the request would pass the test above and break every
        // local sign-in.
        assertThat(redirectUriSentToTheProvider(new HttpHeaders()))
                .isEqualTo("http://localhost:" + port + "/login/oauth2/code/google");
    }

    private String redirectUriSentToTheProvider(HttpHeaders headers) {
        ResponseEntity<String> response = http.exchange(
                "/oauth2/authorization/google", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        String encoded = UriComponentsBuilder.fromUri(response.getHeaders().getLocation())
                .build(true)
                .getQueryParams()
                .getFirst("redirect_uri");
        return UriUtils.decode(encoded, StandardCharsets.UTF_8);
    }
}
