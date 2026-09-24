package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings.Redirects;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.DefaultRedirectStrategy;
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
 * actually deliver those headers is not</b> — and measured against the
 * deployment on 2026-09-24, <b>they do not deliver the host</b>. Render passes
 * {@code X-Forwarded-Host} and {@code Forwarded} straight through to Spring, which
 * honours both; a request through Vercel still produced Render's own host, so it
 * is Vercel's rewrite that does not send it. The proto arrives. So production
 * pins the registration's {@code redirect-uri} to the public origin, and the
 * tests below guard the header path for any proxy that does send one.
 *
 * <p>The redirect <em>after</em> sign-in has the same problem and a different
 * fix: it is relative, so the browser resolves it against the origin the reader
 * is actually on, whatever this application believes its host to be.
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
@Import(ForwardedOriginTest.RedirectProbe.class)
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

    @Test
    @DisplayName("a redirect this application issues is relative, so it lands on the reader's origin")
    void redirectsAreRelative() {
        // Spring Security ends a successful sign-in with sendRedirect("/"), and
        // by default that became an absolute URL on the host this application
        // believes it is on — Render's, since Vercel does not forward the host.
        // The reader would finish signing in on a domain with no page and none
        // of their cookies. The probe redirects exactly the way the success
        // handler does, because the real one needs a provider to reach.
        //
        // With Render's headers, because they change the path taken: Boot's
        // ForwardedHeaderFilter wraps the response only when forwarded headers
        // are present, and its relative redirect answers 303 rather than 302 —
        // harmless, every redirect in these flows is a GET, but it is the path
        // production takes and a request without the headers would not.
        HttpHeaders render = new HttpHeaders();
        render.set("X-Forwarded-Proto", "https");
        render.set("X-Forwarded-For", "203.0.113.9");
        ResponseEntity<String> response =
                http.exchange(RedirectProbe.PATH, HttpMethod.GET, new HttpEntity<>(render), String.class);

        assertThat(response.getStatusCode().is3xxRedirection()).isTrue();
        assertThat(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo("/");
    }

    /**
     * A path that redirects the way {@code SavedRequestAwareAuthenticationSuccessHandler}
     * does, as a servlet filter ordered just after Boot's {@code ForwardedHeaderFilter}
     * — so it sees the same wrapped response the success handler sees, and needs
     * no hole in the security chain.
     *
     * <p><b>Deliberately carries no stereotype annotation</b> and arrives by
     * {@code @Import} on this class alone. {@code StormAlmanacApplication} declares
     * {@code @ComponentScan("io.stormalmanac")}, whose scan does not apply Boot's
     * filter that keeps test classes out: the first version of this probe was a
     * {@code @RestController} nested in a {@code @TestConfiguration}, and the
     * controller registered twice — once from its {@code @Bean}, once from the
     * scan. Anything stereotyped in the test sources is in every test context.
     */
    static class RedirectProbe {

        static final String PATH = "/test-only/redirect-probe";

        @Bean
        FilterRegistrationBean<Filter> redirectProbe() {
            FilterRegistrationBean<Filter> probe = new FilterRegistrationBean<>((request, response, chain) ->
                    new DefaultRedirectStrategy()
                            .sendRedirect((HttpServletRequest) request, (HttpServletResponse) response, "/"));
            probe.addUrlPatterns(PATH);
            probe.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
            return probe;
        }
    }

    private String redirectUriSentToTheProvider(HttpHeaders headers) {
        ResponseEntity<String> response = http.exchange(
                "/oauth2/authorization/google", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode().is3xxRedirection()).isTrue();
        String encoded = UriComponentsBuilder.fromUri(response.getHeaders().getLocation())
                .build(true)
                .getQueryParams()
                .getFirst("redirect_uri");
        return UriUtils.decode(encoded, StandardCharsets.UTF_8);
    }
}
