package io.stormalmanac.gacha;

import io.stormalmanac.common.id.ProfileId;
import java.time.LocalDate;

/**
 * How much pull currency an account will have accrued by a date.
 *
 * <p>Pairing this with the simulator is the feature nobody ships well, and it
 * is the question players actually ask: "I have this much, she arrives in 40
 * days, what is my probability of guaranteeing her?" For Punishing: Gray Raven
 * that answer has to carry the 70% featured split and the 120-pull worst case,
 * which is exactly the nuance existing calculators get wrong.
 */
public interface IncomeModel {

    /** Pulls affordable by {@code date}, counting current balance plus accrual. */
    int projectedPulls(ProfileId profile, LocalDate date);
}
