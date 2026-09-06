package io.stormalmanac.api;

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
}
