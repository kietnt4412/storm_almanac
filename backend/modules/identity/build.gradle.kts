// Accounts, Google/Discord OAuth, sessions, multiple game profiles per user.
dependencies {
    api(project(":modules:common"))
    implementation(libs.springBootStarterSecurity)
    implementation(libs.springBootStarterOauth2)
    implementation(libs.springBootStarterJdbc)
}
