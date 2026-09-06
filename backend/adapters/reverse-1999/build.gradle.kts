// One title, one adapter. The whole cost of onboarding a game is meant to live
// in a module like this one, so that the cost is visible as a directory rather
// than spread through the core as branches.
//
// It depends on :modules:gamedata for the canonical bundle and on nothing else.
// Nothing depends on it except :app, which wires it into the CLI — see the
// adapters layer in ModuleBoundaryTest.
dependencies {
    api(project(":modules:gamedata"))

    // The upstream publishes JSON. Tree model, same as the canonical parser:
    // the upstream's shape is not ours and binding onto records would invent an
    // agreement that does not exist.
    implementation(libs.jacksonDatabind)
}
