package io.stormalmanac.app;

import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * The single deployable.
 *
 * <p>A modular monolith: one process, hard module boundaries, separate schemas,
 * no cross-module database reads. Modules communicate through internal events,
 * so extracting one later is a deployment change rather than a rewrite.
 *
 * <p>The same jar is also {@code gamedata-cli} — see {@link GameDataCli}. One
 * artifact rather than two, because a separate CLI build would be a second
 * thing to keep in step with the schema, and the day it drifts is the day
 * somebody publishes with it.
 */
@SpringBootApplication
@ComponentScan("io.stormalmanac")
public class StormAlmanacApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(StormAlmanacApplication.class);
        if (isCliInvocation(args)) {
            // Decided before the context starts, because by the time a runner
            // could ask, the server has already tried to bind — and the host
            // running the CLI is usually the host already serving on that port.
            application.setWebApplicationType(WebApplicationType.NONE);
        }
        application.run(args);
    }

    private static boolean isCliInvocation(String[] args) {
        return Arrays.stream(args).anyMatch(arg -> arg.startsWith("--" + GameDataCli.OPTION));
    }
}
