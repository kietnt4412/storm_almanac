package io.stormalmanac.devsignin;

import io.stormalmanac.identity.Account;
import io.stormalmanac.identity.AccountRepository;
import io.stormalmanac.identity.LocalDestination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Signs a developer in as a named account, without a provider.
 *
 * <pre>
 * GET /dev/sign-in?as=vertin&amp;then=/
 * GET /dev/sign-out?then=/
 * </pre>
 *
 * <p><b>A GET, and deliberately.</b> In production the sign-in affordance is a
 * link the browser follows to {@code /oauth2/authorization/google}, and what
 * comes back is a redirect with a session attached. This is the same shape, so
 * the frontend's sign-in button is one URL different between the two worlds
 * rather than one mechanism different. The usual objection to a state-changing
 * GET — that a third party can cause it — is the objection to the OAuth
 * authorization endpoint too, and it is answered the same way: what it grants is
 * a session as whoever asked, not an action taken as somebody else.
 *
 * <p><b>The provider is {@code "dev"}.</b> An identity is
 * {@code (provider, subject)}, so a development account can never collide with a
 * real Google or Discord one, and any row that leaked into a real database would
 * be identifiable by a {@code WHERE provider = 'dev'}. It is created through
 * {@link AccountRepository#upsertFromOidc} — the same call the OAuth user
 * services make — so signing in twice as the same name lands on the same
 * account, which is what makes a locally edited inventory survive a restart.
 */
@RestController
@RequestMapping(DevSignInController.BASE)
public class DevSignInController {

    static final String BASE = "/dev";

    /** Never "google" or "discord": that is what keeps these rows distinguishable. */
    private static final String PROVIDER = "dev";

    private final AccountRepository accounts;
    private final SecurityContextRepository contexts = new HttpSessionSecurityContextRepository();

    public DevSignInController(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @GetMapping("/sign-in")
    public ResponseEntity<Void> signIn(
            @RequestParam(name = "as", defaultValue = "dev") String as,
            @RequestParam(name = "then", defaultValue = "/") String then,
            HttpServletRequest request,
            HttpServletResponse response) {

        String subject = as.trim();
        if (subject.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Account account = accounts.upsertFromOidc(
                PROVIDER, subject, subject, subject + "@dev.invalid");

        // A fresh session per sign-in. Reusing the one the browser already has
        // is session fixation, and while the attack does not matter on a laptop,
        // the behaviour does: switching accounts mid-session is exactly what
        // this endpoint is for, and carrying the old session across would leave
        // whatever the previous account cached attached to the new principal.
        HttpSession existing = request.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }

        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                new DevPrincipal(account.id(), subject),
                null,
                AuthorityUtils.createAuthorityList("ROLE_USER"));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        // Saving is explicit in Spring Security 6: nothing persists the context
        // for a request that was permitted rather than authenticated, and this
        // one is permitted. Without this line the sign-in appears to work and
        // the very next request is anonymous again.
        contexts.saveContext(context, request, response);

        return redirectTo(then);
    }

    @GetMapping("/sign-out")
    public ResponseEntity<Void> signOut(
            @RequestParam(name = "then", defaultValue = "/") String then, HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return redirectTo(then);
    }

    /**
     * Where the browser goes next, restricted to this application.
     *
     * <p>An unchecked {@code then} is an open redirect. This check was written
     * here first, refusing a foreign destination even on a local-only endpoint
     * because "the next endpoint that takes a redirect target may not be
     * development-only" — and the next one was not: the provider's sign-in took
     * a {@code then} for Q6. The check moved to {@link LocalDestination} so the
     * two cannot disagree about what counts as local.
     */
    private static ResponseEntity<Void> redirectTo(String then) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", LocalDestination.of(then))
                .build();
    }

    /** What a developer is offered when they ask what this endpoint is. */
    @GetMapping("/sign-in/help")
    public List<String> help() {
        return List.of(
                "GET /dev/sign-in?as=<name>&then=<path> — become <name>, creating the account if new",
                "GET /dev/sign-out?then=<path> — drop the session",
                "this endpoint is absent from storm-almanac.jar; see DeployableJarTest");
    }
}
