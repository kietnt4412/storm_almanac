package io.stormalmanac.gacha;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.BannerId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gacha.IncomeModel.PullBudget;
import io.stormalmanac.gamedata.Availability;
import io.stormalmanac.gamedata.Game;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.Item;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Rarity;
import io.stormalmanac.gamedata.Reward;
import io.stormalmanac.gamedata.Source;
import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.gamedata.banner.FeaturedRule;
import io.stormalmanac.gamedata.banner.PityRule;
import io.stormalmanac.gamedata.banner.PityScope;
import io.stormalmanac.gamedata.banner.PullPrice;
import io.stormalmanac.player.Inventory;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * "She arrives in 40 days — can I afford to guarantee her?", the half of the
 * question the engines have never been able to answer.
 *
 * <p>Every number here comes out of the bundle: the price off the banner, the
 * balance off the inventory, the accrual off the game's own declared grants at
 * their own cadence. There is deliberately no rate-per-day constant anywhere —
 * that number appears on no screen, and a module that carried one would be
 * carrying a game-specific figure it could not defend.
 */
class DeclaredIncomeModelTest {

    private static final ProfileId PROFILE = new ProfileId("income");
    private static final ItemId TICKET = new ItemId("ticket");

    private final IncomeModel income = new DeclaredIncomeModel();

    @Test
    @DisplayName("a balance alone buys whole pulls, and the remainder buys nothing")
    void spendsTheBalance() {
        // 1 100 at 250 apiece is four pulls and 100 left over. Rounding the
        // remainder up would promise a fifth pull the account cannot pay for.
        PullBudget budget = income.affordableWithin(
                game(), banner(250), holding(1_100), 0, Map.of());

        assertThat(budget.held()).isEqualTo(1_100);
        assertThat(budget.accruing()).isZero();
        assertThat(budget.pulls()).isEqualTo(4);
        assertThat(budget.currency()).isEqualTo("ticket");
    }

    @Test
    @DisplayName("declared grants accrue at their own cadence, and a partial week pays nothing")
    void accruesWhatTheGameGrants() {
        // 40 days is 40 dailies and 5 whole weeks: 40x20 + 5x300 = 2 300. The
        // sixth week is five days away and Cadence rounds against the player,
        // so it is not counted.
        GameDefinition game = game(
                daily("daily-sign-in", 20),
                weekly("weekly-clear", 300));

        PullBudget budget = income.affordableWithin(game, banner(250), holding(0), 40, Map.of());

        assertThat(budget.accruing()).isEqualTo(2_300);
        assertThat(budget.pulls()).isEqualTo(9);
    }

    @Test
    @DisplayName("a grant behind a bar the reader has not answered for is dropped and named")
    void dropsWhatTheReaderHasNotSaidTheyReach() {
        GameDefinition game = game(
                daily("daily-sign-in", 20),
                scored("cage-tier-9", 1_000, "cage-score", 1_100_000));

        PullBudget silent = income.affordableWithin(game, banner(250), holding(0), 7, Map.of());
        PullBudget answered = income.affordableWithin(
                game, banner(250), holding(0), 7, Map.of("cage-score", 1_100_000));

        // Silence is dearer than the truth, never cheaper — ADR 0022 — and the
        // budget says which question would move it rather than leaving the
        // reader to wonder why it is short.
        assertThat(silent.accruing()).isEqualTo(140);
        assertThat(silent.uncounted()).containsExactly("cage-tier-9 (needs cage-score >= 1100000)");

        assertThat(answered.accruing()).isEqualTo(1_140);
        assertThat(answered.uncounted()).isEmpty();
    }

    @Test
    @DisplayName("a grant of something else is not income, however generous")
    void ignoresGrantsOfOtherItems() {
        GameDefinition game = game(daily("daily-cogs", new ItemStack(new ItemId("cogs"), 6_000)));

        assertThat(income.affordableWithin(game, banner(250), holding(0), 30, Map.of()).accruing())
                .isZero();
    }

    @Test
    @DisplayName("a banner nobody has priced is refused by name, not answered with zero pulls")
    void refusesAnUnpricedBanner() {
        // "Cannot be afforded" and "nobody read what it costs" are different
        // answers, and a screen showing the first for the second would be wrong
        // in the one way a reader cannot detect.
        assertThatThrownBy(() -> income.affordableWithin(
                game(), banner(null), holding(50_000), 40, Map.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not say what a pull costs");
    }

    @Test
    @DisplayName("a horizon that counts backwards is refused rather than quietly clamped")
    void refusesANegativeHorizon() {
        assertThatThrownBy(() -> income.affordableWithin(
                game(daily("daily-sign-in", 20)), banner(250), holding(0), -1, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("counts backwards");
    }

    // ── Fixtures ────────────────────────────────────────────────────────────

    private static Inventory holding(int tickets) {
        return tickets == 0
                ? Inventory.empty(PROFILE)
                : Inventory.empty(PROFILE).with(TICKET, tickets);
    }

    private static BannerModel banner(Integer perPull) {
        return new BannerModel(
                BannerId.of("pool"),
                "A Pool",
                "event",
                Map.of(new Rarity("S", 3), 0.005),
                Map.of(new Rarity("S", 3), PityRule.hard(60)),
                List.of(),
                new FeaturedRule(0.7, 1),
                PityScope.BANNER_TYPE,
                Availability.ALWAYS,
                perPull == null ? null : new PullPrice(TICKET, perPull));
    }

    private static Reward daily(String id, int tickets) {
        return daily(id, new ItemStack(TICKET, tickets));
    }

    private static Reward daily(String id, ItemStack grant) {
        return new Reward(id, Reward.Cadence.DAILY, List.of(grant), Availability.ALWAYS);
    }

    private static Reward weekly(String id, int tickets) {
        return new Reward(
                id, Reward.Cadence.WEEKLY, List.of(new ItemStack(TICKET, tickets)), Availability.ALWAYS);
    }

    private static Reward scored(String id, int tickets, String measure, int atLeast) {
        return new Reward(
                id,
                Reward.Cadence.WEEKLY,
                List.of(new ItemStack(TICKET, tickets)),
                Availability.ALWAYS,
                new Reward.Requirement(measure, atLeast));
    }

    private static GameDefinition game(Source... sources) {
        GameId id = new GameId("t");
        return new GameDefinition(
                new Game(id, "Test", "Vigour"),
                new GameDataVersion(id, 1, "1", Instant.EPOCH, "hand-written"),
                List.of(
                        new Item(TICKET, "Ticket", new Rarity("5*", 5), "currency"),
                        new Item(new ItemId("cogs"), "Cogs", new Rarity("3*", 3), "currency")),
                List.of(sources),
                List.of(),
                List.of(),
                List.of());
    }
}
