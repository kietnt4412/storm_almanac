package io.stormalmanac.common.id;

public record EntityId(String value) implements Identifier {
    public EntityId {
        value = Identifier.require(value, "EntityId");
    }

    public static EntityId of(String value) {
        return new EntityId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
