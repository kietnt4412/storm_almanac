package io.stormalmanac.api;

import io.stormalmanac.identity.CurrentAccount;
import io.stormalmanac.planner.Optimizer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Turns the edge's refusals into RFC 9457 problem documents.
 *
 * <p>The detail is the message the read model wrote, and those messages are
 * written to be acted on: "no published version 5 of proving-ground" tells the
 * caller what to try instead, where a bare 404 tells them to guess. This is the
 * same principle {@code gamedata-cli} follows for a refused bundle, and for the
 * same reason.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail notFound(ResourceNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail conflict(ConflictException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    /**
     * A malformed identifier is the caller's mistake, not a server fault.
     *
     * <p>The typed-id records reject a blank slug by throwing, and every
     * {@code GameDataVersion} and domain record validates itself in its
     * canonical constructor. Without this, a request for an empty game id would
     * be a 500 in the logs rather than a 400 in the response.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badRequest(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /**
     * An account-scoped route reached without an account.
     *
     * <p>The filter chain should have refused this already, so reaching here is
     * a misconfiguration rather than an ordinary anonymous request — but it
     * answers 401 all the same, because the caller's correct next move is to
     * sign in either way, and a 500 would tell them to file a bug instead.
     */
    @ExceptionHandler(CurrentAccount.NotSignedInException.class)
    ProblemDetail notSignedIn(CurrentAccount.NotSignedInException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    /**
     * The goal set cannot be reached from anything the game currently offers —
     * an expired event stage, an item sold only in a shop the model does not
     * price.
     *
     * <p>422 rather than 400: the request was well-formed and was understood,
     * and the answer is that no plan exists. A 400 would tell the caller to fix
     * their request, and there is nothing in it to fix. The message names the
     * item that could not be sourced, which is the only thing that makes this
     * actionable.
     */
    @ExceptionHandler(Optimizer.InfeasibleGoalException.class)
    ProblemDetail infeasible(Optimizer.InfeasibleGoalException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }
}
