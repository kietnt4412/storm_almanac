package io.stormalmanac.gamedata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.common.id.GameId;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * When a game's day starts, and therefore which weekday an instant is on.
 *
 * <p>Small arithmetic, and the reason it is worth its own class is that getting
 * it wrong is invisible. A plan built on the wrong weekday is a plan: it solves,
 * it prints, it says a number. It is simply a day of energy in the wrong bucket.
 */
class DayBoundaryTest {

    /** 03:00 UTC on a Monday — before a 05:00 reset, after a midnight one. */
    private static final Instant EARLY_MONDAY = Instant.parse("2026-09-21T03:00:00Z");

    @Test
    @DisplayName("before the reset, the day is still yesterday's")
    void theHoursBeforeTheResetBelongToTheDayBefore() {
        DayBoundary pgr = new DayBoundary(ZoneId.of("UTC"), 5);

        assertThat(pgr.dayOfWeekAt(EARLY_MONDAY)).isEqualTo(DayOfWeek.SUNDAY);
        assertThat(pgr.dayOfWeekAt(Instant.parse("2026-09-21T05:00:00Z"))).isEqualTo(DayOfWeek.MONDAY);
        // The minute before the reset is the last minute of Sunday, which is the
        // boundary the whole record exists to place.
        assertThat(pgr.dayOfWeekAt(Instant.parse("2026-09-21T04:59:00Z"))).isEqualTo(DayOfWeek.SUNDAY);
    }

    @Test
    @DisplayName("the placeholder reads a weekday exactly as the planner did before it existed")
    void midnightUtcIsTheOldBehaviour() {
        assertThat(DayBoundary.UTC_MIDNIGHT.dayOfWeekAt(EARLY_MONDAY)).isEqualTo(DayOfWeek.MONDAY);
        assertThat(DayBoundary.UTC_MIDNIGHT.isDefault()).isTrue();
        assertThat(new DayBoundary(ZoneId.of("UTC"), 5).isDefault()).isFalse();
    }

    @Test
    @DisplayName("a game on a civil timezone rolls over on that zone's clock, summer or not")
    void theZoneIsAZoneRatherThanAnOffset() {
        // A game whose servers keep a civil timezone rather than a fixed offset:
        // 05:00 local is 09:00 UTC in summer and 10:00 UTC in winter, and a
        // stored offset could only ever have been one of those. Whether any
        // published game here is one is a reading nobody has taken; the point is
        // that the format does not force the answer.
        DayBoundary civil = new DayBoundary(ZoneId.of("America/New_York"), 5);

        // 2026-07-06 is a Monday in EDT: 08:59 UTC is 04:59 local, still Sunday.
        assertThat(civil.dayOfWeekAt(Instant.parse("2026-07-06T08:59:00Z"))).isEqualTo(DayOfWeek.SUNDAY);
        assertThat(civil.dayOfWeekAt(Instant.parse("2026-07-06T09:00:00Z"))).isEqualTo(DayOfWeek.MONDAY);
        // 2026-12-07 is a Monday in EST, an hour later in UTC.
        assertThat(civil.dayOfWeekAt(Instant.parse("2026-12-07T09:59:00Z"))).isEqualTo(DayOfWeek.SUNDAY);
        assertThat(civil.dayOfWeekAt(Instant.parse("2026-12-07T10:00:00Z"))).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    @DisplayName("an hour that is not an hour of the day is refused, not wrapped")
    void anHourOutsideTheDayIsRefused() {
        assertThatThrownBy(() -> new DayBoundary(ZoneId.of("UTC"), 24))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("0-23");
        assertThatThrownBy(() -> new DayBoundary(ZoneId.of("UTC"), -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DayBoundary(null, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("zone");
    }

    @Test
    @DisplayName("a game that declares no boundary is planned by the placeholder, not by nothing")
    void anUndeclaredBoundaryFallsBack() {
        Game unread = new Game(new GameId("g"), "G", "Vigour");

        assertThat(unread.dayBoundary()).isNull();
        assertThat(unread.dayBoundaryOrDefault()).isEqualTo(DayBoundary.UTC_MIDNIGHT);
    }
}
