import type { ReactElement } from 'react';
import { CostLine } from './CostLine';
import { familiesOf } from './LadderSlider';
import { rangeCost, type CostedStep } from './rangeCost';
import { sectionsOf, standingOn, type Track } from './tracks';

/**
 * One construct on the roster as a card (C2.8, agreed 2026-10-01): each track
 * as a small ladder filled to where the reader stands, and a ring for how far
 * up every track they are together.
 *
 * <p><b>Positions, not costs</b>, as the goal bars of C2.5 were: a square is a state.
 * A long track (a level ladder of a dozen links is fine; one of a hundred is
 * not) is a bar instead of squares, so a card stays a card.
 *
 * <p><b>A square not yet reached says its price</b> on hover and on focus, the
 * step into it by its own costs ({@link rangeCost}, the same numbers as the
 * goal screen's sum), so a reader deciding what to aim at can see what the
 * next rung asks without leaving the roster.
 */
const SQUARES_UP_TO = 24;

export function completionOf(tracks: Track[], states: string[]): { climbed: number; height: number } {
  let climbed = 0;
  let height = 0;
  for (const track of tracks) {
    const at = standingOn(track, states);
    climbed += at === undefined ? 0 : Math.max(0, track.states.findIndex((candidate) => candidate.state === at));
    height += Math.max(0, track.states.length - 1);
  }
  return { climbed, height };
}

export function CompletionRing({ climbed, height }: { climbed: number; height: number }): ReactElement {
  const share = height === 0 ? 0 : climbed / height;
  const r = 22;
  const around = 2 * Math.PI * r;
  const percent = Math.round(share * 100);
  return (
    <svg width="56" height="56" viewBox="0 0 56 56" role="img" aria-label={`${percent}% of the way up every track`}>
      <circle cx="28" cy="28" r={r} fill="none" stroke="var(--line)" strokeWidth="6" />
      {share > 0 && (
        <circle
          cx="28"
          cy="28"
          r={r}
          fill="none"
          stroke="var(--brand)"
          strokeWidth="6"
          strokeLinecap="round"
          strokeDasharray={`${around * share} ${around}`}
          transform="rotate(-90 28 28)"
        />
      )}
      <text x="28" y="32" textAnchor="middle" fontSize="12" fontWeight="700" fill="var(--ink)">
        {percent}%
      </text>
    </svg>
  );
}

export function TrackLadders({
  tracks,
  order,
  states,
  steps,
}: {
  tracks: Track[];
  order?: string[];
  states: string[];
  steps?: CostedStep[];
}): ReactElement {
  const ordered = sectionsOf(tracks, order).flatMap((section) => section.tracks);
  return (
    <div className="grid grid-cols-[minmax(4.5rem,7rem)_1fr] items-center gap-x-3 gap-y-1.5 text-xs">
      {ordered.map((track) => {
        const at = standingOn(track, states);
        const index = at === undefined ? 0 : Math.max(0, track.states.findIndex((candidate) => candidate.state === at));
        const name = track.tag ?? track.name;
        return (
          <div key={track.states[0]!.state} className="contents">
            <span className="muted truncate" title={track.tag ? `${track.tag}, ${track.name}` : track.name}>
              {name}
            </span>
            {track.states.length - 1 > SQUARES_UP_TO ? (
              <span className="flex items-center gap-2">
                <span className="relative h-1.5 flex-1 overflow-hidden rounded-full" style={{ background: 'var(--line)' }}>
                  <span
                    className="absolute inset-y-0 left-0 rounded-full"
                    style={{ width: `${(index / (track.states.length - 1)) * 100}%`, background: 'var(--brand)' }}
                  />
                </span>
                <span className="count">{track.states[index]!.label}</span>
              </span>
            ) : (
              <Squares track={track} index={index} steps={steps} />
            )}
          </div>
        );
      })}
    </div>
  );
}

function Squares({ track, index, steps }: { track: Track; index: number; steps?: CostedStep[] }): ReactElement {
  // A gap where one family of names gives way to the next, "Elite" to "Task Force".
  const starts = new Set<number>();
  let at = 0;
  for (const family of familiesOf(track) ?? []) {
    if (at > 1) starts.add(at);
    at += family.size;
  }
  return (
    <span className="flex items-center gap-[3px]" aria-label={`${track.tag ?? track.name}: ${track.states[index]!.label}`}>
      {track.states.slice(1).map((candidate, offset) => {
        const position = offset + 1;
        const reached = position <= index;
        const price = !reached && steps ? rangeCost(steps, track, position - 1, position) : undefined;
        return (
          <span
            key={candidate.state}
            data-testid="roster-square"
            data-reached={reached}
            tabIndex={reached ? undefined : 0}
            className={`roster-square ${starts.has(position) ? 'ml-1.5' : ''}`}
            style={{ background: reached ? 'var(--brand)' : 'var(--line)' }}
            aria-label={reached ? undefined : candidate.label}
          >
            {!reached && (
              <span className="roster-tip" role="tooltip">
                {candidate.label}
                {price && price.links > 0 && (
                  <>
                    {' · '}
                    <CostLine cost={price} />
                  </>
                )}
              </span>
            )}
          </span>
        );
      })}
    </span>
  );
}
