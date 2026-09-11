package io.stormalmanac.api.gamedata;

import io.stormalmanac.api.gamedata.GameDataView.DiffResponse;
import io.stormalmanac.api.gamedata.GameDataView.EntitiesResponse;
import io.stormalmanac.api.gamedata.GameDataView.EntityResponse;
import io.stormalmanac.api.gamedata.GameDataView.ItemsResponse;
import io.stormalmanac.api.gamedata.GameDataView.UpgradesResponse;
import io.stormalmanac.api.gamedata.GameDataView.VersionsResponse;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.GameId;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The catalog and progression API: phase 1's exit criterion, served.
 *
 * <pre>
 * GET /api/games/{game}/versions
 * GET /api/games/{game}/entities                    [?version=N]
 * GET /api/games/{game}/items                       [?version=N]
 * GET /api/games/{game}/entities/{entity}           [?version=N]
 * GET /api/games/{game}/entities/{entity}/upgrades  [?version=N]
 * GET /api/games/{game}/diff?from=N&amp;to=M
 * </pre>
 *
 * <p><b>Read-only, and anonymous on purpose.</b> These routes serve published
 * game data — the same numbers {@code gamedata-cli} prints, attributed, that a
 * stranger arriving from a search should be able to read without an account.
 * They are added to {@code SecurityConfig}'s public matcher deliberately, one
 * prefix, rather than by loosening the deny-by-default rule; nothing here
 * touches a player's data, and nothing here writes. Publishing stays a human
 * approval through the CLI and gets no endpoint.
 *
 * <p><b>Versions are addressable everywhere.</b> {@code ?version=N} pins a read
 * to one published snapshot and its absence means the latest approved one. That
 * is not a convenience: a plan computed against 1.0 has to still be explainable
 * after 1.1 lands, and a URL that cannot say which patch it describes cannot be
 * cited in a bug report either.
 *
 * <p>The slug in the path is the upstream's own — {@code proving-ground},
 * {@code warden} — which is why identifiers were specified as URL-safe slugs
 * rather than database ids from the beginning.
 */
@RestController
@RequestMapping("/api/games/{game}")
public class GameDataController {

    private final GameDataReadModel readModel;

    public GameDataController(GameDataReadModel readModel) {
        this.readModel = readModel;
    }

    @GetMapping("/versions")
    public VersionsResponse versions(@PathVariable String game) {
        return readModel.versions(new GameId(game));
    }

    @GetMapping("/entities")
    public EntitiesResponse entities(@PathVariable String game, @RequestParam(required = false) Long version) {
        return readModel.entities(new GameId(game), version);
    }

    /** "What is in the bag?" — the list an inventory is entered against. */
    @GetMapping("/items")
    public ItemsResponse items(@PathVariable String game, @RequestParam(required = false) Long version) {
        return readModel.items(new GameId(game), version);
    }

    /** "What does her S2 do at rank 3?" — one half of the phase's exit criterion. */
    @GetMapping("/entities/{entity}")
    public EntityResponse entity(
            @PathVariable String game,
            @PathVariable String entity,
            @RequestParam(required = false) Long version) {
        return readModel.entity(new GameId(game), version, new EntityId(entity));
    }

    /** "What does Insight 2 cost?" — the other half. */
    @GetMapping("/entities/{entity}/upgrades")
    public UpgradesResponse upgrades(
            @PathVariable String game,
            @PathVariable String entity,
            @RequestParam(required = false) Long version) {
        return readModel.upgrades(new GameId(game), version, new EntityId(entity));
    }

    /**
     * The patch diff, as structured changes.
     *
     * <p>{@code from} and {@code to} are required and unordered — any two
     * published versions can be compared, not only adjacent ones, which is what
     * storing a version as a full snapshot buys.
     */
    @GetMapping(value = "/diff", produces = MediaType.APPLICATION_JSON_VALUE)
    public DiffResponse diff(
            @PathVariable String game, @RequestParam long from, @RequestParam long to) {
        return GameDataReadModel.asResponse(readModel.diff(new GameId(game), from, to));
    }

    /**
     * The same diff as the report a human reads.
     *
     * <p>Content negotiation rather than a second path, because it is the same
     * resource: {@code Accept: text/plain} gets the exact text
     * {@code gamedata-cli diff} prints, which is what goes into a release note.
     * One renderer, so the report a reviewer approved and the report a reader
     * sees cannot drift apart.
     */
    @GetMapping(value = "/diff", produces = MediaType.TEXT_PLAIN_VALUE)
    public String diffReport(
            @PathVariable String game, @RequestParam long from, @RequestParam long to) {
        return readModel.diff(new GameId(game), from, to).render();
    }
}
