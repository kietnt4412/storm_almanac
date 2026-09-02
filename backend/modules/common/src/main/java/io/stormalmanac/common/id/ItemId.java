package io.stormalmanac.common.id;

public record ItemId(String value) implements Identifier {
    public ItemId {
        value = Identifier.require(value, "ItemId");
    }

    public static ItemId of(String value) {
        return new ItemId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
