package io.stormalmanac.devsignin;

import io.stormalmanac.identity.SecurityConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * A second filter chain, matching {@code /dev/**} and nothing else.
 *
 * <p><b>Why a separate chain rather than a line in {@code SecurityConfig}.</b>
 * That class is the list of things this application will answer to a stranger,
 * and it is meant to be short enough to read. A {@code permitAll} for
 * {@code /dev/**} sitting in it would be a permanent invitation to wonder
 * whether the guard is still real — and it would be a route the production
 * artifact publishes and cannot serve. Spring Security composes chains, so the
 * development one can be declared entirely here, in the jar that production does
 * not contain: {@code SecurityConfig} does not know this exists, has no hook for
 * it, and needs no change when it comes or goes.
 *
 * <p><b>Ordering.</b> This chain carries a {@code securityMatcher} and
 * {@code @Order(1)}; the product's chain matches every request and is
 * unannotated, which Spring reads as lowest precedence. A matched chain must
 * come before the catch-all or it would never be consulted.
 */
@Configuration
public class DevSignInSecurity {

    private static final Logger log = LoggerFactory.getLogger(DevSignInSecurity.class);

    @Bean
    @Order(1)
    SecurityFilterChain developmentSignInChain(HttpSecurity http) throws Exception {
        http.securityMatcher(DevSignInController.BASE + "/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // The product's CSRF policy, not a relaxed one, and shared with
                // it rather than restated. Both endpoints here are GETs, so
                // protection is not what this buys: what it buys is the
                // XSRF-TOKEN cookie landing on the sign-in response, which is
                // where a browser gets it in production too — the OAuth callback
                // that ends a real sign-in comes back through the product's
                // chain. A development sign-in that skipped it would hand the
                // frontend a session it could read with and not write with, and
                // the difference would be blamed on the frontend.
                .csrf(SecurityConfig.browserCsrf())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                // The point of the endpoint is to leave a session behind.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
        return http.build();
    }

    /**
     * Said out loud at every startup that has it.
     *
     * <p>The exclusion from the deployable jar is what makes this safe, and a
     * log line is not a second guard — it is so that a developer reading a
     * console, or a stranger reading somebody's pasted log, can tell in one line
     * which of the two worlds they are looking at.
     */
    @PostConstruct
    void announce() {
        log.warn(
                "development sign-in is installed at {}/sign-in?as=<name> — this module is excluded from "
                        + "storm-almanac.jar, so a deployed build cannot have it",
                DevSignInController.BASE);
    }
}
