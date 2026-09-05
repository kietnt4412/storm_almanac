package io.stormalmanac.gamedata.catalog;

import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.gamedata.Rarity;
import java.util.List;

/**
 * A thing with an identity, a rarity and an upgrade graph of its own: a
 * character — a Construct in Punishing: Gray Raven, an Arcanist in
 * Reverse: 1999 — or a piece of equipment, a Psychube or a Memory.
 *
 * <p>Equipment is an {@code Entity} rather than a concept of its own, because
 * {@link io.stormalmanac.gamedata.Upgrade} and
 * {@link io.stormalmanac.gamedata.Goal} both key on {@link EntityId}, and gear
 * is upgradeable and is a legitimate goal. See ADR 0007.
 *
 * <p>This is the catalog surface: the pages a stranger actually lands on from a
 * search for "reverse 1999 [character] materials". The planner does not need
 * any of the combat fields, but the funnel does, and the funnel is the hardest
 * problem in the project.
 *
 * @param kind    what sort of thing this is — {@code "character"},
 *                {@code "equipment"} — supplied by the game bundle and opaque to
 *                us. It routes and groups the catalog. <b>The planner, gacha and
 *                stats modules must never read it:</b> the moment behaviour
 *                depends on it, the distinction was real and ADR 0007 was wrong.
 * @param element the game's own axis — Afflatus, Class — kept as a string, and
 *                empty for equipment, which sits on no such axis
 */
public record Entity(
        EntityId id,
        String displayName,
        String kind,
        Rarity rarity,
        String element,
        List<String> tags,
        List<StatCurve> statCurves,
        List<Skill> skills,
        List<Talent> talents
) {
    public Entity {
        // Required, never defaulted: a bundle omitting it yields an unroutable catalog.
        if (kind == null || kind.isBlank()) {
            throw new IllegalArgumentException("kind is required, e.g. \"character\" or \"equipment\"");
        }
        tags = List.copyOf(tags);
        statCurves = List.copyOf(statCurves);
        skills = List.copyOf(skills);
        talents = List.copyOf(talents);
    }
}
