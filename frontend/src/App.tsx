import { useEffect, useRef, useState, type ReactNode } from 'react';
import { useQuery } from '@tanstack/react-query';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { ApiError, getHealth, getMe, signInUrl, signOut, type GameSummary, type Profile } from './api/client';
import { profileForGame, useActiveGame } from './profile';
import { isStep, StepBar, STEPS } from './steps/Steps';
import { usePlannerStore } from './store/plannerStore';
import { useOutboxFlush } from './sync/useOutboxFlush';
import { Icon, type IconName } from './ui/Icon';
import { Intro } from './ui/Intro';
import { GamePicker } from './ui/GamePicker';
import { applyGame, lookOf, LOOKS, useGameChoice } from './ui/gameChoice';
import { GameMark } from './ui/GameMark';
import { Wordmark } from './ui/Wordmark';
import { Footer } from './ui/Footer';
import { useTakingLong } from './ui/time';

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

  // The game the whole site is dressed for (C2.5): its palette on the document,
  // and its mark beside the name.
  const active = useActiveGame();
  useEffect(() => applyGame(active.id), [active.id]);
  const pick = usePickGame(profiles, Boolean(me.data));

  // The first screen (C2.9): a browser that has never chosen a game is asked,
  // and the opening then plays for the game picked. A link into one game's
  // catalog has already said which game, so it is never stood in front of.
  const neverChosen = useGameChoice((state) => state.chosen) === null;
  const linkedToGame = /^\/catalog\/[^/]+/.test(location.pathname);

  // The phone's menu, which a navigation or Escape closes — a menu left open
  // over the page you just asked for is the page not arriving.
  const [open, setOpen] = useState(false);
  useEffect(() => setOpen(false), [location.pathname]);
  useEscape(open, () => setOpen(false));
  const scrolled = useScrolled(24);

  // The server's state is said only when a reader needs it: when it cannot be
  // reached. Its version is a developer's concern and stays on /api/health.
  const unreachable = health.isError;
  // The free host sleeps when nobody has used it for a while, and every request
  // then waits up to a minute while it starts. Said only once the wait is long
  // enough to look like a broken page.
  const waking = useTakingLong(health.isPending);

  const gameSwitch = <GameSwitch active={active.id} games={active.games} pick={pick} />;
  const profilePicker = profiles.length > 1 && <ProfilePicker profiles={profiles} />;

  if (neverChosen && !linkedToGame) return <GamePicker onPick={pick} />;

  return (
    <div className="flex min-h-screen flex-col">
      <Intro game={active.id} />

      {/*
        The top bar (C2.6, the maintainer's ask, 2026-10-01, replacing C2's side
        panel): full width at the top of the page, and a floating pill once the
        page scrolls under it. The four planner steps share one menu, because
        the step bar on each of them already says the way through.
      */}
      <header className="site-nav" data-scrolled={scrolled}>
        <div className="site-nav-inner">
          <Brand game={active.id} />

          <nav aria-label="Menu" className="hidden flex-1 items-center justify-center gap-1 md:flex">
            <TopLink to="/" end>
              Home
            </TopLink>
            <Dropdown
              label="Planner"
              here={isStep(location.pathname)}
              trigger={
                <>
                  Planner <Icon name="chevronDown" size={14} />
                </>
              }
            >
              <ul className="w-72 space-y-0.5">
                {STEPS.map((step) => (
                  <li key={step.to}>
                    <NavLink
                      to={step.to}
                      viewTransition
                      className={({ isActive }) => `menu-row ${isActive ? 'menu-row-here' : ''}`}
                    >
                      <span className="menu-row-icon">
                        <Icon name={STEP_ICONS[step.to]} size={18} />
                      </span>
                      <span className="min-w-0">
                        <span className="block font-medium">{STEP_WORDS[step.to]}</span>
                        <span className="muted block text-xs leading-snug">{step.title}</span>
                      </span>
                    </NavLink>
                  </li>
                ))}
              </ul>
            </Dropdown>
            <TopLink to="/pulls">Pulls</TopLink>
            <TopLink to="/catalog">Catalog</TopLink>
          </nav>

          <div className="ml-auto flex items-center gap-1.5 md:ml-0">
            <div className="hidden md:block">
              <Dropdown
                label="Game"
                align="right"
                trigger={
                  <>
                    <GameMark game={active.id} size={22} />
                    <Icon name="chevronDown" size={14} />
                  </>
                }
              >
                <div className="w-64">{gameSwitch}</div>
              </Dropdown>
            </div>

            {me.data ? (
              <div className="hidden md:block">
                <Dropdown
                  label="Account"
                  align="right"
                  trigger={
                    <>
                      <Avatar name={me.data.displayName} picture={me.data.pictureUrl} />
                      <span className="hidden max-w-[8rem] truncate lg:inline">{me.data.displayName}</span>
                      <Icon name="chevronDown" size={14} />
                    </>
                  }
                >
                  <div className="w-64 space-y-3 p-1 text-sm">
                    <div className="font-medium">{me.data.displayName}</div>
                    {profilePicker}
                    <button type="button" className="btn-quiet w-full" onClick={leave} disabled={signingOut}>
                      <Icon name="signOut" size={16} /> Sign out
                    </button>
                  </div>
                </Dropdown>
              </div>
            ) : signedOut ? (
              <a className="btn btn-pill no-underline" href={signInUrl(location.pathname)}>
                Sign in
              </a>
            ) : null}

            <button
              type="button"
              className="icon-btn md:hidden"
              onClick={() => setOpen(!open)}
              aria-label={open ? 'Close menu' : 'Open menu'}
              aria-expanded={open}
            >
              <Icon name={open ? 'close' : 'menu'} />
            </button>
          </div>
        </div>

        {open && (
          <div className="md:hidden">
            <div className="sheet-scrim" onClick={() => setOpen(false)} aria-hidden="true" />
            <div className="sheet" role="dialog" aria-label="Menu">
              <nav className="grid grid-cols-2 gap-1" aria-label="Pages">
                <SheetLink to="/" icon="home" end>
                  Home
                </SheetLink>
                {STEPS.map((step) => (
                  <SheetLink key={step.to} to={step.to} icon={STEP_ICONS[step.to]}>
                    {STEP_WORDS[step.to]}
                  </SheetLink>
                ))}
                <SheetLink to="/pulls" icon="sparkle">
                  Pulls
                </SheetLink>
                <SheetLink to="/catalog" icon="book">
                  Catalog
                </SheetLink>
              </nav>
              {gameSwitch}
              {profilePicker}
              {me.data ? (
                <div className="flex items-center gap-2 text-sm">
                  <Avatar name={me.data.displayName} picture={me.data.pictureUrl} />
                  <span className="min-w-0 flex-1 truncate">{me.data.displayName}</span>
                  <button type="button" className="btn-quiet" onClick={leave} disabled={signingOut}>
                    <Icon name="signOut" size={16} /> Sign out
                  </button>
                </div>
              ) : null}
            </div>
          </div>
        )}
      </header>

      <div className="min-w-0 flex-1">
        <SyncBar sync={sync} />
        {waking && <WakingBar />}
        <main className="mx-auto max-w-5xl px-4 py-6 md:px-8 md:py-8">
          {isStep(location.pathname) && <StepBar />}
          {/*
            Each page rises in as it is opened (C2.6). Keyed by the first part
            of the path, so moving between two catalog pages is not a new page
            and keeps what it had.
          */}
          <div key={location.pathname.split('/')[1]} className="rise page">
            <Outlet />
          </div>
        </main>
      </div>

      <Footer unreachable={unreachable} />
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

/** What the menu calls each step: the page's name, with the step's own title under it. */
const STEP_WORDS: Record<StepPath, string> = {
  '/inventory': 'Inventory',
  '/roster': 'Roster',
  '/goals': 'Goals',
  '/plan': 'Plan',
};

/** Whether the page has scrolled past `threshold` pixels, read once a frame at most. */
function useScrolled(threshold: number): boolean {
  const [scrolled, setScrolled] = useState(false);
  useEffect(() => {
    let frame = 0;
    const read = () => {
      frame = 0;
      setScrolled(window.scrollY > threshold);
    };
    const onScroll = () => {
      if (!frame) frame = window.requestAnimationFrame(read);
    };
    read();
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => {
      window.removeEventListener('scroll', onScroll);
      if (frame) window.cancelAnimationFrame(frame);
    };
  }, [threshold]);
  return scrolled;
}

function useEscape(active: boolean, close: () => void) {
  useEffect(() => {
    if (!active) return;
    const onKey = (event: KeyboardEvent) => event.key === 'Escape' && close();
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [active, close]);
}

/**
 * A menu under a button in the bar. It opens on a click, and on hover where
 * there is a pointer that hovers, the way the bar it copies does; a click
 * outside, Escape or a navigation closes it.
 */
function Dropdown({
  label,
  trigger,
  here = false,
  align = 'center',
  children,
}: {
  label: string;
  trigger: ReactNode;
  here?: boolean;
  align?: 'center' | 'right';
  children: ReactNode;
}) {
  const [open, setOpen] = useState(false);
  const box = useRef<HTMLDivElement>(null);
  const location = useLocation();
  useEffect(() => setOpen(false), [location.pathname]);
  useEscape(open, () => setOpen(false));
  useEffect(() => {
    if (!open) return;
    const outside = (event: PointerEvent) => {
      if (box.current && !box.current.contains(event.target as Node)) setOpen(false);
    };
    document.addEventListener('pointerdown', outside);
    return () => document.removeEventListener('pointerdown', outside);
  }, [open]);

  // Hover opens only for a mouse: a tap fires pointerenter too. And the click
  // that follows a hover keeps the menu open rather than toggling it shut —
  // the pointer that opened it is usually on its way to clicking it.
  //
  // Leaving closes after a moment, not at once, so a pointer that slips off the
  // edge on its way down to an item finds the menu still there.
  const hovered = useRef(false);
  const closing = useRef<number | undefined>(undefined);
  useEffect(() => () => window.clearTimeout(closing.current), []);
  const hover = (next: boolean) => (event: React.PointerEvent) => {
    if (event.pointerType !== 'mouse') return;
    window.clearTimeout(closing.current);
    if (next) {
      hovered.current = true;
      setOpen(true);
    } else {
      closing.current = window.setTimeout(() => {
        hovered.current = false;
        setOpen(false);
      }, 150);
    }
  };
  const click = () => {
    if (hovered.current) {
      hovered.current = false;
      setOpen(true);
    } else {
      setOpen(!open);
    }
  };

  return (
    <div ref={box} className="relative" onPointerEnter={hover(true)} onPointerLeave={hover(false)}>
      <button
        type="button"
        className={`top-link ${here ? 'top-link-here' : ''}`}
        aria-expanded={open}
        aria-haspopup="true"
        aria-label={typeof trigger === 'string' ? undefined : label}
        onClick={click}
      >
        {trigger}
      </button>
      {open && (
        // The gap between button and menu is padding on this wrapper, not a
        // margin, so the pointer crossing it never leaves the dropdown.
        <div className={`absolute top-full z-50 pt-2 ${align === 'right' ? 'right-0' : 'left-1/2 -translate-x-1/2'}`}>
          <div className="menu-panel">{children}</div>
        </div>
      )}
    </div>
  );
}

/**
 * The way home. For a game with a look: its wordmark with the site's name
 * under it, and no mark (the maintainer, 2026-10-01). Once the bar gathers
 * into a pill, the wordmark squeezes away and the mark turns in where it
 * stood, and back again at the top; `looks.css` does it off the bar's
 * `data-scrolled`. Keyed by game, so switching game plays the letters in
 * again. A game with no look keeps the bolt beside the site's name.
 */
function Brand({ game }: { game: string | null }) {
  const look = lookOf(game);
  if (!look) {
    return (
      <NavLink
        to="/"
        className="flex shrink-0 items-center gap-2 text-base font-semibold no-underline"
        style={{ color: 'var(--ink)' }}
      >
        <GameMark game={game} size={32} />
        <span className="brand-word">Storm Almanac</span>
      </NavLink>
    );
  }
  return (
    <NavLink
      to="/"
      className="brand-morph flex shrink-0 items-center no-underline"
      style={{ color: 'var(--ink)' }}
      aria-label={`Storm Almanac · ${look.name}`}
    >
      <span className="brand-emblem" aria-hidden="true">
        <GameMark game={look.id} size={32} />
      </span>
      <span className="brand-word brand-lockup" aria-hidden="true">
        <Wordmark key={look.id} game={look.id} size="sm" />
        <span className="brand-site">Storm Almanac</span>
      </span>
    </NavLink>
  );
}

function TopLink({ to, end, children }: { to: string; end?: boolean; children: ReactNode }) {
  return (
    <NavLink to={to} end={end} className={({ isActive }) => `top-link ${isActive ? 'top-link-here' : ''}`}>
      {children}
    </NavLink>
  );
}

function SheetLink({ to, icon, end, children }: { to: string; icon: IconName; end?: boolean; children: ReactNode }) {
  return (
    <NavLink to={to} end={end} className={({ isActive }) => `nav-item ${isActive ? 'nav-item-here' : ''}`}>
      <Icon name={icon} />
      <span>{children}</span>
    </NavLink>
  );
}

/**
 * The reader's picture from their provider — Google's photo — or their initial
 * where there is none: a Discord account, the development sign-in, or a photo
 * that will not load. `no-referrer` because Google's photo host refuses some
 * requests that say which site asked. Decoration: the name beside it says who.
 */
function Avatar({ name, picture }: { name: string; picture?: string | null }) {
  const [failed, setFailed] = useState(false);
  if (picture && !failed) {
    return (
      <img
        src={picture}
        alt=""
        width={28}
        height={28}
        referrerPolicy="no-referrer"
        className="avatar h-7 w-7 shrink-0 rounded-full object-cover"
        onError={() => setFailed(true)}
      />
    );
  }
  return (
    <span
      className="inline-flex h-7 w-7 shrink-0 items-center justify-center rounded-full text-xs font-semibold"
      style={{ background: 'color-mix(in srgb, var(--brand) 18%, transparent)', color: 'var(--brand)' }}
      aria-hidden="true"
    >
      {name.trim().charAt(0).toUpperCase() || '?'}
    </span>
  );
}

function ProfilePicker({ profiles }: { profiles: Profile[] }) {
  const profileId = usePlannerStore((state) => state.profileId);
  const selectProfile = usePlannerStore((state) => state.selectProfile);
  const choose = useGameChoice((state) => state.choose);
  return (
    <select
      className="input w-full"
      value={profiles.some((profile) => profile.id === profileId) ? (profileId ?? '') : ''}
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
  );
}

/**
 * Choosing a game, from the switch or the first screen (C2.9): it is
 * remembered, and it picks the reader's profile for that game. With none, a
 * signed-in reader is taken Home with the form to make one open on that game —
 * the screens would otherwise be about a profile of another game under this
 * one's colours. A signed-out reader just gets the game: its catalog and its look.
 */
function usePickGame(profiles: Profile[], signedIn: boolean): (game: string) => void {
  const choose = useGameChoice((state) => state.choose);
  const profileId = usePlannerStore((state) => state.profileId);
  const selectProfile = usePlannerStore((state) => state.selectProfile);
  const navigate = useNavigate();

  return (game: string) => {
    choose(game);
    const profile = profileForGame(profiles, profileId, game);
    if (profile) selectProfile(profile.id);
    else if (signedIn) navigate(`/?new=${encodeURIComponent(game)}`);
  };
}

/**
 * Which game the site is about (C2.5, the maintainer's idea, in place of the
 * light / dark switch). Every game the server publishes is offered, each in
 * its own colours; a look marked upcoming that the server does not publish is
 * listed as coming soon and cannot be picked.
 */
function GameSwitch({
  active,
  games,
  pick,
}: {
  active: string | null;
  games: GameSummary[];
  pick: (game: string) => void;
}) {
  const soon = LOOKS.filter((look) => look.upcoming && !games.some((game) => game.id === look.id));
  if (games.length + soon.length < 2) return null;

  return (
    <div role="group" aria-label="Game" className="flex flex-col gap-1">
      <span className="label px-1">Game</span>
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
          <span className="min-w-0 truncate text-left">{game.displayName}</span>
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
          <span className="min-w-0 flex-1 truncate">{look.name}</span>
          <span className="text-[11px] font-medium uppercase tracking-wide">Soon</span>
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
/** Said while a sleeping server starts, so a page still filling in does not look broken. */
function WakingBar() {
  return (
    <div className="waking-bar" role="status">
      <div className="mx-auto flex max-w-5xl items-center gap-2 px-4 md:px-8">
        <span className="waking-dot" aria-hidden="true" />
        <span>
          Waking the server — it sleeps when nobody has used it for a while, and takes up to a minute to start.
          The page fills in on its own.
        </span>
      </div>
    </div>
  );
}

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
