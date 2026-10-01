import { useState, type ReactElement } from 'react';
import { Link } from 'react-router-dom';
import type { EntitySummary } from '../api/client';
import { Emblem } from '../ui/Emblem';
import { Icon } from '../ui/Icon';
import { tierColour } from '../ui/rarity';
import { CostLine } from './CostLine';
import { rangeCost, sumOf, type CostedStep } from './rangeCost';
import { completionOf, TrackLadders } from './RosterCard';
import { sectionsOf, standingOn, type Track } from './tracks';

/**
 * One construct in the hangar (C2.11, agreed 2026-10-01): a tall card with its
 * emblem in a ring that fills as they climb, where they stand on the first few
 * tracks, and — turned over — every track as a ladder and what is left to the
 * top of all of them.
 *
 * <p><b>What is left is the steps' own prices</b> ({@link rangeCost}, summed),
 * the same numbers as the goal screen's line, not a plan: a step sold at
 * several prices is named and never added, and a gate is not in it.
 *
 * <p>Both faces stay in the page; the one turned away is `inert`, so a keyboard
 * and a screen reader meet only the face that shows.
 */
export function UnitCard({
  entity,
  ranks,
  game,
  tracks,
  order,
  steps,
  states,
  editing,
  onEdit,
}: {
  entity: EntitySummary;
  ranks?: number[];
  game: string;
  tracks: Track[];
  order?: string[];
  steps?: CostedStep[];
  states: string[];
  editing: boolean;
  onEdit: () => void;
}): ReactElement {
  const [turned, setTurned] = useState(false);
  const ordered = sectionsOf(tracks, order).flatMap((section) => section.tracks);
  const { climbed, height } = completionOf(tracks, states);
  const share = height === 0 ? 0 : climbed / height;
  const percent = Math.round(share * 100);
  const placeOf = (track: Track) => {
    const at = standingOn(track, states);
    return at === undefined ? 0 : Math.max(0, track.states.findIndex((candidate) => candidate.state === at));
  };
  const maxed = ordered.filter((track) => placeOf(track) === track.states.length - 1).length;
  const left = steps
    ? sumOf(ordered.map((track) => rangeCost(steps, track, placeOf(track), track.states.length - 1)))
    : undefined;
  // The first track in the game's order that is not at the top, and what its
  // next step costs by its own prices.
  const nextTrack = ordered.find((track) => placeOf(track) < track.states.length - 1);
  const next = nextTrack && {
    track: nextTrack,
    at: placeOf(nextTrack),
    cost: steps ? rangeCost(steps, nextTrack, placeOf(nextTrack), placeOf(nextTrack) + 1) : undefined,
  };
  const tone = ranks && entity.rarity ? tierColour(entity.rarity.rank, ranks) : 'var(--brand)';

  const actions = (
    <div className="unit-actions">
      <button type="button" className="btn-quiet" aria-pressed={turned} onClick={() => setTurned(!turned)}>
        <Icon name="refresh" size={15} /> {turned ? 'Turn back' : "What's left"}
      </button>
      <button type="button" className="btn-quiet" aria-expanded={editing} onClick={onEdit}>
        <Icon name="edit" size={15} /> {editing ? 'Done' : 'Edit'}
      </button>
    </div>
  );

  return (
    <article className="unit-card has-emblem" data-turned={turned} style={{ '--tone': tone } as React.CSSProperties}>
      <div className="unit-inner">
        <section className="unit-face unit-front" inert={turned} aria-label={entity.displayName}>
          <div className="flex items-center justify-between text-xs">
            <span className="font-semibold" style={{ color: tone }}>
              {entity.rarity?.label}
            </span>
            <span className="label">{entity.kindName ?? entity.kind}</span>
          </div>

          <div className="unit-portrait">
            <PortraitRing share={share} />
            <Emblem subject={entity} ranks={ranks} game={game} size={92} />
          </div>

          <div className="text-center">
            <Link to={`/catalog/${game}/${entity.id}`} className="unit-name no-underline">
              {entity.displayName}
            </Link>
            <p className="muted mt-0.5 text-xs">
              <b className="count" style={{ color: 'var(--ink)' }}>
                {percent}%
              </b>{' '}
              up every track · {maxed} of {ordered.length} at the top
            </p>
          </div>

          {next && (
            <div className="unit-next">
              <span className="label">Next rung</span>
              <div className="text-sm">
                <span className="font-semibold">{next.track.tag ?? next.track.name}</span>{' '}
                <span className="muted">
                  {next.track.states[next.at]!.label} → {next.track.states[next.at + 1]!.label}
                </span>
              </div>
              {next.cost && (
                <div className="text-xs">
                  <CostLine cost={next.cost} />
                </div>
              )}
            </div>
          )}

          <dl className="unit-stats">
            {ordered.slice(0, 3).map((track) => (
              <div key={track.states[0]!.state}>
                <dt>{track.tag ?? track.name}</dt>
                <dd>{track.states[placeOf(track)]!.label}</dd>
              </div>
            ))}
          </dl>

          {actions}
        </section>

        <section className="unit-face unit-back" inert={!turned} aria-label={`What is left for ${entity.displayName}`}>
          <div className="flex items-center gap-2">
            <Emblem subject={entity} ranks={ranks} game={game} size={30} />
            <span className="truncate text-sm font-semibold">{entity.displayName}</span>
          </div>
          <div className="unit-scroll">
            <TrackLadders tracks={tracks} order={order} states={states} steps={steps} />
          </div>
          <div className="unit-left">
            <span className="label">To the top of every track</span>
            <div className="text-sm">
              {left ? <CostLine cost={left} empty="Nothing — every track is at the top" /> : <span className="muted">reading the prices…</span>}
            </div>
          </div>
          {actions}
        </section>
      </div>
    </article>
  );
}

/** The ring round the emblem, filled as far as the reader is up every track together. */
function PortraitRing({ share }: { share: number }): ReactElement {
  const r = 62;
  const around = 2 * Math.PI * r;
  const end = -Math.PI / 2 + share * Math.PI * 2;
  return (
    <svg className="unit-ring" viewBox="0 0 140 140" aria-hidden="true">
      <circle cx="70" cy="70" r={r} className="unit-ring-track" />
      {Array.from({ length: 40 }, (_, index) => {
        const angle = (index / 40) * Math.PI * 2;
        return (
          <line
            key={index}
            x1={70 + Math.cos(angle) * 67}
            y1={70 + Math.sin(angle) * 67}
            x2={70 + Math.cos(angle) * (index % 5 === 0 ? 63 : 65)}
            y2={70 + Math.sin(angle) * (index % 5 === 0 ? 63 : 65)}
            className="unit-ring-tick"
          />
        );
      })}
      {share > 0 && (
        <>
          <circle
            cx="70"
            cy="70"
            r={r}
            className="unit-ring-fill"
            strokeDasharray={`${around * share} ${around}`}
            transform="rotate(-90 70 70)"
          />
          <circle cx={70 + Math.cos(end) * r} cy={70 + Math.sin(end) * r} r="4.5" className="unit-ring-head" />
        </>
      )}
    </svg>
  );
}
