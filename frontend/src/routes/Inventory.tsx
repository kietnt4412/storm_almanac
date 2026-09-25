import { useMemo, useRef, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getInventory, getItems, type Item } from '../api/client';
import { ProfileGate } from '../profile';
import { NextStep } from '../steps/Steps';
import { effectiveInventory, outboxOf, usePlannerStore } from '../store/plannerStore';

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

  const rows = useMemo(() => {
    const needle = filter.trim().toLowerCase();
    return (items.data?.items ?? []).filter((item) => {
      if (onlyHeld && !((held[item.id] ?? 0) > 0)) return false;
      if (!needle) return true;
      // The slug is matched as well as the name because the slug is what a
      // shortfall line, a plan and a bug report all name.
      return (
        item.displayName.toLowerCase().includes(needle) ||
        item.id.includes(needle) ||
        item.category.toLowerCase().includes(needle)
      );
    });
  }, [filter, held, items.data, onlyHeld]);

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

      {rows.length === 0 ? (
        <p className="muted">Nothing matches that.</p>
      ) : (
        <div className="space-y-5">
          {groups.map(([category, inCategory]) => (
            <section key={category}>
              <h2 className="label mb-2">{category}</h2>
              <ul className="space-y-1">
                {inCategory.map((item) => {
                  const serverHas = stored.data?.items[item.id] ?? 0;
                  const pending = outbox.inventory[item.id];

                  return (
                    <li
                      key={item.id}
                      className="flex items-center gap-3 rounded-md px-2 py-1"
                      style={{ background: 'var(--surface)' }}
                    >
                      <span className="w-10 text-xs muted">{item.rarity.label}</span>
                      <span className="grow">
                        {item.displayName}
                        {pending && (
                          <span className="ml-2 text-xs" style={{ color: 'var(--signal)' }}>
                            not sent yet
                            {serverHas !== pending.value && ` · server has ${serverHas}`}
                          </span>
                        )}
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
 * Grouped by the game's own category, in the order the item list arrived.
 *
 * The categories are strings a bundle supplies — this project does not know what
 * they mean and must not: an enum of them here would be the first game-specific
 * branch in the client, which is the same invariant the backend holds itself to.
 */
function groupByCategory(items: Item[]): [string, Item[]][] {
  const groups = new Map<string, Item[]>();
  for (const item of items) {
    const key = item.category || 'other';
    const existing = groups.get(key);
    if (existing) existing.push(item);
    else groups.set(key, [item]);
  }
  return [...groups.entries()];
}
