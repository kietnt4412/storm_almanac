import { useEffect, useRef, useState, type CSSProperties, type ElementType, type ReactNode } from 'react';

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
