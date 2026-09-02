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
    }

    dependencies {
        add("testImplementation", lib("junitJupiter"))
        add("testImplementation", lib("assertj"))
        add("testRuntimeOnly", lib("junitPlatformLauncher"))
    }
}
