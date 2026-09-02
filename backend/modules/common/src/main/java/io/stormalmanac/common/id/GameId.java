package io.stormalmanac.common.id;

public record GameId(String value) implements Identifier {
    public GameId {
        value = Identifier.require(value, "GameId");
    }

    public static GameId of(String value) {
        return new GameId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
