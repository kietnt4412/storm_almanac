package io.stormalmanac.api.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stormalmanac.api.ConflictException;
import io.stormalmanac.api.player.PlayerView.DoneRequest;
import io.stormalmanac.api.player.PlayerView.PlanRequest;
import io.stormalmanac.api.player.PlayerView.PlanResponse;
import io.stormalmanac.api.player.PlayerView.SavedPlanResponse;
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
import io.stormalmanac.player.SavedPlan;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 3's exit criterion, served: a plan computed from stored state.
 *
 * <pre>
 * POST /api/me/profiles/{profile}/plan  [?version=N]
 * GET  /api/me/profiles/{profile}/plan
 * PUT  /api/me/profiles/{profile}/plan/done
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
 *
 * <p><b>Every plan answered is saved, and {@code GET} reads it back</b> (C3.1,
 * ADR 0037). One per profile, replaced by the next: the reader's last plan is the
 * one a returning reader is shown, and its sequence is what C3 measures "what has
 * changed since" from. A refusal saves nothing and leaves the last plan standing.
 */
@RestController
public class PlanController {

    private final Optimizer optimizer;
    private final PlayerStateRepository players;
    private final GameDefinitionRepository definitions;
    private final OwnedProfiles owned;
    private final ObjectMapper json;

    public PlanController(
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

        PlanResponse answer = PlanResponse.of(optimizer.solve(solve), definition);
        // To the microsecond, which is what Postgres keeps: the timestamp is what a
        // tick names its plan by, and it has to read back equal to itself.
        Instant savedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        players.savePlan(new SavedPlan(
                owner.id(), gameVersion.sequence(), write(body.resolved()), write(answer), savedAt));
        return answer;
    }

    /**
     * The last plan this profile was shown, or 204 when it has never run one —
     * "no plan yet" is an answer for a returning reader, not a missing resource.
     */
    @GetMapping("/api/me/profiles/{profile}/plan")
    public ResponseEntity<SavedPlanResponse> saved(@PathVariable String profile) {
        PlayerProfile owner = owned.require(profile);
        return players.savedPlanOf(owner.id())
                .map(saved -> ResponseEntity.ok(new SavedPlanResponse(
                        read(saved.request(), PlanRequest.class),
                        read(saved.plan(), PlanResponse.class),
                        saved.savedAt(),
                        saved.done())))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * What the reader has ticked off on their saved plan, replaced whole (V20).
     *
     * <p><b>Named by the plan it is for.</b> A tick sent from a device that has
     * not seen the latest re-run is a 409 rather than a write: the new plan's
     * lines are not the old one's, and carrying "done" across would mark work
     * done that the reader never did.
     */
    @PutMapping("/api/me/profiles/{profile}/plan/done")
    public ResponseEntity<Void> done(@PathVariable String profile, @RequestBody DoneRequest request) {
        PlayerProfile owner = owned.require(profile);
        if (request == null || request.savedAt() == null) {
            throw new IllegalArgumentException("savedAt is required: it says which plan the ticks are for");
        }
        List<String> done = request.done() == null ? List.of() : request.done();
        if (done.size() > MAX_DONE) {
            throw new IllegalArgumentException("at most " + MAX_DONE + " lines can be ticked off, got " + done.size());
        }
        for (String key : done) {
            if (key == null || key.isBlank() || key.length() > MAX_KEY_LENGTH) {
                throw new IllegalArgumentException(
                        "a ticked line is named by a non-blank key of at most " + MAX_KEY_LENGTH + " characters");
            }
        }
        if (players.savedPlanOf(owner.id()).isEmpty()) {
            throw new ResourceNotFoundException("profile " + profile + " has no saved plan to tick off");
        }
        if (!players.markDone(owner.id(), request.savedAt(), List.copyOf(new LinkedHashSet<>(done)))) {
            throw new ConflictException("the plan has been worked out again since; tick off the new one");
        }
        return ResponseEntity.noContent().build();
    }

    /** A plan is tens of lines; this only stops a body from being a dump. */
    private static final int MAX_DONE = 500;

    private static final int MAX_KEY_LENGTH = 300;

    private GameDefinition definitionFor(PlayerProfile profile, Long version) {
        return (version == null
                        ? definitions.findLatest(profile.game())
                        : definitions.find(profile.game(), version))
                .orElseThrow(() -> new ResourceNotFoundException(version == null
                                ? "no published version of " + profile.game().value()
                                : "no published version " + version + " of "
                                        + profile.game().value()));
    }

    private String write(Object value) {
        try {
            return json.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("a plan this server built could not be written as JSON", e);
        }
    }

    /**
     * Read with the application's own mapper, which ignores a field it does not
     * know and leaves one it does not find null — so a plan saved by an older
     * build reads under a newer wire shape, the same rule the page's wire types
     * keep by marking new fields optional (ADR 0037).
     */
    private <T> T read(String document, Class<T> type) {
        try {
            return json.readValue(document, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("a saved plan could not be read back as " + type.getSimpleName(), e);
        }
    }
}
