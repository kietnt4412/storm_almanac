import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach } from 'vitest';

/**
 * One rendered tree per test, and no state carried between them.
 *
 * Testing Library unmounts on cleanup, which matters more here than in most
 * apps: the planner store is a module-level zustand store with a persisted
 * outbox, so a component left mounted keeps a subscription and the next test
 * inherits whatever the last one typed. A suite that passes only in the order it
 * was written is worse than no suite.
 */
afterEach(cleanup);
