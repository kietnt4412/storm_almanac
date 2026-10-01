import { useState, type ReactElement } from 'react';
import { targetOn, targetsBeyond, type GoalRow } from './goalRows';
import { TrackBar } from './TrackBar';
import { sectionsOf, standingOn, type Track } from './tracks';

/**
 * Where one entity is going, edited: one dropdown per track, under the game's
 * own headings — the roster's {@link TrackPicker}, asked forwards.
 *
 * <p>S7 of the maintainer's second rehearsal: "Lucia fully built" was about
 * twelve goal rows, each picked from one flat list of 61 states. Now it is one
 * row, and a track left alone is simply not a goal — "leave as is" is the
 * default and sends nothing, so a reader sets the three tracks they care about
 * and ignores the other ten.
 *
 * <p><b>Only what is ahead is offered.</b> A track lists the states after where
 * the reader stands on it, so the dropdown cannot aim a goal at something
 * already done, which the solver would only report back as "already met". Where
 * they stand is shown beside it, since that is what decides what is ahead. A
 * target already set and since overtaken stays on its list, marked, rather than
 * vanishing from under the reader.
 *
 * <p><b>Only what is being moved is shown, once something is</b> (C2.5). A goal of
 * four tracks read like a goal of thirteen while every track showed its
 * dropdown; now the tracks with a target show, each with a bar from where the
 * reader stands to where the goal takes them, and the rest fold behind one
 * line. A row with nothing set shows everything, since that is the question.
 * A section can be sent to the end of every track in it at once ("Max").
 *
 * @param targets every state some step arrives at, for this entity
 * @param roster  where the reader stands, the roster's states for this entity
 */
export function TargetPicker({
  subject,
  tracks,
  order,
  targets,
  roster,
  row,
  onChange,
  onChangeMany,
}: {
  subject: string;
  tracks: Track[];
  order?: string[];
  targets: string[];
  roster: string[];
  row: GoalRow;
  onChange: (track: Track, state: string | null) => void;
  /** Several tracks set at once; without it there is no "Max". */
  onChangeMany?: (changes: { track: Track; state: string }[]) => void;
}): ReactElement {
  // Folded only as it opens: a row being set up stays whole while the reader
  // works on it, rather than hiding each track the moment another is set.
  const [showAll, setShowAll] = useState(() => row.goals.length === 0);
  const targeted = (track: Track) => targetOn(track, row) !== undefined;
  const folding = !showAll;
  const shown = (track: Track) => !folding || targeted(track);
  const hidden = tracks.filter((track) => !shown(track)).length;
  const indexOf = (track: Track, state: string | undefined) =>
    state === undefined ? 0 : Math.max(0, track.states.findIndex((candidate) => candidate.state === state));

  const line = (track: Track) => {
    const ahead = targetsBeyond(track, roster, targets);
    if (ahead.length === 0 && targetOn(track, row) === undefined) {
      // The end of the track is behind them: nothing to aim at, and a
      // dropdown holding only "leave as is" is a question with no answers.
      return (
        <div key={track.states[0]!.state} className="flex items-center justify-between gap-3 text-sm">
          <TrackName track={track} />
          <span className="muted text-xs">done</span>
        </div>
      );
    }
    const chosen = targetOn(track, row);
    const overtaken = chosen !== undefined && !ahead.some((candidate) => candidate.state === chosen);
    const at = standingOn(track, roster) ?? track.states[0]!.state;
    const atLabel = track.states.find((candidate) => candidate.state === at)?.label ?? at;
    return (
      <div key={track.states[0]!.state} className="space-y-1">
        <label className="flex items-center justify-between gap-3 text-sm">
          <TrackName track={track} />
          <span className="flex shrink-0 items-center gap-2">
            <span className="muted text-xs">now {atLabel} →</span>
            <select
              className="input"
              value={chosen ?? ''}
              aria-label={`Target for ${track.tag ? `${track.tag}, ${track.name}` : track.name} of ${subject}`}
              onChange={(event) => onChange(track, event.target.value === '' ? null : event.target.value)}
            >
              <option value="">leave as is</option>
              {overtaken && (
                <option value={chosen}>
                  {track.states.find((candidate) => candidate.state === chosen)?.label ?? chosen} (reached)
                </option>
              )}
              {ahead.map((candidate) => (
                <option key={candidate.state} value={candidate.state}>
                  {candidate.label}
                </option>
              ))}
            </select>
          </span>
        </label>
        {chosen !== undefined && <TrackBar track={track} at={indexOf(track, at)} to={indexOf(track, chosen)} />}
      </div>
    );
  };

  // The end of every track in a section that still has somewhere to go.
  const maxOf = (section: Track[]) =>
    section.flatMap((track) => {
      const last = targetsBeyond(track, roster, targets).at(-1);
      return last && targetOn(track, row) !== last.state ? [{ track, state: last.state }] : [];
    });

  const known = new Set(tracks.flatMap((track) => track.states.map((candidate) => candidate.state)));
  const elsewhere = row.goals.map((goal) => goal.targetState).filter((state) => !known.has(state));

  return (
    <div className="space-y-3">
      <div className="grid gap-4 sm:grid-cols-2">
        {sectionsOf(tracks, order).map((section) => {
          const visible = section.tracks.filter(shown);
          if (visible.length === 0) return null;
          const max = maxOf(section.tracks);
          return (
            <section key={section.name ?? ''} className="space-y-2">
              <div className="flex items-center justify-between gap-2">
                {section.name && <h3 className="label">{section.name}</h3>}
                {onChangeMany && max.length > 1 && (
                  <button
                    type="button"
                    className="text-xs font-medium"
                    style={{ color: 'var(--brand)' }}
                    aria-label={`Set every ${section.name ?? ''} track of ${subject} to its end`}
                    onClick={() => onChangeMany(max)}
                  >
                    Max
                  </button>
                )}
              </div>
              {visible.map(line)}
            </section>
          );
        })}
      </div>
      {folding && hidden > 0 && (
        <button type="button" className="muted text-sm" onClick={() => setShowAll(true)}>
          + {hidden} more track{hidden === 1 ? '' : 's'}, left as they are
        </button>
      )}
      {showAll && row.goals.length > 0 && (
        <button type="button" className="muted text-sm" onClick={() => setShowAll(false)}>
          Show only the tracks being moved
        </button>
      )}
      {elsewhere.length > 0 && (
        <p className="muted text-xs">Also aiming at, on no track this patch publishes: {elsewhere.join(', ')}</p>
      )}
    </div>
  );
}

function TrackName({ track }: { track: Track }): ReactElement {
  return (
    <span className="min-w-0">
      <span>{track.tag ?? track.name}</span>
      {track.tag && <span className="muted ml-2 text-xs">{track.name}</span>}
    </span>
  );
}
