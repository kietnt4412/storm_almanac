package io.stormalmanac.gacha;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import io.stormalmanac.common.id.BannerId;
import io.stormalmanac.gamedata.Availability;
import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.gamedata.banner.FeaturedRule;
import io.stormalmanac.gamedata.banner.Floor;
import io.stormalmanac.gamedata.banner.PityRule;
import io.stormalmanac.gamedata.banner.PityScope;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * The rules one pull obeys, pinned here rather than inside either engine.
 *
 * <p>Both engines walk these three branches — the exact chain with probabilities,
 * the simulation with a random number — so a mistake made here is a mistake both
 * of them make identically, and the cross-check in {@link EngineAgreementTest}
 * would agree enthusiastically about the wrong answer. This is the test that
 * stops that: the transitions are asserted against a generator handing out
 * chosen numbers, so every branch is taken on purpose.
 */
class PullModelTest {

    /** A generator that returns exactly what the test says, so a branch is a choice. */
    private static RandomGenerator rolling(double... values) {
        return new RandomGenerator() {
            private int at = 0;

            @Override
            public double nextDouble() {
                return values[at++];
            }

            @Override
            public long nextLong() {
                throw new UnsupportedOperationException("the tests only draw doubles");
            }
        };
    }

    @Nested
    @DisplayName("reading the headline question out of a banner")
    class Reading {

        @Test
        void takesTheRarestRarityThatHasAPityRule() {
            PullModel model = PullModel.of(Banners.reverseDebut());
            assertThat(model.headline()).isEqualTo(Banners.SIX_STAR);
            assertThat(model.baseRate()).isEqualTo(0.015);
            assertThat(model.hardAt()).isEqualTo(70);
        }

        @Test
        @DisplayName("refuses a banner with no pity rule, because it has no rarity to pull for")
        void refusesABannerWithNoPityRule() {
            BannerModel nothingToPullFor = new BannerModel(
                    BannerId.of("no-pity"), "No pity", "character",
                    Map.of(Banners.SIX_STAR, 0.015), Map.of(),
                    List.of(), FeaturedRule.ALWAYS, PityScope.GLOBAL, Availability.ALWAYS);

            assertThatThrownBy(() -> PullModel.of(nothingToPullFor))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("no-pity")
                    .hasMessageContaining("no pity rule");
        }

        @Test
        @DisplayName("refuses a pity rule whose rarity has no base rate, and names the rarity")
        void refusesAPityRuleWithNoBaseRate() {
            BannerModel halfDeclared = new BannerModel(
                    BannerId.of("half-declared"), "Half declared", "character",
                    Map.of(Banners.FIVE_STAR, 0.085),
                    Map.of(Banners.SIX_STAR, PityRule.hard(70)),
                    List.of(), FeaturedRule.ALWAYS, PityScope.GLOBAL, Availability.ALWAYS);

            assertThatThrownBy(() -> PullModel.of(halfDeclared))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("half-declared")
                    .hasMessageContaining("6*")
                    .hasMessageContaining("no base rate");
        }

        @Test
        @DisplayName("a floor below the headline rarity is ignored, because it cannot change the answer")
        void ignoresAFloorBelowTheHeadline() {
            // The published 4-star-per-10-pull floor raises low outcomes up and
            // never caps high ones, so the 6-star rate is untouched by it.
            BannerModel withFloor = Banners.reverseDebut();
            BannerModel withoutFloor = new BannerModel(
                    withFloor.id(), withFloor.displayName(), withFloor.bannerType(),
                    withFloor.baseRates(), withFloor.pityRules(),
                    List.of(), withFloor.featuredRule(), withFloor.pityScope(), withFloor.window());

            BannerEngine exact = new MarkovBannerEngine();
            PityState fresh = Banners.freshFor(withFloor);
            assertThat(exact.probabilityOfFeatured(withFloor, fresh, 65, 1))
                    .isEqualTo(exact.probabilityOfFeatured(withoutFloor, fresh, 65, 1));
        }

        @Test
        @DisplayName("a floor that reaches the headline rarity is refused, not quietly ignored")
        void refusesAFloorThatGuaranteesTheHeadline() {
            // Such a floor is a second pity rule on a different period. Neither
            // engine models it, and ignoring it would understate every answer.
            BannerModel floored = new BannerModel(
                    BannerId.of("floored"), "Floored", "character",
                    Map.of(Banners.SIX_STAR, 0.015),
                    Map.of(Banners.SIX_STAR, PityRule.hard(70)),
                    List.of(new Floor(10, Banners.SIX_STAR)),
                    FeaturedRule.ALWAYS, PityScope.GLOBAL, Availability.ALWAYS);

            assertThatThrownBy(() -> PullModel.of(floored))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("floored")
                    .hasMessageContaining("second pity rule");
        }
    }

    @Nested
    @DisplayName("the rate the next pull is taken at")
    class Rates {

        private final PullModel model = PullModel.of(Banners.reverseDebut());

        @Test
        void followsThePityCurve() {
            assertThat(model.hitRateAt(0)).isEqualTo(0.015);
            assertThat(model.hitRateAt(59)).isEqualTo(0.015);
            assertThat(model.hitRateAt(60)).isCloseTo(0.04, within(1e-12));
            assertThat(model.hitRateAt(69)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("a counter carried past the wall is read as certainty rather than refused")
        void clampsACounterPastTheWall() {
            // A player who carried pity over from a banner with a longer wall is a
            // real thing to be; certainty is the only answer that is not worse
            // than the truth.
            assertThat(model.hitRateAt(70)).isEqualTo(1.0);
            assertThat(model.hitRateAt(5_000)).isEqualTo(1.0);
        }

        @Test
        void appliesTheFeaturedSplitUntilTheGuaranteeEngages() {
            PullModel split = PullModel.of(Banners.grayRavenRotational());
            assertThat(split.featuredChanceAfter(0)).isEqualTo(0.70);
            assertThat(split.featuredChanceAfter(1)).isEqualTo(1.0);
            assertThat(split.featuredChanceAfter(9)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("the worst case is the wall once when she cannot be lost, twice when she can")
        void worstCaseFollowsFromTheSplit() {
            assertThat(PullModel.of(Banners.reverseDebut()).worstCasePulls()).isEqualTo(70L);
            assertThat(PullModel.of(Banners.grayRavenRotational()).worstCasePulls()).isEqualTo(120L);
            assertThat(PullModel.of(Banners.grayRavenDebut()).worstCasePulls()).isEqualTo(60L);
        }
    }

    @Nested
    @DisplayName("one pull, every branch taken on purpose")
    class Drawing {

        private final PullModel model = PullModel.of(Banners.grayRavenRotational());
        private final PityState fresh = PityState.fresh(PityScope.BANNER_TYPE, "character");

        @Test
        @DisplayName("a miss raises pity and leaves the split alone")
        void aMiss() {
            PullResult result = model.draw(fresh, rolling(0.9));
            assertThat(result.headlineHit()).isFalse();
            assertThat(result.featured()).isFalse();
            assertThat(result.stateAfter().pullsSinceHit()).isEqualTo(1);
            assertThat(result.stateAfter().consecutiveLosses()).isZero();
        }

        @Test
        @DisplayName("a featured hit resets pity and the accumulated guarantee together")
        void aFeaturedHit() {
            PityState owed = new PityState(PityScope.BANNER_TYPE, "character", 40, 1);
            PullResult result = model.draw(owed, rolling(0.001, 0.1));
            assertThat(result.headlineHit()).isTrue();
            assertThat(result.featured()).isTrue();
            assertThat(result.stateAfter().pullsSinceHit()).isZero();
            assertThat(result.stateAfter().consecutiveLosses()).isZero();
        }

        @Test
        @DisplayName("losing the split resets pity and moves the guarantee one closer")
        void anOffFeaturedHit() {
            PullResult result = model.draw(fresh, rolling(0.001, 0.8));
            assertThat(result.headlineHit()).isTrue();
            assertThat(result.featured()).isFalse();
            assertThat(result.stateAfter().pullsSinceHit()).isZero();
            assertThat(result.stateAfter().consecutiveLosses()).isEqualTo(1);
        }

        @Test
        @DisplayName("once the guarantee is owed, no random number can lose it")
        void theGuaranteeCannotBeLost() {
            PityState guaranteed = new PityState(PityScope.BANNER_TYPE, "character", 0, 1);
            // 0.999999 would lose a 70% split outright; the guarantee overrides it.
            PullResult result = model.draw(guaranteed, rolling(0.001, 0.999999));
            assertThat(result.featured()).isTrue();
        }

        @Test
        @DisplayName("at the wall the rarity arrives whatever the generator says")
        void theWallIgnoresTheGenerator() {
            PityState atTheWall = new PityState(PityScope.BANNER_TYPE, "character", 59, 0);
            PullResult result = model.draw(atTheWall, rolling(0.999999, 0.001));
            assertThat(result.headlineHit()).isTrue();
        }

        @Test
        @DisplayName("a pull cannot be featured without hitting the rarity")
        void featuredImpliesAHit() {
            assertThatThrownBy(() -> new PullResult(false, true, fresh))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be featured");
        }
    }

    @Nested
    @DisplayName("the state a player carries")
    class State {

        @Test
        @DisplayName("a loss count rather than a flag, so a guarantee after two losses is expressible")
        void lossesAreCounted() {
            FeaturedRule afterTwo = new FeaturedRule(0.5, 2);
            PityState fresh = PityState.fresh(PityScope.GLOBAL, "global");

            assertThat(fresh.isGuaranteed(afterTwo)).isFalse();
            assertThat(fresh.afterOffFeaturedHit().isGuaranteed(afterTwo)).isFalse();
            assertThat(fresh.afterOffFeaturedHit().afterOffFeaturedHit().isGuaranteed(afterTwo)).isTrue();

            // The shape both shipped games use, where one loss is enough.
            assertThat(fresh.afterOffFeaturedHit().isGuaranteed(FeaturedRule.ALWAYS)).isTrue();
        }

        @Test
        void refusesNegativeCounters() {
            assertThatThrownBy(() -> new PityState(PityScope.GLOBAL, "global", -1, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("pullsSinceHit");
            assertThatThrownBy(() -> new PityState(PityScope.GLOBAL, "global", 0, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("consecutiveLosses");
        }
    }
}
