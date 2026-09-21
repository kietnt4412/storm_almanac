package io.stormalmanac.planner;

import static io.stormalmanac.planner.TestGame.GOLD;
import static io.stormalmanac.planner.TestGame.GOLD_STAGE;
import static io.stormalmanac.planner.TestGame.HERO;
import static io.stormalmanac.planner.TestGame.INGOT;
import static io.stormalmanac.planner.TestGame.ORE;
import static io.stormalmanac.planner.TestGame.ORE_STAGE;
import static io.stormalmanac.planner.TestGame.RELIC;
import static io.stormalmanac.planner.TestGame.RELIC_STAGE;
import static io.stormalmanac.planner.TestGame.stack;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gamedata.Availability;
import io.stormalmanac.gamedata.Fodder;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.Progress;
import io.stormalmanac.gamedata.Rarity;
import io.stormalmanac.gamedata.Reward;
import io.stormalmanac.gamedata.Shop;
import io.stormalmanac.gamedata.Sink;
import io.stormalmanac.gamedata.Upgrade;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The mixed-integer program, on a game small enough to check on paper.
 *
 * <p>Every expected number below is worked out in its own comment. A solver test
 * that only asserts "some plan came back" would pass against a solver that had
 * quietly stopped solving.
 */
class EnergyMipTest {

    private static final Instant NOW = Instant.parse("2026-09-07T00:00:00Z");

    /**
     * s-ore   · 10 energy · 2.0 ore per run
     * s-gold  ·  5 energy · 100 gold per run
     * smelt   · 3 ore + 50 gold -> 1 ingot
     * forge   · 2 ingot + 100 gold -> 1 relic
     */
    private static GameDefinition workshop(Sink... sinks) {
        TestGame game = TestGame.builder()
                .stage(ORE_STAGE, 10, ORE, 2.0)
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .craft("smelt", List.of(stack(ORE, 3), stack(GOLD, 50)), List.of(stack(INGOT, 1)))
                .craft("forge", List.of(stack(INGOT, 2), stack(GOLD, 100)), List.of(stack(RELIC, 1)));
        for (Sink sink : sinks) game.sink(sink);
        return game.build();
    }

    /**
     * A horizon and a rate wide enough that neither binds.
     *
     * <p>Every test below that predates the time axis asserts an energy total
     * worked out on paper, and those totals are still the answer — but only
     * because a month at a thousand a day is far more than any of them spends.
     * The tests that are <em>about</em> the calendar name their own numbers.
     */
    private static final int UNCONSTRAINED_RATE = 1000;
    private static final int UNCONSTRAINED_HORIZON = 30;

    private static EnergyMip.Outcome solve(
            GameDefinition definition, Map<ItemId, Integer> inventory, Map<ItemId, Integer> demand) {
        return solve(definition, inventory, demand, Objective.LEAST_ENERGY,
                UNCONSTRAINED_RATE, UNCONSTRAINED_HORIZON);
    }

    private static EnergyMip.Outcome solve(
            GameDefinition definition,
            Map<ItemId, Integer> inventory,
            Map<ItemId, Integer> demand,
            Objective objective,
            int energyPerDay,
            int horizonDays) {
        return solve(definition, inventory, demand, objective, energyPerDay, horizonDays, Map.of());
    }

    /** The same solve from a reader who has said what they reach (ADR 0022). */
    private static EnergyMip.Outcome solve(
            GameDefinition definition,
            Map<ItemId, Integer> inventory,
            Map<ItemId, Integer> demand,
            Objective objective,
            int energyPerDay,
            int horizonDays,
            Map<String, Integer> reach) {
        return EnergyMip.solve(new EnergyMip.Inputs(
                definition, YieldTable.declared(definition), inventory, demand, NOW, 2000L,
                objective, energyPerDay, horizonDays, reach));
    }

    @Test
    @DisplayName("the cheapest plan for a crafted item farms the inputs and does the craft")
    void solvesOneCraftDeep() {
        // 6 ingots need 18 ore and 300 gold.
        // 18 ore / 2.0 per run = 9 runs of s-ore  = 90 energy
        // 300 gold / 100 per run = 3 runs of s-gold = 15 energy
        EnergyMip.Outcome outcome = solve(workshop(), Map.of(), Map.of(INGOT, 6));

        assertThat(outcome.totalEnergy()).isEqualTo(105);
        assertThat(outcome.stageRuns()).extracting(run -> run.stage().value(), StageRun::runs)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("s-ore", 9),
                        org.assertj.core.groups.Tuple.tuple("s-gold", 3));
        assertThat(outcome.conversions()).containsExactly(new Conversion("smelt", 6));
    }

    @Test
    @DisplayName("a craft chain two deep needs no recursion, only the constraint rows")
    void solvesTwoCraftsDeep() {
        // 1 relic needs 2 ingot + 100 gold.
        // 2 ingot needs 6 ore + 100 gold.
        // 6 ore / 2.0 = 3 runs of s-ore = 30 energy; 200 gold / 100 = 2 runs of s-gold = 10.
        EnergyMip.Outcome outcome = solve(workshop(), Map.of(), Map.of(RELIC, 1));

        assertThat(outcome.totalEnergy()).isEqualTo(40);
        assertThat(outcome.conversions()).containsExactly(
                new Conversion("forge", 1), new Conversion("smelt", 2));
    }

    @Test
    @DisplayName("what the player already owns is subtracted before anything is farmed")
    void inventoryIsSubtracted() {
        // 6 ingots still need 18 ore and 300 gold; the player holds 10 ore and 250 gold.
        // 8 ore / 2.0 = 4 runs = 40 energy; 50 gold / 100 = 1 run = 5 energy.
        EnergyMip.Outcome outcome = solve(
                workshop(), Map.of(ORE, 10, GOLD, 250), Map.of(INGOT, 6));

        assertThat(outcome.totalEnergy()).isEqualTo(45);
    }

    @Test
    @DisplayName("a fractional run is rounded up into a whole one, which is the point of the MIP")
    void runsAreWholeNumbers() {
        // 0.3 relic per run: 1 / 0.3 = 3.33 runs, and a third of a run buys nothing.
        GameDefinition definition = TestGame.builder()
                .stage(RELIC_STAGE, 30, RELIC, 0.3)
                .build();

        EnergyMip.Outcome outcome = solve(definition, Map.of(), Map.of(RELIC, 1));

        assertThat(outcome.stageRuns()).singleElement()
                .satisfies(run -> assertThat(run.runs()).isEqualTo(4));
        assertThat(outcome.totalEnergy()).isEqualTo(120);
    }

    @Test
    @DisplayName("the cheaper of two stages wins on energy per unit, not on energy per run")
    void picksOnEnergyPerUnit() {
        // s-ore: 10 energy for 2.0 ore  = 5.0 energy per ore.
        // s-gold here drops ore too, at 30 energy for 10.0 ore = 3.0 energy per ore.
        // 20 ore is 2 runs of the dearer-looking stage, 60 energy, against 100.
        GameDefinition definition = TestGame.builder()
                .stage(ORE_STAGE, 10, ORE, 2.0)
                .stage(GOLD_STAGE, 30, ORE, 10.0)
                .build();

        EnergyMip.Outcome outcome = solve(definition, Map.of(), Map.of(ORE, 20));

        assertThat(outcome.totalEnergy()).isEqualTo(60);
        assertThat(outcome.stageRuns()).singleElement()
                .satisfies(run -> assertThat(run.stage()).isEqualTo(GOLD_STAGE));
    }

    @Test
    @DisplayName("an expired event stage makes its drops unreachable, and the message names it")
    void expiredStageIsNamedInTheRefusal() {
        GameDefinition definition = TestGame.builder()
                .stage(ORE_STAGE, 10, ORE, 2.0)
                .stage(RELIC_STAGE, 15,
                        new Availability(Set.of(), null, Instant.parse("2026-08-01T00:00:00Z")),
                        RELIC, 2.0)
                .build();

        assertThatThrownBy(() -> solve(definition, Map.of(), Map.of(RELIC, 1)))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("relic")
                .hasMessageContaining("s-relic")
                .hasMessageContaining("closed or unreleased");
    }

    /** 500 gold buys one relic, three times a week. */
    private static Shop weeklyRelic() {
        return new Shop("weekly-relic", GOLD, 500, new ItemStack(RELIC, 1),
                3, Period.ofDays(7), Availability.ALWAYS);
    }

    @Test
    @DisplayName("a shop is priced in the energy its currency costs, and beats a dearer stage")
    void aPurchaseIsPricedThroughItsCurrency() {
        // One relic two ways:
        //   farm it: s-relic, 30 energy for 1.0 relic = 30.
        //   buy it:  500 gold is 5 runs of s-gold at 5 = 25.
        // The shop costs no energy itself; its currency does, and the solver
        // prices that through the gold row without being told the route exists.
        GameDefinition definition = TestGame.builder()
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .stage(RELIC_STAGE, 30, RELIC, 1.0)
                .source(weeklyRelic())
                .build();

        EnergyMip.Outcome outcome = solve(definition, Map.of(), Map.of(RELIC, 1));

        assertThat(outcome.totalEnergy()).isEqualTo(25);
        assertThat(outcome.conversions()).containsExactly(new Conversion("weekly-relic", 1));
        assertThat(outcome.stageRuns()).singleElement()
                .satisfies(run -> assertThat(run.stage()).isEqualTo(GOLD_STAGE));
        assertThat(outcome.shopVariables()).isEqualTo(1);
    }

    @Test
    @DisplayName("a shop's limit is per period, and the horizon decides how many periods there are")
    void theLimitIsTheCap() {
        // Fourteen days is two whole weeks: 2 * 3 = 6 relics, at 3 000 gold,
        // which is 30 runs of s-gold = 150 energy. A seventh does not exist,
        // however much gold there is.
        GameDefinition definition = TestGame.builder()
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .source(weeklyRelic())
                .build();

        EnergyMip.Outcome six = solve(
                definition, Map.of(), Map.of(RELIC, 6), Objective.LEAST_ENERGY, UNCONSTRAINED_RATE, 14);
        assertThat(six.totalEnergy()).isEqualTo(150);
        assertThat(six.conversions()).containsExactly(new Conversion("weekly-relic", 6));

        assertThatThrownBy(() -> solve(
                definition, Map.of(), Map.of(RELIC, 7), Objective.LEAST_ENERGY, UNCONSTRAINED_RATE, 14))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("1 shop offer(s)")
                .hasMessageContaining("within 14 day(s)");
    }

    @Test
    @DisplayName("a shop that does not reset inside the horizon is refused by name, with the reason")
    void aShopTooSlowForTheHorizonIsNotASource() {
        // Only whole periods count, because nothing says how much of this week's
        // allowance is already spent. Five days holds no whole week.
        GameDefinition definition = TestGame.builder()
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .source(weeklyRelic())
                .build();

        assertThatThrownBy(() -> solve(definition, Map.of(), Map.of(RELIC, 1),
                Objective.LEAST_ENERGY, UNCONSTRAINED_RATE, 5))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("weekly-relic")
                .hasMessageContaining("does not reset inside a 5-day horizon");
    }

    /**
     * Thirty relics ever, the first ten at 100 gold and the other twenty at 200:
     * one stock with a price that rises partway through, written as two offers
     * that never reset.
     */
    private static GameDefinition tieredStock() {
        return TestGame.builder()
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .source(new Shop("first-ten", GOLD, 100, new ItemStack(RELIC, 1),
                        10, null, Availability.ALWAYS))
                .source(new Shop("next-twenty", GOLD, 200, new ItemStack(RELIC, 1),
                        20, null, Availability.ALWAYS))
                .build();
    }

    @Test
    @DisplayName("a stock that never resets is the whole allowance, and the cheap tier is bought out first")
    void aLifetimeStockIsSpentCheapestFirst() {
        // 15 relics: all ten at 100 and five at 200 is 2 000 gold, 20 runs of
        // s-gold at 5 = 100 energy. Nothing tells the solver the order; buying
        // the dear tier first would cost 3 000 gold and it simply is not cheapest.
        EnergyMip.Outcome outcome = solve(tieredStock(), Map.of(), Map.of(RELIC, 15),
                Objective.LEAST_ENERGY, UNCONSTRAINED_RATE, 1);

        assertThat(outcome.totalEnergy()).isEqualTo(100);
        assertThat(outcome.conversions()).containsExactlyInAnyOrder(
                new Conversion("first-ten", 10), new Conversion("next-twenty", 5));
    }

    @Test
    @DisplayName("a stock that never resets does not grow with the horizon: thirty ever is thirty in a year")
    void aLifetimeStockDoesNotRefill() {
        // A weekly limit of 30 would give 30 * 52 in a year. This gives 30, and
        // a thirty-first relic has no source however long the plan runs.
        assertThat(solve(tieredStock(), Map.of(), Map.of(RELIC, 30),
                Objective.LEAST_ENERGY, UNCONSTRAINED_RATE, 1).conversions())
                .containsExactlyInAnyOrder(
                        new Conversion("first-ten", 10), new Conversion("next-twenty", 20));

        assertThatThrownBy(() -> solve(tieredStock(), Map.of(), Map.of(RELIC, 31),
                Objective.LEAST_ENERGY, UNCONSTRAINED_RATE, 365))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class);
    }

    @Test
    @DisplayName("a shop whose currency nothing supplies is refused, and the currency is named")
    void aShopWithNoCurrencySourceNamesTheCurrency() {
        // The relic is on sale; the ingots to pay for it come from nowhere. The
        // useful sentence names the ingot, because that is what the reader has
        // to go and find a source for.
        GameDefinition definition = TestGame.builder()
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .source(new Shop("ingot-exchange", INGOT, 2, new ItemStack(RELIC, 1),
                        0, Period.ofDays(1), Availability.ALWAYS))
                .build();

        assertThatThrownBy(() -> solve(definition, Map.of(), Map.of(RELIC, 1)))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("ingot-exchange")
                .hasMessageContaining("currency \"ingot\"");
    }

    @Test
    @DisplayName("the price buys the whole stack, so a purchase is rounded up in stacks, not units")
    void anUnlimitedOfferIsBoughtInWholeStacks() {
        // 262 gold buys ten relics, no limit. Twenty-five relics is three
        // stacks — two leave five short — so 786 gold, which is 8 runs of s-gold
        // (800 gold) = 40 energy.
        GameDefinition definition = TestGame.builder()
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .source(new Shop("relic-crate", GOLD, 262, new ItemStack(RELIC, 10),
                        0, Period.ofDays(1), Availability.ALWAYS))
                .build();

        EnergyMip.Outcome outcome = solve(definition, Map.of(), Map.of(RELIC, 25));

        assertThat(outcome.conversions()).containsExactly(new Conversion("relic-crate", 3));
        assertThat(outcome.totalEnergy()).isEqualTo(40);
    }

    @Test
    @DisplayName("an item a reward grants is supplied by waiting, and the plan says how many times")
    void rewardIncomeIsSupply() {
        // The weekly quest grants 2 relics and fires 30 / 7 = 4 times in the
        // horizon. One relic is wanted, so one claim covers it and nothing is
        // farmed — the whole point of giving the model a calendar.
        GameDefinition definition = TestGame.builder()
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .source(new Reward("weekly-quest", Reward.Cadence.WEEKLY,
                        List.of(new ItemStack(RELIC, 2)), Availability.ALWAYS))
                .build();

        EnergyMip.Outcome outcome = solve(definition, Map.of(), Map.of(RELIC, 1));

        assertThat(outcome.totalEnergy()).isZero();
        assertThat(outcome.stageRuns()).isEmpty();
        assertThat(outcome.rewardClaims()).containsExactly(new RewardClaim("weekly-quest", 1));
        // A week is a week: no amount of spare energy makes the quest come round sooner.
        assertThat(outcome.daysNeeded()).isEqualTo(7);
    }

    @Test
    @DisplayName("a cadence that does not come round inside the horizon is refused by name")
    void aRewardTooSlowForTheHorizonIsNotASource() {
        GameDefinition definition = TestGame.builder()
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .source(new Reward("weekly-quest", Reward.Cadence.WEEKLY,
                        List.of(new ItemStack(RELIC, 2)), Availability.ALWAYS))
                .build();

        assertThatThrownBy(() -> solve(definition, Map.of(), Map.of(RELIC, 1),
                Objective.LEAST_ENERGY, UNCONSTRAINED_RATE, 5))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("weekly-quest")
                .hasMessageContaining("5-day horizon");
    }

    @Test
    @DisplayName("free income is not claimed for its own sake: the plan reports what it leans on")
    void unusedRewardsAreNotClaimed() {
        // Thirty daily logins are available and one is needed. A plan reporting
        // all thirty would be telling a player it depends on a month of logins
        // when it depends on one day of them.
        GameDefinition definition = TestGame.builder()
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .source(new Reward("daily-login", Reward.Cadence.DAILY,
                        List.of(new ItemStack(RELIC, 1)), Availability.ALWAYS))
                .build();

        EnergyMip.Outcome outcome = solve(definition, Map.of(), Map.of(RELIC, 1));

        assertThat(outcome.rewardClaims()).containsExactly(new RewardClaim("daily-login", 1));
        assertThat(outcome.daysNeeded()).isEqualTo(1);
    }

    // ── Grants paid by how well the reader plays (ADR 0022) ─────────────────

    /**
     * A weekly mode paying by score: 1 relic for showing up, 5 more for clearing
     * it. The tier is a reward of its own standing behind a bar, which is how a
     * ladder of nine tiers is written without the format learning what a ladder
     * is.
     */
    private static GameDefinition scoredWeekly() {
        return TestGame.builder()
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .source(new Reward("cage-bronze", Reward.Cadence.WEEKLY,
                        List.of(new ItemStack(RELIC, 1)), Availability.ALWAYS,
                        new Reward.Requirement("cage-score", 30_000)))
                .source(new Reward("cage-gold", Reward.Cadence.WEEKLY,
                        List.of(new ItemStack(RELIC, 5)), Availability.ALWAYS,
                        new Reward.Requirement("cage-score", 500_000)))
                .build();
    }

    @Test
    @DisplayName("a grant behind a score is counted for the reader who says they reach it")
    void aGrantIsIncomeForAReaderWhoClearsIt() {
        // 500 000 clears both bars, so both tiers pay every week: 6 relics a
        // week, and 4 weeks fit in the horizon.
        EnergyMip.Outcome outcome = solve(scoredWeekly(), Map.of(), Map.of(RELIC, 6),
                Objective.LEAST_ENERGY, UNCONSTRAINED_RATE, UNCONSTRAINED_HORIZON,
                Map.of("cage-score", 500_000));

        assertThat(outcome.totalEnergy()).isZero();
        assertThat(outcome.rewardClaims()).containsExactlyInAnyOrder(
                new RewardClaim("cage-bronze", 1), new RewardClaim("cage-gold", 1));
        assertThat(outcome.withheldGrants()).isEmpty();
    }

    @Test
    @DisplayName("the tiers above what the reader reaches are not income, and the plan names them")
    void aGrantAboveTheReaderIsWithheldAndReported() {
        // 30 000 clears the bronze bar only. One relic a week, four weeks, so
        // six relics are not reachable at all — and the refusal has to say why,
        // because "no source yields it" would be a lie about a mode the reader
        // plays every week.
        EnergyMip.Outcome outcome = solve(scoredWeekly(), Map.of(), Map.of(RELIC, 4),
                Objective.LEAST_ENERGY, UNCONSTRAINED_RATE, UNCONSTRAINED_HORIZON,
                Map.of("cage-score", 30_000));

        assertThat(outcome.rewardClaims()).containsExactly(new RewardClaim("cage-bronze", 4));
        assertThat(outcome.withheldGrants()).containsExactly(
                new EnergyMip.Withheld("cage-gold", "cage-score", 500_000, 30_000));
    }

    @Test
    @DisplayName("a reader who says nothing collects nothing, which is dearer and never cheaper")
    void silenceCountsNoScoredGrant() {
        EnergyMip.Outcome outcome = solve(scoredWeekly(), Map.of(), Map.of(GOLD, 100));

        assertThat(outcome.rewardClaims()).isEmpty();
        // The relics are not in this goal set, so the withheld grants supply
        // nothing anybody asked for and are not worth a note.
        assertThat(outcome.withheldGrants()).isEmpty();
    }

    @Test
    @DisplayName("an item only a grant out of reach supplies is refused by the bar, not by the cadence")
    void theRefusalNamesTheBarAndWhatTheReaderSaid() {
        assertThatThrownBy(() -> solve(scoredWeekly(), Map.of(), Map.of(RELIC, 1),
                Objective.LEAST_ENERGY, UNCONSTRAINED_RATE, UNCONSTRAINED_HORIZON, Map.of()))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("cage-bronze")
                .hasMessageContaining("30000")
                .hasMessageContaining("cage-score")
                .hasMessageContaining("reaching 0");
    }

    // ── Grants that run out before the horizon does (ADR 0024) ─────────────

    /** Three days from {@code NOW}: shorter than the weekly cadence below. */
    private static final Instant CLOSES_SOON = Instant.parse("2026-09-10T00:00:00Z");

    /** Fifteen days from {@code NOW}: two weeklies fit, the other two do not. */
    private static final Instant CLOSES_MIDWAY = Instant.parse("2026-09-22T00:00:00Z");

    /**
     * A festival handing out a relic a week until it shuts, next to a stage that
     * sells the same relic for energy. Which of the two a plan leans on is the
     * whole question, and the date is the only thing that decides it.
     */
    private static GameDefinition festival(Instant closesAt) {
        return TestGame.builder()
                .stage(RELIC_STAGE, 40, RELIC, 1.0)
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .source(new Reward("festival-weekly", Reward.Cadence.WEEKLY,
                        List.of(new ItemStack(RELIC, 1)),
                        new Availability(Set.of(), null, closesAt)))
                .build();
    }

    @Test
    @DisplayName("a claim the plan leans on carries the date it stops being collectable")
    void anExpiringClaimIsReportedWithItsDeadline() {
        // 15 days of a weekly is 2 claims, not 4: the window truncates the
        // cadence, which already worked. The remaining 2 relics cost 40 each.
        EnergyMip.Outcome outcome = solve(festival(CLOSES_MIDWAY), Map.of(), Map.of(RELIC, 4),
                Objective.LEAST_ENERGY, UNCONSTRAINED_RATE, UNCONSTRAINED_HORIZON);

        assertThat(outcome.rewardClaims()).containsExactly(new RewardClaim("festival-weekly", 2));
        assertThat(outcome.totalEnergy()).isEqualTo(80);
        assertThat(outcome.expiringClaims()).containsExactly(
                new EnergyMip.Expiring("festival-weekly", 2, CLOSES_MIDWAY, 15));
        assertThat(outcome.lapsedGrants()).isEmpty();
    }

    @Test
    @DisplayName("a grant that shuts before it pays once is reported, not silently dropped")
    void aLapsedGrantIsNamedRatherThanVanishing() {
        // Three days of a weekly is no claims at all, so claimable() removes it
        // and the plan pays 40 energy a relic with nothing to say for itself —
        // which is the silence ADR 0024 is about. A 30-day horizon would have
        // allowed 4 of them.
        EnergyMip.Outcome outcome = solve(festival(CLOSES_SOON), Map.of(), Map.of(RELIC, 2),
                Objective.LEAST_ENERGY, UNCONSTRAINED_RATE, UNCONSTRAINED_HORIZON);

        assertThat(outcome.rewardClaims()).isEmpty();
        assertThat(outcome.totalEnergy()).isEqualTo(80);
        assertThat(outcome.expiringClaims()).isEmpty();
        assertThat(outcome.lapsedGrants()).containsExactly(
                new EnergyMip.Lapsed("festival-weekly", CLOSES_SOON, 4));
    }

    @Test
    @DisplayName("a cadence too slow for the horizon is not a lapse, and is not reported as one")
    void aSlowCadenceIsNotADeadline() {
        // A monthly grant in a 7-day horizon pays nothing either, and no date is
        // to blame: its window runs for another year. Reporting this as a lapsed
        // event would be the note telling a reader to hurry over a deadline that
        // does not exist.
        GameDefinition yearLong = TestGame.builder()
                .stage(RELIC_STAGE, 40, RELIC, 1.0)
                .source(new Reward("monthly-gift", Reward.Cadence.MONTHLY,
                        List.of(new ItemStack(RELIC, 1)),
                        new Availability(Set.of(), null, Instant.parse("2027-09-07T00:00:00Z"))))
                .build();

        EnergyMip.Outcome outcome = solve(yearLong, Map.of(), Map.of(RELIC, 1),
                Objective.LEAST_ENERGY, UNCONSTRAINED_RATE, 7);

        assertThat(outcome.totalEnergy()).isEqualTo(40);
        assertThat(outcome.lapsedGrants()).isEmpty();
    }

    @Test
    @DisplayName("an expiring grant nothing in the goal set needs gets no deadline")
    void anUnclaimedExpiringGrantIsNotADeadline() {
        // The festival is still live and still closing, and this plan is about
        // gold. A date for something nobody was asked to do is how a note stops
        // being read.
        EnergyMip.Outcome outcome = solve(festival(CLOSES_MIDWAY), Map.of(), Map.of(GOLD, 100),
                Objective.LEAST_ENERGY, UNCONSTRAINED_RATE, UNCONSTRAINED_HORIZON);

        assertThat(outcome.totalEnergy()).isEqualTo(5);
        assertThat(outcome.expiringClaims()).isEmpty();
        assertThat(outcome.lapsedGrants()).isEmpty();
    }

    @Test
    @DisplayName("an item only a closed grant supplies is refused by the window, not by the cadence")
    void theRefusalNamesTheWindowRatherThanTheCadence() {
        // "It does not come round inside a 30-day horizon" is true and sends the
        // reader off to wait for something that is never coming back.
        GameDefinition noStage = TestGame.builder()
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .source(new Reward("festival-weekly", Reward.Cadence.WEEKLY,
                        List.of(new ItemStack(RELIC, 1)),
                        new Availability(Set.of(), null, CLOSES_SOON)))
                .build();

        assertThatThrownBy(() -> solve(noStage, Map.of(), Map.of(RELIC, 1)))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("festival-weekly")
                .hasMessageContaining("closes 2026-09-10T00:00:00Z")
                .hasMessageContaining("the horizon holds 4 of them and the window holds none");
    }

    private static final ItemId HERO_EXP = Demand.progressItem("hero-exp");

    /** The workshop, plus one rule: any 3★-or-better material is worth 100 hero EXP, for 10 gold. */
    private static GameDefinition workshopWithFodder(Sink... sinks) {
        TestGame game = TestGame.builder()
                .stage(ORE_STAGE, 10, ORE, 2.0)
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .craft("smelt", List.of(stack(ORE, 3), stack(GOLD, 50)), List.of(stack(INGOT, 1)))
                .craft("forge", List.of(stack(INGOT, 2), stack(GOLD, 100)), List.of(stack(RELIC, 1)))
                .sink(new Fodder("feed-material", "material", new Rarity("3*", 3), "hero-exp", 100,
                        List.of(stack(GOLD, 10))));
        for (Sink sink : sinks) game.sink(sink);
        return game.build();
    }

    @Test
    @DisplayName("EXP is made by feeding fodder, and the fodder is farmed like anything else")
    void progressIsPaidByFodder() {
        // 250 EXP at 100 a unit is 3 units; ore is 2★, so ingot (3★) and relic
        // (4★) are the eligible ones, and an ingot is the cheaper to make.
        //   3 ingots: 9 ore + 150 gold; feeding them: 30 gold
        //   9 ore / 2.0 = 4.5, so 5 runs of s-ore = 50 energy
        //   180 gold / 100 = 1.8, so 2 runs of s-gold = 10 energy
        EnergyMip.Outcome outcome = solve(workshopWithFodder(), Map.of(), Map.of(HERO_EXP, 250));

        assertThat(outcome.totalEnergy()).isEqualTo(60);
        assertThat(outcome.conversions()).containsExactly(
                new Conversion("feed-material:ingot", 3), new Conversion("smelt", 3));
    }

    @Test
    @DisplayName("fodder the player holds is fed before anything is farmed for it")
    void heldFodderIsFedFirst() {
        // Three relics held are 300 EXP; feeding them costs 30 gold, one run of
        // s-gold at 5. Making ingots instead would need ore, which is dearer.
        EnergyMip.Outcome outcome = solve(
                workshopWithFodder(), Map.of(RELIC, 3), Map.of(HERO_EXP, 250));

        assertThat(outcome.totalEnergy()).isEqualTo(5);
        assertThat(outcome.conversions()).containsExactly(new Conversion("feed-material:relic", 3));
    }

    @Test
    @DisplayName("progress no fodder rule pays is refused by name, and a rule that names no progress pays none")
    void unpaidProgressIsRefused() {
        GameDefinition inert = TestGame.builder()
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .sink(new Fodder("read-too-early", "currency", new Rarity("1*", 1), null, 100, List.of()))
                .build();

        assertThatThrownBy(() -> solve(inert, Map.of(GOLD, 999), Map.of(HERO_EXP, 100)))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("progress:hero-exp")
                .hasMessageContaining("no fodder rule in this game version pays it");
    }

    /** One move at two prices, over the workshop: three ingots, or one relic. */
    private static final Upgrade BY_INGOT =
            new Upgrade("by-ingot", HERO, "start", "resonance", List.of(stack(INGOT, 3)));
    private static final Upgrade BY_RELIC =
            new Upgrade("by-relic", HERO, "start", "resonance", List.of(stack(RELIC, 1)));
    private static final ItemId RESONANCE = Demand.choiceItem(BY_INGOT);

    @Test
    @DisplayName("of a step's several prices, the one cheapest to farm is paid, and the plan names it")
    void theCheapestPriceIsPaid() {
        // By ingot: 3 ingots = 9 ore + 150 gold = 5 + 2 runs = 60 energy.
        // By relic: 2 ingots + 100 gold = 6 ore + 200 gold = 3 + 2 runs = 40.
        GameDefinition game = workshop(BY_INGOT, BY_RELIC);

        EnergyMip.Outcome outcome = solve(game, Map.of(), Map.of(RESONANCE, 1));

        assertThat(outcome.totalEnergy()).isEqualTo(40);
        assertThat(outcome.conversions()).containsExactly(
                new Conversion("by-relic", 1), new Conversion("forge", 1), new Conversion("smelt", 2));
    }

    @Test
    @DisplayName("a price the player already holds is paid before anything is farmed for a cheaper one")
    void aHeldPriceIsPaid() {
        GameDefinition game = workshop(BY_INGOT, BY_RELIC);

        EnergyMip.Outcome outcome = solve(game, Map.of(INGOT, 3), Map.of(RESONANCE, 1));

        assertThat(outcome.totalEnergy()).isZero();
        assertThat(outcome.conversions()).containsExactly(new Conversion("by-ingot", 1));
    }

    @Test
    @DisplayName("a price paid in EXP pulls fodder in, the same as a step with one price")
    void aPriceInProgressIsFed() {
        // By gold: 2 000 gold = 20 runs = 100 energy. By EXP: 250 EXP = 3 ingots
        // fed, which progressIsPaidByFodder prices at 60.
        Upgrade byGold = new Upgrade("by-gold", HERO, "start", "resonance", List.of(stack(GOLD, 2000)));
        Upgrade byExp = new Upgrade("by-exp", HERO, "start", "resonance", List.of(),
                List.of(), List.of(new Progress("hero-exp", 250)));
        GameDefinition game = workshopWithFodder(byGold, byExp);

        EnergyMip.Outcome outcome = solve(game, Map.of(), Map.of(Demand.choiceItem(byGold), 1));

        assertThat(outcome.totalEnergy()).isEqualTo(60);
        assertThat(outcome.conversions()).containsExactly(
                new Conversion("by-exp", 1), new Conversion("feed-material:ingot", 3), new Conversion("smelt", 3));
    }

    @Test
    @DisplayName("a step none of whose prices can be supplied is refused, naming every price")
    void noPayablePriceIsRefused() {
        Upgrade a = new Upgrade("by-moonstone", HERO, "start", "resonance", List.of(stack(ItemId.of("moonstone"), 1)));
        Upgrade b = new Upgrade("by-sunstone", HERO, "start", "resonance", List.of(stack(ItemId.of("sunstone"), 1)));
        GameDefinition game = workshop(a, b);

        assertThatThrownBy(() -> solve(game, Map.of(), Map.of(Demand.choiceItem(a), 1)))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("any one of by-moonstone, by-sunstone");
    }
    @Test
    @DisplayName("an item nothing in the game yields is refused, and says exactly that")
    void unknownItemIsRefused() {
        assertThatThrownBy(() -> solve(workshop(), Map.of(), Map.of(ItemId.of("moonstone"), 1)))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("moonstone")
                .hasMessageContaining("no source in this game version yields it");
    }

    @Test
    @DisplayName("a rotating stage is limited by the energy of the days it is actually open")
    void rotationIsACapacityRatherThanAFilter() {
        // NOW is a Monday. A fourteen-day horizon starting on a Monday contains
        // the Tuesdays at offsets 1 and 8 — two of them — so a Tuesday-only stage
        // gets 2 * 30 = 60 energy, which is six runs and twelve ore.
        assertThat(NOW.atZone(ZoneOffset.UTC).getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        GameDefinition definition = TestGame.builder()
                .stage(ORE_STAGE, 10, new Availability(Set.of(DayOfWeek.TUESDAY), null, null),
                        ORE, 2.0)
                .build();

        EnergyMip.Outcome outcome =
                solve(definition, Map.of(), Map.of(ORE, 12), Objective.LEAST_ENERGY, 30, 14);

        assertThat(outcome.totalEnergy()).isEqualTo(60);
        // Both Tuesdays have to have happened, and the second is nine days out.
        assertThat(outcome.daysNeeded()).isEqualTo(9);
    }

    @Test
    @DisplayName("a rotating stage cannot be farmed past the days the horizon gives it")
    void rotationCanMakeAGoalUnreachableInTime() {
        GameDefinition definition = TestGame.builder()
                .stage(ORE_STAGE, 10, new Availability(Set.of(DayOfWeek.TUESDAY), null, null),
                        ORE, 2.0)
                .build();

        // Fourteen ore is seven runs, seventy energy, and two Tuesdays supply sixty.
        assertThatThrownBy(() -> solve(
                definition, Map.of(), Map.of(ORE, 14), Objective.LEAST_ENERGY, 30, 14))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("within 14 day(s)")
                .hasMessageContaining("30 energy a day");
    }

    @Test
    @DisplayName("two rotations sharing a weekday are capped together, not one at a time")
    void rotationsCompeteForTheSameDays() {
        // A week from Monday holds one Tuesday and one Friday, 100 energy each.
        //   s-ore is Tuesday-only and wants 20 ore = 10 runs = 100 energy.
        //   s-relic is Tuesday-or-Friday and wants 30 relic = 15 runs = 150 energy.
        // Each fits its own days — 100 <= 100 and 150 <= 200 — and together they
        // want 250 out of the 200 the two days between them supply. Checking the
        // groups one at a time would call this a plan.
        GameDefinition definition = rotatingPair();

        assertThatThrownBy(() -> solve(
                definition, Map.of(), Map.of(ORE, 20, RELIC, 30), Objective.LEAST_ENERGY, 100, 7))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class);

        // The same shape with twenty relics is 200 against 200, and is a plan.
        EnergyMip.Outcome outcome = solve(
                definition, Map.of(), Map.of(ORE, 20, RELIC, 20), Objective.LEAST_ENERGY, 100, 7);
        assertThat(outcome.totalEnergy()).isEqualTo(200);
    }

    private static GameDefinition rotatingPair() {
        return TestGame.builder()
                .stage(ORE_STAGE, 10, new Availability(Set.of(DayOfWeek.TUESDAY), null, null),
                        ORE, 2.0)
                .stage(RELIC_STAGE, 10,
                        new Availability(Set.of(DayOfWeek.TUESDAY, DayOfWeek.FRIDAY), null, null),
                        RELIC, 2.0)
                .build();
    }

    @Test
    @DisplayName("fewest days and least energy are different plans once free income accrues")
    void theTwoObjectivesSeparate() {
        // s-gold pays 100 gold for 5 energy; the daily login pays 100 for nothing.
        // At 10 energy a day, a day is worth 2 runs (200 gold) plus the login
        // (100), so 1 000 gold needs ceil(1000 / 300) = 4 days.
        GameDefinition definition = TestGame.builder()
                .stage(GOLD_STAGE, 5, GOLD, 100.0)
                .source(new Reward("daily-login", Reward.Cadence.DAILY,
                        List.of(new ItemStack(GOLD, 100)), Availability.ALWAYS))
                .build();

        EnergyMip.Outcome cheapest =
                solve(definition, Map.of(), Map.of(GOLD, 1000), Objective.LEAST_ENERGY, 10, 30);
        EnergyMip.Outcome fastest =
                solve(definition, Map.of(), Map.of(GOLD, 1000), Objective.FEWEST_DAYS, 10, 30);

        // Waiting is free, so the cheapest plan waits: ten logins, no farming.
        assertThat(cheapest.totalEnergy()).isZero();
        assertThat(cheapest.daysNeeded()).isEqualTo(10);

        // The fastest plan buys the other twenty days with energy.
        assertThat(fastest.horizonUsed()).isEqualTo(4);
        assertThat(fastest.totalEnergy()).isEqualTo(30);
        assertThat(fastest.rewardClaims()).containsExactly(new RewardClaim("daily-login", 4));
    }

    @Test
    @DisplayName("with nothing on a cadence and nothing rotating, the two objectives coincide")
    void theTwoObjectivesCoincideWhenTheDataSaysNothingAboutTime() {
        // Not a property of the model — a property of a game whose data declares
        // no rewards and no rotation, which is every game ingested so far.
        EnergyMip.Outcome cheapest = solve(workshop(), Map.of(), Map.of(INGOT, 6));
        EnergyMip.Outcome fastest = solve(workshop(), Map.of(), Map.of(INGOT, 6),
                Objective.FEWEST_DAYS, UNCONSTRAINED_RATE, UNCONSTRAINED_HORIZON);

        assertThat(fastest.totalEnergy()).isEqualTo(cheapest.totalEnergy()).isEqualTo(105);
        assertThat(fastest.daysNeeded()).isZero();
    }

    @Test
    @DisplayName("a goal that will not fit in the horizon's energy is refused, and says so in days")
    void theHorizonIsAConstraintAndNotADecoration() {
        // 6 ingots cost 105 energy; three days at 20 a day is 60.
        assertThatThrownBy(() -> solve(
                workshop(), Map.of(), Map.of(INGOT, 6), Objective.LEAST_ENERGY, 20, 3))
                .isInstanceOf(Optimizer.InfeasibleGoalException.class)
                .hasMessageContaining("within 3 day(s) at 20 energy a day");
    }

    @Test
    @DisplayName("a demand the inventory already covers costs no runs at all")
    void nothingToFarm() {
        EnergyMip.Outcome outcome = solve(workshop(), Map.of(INGOT, 10), Map.of(INGOT, 6));

        assertThat(outcome.totalEnergy()).isZero();
        assertThat(outcome.stageRuns()).isEmpty();
        assertThat(outcome.conversions()).isEmpty();
    }

    @Test
    @DisplayName("a free conversion loop is not run for its own sake")
    void pointlessConversionsAreNotMade() {
        // ingot -> relic -> ingot at no loss: the tie-break weight is what stops
        // the solver returning an arbitrary number of laps round the cycle.
        GameDefinition definition = TestGame.builder()
                .stage(ORE_STAGE, 10, ORE, 2.0)
                .craft("smelt", List.of(stack(ORE, 3)), List.of(stack(INGOT, 1)))
                .craft("bless", List.of(stack(INGOT, 1)), List.of(stack(RELIC, 1)))
                .craft("melt", List.of(stack(RELIC, 1)), List.of(stack(INGOT, 1)))
                .build();

        EnergyMip.Outcome outcome = solve(definition, Map.of(), Map.of(INGOT, 2));

        assertThat(outcome.totalEnergy()).isEqualTo(30); // 6 ore, 3 runs
        assertThat(outcome.conversions()).containsExactly(new Conversion("smelt", 2));
    }
}
