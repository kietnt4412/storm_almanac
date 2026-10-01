import { useState, type CSSProperties } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import {
  ApiError,
  getBanners,
  getGames,
  getGoals,
  getInventory,
  getMe,
  getPity,
  getRoster,
  getSavedPlan,
  signInUrl,
  type GameSummary,
  type Plan,
  type Profile,
} from '../api/client';
import { useActiveGame, useCreateProfile, useDeleteProfile, useRenameProfile, useSelectedProfile } from '../profile';
import { STEPS } from '../steps/Steps';
import { usePlannerStore } from '../store/plannerStore';
import { daysOf } from './PlanView';
import { useSince } from './SinceNotice';
import { Explain } from '../ui/Explain';
import { Icon, type IconName } from '../ui/Icon';
import { SpendBars } from '../ui/SpendBar';
import { formatUntil, nextReset, useNow } from '../ui/time';
import { useGameChoice } from '../ui/gameChoice';
import { Reveal, Words } from '../ui/motion';

/**
 * Where a reader lands, as a dashboard (2026-10-01): what this is and which
 * patch it reads, then — for a reader with a profile — their saved plan, the
 * live banner, how far through the four steps they are, and their profiles.
 *
 * <p><b>The plan comes first once there is one</b> (maintainer, 2026-10-01): a
 * returning reader came for it. Until then the setup card takes its place,
 * because the next step is the only useful thing to say.
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

  const signedOut = me.error instanceof ApiError && me.error.isSignedOut;
  const profiles = me.data?.profiles ?? [];
  // The game the switch is on, and the reader's profile for it — none, if they
  // switched to a game they have not made one for (C2.5).
  const { game, games: published } = useActiveGame();
  const { profile: active } = useSelectedProfile();
  // Read for whether it has answered yet; useActiveGame holds the same query.
  const games = useQuery({ queryKey: ['games'], queryFn: getGames });

  return (
    <div className="space-y-6">
      {/*
        The hero arrives (C2.6): the headline word by word, then the line under
        it and the facts, with a streak of light in the corner.
      */}
      <section className="hero hero-streak space-y-4">
        <div>
          <h1 className="text-2xl font-semibold sm:text-3xl">
            <Words text="Work out the cheapest way to get there" delay={80} />
          </h1>
          <Explain className="rise muted mt-1 max-w-2xl [--delay:420ms]" lead="Tell it what you own and what you want.">
            It reads the published patch data, works out what your goals actually cost, and says which stages
            to run — and how much of the answer it could prove inside its own time budget.
          </Explain>
        </div>
        {game ? <GameFacts game={game} /> : games.isPending && <ChipsWaiting />}
        {signedOut && (
          <div className="rise space-y-2" style={{ '--delay': '650ms' } as CSSProperties}>
            <div className="flex flex-wrap gap-3">
              <a className="btn no-underline" href={signInUrl(location.pathname)}>
                Sign in
              </a>
              <Link className="btn-quiet no-underline" to="/catalog">
                <Icon name="book" size={16} /> Browse the catalog
              </Link>
            </div>
            <p className="muted text-sm">
              Signing in keeps an inventory across devices. The catalog is public, and every number in it
              says which patch it came from.
            </p>
          </div>
        )}
      </section>

      {me.isPending || games.isPending ? (
        <CardsWaiting />
      ) : active && game ? (
        <Dashboard profile={active} game={game} />
      ) : (
        game && (
          <div className="grid gap-4 lg:grid-cols-3">
            <section className="grid gap-3 sm:grid-cols-2 lg:col-span-2" aria-label="How it works">
              {STEPS.map((step, index) => (
                <Link
                  key={step.to}
                  to={step.to}
                  className="card rise block no-underline"
                  style={{ color: 'var(--ink)', '--delay': `${700 + index * 90}ms` } as CSSProperties}
                >
                  <div className="flex items-center gap-2" style={{ color: 'var(--brand)' }}>
                    <Icon name={STEP_ICONS[step.to]} size={18} />
                    <span className="label">Step {index + 1}</span>
                  </div>
                  <div className="mt-1 font-medium">{step.title}</div>
                  <p className="muted mt-1 text-sm">{step.blurb}</p>
                </Link>
              ))}
            </section>
            <div className="rise" style={{ '--delay': '1060ms' } as CSSProperties}>
              <BannerCard game={game.id} profileId={null} />
            </div>
          </div>
        )
      )}

      {me.data && (
        <Reveal>
          <Profiles profiles={profiles} activeId={active?.id ?? null} published={published} />
        </Reveal>
      )}
    </div>
  );
}

/**
 * The shape of what is coming, while the server has not answered: on a host
 * that has been asleep that is up to a minute, and a headline over nothing
 * reads as a page that broke. Hidden from a screen reader, which hears the
 * waking notice instead.
 */
function ChipsWaiting() {
  return (
    <div className="flex flex-wrap gap-2" aria-hidden="true">
      <span className="skeleton h-7 w-72 max-w-full" />
      <span className="skeleton h-7 w-48" />
    </div>
  );
}

function CardsWaiting() {
  return (
    <div className="grid gap-4 lg:grid-cols-3" aria-hidden="true" data-testid="home-waiting">
      <div className="grid gap-3 sm:grid-cols-2 lg:col-span-2">
        {[0, 1, 2, 3].map((index) => (
          <div key={index} className="skeleton h-28" style={{ '--delay': `${index * 120}ms` } as CSSProperties} />
        ))}
      </div>
      <div className="skeleton h-40 lg:h-full" style={{ '--delay': '480ms' } as CSSProperties} />
    </div>
  );
}

type StepPath = (typeof STEPS)[number]['to'];

const STEP_ICONS: Record<StepPath, IconName> = {
  '/inventory': 'box',
  '/roster': 'users',
  '/goals': 'target',
  '/plan': 'route',
};

/**
 * The patch the numbers come from and when the game's day next rolls over.
 * The countdown is absent, not guessed, for a game whose reset nobody read.
 */
function GameFacts({ game }: { game: GameSummary }) {
  const now = useNow();
  const boundary = game.dayBoundary;
  const reset = boundary ? nextReset(boundary.zone, boundary.hour, now) : null;
  return (
    <ul className="rise flex flex-wrap gap-2" aria-label="The game" style={{ '--delay': '560ms' } as CSSProperties}>
      <li className="chip">
        <Icon name="book" size={14} />
        {game.displayName} · v{game.latest.sequence} · {game.latest.label}
      </li>
      {boundary && reset && (
        <li className="chip" title={`The game's day starts at ${String(boundary.hour).padStart(2, '0')}:00 ${boundary.zone}`}>
          <Icon name="clock" size={14} />
          Daily reset in {formatUntil(reset.getTime() - now.getTime())}
        </li>
      )}
    </ul>
  );
}

function Dashboard({ profile, game }: { profile: Profile; game: GameSummary }) {
  const saved = useQuery({
    queryKey: ['savedPlan', profile.id],
    queryFn: async () => (await getSavedPlan(profile.id)) ?? null,
  });
  const plan = saved.data?.plan ?? null;

  return (
    <div className="space-y-4">
      <div className="grid gap-4 lg:grid-cols-3">
        <div className="rise lg:col-span-2" style={{ '--delay': '700ms' } as CSSProperties}>
          {plan ? (
            <PlanCard profile={profile} plan={plan} energyUnit={game.energyUnit} />
          ) : (
            <SetupCard profile={profile} plan={null} wide={false} />
          )}
        </div>
        <div className="rise" style={{ '--delay': '820ms' } as CSSProperties}>
          <BannerCard game={game.id} profileId={profile.id} />
        </div>
      </div>
      {plan && (
        <Reveal>
          <SetupCard profile={profile} plan={plan} wide />
        </Reveal>
      )}
    </div>
  );
}

/**
 * The profile's saved plan (C2.3, grown into a card): what it costs, how long,
 * where the currency goes, and — when a sequence has come out since — what it
 * changed (C3.3). It reads the same query the plan screen does, so the two never
 * show different plans.
 */
function PlanCard({ profile, plan, energyUnit }: { profile: Profile; plan: Plan; energyUnit: string }) {
  const spends = plan.conversions.flatMap((conversion) => (conversion.spends ? [conversion.spends] : []));
  const runs = plan.stages.reduce((sum, stage) => sum + stage.runs, 0);
  return (
    <section className="card-raised h-full space-y-4" aria-labelledby="plan-heading">
      <div className="flex flex-wrap items-baseline justify-between gap-2">
        <h2 id="plan-heading" className="flex items-center gap-2 text-lg font-semibold">
          <span style={{ color: 'var(--brand)' }}>
            <Icon name="route" size={18} />
          </span>
          Your plan
        </h2>
        <span className="muted text-sm">
          {profile.displayName} · on v{plan.version}
        </span>
      </div>
      <SinceLine profile={profile} />
      <div className="grid grid-cols-3 gap-3">
        <div className="stat-tile">
          <span className="stat-label">{energyUnit}</span>
          <span className="stat-value count" style={{ color: 'var(--brand)' }}>
            {plan.totalEnergy.toLocaleString()}
          </span>
        </div>
        <div className="stat-tile">
          <span className="stat-label">Days</span>
          <span className="stat-value count">{daysOf(plan.etaDays)}</span>
        </div>
        <div className="stat-tile">
          <span className="stat-label">Runs</span>
          <span className="stat-value count">{runs.toLocaleString()}</span>
        </div>
      </div>
      <SpendBars spends={spends} />
      <div className="flex justify-end">
        <Link to="/plan" className="btn no-underline">
          Open plan →
        </Link>
      </div>
    </section>
  );
}

/**
 * One line when a sequence has come out since the plan (C3.3), so a returning
 * reader hears about it without asking.
 *
 * <p><b>It counts what touches their plan</b>, which costs two solves per
 * out-of-date profile once per patch; the card never waits for it. It says so
 * when nothing does, because no line at all reads the same as up to date. It
 * goes away when the reader re-plans, and not before (maintainer, 2026-09-29).
 */
function SinceLine({ profile }: { profile: Profile }) {
  const since = useSince(profile.id);
  const report = since.data;
  if (!report || report.savedVersion >= report.latestVersion) return null;
  const touching = report.changes.length;
  return (
    <p
      className="rounded-lg border-l-4 px-3 py-2 text-sm"
      style={{ borderColor: 'var(--signal)', background: 'color-mix(in srgb, var(--signal) 10%, transparent)' }}
    >
      <span className="font-medium">New since your plan:</span> {report.latestVersionLabel} (v{report.latestVersion}) ·{' '}
      {touching === 0 ? (
        <>
          nothing in it touches your plan · <Link to="/plan">Re-plan →</Link>
        </>
      ) : (
        <>
          {touching} change{touching === 1 ? '' : 's'} in it touch{touching === 1 ? 'es' : ''} your plan ·{' '}
          <Link to="/plan">See what changed →</Link>
        </>
      )}
    </p>
  );
}

/**
 * The four steps with where this reader actually is on each, and the first one
 * not yet done marked as the one to do. Each count reads the query its own
 * screen reads, so Home and the screen never disagree. `wide` when it spans the
 * page under the plan; beside the banner, four columns would wrap every title.
 */
function SetupCard({ profile, plan, wide }: { profile: Profile; plan: Plan | null; wide: boolean }) {
  const inventory = useQuery({ queryKey: ['inventory', profile.id], queryFn: () => getInventory(profile.id) });
  const roster = useQuery({ queryKey: ['roster', profile.id], queryFn: () => getRoster(profile.id) });
  const goals = useQuery({ queryKey: ['goals', profile.id], queryFn: () => getGoals(profile.id) });

  const held = inventory.data ? Object.values(inventory.data.items).filter((quantity) => quantity > 0).length : null;
  const recorded = roster.data ? Object.keys(roster.data.entities).length : null;
  const wanted = goals.data ? goals.data.goals.length : null;
  const status: Record<StepPath, { count: number | null; failed: boolean; say: (n: number) => string }> = {
    '/inventory': {
      count: held,
      failed: inventory.isError,
      say: (n) => `${n.toLocaleString()} item${n === 1 ? '' : 's'} held`,
    },
    '/roster': { count: recorded, failed: roster.isError, say: (n) => `${n} recorded` },
    '/goals': { count: wanted, failed: goals.isError, say: (n) => `${n} goal${n === 1 ? '' : 's'}` },
    '/plan': { count: plan ? 1 : 0, failed: false, say: () => (plan ? `saved on v${plan.version}` : 'not run yet') },
  };
  const rows = STEPS.map((step) => ({ step, ...status[step.to], done: (status[step.to].count ?? 0) > 0 }));
  const doneCount = rows.filter((row) => row.done).length;
  const next = rows.findIndex((row) => !row.done);

  // Finished, it is one line (C2.5): four full cards saying "done" held the
  // middle of Home for a reader who had nothing left to set up, below the plan
  // they came for. The steps stay one tap away, as links.
  if (next === -1) {
    return (
      <section className="card flex flex-wrap items-center gap-x-4 gap-y-2" aria-labelledby="setup-heading">
        <h2 id="setup-heading" className="font-semibold">
          Your setup
        </h2>
        <span className="text-sm font-medium" style={{ color: 'var(--brand)' }}>
          ✓ {doneCount} of {STEPS.length} done
        </span>
        <nav className="flex flex-wrap gap-x-3 gap-y-1 text-sm" aria-label="Setup steps">
          {rows.map(({ step, count, say }) => (
            <Link key={step.to} to={step.to} className="no-underline" title={count !== null ? say(count) : undefined}>
              {step.title}
            </Link>
          ))}
        </nav>
        <span className="muted text-sm sm:ml-auto">Re-plan whenever your inventory moves.</span>
      </section>
    );
  }

  return (
    <section className="card h-full space-y-4" aria-labelledby="setup-heading">
      <div className="flex flex-wrap items-baseline justify-between gap-2">
        <h2 id="setup-heading" className="text-lg font-semibold">
          Your setup
        </h2>
        <span className="muted text-sm">
          {doneCount} of {STEPS.length} done
        </span>
      </div>
      <div className="grid grid-cols-4 gap-1.5" aria-hidden="true">
        {rows.map((row) => (
          <span
            key={row.step.to}
            className="h-1.5 rounded-full"
            style={{ background: row.done ? 'var(--brand)' : 'var(--line)' }}
          />
        ))}
      </div>
      <ol className={`grid grid-cols-2 gap-3 ${wide ? 'lg:grid-cols-4' : ''}`}>
        {rows.map(({ step, count, failed, say, done }, index) => {
          const isNext = index === next;
          return (
            <li key={step.to} className="min-w-0">
              <Link
                to={step.to}
                className={`card block h-full no-underline ${isNext ? 'card-next' : ''}`}
                style={{ color: 'var(--ink)' }}
              >
                <div className="flex items-center gap-2" style={{ color: done ? 'var(--brand)' : 'var(--muted)' }}>
                  <Icon name={STEP_ICONS[step.to]} size={18} />
                  <span className="label">Step {index + 1}</span>
                  {done && <span className="ml-auto text-xs font-semibold">✓</span>}
                </div>
                <div className="mt-1 font-medium">{step.title}</div>
                <p className="muted mt-0.5 text-sm">{count !== null ? say(count) : failed ? 'could not read it' : '…'}</p>
                {isNext && (
                  <p className="mt-1 text-xs font-semibold" style={{ color: 'var(--brand)' }}>
                    Do this next →
                  </p>
                )}
              </Link>
            </li>
          );
        })}
      </ol>
    </section>
  );
}

/**
 * The banner open now, how long it has left, and — for a signed-in reader —
 * the pity counter they stored for it. Prefers a banner the pull planner can
 * answer about (one with a price read), since that is where its link goes.
 */
function BannerCard({ game, profileId }: { game: string; profileId: string | null }) {
  const now = useNow();
  const banners = useQuery({ queryKey: ['banners', game], queryFn: () => getBanners(game) });
  const open = (banners.data?.banners ?? []).filter((banner) => banner.open);
  const banner = open.find((candidate) => candidate.pullPrice !== null) ?? open[0] ?? null;
  const pity = useQuery({
    queryKey: ['pity', profileId, banner?.id],
    queryFn: () => getPity(profileId!, banner!.id),
    enabled: profileId !== null && banner !== null,
  });

  return (
    <section className="card h-full space-y-3" aria-labelledby="banner-heading">
      <h2 id="banner-heading" className="flex items-center gap-2 text-lg font-semibold">
        <span style={{ color: 'var(--violet)' }}>
          <Icon name="sparkle" size={18} />
        </span>
        Live banner
      </h2>
      {banners.isPending ? (
        <p className="muted text-sm">Loading the banners…</p>
      ) : banners.isError ? (
        <p className="muted text-sm">Could not load the banners: {banners.error.message}</p>
      ) : banner === null ? (
        <p className="muted text-sm">No banner is open right now.</p>
      ) : (
        <>
          <div>
            <div className="text-lg font-semibold leading-snug">{banner.displayName}</div>
            <div className="muted text-sm">
              {banner.closesAt
                ? `Closes in ${formatUntil(new Date(banner.closesAt).getTime() - now.getTime())}`
                : 'No close date read'}
              {open.length > 1 && ` · ${open.length - 1} more open`}
            </div>
          </div>
          {pity.data && (
            <div className="stat-tile space-y-1.5">
              <div className="flex items-baseline justify-between">
                <span className="stat-label">Your pity</span>
                <span className="count font-semibold">
                  {pity.data.pullsSinceHit} / {pity.data.hardAt}
                </span>
              </div>
              <div className="h-1.5 overflow-hidden rounded-full" style={{ background: 'var(--line)' }} aria-hidden="true">
                <div
                  className="h-full rounded-full"
                  style={{
                    width: `${Math.min(100, (pity.data.pullsSinceHit / Math.max(1, pity.data.hardAt)) * 100)}%`,
                    background: 'var(--violet)',
                  }}
                />
              </div>
            </div>
          )}
          <Link to="/pulls" className="btn-quiet no-underline">
            Plan your pulls →
          </Link>
        </>
      )}
    </section>
  );
}

/**
 * The reader's profiles, and a way to add one that stays closed until asked
 * for (maintainer, 2026-10-01) — open from the start only when there is no
 * profile yet, or when another screen sent the reader here to make one.
 */
function Profiles({
  profiles,
  activeId,
  published,
}: {
  profiles: Profile[];
  activeId: string | null;
  published: GameSummary[];
}) {
  const navigate = useNavigate();
  // Where a screen that needed a profile sent the reader from, so making one
  // goes back there rather than stranding them here. A path on this site only.
  const [search] = useSearchParams();
  const then = localPath(search.get('then'));
  // A game the switch sent the reader here to make a profile for (C2.5).
  const asked = search.get('new');
  const wanted = published.some((candidate) => candidate.id === asked) ? asked : null;
  const selectProfile = usePlannerStore((state) => state.selectProfile);
  const choose = useGameChoice((state) => state.choose);

  const [adding, setAdding] = useState(false);
  const [game, setGame] = useState(wanted ?? '');
  const [region, setRegion] = useState('global');
  const [displayName, setDisplayName] = useState('Main');
  const add = useCreateProfile();

  const forced = profiles.length === 0 || then !== null || wanted !== null;
  const showForm = forced || adding;
  const chosen = game || wanted || published[0]?.id || '';
  // One profile per game per server is the server's rule. The defaults are the
  // first game on "global", which is the profile a reader with one already has,
  // so the form says so rather than letting the server refuse it.
  const taken = profiles.find((profile) => profile.game === chosen && profile.region === region);
  const gameName = (id: string) => published.find((candidate) => candidate.id === id)?.displayName ?? id;
  const chosenName = gameName(chosen);

  return (
    <section className="space-y-3">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div>
          <h2 className="text-lg font-semibold">Profiles</h2>
          <p className="muted text-sm">One profile is one game on one server.</p>
        </div>
        {!showForm && (
          <button type="button" className="btn-quiet" onClick={() => setAdding(true)}>
            <Icon name="plus" size={16} /> New profile
          </button>
        )}
      </div>

      {profiles.length === 0 ? (
        <p className="muted">None yet.</p>
      ) : (
        <ul className="space-y-2">
          {profiles.map((profile) => (
            <ProfileRow
              key={profile.id}
              profile={profile}
              gameName={gameName(profile.game)}
              active={profile.id === activeId}
              onSelect={() => {
                selectProfile(profile.id);
                choose(profile.game);
              }}
            />
          ))}
        </ul>
      )}

      {showForm && (
        <form
          className="card flex flex-wrap items-end gap-3"
          onSubmit={(event) => {
            event.preventDefault();
            add.mutate(
              { game: chosen, region, displayName },
              {
                onSuccess: () => {
                  setAdding(false);
                  if (then) navigate(then);
                },
              },
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
            <input id="region" className="input" value={region} onChange={(event) => setRegion(event.target.value)} />
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
          {!forced && (
            <button
              type="button"
              className="btn-quiet"
              onClick={() => {
                add.reset();
                setAdding(false);
              }}
            >
              Cancel
            </button>
          )}
          {taken && (
            <p className="muted w-full text-sm">
              You already have a {chosenName} profile on {region}: {taken.displayName}.
            </p>
          )}
          {published.length === 0 && (
            <p className="muted w-full text-sm">
              Nothing is published on this installation yet, so there is no game to plan for.
            </p>
          )}
          {add.isError && <p className="w-full text-sm">Could not create it: {(add.error as Error).message}</p>}
        </form>
      )}
    </section>
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
    <li className={`card flex flex-wrap items-center gap-3 ${active ? 'card-next' : ''}`}>
      <span
        className="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-full text-sm font-semibold"
        style={{ background: 'color-mix(in srgb, var(--brand) 18%, transparent)', color: 'var(--brand)' }}
        aria-hidden="true"
      >
        {profile.displayName.trim().charAt(0).toUpperCase() || '?'}
      </span>
      <div className="min-w-0 grow">
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

/** A path on this site, or null: never another origin, which is what "//host" would be. */
export function localPath(then: string | null): string | null {
  return then && then.startsWith('/') && !then.startsWith('//') && !then.startsWith('/\\') ? then : null;
}
