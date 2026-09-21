package io.stormalmanac.gamedata;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * When a game's day rolls over, which is what decides the weekday an instant
 * falls on.
 *
 * <p>A game's day is not the calendar's. Punishing: Gray Raven resets at 05:00
 * on a server clock that runs UTC; Reverse: 1999 Global resets at 05:00 UTC−5.
 * So 03:00 UTC on a Monday is still Sunday to a PGR player, and a plan that
 * starts there and farms a Monday-only stage is farming a day that has not
 * begun. The planner cannot know that — asking it to would be a game-specific
 * branch in a game-agnostic module — so the answer lives here, on data hanging
 * off {@link Game}.
 *
 * <p>The zone is stored rather than an offset because an offset cannot summer.
 * A game whose servers keep a civil timezone shifts with it, and a fixed −5
 * would be an hour wrong for half the year; a fixed offset is a {@code ZoneId}
 * too — {@link ZoneOffset#UTC}, or the id {@code "UTC+7"} — so a game that
 * really is on one costs nothing to express and loses nothing by it.
 *
 * <p>This is deliberately <em>not</em> a day index. The horizon stays a scalar
 * (ADR 0013) and no variable in the model is indexed by day: all this changes is
 * which weekday day zero is, which is one {@code DayOfWeek} and no rows.
 *
 * @param zone the clock the game's servers keep
 * @param hour the hour on that clock at which the new day starts, 0–23
 */
public record DayBoundary(ZoneId zone, int hour) {

    /**
     * What every version published before this field existed was planned by, and
     * therefore what an undeclared boundary still means.
     *
     * <p>Midnight UTC is a placeholder and not a reading. A bundle that declares
     * nothing gets it, and gets exactly the plan it got before — which is the
     * point: a stored version is immutable, so the default has to be the old
     * behaviour rather than the better guess.
     */
    public static final DayBoundary UTC_MIDNIGHT = new DayBoundary(ZoneOffset.UTC, 0);

    public DayBoundary {
        if (zone == null) {
            throw new IllegalArgumentException("a day boundary needs the zone its hour is on");
        }
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("rollover hour must be 0-23, was " + hour);
        }
    }

    /**
     * The weekday of the game day containing this instant.
     *
     * <p>Subtracting the rollover hour before taking the date is the whole
     * trick: the hours between midnight and the reset belong to the day before.
     * Done on the zoned value rather than the instant so that a zone which
     * changes offset over the boundary moves the boundary with it.
     */
    public DayOfWeek dayOfWeekAt(Instant instant) {
        return instant.atZone(zone).minusHours(hour).getDayOfWeek();
    }

    /** True when this is the placeholder rather than something a reader was told. */
    public boolean isDefault() {
        return UTC_MIDNIGHT.equals(this);
    }
}
