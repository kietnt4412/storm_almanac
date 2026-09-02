package io.stormalmanac.common.event;

import java.time.Instant;

/**
 * Modules do not read each other's tables. They publish these instead.
 *
 * <p>That constraint is what makes extracting a module into its own service a
 * deployment change rather than a rewrite, so it is enforced by an ArchUnit
 * test rather than by good intentions.
 */
public interface DomainEvent {
    Instant occurredAt();
}
