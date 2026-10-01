import { useEffect, useRef, useState, type ReactElement } from 'react';
import { useQuery } from '@tanstack/react-query';
import { createPortal } from 'react-dom';
import { getGames, type EntitySummary, type Plan } from '../api/client';
import { Icon } from './Icon';
import { ranksByKind } from './rarity';
import { ShareCard, toPng, type ShareFace } from './ShareCard';

/**
 * "Share" on the plan screen (C2.13): the card as it will be sent, and the
 * three ways out of the page this browser offers — the share sheet a phone
 * has, a copy for pasting into a chat, and a file.
 *
 * <p><b>Nothing leaves the device until the reader picks one.</b> The image is
 * drawn here, from the plan already on screen; no server is asked and no link
 * is made, so a plan is never readable by anyone the reader did not hand it to.
 */
export function ShareDialog({
  plan,
  energyUnit,
  entities,
  onClose,
}: {
  plan: Plan;
  energyUnit: string;
  entities: EntitySummary[];
  onClose: () => void;
}): ReactElement {
  const games = useQuery({ queryKey: ['games'], queryFn: getGames });
  const gameName = games.data?.games.find((game) => game.id === plan.game)?.displayName ?? plan.game;
  const card = useRef<SVGSVGElement>(null);
  const [said, setSaid] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  // Who the plan is mostly for: the entity with the most steps, the rest counted.
  const byEntity = new Map<string, number>();
  for (const step of plan.payingFor ?? []) {
    if (step.entity) byEntity.set(step.entity, (byEntity.get(step.entity) ?? 0) + 1);
  }
  const [first] = [...byEntity.entries()].sort(([, a], [, b]) => b - a);
  const entity = first ? entities.find((candidate) => candidate.id === first[0]) : undefined;
  const ranks = ranksByKind(entities, (one) => one.kind);
  const face: ShareFace | undefined = entity ? { entity, ranks: ranks.get(entity.kind) } : undefined;

  useEffect(() => {
    const close = (event: KeyboardEvent) => event.key === 'Escape' && onClose();
    window.addEventListener('keydown', close);
    return () => window.removeEventListener('keydown', close);
  }, [onClose]);

  const fileName = `storm-almanac-plan-v${plan.version}.png`;
  const run = async (how: 'share' | 'copy' | 'save') => {
    if (!card.current) return;
    setBusy(true);
    setSaid(null);
    try {
      const png = await toPng(card.current, 2);
      if (how === 'share') {
        await navigator.share({ files: [new File([png], fileName, { type: 'image/png' })], title: 'My plan' });
        setSaid('Shared.');
      } else if (how === 'copy') {
        await navigator.clipboard.write([new ClipboardItem({ 'image/png': png })]);
        setSaid('Copied — paste it into a chat.');
      } else {
        const link = document.createElement('a');
        link.href = URL.createObjectURL(png);
        link.download = fileName;
        link.click();
        URL.revokeObjectURL(link.href);
        setSaid('Saved.');
      }
    } catch (error) {
      // A share sheet closed without sharing is a choice, not a failure.
      if (!(error instanceof DOMException && error.name === 'AbortError')) {
        setSaid(error instanceof Error ? error.message : 'That did not work.');
      }
    } finally {
      setBusy(false);
    }
  };

  const canShareFiles =
    typeof navigator.canShare === 'function' &&
    navigator.canShare({ files: [new File([new Blob()], fileName, { type: 'image/png' })] });
  const canCopy = typeof ClipboardItem === 'function' && typeof navigator.clipboard?.write === 'function';

  // On the body, not where the button is: the page rises in on an animated
  // transform, which would make it, and not the window, what "fixed" is fixed to.
  return createPortal(
    <div className="share-overlay" role="dialog" aria-modal="true" aria-label="Share your plan">
      <div className="share-scrim" onClick={onClose} aria-hidden="true" />
      <div className="share-panel">
        <div className="flex items-center justify-between gap-2">
          <h2 className="font-semibold">Share your plan</h2>
          <button type="button" className="icon-btn" onClick={onClose} aria-label="Close">
            <Icon name="close" size={18} />
          </button>
        </div>
        <div className="share-preview">
          <ShareCard ref={card} plan={plan} energyUnit={energyUnit} gameName={gameName} face={face} others={Math.max(0, byEntity.size - 1)} />
        </div>
        <p className="muted text-xs">
          Drawn on this device from the plan on screen. Nothing is uploaded and no link is made.
        </p>
        <div className="flex flex-wrap gap-2">
          {canShareFiles && (
            <button type="button" className="btn" disabled={busy} onClick={() => run('share')}>
              Share…
            </button>
          )}
          {canCopy && (
            <button type="button" className={canShareFiles ? 'btn-quiet' : 'btn'} disabled={busy} onClick={() => run('copy')}>
              <Icon name="copy" size={15} /> Copy image
            </button>
          )}
          <button type="button" className="btn-quiet" disabled={busy} onClick={() => run('save')}>
            Save as PNG
          </button>
        </div>
        {said && (
          <p className="text-sm" role="status">
            {said}
          </p>
        )}
      </div>
    </div>,
    document.body,
  );
}
