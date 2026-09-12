package io.stormalmanac.gacha;

import static org.assertj.core.api.Assertions.assertThat;

import io.stormalmanac.gamedata.banner.BannerModel;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Phase 5's exit criterion, first half: the two engines agree within 0.3%.
 *
 * <p>They share {@link PullModel}'s three branches and nothing else. One walks a
 * probability distribution forward and reads off the mass that never arrived; the
 * other pulls the banner half a million times and counts. Agreement across every
 * published banner is a correctness argument about the pity model that neither
 * method can make alone — and the first place they part company will be a real
 * bug in the model rather than a rounding difference in either.
 *
 * <p><b>Two tolerances, because one of them would be dishonest on its own.</b>
 * The 0.3 percentage points is the plan's criterion and this test asserts it. The
 * second tolerance is what says the engines <em>agree</em> rather than that one
 * seed was kind: every gap is inside three standard errors of the simulation that
 * produced it, which is a claim about sampling noise and not about this seed.
 *
 * <p>The two tolerances are why the trial count is not the plan's 100 000 — see
 * {@link MonteCarloBannerEngine#DEFAULT_TRIALS}. At that count 0.3 points is
 * under two standard errors, and this test duly failed on its first run with both
 * engines correct. The criterion did not move; the sample size did.
 *
 * <p>The worst gap observed is printed rather than hidden, because the number
 * that matters is how much headroom the criterion has, not that it passed.
 */
class EngineAgreementTest {

    /** The plan's criterion, as an absolute gap between two probabilities. */
    private static final double CRITERION = 0.003;

    private final MarkovBannerEngine exact = new MarkovBannerEngine();
    private final MonteCarloBannerEngine sampled = new MonteCarloBannerEngine();

    /** One question asked of both engines. */
    private record Question(BannerModel banner, PityState from, int pulls, int copies) {
        @Override
        public String toString() {
            return "%s from (pity %d, losses %d) over %d pulls for %d copies"
                    .formatted(banner.id(), from.pullsSinceHit(), from.consecutiveLosses(), pulls, copies);
        }
    }

    /**
     * Every published banner, asked at the interesting places: well short of the
     * wall, at it, one past it, and at the worst case — plus the states a real
     * player turns up in, carrying pity or carrying a lost split.
     */
    private static List<Question> questions() {
        List<Question> asked = new ArrayList<>();
        for (BannerModel banner : Banners.all()) {
            PullModel model = PullModel.of(banner);
            int wall = model.hardAt();
            PityState fresh = Banners.freshFor(banner);

            for (int pulls : new int[] {1, 10, wall / 2, wall - 1, wall, wall + 1, (int) model.worstCasePulls()}) {
                asked.add(new Question(banner, fresh, pulls, 1));
            }
            // Carrying most of a wall: the states where a soft-pity curve matters most.
            PityState carried = new PityState(banner.pityScope(), banner.bannerType(), wall - 5, 0);
            asked.add(new Question(banner, carried, 10, 1));
            asked.add(new Question(banner, carried, wall, 1));

            // Carrying a lost split, where one engine's guarantee bookkeeping could
            // differ from the other's without either being obviously wrong.
            PityState owed = new PityState(banner.pityScope(), banner.bannerType(), 0, 1);
            asked.add(new Question(banner, owed, wall, 1));

            // Two copies, which doubles the absorbing dimension and is where an
            // off-by-one in the exact chain would hide.
            asked.add(new Question(banner, fresh, wall * 2, 2));
            asked.add(new Question(banner, fresh, (int) model.worstCasePulls() * 2, 2));
        }
        return asked;
    }

    @Test
    @DisplayName("the exact chain and the simulation agree within 0.3% on every published banner")
    void theEnginesAgree() {
        double worstGap = 0.0;
        double worstSigma = 0.0;
        Question worstAt = null;

        for (Question question : questions()) {
            double exactly = exact.probabilityOfFeatured(
                    question.banner(), question.from(), question.pulls(), question.copies());
            double simulated = sampled.probabilityOfFeatured(
                    question.banner(), question.from(), question.pulls(), question.copies());
            double gap = Math.abs(exactly - simulated);
            double standardError = sampled.standardErrorAt(exactly);

            assertThat(gap)
                    .as("%s: exact %.6f against simulated %.6f", question, exactly, simulated)
                    .isLessThanOrEqualTo(CRITERION);

            // A gap of zero at a probability of zero or one has no standard error
            // to be measured against, and those are the certainty cases — the walls
            // — where the assertion above is already the stronger one.
            if (standardError > 0.0) {
                double sigma = gap / standardError;
                assertThat(sigma)
                        .as("%s: gap %.6f is %.2f standard errors of the simulation", question, gap, sigma)
                        .isLessThan(3.0);
                if (sigma > worstSigma) worstSigma = sigma;
            }
            if (gap > worstGap) {
                worstGap = gap;
                worstAt = question;
            }
        }

        System.out.printf(
                "engine agreement over %d questions at %d trials: worst gap %.6f (%.3f points) at %s;"
                        + " worst %.2f standard errors%n",
                questions().size(), sampled.trials(), worstGap, worstGap * 100, worstAt, worstSigma);
    }

    @Test
    @DisplayName("they agree on the expected wait too, inside the bound its own variance allows")
    void theEnginesAgreeOnTheAverage() {
        for (BannerModel banner : Banners.all()) {
            PullModel model = PullModel.of(banner);
            PityState fresh = Banners.freshFor(banner);

            double exactly = exact.expectedPullsToFeatured(banner, fresh);
            double simulated = sampled.expectedPullsToFeatured(banner, fresh);

            // A pull count lies in [1, worstCase], so by Popoviciu's inequality its
            // variance is at most (range/2)^2 however the pity curve is shaped.
            // That is loose — no real banner is anywhere near it — and it is a
            // bound rather than a number tuned until the test passed.
            double range = model.worstCasePulls() - 1;
            double standardErrorBound = (range / 2.0) / Math.sqrt(sampled.trials());

            assertThat(Math.abs(exactly - simulated))
                    .as("%s: exact %.4f pulls against simulated %.4f, bound %.4f",
                            banner.id(), exactly, simulated, 4.0 * standardErrorBound)
                    .isLessThanOrEqualTo(4.0 * standardErrorBound);
        }
    }

    @Test
    @DisplayName("the simulation is the same simulation twice, so a changed answer is changed code")
    void theSimulationIsDeterministic() {
        BannerModel banner = Banners.grayRavenRotational();
        PityState fresh = Banners.freshFor(banner);

        MonteCarloBannerEngine first = new MonteCarloBannerEngine();
        MonteCarloBannerEngine second = new MonteCarloBannerEngine();

        assertThat(first.probabilityOfFeatured(banner, fresh, 60, 1))
                .isEqualTo(second.probabilityOfFeatured(banner, fresh, 60, 1));
        assertThat(first.expectedPullsToFeatured(banner, fresh))
                .isEqualTo(second.expectedPullsToFeatured(banner, fresh));

        // And a different seed is a different answer, or the seed is not being used.
        MonteCarloBannerEngine elsewhere = new MonteCarloBannerEngine(
                MonteCarloBannerEngine.DEFAULT_TRIALS, 1L);
        assertThat(elsewhere.probabilityOfFeatured(banner, fresh, 60, 1))
                .isNotEqualTo(first.probabilityOfFeatured(banner, fresh, 60, 1));
    }

    @Test
    @DisplayName("the two engines name their methods, so an answer can say which produced it")
    void eachEngineSaysWhatItIs() {
        assertThat(exact.method()).isEqualTo("markov-exact");
        assertThat(sampled.method()).isEqualTo("monte-carlo");
    }
}
