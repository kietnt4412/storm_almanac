import { useMemo, useState, type CSSProperties, type ReactElement } from 'react';
import { Link } from 'react-router-dom';
import type { EntitySummary, UpgradeStep } from '../api/client';
import { familiesOf } from '../roster/LadderSlider';
import { rangeCost } from '../roster/rangeCost';
import { completionOf } from '../roster/RosterCard';
import { sectionsOf, standingOn, tracksOfGraph, type Track } from '../roster/tracks';
import { Emblem, serial } from '../ui/Emblem';
import { lookOf } from '../ui/gameChoice';
import { Icon } from '../ui/Icon';
import { PortraitRing } from '../ui/PortraitRing';
import { tierColour } from '../ui/rarity';

/**
 * The top of a character page as a dossier (C2.12, agreed 2026-10-01): the
 * emblem large, in a ring filled as far as the reader has them; the name in
 * the game's display type; the first track as a staircase with the reader on
 * it; and what reaching the top of every track costs, one currency at a time,
 * split by track.
 *
 * <p><b>Every number here is the steps' own prices</b> ({@link rangeCost}), as on
 * the goal screen: not a plan, a step sold at several prices left out of the
 * sum and named, and nothing for a gate. The reader's own shortfall is the
 * section under this one, which the server works out.
 */
export function Dossier({
  entity,
  ranks,
  game,
  steps,
  order,
  states,
}: {
  entity: EntitySummary;
  ranks?: number[];
  game: string;
  steps: UpgradeStep[];
  order?: string[];
  /** Where the reader has them; undefined when nobody is signed in or they are not on the roster. */
  states?: string[];
}): ReactElement {
  const tracks = useMemo(() => sectionsOf(tracksOfGraph(steps), order).flatMap((section) => section.tracks), [steps, order]);
  const tone = ranks && entity.rarity ? tierColour(entity.rarity.rank, ranks) : 'var(--brand)';
  const progress = states && tracks.length > 0 ? completionOf(tracks, states) : undefined;
  const share = progress && progress.height > 0 ? progress.climbed / progress.height : 0;
  const style = lookOf(game)?.emblem ?? 'hex';

  return (
    <div className="space-y-4">
      <section className="dossier-hero" style={{ '--tone': tone } as CSSProperties} aria-label={entity.displayName}>
        <div className="dossier-portrait">
          <PortraitRing share={share} />
          <Emblem subject={entity} ranks={ranks} game={game} size={128} />
        </div>
        <div className="min-w-0 space-y-3">
          <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-xs">
            <span className="font-semibold" style={{ color: tone }}>
              {entity.rarity?.label}
            </span>
            <span className="label">{entity.kindName ?? entity.kind}</span>
            <span className="label" style={{ color: tone }}>
              {serial(entity.id, style)}
            </span>
          </div>
          <h1 className="dossier-name">{entity.displayName}</h1>
          {(entity.element || entity.tags.length > 0) && (
            <ul className="flex flex-wrap gap-1.5">
              {[entity.element, ...entity.tags].filter(Boolean).map((tag) => (
                <li key={tag} className="chip">
                  {tag}
                </li>
              ))}
            </ul>
          )}
          <dl className="dossier-stats">
            <div>
              <dt>Tracks</dt>
              <dd className="count">{tracks.length}</dd>
            </div>
            <div>
              <dt>Upgrades</dt>
              <dd className="count">{steps.length}</dd>
            </div>
            <div>
              <dt>Yours</dt>
              <dd className="count">{progress ? `${Math.round(share * 100)}%` : '—'}</dd>
            </div>
          </dl>
          {tracks.length > 0 && (
            <div className="flex flex-wrap gap-2">
              <Link to={`/goals?add=${encodeURIComponent(entity.id)}`} className="btn no-underline">
                Plan this <Icon name="chevron" size={14} />
              </Link>
            </div>
          )}
        </div>
      </section>

      {tracks.length > 0 && (
        <div className="grid gap-4 lg:grid-cols-2">
          <Staircase track={tracks[0]!} states={states} steps={steps} />
          <CostByTrack tracks={tracks} steps={steps} />
        </div>
      )}
    </div>
  );
}

/**
 * The first track in the game's order, as stairs: a step a state, each one
 * higher than the last, filled up to where the reader stands, the names of
 * the rank families under them where the track has families.
 */
export function Staircase({ track, states, steps }: { track: Track; states?: string[]; steps: UpgradeStep[] }): ReactElement {
  const count = track.states.length;
  const at = states ? standingOn(track, states) : undefined;
  const here = at === undefined ? (states ? 0 : -1) : track.states.findIndex((candidate) => candidate.state === at);
  const families = familiesOf(track);
  const width = 100 / count;
  const top = rangeCost(steps, track, 0, count - 1);

  return (
    <section className="card space-y-3" aria-label={`${track.tag ?? track.name}, the climb`}>
      <div className="flex items-baseline justify-between gap-2">
        <h2 className="font-medium">{track.tag ?? track.name}</h2>
        <span className="muted text-xs">
          {track.states[0]!.label} → {track.states[count - 1]!.label} · {top.links} step{top.links === 1 ? '' : 's'}
        </span>
      </div>
      <svg className="staircase" viewBox="0 0 100 44" preserveAspectRatio="none" role="img" aria-label={here >= 0 ? `You are at ${track.states[here]!.label}` : `${count} states`}>
        {track.states.map((candidate, index) => {
          const height = 6 + (index / Math.max(1, count - 1)) * 36;
          return (
            <rect
              key={candidate.state}
              x={index * width + 0.3}
              y={44 - height}
              width={width - 0.6}
              height={height}
              className={index <= here ? 'stair stair-reached' : 'stair'}
            >
              <title>{candidate.label}</title>
            </rect>
          );
        })}
      </svg>
      {here >= 0 && (
        <p className="text-sm">
          You are at <b>{track.states[here]!.label}</b>
          {here < count - 1 ? (
            <span className="muted">
              {' '}
              — {count - 1 - here} step{count - 1 - here === 1 ? '' : 's'} to {track.states[count - 1]!.label}
            </span>
          ) : (
            <span className="muted"> — the top</span>
          )}
        </p>
      )}
      {families && (
        <div className="staircase-families" aria-hidden="true">
          {families.map((family) => (
            <span key={family.name} style={{ flexGrow: family.size }}>
              {family.name}
            </span>
          ))}
        </div>
      )}
    </section>
  );
}

const SEGMENT_TONES = ['var(--brand)', 'var(--violet)', 'var(--signal)', 'var(--ok)', 'var(--rarity-2)', 'var(--rarity-4)', 'var(--muted)'];

/**
 * What the top of every track costs in one currency, as one bar split by
 * track. Currencies cannot be added to each other, so a chip picks which one;
 * the first is the one the most tracks spend.
 */
export function CostByTrack({ tracks, steps }: { tracks: Track[]; steps: UpgradeStep[] }): ReactElement | null {
  const totals = useMemo(
    () =>
      tracks.map((track) => {
        const cost = rangeCost(steps, track, 0, track.states.length - 1);
        const amounts = new Map<string, { name: string; quantity: number }>();
        for (const line of cost.progress) amounts.set(`progress:${line.kind}`, { name: line.displayName, quantity: line.quantity });
        for (const line of cost.items) amounts.set(line.item, { name: line.displayName, quantity: line.quantity });
        return { track, amounts };
      }),
    [tracks, steps],
  );
  const currencies = useMemo(() => {
    const seen = new Map<string, { name: string; tracks: number; total: number }>();
    for (const { amounts } of totals) {
      for (const [key, { name, quantity }] of amounts) {
        const entry = seen.get(key) ?? { name, tracks: 0, total: 0 };
        seen.set(key, { name, tracks: entry.tracks + 1, total: entry.total + quantity });
      }
    }
    return [...seen.entries()].sort(([, a], [, b]) => b.tracks - a.tracks || b.total - a.total);
  }, [totals]);
  const [chosen, setChosen] = useState<string | null>(null);
  if (currencies.length === 0) return null;
  const key = chosen ?? currencies[0]![0];
  const total = currencies.find(([candidate]) => candidate === key)?.[1].total ?? 0;
  const parts = totals
    .flatMap(({ track, amounts }) => {
      const amount = amounts.get(key);
      return amount ? [{ track, quantity: amount.quantity }] : [];
    })
    .sort((a, b) => b.quantity - a.quantity);

  return (
    <section className="card space-y-3" aria-label="To the top of every track">
      <div className="flex items-baseline justify-between gap-2">
        <h2 className="font-medium">To the top of every track</h2>
        <span className="count text-sm font-semibold">
          {total.toLocaleString()} <span className="muted font-normal">{currencies.find(([candidate]) => candidate === key)?.[1].name}</span>
        </span>
      </div>
      <div className="flex flex-wrap gap-1.5" role="group" aria-label="Currency">
        {currencies.slice(0, 6).map(([candidate, { name }]) => (
          <button
            key={candidate}
            type="button"
            className="chip"
            aria-pressed={candidate === key}
            style={candidate === key ? { borderColor: 'var(--brand)', background: 'color-mix(in srgb, var(--brand) 14%, var(--surface))' } : undefined}
            onClick={() => setChosen(candidate)}
          >
            {name}
          </button>
        ))}
      </div>
      <div className="cost-split" role="img" aria-label={parts.map((part) => `${part.track.tag ?? part.track.name} ${part.quantity.toLocaleString()}`).join(', ')}>
        {parts.map((part, index) => (
          <span
            key={part.track.states[0]!.state}
            style={{ flexGrow: part.quantity, background: SEGMENT_TONES[index % SEGMENT_TONES.length] }}
            title={`${part.track.tag ?? part.track.name}: ${part.quantity.toLocaleString()}`}
          />
        ))}
      </div>
      <ul className="grid grid-cols-1 gap-x-4 gap-y-1 text-xs sm:grid-cols-2">
        {parts.map((part, index) => (
          <li key={part.track.states[0]!.state} className="flex items-center gap-2">
            <span className="h-2.5 w-2.5 shrink-0 rounded-sm" style={{ background: SEGMENT_TONES[index % SEGMENT_TONES.length] }} />
            <span className="min-w-0 flex-1 truncate">{part.track.tag ?? part.track.name}</span>
            <span className="count muted">{part.quantity.toLocaleString()}</span>
            <span className="count w-9 text-right">{Math.round((part.quantity / total) * 100)}%</span>
          </li>
        ))}
      </ul>
    </section>
  );
}
