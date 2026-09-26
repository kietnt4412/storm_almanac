package io.stormalmanac.gamedata.ingest;

import io.stormalmanac.common.GameDataVersion;
import io.stormalmanac.common.id.EntityId;
import io.stormalmanac.common.id.ItemId;
import io.stormalmanac.gamedata.Craft;
import io.stormalmanac.gamedata.FactRef;
import io.stormalmanac.gamedata.Fodder;
import io.stormalmanac.gamedata.Game;
import io.stormalmanac.gamedata.GameDefinition;
import io.stormalmanac.gamedata.Item;
import io.stormalmanac.gamedata.ItemStack;
import io.stormalmanac.gamedata.ProgressKind;
import io.stormalmanac.gamedata.Provenance;
import io.stormalmanac.gamedata.Reward;
import io.stormalmanac.gamedata.Shop;
import io.stormalmanac.gamedata.Sink;
import io.stormalmanac.gamedata.Source;
import io.stormalmanac.gamedata.Stage;
import io.stormalmanac.gamedata.Upgrade;
import io.stormalmanac.gamedata.Word;
import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.gamedata.catalog.Entity;
import io.stormalmanac.gamedata.catalog.Skill;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * One snapshot of a game's data as an adapter produced it, before anyone
 * approved it.
 *
 * <p>This is deliberately <em>not</em> a {@link GameDefinition}. A definition
 * carries a {@link GameDataVersion}, and that record requires a non-null
 * {@code publishedAt} — so an unapproved snapshot has no representation as a
 * definition at all, and the type system says what the schema's
 * {@code game_data_version_published_has_timestamp} constraint says. Ingest
 * produces a bundle; approval turns it into a definition.
 *
 * <p>Onboarding a title is writing something that produces one of these. That
 * is the whole contract a parser adapter has to meet, and the reason the
 * validation below lives here rather than in any one adapter: every adapter
 * gets it, and none of them can skip it.
 *
 * @param sequence        monotonic within a game; the ordering key, supplied by
 *                        whoever fetched the snapshot rather than by the upstream
 * @param attribution     where these numbers came from, in one sentence a reader
 *                        sees. Required, because "numbers and text only,
 *                        attributed" is a project invariant and an unattributed
 *                        snapshot is one nobody can defend later
 * @param provenance      the sourcing record behind those numbers, which is a
 *                        different question from {@code attribution} and the one
 *                        with teeth. Attribution is a credit line; this says who
 *                        read what, where, and when, and it is what makes
 *                        "self-sourced" falsifiable rather than merely asserted.
 *                        At least one entry, ids unique. See ADR 0015
 * @param sourcedBy       the id of the {@link Provenance} covering every fact
 *                        that does not name its own. A default rather than a
 *                        required per-fact field because the realistic bundle is
 *                        one sitting, one screen, one reader — asking for 2 700
 *                        identical strings would produce 2 700 copy-pastes and
 *                        no more truth
 * @param factProvenance  the exceptions: {@link FactRef} to provenance id, for
 *                        the facts that did <em>not</em> come from
 *                        {@code sourcedBy}. Overrides only; a fact absent here
 *                        is covered by the default, so this map is small on
 *                        purpose and every entry in it is a deliberate claim
 * @param progressKinds   names for the progress kinds this bundle's upgrades
 *                        cost and its fodder rules feed. Not facts and not
 *                        required — see {@link ProgressKind} — so they appear in
 *                        no {@code factRef} and a kind nobody names renders as
 *                        its slug
 * @param sections        the headings the game's screens group an entity's
 *                        tracks under, in the order it shows them. Not facts:
 *                        each step says which heading its track is under, and
 *                        that is the reading; this is the order, and it may hold
 *                        the bundle's own word for a group the game leaves
 *                        untitled (ADR 0032). Empty for every bundle before
 *                        sequence 11
 * @param words           what to call the bundle's own keys — a measure, an item
 *                        category, an entity kind. Not facts, like progress kind
 *                        names (ADR 0033), and every one has to name a key the
 *                        bundle uses. Empty for every bundle before sequence 13
 */
public record GameDataBundle(
        Game game,
        long sequence,
        String label,
        String attribution,
        List<Provenance> provenance,
        String sourcedBy,
        Map<String, String> factProvenance,
        List<Item> items,
        List<Source> sources,
        List<Sink> sinks,
        List<BannerModel> banners,
        List<Entity> entities,
        List<ProgressKind> progressKinds,
        List<String> sections,
        List<Word> words
) {

    /** A bundle from before its keys could be given words. */
    public GameDataBundle(
            Game game,
            long sequence,
            String label,
            String attribution,
            List<Provenance> provenance,
            String sourcedBy,
            Map<String, String> factProvenance,
            List<Item> items,
            List<Source> sources,
            List<Sink> sinks,
            List<BannerModel> banners,
            List<Entity> entities,
            List<ProgressKind> progressKinds,
            List<String> sections) {
        this(game, sequence, label, attribution, provenance, sourcedBy, factProvenance,
                items, sources, sinks, banners, entities, progressKinds, sections, List.of());
    }

    /** A bundle from before a step could name the section its track sits under. */
    public GameDataBundle(
            Game game,
            long sequence,
            String label,
            String attribution,
            List<Provenance> provenance,
            String sourcedBy,
            Map<String, String> factProvenance,
            List<Item> items,
            List<Source> sources,
            List<Sink> sinks,
            List<BannerModel> banners,
            List<Entity> entities,
            List<ProgressKind> progressKinds) {
        this(game, sequence, label, attribution, provenance, sourcedBy, factProvenance,
                items, sources, sinks, banners, entities, progressKinds, List.of(), List.of());
    }

    /** A bundle from before a progress kind could be named. */
    public GameDataBundle(
            Game game,
            long sequence,
            String label,
            String attribution,
            List<Provenance> provenance,
            String sourcedBy,
            Map<String, String> factProvenance,
            List<Item> items,
            List<Source> sources,
            List<Sink> sinks,
            List<BannerModel> banners,
            List<Entity> entities) {
        this(game, sequence, label, attribution, provenance, sourcedBy, factProvenance,
                items, sources, sinks, banners, entities, List.of(), List.of(), List.of());
    }

    public GameDataBundle {
        if (game == null) throw new BundleFormatException("game is required");
        if (sequence < 0) throw new BundleFormatException("sequence must not be negative");
        if (attribution == null || attribution.isBlank()) {
            throw new BundleFormatException("attribution is required: an unattributed snapshot is not publishable");
        }
        label = label == null || label.isBlank() ? String.valueOf(sequence) : label;

        items = List.copyOf(items);
        sources = List.copyOf(sources);
        sinks = List.copyOf(sinks);
        banners = List.copyOf(banners);
        entities = List.copyOf(entities);
        sections = List.copyOf(sections);
        // Sorted, like GameDefinition's: names have no order, and a bundle that
        // lists them in reading order has to equal the one the database returns.
        progressKinds = progressKinds.stream()
                .sorted(Comparator.comparing(ProgressKind::kind))
                .toList();
        words = GameDefinition.sortedWords(words);
        // Silence is given a meaning rather than left as a hole: a bundle that
        // declares nothing is a bundle whose facts came from nowhere anybody
        // recorded, which is not first-hand and so cannot be published. See
        // Provenance.Origin.UNRECORDED for why this is not simply a required
        // field.
        if (provenance.isEmpty() && (sourcedBy == null || sourcedBy.isBlank())) {
            provenance = List.of(Provenance.unrecorded());
            sourcedBy = Provenance.UNRECORDED_ID;
        }
        provenance = List.copyOf(provenance);
        factProvenance = Map.copyOf(factProvenance);

        // Checked here, before a connection is opened, because the composite
        // foreign keys will reject a dangling reference with a message naming a
        // constraint. "stage_drop_item_fk" is not something the person
        // approving a publish can act on; "stage '1-1' drops unknown item
        // 'sulfr'" is. See ADR 0008.
        validate(items, sources, sinks, entities, banners, progressKinds);
        validateLabels(sinks, sections);
        validateWords(words, items, sources, entities);
        validateProvenance(provenance, sourcedBy, factProvenance,
                refsOf(items, sources, sinks, banners, entities));
    }

    /**
     * The same data as a {@link GameDefinition}, as of the moment a human
     * approved it.
     *
     * @param approvedAt when the approval happened, never when the fetch did
     */
    public GameDefinition definitionApprovedAt(Instant approvedAt) {
        return new GameDefinition(
                game,
                new GameDataVersion(game.id(), sequence, label, approvedAt, attribution),
                items,
                sources,
                sinks,
                banners,
                entities,
                progressKinds,
                sections,
                words);
    }

    /**
     * Every word names a key some fact uses, and no key has two: a word for a
     * measure no bar asks for is a typo that would rename nothing and fail
     * nowhere, which is the check {@link ProgressKind} names get for the same
     * reason. See {@link Word}.
     */
    private static void validateWords(List<Word> words, List<Item> items, List<Source> sources,
            List<Entity> entities) {
        Map<Word.Subject, Set<String>> inUse = new HashMap<>();
        for (Word.Subject subject : Word.Subject.values()) inUse.put(subject, new LinkedHashSet<>());
        items.forEach(item -> inUse.get(Word.Subject.CATEGORY).add(item.category()));
        entities.forEach(entity -> inUse.get(Word.Subject.ENTITY_KIND).add(entity.kind()));
        for (Source source : sources) {
            if (source instanceof Reward reward && reward.requires() != null) {
                inUse.get(Word.Subject.MEASURE).add(reward.requires().measure());
            }
        }

        List<String> problems = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Word word : words) {
            String spelled = word.subject().spelled() + " '" + word.key() + "'";
            if (!seen.add(spelled)) {
                problems.add(spelled + " has two words");
            } else if (!inUse.get(word.subject()).contains(word.key())) {
                problems.add(spelled + " has a word but nothing in this bundle uses it; in use: "
                        + inUse.get(word.subject()));
            }
        }
        if (!problems.isEmpty()) {
            throw new BundleFormatException(String.join(System.lineSeparator(), problems));
        }
    }

    // ── What the game calls things ──────────────────────────────────────────

    /**
     * Two rules, each refusing a label that would render as something other
     * than what the author wrote (ADR 0032).
     *
     * <p>A section a step names must be declared, and every declared section
     * must be named by a step: the declaration is only an order, so a typo on
     * either side would put a track under a heading of its own, or leave a
     * heading nothing sits under, and the page would show it without complaint.
     *
     * <p>One state has one name. Two steps of an entity may both name a state —
     * one arriving at it and one leaving it, or two prices of one step (ADR
     * 0021) — and when they disagree there is no right one to show.
     */
    private static void validateLabels(List<Sink> sinks, List<String> sections) {
        List<String> problems = new ArrayList<>();
        Set<String> declared = new LinkedHashSet<>();
        for (String section : sections) {
            if (section == null || section.isBlank()) {
                throw new BundleFormatException("sections must not hold a blank heading");
            }
            if (!declared.add(section)) {
                throw new BundleFormatException("duplicate section '" + section + "'");
            }
        }

        Set<String> used = new LinkedHashSet<>();
        Map<String, String> nameOf = new HashMap<>();
        for (Sink sink : sinks) {
            if (!(sink instanceof Upgrade upgrade)) continue;
            Upgrade.Labels labels = upgrade.labels();
            if (labels.section() != null) {
                used.add(labels.section());
                if (!declared.contains(labels.section())) {
                    problems.add("upgrade '" + upgrade.id() + "' sits under section '" + labels.section()
                            + "', which sections does not declare; declared: " + declared);
                }
            }
            name(nameOf, upgrade, upgrade.fromState(), labels.fromName(), problems);
            name(nameOf, upgrade, upgrade.toState(), labels.toName(), problems);
        }
        for (String section : declared) {
            if (!used.contains(section)) {
                problems.add("section '" + section + "' is declared but no upgrade sits under it");
            }
        }
        if (!problems.isEmpty()) {
            throw new BundleFormatException(String.join(System.lineSeparator(), problems));
        }
    }

    private static void name(Map<String, String> nameOf, Upgrade upgrade, String state, String name,
            List<String> problems) {
        if (name == null) return;
        String key = upgrade.entity().value() + " " + state;
        String earlier = nameOf.putIfAbsent(key, name);
        if (earlier != null && !earlier.equals(name)) {
            problems.add("state '" + state + "' of '" + upgrade.entity().value() + "' is called both '"
                    + earlier + "' and '" + name + "' (upgrade '" + upgrade.id() + "')");
        }
    }

    // ── Provenance ──────────────────────────────────────────────────────────

    /**
     * Every fact this bundle declares, in a stable order.
     *
     * <p>"Fact" here means one declared row — a stage, an item, a character —
     * not one number inside it. That granularity is a deliberate stop: ADR 0015
     * asks for provenance "per fact or at worst per source", and a stage's
     * energy cost and its drop table are read off the same screen in the same
     * sitting by the same person. Splitting them would multiply the typing
     * without splitting the claim.
     */
    public List<String> factRefs() {
        return refsOf(items, sources, sinks, banners, entities);
    }

    /**
     * Where one fact came from.
     *
     * <p>Falls back to {@link #sourcedBy} rather than to nothing: a fact with no
     * override is not a fact with no provenance, and returning empty here would
     * make the common case look like the unsourced one.
     *
     * @throws BundleFormatException if the ref names nothing this bundle declares
     */
    public Provenance provenanceOf(String factRef) {
        String id = factProvenance.getOrDefault(factRef, sourcedBy);
        return provenance.stream()
                .filter(candidate -> candidate.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new BundleFormatException(
                        "no provenance declared for '" + factRef + "'"));
    }

    /**
     * The facts this bundle is not entitled to publish, sorted.
     *
     * <p>The whole reason the provenance fields exist. ADR 0015's decision is
     * unenforceable as prose — a laundered number and a sourced one are the same
     * bytes — so the question has to be answerable mechanically, and the answer
     * has to name names rather than return a boolean. An operator told "this
     * bundle is not first-hand" can do nothing; one told "stage:1-1 came from
     * kornblume-3.5" can go and read the stage screen.
     */
    public List<String> secondHandFacts() {
        return factRefs().stream()
                .filter(ref -> !provenanceOf(ref).isFirstHand())
                .sorted()
                .toList();
    }

    /** True when every fact here is this project's to publish. */
    public boolean isFirstHand() {
        return secondHandFacts().isEmpty();
    }

    /** How many facts each declared provenance actually covers, declaration order. */
    public Map<Provenance, Long> factsByProvenance() {
        Map<Provenance, Long> counts = new LinkedHashMap<>();
        provenance.forEach(entry -> counts.put(entry, 0L));
        factRefs().forEach(ref -> counts.merge(provenanceOf(ref), 1L, Long::sum));
        return counts;
    }

    private static List<String> refsOf(
            List<Item> items, List<Source> sources, List<Sink> sinks,
            List<BannerModel> banners, List<Entity> entities) {

        List<String> refs = new ArrayList<>();
        items.forEach(item -> refs.add(FactRef.of(item)));
        entities.forEach(entity -> refs.add(FactRef.of(entity)));
        sources.forEach(source -> refs.add(FactRef.of(source)));
        sinks.forEach(sink -> refs.add(FactRef.of(sink)));
        banners.forEach(banner -> refs.add(FactRef.of(banner)));
        return refs;
    }

    private static void validateProvenance(
            List<Provenance> provenance, String sourcedBy,
            Map<String, String> factProvenance, List<String> factRefs) {

        Set<String> ids = new LinkedHashSet<>();
        provenance.forEach(entry -> {
            if (!ids.add(entry.id())) {
                throw new BundleFormatException("duplicate provenance '" + entry.id() + "'");
            }
            // The id a silent bundle is given. A declared entry reusing it could
            // give "unrecorded" a first-hand origin, and then the one slug whose
            // meaning is fixed would be the one meaning nothing in particular.
            if (entry.id().equals(Provenance.UNRECORDED_ID)
                    && entry.origin() != Provenance.Origin.UNRECORDED) {
                throw new BundleFormatException(
                        "'" + Provenance.UNRECORDED_ID + "' is reserved for a bundle that declares"
                                + " no provenance and cannot be redefined as "
                                + entry.origin());
            }
        });
        if (sourcedBy == null || sourcedBy.isBlank()) {
            // Reachable only when the bundle declared provenance and then did not
            // say which one covers the facts. Declaring some and defaulting to
            // none is the shape most likely to be a half-finished edit, so it is
            // refused rather than quietly treated as declaring none at all.
            throw new BundleFormatException(
                    "this bundle declares provenance " + ids + " but no sourcedBy naming which of"
                            + " them covers a fact that does not override it");
        }
        if (!ids.contains(sourcedBy)) {
            throw new BundleFormatException(
                    "sourcedBy names undeclared provenance '" + sourcedBy + "'; declared: " + ids);
        }

        // An override pointing at nothing is the failure mode that matters: it
        // reads as a deliberate exception and behaves as the default, so the
        // bundle would claim first-hand sourcing for a fact somebody had
        // explicitly marked otherwise.
        Set<String> known = new LinkedHashSet<>(factRefs);
        List<String> broken = new ArrayList<>();
        factProvenance.forEach((ref, id) -> {
            if (!known.contains(ref)) {
                broken.add("factProvenance names '" + ref + "', which this bundle does not declare");
            }
            if (!ids.contains(id)) {
                broken.add("fact '" + ref + "' names undeclared provenance '" + id + "'");
            }
        });
        if (!broken.isEmpty()) {
            throw new BundleFormatException(String.join(System.lineSeparator(), broken));
        }
    }

    private static void validate(
            List<Item> items, List<Source> sources, List<Sink> sinks, List<Entity> entities,
            List<BannerModel> banners, List<ProgressKind> progressKinds) {

        Set<ItemId> knownItems = new LinkedHashSet<>();
        for (Item item : items) {
            if (!knownItems.add(item.id())) {
                throw new BundleFormatException("duplicate item '" + item.id() + "'");
            }
        }
        Set<EntityId> knownEntities = new LinkedHashSet<>();
        for (Entity entity : entities) {
            if (!knownEntities.add(entity.id())) {
                throw new BundleFormatException("duplicate entity '" + entity.id() + "'");
            }
        }

        // Slugs are unique per table, not across all of them: the schema says so
        // deliberately, and nothing in the domain requires a Source.id() to be
        // unique across the four source shapes. So each kind is checked against
        // its own kind and no further.
        Map<Class<?>, Set<String>> slugsByKind = new LinkedHashMap<>();
        sources.forEach(source -> uniqueSlug(slugsByKind, source.getClass(), source.id()));
        sinks.forEach(sink -> uniqueSlug(slugsByKind, sink.getClass(), sink.id()));

        Set<String> skillSlugs = new LinkedHashSet<>();
        Set<String> talentSlugs = new LinkedHashSet<>();
        for (Entity entity : entities) {
            // Unique per *version*, not per entity: gamedata.skill and
            // gamedata.talent both carry UNIQUE (version_id, slug).
            entity.skills().forEach(skill -> {
                if (!skillSlugs.add(skill.id())) {
                    throw new BundleFormatException("duplicate skill '" + skill.id() + "'");
                }
            });
            entity.talents().forEach(talent -> {
                if (!talentSlugs.add(talent.id())) {
                    throw new BundleFormatException("duplicate talent '" + talent.id() + "'");
                }
            });
        }

        List<String> dangling = new ArrayList<>();
        for (Source source : sources) {
            switch (source) {
                case Stage stage -> stage.drops().forEach(drop ->
                        require(knownItems, drop.item(), dangling, "stage '" + stage.id() + "' drops"));
                case Craft craft -> {
                    requireStacks(knownItems, craft.consumes(), dangling, "craft '" + craft.id() + "' consumes");
                    requireStacks(knownItems, craft.produces(), dangling, "craft '" + craft.id() + "' produces");
                }
                case Shop shop -> {
                    require(knownItems, shop.currency(), dangling, "shop '" + shop.id() + "' is priced in");
                    require(knownItems, shop.offer().item(), dangling, "shop '" + shop.id() + "' offers");
                }
                case Reward reward ->
                        requireStacks(knownItems, reward.grants(), dangling, "reward '" + reward.id() + "' grants");
            }
        }
        for (Sink sink : sinks) {
            requireStacks(knownItems, sink.costs(), dangling, sinkKind(sink) + " '" + sink.id() + "' costs");
            if (sink instanceof Upgrade upgrade && !knownEntities.contains(upgrade.entity())) {
                dangling.add("upgrade '" + upgrade.id() + "' advances unknown entity '" + upgrade.entity() + "'");
            }
        }
        for (BannerModel banner : banners) {
            if (banner.pullPrice() != null) {
                require(knownItems, banner.pullPrice().currency(), dangling,
                        "banner '" + banner.id().value() + "' prices a pull in");
            }
        }
        // A gate naming a state no upgrade touches cannot be met by any plan, and
        // would be reported at solve time as a goal nobody can reach. It is a typo
        // in the bundle, and the bundle is where to say so.
        Map<EntityId, Set<String>> statesOf = new HashMap<>();
        for (Sink sink : sinks) {
            if (sink instanceof Upgrade upgrade) {
                Set<String> states = statesOf.computeIfAbsent(upgrade.entity(), e -> new HashSet<>());
                states.add(upgrade.fromState());
                states.add(upgrade.toState());
            }
        }
        for (Sink sink : sinks) {
            if (!(sink instanceof Upgrade upgrade)) continue;
            for (String required : upgrade.requires()) {
                if (!statesOf.getOrDefault(upgrade.entity(), Set.of()).contains(required)) {
                    dangling.add("upgrade '" + upgrade.id() + "' requires state '" + required
                            + "', which no upgrade of '" + upgrade.entity() + "' reaches or leaves");
                }
            }
        }
        for (Entity entity : entities) {
            for (Skill skill : entity.skills()) {
                for (Skill.Rank rank : skill.ranks()) {
                    requireStacks(knownItems, rank.upgradeCost(), dangling,
                            "skill '" + skill.id() + "' rank " + rank.rank() + " costs");
                }
            }
        }

        // A name for a kind nothing names is a typo, and a silent one: the name
        // is never looked up, so the line it was written for goes on rendering
        // its slug and the bundle looks like it fixed something. The opposite —
        // a kind with no name — is legal and always will be, because every
        // version published before names existed is exactly that.
        Set<String> kindsInUse = new LinkedHashSet<>();
        for (Sink sink : sinks) {
            switch (sink) {
                case Upgrade upgrade -> upgrade.progress().forEach(p -> kindsInUse.add(p.kind()));
                case Fodder fodder -> {
                    if (fodder.progress() != null) kindsInUse.add(fodder.progress());
                }
            }
        }
        Set<String> named = new LinkedHashSet<>();
        for (ProgressKind kind : progressKinds) {
            if (!named.add(kind.kind())) {
                throw new BundleFormatException("duplicate progress kind '" + kind.kind() + "'");
            }
            if (!kindsInUse.contains(kind.kind())) {
                dangling.add("progress kind '" + kind.kind() + "' is named but no upgrade costs it"
                        + " and no fodder rule feeds it; in use: " + kindsInUse);
            }
        }

        if (!dangling.isEmpty()) {
            throw new BundleFormatException(
                    "bundle references " + dangling.size() + " thing(s) it does not define:"
                            + System.lineSeparator() + String.join(System.lineSeparator(), dangling));
        }
    }

    private static void uniqueSlug(Map<Class<?>, Set<String>> byKind, Class<?> kind, String slug) {
        if (!byKind.computeIfAbsent(kind, k -> new LinkedHashSet<>()).add(slug)) {
            throw new BundleFormatException(
                    "duplicate " + kind.getSimpleName().toLowerCase(Locale.ROOT) + " '" + slug + "'");
        }
    }

    private static void requireStacks(
            Set<ItemId> known, List<ItemStack> stacks, List<String> dangling, String what) {
        stacks.forEach(stack -> require(known, stack.item(), dangling, what));
    }

    private static void require(Set<ItemId> known, ItemId item, List<String> dangling, String what) {
        if (!known.contains(item)) {
            dangling.add(what + " unknown item '" + item + "'");
        }
    }

    /** Only for the message; the sealed hierarchy is what actually matters. */
    private static String sinkKind(Sink sink) {
        return sink instanceof Fodder ? "fodder" : "upgrade";
    }
}
