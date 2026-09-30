import { useId, useState, type ReactElement, type ReactNode } from 'react';

/**
 * One sentence on screen, the rest behind "Why?" (C2, agreed 2026-09-30).
 *
 * <p>The explanations are what made five strangers trust the numbers, so none of
 * the wording is cut; what changes is that a screen no longer opens on three
 * sentences before its first control. The first sentence says what the thing is,
 * and the reason is a tap away for whoever wants it.
 */
export function Explain({
  lead,
  children,
  className = 'muted text-sm',
}: {
  lead: ReactNode;
  children: ReactNode;
  className?: string;
}): ReactElement {
  const [open, setOpen] = useState(false);
  const id = useId();
  return (
    <div className={className}>
      <p>
        {lead}{' '}
        <button
          type="button"
          className="font-medium underline decoration-dotted underline-offset-2"
          style={{ color: 'var(--brand)' }}
          aria-expanded={open}
          aria-controls={id}
          onClick={() => setOpen(!open)}
        >
          {open ? 'Less' : 'Why?'}
        </button>
      </p>
      {open && (
        <p id={id} className="mt-1">
          {children}
        </p>
      )}
    </div>
  );
}
