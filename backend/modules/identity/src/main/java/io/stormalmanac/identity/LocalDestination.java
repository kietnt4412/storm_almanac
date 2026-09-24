package io.stormalmanac.identity;

/**
 * Where a sign-in may send the reader afterwards: a path on this application,
 * or {@code /}.
 *
 * <p>An unchecked destination is an open redirect — a link to this site's
 * sign-in that lands the reader, freshly signed in and trusting the page, on
 * somebody else's. Both sign-ins take one, the development one and the
 * provider's ({@link ReturnAfterSignIn}), and this is one class rather than a
 * check in each so the two cannot disagree about what counts as local. It has
 * no Spring in it on purpose: the development sign-in does not carry the OAuth
 * client library, and must not need it to ask this question.
 */
public final class LocalDestination {

    private LocalDestination() {}

    /**
     * {@code then} if it stays on this origin, otherwise {@code /}.
     *
     * <p>{@code //host} and {@code /\host} are protocol-relative and leave the
     * origin, so a leading slash alone is not enough; a control character is
     * refused because the value becomes a header.
     */
    public static String of(String then) {
        boolean local = then != null
                && then.startsWith("/")
                && !then.startsWith("//")
                && !then.startsWith("/\\")
                && then.chars().noneMatch(Character::isISOControl);
        return local ? then : "/";
    }
}
