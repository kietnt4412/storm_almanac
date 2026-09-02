package io.stormalmanac.player;

import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gamedata.Goal;
import java.util.List;

/** What the profile is aiming at. Ordered, because priority changes the plan. */
public record Goals(ProfileId profile, List<Goal> goals) {
    public Goals {
        goals = List.copyOf(goals);
    }
}
