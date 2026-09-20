package io.stormalmanac.gamedata.banner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * The published rates are the acceptance fixtures. If one record cannot express
 * both shipped shapes, the model is wrong and the engines cannot save it.
 *
 * <p>Figures per the sources cited in the plan; re-verify against in-game
 * disclosure before the simulator ships.
 */
class PityRuleTest {

    @Nested
    @DisplayName("Reverse: 1999 — soft pity, curve-shaped")
    class ReverseNineteenNinetyNine {

        // base 1.5%, soft pity after 60 pulls jumps to 4% then +2.5%/pull, hard 70
        private final PityRule rule = new PityRule(70, 60, 0.04, 0.025);
        private static final double BASE = 0.015;

        @Test
        void flatBeforeSoftPity() {
            assertThat(rule.rateAt(0, BASE)).isEqualTo(BASE);
            assertThat(rule.rateAt(59, BASE)).isEqualTo(BASE);
        }

        @Test
        void jumpsOnTheFirstPullPastTheThreshold() {
            // 60 misses so far, so the next pull is number 61 — the first past soft pity.
            assertThat(rule.rateAt(60, BASE)).isCloseTo(0.04, within(1e-9));
            assertThat(rule.rateAt(61, BASE)).isCloseTo(0.065, within(1e-9));
            assertThat(rule.rateAt(62, BASE)).isCloseTo(0.09, within(1e-9));
        }

        @Test
        void guaranteedAtHardPity() {
            assertThat(rule.rateAt(69, BASE)).isEqualTo(1.0);
            assertThat(rule.rateAt(200, BASE)).isEqualTo(1.0);
        }

        @Test
        void neverExceedsCertainty() {
            for (int pity = 0; pity < 80; pity++) {
                assertThat(rule.rateAt(pity, BASE)).isBetween(0.0, 1.0);
            }
        }
    }

    @Nested
    @DisplayName("Punishing: Gray Raven — hard pity, split-shaped")
    class PunishingGrayRaven {

        // base S-rank 0.5%, hard pity 60, no soft curve
        private final PityRule rule = PityRule.hard(60);
        private static final double BASE = 0.005;

        @Test
        void flatUntilTheWall() {
            assertThat(rule.rateAt(0, BASE)).isEqualTo(BASE);
            assertThat(rule.rateAt(58, BASE)).isEqualTo(BASE);
            assertThat(rule.rateAt(59, BASE)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("the 120-pull worst case follows from the featured split")
        void worstCaseIsTwoPityRuns() {
            // Lose the 70/30 split at pity, then be guaranteed on the next hit.
            FeaturedRule featured = new FeaturedRule(0.70, 1);
            assertThat(featured.guaranteeAfterLoss()).isEqualTo(1);
            assertThat(rule.hardAt() * (1 + featured.guaranteeAfterLoss())).isEqualTo(120);
        }
    }

    @Nested
    @DisplayName("Punishing: Gray Raven — a guarantee the game draws, 80 to 100")
    class ADrawnGuarantee {

        // The Themed Construct pool: 1.50% base, a threshold generated uniformly
        // between 80 and 100 and generated again on every S-Rank. The counter on
        // the client reads 8/80~100, so the player is never told their own.
        private final PityRule rule = PityRule.drawn(80, 100);
        private static final double BASE = 0.015;

        @Test
        @DisplayName("flat below the range, because no draw can land there")
        void flatBelowTheRange() {
            assertThat(rule.rateAt(0, BASE)).isEqualTo(BASE);
            assertThat(rule.rateAt(78, BASE)).isEqualTo(BASE);
        }

        @Test
        @DisplayName("the first pull in the range is forced one time in twenty-one")
        void theFirstPullInTheRange() {
            // 79 misses, so this is pull 80 and the threshold is still uniform on
            // 80..100: it is this pull one time in 21, and the base rate otherwise.
            double forced = 1.0 / 21.0;
            assertThat(rule.rateAt(79, BASE))
                    .isCloseTo(forced + (1.0 - forced) * BASE, within(1e-12))
                    .isCloseTo(0.0619047619, within(1e-9));
        }

        @Test
        @DisplayName("the odds rise as the misses rule thresholds out, and end at certainty")
        void itRisesAcrossTheRange() {
            // Every miss past 80 eliminates one threshold, so the conditional
            // chance the wall is *this* pull climbs 1/21, 1/20, ... 1/1. Nothing
            // about the base rate changes; the posterior does.
            assertThat(rule.rateAt(89, BASE)).isCloseTo(0.1045454545, within(1e-9));
            assertThat(rule.rateAt(98, BASE)).isCloseTo(0.5075, within(1e-9));
            assertThat(rule.rateAt(99, BASE)).isEqualTo(1.0);
            assertThat(rule.rateAt(500, BASE)).isEqualTo(1.0);

            for (int pity = 79; pity < 99; pity++) {
                assertThat(rule.rateAt(pity, BASE))
                        .as("pull %d", pity + 1)
                        .isGreaterThan(rule.rateAt(pity - 1, BASE))
                        .isBetween(0.0, 1.0);
            }
        }

        @Test
        @DisplayName("the curve is the draw integrated out, checked against all twenty-one walls")
        void theCurveIsExactlyTheAverageOverEveryThreshold() {
            // This is the claim the whole record rests on, so it is checked by
            // brute force rather than argued. Survive n pulls the chain's way —
            // multiply one minus the rate at each count — and survive them the
            // game's way: draw a threshold, fail n base rolls, and require the
            // threshold to be past n. If these part company, the posterior is
            // wrong and every answer the engines give about this banner is too.
            double survivingTheCurve = 1.0;
            for (int n = 1; n <= 100; n++) {
                survivingTheCurve *= 1.0 - rule.rateAt(n - 1, BASE);

                double survivingTheDraw = 0.0;
                for (int wall = 80; wall <= 100; wall++) {
                    if (wall > n) survivingTheDraw += Math.pow(1.0 - BASE, n);
                }
                survivingTheDraw /= 21.0;

                assertThat(survivingTheCurve)
                        .as("surviving %d pulls", n)
                        .isCloseTo(survivingTheDraw, within(1e-12));
            }
            assertThat(survivingTheCurve).isZero();
        }

        @Test
        @DisplayName("a caller holding a drawn threshold gets the flat rate and a hard wall")
        void theSampledRoadIsTheOtherOne() {
            // What a simulation sees: no posterior, no rise, just the wall it drew.
            assertThat(rule.rateAtWall(0, BASE, 90)).isEqualTo(BASE);
            assertThat(rule.rateAtWall(88, BASE, 90)).isEqualTo(BASE);
            assertThat(rule.rateAtWall(89, BASE, 90)).isEqualTo(1.0);

            // And the curve is emphatically not that: at pull 89 the integrated
            // rate is already ten points up, because nineteen thresholds are gone.
            assertThat(rule.rateAt(88, BASE)).isGreaterThan(rule.rateAtWall(88, BASE, 90));
        }

        @Test
        @DisplayName("a range of one is a fixed wall, and is refused rather than spelled twice")
        void aRangeOfOneIsRefused() {
            assertThatThrownBy(() -> PityRule.drawn(100, 100))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("PityRule.hard(100)");
            assertThatThrownBy(() -> PityRule.drawn(101, 100))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> PityRule.drawn(0, 100))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("a fixed guarantee is the same record saying nothing, and still flat")
        void anAbsentDrawIsAFixedWall() {
            assertThat(rule.hasDrawnGuarantee()).isTrue();
            assertThat(PityRule.hard(60).hasDrawnGuarantee()).isFalse();
            assertThat(PityRule.hard(60).drawnFrom()).isNull();
            // The four-argument constructor is the shape every banner had before a
            // game drew its wall, and it has to keep meaning what it meant.
            assertThat(new PityRule(70, 60, 0.04, 0.025))
                    .isEqualTo(new PityRule(70, 60, 0.04, 0.025, null));
        }

        @Test
        @DisplayName("a drawn guarantee and a soft curve are independent, so they compose")
        void itComposesWithSoftPity() {
            // No shipped game pairs them. The posterior argument never mentions
            // the curve — a miss is a failed roll and an absent wall, and the roll
            // is independent of the draw — so the two multiply rather than
            // interfering, and that is worth pinning before a game needs it.
            PityRule both = new PityRule(100, 60, 0.04, 0.025, 80);
            // Pull 61 is the first past soft pity and is still twenty short of
            // any threshold, so the curve has it to itself.
            assertThat(both.curveRateAt(60, BASE)).isCloseTo(0.04, within(1e-9));
            assertThat(both.rateAt(60, BASE)).isCloseTo(0.04, within(1e-9));

            double forced = 1.0 / 21.0;
            double curve = both.curveRateAt(79, BASE);
            assertThat(both.rateAt(79, BASE)).isCloseTo(forced + (1.0 - forced) * curve, within(1e-12));
        }
    }

    @Test
    void softPityMustPrecedeHardPity() {
        assertThatThrownBy(() -> new PityRule(60, 70, 0.04, 0.025))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void softPityNeedsAllThreeParameters() {
        assertThatThrownBy(() -> new PityRule(70, 60, null, 0.025))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
