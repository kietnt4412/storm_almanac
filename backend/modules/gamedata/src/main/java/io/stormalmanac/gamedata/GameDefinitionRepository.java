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
}
