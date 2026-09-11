import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { getEntities, getGames } from '../api/client';
import { Sourcing } from '../catalog/Sourcing';
import { useSelectedProfile } from '../profile';

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
  const { profile } = useSelectedProfile();

  // No game in the path: send the reader to their own if they have one, and
  // otherwise to the only one published. Both are better than a chooser for a
  // list with one entry on it.
  const resolved = game ?? profile?.game ?? games.data?.games[0]?.id;

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
        (entity.element ?? '').toLowerCase().includes(needle) ||
        entity.tags.some((tag) => tag.toLowerCase().includes(needle)),
    );
  }, [entities.data, query]);

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

      {rows.length === 0 ? (
        <p className="muted">Nobody matches that in this patch.</p>
      ) : (
        <ul className="grid gap-2 sm:grid-cols-2">
          {rows.map((entity) => (
            <li key={entity.id}>
              <Link
                to={`/catalog/${game}/${entity.id}`}
                className="card block no-underline"
                style={{ color: 'var(--ink)' }}
              >
                <div className="flex items-baseline gap-2">
                  <span className="font-medium">{entity.displayName}</span>
                  <span className="muted text-xs">{entity.rarity.label}</span>
                </div>
                <div className="muted mt-1 text-sm">
                  {[entity.kind, entity.element, ...entity.tags].filter(Boolean).join(' · ')}
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
