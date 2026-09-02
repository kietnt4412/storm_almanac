/**
 * <b>almanac-raft</b> — consensus, with a reason to exist. Track B, phase 8.
 *
 * <p>Not started. The problem is real and predates the implementation: the
 * solver fleet needs work assigned exactly once, and a node dying mid-solve
 * must have its job reassigned rather than lost or run twice. That is a
 * replicated log with a leader.
 *
 * <pre>
 * consensus   leader election, log replication, commit index,
 *             term safety, log matching
 * durability  snapshotting, log compaction, restart recovery
 * membership  joint-consensus config change; add/remove a node live
 * applied to  SolveCoordinator
 *               replicated job log (submit -&gt; assign -&gt; complete)
 *               leader assigns work, followers execute
 *               lease-based liveness; reassign on expiry
 *               exactly-once completion via idempotency keys
 *             and a replicated KV mirroring the Redis solve cache
 * </pre>
 *
 * <p>Exposed first as the replicated KV, so it can be tested in isolation
 * before anything depends on it.
 *
 * <p>Deliberately skipped, and written down rather than silently omitted:
 * pre-vote, leader leases for reads, and multi-raft groups. Knowing exactly
 * what an implementation does not do is the mark of having read the paper.
 *
 * @see io.stormalmanac.planner.SolveCoordinator
 */
package io.stormalmanac.raft;
