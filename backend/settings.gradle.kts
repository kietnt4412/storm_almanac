rootProject.name = "storm-almanac"

// ── Track A · product modules ───────────────────────────────────────────────
// Hard module boundaries, separate schemas, no cross-module database reads.
// Modules talk through internal events so that extraction later is a
// deployment change rather than a rewrite. See docs/adr/0002-modular-monolith.md
include(
    ":modules:common",
    ":modules:gamedata",
    ":modules:identity",
    ":modules:player",
    ":modules:planner",
    ":modules:gacha",
    ":modules:stats",
    ":modules:api",
    ":app",
)

// ── Track B · substrate ─────────────────────────────────────────────────────
// Hand-built infrastructure. Each one sits behind an interface that already has
// a boring implementation; both stay selectable by config.
// See docs/adr/0003-the-honesty-rule.md
include(
    ":substrate:almanac-store",
    ":substrate:almanac-raft",
    ":substrate:almanac-chaos",
)

project(":substrate:almanac-store").projectDir = file("substrate/almanac-store")
project(":substrate:almanac-raft").projectDir = file("substrate/almanac-raft")
project(":substrate:almanac-chaos").projectDir = file("substrate/almanac-chaos")

dependencyResolutionManagement {
    repositories { mavenCentral() }
}
