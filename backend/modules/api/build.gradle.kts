// REST + WebSocket edge, DTOs, auth, rate limiting.
// The only module allowed to know about every other module.
dependencies {
    api(project(":modules:common"))
    implementation(project(":modules:gamedata"))
    implementation(project(":modules:identity"))
    implementation(project(":modules:player"))
    implementation(project(":modules:planner"))
    implementation(project(":modules:gacha"))
    implementation(project(":modules:stats"))
    implementation(libs.springBootStarterWeb)
    implementation(libs.springBootStarterWebsocket)
    implementation(libs.springBootStarterValidation)
    implementation(libs.springBootStarterActuator)
}
