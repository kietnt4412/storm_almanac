export interface Health {
  status: string;
  service: string;
  version: string;
  time: string;
}

export interface Profile {
  id: string;
  game: string;
  region: string;
  displayName: string;
}

export interface Me {
  accountId: string;
  displayName: string;
  email: string;
  profiles: Profile[];
}

// ── Catalog ────────────────────────────────────────────────────────────────
//
// Public and anonymous: these are the routes a stranger arriving from a search
// reads. Everything below the divider after them needs an account.

export interface Rarity {
  label: string;
  rank: number;
}

export interface Version {
  sequence: number;
  label: string;
  publishedAt: string;
  attribution: string;
}

export interface GameSummary {
  id: string;
  displayName: string;
  /** The game's own word for energy — Activity, Serum, Vigour. */
  energyUnit: string;
  latest: Version;
}

export interface GamesResponse {
  games: GameSummary[];
}

/**
 * One sourcing record: how a reader could go and check a number themselves.
 *
 * `firstHand` arrives from the server rather than being derived here from
 * `origin`. Deriving it would be a second copy of the policy that lives in
 * `Provenance.Origin` — in a language that cannot be made to fail to compile
 * when the Java one gains a member, which is the whole reason it is one field
 * in one place.
 */
export interface Provenance {
  id: string;
  origin: string;
  firstHand: boolean;
  detail: string;
  observedOn: string;
}

/**
 * Where this response's numbers were read.
 *
 * `facts` maps `kind:slug` to the id of the record it was read under. A fact
 * that is not in the map is one nobody sourced — silence, which is the defined
 * meaning of an absent record rather than an omission to paper over.
 */
export interface Sourcing {
  sources: Provenance[];
  facts: Record<string, string>;
}

/*
 * Every response below marks `sourcing` optional, and today's server always
 * sends it. The optionality is not about the server being unsure — it is about
 * *which* server. The frontend and the backend are separate hosts (D1) and do
 * not deploy at the same instant, so every release has a window in which this
 * bundle is talking to an API that predates the field. A browser found that the
 * first time this shipped: the page rendered as nothing at all, because reading
 * through an absent object throws before React paints. Optional here is what
 * makes the compiler point at every place that has to cope.
 */

export interface Item {
  id: string;
  displayName: string;
  rarity: Rarity;
  category: string;
  /** The bundle's word for the category, which several categories may share (ADR 0033). */
  categoryName?: string;
}

export interface ItemsResponse {
  game: string;
  version: Version;
  items: Item[];
  sourcing?: Sourcing;
}

export interface EntitySummary {
  id: string;
  displayName: string;
  kind: string;
  rarity: Rarity;
  element: string | null;
  tags: string[];
  /** The bundle's word for the kind, "Construct" for character (ADR 0033). */
  kindName?: string;
}

export interface EntitiesResponse {
  game: string;
  version: Version;
  entities: EntitySummary[];
  sourcing?: Sourcing;
}

export interface Cost {
  item: string;
  displayName: string;
  quantity: number;
}

export interface SkillRank {
  rank: number;
  description: string;
  values: Record<string, number>;
  upgradeCost: Cost[];
}

export interface EntityDetail extends EntitySummary {
  statCurves: { stat: string; breakpoints: { ascensionTier: number; level: number; value: number }[] }[];
  skills: { id: string; displayName: string; ranks: SkillRank[] }[];
  talents: { id: string; displayName: string; unlockCondition: string; effect: string }[];
  /** The bundle's word for the kind (ADR 0033); absent from an older server. */
  kindName?: string;
}

export interface EntityResponse {
  game: string;
  version: Version;
  entity: EntityDetail;
  sourcing?: Sourcing;
}

/**
 * One rung of a scored ladder: the bar, and what clearing it pays.
 *
 * The grants are why this is worth fetching rather than assumed. A measure is an
 * opaque slug the bundle supplies — `phantom-pain-cage-score` — and no reader
 * has ever seen that string; what they recognise is the nine Scars at the top of
 * it. ADR 0022, and the `measures` route that exists for this screen.
 */
export interface MeasureBar {
  reward: string;
  atLeast: number;
  cadence: string;
  grants: Cost[];
}

export interface Measure {
  measure: string;
  /** The bundle's word for it (ADR 0033); absent from an older server, the measure itself before sequence 13. */
  displayName?: string;
  bars: MeasureBar[];
}

export interface MeasuresResponse {
  game: string;
  version: Version;
  measures: Measure[];
}

export interface UpgradeStep {
  id: string;
  fromState: string;
  toState: string;
  costs: Cost[];
  /**
   * What the game calls the two states, and the heading and tag the step's
   * track sits under (ADR 0032). Null or absent when the bundle gave none —
   * every version before sequence 11, and a server older than this page.
   */
  fromName?: string | null;
  toName?: string | null;
  section?: string | null;
  tag?: string | null;
}

export interface UpgradesResponse {
  game: string;
  version: Version;
  entity: EntitySummary;
  steps: UpgradeStep[];
  totalCost: Cost[];
  sourcing?: Sourcing;
  /** The game's order for the headings its steps name; absent before sequence 11. */
  sections?: string[];
}

// ── Player state ───────────────────────────────────────────────────────────

export interface InventoryResponse {
  profile: string;
  items: Record<string, number>;
}

export interface RosterResponse {
  profile: string;
  entities: Record<string, string[]>;
}

export interface Goal {
  entity: string;
  targetState: string;
  satisfiability?: string;
  priority?: number;
}

export interface GoalsResponse {
  profile: string;
  goals: Goal[];
}

/** One item's edit, with the client clock that decides a tie. */
export interface InventoryEdit {
  quantity: number;
  at: string;
}

export interface InventoryPatchResponse {
  profile: string;
  items: Record<string, number>;
  applied: string[];
  /** The keys whose edits lost the merge. Never silently dropped. */
  rejected: string[];
}

export interface RosterPatchResponse {
  profile: string;
  entities: Record<string, string[]>;
  applied: string[];
  rejected: string[];
}

/**
 * What one more of a thing would cost, with something to call it.
 *
 * A list rather than the `Record<string, number>` this was: a demand line can
 * stand for something the item table has no row for — EXP, or a step offered at
 * several prices — so the key is not always a name, and `character-exp` rendered
 * beside a properly named `Cogs`.
 */
export interface ShadowPrice {
  item: string;
  displayName: string;
  price: number;
}

export interface Plan {
  id: string;
  profile: string;
  game: string;
  version: number;
  versionLabel: string;
  attribution: string;
  objective: string;
  /*
    Each line's name sits beside its id, as a shadow price's does. The name is
    optional on the wire because the page and the API deploy at different
    instants, and a page that meets an older server shows the id rather than a
    blank row.
  */
  stages: { stage: string; displayName?: string; runs: number; energyCost: number; totalEnergy: number }[];
  conversions: { step: string; displayName?: string; times: number }[];
  rewards: { reward: string; displayName?: string; times: number }[];
  totalEnergy: number;
  etaDays: number;
  shadowPrice: ShadowPrice[];
  bindingStages: string[];
  notes: string[];
  /*
    The upgrade steps the goals pay for, as data so the page can name each
    state the way the goal and roster screens do. Absent from a server older
    than this page, which said it as a note instead; the notes still render.
  */
  payingFor?: PayingFor[];
  computedAt: string;
}

export interface PayingFor {
  step: string;
  /** Null only for a step the plan's version does not know, which it cannot produce. */
  entity: string | null;
  entityName: string | null;
  fromState: string | null;
  toState: string | null;
  /** The server's unguessed name, "Lucia: Inverse Crown to abyssal-lament-18". */
  displayName: string;
}

export interface ShortfallLine {
  item: string;
  displayName: string;
  required: number;
  owned: number;
  missing: number;
}

export interface Shortfall {
  profile: string;
  game: string;
  version: number;
  versionLabel: string;
  attribution: string;
  entity: string;
  currentStates: string[];
  targetState: string;
  alreadyMet: boolean;
  steps: string[];
  items: ShortfallLine[];
  complete: boolean;
}

const BASE = import.meta.env.VITE_API_BASE ?? '';

/**
 * The CSRF token the server put in a cookie, echoed back in a header.
 *
 * The server issues it on every response rather than lazily, so by the time a
 * write happens the cookie is there — see SecurityConfig.browserCsrf. Reading a
 * cookie is the whole mechanism: another origin's page cannot, which is why the
 * token being readable here is not a weakening.
 */
function csrfToken(): string {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);
  return match?.[1] ? decodeURIComponent(match[1]) : '';
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await fetch(`${BASE}${path}`, {
    ...init,
    headers: {
      Accept: 'application/json',
      ...(init.body ? { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': csrfToken() } : {}),
      ...init.headers,
    },
    credentials: 'include',
  });
  if (!response.ok) {
    throw new ApiError(path, response.status, await detailOf(response));
  }
  return response.json() as Promise<T>;
}

/**
 * The sentence the server wrote about its own refusal.
 *
 * Every refusal on this API is an RFC 9457 problem document whose `detail` was
 * written to be acted on — "no upgrades for entity X in proving-ground 1.0"
 * rather than a bare 422. Throwing that away and rendering the status code
 * would undo the whole reason ApiExceptionHandler exists.
 */
async function detailOf(response: Response): Promise<string | undefined> {
  try {
    const problem = await response.clone().json();
    return typeof problem?.detail === 'string' ? problem.detail : undefined;
  } catch {
    return undefined;
  }
}

/**
 * A failed request that still says what failed.
 *
 * The status matters to a caller in exactly one case and it is the common one:
 * 401 is "not signed in", which is a state the interface renders rather than an
 * error it reports. Everything else is a genuine failure.
 */
export class ApiError extends Error {
  constructor(readonly path: string, readonly status: number, readonly detail?: string) {
    super(detail ?? `${path} responded ${status}`);
  }

  get isSignedOut(): boolean {
    return this.status === 401;
  }

  /** The goal is understood and has no answer — an unreachable state, an unsourceable item. */
  get isUnanswerable(): boolean {
    return this.status === 422;
  }

  /**
   * The server read the request and said no. Asking again gets the same answer,
   * so a query should show it rather than retry: the character page once retried
   * a 400 with no limit and sat on "Working it out…" for good.
   */
  get isRefusal(): boolean {
    return this.status >= 400 && this.status < 500;
  }
}

const versioned = (path: string, version?: number) =>
  version === undefined ? path : `${path}${path.includes('?') ? '&' : '?'}version=${version}`;

export const getHealth = () => request<Health>('/api/health');

export const getMe = () => request<Me>('/api/me');

export const createProfile = (game: string, region: string, displayName: string) =>
  request<Profile>('/api/me/profiles', {
    method: 'POST',
    body: JSON.stringify({ game, region, displayName }),
  });

export const getGames = () => request<GamesResponse>('/api/games');

export const getItems = (game: string, version?: number) =>
  request<ItemsResponse>(versioned(`/api/games/${game}/items`, version));

export const getEntities = (game: string, version?: number) =>
  request<EntitiesResponse>(versioned(`/api/games/${game}/entities`, version));

/**
 * What this game scores a grant on, and the bars it pays at.
 *
 * The read side of `reach`. Nothing else on this API says which measures exist —
 * a measure is a label on a reward and is declared nowhere — so a screen cannot
 * ask the reader how far they get without asking this first.
 */
export const getMeasures = (game: string, version?: number) =>
  request<MeasuresResponse>(versioned(`/api/games/${game}/measures`, version));

export const getEntity = (game: string, entity: string, version?: number) =>
  request<EntityResponse>(versioned(`/api/games/${game}/entities/${entity}`, version));

export const getUpgrades = (game: string, entity: string, version?: number) =>
  request<UpgradesResponse>(versioned(`/api/games/${game}/entities/${entity}/upgrades`, version));

export const getInventory = (profile: string) =>
  request<InventoryResponse>(`/api/me/profiles/${profile}/inventory`);

/**
 * Merge this device's edits, key by key.
 *
 * PATCH and never PUT, and that is the difference between a phone that was
 * offline for an hour and one that silently deletes what a browser added in the
 * meantime. The timestamps are the client's own and are required: a
 * server-stamped edit would win for having arrived late, which is the bug the
 * route exists to fix.
 */
export const patchInventory = (profile: string, items: Record<string, InventoryEdit>) =>
  request<InventoryPatchResponse>(`/api/me/profiles/${profile}/inventory`, {
    method: 'PATCH',
    body: JSON.stringify({ items }),
  });

export const getRoster = (profile: string) =>
  request<RosterResponse>(`/api/me/profiles/${profile}/roster`);

export const patchRoster = (profile: string, entities: Record<string, { states: string[] | null; at: string }>) =>
  request<RosterPatchResponse>(`/api/me/profiles/${profile}/roster`, {
    method: 'PATCH',
    body: JSON.stringify({ entities }),
  });

export const getGoals = (profile: string) => request<GoalsResponse>(`/api/me/profiles/${profile}/goals`);

/**
 * Goals are replaced whole, because they are an ordered list and an order has
 * no per-key merge. The server has no PATCH here and that is a decision rather
 * than a gap — see PlayerController.
 */
export const saveGoals = (profile: string, goals: Goal[]) =>
  request<GoalsResponse>(`/api/me/profiles/${profile}/goals`, {
    method: 'PUT',
    body: JSON.stringify({ goals }),
  });

/**
 * `reach` is what the reader says they clear, by measure.
 *
 * Absent or empty counts none of the scored grants, which is the honest default
 * rather than the generous one: a plan that counted a weekly the reader cannot
 * actually clear promises income that never arrives. Being wrong this way makes
 * the plan dearer than the truth, and the server's notes name every grant it
 * left out. ADR 0022.
 */
export const solve = (
  profile: string,
  body: { energyPerDay: number; horizonDays?: number; objective?: string; reach?: Record<string, number> },
  version?: number,
) =>
  request<Plan>(versioned(`/api/me/profiles/${profile}/plan`, version), {
    method: 'POST',
    body: JSON.stringify(body),
  });

/**
 * What this reader is still short of for one entity at one target state.
 *
 * `game` is the game the page is showing. An entity id means something only
 * inside one game, and the server refuses a profile of another game by name
 * rather than answering about whatever that game calls the same id.
 */
export const getShortfall = (profile: string, game: string, entity: string, target: string, version?: number) =>
  request<Shortfall>(
    versioned(
      `/api/me/profiles/${profile}/shortfall?game=${encodeURIComponent(game)}&entity=${encodeURIComponent(entity)}&target=${encodeURIComponent(target)}`,
      version,
    ),
  );

/**
 * Where the sign-in button goes.
 *
 * In a production build `import.meta.env.DEV` is `false` and Vite eliminates the
 * other branch, so the development URL is not merely unused in the deployed
 * bundle — it is not in it. That is the same guarantee the backend gives by
 * keeping the development sign-in out of the deployable jar, made on the same
 * principle: a back door that can be reached by changing a setting is a back
 * door.
 *
 * Neither URL is a fetch. Both are full-page navigations, because an OAuth
 * redirect cannot be followed by XHR and the development one is deliberately the
 * same shape.
 *
 * Both carry `then`, the page to come back to. The provider's took it only from
 * 2026-09-24 (Q6) — until then every real sign-in ended on `/` — and the server
 * holds it across the round trip to Google and refuses anything off this site.
 */
export const signInUrl = (then = '/'): string =>
  import.meta.env.DEV
    ? `/dev/sign-in?as=dev&then=${encodeURIComponent(then)}`
    : `/oauth2/authorization/google?then=${encodeURIComponent(then)}`;

/**
 * Ends the session on the server.
 *
 * A POST carrying the CSRF token, not a link: a sign-out anybody's page could
 * trigger with an `<img>` is a nuisance an attacker gets for free. The server
 * answers 204 and says nothing about where to go, so the caller decides. Until
 * B5 this was a development-only link and the deployed product had no sign-out
 * at all — `signOutUrl()` returned null outside `DEV`, and the page drew nothing.
 */
export async function signOut(): Promise<void> {
  const response = await fetch(`${BASE}/logout`, {
    method: 'POST',
    headers: { 'X-XSRF-TOKEN': csrfToken() },
    credentials: 'include',
  });
  if (!response.ok) {
    throw new ApiError('/logout', response.status, await detailOf(response));
  }
}
