package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
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
 * <b>C1's exit, asked over HTTP:</b> a signed-in reader's stored pity and the
 * game's declared income answer "how likely by when" for a live banner.
 *
 * <p>Against a synthetic game rather than the launch title, because the launch
 * title's live banner closes on a date and a test that reads it would start
 * failing the day after. {@code AuthoredBundleIncomeTest} pins the real
 * bundle's numbers without a clock.
 */
class PullPlannerTest extends SharedDatabaseTest {

    private static final GameId TIDEWATER = new GameId("tidewater");

    @Autowired
    private GameDataIngestRepository ingest;

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private RequestPostProcessor reader;

    private String profile;

    @BeforeEach
    void aReaderOnTidewater() throws Exception {
        GameDataBundle bundle = bundle("tidewater-1.0.json");
        ingest.ingestDraft(bundle);
        ingest.publish(TIDEWATER, bundle.sequence());

        reader = signedIn("sub-reader", "Reader");
        profile = body(mvc.perform(post("/api/me/profiles")
                        .with(reader)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                Map.of("game", "tidewater", "region", "global", "displayName", "Main")))))
                .get("id")
                .asText();
    }

    @Test
    @DisplayName("a banner's rates, wall, price and window are on the catalog, anonymously")
    void bannersAreCatalogData() throws Exception {
        Map<String, JsonNode> banners = byId(body(mvc.perform(get("/api/games/tidewater/banners"))).get("banners"));

        JsonNode certain = banners.get("tide-certain");
        assertThat(certain.get("hardAt").asInt()).isEqualTo(60);
        assertThat(certain.get("baseRate").asDouble()).isEqualTo(0.005);
        // One hit is enough at 100%, so the worst case is the wall; at 70%
        // with a guarantee after one loss it is the wall twice.
        assertThat(certain.get("worstCasePulls").asLong()).isEqualTo(60);
        assertThat(banners.get("tide-split").get("worstCasePulls").asLong()).isEqualTo(120);
        assertThat(certain.get("pullPrice").get("currencyName").asText()).isEqualTo("Tide Ticket");
        assertThat(certain.get("pullPrice").get("perPull").asInt()).isEqualTo(250);
        assertThat(certain.get("open").asBoolean()).isTrue();

        assertThat(banners.get("tide-gone").get("open").asBoolean()).isFalse();
        assertThat(banners.get("tide-unpriced").get("pullPrice").isNull()).isTrue();
    }

    @Test
    @DisplayName("each ladder says whether it pays for pulls, for plans, or both")
    void measuresSayWhatTheyPayFor() throws Exception {
        Map<String, JsonNode> measures = StreamSupport
                .stream(body(mvc.perform(get("/api/games/tidewater/measures"))).get("measures").spliterator(), false)
                .collect(Collectors.toMap(measure -> measure.get("measure").asText(), measure -> measure));

        // Cards exchange into the ticket, so a ladder paying only cards is a
        // pull screen's question and not a plan's; one paying cards and shells
        // is both; one paying only shells is a plan's.
        assertThat(flags(measures.get("daily-bar"))).containsExactly(true, false);
        assertThat(flags(measures.get("arena"))).containsExactly(true, true);
        assertThat(flags(measures.get("chores"))).containsExactly(false, true);
    }

    @Test
    @DisplayName("a pity counter is stored, and two pools of one type share it")
    void pityIsStoredByScope() throws Exception {
        JsonNode fresh = body(mvc.perform(get(pity("tide-certain")).with(reader)));
        assertThat(fresh.get("pullsSinceHit").asInt()).isZero();
        assertThat(fresh.get("scopeKey").asText()).isEqualTo("type:tide");

        mvc.perform(put(pity("tide-certain"))
                .with(reader)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pullsSinceHit\":45,\"consecutiveLosses\":1}"));

        // Written on one pool and read on its sibling: the game carries pity
        // between pools of a type, so the reader types it once.
        JsonNode sibling = body(mvc.perform(get(pity("tide-split")).with(reader)));
        assertThat(sibling.get("pullsSinceHit").asInt()).isEqualTo(45);
        assertThat(sibling.get("consecutiveLosses").asInt()).isEqualTo(1);
        assertThat(sibling.get("guaranteedNext").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("half a counter is refused rather than guessed")
    void aHalfReportedCounterIsRefused() throws Exception {
        MvcResult refused = mvc.perform(put(pity("tide-certain"))
                        .with(reader)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pullsSinceHit\":45}"))
                .andReturn();

        assertThat(refused.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("held cards count as tickets, and fifteen pulls from 45 reach a wall of 60")
    void theStoredCounterAndTheBalanceAnswerTogether() throws Exception {
        holding(Map.of("tide-card", 3_750));
        savePity("tide-certain", 45, 0);

        JsonNode odds = odds(Map.of("banner", "tide-certain", "days", 0));

        assertThat(odds.at("/budget/held").asLong()).isEqualTo(3_750);
        assertThat(odds.at("/budget/pulls").asLong()).isEqualTo(15);
        assertThat(odds.at("/budget/converted/0/displayName").asText()).isEqualTo("Tide Card");
        assertThat(odds.at("/pity/pullsSinceHit").asInt()).isEqualTo(45);
        assertThat(odds.get("worstCasePulls").asLong()).isEqualTo(15);
        assertThat(odds.get("chance").asDouble()).isEqualTo(1.0);
        assertThat(odds.get("method").asText()).isNotBlank();
    }

    @Test
    @DisplayName("income counts only the ladders the reader says they reach, and names the rest")
    void incomeIsWhatTheReaderAnswersFor() throws Exception {
        // A week of the daily bar at the top and the arena: 7 x 30 + 100 = 310
        // cards, one pull, and from a fresh counter one pull is the base rate.
        JsonNode answered = odds(Map.of(
                "banner", "tide-certain", "days", 7, "reach", Map.of("daily-bar", 100, "arena", 10)));
        assertThat(answered.at("/budget/accruing").asLong()).isEqualTo(310);
        assertThat(answered.at("/budget/pulls").asLong()).isEqualTo(1);
        assertThat(answered.get("chance").asDouble()).isCloseTo(0.005, within(1e-12));

        JsonNode silent = odds(Map.of("banner", "tide-certain", "days", 7));
        assertThat(silent.at("/budget/accruing").asLong()).isZero();
        assertThat(silent.at("/budget/uncounted")).hasSize(2);
    }

    @Test
    @DisplayName("a horizon past the banner's close stops at the close, and says so")
    void theHorizonStopsAtTheClose() throws Exception {
        JsonNode odds = odds(Map.of("banner", "tide-certain", "days", 40_000));

        assertThat(odds.get("daysAsked").asInt()).isEqualTo(40_000);
        assertThat(odds.get("days").asInt()).isLessThan(40_000);
        assertThat(odds.get("cappedAtClose").asBoolean()).isTrue();
        assertThat(Instant.parse(odds.get("closesAt").asText())).isEqualTo(Instant.parse("2099-01-01T00:00:00Z"));
    }

    @Test
    @DisplayName("two copies on a split pool from a fresh counter take up to 240 pulls")
    void copiesAddAWorstCaseEach() throws Exception {
        JsonNode odds = odds(Map.of("banner", "tide-split", "days", 0, "copies", 2));

        assertThat(odds.get("worstCasePulls").asLong()).isEqualTo(240);
        assertThat(odds.get("chance").asDouble()).isZero();
    }

    @Test
    @DisplayName("a closed banner and an unpriced one are refused by name, and no horizon is not a question")
    void unanswerableQuestionsAreRefused() throws Exception {
        MvcResult closed = oddsResult(Map.of("banner", "tide-gone", "days", 7));
        assertThat(closed.getResponse().getStatus()).isEqualTo(422);
        assertThat(closed.getResponse().getContentAsString()).contains("The Tide That Went Out", "closed");

        MvcResult unpriced = oddsResult(Map.of("banner", "tide-unpriced", "days", 7));
        assertThat(unpriced.getResponse().getStatus()).isEqualTo(422);
        assertThat(unpriced.getResponse().getContentAsString()).contains("does not say what a pull costs");

        assertThat(oddsResult(Map.of("banner", "tide-certain")).getResponse().getStatus()).isEqualTo(400);
        assertThat(oddsResult(Map.of("banner", "no-such-tide", "days", 1)).getResponse().getStatus())
                .isEqualTo(404);
    }

    @Test
    @DisplayName("another reader's pity and odds are not found, and an anonymous caller is not answered")
    void pityIsNobodyElsesBusiness() throws Exception {
        RequestPostProcessor someoneElse = signedIn("sub-someone-else", "Someone Else");

        assertThat(mvc.perform(get(pity("tide-certain")).with(someoneElse)).andReturn().getResponse().getStatus())
                .isEqualTo(404);
        assertThat(mvc.perform(get(pity("tide-certain"))).andReturn().getResponse().getStatus())
                .isEqualTo(401);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private String pity(String banner) {
        return "/api/me/profiles/" + profile + "/pity?banner=" + banner;
    }

    private void savePity(String banner, int pulls, int losses) throws Exception {
        body(mvc.perform(put(pity(banner))
                .with(reader)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("pullsSinceHit", pulls, "consecutiveLosses", losses)))));
    }

    private void holding(Map<String, Integer> items) throws Exception {
        body(mvc.perform(put("/api/me/profiles/" + profile + "/inventory")
                .with(reader)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("items", items)))));
    }

    private JsonNode odds(Map<String, Object> request) throws Exception {
        MvcResult result = oddsResult(request);
        assertThat(result.getResponse().getStatus())
                .as(result.getResponse().getContentAsString())
                .isEqualTo(200);
        return json.readTree(result.getResponse().getContentAsString());
    }

    private MvcResult oddsResult(Map<String, Object> request) throws Exception {
        return mvc.perform(post("/api/me/profiles/" + profile + "/pulls")
                        .with(reader)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new HashMap<>(request))))
                .andReturn();
    }

    private static List<Boolean> flags(JsonNode measure) {
        return List.of(measure.get("paysForPulls").asBoolean(), measure.get("paysForPlans").asBoolean());
    }

    private static Map<String, JsonNode> byId(JsonNode array) {
        return StreamSupport.stream(array.spliterator(), false)
                .collect(Collectors.toMap(node -> node.get("id").asText(), node -> node));
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

    private JsonNode body(ResultActions actions) throws Exception {
        MvcResult result = actions.andReturn();
        assertThat(result.getResponse().getStatus())
                .as("%s %s: %s", result.getRequest().getMethod(), result.getRequest().getRequestURI(),
                        result.getResponse().getContentAsString())
                .isBetween(200, 299);
        return json.readTree(result.getResponse().getContentAsString());
    }

    private static GameDataBundle bundle(String fixture) {
        try (InputStream in = PullPlannerTest.class.getResourceAsStream("/gamedata/" + fixture)) {
            if (in == null) throw new IllegalStateException("fixture not on the classpath: " + fixture);
            return new CanonicalBundleParser().parse(in);
        } catch (IOException e) {
            throw new IllegalStateException("could not read " + fixture, e);
        }
    }
}
