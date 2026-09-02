package io.stormalmanac.common.id;

public record ProfileId(String value) implements Identifier {
    public ProfileId {
        value = Identifier.require(value, "ProfileId");
    }

    public static ProfileId of(String value) {
        return new ProfileId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
