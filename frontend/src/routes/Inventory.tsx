import { useMemo, useRef, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getInventory, getItems, getSavedPlan, type Item, type Plan } from '../api/client';
import { ProfileGate } from '../profile';
import { NextStep } from '../steps/Steps';
import { effectiveInventory, outboxOf, usePlannerStore } from '../store/plannerStore';
import { Emblem } from '../ui/Emblem';
import { tierColour } from '../ui/rarity';

/**
 * Bulk entry, which is where a companion tool lives or dies.
 *
 * <p>The thing being optimised is a player holding a phone next to the game,
 * copying a few hundred numbers out of their bag. Everything here follows from
 * that:
 *
 * <ul>
 *   <li><b>Nothing is saved by a button.</b> A number typed is an edit recorded,
 *       timestamped on this device and queued. A save button would make the
 *       offline case — the case this screen exists for — the one that loses work.
 *   <li><b>Enter moves down the column.</b> Counting a bag is one hand on the
 *       number row, and reaching for a pointer between every field is the whole
 *       cost of doing this on a phone.
 *   <li><b>The filter narrows, it does not search-then-select.</b> A player
 *       entering Sigils wants the four Sigil rows at once, not one match they
 *       have to confirm.
 *   <li><b>Every row shows what the server holds when it differs</b>, so an edit
 *       that lost a merge is visible rather than quietly overwritten on the next
 *       refresh.
 * </ul>
 */
export function Inventory() {
  return <ProfileGate>{(profile) => <Editor profileId={profile.id} game={profile.game} />}</ProfileGate>;
}

function Editor({ profileId, game }: { profileId: string; game: string }) {
  const items = useQuery({ queryKey: ['items', game], queryFn: () => getItems(game) });
  const stored = useQuery({
    queryKey: ['inventory', profileId],
    queryFn: () => getInventory(profileId),
  });

  const outbox = usePlannerStore((state) => outboxOf(state, profileId));
  const editQuantity = usePlannerStore((state) => state.editQuantity);

  const [filter, setFilter] = useState('');
  const [onlyHeld, setOnlyHeld] = useState(false);
  // Keyed by item rather than by position: the rows are filtered and regrouped
  // as the player types, and an index into a list that has just changed under it
  // points at whatever moved into that slot.
  const fields = useRef<Record<string, HTMLInputElement | null>>({});

  const held = useMemo(
    () => effectiveInventory(stored.data?.items ?? {}, outbox.inventory),
    [outbox.inventory, stored.data],
  );

  // What the saved plan does with each item, so a tile says it while the reader
  // is counting (C2.5, the maintainer's answer: tie the bag to the plan). The
  // same query the plan screen reads, so it is one request between them.
  const saved = useQuery({
    queryKey: ['savedPlan', profileId],
    queryFn: async () => (await getSavedPlan(profileId)) ?? null,
  });
  const planUse = useMemo(() => usesOf(saved.data?.plan), [saved.data]);
  // Against what the plan needs (C2.8): a ring per tile, a line of counts, and
  // the chips. Live as the reader types, since the need is the plan's and the
  // count is theirs.
  const coverage = useMemo(
    () => coverageOf(saved.data?.plan, held, new Set((items.data?.items ?? []).map((item) => item.id))),
    [saved.data, held, items.data],
  );
  const [showing, setShowing] = useState<Showing>('all');
  const needed = [...coverage.values()].filter((entry) => entry.state === 'covered' || entry.state === 'short');
  const short = needed.filter((entry) => entry.state === 'short').length;

  // Every rank in the bag, highest first: a tile's stripe is its place in that
  // order, so the colours come from the data rather than from a game's scale.
  const ranks = useMemo(
    () => [...new Set((items.data?.items ?? []).map((item) => item.rarity.rank))].sort((a, b) => b - a),
    [items.data],
  );

  const rows = useMemo(() => {
    const needle = filter.trim().toLowerCase();
    return (items.data?.items ?? []).filter((item) => {
      if (onlyHeld && !((held[item.id] ?? 0) > 0)) return false;
      if (!shows(showing, coverage.get(item.id))) return false;
      if (!needle) return true;
      // The slug is matched as well as the name because the slug is what a
      // shortfall line, a plan and a bug report all name.
      return (
        item.displayName.toLowerCase().includes(needle) ||
        item.id.includes(needle) ||
        item.category.toLowerCase().includes(needle) ||
        (item.categoryName ?? '').toLowerCase().includes(needle)
      );
    });
  }, [coverage, filter, held, items.data, onlyHeld, showing]);

  if (items.isPending || stored.isPending) return <p className="muted">Loading the bag…</p>;
  if (items.isError) {
    return <p>Could not read the item list: {(items.error as Error).message}</p>;
  }

  const groups = groupByCategory(rows);

  // Where Enter goes next, in the order the rows are actually drawn.
  //
  // It was the order of the flat filtered list until a browser proved it was
  // not the same thing: the list is sorted by rarity and the rows are grouped by
  // category, so Enter on the second Sigil jumped to a different section. The
  // sequence the hand moves through has to be the sequence the eye is reading,
  // and the only place that order exists is after the grouping.
  const ordered = groups.flatMap(([, inCategory]) => inCategory);
  const nextAfter = new Map(ordered.map((item, index) => [item.id, ordered[index + 1]?.id]));

  return (
    <div className="space-y-4">
      <header className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-xl font-semibold">Inventory</h1>
          <p className="muted text-sm">
            Type a count and press Enter to move down. Nothing needs saving — edits are kept here and
            sent when they can be.
          </p>
        </div>
        <div className="muted text-xs">
          {items.data?.items.length} items · patch {items.data?.version.label}
        </div>
      </header>

      <div className="flex flex-wrap items-center gap-3">
        <input
          className="input grow"
          placeholder="Filter by name, category or slug"
          value={filter}
          onChange={(event) => setFilter(event.target.value)}
          autoFocus
        />
        <label className="flex items-center gap-2 text-sm">
          <input type="checkbox" checked={onlyHeld} onChange={(event) => setOnlyHeld(event.target.checked)} />
          Only what I hold
        </label>
      </div>

      {coverage.size > 0 && (
        <div className="flex flex-wrap items-center gap-2 text-sm">
          {needed.length > 0 ? (
            <span className="count">
              <b>
                Covers {needed.length - short} of {needed.length}
              </b>
              {short > 0 && (
                <>
                  <span className="muted"> · </span>
                  <span style={{ color: 'var(--brand)' }}>{short} the plan has to get</span>
                </>
              )}
            </span>
          ) : (
            <span className="muted">Work the plan out again to see what it needs from the bag.</span>
          )}
          <span className="grow" />
          {(
            [
              ['all', 'Everything'],
              ['short', 'Short for the plan'],
              ['used', 'Used by the plan'],
              ['unused', 'Not used'],
            ] as const
          ).map(([value, label]) => (
            <button
              key={value}
              type="button"
              className="chip"
              aria-pressed={showing === value}
              style={showing === value ? { borderColor: 'var(--brand)', background: 'color-mix(in srgb, var(--brand) 14%, var(--surface))' } : undefined}
              onClick={() => setShowing(value)}
            >
              {label}
            </button>
          ))}
        </div>
      )}

      {rows.length === 0 ? (
        <p className="muted">Nothing matches that.</p>
      ) : (
        <div className="space-y-5">
          {groups.map(([category, inCategory]) => (
            <section key={category}>
              <h2 className="label mb-2">{category}</h2>
              <ul className="grid grid-cols-2 gap-2 sm:grid-cols-3 lg:grid-cols-4">
                {inCategory.map((item) => {
                  const serverHas = stored.data?.items[item.id] ?? 0;
                  const pending = outbox.inventory[item.id];

                  const spends = planUse.spends.get(item.id);
                  const buys = planUse.buys.get(item.id);
                  const covers = coverage.get(item.id);
                  const count = held[item.id] ?? 0;

                  return (
                    <li
                      key={item.id}
                      className="flex flex-col justify-between gap-2 rounded-lg border border-l-4 p-3"
                      style={{
                        background: 'var(--surface)',
                        borderColor: 'var(--line)',
                        borderLeftColor: tierColour(item.rarity.rank, ranks),
                      }}
                    >
                      <span className="flex items-start justify-between gap-2">
                      {/* A face beside the name where a tile has the room; on a phone the stripe says the rarity. */}
                      <Emblem
                        subject={{ ...item, kind: item.category }}
                        ranks={ranks}
                        game={game}
                        size={32}
                        className="hidden sm:block"
                      />
                      <span className="min-w-0 flex-1">
                        <span className="muted flex items-center gap-1.5 text-xs">
                          {item.rarity.label}
                          {/* Saved, or still in this device's outbox: the same state as "not sent yet". */}
                          <span
                            className="inline-block h-1.5 w-1.5 rounded-full"
                            data-testid="sync-dot"
                            data-pending={pending ? 'true' : 'false'}
                            title={pending ? 'waiting to send' : 'saved'}
                            style={{ background: pending ? 'var(--signal)' : 'var(--ok)' }}
                          />
                        </span>
                        <span className="block leading-snug">{item.displayName}</span>
                        {covers?.state === 'covered' && (
                          <span className="block text-xs" style={{ color: 'var(--ok)' }}>
                            covers the {covers.need.toLocaleString()} needed
                          </span>
                        )}
                        {covers?.state === 'short' && (
                          <span className="block text-xs" style={{ color: 'var(--brand)' }}>
                            {(covers.need - count).toLocaleString()} more — the plan gets them
                          </span>
                        )}
                        {pending && (
                          <span className="block text-xs" style={{ color: 'var(--signal)' }}>
                            not sent yet
                            {serverHas !== pending.value && ` · server has ${serverHas}`}
                          </span>
                        )}
                      </span>
                      {/* Beside the name, so the row with the count keeps its room on a phone. */}
                      {covers?.need !== undefined && <CoverageRing held={count} need={covers.need} />}
                      </span>
                      <span className="flex items-end justify-between gap-2">
                        <span className="muted min-w-0 text-xs">
                          {spends !== undefined && <span className="block">plan spends {spends.toLocaleString()}</span>}
                          {buys !== undefined && <span className="block">plan buys {buys.toLocaleString()}</span>}
                        </span>
                      <input
                        ref={(element) => {
                          fields.current[item.id] = element;
                        }}
                        className="input count w-24 text-right"
                        type="number"
                        min={0}
                        inputMode="numeric"
                        value={held[item.id] ?? 0}
                        aria-label={item.displayName}
                        onFocus={(event) => event.currentTarget.select()}
                        onChange={(event) => {
                          const quantity = Number(event.target.value);
                          // A blank field reads as NaN; it means "none", which
                          // the server accepts as zero and drops. Refusing it
                          // would make clearing a field an error.
                          editQuantity(profileId, item.id, Number.isFinite(quantity) ? Math.max(0, quantity) : 0);
                        }}
                        onKeyDown={(event) => {
                          if (event.key !== 'Enter') return;
                          event.preventDefault();
                          const next = nextAfter.get(item.id);
                          if (next) fields.current[next]?.focus();
                        }}
                      />
                      </span>
                    </li>
                  );
                })}
              </ul>
            </section>
          ))}
        </div>
      )}

      <NextStep from="/inventory" />
    </div>
  );
}

/**
 * Grouped by the category's name, in the order the item list arrived.
 *
 * By name and not by key since sequence 13 (ADR 0033): three EXP Pod sizes are
 * three categories, because a fodder rule tells them apart, and one heading,
 * because a reader does not. A category with no word is its own key, as before.
 *
 * The categories are strings a bundle supplies — this project does not know what
 * they mean and must not: an enum of them here would be the first game-specific
 * branch in the client, which is the same invariant the backend holds itself to.
 */
export function groupByCategory(items: Item[]): [string, Item[]][] {
  const groups = new Map<string, Item[]>();
  for (const item of items) {
    const key = item.categoryName || item.category || 'other';
    const existing = groups.get(key);
    if (existing) existing.push(item);
    else groups.set(key, [item]);
  }
  return [...groups.entries()];
}

/**
 * What a plan spends of each currency and buys of each item, all its purchases
 * together. Off the numbers a purchase carries (`spends`), never its sentence;
 * a plan saved before those numbers marks nothing rather than guessing.
 */
export function usesOf(plan: Plan | undefined): { spends: Map<string, number>; buys: Map<string, number> } {
  const spends = new Map<string, number>();
  const buys = new Map<string, number>();
  for (const conversion of plan?.conversions ?? []) {
    const spent = conversion.spends;
    if (!spent) continue;
    spends.set(spent.item, (spends.get(spent.item) ?? 0) + spent.quantity);
    if (spent.boughtItem && spent.boughtQuantity) {
      buys.set(spent.boughtItem, (buys.get(spent.boughtItem) ?? 0) + spent.boughtQuantity);
    }
  }
  return { spends, buys };
}

export type Coverage =
  | { state: 'covered' | 'short'; need: number }
  | { state: 'used' | 'unused'; need?: undefined };

/**
 * What the bag holds against what the saved plan needs (C2.8, agreed
 * 2026-10-01), item by item.
 *
 * <p><b>Need is the plan's own</b>: `needs`, the demand the solver resolved
 * before taking the bag off, gates added and crossed states left out. Not what
 * the purchases spend, which is what this screen showed until now: set against
 * the bag, that called Simulation Score 3,362 short when the plan farms every
 * point of it. A currency the plan only spends is "used", with no ring.
 *
 * <p>Short means the plan has to get the rest, by a run, a purchase or a claim;
 * a plan that answered at all gets all of it. A plan saved before `needs` marks
 * only what it spends and buys.
 *
 * <p>Only what a bag can hold is counted: a need for EXP (`progress:…`) or for
 * one step at several prices (`choice:…`) is no tile, and counting it read
 * "covers 1 of 3" on a screen showing two.
 *
 * @param known the catalog's item ids, the tiles there are
 */
export function coverageOf(
  plan: Plan | undefined,
  held: Record<string, number>,
  known: Set<string>,
): Map<string, Coverage> {
  const coverage = new Map<string, Coverage>();
  const { spends, buys } = usesOf(plan);
  for (const need of plan?.needs ?? []) {
    if (need.quantity <= 0 || !known.has(need.item)) continue;
    coverage.set(need.item, {
      state: (held[need.item] ?? 0) >= need.quantity ? 'covered' : 'short',
      need: need.quantity,
    });
  }
  for (const item of [...spends.keys(), ...buys.keys()]) {
    if (!coverage.has(item)) coverage.set(item, { state: 'used' });
  }
  return coverage;
}

/** Which tiles the chips leave on screen. */
export type Showing = 'all' | 'short' | 'used' | 'unused';

export function shows(showing: Showing, coverage: Coverage | undefined): boolean {
  const state = coverage?.state ?? 'unused';
  if (showing === 'short') return state === 'short';
  if (showing === 'used') return state !== 'unused';
  if (showing === 'unused') return state === 'unused';
  return true;
}

/** A tile's ring: held against need, green once covered, the plan's colour while it still has to get some. */
function CoverageRing({ held, need }: { held: number; need: number }) {
  const share = Math.min(1, held / need);
  const r = 15;
  const around = 2 * Math.PI * r;
  return (
    <svg width="36" height="36" viewBox="0 0 38 38" aria-hidden="true">
      <circle cx="19" cy="19" r={r} fill="none" stroke="var(--line)" strokeWidth="4" />
      {share > 0 && (
        <circle
          className="coverage-arc"
          cx="19"
          cy="19"
          r={r}
          fill="none"
          stroke={share >= 1 ? 'var(--ok)' : 'var(--brand)'}
          strokeWidth="4"
          strokeLinecap="round"
          strokeDasharray={`${around * share} ${around}`}
          transform="rotate(-90 19 19)"
        />
      )}
    </svg>
  );
}

/** A tile's stripe is its rarity's place among the ranks in the bag; moved to `ui/rarity.ts` for the emblems (C2.10). */
export { tierColour };
