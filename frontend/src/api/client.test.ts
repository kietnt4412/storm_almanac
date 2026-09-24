import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiError, signOut } from './client';

describe('signOut', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
    document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/';
  });

  it('posts to /logout with the CSRF token the server put in the cookie', async () => {
    document.cookie = 'XSRF-TOKEN=token-from-the-server; path=/';
    const fetch = vi.fn().mockResolvedValue(new Response(null, { status: 204 }));
    vi.stubGlobal('fetch', fetch);

    await signOut();

    // A POST, not a link: a GET sign-out is one any page could fire with an
    // <img>. And the header, or the server refuses it as a forgery.
    expect(fetch).toHaveBeenCalledWith('/logout', {
      method: 'POST',
      headers: { 'X-XSRF-TOKEN': 'token-from-the-server' },
      credentials: 'include',
    });
  });

  it('says so when the server refuses, rather than pretending the reader left', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(null, { status: 403 })));

    await expect(signOut()).rejects.toBeInstanceOf(ApiError);
  });
});
