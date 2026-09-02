package io.stormalmanac.planner;

/**
 * The mixed-integer program.
 *
 * <pre>
 * minimize    sum over stages s of  x_s * energy_s
 * subject to  for every item i:
 *               sum_s x_s * drop[s,i]
 *             + sum_c y_c * (produce[c,i] - consume[c,i])
 *             + inventory_i  >=  demand_i
 *             x_s in Z>=0     stage runs are whole numbers
 *             y_c in Z>=0     so are conversions
 * </pre>
 *
 * <p>Solved with ojAlgo's {@code ExpressionsBasedModel} — pure Java, so there
 * are no native binaries to fight inside a container. OR-Tools is the
 * documented fallback if the MIP outgrows it.
 *
 * <p>Budget is two seconds synchronous. Past that the request is queued and the
 * result pushed over WebSocket, which is precisely the queue phase 9 replicates.
 */
public interface Optimizer {

    /**
     * @throws InfeasibleGoalException when no combination of available sources
     *         can reach the goal set — usually an expired event stage, and the
     *         message must say which
     */
    Plan solve(SolveRequest request);

    class InfeasibleGoalException extends RuntimeException {
        public InfeasibleGoalException(String message) {
            super(message);
        }
    }
}
