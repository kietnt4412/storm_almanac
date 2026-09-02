package io.stormalmanac.common.id;

public record StageId(String value) implements Identifier {
    public StageId {
        value = Identifier.require(value, "StageId");
    }

    public static StageId of(String value) {
        return new StageId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
