package io.stormalmanac.app;

import jakarta.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * A CSRF token for a MockMvc request, carried the way a browser carries it here:
 * an {@code XSRF-TOKEN} cookie and the same value echoed in {@code X-XSRF-TOKEN}.
 *
 * <p><b>Use this, never Spring Security's {@code csrf()} post-processor.</b> That
 * one does not only decorate its request. The first time it runs in an
 * application context it reaches into the product chain's {@code CsrfFilter} and
 * swaps its token repository for a session-backed test double, and the swap
 * outlives the request. In a {@code RANDOM_PORT} context the real server runs
 * through that same filter, so every socket test sharing the context afterwards
 * looks for its token in the session, finds none, and is refused a write it
 * made correctly. That is what failed {@code DevSignInTest}'s write on
 * 2026-09-29, the first time a class using {@code csrf()} sorted before it.
 * {@code DevSignInTest} also scans the test sources so it cannot come back.
 *
 * <p>This also means a MockMvc write now meets the real policy,
 * {@code SecurityConfig.browserCsrf()}, rather than a stand-in for it: the
 * cookie repository accepts any token whose cookie and header agree, which is
 * the double-submit check production makes.
 */
final class BrowserCsrf {

    private BrowserCsrf() {}

    static RequestPostProcessor csrf() {
        return request -> {
            String token = UUID.randomUUID().toString();
            List<Cookie> cookies = new ArrayList<>(
                    request.getCookies() == null ? List.of() : Arrays.asList(request.getCookies()));
            cookies.add(new Cookie("XSRF-TOKEN", token));
            request.setCookies(cookies.toArray(Cookie[]::new));
            request.addHeader("X-XSRF-TOKEN", token);
            return request;
        };
    }
}
