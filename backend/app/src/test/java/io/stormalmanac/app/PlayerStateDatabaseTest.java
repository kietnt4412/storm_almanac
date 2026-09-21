package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gamedata.Goal;
import io.stormalmanac.identity.Account;
import io.stormalmanac.identity.AccountRepository;
import io.stormalmanac.player.Goals;
import io.stormalmanac.player.Inventory;
import io.stormalmanac.player.PlayerProfile;
import io.stormalmanac.player.PlayerStateRepository;
import io.stormalmanac.player.Roster;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Phase 3's foundation, against a real Postgres.
 *
 * <p>The schema is the thing under test here, not the mapping. Every assertion
 * below that ends in an exception is checking a constraint that exists because
 * the alternative is a silent wrong answer somewhere downstream: an inventory
 * that means two different things, a goal counted twice, one person's account
 * reachable by claiming another's email.
 */
class PlayerStateDatabaseTest extends SharedDatabaseTest {

    private static final GameId GAME = GameId.of("reverse-1999");

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private PlayerStateRepository players;

    @Test
    @DisplayName("a first sign-in creates an account, and the second one finds it again")
    void signInIsIdempotent() {
        Account first = accounts.upsertFromOidc("google", "sub-1", "Vertin", "vertin@example.com");
        Account second = accounts.upsertFromOidc("google", "sub-1", "Vertin", "vertin@example.com");

        assertThat(second.id()).isEqualTo(first.id());
        assertThat(second.createdAt()).isEqualTo(first.createdAt());
        assertThat(accounts.find(first.id())).contains(second);
    }

    @Test
    @DisplayName("the provider's display name and email are refreshed on every sign-in")
    void providerOwnsTheProfileFields() {
        Account first = accounts.upsertFromOidc("google", "sub-1", "Vertin", "old@example.com");
        Account renamed = accounts.upsertFromOidc("google", "sub-1", "Timekeeper", "new@example.com");

        assertThat(renamed.id()).isEqualTo(first.id());
        assertThat(renamed.displayName()).isEqualTo("Timekeeper");
        assertThat(renamed.email()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("two providers claiming the same email are two accounts, not one")
    void identityIsProviderAndSubjectNeverEmail() {
        Account google = accounts.upsertFromOidc("google", "sub-1", "Vertin", "same@example.com");
        Account discord = accounts.upsertFromOidc("discord", "sub-1", "Vertin", "same@example.com");

        // Same subject string, same email, different provider: still two people
        // as far as this service is concerned. Merging them is a link flow
        // performed by an authenticated user and does not exist yet.
        assertThat(discord.id()).isNotEqualTo(google.id());
    }

    @Test
    @DisplayName("an inventory round-trips exactly, and an absent item reads as zero")
    void inventoryRoundTrips() {
        ProfileId profile = profileFor(account(), "global");

        Inventory saved = Inventory.empty(profile)
                .with(ItemId.of("sharpened-tool"), 42)
                .with(ItemId.of("crystal-casket"), 7);
        players.saveInventory(saved);

        Inventory read = players.inventoryOf(profile);
        assertThat(read.quantities()).isEqualTo(saved.quantities());
        assertThat(read.quantityOf(ItemId.of("sharpened-tool"))).isEqualTo(42);
        assertThat(read.quantityOf(ItemId.of("never-owned"))).isZero();
    }

    @Test
    @DisplayName("saving an inventory replaces it, so an item dropped from the map is gone")
    void savingAnInventoryReplacesIt() {
        ProfileId profile = profileFor(account(), "global");

        players.saveInventory(Inventory.empty(profile)
                .with(ItemId.of("sharpened-tool"), 42)
                .with(ItemId.of("crystal-casket"), 7));
        players.saveInventory(Inventory.empty(profile).with(ItemId.of("sharpened-tool"), 1));

        assertThat(players.inventoryOf(profile).quantities())
                .containsExactly(Map.entry(ItemId.of("sharpened-tool"), 1));
    }

    @Test
    @DisplayName("a zero quantity is stored as no row at all, because absent already means zero")
    void zeroIsNotARow() {
        ProfileId profile = profileFor(account(), "global");

        players.saveInventory(Inventory.empty(profile).with(ItemId.of("sharpened-tool"), 42));
        // Inventory.with(item, 0) removes the key rather than storing a zero.
        players.saveInventory(players.inventoryOf(profile).with(ItemId.of("sharpened-tool"), 0));

        assertThat(players.inventoryOf(profile).quantities()).isEmpty();
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM player.inventory_item WHERE profile_id = ?",
                        Integer.class,
                        profile.value()))
                .isZero();
    }

    @Test
    @DisplayName("the database refuses a zero-quantity row even when Java is bypassed")
    void theDatabaseRefusesTheOtherRepresentation() {
        ProfileId profile = profileFor(account(), "global");

        assertThatThrownBy(() -> jdbc.update(
                        """
                        INSERT INTO player.inventory_item (profile_id, item_slug, quantity)
                        VALUES (?, 'sharpened-tool', 0)
                        """,
                        profile.value()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("a roster round-trips, states and all")
    void rosterRoundTrips() {
        ProfileId profile = profileFor(account(), "global");

        Roster saved = new Roster(
                profile,
                Map.of(
                        EntityId.of("vertin"), Set.of("insight-1", "level-40"),
                        EntityId.of("regulus"), Set.of("insight-2")));
        players.saveRoster(saved);

        assertThat(players.rosterOf(profile).currentStates()).isEqualTo(saved.currentStates());
        assertThat(players.rosterOf(profile).owns(EntityId.of("vertin"))).isTrue();
        assertThat(players.rosterOf(profile).owns(EntityId.of("sotheby"))).isFalse();
    }

    @Test
    @DisplayName("goals keep the order they were saved in, because the order is the player's priority")
    void goalsKeepTheirOrder() {
        ProfileId profile = profileFor(account(), "global");

        List<Goal> ordered = List.of(
                new Goal(EntityId.of("regulus"), "insight-3", Goal.Satisfiability.DETERMINISTIC, 0),
                new Goal(EntityId.of("vertin"), "insight-2", Goal.Satisfiability.DETERMINISTIC, 0),
                new Goal(EntityId.of("sotheby"), "resonance-9", Goal.Satisfiability.PROBABILISTIC, 5));
        players.saveGoals(new Goals(profile, ordered));

        // Not sorted by entity, not sorted by priority: the first two share a
        // priority and would tie under any other ordering.
        assertThat(players.goalsOf(profile).goals()).containsExactlyElementsOf(ordered);
    }

    @Test
    @DisplayName("the same goal cannot be stored twice for one profile")
    void aGoalIsNotStrongerForBeingRepeated() {
        ProfileId profile = profileFor(account(), "global");

        Goal goal = Goal.deterministic(EntityId.of("vertin"), "insight-2");
        assertThatThrownBy(() -> players.saveGoals(new Goals(profile, List.of(goal, goal))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("one account's profiles are listed and another's are not")
    void profilesBelongToOneAccount() {
        AccountId mine = account();
        AccountId theirs = accounts
                .upsertFromOidc("discord", "sub-2", "Someone Else", "else@example.com")
                .id();

        ProfileId global = profileFor(mine, "global");
        ProfileId cn = profileFor(mine, "cn");
        profileFor(theirs, "global");

        assertThat(players.profilesOf(mine)).extracting(PlayerProfile::id).containsExactlyInAnyOrder(global, cn);
        assertThat(players.profilesOf(theirs)).hasSize(1);
    }

    @Test
    @DisplayName("one account cannot hold two profiles on the same game and region")
    void oneProfilePerGamePerRegion() {
        AccountId owner = account();
        profileFor(owner, "global");

        assertThatThrownBy(() -> players.saveProfile(
                        new PlayerProfile(ProfileId.of("second"), owner, GAME, "Alt", "global")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("saving a profile again renames it and does not move it to another account")
    void savingAProfileIsARename() {
        AccountId owner = account();
        ProfileId profile = profileFor(owner, "global");
        AccountId other = accounts
                .upsertFromOidc("discord", "sub-3", "Other", "other@example.com")
                .id();

        players.saveProfile(new PlayerProfile(profile, other, GAME, "Renamed", "global"));

        PlayerProfile read = players.findProfile(profile).orElseThrow();
        assertThat(read.displayName()).isEqualTo("Renamed");
        assertThat(read.owner()).isEqualTo(owner);
    }

    @Test
    @DisplayName("deleting a profile takes its inventory, roster and goals with it")
    void deletingAProfileCascades() {
        ProfileId profile = profileFor(account(), "global");
        players.saveInventory(Inventory.empty(profile).with(ItemId.of("sharpened-tool"), 42));
        players.saveRoster(new Roster(profile, Map.of(EntityId.of("vertin"), Set.of("insight-1"))));
        players.saveGoals(new Goals(profile, List.of(Goal.deterministic(EntityId.of("vertin"), "insight-2"))));

        jdbc.update("DELETE FROM player.profile WHERE id = ?", profile.value());

        assertThat(players.findProfile(profile)).isEmpty();
        assertThat(players.inventoryOf(profile).quantities()).isEmpty();
        assertThat(players.rosterOf(profile).currentStates()).isEmpty();
        assertThat(players.goalsOf(profile).goals()).isEmpty();
    }

    private AccountId account() {
        return accounts.upsertFromOidc("google", "sub-1", "Vertin", "vertin@example.com").id();
    }

    private ProfileId profileFor(AccountId owner, String region) {
        ProfileId id = ProfileId.of(owner.value() + "-" + region);
        players.saveProfile(new PlayerProfile(id, owner, GAME, "Main", region));
        return id;
    }
}
