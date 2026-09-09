import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ApiError, createProfile, getHealth, getMe, signInUrl, signOutUrl } from './api/client';

/**
 * The first page in this project that knows who is reading it.
 *
 * It is still small, and deliberately: Phase 4's screens — the inventory editor,
 * the goal picker, the plan view, the catalog — are N25, and none of them could
 * be started while nothing could sign in. What this page is for is the thing
 * that was missing, which is a signed-in state to develop against: it renders
 * the account, its profiles, and it can create one, which exercises a read, a
 * write and the CSRF token in between.
 *
 * A 401 is a state, not an error. It is what an anonymous reader gets, and
 * treating it as a failure would put an error message in front of every first
 * visit.
 */
export function App() {
  const health = useQuery({ queryKey: ['health'], queryFn: getHealth });
  const me = useQuery({
    queryKey: ['me'],
    queryFn: getMe,
    // A signed-out reader is answered, not failed at, so there is nothing here
    // worth retrying — and retrying it delays the sign-in prompt by a round trip.
    retry: (_failures, error) => !(error instanceof ApiError && error.isSignedOut),
  });

  const queries = useQueryClient();
  const addProfile = useMutation({
    mutationFn: () => createProfile('reverse-1999', 'global', 'Main'),
    onSuccess: () => queries.invalidateQueries({ queryKey: ['me'] }),
  });

  const signedOut = me.error instanceof ApiError && me.error.isSignedOut;
  const signOut = signOutUrl();

  return (
    <main style={{ fontFamily: 'system-ui, sans-serif', padding: '3rem', lineHeight: 1.6, maxWidth: '42rem' }}>
      <h1>Storm Almanac</h1>
      <p>A progression optimizer for live-service games.</p>

      <p>
        Backend:{' '}
        {health.isPending && 'checking…'}
        {health.isError && 'unreachable'}
        {health.data && `${health.data.status} (${health.data.version})`}
      </p>

      {me.isPending && <p>Reading your account…</p>}

      {signedOut && (
        <p>
          <a href={signInUrl('/')}>Sign in</a> to save an inventory and get a plan.
        </p>
      )}

      {me.isError && !signedOut && <p>Could not read your account: {(me.error as Error).message}</p>}

      {me.data && (
        <section>
          <p>
            Signed in as <strong>{me.data.displayName}</strong>
            {signOut && (
              <>
                {' — '}
                <a href={signOut}>sign out</a>
              </>
            )}
          </p>

          <h2>Profiles</h2>
          {me.data.profiles.length === 0 ? (
            <p>No profiles yet. One profile is one game on one server.</p>
          ) : (
            <ul>
              {me.data.profiles.map((profile) => (
                <li key={profile.id}>
                  {profile.displayName} — {profile.game} ({profile.region})
                </li>
              ))}
            </ul>
          )}

          <button type="button" onClick={() => addProfile.mutate()} disabled={addProfile.isPending}>
            {addProfile.isPending ? 'Creating…' : 'Add a profile'}
          </button>
          {addProfile.isError && <p>Could not create it: {(addProfile.error as Error).message}</p>}
        </section>
      )}
    </main>
  );
}
