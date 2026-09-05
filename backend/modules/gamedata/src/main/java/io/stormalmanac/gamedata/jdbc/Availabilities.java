package io.stormalmanac.gamedata.jdbc;

import io.stormalmanac.gamedata.Availability;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link Availability} across the JDBC boundary, in both directions.
 *
 * <p>The weekday set is a {@code TEXT[]} column. It stayed an array rather than
 * becoming a {@code smallint} bitmask because a human approving a publish reads
 * these rows in {@code psql}, and {@code {MONDAY,THURSDAY}} is legible where
 * {@code 9} is not — see ADR 0008.
 */
final class Availabilities {

    private Availabilities() {}

    /**
     * The array literal Postgres parses back into {@code TEXT[]}.
     *
     * <p>Written as a literal rather than through {@code createArrayOf} because
     * that call needs the raw {@code Connection} and this needs a bind
     * parameter. It is safe against injection for the reason that matters:
     * {@link DayOfWeek} is a closed set of seven names with no quoting to
     * escape, and anything else cannot reach here.
     *
     * <p>Sorted, so that two bundles declaring the same days in different orders
     * produce the same row and the patch diff does not report a change nobody
     * made.
     */
    static String days(Availability availability) {
        return availability.days().stream()
                .sorted(Comparator.naturalOrder())
                .map(DayOfWeek::name)
                .collect(Collectors.joining(",", "{", "}"));
    }

    /** An empty array means every day, exactly as {@code Availability} reads it. */
    static Availability read(ResultSet rs) throws SQLException {
        Array days = rs.getArray("available_days");
        Set<DayOfWeek> parsed = EnumSet.noneOf(DayOfWeek.class);
        if (days != null) {
            Arrays.stream((String[]) days.getArray()).map(DayOfWeek::valueOf).forEach(parsed::add);
        }
        return new Availability(parsed, Timestamps.instant(rs, "opens_at"), Timestamps.instant(rs, "closes_at"));
    }
}
