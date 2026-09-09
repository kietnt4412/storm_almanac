package io.stormalmanac.app;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.gamedata.diff.VersionDiff;
import io.stormalmanac.gamedata.ingest.BundleFormatException;
import io.stormalmanac.gamedata.ingest.CanonicalBundleParser;
import io.stormalmanac.gamedata.ingest.CanonicalBundleWriter;
import io.stormalmanac.gamedata.ingest.GameDataBundle;
import io.stormalmanac.gamedata.ingest.GameDataIngestRepository;
import io.stormalmanac.gamedata.ingest.UpstreamAdapter;
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
 * java -jar storm-almanac.jar --gamedata=adapters
 * java -jar storm-almanac.jar --gamedata=adapt    reverse-1999 ./snapshot 0 1.0 out.json
 * java -jar storm-almanac.jar --gamedata=validate bundle.json
 * java -jar storm-almanac.jar --gamedata=preview  bundle.json
 * java -jar storm-almanac.jar --gamedata=ingest   bundle.json
 * java -jar storm-almanac.jar --gamedata=drafts   proving-ground
 * java -jar storm-almanac.jar --gamedata=publish  proving-ground 1 [second-hand]
 * java -jar storm-almanac.jar --gamedata=versions proving-ground
 * java -jar storm-almanac.jar --gamedata=diff     proving-ground 0 1
 * </pre>
 *
 * <p>{@code adapt} is the step before all of them and only exists for titles
 * whose data somebody else publishes: it converts an upstream snapshot into a
 * canonical bundle <em>file</em>, which then goes through the same three
 * commands a hand-written bundle does. Writing the file out rather than
 * ingesting straight from the adapter is the point — it is what lets a person
 * read the thing, diff it, and keep the exact bytes that were approved.
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
                case "adapters" -> adapters();
                case "adapt" -> adapt(args);
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
            out.println("usage: --gamedata=<adapters|adapt|validate|preview|ingest"
                    + "|publish|drafts|versions|diff> [args]");
            return MISUSE;
        } catch (BundleFormatException | GameDataIngestRepository.PublishedVersionIsImmutableException
                | GameDataIngestRepository.NoDraftToPublishException
                | GameDataIngestRepository.SecondHandDataException refusal) {
            out.println("refused: " + refusal.getMessage());
            return REFUSED;
        }
    }

    // ── Commands ────────────────────────────────────────────────────────────

    /** Which titles this build can onboard, which is a shorter list than it looks. */
    private int adapters() {
        UpstreamAdapters.all().forEach(adapter ->
                out.printf("  %-16s %s%n", adapter.game().value(), adapter.expects()));
        return OK;
    }

    /**
     * An upstream snapshot, as a canonical bundle file.
     *
     * <p>Writes nothing to the database on purpose. The output is a file that
     * {@code preview} can be pointed at, and the notes the adapter prints on the
     * way through are the part worth reading: they say what it refused to
     * convert. A snapshot that yields half a catalogue should be caught here,
     * not after publishing.
     */
    private int adapt(List<String> args) {
        if (args.size() != 4 && args.size() != 5) {
            throw new Misuse("adapt <game> <upstream-dir> <sequence> <label> [out.json]");
        }
        GameId game = new GameId(args.get(0));
        UpstreamAdapter adapter = UpstreamAdapters.forGame(game, note -> out.println("  " + note))
                .orElseThrow(() -> new Misuse("no adapter for '" + game.value()
                        + "'; --gamedata=adapters lists the ones this build has"));

        GameDataBundle bundle = adapter.adapt(Path.of(args.get(1)), number(args.get(2)), args.get(3));
        String canonical = new CanonicalBundleWriter().write(bundle);

        if (args.size() == 4) {
            out.print(canonical);
            return OK;
        }
        Path target = Path.of(args.get(4));
        try {
            Files.writeString(target, canonical);
        } catch (IOException e) {
            throw new BundleFormatException("cannot write " + target.toAbsolutePath() + ": " + e.getMessage(), e);
        }
        out.printf("wrote %s %s to %s. Nothing is in the database yet:%n",
                bundle.game().id().value(), bundle.label(), target);
        out.printf("  --gamedata=preview %s%n", target);
        return OK;
    }

    private int validate(String file) {
        GameDataBundle bundle = read(file);
        out.printf("%s %s is well-formed: %d items, %d entities, %d sources, %d sinks, %d banners%n",
                bundle.game().id().value(), bundle.label(),
                bundle.items().size(), bundle.entities().size(),
                bundle.sources().size(), bundle.sinks().size(), bundle.banners().size());
        out.println("attribution: " + bundle.attribution());
        provenance(bundle);
        return OK;
    }

    /** What this file would change, computed without writing a row. */
    private int preview(String file) {
        GameDataBundle bundle = read(file);
        report(bundle);
        provenance(bundle);
        return OK;
    }

    private int ingest(String file) {
        GameDataBundle bundle = read(file);
        // The report first: if the ingest is refused, the operator still learns
        // what the file was proposing, which is usually the thing they need.
        report(bundle);
        provenance(bundle);
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

    /**
     * Where each of this bundle's facts came from.
     *
     * <p>Printed on every command that puts a bundle in front of a person,
     * because publishing is a human approval and an approval that could not see
     * this is exactly the rubber stamp ADR 0015 is about. The counts are the
     * point: "1 second-hand fact" and "2 700 second-hand facts" are different
     * decisions and a yes/no line would render them identically.
     */
    private void provenance(GameDataBundle bundle) {
        out.println("provenance:");
        bundle.factsByProvenance().forEach((provenance, facts) ->
                out.printf("  %-24s %5d fact(s)  %-20s %s  %s%n",
                        provenance.id(), facts, provenance.origin(),
                        provenance.observedOn(),
                        provenance.isFirstHand() ? "" : "<- NOT ours to publish"));

        List<String> secondHand = bundle.secondHandFacts();
        if (secondHand.isEmpty()) {
            return;
        }
        out.printf("  %d of %d facts are somebody else's. This bundle will not publish without"
                        + " the 'second-hand' argument, and ADR 0015 says it should not be shipped"
                        + " with one either.%n",
                secondHand.size(), bundle.factRefs().size());
    }

    /**
     * The approval.
     *
     * <p>The optional third argument is the deliberate ugliness. ADR 0015 allows
     * second-hand data to reach a published version — that is what keeps the
     * Kornblume adapter usable as a cross-check to diff against — and forbids
     * shipping it. A flag nobody has to type would collapse those two into one;
     * a word somebody has to type, on a command line that ends up in a shell
     * history, does not.
     */
    private int publish(List<String> args) {
        if (args.size() != 2 && args.size() != 3) {
            throw new Misuse("publish <game> <sequence> [second-hand]");
        }
        boolean secondHand = args.size() == 3;
        if (secondHand && !args.get(2).equals("second-hand")) {
            throw new Misuse("publish <game> <sequence> [second-hand]");
        }
        GameId game = new GameId(args.get(0));
        GameDataVersion published = ingest.publish(game, number(args.get(1)), secondHand);

        if (secondHand) {
            out.println("WARNING: published data this project did not source."
                    + " ADR 0015 says it must not be shipped.");
        }
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

        Optional<GameDefinition> from = definitions.find(game, number(args.get(1)));
        Optional<GameDefinition> to = definitions.find(game, number(args.get(2)));
        if (from.isEmpty() || to.isEmpty()) {
            out.println("refused: both sequences must name a published version of " + game.value());
            return REFUSED;
        }
        out.print(VersionDiff.between(from.get(), to.get()).render());
        return OK;
    }

    // ── Plumbing ────────────────────────────────────────────────────────────

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
