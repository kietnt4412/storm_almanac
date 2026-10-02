import { QueryClient } from '@tanstack/react-query';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { entityQuery, prefetchHandlers, waitForEntity } from './prefetch';
import * as api from '../api/client';

/**
 * C2.19: a character page is asked for before it is opened, so the emblem has a
 * dossier to fly into — and the router never holds a click for long doing it.
 */
const PGR = 'punishing-gray-raven';

afterEach(() => {
  vi.restoreAllMocks();
  vi.useRealTimers();
});

describe('opening a character page', () => {
  it('asks for the character and their upgrades as a link is approached', async () => {
    const entity = vi.spyOn(api, 'getEntity').mockResolvedValue({} as never);
    const upgrades = vi.spyOn(api, 'getUpgrades').mockResolvedValue({} as never);
    const client = new QueryClient();

    prefetchHandlers(client, PGR, 'lucia').onPointerEnter();
    await vi.waitFor(() => expect(entity).toHaveBeenCalledWith(PGR, 'lucia'));
    expect(upgrades).toHaveBeenCalledWith(PGR, 'lucia');
  });

  it('opens at once when the character is already here', async () => {
    const entity = vi.spyOn(api, 'getEntity');
    const client = new QueryClient();
    client.setQueryData(entityQuery(PGR, 'lucia').queryKey, {} as never);

    await expect(waitForEntity(client, PGR, 'lucia')).resolves.toBeNull();
    expect(entity).not.toHaveBeenCalled();
  });

  it('waits for a slow server no longer than it was told to', async () => {
    vi.useFakeTimers();
    vi.spyOn(api, 'getEntity').mockReturnValue(new Promise(() => {}));
    const client = new QueryClient();

    let opened = false;
    void waitForEntity(client, PGR, 'lucia', 300).then(() => (opened = true));
    await vi.advanceTimersByTimeAsync(299);
    expect(opened).toBe(false);
    await vi.advanceTimersByTimeAsync(1);
    expect(opened).toBe(true);
  });
});
