package io.stormalmanac.api.gamedata;

import io.stormalmanac.api.gamedata.GameDataView.GamesResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The one route in the catalog that is not about a particular game.
 *
 * <pre>
 * GET /api/games
 * </pre>
 *
 * <p>Its own class because {@link GameDataController} is mapped at
 * {@code /api/games/&#123;game&#125;} and every method on it is therefore inside
 * one game. The index is the step before that, and a controller whose class-level
 * mapping had to be widened to hold it would make every route under it look
 * optional.
 *
 * <p><b>It exists because phase 4 needs a way in.</b> Every read before this
 * started from a slug the caller already had — a test's constant, a CLI argument,
 * a player's profile. A stranger arriving with no account had to guess a URL,
 * which made the public half of the catalog unreachable in practice. Anonymous,
 * like the rest of the published data.
 */
@RestController
public class GameIndexController {

    private final GameDataReadModel readModel;

    public GameIndexController(GameDataReadModel readModel) {
        this.readModel = readModel;
    }

    @GetMapping("/api/games")
    public GamesResponse games() {
        return readModel.games();
    }
}
