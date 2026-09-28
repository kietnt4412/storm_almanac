import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { Profile } from '../api/client';
import { usePlannerStore } from '../store/plannerStore';
import { Roster } from './Roster';

/**
 * T6, found driving the roster on 2026-09-28: someone added and left at every
 * default was not recorded, and was gone on reload. The dropdowns already
 * showed the base of every track, so a reader who owns her untouched had
 * nothing to change — and changing something was the only way to save.
 */
describe('adding someone to the roster', () => {
  const PGR = 'punishing-gray-raven';
  const PROFILE: Profile = { id: 'p', game: PGR, region: 'global', displayName: 'Main' };

  beforeEach(() => {
    usePlannerStore.setState({ profileId: PROFILE.id, outbox: {}, reach: {}, rejected: [], lastSyncedAt: null });
    serve();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('records them at the base of every track, so an untouched construct is saved', async () => {
    render(
      <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
        <MemoryRouter>
          <Roster />
        </MemoryRouter>
      </QueryClientProvider>,
    );

    await userEvent.selectOptions(await screen.findByLabelText('Add someone'), 'selena-pianissimo');
    await userEvent.click(screen.getByRole('button', { name: 'Add' }));

    const queued = usePlannerStore.getState().outbox[PROFILE.id]?.roster['selena-pianissimo'];
    expect(queued?.value).toEqual(['level-1', 'fugal-sonata-1']);
    expect(await screen.findByRole('button', { name: 'Remove Selena: Pianissimo from the roster' })).toBeInTheDocument();
  });

  function serve() {
    const version = { game: PGR, sequence: 15, label: 'Anchored in Faith', attribution: 'read from the client' };
    const selena = { id: 'selena-pianissimo', displayName: 'Selena: Pianissimo', kind: 'character' };
    vi.stubGlobal(
      'fetch',
      vi.fn(async (url: string) => {
        if (url === '/api/me') {
          return ok({ accountId: 'a', displayName: 'Reader', email: 'r@example.invalid', profiles: [PROFILE] });
        }
        if (url === `/api/me/profiles/${PROFILE.id}/roster`) return ok({ profile: PROFILE.id, entities: {} });
        if (url === `/api/games/${PGR}/entities`) return ok({ game: PGR, version, entities: [selena] });
        if (url === `/api/games/${PGR}/entities/selena-pianissimo/upgrades`) {
          return ok({
            game: PGR,
            version,
            entity: selena,
            totalCost: [],
            sections: ['Growth', 'Special Skill'],
            steps: [
              { id: 'level-2', fromState: 'level-1', toState: 'level-2', costs: [], section: 'Growth' },
              {
                id: 'fugal-sonata-2',
                fromState: 'fugal-sonata-1',
                toState: 'fugal-sonata-2',
                costs: [],
                section: 'Special Skill',
                tag: 'Signature Move',
              },
            ],
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
