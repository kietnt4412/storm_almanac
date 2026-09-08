package io.stormalmanac.identity;

import java.util.Map;

/**
 * What a provider tells us about the person signing in, reduced to the three
 * things this service stores.
 *
 * <p>Every provider is a different shape of JSON and exactly one of the
 * differences matters: which key holds the stable subject. Google's OIDC
 * {@code sub}, Discord's {@code id}. The rest — a display name that might be
 * under {@code name} or {@code global_name} or {@code username} — is cosmetic,
 * and a missing one is a blank string rather than a failed sign-in. Refusing to
 * let someone in because their provider stopped sending a display name would be
 * an outage caused by decoration.
 *
 * <p>Kept as a small pure function so the provider-specific half is testable
 * without a provider. The alternative — reading these keys inside a Spring
 * {@code OAuth2UserService} — is only testable by standing up an authorization
 * server, which tests Spring's protocol implementation rather than ours.
 */
public record SignIn(String provider, String subject, String displayName, String email) {

    public SignIn {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("provider must not be blank");
        }
        if (subject == null || subject.isBlank()) {
            // Not a blank-string default like the others. Without a subject
            // there is no way to tell this person from the next one, and the
            // failure mode of guessing is handing them somebody else's account.
            throw new IllegalArgumentException(
                    "provider '" + provider + "' returned no stable subject claim");
        }
        displayName = displayName == null ? "" : displayName;
        email = email == null ? "" : email;
    }

    /**
     * Read a provider's user attributes.
     *
     * @param provider   the registration id, which is also what
     *                   {@code identity.account_identity.provider} stores
     * @param attributes whatever the provider returned, unmodified
     */
    public static SignIn from(String provider, Map<String, Object> attributes) {
        return new SignIn(
                provider,
                // OIDC says "sub" and every OIDC provider honours it. Discord is
                // OAuth2 and not OIDC, so it says "id" — the one difference that
                // is not cosmetic, and the reason this is a lookup rather than a
                // constant.
                string(attributes, "sub", "id"),
                string(attributes, "global_name", "name", "username"),
                string(attributes, "email"));
    }

    private static String string(Map<String, Object> attributes, String... keys) {
        for (String key : keys) {
            Object value = attributes.get(key);
            if (value != null && !value.toString().isBlank()) {
                return value.toString();
            }
        }
        return null;
    }
}
