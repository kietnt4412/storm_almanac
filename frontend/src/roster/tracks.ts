/**
 * An entity's upgrade graph, read as the separate tracks a reader stands on.
 *
 * <p><b>Why tracks.</b> A construct is on many ladders at once — a rank, a level,
 * an evolution, one per skill — and the game ties none of them together. Asking
 * "where are you" once per ladder is one dropdown each; asking it once for the
 * whole entity was one dropdown of every state on every ladder, 70 for one
 * Punishing: Gray Raven construct, read as raw ids. That was stall S2 of the
 * maintainer's rehearsal.
 *
 * <p><b>The tracks come from the graph, not from the names.</b> A track is a set
 * of states the steps connect, so this knows nothing about any game.
 *
 * <p><b>The words come from the game where the bundle has them</b> (ADR 0032,
 * since sequence 11): a state's name ("Elite ★3"), and the heading and tag its
 * track sits under ("Basic Skill", "Yellow Orb") — which is how a player finds
 * a skill, since most never read its name. <b>Where it has none, they are
 * guessed from the ids</b>, "Flaming chord · 4" off {@code flaming-chord-4}, by
 * taking the words every state on a track shares. The guess is display only and
 * never sent anywhere; the raw id is what every request carries.
 */

export interface TrackState {
  state: string;
  label: string;
}

export interface Track {
  name: string;
  /** In order along the track, the base first. */
  states: TrackState[];
  /** The heading the game shows the track under, when the bundle says. */
  section?: string;
  /** The game's bracketed kind for the track, such as an orb colour. */
  tag?: string;
}

/** A step as far as this file needs one: its edge, and the game's words if any. */
export interface LabelledStep {
  fromState: string;
  toState: string;
  fromName?: string | null;
  toName?: string | null;
  section?: string | null;
  tag?: string | null;
}

/**
 * Two different lists per entity, and conflating them was a real bug.
 *
 * <p>A <em>target</em> can only be a state some upgrade arrives at — aiming at a
 * state nothing reaches is a goal the solver can only refuse. A <em>current</em>
 * state can also be one nothing arrives at: the base of a track is a `fromState`
 * and never a `toState`, and a player sitting on it could not say so while these
 * were the same list. They only looked equivalent because "don't own her"
 * happens to resolve the same way on a track with one base.
 */
export function statesOfGraph(steps: { fromState: string; toState: string }[]): {
  targets: string[];
  starts: string[];
} {
  const targets: string[] = [];
  const starts: string[] = [];
  const remember = (list: string[], state: string) => {
    if (!list.includes(state)) list.push(state);
  };
  for (const step of steps) {
    remember(starts, step.fromState);
    remember(targets, step.toState);
    remember(starts, step.toState);
  }
  return { targets, starts };
}

/**
 * The connected pieces of the graph, each ordered from its base along its steps,
 * in the order the catalog lists them.
 *
 * <p>Several steps may join the same two states — one step at several prices is a
 * choice (ADR 0021) — and that is still one link. A state with two ways on is
 * walked breadth-first, which keeps a branch's states together without
 * pretending the branch is a line.
 */
export function tracksOfGraph(steps: LabelledStep[]): Track[] {
  const order = statesOfGraph(steps).starts;
  const next = new Map<string, string[]>();
  const arrivedAt = new Set<string>();
  const parent = new Map<string, string>(order.map((state) => [state, state]));
  const root = (state: string): string => {
    let at = state;
    while (parent.get(at) !== at) at = parent.get(at)!;
    return at;
  };

  for (const step of steps) {
    const onward = next.get(step.fromState) ?? [];
    if (!onward.includes(step.toState)) onward.push(step.toState);
    next.set(step.fromState, onward);
    arrivedAt.add(step.toState);
    parent.set(root(step.toState), root(step.fromState));
  }

  const pieces = new Map<string, string[]>();
  for (const state of order) {
    const piece = pieces.get(root(state)) ?? [];
    piece.push(state);
    pieces.set(root(state), piece);
  }

  return [...pieces.values()].map((piece) => {
    const walked: string[] = [];
    const queue = piece.filter((state) => !arrivedAt.has(state));
    while (queue.length > 0) {
      const state = queue.shift()!;
      if (walked.includes(state)) continue;
      walked.push(state);
      queue.push(...(next.get(state) ?? []));
    }
    // A cycle has no base; its states are still where the reader may stand.
    for (const state of piece) if (!walked.includes(state)) walked.push(state);

    const guessed = named(walked);
    const mine = steps.filter((step) => walked.includes(step.fromState));
    const said = mine.find((step) => step.section || step.tag);
    return {
      ...guessed,
      states: guessed.states.map((candidate) => ({
        state: candidate.state,
        label: gameName(steps, candidate.state) ?? candidate.label,
      })),
      ...(said?.section ? { section: said.section } : {}),
      ...(said?.tag ? { tag: said.tag } : {}),
    };
  });
}

/** What the game calls a state, from whichever step says: arriving at it or leaving it. */
function gameName(steps: LabelledStep[], state: string): string | undefined {
  for (const step of steps) {
    if (step.toState === state && step.toName) return step.toName;
    if (step.fromState === state && step.fromName) return step.fromName;
  }
  return undefined;
}

/** A heading and the tracks under it; a heading of undefined holds the tracks the bundle placed nowhere. */
export interface Section {
  name?: string;
  tracks: Track[];
}

/**
 * The tracks under their headings, in the game's order.
 *
 * <p>Order comes from {@code order}, the bundle's list, and not from the order
 * the steps arrive in: the leader skill's step comes before the skills', and its
 * heading comes after theirs on the game's screen. Tracks with no heading — every
 * track of every version before sequence 11, or a weapon nobody grouped — follow
 * in one untitled group, so nothing a reader recorded disappears.
 */
export function sectionsOf(tracks: Track[], order: string[] = []): Section[] {
  const headed = new Map<string, Track[]>();
  const loose: Track[] = [];
  for (const track of tracks) {
    if (!track.section) {
      loose.push(track);
      continue;
    }
    headed.set(track.section, [...(headed.get(track.section) ?? []), track]);
  }
  const names = [...order.filter((name) => headed.has(name)), ...[...headed.keys()].filter((name) => !order.includes(name))];
  const sections: Section[] = names.map((name) => ({ name, tracks: headed.get(name)! }));
  if (loose.length > 0) sections.push({ tracks: loose });
  return sections;
}

/** The track a state is on, if the graph has it. */
export function trackOf(tracks: Track[], state: string): Track | undefined {
  return tracks.find((track) => track.states.some((candidate) => candidate.state === state));
}

/**
 * Where the reader stands on one track: the furthest state recorded on it.
 *
 * <p>Furthest, because a roster may already hold two states of one track — the
 * old editor let a reader add both — and a crossed gate is a reached state
 * (ADR 0026), so the further one says everything the nearer one did.
 */
export function standingOn(track: Track, states: string[]): string | undefined {
  let at = -1;
  track.states.forEach((candidate, index) => {
    if (states.includes(candidate.state)) at = index;
  });
  return at >= 0 ? track.states[at]!.state : undefined;
}

/** The roster entry with this track's answer replaced and every other track's kept. */
export function placeOn(track: Track, states: string[], chosen: string): string[] {
  const mine = new Set(track.states.map((candidate) => candidate.state));
  return [...states.filter((state) => !mine.has(state)), chosen];
}

function named(states: string[]): Track {
  const words = states.map((state) => state.split('-'));
  // Every state keeps at least one word of its own, so none is labelled blank.
  const room = Math.min(...words.map((parts) => parts.length)) - 1;
  let shared = 0;
  while (shared < room && words.every((parts) => parts[shared] === words[0]![shared])) shared += 1;

  if (shared === 0) {
    return {
      name: readable(states[0]!.split('-')),
      states: states.map((state) => ({ state, label: readable(state.split('-')) })),
    };
  }
  return {
    name: readable(words[0]!.slice(0, shared)),
    states: states.map((state, index) => ({ state, label: readable(words[index]!.slice(shared)) })),
  };
}

function readable(parts: string[]): string {
  const text = parts.join(' ');
  // A grade written as one or two letters reads as a grade: "ss", not "Ss".
  if (/^[a-z]{1,2}$/.test(text)) return text.toUpperCase();
  return text.charAt(0).toUpperCase() + text.slice(1);
}
