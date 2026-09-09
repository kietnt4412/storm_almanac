package io.stormalmanac.api.player;

import io.stormalmanac.api.ResourceNotFoundException;
import io.stormalmanac.api.player.PlayerView.CreateProfileRequest;
import io.stormalmanac.api.player.PlayerView.GoalView;
import io.stormalmanac.api.player.PlayerView.GoalsRequest;
import io.stormalmanac.api.player.PlayerView.GoalsResponse;
import io.stormalmanac.api.player.PlayerView.InventoryPatchRequest;
import io.stormalmanac.api.player.PlayerView.InventoryPatchResponse;
import io.stormalmanac.api.player.PlayerView.InventoryRequest;
import io.stormalmanac.api.player.PlayerView.InventoryResponse;
import io.stormalmanac.api.player.PlayerView.MeResponse;
import io.stormalmanac.api.player.PlayerView.ProfileResponse;
import io.stormalmanac.api.player.PlayerView.RosterPatchRequest;
import io.stormalmanac.api.player.PlayerView.RosterPatchResponse;
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
import io.stormalmanac.player.InventoryEdit;
import io.stormalmanac.player.MergeOutcome;
import io.stormalmanac.player.PlayerProfile;
import io.stormalmanac.player.PlayerStateRepository;
import io.stormalmanac.player.Roster;
import io.stormalmanac.player.RosterEdit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
 * GET   /api/me/profiles/{profile}/inventory
 * PUT   /api/me/profiles/{profile}/inventory
 * PATCH /api/me/profiles/{profile}/inventory
 * GET   /api/me/profiles/{profile}/roster
 * PUT   /api/me/profiles/{profile}/roster
 * PATCH /api/me/profiles/{profile}/roster
 * GET   /api/me/profiles/{profile}/goals
 * PUT   /api/me/profiles/{profile}/goals
 * </pre>
 *
 * <p><b>Everything here is under {@code /api/me}, and that is load-bearing.</b>
 * There is no route that takes an account id, so there is no route whose
 * authorization can be forgotten — the only account these methods can reach is
 * the one that authenticated. A profile id still appears in the path, because a
 * person runs several, and {@link OwnedProfiles} is what makes it safe there.
 *
 * <p><b>PUT and PATCH are different contracts, not two spellings of save.</b> A
 * PUT body is the complete inventory, roster or goal list and replaces what was
 * there; that is what the repository's signature says and what the schema
 * stores. A PATCH body names only the keys one device changed and when it
 * changed them, and merges them per key. Collapsing the two — treating PUT as
 * the sync route — is what makes a phone that was offline for an hour silently
 * delete everything a browser added in the meantime.
 *
 * <p><b>Goals have no PATCH, and that is a decision rather than an omission.</b>
 * An inventory and a roster are maps, so a key is a merge unit and two devices
 * touching different keys have an obvious answer. Goals are an ordered list
 * whose order is itself what the player is editing, and two devices that
 * reordered it have no per-key answer at all. A PATCH route here would have to
 * invent one, and inventing one would mean a plan computed against priorities
 * nobody chose.
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

    /**
     * Merge an offline device's inventory edits, key by key.
     *
     * <p>Returns the merged inventory <em>and</em> the keys whose edits lost, so
     * a client is never left displaying a value the server did not take. The
     * response is 200 rather than 207 even when some keys were rejected: the
     * merge succeeded, and losing a tiebreak is the route working rather than
     * failing.
     */
    @PatchMapping("/profiles/{profile}/inventory")
    public InventoryPatchResponse patchInventory(
            @PathVariable String profile, @RequestBody InventoryPatchRequest request) {
        ProfileId id = owned.require(profile).id();

        List<InventoryEdit> edits = new ArrayList<>();
        each(request.items()).forEach((slug, edit) -> {
            if (edit == null || edit.quantity() == null) {
                throw new IllegalArgumentException("item '" + slug + "' has no quantity");
            }
            if (edit.quantity() < 0) {
                throw new IllegalArgumentException(
                        "item '" + slug + "' has a negative quantity: " + edit.quantity());
            }
            // Required rather than defaulted to now(): a server-stamped edit
            // wins for having arrived late, which is the bug this route exists
            // to fix rather than a convenient default.
            if (edit.at() == null) {
                throw new IllegalArgumentException("item '" + slug + "' does not say when it was edited");
            }
            edits.add(new InventoryEdit(ItemId.of(slug), edit.quantity(), edit.at()));
        });

        MergeOutcome<ItemId> outcome = players.mergeInventory(id, edits);
        return InventoryPatchResponse.of(players.inventoryOf(id), outcome);
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

    /**
     * Merge an offline device's roster edits, key by key.
     *
     * <p>A null or absent {@code state} removes the entity from the roster, and
     * is the one place a client can say "I no longer own this" — the PUT route
     * says it by leaving the key out, which a patch by definition cannot.
     */
    @PatchMapping("/profiles/{profile}/roster")
    public RosterPatchResponse patchRoster(@PathVariable String profile, @RequestBody RosterPatchRequest request) {
        ProfileId id = owned.require(profile).id();

        List<RosterEdit> edits = new ArrayList<>();
        each(request.entities()).forEach((slug, edit) -> {
            if (edit == null) {
                throw new IllegalArgumentException("entity '" + slug + "' has no edit");
            }
            if (edit.at() == null) {
                throw new IllegalArgumentException("entity '" + slug + "' does not say when it was edited");
            }
            // Blank is refused where null is accepted: null is "no longer
            // owned", blank is a form that did not fill in, and treating the
            // second as the first would delete a roster entry on a client bug.
            if (edit.state() != null && edit.state().isBlank()) {
                throw new IllegalArgumentException("entity '" + slug + "' has a blank current state");
            }
            edits.add(new RosterEdit(EntityId.of(slug), edit.state(), edit.at()));
        });

        MergeOutcome<EntityId> outcome = players.mergeRoster(id, edits);
        return RosterPatchResponse.of(players.rosterOf(id), outcome);
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
