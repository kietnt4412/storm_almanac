package io.stormalmanac.app;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The modular monolith's boundaries, enforced rather than documented.
 *
 * <p>"Modules communicate through internal events, so extraction later is a
 * deployment change rather than a rewrite" is only true while it is checked.
 * The first cross-module reach-through that ships unnoticed is the one that
 * turns the monolith into a ball of mud.
 */
class ModuleBoundaryTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("io.stormalmanac");

    @Test
    @DisplayName("modules depend only on what they are allowed to depend on")
    void layering() {
        Architectures.layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                // The Track B layers are empty until phases 7-10 and that is the
                // point: the gate says no substrate code before the product is
                // deployed. The rules are declared now so they bite the moment
                // the first class lands there, rather than being written from
                // scratch at the exact moment the temptation to skip them peaks.
                .withOptionalLayers(true)

                .layer("common").definedBy("io.stormalmanac.common..")
                .layer("gamedata").definedBy("io.stormalmanac.gamedata..")
                .layer("identity").definedBy("io.stormalmanac.identity..")
                .layer("player").definedBy("io.stormalmanac.player..")
                .layer("stats").definedBy("io.stormalmanac.stats..")
                .layer("planner").definedBy("io.stormalmanac.planner..")
                .layer("gacha").definedBy("io.stormalmanac.gacha..")
                .layer("api").definedBy("io.stormalmanac.api..")
                .layer("adapters").definedBy("io.stormalmanac.adapters..")
                .layer("app").definedBy("io.stormalmanac.app..")
                .layer("store").definedBy("io.stormalmanac.store..")
                .layer("raft").definedBy("io.stormalmanac.raft..")
                .layer("chaos").definedBy("io.stormalmanac.chaos..")

                // The shared kernel is depended on by everyone and depends on no one.
                .whereLayer("common").mayOnlyBeAccessedByLayers(
                        "gamedata", "identity", "player", "stats", "planner", "gacha", "api", "app", "adapters")
                .whereLayer("gamedata").mayOnlyBeAccessedByLayers(
                        "player", "stats", "planner", "gacha", "api", "app", "adapters")
                .whereLayer("player").mayOnlyBeAccessedByLayers("planner", "gacha", "api", "app")
                .whereLayer("stats").mayOnlyBeAccessedByLayers("planner", "api", "app", "store")
                .whereLayer("planner").mayOnlyBeAccessedByLayers("api", "app", "raft")
                .whereLayer("gacha").mayOnlyBeAccessedByLayers("api", "app")
                .whereLayer("identity").mayOnlyBeAccessedByLayers("api", "app")
                // The API edge is the only module that knows about all the others,
                // and nothing may reach back into it.
                .whereLayer("api").mayOnlyBeAccessedByLayers("app")
                // One title, one adapter, and only the CLI in :app ever reaches for one.
                // A core module depending on an adapter would be the exact failure the
                // game-agnosticism rule exists to prevent, arriving through the back door.
                .whereLayer("adapters").mayOnlyBeAccessedByLayers("app")
                .whereLayer("app").mayNotBeAccessedByAnyLayer()

                // Track B is reachable only through the ports it implements.
                .whereLayer("store").mayOnlyBeAccessedByLayers("stats", "raft", "chaos", "app")
                .whereLayer("raft").mayOnlyBeAccessedByLayers("planner", "chaos", "app")
                .whereLayer("chaos").mayNotBeAccessedByAnyLayer()

                .check(CLASSES);
    }
}
