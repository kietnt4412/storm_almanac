import type { ReactElement } from 'react';
import { targetOn, targetsBeyond, type GoalRow } from './goalRows';
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
}: {
  subject: string;
  tracks: Track[];
  order?: string[];
  targets: string[];
  roster: string[];
  row: GoalRow;
  onChange: (track: Track, state: string | null) => void;
}): ReactElement {
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
      <label key={track.states[0]!.state} className="flex items-center justify-between gap-3 text-sm">
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
    );
  };

  const known = new Set(tracks.flatMap((track) => track.states.map((candidate) => candidate.state)));
  const elsewhere = row.goals.map((goal) => goal.targetState).filter((state) => !known.has(state));

  return (
    <div className="grid gap-4 sm:grid-cols-2">
      {sectionsOf(tracks, order).map((section) => (
        <section key={section.name ?? ''} className="space-y-2">
          {section.name && <h3 className="label">{section.name}</h3>}
          {section.tracks.map(line)}
        </section>
      ))}
      {elsewhere.length > 0 && (
        <p className="muted text-xs sm:col-span-2">
          Also aiming at, on no track this patch publishes: {elsewhere.join(', ')}
        </p>
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
