package io.stormalmanac.api;

/**
 * A request that was understood and has no answer — a banner that has closed,
 * a banner nobody has priced.
 *
 * <p>422 and not 400, for the reason {@code Optimizer.InfeasibleGoalException}
 * is: there is nothing in the request to fix, and the message says what is
 * missing instead.
 */
public class UnanswerableException extends RuntimeException {

    public UnanswerableException(String message) {
        super(message);
    }
}
