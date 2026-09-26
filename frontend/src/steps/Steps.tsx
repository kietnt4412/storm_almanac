import { useState, type ReactElement } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';

/**
 * The four steps from nothing to a plan, in the one order every screen shows.
 *
 * <p>Until the maintainer's rehearsal (S3, 2026-09-25) the steps were cards on
 * the home page and nowhere else: a reader who had saved their goals was left on
 * a screen with no way on, and one who forgot what came next had to go back
 * home to read it. The step bar, each step's Next button and the home page's
 * cards now read this one list, so they cannot disagree about the order.
 *
 * <p><b>The roster is a step of its own.</b> The home page used to skip it, and
 * it is where the bill is decided: a construct nobody said was levelled is
 * charged for the whole ladder.
 */
export const STEPS = [
  {
    to: '/inventory',
    title: 'What you own',
    blurb: 'Your materials. Bulk entry, searchable, and it keeps working with no signal.',
  },
  {
    to: '/roster',
    title: 'Where they stand',
    blurb: 'How far each character already is, so nothing already done is charged again.',
  },
  {
    to: '/goals',
    title: 'What you want',
    blurb: 'For each of them, how far on each track. Order them by what matters.',
  },
  {
    to: '/plan',
    title: 'The plan',
    blurb: 'Stages and runs, with what the answer cost and what it could not prove.',
  },
] as const;

export function isStep(pathname: string): boolean {
  return STEPS.some((step) => step.to === pathname);
}

/** Every step, which one this is, and a way to any of them. */
export function StepBar(): ReactElement {
  return (
    <nav aria-label="Steps" className="mb-6">
      <ol className="flex flex-wrap gap-2 text-sm">
        {STEPS.map((step, index) => (
          <li key={step.to}>
            <NavLink
              to={step.to}
              className={({ isActive }) => `step ${isActive ? 'step-here' : ''}`}
            >
              <span className="step-n">{index + 1}</span>
              {step.title}
            </NavLink>
          </li>
        ))}
      </ol>
    </nav>
  );
}

/**
 * The way on from one step to the next.
 *
 * @param before what has to happen before leaving, if anything — the goal list
 *               is only a draft until it is saved, and leaving would lose it.
 *               If it fails the reader stays, and the screen shows why.
 * @param label  what the button says when leaving does more than leave
 */
export function NextStep({
  from,
  before,
  label,
}: {
  from: string;
  before?: () => Promise<unknown>;
  label?: string;
}): ReactElement | null {
  const navigate = useNavigate();
  const [going, setGoing] = useState(false);
  const at = STEPS.findIndex((step) => step.to === from);
  const next = STEPS[at + 1];
  const previous = STEPS[at - 1];

  return (
    <div className="flex flex-wrap items-center justify-between gap-3 border-t pt-4" style={{ borderColor: 'var(--line)' }}>
      {previous ? (
        <Link to={previous.to} className="text-sm">
          ← {previous.title}
        </Link>
      ) : (
        <span />
      )}
      {next &&
        (before ? (
          <button
            type="button"
            className="btn"
            disabled={going}
            onClick={async () => {
              setGoing(true);
              try {
                await before();
                navigate(next.to);
              } catch {
                // The screen owns the error; the reader stays to read it.
              } finally {
                setGoing(false);
              }
            }}
          >
            {going ? 'Saving…' : (label ?? `Next: ${next.title} →`)}
          </button>
        ) : (
          <Link to={next.to} className="btn no-underline">
            Next: {next.title} →
          </Link>
        ))}
    </div>
  );
}
