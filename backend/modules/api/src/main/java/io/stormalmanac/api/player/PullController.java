package io.stormalmanac.api.player;

import io.stormalmanac.api.ResourceNotFoundException;
import io.stormalmanac.api.UnanswerableException;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gacha.DeclaredIncomeModel;
import io.stormalmanac.gacha.IncomeModel;
import io.stormalmanac.gacha.IncomeModel.PullBudget;
import io.stormalmanac.gacha.MarkovBannerEngine;
import io.stormalmanac.gacha.PityState;
import io.stormalmanac.gacha.PullModel;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.GameDefinitionRepository;
import io.stormalmanac.gamedata.Item;
import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.player.CarriedPity;
import io.stormalmanac.player.PlayerProfile;
import io.stormalmanac.player.PlayerStateRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * C1's exit, served: "how likely am I to get her, by when", from the reader's
 * own pity and the game's declared income.
 *
 * <pre>
 * GET  /api/me/profiles/{profile}/pity?banner=B
 * PUT  /api/me/profiles/{profile}/pity?banner=B
 * POST /api/me/profiles/{profile}/pulls
 * </pre>
 *
 * <p><b>Pity is addressed by banner, stored by scope.</b> A reader knows which
 * banner they are looking at and has never seen a scope key; the key is derived
 * here from the banner in the latest published version, so two Punishing: Gray
 * Raven pools of one type read and write one counter, as the game carries it.
 *
 * <p><b>The exact chain answers, and only it.</b> The simulation agrees with it
 * to within its error on every published banner ({@code EngineAgreementTest}),
 * and 500 000 trials per request on a 0.1-CPU host would spend seconds proving
 * that again. {@code method} is on the answer so a second engine can be added
 * without a client guessing which one spoke.
 *
 * <p><b>Always the latest version.</b> A banner is a thing that is live now; a
 * pinned patch would be asking about a pool that may have closed.
 */
@RestController
@RequestMapping("/api/me/profiles/{profile}")
public class PullController {

    private static final long SECONDS_PER_DAY = Duration.ofDays(1).toSeconds();

    private final PlayerStateRepository players;
    private final GameDefinitionRepository definitions;
    private final OwnedProfiles owned;
    private final IncomeModel income = new DeclaredIncomeModel();
    // The exact chain by its own type, because only it draws the curve.
    private final MarkovBannerEngine engine = new MarkovBannerEngine();

    public PullController(PlayerStateRepository players, GameDefinitionRepository definitions, OwnedProfiles owned) {
        this.players = players;
        this.definitions = definitions;
        this.owned = owned;
    }

    @GetMapping("/pity")
    public PityView pity(@PathVariable String profile, @RequestParam String banner) {
        PlayerProfile owner = owned.require(profile);
        BannerModel found = bannerOf(latest(owner), banner);
        return PityView.of(found, players.pityOf(owner.id(), PityState.scopeKeyOf(found)));
    }

    /**
     * What the reader's counter shows now. Both numbers are required: a counter
     * half-reported is one the planner would have to guess the other half of.
     */
    @PutMapping("/pity")
    public PityView savePity(
            @PathVariable String profile, @RequestParam String banner, @RequestBody PityRequest request) {
        PlayerProfile owner = owned.require(profile);
        BannerModel found = bannerOf(latest(owner), banner);
        if (request == null || request.pullsSinceHit() == null || request.consecutiveLosses() == null) {
            throw new IllegalArgumentException("pullsSinceHit and consecutiveLosses are both required");
        }
        CarriedPity pity = new CarriedPity(
                owner.id(), PityState.scopeKeyOf(found), request.pullsSinceHit(), request.consecutiveLosses());
        players.savePity(pity);
        return PityView.of(found, pity);
    }

    @PostMapping("/pulls")
    public OddsResponse odds(@PathVariable String profile, @RequestBody OddsRequest request) {
        PlayerProfile owner = owned.require(profile);
        if (request == null || request.banner() == null || request.days() == null) {
            // Not defaulted, as the plan route's energy is not: "by when" is the
            // question, and answering a horizon nobody asked for is answering a
            // different one.
            throw new IllegalArgumentException("banner and days are required");
        }
        if (request.days() < 0) {
            throw new IllegalArgumentException("a horizon of " + request.days() + " days counts backwards");
        }
        int copies = request.copies() == null ? 1 : request.copies();
        if (copies < 1) throw new IllegalArgumentException("copies must be at least 1, was " + copies);

        GameDefinition definition = latest(owner);
        BannerModel banner = bannerOf(definition, request.banner());
        if (banner.pullPrice() == null) {
            throw new UnanswerableException("banner " + banner.id().value()
                    + " does not say what a pull costs, so what you can afford on it cannot be worked out");
        }

        // The horizon stops at the banner's close: income that arrives after it
        // pulls on nothing. Whole days only, rounding down, as a cadence does.
        Instant now = Instant.now();
        Instant closes = banner.window().closesAt();
        if (closes != null && !now.isBefore(closes)) {
            throw new UnanswerableException(banner.displayName() + " closed at " + closes);
        }
        int days = request.days();
        boolean cappedAtClose = false;
        if (closes != null) {
            int left = (int) (Duration.between(now, closes).toSeconds() / SECONDS_PER_DAY);
            if (days > left) {
                days = left;
                cappedAtClose = true;
            }
        }

        Map<String, Integer> reach = request.reach() == null ? Map.of() : request.reach();
        PullBudget budget = income.affordableWithin(
                definition, banner, players.inventoryOf(owner.id()), days, reach);

        CarriedPity carried = players.pityOf(owner.id(), PityState.scopeKeyOf(banner));
        PityState from = new PityState(
                banner.pityScope(), carried.scopeKey(), carried.pullsSinceHit(), carried.consecutiveLosses());
        PullModel model = PullModel.of(banner);
        long worstCase = model.worstCasePullsFrom(from, copies);

        // Past the worst case the answer is certainty, and the chain need not
        // walk every pull of a very large balance to say so. The curve runs to
        // the worst case whatever the budget, so the chart shows the whole shape
        // and marks where this reader's pulls run out on it; the chance is read
        // off the same curve, so the number and the chart are one answer.
        int asked = (int) Math.min(budget.pulls(), worstCase);
        double[] curve = engine.curveOfFeatured(banner, from, (int) worstCase, copies);
        double chance = curve[asked];

        return new OddsResponse(
                banner.id().value(),
                banner.displayName(),
                definition.version().sequence(),
                definition.version().label(),
                request.days(),
                days,
                cappedAtClose,
                closes,
                copies,
                PityView.of(banner, carried),
                BudgetView.of(budget, definition.itemsById()),
                chance,
                engine.expectedPullsToFeatured(banner, from),
                worstCase,
                CurveView.of(curve),
                engine.method());
    }

    private GameDefinition latest(PlayerProfile owner) {
        return definitions.findLatest(owner.game())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "no published version of " + owner.game().value()));
    }

    private static BannerModel bannerOf(GameDefinition definition, String banner) {
        return definition.banners().stream()
                .filter(candidate -> candidate.id().value().equals(banner))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("no banner '" + banner + "' in "
                        + definition.game().id().value() + " " + definition.version().label()));
    }

    // ── Wire ────────────────────────────────────────────────────────────────

    public record PityRequest(Integer pullsSinceHit, Integer consecutiveLosses) {}

    /**
     * @param days   how many days ahead to count income
     * @param copies how many copies of the featured unit; one when absent
     * @param reach  how far the reader gets on each scored ladder (ADR 0022)
     */
    public record OddsRequest(String banner, Integer days, Integer copies, Map<String, Integer> reach) {}

    /**
     * @param scopeKey       the counter this banner shares, so a screen can say
     *                       that another banner of its type moves it too
     * @param guaranteedNext whether the next hit is certain to be the featured unit
     */
    public record PityView(
            String banner, String scopeKey, int pullsSinceHit, int consecutiveLosses, boolean guaranteedNext,
            int hardAt) {

        static PityView of(BannerModel banner, CarriedPity pity) {
            PullModel model = PullModel.of(banner);
            return new PityView(
                    banner.id().value(),
                    pity.scopeKey(),
                    pity.pullsSinceHit(),
                    pity.consecutiveLosses(),
                    model.featuredChanceAfter(pity.consecutiveLosses()) >= 1.0,
                    model.hardAt());
        }
    }

    /** One item counted as pull currency through a conversion, named. */
    public record ConvertedView(String item, String displayName, long held, long accruing, List<String> via) {}

    public record BudgetView(
            String currency,
            String currencyName,
            long held,
            long accruing,
            int perPull,
            long pulls,
            List<String> uncounted,
            List<ConvertedView> converted) {

        static BudgetView of(PullBudget budget, Map<ItemId, Item> items) {
            return new BudgetView(
                    budget.currency(),
                    nameOf(budget.currency(), items),
                    budget.held(),
                    budget.accruing(),
                    budget.perPull(),
                    budget.pulls(),
                    budget.uncounted(),
                    budget.converted().stream()
                            .map(converted -> new ConvertedView(
                                    converted.item(),
                                    nameOf(converted.item(), items),
                                    converted.held(),
                                    converted.accruing(),
                                    converted.via()))
                            .toList());
        }

        private static String nameOf(String item, Map<ItemId, Item> items) {
            Item found = items.get(new ItemId(item));
            return found == null ? item : found.displayName();
        }
    }

    /**
     * @param daysAsked      the horizon the reader asked for
     * @param days           the horizon counted, which is shorter when the
     *                       banner closes first — and then {@code cappedAtClose}
     * @param chance         probability of at least {@code copies} featured
     *                       copies within the pulls the budget affords
     * @param expectedPulls  expected pulls to the first featured copy from here
     * @param worstCasePulls the most pulls {@code copies} copies can take from here
     * @param curve          the chance at every pull count from none to the worst
     *                       case; {@code chance} is its point at the pulls the
     *                       budget affords
     */
    public record OddsResponse(
            String banner,
            String bannerName,
            long versionSequence,
            String versionLabel,
            int daysAsked,
            int days,
            boolean cappedAtClose,
            Instant closesAt,
            int copies,
            PityView pity,
            BudgetView budget,
            double chance,
            double expectedPulls,
            long worstCasePulls,
            List<Double> curve,
            String method) {}

    /**
     * The curve as the wire carries it: rounded to four decimals, because a
     * chart is drawn in pixels and nobody reads a chance past 0.01%. Unrounded,
     * six copies' 720 points were 13 KB of digits nobody looks at.
     */
    static final class CurveView {
        private CurveView() {}

        static List<Double> of(double[] curve) {
            return Arrays.stream(curve)
                    .map(chance -> Math.round(chance * 10_000) / 10_000.0)
                    .boxed()
                    .toList();
        }
    }
}
