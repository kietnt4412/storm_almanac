package io.stormalmanac.api.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stormalmanac.api.ResourceNotFoundException;
import io.stormalmanac.api.player.PlayerView.ChangeForYouView;
import io.stormalmanac.api.player.PlayerView.PlanRequest;
import io.stormalmanac.api.player.PlayerView.PlanSideView;
import io.stormalmanac.api.player.PlayerView.SinceResponse;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gamedata.Goal;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.gamedata.diff.Change;
import io.stormalmanac.gamedata.diff.VersionDiff;
import io.stormalmanac.planner.Optimizer;
import io.stormalmanac.planner.Plan;
import io.stormalmanac.planner.SolveRequest;
import io.stormalmanac.player.PlayerProfile;
import io.stormalmanac.player.PlayerStateRepository;
import io.stormalmanac.player.SavedPlan;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * What the sequences published since a reader's last plan changed for them (C3.2).
 *
 * <pre>
 * GET /api/me/profiles/{profile}/since
 * </pre>
 *
 * <p><b>Two fresh solves, not a comparison with the saved plan</b> (ADR 0037,
 * decision 5). The saved plan counted what the reader owned when they asked, so
 * comparing it with today's plan would mix the patch with everything they have
 * farmed, spent and retargeted since. Here the saved request is solved twice
 * against today's state, once on the sequence it was saved on and once on the
 * latest, and with everything but the patch held still the difference is the
 * patch's.
 *
 * <p><b>Read-only.</b> Neither solve is saved; the reader's plan changes when they
 * ask for one. The cost is two solves on the synchronous path — up to four
 * seconds — paid only when there is something new to measure.
 *
 * <p>204 when the profile has never planned: there is nothing to have changed
 * since.
 */
@RestController
public class SinceController {

    private final Optimizer optimizer;
    private final PlayerStateRepository players;
    private final GameDefinitionRepository definitions;
    private final OwnedProfiles owned;
    private final ObjectMapper json;

    public SinceController(
            Optimizer optimizer,
            PlayerStateRepository players,
            GameDefinitionRepository definitions,
            OwnedProfiles owned,
            ObjectMapper json) {
        this.optimizer = optimizer;
        this.players = players;
        this.definitions = definitions;
        this.owned = owned;
        this.json = json;
    }

    @GetMapping("/api/me/profiles/{profile}/since")
    public ResponseEntity<SinceResponse> since(@PathVariable String profile) {
        PlayerProfile owner = owned.require(profile);
        Optional<SavedPlan> found = players.savedPlanOf(owner.id());
        if (found.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        SavedPlan saved = found.get();

        GameDefinition latest = definitions.findLatest(owner.game()).orElseThrow(() ->
                new ResourceNotFoundException("no published version of " + owner.game().value()));

        if (latest.version().sequence() <= saved.gameVersion()) {
            return ResponseEntity.ok(new SinceResponse(
                    profile, owner.game().value(),
                    saved.gameVersion(), latest.version().label(),
                    latest.version().sequence(), latest.version().label(),
                    saved.savedAt(), List.of(), 0, null, null));
        }

        GameDefinition then = definitions.find(owner.game(), saved.gameVersion()).orElseThrow(() ->
                new ResourceNotFoundException("the saved plan was solved on version " + saved.gameVersion()
                        + " of " + owner.game().value() + ", which can no longer be read"));

        PlanRequest request = read(saved.request());
        List<Goal> goals = players.goalsOf(owner.id()).goals();
        Solved onSaved = solve(profile, then, goals, request);
        Solved onLatest = solve(profile, latest, goals, request);

        VersionDiff diff = VersionDiff.between(then, latest);
        PlanConcerns concerns = PlanConcerns.of(
                Stream.of(onSaved.plan(), onLatest.plan()).flatMap(Optional::stream).toList(),
                List.of(then, latest));
        List<ChangeForYouView> changes = concerns.in(diff).stream()
                .map(change -> view(change, change.kind() == Change.Kind.REMOVED ? then : latest))
                .toList();

        return ResponseEntity.ok(new SinceResponse(
                profile, owner.game().value(),
                then.version().sequence(), then.version().label(),
                latest.version().sequence(), latest.version().label(),
                saved.savedAt(), changes, diff.changes().size(),
                onSaved.view(), onLatest.view()));
    }

    /** A plan, or the reason there is none — each side is reported either way. */
    private record Solved(GameDefinition definition, Optional<Plan> plan, String refused) {

        PlanSideView view() {
            return new PlanSideView(
                    definition.version().sequence(),
                    definition.version().label(),
                    plan.map(Plan::totalEnergy).orElse(null),
                    plan.map(Plan::etaDays).orElse(null),
                    refused);
        }
    }

    private Solved solve(String profile, GameDefinition definition, List<Goal> goals, PlanRequest request) {
        if (goals.isEmpty()) {
            return new Solved(definition, Optional.empty(), "no goals are saved, so there is nothing to plan for");
        }
        try {
            return new Solved(definition, Optional.of(optimizer.solve(new SolveRequest(
                    ProfileId.of(profile),
                    definition.version(),
                    goals,
                    request.resolvedObjective(),
                    request.energyPerDay(),
                    request.resolvedHorizonDays(),
                    request.resolvedReach()))), null);
        } catch (Optimizer.InfeasibleGoalException refused) {
            return new Solved(definition, Optional.empty(), refused.getMessage());
        }
    }

    private static ChangeForYouView view(Change change, GameDefinition namedBy) {
        return new ChangeForYouView(
                change.kind().name(),
                change.about(),
                change.slug(),
                PlanConcerns.name(change, namedBy),
                change.detail(),
                change.before(),
                change.after());
    }

    private PlanRequest read(String document) {
        try {
            return json.readValue(document, PlanRequest.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("a saved plan's request could not be read back", e);
        }
    }
}
