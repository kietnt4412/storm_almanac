package io.stormalmanac.api.player;

import io.stormalmanac.api.player.PlayerView.PlanRequest;
import io.stormalmanac.api.player.PlayerView.PlanResponse;
import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.api.ResourceNotFoundException;
import io.stormalmanac.planner.Optimizer;
import io.stormalmanac.planner.SolveRequest;
import io.stormalmanac.player.Goals;
import io.stormalmanac.player.PlayerProfile;
import io.stormalmanac.player.PlayerStateRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 3's exit criterion, served: a plan computed from stored state.
 *
 * <pre>
 * POST /api/me/profiles/{profile}/plan  [?version=N]
 * </pre>
 *
 * <p><b>The goals come from the database, not from the body.</b> That is the
 * whole difference between this route and every solve that came before it. Until
 * now every plan in this repository was computed from a goal set a test handed
 * the optimizer; here the goals, the inventory and the roster are all read from
 * what the player saved, and the body carries only the two things that are
 * properties of the sitting rather than of the account — how much energy a day
 * they have, and how long they are willing to take.
 *
 * <p><b>Synchronous, because the budget is a promise it can keep.</b>
 * {@code MipOptimizer} is bounded at two seconds and returns the best it has
 * proven rather than running long, and the p95 over a real patch is under that
 * bound. The queue that would hand back a ticket instead exists
 * ({@code SolveCoordinator}) and is deliberately not wired here — see
 * {@code PlannerConfiguration}.
 *
 * <p><b>{@code ?version=N} pins the patch.</b> Absent means the latest published
 * one, which is what a player wants. Naming one is what makes a plan reproducible
 * after the next patch lands, and it is the same parameter the catalog routes
 * take, for the same reason.
 */
@RestController
public class PlanController {

    private final Optimizer optimizer;
    private final PlayerStateRepository players;
    private final GameDefinitionRepository definitions;
    private final OwnedProfiles owned;

    public PlanController(
            Optimizer optimizer,
            PlayerStateRepository players,
            GameDefinitionRepository definitions,
            OwnedProfiles owned) {
        this.optimizer = optimizer;
        this.players = players;
        this.definitions = definitions;
        this.owned = owned;
    }

    @PostMapping("/api/me/profiles/{profile}/plan")
    public PlanResponse plan(
            @PathVariable String profile,
            @RequestParam(required = false) Long version,
            @RequestBody(required = false) PlanRequest request) {

        PlayerProfile owner = owned.require(profile);
        PlanRequest body = request == null ? new PlanRequest(null, null, null, null) : request;

        if (body.energyPerDay() == null) {
            // Not defaulted. Every other field here has an honest default and
            // this one does not: a player's daily energy is a fact about their
            // account, and guessing it produces a plan that is wrong in days
            // without being wrong in any way the reader can see.
            throw new IllegalArgumentException("energyPerDay is required: it is a fact about the account");
        }

        // The whole definition, not only its version: the plan's shadow prices
        // have to be named, and a demand line can stand for something with no
        // entry in the item table — EXP, or one step at several prices. See
        // PlayerView.ShadowPriceView.
        GameDefinition definition = definitionFor(owner, version);
        GameDataVersion gameVersion = definition.version();

        Goals goals = players.goalsOf(owner.id());
        if (goals.goals().isEmpty()) {
            throw new ResourceNotFoundException(
                    "profile " + profile + " has no goals saved, so there is nothing to plan for");
        }

        SolveRequest solve = new SolveRequest(
                ProfileId.of(profile),
                gameVersion,
                goals.goals(),
                body.resolvedObjective(),
                body.energyPerDay(),
                body.resolvedHorizonDays(),
                body.resolvedReach());

        return PlanResponse.of(optimizer.solve(solve), definition);
    }

    private GameDefinition definitionFor(PlayerProfile profile, Long version) {
        return (version == null
                        ? definitions.findLatest(profile.game())
                        : definitions.find(profile.game(), version))
                .orElseThrow(() -> new ResourceNotFoundException(version == null
                                ? "no published version of " + profile.game().value()
                                : "no published version " + version + " of "
                                        + profile.game().value()));
    }
}
