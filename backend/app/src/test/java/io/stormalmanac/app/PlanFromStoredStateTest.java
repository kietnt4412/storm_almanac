package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.gamedata.ingest.CanonicalBundleParser;
import io.stormalmanac.gamedata.ingest.GameDataBundle;
import io.stormalmanac.gamedata.ingest.GameDataIngestRepository;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.identity.AccountRepository;
import io.stormalmanac.identity.oauth.AccountUserServices.AccountOidcUser;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * <b>Phase 3's exit criterion: a plan computed end-to-end from stored state on a
 * real account.</b>
 *
 * <p>Everything before this session computed plans from a goal set a test handed
 * the optimizer directly. Here nothing is handed to it: a person signs in, the
 * sign-in creates an account, the account creates a profile, the profile is given
 * an inventory, a roster and goals over HTTP, and the plan route reads all three
 * back out of Postgres. The only thing the request body carries is how much
 * energy a day this player has and how long they will take, because those are
 * the two things that are not facts about the account.
 *
 * <p><b>What this goes through, and what it does not.</b> These requests go
 * through the real filter chain, the real dispatcher, the real Jackson
 * serialisation and a real database — MockMvc rather than a socket, which is a
 * deliberate step down from {@code GameDataApiTest}'s real HTTP and is worth
 * naming. It is here because an authenticated session cannot be minted over a
 * socket without an authorization server to redirect to, and standing one up
 * would test Spring's implementation of OAuth2 rather than this application's
 * authorization rules. The rules are what must not be wrong, and the filter chain
 * that enforces them is exercised: the anonymous and cross-account cases below
 * both fail at it.
 *
 * <p>What is therefore <em>not</em> covered: the token exchange with a real
 * provider, and any behaviour that lives between the servlet container and the
 * filter chain. The first needs a provider; the second was what caught phase 0's
 * 401, and {@code GameDataApiTest} still covers it for the anonymous surface.
 */
class PlanFromStoredStateTest extends SharedDatabaseTest {

    private static final GameId PROVING_GROUND = new GameId("proving-ground");

    @Autowired
    private GameDataIngestRepository ingest;

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    @DisplayName("a person signs in, saves what they own and what they want, and gets a plan for it")
    void aPlanFromNothingButStoredState() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");

        // 1. Signing in created the account. Nothing else had to.
        JsonNode me = body(mvc.perform(get("/api/me").with(player)));
        assertThat(me.get("profiles")).isEmpty();
        String accountId = me.get("accountId").asText();

        // 2. A profile: one account, one game, one region.
        JsonNode created = body(mvc.perform(post("/api/me/profiles")
                .with(player)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        Map.of("game", "proving-ground", "region", "global", "displayName", "Main")))));
        String profile = created.get("id").asText();

        // 3. What they own, and where their roster stands.
        mvc.perform(put("/api/me/profiles/" + profile + "/inventory")
                        .with(player)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                Map.of("items", Map.of("sigil-lesser", 4, "gold", 5000)))))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(200));

        mvc.perform(put("/api/me/profiles/" + profile + "/roster")
                        .with(player)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("entities", Map.of("warden", List.of("insight-0"))))))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(200));

        // 4. What they want.
        mvc.perform(put("/api/me/profiles/" + profile + "/goals")
                        .with(player)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "goals", List.of(Map.of("entity", "warden", "targetState", "insight-1"))))))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(200));

        // 5. The plan. The body says nothing about goals: they came from the
        //    database, which is the whole point of the phase.
        JsonNode plan = body(mvc.perform(post("/api/me/profiles/" + profile + "/plan")
                .with(player)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("energyPerDay", 240, "horizonDays", 7)))));

        assertThat(plan.get("profile").asText()).isEqualTo(profile);
        assertThat(plan.get("objective").asText()).isEqualTo("LEAST_ENERGY");

        // The version the numbers came from, and where they came from, travel
        // with the answer. A plan that cannot say which patch it describes is
        // one nobody can check after the next one lands.
        assertThat(plan.get("game").asText()).isEqualTo("proving-ground");
        assertThat(plan.get("versionLabel").asText()).isNotBlank();
        assertThat(plan.get("attribution").asText()).isNotBlank();

        // The player already owns the four Lesser Sigils and the gold that
        // Insight 1 costs, so the honest plan is to farm nothing.
        assertThat(plan.get("totalEnergy").asInt()).isZero();
        assertThat(plan.get("stages")).isEmpty();

        // And the account it hangs off is the one the sign-in created.
        assertThat(accounts.find(AccountId.of(accountId))).isPresent();
    }

    @Test
    @DisplayName("the same stored state with less in the bank makes the solver go farming")
    void whatIsNotOwnedIsFarmed() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileWithGoal(player, Map.of());

        JsonNode plan = body(mvc.perform(post("/api/me/profiles/" + profile + "/plan")
                .with(player)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("energyPerDay", 240, "horizonDays", 7)))));

        // Same goal, empty inventory: now there is work to do, and every line of
        // it names a stage that exists in the patch it was solved against.
        assertThat(plan.get("totalEnergy").asInt()).isPositive();
        assertThat(plan.get("stages")).isNotEmpty();
        for (JsonNode stage : plan.get("stages")) {
            assertThat(stage.get("stage").asText()).isNotBlank();
            assertThat(stage.get("runs").asInt()).isPositive();
        }
    }

    @Test
    @DisplayName("a profile with no goals is told so, rather than handed an empty plan")
    void nothingToPlanFor() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");

        String profile = body(mvc.perform(post("/api/me/profiles")
                        .with(player)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                Map.of("game", "proving-ground", "region", "global", "displayName", "Main")))))
                .get("id")
                .asText();

        // An empty plan is indistinguishable from "you have everything you
        // need", which is a different and much more encouraging thing to be told.
        mvc.perform(post("/api/me/profiles/" + profile + "/plan")
                        .with(player)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("energyPerDay", 240))))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(404));
    }

    @Test
    @DisplayName("energyPerDay is required, because there is no honest default for it")
    void energyPerDayIsRequired() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileWithGoal(player, Map.of());

        mvc.perform(post("/api/me/profiles/" + profile + "/plan")
                        .with(player)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("another account's profile is not found, rather than forbidden")
    void anotherAccountsProfileIsNotEvenThere() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor mine = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileWithGoal(mine, Map.of());

        RequestPostProcessor theirs = signedIn("google", "sub-someone-else", "Someone Else");

        // 404 and not 403: a 403 would confirm the profile exists, which turns a
        // profile id into an oracle for enumerating other people's.
        mvc.perform(get("/api/me/profiles/" + profile + "/inventory").with(theirs))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(404));

        mvc.perform(post("/api/me/profiles/" + profile + "/plan")
                        .with(theirs)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("energyPerDay", 240))))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(404));

        // And the owner is unaffected — the check is on ownership, not on the
        // profile having been touched by a stranger.
        mvc.perform(get("/api/me/profiles/" + profile + "/inventory").with(mine))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(200));
    }

    @Test
    @DisplayName("an anonymous caller gets 401 from every account-scoped route")
    void anonymousIsRefused() throws Exception {
        mvc.perform(get("/api/me"))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(401));
        mvc.perform(get("/api/me/profiles/anything/inventory"))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(401));
        mvc.perform(post("/api/me/profiles/anything/plan").with(csrf()))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(401));

        // The public half is still public: this is the route phase 0 existed to
        // deploy, and it answers a probe with no credentials to offer.
        mvc.perform(get("/api/health"))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(200));
    }

    @Test
    @DisplayName("a write without a CSRF token is refused, signed in or not")
    void csrfIsEnforced() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");

        // A cookie-authenticated surface exists now, so a form on somebody
        // else's page must not be able to write here. No .with(csrf()).
        mvc.perform(post("/api/me/profiles")
                        .with(player)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                Map.of("game", "proving-ground", "region", "global", "displayName", "Main"))))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(403));
    }

    /**
     * A signed-in caller whose account really exists.
     *
     * <p>The principal is the one {@code AccountUserServices} mints, built over
     * an account that {@code AccountRepository} really created — so the account
     * id this test authorizes against is a row in the database and not a
     * fixture. What is skipped is the redirect and the token exchange, which is
     * Spring's code and not this application's.
     */
    private RequestPostProcessor signedIn(String provider, String subject, String displayName) {
        AccountId account = accounts
                .upsertFromOidc(provider, subject, displayName, subject + "@example.com")
                .id();

        OidcUser delegate = new DefaultOidcUser(
                List.of(),
                new OidcIdToken(
                        "token",
                        Instant.now(),
                        Instant.now().plusSeconds(3600),
                        Map.of("sub", subject, "name", displayName)));

        return oidcLogin().oidcUser(new AccountOidcUser(delegate, account));
    }

    /** A profile owned by {@code player}, wanting Warden at Insight 1. */
    private String profileWithGoal(RequestPostProcessor player, Map<String, Integer> items) throws Exception {
        String profile = body(mvc.perform(post("/api/me/profiles")
                        .with(player)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                Map.of("game", "proving-ground", "region", "global", "displayName", "Main")))))
                .get("id")
                .asText();

        mvc.perform(put("/api/me/profiles/" + profile + "/inventory")
                .with(player)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("items", items))));

        mvc.perform(put("/api/me/profiles/" + profile + "/roster")
                .with(player)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("entities", Map.of("warden", List.of("insight-0"))))));

        mvc.perform(put("/api/me/profiles/" + profile + "/goals")
                .with(player)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        Map.of("goals", List.of(Map.of("entity", "warden", "targetState", "insight-1"))))));

        return profile;
    }

    private JsonNode body(org.springframework.test.web.servlet.ResultActions actions) throws Exception {
        MvcResult result = actions.andReturn();
        assertThat(result.getResponse().getStatus())
                .as("%s %s -> %s", result.getRequest().getMethod(), result.getRequest().getRequestURI(),
                        result.getResponse().getContentAsString())
                .isBetween(200, 299);
        return json.readTree(result.getResponse().getContentAsString());
    }

    private void publish(String fixture) {
        GameDataBundle bundle = bundle(fixture);
        ingest.ingestDraft(bundle);
        ingest.publish(PROVING_GROUND, bundle.sequence());
    }

    private static GameDataBundle bundle(String fixture) {
        try (InputStream in = PlanFromStoredStateTest.class.getResourceAsStream("/gamedata/" + fixture)) {
            if (in == null) throw new IllegalStateException("fixture not on the classpath: " + fixture);
            return new CanonicalBundleParser().parse(in);
        } catch (IOException e) {
            throw new IllegalStateException("could not read " + fixture, e);
        }
    }
}
