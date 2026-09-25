import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link, useLocation } from 'react-router-dom';
import { ApiError, getGames, getMe, signInUrl } from '../api/client';
import { useCreateProfile } from '../profile';
import { usePlannerStore } from '../store/plannerStore';

/**
 * Where a reader lands: what this is, which profile they are planning for, and
 * the three things they can do with it.
 *
 * <p>Creating a profile asks which game, from the games this installation has
 * actually published, rather than offering a list of titles it cannot answer
 * questions about. A tool that lets you make a profile for a game it has no data
 * for is one that fails on the next screen instead of this one.
 */
export function Home() {
  const location = useLocation();
  const me = useQuery({
    queryKey: ['me'],
    queryFn: getMe,
    retry: (_failures, error) => !(error instanceof ApiError && error.isSignedOut),
  });
  const games = useQuery({ queryKey: ['games'], queryFn: getGames });

  const profileId = usePlannerStore((state) => state.profileId);
  const selectProfile = usePlannerStore((state) => state.selectProfile);

  const [game, setGame] = useState('');
  const [region, setRegion] = useState('global');
  const [displayName, setDisplayName] = useState('Main');

  const add = useCreateProfile();

  const signedOut = me.error instanceof ApiError && me.error.isSignedOut;
  const published = games.data?.games ?? [];
  const chosen = game || published[0]?.id || '';
  // One profile per game per server is the server's rule. The defaults are the
  // first game on "global", which is the profile a reader with one already has,
  // so the form says so rather than letting the server refuse it.
  const taken = me.data?.profiles.find((profile) => profile.game === chosen && profile.region === region);
  const chosenName = published.find((published) => published.id === chosen)?.displayName ?? chosen;

  return (
    <div className="space-y-6">
      <section>
        <h1 className="text-2xl font-semibold">Work out the cheapest way to get there</h1>
        <p className="muted mt-1 max-w-2xl">
          Tell it what you own and what you want. It reads the published patch data, works out what
          your goals actually cost, and says which stages to run — and how much of the answer it could
          prove inside its own time budget.
        </p>
      </section>

      {signedOut && (
        <div className="card">
          <p>
            <a href={signInUrl(location.pathname)}>Sign in</a> to keep an inventory across devices.
          </p>
          <p className="muted mt-2 text-sm">
            Or read the <Link to="/catalog">catalog</Link> first — it is public, and every number on
            it says which patch it came from.
          </p>
        </div>
      )}

      {me.data && (
        <section className="space-y-3">
          <h2 className="text-lg font-semibold">Profiles</h2>
          <p className="muted text-sm">One profile is one game on one server.</p>

          {me.data.profiles.length === 0 ? (
            <p className="muted">None yet.</p>
          ) : (
            <ul className="space-y-2">
              {me.data.profiles.map((profile) => {
                const active = profile.id === profileId;
                return (
                  <li key={profile.id} className="card flex items-center gap-3">
                    <div className="grow">
                      <div className="font-medium">{profile.displayName}</div>
                      <div className="muted text-sm">
                        {profile.game} · {profile.region}
                      </div>
                    </div>
                    {active ? (
                      <span className="text-sm" style={{ color: 'var(--brand)' }}>
                        planning for this one
                      </span>
                    ) : (
                      <button type="button" className="btn-quiet" onClick={() => selectProfile(profile.id)}>
                        Plan for this one
                      </button>
                    )}
                  </li>
                );
              })}
            </ul>
          )}

          <form
            className="card flex flex-wrap items-end gap-3"
            onSubmit={(event) => {
              event.preventDefault();
              add.mutate({ game: chosen, region, displayName });
            }}
          >
            <div>
              <label className="label" htmlFor="game">
                Game
              </label>
              <select
                id="game"
                className="input"
                value={chosen}
                onChange={(event) => setGame(event.target.value)}
                disabled={published.length === 0}
              >
                {published.map((published) => (
                  <option key={published.id} value={published.id}>
                    {published.displayName}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="label" htmlFor="region">
                Server
              </label>
              <input
                id="region"
                className="input"
                value={region}
                onChange={(event) => setRegion(event.target.value)}
              />
            </div>
            <div>
              <label className="label" htmlFor="name">
                Name
              </label>
              <input
                id="name"
                className="input"
                value={displayName}
                onChange={(event) => setDisplayName(event.target.value)}
              />
            </div>
            <button type="submit" className="btn" disabled={add.isPending || published.length === 0 || Boolean(taken)}>
              {add.isPending ? 'Creating…' : 'Add a profile'}
            </button>
            {taken && (
              <p className="muted text-sm">
                You already have a {chosenName} profile on {region}: {taken.displayName}.
              </p>
            )}
            {published.length === 0 && !games.isPending && (
              <p className="muted text-sm">
                Nothing is published on this installation yet, so there is no game to plan for.
              </p>
            )}
            {add.isError && <p className="text-sm">Could not create it: {(add.error as Error).message}</p>}
          </form>
        </section>
      )}

      <section className="grid gap-3 sm:grid-cols-3">
        <Step to="/inventory" n={1} title="Say what you own">
          Bulk entry, searchable, and it keeps working with no signal.
        </Step>
        <Step to="/goals" n={2} title="Say what you want">
          A character and a state to get her to. Order them by what matters.
        </Step>
        <Step to="/plan" n={3} title="Get the plan">
          Stages and runs, with what the answer cost and what it could not prove.
        </Step>
      </section>
    </div>
  );
}

function Step({
  to,
  n,
  title,
  children,
}: {
  to: string;
  n: number;
  title: string;
  children: React.ReactNode;
}) {
  return (
    <Link to={to} className="card block no-underline" style={{ color: 'var(--ink)' }}>
      <div className="label">Step {n}</div>
      <div className="mt-1 font-medium">{title}</div>
      <p className="muted mt-1 text-sm">{children}</p>
    </Link>
  );
}
