import { queryOptions, type QueryClient } from '@tanstack/react-query';
import { getEntity, getUpgrades } from '../api/client';

/**
 * A character page's two questions, in one place so the page, the links into
 * it and the router ask them under the same keys.
 */
export const entityQuery = (game: string, entity: string) =>
  queryOptions({ queryKey: ['entity', game, entity], queryFn: () => getEntity(game, entity) });

export const upgradesQuery = (game: string, entity: string) =>
  queryOptions({
    queryKey: ['upgrades', game, entity],
    queryFn: () => getUpgrades(game, entity),
    staleTime: Infinity,
  });

/**
 * Ask for a character page before it is opened (C2.19): on hover, on focus and
 * as a press starts. The emblem flies from the link into the page's dossier,
 * and a page still loading has no dossier for it to fly into.
 */
export function prefetchHandlers(client: QueryClient, game: string, entity: string) {
  const ask = () => {
    void client.prefetchQuery(entityQuery(game, entity));
    void client.prefetchQuery(upgradesQuery(game, entity));
  };
  return { onPointerEnter: ask, onFocus: ask, onPointerDown: ask };
}

/**
 * The router's wait before a character page (C2.19): until the entity is in,
 * or `ms`, whichever comes first. A link nobody hovered — a tap, a keyboard —
 * still usually arrives with its dossier drawn; a slow server never holds the
 * click for longer than a blink, and the page then shows its own loading line.
 */
export async function waitForEntity(client: QueryClient, game: string, entity: string, ms = 300): Promise<null> {
  if (client.getQueryData(entityQuery(game, entity).queryKey)) return null;
  await Promise.race([
    client.prefetchQuery(entityQuery(game, entity)),
    new Promise((resolve) => setTimeout(resolve, ms)),
  ]);
  return null;
}
