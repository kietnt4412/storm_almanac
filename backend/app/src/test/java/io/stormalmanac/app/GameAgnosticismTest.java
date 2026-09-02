package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The rule that makes or breaks the project, as a failing build rather than a
 * note in the README.
 *
 * <p>No game-specific branching in planner, gacha or stats. Not one
 * {@code if (game == REVERSE_1999)}. A new game is a {@code GameDefinition}
 * bundle plus a parser adapter, nothing more — and phase 11 exists to prove the
 * line held. This test is what makes that proof cheap to repeat rather than a
 * thing rediscovered under deadline.
 *
 * <p>It reads source rather than bytecode because the failure mode being
 * guarded against is a string literal or a comparison against a slug, neither
 * of which reliably survives into a form ArchUnit can see.
 */
class GameAgnosticismTest {

    /** Module source roots that must stay game-agnostic. */
    private static final List<Path> GUARDED = List.of(
            Path.of("..", "modules", "planner", "src", "main", "java"),
            Path.of("..", "modules", "gacha", "src", "main", "java"),
            Path.of("..", "modules", "stats", "src", "main", "java"));

    /** Slugs and names that only ever belong in a game-data bundle or an adapter. */
    private static final Pattern FORBIDDEN = Pattern.compile(
            "reverse[_\\s-]?1999|r1999"
                    + "|punishing[_\\s-]?gray[_\\s-]?raven"
                    + "|\\bpgr\\b|bluepoch|kuro\\s*games"
                    + "|arcanist|afflatus|\\bsonorous\\b"
                    + "|\\bresonance\\b|\\bmemory\\s+enhancement\\b",
            Pattern.CASE_INSENSITIVE);

    /**
     * Prose naming a game to explain <em>why</em> an abstraction has the shape it
     * has is the point of the comment, not a violation. Only code is scanned.
     */
    @Test
    @DisplayName("planner, gacha and stats contain no game-specific code")
    void noGameSpecificCode() throws IOException {
        List<String> violations = new ArrayList<>();

        for (Path root : GUARDED) {
            if (!Files.isDirectory(root)) {
                throw new IllegalStateException(
                        "guarded source root is missing: " + root.toAbsolutePath());
            }
            try (Stream<Path> files = Files.walk(root)) {
                for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                    scan(file, violations);
                }
            }
        }

        assertThat(violations)
                .as("a new game must be a GameDefinition bundle plus a parser adapter, nothing more")
                .isEmpty();
    }

    private static void scan(Path file, List<String> violations) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        boolean inBlockComment = false;

        for (int i = 0; i < lines.size(); i++) {
            String trimmed = lines.get(i).trim();

            if (inBlockComment) {
                if (trimmed.contains("*/")) {
                    inBlockComment = false;
                }
                continue;
            }
            if (trimmed.startsWith("/*")) {
                if (!trimmed.contains("*/")) {
                    inBlockComment = true;
                }
                continue;
            }
            if (trimmed.startsWith("//") || trimmed.startsWith("*")) {
                continue;
            }

            if (FORBIDDEN.matcher(stripTrailingComment(lines.get(i))).find()) {
                violations.add("%s:%d  %s".formatted(file, i + 1, trimmed));
            }
        }
    }

    private static String stripTrailingComment(String line) {
        int marker = line.indexOf("//");
        String code = marker >= 0 ? line.substring(0, marker) : line;
        return code.toLowerCase(Locale.ROOT);
    }
}
