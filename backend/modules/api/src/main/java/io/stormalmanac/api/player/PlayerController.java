package io.stormalmanac.api.player;

import io.stormalmanac.api.ResourceNotFoundException;
import io.stormalmanac.api.player.PlayerView.CreateProfileRequest;
import io.stormalmanac.api.player.PlayerView.GoalView;
import io.stormalmanac.api.player.PlayerView.GoalsRequest;
import io.stormalmanac.api.player.PlayerView.GoalsResponse;
import io.stormalmanac.api.player.PlayerView.InventoryRequest;
import io.stormalmanac.api.player.PlayerView.InventoryResponse;
import io.stormalmanac.api.player.PlayerView.MeResponse;
import io.stormalmanac.api.player.PlayerView.ProfileResponse;
import io.stormalmanac.api.player.PlayerView.RosterRequest;
import io.stormalmanac.api.player.PlayerView.RosterResponse;
import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.GameId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.common.id.ProfileId;
import io.stormalmanac.gamedata.Goal;
import io.stormalmanac.identity.AccountRepository;
import io.stormalmanac.player.Goals;
import io.stormalmanac.player.Inventory;
import io.stormalmanac.player.PlayerProfile;
import io.stormalmanac.player.PlayerStateRepository;
import io.stormalmanac.player.Roster;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * A player's own data: who they are, which profiles they run, and what each one
 * owns and wants.
 *
 * <pre>
 * GET  /api/me
 * POST /api/me/profiles
 * GET  /api/me/profiles/{profile}
 * GET  /api/me/profiles/{profile}/inventory
 * PUT  /api/me/profiles/{profile}/inventory
 * GET  /api/me/profiles/{profile}/roster
 * PUT  /api/me/profiles/{profile}/roster
 * GET  /api/me/profiles/{profile}/goals
 * PUT  /api/me/profiles/{profile}/goals
 * </pre>
 *
 * <p><b>Everything here is under {@code /api/me}, and that is load-bearing.</b>
 * There is no route that takes an account id, so there is no route whose
 * authorization can be forgotten — the only account these methods can reach is
 * the one that authenticated. A profile id still appears in the path, because a
 * person runs several, and {@link OwnedProfiles} is what makes it safe there.
 *
 * <p><b>PUT, not PATCH, and the difference is the whole contract.</b> Each of
 * these bodies is the complete inventory, roster or goal list, and saving one
 * replaces what was there. That is what the repository's signature says and what
 * the schema stores. A per-key patch is a different route with a different
 * method, and it is what offline sync needs — it is not written yet, and
 * pretending PUT is it would mean a phone that was offline for an hour silently
 * deleting everything a browser added in the meantime.
 */
@RestController
@RequestMapping("/api/me")
public class PlayerController {

    private final PlayerStateRepository players;
    private final AccountRepository accounts;
    private final OwnedProfiles owned;

    public PlayerController(PlayerStateRepository players, AccountRepository accounts, OwnedProfiles owned) {
        this.players = players;
        this.accounts = accounts;
        this.owned = owned;
    }

    @GetMapping
    public MeResponse me() {
        AccountId id = owned.account();
        return MeResponse.of(
                accounts.find(id).orElseThrow(() -> new ResourceNotFoundException("no account " + id.value())),
                players.profilesOf(id));
    }

    /**
     * The server owns the profile id.
     *
     * <p>A client-supplied one would let a caller name a profile that another
     * account already holds, and the unique constraint would then answer
     * "conflict" — which is a leak of the fact that the id is taken. It is also
     * simply not the client's to choose: nothing about a profile is derivable
     * from anything the client knows.
     */
    @PostMapping("/profiles")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse createProfile(@RequestBody CreateProfileRequest request) {
        PlayerProfile profile = new PlayerProfile(
                ProfileId.of(UUID.randomUUID().toString()),
                owned.account(),
                GameId.of(request.game()),
                request.displayName() == null || request.displayName().isBlank() ? "Main" : request.displayName(),
                request.region());
        players.saveProfile(profile);
        return ProfileResponse.of(profile);
    }

    @GetMapping("/profiles/{profile}")
    public ProfileResponse profile(@PathVariable String profile) {
        return ProfileResponse.of(owned.require(profile));
    }

    @GetMapping("/profiles/{profile}/inventory")
    public InventoryResponse inventory(@PathVariable String profile) {
        return InventoryResponse.of(players.inventoryOf(owned.require(profile).id()));
    }

    @PutMapping("/profiles/{profile}/inventory")
    public InventoryResponse saveInventory(@PathVariable String profile, @RequestBody InventoryRequest request) {
        ProfileId id = owned.require(profile).id();

        Map<ItemId, Integer> quantities = new LinkedHashMap<>();
        each(request.items()).forEach((slug, quantity) -> {
            if (quantity == null) {
                throw new IllegalArgumentException("item '" + slug + "' has no quantity");
            }
            if (quantity < 0) {
                throw new IllegalArgumentException(
                        "item '" + slug + "' has a negative quantity: " + quantity);
            }
            // Zero is accepted and dropped rather than refused. A client
            // clearing a field means "I have none of these", and making that an
            // error would force every client to filter its own form.
            if (quantity > 0) {
                quantities.put(ItemId.of(slug), quantity);
            }
        });

        Inventory inventory = new Inventory(id, quantities);
        players.saveInventory(inventory);
        return InventoryResponse.of(inventory);
    }

    @GetMapping("/profiles/{profile}/roster")
    public RosterResponse roster(@PathVariable String profile) {
        return RosterResponse.of(players.rosterOf(owned.require(profile).id()));
    }

    @PutMapping("/profiles/{profile}/roster")
    public RosterResponse saveRoster(@PathVariable String profile, @RequestBody RosterRequest request) {
        ProfileId id = owned.require(profile).id();

        Map<EntityId, String> states = new LinkedHashMap<>();
        each(request.entities()).forEach((slug, state) -> {
            if (state == null || state.isBlank()) {
                // An owned entity is always at some state, so a blank one is a
                // client bug rather than a way of saying "not owned" — that is
                // what leaving the key out means.
                throw new IllegalArgumentException("entity '" + slug + "' has no current state");
            }
            states.put(EntityId.of(slug), state);
        });

        Roster roster = new Roster(id, states);
        players.saveRoster(roster);
        return RosterResponse.of(roster);
    }

    @GetMapping("/profiles/{profile}/goals")
    public GoalsResponse goals(@PathVariable String profile) {
        return GoalsResponse.of(players.goalsOf(owned.require(profile).id()));
    }

    @PutMapping("/profiles/{profile}/goals")
    public GoalsResponse saveGoals(@PathVariable String profile, @RequestBody GoalsRequest request) {
        ProfileId id = owned.require(profile).id();

        List<GoalView> submitted = request.goals() == null ? List.of() : request.goals();
        List<Goal> goals = submitted.stream().map(PlayerController::goal).toList();

        Goals saved = new Goals(id, goals);
        players.saveGoals(saved);
        return GoalsResponse.of(saved);
    }

    private static Goal goal(GoalView view) {
        Goal.Satisfiability satisfiability = view.satisfiability() == null
                        || view.satisfiability().isBlank()
                ? Goal.Satisfiability.DETERMINISTIC
                : Goal.Satisfiability.valueOf(view.satisfiability().trim().toUpperCase());
        return new Goal(
                EntityId.of(view.entity()),
                view.targetState(),
                satisfiability,
                view.priority() == null ? 0 : view.priority());
    }

    /** An absent body member is an empty collection, never a null to walk into. */
    private static <K, V> Map<K, V> each(Map<K, V> value) {
        return value == null ? Map.of() : value;
    }
}
