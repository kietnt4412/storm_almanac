package io.stormalmanac.gamedata.ingest;

import io.stormalmanac.common.id.GameId;
import java.nio.file.Path;

/**
 * The whole of what onboarding a title is allowed to cost.
 *
 * <p>The project's central claim is that a new game is a {@link GameDataBundle}
 * plus a parser adapter, and nothing else. This is the seam that claim is made
 * across: an implementation reads whatever an upstream publishes — its file
 * layout, its keys, its units, its display-name-as-primary-key habit — and
 * hands back the canonical bundle. Every module downstream of here sees one
 * shape and never learns which game it came from.
 *
 * <p>Deliberately a port with no Spring in it and no database behind it. An
 * adapter converts files to a record; it does not decide whether the result is
 * fit to publish. That decision is a human's, made against a preview, and the
 * ingest path is unchanged by the existence of adapters.
 */
public interface UpstreamAdapter {

    /** The game this adapter onboards. One adapter, one title. */
    GameId game();

    /**
     * Where this adapter expects to find the upstream's files, as one line of
     * help — the directory layout is the adapter's business and nobody else's.
     */
    String expects();

    /**
     * Converts one upstream snapshot into a bundle.
     *
     * @param upstream a directory holding the snapshot, laid out as {@link #expects()} says
     * @param sequence the version number this snapshot becomes
     * @param label    the patch it corresponds to upstream, for humans
     * @throws BundleFormatException if the snapshot is missing, malformed, or says
     *                               something the canonical model refuses to hold
     */
    GameDataBundle adapt(Path upstream, long sequence, String label);
}
