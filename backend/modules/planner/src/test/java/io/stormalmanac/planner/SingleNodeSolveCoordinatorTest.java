package io.stormalmanac.planner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.PlanId;
import io.stormalmanac.common.id.ProfileId;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The queue phase 9 replicates, held to the one promise it makes.
 *
 * <p>The optimizer is a stub here on purpose. What is under test is the
 * bookkeeping — one execution per idempotency key, a pollable handle, an honest
 * queue depth — and a real solve would only make those assertions slower and less
 * certain.
 */
class SingleNodeSolveCoordinatorTest {

    private static final ProfileId PROFILE = new ProfileId("p1");
    private static final GameDataVersion VERSION =
            new GameDataVersion(GameId.of("g"), 1, "1.0", Instant.EPOCH, "test");

    private static SolveRequest request() {
        return new SolveRequest(PROFILE, VERSION, List.of(), Objective.LEAST_ENERGY, 60);
    }

    private static Plan plan(int energy) {
        return new Plan(
                PlanId.of("plan-x"),
                PROFILE,
                VERSION,
                Objective.LEAST_ENERGY,
                List.of(),
                List.of(),
                energy,
                0.0,
                new Explanation(Map.of(), List.of(), List.of()),
                Instant.EPOCH);
    }

    /** Counts what it was asked to do, and can be held open on a latch. */
    private static final class CountingOptimizer implements Optimizer {
        private final AtomicInteger solves = new AtomicInteger();
        private final CountDownLatch release;
        private final RuntimeException failure;

        CountingOptimizer() {
            this(new CountDownLatch(0), null);
        }

        CountingOptimizer(CountDownLatch release, RuntimeException failure) {
            this.release = release;
            this.failure = failure;
        }

        @Override
        public Plan solve(SolveRequest request) {
            try {
                if (!release.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("the test never released the solve");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
            solves.incrementAndGet();
            if (failure != null) {
                throw failure;
            }
            return plan(105);
        }
    }

    /** Polls until the plan is there, so a test never races the executor. */
    private static Plan await(SolveCoordinator coordinator, PlanId ticket) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            Optional<Plan> plan = coordinator.poll(ticket);
            if (plan.isPresent()) {
                return plan.get();
            }
            Thread.onSpinWait();
        }
        throw new AssertionError("the solve never completed");
    }

    @Test
    @DisplayName("a submitted solve becomes pollable, and the plan is the optimizer's")
    void submitThenPoll() {
        CountingOptimizer optimizer = new CountingOptimizer();
        SingleNodeSolveCoordinator coordinator = new SingleNodeSolveCoordinator(optimizer);

        PlanId ticket = coordinator.submit(request(), "req-1");

        assertThat(await(coordinator, ticket).totalEnergy()).isEqualTo(105);
        assertThat(optimizer.solves).hasValue(1);
    }

    @Test
    @DisplayName("the same idempotency key runs one solve however many times it is submitted")
    void theSameKeyRunsOnce() {
        CountingOptimizer optimizer = new CountingOptimizer();
        SingleNodeSolveCoordinator coordinator = new SingleNodeSolveCoordinator(optimizer);

        PlanId first = coordinator.submit(request(), "req-1");
        PlanId retry = coordinator.submit(request(), "req-1");
        PlanId again = coordinator.submit(request(), "req-1");

        assertThat(retry).isEqualTo(first);
        assertThat(again).isEqualTo(first);
        await(coordinator, first);
        assertThat(optimizer.solves).hasValue(1);
    }

    @Test
    @DisplayName("submits racing on one key still run one solve, whichever thread wins")
    void concurrentSubmitsOfOneKeyRunOnce() throws Exception {
        int threads = 16;
        CountDownLatch release = new CountDownLatch(1);
        CountingOptimizer optimizer = new CountingOptimizer(release, null);
        SingleNodeSolveCoordinator coordinator = new SingleNodeSolveCoordinator(optimizer);

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch go = new CountDownLatch(1);
        ExecutorService racers = Executors.newFixedThreadPool(threads);
        try {
            List<java.util.concurrent.Future<PlanId>> tickets = new java.util.ArrayList<>();
            for (int i = 0; i < threads; i++) {
                tickets.add(racers.submit(() -> {
                    ready.countDown();
                    go.await();
                    return coordinator.submit(request(), "req-shared");
                }));
            }
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            go.countDown();

            PlanId expected = tickets.get(0).get(5, TimeUnit.SECONDS);
            for (var ticket : tickets) {
                assertThat(ticket.get(5, TimeUnit.SECONDS)).isEqualTo(expected);
            }
            // Every submit has returned and the solve is still held, so the queue
            // depth is one rather than sixteen.
            assertThat(coordinator.pending()).isEqualTo(1);

            release.countDown();
            await(coordinator, expected);
            assertThat(optimizer.solves).hasValue(1);
            assertThat(coordinator.pending()).isZero();
        } finally {
            racers.shutdownNow();
        }
    }

    @Test
    @DisplayName("different idempotency keys are different jobs, even for the same question")
    void differentKeysAreDifferentJobs() {
        CountingOptimizer optimizer = new CountingOptimizer();
        SingleNodeSolveCoordinator coordinator = new SingleNodeSolveCoordinator(optimizer);

        PlanId one = coordinator.submit(request(), "req-1");
        PlanId two = coordinator.submit(request(), "req-2");

        assertThat(two).isNotEqualTo(one);
        // Both plans carry the same PlanId, because it is the same plan. The
        // tickets differ because the jobs do. Collapsing these two into one solve
        // is the cache's job, not the coordinator's.
        assertThat(await(coordinator, one).id()).isEqualTo(await(coordinator, two).id());
        assertThat(optimizer.solves).hasValue(2);
    }

    @Test
    @DisplayName("an unfinished solve polls empty, and so does a ticket nobody submitted")
    void pollingBeforeThereIsAnAnswer() {
        CountDownLatch release = new CountDownLatch(1);
        SingleNodeSolveCoordinator coordinator =
                new SingleNodeSolveCoordinator(new CountingOptimizer(release, null));

        PlanId ticket = coordinator.submit(request(), "req-1");

        assertThat(coordinator.poll(ticket)).isEmpty();
        assertThat(coordinator.pending()).isEqualTo(1);
        assertThat(coordinator.poll(PlanId.of("job-nobody-submitted"))).isEmpty();

        release.countDown();
        assertThat(await(coordinator, ticket).totalEnergy()).isEqualTo(105);
        assertThat(coordinator.pending()).isZero();
    }

    @Test
    @DisplayName("a solve that threw surfaces on poll instead of looking like a slow one")
    void aFailedSolveIsNotASlowSolve() {
        SingleNodeSolveCoordinator coordinator = new SingleNodeSolveCoordinator(
                new CountingOptimizer(
                        new CountDownLatch(0),
                        new Optimizer.InfeasibleGoalException("stage s-gone expired")));

        PlanId ticket = coordinator.submit(request(), "req-1");

        // The job finishes either way; what differs is what poll does with it.
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (coordinator.pending() > 0 && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertThatThrownBy(() -> coordinator.poll(ticket))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(ticket.value())
                .hasRootCauseMessage("stage s-gone expired");
    }

    @Test
    @DisplayName("a missing idempotency key is refused, because the default would be a duplicate solve")
    void anAbsentKeyIsRefused() {
        SingleNodeSolveCoordinator coordinator =
                new SingleNodeSolveCoordinator(new CountingOptimizer());

        assertThatThrownBy(() -> coordinator.submit(request(), "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idempotency key");
        assertThatThrownBy(() -> coordinator.submit(request(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
