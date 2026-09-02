package io.stormalmanac.common.id;

public record AccountId(String value) implements Identifier {
    public AccountId {
        value = Identifier.require(value, "AccountId");
    }

    public static AccountId of(String value) {
        return new AccountId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
