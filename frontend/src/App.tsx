import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { ApiError, getHealth, getMe, signInUrl, signOut, type GameSummary, type Profile } from './api/client';
import { profileForGame, useActiveGame } from './profile';
import { isStep, StepBar } from './steps/Steps';
import { usePlannerStore } from './store/plannerStore';
import { useOutboxFlush } from './sync/useOutboxFlush';
import { Icon, type IconName } from './ui/Icon';
import { applyGame, lookOf, LOOKS, useGameChoice } from './ui/gameChoice';
import { GameMark } from './ui/GameMark';
import { usePanelHidden } from './ui/preferences';

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
  // A full navigation rather than invalidating the query: everything cached on
  // this page was fetched as the reader who is leaving, and a reload is the one
  // way to be sure none of it is shown to whoever sits down next. A failure —
  // offline, most likely — leaves them signed in and the button usable again.
  const [signingOut, setSigningOut] = useState(false);
  const leave = async () => {
    setSigningOut(true);
    try {
      await signOut();
      window.location.assign('/');
    } catch {
      setSigningOut(false);
    }
  };

  // The side panel (C2, the maintainer's call): beside the page on a wide
  // screen, where hiding it leaves a rail of icons; over the page on a phone,
  // where it starts closed and a menu button opens it. `hidden` is the wide
  // screen's remembered choice, `open` the phone's drawer, which a navigation
  // or Escape closes — a drawer left open over the page you just asked for is
  // the page not arriving.
  const [hidden, setHidden] = usePanelHidden();

  // The game the whole site is dressed for (C2.5): its palette on the document,
  // and its mark beside the name.
  const active = useActiveGame();
  const choose = useGameChoice((state) => state.choose);
  useEffect(() => applyGame(active.id), [active.id]);
  const [open, setOpen] = useState(false);
  useEffect(() => setOpen(false), [location.pathname]);
  useEffect(() => {
    if (!open) return;
    const close = (event: KeyboardEvent) => event.key === 'Escape' && setOpen(false);
    window.addEventListener('keydown', close);
    return () => window.removeEventListener('keydown', close);
  }, [open]);

  // Words that the rail hides from sight and keeps for a screen reader, so a
  // link is still named "Plan" when all that shows is its icon.
  const word = hidden ? 'md:sr-only' : '';

  return (
    <div className="min-h-screen md:flex">
      <div
        className="sticky top-0 z-30 flex items-center gap-2 border-b px-3 py-2 md:hidden"
        style={{ borderColor: 'var(--line)', background: 'var(--surface)' }}
      >
        <button type="button" className="icon-btn" onClick={() => setOpen(true)} aria-label="Open menu">
          <Icon name="menu" />
        </button>
        <Brand game={active.id} />
      </div>

      {open && (
        <div
          className="fixed inset-0 z-40 md:hidden"
          style={{ background: 'rgb(0 0 0 / 50%)' }}
          onClick={() => setOpen(false)}
          aria-hidden="true"
        />
      )}

      <aside
        aria-label="Menu"
        className={`fixed inset-y-0 left-0 z-50 flex w-64 flex-col gap-4 border-r p-3 transition-transform md:sticky md:top-0 md:h-screen md:translate-x-0 ${
          open ? 'translate-x-0' : '-translate-x-full'
        } ${hidden ? 'md:w-[4.25rem]' : 'md:w-60'}`}
        style={{ borderColor: 'var(--line)', background: 'var(--surface)' }}
      >
        <div className={`flex items-center gap-2 ${hidden ? 'md:flex-col' : ''}`}>
          <Brand game={active.id} wordClass={word} />
          <button
            type="button"
            className="icon-btn ml-auto hidden md:inline-flex"
            onClick={() => setHidden(!hidden)}
            aria-label={hidden ? 'Show menu' : 'Hide menu'}
            title={hidden ? 'Show menu' : 'Hide menu'}
          >
            <Icon name="panel" />
          </button>
          <button
            type="button"
            className="icon-btn ml-auto md:hidden"
            onClick={() => setOpen(false)}
            aria-label="Close menu"
          >
            <Icon name="close" />
          </button>
        </div>

        <nav className="flex flex-col gap-1">
          {/* In the order of the steps, so the menu reads as the way through. */}
          <Tab to="/" icon="home" word={word} end>Home</Tab>
          <Tab to="/inventory" icon="box" word={word}>Inventory</Tab>
          <Tab to="/roster" icon="users" word={word}>Roster</Tab>
          <Tab to="/goals" icon="target" word={word}>Goals</Tab>
          <Tab to="/plan" icon="route" word={word}>Plan</Tab>
          <Tab to="/pulls" icon="sparkle" word={word}>Pulls</Tab>
          <Tab to="/catalog" icon="book" word={word}>Catalog</Tab>
        </nav>

        <div className="mt-auto flex flex-col gap-3 text-sm">
          {profiles.length > 1 && (
            <select
              className={`input w-full ${hidden ? 'md:hidden' : ''}`}
              value={selected?.id ?? ''}
              onChange={(event) => {
                selectProfile(event.target.value);
                // A profile is one game, so picking one picks its game too.
                const picked = profiles.find((profile) => profile.id === event.target.value);
                if (picked) choose(picked.game);
              }}
              aria-label="Profile"
            >
              {profiles.map((profile) => (
                <option key={profile.id} value={profile.id}>
                  {profile.displayName} — {profile.game} ({profile.region})
                </option>
              ))}
            </select>
          )}

          <GameSwitch
            active={active.id}
            games={active.games}
            profiles={profiles}
            signedIn={Boolean(me.data)}
            stacked={hidden}
            word={word}
          />

          {me.data ? (
            <div className={`flex items-center gap-2 ${hidden ? 'md:flex-col' : ''}`}>
              <span className={`muted min-w-0 flex-1 truncate ${word}`}>{me.data.displayName}</span>
              <button
                type="button"
                className="icon-btn"
                onClick={leave}
                disabled={signingOut}
                aria-label="Sign out"
                title="Sign out"
              >
                <Icon name="signOut" />
              </button>
            </div>
          ) : signedOut ? (
            <a className="btn no-underline" href={signInUrl(location.pathname)}>
              Sign in
            </a>
          ) : (
            <span className="muted">{me.isPending ? 'checking…' : ''}</span>
          )}

          <p className={`text-xs muted ${word}`}>
            {health.isError
              ? 'Backend unreachable — anything below is what this device remembers.'
              : health.data
                ? `Backend ${health.data.status} · ${health.data.version}`
                : 'Backend: checking…'}
          </p>
        </div>
      </aside>

      <div className="min-w-0 flex-1">
        <SyncBar sync={sync} />
        <main className="mx-auto max-w-5xl px-4 py-6 md:px-8 md:py-8">
          {isStep(location.pathname) && <StepBar />}
          <Outlet />
        </main>
      </div>
    </div>
  );
}

function Brand({ game, wordClass = '' }: { game: string | null; wordClass?: string }) {
  return (
    <NavLink
      to="/"
      className="flex items-center gap-2 px-1 text-base font-semibold no-underline"
      style={{ color: 'var(--ink)' }}
    >
      <GameMark game={game} size={32} />
      <span className={wordClass}>Storm Almanac</span>
    </NavLink>
  );
}

function Tab({
  to,
  icon,
  word,
  end,
  children,
}: {
  to: string;
  icon: IconName;
  word: string;
  end?: boolean;
  children: React.ReactNode;
}) {
  return (
    <NavLink
      to={to}
      end={end}
      title={typeof children === 'string' ? children : undefined}
      className={({ isActive }) => `nav-item ${isActive ? 'nav-item-here' : ''}`}
    >
      <Icon name={icon} />
      <span className={word}>{children}</span>
    </NavLink>
  );
}

/**
 * Which game the site is about (C2.5, the maintainer's idea, in place of the
 * light / dark switch). Every game the server publishes is offered, each in
 * its own colours; a look marked upcoming that the server does not publish is
 * listed as coming soon and cannot be picked.
 *
 * <p>Switching picks the reader's profile for that game. With none, a signed-in
 * reader is taken Home with the form to make one open on that game — the
 * screens would otherwise be about a profile of another game under this one's
 * colours. A signed-out reader just gets the game: its catalog and its look.
 */
function GameSwitch({
  active,
  games,
  profiles,
  signedIn,
  stacked,
  word,
}: {
  active: string | null;
  games: GameSummary[];
  profiles: Profile[];
  signedIn: boolean;
  stacked: boolean;
  word: string;
}) {
  const choose = useGameChoice((state) => state.choose);
  const profileId = usePlannerStore((state) => state.profileId);
  const selectProfile = usePlannerStore((state) => state.selectProfile);
  const navigate = useNavigate();

  const soon = LOOKS.filter((look) => look.upcoming && !games.some((game) => game.id === look.id));
  if (games.length + soon.length < 2) return null;

  const pick = (game: string) => {
    choose(game);
    const profile = profileForGame(profiles, profileId, game);
    if (profile) selectProfile(profile.id);
    else if (signedIn) navigate(`/?new=${encodeURIComponent(game)}`);
  };

  return (
    <div role="group" aria-label="Game" className={`flex flex-col gap-1 ${stacked ? 'md:items-center' : ''}`}>
      <span className={`label px-1 ${word}`}>Game</span>
      {games.map((game) => (
        <button
          key={game.id}
          type="button"
          data-game={lookOf(game.id) ? game.id : 'storm'}
          className={`nav-item ${game.id === active ? 'nav-item-here' : ''}`}
          aria-pressed={game.id === active}
          title={game.displayName}
          onClick={() => pick(game.id)}
        >
          <GameMark game={game.id} size={22} />
          <span className={`min-w-0 truncate text-left ${word}`}>{game.displayName}</span>
        </button>
      ))}
      {soon.map((look) => (
        <span
          key={look.id}
          data-game={look.id}
          className="nav-item cursor-default opacity-60"
          aria-disabled="true"
          title={`${look.name} — coming soon`}
        >
          <GameMark game={look.id} size={22} />
          <span className={`min-w-0 flex-1 truncate ${word}`}>{look.name}</span>
          <span className={`text-[11px] font-medium uppercase tracking-wide ${word}`}>Soon</span>
        </span>
      ))}
    </div>
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
