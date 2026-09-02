package io.stormalmanac.stats;

import io.stormalmanac.gamedata.GameDefinition;

/**
 * Reports are validated against the game definition on ingest.
 *
 * <p>An item that cannot drop on that stage is a rejected report, not a data
 * point — averaging it in is how a crowdsourced dataset quietly rots. Abuse
 * resistance beyond this is rate limiting, reputation weighting and outlier
 * down-weighting rather than bans, because the community is small and a wrong
 * ban costs more than a wrong data point.
 */
public interface ReportValidator {

    sealed interface Verdict {
        record Accepted(double weight) implements Verdict {}
        record Rejected(String reason) implements Verdict {}
    }

    Verdict validate(DropReport report, GameDefinition definition);
}
