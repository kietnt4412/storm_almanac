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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * C3.2: what a new sequence changed for one reader's goals and plan (ADR 0037).
 *
 * <p>The two proving-ground fixtures are the patch. 1.1's header lists every
 * difference it carries, on both axes, and most of them touch nothing a reader
 * wanting Warden at Insight 1 uses — which is what makes it the right patch to
 * narrow: a report that passed everything through would pass these tests' counts
 * and fail their exclusions.
 */
class ChangesSinceTest extends SharedDatabaseTest {

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
    @DisplayName("a profile that has never planned has nothing to have changed since, and says so with a 204")
    void noPlanNoReport() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("sub-vertin");
        String profile = profileWantingInsightOne(player);

        mvc.perform(get("/api/me/profiles/" + profile + "/since").with(player))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(204));
    }

    @Test
    @DisplayName("a plan on the latest sequence is up to date, and nothing is solved to say so")
    void upToDate() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("sub-vertin");
        String profile = profileWantingInsightOne(player);
        plan(player, profile);

        JsonNode since = body(mvc.perform(get("/api/me/profiles/" + profile + "/since").with(player)));

        assertThat(since.get("savedVersion").asLong()).isEqualTo(since.get("latestVersion").asLong());
        assertThat(since.get("changes")).isEmpty();
        assertThat(since.get("allChanges").asInt()).isZero();
        assertThat(since.get("onSaved").isNull()).isTrue();
        assertThat(since.get("onLatest").isNull()).isTrue();
    }

    @Test
    @DisplayName("after a new sequence, the report keeps the changes the reader's plans touch and drops the rest")
    void theChangesThatConcernThePlan() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("sub-vertin");
        String profile = profileWantingInsightOne(player);
        JsonNode shown = plan(player, profile);

        // The precondition the assertions below stand on: with nothing owned,
        // Insight 1's gold is farmed at Ashfall Approach.
        assertThat(shown.get("stages")).extracting(stage -> stage.get("stage").asText()).contains("pg-1-1");

        publish("proving-ground-1.1.json");
        JsonNode since = body(mvc.perform(get("/api/me/profiles/" + profile + "/since").with(player)));

        assertThat(since.get("savedVersion").asLong()).isZero();
        assertThat(since.get("savedVersionLabel").asText()).isEqualTo("1.0");
        assertThat(since.get("latestVersion").asLong()).isEqualTo(1);
        assertThat(since.get("latestVersionLabel").asText()).isEqualTo("1.1");

        List<JsonNode> changes = new ArrayList<>();
        since.get("changes").forEach(changes::add);

        // The stage the plan farms moved, and the report says so by its name.
        assertThat(changes).anySatisfy(change -> {
            assertThat(change.get("kind").asText()).isEqualTo("CHANGED");
            assertThat(change.get("about").asText()).isEqualTo("stage");
            assertThat(change.get("slug").asText()).isEqualTo("pg-1-1");
            assertThat(change.get("name").asText()).isEqualTo("Ashfall Approach");
            assertThat(change.get("detail").asText()).isEqualTo("drop ore-rough");
            assertThat(change.get("before").asText()).isEqualTo("1.4");
            assertThat(change.get("after").asText()).isEqualTo("1.6");
        });

        // What it left out is most of the patch. Insight 2's gold is a step past
        // the goal; the skill multiplier and stat curve are combat numbers; the
        // new stage and the new sigil are nothing either plan uses.
        assertThat(changes).extracting(change -> change.get("about").asText() + " " + change.get("slug").asText())
                .doesNotContain(
                        "upgrade warden-insight-2", "entity warden", "stage pg-3-1", "item sigil-radiant");
        assertThat(since.get("allChanges").asInt()).isGreaterThan(changes.size());

        // Both sides solved against today's state with the saved request.
        for (String side : List.of("onSaved", "onLatest")) {
            assertThat(since.get(side).get("refused").isNull()).as(side).isTrue();
            assertThat(since.get(side).get("totalEnergy").asInt()).as(side).isPositive();
        }
        assertThat(since.get("onSaved").get("version").asLong()).isZero();
        assertThat(since.get("onLatest").get("version").asLong()).isEqualTo(1);

        // And asking wrote nothing: the saved plan is still the one shown.
        JsonNode saved = body(mvc.perform(get("/api/me/profiles/" + profile + "/plan").with(player)));
        assertThat(saved.get("plan")).isEqualTo(shown);
    }

    @Test
    @DisplayName("a side with nothing to plan says why, rather than reporting an empty plan")
    void aRefusedSideSaysWhy() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("sub-vertin");
        String profile = profileWantingInsightOne(player);
        plan(player, profile);
        publish("proving-ground-1.1.json");

        mvc.perform(put("/api/me/profiles/" + profile + "/goals")
                .with(player)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("goals", List.of()))));

        JsonNode since = body(mvc.perform(get("/api/me/profiles/" + profile + "/since").with(player)));
        for (String side : List.of("onSaved", "onLatest")) {
            assertThat(since.get(side).get("refused").asText()).as(side).contains("no goals");
            assertThat(since.get(side).get("totalEnergy").isNull()).as(side).isTrue();
        }
    }

    @Test
    @DisplayName("another account's report is not found")
    void theReportIsTheProfilesAlone() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor mine = signedIn("sub-vertin");
        String profile = profileWantingInsightOne(mine);
        plan(mine, profile);

        mvc.perform(get("/api/me/profiles/" + profile + "/since").with(signedIn("sub-someone-else")))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(404));
    }

    private RequestPostProcessor signedIn(String subject) {
        AccountId account = accounts.upsertFromOidc("google", subject, subject, subject + "@example.com").id();
        return oidcLogin().oidcUser(new AccountOidcUser(
                new DefaultOidcUser(
                        List.of(),
                        new OidcIdToken(
                                "token", Instant.now(), Instant.now().plusSeconds(3600), Map.of("sub", subject))),
                account));
    }

    /** Nothing owned, Warden at Insight 0, wanting Insight 1. */
    private String profileWantingInsightOne(RequestPostProcessor player) throws Exception {
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

    private JsonNode plan(RequestPostProcessor player, String profile) throws Exception {
        return body(mvc.perform(post("/api/me/profiles/" + profile + "/plan")
                .with(player)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("energyPerDay", 240, "horizonDays", 7)))));
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
        try (InputStream in = ChangesSinceTest.class.getResourceAsStream("/gamedata/" + fixture)) {
            if (in == null) throw new IllegalStateException("fixture not on the classpath: " + fixture);
            GameDataBundle bundle = new CanonicalBundleParser().parse(in);
            ingest.ingestDraft(bundle);
            ingest.publish(PROVING_GROUND, bundle.sequence());
        } catch (IOException e) {
            throw new IllegalStateException("could not read " + fixture, e);
        }
    }
}
