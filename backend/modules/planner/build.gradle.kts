// The MIP optimizer, plan persistence, explanation output.
dependencies {
    api(project(":modules:common"))
    api(project(":modules:gamedata"))
    api(project(":modules:player"))
    api(project(":modules:stats"))
    implementation(libs.ojalgo)
    implementation(libs.springBootStarterRedis)
}
