// Accounts, Google/Discord OAuth, sessions, multiple game profiles per user.
dependencies {
    api(project(":modules:common"))
    implementation(libs.springBootStarterSecurity)
    implementation(libs.springBootStarterOauth2)
    implementation(libs.springBootStarterJdbc)
    // ReturnAfterSignIn handles the request itself. compileOnly because the
    // container that runs it arrives with :app's web starter, not with this module.
    compileOnly("jakarta.servlet:jakarta.servlet-api")
}
