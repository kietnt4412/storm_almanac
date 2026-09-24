# 30. The development sign-in stays, because the hands that need it cannot sign in

**Status:** Accepted · 2026-09-24

Supersedes the **reversal trigger** of
[ADR 0017](0017-the-development-sign-in-is-absent-from-the-artifact.md), and
nothing else in it. The decision there — absence from the artifact rather than a
switch inside it — stands unchanged, and is the reason this one is safe to make.

## Context

ADR 0017 ended on a condition for deleting `:modules:identity-dev`: *once a real
provider is configured against a deployed URL and a developer can sign in
locally against it*, because then a second way in would be "a liability with no
remaining benefit".

On 2026-09-24 the first half came true. `storm-almanac.vercel.app` rewrites to
Render, a Google client is registered against it, the maintainer signed in, and
the first production write landed through the rewrite. The session prepared the
second half — the Vite proxy forwards `/oauth2`, `/login/oauth2` and `/logout`
keeping the Host header, and `bootRun` alone reads `~/.storm-almanac/` for a
client secret — and put the last step to the maintainer: register
`http://localhost:5173/login/oauth2/code/google` and sign in locally.

**The maintainer declined, and the reason exposes an assumption in 0017.** The
trigger pictured the developer as a person who can type a Google password. Most
of the signed-in work in this repository is not done by one. It is done by
automated sessions, which may not enter credentials at all — and every one of
them that has built or changed a screen behind `/api/me` drove it through
`/dev/sign-in` first: the inventory editor, the goal picker, the plan view, the
roster, and on the same day as this record, the sign-out button, whose `POST
/logout → 204 → reload → 401` was proven that way. The project's own rule is
that appearance is a person's job and a browser drives a screen before it ships;
the development sign-in is how that rule is kept for the hands that do most of
the shipping.

It is also the only thing that authenticates over a socket. `DevSignInTest`
runs a real port with a cookie jar kept by hand, and on its first run it found
that no browser would ever be issued a CSRF cookie — every write in production
would have been refused, invisible for two phases to tests that used MockMvc's
`csrf()`. Deleting the module deletes that test with it, and 0017 counted it as
one of the two tests the deletion would cost.

## Decision

**`:modules:identity-dev` stays.** Nothing about it changes: a filter chain
matching `/dev/**`, a controller minting an ordinary principal under provider
`dev`, taken by `:app` as `testAndDevelopmentOnly` and therefore present on
`bootRun` and in the tests and absent from `storm-almanac.jar`.

**What made 0017 worry is what makes this safe.** The liability was never that
the code exists; it is that it could be reached in production, and that is
answered by absence, not by the calendar. `DeployableJarTest` opens the jar on
every build and proves no `io/stormalmanac/devsignin/` class is in it, and the
deployed service confirms it from the outside: `/dev/sign-in` on Render answers
401 from the product's own chain, as an unknown path does.

**Local sign-in through the real provider stays possible and optional.** The
proxy entries and `bootRun`'s optional config location cost nothing when unused
and let any developer who wants the real flow locally have it without a change
to the repository.

## Consequences

- **B5 no longer ends with the module deleted.** It ends when the pipeline's
  `deploy` job first goes green on `main`. The cut is recorded in the session log
  as the tracker requires, rather than left to be forgotten.
- **Two ways in exist on a developer's machine and one in production**, which is
  exactly the arrangement 0017 built. `provider = 'dev'` rows can only be minted
  by a process running from source, so any such row in the deployed database is
  evidence that the guard failed, not noise.
- **The real exchange is exercised by nobody's tests.** It has run in production
  and is now a thing a person verifies by signing in; `ForwardedOriginTest`
  covers the redirect URI and the relative redirect after it, which are the two
  parts that broke. That gap existed under 0017 too and is not widened here.

## Reversal trigger

Delete the module when **either** is true:

- **Signed-in screens can be driven without it** — a way for an automated
  session to hold a real authenticated browser session that does not involve a
  credential it may not type. Then the benefit is gone and 0017's reasoning
  applies as written.
- **Absence stops being provable.** If `DeployableJarTest` ever has to be
  loosened, skipped, or taught an exception, or the artifact that is deployed
  stops being the jar it inspects, the guarantee this record leans on has gone,
  and the module goes with it rather than the test being bargained with.
