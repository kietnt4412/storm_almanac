import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { ApiError, getGames, getMe, signInUrl, type Profile } from '../api/client';
import { useCreateProfile, useDeleteProfile, useRenameProfile } from '../profile';
import { STEPS } from '../steps/Steps';
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
  const navigate = useNavigate();
  // Where a screen that needed a profile sent the reader from, so making one
  // goes back there rather than stranding them here. A path on this site only.
  const [search] = useSearchParams();
  const then = localPath(search.get('then'));
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
  const gameName = (id: string) => published.find((candidate) => candidate.id === id)?.displayName ?? id;
  const chosenName = gameName(chosen);

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
              {me.data.profiles.map((profile) => (
                <ProfileRow
                  key={profile.id}
                  profile={profile}
                  gameName={gameName(profile.game)}
                  active={profile.id === profileId}
                  onSelect={() => selectProfile(profile.id)}
                />
              ))}
            </ul>
          )}

          <form
            className="card flex flex-wrap items-end gap-3"
            onSubmit={(event) => {
              event.preventDefault();
              add.mutate(
                { game: chosen, region, displayName },
                { onSuccess: () => then && navigate(then) },
              );
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

      <section className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        {STEPS.map((step, index) => (
          <Step key={step.to} to={step.to} n={index + 1} title={step.title}>
            {step.blurb}
          </Step>
        ))}
      </section>

      {/* Not a fifth step: a pull is not something a plan leads to, it is a
          question asked beside one. */}
      <Link to="/pulls" className="card block no-underline" style={{ color: 'var(--ink)' }}>
        <span className="font-medium">Pulls</span>
        <span className="muted block text-sm">
          How likely you are to get the featured unit on a banner, and by when — from your own pity counter
          and what the game pays you.
        </span>
      </Link>
    </div>
  );
}

/**
 * One profile, and what its owner can do with it: plan for it, rename it,
 * delete it. Until 2026-09-28 only the first — the first note the strangers who
 * closed Phase 4 left, since a profile made on the wrong server could be neither
 * fixed nor removed, and blocked the right one from being made.
 *
 * <p><b>Delete asks once, in the row, and says what goes.</b> Not a browser
 * dialog, which reads as the browser asking; and not a vague "are you sure",
 * because the thing worth knowing is that the inventory and roster typed into
 * this profile go too, on every device.
 */
function ProfileRow({
  profile,
  gameName,
  active,
  onSelect,
}: {
  profile: Profile;
  gameName: string;
  active: boolean;
  onSelect: () => void;
}) {
  const [mode, setMode] = useState<'idle' | 'renaming' | 'deleting'>('idle');
  const [name, setName] = useState(profile.displayName);
  const rename = useRenameProfile();
  const remove = useDeleteProfile();
  const where = `${gameName} · ${profile.region}`;

  if (mode === 'renaming') {
    return (
      <li className="card">
        <form
          className="flex flex-wrap items-end gap-3"
          onSubmit={(event) => {
            event.preventDefault();
            rename.mutate({ profile: profile.id, displayName: name.trim() }, { onSuccess: () => setMode('idle') });
          }}
        >
          <div className="grow">
            <label className="label" htmlFor={`rename-${profile.id}`}>
              New name for {where}
            </label>
            <input
              id={`rename-${profile.id}`}
              className="input w-full"
              value={name}
              onChange={(event) => setName(event.target.value)}
              autoFocus
            />
          </div>
          <button type="submit" className="btn" disabled={rename.isPending || name.trim() === ''}>
            {rename.isPending ? 'Saving…' : 'Save'}
          </button>
          <button
            type="button"
            className="btn-quiet"
            onClick={() => {
              setName(profile.displayName);
              rename.reset();
              setMode('idle');
            }}
          >
            Cancel
          </button>
          {rename.isError && <p className="w-full text-sm">Could not rename it: {rename.error.message}</p>}
        </form>
      </li>
    );
  }

  if (mode === 'deleting') {
    return (
      <li className="card space-y-3" role="group" aria-label={`Delete ${profile.displayName}`}>
        <p>
          Delete <strong>{profile.displayName}</strong> ({where})? Its inventory, roster and goals go
          with it, on every device, and cannot be brought back.
        </p>
        <div className="flex flex-wrap gap-3">
          <button type="button" className="btn" disabled={remove.isPending} onClick={() => remove.mutate(profile.id)}>
            {remove.isPending ? 'Deleting…' : 'Delete it'}
          </button>
          <button
            type="button"
            className="btn-quiet"
            onClick={() => {
              remove.reset();
              setMode('idle');
            }}
          >
            Keep it
          </button>
        </div>
        {remove.isError && <p className="text-sm">Could not delete it: {remove.error.message}</p>}
      </li>
    );
  }

  return (
    <li className="card flex flex-wrap items-center gap-3">
      <div className="grow">
        <div className="font-medium">{profile.displayName}</div>
        <div className="muted text-sm">{where}</div>
      </div>
      {active ? (
        <span className="text-sm" style={{ color: 'var(--brand)' }}>
          planning for this one
        </span>
      ) : (
        <button type="button" className="btn-quiet" onClick={onSelect}>
          Plan for this one
        </button>
      )}
      <button
        type="button"
        className="btn-quiet"
        aria-label={`Rename ${profile.displayName}`}
        onClick={() => {
          setName(profile.displayName);
          setMode('renaming');
        }}
      >
        Rename
      </button>
      <button
        type="button"
        className="btn-quiet"
        aria-label={`Delete ${profile.displayName}`}
        onClick={() => setMode('deleting')}
      >
        Delete
      </button>
    </li>
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

/** A path on this site, or null: never another origin, which is what "//host" would be. */
export function localPath(then: string | null): string | null {
  return then && then.startsWith('/') && !then.startsWith('//') && !then.startsWith('/\\') ? then : null;
}
