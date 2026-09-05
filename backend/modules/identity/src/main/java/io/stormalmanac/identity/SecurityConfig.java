package io.stormalmanac.identity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

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
 * <p>Phase 3 replaces the deny-by-default half with OAuth2 login. The public
 * half stays public: an unauthenticated liveness probe is the point of it.
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
     * </ul>
     *
     * Everything else is denied until phase 3 gives it an identity to check.
     */
    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth.requestMatchers("/api/health", actuatorHealth)
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                // A JSON API answers 401; it does not redirect a probe to a
                // login form, and basic auth would pop a browser dialog.
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // Without this every anonymous health check mints a JSESSIONID.
                // A probe every ten seconds would accumulate sessions forever.
                // Phase 3 revisits this when OAuth2 login needs a session.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // No browser-submitted state exists yet. Phase 3 re-enables this
                // for the cookie-authenticated surface it introduces.
                .csrf(csrf -> csrf.disable())
                .build();
    }
}
