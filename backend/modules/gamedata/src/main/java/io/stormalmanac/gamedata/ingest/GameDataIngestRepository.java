package io.stormalmanac.gamedata.ingest;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.GameId;
import java.util.List;

/**
 * The write half of the gamedata schema. {@code GameDefinitionRepository} is the
 * read half, and it is the only one anything outside this module may touch.
 *
 * <p>Ingestion is automated and publishing is a deliberate human approval, so
 * the two are separate calls and there is no method that does both. A snapshot
 * arrives as a draft, somebody looks at the diff, and only then does it become
 * the thing {@code findLatest} will return.
 */
public interface GameDataIngestRepository {

    /**
     * Writes a bundle as a new draft version and returns its surrogate id.
     *
     * <p>Re-ingesting the same {@code (game, sequence)} replaces an existing
     * <em>draft</em>, because a fetch that runs on a schedule will re-run and a
     * failed ingest must not wedge the sequence. Re-ingesting over a
     * <em>published</em> version is refused: an approved snapshot is immutable,
     * and every plan and drop estimate computed against it recorded its
     * sequence on the promise that it still means the same thing.
     *
     * @throws PublishedVersionIsImmutableException if that sequence is published
     */
    long ingestDraft(GameDataBundle bundle);

    /**
     * Approves a draft, which is what makes it visible to {@code findLatest}.
     *
     * <p>The approval timestamp is the database's {@code now()} rather than the
     * caller's clock: it is the one clock every version in the table is stamped
     * by, and ordering approvals across two application instances by two
     * different clocks is a bug waiting for a second deployment.
     *
     * @throws NoDraftToPublishException if there is no draft at that sequence
     */
    GameDataVersion publish(GameId game, long sequence);

    /** Draft versions awaiting approval, newest sequence first. */
    List<DraftVersion> drafts(GameId game);

    /**
     * A version that has been ingested and not yet approved.
     *
     * <p>Not a {@link GameDataVersion}: that record requires a non-null
     * {@code publishedAt}, and a draft has not been approved by anyone.
     *
     * @param ingestedAt when the snapshot was fetched, which is not an approval
     */
    record DraftVersion(long id, GameId game, long sequence, String label, String attribution,
            java.time.Instant ingestedAt) {}

    /**
     * Refusal to overwrite an approved snapshot.
     *
     * <p>Both failures here are named types rather than {@code IllegalStateException}
     * for a reason worth recording: the implementation is a Spring
     * {@code @Repository}, and persistence exception translation rewrites a
     * bare {@code IllegalStateException} thrown from one into
     * {@code InvalidDataAccessApiUsageException} — a data-access failure, which
     * this is not. A domain refusal should survive the trip out of the
     * repository saying what it was.
     */
    class PublishedVersionIsImmutableException extends RuntimeException {
        public PublishedVersionIsImmutableException(String message) {
            super(message);
        }
    }

    /** Refusal to approve something that is not awaiting approval. */
    class NoDraftToPublishException extends RuntimeException {
        public NoDraftToPublishException(String message) {
            super(message);
        }
    }
}
