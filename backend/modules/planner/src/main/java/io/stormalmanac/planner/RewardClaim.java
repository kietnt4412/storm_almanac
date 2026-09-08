package io.stormalmanac.planner;

/**
 * "Twenty-eight daily logins and four weekly quests, over the plan's horizon."
 *
 * <p>Free income is not an instruction — nobody has to be told to collect a
 * daily login — but it is the reason a plan costs what it costs, and a plan that
 * silently assumed it would look like the solver had found energy from nowhere.
 * So it is reported: these are the grants the plan is <em>counting on</em>, and
 * a player who does not log in for a fortnight is looking at a different plan.
 *
 * <p>Separate from {@link Conversion} rather than folded into it because the two
 * read differently under a plan. A conversion is work; this is arithmetic about
 * waiting.
 */
public record RewardClaim(String reward, int times) {
    public RewardClaim {
        if (times < 0) throw new IllegalArgumentException("times must not be negative");
    }
}
