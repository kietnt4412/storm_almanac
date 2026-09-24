package io.stormalmanac.api.player;

import io.stormalmanac.api.ResourceNotFoundException;
import io.stormalmanac.api.player.ShortfallView.ShortfallResponse;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.gamedata.Goal;
import io.stormalmanac.planner.Demand;
import io.stormalmanac.planner.DemandResolver;
import io.stormalmanac.player.PlayerProfile;
import io.stormalmanac.player.PlayerStateRepository;
import io.stormalmanac.player.Roster;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * What this reader is short of for one entity, at one target state.
 *
 * <pre>
 * GET /api/me/profiles/{profile}/shortfall?entity=warden&amp;target=insight-2  [&amp;game=G] [&amp;version=N]
 * </pre>
 *
 * <p><b>This is phase 4's second exit clause, served.</b> The criterion is that
 * a logged-in character page shows what that reader is short of, and a character
 * page is a catalog page — a route that answers it for any entity and any
 * reachable state, rather than only for goals the reader has already saved, is
 * what lets the overlay sit on a page somebody arrived at from search.
 *
 * <p><b>It asks the resolver rather than letting the page walk the graph.</b>
 * The upgrade steps are already on the wire — {@code /entities/{id}/upgrades}
 * carries every one of them with its costs — so a client could chain them
 * itself, and every client would then own a second copy of rules the server
 * already enforces: that a state the graph cannot reach is refused by name
 * rather than approximated, that a state already behind the player costs
 * nothing, and that a probabilistic goal has no scalar cost at all. Two
 * implementations of a traversal is one implementation too many, and the one in
 * the browser is the one nobody tests.
 *
 * <p><b>Not the same question as a plan, and deliberately a separate route.</b>
 * A plan answers "what should I do, given everything I want" and costs a solve;
 * this answers "what does this one thing still cost me", needs no solver, and is
 * cheap enough to fire on every catalog page. Folding it into
 * {@code POST .../plan} would make browsing the catalog run the optimizer.
 *
 * <p>Read-only, so it is a GET with the entity and the target in the query
 * string, which also makes a shortfall a link a player can send to somebody.
 */
@RestController
public class ShortfallController {

    /** Stateless and dependency-free; a field rather than a bean for that reason. */
    private final DemandResolver resolver = new DemandResolver();

    private final PlayerStateRepository players;
    private final GameDefinitionRepository definitions;
    private final OwnedProfiles owned;

    public ShortfallController(
            PlayerStateRepository players, GameDefinitionRepository definitions, OwnedProfiles owned) {
        this.players = players;
        this.definitions = definitions;
        this.owned = owned;
    }

    @GetMapping("/api/me/profiles/{profile}/shortfall")
    public ShortfallResponse shortfall(
            @PathVariable String profile,
            @RequestParam String entity,
            @RequestParam String target,
            @RequestParam(required = false) String game,
            @RequestParam(required = false) Long version) {

        PlayerProfile owner = owned.require(profile);
        if (target == null || target.isBlank()) {
            throw new IllegalArgumentException("target is required: a shortfall is measured against a state");
        }
        // An entity id means something only inside one game, so without this a
        // character page that sent the wrong profile was answered about an
        // entity of the same name in another game, or with whatever loading
        // that game's version threw: on 2026-09-24 a PGR page holding an R1999
        // profile got R1999's parse error. The page says which game it is
        // showing, and a mismatch is refused by name before anything is loaded.
        // Optional, so a shortfall link sent before this existed still answers.
        if (game != null && !game.equals(owner.game().value())) {
            throw new IllegalArgumentException("profile " + owner.id().value() + " plays " + owner.game().value()
                    + ", not " + game + ": ask with a profile for " + game);
        }

        GameDefinition definition = (version == null
                        ? definitions.findLatest(owner.game())
                        : definitions.find(owner.game(), version))
                .orElseThrow(() -> new ResourceNotFoundException(version == null
                        ? "no published version of " + owner.game().value()
                        : "no published version " + version + " of " + owner.game().value()));

        EntityId id = EntityId.of(entity);
        Roster roster = players.rosterOf(owner.id());

        // Priority is meaningless for a single goal and the resolver ignores it
        // anyway; DETERMINISTIC because a probabilistic target has no scalar
        // cost, and the resolver says so by name rather than guessing one.
        Demand demand = resolver.resolve(
                definition, roster, List.of(new Goal(id, target, Goal.Satisfiability.DETERMINISTIC, 0)));

        return ShortfallResponse.of(
                owner.id().value(),
                definition,
                entity,
                roster.statesOf(id).stream().sorted().toList(),
                target,
                demand,
                players.inventoryOf(owner.id()));
    }
}
