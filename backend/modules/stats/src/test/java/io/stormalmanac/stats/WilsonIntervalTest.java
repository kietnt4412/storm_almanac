package io.stormalmanac.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WilsonIntervalTest {

    private static final double TOL = 1e-4;

    @Test
    @DisplayName("no evidence is the whole interval, not a point at zero")
    void zeroTrials() {
        WilsonInterval interval = WilsonInterval.of(0, 0);
        assertThat(interval.lower()).isEqualTo(0.0);
        assertThat(interval.upper()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("a thin sample stays honestly wide")
    void smallSampleIsWide() {
        // The case the plan calls out: at n=7 a naive 43% is noise.
        WilsonInterval interval = WilsonInterval.of(3, 7);
        assertThat(interval.lower()).isLessThan(0.20);
        assertThat(interval.upper()).isGreaterThan(0.70);
        assertThat(interval.width()).isGreaterThan(0.5);
    }

    @Test
    @DisplayName("known values from the standard 95% formula")
    void knownValues() {
        // 10 hits in 100 runs, two-sided 95%: [0.055229, 0.174366].
        // Computed independently from the closed form, not from this class.
        WilsonInterval interval = WilsonInterval.of(10, 100);
        assertThat(interval.lower()).isCloseTo(0.055229, org.assertj.core.data.Offset.offset(TOL));
        assertThat(interval.upper()).isCloseTo(0.174366, org.assertj.core.data.Offset.offset(TOL));
    }

    @Test
    @DisplayName("zero successes still yields a positive upper bound")
    void zeroSuccessesIsNotCertainty() {
        // The normal approximation collapses to [0,0] here, which would tell the
        // optimizer an item never drops after twenty runs. Wilson does not.
        WilsonInterval interval = WilsonInterval.of(0, 20);
        assertThat(interval.lower()).isEqualTo(0.0);
        assertThat(interval.upper()).isGreaterThan(0.10);
    }

    @Test
    @DisplayName("bounds never leave [0,1] and narrow as evidence accumulates")
    void staysInRangeAndNarrows() {
        WilsonInterval thin = WilsonInterval.of(50, 100);
        WilsonInterval thick = WilsonInterval.of(5_000, 10_000);
        assertThat(thin.lower()).isGreaterThanOrEqualTo(0.0);
        assertThat(thin.upper()).isLessThanOrEqualTo(1.0);
        assertThat(thick.width()).isLessThan(thin.width());
        assertThat(thick.midpoint()).isCloseTo(0.5, org.assertj.core.data.Offset.offset(0.02));
    }

    @Test
    void rejectsImpossibleCounts() {
        assertThatThrownBy(() -> WilsonInterval.of(5, 3))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> WilsonInterval.of(-1, 3))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
