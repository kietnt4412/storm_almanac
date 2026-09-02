package io.stormalmanac.planner;

import io.stormalmanac.common.id.PlanId;
import java.util.Optional;

/**
 * Assigns solves to workers, exactly once.
 *
 * <p>The second seam of the two-track structure. A single-node implementation
 * ships in phase 2 and stays; the Raft-backed one arrives in phase 9. The
 * requirement that makes consensus the honest answer rather than a decoration:
 * a node dying mid-solve must have its job reassigned, not lost and not run
 * twice, which is a replicated log with a leader.
 *
 * @see io.stormalmanac.raft
 */
public interface SolveCoordinator {

    /**
     * @param idempotencyKey identical keys must yield one execution, however
     *                       many times a client retries or a leader changes
     */
    PlanId submit(SolveRequest request, String idempotencyKey);

    Optional<Plan> poll(PlanId id);

    /** Depth of the queue, for backpressure at the API edge. */
    int pending();
}
