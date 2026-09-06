package io.stormalmanac.api;

/**
 * The caller asked for something that is not there.
 *
 * <p>A separate type rather than a bare {@code ResponseStatusException} so that
 * the read models stay ignorant of HTTP: a read model's job is to say "no
 * published version 5 of proving-ground", and which status code that becomes is
 * the edge's business. {@link ApiExceptionHandler} makes the translation in one
 * place.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
