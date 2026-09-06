package io.stormalmanac.adapters.r1999;

import io.stormalmanac.gamedata.ingest.BundleFormatException;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The upstream's primary key is an English display name. Ours is a stable slug.
 * This is the whole of the translation, and it is the riskiest twenty lines in
 * the adapter.
 *
 * <p>Why it is risky, from {@code docs/prior-art.md} §4.2: a localisation fix
 * upstream renames a thing and silently orphans an id; two things in different
 * categories share a display name and collide into one row; a typo invents a
 * material that no stage drops. All three are quiet, and all three corrupt a
 * plan rather than crashing it.
 *
 * <p>So this class is built to be loud instead:
 *
 * <ul>
 *   <li>A name is <em>declared</em> before it can be <em>referenced</em>.
 *       {@link #declare} is called once per row in the upstream's own catalogue
 *       files; {@link #reference} is called everywhere a name is cited. A
 *       reference to something never declared is a refused ingest naming the
 *       citing record, not a new item conjured into existence.
 *   <li>Two different names that slug the same way are refused at declaration,
 *       with both names in the message. Slugging is lossy and the loss has to
 *       surface here rather than as one row overwriting another.
 *   <li>The mapping is kept, and {@link #mapping} hands it back so the caller
 *       can write it into the bundle. It is not recomputed from nothing on the
 *       next ingest: the previous published version carries every display name
 *       beside its slug, so a rename shows up in the patch diff as a removal
 *       plus an addition — which is exactly the loud failure §4.2 asks for.
 * </ul>
 */
final class Names {

    private final String kind;
    private final Map<String, String> slugByName = new LinkedHashMap<>();
    private final Map<String, String> nameBySlug = new LinkedHashMap<>();
    private final Set<String> referencedButNotDeclared = new LinkedHashSet<>();
    private final Map<String, String> whoReferenced = new TreeMap<>();

    Names(String kind) {
        this.kind = kind;
    }

    /**
     * Registers a name the upstream defines, and returns its slug.
     *
     * @throws BundleFormatException if another name already slugs to the same thing
     */
    String declare(String name) {
        String existing = slugByName.get(name);
        if (existing != null) return existing;

        String slug = slug(name);
        String taken = nameBySlug.get(slug);
        if (taken != null) {
            throw new BundleFormatException(
                    "two upstream " + kind + " names collapse to the same id '" + slug + "': \""
                            + taken + "\" and \"" + name + "\". Slugging is lossy; this needs an"
                            + " explicit override rather than one row silently overwriting the other");
        }
        slugByName.put(name, slug);
        nameBySlug.put(slug, name);
        return slug;
    }

    /**
     * Resolves a name the upstream cites. Unknown names are collected rather
     * than thrown one at a time — somebody fixing a snapshot wants the whole
     * list, and there is no point failing on the first of forty.
     *
     * @param citedBy what referenced it, so the message says where to look
     */
    String reference(String name, String citedBy) {
        String slug = slugByName.get(name);
        if (slug != null) return slug;

        referencedButNotDeclared.add(name);
        whoReferenced.putIfAbsent(name, citedBy);
        // A placeholder so the rest of the snapshot keeps converting and the
        // caller can report every unknown name at once. It never reaches the
        // database: verify() throws before the bundle is built.
        return slug(name);
    }

    /**
     * Refuses the whole snapshot if anything cited a name the upstream never
     * defined. Called once, after conversion, before a bundle exists.
     */
    void verify() {
        if (referencedButNotDeclared.isEmpty()) return;

        StringBuilder message = new StringBuilder(
                "upstream cites " + referencedButNotDeclared.size() + " " + kind
                        + " name(s) it never defines — a rename upstream, or a typo:");
        whoReferenced.forEach((name, citedBy) ->
                message.append(System.lineSeparator())
                        .append("  \"").append(name).append("\" cited by ").append(citedBy));
        throw new BundleFormatException(message.toString());
    }

    /** Every declared name against the slug it became, in declaration order. */
    Map<String, String> mapping() {
        return Map.copyOf(slugByName);
    }

    /**
     * Lower-case, hyphen-separated: {@code "Spell of Banishing"} becomes
     * {@code "spell-of-banishing"}.
     *
     * <p>Accents are decomposed and their marks dropped, so {@code "Café Crème"}
     * becomes {@code "cafe-creme"} — the reading a Latin-alphabet name is
     * expected to have. What survives that is kept whatever script it is in:
     * the rule is "letters and digits stay, everything else becomes a hyphen,
     * runs collapse", by Unicode's idea of a letter rather than ASCII's.
     *
     * <p>That last part is not hypothetical and is <em>why</em> the rule reads
     * this way. Folding to ASCII first looked obviously right until a real
     * snapshot turned up a character named Зима, which folds to nothing at all
     * and refused the whole ingest. An id in the script its name is written in
     * is stable, unique and readable to somebody; an id invented from a hash
     * would be none of those. Ids are {@code TEXT} in the schema and
     * {@code Identifier} asks only that they not be blank, so nothing downstream
     * had an opinion — the ASCII assumption lived only here.
     */
    static String slug(String name) {
        String withoutAccents = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        String slug = withoutAccents.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]+", "-")
                .replaceAll("^-+|-+$", "");
        if (slug.isEmpty()) {
            throw new BundleFormatException(
                    "upstream name \"" + name + "\" has no characters an id can be made from");
        }
        return slug;
    }
}
