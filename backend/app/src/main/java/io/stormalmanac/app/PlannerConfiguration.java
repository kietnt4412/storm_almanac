package io.stormalmanac.app;

import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.planner.InProcessSolveCache;
import io.stormalmanac.planner.MipOptimizer;
import io.stormalmanac.planner.Optimizer;
import io.stormalmanac.planner.SolveCache;
import io.stormalmanac.player.PlayerStateRepository;
import io.stormalmanac.stats.DropEstimateRepository;
import java.time.Clock;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The solver, assembled and finally reachable.
 *
 * <p>Until phase 3 there were no beans here, and the absence was the point:
 * {@link Optimizer} and {@link SolveCache} existed, were tested, and had no
 * player state to solve against, so registering them would have been ceremony —
 * a bean nothing consumes proves only that Spring can construct one. What
 * changed is that {@code player} has an implementation behind it and a route
 * above it, so these now sit on a path a real request takes.
 *
 * <p><b>{@code SolveCoordinator} is still not here, on the same reasoning.</b>
 * It exists to run a solve that does not fit in the synchronous budget and hand
 * the caller a ticket, and there is no asynchronous surface for a ticket to be
 * useful on — the WebSocket push that would make one is phase 9's neighbourhood.
 * The plan route below is synchronous because a two-second budget is a promise
 * it can keep; registering a queue nobody submits to would be the ceremony this
 * paragraph exists to refuse.
 */
@Configuration
public class PlannerConfiguration {

    /**
     * Which cache backs the solver, per {@code storm-almanac.substrate.solve-cache}.
     *
     * <p>The unimplemented choices fail loudly at startup rather than falling
     * back to the working one. A configuration value that silently means
     * something other than what it says is how a benchmark ends up measuring the
     * wrong implementation, which ADR 0003 exists to prevent.
     */
    @Bean
    SolveCache solveCache(@Value("${storm-almanac.substrate.solve-cache:in-process}") String choice) {
        return switch (choice) {
            case "in-process" -> new InProcessSolveCache();
            case "redis" -> throw new IllegalStateException(
                    "solve-cache=redis: RedisSolveCache is deferred until there is a second node "
                            + "to share with — see docs/adr/0012 and N19 in TRACKER.md");
            case "almanac-raft-kv" -> throw new IllegalStateException(
                    "solve-cache=almanac-raft-kv: the replicated KV is phase 8, and Track B is gated "
                            + "until the product is deployed — see TRACKER.md");
            default -> throw new IllegalStateException("unknown solve-cache '" + choice + "'");
        };
    }

    /**
     * @param estimates absent until phase 6 publishes one, and absent means
     *                  every coefficient is the bundle's declared yield. That is
     *                  the honest state of the world rather than a degraded mode,
     *                  and the plan says so in its notes
     */
    @Bean
    Optimizer optimizer(
            GameDefinitionRepository definitions,
            PlayerStateRepository players,
            ObjectProvider<DropEstimateRepository> estimates,
            SolveCache cache) {
        return new MipOptimizer(
                definitions,
                players,
                estimates.getIfAvailable(),
                Clock.systemUTC(),
                MipOptimizer.DEFAULT_BUDGET,
                cache);
    }
}
