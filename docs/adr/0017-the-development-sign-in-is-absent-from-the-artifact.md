# 17. The development sign-in is absent from the artifact, not disabled in it

**Status:** Accepted · 2026-09-09 ·
Unblocks Phase 4's signed-in product (**N24**); does not replace the OAuth
exchange, which lands with the first deployment (**B5**).

## Context

Everything the product does for a player is behind `/api/me`, and nothing could
reach it. `SecurityConfig` installs `oauth2Login` only when a provider is
configured, no provider is configured, and none can be: a client registration is
issued against a redirect URI and there is no deployed URL to register one
against ([D1](../../TRACKER.md#d1--deployment-deferred-2026-09-02)). So there was
no login URL, every `/api/me` route answered 401, and Phase 4's inventory editor,
goal picker and plan view had nothing to be built against.

Two ways out, and the tracker was right that they are not equivalent. A real
provider needs the deploy and does finally run the token exchange that never has.
A development-only sign-in unblocks the interface now and proves nothing about
the exchange — and is **the more dangerous of the two**, because what it is, is a
back door in an authentication system.

That is the decision this record is about. Not whether to have one: how to make
having one safe.

## Decision

**The development sign-in is a Gradle module of its own, and the deployable jar
does not contain it.**

`:modules:identity-dev` holds a filter chain matching `/dev/**` and a controller
that mints a principal for a named account. `:app` depends on it through
`testAndDevelopmentOnly`, which the Spring Boot plugin puts on the classpath of
`bootRun` and of the tests and excludes from `bootJar`. `storm-almanac.jar` — the
artifact Docker builds and a host runs — has no such classes in it.

**Every configuration-shaped guard was rejected, and for one reason.** A Spring
profile is not activated until it is; `@ConditionalOnProperty` guards a property
until somebody sets it; an environment check reads an environment somebody
supplies. Each of those is one mistake away from a public service on which a URL
is an account, and each of those mistakes is the kind that gets made by copying a
working configuration from one place to another. Absence has no such failure
mode. There is nothing left to switch on.

**The guard is a build file line, so a test reads the artifact.**
`implementation` and `testAndDevelopmentOnly` differ by one word, the change
breaks nothing, and nothing else in the build would notice. `DeployableJarTest`
opens `storm-almanac.jar`, walks every nested dependency jar, and asserts no
class under `io/stormalmanac/devsignin/` is anywhere inside it. It looks *inside*
the nested jars rather than at their names, because moving the classes into
`:modules:identity` is the mistake most available to somebody who found the split
inconvenient. And it asserts the same scan finds `SecurityConfig` — an absence
test that looks in the wrong place passes silently forever.

**`SecurityConfig` does not know this exists.** Spring Security composes filter
chains, so the development one is declared entirely in the module production does
not ship: no `permitAll` for `/dev/**` in the product's matcher, no hook, no
condition, and nothing to change when it comes or goes. The product's chain is
meant to be short enough to read as *the list of things this application answers
to a stranger*, and a line about a back door in it would be a permanent invitation
to wonder whether the guard is still real.

**The frontend makes the same move.** The sign-in link is chosen behind
`import.meta.env.DEV`, which Vite substitutes at build time, so the development
URL is not merely unused in a production bundle — it is not in it.

**The principal is an ordinary one.** It implements `AuthenticatedAccount` and
nothing more, and the account is created through the same
`AccountRepository.upsertFromOidc` call the OAuth user services make, under
provider `dev` — an identity is `(provider, subject)`, so a development account
cannot collide with a real one and any row that ever reached a real database is a
`WHERE provider = 'dev'` away from being found. Everything downstream —
`CurrentAccount`, `OwnedProfiles`, every authorization rule — treats it exactly
as it treats a principal built from Google's claims. A shortcut around that
interface would mean developing the interface against a security model the
product does not have.

## Consequences

- **Phase 4's signed-in screens are unblocked**, which was the point. A page has
  now signed in, read `/api/me`, created a profile and signed out, in a browser.
- **The first authenticated request over a socket.** The tracker has carried
  "the end-to-end test goes through MockMvc, not a socket" as a known gap since
  phase 3, because a session could not be minted over real HTTP. It can now:
  `DevSignInTest` runs against a real port with a cookie jar kept by hand and no
  Spring Security test post-processor in it.
- **It found a real defect in the product on its first run.** Spring Security 6
  loads the CSRF token lazily, so no `XSRF-TOKEN` cookie was ever issued and the
  first write from any browser would have been refused — invisible for two phases
  because every test of a write used MockMvc's `csrf()` post-processor, which
  supplies the token production had not handed out. `SecurityConfig.browserCsrf`
  is the fix and both chains share it. **This is the argument for the decision,
  not a side benefit:** a development sign-in whose sessions differ from real
  ones would have hidden it instead.
- **The token exchange is still owed and is still untested.** Nothing here
  exchanges anything with a provider. Reading this record as "sign-in works"
  would be exactly the misreading it is written to prevent.
- **A developer who wants a login runs `bootRun`.** `docker compose` builds the
  same jar the deploy does, so the sign-in is absent there too. That is correct
  and occasionally inconvenient; the inconvenience is the guarantee.
- **One more module in `settings.gradle.kts`, and a COPY line's worth of
  attention in the Dockerfile.** Both are listed in `ModuleBoundaryTest`, which
  now governs `devsignin` explicitly rather than ignoring it as an undeclared
  layer.

## Reversal trigger

**Delete this module** once a real provider is configured against a deployed URL
and a developer can sign in locally against it. The reason it exists is that
nothing can; when something can, keeping a second way in is a liability with no
remaining benefit. Deleting it should cost one line in `settings.gradle.kts`, one
in `:app`'s dependencies, one directory, and two tests — and if it ever costs
more than that, something has grown a dependency on it that should not have.

**Reconsider the mechanism, not the decision,** if Gradle or the Spring Boot
plugin stops excluding `testAndDevelopmentOnly` from `bootJar`.
`DeployableJarTest` is what would tell you, on the build that broke it. The
decision — absence rather than configuration — survives any change in how absence
is arranged.
