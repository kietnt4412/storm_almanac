// Development-only sign-in. See package-info in io.stormalmanac.devsignin for
// why this is a module of its own rather than a profile inside :modules:identity.
//
// Nothing in the product depends on this project. It is consumed by :app through
// `developmentOnly`/`testAndDevelopmentOnly`, which is what keeps it out of the
// deployable jar — and DeployableJarTest opens that jar and proves it.
dependencies {
    api(project(":modules:common"))
    implementation(project(":modules:identity"))
    implementation(libs.springBootStarterSecurity)
    implementation(libs.springBootStarterWeb)
}
