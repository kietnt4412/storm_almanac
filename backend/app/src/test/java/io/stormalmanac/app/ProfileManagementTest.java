package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.identity.AccountRepository;
import io.stormalmanac.identity.oauth.AccountUserServices.AccountOidcUser;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * A profile can be renamed and deleted by its owner, and by nobody else.
 *
 * <p>The first note the five strangers who closed Phase 4 left: a profile, once
 * made, could not be managed at all. With one profile per game per server, a
 * profile made on the wrong server also blocked the right one from being made.
 *
 * <p>The delete is one row and the schema cascades the rest, so what is under
 * test is the schema doing that — counted in the tables, not inferred from a
 * 404 — and the edge refusing everyone but the owner.
 */
class ProfileManagementTest extends SharedDatabaseTest {

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    @DisplayName("renaming a profile changes its name and nothing else")
    void renameChangesTheNameOnly() throws Exception {
        RequestPostProcessor player = signedIn("sub-lucia", "Lucia");
        String profile = profileOf(player, "global");

        JsonNode renamed = body(rename(player, profile, "  Alt account  ").andExpect(status(200)));

        assertThat(renamed.get("displayName").asText()).isEqualTo("Alt account");
        assertThat(renamed.get("game").asText()).isEqualTo("proving-ground");
        assertThat(renamed.get("region").asText()).isEqualTo("global");
        assertThat(body(mvc.perform(get("/api/me/profiles/" + profile).with(player)))
                        .get("displayName")
                        .asText())
                .isEqualTo("Alt account");
    }

    @Test
    @DisplayName("a blank name is refused rather than silently defaulted")
    void aBlankNameIsRefused() throws Exception {
        RequestPostProcessor player = signedIn("sub-lucia", "Lucia");
        String profile = profileOf(player, "global");

        rename(player, profile, "   ").andExpect(status(400));

        assertThat(body(mvc.perform(get("/api/me/profiles/" + profile).with(player)))
                        .get("displayName")
                        .asText())
                .isEqualTo("Main");
    }

    @Test
    @DisplayName("deleting a profile deletes everything it owns, and no other profile's")
    void deleteTakesEverythingItOwns() throws Exception {
        RequestPostProcessor player = signedIn("sub-lucia", "Lucia");
        String doomed = profileOf(player, "global");
        String kept = profileOf(player, "asia");
        for (String profile : List.of(doomed, kept)) {
            putJson(player, "/api/me/profiles/" + profile + "/inventory", Map.of("items", Map.of("cogs", 5000)));
            putJson(player, "/api/me/profiles/" + profile + "/roster",
                    Map.of("entities", Map.of("lucia", List.of("level-10"))));
            putJson(player, "/api/me/profiles/" + profile + "/goals",
                    Map.of("goals", List.of(Map.of("entity", "lucia", "targetState", "level-80"))));
            patchJson(player, "/api/me/profiles/" + profile + "/inventory",
                    Map.of("items", Map.of("cogs", Map.of("quantity", 6000, "at", Instant.now().toString()))));
        }

        mvc.perform(delete("/api/me/profiles/" + doomed).with(player).with(csrf())).andExpect(status(204));

        mvc.perform(get("/api/me/profiles/" + doomed).with(player)).andExpect(status(404));
        for (String table : List.of("inventory_item", "roster_entry", "goal", "sync_clock", "sync_watermark")) {
            assertThat(rowsOf(table, doomed)).as("%s rows left for the deleted profile", table).isZero();
        }
        assertThat(rowsOf("inventory_item", kept)).isEqualTo(1);
        assertThat(rowsOf("roster_entry", kept)).isEqualTo(1);
        assertThat(rowsOf("goal", kept)).isEqualTo(1);
        assertThat(rowsOf("sync_clock", kept)).isPositive();
        assertThat(body(mvc.perform(get("/api/me").with(player))).get("profiles"))
                .extracting(node -> node.get("id").asText())
                .containsExactly(kept);
    }

    @Test
    @DisplayName("deleting a profile frees its game and server for a new one")
    void deleteFreesThePlace() throws Exception {
        RequestPostProcessor player = signedIn("sub-lucia", "Lucia");
        String wrong = profileOf(player, "global");

        mvc.perform(delete("/api/me/profiles/" + wrong).with(player).with(csrf())).andExpect(status(204));

        assertThat(profileOf(player, "global")).isNotEqualTo(wrong);
    }

    @Test
    @DisplayName("another account's profile can be neither renamed nor deleted, and reads as not found")
    void onlyTheOwnerManagesAProfile() throws Exception {
        RequestPostProcessor owner = signedIn("sub-lucia", "Lucia");
        RequestPostProcessor stranger = signedIn("sub-liv", "Liv");
        String profile = profileOf(owner, "global");

        rename(stranger, profile, "Mine now").andExpect(status(404));
        mvc.perform(delete("/api/me/profiles/" + profile).with(stranger).with(csrf())).andExpect(status(404));

        JsonNode still = body(mvc.perform(get("/api/me/profiles/" + profile).with(owner)).andExpect(status(200)));
        assertThat(still.get("displayName").asText()).isEqualTo("Main");
    }

    // ── Plumbing ────────────────────────────────────────────────────────────

    private int rowsOf(String table, String profile) {
        Integer rows = jdbc.queryForObject(
                "SELECT count(*) FROM player." + table + " WHERE profile_id = ?", Integer.class, profile);
        return rows == null ? 0 : rows;
    }

    private static org.springframework.test.web.servlet.ResultMatcher status(int expected) {
        return performed -> assertThat(performed.getResponse().getStatus()).isEqualTo(expected);
    }

    private ResultActions rename(RequestPostProcessor player, String profile, String name) throws Exception {
        return mvc.perform(patch("/api/me/profiles/" + profile)
                .with(player)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("displayName", name))));
    }

    private void putJson(RequestPostProcessor player, String path, Object body) throws Exception {
        mvc.perform(put(path)
                        .with(player)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status(200));
    }

    private void patchJson(RequestPostProcessor player, String path, Object body) throws Exception {
        mvc.perform(patch(path)
                        .with(player)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status(200));
    }

    /** A profile owned by {@code player}. No game data is published: nothing here needs any. */
    private String profileOf(RequestPostProcessor player, String region) throws Exception {
        return body(mvc.perform(post("/api/me/profiles")
                                .with(player)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json.writeValueAsString(
                                        Map.of("game", "proving-ground", "region", region, "displayName", "Main"))))
                        .andExpect(status(201)))
                .get("id")
                .asText();
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

    private JsonNode body(ResultActions performed) throws Exception {
        return json.readTree(performed.andReturn().getResponse().getContentAsString());
    }
}
