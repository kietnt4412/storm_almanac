import type { ReactElement } from 'react';
import { placeOn, standingOn, type Track } from './tracks';

/**
 * Where one entity stands, edited: one dropdown per track.
 *
 * <p><b>This replaced a row of chips and an "and also…" dropdown</b> holding
 * every state on every track, which the maintainer's rehearsal could not read
 * (S2, 2026-09-25). Before that it was a `select multiple`, which passed every
 * test and was unusable. jsdom computes no layout, so whether this one reads is
 * a person's job with a browser; what the tests pin is what it sends.
 *
 * <p><b>One answer per track, and it is the furthest one.</b> Being at rank 5
 * means ranks 0 to 4 are behind you, and the planner already credits a crossed
 * gate as reached (ADR 0026), so a tick per state would ask for 70 answers where
 * 13 say the same. A track nothing was recorded on shows its base, which is what
 * the planner charges from; choosing the base records it, so a reader can say
 * "I own her, untouched" and have that be different from not owning her.
 *
 * <p>States the roster holds that are on none of these tracks — a patch that
 * renamed one — are kept untouched in every answer this sends, and named, so an
 * edit here never silently drops something another screen recorded.
 *
 * @param tracks every track the entity has, which is what decides whether a
 *               recorded state is on one
 * @param only   the tracks to show, when not all of them — a goal row shows the
 *               goal's own. Without the split, the goal row called the reader's
 *               other answers "on no track", which the first browser run caught
 */
export function TrackPicker({
  subject,
  tracks,
  only,
  states,
  onChange,
}: {
  subject: string;
  tracks: Track[];
  only?: Track[];
  states: string[];
  onChange: (states: string[]) => void;
}): ReactElement {
  const known = new Set(tracks.flatMap((track) => track.states.map((candidate) => candidate.state)));
  const elsewhere = states.filter((state) => !known.has(state));

  return (
    <div className={only ? 'flex flex-wrap gap-2' : 'grid grow gap-x-4 gap-y-2 sm:grid-cols-2 lg:grid-cols-3'}>
      {(only ?? tracks).map((track) => (
        <label key={track.states[0]!.state} className="flex items-center justify-between gap-2 text-sm">
          <span className="muted">{track.name}</span>
          <select
            className="input"
            value={standingOn(track, states) ?? track.states[0]!.state}
            aria-label={`${track.name} for ${subject}`}
            onChange={(event) => onChange(placeOn(track, states, event.target.value))}
          >
            {track.states.map((candidate) => (
              <option key={candidate.state} value={candidate.state}>
                {candidate.label}
              </option>
            ))}
          </select>
        </label>
      ))}
      {elsewhere.length > 0 && (
        <p className="muted text-xs sm:col-span-2 lg:col-span-3">
          Also recorded, on no track this patch publishes: {elsewhere.join(', ')}
        </p>
      )}
    </div>
  );
}
