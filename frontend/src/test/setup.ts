import '@testing-library/jest-dom/vitest';
import { cleanup, configure } from '@testing-library/react';
import { afterEach } from 'vitest';

/**
 * Five seconds for every `findBy` and `waitFor`, not Testing Library's one.
 *
 * <p>The limit is how long a query waits before it fails, not how long a passing
 * one takes — a passing `findBy` returns the moment its element appears — so a
 * wider limit costs nothing but a slower report of a real failure. One second
 * was too tight for the runner: the character page's first test mounts the whole
 * shell and waits through a chain of requests, and the same commit took it in
 * 486 ms on one run and 1 320 ms on the next, which failed the merge of PR #46
 * into `main` and so kept that merge from deploying.
 */
configure({ asyncUtilTimeout: 5000 });

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
