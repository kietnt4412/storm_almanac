package io.stormalmanac.api;

import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 0's entire product surface: the endpoint the pipeline deploys to a real
 * URL before any real code exists, so that the deploy path is proven while it
 * is still cheap to fix.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    private final String version;

    public HealthController(@Value("${storm-almanac.version:dev}") String version) {
        this.version = version;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "ok",
                "service", "storm-almanac",
                "version", version,
                "time", Instant.now().toString());
    }
}
