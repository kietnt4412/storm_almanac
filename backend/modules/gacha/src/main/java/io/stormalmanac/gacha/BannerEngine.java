package io.stormalmanac.gacha;

import io.stormalmanac.gamedata.banner.BannerModel;

/**
 * Answers "what are my odds" for one banner.
 *
 * <p>Two implementations, deliberately: an exact Markov chain that treats pity
 * as a state machine, and a Monte Carlo run of 100k trials parallelised across
 * virtual threads. Two independent methods agreeing to three decimals is a
 * correctness argument worth publishing, and the first place they disagree will
 * be a real bug in the pity model rather than in either engine.
 *
 * <p>The published rates for both shipped games are the acceptance fixtures.
 * Reproduce them from the {@link BannerModel} alone or the model is wrong.
 */
public interface BannerEngine {

    /**
     * Probability of at least {@code copies} copies of the featured unit within
     * {@code pulls}, starting from {@code from}.
     */
    double probabilityOfFeatured(BannerModel banner, PityState from, int pulls, int copies);

    /** Expected pulls to the first featured copy. */
    double expectedPullsToFeatured(BannerModel banner, PityState from);

    /** Which method produced the answer, so the UI can show both side by side. */
    String method();
}
