package io.stormalmanac.planner;

import io.stormalmanac.common.id.PlanId;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The boring {@link SolveCoordinator}: one JVM, one queue, no consensus.
 *
 * <p>This is the implementation phase 9 replaces and, per
 * {@code docs/adr/0003-the-honesty-rule.md}, does not delete. It exists now so
 * that the Raft-backed one has something to be compared against and something to
 * be diffed from — a replicated log whose only reference implementation is
 * itself proves nothing.
 *
 * <h2>What "exactly once" means here, and what it does not</h2>
 *
 * <p>Two submissions carrying the same idempotency key run one solve and share
 * one result, however they interleave: the map's {@code computeIfAbsent} is
 * atomic and the job is created inside it, so the loser of the race never
 * reaches the executor. That is the whole guarantee.
 *
 * <p><b>It does not survive a restart, and that is the honest bound of a
 * single-node coordinator rather than a defect to be patched.</b> The queue is
 * heap; a kill loses every in-flight solve and every completed result, and a
 * client retrying afterwards gets a fresh execution under the same key. Making
 * that survivable is not a matter of adding a table — it is the requirement that
 * makes a replicated log the honest answer, which is exactly the argument phase 9
 * exists to make. Writing a durable single-node queue here would answer the
 * question phase 9 is meant to ask.
 *
 * <p><b>The ticket is not the plan's id.</b> {@link #submit} hands back a handle
 * derived from the idempotency key, because a plan's own id is a fingerprint of
 * the player's state and that state has not been read yet when the caller needs
 * an answer. Two clients that submit the same question under different
 * idempotency keys therefore get two tickets, two executions, and two plans
 * carrying the same {@link PlanId} — which is correct, because it is the same
 * plan. Deduplicating those is {@link SolveCache}'s job and not this one's; the
 * second execution finds the first one's answer waiting.
 */
public final class SingleNodeSolveCoordinator implements SolveCoordinator {

    private final Optimizer optimizer;
    private final ExecutorService workers;
    private final Map<String, Job> jobs = new ConcurrentHashMap<>();
    private final Map<PlanId, Job> byTicket = new ConcurrentHashMap<>();

    private record Job(PlanId ticket, CompletableFuture<Plan> result) {}

    /**
     * @param workers where solves run. Virtual threads are the intended pool —
     *                a solve is a long CPU-bound stretch with no blocking, so the
     *                carrier count is what bounds concurrency, and that belongs
     *                to whoever configures the pool rather than to this class
     */
    public SingleNodeSolveCoordinator(Optimizer optimizer, ExecutorService workers) {
        this.optimizer = optimizer;
        this.workers = workers;
    }

    public SingleNodeSolveCoordinator(Optimizer optimizer) {
        this(optimizer, Executors.newVirtualThreadPerTaskExecutor());
    }

    @Override
    public PlanId submit(SolveRequest request, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            // Refused rather than defaulted to a random key. A caller who omits
            // it wants the retry semantics and has not noticed they are asking
            // for the opposite; handing them a unique key every time would make
            // a duplicate solve the silent default.
            throw new IllegalArgumentException("an idempotency key is required, and must not be blank");
        }
        // Built before the lookup so that "did this call create the job" is an
        // identity test rather than a flag smuggled out of the mapping function.
        // The allocation on a duplicate submit is a record and a digest, which is
        // nothing against the solve it is preventing.
        Job fresh = new Job(ticketFor(idempotencyKey), new CompletableFuture<>());
        Job job = jobs.computeIfAbsent(idempotencyKey, key -> {
            // Indexed under the lock so the ticket is pollable the instant any
            // thread can see it. A different map, so there is no re-entrancy.
            byTicket.put(fresh.ticket(), fresh);
            return fresh;
        });
        if (job == fresh) {
            // Handed to the executor outside computeIfAbsent: the mapping
            // function runs under the bin's lock, and a solve that finished
            // inside it would be a task touching this map while the entry it
            // belongs to is still being written.
            start(job, request);
        }
        return job.ticket();
    }

    private void start(Job job, SolveRequest request) {
        workers.execute(() -> {
            try {
                job.result().complete(optimizer.solve(request));
            } catch (RuntimeException | Error e) {
                job.result().completeExceptionally(e);
            }
        });
    }

    /**
     * The plan, once there is one.
     *
     * <p>Empty means "not finished", not "no such job" — an unknown ticket is
     * also empty. The distinction matters to an API edge that wants to 404, and
     * it is not drawn here because a single-node coordinator that has restarted
     * genuinely cannot tell the two apart, and a port that promises a
     * distinction its replicated implementation cannot keep is a port shaped to
     * flatter this one.
     *
     * @throws IllegalStateException if the solve failed, wrapping the cause, so
     *         a failed solve is not silently indistinguishable from a slow one
     */
    @Override
    public Optional<Plan> poll(PlanId id) {
        Job job = byTicket.get(id);
        if (job == null || !job.result().isDone()) {
            return Optional.empty();
        }
        try {
            return Optional.of(job.result().get());
        } catch (ExecutionException e) {
            throw new IllegalStateException("solve " + id.value() + " failed", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted polling " + id.value(), e);
        }
    }

    @Override
    public int pending() {
        return (int) jobs.values().stream().filter(job -> !job.result().isDone()).count();
    }

    /**
     * A stable handle for a client-supplied string.
     *
     * <p>Hashed rather than used verbatim: an idempotency key is whatever the
     * client sent, and the ticket comes back in a URL. Sixteen hex characters for
     * the same reason {@link SolveKey} uses them.
     */
    private static PlanId ticketFor(String idempotencyKey) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(idempotencyKey.getBytes(StandardCharsets.UTF_8));
            return PlanId.of("job-" + HexFormat.of().formatHex(hash, 0, 8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required of every JVM", e);
        }
    }
}
