import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useLocation } from 'react-router-dom';
import { ApiError, createProfile, getMe, signInUrl, type Me, type Profile } from './api/client';
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
  const { profiles, profileId, signedOut, pending } = useAccount();
  return {
    profile: profiles.find((profile) => profile.id === profileId) ?? profiles[0] ?? null,
    signedOut,
    pending,
  };
}

/**
 * The reader's profile for one game — the selected one if it plays that game,
 * otherwise their first that does, otherwise none.
 *
 * <p>A page about one game's entity must not ask with whichever profile happens
 * to be selected: an entity id means something only inside its game. The
 * character page did exactly that until 2026-09-24, and a PGR construct asked
 * with an R1999 profile got R1999's own error back.
 */
export function profileForGame(profiles: Profile[], selectedId: string | null, game: string): Profile | null {
  const playing = profiles.filter((profile) => profile.game === game);
  return playing.find((profile) => profile.id === selectedId) ?? playing[0] ?? null;
}

/** {@link profileForGame} for the signed-in reader. */
export function useProfileFor(game: string): {
  profile: Profile | null;
  signedOut: boolean;
  pending: boolean;
} {
  const { profiles, profileId, signedOut, pending } = useAccount();
  return { profile: profileForGame(profiles, profileId, game), signedOut, pending };
}

/**
 * Makes a profile and selects it.
 *
 * <p><b>The order matters, and it was wrong in both places that made one.</b>
 * The shell re-selects the account's first profile whenever the selected id is
 * not among the account's profiles (a ghost left by another device). Selecting
 * the new id before `/api/me` had been read again put it in exactly that state,
 * and the shell switched straight back. So the new profile goes into the
 * cached account first, then it is selected, then the account is re-read.
 * Found driving the character page's offer, 2026-09-24; the home screen's form
 * had the same race.
 */
export function useCreateProfile() {
  const queries = useQueryClient();
  const selectProfile = usePlannerStore((state) => state.selectProfile);
  return useMutation({
    mutationFn: ({ game, region, displayName }: { game: string; region: string; displayName: string }) =>
      createProfile(game, region, displayName),
    onSuccess: (profile) => {
      queries.setQueryData<Me>(['me'], (me) => (me ? { ...me, profiles: [...me.profiles, profile] } : me));
      selectProfile(profile.id);
      queries.invalidateQueries({ queryKey: ['me'] });
    },
  });
}

function useAccount() {
  const me = useQuery({
    queryKey: ['me'],
    queryFn: getMe,
    retry: (_failures, error) => !(error instanceof ApiError && error.isSignedOut),
  });
  const profileId = usePlannerStore((state) => state.profileId);
  return {
    profiles: me.data?.profiles ?? [],
    profileId,
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
        <Link to={`/?then=${encodeURIComponent(location.pathname)}`}>Make one</Link> and you come
        back here with something for this screen to be about.
      </p>
    </div>
  );
}
