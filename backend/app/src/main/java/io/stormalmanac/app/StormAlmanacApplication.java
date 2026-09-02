package io.stormalmanac.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * The single deployable.
 *
 * <p>A modular monolith: one process, hard module boundaries, separate schemas,
 * no cross-module database reads. Modules communicate through internal events,
 * so extracting one later is a deployment change rather than a rewrite.
 */
@SpringBootApplication
@ComponentScan("io.stormalmanac")
public class StormAlmanacApplication {

    public static void main(String[] args) {
        SpringApplication.run(StormAlmanacApplication.class, args);
    }
}
