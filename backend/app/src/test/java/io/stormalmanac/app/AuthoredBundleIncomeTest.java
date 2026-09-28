package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;

import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gacha.DeclaredIncomeModel;
import io.stormalmanac.gacha.IncomeModel;
import io.stormalmanac.gacha.IncomeModel.PullBudget;
import io.stormalmanac.gacha.MarkovBannerEngine;
import io.stormalmanac.gacha.PityState;
import io.stormalmanac.gacha.PullModel;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.gamedata.ingest.CanonicalBundleParser;
import io.stormalmanac.player.Inventory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What a pull costs on the launch title, and what that buys.
 *
 * <p>The price is the last thing {@code IncomeModel} was waiting on and the
 * first arithmetic in this repository that spends a currency rather than
 * farming one. Pinned on the real bundle rather than on a fixture, because the
 * number is a reading: 250 Event Construct R&D Tickets a pull, off the pool
 * screen's own buttons on 2026-09-18, and a sequence that corrected it should
 * fail here rather than quietly move what a reader is told they can afford.
 *
 * <p>No database. The parse is the whole of what feeds this.
 */
class AuthoredBundleIncomeTest {

    private static final ProfileId PROFILE = new ProfileId("income");
    private static final ItemId TICKET = new ItemId("event-construct-rd-ticket");
    private static final ItemId BLACK_CARD = new ItemId("black-card");

    private final GameDefinition definition = load();
    private final IncomeModel income = new DeclaredIncomeModel();

    @Test
    @DisplayName("a pull on the Arrival Construct pool costs 250 tickets, and ten cost 2 500")
    void thePriceIsTheOneOnTheScreen() {
        BannerModel banner = definition.banners().getFirst();

        assertThat(banner.pricedPull().perPull()).isEqualTo(250);
        assertThat(banner.pricedPull().currency()).isEqualTo(TICKET);
        // The screen shows 2 500 for ten and this model holds one price, so the
        // absence of a discount is an assertion rather than an omission.
        assertThat(banner.pricedPull().perPull() * 10).isEqualTo(2_500);
    }

    @Test
    @DisplayName("a reader holding 1 000 tickets can afford four pulls and no more")
    void aBalanceBuysWholePulls() {
        PullBudget budget = income.affordableWithin(
                definition,
                definition.banners().getFirst(),
                Inventory.empty(PROFILE).with(TICKET, 1_000),
                0,
                Map.of());

        assertThat(budget.pulls()).isEqualTo(4);
    }

    @Test
    @DisplayName("Black Cards are tickets through Direct Exchange, held and granted alike")
    void blackCardsAreCountedAsTickets() {
        // One for one, 21 893 Black Cards is 87 pulls at 250 with 143 over,
        // and the 143 buy nothing.
        PullBudget budget = income.affordableWithin(
                definition,
                definition.banners().getFirst(),
                Inventory.empty(PROFILE).with(BLACK_CARD, 21_893),
                0,
                Map.of());

        assertThat(budget.held()).isEqualTo(21_893);
        assertThat(budget.pulls()).isEqualTo(87);
        assertThat(budget.converted())
                .containsExactly(new IncomeModel.Converted(
                        "black-card", List.of("direct-exchange-black-card"), 21_893, 0));
    }

    @Test
    @DisplayName("forty days of every mission and the top of the Cage pay 6 325 Black Cards, and silence pays none")
    void everyBlackCardGrantStandsBehindTheReadersAnswer() {
        // The daily bar's three points are 30 a day, forty of them 1 200; the
        // twelve weeklies are 1 000 a week and the Cage's two tiers 25, over
        // five whole weeks 5 125. That is 25 pulls with 75 over — and every
        // one of those grants is behind a bar, so a reader who says nothing
        // is told which six questions would move it.
        BannerModel banner = definition.banners().getFirst();

        PullBudget silent = income.affordableWithin(definition, banner, Inventory.empty(PROFILE), 40, Map.of());
        PullBudget everything = income.affordableWithin(
                definition,
                banner,
                Inventory.empty(PROFILE),
                40,
                Map.of("phantom-pain-cage-score", 1_100_000, "daily-missions", 100, "weekly-missions", 12));

        assertThat(silent.accruing()).isZero();
        assertThat(silent.uncounted()).containsExactly(
                "phantom-pain-cage-500000 (needs phantom-pain-cage-score >= 500000)",
                "phantom-pain-cage-1000000 (needs phantom-pain-cage-score >= 1000000)",
                "daily-missions-60 (needs daily-missions >= 60)",
                "daily-missions-80 (needs daily-missions >= 80)",
                "daily-missions-100 (needs daily-missions >= 100)",
                "weekly-missions-12 (needs weekly-missions >= 12)");
        assertThat(everything.accruing()).isEqualTo(6_325);
        assertThat(everything.pulls()).isEqualTo(25);
        assertThat(everything.uncounted()).isEmpty();
    }

    @Test
    @DisplayName("the Crucible pool hands the featured unit over on its first S-Rank, so sixty pulls are certain")
    void theCruciblePoolIsCertainAtItsWall() {
        // 0.50% and a wall at 60, as the Arrival pool, but 100% in the S-Rank
        // pool: one hit is enough, so the worst case is the wall and not 120.
        // Its pity is its own bucket, so an Arrival counter does not leak in.
        BannerModel crucible = definition.banners().stream()
                .filter(banner -> banner.id().value().equals("pgr-adelyde-anabasis"))
                .findFirst()
                .orElseThrow();

        assertThat(crucible.bannerType()).isEqualTo("crucible-event-construct");
        assertThat(crucible.pricedPull().perPull()).isEqualTo(250);
        assertThat(crucible.window().closesAt()).isEqualTo(Instant.parse("2026-11-04T23:00:00Z"));
        assertThat(PullModel.of(crucible).worstCasePulls()).isEqualTo(60);
        assertThat(new MarkovBannerEngine()
                        .probabilityOfFeatured(crucible, PityState.fresh(
                                crucible.pityScope(), crucible.bannerType()), 60, 1))
                .isEqualTo(1.0);
    }

    @Test
    @DisplayName("the three EXP pools are named, and the names are not the game's own word")
    void progressKindsAreNamed() {
        assertThat(definition.nameOfProgress("character-exp")).isEqualTo("Character EXP");
        assertThat(definition.nameOfProgress("weapon-exp")).isEqualTo("Weapon EXP");
        assertThat(definition.nameOfProgress("memory-exp")).isEqualTo("Memory EXP");

        // A kind nobody named falls back to its slug rather than to a blank or
        // an error, which is what every sequence before 7 does for all three.
        assertThat(definition.nameOfProgress("simulation-score")).isEqualTo("simulation-score");
    }

    private static GameDefinition load() {
        Path bundle = Path.of("..", "..", "data", "bundles", "punishing-gray-raven-steering-by-light.json");
        try (InputStream in = Files.newInputStream(bundle)) {
            return new CanonicalBundleParser().parse(in).definitionApprovedAt(Instant.EPOCH);
        } catch (IOException e) {
            throw new IllegalStateException("could not read " + bundle.toAbsolutePath().normalize(), e);
        }
    }
}
