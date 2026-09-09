/**
 * A sign-in that exists so the signed-in half of the product can be built, and
 * that cannot be turned on in production because it is not there.
 *
 * <h2>The problem this solves</h2>
 *
 * <p>Everything the player-facing product does lives behind {@code /api/me}, and
 * nothing can reach it: no OAuth provider is configured, because a client
 * registration is issued against a redirect URI and there is no deployed URL to
 * register one against. So there is no login URL, every {@code /api/me} route
 * answers 401, and the whole signed-in half of the frontend has nothing to
 * develop against. That is N24 in TRACKER.md.
 *
 * <h2>Why a Gradle module and not a Spring profile</h2>
 *
 * <p>A back door that mints a session for anyone who asks is the most dangerous
 * thing that can be added to this codebase, and every configuration-shaped guard
 * for one fails the same way: a profile that is not activated, an
 * {@code @ConditionalOnProperty} whose property is set, an environment variable
 * copied from a developer's notes into a deployment. Each of those is one
 * mistake away from a public service where a URL is an account.
 *
 * <p>So the guard is not configuration. This code is compiled into its own jar,
 * {@code :app} depends on it through Gradle's {@code testAndDevelopmentOnly}
 * configuration, and the Spring Boot plugin excludes that configuration from
 * {@code bootJar}. The deployable artifact does not contain these classes, so no
 * property, profile or environment variable can reach them —
 * {@code DeployableJarTest} opens {@code storm-almanac.jar} and asserts both
 * halves of that: that this module is absent, and that a module which
 * <em>should</em> be there is present, so the assertion cannot pass by looking
 * in the wrong place.
 *
 * <p>The same reasoning runs through the frontend: the sign-in link is behind
 * Vite's {@code import.meta.env.DEV}, which is substituted at build time, so a
 * production bundle does not contain the URL either.
 *
 * <h2>What it does not prove</h2>
 *
 * <p><b>This is not a substitute for the real thing.</b> No token has ever been
 * exchanged with a provider, and nothing here exercises that: it builds a
 * principal directly. What it does exercise is everything downstream of one —
 * the account row, the session, the filter chain, {@code CurrentAccount},
 * {@code OwnedProfiles} and every authorization rule — because the principal it
 * builds is an {@code AuthenticatedAccount} like any other, created through the
 * same {@code AccountRepository} call the OAuth user services make. The token
 * exchange is still owed, and it lands with the first deployment (B5).
 */
package io.stormalmanac.devsignin;
