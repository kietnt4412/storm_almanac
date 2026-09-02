// Track B · Phase 8. Raft consensus, applied to SolveCoordinator and a
// replicated KV that mirrors the Redis solve cache.
dependencies {
    implementation(project(":substrate:almanac-store"))
    testImplementation(libs.jqwik)
}
