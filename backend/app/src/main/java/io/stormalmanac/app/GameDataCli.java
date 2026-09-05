package io.stormalmanac.app;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.gamedata.diff.VersionDiff;
import io.stormalmanac.gamedata.ingest.BundleFormatException;
import io.stormalmanac.gamedata.ingest.CanonicalBundleParser;
import io.stormalmanac.gamedata.ingest.GameDataBundle;
import io.stormalmanac.gamedata.ingest.GameDataIngestRepository;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * {@code gamedata-cli}: onboarding a title without touching backend code.
 *
 * <p>Run with {@code --gamedata=<command>} against the same jar the server runs
 * from. {@link StormAlmanacApplication#main} sees that option and starts without
 * a web server, so this can be run on a host that is already serving.
 *
 * <pre>
 * java -jar storm-almanac.jar --gamedata=validate bundle.json
 * java -jar storm-almanac.jar --gamedata=preview  bundle.json
 * java -jar storm-almanac.jar --gamedata=ingest   bundle.json
 * java -jar storm-almanac.jar --gamedata=drafts   proving-ground
 * java -jar storm-almanac.jar --gamedata=publish  proving-ground 1
 * java -jar storm-almanac.jar --gamedata=versions proving-ground
 * java -jar storm-almanac.jar --gamedata=diff     proving-ground 0 1
 * </pre>
 *
 * <p>The commands are shaped around <em>preview, ingest, publish</em>, because
 * publishing is a human approval and an approval nobody could have reviewed is
 * a rubber stamp. {@code preview} answers "what would this file change?"
 * without writing a row, which is the question somebody actually has before
 * they run {@code ingest}; {@code ingest} prints the same report again over
 * what it just wrote.
 *
 * <p>Exit codes: {@code 0} success, {@code 1} the command was refused — a bad
 * bundle, an immutable version, a draft that is not there — and {@code 2} the
 * command line itself was wrong. A refusal is an expected outcome of a wrong
 * file, so it prints one sentence rather than a stack trace.
 */
@Component
public class GameDataCli implements ApplicationRunner {

    /** Presence of this option is what turns the jar into the CLI. */
    static final String OPTION = "gamedata";

    static final int OK = 0;
    static final int REFUSED = 1;
    static final int MISUSE = 2;

    private final GameDataIngestRepository ingest;
    private final GameDefinitionRepository definitions;
    private final ApplicationContext context;
    private final PrintStream out;

    @Autowired
    public GameDataCli(
            GameDataIngestRepository ingest,
            GameDefinitionRepository definitions,
            ApplicationContext context) {
        this(ingest, definitions, context, System.out);
    }

    GameDataCli(
            GameDataIngestRepository ingest,
            GameDefinitionRepository definitions,
            ApplicationContext context,
            PrintStream out) {
        this.ingest = ingest;
        this.definitions = definitions;
        this.context = context;
        this.out = out;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!args.containsOption(OPTION)) {
            return;
        }
        List<String> commands = args.getOptionValues(OPTION);
        int code = execute(commands.isEmpty() ? "" : commands.getFirst(), args.getNonOptionArgs());

        // The context came up to serve this one command; leaving it running would
        // hold a connection pool open for nothing.
        System.exit(SpringApplication.exit(context, () -> code));
    }

    /**
     * Runs one command and returns its exit code, without exiting.
     *
     * <p>Separated from {@link #run} so it can be tested. A test that called
     * {@code run} would take the JVM down with it.
     */
    int execute(String command, List<String> args) {
        try {
            return switch (command) {
                case "validate" -> validate(one(args, "validate <bundle.json>"));
                case "preview" -> preview(one(args, "preview <bundle.json>"));
                case "ingest" -> ingest(one(args, "ingest <bundle.json>"));
                case "publish" -> publish(args);
                case "drafts" -> drafts(new GameId(one(args, "drafts <game>")));
                case "versions" -> versions(new GameId(one(args, "versions <game>")));
                case "diff" -> diff(args);
                default -> throw new Misuse("unknown command '" + command + "'");
            };
        } catch (Misuse misuse) {
            out.println(misuse.getMessage());
            out.println("usage: --gamedata=<validate|preview|ingest|publish|drafts|versions|diff> [args]");
            return MISUSE;
        } catch (BundleFormatException | GameDataIngestRepository.PublishedVersionIsImmutableException
                | GameDataIngestRepository.NoDraftToPublishException refusal) {
            out.println("refused: " + refusal.getMessage());
            return REFUSED;
        }
    }

    // ── Commands ────────────────────────────────────────────────────────────

    private int validate(String file) {
        GameDataBundle bundle = read(file);
        out.printf("%s %s is well-formed: %d items, %d entities, %d sources, %d sinks, %d banners%n",
                bundle.game().id().value(), bundle.label(),
                bundle.items().size(), bundle.entities().size(),
                bundle.sources().size(), bundle.sinks().size(), bundle.banners().size());
        out.println("attribution: " + bundle.attribution());
        return OK;
    }

    /** What this file would change, computed without writing a row. */
    private int preview(String file) {
        report(read(file));
        return OK;
    }

    private int ingest(String file) {
        GameDataBundle bundle = read(file);
        // The report first: if the ingest is refused, the operator still learns
        // what the file was proposing, which is usually the thing they need.
        report(bundle);
        ingest.ingestDraft(bundle);

        out.printf("%ningested %s %s as a draft at sequence %d. Nothing is published yet:%n",
                bundle.game().id().value(), bundle.label(), bundle.sequence());
        out.printf("  --gamedata=publish %s %d%n", bundle.game().id().value(), bundle.sequence());
        return OK;
    }

    /** The bundle against the currently published version — what approval would move. */
    private void report(GameDataBundle bundle) {
        Optional<GameDefinition> current = definitions.findLatest(bundle.game().id());
        if (current.isEmpty()) {
            out.printf("no published version of %s yet; this bundle would be the first,"
                            + " with %d items and %d entities%n",
                    bundle.game().id().value(), bundle.items().size(), bundle.entities().size());
            return;
        }
        out.print(VersionDiff.between(current.get(), bundle.definitionApprovedAt(Instant.EPOCH)).render());
    }

    private int publish(List<String> args) {
        if (args.size() != 2) throw new Misuse("publish <game> <sequence>");
        GameId game = new GameId(args.get(0));
        GameDataVersion published = ingest.publish(game, number(args.get(1)));

        out.printf("published %s %s at %s%n", game.value(), published.label(), published.publishedAt());
        return OK;
    }

    private int drafts(GameId game) {
        List<GameDataIngestRepository.DraftVersion> drafts = ingest.drafts(game);
        if (drafts.isEmpty()) {
            out.println("no drafts awaiting approval for " + game.value());
            return OK;
        }
        drafts.forEach(draft -> out.printf("  %d  %-8s  ingested %s  (%s)%n",
                draft.sequence(), draft.label(), draft.ingestedAt(), draft.attribution()));
        return OK;
    }

    private int versions(GameId game) {
        List<GameDataVersion> versions = definitions.versions(game);
        if (versions.isEmpty()) {
            out.println("nothing published for " + game.value());
            return OK;
        }
        versions.forEach(version -> out.printf("  %d  %-8s  published %s%n",
                version.sequence(), version.label(), version.publishedAt()));
        return OK;
    }

    private int diff(List<String> args) {
        if (args.size() != 3) throw new Misuse("diff <game> <fromSequence> <toSequence>");
        GameId game = new GameId(args.get(0));

        Optional<GameDefinition> from = load(game, number(args.get(1)));
        Optional<GameDefinition> to = load(game, number(args.get(2)));
        if (from.isEmpty() || to.isEmpty()) {
            out.println("refused: both sequences must name a published version of " + game.value());
            return REFUSED;
        }
        out.print(VersionDiff.between(from.get(), to.get()).render());
        return OK;
    }

    // ── Plumbing ────────────────────────────────────────────────────────────

    private Optional<GameDefinition> load(GameId game, long sequence) {
        // find() keys on the sequence; the label and approval time on this
        // record are not knowable here and are not read.
        return definitions.find(game, new GameDataVersion(game, sequence, "", Instant.EPOCH));
    }

    private GameDataBundle read(String file) {
        Path path = Path.of(file);
        if (!Files.isReadable(path)) {
            throw new BundleFormatException("cannot read " + path.toAbsolutePath());
        }
        try (InputStream in = Files.newInputStream(path)) {
            return new CanonicalBundleParser().parse(in);
        } catch (IOException e) {
            throw new BundleFormatException("cannot read " + path.toAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    private static String one(List<String> args, String form) {
        if (args.size() != 1) throw new Misuse(form);
        return args.getFirst();
    }

    private static long number(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new BundleFormatException("'" + value + "' is not a sequence number", e);
        }
    }

    /** The command line was wrong, as opposed to the data being wrong. */
    private static final class Misuse extends RuntimeException {
        Misuse(String message) {
            super(message);
        }
    }
}
