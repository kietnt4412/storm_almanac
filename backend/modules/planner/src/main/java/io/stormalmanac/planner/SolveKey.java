package io.stormalmanac.planner;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gamedata.Goal;
import io.stormalmanac.player.Inventory;
import io.stormalmanac.player.Roster;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * The fingerprint of a solve.
 *
 * <p>A solve is a pure function of the game version, the goal set, the
 * objective, the inventory and the roster. Hash that tuple and two things follow
 * for free: the same question asked twice gets the same answer, and the answer
 * can be cached under this key without a cache invalidation strategy — a patch
 * publishes a new version, which is a different key, so a stale plan is not
 * reachable rather than merely refreshed.
 *
 * <p>The digest is fed a canonical rendering, not {@code hashCode}. Map and set
 * iteration order is not part of a solve's meaning, and {@code Object.hashCode}
 * is not stable across JVM runs, which would make a cache miss every restart and
 * a nondeterministic {@link io.stormalmanac.common.id.PlanId} every solve.
 *
 * <p>Drop estimates are deliberately <em>not</em> in the key yet. Nothing
 * publishes them, so folding an empty repository into the fingerprint would be
 * ceremony. The moment estimates move — phase 6 — they belong here, and a solve
 * cached against yesterday's rates is exactly the bug this class exists to make
 * impossible.
 */
public final class SolveKey {

    private SolveKey() {}

    public static String of(
            GameDataVersion version, SolveRequest request, Inventory inventory, Roster roster) {

        StringBuilder canonical = new StringBuilder()
                .append("game=").append(version.game().value())
                .append("\nsequence=").append(version.sequence())
                .append("\nobjective=").append(request.objective())
                .append("\nenergyPerDay=").append(request.energyPerDay());

        canonical.append("\ngoals=");
        List<Goal> goals = request.goals().stream()
                .sorted(Comparator.comparing((Goal g) -> g.entity().value())
                        .thenComparing(Goal::targetState))
                .toList();
        for (Goal goal : goals) {
            canonical.append(goal.entity().value())
                    .append(':').append(goal.targetState())
                    .append(':').append(goal.satisfiability())
                    .append(',');
        }

        canonical.append("\ninventory=");
        inventory.quantities().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ItemId::value)))
                .forEach(e -> canonical.append(e.getKey().value())
                        .append('=').append(e.getValue()).append(','));

        canonical.append("\nroster=");
        roster.currentState().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(EntityId::value)))
                .forEach(e -> canonical.append(e.getKey().value())
                        .append('=').append(e.getValue()).append(','));

        return digest(canonical.toString());
    }

    private static String digest(String canonical) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8));
            // Sixteen hex characters is 64 bits: short enough to read in a URL,
            // long enough that a collision is not a thing that happens to a
            // cache holding plans.
            return HexFormat.of().formatHex(hash, 0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required of every JVM", e);
        }
    }
}
