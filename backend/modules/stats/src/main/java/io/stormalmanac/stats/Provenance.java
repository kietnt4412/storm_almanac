package io.stormalmanac.stats;

/**
 * Where an estimate came from. Shown on every number in the UI alongside its
 * sample size, because visible honesty about data quality is a feature and the
 * cold start makes it unavoidable anyway.
 */
public enum Provenance {
    /** Imported from published community data on day one, so the tool is useful with zero reports. */
    SEEDED,
    /** Derived from reports submitted to this instance. Displaces SEEDED once the sample supports it. */
    COMMUNITY
}
