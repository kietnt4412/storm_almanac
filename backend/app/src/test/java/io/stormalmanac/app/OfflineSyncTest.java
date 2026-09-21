package io.stormalmanac.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.identity.AccountRepository;
import io.stormalmanac.identity.oauth.AccountUserServices.AccountOidcUser;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
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
 * <b>N23 — the sync Phase 3's scope named and Phase 3 did not build.</b>
 *
 * <p>Everything here is one scenario in different clothes: a phone was in a
 * tunnel, a browser was not, and both have something to say about the same
 * profile. The route under test is what makes that a merge rather than a race,
 * and every assertion below is about a way the merge could be wrong
 * <em>silently</em> — a device deleting what it never saw, a stale edit winning
 * for arriving late, a cleared item coming back from the dead, a wrong clock
 * pinning a key forever.
 *
 * <p>These go through the same MockMvc surface as {@code PlanFromStoredStateTest}
 * and for the same reason: an authenticated session cannot be minted over a
 * socket without an authorization server to redirect to. The filter chain, the
 * dispatcher, Jackson and a real Postgres are all exercised; the servlet
 * container is not.
 */
class OfflineSyncTest extends SharedDatabaseTest {

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    // ── The thing sync exists for ───────────────────────────────────────────

    @Test
    @DisplayName("a patch touches only the keys it names, so a device that was away deletes nothing")
    void aPatchIsNotASave() throws Exception {
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileOf(player);

        // The browser, online, records two things an hour ago.
        patchInventory(
                player,
                profile,
                Map.of("gold", edit(5000, minutesAgo(60)), "sigil-lesser", edit(4, minutesAgo(60))));

        // The phone has been in a tunnel and has never heard of the sigil. It
        // says what it knows and nothing more.
        JsonNode merged = body(patchInventory(player, profile, Map.of("gold", edit(6000, minutesAgo(1)))));

        // A PUT of the same body would have deleted the sigil. This did not.
        assertThat(merged.get("items").get("gold").asInt()).isEqualTo(6000);
        assertThat(merged.get("items").get("sigil-lesser").asInt()).isEqualTo(4);
        assertThat(names(merged.get("applied"))).containsExactly("gold");
        assertThat(merged.get("rejected")).isEmpty();
    }

    @Test
    @DisplayName("two devices editing different keys both land, whichever order they arrive in")
    void differentKeysDoNotCollide() throws Exception {
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileOf(player);

        // The older edit arrives last. Per-key merging means the order on the
        // wire decides nothing.
        patchInventory(player, profile, Map.of("gold", edit(100, minutesAgo(5))));
        JsonNode merged = body(patchInventory(player, profile, Map.of("sigil-lesser", edit(7, minutesAgo(60)))));

        assertThat(merged.get("items").get("gold").asInt()).isEqualTo(100);
        assertThat(merged.get("items").get("sigil-lesser").asInt()).isEqualTo(7);
        assertThat(merged.get("rejected")).isEmpty();
    }

    // ── Which edit wins ─────────────────────────────────────────────────────

    @Test
    @DisplayName("the newer edit for a key wins even when it arrives first")
    void newerWinsRegardlessOfArrival() throws Exception {
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileOf(player);

        patchInventory(player, profile, Map.of("gold", edit(999, minutesAgo(5))));
        JsonNode merged = body(patchInventory(player, profile, Map.of("gold", edit(100, minutesAgo(60)))));

        // The hour-old edit lost, and the client is told so rather than left
        // showing 100 on a screen the server disagrees with.
        assertThat(merged.get("items").get("gold").asInt()).isEqualTo(999);
        assertThat(names(merged.get("rejected"))).containsExactly("gold");
        assertThat(merged.get("applied")).isEmpty();
    }

    @Test
    @DisplayName("an edit carrying exactly the stored timestamp leaves the stored value alone")
    void aTieGoesToWhatIsAlreadyThere() throws Exception {
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileOf(player);

        Instant sameInstant = minutesAgo(30);
        patchInventory(player, profile, Map.of("gold", edit(1, sameInstant)));
        JsonNode merged = body(patchInventory(player, profile, Map.of("gold", edit(2, sameInstant))));

        // Ties are not exotic — two devices saving within the same second is
        // ordinary — so the rule has to be stated rather than left to whichever
        // request the server happened to schedule first.
        assertThat(merged.get("items").get("gold").asInt()).isEqualTo(1);
        assertThat(names(merged.get("rejected"))).containsExactly("gold");
    }

    @Test
    @DisplayName("an edit dated in the future is clamped to now, so a wrong clock cannot pin a key")
    void aFastClockDoesNotWinForever() throws Exception {
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileOf(player);

        // A device whose clock is a year fast. Unclamped, its timestamp would
        // beat every edit made by anything for the next twelve months, and no
        // later write from any device could ever correct it.
        patchInventory(player, profile, Map.of("gold", edit(1, Instant.now().plus(Duration.ofDays(365)))));

        // A minute into the future is still the future and still clamped, so
        // this edit's effective time is simply later than the last one's.
        JsonNode merged =
                body(patchInventory(player, profile, Map.of("gold", edit(2, Instant.now().plusSeconds(60)))));

        assertThat(merged.get("items").get("gold").asInt()).isEqualTo(2);
        assertThat(names(merged.get("applied"))).containsExactly("gold");
    }

    // ── Removals, and why they need a tombstone ─────────────────────────────

    @Test
    @DisplayName("a cleared item stays cleared, because a removal keeps its clock after its row is gone")
    void aRemovalIsRememberedAfterTheRowIsGone() throws Exception {
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileOf(player);

        patchInventory(player, profile, Map.of("gold", edit(5000, minutesAgo(60))));
        // Spent it all. Zero is a removal, not a rejected value.
        patchInventory(player, profile, Map.of("gold", edit(0, minutesAgo(30))));

        assertThat(body(getInventory(player, profile)).get("items").has("gold")).isFalse();

        // The phone still thinks it has the gold, from before the spend. If the
        // clock had lived on the row it would have died with it, this edit would
        // find nothing to lose against, and five thousand gold would come back
        // from the dead with nobody told.
        JsonNode merged = body(patchInventory(player, profile, Map.of("gold", edit(5000, minutesAgo(45)))));

        assertThat(merged.get("items").has("gold")).isFalse();
        assertThat(names(merged.get("rejected"))).containsExactly("gold");
    }

    @Test
    @DisplayName("a removal older than the value it would clear is itself rejected")
    void aStaleRemovalLosesToo() throws Exception {
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileOf(player);

        patchInventory(player, profile, Map.of("gold", edit(5000, minutesAgo(10))));
        JsonNode merged = body(patchInventory(player, profile, Map.of("gold", edit(0, minutesAgo(90)))));

        // Deletion is not privileged. An old "I spent it" does not beat a fresh
        // "I have it" any more than an old quantity does.
        assertThat(merged.get("items").get("gold").asInt()).isEqualTo(5000);
        assertThat(names(merged.get("rejected"))).containsExactly("gold");
    }

    // ── Where PUT and PATCH meet ────────────────────────────────────────────

    @Test
    @DisplayName("replacing the whole inventory settles every key, so an older patch cannot undo it")
    void aSaveSettlesTheWholeAggregate() throws Exception {
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileOf(player);

        // The browser types a fresh inventory from scratch. That is a statement
        // about every key at once, including the ones it left out.
        putInventory(player, profile, Map.of("gold", 100));

        JsonNode merged = body(patchInventory(
                player, profile, Map.of("gold", edit(999, minutesAgo(1)), "sigil-lesser", edit(9, minutesAgo(1)))));

        // Without the save stamping the clock, both of these would have arrived
        // with nothing to lose against and the hour-old phone would have beaten
        // the browser that had just finished typing.
        assertThat(merged.get("items").get("gold").asInt()).isEqualTo(100);
        assertThat(merged.get("items").has("sigil-lesser")).isFalse();
        assertThat(names(merged.get("rejected"))).containsExactly("gold", "sigil-lesser");
    }

    @Test
    @DisplayName("a save also denies the keys it left out, so a stale patch cannot introduce one")
    void aSaveSpeaksForKeysThatHaveNeverExisted() throws Exception {
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileOf(player);

        // "This is my whole inventory" is a claim about every slug in the game,
        // including the ones the player has none of — and a per-key clock has no
        // row on which to write that down. Without a whole-aggregate mark this
        // patch would find nothing to lose against and the sigil would appear,
        // while the same stale edit for gold correctly lost. Rejecting one and
        // accepting the other is a rule nobody could explain.
        putInventory(player, profile, Map.of("gold", 100));

        JsonNode merged = body(patchInventory(player, profile, Map.of("sigil-lesser", edit(9, minutesAgo(90)))));

        assertThat(merged.get("items").has("sigil-lesser")).isFalse();
        assertThat(names(merged.get("rejected"))).containsExactly("sigil-lesser");

        // And an edit made after the save still lands, so the mark is a
        // watermark rather than a freeze.
        JsonNode after =
                body(patchInventory(player, profile, Map.of("sigil-lesser", edit(9, Instant.now().plusSeconds(5)))));
        assertThat(after.get("items").get("sigil-lesser").asInt()).isEqualTo(9);
    }

    // ── The roster, on the same three rules ─────────────────────────────────

    @Test
    @DisplayName("a roster patch merges per entity, and a null state takes one off the roster")
    void theRosterMergesOnTheSameTerms() throws Exception {
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileOf(player);

        patchRoster(
                player,
                profile,
                Map.of(
                        "warden", rosterEdit(minutesAgo(60), "insight-0"),
                        "sotheby", rosterEdit(minutesAgo(60), "insight-1")));

        Map<String, Object> edits = new HashMap<>();
        edits.put("warden", rosterEdit(minutesAgo(1), "insight-2"));
        // Null, not blank: "no longer on the roster" rather than "a form that
        // did not fill in".
        edits.put("sotheby", rosterEdit(minutesAgo(1)));

        JsonNode merged = body(patchRoster(player, profile, edits));

        assertThat(names(merged.get("entities").get("warden"))).containsExactly("insight-2");
        assertThat(merged.get("entities").has("sotheby")).isFalse();
        assertThat(names(merged.get("applied"))).containsExactly("sotheby", "warden");
    }

    @Test
    @DisplayName("a stale roster edit loses per entity, the same way an inventory one does")
    void aStaleRosterEditLoses() throws Exception {
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileOf(player);

        patchRoster(player, profile, Map.of("warden", rosterEdit(minutesAgo(5), "insight-2")));
        JsonNode merged =
                body(patchRoster(player, profile, Map.of("warden", rosterEdit(minutesAgo(60), "insight-0"))));

        assertThat(names(merged.get("entities").get("warden"))).containsExactly("insight-2");
        assertThat(names(merged.get("rejected"))).containsExactly("warden");
    }

    @Test
    @DisplayName("an entity's states move as one, so a state left out of a newer edit is given up")
    void theWholeStateSetIsTheMergeUnit() throws Exception {
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileOf(player);

        patchRoster(player, profile, Map.of("warden", rosterEdit(minutesAgo(60), "insight-1", "level-40")));

        // The same entity, later, on two tracks — one advanced and one dropped.
        // If the merge were per state rather than per entity, "level-40" would
        // survive this because nothing newer names it, and the roster would say
        // something no device ever said.
        JsonNode merged =
                body(patchRoster(player, profile, Map.of("warden", rosterEdit(minutesAgo(1), "insight-2"))));

        assertThat(names(merged.get("entities").get("warden"))).containsExactly("insight-2");
    }

    @Test
    @DisplayName("two devices on different entities both win, even when each names several states")
    void differentEntitiesDoNotCollide() throws Exception {
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileOf(player);

        patchRoster(player, profile, Map.of("warden", rosterEdit(minutesAgo(60), "insight-1", "level-40")));
        JsonNode merged = body(patchRoster(
                player, profile, Map.of("sotheby", rosterEdit(minutesAgo(1), "insight-2", "level-60"))));

        assertThat(names(merged.get("entities").get("warden"))).containsExactly("insight-1", "level-40");
        assertThat(names(merged.get("entities").get("sotheby"))).containsExactly("insight-2", "level-60");
    }

    // ── Refusals ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("an edit that does not say when it happened is refused rather than stamped by the server")
    void anEditMustSayWhenItHappened() throws Exception {
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileOf(player);

        Map<String, Object> undated = new HashMap<>();
        undated.put("quantity", 5);
        undated.put("at", null);

        // Defaulting to now() would make an edit win for having arrived late,
        // which is the bug this route exists to fix.
        mvc.perform(patch("/api/me/profiles/" + profile + "/inventory")
                        .with(player)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("items", Map.of("gold", undated)))))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("a blank roster state is refused, because it is a client bug and not a removal")
    void blankIsNotTheSameAsNull() throws Exception {
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileOf(player);

        mvc.perform(patch("/api/me/profiles/" + profile + "/roster")
                        .with(player)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                Map.of("entities", Map.of("warden", rosterEdit(minutesAgo(1), "   "))))))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("another account cannot patch this profile, and is told it does not exist")
    void aPatchIsAuthorizedLikeEverythingElse() throws Exception {
        RequestPostProcessor mine = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileOf(mine);
        putInventory(mine, profile, Map.of("gold", 5000));

        RequestPostProcessor theirs = signedIn("google", "sub-someone-else", "Someone Else");

        mvc.perform(patch("/api/me/profiles/" + profile + "/inventory")
                        .with(theirs)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("items", Map.of("gold", edit(0, Instant.now()))))))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(404));

        // And nothing of the owner's moved.
        assertThat(body(getInventory(mine, profile)).get("items").get("gold").asInt())
                .isEqualTo(5000);
    }

    @Test
    @DisplayName("a patch without a CSRF token is refused, signed in or not")
    void csrfCoversTheNewMethodToo() throws Exception {
        RequestPostProcessor player = signedIn("google", "sub-vertin", "Vertin");
        String profile = profileOf(player);

        mvc.perform(patch("/api/me/profiles/" + profile + "/inventory")
                        .with(player)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("items", Map.of("gold", edit(1, Instant.now()))))))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(403));
    }

    @Test
    @DisplayName("an anonymous caller cannot patch anything")
    void anonymousIsRefused() throws Exception {
        mvc.perform(patch("/api/me/profiles/anything/inventory")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(401));
    }

    // ── Plumbing ────────────────────────────────────────────────────────────

    private static Instant minutesAgo(int minutes) {
        return Instant.now().minus(Duration.ofMinutes(minutes));
    }

    private static Map<String, Object> edit(int quantity, Instant at) {
        return Map.of("quantity", quantity, "at", at.toString());
    }

    /**
     * A roster edit states the entity in full: the list replaces whatever was
     * held, because the entity is the merge unit and not the state. Null says
     * the entity is off the roster.
     */
    private static Map<String, Object> rosterEdit(Instant at, String... states) {
        Map<String, Object> edit = new HashMap<>();
        edit.put("states", states.length == 0 ? null : List.of(states));
        edit.put("at", at.toString());
        return edit;
    }

    private static List<String> names(JsonNode array) {
        List<String> values = new ArrayList<>();
        array.forEach(node -> values.add(node.asText()));
        return values;
    }

    private ResultActions patchInventory(RequestPostProcessor player, String profile, Map<String, ?> items)
            throws Exception {
        return mvc.perform(patch("/api/me/profiles/" + profile + "/inventory")
                        .with(player)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("items", items))))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(200));
    }

    private ResultActions patchRoster(RequestPostProcessor player, String profile, Map<String, ?> entities)
            throws Exception {
        return mvc.perform(patch("/api/me/profiles/" + profile + "/roster")
                        .with(player)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("entities", entities))))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(200));
    }

    private ResultActions getInventory(RequestPostProcessor player, String profile) throws Exception {
        return mvc.perform(get("/api/me/profiles/" + profile + "/inventory").with(player));
    }

    private void putInventory(RequestPostProcessor player, String profile, Map<String, Integer> items)
            throws Exception {
        mvc.perform(put("/api/me/profiles/" + profile + "/inventory")
                        .with(player)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("items", items))))
                .andExpect(status -> assertThat(status.getResponse().getStatus()).isEqualTo(200));
    }

    /** A profile owned by {@code player}. No game data is published: sync needs none. */
    private String profileOf(RequestPostProcessor player) throws Exception {
        return body(mvc.perform(post("/api/me/profiles")
                        .with(player)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                Map.of("game", "proving-ground", "region", "global", "displayName", "Main")))))
                .get("id")
                .asText();
    }

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

    private JsonNode body(ResultActions performed) throws Exception {
        return json.readTree(performed.andReturn().getResponse().getContentAsString());
    }
}
