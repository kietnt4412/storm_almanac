import { useMemo, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { getEntities, getGames } from '../api/client';
import { prefetchHandlers } from '../catalog/prefetch';
import { Sourcing } from '../catalog/Sourcing';
import { useActiveGame } from '../profile';
import { Emblem } from '../ui/Emblem';
import { ranksByKind, tierColour } from '../ui/rarity';

/**
 * Browse and search, and the one part of the product a stranger can read.
 *
 * <p>The game is in the URL here and nowhere else in the app. A catalog page is
 * the thing somebody sends a link to, and a link that only resolves for whoever
 * has the right profile selected is not a link — while an inventory is the
 * reader's own state and has no business in a path.
 *
 * <p><b>Search filters rather than submits.</b> There are a few hundred entities
 * in a real patch, they arrive in one response for the version, and the answer to
 * "is she in this patch" should appear while the reader is still typing her name.
 * A server-side search would be a round trip per keystroke to sort a list the
 * client is already holding.
 */
export function Catalog() {
  const { game } = useParams();
  const games = useQuery({ queryKey: ['games'], queryFn: getGames });
  const active = useActiveGame();

  // No game in the path: the one the switch is on, which is the reader's own
  // profile's when they have not switched, and otherwise the first published.
  const resolved = game ?? active.id ?? games.data?.games[0]?.id;

  if (!resolved) {
    return games.isPending ? (
      <p className="muted">Loading…</p>
    ) : (
      <p className="card">Nothing is published on this installation yet, so there is no catalog to read.</p>
    );
  }

  return <Browser game={resolved} />;
}

function Browser({ game }: { game: string }) {
  const entities = useQuery({ queryKey: ['entities', game], queryFn: () => getEntities(game) });
  const client = useQueryClient();
  const [query, setQuery] = useState('');

  const rows = useMemo(() => {
    const needle = query.trim().toLowerCase();
    const all = entities.data?.entities ?? [];
    if (!needle) return all;
    return all.filter(
      (entity) =>
        entity.displayName.toLowerCase().includes(needle) ||
        entity.id.includes(needle) ||
        entity.kind.toLowerCase().includes(needle) ||
        (entity.kindName ?? '').toLowerCase().includes(needle) ||
        (entity.element ?? '').toLowerCase().includes(needle) ||
        entity.tags.some((tag) => tag.toLowerCase().includes(needle)),
    );
  }, [entities.data, query]);

  // The kinds the patch has, each once in the order it first appears, by the
  // bundle's word for it: a filter over what is there, not a list of a game's.
  const [kind, setKind] = useState('');
  const kinds = useMemo(() => {
    const seen = new Map<string, string>();
    for (const entity of entities.data?.entities ?? []) {
      if (!seen.has(entity.kind)) seen.set(entity.kind, entity.kindName ?? entity.kind);
    }
    return [...seen.entries()];
  }, [entities.data]);
  const shown = kind ? rows.filter((entity) => entity.kind === kind) : rows;
  const ranks = useMemo(() => ranksByKind(entities.data?.entities ?? [], (entity) => entity.kind), [entities.data]);

  if (entities.isPending) return <p className="muted">Loading the catalog…</p>;
  if (entities.isError) {
    return <p className="card">Could not read the catalog: {(entities.error as Error).message}</p>;
  }

  return (
    <div className="space-y-4">
      <header className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-xl font-semibold">Catalog</h1>
          <p className="muted text-sm">
            Every character and piece of equipment in the patch. Sign in and a page also says what{' '}
            <em>you</em> are short of.
          </p>
        </div>
        <div className="muted text-xs">
          {entities.data?.game} · patch {entities.data?.version.label}
        </div>
      </header>

      <input
        className="input w-full"
        placeholder="Search by name, element, tag or kind"
        value={query}
        onChange={(event) => setQuery(event.target.value)}
        autoFocus
      />

      {kinds.length > 1 && (
        <div className="flex flex-wrap gap-2" role="group" aria-label="Kind">
          {[['', 'Everything'] as const, ...kinds].map(([value, label]) => (
            <button
              key={value}
              type="button"
              className="chip"
              aria-pressed={kind === value}
              style={kind === value ? { borderColor: 'var(--brand)', background: 'color-mix(in srgb, var(--brand) 14%, var(--surface))' } : undefined}
              onClick={() => setKind(value)}
            >
              {label}
            </button>
          ))}
        </div>
      )}

      {shown.length === 0 ? (
        <p className="muted">Nobody matches that in this patch.</p>
      ) : (
        <ul className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {shown.map((entity) => (
            <li key={entity.id}>
              <Link
                to={`/catalog/${game}/${entity.id}`}
                viewTransition
                {...prefetchHandlers(client, game, entity.id)}
                className="card has-emblem flex h-full items-center gap-3 no-underline"
                style={{ color: 'var(--ink)' }}
              >
                <Emblem subject={entity} ranks={ranks.get(entity.kind)} game={game} size={56} flies />
                <div className="min-w-0">
                  <div className="font-medium leading-snug">{entity.displayName}</div>
                  <div className="mt-1 flex flex-wrap items-center gap-x-2 gap-y-0.5 text-xs">
                    <span className="font-semibold" style={{ color: tierColour(entity.rarity.rank, ranks.get(entity.kind) ?? []) }}>
                      {entity.rarity.label}
                    </span>
                    <span className="muted">
                      {[entity.kindName ?? entity.kind, entity.element, ...entity.tags].filter(Boolean).join(' · ')}
                    </span>
                  </div>
                </div>
              </Link>
            </li>
          ))}
        </ul>
      )}

      {entities.data && (
        <Sourcing sourcing={entities.data.sourcing} attribution={entities.data.version.attribution} />
      )}
    </div>
  );
}
