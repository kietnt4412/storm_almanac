import { useQuery } from '@tanstack/react-query';
import { getHealth } from './api/client';

/**
 * Phase 0 shell. It renders the health endpoint and nothing else, on purpose:
 * the point of this phase is a green pipeline putting a real URL in front of a
 * real browser, before there is anything to get attached to.
 */
export function App() {
  const health = useQuery({ queryKey: ['health'], queryFn: getHealth });

  return (
    <main style={{ fontFamily: 'system-ui, sans-serif', padding: '3rem', lineHeight: 1.6 }}>
      <h1>Storm Almanac</h1>
      <p>A progression optimizer for live-service games.</p>
      <p>
        Backend:{' '}
        {health.isPending && 'checking…'}
        {health.isError && 'unreachable'}
        {health.data && `${health.data.status} (${health.data.version})`}
      </p>
    </main>
  );
}
