package io.stormalmanac.gamedata;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.Set;

/**
 * When a source can actually be used.
 *
 * <p>Stages rotate by weekday and event stages expire, which is what makes the
 * optimizer time-indexed rather than a single static LP — and time-indexing is
 * where the interesting part of phase 2 lives.
 *
 * @param days     empty means every day
 * @param opensAt  null means "since forever"
 * @param closesAt null means "no announced end"
 */
public record Availability(Set<DayOfWeek> days, Instant opensAt, Instant closesAt) {

    public static final Availability ALWAYS = new Availability(Set.of(), null, null);

    public Availability {
        days = days == null ? Set.of() : Set.copyOf(days);
    }

    public boolean isOpenOn(DayOfWeek day, Instant at) {
        if (!days.isEmpty() && !days.contains(day)) return false;
        if (opensAt != null && at.isBefore(opensAt)) return false;
        return closesAt == null || at.isBefore(closesAt);
    }

    /** True if this source will disappear, which makes it worth prioritising. */
    public boolean isExpiring() {
        return closesAt != null;
    }
}
