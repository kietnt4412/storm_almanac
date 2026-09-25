package io.stormalmanac.api;

/**
 * The caller asked for something the account already has, and may have only once.
 *
 * <p>The same split as {@link ResourceNotFoundException}: the controller says what
 * is taken, and {@link ApiExceptionHandler} decides that it is a 409.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
