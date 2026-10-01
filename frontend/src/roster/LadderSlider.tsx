import type { ReactElement } from 'react';
import type { Track } from './tracks';

/**
 * One track as a ladder the reader drags a target along (C2.8, in place of the
 * target dropdown).
 *
 * <p>It is a native range input underneath, drawn over: the arrow keys, Home
 * and End move the target, a screen reader hears the same label the dropdown
 * had, and the state's name as the value. The grey part is where the reader
 * already stands; the coloured part is how far the goal takes them.
 *
 * <p><b>Only a state some step arrives at can be a target</b>, as the dropdown
 * offered (`targetsBeyond`). Landing between two is snapped forward to the
 * next one, and dragging back to where the reader stands means "leave as is".
 *
 * @param at       the index the reader stands at
 * @param chosen   the index of the target, when one is set and still ahead
 * @param allowed  the indices a target may sit at, all after {@code at}
 */
export function LadderSlider({
  track,
  at,
  chosen,
  allowed,
  label,
  onChange,
}: {
  track: Track;
  at: number;
  chosen?: number;
  allowed: number[];
  label: string;
  onChange: (state: string | null) => void;
}): ReactElement {
  const last = Math.max(1, track.states.length - 1);
  const share = (index: number) => (Math.max(0, Math.min(index, last)) / last) * 100;
  const value = chosen !== undefined && chosen > at ? chosen : at;
  const families = familiesOf(track);

  const pick = (index: number) => {
    if (index <= at) return onChange(null);
    const forward = allowed.find((candidate) => candidate >= index);
    const target = forward ?? allowed.at(-1);
    onChange(target === undefined ? null : track.states[target]!.state);
  };

  return (
    <div>
      <div className="ladder">
        <span className="ladder-rail" aria-hidden="true" />
        <span className="ladder-have" style={{ width: `${share(at)}%` }} aria-hidden="true" />
        {value > at && (
          <span
            className="ladder-want"
            style={{ left: `${share(at)}%`, width: `${share(value) - share(at)}%` }}
            aria-hidden="true"
          />
        )}
        {track.states.length <= 40 &&
          track.states.slice(1, -1).map((candidate, index) => (
            <span key={candidate.state} className="ladder-pip" style={{ left: `${share(index + 1)}%` }} aria-hidden="true" />
          ))}
        <span className="ladder-thumb" style={{ left: `${share(value)}%` }} aria-hidden="true" />
        <input
          type="range"
          min={0}
          max={track.states.length - 1}
          step={1}
          value={value}
          aria-label={label}
          aria-valuetext={value > at ? track.states[value]!.label : `leave as is, now ${track.states[at]!.label}`}
          onChange={(event) => pick(Number(event.target.value))}
        />
      </div>
      {families && (
        <div className="ladder-families" aria-hidden="true">
          {families.map((family, index) => (
            <span key={`${family.name}-${index}`} style={{ flex: family.size }}>
              {family.name}
            </span>
          ))}
        </div>
      )}
    </div>
  );
}

/**
 * Runs of states that share a name but their last word, "Elite ★1", "Elite ★2",
 * as one label under the ladder; null when that names nothing new, as on a
 * skill whose states are bare levels. Off the labels only, so no game's ranks
 * are known here.
 */
export function familiesOf(track: Track): { name: string; size: number }[] | null {
  const families: { name: string; size: number }[] = [];
  for (const candidate of track.states) {
    const words = candidate.label.split(' ');
    const name = words.length > 1 ? words.slice(0, -1).join(' ') : candidate.label;
    const previous = families.at(-1);
    if (previous && previous.name === name) previous.size += 1;
    else families.push({ name, size: 1 });
  }
  return families.length >= 2 && families.length < track.states.length ? families : null;
}
