// Drop reports, aggregation, estimate publication.
// Owns the DropReportStore port; the Postgres adapter ships first and the
// almanac-store adapter joins it in Phase 7. Both stay selectable by config.
dependencies {
    api(project(":modules:common"))
    api(project(":modules:gamedata"))
    implementation(project(":substrate:almanac-store"))
    implementation(libs.springBootStarterJdbc)
}
