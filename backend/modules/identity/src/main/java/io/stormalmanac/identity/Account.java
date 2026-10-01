package io.stormalmanac.identity;

import io.stormalmanac.common.id.AccountId;
import java.time.Instant;

/**
 * A person. Authentication is delegated to Google and Discord — Discord because
 * that is where these communities already are — so no password ever reaches
 * this service.
 *
 * @param pictureUrl the provider's picture of this person, an {@code https}
 *                   link it hosts; null when it sent none
 */
public record Account(AccountId id, String displayName, String email, String pictureUrl, Instant createdAt) {}
