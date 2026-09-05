package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Boots the whole application against a real database, over real HTTP.
 *
 * <p>{@link HealthControllerTest} calls the controller method directly, which is
 * worth having and proves almost nothing about the deployed system. Everything
 * that broke the first time this application was actually started was invisible
 * to it: the datasource opening, Flyway running {@code V1__baseline.sql},
 * Hibernate validating against the migrated schema, and — the one that actually
 * bit — Spring Security's default auto-configuration answering 401 to the one
 * endpoint phase 0 exists to deploy.
 *
 * <p>This test is the regression guard for all four. It is slow on purpose.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ApplicationBootTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private TestRestTemplate http;

    @Test
    @DisplayName("the health endpoint answers 200 to an anonymous request")
    void healthIsPublic() {
        // No credentials. The deploy smoke test has none to offer either.
        ResponseEntity<Map<String, Object>> response =
                http.exchange("/api/health", HttpMethod.GET, null, MAP);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "ok").containsEntry("service", "storm-almanac");
    }

    @Test
    @DisplayName("anything that is not explicitly public is denied, not merely unmapped")
    void everythingElseIsDenied() {
        // Guards the other direction: a chain that permits everything would pass
        // the test above and ship an open API in phase 3.
        assertThat(http.getForEntity("/api/anything-else", String.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private static final ParameterizedTypeReference<Map<String, Object>> MAP = new ParameterizedTypeReference<>() {};
}
