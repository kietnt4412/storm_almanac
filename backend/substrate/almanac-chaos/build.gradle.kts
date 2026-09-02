// Track B · Phase 10. Fault injection and linearizability checking.
// Test-scope tooling that happens to live in main so CI can run it nightly.
dependencies {
    implementation(project(":substrate:almanac-store"))
    implementation(project(":substrate:almanac-raft"))
    testImplementation(libs.jqwik)
}
