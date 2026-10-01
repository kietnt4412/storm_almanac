import type { ReactElement } from 'react';

/** The ring round the emblem, filled as far as the reader is up every track together. */
export function PortraitRing({ share }: { share: number }): ReactElement {
  const r = 62;
  const around = 2 * Math.PI * r;
  const end = -Math.PI / 2 + share * Math.PI * 2;
  return (
    <svg className="unit-ring" viewBox="0 0 140 140" aria-hidden="true">
      <circle cx="70" cy="70" r={r} className="unit-ring-track" />
      {Array.from({ length: 40 }, (_, index) => {
        const angle = (index / 40) * Math.PI * 2;
        return (
          <line
            key={index}
            x1={70 + Math.cos(angle) * 67}
            y1={70 + Math.sin(angle) * 67}
            x2={70 + Math.cos(angle) * (index % 5 === 0 ? 63 : 65)}
            y2={70 + Math.sin(angle) * (index % 5 === 0 ? 63 : 65)}
            className="unit-ring-tick"
          />
        );
      })}
      {share > 0 && (
        <>
          <circle
            cx="70"
            cy="70"
            r={r}
            className="unit-ring-fill"
            strokeDasharray={`${around * share} ${around}`}
            transform="rotate(-90 70 70)"
          />
          <circle cx={70 + Math.cos(end) * r} cy={70 + Math.sin(end) * r} r="4.5" className="unit-ring-head" />
        </>
      )}
    </svg>
  );
}
