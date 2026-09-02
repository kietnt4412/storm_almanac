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

    implementation(libs.springBootStarterWeb)
    implementation(libs.springBootStarterActuator)
    implementation(libs.springBootStarterJdbc)
    implementation(libs.flywayCore)
    runtimeOnly(libs.flywayPostgres)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.springBootStarterTest)
    testImplementation(libs.archunit)
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("storm-almanac.jar")
}
