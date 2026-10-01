// A second local stack, for when another session already holds 5173 and 8080:
// the same config, served on 5174 and proxied to a backend started on 8081
// (`web-alt` and `api-alt` in .claude/launch.json). Everything else is the
// main config, so the two cannot drift apart.
import { mergeConfig } from 'vitest/config';
import base from './vite.config';

const proxied = Object.fromEntries(
  Object.entries(base.server?.proxy ?? {}).map(([path, rule]) => [
    path,
    typeof rule === 'string' ? rule.replace(':8080', ':8081') : { ...rule, target: String(rule.target).replace(':8080', ':8081') },
  ]),
);

export default mergeConfig(base, { server: { port: 5174, strictPort: true, proxy: proxied } }, false);
