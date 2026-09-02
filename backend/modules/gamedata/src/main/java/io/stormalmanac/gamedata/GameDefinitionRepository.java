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

    Optional<GameDefinition> find(GameId game, GameDataVersion version);

    List<GameDataVersion> versions(GameId game);
}
