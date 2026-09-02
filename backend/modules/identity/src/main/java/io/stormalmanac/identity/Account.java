package io.stormalmanac.identity;

import io.stormalmanac.common.id.AccountId;
import java.time.Instant;

/**
 * A person. Authentication is delegated to Google and Discord — Discord because
 * that is where these communities already are — so no password ever reaches
 * this service.
 */
public record Account(AccountId id, String displayName, String email, Instant createdAt) {}
