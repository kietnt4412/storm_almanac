plugins {
    alias(libs.plugins.springBoot)
}

// The single deployable. Assembles the modular monolith; owns configuration,
// the Flyway migrations and nothing else.
dependencies {
    implementation(project(":modules:api"))
    implementation(project(":modules:common"))
    implementation(project(":modules:gamedata"))
    implementation(project(":modules:identity"))
    implementation(project(":modules:player"))
    implementation(project(":modules:planner"))
    implementation(project(":modules:gacha"))
    implementation(project(":modules:stats"))

    // One title, one adapter. :app is the only module allowed to know these
    // exist, and it uses them from the CLI alone — see ModuleBoundaryTest.
    implementation(project(":adapters:reverse-1999"))

    implementation(libs.springBootStarterWeb)
    implementation(libs.springBootStarterActuator)
    implementation(libs.springBootStarterJdbc)
    implementation(libs.flywayCore)
    runtimeOnly(libs.flywayPostgres)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.springBootStarterTest)
    testImplementation(libs.archunit)

    // ApplicationBootTest starts the real context against a real Postgres.
    testImplementation(libs.testcontainersPostgres)
    testImplementation(libs.testcontainersJunit)
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("storm-almanac.jar")
}
