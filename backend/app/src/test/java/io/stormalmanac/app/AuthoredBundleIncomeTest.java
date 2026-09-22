package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;

import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gacha.DeclaredIncomeModel;
import io.stormalmanac.gacha.IncomeModel;
import io.stormalmanac.gacha.IncomeModel.PullBudget;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.gamedata.ingest.CanonicalBundleParser;
import io.stormalmanac.player.Inventory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
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
    @DisplayName("nothing in this bundle grants a ticket, so forty days accrue nothing and say so")
    void thisBundleDeclaresNoIncome() {
        // The honest answer and a visibly incomplete one. PGR hands out research
        // tickets — the maintainer plays daily — and no reading in this
        // repository says how many, so the model reports zero rather than
        // inventing a rate. A sequence that reads one will fail this test, which
        // is the right way round: the assertion is a marker for a missing
        // reading, not a claim that the game is stingy.
        PullBudget budget = income.affordableWithin(
                definition,
                definition.banners().getFirst(),
                Inventory.empty(PROFILE),
                40,
                Map.of("phantom-pain-cage-score", 1_100_000));

        assertThat(budget.accruing()).isZero();
        assertThat(budget.uncounted())
                .as("no ticket grant is declared at all, so none can have been dropped for a bar")
                .isEmpty();
        assertThat(budget.pulls()).isZero();
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
