package io.stormalmanac.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PoissonRateIntervalTest {

    private static final Offset<Double> TOL = Offset.offset(1e-6);

    @Test
    @DisplayName("known values from the closed form, computed independently of this class")
    void knownValues() {
        // 10 observed over 100 runs, two-sided 95%:
        //   (10 + z²/2 ± z·sqrt(10 + z²/4)) / 100  with z = 1.959963984540054
        // = (11.9207294 ± 6.4887420) / 100
        PoissonRateInterval interval = PoissonRateInterval.of(10, 100);
        assertThat(interval.lower()).isCloseTo(0.05431987, TOL);
        assertThat(interval.upper()).isCloseTo(0.18409471, TOL);
    }

    @Test
    @DisplayName("the same mean over a hundred times the runs is not the same evidence")
    void sampleSizeMoves() {
        // This is the defect the whole class exists for: both of these say
        // "0.1 of this item per run", and only one of them has earned it.
        PoissonRateInterval thin = PoissonRateInterval.ofMean(0.1, 100);
        PoissonRateInterval thick = PoissonRateInterval.ofMean(0.1, 10_000);

        assertThat(thin.midpoint()).isCloseTo(thick.midpoint(), Offset.offset(0.02));
        assertThat(thin.lower()).isLessThan(thick.lower());
        assertThat(thin.lower()).isCloseTo(0.0543, Offset.offset(1e-3));
        assertThat(thick.lower()).isCloseTo(0.0940, Offset.offset(1e-3));

        // The planner ranks on the lower bound, so state the consequence rather
        // than only the inputs: the thin sample loses nearly half its rate and
        // the well-sampled one loses six percent of it.
        assertThat(thin.lower() / 0.1).isLessThan(0.60);
        assertThat(thick.lower() / 0.1).isGreaterThan(0.90);
    }

    @Test
    @DisplayName("a rate above one per run is representable, which is why this is not Wilson")
    void ratesAboveOne() {
        // A stage yielding three copies a run has a binomial proportion of 1.0
        // and a mean of 3.0. Wilson would clamp this to the wrong number.
        PoissonRateInterval interval = PoissonRateInterval.ofMean(3.0, 500);
        assertThat(interval.upper()).isGreaterThan(3.0);
        assertThat(interval.lower()).isGreaterThan(2.8).isLessThan(3.0);
    }

    @Test
    @DisplayName("nothing observed yet is not proof that nothing drops")
    void zeroObserved() {
        PoissonRateInterval interval = PoissonRateInterval.of(0, 20);
        assertThat(interval.lower()).isEqualTo(0.0);
        // At zero the bound collapses to exactly z²/n, which is the most the
        // evidence can rule out rather than a claim the item does not exist.
        assertThat(interval.upper()).isCloseTo(3.8414588206941245 / 20, TOL);
    }

    @Test
    @DisplayName("any positive sighting supports a positive rate, so a source never vanishes")
    void neverZeroesASource() {
        // The planner drops zero coefficients, so a discount that could reach
        // zero would silently remove the only source of an item.
        assertThat(PoissonRateInterval.of(1, 41_212).lower()).isGreaterThan(0.0);
        assertThat(PoissonRateInterval.ofMean(0.0001, 105).lower()).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("the interval narrows onto the mean as runs accumulate")
    void narrows() {
        double previous = Double.MAX_VALUE;
        for (long runs : new long[] {105, 880, 2_680, 8_612, 41_212}) {
            PoissonRateInterval interval = PoissonRateInterval.ofMean(0.25, runs);
            assertThat(interval.width()).isLessThan(previous);
            assertThat(interval.lower()).isLessThan(0.25);
            assertThat(interval.upper()).isGreaterThan(0.25);
            previous = interval.width();
        }
        // The upstream's widest sample: still within a percent of the mean.
        assertThat(PoissonRateInterval.ofMean(0.25, 41_212).lower())
                .isCloseTo(0.25, Offset.offset(0.005));
    }

    @Test
    @DisplayName("a yield with no runs behind it is a declaration, and refuses to be an interval")
    void declaredIsNotSampled() {
        assertThatThrownBy(() -> PoissonRateInterval.of(1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("declared yield");
        assertThatThrownBy(() -> PoissonRateInterval.of(1, -5))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PoissonRateInterval.of(Double.NaN, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
