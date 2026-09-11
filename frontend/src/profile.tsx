import { useQuery } from '@tanstack/react-query';
import { Link, useLocation } from 'react-router-dom';
import { ApiError, getMe, signInUrl, type Profile } from './api/client';
import { usePlannerStore } from './store/plannerStore';

/**
 * The profile every authenticated screen is about.
 *
 * <p>One query key for the account across the whole app, so the four screens
 * that need to know who is reading share one request rather than making four.
 */
export function useSelectedProfile(): {
  profile: Profile | null;
  signedOut: boolean;
  pending: boolean;
} {
  const me = useQuery({
    queryKey: ['me'],
    queryFn: getMe,
    retry: (_failures, error) => !(error instanceof ApiError && error.isSignedOut),
  });
  const profileId = usePlannerStore((state) => state.profileId);

  const profiles = me.data?.profiles ?? [];
  return {
    profile: profiles.find((profile) => profile.id === profileId) ?? profiles[0] ?? null,
    signedOut: me.error instanceof ApiError && me.error.isSignedOut,
    pending: me.isPending,
  };
}

/**
 * What a screen renders instead of itself when there is nobody to render it for.
 *
 * <p>Three different states and three different things to say, because they have
 * three different next actions: sign in, make a profile, or wait. Collapsing
 * them into one "not available" is how a tool ends up with readers who do not
 * know what they did wrong.
 */
export function ProfileGate({
  children,
}: {
  children: (profile: Profile) => React.ReactNode;
}): React.ReactElement {
  const { profile, signedOut, pending } = useSelectedProfile();
  const location = useLocation();

  if (profile) return <>{children(profile)}</>;
  if (pending) return <p className="muted">Reading your account…</p>;

  if (signedOut) {
    return (
      <div className="card">
        <p>
          <a href={signInUrl(location.pathname)}>Sign in</a> to keep an inventory, set goals and get a
          plan.
        </p>
        <p className="muted mt-2 text-sm">
          The <Link to="/catalog">catalog</Link> is readable without an account — signing in is what
          adds what <em>you</em> are short of to it.
        </p>
      </div>
    );
  }

  return (
    <div className="card">
      <p>
        You have no profile yet. One profile is one game on one server.{' '}
        <Link to="/">Make one</Link> and this screen has something to be about.
      </p>
    </div>
  );
}
