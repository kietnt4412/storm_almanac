package io.stormalmanac.gamedata;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.GameId;
import java.util.List;
import java.util.Optional;

/**
 * Read access to published game data. Nothing outside this module reads the
 * gamedata schema; everything goes through here.
 *
 * <p>Ingestion is automated and publishing is a deliberate manual approval, so
 * "latest" means "latest approved", never "latest fetched".
 */
public interface GameDefinitionRepository {

    Optional<GameDefinition> findLatest(GameId game);

    /**
     * One specific published version, by its sequence.
     *
     * <p>Keyed on the sequence rather than on a {@link GameDataVersion}, because
     * a caller asking for version 3 does not know that version's label,
     * approval time or attribution — those are answers, not questions. Every
     * caller that took the old signature had to fabricate a record with dummy
     * fields to get past it, which is a port shaped for the implementation
     * rather than for its callers.
     */
    Optional<GameDefinition> find(GameId game, long sequence);

    List<GameDataVersion> versions(GameId game);

    /**
     * Every game with something published, with its newest published version.
     *
     * <p>Added for phase 4, and the reason is worth stating: until something had
     * to <em>navigate</em> the catalog, every read started from a game slug the
     * caller already had — a test, a CLI argument, a player's profile. A reader
     * arriving with no account and no slug had nowhere to start, which made the
     * public half of the catalog unreachable except by guessing a URL.
     *
     * <p>A game with only drafts does not appear. That is the same rule as every
     * other method here: a draft has been fetched, not approved, and listing one
     * would advertise data nobody has agreed to publish.
     */
    List<PublishedGame> publishedGames();

    /**
     * @param version the newest published snapshot, which is what a reader lands
     *                on. It carries its own attribution, so an index can credit
     *                a source without a second round trip per game
     */
    record PublishedGame(Game game, GameDataVersion version) {}
}
