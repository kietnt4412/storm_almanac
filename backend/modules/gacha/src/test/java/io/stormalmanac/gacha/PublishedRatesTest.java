package io.stormalmanac.gacha;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.gamedata.banner.FeaturedRule;
import io.stormalmanac.gamedata.banner.PityRule;
import io.stormalmanac.gamedata.banner.PityScope;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Phase 5's exit criterion, second half: the published rates of both shipped
 * games, reproduced from {@link BannerModel} alone.
 *
 * <p>The numbers pinned here were computed independently of the engine being
 * tested — a separate implementation of the same chain, written to check this one
 * rather than to ship — so a shared mistake has to be made twice in two languages
 * to survive. The structural claims are the ones worth reading: the walls land
 * exactly where the games say they do, and the probability one pull short of each
 * is strictly below certainty. A wall is the one part of a pity system a player
 * can verify for themselves, so it is the part an engine has no excuse to be
 * approximately right about.
 *
 * <p><b>One banner here is first-hand, and the rest are still second-hand.</b>
 * {@link Disclosed} is read off the client's own rules screen (Q4, 2026-09-13), and
 * it is the only nested class that is evidence about the game rather than about
 * the model. Everything else comes from the sources the plan cites and proves only
 * that the model reproduces the figures it was given.
 */
class PublishedRatesTest {

    private final BannerEngine exact = new MarkovBannerEngine();

    @Nested
    @DisplayName("Reverse: 1999 — read off the client's rules screen, not a guide")
    class Disclosed {

        private final BannerModel banner = Banners.reverseAnniversaryLimited();
        private final PityState fresh = Banners.freshFor(banner);

        /** The same banner with the split removed, so the featured wait is the 6-star wait. */
        private BannerModel anySixStar(PityRule curve) {
            return new BannerModel(banner.id(), banner.displayName(), banner.bannerType(),
                    banner.baseRates(), Map.of(Banners.SIX_STAR, curve), banner.floors(),
                    FeaturedRule.ALWAYS, banner.pityScope(), banner.window());
        }

        private static double consolidatedPercent(double expectedPulls) {
            return Math.round(10_000.0 / expectedPulls) / 100.0;
        }

        @Test
        @DisplayName("the screen's own 2.36% overall rate falls out of the curve the screen states")
        void theOverallRateIsTheCurve() {
            // The publisher's "overall rate (including Guarantee)" is one 6-star per
            // expected wait. It is the only number on the screen the curve was not
            // written from, so it is the one that checks the curve.
            PityRule stated = banner.pityRules().get(Banners.SIX_STAR);
            double wait = exact.expectedPullsToFeatured(anySixStar(stated), fresh);
            assertThat(wait).isCloseTo(42.3868867154, within(1e-9));
            assertThat(consolidatedPercent(wait)).isEqualTo(2.36);
        }

        @Test
        @DisplayName("and it would catch a curve that started one pull early or late")
        void theOverallRateHasTeeth() {
            // A check every plausible curve passes is not a check. These are the
            // near misses: each prints a different two-decimal rate from 2.36%.
            assertThat(consolidatedPercent(exact.expectedPullsToFeatured(
                    anySixStar(new PityRule(70, 59, 0.04, 0.025)), fresh))).isEqualTo(2.38);
            assertThat(consolidatedPercent(exact.expectedPullsToFeatured(
                    anySixStar(new PityRule(70, 61, 0.04, 0.025)), fresh))).isEqualTo(2.34);
            assertThat(consolidatedPercent(exact.expectedPullsToFeatured(
                    anySixStar(PityRule.hard(70)), fresh))).isEqualTo(2.30);
        }

        @Test
        @DisplayName("70 pulls guarantees a 6-star and only 66% of the time guarantees her")
        void theWallGuaranteesTheRarityAndNotHer() {
            assertThat(exact.probabilityOfFeatured(anySixStar(banner.pityRules().get(Banners.SIX_STAR)), fresh, 70, 1))
                    .isEqualTo(1.0);
            assertThat(exact.probabilityOfFeatured(banner, fresh, 70, 1))
                    .isCloseTo(0.6578297188, within(1e-9));
        }

        @Test
        @DisplayName("she is certain at 140 and not at 139, and the 200-cassette shop cannot beat that")
        void worstCaseForOneCopy() {
            // Cassettes of the Lost buy her in the Limited Shop, and no engine models
            // that road. For one copy it does not need to: a summon grants one
            // cassette, so the shop opens at pull 200, after the pulls have already
            // guaranteed her. For two copies it does bind (200, not 280), which is
            // why this claim is about one copy and nothing more.
            long shopPrice = 200;
            assertThat(PullModel.of(banner).worstCasePulls()).isEqualTo(140L).isLessThan(shopPrice);
            assertThat(exact.probabilityOfFeatured(banner, fresh, 140, 1)).isEqualTo(1.0);
            assertThat(exact.probabilityOfFeatured(banner, fresh, 139, 1))
                    .isCloseTo(0.9948695074, within(1e-9));
        }

        @Test
        @DisplayName("an average of 63.58 pulls to her, which is the 6-star wait times 1.5")
        void expectedPullsToHer() {
            assertThat(exact.expectedPullsToFeatured(banner, fresh))
                    .isCloseTo(63.5803300731, within(1e-9))
                    .isCloseTo(42.3868867154 * 1.5, within(1e-9));
        }
    }

    @Nested
    @DisplayName("Reverse: 1999 — soft pity, curve-shaped (second-hand, no split)")
    class ReverseNineteenNinetyNine {

        private final BannerModel banner = Banners.reverseDebut();
        private final PityState fresh = Banners.freshFor(banner);

        @Test
        @DisplayName("the wall at 70 is certainty, and 69 pulls is not")
        void hardPityIsExact() {
            assertThat(exact.probabilityOfFeatured(banner, fresh, 70, 1)).isEqualTo(1.0);
            assertThat(exact.probabilityOfFeatured(banner, fresh, 69, 1))
                    .isCloseTo(0.8987034786, within(1e-9))
                    .isLessThan(1.0);
        }

        @Test
        @DisplayName("flat at 1.5% until the curve starts, so ten pulls is 1 - 0.985^10")
        void flatBeforeSoftPity() {
            assertThat(exact.probabilityOfFeatured(banner, fresh, 10, 1))
                    .isCloseTo(1.0 - Math.pow(0.985, 10), within(1e-12))
                    .isCloseTo(0.1402695577, within(1e-9));
            assertThat(exact.probabilityOfFeatured(banner, fresh, 60, 1))
                    .isCloseTo(1.0 - Math.pow(0.985, 60), within(1e-12))
                    .isCloseTo(0.5961930124, within(1e-9));
        }

        @Test
        @DisplayName("the curve bends the odds up from pull 61, not from pull 60")
        void softPityBitesOnePullPastTheThreshold() {
            double atSixty = exact.probabilityOfFeatured(banner, fresh, 60, 1);
            double atSixtyOne = exact.probabilityOfFeatured(banner, fresh, 61, 1);
            double atSixtyFive = exact.probabilityOfFeatured(banner, fresh, 65, 1);

            // The 61st pull is worth 4%, not 1.5%: the first increment the curve adds.
            assertThat(atSixtyOne - atSixty).isCloseTo(0.04 * (1.0 - atSixty), within(1e-12));
            assertThat(atSixtyOne).isCloseTo(0.6123452919, within(1e-9));
            assertThat(atSixtyFive).isCloseTo(0.7489618140, within(1e-9));
        }

        @Test
        @DisplayName("an average of 42.39 pulls to a 6-star, and the wall is what bounds it")
        void expectedPulls() {
            assertThat(exact.expectedPullsToFeatured(banner, fresh))
                    .isCloseTo(42.3868867154, within(1e-9))
                    .isLessThan(70.0);
        }

        @Test
        @DisplayName("carried pity is worth what it should be: 6.54 pulls left at 60")
        void carriedPityShortensTheWait() {
            PityState atSixty = new PityState(PityScope.BANNER, "character", 60, 0);
            assertThat(exact.expectedPullsToFeatured(banner, atSixty))
                    .isCloseTo(6.5394754694, within(1e-9));

            PityState atTheWall = new PityState(PityScope.BANNER, "character", 69, 0);
            assertThat(exact.expectedPullsToFeatured(banner, atTheWall)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("the beginner banner's 6-star within 30 is the same record, a shorter wall")
        void beginnerBanner() {
            BannerModel beginner = Banners.reverseBeginner();
            PityState from = Banners.freshFor(beginner);
            assertThat(exact.probabilityOfFeatured(beginner, from, 30, 1)).isEqualTo(1.0);
            assertThat(exact.probabilityOfFeatured(beginner, from, 29, 1)).isLessThan(1.0);
        }
    }

    @Nested
    @DisplayName("Punishing: Gray Raven — hard pity, split-shaped")
    class PunishingGrayRaven {

        private final BannerModel banner = Banners.grayRavenRotational();
        private final PityState fresh = Banners.freshFor(banner);

        @Test
        @DisplayName("the published 120-pull worst case falls out of the wall and the split")
        void worstCaseIsOneHundredAndTwenty() {
            assertThat(PullModel.of(banner).worstCasePulls()).isEqualTo(120L);
            assertThat(exact.probabilityOfFeatured(banner, fresh, 120, 1)).isEqualTo(1.0);
            assertThat(exact.probabilityOfFeatured(banner, fresh, 119, 1))
                    .isCloseTo(0.8339477431, within(1e-9))
                    .isLessThan(1.0);
        }

        @Test
        @DisplayName("60 pulls guarantees an S-rank, and only 71% of the time guarantees her")
        void theWallGuaranteesTheRarityAndNotTheUnit() {
            // Above 0.70 rather than equal to it: the wall guarantees one S-rank
            // within 60, and a lucky early hit leaves room for a second.
            assertThat(exact.probabilityOfFeatured(banner, fresh, 60, 1))
                    .isCloseTo(0.7109634351, within(1e-9))
                    .isGreaterThan(0.70);
        }

        @Test
        @DisplayName("a debut banner hands her over outright, so the wall is the whole answer")
        void debutBannerNeedsOneHitNotTwo() {
            BannerModel debut = Banners.grayRavenDebut();
            PityState from = Banners.freshFor(debut);
            assertThat(PullModel.of(debut).worstCasePulls()).isEqualTo(60L);
            assertThat(exact.probabilityOfFeatured(debut, from, 60, 1)).isEqualTo(1.0);
            assertThat(exact.probabilityOfFeatured(debut, from, 59, 1)).isLessThan(1.0);
        }

        @Test
        @DisplayName("an average of 67.53 pulls, which is the S-rank wait times 1.3")
        void expectedPulls() {
            assertThat(exact.expectedPullsToFeatured(banner, fresh))
                    .isCloseTo(67.5321509989, within(1e-9));

            // Losing the split costs exactly one more full wait, 30% of the time.
            PityState guaranteed = new PityState(PityScope.BANNER_TYPE, "character", 0, 1);
            double whenGuaranteed = exact.expectedPullsToFeatured(banner, guaranteed);
            assertThat(whenGuaranteed).isCloseTo(51.9478084607, within(1e-9));
            assertThat(exact.expectedPullsToFeatured(banner, fresh))
                    .isCloseTo(whenGuaranteed * 1.3, within(1e-9));
        }

        @Test
        @DisplayName("variable walls are numbers and not types: 30 for weapons, 40 beginner, 80 floating")
        void everyPublishedWallIsTheSameRecord() {
            assertThat(PullModel.of(Banners.grayRavenWeapon()).hardAt()).isEqualTo(30);
            assertThat(PullModel.of(Banners.grayRavenBeginner()).hardAt()).isEqualTo(40);
            assertThat(PullModel.of(Banners.grayRavenFloating()).hardAt()).isEqualTo(80);

            // Certainty arrives at the worst case and not at the wall. Writing this
            // test the other way round is how the distinction got checked: asserting
            // the 30-pull weapon wall was certainty failed at 0.7030, which is the
            // 70% split and a little luck — the wall guarantees the rarity, and the
            // guarantee after a loss is what guarantees her.
            for (BannerModel each : new BannerModel[] {
                    Banners.grayRavenWeapon(), Banners.grayRavenBeginner(), Banners.grayRavenFloating()}) {
                int worst = (int) PullModel.of(each).worstCasePulls();
                PityState from = Banners.freshFor(each);
                assertThat(exact.probabilityOfFeatured(each, from, worst, 1))
                        .as("the worst case of %s is certainty", each.id())
                        .isEqualTo(1.0);
                assertThat(exact.probabilityOfFeatured(each, from, worst - 1, 1))
                        .as("one pull short of the worst case of %s is not", each.id())
                        .isLessThan(1.0);
            }
            assertThat(exact.probabilityOfFeatured(
                    Banners.grayRavenWeapon(), Banners.freshFor(Banners.grayRavenWeapon()), 30, 1))
                    .isCloseTo(0.7029728946, within(1e-9));
        }

        @Test
        @DisplayName("the floating variant's 1.5% base is the flat rate it claims")
        void floatingVariantBaseRate() {
            BannerModel floating = Banners.grayRavenFloating();
            PullModel model = PullModel.of(floating);
            assertThat(model.hitRateAt(0)).isEqualTo(0.015);
            assertThat(model.hitRateAt(78)).isEqualTo(0.015);
            assertThat(model.hitRateAt(79)).isEqualTo(1.0);

            // Not simply "one hit in ten pulls times the 70% split". That product
            // was the first thing written here, and it is 0.0982 against 0.1010:
            // it forgets that losing the split early leaves room to hit again inside
            // the same ten pulls, and the second hit is guaranteed.
            double oneHit = 1.0 - Math.pow(0.985, 10);
            assertThat(exact.probabilityOfFeatured(floating, Banners.freshFor(floating), 10, 1))
                    .isCloseTo(0.1009925325, within(1e-9))
                    .isGreaterThan(oneHit * 0.70)
                    .isLessThan(oneHit);
        }

        @Test
        @DisplayName("pity is carried by the counter the caller keys, so a scope cannot leak")
        void pityScopeIsTheCallersBookkeeping() {
            // The published rule is that a character counter does not help a weapon
            // banner. The engines enforce nothing of the kind and must not: they
            // answer about the state they are handed. What this pins is that the
            // scope key is never read — so the only thing that can carry pity
            // across banner types is a caller fetching the wrong row.
            PityState asCharacter = new PityState(PityScope.BANNER_TYPE, "character", 40, 0);
            PityState asWeapon = new PityState(PityScope.BANNER_TYPE, "weapon", 40, 0);
            assertThat(exact.probabilityOfFeatured(banner, asCharacter, 10, 1))
                    .isEqualTo(exact.probabilityOfFeatured(banner, asWeapon, 10, 1));

            // And that the counter itself is what moves the answer, so the keying
            // is worth doing.
            assertThat(exact.probabilityOfFeatured(banner, asCharacter, 20, 1))
                    .isGreaterThan(exact.probabilityOfFeatured(banner, Banners.freshFor(banner), 20, 1));
        }
    }
}
