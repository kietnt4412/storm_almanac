import { useEffect, useLayoutEffect, useRef, useState, type CSSProperties, type ElementType, type ReactNode, type RefObject } from 'react';

/**
 * The motion pass (C2.6, the maintainer's ask of 2026-10-01, after a recording
 * of a site whose page arrives rather than appears): words that rise into
 * place, blocks that slide in as they scroll into view.
 *
 * <p><b>Motion is decoration, so its absence is the safe state.</b> Every
 * effect here starts from content that is already visible and only hides it
 * once the browser has proved it can bring it back: no `matchMedia` or no
 * `IntersectionObserver` — jsdom, an old browser — means nothing animates and
 * nothing is hidden. A reader whose system asks for reduced motion gets the
 * page still, which `index.css` enforces a second time in case a class slips
 * through.
 */
export function prefersMotion(): boolean {
  if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') return false;
  return !window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}

/**
 * A heading whose words rise in one after another. The sentence is said once,
 * whole, to a screen reader and to a test; the split copy is for the eye only.
 */
export function Words({ text, delay = 0, step = 70 }: { text: string; delay?: number; step?: number }) {
  return (
    <>
      <span className="sr-only">{text}</span>
      <span aria-hidden="true">
        {text.split(' ').map((word, index) => (
          <span key={index} className="word-in" style={{ '--delay': `${delay + index * step}ms` } as CSSProperties}>
            {word}{' '}
          </span>
        ))}
      </span>
    </>
  );
}

/**
 * Whether an element has scrolled into view yet, once: a block that has
 * arrived stays arrived, because sliding the plan out again on the way back up
 * would make a reader watch it twice.
 */
export function useInView<T extends Element>(): [React.RefObject<T | null>, boolean] {
  const ref = useRef<T>(null);
  const [seen, setSeen] = useState(() => !canReveal());
  useEffect(() => {
    const element = ref.current;
    if (seen || !element) return;
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries.some((entry) => entry.isIntersecting)) {
          setSeen(true);
          observer.disconnect();
        }
      },
      // A little before its edge is on screen, so it is moving as it enters
      // rather than popping in once it is already being read.
      { rootMargin: '0px 0px -8% 0px' },
    );
    observer.observe(element);
    return () => observer.disconnect();
  }, [seen]);
  return [ref, seen];
}

function canReveal(): boolean {
  return prefersMotion() && typeof IntersectionObserver === 'function';
}

/**
 * A number that moves to its value instead of jumping there (C2.17): from
 * `from` on mount, which counts an answer up as it lands, and from wherever it
 * stood when the value changes, so a dial follows the reader's typing.
 *
 * <p>The one JavaScript animation primitive; everything that is not a number is
 * drawn by the classes in `motion.css`. Still where motion is reduced, where
 * nothing can draw frames (jsdom, a hidden tab), and on the first frame of a
 * reader who never sees it — the value is the truth and the motion is a guest.
 */
export function useTween(target: number, { from = 0, duration = 900 }: { from?: number; duration?: number } = {}): number {
  const [still] = useState(() => !prefersMotion() || typeof requestAnimationFrame !== 'function');
  const [shown, setShown] = useState(() => (still || hidden() ? target : from));
  const current = useRef(shown);
  current.current = shown;

  useEffect(() => {
    if (still) return;
    const start = current.current;
    if (start === target) return;
    if (hidden()) {
      setShown(target);
      return;
    }
    const began = performance.now();
    let frame = requestAnimationFrame(function step(now) {
      const t = Math.min(1, (now - began) / duration);
      setShown(t >= 1 ? target : start + (target - start) * easeOut(t));
      if (t < 1) frame = requestAnimationFrame(step);
    });
    // A window that says it is visible and draws no frames (behind another,
    // or a preview pane) would hold the number on a wrong frame; a timer runs
    // there, so the count ends on the value whether or not anybody saw it move.
    const settle = setTimeout(() => setShown(target), duration + 250);
    return () => {
      cancelAnimationFrame(frame);
      clearTimeout(settle);
    };
  }, [target, duration, still]);

  return still ? target : shown;
}

/** Fast out of the gate, settling into place: the curve every entrance here uses. */
const easeOut = (t: number) => 1 - Math.pow(1 - t, 4);

const hidden = () => typeof document !== 'undefined' && document.visibilityState === 'hidden';

/**
 * A number in the page that counts to its value. A screen reader is told the
 * value once, never the frames on the way; a whole number counts in whole steps.
 */
export function Count({
  value,
  format = (n) => n.toLocaleString(),
  from,
  duration,
}: {
  value: number;
  format?: (value: number) => string;
  from?: number;
  duration?: number;
}) {
  const shown = useTween(value, { from, duration });
  if (shown === value) return <>{format(value)}</>;
  const step = Number.isInteger(value) ? Math.round(shown) : shown;
  return (
    <>
      <span aria-hidden="true">{format(step)}</span>
      <span className="sr-only">{format(value)}</span>
    </>
  );
}

/**
 * A block that slides up into place when it scrolls into view. `delay` staggers
 * siblings that arrive together, the way the cards under a heading follow it.
 */
export function Reveal({
  as: Tag = 'div',
  delay = 0,
  className = '',
  style,
  children,
  ...rest
}: {
  as?: ElementType;
  delay?: number;
  className?: string;
  style?: CSSProperties;
  children: ReactNode;
} & Record<string, unknown>) {
  const [ref, seen] = useInView<HTMLElement>();
  // Hidden only while it is waiting and only where it can be brought back.
  const [armed] = useState(canReveal);
  const state = !armed ? '' : seen ? 'reveal reveal-in' : 'reveal';
  return (
    <Tag
      ref={ref}
      className={`${state} ${className}`.trim()}
      style={{ ...style, '--delay': `${delay}ms` } as CSSProperties}
      {...rest}
    >
      {children}
    </Tag>
  );
}

/**
 * A figure whose digits roll to their value like an odometer (C2.20, F1): each
 * digit is a strip of 0–9 slid to its place, so a what-if moving 14 to 11 is
 * seen moving rather than swapped. A screen reader is told the text once.
 *
 * <p>Columns are keyed from the right, so 9 → 10 keeps the units column and
 * adds a tens column rather than re-rolling both. Anything that is not a digit
 * — a point, a sign — stands still. Still where motion is reduced (`motion.css`).
 */
export function Odometer({ text }: { text: string }) {
  const chars = [...text];
  return (
    <span className="odo">
      <span className="sr-only">{text}</span>
      <span className="odo-digits" aria-hidden="true">
        {chars.map((char, index) => {
          const key = chars.length - index;
          if (!/\d/.test(char)) {
            return (
              <span key={key} className="odo-char">
                {char}
              </span>
            );
          }
          return (
            <span key={key} className="odo-col">
              <span className="odo-strip" style={{ transform: `translateY(-${Number(char)}em)` }}>
                {DIGITS.map((digit) => (
                  <span key={digit}>{digit}</span>
                ))}
              </span>
            </span>
          );
        })}
      </span>
    </span>
  );
}

const DIGITS = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9'];

/**
 * Rows that slide to their new places instead of jumping there (C2.20, F1) —
 * the FLIP technique: measure where each `[data-flip]` child was, let React
 * move it, and animate it from the old place to the new one.
 *
 * <p>Measured by `offsetTop` against the list (which must be positioned), not
 * by the viewport, so a page scrolled between two answers does not read as
 * every row moving. `key` is what changes when the order may have; a render
 * that changes neither moves nothing. Still where motion is reduced, and where
 * there is no Web Animations API (jsdom).
 */
export function useFlip(list: RefObject<HTMLElement | null>, key: string) {
  const last = useRef(new Map<string, number>());
  useLayoutEffect(() => {
    const box = list.current;
    if (!box) return;
    const next = new Map<string, number>();
    const moving = prefersMotion();
    box.querySelectorAll<HTMLElement>('[data-flip]').forEach((row) => {
      const id = row.dataset.flip!;
      const top = row.offsetTop;
      next.set(id, top);
      const was = last.current.get(id);
      if (moving && was !== undefined && was !== top && typeof row.animate === 'function') {
        row.animate([{ transform: `translateY(${was - top}px)` }, { transform: 'none' }], {
          duration: 450,
          easing: 'cubic-bezier(.2, .8, .2, 1)',
        });
      }
    });
    last.current = next;
  }, [list, key]);
}
