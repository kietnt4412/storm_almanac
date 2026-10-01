import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { Profile } from '../api/client';
import { usePlannerStore } from '../store/plannerStore';
import { Goals } from './Goals';

/**
 * C2.8: a goal row is dragged by its grip. Goals have no PATCH, because an
 * ordered list has no per-key merge, so a drop must send the whole list in its
 * new order, exactly as ↑ and ↓ do.
 */
describe('reordering goals by dragging', () => {
  const PGR = 'punishing-gray-raven';
  const PROFILE: Profile = { id: 'p', game: PGR, region: 'global', displayName: 'Main' };
  const saved = vi.fn();

  beforeEach(() => {
    usePlannerStore.setState({ profileId: PROFILE.id, outbox: {}, reach: {}, rejected: [], lastSyncedAt: null });
    saved.mockReset();
    serve();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('sends the whole list in the order the rows were dropped in', async () => {
    render(
      <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
        <MemoryRouter>
          <Goals />
        </MemoryRouter>
      </QueryClientProvider>,
    );

    const lucia = (await screen.findByRole('link', { name: 'Lucia' })).closest('li')!;
    const selena = screen.getByRole('link', { name: 'Selena' }).closest('li')!;
    expect(screen.getByLabelText('Priority 1').closest('li')).toBe(lucia);

    fireEvent.dragStart(selena.querySelector('[draggable]')!);
    fireEvent.dragOver(lucia);
    fireEvent.drop(lucia);

    expect(screen.getByLabelText('Priority 1').closest('li')).toHaveTextContent('Selena');
    await userEvent.click(screen.getByRole('button', { name: 'Save goals' }));
    await waitFor(() => expect(saved).toHaveBeenCalled());
    expect(saved.mock.calls[0]![0].goals).toEqual([
      { entity: 'selena', targetState: 'level-10', priority: 0 },
      { entity: 'lucia', targetState: 'level-10', priority: 1 },
    ]);

    // C2.18: a save that went through says so with a tick, which a screen reader is not read.
    const done = await screen.findByRole('button', { name: 'Saved' });
    expect(done.querySelector('.tick-in')).not.toBeNull();
  });

  it("opens a row for whoever a character page's \"Plan this\" names, and none for an id the patch lacks", async () => {
    render(
      <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
        <MemoryRouter initialEntries={['/goals?add=karenina']}>
          <Goals />
        </MemoryRouter>
      </QueryClientProvider>,
    );
    expect(await screen.findByRole('link', { name: 'Karenina' })).toBeInTheDocument();
    expect(screen.getByLabelText('Priority 3').closest('li')).toHaveTextContent('Karenina');
  });

  it('opens nothing for an id the patch does not have', async () => {
    render(
      <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
        <MemoryRouter initialEntries={['/goals?add=nobody']}>
          <Goals />
        </MemoryRouter>
      </QueryClientProvider>,
    );
    await screen.findByRole('link', { name: 'Lucia' });
    expect(screen.queryByLabelText('Priority 3')).not.toBeInTheDocument();
  });

  function serve() {
    const version = { game: PGR, sequence: 18, label: 'Anchored in Faith', attribution: 'read from the client' };
    const entities = [
      { id: 'lucia', displayName: 'Lucia', kind: 'character' },
      { id: 'selena', displayName: 'Selena', kind: 'character' },
      { id: 'karenina', displayName: 'Karenina', kind: 'character' },
    ];
    let goals = [
      { entity: 'lucia', targetState: 'level-10', priority: 0 },
      { entity: 'selena', targetState: 'level-10', priority: 1 },
    ];
    vi.stubGlobal(
      'fetch',
      vi.fn(async (url: string, init?: RequestInit) => {
        if (url === '/api/me') {
          return ok({ accountId: 'a', displayName: 'Reader', email: 'r@example.invalid', profiles: [PROFILE] });
        }
        if (url === `/api/me/profiles/${PROFILE.id}/goals`) {
          if (init?.method === 'PUT') {
            const body = JSON.parse(String(init.body));
            saved(body);
            goals = body.goals;
          }
          return ok({ profile: PROFILE.id, goals });
        }
        if (url === `/api/me/profiles/${PROFILE.id}/roster`) return ok({ profile: PROFILE.id, entities: {} });
        if (url === `/api/games/${PGR}/entities`) return ok({ game: PGR, version, entities });
        const upgrades = url.match(new RegExp(`^/api/games/${PGR}/entities/(\\w+)/upgrades$`));
        if (upgrades) {
          return ok({
            game: PGR,
            version,
            entity: entities.find((entity) => entity.id === upgrades[1]),
            totalCost: [],
            steps: [{ id: 'level-10', fromState: 'level-1', toState: 'level-10', costs: [] }],
          });
        }
        return new Response(JSON.stringify({ status: 404, detail: `no fake for ${url}` }), { status: 404 });
      }),
    );
  }
});

function ok(body: unknown): Response {
  return new Response(JSON.stringify(body), { status: 200, headers: { 'Content-Type': 'application/json' } });
}
