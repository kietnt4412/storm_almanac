package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;

import io.stormalmanac.api.HealthController;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Phase 0's exit criterion is a green pipeline deploying this to a real URL.
 * Unit-level here; the deploy itself is proven by the smoke step in CI.
 */
class HealthControllerTest {

    @Test
    void reportsOkAndTheDeployedVersion() {
        Map<String, Object> body = new HealthController("1.2.3").health();

        assertThat(body).containsEntry("status", "ok");
        assertThat(body).containsEntry("service", "storm-almanac");
        assertThat(body).containsEntry("version", "1.2.3");
        assertThat(body).containsKey("time");
    }
}
