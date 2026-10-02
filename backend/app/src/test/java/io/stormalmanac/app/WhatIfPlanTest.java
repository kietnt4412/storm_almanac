package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;
import static io.stormalmanac.app.BrowserCsrf.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.gamedata.ingest.CanonicalBundleParser;
import io.stormalmanac.gamedata.ingest.GameDataBundle;
import io.stormalmanac.gamedata.ingest.GameDataIngestRepository;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * C2.20's route: the plan's question asked while a slider moves, which must
 * answer like the plan route and must never write like it.
 */
class WhatIfPlanTest extends SharedDatabaseTest {

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
    @DisplayName("a what-if answers the plan's question and leaves the saved plan, and its ticks, as they were")
    void aWhatIfSavesNothing() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("sub-what-if-saves", "Vertin");
        String profile = profileWithGoal(player);

        JsonNode plan = body(mvc.perform(post("/api/me/profiles/" + profile + "/plan")
                .with(player)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("energyPerDay", 240, "horizonDays", 7)))));
        JsonNode saved = body(mvc.perform(get("/api/me/profiles/" + profile + "/plan").with(player)));
        String key = plan.get("stages").get(0).get("stage").asText();
        mvc.perform(put("/api/me/profiles/" + profile + "/plan/done")
                        .with(player)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                Map.of("savedAt", saved.get("savedAt").asText(), "done", List.of(key)))))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(204));

        JsonNode whatIf = whatIf(player, profile, Map.of(
                "energyPerDay", 120, "horizonDays", 30, "objective", "FEWEST_DAYS"));
        assertThat(whatIf.get("plan").get("objective").asText()).isEqualTo("FEWEST_DAYS");
        assertThat(whatIf.get("solveMillis").asLong()).isNotNegative();

        JsonNode after = body(mvc.perform(get("/api/me/profiles/" + profile + "/plan").with(player)));
        assertThat(after.get("savedAt")).isEqualTo(saved.get("savedAt"));
        assertThat(after.get("request")).isEqualTo(saved.get("request"));
        assertThat(after.get("plan").get("objective").asText()).isEqualTo("LEAST_ENERGY");
        assertThat(after.get("done")).containsExactly(json.getNodeFactory().textNode(key));
    }

    @Test
    @DisplayName("a profile that never planned still has no saved plan after a what-if")
    void aWhatIfIsNotAFirstPlan() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("sub-what-if-first", "Vertin");
        String profile = profileWithGoal(player);

        whatIf(player, profile, Map.of("energyPerDay", 240));

        mvc.perform(get("/api/me/profiles/" + profile + "/plan").with(player))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(204));
    }

    @Test
    @DisplayName("pretending to hold what the goal costs plans nothing, and the same pretence again is served from cache")
    void extraIsSolvedAsHeld() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("sub-what-if-extra", "Vertin");
        String profile = profileWithGoal(player);

        JsonNode farming = whatIf(player, profile, Map.of("energyPerDay", 240, "horizonDays", 7));
        assertThat(farming.get("plan").get("totalEnergy").asInt()).isPositive();
        // The shadow prices say which lines a reader could hold more of.
        assertThat(farming.get("plan").get("shadowPrice"))
                .isNotEmpty()
                .allSatisfy(price -> assertThat(price.get("holdable").isBoolean()).isTrue())
                .anySatisfy(price -> assertThat(price.get("holdable").asBoolean()).isTrue());

        // A horizon no other test asks, because the solve cache outlives a test in
        // a shared context, and this one is about the first ask being a miss.
        Map<String, Object> pretend = Map.of(
                "energyPerDay", 240,
                "horizonDays", 11,
                "extra", Map.of("sigil-lesser", 4, "gold", 5000));
        JsonNode first = whatIf(player, profile, pretend);
        JsonNode second = whatIf(player, profile, pretend);

        // What PlanFromStoredStateTest's first reader holds for real: nothing to farm.
        assertThat(first.get("plan").get("totalEnergy").asInt()).isZero();
        assertThat(first.get("fromCache").asBoolean()).isFalse();
        assertThat(second.get("fromCache").asBoolean()).isTrue();
        assertThat(second.get("plan").get("id")).isEqualTo(first.get("plan").get("id"));
    }

    @Test
    @DisplayName("a what-if refuses an item the game does not have, a negative pretence, and a missing energy")
    void badPretencesAreRefused() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("sub-what-if-refused", "Vertin");
        String profile = profileWithGoal(player);

        assertThat(status(player, profile, Map.of("energyPerDay", 240, "extra", Map.of("no-such-item", 1))))
                .isEqualTo(400);
        assertThat(status(player, profile, Map.of("energyPerDay", 240, "extra", Map.of("gold", -5))))
                .isEqualTo(400);
        assertThat(status(player, profile, Map.of("extra", Map.of("gold", 5)))).isEqualTo(400);
    }

    @Test
    @DisplayName("a what-if on somebody else's profile is refused like their plan is")
    void anotherReadersProfileIsRefused() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor owner = signedIn("sub-what-if-owner", "Vertin");
        RequestPostProcessor stranger = signedIn("sub-what-if-stranger", "Sonetto");
        String profile = profileWithGoal(owner);

        assertThat(status(stranger, profile, Map.of("energyPerDay", 240))).isBetween(400, 404);
    }

    private JsonNode whatIf(RequestPostProcessor player, String profile, Map<String, Object> request)
            throws Exception {
        return body(ask(player, profile, request));
    }

    private int status(RequestPostProcessor player, String profile, Map<String, Object> request) throws Exception {
        return ask(player, profile, request).andReturn().getResponse().getStatus();
    }

    private ResultActions ask(RequestPostProcessor player, String profile, Map<String, Object> request)
            throws Exception {
        return mvc.perform(post("/api/me/profiles/" + profile + "/plan/what-if")
                .with(player)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(request)));
    }

    private RequestPostProcessor signedIn(String subject, String displayName) {
        AccountId account = accounts
                .upsertFromOidc("google", subject, displayName, subject + "@example.com")
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

    /** A profile owned by {@code player} with an empty bag, wanting Warden at Insight 1. */
    private String profileWithGoal(RequestPostProcessor player) throws Exception {
        String profile = body(mvc.perform(post("/api/me/profiles")
                        .with(player)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                Map.of("game", "proving-ground", "region", "global", "displayName", "Main")))))
                .get("id")
                .asText();

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

    private JsonNode body(ResultActions actions) throws Exception {
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
        try (InputStream in = WhatIfPlanTest.class.getResourceAsStream("/gamedata/" + fixture)) {
            if (in == null) throw new IllegalStateException("fixture not on the classpath: " + fixture);
            return new CanonicalBundleParser().parse(in);
        } catch (IOException e) {
            throw new IllegalStateException("could not read " + fixture, e);
        }
    }
}
