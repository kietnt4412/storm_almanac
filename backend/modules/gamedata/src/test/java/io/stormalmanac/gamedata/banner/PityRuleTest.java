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
        @DisplayName("the floating-guarantee variant is the same record, different numbers")
        void floatingGuaranteeVariant() {
            // 1.5% base, pity somewhere in 80..100 — expressible without a new type.
            PityRule floating = PityRule.hard(80);
            assertThat(floating.rateAt(0, 0.015)).isEqualTo(0.015);
            assertThat(floating.rateAt(79, 0.015)).isEqualTo(1.0);
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
