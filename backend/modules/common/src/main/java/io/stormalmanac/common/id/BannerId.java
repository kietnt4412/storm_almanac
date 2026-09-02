package io.stormalmanac.common.id;

public record BannerId(String value) implements Identifier {
    public BannerId {
        value = Identifier.require(value, "BannerId");
    }

    public static BannerId of(String value) {
        return new BannerId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
