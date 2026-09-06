package io.stormalmanac.app;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * One Postgres for every gamedata test class in this JVM, and one Spring
 * context to go with it.
 *
 * <p>Started in a static initialiser rather than by {@code @Testcontainers} and
 * {@code @Container}, which start and stop a container per test class. Two
 * classes need this database and more will follow; at roughly three seconds of
 * container startup and several of context boot each, per-class containers is a
 * cost the pipeline pays on every push forever. Nothing stops it: Testcontainers'
 * reaper removes it when the JVM exits, which is the documented shape for a
 * container shared across classes.
 *
 * <p>Because every subclass registers the same properties, Spring's test context
 * cache hands them all the same context too, so the application boots once.
 *
 * <p>{@code ApplicationBootTest} keeps its own container on purpose. It exists to
 * prove that the application starts from cold against a database nothing has
 * touched, and sharing a schema that another class has been writing to would
 * quietly weaken exactly the thing it is for.
 *
 * <p>The context serves HTTP because {@code GameDataApiTest} needs it to, and a
 * second context that differed only in {@code webEnvironment} would mean a
 * second container, a second boot and a second copy of this class. The two
 * classes that do not make a request are not harmed by a servlet container they
 * never call.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class GameDataDatabaseTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    protected JdbcTemplate jdbc;

    /**
     * Each test starts from an empty schema rather than from whatever the
     * previous one published. Deleting the version rows is enough:
     * {@code ON DELETE CASCADE} reaches every versioned table from there — which
     * is itself worth exercising, since V2 shipped a version that could not
     * actually be deleted and V3 is the fix.
     */
    @BeforeEach
    void emptyTheSchema() {
        jdbc.update("DELETE FROM gamedata.game_data_version");
        jdbc.update("DELETE FROM gamedata.game");
    }
}
