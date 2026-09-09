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
     * <p><b>Refuses a draft carrying facts that are not this project's to
     * publish.</b> ADR 0015 says the shipped product carries only first-hand
     * data; this is the one place that decision is enforced rather than
     * described, and it sits on {@code publish} rather than on
     * {@link #ingestDraft} deliberately. Ingesting somebody else's data is how
     * the Kornblume adapter stays useful as a cross-check — read, diffed,
     * never shipped — so the gate belongs at the step that makes a version
     * visible, not at the one that makes it inspectable.
     *
     * @throws NoDraftToPublishException if there is no draft at that sequence
     * @throws SecondHandDataException   if the draft carries facts sourced from
     *                                   somebody else
     */
    GameDataVersion publish(GameId game, long sequence);

    /**
     * Approves a draft, optionally accepting that it carries somebody else's
     * data.
     *
     * <p>The escape hatch exists because ADR 0015 forbids <em>shipping</em>
     * second-hand data, not handling it, and a cross-check has to be able to
     * reach a published version to be diffed against one. What it must never be
     * is the quiet path: {@code publish(game, sequence)} is the short call and
     * it is the strict one, so accepting second-hand data costs an extra
     * argument at every call site that does it and shows up in every review of
     * one.
     *
     * @param acceptingSecondHandData when true, publishes anyway and the caller
     *                                owns the claim that it is not being shipped
     */
    GameDataVersion publish(GameId game, long sequence, boolean acceptingSecondHandData);

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

    /**
     * Refusal to publish facts this project did not source.
     *
     * <p>Names the facts rather than counting them, because an operator told
     * "this draft is not first-hand" can do nothing with that, and one told
     * "stage:1-1 came from kornblume-snapshot" can go and read the stage screen.
     * The list is truncated for the message and the count is not, so a whole
     * second-hand catalogue does not arrive as a wall of text pretending to be
     * an error.
     */
    class SecondHandDataException extends RuntimeException {
        public SecondHandDataException(String message) {
            super(message);
        }
    }
}
