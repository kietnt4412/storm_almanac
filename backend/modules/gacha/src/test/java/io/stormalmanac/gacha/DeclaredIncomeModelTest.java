package io.stormalmanac.gacha;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.BannerId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gacha.IncomeModel.PullBudget;
import io.stormalmanac.gamedata.Availability;
import io.stormalmanac.gamedata.Craft;
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
import java.util.Set;
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
    private static final ItemId BLACK = new ItemId("black");
    private static final ItemId RAINBOW = new ItemId("rainbow");
    private static final ItemId BOX = new ItemId("box");

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

@Test
    @DisplayName("an item that exchanges into the currency is counted as it, held and granted alike")
    void countsWhatConvertsIntoTheCurrency() {
        // The Black Card shape: missions pay a card that exchanges one for one
        // into the ticket a pull spends. 600 held and 30 a day for 10 days is
        // 900 cards, 900 tickets, three pulls at 250 with 150 over.
        GameDefinition game = game(
                exchange("exchange", BLACK, 1, TICKET, 1),
                daily("daily-missions", new ItemStack(BLACK, 30)));

        PullBudget budget = income.affordableWithin(
                game, banner(250), Inventory.empty(PROFILE).with(BLACK, 600), 10, Map.of());

        assertThat(budget.held()).isEqualTo(600);
        assertThat(budget.accruing()).isEqualTo(300);
        assertThat(budget.pulls()).isEqualTo(3);
        assertThat(budget.converted())
                .containsExactly(new IncomeModel.Converted("black", List.of("exchange"), 600, 300));
    }

    @Test
    @DisplayName("a chain of exchanges is walked to the end, in whole lots at every step")
    void walksAChainInWholeLots() {
        // Rainbow -> 10 Black -> 1 Black per ticket at 1:1, with a second leg
        // that wants lots of 3: 7 Rainbow is 70 Black is 23 lots of 3 is 23
        // tickets, and the one Black over buys nothing.
        GameDefinition game = game(
                exchange("rainbow-to-black", RAINBOW, 1, BLACK, 10),
                exchange("black-to-ticket", BLACK, 3, TICKET, 1));

        PullBudget budget = income.affordableWithin(
                game, banner(1), Inventory.empty(PROFILE).with(RAINBOW, 7), 0, Map.of());

        assertThat(budget.held()).isEqualTo(23);
        assertThat(budget.converted())
                .extracting(IncomeModel.Converted::item, IncomeModel.Converted::via)
                .contains(tuple("rainbow", List.of("rainbow-to-black", "black-to-ticket")));
    }

    @Test
    @DisplayName("a recipe, a closing exchange and a round trip are not income")
    void countsOnlyStandingOneForOneExchanges() {
        // A box that opens into the ticket and something else is a recipe; an
        // exchange that closes may close inside the horizon; and a pair that
        // trades two items back and forth must neither loop nor mint anything.
        GameDefinition game = game(
                new Craft("open-box", List.of(new ItemStack(BOX, 1)),
                        List.of(new ItemStack(TICKET, 5), new ItemStack(new ItemId("cogs"), 1)),
                        Availability.ALWAYS),
                new Craft("event-exchange", List.of(new ItemStack(RAINBOW, 1)),
                        List.of(new ItemStack(TICKET, 1)),
                        new Availability(Set.of(), null, Instant.parse("2030-01-01T00:00:00Z"))),
                exchange("black-to-rainbow", BLACK, 1, RAINBOW, 1),
                exchange("rainbow-to-black", RAINBOW, 1, BLACK, 1));

        PullBudget budget = income.affordableWithin(
                game,
                banner(1),
                Inventory.empty(PROFILE).with(BOX, 10).with(RAINBOW, 10).with(BLACK, 10),
                0,
                Map.of());

        assertThat(budget.held()).isZero();
        assertThat(budget.converted()).isEmpty();
    }

    @Test
    @DisplayName("a converted grant behind an unanswered bar is dropped and named like any other")
    void dropsAConvertedGrantBehindABar() {
        GameDefinition game = game(
                exchange("exchange", BLACK, 1, TICKET, 1),
                new Reward("weekly-missions", Reward.Cadence.WEEKLY, List.of(new ItemStack(BLACK, 1_000)),
                        Availability.ALWAYS, new Reward.Requirement("weekly-missions", 1)));

        PullBudget silent = income.affordableWithin(game, banner(250), holding(0), 14, Map.of());
        PullBudget answered = income.affordableWithin(
                game, banner(250), holding(0), 14, Map.of("weekly-missions", 1));

        assertThat(silent.accruing()).isZero();
        assertThat(silent.uncounted()).containsExactly("weekly-missions (needs weekly-missions >= 1)");
        assertThat(answered.accruing()).isEqualTo(2_000);
    }

    @Test
    @DisplayName("a pity counter is keyed by what the banner says it is carried by")
    void pityIsKeyedByTheBannersScope() {
        // Two pools of one type share a counter because they share a key, and
        // that is the whole of how the game's inheritance is expressed here.
        assertThat(PityState.scopeKeyOf(banner(250))).isEqualTo("type:event");
    }

    @Test
    @DisplayName("the worst case from a stored counter counts the pulls already made and the loss already carried")
    void theWorstCaseStartsWhereTheReaderIs() {
        PullModel split = PullModel.of(banner(250));
        PityState fresh = PityState.fresh(PityScope.BANNER_TYPE, "type:event");

        assertThat(split.worstCasePullsFrom(fresh, 1)).isEqualTo(120);
        // 45 in: 15 to this wall, then one more wall if that hit is lost.
        assertThat(split.worstCasePullsFrom(new PityState(PityScope.BANNER_TYPE, "k", 45, 0), 1)).isEqualTo(75);
        // A loss already carried makes the next hit certain, so only this wall.
        assertThat(split.worstCasePullsFrom(new PityState(PityScope.BANNER_TYPE, "k", 45, 1), 1)).isEqualTo(15);
        // Each further copy starts fresh.
        assertThat(split.worstCasePullsFrom(fresh, 3)).isEqualTo(360);
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

    private static Craft exchange(String id, ItemId from, int lot, ItemId to, int yields) {
        return new Craft(id, List.of(new ItemStack(from, lot)), List.of(new ItemStack(to, yields)), Availability.ALWAYS);
    }

    private static GameDefinition game(Source... sources) {
        GameId id = new GameId("t");
        return new GameDefinition(
                new Game(id, "Test", "Vigour"),
                new GameDataVersion(id, 1, "1", Instant.EPOCH, "hand-written"),
                List.of(
                        new Item(TICKET, "Ticket", new Rarity("5*", 5), "currency"),
                        new Item(new ItemId("cogs"), "Cogs", new Rarity("3*", 3), "currency"),
                        new Item(BLACK, "Black", new Rarity("5*", 5), "currency"),
                        new Item(RAINBOW, "Rainbow", new Rarity("5*", 5), "currency"),
                        new Item(BOX, "Box", new Rarity("4*", 4), "material-box")),
                List.of(sources),
                List.of(),
                List.of(),
                List.of());
    }
}
