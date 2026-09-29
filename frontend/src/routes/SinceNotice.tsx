import { useMemo } from 'react';
import { useQueries, useQuery } from '@tanstack/react-query';
import { ApiError, getItems, getSince, getUpgrades, type ChangeForYou, type PlanSide, type Since } from '../api/client';
import { groupChanges } from '../roster/changes';
import { sectionsOf, tracksOfGraph, type Track } from '../roster/tracks';

/**
 * What the sequences published since the reader's last plan changed for them,
 * said without being asked (C3.3, ADR 0037).
 *
 * <p><b>Only a re-plan dismisses it.</b> A notice the reader could close would
 * leave the old plan on screen looking current, and re-planning is one click
 * (maintainer, 2026-09-29). When the patch touches nothing in the plan it says
 * so in one line, because silence reads the same as "we did not check".
 *
 * <p><b>The cost line is two fresh solves, not the saved plan.</b> Both sides
 * solve the saved request against what the reader owns today, so the difference
 * between them is the patch's; the saved plan below still shows what the reader
 * was shown at the time.
 */
export function SinceNotice({
  profileId,
  game,
  energyUnit,
  replanning,
  onReplan,
}: {
  profileId: string;
  game: string;
  energyUnit: string;
  replanning: boolean;
  onReplan: () => void;
}) {
  const since = useSince(profileId);
  const report = since.data && since.data.savedVersion < since.data.latestVersion ? since.data : undefined;

  // Each touched entity's tracks at the latest sequence, the key the plan's
  // own "pays for" uses, so a graph fetched for one is there for the other.
  const entities = [...new Set((report?.changes ?? []).flatMap((change) => (change.entity ? [change.entity] : [])))];
  const graphs = useQueries({
    queries: entities.map((entity) => ({
      queryKey: ['upgrades', game, entity, report?.latestVersion],
      queryFn: () => getUpgrades(game, entity, report?.latestVersion),
      staleTime: Infinity,
    })),
  });
  const items = useQuery({
    queryKey: ['items', game],
    queryFn: () => getItems(game),
    enabled: (report?.changes.length ?? 0) > 0,
  });

  const lines = useMemo(() => {
    const tracks = new Map<string, Track[]>();
    graphs.forEach((graph) => {
      if (graph.data) {
        tracks.set(
          graph.data.entity.id,
          sectionsOf(tracksOfGraph(graph.data.steps), graph.data.sections).flatMap((section) => section.tracks),
        );
      }
    });
    const itemNames = new Map((items.data?.items ?? []).map((item) => [item.id, item.displayName]));
    return groupChanges(report?.changes ?? [], tracks, itemNames);
  }, [graphs, items.data, report]);

  if (!report) return null;

  const patch = `patch ${report.latestVersionLabel} (v${report.latestVersion})`;
  // Several sequences can carry one patch's label — a reading corrected within
  // a patch is a new sequence — and then naming it twice says nothing.
  const span =
    report.savedVersionLabel === report.latestVersionLabel
      ? `v${report.savedVersion} → v${report.latestVersion}, both on patch ${report.latestVersionLabel}`
      : `patch ${report.savedVersionLabel} (v${report.savedVersion}) → ${report.latestVersionLabel} (v${report.latestVersion})`;
  const replan = (
    <button type="button" className="btn" disabled={replanning} onClick={onReplan}>
      {replanning ? 'Solving…' : `Re-plan on the latest data (v${report.latestVersion})`}
    </button>
  );

  if (report.changes.length === 0) {
    return (
      <section className="card flex flex-wrap items-center gap-3" aria-label="Since this plan">
        <p className="grow">
          {cap(patch)} is out since this plan, and nothing in it touches your plan.
          <Refusals report={report} />
        </p>
        {replan}
      </section>
    );
  }

  return (
    <section className="card space-y-3" aria-label="Since this plan">
      <div>
        <h2 className="font-medium">Since this plan: {span}</h2>
        <p className="muted text-sm">
          {report.changes.length} of the {report.allChanges.toLocaleString()} changes in it touch something your plan
          uses.
        </p>
      </div>

      <Cost report={report} energyUnit={energyUnit} />

      <div>
        <h3 className="label">What changed for you</h3>
        <ul className="mt-1 space-y-1 text-sm">
          {lines.map((line) => (
            <li key={line.key} className="flex flex-wrap justify-between gap-x-4">
              <span>{line.name}</span>
              <span className="muted">{line.what}</span>
            </li>
          ))}
        </ul>
        <details className="mt-2 text-sm">
          <summary className="muted cursor-pointer">Every line ({report.changes.length})</summary>
          <ul className="muted mt-1 space-y-0.5 text-xs">
            {report.changes.map((change, index) => (
              <li key={`${change.about}|${change.slug}|${change.detail ?? ''}|${index}`}>{rendered(change)}</li>
            ))}
          </ul>
        </details>
      </div>

      {replan}
    </section>
  );
}

/** The report, shared with Home's line: one query per profile, and a refusal is shown rather than retried. */
export function useSince(profileId: string) {
  return useQuery<Since | null>({
    queryKey: ['since', profileId],
    // Null for "never planned", because a query may not resolve to undefined.
    queryFn: async () => (await getSince(profileId)) ?? null,
    retry: (failures, error) => !(error instanceof ApiError && error.isRefusal) && failures < 2,
  });
}

/**
 * What the plan costs on each side, both worked out from what the reader owns
 * now. A side that cannot be planned says why in the solver's words — a patch
 * that takes away the only source of something is the case this most needs to
 * tell.
 */
function Cost({ report, energyUnit }: { report: Since; energyUnit: string }) {
  const then = report.onSaved;
  const now = report.onLatest;
  const unit = energyUnit;
  let line: string | null = null;
  if (planned(then) && planned(now)) {
    line =
      then.totalEnergy === now.totalEnergy && then.etaDays.toFixed(1) === now.etaDays.toFixed(1)
        ? `Same cost: ${now.totalEnergy.toLocaleString()} ${unit} over ${now.etaDays.toFixed(1)} days`
        : `${then.totalEnergy.toLocaleString()} → ${now.totalEnergy.toLocaleString()} ${unit} · ` +
          `${then.etaDays.toFixed(1)} → ${now.etaDays.toFixed(1)} days`;
  } else if (planned(now)) {
    line = `On v${now.version}: ${now.totalEnergy.toLocaleString()} ${unit} over ${now.etaDays.toFixed(1)} days`;
  }
  return (
    <div className="text-sm">
      <h3 className="label">Your plan, worked out again from what you own now</h3>
      {line && <p className="count mt-1">{line}</p>}
      <Refusals report={report} />
    </div>
  );
}

function Refusals({ report }: { report: Since }) {
  const refused = [report.onSaved, report.onLatest].filter((side): side is PlanSide & { refused: string } =>
    Boolean(side?.refused),
  );
  if (refused.length === 0) return null;
  return (
    <>
      {refused.map((side) => (
        <span key={side.version} className="mt-1 block text-sm" style={{ color: 'var(--signal)' }}>
          On patch {side.versionLabel} (v{side.version}) your goals can’t be planned: {side.refused}
        </span>
      ))}
    </>
  );
}

function planned(side: PlanSide | null): side is PlanSide & { totalEnergy: number; etaDays: number } {
  return side !== null && side.totalEnergy !== null && side.etaDays !== null;
}

/** One change as the CLI's report prints it, with the name in place of the slug. */
function rendered(change: ChangeForYou): string {
  if (change.kind === 'ADDED') return `+ ${change.name}`;
  if (change.kind === 'REMOVED') return `− ${change.name}`;
  return `~ ${change.name} · ${change.detail}: ${value(change.before)} → ${value(change.after)}`;
}

/** A value holding an arrow of its own — a step's edge — bracketed, so the line's own arrow is the only bare one. */
function value(text: string | null): string {
  return text && text.includes('→') ? `(${text})` : String(text);
}

function cap(text: string): string {
  return text.charAt(0).toUpperCase() + text.slice(1);
}
