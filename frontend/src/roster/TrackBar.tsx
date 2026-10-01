import type { ReactElement } from 'react';
import type { Track } from './tracks';

/**
 * One track as a bar: how far along it the reader is, and — when a goal is set —
 * how far the goal takes them (C2.3's goal-track bar, built in C2.5).
 *
 * <p><b>Positions, not costs.</b> A segment is a state's place on the track, so
 * Level 60 → 80 fills the last quarter of fourteen states whatever the EXP
 * between them costs. The plan says what the climb costs; this says where on
 * the ladder it is, which is what a reader setting a target is deciding.
 *
 * @param at the index the reader stands at on the track
 * @param to the index the goal reaches, when there is one
 */
export function TrackBar({ track, at, to }: { track: Track; at: number; to?: number }): ReactElement {
  const last = Math.max(1, track.states.length - 1);
  const share = (index: number) => `${(Math.max(0, Math.min(index, last)) / last) * 100}%`;
  return (
    <span
      className="relative block h-1.5 w-full overflow-hidden rounded-full"
      style={{ background: 'var(--line)' }}
      aria-hidden="true"
    >
      <span className="absolute inset-y-0 left-0" style={{ width: share(at), background: 'var(--muted)' }} />
      {to !== undefined && to > at && (
        <span
          className="absolute inset-y-0"
          style={{ left: share(at), width: `calc(${share(to)} - ${share(at)})`, background: 'var(--brand)' }}
        />
      )}
    </span>
  );
}
