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

// ── Parser adapters · one per title ─────────────────────────────────────────
// The project's central claim is that a new game costs a GameDataBundle plus a
// parser adapter and nothing else. These modules are where the receipt for that
// claim is kept: every quirk of one upstream — its file layout, its units, its
// habit of keying on display names — is confined to exactly one directory, and
// no core module may depend on any of them.
include(
    ":adapters:reverse-1999",
)

project(":adapters:reverse-1999").projectDir = file("adapters/reverse-1999")

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
