package io.stormalmanac.common.id;

public record PlanId(String value) implements Identifier {
    public PlanId {
        value = Identifier.require(value, "PlanId");
    }

    public static PlanId of(String value) {
        return new PlanId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
