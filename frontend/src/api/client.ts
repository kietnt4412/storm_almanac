export interface Health {
  status: string;
  service: string;
  version: string;
  time: string;
}

export interface Profile {
  id: string;
  game: string;
  region: string;
  displayName: string;
}

export interface Me {
  accountId: string;
  displayName: string;
  email: string;
  profiles: Profile[];
}

const BASE = import.meta.env.VITE_API_BASE ?? '';

/**
 * The CSRF token the server put in a cookie, echoed back in a header.
 *
 * The server issues it on every response rather than lazily, so by the time a
 * write happens the cookie is there — see SecurityConfig.browserCsrf. Reading a
 * cookie is the whole mechanism: another origin's page cannot, which is why the
 * token being readable here is not a weakening.
 */
function csrfToken(): string {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);
  return match?.[1] ? decodeURIComponent(match[1]) : '';
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await fetch(`${BASE}${path}`, {
    ...init,
    headers: {
      Accept: 'application/json',
      ...(init.body ? { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': csrfToken() } : {}),
      ...init.headers,
    },
    credentials: 'include',
  });
  if (!response.ok) {
    throw new ApiError(path, response.status);
  }
  return response.json() as Promise<T>;
}

/**
 * A failed request that still says what failed.
 *
 * The status matters to a caller in exactly one case and it is the common one:
 * 401 is "not signed in", which is a state the interface renders rather than an
 * error it reports. Everything else is a genuine failure.
 */
export class ApiError extends Error {
  constructor(readonly path: string, readonly status: number) {
    super(`${path} responded ${status}`);
  }

  get isSignedOut(): boolean {
    return this.status === 401;
  }
}

export const getHealth = () => request<Health>('/api/health');

export const getMe = () => request<Me>('/api/me');

export const createProfile = (game: string, region: string, displayName: string) =>
  request<Profile>('/api/me/profiles', {
    method: 'POST',
    body: JSON.stringify({ game, region, displayName }),
  });

/**
 * Where the sign-in button goes.
 *
 * In a production build `import.meta.env.DEV` is `false` and Vite eliminates the
 * other branch, so the development URL is not merely unused in the deployed
 * bundle — it is not in it. That is the same guarantee the backend gives by
 * keeping the development sign-in out of the deployable jar, made on the same
 * principle: a back door that can be reached by changing a setting is a back
 * door.
 *
 * Neither URL is a fetch. Both are full-page navigations, because an OAuth
 * redirect cannot be followed by XHR and the development one is deliberately the
 * same shape.
 */
export const signInUrl = (then = '/'): string =>
  import.meta.env.DEV
    ? `/dev/sign-in?as=dev&then=${encodeURIComponent(then)}`
    : `/oauth2/authorization/google`;

export const signOutUrl = (then = '/'): string | null =>
  import.meta.env.DEV ? `/dev/sign-out?then=${encodeURIComponent(then)}` : null;
