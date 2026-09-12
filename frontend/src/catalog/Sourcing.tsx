import type { Sourcing as SourcingRecord } from '../api/client';

/**
 * Where the numbers above this line were read — ADR 0016, shown to the reader.
 *
 * The attribution line that used to sit here on its own is a credit, not a
 * record: one sentence per patch, written for a lawyer as much as for a player,
 * and it cannot say that a banner's rates came from the publisher's own rules
 * screen while the stage beside it came from somebody counting three hundred
 * runs. ADR 0015 says this project ships only data it sourced itself, and that
 * promise is worth exactly as much as a reader's ability to check it. Until
 * this component existed, provenance was written at ingest, enforced at publish,
 * and invisible to everybody the promise was made to.
 *
 * Nothing here is decoration. The three states it can be in are all real today:
 * a first-hand reading, somebody else's data published under the explicit
 * exception, and silence — and as of this writing the repository has no
 * first-hand game data at all, so the honest rendering of most pages is one of
 * the latter two. A component that quietly showed nothing in those cases would
 * be worse than no component, because it would read as approval.
 */
export function Sourcing({
  sourcing,
  attribution,
}: {
  /**
   * Optional, though the wire type says otherwise, and the gap is the point.
   *
   * A server older than this bundle does not send the field, and the two do not
   * deploy at the same instant: the frontend and the backend are separate hosts
   * (D1), so every release has a window in which a new page is talking to an old
   * API. A browser found this the first time this component was served — the
   * whole page rendered as nothing, because destructuring an absent object
   * throws before React has anything to show.
   *
   * Swallowing that is not defensiveness, it is this component's own thesis:
   * a response that says nothing about where its numbers came from is silence,
   * and silence has a defined rendering. The one thing it must never do is look
   * like approval.
   */
  sourcing: SourcingRecord | undefined;
  attribution: string;
}) {
  const sources = sourcing?.sources ?? [];
  const borrowed = sources.filter((source) => !source.firstHand);

  return (
    <section className="card">
      <h2 className="mb-2 font-medium">Where these numbers came from</h2>

      {sources.length === 0 ? (
        // Not an empty list rendered as nothing. A version published before ADR
        // 0016 carries no records at all, and "we did not record it" is a
        // different and more useful answer than a blank space.
        <p className="text-sm" style={{ color: 'var(--signal)' }}>
          Nobody recorded where these numbers were read. They may be right; nothing here says so.
        </p>
      ) : (
        <ul className="space-y-2 text-sm">
          {sources.map((source) => (
            <li key={source.id}>
              <span className="font-medium" style={{ color: source.firstHand ? undefined : 'var(--short)' }}>
                {describe(source.origin)}
              </span>
              <span className="muted"> · read {source.observedOn}</span>
              <p className="muted mt-0.5">{source.detail}</p>
            </li>
          ))}
        </ul>
      )}

      {borrowed.length > 0 && (
        // The publish gate refuses second-hand data unless somebody says out
        // loud that it is not being shipped. Once it is published anyway, this
        // is the only place a reader finds out — so it says so plainly rather
        // than leaving them to read an origin string and work it out.
        <p className="mt-3 text-sm" style={{ color: 'var(--short)' }}>
          Some of this is not our own reading. It is here to be checked against ours, not to be trusted
          on its own.
        </p>
      )}

      <p className="muted mt-3 text-xs">{attribution}</p>
    </section>
  );
}

/**
 * The sourcing of two responses rendered on one page, as one record.
 *
 * A character page asks for the entity and for its upgrade table, and those are
 * two readings — a profile screen and a levelling screen — which per-fact
 * provenance exists to let a bundle distinguish. They usually share a record and
 * they are not required to, so the page shows the union rather than picking one
 * and hoping. De-duplicated by id, because a record listed twice reads as two
 * readings.
 */
export function merge(...parts: (SourcingRecord | undefined)[]): SourcingRecord {
  const present = parts.filter((part): part is SourcingRecord => Boolean(part));
  const sources = new Map(present.flatMap((part) => part.sources.map((source) => [source.id, source])));

  return {
    sources: [...sources.values()],
    facts: Object.assign({}, ...present.map((part) => part.facts)),
  };
}

/**
 * The origin in the words a player would use.
 *
 * A lookup rather than a formatted enum name, because the distinctions matter
 * to a reader deciding whether to trust a number and `SAMPLED_IN_GAME` does not
 * convey them. The fallback is deliberate and reads as unknown rather than as
 * fine: an origin this build has never heard of is a server newer than this
 * bundle, and guessing in the reassuring direction is the one mistake this
 * whole feature exists to stop.
 */
function describe(origin: string): string {
  switch (origin) {
    case 'OBSERVED_IN_GAME':
      return 'Read off a screen in the game';
    case 'SAMPLED_IN_GAME':
      return 'Counted by playing it';
    case 'PUBLISHER_DISCLOSURE':
      return "Stated by the game's publisher";
    case 'AUTHORED_FIXTURE':
      return 'Invented for this project — not any real game';
    case 'THIRD_PARTY':
      return "Somebody else's data, not ours";
    case 'UNRECORDED':
      return 'Not recorded';
    default:
      return `Unrecognised source (${origin})`;
  }
}
