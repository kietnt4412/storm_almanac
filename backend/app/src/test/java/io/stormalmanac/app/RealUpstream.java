package io.stormalmanac.app;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.gamedata.ingest.CanonicalBundleParser;
import io.stormalmanac.gamedata.ingest.CanonicalBundleWriter;
import io.stormalmanac.gamedata.ingest.GameDataBundle;
import io.stormalmanac.gamedata.ingest.UpstreamAdapter;
import io.stormalmanac.planner.MipOptimizer;
import io.stormalmanac.planner.SolveCache;
import io.stormalmanac.player.Goals;
import io.stormalmanac.player.Inventory;
import io.stormalmanac.player.PlayerProfile;
import io.stormalmanac.player.PlayerStateRepository;
import io.stormalmanac.player.Roster;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A real Reverse: 1999 patch, converted and wired to a solver, for the tests
 * that need one.
 *
 * <p>Shared by {@link RealUpstreamPlanTest} and {@link CommunityBenchmarkTest}
 * because the alternative is two copies of a fake repository pair, and a fake
 * that drifts between two tests is a fake that makes them disagree for reasons
 * neither is about.
 *
 * <p><b>Nothing here is committed data.</b> The snapshot is fetched on demand by
 * {@code tools/fetch-upstream.sh} into an ignored directory — see
 * {@code docs/adr/0009} — so everything using this class must guard itself with
 * {@link #snapshotIsPresent()} and skip when it is not there.
 */
final class RealUpstream {

    static final GameId REVERSE_1999 = new GameId("reverse-1999");
    static final String PATCH = "3.5";
    static final Instant NOW = Instant.parse("2026-09-07T12:00:00Z");
    static final ProfileId PROFILE = new ProfileId("real-plan");

    private RealUpstream() {}

    /** The patch, adapted and round-tripped through the canonical format a human would approve. */
    static GameDefinition definition() {
        UpstreamAdapter adapter = UpstreamAdapters.forGame(REVERSE_1999, note -> { }).orElseThrow();
        GameDataBundle converted = adapter.adapt(snapshot().resolve(PATCH), 0, PATCH);
        return new CanonicalBundleParser()
                .parse(new CanonicalBundleWriter().write(converted))
                .definitionApprovedAt(Instant.EPOCH);
    }

    /**
     * <b>Cache-free, and it has to stay that way.</b> The p95 test asks the same
     * question fifty-five times, which is the right shape for a timing
     * measurement and exactly the wrong shape for a cache: hand this method a
     * real {@link SolveCache} and fifty-four of those solves become lookups, the
     * p95 drops to nothing, and phase 2's exit criterion silently starts
     * measuring a hash map. Anything wanting a cached optimizer builds its own —
     * see {@link #cachingOptimizer}.
     */
    static MipOptimizer optimizer(GameDefinition definition, Inventory inventory, Duration budget) {
        return new MipOptimizer(
                new OneVersion(definition),
                new FixedPlayer(REVERSE_1999, inventory),
                null,
                Clock.fixed(NOW, ZoneOffset.UTC),
                budget,
                SolveCache.none());
    }

    /** The same optimizer with a cache behind it, for the tests that want one. */
    static MipOptimizer cachingOptimizer(
            GameDefinition definition, Inventory inventory, Duration budget, SolveCache cache) {
        return new MipOptimizer(
                new OneVersion(definition),
                new FixedPlayer(REVERSE_1999, inventory),
                null,
                Clock.fixed(NOW, ZoneOffset.UTC),
                budget,
                cache);
    }

    /** The directory the fetch script writes into, or wherever {@code -D} says. */
    static Path snapshot() {
        String configured = System.getProperty("storm-almanac.upstream");
        return configured != null ? Path.of(configured) : Path.of("..", "build", "upstream-snapshots");
    }

    static boolean snapshotIsPresent() {
        return Files.isReadable(snapshot().resolve(PATCH).resolve("items.json"));
    }

    record OneVersion(GameDefinition definition) implements GameDefinitionRepository {
        @Override
        public Optional<GameDefinition> findLatest(GameId game) {
            return find(game, definition.version().sequence());
        }

        @Override
        public Optional<GameDefinition> find(GameId game, long sequence) {
            return game.equals(definition.game().id()) && sequence == definition.version().sequence()
                    ? Optional.of(definition)
                    : Optional.empty();
        }

        @Override
        public List<GameDataVersion> versions(GameId game) {
            return List.of(definition.version());
        }
    }

    record FixedPlayer(GameId game, Inventory inventory) implements PlayerStateRepository {
        @Override
        public List<PlayerProfile> profilesOf(AccountId account) {
            return List.of(profile());
        }

        @Override
        public Optional<PlayerProfile> findProfile(ProfileId id) {
            return Optional.of(profile());
        }

        private PlayerProfile profile() {
            return new PlayerProfile(PROFILE, new AccountId("real-plan"), game, "Tester", "global");
        }

        @Override
        public Inventory inventoryOf(ProfileId profile) {
            return inventory;
        }

        @Override
        public Roster rosterOf(ProfileId profile) {
            return new Roster(profile, Map.of());
        }

        @Override
        public Goals goalsOf(ProfileId profile) {
            return new Goals(profile, List.of());
        }

        @Override
        public void saveProfile(PlayerProfile value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void saveInventory(Inventory value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void saveRoster(Roster value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void saveGoals(Goals value) {
            throw new UnsupportedOperationException();
        }
    }
}
