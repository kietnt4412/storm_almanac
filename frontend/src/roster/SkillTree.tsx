import { useLayoutEffect, useRef, useState, type ReactElement } from 'react';
import type { EntitySummary } from '../api/client';
import { Emblem } from '../ui/Emblem';
import { CostLine } from './CostLine';
import { targetOn, targetsBeyond, type GoalRow } from './goalRows';
import { rangeCost, sumOf, type CostedStep, type RangeCost } from './rangeCost';
import { sectionsOf, standingOn, type Track } from './tracks';

/**
 * A construct's goals picked on a tree (C2.15, agreed 2026-10-01): the emblem
 * in the middle, a spoke per track grouped by the game's sections, a node per
 * state — lit to where the reader stands, a bright path to where the goal takes
 * them — and a click on a node aims the track there.
 *
 * <p><b>It is the ladder screen drawn another way, never a second editor.</b>
 * It takes the same inputs as {@link TargetPicker} and sends the same
 * `onChange`, so a goal set here is the goal the ladders show, and the plan
 * cannot tell which one the reader used. The ladders stay the keyboard's and
 * the screen reader's way in, and the phone's: the tree is offered on a wide
 * screen only.
 *
 * <p><b>No locks.</b> The upgrades route does not say which steps have a gate
 * (ADR 0019 gates are resolved into demand on the server), so a lock drawn here
 * would be a guess. What a node offers is only what some step arrives at, the
 * same rule as the ladder.
 */
const SIZE = 680;
const C = SIZE / 2;
const INNER = 92;
const OUTER = 262;
/** Where a track's name starts, past its last node. */
const LABEL = OUTER + 18;

export function SkillTree({
  entity,
  game,
  ranks,
  tracks,
  order,
  targets,
  roster,
  row,
  steps,
  onChange,
}: {
  entity: EntitySummary;
  game: string;
  ranks?: number[];
  tracks: Track[];
  order?: string[];
  targets: string[];
  roster: string[];
  row: GoalRow;
  steps?: CostedStep[];
  onChange: (track: Track, state: string | null) => void;
}): ReactElement {
  const sections = sectionsOf(tracks, order);
  const [focus, setFocus] = useState<Track | null>(null);

  // A slot per track and an empty one before each section, so a section is its
  // own sector of the circle and its name runs along the gap that opens it.
  const slots = tracks.length + sections.length;
  let slot = 0;
  const sectionLabels: { name: string | undefined; angle: number; key: string }[] = [];
  const spokes = sections.flatMap((section, index) => {
    sectionLabels.push({ name: section.name, angle: angleAt(slot++, slots), key: `${section.name ?? ''}${index}` });
    return section.tracks.map((track) => ({ track, angle: angleAt(slot++, slots) }));
  });

  const indexOf = (track: Track, state: string | undefined) =>
    state === undefined ? 0 : Math.max(0, track.states.findIndex((candidate) => candidate.state === state));
  const rangeOf = (track: Track): RangeCost | undefined => {
    const chosen = targetOn(track, row);
    if (!steps || chosen === undefined) return undefined;
    const at = indexOf(track, standingOn(track, roster));
    const to = indexOf(track, chosen);
    return to > at ? rangeCost(steps, track, at, to) : undefined;
  };
  // Each name's drawn length, read before the first paint in the game's own
  // type; until then, and where nothing is laid out (jsdom), an estimate.
  const svg = useRef<SVGSVGElement>(null);
  const [lengths, setLengths] = useState<Record<string, number>>({});
  useLayoutEffect(() => {
    const read: Record<string, number> = {};
    svg.current?.querySelectorAll<SVGTextElement>('.tree-label').forEach((label) => {
      if (typeof label.getComputedTextLength === 'function') read[label.textContent ?? ''] = label.getComputedTextLength();
    });
    const changed = Object.keys(read).some((name) => Math.abs((lengths[name] ?? -1) - read[name]!) > 0.5);
    if (changed) setLengths(read);
  });
  const box = boxFor(spokes.map(({ track, angle }) => ({ name: track.tag ?? track.name, angle })), lengths);
  const total = sumOf(tracks.map(rangeOf).filter((range): range is RangeCost => range !== undefined));
  const moving = tracks.filter((track) => targetOn(track, row) !== undefined).length;

  return (
    <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_16rem]">
      <svg
        ref={svg}
        className="skill-tree"
        viewBox={`${box.x} ${box.y} ${box.width} ${box.height}`}
        style={{ maxWidth: `${box.width}px` }}
        role="img" aria-label={`${entity.displayName}: ${moving} tracks being moved. The ladders view sets the same goals.`}>
        <circle cx={C} cy={C} r={OUTER + 14} className="tree-halo" />
        <circle cx={C} cy={C} r={INNER - 18} className="tree-core" />

        {sectionLabels.map((label) => {
          if (!label.name) return null;
          const at = polar((INNER + OUTER) / 2 + 10, label.angle);
          const flip = Math.cos(label.angle) < -1e-6;
          const degrees = (label.angle * 180) / Math.PI + (flip ? 180 : 0);
          return (
            <text
              key={label.key}
              x={at.x}
              y={at.y}
              className="tree-section"
              textAnchor="middle"
              dominantBaseline="middle"
              transform={`rotate(${degrees} ${at.x} ${at.y})`}
            >
              {label.name.toUpperCase()}
            </text>
          );
        })}

        {spokes.map(({ track, angle }) => {
          const at = indexOf(track, standingOn(track, roster));
          const chosen = targetOn(track, row);
          const to = chosen === undefined ? at : Math.max(at, indexOf(track, chosen));
          const ahead = new Set(targetsBeyond(track, roster, targets).map((candidate) => candidate.state));
          const last = track.states.length - 1;
          const radius = (k: number) => INNER + (last === 0 ? 0 : (k / last) * (OUTER - INNER));
          const flip = Math.cos(angle) < 0;
          const name = track.tag ?? track.name;
          const label = polar(LABEL, angle);
          const degrees = (angle * 180) / Math.PI + (flip ? 180 : 0);
          const active = focus === track;
          return (
            <g key={track.states[0]!.state} className="tree-spoke" data-active={active || undefined} onMouseEnter={() => setFocus(track)}>
              <line {...line(radius(0), radius(last), angle)} className="tree-rail" />
              {at > 0 && <line {...line(radius(0), radius(at), angle)} className="tree-have" />}
              {to > at && <line {...line(radius(at), radius(to), angle)} className="tree-want" />}
              {track.states.map((candidate, k) => {
                const point = polar(radius(k), angle);
                const state = k <= at ? 'have' : k <= to ? 'want' : ahead.has(candidate.state) ? 'open' : 'closed';
                const price = steps && k > at ? rangeCost(steps, track, at, k) : undefined;
                const pick = () => {
                  setFocus(track);
                  if (state === 'closed') return;
                  // A node behind the reader, or the target itself again, clears the goal.
                  onChange(track, k <= at || candidate.state === chosen ? null : candidate.state);
                };
                return (
                  <g key={candidate.state} className="tree-node" data-state={state} data-target={candidate.state === chosen || undefined} onClick={pick}>
                    <circle cx={point.x} cy={point.y} r="7.5" className="tree-hit" />
                    <circle cx={point.x} cy={point.y} r={candidate.state === chosen ? 6 : 3.6} className="tree-dot" />
                    <title>
                      {`${name}: ${candidate.label}`}
                      {price && price.links > 0 ? ` — ${costText(price)}` : k <= at ? ' — reached' : ''}
                    </title>
                  </g>
                );
              })}
              <text
                x={label.x}
                y={label.y}
                className="tree-label"
                textAnchor={flip ? 'end' : 'start'}
                dominantBaseline="middle"
                transform={`rotate(${degrees} ${label.x} ${label.y})`}
              >
                {name}
              </text>
            </g>
          );
        })}

        <g transform={`translate(${C - 52} ${C - 52})`}>
          <Emblem subject={entity} ranks={ranks} game={game} size={104} />
        </g>
      </svg>

      <aside className="tree-panel space-y-3">
        {focus ? (
          <TrackNote track={focus} roster={roster} row={row} cost={rangeOf(focus)} onClear={() => onChange(focus, null)} />
        ) : (
          <p className="muted text-sm">Point at a track. Click a node to aim it there; click the target again, or anything behind you, to leave it.</p>
        )}
        <div className="tree-total">
          <span className="label">
            {moving} track{moving === 1 ? '' : 's'} moving · steps' own prices
          </span>
          <div className="text-sm">
            <CostLine cost={total} empty="Nothing set yet" />
          </div>
        </div>
        <ul className="space-y-1 text-xs">
          <li className="flex items-center gap-2">
            <span className="tree-key" data-state="have" /> where you stand
          </li>
          <li className="flex items-center gap-2">
            <span className="tree-key" data-state="want" /> where the goal takes them
          </li>
          <li className="flex items-center gap-2">
            <span className="tree-key" data-state="open" /> can aim here
          </li>
        </ul>
      </aside>
    </div>
  );
}

function TrackNote({
  track,
  roster,
  row,
  cost,
  onClear,
}: {
  track: Track;
  roster: string[];
  row: GoalRow;
  cost?: RangeCost;
  onClear: () => void;
}): ReactElement {
  const at = standingOn(track, roster);
  const chosen = targetOn(track, row);
  const label = (state: string | undefined) => track.states.find((candidate) => candidate.state === state)?.label;
  return (
    <div className="space-y-1">
      <div className="font-semibold">{track.tag ?? track.name}</div>
      {track.tag && <div className="muted text-xs">{track.name}</div>}
      <div className="text-sm">
        <span className="muted">now {label(at) ?? track.states[0]!.label} →</span>{' '}
        <b>{chosen ? label(chosen) : <span className="muted font-normal">leave as is</span>}</b>
      </div>
      {cost && (
        <div className="text-xs">
          <CostLine cost={cost} />
        </div>
      )}
      {chosen && (
        <button type="button" className="text-xs font-medium" style={{ color: 'var(--brand)' }} onClick={onClear}>
          Leave this track as is
        </button>
      )}
    </div>
  );
}

/**
 * The drawing's box, grown to hold every track's name: a name runs outward
 * from {@link LABEL}, and one as long as "SS Rank Passive Skill" ran out of the
 * square and over the card's edge. A length not yet measured is estimated
 * generously (0.62 em a letter at 12px; measured names run 0.40 to 0.59).
 */
function boxFor(
  labels: { name: string; angle: number }[],
  lengths: Record<string, number>,
): { x: number; y: number; width: number; height: number } {
  const PAD = 12;
  let [left, top, right, bottom] = [0, 0, SIZE, SIZE];
  for (const { name, angle } of labels) {
    const end = polar(LABEL + (lengths[name] ?? name.length * 12 * 0.62), angle);
    left = Math.min(left, end.x - PAD);
    right = Math.max(right, end.x + PAD);
    top = Math.min(top, end.y - PAD);
    bottom = Math.max(bottom, end.y + PAD);
  }
  return { x: Math.floor(left), y: Math.floor(top), width: Math.ceil(right - left), height: Math.ceil(bottom - top) };
}

/** Clockwise from the top. */
function angleAt(slot: number, slots: number): number {
  return -Math.PI / 2 + (slot / slots) * Math.PI * 2;
}

function polar(radius: number, angle: number): { x: number; y: number } {
  return { x: C + Math.cos(angle) * radius, y: C + Math.sin(angle) * radius };
}

function line(from: number, to: number, angle: number) {
  const a = polar(from, angle);
  const b = polar(to, angle);
  return { x1: a.x, y1: a.y, x2: b.x, y2: b.y };
}

function costText(cost: RangeCost): string {
  const parts = [
    ...cost.progress.map((line) => `${line.quantity.toLocaleString()} ${line.displayName}`),
    ...cost.items.map((line) => `${line.quantity.toLocaleString()} ${line.displayName}`),
  ];
  if (cost.choices.length > 0) parts.push(`${cost.choices.length} at one of several prices`);
  return parts.join(' · ');
}
