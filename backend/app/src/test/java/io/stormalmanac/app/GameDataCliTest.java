package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;

import io.stormalmanac.common.id.GameId;
import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.gamedata.ingest.GameDataIngestRepository;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * {@code gamedata-cli}, driven the way an operator drives it.
 *
 * <p>The commands are exercised through {@link GameDataCli#execute}, not through
 * {@code run} — {@code run} ends in {@code System.exit}, and a test that called
 * it would take the JVM with it. What is asserted is the pair an operator
 * actually acts on: the exit code, and the sentence printed. A tool that refuses
 * a bundle with a stack trace has told somebody that something is wrong and
 * nothing about what.
 *
 * <p>The whole point of this being a CLI is in the plan: "onboarding a title
 * never requires touching backend code". So the walkthrough below — preview,
 * ingest, publish, diff — is the acceptance test for that claim, and it runs
 * against the same repositories the server uses.
 */
class GameDataCliTest extends GameDataDatabaseTest {

    private static final GameId PROVING_GROUND = new GameId("proving-ground");

    @Autowired
    private GameDataIngestRepository ingest;

    @Autowired
    private GameDefinitionRepository definitions;

    @Autowired
    private ApplicationContext context;

    private ByteArrayOutputStream printed;
    private GameDataCli cli;

    @BeforeEach
    void freshCli() {
        printed = new ByteArrayOutputStream();
        cli = new GameDataCli(ingest, definitions, context,
                new PrintStream(printed, true, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("a title is onboarded end to end without touching backend code")
    void theWholeWalkthrough() {
        // 1. Is the file even well-formed? No database involved yet.
        assertThat(run("validate", fixture("1.0"))).isEqualTo(GameDataCli.OK);
        assertThat(output()).contains("proving-ground 1.0 is well-formed")
                .contains("6 items").contains("2 entities");

        // 2. What would it change? Nothing is published, so the honest answer is
        //    "this would be the first", not an empty diff.
        assertThat(run("preview", fixture("1.0"))).isEqualTo(GameDataCli.OK);
        assertThat(output()).contains("no published version of proving-ground yet");

        // 3. Write it as a draft. Still not live.
        assertThat(run("ingest", fixture("1.0"))).isEqualTo(GameDataCli.OK);
        assertThat(output()).contains("as a draft at sequence 0")
                .contains("--gamedata=publish proving-ground 0");
        assertThat(definitions.findLatest(PROVING_GROUND)).isEmpty();

        assertThat(run("drafts", "proving-ground")).isEqualTo(GameDataCli.OK);
        assertThat(output()).contains("0").contains("1.0");

        // 4. Approve it. Now it is the answer to "latest".
        assertThat(run("publish", "proving-ground", "0")).isEqualTo(GameDataCli.OK);
        assertThat(output()).contains("published proving-ground 1.0 at");
        assertThat(definitions.findLatest(PROVING_GROUND)).isPresent();

        // 5. The patch arrives. Preview shows what approving it would move,
        //    before anything is written.
        assertThat(run("preview", fixture("1.1"))).isEqualTo(GameDataCli.OK);
        assertThat(output()).contains("~ stage 'pg-1-1' · drop ore-rough: 1.4 → 1.6")
                .contains("- stage 'pg-event-1'");

        assertThat(run("ingest", fixture("1.1"))).isEqualTo(GameDataCli.OK);
        assertThat(run("publish", "proving-ground", "1")).isEqualTo(GameDataCli.OK);

        // 6. And the two published versions can be compared afterwards, which is
        //    what goes in a release note.
        assertThat(run("diff", "proving-ground", "0", "1")).isEqualTo(GameDataCli.OK);
        assertThat(output()).contains("proving-ground: 1.0 → 1.1")
                .contains("progression · ").contains("catalog · ");

        assertThat(run("versions", "proving-ground")).isEqualTo(GameDataCli.OK);
        assertThat(output()).contains("1.1").contains("1.0");
    }

    @Test
    @DisplayName("a file that is not there is refused with the path it looked at")
    void aMissingFileIsRefused() {
        assertThat(run("validate", "bundles/nope.json")).isEqualTo(GameDataCli.REFUSED);
        assertThat(output()).startsWith("refused: cannot read").contains("nope.json");
    }

    @Test
    @DisplayName("publishing a sequence with no draft is refused, not silently ignored")
    void publishingNothingIsRefused() {
        assertThat(run("publish", "proving-ground", "9")).isEqualTo(GameDataCli.REFUSED);
        assertThat(output()).contains("no draft of proving-ground at sequence 9");
    }

    @Test
    @DisplayName("diffing against a version that was never published is refused")
    void diffingAnUnpublishedVersionIsRefused() {
        assertThat(run("ingest", fixture("1.0"))).isEqualTo(GameDataCli.OK);

        // The draft is in the database. The diff still cannot see it, because
        // GameDefinitionRepository only reads approved snapshots — and that is
        // the reason `preview` exists and takes a file.
        assertThat(run("diff", "proving-ground", "0", "1")).isEqualTo(GameDataCli.REFUSED);
        assertThat(output()).contains("must name a published version");
    }

    @Test
    @DisplayName("a wrong command line is told apart from wrong data")
    void misuseIsItsOwnExitCode() {
        // Exit code 2, not 1. A script that retries on a bad file must not retry
        // on a typo in its own arguments.
        assertThat(run("summon")).isEqualTo(GameDataCli.MISUSE);
        assertThat(output()).contains("unknown command 'summon'").contains("usage:");

        assertThat(run("publish", "proving-ground")).isEqualTo(GameDataCli.MISUSE);
        assertThat(output()).contains("publish <game> <sequence>");
    }

    @Test
    @DisplayName("a sequence that is not a number is refused as data, not as misuse")
    void aNonNumericSequence() {
        assertThat(run("publish", "proving-ground", "latest")).isEqualTo(GameDataCli.REFUSED);
        assertThat(output()).contains("'latest' is not a sequence number");
    }

    private int run(String command, String... args) {
        printed.reset();
        return cli.execute(command, List.of(args));
    }

    private String output() {
        return printed.toString(StandardCharsets.UTF_8);
    }

    /** The CLI reads from the filesystem, so the fixture is resolved to a real path. */
    private static String fixture(String label) {
        String resource = "/gamedata/proving-ground-" + label + ".json";
        try {
            return Path.of(GameDataCliTest.class.getResource(resource).toURI()).toString();
        } catch (URISyntaxException | NullPointerException e) {
            throw new IllegalStateException("fixture not on the classpath: " + resource, e);
        }
    }
}
