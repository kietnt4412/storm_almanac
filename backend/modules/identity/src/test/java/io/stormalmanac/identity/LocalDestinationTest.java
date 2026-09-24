package io.stormalmanac.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What either sign-in will send a reader to afterwards. The cases that matter
 * are the ones that look local and are not.
 */
class LocalDestinationTest {

    @Test
    @DisplayName("a path on this site is kept as written, query and all")
    void localPathsAreKept() {
        for (String local : List.of("/", "/catalog", "/catalog/punishing-gray-raven/lucia-inverse-crown", "/plan?x=1")) {
            assertThat(LocalDestination.of(local)).as(local).isEqualTo(local);
        }
    }

    @Test
    @DisplayName("anything that leaves the origin, or is not a path, becomes /")
    void everythingElseIsRoot() {
        for (String foreign : List.of(
                "https://attacker.example/",
                "//attacker.example/",
                "/\\attacker.example/",
                "catalog",
                "",
                "javascript:alert(1)")) {
            assertThat(LocalDestination.of(foreign)).as(foreign).isEqualTo("/");
        }
    }

    @Test
    @DisplayName("a control character is refused, because the destination becomes a header")
    void controlCharactersAreRefused() {
        assertThat(LocalDestination.of("/catalog\r\nSet-Cookie: x=y")).isEqualTo("/");
    }

    @Test
    @DisplayName("no destination is /")
    void nullIsRoot() {
        assertThat(LocalDestination.of(null)).isEqualTo("/");
    }
}
