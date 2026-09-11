package io.stormalmanac.planner;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.gamedata.GameDefinitionRepository.PublishedGame;
import io.stormalmanac.player.Goals;
import io.stormalmanac.player.Inventory;
import io.stormalmanac.player.InventoryEdit;
import io.stormalmanac.player.MergeOutcome;
import io.stormalmanac.player.PlayerProfile;
import io.stormalmanac.player.PlayerStateRepository;
import io.stormalmanac.player.Roster;
import io.stormalmanac.player.RosterEdit;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory repositories, so the port can be tested without a database.
 *
 * <p>Shared between the tests in this package rather than copied into each,
 * because two fake {@link PlayerStateRepository}s that drift apart is how a test
 * ends up asserting against a fixture nobody else uses.
 */
final class TestRepositories {

    private TestRepositories() {}

    record Definitions(GameDefinition definition) implements GameDefinitionRepository {

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

        @Override
        public List<PublishedGame> publishedGames() {
            return List.of(new PublishedGame(definition.game(), definition.version()));
        }
    }

    /**
     * Any number of profiles, each with its own state.
     *
     * <p>More than one is not decoration: {@link SolveKey} fingerprints a
     * player's state and not their identity, so "two profiles in the same
     * position ask the same question" is a case that only exists if a fixture
     * can hold two profiles.
     */
    static final class Players implements PlayerStateRepository {

        private final GameId game;
        private final Map<ProfileId, Inventory> inventories = new LinkedHashMap<>();
        private final Map<ProfileId, Roster> rosters = new LinkedHashMap<>();

        Players(GameId game) {
            this.game = game;
        }

        static Players of(GameId game, ProfileId profile, Inventory inventory, Roster roster) {
            return new Players(game).with(profile, inventory, roster);
        }

        Players with(ProfileId profile, Inventory inventory, Roster roster) {
            inventories.put(profile, inventory);
            rosters.put(profile, roster);
            return this;
        }

        @Override
        public List<PlayerProfile> profilesOf(AccountId account) {
            return inventories.keySet().stream().map(this::profileRecord).toList();
        }

        @Override
        public Optional<PlayerProfile> findProfile(ProfileId id) {
            return inventories.containsKey(id) ? Optional.of(profileRecord(id)) : Optional.empty();
        }

        private PlayerProfile profileRecord(ProfileId id) {
            return new PlayerProfile(id, new AccountId("a1"), game, "Tester", "global");
        }

        @Override
        public Inventory inventoryOf(ProfileId profile) {
            return inventories.get(profile);
        }

        @Override
        public Roster rosterOf(ProfileId profile) {
            return rosters.get(profile);
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

        // Sync has nothing to do with solving. A fake that answered these would
        // be a second implementation of the merge rules, drifting from the real
        // one until a test passed against a merge nothing ships.
        @Override
        public MergeOutcome<ItemId> mergeInventory(ProfileId profile, Collection<InventoryEdit> edits) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MergeOutcome<EntityId> mergeRoster(ProfileId profile, Collection<RosterEdit> edits) {
            throw new UnsupportedOperationException();
        }
    }
}
