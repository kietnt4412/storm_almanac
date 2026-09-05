// Canonical catalog, versioned publishing, ingestion and diffing.
dependencies {
    api(project(":modules:common"))
    implementation(libs.springBootStarterJdbc)

    // The canonical bundle format is JSON. Tree model only — the domain records
    // stay free of mapping annotations. See ADR 0008.
    implementation(libs.jacksonDatabind)
}
