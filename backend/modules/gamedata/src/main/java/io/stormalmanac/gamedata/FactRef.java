package io.stormalmanac.gamedata;

import io.stormalmanac.gamedata.banner.BannerModel;
import io.stormalmanac.gamedata.catalog.Entity;

/**
 * How one declared thing in a bundle is named when something outside the bundle
 * has to point at it — today, {@link Provenance}.
 *
 * <p>The form is {@code kind:slug}: {@code stage:1-1}, {@code item:silver-ore},
 * {@code entity:sotheby}. The kind prefix is not decoration. Slugs are unique
 * per table and deliberately not across all of them — the schema says so, and
 * nothing in the domain requires a {@code Source.id()} to be distinct from a
 * {@code Sink.id()} — so a bare slug is not an identifier and a map keyed by one
 * would silently collapse two facts into one.
 *
 * <p>Kept as strings rather than a record because the value's whole job is to be
 * a stable map key that also survives a round trip through JSON and a
 * {@code TEXT} column without a codec on either side. A person reading a bundle
 * or a database row should be able to see what it points at without one.
 */
public final class FactRef {

    private FactRef() {}

    public static String of(Item item) {
        return "item:" + item.id().value();
    }

    public static String of(Entity entity) {
        return "entity:" + entity.id().value();
    }

    public static String of(BannerModel banner) {
        return "banner:" + banner.id().value();
    }

    /**
     * A source's reference, discriminated by which of the four shapes it is.
     *
     * <p>Switching over the sealed hierarchy rather than reading a {@code kind}
     * field means a fifth source shape cannot be added without this failing to
     * compile — which is the point of sealing them.
     */
    public static String of(Source source) {
        return switch (source) {
            case Stage stage -> "stage:" + stage.id();
            case Craft craft -> "craft:" + craft.id();
            case Shop shop -> "shop:" + shop.id();
            case Reward reward -> "reward:" + reward.id();
        };
    }

    public static String of(Sink sink) {
        return switch (sink) {
            case Upgrade upgrade -> "upgrade:" + upgrade.id();
            case Fodder fodder -> "fodder:" + fodder.id();
        };
    }
}
