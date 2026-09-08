import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    alias(libs.plugins.springBoot) apply false
    alias(libs.plugins.springDepMgmt) apply false
}

allprojects {
    group = "io.stormalmanac"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "io.spring.dependency-management")

    // The version catalog accessor (`libs`) is not generated inside a
    // cross-project `subprojects` block, so look it up explicitly.
    val catalog = rootProject.extensions
        .getByType(VersionCatalogsExtension::class.java)
        .named("libs")
    fun lib(alias: String) = catalog.findLibrary(alias).orElseThrow()
    fun ver(alias: String) = catalog.findVersion(alias).orElseThrow().requiredVersion

    extensions.configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${ver("springBoot")}")
        }
    }

    extensions.configure<JavaPluginExtension> {
        toolchain { languageVersion.set(JavaLanguageVersion.of(ver("java"))) }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        // -parameters keeps constructor parameter names for Jackson record binding.
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-serial", "-parameters"))
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging { events("passed", "skipped", "failed") }

        // Which Docker API version Testcontainers speaks. Boot 3.5.6 manages
        // Testcontainers 1.21.3, whose docker-java 3.4.2 defaults to API 1.32;
        // Docker Engine 29 raised its minimum to 1.40 and answers 400 "client
        // version 1.32 is too old" to every container it is asked to create. So
        // every container-backed test fails at startup on a machine that has
        // taken the Docker 29 update, and passes on one that has not — which is
        // the worst shape a build failure can have.
        //
        // 1.44 is docker-java's own config key (`api.version`), it is well
        // inside every engine this project runs against, and it is set here
        // rather than in an environment variable because a Gradle test worker
        // inherits the daemon's environment and not the shell's. Fixing it by
        // moving Testcontainers is the better answer and is not available: the
        // 2.x line renamed the module artifacts, which is a migration and not a
        // version bump. See environment note E2 in TRACKER.md.
        systemProperty("api.version", "1.44")
    }

    dependencies {
        add("testImplementation", lib("junitJupiter"))
        add("testImplementation", lib("assertj"))
        add("testRuntimeOnly", lib("junitPlatformLauncher"))
    }
}
