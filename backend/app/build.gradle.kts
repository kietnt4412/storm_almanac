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

    // The development-only sign-in, and the one dependency in this file that is
    // about what the artifact must NOT contain.
    //
    // `testAndDevelopmentOnly` puts it on the classpath of `bootRun` and of the
    // tests, and the Spring Boot plugin excludes it from `bootJar`. So a
    // developer running the backend locally has a login, the test suite can
    // exercise every authorization rule behind a real session, and the jar that
    // gets deployed does not contain the classes at all — which is a stronger
    // guarantee than any profile or property could give, because there is
    // nothing left to switch on. DeployableJarTest opens the jar and checks.
    //
    // If this line ever becomes `implementation`, the guard is gone and nothing
    // else in the build would notice; that test is what notices.
    testAndDevelopmentOnly(project(":modules:identity-dev"))

    implementation(libs.springBootStarterWeb)
    implementation(libs.springBootStarterActuator)
    implementation(libs.springBootStarterJdbc)
    implementation(libs.flywayCore)
    runtimeOnly(libs.flywayPostgres)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.springBootStarterTest)
    // Signs a test request in as an OIDC user without standing up a provider.
    // The alternative is a mock authorization server, which would be testing
    // Spring Security's protocol implementation rather than this application's
    // authorization rules — and those rules are the thing that must not be wrong.
    testImplementation(libs.springSecurityTest)
    // identity keeps the OAuth2 starter to itself (implementation, not api), so
    // it reaches this module at runtime but not at compile time. The end-to-end
    // test builds an OIDC principal by hand, so it needs the types here too.
    testImplementation(libs.springBootStarterOauth2)
    testImplementation(libs.archunit)

    // ApplicationBootTest starts the real context against a real Postgres.
    testImplementation(libs.testcontainersPostgres)
    testImplementation(libs.testcontainersJunit)
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("storm-almanac.jar")
}

// Local sign-in is the real Google client, so a developer needs its id and
// secret, and neither may be committed. `bootRun` — and only `bootRun` — also
// reads ~/.storm-almanac/, where a developer keeps an application.yml holding
// the registration. `optional:` because a checkout with no file must still run,
// signed out. Not the tests, which must never see a real secret, and not the
// jar, which is configured by its host's environment.
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    val home = System.getProperty("user.home").replace('\\', '/')
    systemProperty("spring.config.additional-location", "optional:file:$home/.storm-almanac/")
}

// DeployableJarTest opens the artifact and asserts what is and is not inside it
// — the development sign-in must not be, SecurityConfig must be. That only means
// anything against a jar built from the current sources, so the test task builds
// one and is told where it is rather than guessing from a working directory.
tasks.named<Test>("test") {
    val deployable = tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar")
    dependsOn(deployable)
    systemProperty("storm-almanac.deployable-jar", deployable.get().archiveFile.get().asFile.absolutePath)

    // AuthoredBundlesTest reads the hand-typed game data under data/bundles, which
    // is outside every source set and so is not an input to anything. Without this
    // the task is FROM-CACHE after a bundle changes — measured, not feared: a
    // deliberately second-hand bundle was dropped in and the build stayed green
    // until --rerun-tasks. Declaring the directory is what makes the guard real.
    inputs.dir(layout.projectDirectory.dir("../../data/bundles"))
        .withPropertyName("authoredBundles")
        .withPathSensitivity(PathSensitivity.RELATIVE)

    // The same hole as data/bundles above, with two extra teeth, both found by
    // running the documented workflow in N36 and watching it prove nothing.
    //
    // RealUpstreamPatchTest, RealUpstreamPlanTest and CommunityBenchmarkTest read
    // snapshots that ADR 0009 forbids vendoring, so tools/fetch-upstream.sh puts
    // them outside every source set and the tests skip themselves when they are
    // absent. First tooth: -Dstorm-almanac.upstream on the Gradle command line
    // never reached the test worker, because nothing forwarded it — so the
    // script's own custom-directory mode sent the tests to the default path,
    // where they found nothing and skipped, silently and green. Second tooth: the
    // default path is under build/, so a plain `./gradlew build` immediately
    // after fetching comes back UP-TO-DATE from the run that had no snapshots at
    // all. Forwarding the property fixes the first; declaring the directory fixes
    // the second, and it is optional because absent snapshots must still be a
    // skip rather than a build failure — that is the whole point of ADR 0009.
    val upstream = providers.systemProperty("storm-almanac.upstream")
        .orElse(layout.projectDirectory.dir("../build/upstream-snapshots").asFile.path)
    systemProperty("storm-almanac.upstream", upstream.get())
    inputs.files(fileTree(upstream.get()))
        .withPropertyName("upstreamSnapshots")
        .withPathSensitivity(PathSensitivity.RELATIVE)
        .optional(true)
}
