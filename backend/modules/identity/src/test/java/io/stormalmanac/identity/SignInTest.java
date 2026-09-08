package io.stormalmanac.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The one part of sign-in that differs per provider, tested without a provider.
 *
 * <p>These attribute maps are what Google and Discord actually return, reduced
 * to the keys this service reads. Everything else about the flow is Spring's
 * implementation of two protocols and is not worth re-testing; this is the part
 * that is ours to get wrong, and getting it wrong means either refusing a valid
 * sign-in or — far worse — reading a subject from a field the provider does not
 * promise to keep stable.
 */
class SignInTest {

    @Test
    @DisplayName("Google's OIDC claims: the subject is sub, the name is name")
    void google() {
        SignIn signIn = SignIn.from(
                "google",
                Map.of(
                        "sub", "104829375618293746501",
                        "name", "Nguyen Tuan Kiet",
                        "email", "someone@gmail.com",
                        "picture", "https://example.invalid/avatar.png"));

        assertThat(signIn.provider()).isEqualTo("google");
        assertThat(signIn.subject()).isEqualTo("104829375618293746501");
        assertThat(signIn.displayName()).isEqualTo("Nguyen Tuan Kiet");
        assertThat(signIn.email()).isEqualTo("someone@gmail.com");
    }

    @Test
    @DisplayName("Discord is not OIDC, so its stable subject is id rather than sub")
    void discord() {
        SignIn signIn = SignIn.from(
                "discord",
                Map.of(
                        "id", "80351110224678912",
                        "username", "vertin",
                        "global_name", "Vertin",
                        "email", "someone@example.com"));

        // The one difference that is not cosmetic. Reading "sub" here would find
        // nothing and reading "username" would find something that changes.
        assertThat(signIn.subject()).isEqualTo("80351110224678912");
        // global_name is the one Discord shows, and it wins over the handle.
        assertThat(signIn.displayName()).isEqualTo("Vertin");
    }

    @Test
    @DisplayName("a provider that sends only the legacy handle still gets a display name")
    void discordWithoutAGlobalName() {
        SignIn signIn = SignIn.from("discord", Map.of("id", "8035111", "username", "vertin"));

        assertThat(signIn.displayName()).isEqualTo("vertin");
    }

    @Test
    @DisplayName("a missing display name or email is blank, not a failed sign-in")
    void decorationIsOptional() {
        SignIn signIn = SignIn.from("google", Map.of("sub", "abc"));

        // Refusing to let somebody in because their provider stopped sending a
        // display name would be an outage caused by decoration.
        assertThat(signIn.displayName()).isEmpty();
        assertThat(signIn.email()).isEmpty();
    }

    @Test
    @DisplayName("a blank claim is treated as absent rather than stored as a name")
    void blankIsAbsent() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "abc");
        attributes.put("name", "   ");
        attributes.put("email", "");

        SignIn signIn = SignIn.from("google", attributes);

        assertThat(signIn.displayName()).isEmpty();
        assertThat(signIn.email()).isEmpty();
    }

    @Test
    @DisplayName("no stable subject is refused by name, because guessing means handing over an account")
    void noSubjectIsRefused() {
        assertThatThrownBy(() -> SignIn.from("google", Map.of("name", "Vertin", "email", "a@b.c")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("google")
                .hasMessageContaining("stable subject");
    }

    @Test
    @DisplayName("the email is never read as a subject, however tempting the shape")
    void theEmailIsNotAnIdentity() {
        // A provider that sends an email and no subject is not a provider we can
        // tell one person from another with. This is the account-takeover route
        // the schema's third decision exists to close, asserted rather than
        // merely commented.
        assertThatThrownBy(() -> SignIn.from("discord", Map.of("email", "someone@example.com")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
