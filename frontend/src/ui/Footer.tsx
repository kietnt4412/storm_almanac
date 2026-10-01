import { useEffect, useState, type ReactElement } from 'react';
import { Icon } from './Icon';

/** The maintainer's Discord username (2026-10-01): shown as @youngthel, copied without the @, which is what Add Friend takes. */
export const DISCORD_HANDLE = 'youngthel';

/**
 * The foot of every page (2026-10-01, the maintainer's ask): what this is, who
 * made it, and how to reach them. The credit is the maintainer's name, Thel,
 * written out by a pen over and over: one stroke drawn after another, a swash
 * under it, a pause, then it fades and writes itself again.
 *
 * <p><b>No server status here.</b> A reader has no use for a commit hash
 * (the maintainer: "our production level can't contain that"). What they do
 * need is to know when the server cannot be reached, because then the screens
 * show what this device remembers, so that alone is said, and only then.
 *
 * <p>The signature is one hand-drawn path per pen stroke, each with
 * `pathLength` 1, so a dash of 1 draws any stroke whole and `looks.css`
 * animates them in turn. Said once to a screen reader as "Thel".
 */
export function Footer({ unreachable }: { unreachable: boolean }): ReactElement {
  return (
    <footer className="site-footer">
      <div className="mx-auto grid max-w-5xl gap-6 px-4 py-8 sm:grid-cols-[1fr_auto] sm:items-end md:px-8">
        <div className="space-y-2 text-xs">
          <div className="text-sm font-semibold" style={{ color: 'var(--ink)' }}>
            Storm Almanac
          </div>
          <p className="muted max-w-md">
            Numbers and text only, each with where it was read. A fan-made planner, not affiliated with any
            game's publisher.
          </p>
          {unreachable && (
            <p className="footer-offline" role="status">
              Can't reach the server — showing what this device remembers.
            </p>
          )}
        </div>

        <div className="flex flex-col items-start gap-2 sm:items-end">
          <span className="label">Made by</span>
          <Signature />
          <DiscordHandle handle={DISCORD_HANDLE} />
        </div>
      </div>
    </footer>
  );
}

function Signature() {
  return (
    <svg className="signature" viewBox="0 0 220 92" width="176" height="74" role="img" aria-label="Thel">
      <path
        className="sig-stroke sig-1"
        pathLength={1}
        d="M14 30 C 30 22, 58 20, 86 24 C 92 25, 96 23, 98 20"
      />
      <path className="sig-stroke sig-2" pathLength={1} d="M58 24 C 56 40, 52 58, 44 72 C 40 79, 30 80, 26 74" />
      <path
        className="sig-stroke sig-3"
        pathLength={1}
        d="M74 72 C 80 54, 90 30, 96 16 C 99 9, 92 7, 90 16 C 86 34, 82 56, 80 72 C 84 60, 92 52, 98 54 C 104 56, 100 66, 102 70 C 104 74, 110 72, 114 66 C 118 62, 126 58, 126 53 C 126 48, 117 48, 115 55 C 113 63, 119 72, 128 70 C 134 69, 138 63, 142 56 C 150 42, 158 24, 158 16 C 158 9, 150 10, 148 20 C 145 36, 145 58, 150 68 C 154 75, 164 72, 172 62"
      />
      <path className="sig-stroke sig-4" pathLength={1} d="M30 86 C 80 80, 140 80, 196 72 C 202 71, 205 68, 200 66" />
    </svg>
  );
}

/** The handle, with a button that copies it: Discord has no link to a person that works for everyone. */
function DiscordHandle({ handle }: { handle: string }) {
  const [copied, setCopied] = useState(false);
  useEffect(() => {
    if (!copied) return;
    const timer = window.setTimeout(() => setCopied(false), 2000);
    return () => window.clearTimeout(timer);
  }, [copied]);

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(handle);
      setCopied(true);
    } catch {
      // No clipboard (an old browser, a denied permission): the handle is on screen to type.
    }
  };

  return (
    <div className="footer-contact">
      <Icon name="chat" size={16} />
      <span className="muted text-xs">Discord</span>
      <span className="font-medium">@{handle}</span>
      <button type="button" className="footer-copy" onClick={copy} aria-label={`Copy ${handle}`}>
        <Icon name={copied ? 'check' : 'copy'} size={14} />
        <span aria-live="polite">{copied ? 'Copied' : 'Copy'}</span>
      </button>
    </div>
  );
}
