import type { ReactElement } from 'react';

/**
 * Where one entity stands, edited: the states it has reached, as removable
 * chips, plus a dropdown of the ones it has not.
 *
 * <p><b>Several at once, because an entity is on several tracks and the game
 * ties none of them together.</b> A Punishing: Gray Raven construct has a level,
 * a rank, an evolution and six skills, and a reader may sit at Lv 80 and rank 0
 * — the game permits it. A single select would make recording the second erase
 * the first, and the planner would then charge them for a ladder they have
 * already climbed. ADR 0027 is the server's half of that.
 *
 * <p><b>Chips and a dropdown rather than a `select multiple`.</b> The multi-select
 * was tried first and shipped, passing every frontend test, and was unusable:
 * the launch bundle publishes 68 start states, a list box shows four of them at a
 * time, clearing one needs a ctrl-click nobody discovers, and what is currently
 * chosen cannot be seen without scrolling. A dropdown of what is <em>not</em> yet
 * chosen keeps a familiar control and stays type-ahead searchable; the chips are
 * the answer to "where am I".
 *
 * <p><b>Empty is "not on the roster", and that is one meaning rather than two.</b>
 * Removing the last chip reports `null`, which the API refuses to confuse with an
 * empty list — see V13 and the roster PATCH.
 *
 * @param subject   what this entity is called, for the labels a screen reader
 *                  reads out. Two of these on one page are otherwise the same
 *                  control twice
 * @param choices   every state the reader could say they are at, which is not
 *                  the same list as the states they could aim at: the base of a
 *                  track is a `fromState` and never a `toState`
 * @param emptyWord what the dropdown says when nothing is chosen. The two
 *                  screens that use this are asking slightly different
 *                  questions — "where are you with her" and "what have you got"
 */
export function StateChips({
  subject,
  states,
  choices,
  emptyWord,
  onChange,
}: {
  subject: string;
  states: string[];
  choices: string[];
  emptyWord: string;
  onChange: (states: string[] | null) => void;
}): ReactElement {
  return (
    <span className="flex flex-wrap items-center gap-1">
      {states.map((state) => (
        <button
          key={state}
          type="button"
          className="chip"
          aria-label={`Remove ${state} from ${subject}`}
          title={`No longer at ${state}`}
          onClick={() => {
            const left = states.filter((held) => held !== state);
            onChange(left.length > 0 ? left : null);
          }}
        >
          {state}
          <span className="chip-x" aria-hidden="true">
            ✕
          </span>
        </button>
      ))}

      <select
        className="input"
        value=""
        aria-label={`Add a current state for ${subject}`}
        onChange={(event) => {
          if (!event.target.value) return;
          onChange([...states, event.target.value]);
        }}
      >
        <option value="">{states.length > 0 ? 'and also…' : emptyWord}</option>
        {choices
          .filter((state) => !states.includes(state))
          .map((state) => (
            <option key={state} value={state}>
              {state}
            </option>
          ))}
      </select>
    </span>
  );
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
