import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useLocation } from 'react-router-dom';
import {
  ApiError,
  createProfile,
  deleteProfile,
  getGames,
  getMe,
  renameProfile,
  signInUrl,
  type GameSummary,
  type Me,
  type Profile,
} from './api/client';
import { usePlannerStore } from './store/plannerStore';
import { useGameChoice } from './ui/gameChoice';

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
  const { chosen } = useActiveGame();
  const selected = profiles.find((profile) => profile.id === profileId) ?? profiles[0] ?? null;
  return {
    // The game switch decides which game every screen is about (C2.5): the
    // reader's profile for it, or none — which the gate turns into an offer to
    // make one, rather than showing another game's data under this game's look.
    profile: chosen ? profileForGame(profiles, profileId, chosen) : selected,
    signedOut,
    pending,
  };
}

/**
 * The game the site is about right now: the one the reader switched to, if the
 * server publishes it; otherwise their selected profile's; otherwise the first
 * the server publishes. `chosen` is set only for a switch the server can serve.
 */
export function useActiveGame(): {
  id: string | null;
  game: GameSummary | null;
  games: GameSummary[];
  chosen: string | null;
} {
  const games = useQuery({ queryKey: ['games'], queryFn: getGames });
  const { profiles, profileId } = useAccount();
  const choice = useGameChoice((state) => state.chosen);
  const served = games.data?.games ?? [];
  // Until there is a list to check it against — loading, or a server that cannot
  // be reached — the choice is trusted: what this browser chose last is far
  // likelier right than wrong, and a flash of another game is worse.
  const chosen = choice && (!games.data || served.some((game) => game.id === choice)) ? choice : null;
  const selected = profiles.find((profile) => profile.id === profileId) ?? profiles[0] ?? null;
  const id = chosen ?? selected?.game ?? served[0]?.id ?? null;
  return { id, game: served.find((game) => game.id === id) ?? null, games: served, chosen };
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
      // A new profile is for the game the reader means to look at.
      useGameChoice.getState().choose(profile.game);
      queries.invalidateQueries({ queryKey: ['me'] });
    },
  });
}

/** Renames a profile, and puts the new name wherever the account is cached. */
export function useRenameProfile() {
  const queries = useQueryClient();
  return useMutation({
    mutationFn: ({ profile, displayName }: { profile: string; displayName: string }) =>
      renameProfile(profile, displayName),
    onSuccess: (renamed) => {
      queries.setQueryData<Me>(['me'], (me) =>
        me ? { ...me, profiles: me.profiles.map((held) => (held.id === renamed.id ? renamed : held)) } : me,
      );
    },
  });
}

/**
 * Deletes a profile and forgets it on this device.
 *
 * <p>Out of the cached account first, for the same reason {@link useCreateProfile}
 * puts one in first: the shell re-selects the first profile it holds whenever
 * the selected one is missing, and it should find the profiles that are left.
 * What was cached about the profile — its inventory, roster, goals, plan — goes
 * with it, so no screen can render a profile the server no longer has.
 */
export function useDeleteProfile() {
  const queries = useQueryClient();
  const forgetProfile = usePlannerStore((state) => state.forgetProfile);
  return useMutation({
    mutationFn: (profile: string) => deleteProfile(profile),
    onSuccess: (_nothing, profile) => {
      queries.setQueryData<Me>(['me'], (me) =>
        me ? { ...me, profiles: me.profiles.filter((held) => held.id !== profile) } : me,
      );
      forgetProfile(profile);
      for (const key of ['inventory', 'roster', 'goals', 'plan', 'shortfall']) {
        queries.removeQueries({ queryKey: [key, profile] });
      }
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
  const { chosen, game } = useActiveGame();
  const { profiles } = useAccount();
  const location = useLocation();

  if (profile) return <>{children(profile)}</>;
  if (pending) return <p className="muted">Reading your account…</p>;

  // Switched to a game they have no profile for: say which, and offer it.
  if (!signedOut && chosen && profiles.length > 0) {
    return (
      <div className="card">
        <p>
          You have no {game?.displayName ?? chosen} profile yet.{' '}
          <Link to={`/?new=${encodeURIComponent(chosen)}&then=${encodeURIComponent(location.pathname)}`}>
            Make one
          </Link>{' '}
          and you come back here with it — or switch game in the menu.
        </p>
      </div>
    );
  }

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
