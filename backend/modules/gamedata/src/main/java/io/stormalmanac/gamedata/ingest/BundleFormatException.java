package io.stormalmanac.gamedata.ingest;

/**
 * A bundle that cannot be ingested, with a message aimed at the person holding
 * the file rather than at the person holding a stack trace.
 *
 * <p>Every message names the offending thing by its upstream slug. An ingest
 * tool that reports {@code stage_drop_item_fk} has told the operator that
 * something is wrong and nothing about what.
 */
public class BundleFormatException extends RuntimeException {

    public BundleFormatException(String message) {
        super(message);
    }

    public BundleFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
