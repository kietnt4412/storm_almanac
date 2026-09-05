package io.stormalmanac.gamedata.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * {@code TIMESTAMPTZ} to {@link Instant} and back, without going through the
 * JVM's default time zone.
 *
 * <p>{@code java.sql.Timestamp} would work and would be converted via whatever
 * zone the machine happens to be set to. That round-trips correctly today and
 * is a latent difference between a developer's laptop and a UTC container, for
 * no benefit: the driver maps {@link OffsetDateTime} to {@code TIMESTAMPTZ}
 * directly.
 */
final class Timestamps {

    private Timestamps() {}

    static OffsetDateTime at(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    static Instant instant(ResultSet rs, String column) throws SQLException {
        OffsetDateTime value = rs.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }
}
