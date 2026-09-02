package io.stormalmanac.common.event;

/**
 * The one-way door between modules. In-process today (Spring's
 * {@code ApplicationEventPublisher}); a broker later, without callers noticing.
 */
@FunctionalInterface
public interface EventPublisher {
    void publish(DomainEvent event);
}
