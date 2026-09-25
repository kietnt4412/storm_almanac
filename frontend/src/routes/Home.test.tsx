import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import type { Profile } from '../api/client';
import { Home } from './Home';

/**
 * Found in the maintainer's rehearsal on 2026-09-25: the form's defaults are the
 * first published game on "global", which is exactly the profile a reader with
 * one profile already has, and adding it answered "/api/me/profiles responded
 * 500". The server now refuses it by name; the form should not offer it at all.
 */

const PGR = 'punishing-gray-raven';
const THEL: Profile = { id: 'thel', game: PGR, region: 'global', displayName: 'Thel' };

describe('the add-a-profile form', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('says which profile already holds a game and server, and will not add a second', async () => {
    const posted = serve([THEL]);
    renderHome();

    expect(
      await screen.findByText('You already have a Punishing: Gray Raven profile on global: Thel.'),
    ).toBeInTheDocument();
    const add = screen.getByRole('button', { name: 'Add a profile' });
    expect(add).toBeDisabled();

    const server = screen.getByLabelText('Server');
    await userEvent.clear(server);
    await userEvent.type(server, 'cn');

    expect(screen.queryByText(/You already have/)).not.toBeInTheDocument();
    expect(add).toBeEnabled();
    expect(posted()).toBe(0);
  });

  it('offers the form as before when the place is free', async () => {
    serve([]);
    renderHome();

    expect(await screen.findByRole('button', { name: 'Add a profile' })).toBeEnabled();
    expect(screen.queryByText(/You already have/)).not.toBeInTheDocument();
  });
});

function serve(profiles: Profile[]): () => number {
  let posts = 0;
  vi.stubGlobal(
    'fetch',
    vi.fn(async (url: string, init: RequestInit = {}) => {
      const method = init.method ?? 'GET';
      if (url === '/api/me' && method === 'GET') {
        return ok({ accountId: 'a', displayName: 'Reader', email: 'r@example.invalid', profiles });
      }
      if (url === '/api/me/profiles' && method === 'POST') {
        posts += 1;
        return ok(THEL);
      }
      if (url === '/api/games') {
        return ok({
          games: [
            {
              id: PGR,
              displayName: 'Punishing: Gray Raven',
              energyUnit: 'Serum',
              latest: { game: PGR, sequence: 10, label: 'Anchored in Faith', attribution: 'read from the client' },
            },
          ],
        });
      }
      return new Response(JSON.stringify({ status: 404, detail: `no fake for ${method} ${url}` }), {
        status: 404,
        headers: { 'Content-Type': 'application/problem+json' },
      });
    }),
  );
  return () => posts;
}

function renderHome() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <Home />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

function ok(body: unknown): Response {
  return new Response(JSON.stringify(body), { status: 200, headers: { 'Content-Type': 'application/json' } });
}
