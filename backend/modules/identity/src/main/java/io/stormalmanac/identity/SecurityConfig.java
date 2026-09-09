package io.stormalmanac.identity;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * The application's single filter chain.
 *
 * <p>This module is the only one that depends on Spring Security, so the chain
 * lives here rather than in {@code app}. It exists from phase 0 because Boot's
 * default auto-configuration secures <em>everything</em> the moment the starter
 * is on the classpath: without this class {@code GET /api/health} answers 401
 * and the deploy smoke test fails. That is invisible to a unit test that calls
 * the controller method directly, and was found by booting the container.
 *
 * <p>Phase 3 replaced the deny-by-default half with OAuth2 login. The public
 * half stays public: an unauthenticated liveness probe is the point of it.
 *
 * <h2>Login is conditional, and that is deliberate</h2>
 *
 * <p>{@code oauth2Login} is only installed when a {@link ClientRegistrationRepository}
 * exists, which is to say when someone has configured a provider's client id and
 * secret. No secrets exist: nothing is deployed (see D1 in TRACKER.md), and a
 * client registration is issued against a redirect URI, which needs a URL. Wiring
 * login unconditionally would mean the application refuses to start anywhere it
 * has not been given credentials — including in every test and on every
 * developer's machine — so the chain is assembled around what is configured
 * rather than around what is intended.
 *
 * <p>The half that is <em>not</em> conditional is the authorization: every route
 * outside the public list is denied without an authenticated account whether or
 * not a provider is configured. An unconfigured deployment serves the public
 * catalog and refuses everything else, which is the correct behaviour for one.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Read rather than hardcoded, so moving the actuator does not silently un-probe it. */
    private final String actuatorHealth;

    public SecurityConfig(@Value("${management.endpoints.web.base-path:/actuator}") String actuatorBasePath) {
        this.actuatorHealth = actuatorBasePath + "/health/**";
    }

    /**
     * Public by design:
     *
     * <ul>
     *   <li>{@code /api/health} — phase 0's deliverable, smoked by the pipeline
     *       against the deployed URL with no credentials to offer.
     *   <li>the actuator health group — container and orchestrator probes.
     *   <li>{@code GET /api/games/**} — published game data. Reference numbers,
     *       attributed, that a stranger arriving from a search reads without an
     *       account; the funnel does not survive a login wall. Read-only by
     *       method, not merely by convention: publishing is a human approval
     *       through {@code gamedata-cli} and has no endpoint, so a POST here is
     *       denied rather than 405'd.
     * </ul>
     *
     * Everything else is denied until phase 3 gives it an identity to check.
     * Each public route is listed here one at a time on purpose — the matcher is
     * the list of things this application will answer to a stranger, and it
     * should be short enough to read.
     */
    @Bean
    SecurityFilterChain apiSecurity(
            HttpSecurity http,
            ObjectProvider<ClientRegistrationRepository> registrations,
            ObjectProvider<OAuth2UserService<OidcUserRequest, OidcUser>> oidcUsers,
            ObjectProvider<OAuth2UserService<OAuth2UserRequest, OAuth2User>> oauth2Users)
            throws Exception {

        http.authorizeHttpRequests(auth -> auth
                        // Boot renders every unhandled status by forwarding to
                        // /error, and until that path was permitted the forward
                        // hit anyRequest().authenticated() and came back 401. So
                        // an anonymous caller who mistyped a public URL was told
                        // "unauthorized" — which a client reasonably reads as a
                        // dead session, and acts on by sending the reader to a
                        // login page. Found by serving the frontend and probing
                        // the routes a client would actually ask for; every test
                        // in the suite passed without it, because every test
                        // asked for a path that exists.
                        //
                        // Permitting it leaks nothing. The status and body of an
                        // error forward come from the original request, which was
                        // authorized on its own terms, so a 401 stays a 401 — and
                        // a request that asks for /error directly gets Boot's
                        // generic body with nothing in it.
                        //
                        // The precise form of this is a DispatcherType.ERROR
                        // matcher, which needs the servlet API on the compile
                        // path. This module has security and no web starter, and
                        // pulling one in for one enum is a worse trade than
                        // naming the path.
                        .requestMatchers("/error")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/games/**")
                        .permitAll()
                        .requestMatchers("/api/health", actuatorHealth)
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                // A JSON API answers 401; it does not redirect a probe to a
                // login form, and basic auth would pop a browser dialog.
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // Was STATELESS, because an anonymous health check every ten
                // seconds would otherwise accumulate a JSESSIONID forever. It
                // still does not: IF_REQUIRED mints a session when something
                // needs one, and a permitAll probe never does. What changed is
                // that a signed-in browser now needs one.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .csrf(browserCsrf());

        // A second filter chain may exist ahead of this one — the development
        // sign-in declares its own, in a jar the deployable artifact does not
        // contain. This method does not know about it and must not: what makes
        // that safe is the artifact, not a condition here.

        if (registrations.getIfAvailable() != null) {
            http.oauth2Login(login -> login.userInfoEndpoint(userInfo -> {
                oidcUsers.ifAvailable(userInfo::oidcUserService);
                oauth2Users.ifAvailable(userInfo::userService);
            }));
        }

        return http.build();
    }

    /**
     * CSRF as a browser-based single-page application actually performs it.
     *
     * <p>The token goes in a cookie the page can read, which is what a separate
     * front end needs in order to echo it in a header; that is not a weakening —
     * an attacker's page cannot read a cookie from another origin, which is the
     * whole mechanism.
     *
     * <p><b>The second line is the one that took a real browser to find.</b>
     * Spring Security 6 loads the CSRF token lazily: the cookie is written only
     * on a response where something actually read the token, and nothing on a
     * plain {@code GET} does. A single-page application therefore reads
     * everything successfully, never receives an {@code XSRF-TOKEN} cookie, and
     * has its first write refused — which presents as a broken session rather
     * than as a missing header, on the first save a new user ever attempts.
     * Setting the request attribute name to null opts out of the deferred load,
     * so the token is resolved on every request and the cookie is there before
     * it is needed.
     *
     * <p>It stayed invisible for two phases because every test of a write used
     * MockMvc's {@code csrf()} post-processor, which hands the request the token
     * production had not issued. {@code DevSignInTest} performs both halves the
     * way a browser does, over a socket, and is where this is now pinned.
     *
     * <p>Shared rather than copied because the development sign-in declares a
     * filter chain of its own and a browser meets both. Two spellings of one
     * policy is how the two drift apart, and the drift would show up as a cookie
     * that exists in one half of a flow.
     */
    public static Customizer<CsrfConfigurer<HttpSecurity>> browserCsrf() {
        CsrfTokenRequestAttributeHandler eager = new CsrfTokenRequestAttributeHandler();
        eager.setCsrfRequestAttributeName(null);
        return csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(eager);
    }
}
