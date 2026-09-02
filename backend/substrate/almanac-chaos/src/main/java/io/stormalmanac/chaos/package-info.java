/**
 * <b>almanac-chaos</b> — the verification harness. Track B, phase 10.
 *
 * <p>Not started. This is the phase most people skip and the one that makes the
 * rest count: both substrate components are only as credible as the evidence
 * that they are correct.
 *
 * <ul>
 *   <li>Fault injection: partition the network, delay and reorder messages,
 *       pause a process (a GC or hypervisor stall), kill and restart nodes,
 *       corrupt a segment on disk.
 *   <li>Linearizability checking: record concurrent client histories against
 *       the replicated register and verify a valid sequential ordering exists.
 *       Jepsen's approach, at this scale.
 *   <li>Nightly in CI with randomized seeds. A failing seed is saved and
 *       replayed as a permanent regression test.
 * </ul>
 *
 * <p>The first real bug this finds gets written up. It will find one, and that
 * writeup is worth more than the implementation.
 */
package io.stormalmanac.chaos;
