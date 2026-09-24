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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * <b>The personalized overlay: what <em>this</em> reader is short of.</b>
 *
 * <p>Phase 4's exit criterion has two clauses and this is the second — a
 * logged-in character page shows what that reader still needs. Everything the
 * catalog served before this was true of the game rather than of the reader, and
 * a page that is only true of the game is a wiki page somebody else already
 * wrote. The route under test is what makes an account worth having.
 *
 * <p><b>Why the route exists rather than the browser walking the graph.</b>
 * Every upgrade step is already on the wire, so a client could chain them
 * itself. It must not: the rules about what a chain <em>means</em> — a state the
 * graph cannot reach, a state the player is already past, a probabilistic target
 * with no scalar cost — live in {@code DemandResolver}, and a second copy of
 * them in TypeScript is a copy nothing in this repository tests. The cases below
 * are those rules, asked over HTTP.
 */
class ShortfallTest extends SharedDatabaseTest {

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
    @DisplayName("a character page says what is owed, what is held and what is missing")
    void whatThisReaderIsShortOf() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");

        // Sitting at Insight 1 with some of Insight 2's bill already in the bank.
        String profile = profile(player, Map.of("sigil-greater", 2, "gold", 20000), "insight-1");

        JsonNode shortfall = body(mvc.perform(get("/api/me/profiles/" + profile + "/shortfall")
                .param("entity", "warden")
                .param("target", "insight-2")
                .with(player)));

        assertThat(shortfall.get("entity").asText()).isEqualTo("warden");
        assertThat(lines(shortfall.get("currentStates"))).containsExactly("insight-1");
        assertThat(shortfall.get("targetState").asText()).isEqualTo("insight-2");
        assertThat(shortfall.get("alreadyMet").asBoolean()).isFalse();
        assertThat(shortfall.get("complete").asBoolean()).isFalse();

        // The step being paid for, named — so the total has something to be a
        // total of, and a reader who disputes it knows what to dispute.
        assertThat(lines(shortfall.get("steps"))).containsExactly("warden-insight-2");

        Map<String, JsonNode> byItem = byItem(shortfall);

        // Insight 2 costs 6 Greater Sigils, 8 Refined Ore and 20 000 Gold.
        // Two sigils and the whole of the gold are already held, and all three
        // numbers travel rather than only the subtraction: a reader who
        // disagrees with a shortfall needs to see which half they disagree with.
        assertThat(byItem.get("sigil-greater").get("required").asInt()).isEqualTo(6);
        assertThat(byItem.get("sigil-greater").get("owned").asInt()).isEqualTo(2);
        assertThat(byItem.get("sigil-greater").get("missing").asInt()).isEqualTo(4);

        assertThat(byItem.get("ore-refined").get("owned").asInt()).isZero();
        assertThat(byItem.get("ore-refined").get("missing").asInt()).isEqualTo(8);

        assertThat(byItem.get("gold").get("required").asInt()).isEqualTo(20000);
        assertThat(byItem.get("gold").get("missing").asInt()).isZero();

        // The item's name is resolved here so that no client has to join the
        // catalog to render a line of its own answer.
        assertThat(byItem.get("gold").get("displayName").asText()).isEqualTo("Gold");

        // Biggest gap first: the list is read to decide what to go and farm.
        assertThat(shortfall.get("items").get(0).get("item").asText()).isEqualTo("ore-refined");

        // And it says which patch it is true of, like every other answer here.
        assertThat(shortfall.get("versionLabel").asText()).isNotBlank();
        assertThat(shortfall.get("attribution").asText()).isNotBlank();
    }

    @Test
    @DisplayName("an inventory that covers the bill is complete, not empty")
    void nothingLeftToFarm() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profile(player, Map.of("sigil-lesser", 10, "gold", 50000), "insight-0");

        JsonNode shortfall = body(mvc.perform(get("/api/me/profiles/" + profile + "/shortfall")
                .param("entity", "warden")
                .param("target", "insight-1")
                .with(player)));

        assertThat(shortfall.get("complete").asBoolean()).isTrue();
        assertThat(shortfall.get("alreadyMet").asBoolean()).isFalse();
        assertThat(shortfall.get("items")).isNotEmpty();
        for (JsonNode line : shortfall.get("items")) {
            assertThat(line.get("missing").asInt()).isZero();
        }
    }

    @Test
    @DisplayName("a target the reader is already past costs nothing, and says so in its own field")
    void alreadyThere() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profile(player, Map.of(), "insight-2");

        JsonNode shortfall = body(mvc.perform(get("/api/me/profiles/" + profile + "/shortfall")
                .param("entity", "warden")
                .param("target", "insight-1")
                .with(player)));

        // Two different facts that a single "nothing needed" would collapse:
        // this reader has finished, where the test above has the materials in
        // hand and has not spent them.
        assertThat(shortfall.get("alreadyMet").asBoolean()).isTrue();
        assertThat(shortfall.get("items")).isEmpty();
        assertThat(shortfall.get("complete").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("a reader who does not own her is quoted the whole track, not refused")
    void notOwnedIsAStartingPoint() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");

        // No roster entry at all — the case a forward walk over the graph cannot
        // answer, and the reason the resolver walks backwards.
        String profile = profileWithoutRoster(player);

        JsonNode shortfall = body(mvc.perform(get("/api/me/profiles/" + profile + "/shortfall")
                .param("entity", "warden")
                .param("target", "insight-2")
                .with(player)));

        assertThat(shortfall.get("currentStates")).isEmpty();
        assertThat(lines(shortfall.get("steps")))
                .containsExactlyInAnyOrder("warden-insight-1", "warden-insight-2");

        Map<String, JsonNode> byItem = byItem(shortfall);
        // Both steps' gold, summed: 5 000 for Insight 1 and 20 000 for Insight 2.
        assertThat(byItem.get("gold").get("required").asInt()).isEqualTo(25000);
    }

    @Test
    @DisplayName("a state the upgrade graph cannot reach is refused by name, not approximated")
    void anUnreachableStateIsARefusal() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profile(player, Map.of(), "insight-0");

        // 422: the request was understood and there is no answer, which is a
        // different thing from a request that needs fixing.
        mvc.perform(get("/api/me/profiles/" + profile + "/shortfall")
                        .param("entity", "warden")
                        .param("target", "insight-9")
                        .with(player))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(422));
    }

    @Test
    @DisplayName("an entity the patch has no upgrades for is refused, rather than costing nothing")
    void anUnknownEntityIsARefusal() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profile(player, Map.of(), "insight-0");

        mvc.perform(get("/api/me/profiles/" + profile + "/shortfall")
                        .param("entity", "nobody")
                        .param("target", "insight-1")
                        .with(player))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(422));
    }

    @Test
    @DisplayName("an entity asked of another game is refused by name, naming both games")
    void anotherGamesEntityIsRefusedByName() throws Exception {
        // The character page for a PGR construct once asked with the reader's
        // R1999 profile and got R1999's own parse error back. The page now says
        // which game it is showing, and the route names the mismatch.
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profile(player, Map.of(), "insight-0");

        MvcResult refused = mvc.perform(get("/api/me/profiles/" + profile + "/shortfall")
                        .param("entity", "samantha")
                        .param("target", "upper-resonance-1")
                        .param("game", "punishing-gray-raven")
                        .with(player))
                .andReturn();

        assertThat(refused.getResponse().getStatus()).isEqualTo(400);
        assertThat(json.readTree(refused.getResponse().getContentAsString()).get("detail").asText())
                .contains("plays proving-ground", "not punishing-gray-raven");
    }

    @Test
    @DisplayName("an entity asked of the profile's own game is answered as before")
    void theSameGameIsAnswered() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profile(player, Map.of(), "insight-0");

        JsonNode shortfall = body(mvc.perform(get("/api/me/profiles/" + profile + "/shortfall")
                .param("entity", "warden")
                .param("target", "insight-1")
                .param("game", "proving-ground")
                .with(player)));

        assertThat(lines(shortfall.get("steps"))).containsExactly("warden-insight-1");
    }

    @Test
    @DisplayName("another account's shortfall is not found, and an anonymous one is not answered")
    void theOverlayIsNobodyElsesBusiness() throws Exception {
        publish("proving-ground-1.0.json");
        RequestPostProcessor mine = signedIn("google", "sub-vertin", "Vertin");
        String profile = profile(mine, Map.of(), "insight-0");

        RequestPostProcessor theirs = signedIn("google", "sub-someone-else", "Someone Else");

        // 404 rather than 403, for the same reason every other /api/me route
        // does it: a 403 confirms the profile exists.
        mvc.perform(get("/api/me/profiles/" + profile + "/shortfall")
                        .param("entity", "warden")
                        .param("target", "insight-1")
                        .with(theirs))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(404));

        mvc.perform(get("/api/me/profiles/" + profile + "/shortfall")
                        .param("entity", "warden")
                        .param("target", "insight-1"))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isEqualTo(401));
    }

    // ── helpers ─────────────────────────────────────────────────────────────

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

    /** A profile owning Warden at {@code state}, holding {@code items}. */
    private String profile(RequestPostProcessor player, Map<String, Integer> items, String state)
            throws Exception {
        String profile = profileWithoutRoster(player);

        mvc.perform(put("/api/me/profiles/" + profile + "/inventory")
                .with(player)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("items", items))));

        mvc.perform(put("/api/me/profiles/" + profile + "/roster")
                .with(player)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("entities", Map.of("warden", List.of(state))))));

        return profile;
    }

    private String profileWithoutRoster(RequestPostProcessor player) throws Exception {
        return body(mvc.perform(post("/api/me/profiles")
                        .with(player)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                Map.of("game", "proving-ground", "region", "global", "displayName", "Main")))))
                .get("id")
                .asText();
    }

    private Map<String, JsonNode> byItem(JsonNode shortfall) {
        return java.util.stream.StreamSupport.stream(shortfall.get("items").spliterator(), false)
                .collect(java.util.stream.Collectors.toMap(line -> line.get("item").asText(), line -> line));
    }

    private List<String> lines(JsonNode array) {
        return java.util.stream.StreamSupport.stream(array.spliterator(), false)
                .map(JsonNode::asText)
                .toList();
    }

    private JsonNode body(org.springframework.test.web.servlet.ResultActions actions) throws Exception {
        MvcResult result = actions.andReturn();
        assertThat(result.getResponse().getStatus())
                .as("%s %s", result.getRequest().getMethod(), result.getRequest().getRequestURI())
                .isBetween(200, 299);
        return json.readTree(result.getResponse().getContentAsString());
    }

    private void publish(String fixture) {
        GameDataBundle bundle = bundle(fixture);
        ingest.ingestDraft(bundle);
        ingest.publish(PROVING_GROUND, bundle.sequence());
    }

    private static GameDataBundle bundle(String fixture) {
        try (InputStream in = ShortfallTest.class.getResourceAsStream("/gamedata/" + fixture)) {
            if (in == null) throw new IllegalStateException("fixture not on the classpath: " + fixture);
            return new CanonicalBundleParser().parse(in);
        } catch (IOException e) {
            throw new IllegalStateException("could not read " + fixture, e);
        }
    }
}
