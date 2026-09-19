package io.stormalmanac.app;

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
import java.util.List;
import java.util.Optional;

/**
 * The two repositories the solver reads, answered from memory: one published
 * version of one game, and one player whose state does not move. With these a
 * test can run the real optimizer on a bundle read from disk, without a
 * database.
 */
final class InMemoryPlanning {

    private InMemoryPlanning() {}

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

        @Override
        public List<PublishedGame> publishedGames() {
            return List.of(new PublishedGame(definition.game(), definition.version()));
        }
    }

    record FixedPlayer(ProfileId id, GameId game, Inventory inventory, Roster roster)
            implements PlayerStateRepository {

        @Override
        public List<PlayerProfile> profilesOf(AccountId account) {
            return List.of(profile());
        }

        @Override
        public Optional<PlayerProfile> findProfile(ProfileId profile) {
            return Optional.of(profile());
        }

        private PlayerProfile profile() {
            return new PlayerProfile(id, new AccountId("acceptance"), game, "Tester", "global");
        }

        @Override
        public Inventory inventoryOf(ProfileId profile) {
            return inventory;
        }

        @Override
        public Roster rosterOf(ProfileId profile) {
            return roster;
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
