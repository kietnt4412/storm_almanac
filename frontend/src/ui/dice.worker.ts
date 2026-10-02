import { BATCH, boardOf, rollHistories, seeded, type Ask } from './dice';

/**
 * The dice, off the page's thread (C2.21). One generator for the whole ask, so
 * the batches are one run cut into pieces rather than separate runs, and each
 * batch is handed over rather than copied.
 */
interface Scope {
  onmessage: ((event: MessageEvent<Ask>) => void) | null;
  postMessage(message: unknown, transfer?: Transferable[]): void;
}

const scope = self as unknown as Scope;

scope.onmessage = (event) => {
  const ask = event.data;
  const board = boardOf(ask.tables);
  const random = seeded(ask.seed);
  for (let rolled = 0; rolled < ask.count; rolled += BATCH) {
    const results = rollHistories(board, ask.start, ask.copies, ask.ceiling, Math.min(BATCH, ask.count - rolled), random);
    scope.postMessage({ results }, [results.buffer]);
  }
  scope.postMessage({ done: true });
};
