import { useState, type ReactElement } from 'react';

/**
 * Where the signature writes to (2026-10-01, the maintainer: a mailto on the
 * signature, in place of the Discord handle), kept in two halves.
 *
 * <p><b>The whole address is never written down until a hand reaches for it.</b>
 * Spam harvesters read pages and bundles for anything shaped like an address;
 * neither this source, the built JavaScript, nor the page's markup holds one.
 * The link gets its `mailto:` the moment a pointer, a finger or the keyboard
 * arrives on it, so to a reader it is an ordinary link.
 */
const CONTACT = { user: 'youngthel4412', domain: 'gmail.com' };

export function contactEmail(): string {
  return [CONTACT.user, CONTACT.domain].join('@');
}

/**
 * The foot of every page (2026-10-01, the maintainer's ask): what this is, who
 * made it, and how to reach them. The credit is the maintainer's name, Thel,
 * kept icon-sized (the maintainer), written out by a pen over and over: one
 * stroke drawn after another, a pause, then it fades and writes itself again.
 * Pressing it writes an email to them.
 *
 * <p><b>No server status here.</b> A reader has no use for a commit hash
 * (the maintainer: "our production level can't contain that"). What they do
 * need is to know when the server cannot be reached, because then the screens
 * show what this device remembers, so that alone is said, and only then.
 *
 * <p>The signature is one hand-drawn path per pen stroke, each with
 * `pathLength` 1, so a dash of 1 draws any stroke whole and `looks.css`
 * animates them in turn.
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

        <div className="flex flex-wrap items-center gap-x-3 gap-y-2">
          <span className="label">Made by</span>
          <EmailLink />
        </div>
      </div>
    </footer>
  );
}

/** The signature as a link that writes an email, armed only when someone reaches for it. */
function EmailLink() {
  const [armed, setArmed] = useState(false);
  const arm = () => setArmed(true);
  const address = armed ? contactEmail() : null;

  return (
    <a
      className="signature-link"
      href={address ? `mailto:${address}` : '#write-to-me'}
      aria-label="Email Thel"
      title={address ?? undefined}
      onPointerEnter={arm}
      onTouchStart={arm}
      onFocus={arm}
      onClick={(event) => {
        // A click that arrived with nothing before it — a script, an odd device.
        if (!address) {
          event.preventDefault();
          window.location.href = `mailto:${contactEmail()}`;
        }
      }}
    >
      <Signature />
      <WriteToMe />
    </a>
  );
}

/**
 * The hint that the signature can be pressed (the maintainer: a reader may not
 * guess it): a note in the margin, an arrow curled back to the name and "write
 * to me" in a hand, as if left beside a signed letter. The arrow is drawn by
 * the same pen once the name is written; under the pointer it nudges toward it.
 */
function WriteToMe() {
  return (
    <span className="sig-hint" aria-hidden="true">
      <svg className="sig-arrow" viewBox="0 0 44 28" width="30" height="19" focusable="false">
        <path className="sig-arrow-stroke" pathLength={1} d="M42 22 C 34 26, 20 24, 12 12 C 10 9, 9 7, 8 4" />
        <path className="sig-arrow-stroke sig-arrow-head" pathLength={1} d="M2 10 L 8 3 L 14 8" />
      </svg>
      <span className="sig-hint-text">write to me</span>
    </span>
  );
}

function Signature() {
  return (
    <svg className="signature" viewBox="0 0 220 92" width="67" height="28" aria-hidden="true" focusable="false">
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
    </svg>
  );
}
