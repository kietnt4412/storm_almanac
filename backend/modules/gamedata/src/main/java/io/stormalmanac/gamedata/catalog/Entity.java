package io.stormalmanac.gamedata.catalog;

import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.gamedata.Rarity;
import java.util.List;

/**
 * A character, or whatever the game calls one — a Construct in Punishing: Gray
 * Raven, an Arcanist in Reverse: 1999.
 *
 * <p>This is the catalog surface: the pages a stranger actually lands on from a
 * search for "reverse 1999 [character] materials". The planner does not need
 * any of the combat fields, but the funnel does, and the funnel is the hardest
 * problem in the project.
 *
 * @param element the game's own axis — Afflatus, Class — kept as a string
 */
public record Entity(
        EntityId id,
        String displayName,
        Rarity rarity,
        String element,
        List<String> tags,
        List<StatCurve> statCurves,
        List<Skill> skills,
        List<Talent> talents
) {
    public Entity {
        tags = List.copyOf(tags);
        statCurves = List.copyOf(statCurves);
        skills = List.copyOf(skills);
        talents = List.copyOf(talents);
    }
}
