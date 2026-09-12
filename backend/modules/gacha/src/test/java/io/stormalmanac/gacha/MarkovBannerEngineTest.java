package io.stormalmanac.gacha;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import io.stormalmanac.common.id.BannerId;
import io.stormalmanac.gamedata.Availability;
import io.stormalmanac.gamedata.Rarity;
import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.gamedata.banner.FeaturedRule;
import io.stormalmanac.gamedata.banner.PityRule;
import io.stormalmanac.gamedata.banner.PityScope;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * What the exact chain must be true of, beyond reproducing two games' rates.
 *
 * <p>The published-rate fixtures say the engine gets the right answer for the
 * banners that exist. These are the properties that say it is the right
 * <em>engine</em>: that the answer is a probability, that more pulls never hurt,
 * that the rarities nobody is pulling for genuinely do not matter, and that the
 * cases it cannot answer are refused with a sentence rather than met with an
 * {@link OutOfMemoryError}.
 */
class MarkovBannerEngineTest {

    private final MarkovBannerEngine exact = new MarkovBannerEngine();

    @Nested
    @DisplayName("properties that hold for every banner")
    class Properties {

        @Test
        @DisplayName("no pulls is no chance, and every answer is a probability")
        void answersAreProbabilities() {
            for (BannerModel banner : Banners.all()) {
                PityState fresh = Banners.freshFor(banner);
                assertThat(exact.probabilityOfFeatured(banner, fresh, 0, 1)).isZero();
                for (int pulls = 0; pulls <= 130; pulls += 13) {
                    assertThat(exact.probabilityOfFeatured(banner, fresh, pulls, 1))
                            .as("%s over %d pulls", banner.id(), pulls)
                            .isBetween(0.0, 1.0);
                }
            }
        }

        @Test
        @DisplayName("one more pull never makes her less likely")
        void morePullsNeverHurt() {
            for (BannerModel banner : Banners.all()) {
                PityState fresh = Banners.freshFor(banner);
                double previous = 0.0;
                for (int pulls = 0; pulls <= (int) PullModel.of(banner).worstCasePulls(); pulls++) {
                    double now = exact.probabilityOfFeatured(banner, fresh, pulls, 1);
                    assertThat(now)
                            .as("%s: %d pulls against %d", banner.id(), pulls, pulls - 1)
                            .isGreaterThanOrEqualTo(previous);
                    previous = now;
                }
                assertThat(previous).isEqualTo(1.0);
            }
        }

        @Test
        @DisplayName("a second copy is never easier than a first")
        void moreCopiesAreNeverEasier() {
            for (BannerModel banner : Banners.all()) {
                PityState fresh = Banners.freshFor(banner);
                int pulls = (int) PullModel.of(banner).worstCasePulls() * 2;
                assertThat(exact.probabilityOfFeatured(banner, fresh, pulls, 2))
                        .as("two copies of %s", banner.id())
                        .isLessThanOrEqualTo(exact.probabilityOfFeatured(banner, fresh, pulls, 1));
            }
        }

        @Test
        @DisplayName("twice the worst case is two guaranteed copies")
        void theWorstCaseScalesWithCopies() {
            for (BannerModel banner : Banners.all()) {
                PityState fresh = Banners.freshFor(banner);
                int worst = (int) PullModel.of(banner).worstCasePulls();
                assertThat(exact.probabilityOfFeatured(banner, fresh, worst * 2, 2))
                        .as("two copies of %s within %d pulls", banner.id(), worst * 2)
                        .isEqualTo(1.0);
                assertThat(exact.probabilityOfFeatured(banner, fresh, worst * 2 - 1, 2))
                        .isLessThan(1.0);
            }
        }

        @Test
        @DisplayName("the expected wait never exceeds the worst case, and is at least one pull")
        void theAverageSitsInsideTheBound() {
            for (BannerModel banner : Banners.all()) {
                double average = exact.expectedPullsToFeatured(banner, Banners.freshFor(banner));
                assertThat(average)
                        .as("average wait on %s", banner.id())
                        .isGreaterThanOrEqualTo(1.0)
                        .isLessThanOrEqualTo(PullModel.of(banner).worstCasePulls());
            }
        }
    }

    @Nested
    @DisplayName("the rarities nobody is pulling for")
    class Irrelevance {

        @Test
        @DisplayName("adding base rates and pity rules for lower rarities changes nothing")
        void lowerRaritiesDoNotMoveTheAnswer() {
            // A real rate table carries every rarity. The headline counter measures
            // pulls since the last headline hit, so a 5-star arriving does not reset
            // it and a 5-star pity rule cannot change the 6-star's odds. That is an
            // argument, and this is the test that makes it a fact.
            BannerModel published = Banners.reverseDebut();

            Map<Rarity, Double> fullTable = new HashMap<>(published.baseRates());
            fullTable.put(Banners.FIVE_STAR, 0.085);
            fullTable.put(Banners.FOUR_STAR, 0.40);
            Map<Rarity, PityRule> fullPity = new HashMap<>(published.pityRules());
            fullPity.put(Banners.FIVE_STAR, PityRule.hard(10));

            BannerModel crowded = new BannerModel(
                    published.id(), published.displayName(), published.bannerType(),
                    fullTable, fullPity, published.floors(),
                    published.featuredRule(), published.pityScope(), published.window());

            assertThat(PullModel.of(crowded).headline()).isEqualTo(Banners.SIX_STAR);

            PityState fresh = Banners.freshFor(published);
            for (int pulls : new int[] {1, 10, 60, 65, 70}) {
                assertThat(exact.probabilityOfFeatured(crowded, fresh, pulls, 1))
                        .as("%d pulls with a full rate table", pulls)
                        .isEqualTo(exact.probabilityOfFeatured(published, fresh, pulls, 1));
            }
            assertThat(exact.expectedPullsToFeatured(crowded, fresh))
                    .isEqualTo(exact.expectedPullsToFeatured(published, fresh));
        }
    }

    @Nested
    @DisplayName("what it refuses")
    class Refusals {

        private final BannerModel banner = Banners.grayRavenRotational();

        @Test
        void refusesNegativePulls() {
            assertThatThrownBy(() -> exact.probabilityOfFeatured(banner, Banners.freshFor(banner), -1, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("pulls must be >= 0");
        }

        @Test
        void refusesAskingForNoCopies() {
            assertThatThrownBy(() -> exact.probabilityOfFeatured(banner, Banners.freshFor(banner), 10, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("copies must be >= 1");
        }

        @Test
        @DisplayName("refuses a state space it cannot allocate, and says how big it was")
        void refusesAnUnallocatableChain() {
            // A wall and a guarantee depth no game publishes, multiplied by a copy
            // count nobody wants. The honest failure is a sentence naming the three
            // numbers, not an OutOfMemoryError from inside a loop.
            BannerModel absurd = new BannerModel(
                    BannerId.of("absurd"), "Absurd", "character",
                    Map.of(Banners.S_RANK, 0.005),
                    Map.of(Banners.S_RANK, PityRule.hard(100_000)),
                    List.of(), new FeaturedRule(0.5, 1_000),
                    PityScope.GLOBAL, Availability.ALWAYS);

            assertThatThrownBy(() -> exact.probabilityOfFeatured(
                    absurd, PityState.fresh(PityScope.GLOBAL, "global"), 10, 1_000))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("absurd")
                    .hasMessageContaining("exact states");
        }
    }

    @Nested
    @DisplayName("the shapes only one game has, and the shapes neither has yet")
    class Shapes {

        @Test
        @DisplayName("a banner that is pity and nothing else still answers")
        void aZeroBaseRateIsStillABanner() {
            // Nothing published looks like this. It is the degenerate end of the
            // same record, and an engine that divided by the base rate anywhere
            // would fall over on it.
            BannerModel pityOnly = new BannerModel(
                    BannerId.of("pity-only"), "Pity only", "character",
                    Map.of(Banners.S_RANK, 0.0),
                    Map.of(Banners.S_RANK, PityRule.hard(10)),
                    List.of(), FeaturedRule.ALWAYS, PityScope.GLOBAL, Availability.ALWAYS);
            PityState fresh = PityState.fresh(PityScope.GLOBAL, "global");

            assertThat(exact.probabilityOfFeatured(pityOnly, fresh, 9, 1)).isZero();
            assertThat(exact.probabilityOfFeatured(pityOnly, fresh, 10, 1)).isEqualTo(1.0);
            assertThat(exact.expectedPullsToFeatured(pityOnly, fresh)).isEqualTo(10.0);
        }

        @Test
        @DisplayName("a guarantee after two losses is a number, and the average knows it")
        void aDeeperGuarantee() {
            // The shape PityState stopped carrying a boolean for. At a 50% split and
            // a wall of 10, the wait is the wall plus half a wall plus a quarter:
            // 10 * (1 + 0.5 + 0.25) = 17.5.
            BannerModel deep = new BannerModel(
                    BannerId.of("deep"), "Deep guarantee", "character",
                    Map.of(Banners.S_RANK, 0.0),
                    Map.of(Banners.S_RANK, PityRule.hard(10)),
                    List.of(), new FeaturedRule(0.5, 2), PityScope.GLOBAL, Availability.ALWAYS);
            PityState fresh = PityState.fresh(PityScope.GLOBAL, "global");

            assertThat(exact.expectedPullsToFeatured(deep, fresh)).isCloseTo(17.5, within(1e-9));
            // One loss in, a quarter of the wait is already behind her.
            assertThat(exact.expectedPullsToFeatured(
                    deep, new PityState(PityScope.GLOBAL, "global", 0, 1)))
                    .isCloseTo(15.0, within(1e-9));
            assertThat(exact.expectedPullsToFeatured(
                    deep, new PityState(PityScope.GLOBAL, "global", 0, 2)))
                    .isCloseTo(10.0, within(1e-9));
            assertThat(exact.probabilityOfFeatured(deep, fresh, 30, 1)).isEqualTo(1.0);
            assertThat(exact.probabilityOfFeatured(deep, fresh, 29, 1)).isLessThan(1.0);
        }

        @Test
        @DisplayName("a split that can never be won is still finite, because the guarantee ends it")
        void aSplitThatNeverWins() {
            BannerModel never = new BannerModel(
                    BannerId.of("never"), "Never featured", "character",
                    Map.of(Banners.S_RANK, 0.0),
                    Map.of(Banners.S_RANK, PityRule.hard(10)),
                    List.of(), new FeaturedRule(0.0, 3), PityScope.GLOBAL, Availability.ALWAYS);
            PityState fresh = PityState.fresh(PityScope.GLOBAL, "global");

            // Four hits at ten pulls each: three losses, then the guarantee.
            assertThat(exact.expectedPullsToFeatured(never, fresh)).isCloseTo(40.0, within(1e-9));
            assertThat(exact.probabilityOfFeatured(never, fresh, 40, 1)).isEqualTo(1.0);
            assertThat(exact.probabilityOfFeatured(never, fresh, 39, 1)).isZero();
        }
    }
}
