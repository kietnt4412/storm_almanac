import { useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { ApiError, getHealth, getMe, signInUrl, signOutUrl } from './api/client';
import { usePlannerStore } from './store/plannerStore';
import { useOutboxFlush } from './sync/useOutboxFlush';

/**
 * The shell every screen hangs off: who is reading, which profile they are
 * reading as, and whether this device has anything it has not managed to send.
 *
 * <p><b>The profile selector lives here rather than on each screen</b> because a
 * profile is the subject of every authenticated page — an inventory, a goal
 * list, a plan and a shortfall are all "for this profile" — and a screen that
 * asked again would be asking the same question four times. It is one game on
 * one server, which is why a person has several.
 *
 * <p><b>A 401 is a state, not an error.</b> It is what an anonymous reader gets,
 * and treating it as a failure would put an error message in front of every
 * first visit. The catalog stays readable in that state, which is the whole
 * point of it being public.
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

  const profileId = usePlannerStore((state) => state.profileId);
  const selectProfile = usePlannerStore((state) => state.selectProfile);
  const sync = useOutboxFlush(profileId);
  const location = useLocation();

  const profiles = me.data?.profiles ?? [];
  const selected = profiles.find((profile) => profile.id === profileId) ?? null;

  // A profile id persisted from a previous session can outlive the profile —
  // another device deleted it, or this is a different account on the same
  // browser. Selecting the first one it really has beats rendering screens that
  // 404 against a ghost.
  useEffect(() => {
    if (profiles.length === 0) return;
    const first = profiles[0];
    if (first && !profiles.some((profile) => profile.id === profileId)) {
      selectProfile(first.id);
    }
  }, [profileId, profiles, selectProfile]);

  const signedOut = me.error instanceof ApiError && me.error.isSignedOut;
  const signOut = signOutUrl();

  return (
    <div className="min-h-screen">
      <header className="border-b" style={{ borderColor: 'var(--line)', background: 'var(--surface)' }}>
        <div className="mx-auto flex max-w-5xl flex-wrap items-center gap-x-6 gap-y-2 px-4 py-3">
          <NavLink to="/" className="text-lg font-semibold" style={{ color: 'var(--ink)' }}>
            Storm Almanac
          </NavLink>

          <nav className="flex gap-4 text-sm">
            <Tab to="/plan">Plan</Tab>
            <Tab to="/inventory">Inventory</Tab>
            <Tab to="/goals">Goals</Tab>
            <Tab to="/catalog">Catalog</Tab>
          </nav>

          <div className="ml-auto flex items-center gap-3 text-sm">
            {profiles.length > 1 && (
              <select
                className="input"
                value={selected?.id ?? ''}
                onChange={(event) => selectProfile(event.target.value)}
                aria-label="Profile"
              >
                {profiles.map((profile) => (
                  <option key={profile.id} value={profile.id}>
                    {profile.displayName} — {profile.game} ({profile.region})
                  </option>
                ))}
              </select>
            )}

            {me.data ? (
              <>
                <span className="muted">{me.data.displayName}</span>
                {signOut && <a href={signOut}>sign out</a>}
              </>
            ) : signedOut ? (
              <a href={signInUrl(location.pathname)}>Sign in</a>
            ) : (
              <span className="muted">{me.isPending ? 'checking…' : ''}</span>
            )}
          </div>
        </div>

        <SyncBar sync={sync} />
      </header>

      <main className="mx-auto max-w-5xl px-4 py-6">
        <Outlet />
      </main>

      <footer className="mx-auto max-w-5xl px-4 pb-10 text-xs muted">
        {health.isError
          ? 'Backend unreachable — anything below is what this device remembers.'
          : health.data
            ? `Backend ${health.data.status} · ${health.data.version}`
            : 'Backend: checking…'}
      </footer>
    </div>
  );
}

function Tab({ to, children }: { to: string; children: React.ReactNode }) {
  return (
    <NavLink
      to={to}
      className={({ isActive }) => (isActive ? 'font-semibold' : '')}
      style={({ isActive }) => ({ color: isActive ? 'var(--brand)' : 'var(--muted)' })}
    >
      {children}
    </NavLink>
  );
}

/**
 * The one honest place to say what this device is holding.
 *
 * It is deliberately a strip in the chrome rather than a toast on save: an
 * offline editor's normal state is "some edits are not sent yet", and a
 * notification for a normal state is noise that teaches people to ignore it.
 */
function SyncBar({ sync }: { sync: ReturnType<typeof useOutboxFlush> }) {
  const nothingToSay = sync.pending === 0 && sync.rejected.length === 0 && !sync.error && sync.online;
  if (nothingToSay) return null;

  return (
    <div
      className="border-t px-4 py-2 text-xs"
      style={{ borderColor: 'var(--line)', color: 'var(--muted)' }}
    >
      <div className="mx-auto flex max-w-5xl flex-wrap items-center gap-3">
        {!sync.online && <span>Offline — edits are saved here and sent when you are back.</span>}
        {sync.pending > 0 && (
          <span>
            {sync.pending} edit{sync.pending === 1 ? '' : 's'} waiting
            {sync.flushing ? ' — sending…' : ''}
          </span>
        )}
        {sync.rejected.length > 0 && (
          <>
            <span style={{ color: 'var(--signal)' }}>
              Another device had newer values for {sync.rejected.join(', ')} — those were kept.
            </span>
            <button type="button" className="btn-quiet" onClick={sync.acknowledge}>
              Got it
            </button>
          </>
        )}
        {sync.error && <span style={{ color: 'var(--signal)' }}>{sync.error}</span>}
        {sync.pending > 0 && sync.online && (
          <button type="button" className="btn-quiet" onClick={sync.flushNow} disabled={sync.flushing}>
            Send now
          </button>
        )}
      </div>
    </div>
  );
}
