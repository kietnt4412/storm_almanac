package io.stormalmanac.player;

import io.stormalmanac.common.id.ProfileId;
import java.time.Instant;

/**
 * The last plan a profile was shown, and what was asked to get it.
 *
 * <p><b>Both halves are opaque JSON here, on purpose.</b> The player module is
 * not allowed to know what a plan is — the planner's types are not on its
 * classpath, and the wire shape belongs to the api module — so it keeps the
 * request and the answer as documents it never reads, exactly as it keeps a
 * pity counter's scope key. What it does read is {@code gameVersion}: the
 * sequence the plan was solved against, which is what "what has changed since"
 * is measured from (ADR 0037).
 *
 * @param gameVersion the published sequence the plan was solved against
 * @param request     what the reader asked with — energy a day, horizon,
 *                    objective and reach — as JSON
 * @param plan        the plan as the reader was shown it, as JSON
 */
public record SavedPlan(ProfileId profile, long gameVersion, String request, String plan, Instant savedAt) {

    public SavedPlan {
        if (gameVersion < 0) {
            throw new IllegalArgumentException("gameVersion must be >= 0, was " + gameVersion);
        }
        if (request == null || request.isBlank()) {
            throw new IllegalArgumentException("a saved plan keeps the request it answered");
        }
        if (plan == null || plan.isBlank()) {
            throw new IllegalArgumentException("a saved plan keeps the plan");
        }
    }
}
